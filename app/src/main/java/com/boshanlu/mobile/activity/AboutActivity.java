package com.boshanlu.mobile.activity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.IntentUtils;
import com.boshanlu.mobile.widget.htmlview.HtmlView;


/**
 * Created by yluo on 2015/10/5 0005.
 * 关于页面
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        }

        TextView version = findViewById(R.id.version);
        TextView serverVersion = findViewById(R.id.server_version);
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());

        String ss = "<b>博山庐手机客户端</b><br />    本项目基于西电睿思手机Discuz客户端开源项目在Apache License V2.0下二次开发，原项目链接："+
                "<a href=\"https://github.com/freedom10086/Ruisi\">GitHub</a>  ，<br />"+
                "功能不断更新完善中，bug较多还请多多反馈<br />bug反馈:<br />" +
                "<ul>\n" +
                "  <li>到 <a href=\"forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2\">本帖</a> 回复</li>\n" +
                "  <li>发帖 <a href=\"home.php?mod=space&uid=1&do=profile&mobile=2\">@admin</a></li>\n" +
                "</ul>\n<br />" +
                "<p>本项目开源地址：<a href=\"https://github.com/boshanlu/com.boshanlu.mobile\">com.boshanlu.mobile</a>   </p>";

        TextView htmlView = findViewById(R.id.html_text);
        HtmlView.parseHtml(ss).into(htmlView);

        PackageInfo info = null;
        PackageManager manager = getPackageManager();
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int versionCode = 0;
        if (info != null) {
            String version_name = info.versionName;
            versionCode = info.versionCode;
            String a = "当前版本:" + version_name;
            version.setText(a);
        }

        findViewById(R.id.fab).setOnClickListener(v -> Snackbar.make(v, "你要提交bug或者建议吗?", Snackbar.LENGTH_LONG)
                .setAction("确定", view -> {
                    String user = App.getName(AboutActivity.this);
                    if (user != null) {
                        user = "by:" + user;
                    }
                    IntentUtils.sendMail(getApplicationContext(), user);
                })
                .show());

        int finalVersionCode = versionCode;

        // 检查更新实现 读取我发帖的标题比较版本号
        // 我会把版本号写在标题上[code:xxx]
        HttpUtil.get(App.CHECK_UPDATE_URL, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                int ih = res.indexOf("keywords");
                int h_start = res.indexOf('\"', ih + 15);
                int h_end = res.indexOf('\"', h_start + 1);
                String title = res.substring(h_start + 1, h_end);
                if (title.contains("code")) {
                    SharedPreferences.Editor editor = getSharedPreferences(App.MY_SHP_NAME, MODE_PRIVATE).edit();
                    editor.putLong(App.CHECK_UPDATE_KEY, System.currentTimeMillis());
                    editor.apply();
                    int st = title.indexOf("code");
                    int code = GetId.getNumber(title.substring(st));
                    if (code > finalVersionCode) {
                        serverVersion.setText("检测到新版本点击查看");
                        serverVersion.setOnClickListener(view -> PostActivity.open(AboutActivity.this, App.CHECK_UPDATE_URL, "admin"));
                        return;
                    }
                }

                serverVersion.setText("当前已是最新版本");
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                serverVersion.setText("检测新版本失败...");
            }
        });
    }

}
