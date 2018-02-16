package com.boshanlu.mobile.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.widget.InputValidDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends BaseActivity implements InputValidDialog.OnInputValidListener {

    public int answerSelect = -1;
    boolean err = true;
    private EditText password1, password2, password3;
    private TextInputLayout passwordLayout;
    private List<String> list = new ArrayList<>();
    private EditText edAnswer;
    private ProgressDialog dialog;
    private String emailNew;
    private boolean isLoad = false;//是否已经加载

    // 验证码相关
    private boolean haveValid = false;
    private String seccodehash = null;
    private String validValue = null; //验证码输入值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initToolBar(true, "修改密码");
        password1 = findViewById(R.id.old_pass);
        password2 = findViewById(R.id.new_pass);
        password3 = findViewById(R.id.new_pass2);

        addToolbarMenu(R.drawable.ic_check_24dp).setOnClickListener(view -> {
            submit();
        });

        passwordLayout = findViewById(R.id.new_pass_c2);
        edAnswer = findViewById(R.id.anwser_text);

        list.add("保持原有的安全提问和答案");
        list.add("无安全提问"); //0
        list.add("母亲的名字");
        list.add("爷爷的名字");
        list.add("父亲出生的城市");
        list.add("您其中一位老师的名字");
        list.add("您个人计算机的型号");
        list.add("您最喜欢的餐馆名称");
        list.add("驾驶执照最后四位数字");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner questionSpinner = findViewById(R.id.anwser_select);
        questionSpinner.setAdapter(spinnerAdapter);
        questionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                answerSelect = i;
                if (i == 0 || i == 1) {
                    edAnswer.setVisibility(View.GONE);
                } else {
                    edAnswer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        password3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkInput();
            }
        });

        password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkInput();
            }
        });

        loadData(false);
    }

    private void loadData(boolean fromSubmit) {
        if (isLoad) return;
        HttpUtil.get("home.php?mod=spacecp&ac=profile&op=password&mobile=2", new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                //<input type="text" name="emailnew" id="emailnew" value="2351386755@qq.com" class="px">
                Document document = Jsoup.parse(res);
                Elements es = document.select("#emailnew");

                //updateseccode('cSiB0UMM', '<table cellspacing="0" cellpadding="0" class="tfm"><tr><th><sec></th><td><sec><p class="d"><sec></p></td></tr></table>', 'home::spacecp')
                int index = res.indexOf("updateseccode");
                if (index > 0) {
                    haveValid = true;
                    int start = res.indexOf("'", index) + 1;
                    int end = res.indexOf("'", start);
                    seccodehash = res.substring(start, end);
                }

                if (es.size() > 0) {
                    emailNew = es.get(0).attr("value");
                    if (!TextUtils.isEmpty(emailNew)) {
                        isLoad = true;
                        return;
                    }
                }

                if (fromSubmit) {
                    showToast("提交失败,请重试");
                }
            }

            @Override
            public void onFailure(Throwable e) {
                if (fromSubmit) {
                    showToast("网络错误:" + e.getMessage());
                }
            }
        });
    }

    private void checkInput() {
        String newPass = password2.getText().toString();
        String newPass2 = password3.getText().toString();
        if (TextUtils.isEmpty(newPass)) {
            err = true;
            passwordLayout.setError("新密码不能为空");
            return;
        }

        if (!Objects.equals(newPass, newPass2)) {
            passwordLayout.setError("两次输入的密码不一致");
            err = true;
        } else if (newPass.length() < 6) {
            passwordLayout.setError("密码太短");
            err = true;
        } else if (!checkSecurity(newPass)) {
            passwordLayout.setError("密码中必须包含数字、小写字母");
            err = true;
        } else if (answerSelect > 0 && TextUtils.isEmpty(edAnswer.getText().toString())) {
            passwordLayout.setError("安全提问不能为空");
            err = true;
        } else {
            passwordLayout.setError(null);
            err = false;
        }
    }


    private void submit() {
        checkInput();
        if (err) return;

        if (haveValid && TextUtils.isEmpty(validValue)) {
            showInputValidDialog();
            return;
        }

        if (!isLoad) loadData(true);

        String old = password1.getText().toString();
        String new1 = password2.getText().toString();
        String new2 = password3.getText().toString();

        if (TextUtils.isEmpty(old) ||
                TextUtils.isEmpty(new1) || TextUtils.isEmpty(new2)) {
            Snackbar.make(findViewById(R.id.content), "输入不能为空",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!Objects.equals(new1, new2)) {
            Snackbar.make(findViewById(R.id.content), "两次输入的密码不一致",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("提交中，请稍后......");
        dialog.show();

        Map<String, String> ps = new HashMap<>();
        //ps.put("formhash", "711c5dd9");
        ps.put("oldpassword", old);
        ps.put("newpassword", new1);
        ps.put("newpassword2", new1);
        ps.put("emailnew", emailNew);

        if (haveValid) { //是否有验证码
            ps.put("seccodehash", seccodehash);
            ps.put("seccodeverify", validValue);
        }

        if (answerSelect <= 0) {
            ps.put("questionidnew", "");
            ps.put("answernew", "");
        } else {
            ps.put("questionidnew", String.valueOf(answerSelect - 1));
            ps.put("answernew", edAnswer.getText().toString());
        }

        ps.put("pwdsubmit", "true");
        ps.put("passwordsubmit", "true");


        HttpUtil.getClient().setConnectionTimeout(20000);
        HttpUtil.getClient().setReadTimeout(20000);

        HttpUtil.post("home.php?mod=spacecp&ac=profile&mobile=2", ps, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                dialog.dismiss();
                String res = new String(response);
                if (res.contains("个人资料保存成功")) {
                    showLongToast("修改密码成功 请重新登陆");
                    //DataManager.cleanApplicationData(ChangePasswordActivity.this);
                    App.setUid(ChangePasswordActivity.this);
                    HttpUtil.exit();
                    finish();
                    startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                } else if (res.contains("class=\"jump_c\"")) {
                    int start = res.indexOf("<p>", res.indexOf("class=\"jump_c\"")) + 3;
                    int end = res.indexOf("</p>", start);
                    String reason = res.substring(start, end);
                    if ("抱歉，验证码填写错误".equals(reason)) {
                        showInputValidDialog();
                    }
                    Toast.makeText(ChangePasswordActivity.this, reason, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "我也不知道出了什么问题", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Toast.makeText(ChangePasswordActivity.this, "连接超时,可能修改成功",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFinish() {
                HttpUtil.getClient().setConnectionTimeout(8000);
                HttpUtil.getClient().setReadTimeout(8000);
            }
        });
    }

    private boolean checkSecurity(String pwd) {
        String p = "", p2 = "";
        Pattern pa1 = Pattern.compile("\\d+");
        Pattern pa2 = Pattern.compile("[a-z]+");
        Matcher m1 = pa1.matcher(pwd);
        Matcher m2 = pa2.matcher(pwd);
        if (m1.find()) {
            p = m1.group();
        } else if (m2.find()) {
            p2 = m2.group();
        }

        return !(p.length() == pwd.length() || p2.length() == pwd.length());
    }


    //显示填写验证码框子
    private void showInputValidDialog() {
        InputValidDialog dialog = InputValidDialog.newInstance(this, seccodehash, "");
        dialog.show(getFragmentManager(), "valid");
    }

    @Override
    public void onInputFinish(boolean click, String hash, String value) {
        // 输入验证码
        seccodehash = hash;
        validValue = value;
        if (click) { //提交
            submit();
        }
    }
}
