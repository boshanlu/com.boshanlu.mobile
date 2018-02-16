package com.boshanlu.mobile.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.utils.DimmenUtils;

import java.util.Calendar;

/**
 * Created by free2 on 16-4-11.
 * 所有activity的基类
 */
public class BaseActivity extends AppCompatActivity {
    private static Toast mToast;

    //谁用了FREEDOM
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchTheme();
    }

    //中途 切换主题
    public void switchTheme() {
        //直接夜间 设置退出
        int theme = App.getCustomTheme(this);
        int cur = AppCompatDelegate.getDefaultNightMode();
        int to = cur;
        boolean autoChnage = false;

        //夜间主题
        if (theme == ThemeActivity.THEME_NIGHT) {
            to = AppCompatDelegate.MODE_NIGHT_YES;
        } else {//白天主题
            if (App.isAutoDarkMode(this)) {
                autoChnage = true;
                int[] time = App.getDarkModeTime(this);
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if ((hour >= time[0] || hour < time[1])) {
                    //自动切换
                    to = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    to = AppCompatDelegate.MODE_NIGHT_NO;
                }
            } else {
                to = AppCompatDelegate.MODE_NIGHT_NO;
            }
        }

        if (to == AppCompatDelegate.MODE_NIGHT_YES) {
            //夜间模式主题
            setTheme(R.style.AppTheme);
        } else {
            setTheme(theme);
        }

        //黑白发生了变化
        if (to != cur) {
//            if (autoChnage) {
//                showToast("自动" + (to == AppCompatDelegate.MODE_NIGHT_YES ?
//                        "切换到夜间模式" : "关闭夜间模式"));
//            }
            AppCompatDelegate.setDefaultNightMode(to);
        }
    }

    //判断是否需要弹出登录dialog
    public boolean isLogin() {
        if (!TextUtils.isEmpty(App.getUid(this))) {
            return true;
        } else {
            Dialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("需要登陆")
                    .setMessage("你还没有登陆，要去登陆吗？？")
                    .setPositiveButton("登陆", (dialog, which) -> startActivity(new Intent(BaseActivity.this, LoginActivity.class)))
                    .setNegativeButton("取消", null)
                    .setCancelable(true)
                    .create();

            alertDialog.show();
        }
        return false;
    }


    protected void initToolBar(boolean isshowBack, String text) {
        View toolbar = findViewById(R.id.myToolBar);
        if (toolbar != null) {
            ((TextView) toolbar.findViewById(R.id.title)).setText(text);
            if (isshowBack) {
                findViewById(R.id.logo).setOnClickListener(view -> finish());
            } else {
                findViewById(R.id.logo).setVisibility(View.GONE);
            }
        }
    }

    protected void setTitle(String s) {
        View toolbar = findViewById(R.id.myToolBar);
        if (toolbar != null) {
            ((TextView) toolbar.findViewById(R.id.title)).setText(s);
        }
    }

    protected ImageView addToolbarMenu(int resid) {
        View toolbar = findViewById(R.id.myToolBar);
        if (toolbar != null) {
            ImageView i = toolbar.findViewById(R.id.menu);
            i.setImageResource(resid);
            i.setVisibility(View.VISIBLE);
            return i;
        }
        return null;
    }

    protected void addToolbarView(View v) {
        FrameLayout toolbar = findViewById(R.id.myToolBar);
        if (toolbar != null) {
            FrameLayout.LayoutParams pls = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            v.setLayoutParams(pls);
            int padding = DimmenUtils.dip2px(this, 12);
            v.setPadding(padding, padding, padding, padding);
            pls.setMarginEnd(padding);
            pls.gravity = Gravity.END;
            toolbar.addView(v);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    protected void showToast(String str) {
        if (mToast == null) {
            mToast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(str);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    protected void showLongToast(String str) {
        if (mToast == null) {
            mToast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        } else {
            mToast.setText(str);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
