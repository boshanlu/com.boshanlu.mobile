package com.boshanlu.mobile.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.UrlUtils;
import com.boshanlu.mobile.widget.CircleImageView;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by free2 on 16-3-15.
 * 签到activity
 */
public class SignActivity extends BaseActivity {

    protected CircleImageView userImage;
    protected ProgressBar progressBar;
    private View signYes, signNo;
    private TextView signError;
    private int spinnerSelect = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        initToolBar(true, "签到中心");

        progressBar = findViewById(R.id.progressBar);
        signYes = findViewById(R.id.sign_yes);
        signNo = findViewById(R.id.sign_not);
        signError = findViewById(R.id.sign_error);
        signYes.setVisibility(View.GONE);
        signNo.setVisibility(View.GONE);
        signError.setVisibility(View.GONE);
        userImage = findViewById(R.id.avatar);

        checkState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Picasso.with(this).load(UrlUtils.getAvatarUrl(App.getUid(this), "b")).
                placeholder(R.drawable.image_placeholder).into(userImage);
    }

    //看看是否已经签到
    private void checkState() {
        progressBar.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance();
        int HOUR_OF_DAY = c.get(Calendar.HOUR_OF_DAY);
        if (!(6 <= HOUR_OF_DAY && HOUR_OF_DAY < 23)) {
            sign_error();
            return;
        }

        String urlget = "plugin.php?id=dc_signin:sign&mobile=2";
        HttpUtil.get(urlget, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                //// TODO: 16-8-26
                Document doc = Jsoup.parse(res);
                String hash = doc.select("input[name=formhash]").attr("value");
                App.setHash(SignActivity.this, hash);
                if (res.contains("今日您已签到")) {
                    int i = 0;
                    String[] infoList = new String[7];
                    for (Element temp : doc.select(".infomore").select("p")) {
                        infoList[i++] = temp.text();
                    }
                    sign_yes(infoList);
                } else {
                    sign_no();
                }
            }

            @Override
            public void onFailure(Throwable e) {
                showNtice("网络错误");
            }
        });
    }


    private void sign_error() {
        progressBar.setVisibility(View.GONE);
        signError.setVisibility(View.VISIBLE);
    }

    private void sign_yes(String[] infoList) {
        progressBar.setVisibility(View.GONE);
        signYes.setVisibility(View.VISIBLE);
        TextView sign_info = findViewById(R.id.totail_sign_info);
        TextView sign_day = findViewById(R.id.total_sign_day);
        TextView sign_month = findViewById(R.id.total_sign_month);
        TextView sign_last = findViewById(R.id.total_sign_last);
        TextView sign_last_gift = findViewById(R.id.total_sign_last_gift);
        TextView sign_level = findViewById(R.id.total_sign_level);
        TextView sign_level_to = findViewById(R.id.total_sign_level_to);
        int i = 0;
        sign_info.setText(String.format("\uF05D %s", infoList[i++]));
        sign_day.setText(infoList[i++]);
        sign_month.setText(infoList[i++]);
        sign_last.setText(infoList[i++]);
        sign_last_gift.setText(infoList[i++]);
        sign_level.setText(infoList[i++]);
        sign_level_to.setText(infoList[i]);
    }

    private void sign_no() {
        progressBar.setVisibility(View.GONE);
        signNo.setVisibility(View.VISIBLE);
        Spinner spinner = findViewById(R.id.spinner);
        final String[] mItems = {"开心", "难过", "无聊", "郁闷", "擦汗", "大哭", "慵懒", "萌哒", "可爱", "闭嘴"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                spinnerSelect = pos + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button b = findViewById(R.id.btn_submit);
        b.setOnClickListener(view -> startDaka());
    }

    //点击签到按钮
    private void startDaka() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在签到...");
        dialog.show();
        //String formhash = hash;
        String todaysay = "";

        EditText input = findViewById(R.id.input);
        if (!TextUtils.isEmpty(input.getText().toString())) {
            todaysay = input.getText().toString() + "  --来自博山庐手机客户端";
        }

        Map<String, String> params = new HashMap<>();
        //params.put("formhash", formhash);
        params.put("emotid", getGroup1_select());
        params.put("handlekey", "signin");
        params.put("signsubmit", "yes");
        params.put("refer", UrlUtils.getSignUrl());
        params.put("content", todaysay);
        params.put("signpn", "true");

        String url = UrlUtils.getSignUrl();
        HttpUtil.post(url, params, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                Log.d("===签到===", res);
                int start = res.indexOf("签到成功");
                if (start > 0) {
                    showNtice("签到成功~");
                    signNo.setVisibility(View.GONE);
                    checkState();
                } else {
                    showNtice("未知错误,签到失败");
                }
            }

            @Override
            public void onFailure(Throwable e) {
                showNtice("网络错误!!!!!");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }
        });
    }

    //获得选择的心情
    private String getGroup1_select() {
        return String.valueOf(spinnerSelect);
    }

    private void showNtice(String res) {
        progressBar.setVisibility(View.GONE);
        View container = findViewById(R.id.container);
        Snackbar.make(container, res, Snackbar.LENGTH_LONG).show();
    }
}
