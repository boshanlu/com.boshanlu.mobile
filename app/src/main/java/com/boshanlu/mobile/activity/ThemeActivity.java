package com.boshanlu.mobile.activity;

import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.widget.MyCircleView;

import java.util.Calendar;

/**
 * 默认主题0
 * 夜间主题1
 * ...
 */
public class ThemeActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    public static final int requestCode = 10;
    public static final int THEME_DEFAULT = R.style.AppTheme;
    public static final int THEME_NIGHT = 1;

    private int[] colors = new int[]{
            0xd12121, 0x1e1e1e, 0xf44836, 0xf2821e, 0x7bb736, 0x16c24b,
            0x16a8c2, 0x2b86e3, 0x3f51b5, 0x9c27b0, 0xcc268f, 0x39c5bb
    };

    private int[] colorsDark = new int[]{
            0xac1c1b, 0x141414, 0xf44836, 0xf2821e, 0x7bb736, 0x16c24b,
            0x16a8c2, 0x2b86e3, 0x3f51b5, 0x9c27b0, 0xcc268f, 0x39c5bb
    };

    private int[] themeIds = new int[]{
            R.style.AppTheme, THEME_NIGHT, R.style.AppTheme_2,
            R.style.AppTheme_3, R.style.AppTheme_4, R.style.AppTheme_5,
            R.style.AppTheme_6, R.style.AppTheme_7, R.style.AppTheme_8,
            R.style.AppTheme_9, R.style.AppTheme_10, R.style.AppTheme_11,
    };

    private String[] names = new String[]{
            "默认", "黑色", "橘红", "橘黄", "原谅", "翠绿",
            "青色", "天蓝", "蓝色", "紫色", "紫红", "初音"
    };


    private int currentSelect = 0;
    private int currentTheme = THEME_DEFAULT;

    private ColorAdapter adpter;
    private View nightViews;
    private TextView startText, endText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        if (savedInstanceState != null) {
            currentSelect = savedInstanceState.getInt("position", 0);
        }

        initToolBar(true, "主题设置");
        addToolbarMenu(R.drawable.ic_done_black_24dp).setOnClickListener(v -> {
            boolean isChange = false;
            int curr = AppCompatDelegate.getDefaultNightMode();
            int to = curr;

            if (App.getCustomTheme(this) != themeIds[currentSelect]) {
                App.setCustomTheme(this, themeIds[currentSelect]);
                isChange = true;
                if (themeIds[currentSelect] == THEME_NIGHT) {
                    to = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    to = AppCompatDelegate.MODE_NIGHT_NO;
                }
            }

            if (themeIds[currentSelect] != THEME_NIGHT) {
                //黑主题 自动主题无效
                if (App.isAutoDarkMode(this)) {
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


            if (curr != to) {
                AppCompatDelegate.setDefaultNightMode(to);
                isChange = true;
            }

            if (isChange) {
                showToast("已更改主题");
                setResult(RESULT_OK);
            }

            finish();
        });

        GridView gridView = findViewById(R.id.commons_colors);
        adpter = new ColorAdapter();
        gridView.setAdapter(adpter);
        gridView.setOnItemClickListener(this);


        CheckBox auto = findViewById(R.id.auto_dark_mode);
        boolean isAuto = App.isAutoDarkMode(this);
        auto.setChecked(isAuto);

        View startView = findViewById(R.id.start_time);
        View endView = findViewById(R.id.end_time);
        nightViews = findViewById(R.id.night_views);
        startText = findViewById(R.id.start_time_text);
        endText = findViewById(R.id.end_time_text);

        currentTheme = App.getCustomTheme(this);
        currentSelect = getSelect();

        if (currentSelect == THEME_NIGHT) {
            nightViews.setVisibility(View.GONE);
        } else {
            nightViews.setVisibility(View.VISIBLE);
        }


        startText.setText(App.getDarkModeTime(this)[0] + ":00");
        endText.setText(App.getDarkModeTime(this)[1] + ":00");

        auto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            App.setAutoDarkMode(this, isChecked);
        });

        startView.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                startText.setText(hourOfDay + ":00");
                App.setDarkModeTime(this, true, hourOfDay);
                Log.d("==", hourOfDay + "");
            }, App.getDarkModeTime(this)[0], 0, true).show();
        });

        endView.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                endText.setText(hourOfDay + ":00");
                Log.d("==", hourOfDay + "");
                App.setDarkModeTime(this, false, hourOfDay);
            }, App.getDarkModeTime(this)[1], 0, true).show();
        });
    }


    public int getSelect() {
        for (int i = 0; i < themeIds.length; i++) {
            if (currentTheme == themeIds[i]) {
                return i;
            }
        }

        return 0;
    }

    private void changeTheme(int position) {
        View toolbar = findViewById(R.id.myToolBar);
        toolbar.setBackgroundColor(0xff000000 | colors[position]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff000000 | colorsDark[position]);
            //底部导航栏
            //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
        }

        if (themeIds[position] == THEME_NIGHT) {
            nightViews.setVisibility(View.GONE);
            //window背景颜色
            getWindow().setBackgroundDrawable(new ColorDrawable(0xff333333));
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(0xfff5f5f5));
            nightViews.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentSelect == position) return;
        currentSelect = position;
        adpter.notifyDataSetChanged();
        changeTheme(position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", currentSelect);
    }

    private class ColorAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Object getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.item_color, null);
            MyCircleView circleView = convertView.findViewById(R.id.color);
            circleView.setColor(colors[position]);
            circleView.setSelect(position == currentSelect);
            ((TextView) convertView.findViewById(R.id.name)).setText(names[position]);
            return convertView;
        }
    }
}
