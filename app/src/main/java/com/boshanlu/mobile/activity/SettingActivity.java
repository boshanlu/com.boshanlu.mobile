package com.boshanlu.mobile.activity;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.boshanlu.mobile.R;
import com.boshanlu.mobile.fragment.FragSetting;

/**
 * 设置页面
 */
public class SettingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View toolBar = LayoutInflater.from(this).inflate(R.layout.my_toolbar, null);
        root.addView(toolBar);

        FrameLayout f = new FrameLayout(this);
        f.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        f.setId(R.id.container);

        root.addView(f);
        setContentView(root);
        initToolBar(true, "设置");

        Fragment to = new FragSetting();
        getFragmentManager().beginTransaction().replace(R.id.container, to).commit();
    }

}
