package com.boshanlu.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.utils.RuisUtils;
import com.boshanlu.mobile.widget.CircleImageView;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by free2 on 16-3-19.
 * 启动activity
 * 检查是否登陆
 * 读取相关设置写到{@link App}
 */
public class LaunchActivity extends BaseActivity {
    private final static int WAIT_TIME = 350;//最少等待时间ms
    private TextView launch_text;
    private CircleImageView logo;
    private boolean isForeGround = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);

        App app = (App) getApplication();
        app.regReciever();

        launch_text = findViewById(R.id.app_name);
        logo = findViewById(R.id.logo);
        loadUserImg();
        setCopyRight();
        new Handler().postDelayed(this::enterHome, WAIT_TIME);
    }

    //自动续命copyright
    private void setCopyRight() {
        int year = 2016;
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);

        if (year < yearNow) {
            year = yearNow;
        }
        ((TextView) findViewById(R.id.copyright))
                .setText("©2016-" + year + " 博山小叙");
    }

    //设置头像
    private void loadUserImg() {
        String uid = App.getUid(this);
        if (!TextUtils.isEmpty(uid)) {
            RuisUtils.LoadMyAvatar(new WeakReference<>(this),
                    uid, new WeakReference<>(logo), "m");
        }
    }

    private void enterHome() {
        if (isForeGround) {
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out);
            Intent i = new Intent();
            ActivityCompat.startActivity(this, new Intent(this, HomeActivity.class), compat.toBundle());
            new Handler().postDelayed(this::finish, 305);
        }
    }

    @Override
    public void finish() {
        super.finish();
        // 去掉自带的转场动画
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
    }


    @Override
    protected void onStart() {
        super.onStart();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.4f, 1.0f);
        alphaAnimation.setDuration((long) (WAIT_TIME * 0.85));// 设置动画显示时间
        launch_text.startAnimation(alphaAnimation);
        logo.startAnimation(alphaAnimation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeGround = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeGround = false;
    }
}
