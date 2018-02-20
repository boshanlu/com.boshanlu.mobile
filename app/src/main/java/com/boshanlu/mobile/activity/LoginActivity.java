package com.boshanlu.mobile.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.InputValidDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by yang on 2016/1/11 0011.
 * <p>
 * edit in 2016 03 14
 * <p>
 * 登陆activity
 */
public class LoginActivity extends BaseActivity implements InputValidDialog.OnInputValidListener {

    private TextInputEditText edUsername, edPassword;
    private EditText edAnswer;
    private CheckBox remPassword;
    private View btnLogin;

    private SharedPreferences shp;
    private List<String> list = new ArrayList<>();
    private String loginUrl;
    private int answerSelect = 0;
    private ProgressDialog dialog;
    private TextInputLayout usernameTextInput;

    // 验证码相关
    private boolean haveValid = false;
    private String seccodehash = null;
    private String validValue = null; //验证码输入值
    private String validImageSrc = null; //验证码图片地址

    public static void open(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initToolBar(true, "登陆");
        edUsername = findViewById(R.id.login_name);
        edPassword = findViewById(R.id.login_pas);
        btnLogin = findViewById(R.id.btn_login);
        remPassword = findViewById(R.id.rem_user);

        edAnswer = findViewById(R.id.anwser_text);
        usernameTextInput = findViewById(R.id.username_input);

        shp = getSharedPreferences(App.MY_SHP_NAME, Context.MODE_PRIVATE);
        if (shp.getBoolean(App.IS_REMBER_PASS_USER, false)) {
            remPassword.setChecked(true);
            edUsername.setText(shp.getString(App.LOGIN_NAME, ""));
            edPassword.setText(shp.getString(App.LOGIN_PASS, ""));
        }

        btnLogin.setOnClickListener(v -> startLogin());

        list.add("安全提问(未设置请忽略)");
        list.add("母亲的名字");
        list.add("爷爷的名字");
        list.add("父亲出生的城市");
        list.add("您其中一位老师的名字");
        list.add("您个人计算机的型号");
        list.add("您最喜欢的餐馆名称");
        list.add("驾驶执照最后四位数字");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner questionSpinner = findViewById(R.id.anwser_select);
        questionSpinner.setAdapter(spinnerAdapter);
        questionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                answerSelect = i;
                if (i != 0) {
                    edAnswer.setVisibility(View.VISIBLE);
                } else {
                    edAnswer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        edUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                usernameTextInput.setError(null);
                if (!TextUtils.isEmpty(edUsername.getText()) && !TextUtils.isEmpty(edPassword.getText())) {
                    btnLogin.setEnabled(true);
                } else {
                    btnLogin.setEnabled(false);
                }
            }
        });

        edPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(edUsername.getText()) && !TextUtils.isEmpty(edPassword.getText())) {
                    btnLogin.setEnabled(true);
                } else {
                    btnLogin.setEnabled(false);
                }
            }
        });

        loadData();
    }

    private void loadData() {
        String url = UrlUtils.getLoginUrl();
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Document doc = Jsoup.parse(res);

                //判断是否有验证码
                Element element = doc.select("#loginform .sec_code").first();
                if (element != null) {
                    haveValid = true;
                    seccodehash = element.select("input[name=seccodehash]").attr("value");
                    validImageSrc = element.select("img").attr("src");
                }

                loginUrl = doc.select("form#loginform").attr("action");
                String hash = doc.select("input[name=formhash]").attr("value");
                App.setHash(LoginActivity.this, hash);
            }

            @Override
            public void onFailure(Throwable e) {
                AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("加载失败")
                        .setMessage("是否重新加载")
                        .setPositiveButton("重新加载", (dialogInterface, i) -> loadData())
                        .setNegativeButton("关闭", (dialogInterface, i) -> finish())
                        .create();
                dialog.show();
            }
        });
    }

    //显示填写验证码框子
    private void showInputValidDialog() {
        String update = GetId.getId("update=", validImageSrc);
        InputValidDialog dialog = InputValidDialog.newInstance(this, seccodehash, update);
        dialog.show(getFragmentManager(), "valid");
    }

    private void startLogin() {
        if (haveValid && TextUtils.isEmpty(validValue)) {
            showInputValidDialog();
            return;
        }

        dialog = new ProgressDialog(this);
        dialog.setMessage("登陆中，请稍后......");
        dialog.show();

        final String username = edUsername.getText().toString().trim();
        final String passNo = edPassword.getText().toString().trim();

        Map<String, String> params = new HashMap<>();
        params.put("fastloginfield", "username");
        params.put("cookietime", "2592000");
        params.put("username", username);
        params.put("password", passNo);
        params.put("questionid", answerSelect + "");

        if (haveValid) { //是否有验证码
            params.put("seccodehash", seccodehash);
            params.put("seccodeverify", validValue);
        }

        if (answerSelect == 0) {
            params.put("answer", "");
        } else {
            params.put("answer", edAnswer.getText().toString());
        }

        HttpUtil.post(loginUrl, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (res.contains("欢迎您回来")) {
                    loginOk(res);
                } else if (res.contains("class=\"jump_c\"")) {
                    int start = res.indexOf("<p>", res.indexOf("class=\"jump_c\"")) + 3;
                    int end = res.indexOf("</p>", start);
                    String reason = res.substring(start, end);
                    if ("抱歉，验证码填写错误".equals(reason)) {
                        showInputValidDialog();
                    } else if (reason.contains("登录失败") && reason.contains("您还可以尝试")) {
                        reason = reason.replace("登录失败", "账号或者密码错误");
                    }
                    networkErr(reason);
                } else {
                    passwordOrUsernameErr();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                networkErr("网络异常");
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        });
    }


    //登陆成功执行
    private void loginOk(String res) {
        //写入到首选项
        SharedPreferences.Editor editor = shp.edit();
        if (remPassword.isChecked()) {
            editor.putBoolean(App.IS_REMBER_PASS_USER, true);
            editor.putString(App.LOGIN_NAME, edUsername.getText().toString().trim());
            editor.putString(App.LOGIN_PASS, edPassword.getText().toString().trim());
        } else {
            editor.putBoolean(App.IS_REMBER_PASS_USER, false);
            editor.putString(App.LOGIN_NAME, "");
            editor.putString(App.LOGIN_PASS, "");
        }

        int i = res.indexOf("欢迎您回来");
        String grade, name;

        String info = res.substring(i, i + 70);
        int pos = info.indexOf("，");
        if (info.charAt(pos + 1) == '<') {//管理员
            int pos2 = info.indexOf(">", pos);
            int pos3 = info.indexOf("<", pos2);
            grade = info.substring(pos2 + 1, pos3);
            int pos4 = info.indexOf(" ", pos3);
            int pos5 = info.indexOf("，", pos4);
            name = info.substring(pos4 + 1, pos5);
        } else {
            int pos2 = info.indexOf(" ", pos);
            grade = info.substring(pos + 1, pos2);
            int pos3 = info.indexOf("，", pos2);
            name = info.substring(pos2 + 1, pos3);
        }

        String uid = GetId.getId("uid=", res.substring(i));
        int indexhash = res.indexOf("formhash");
        String hash = res.substring(indexhash + 9, indexhash + 17);
        editor.putString(App.USER_UID_KEY, uid);
        editor.putString(App.USER_NAME_KEY, name);
        editor.putString(App.USER_GRADE_KEY, grade);
        editor.putString(App.HASH_KEY, hash);
        editor.apply();
        showToast("欢迎你" + name + "登陆成功");
        Log.d("login result", "grade " + grade + " uid " + uid + " name " + name + " hash " + hash);

        Intent intent = new Intent();
        intent.putExtra("status", "ok");
        //设置返回数据
        LoginActivity.this.setResult(RESULT_OK, intent);

        finish();
    }

    private void passwordOrUsernameErr() {
        usernameTextInput.setError("账号或者密码错误");
    }

    //登陆失败执行
    private void networkErr(String res) {
        showToast(res);
    }

    @Override
    public void onInputFinish(boolean click, String hash, String value) {
        // 输入验证码
        seccodehash = hash;
        validValue = value;
        if (click) { //提交
            startLogin();
        }
    }
}



