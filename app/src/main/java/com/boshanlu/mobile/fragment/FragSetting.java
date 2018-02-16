package com.boshanlu.mobile.fragment;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.PostActivity;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.DataManager;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.utils.IntentUtils;

/**
 * Created by free2 on 16-7-18.
 * 设置页面
 */

public class FragSetting extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //小尾巴string
    private EditTextPreference setting_user_tail;
    //论坛地址
    private SharedPreferences sharedPreferences;
    private Preference clearCache;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        //小尾巴
        setting_user_tail = (EditTextPreference) findPreference("setting_user_tail");
        setting_user_tail.setEnabled(sharedPreferences.getBoolean("setting_show_tail", false));
        setting_user_tail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
        //清除缓存
        clearCache = findPreference("clean_cache");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        PackageManager manager;
        PackageInfo info = null;
        manager = getActivity().getPackageManager();
        try {
            info = manager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int version_code = 1;
        String version_name = "1.0";
        if (info != null) {
            version_code = info.versionCode;
            version_name = info.versionName;
        }

        findPreference("about_this")
                .setSummary("当前版本" + version_name + "  version code:" + version_code);


        //[2016年6月9日更新][code:25]睿思手机客户端
        //更新逻辑 检查睿思帖子标题 比对版本号
        final int finalversion_code = version_code;
        findPreference("about_this").setOnPreferenceClickListener(
                preference -> {
                    Toast.makeText(getActivity(), "正在检查更新", Toast.LENGTH_SHORT).show();
                    HttpUtil.get(App.CHECK_UPDATE_URL, new ResponseHandler() {
                        @Override
                        public void onSuccess(byte[] response) {
                            String res = new String(response);
                            int ih = res.indexOf("keywords");
                            int h_start = res.indexOf('\"', ih + 15);
                            int h_end = res.indexOf('\"', h_start + 1);
                            String title = res.substring(h_start + 1, h_end);
                            if (title.contains("code")) {
                                int st = title.indexOf("code");
                                int code = GetId.getNumber(title.substring(st));
                                if (code > finalversion_code) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putLong(App.CHECK_UPDATE_KEY, System.currentTimeMillis());
                                    editor.apply();
                                    new AlertDialog.Builder(getActivity()).
                                            setTitle("检测到新版本").
                                            setMessage(title).
                                            setPositiveButton("查看",
                                                    (dialog, which) -> PostActivity.open(getActivity(),
                                                            App.CHECK_UPDATE_URL, "admin"))
                                            .setNegativeButton("取消", null)
                                            .setCancelable(true)
                                            .create()
                                            .show();

                                } else {
                                    Toast.makeText(getActivity(), "暂无更新", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    return true;
                });

        findPreference("open_sourse").setOnPreferenceClickListener(preference -> {
            IntentUtils.openBroswer(getActivity(), "https://github.com/freedom10086/Ruisi");
            return false;
        });
        clearCache.setSummary("缓存大小：" + DataManager.getTotalCacheSize(getActivity()));
        clearCache.setOnPreferenceClickListener(preference -> {
            DataManager.cleanApplicationData(getActivity());

            Toast.makeText(getActivity(), "缓存清理成功!请重新登陆", Toast.LENGTH_SHORT).show();
            clearCache.setSummary("缓存大小：" + DataManager.getTotalCacheSize(getActivity()));
            return false;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "setting_show_tail":
                boolean b = sharedPreferences.getBoolean("setting_show_tail", false);
                setting_user_tail.setEnabled(b);
                setting_user_tail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
                break;
            case "setting_user_tail":
                setting_user_tail.setSummary(sharedPreferences.getString("setting_user_tail", "无小尾巴"));
                break;
            case "setting_hide_zhidin":
                break;
            case "setting_show_plain":
                boolean bbbb = sharedPreferences.getBoolean("setting_show_plain", false);
                Toast.makeText(getActivity(), bbbb ? "文章显示模式：简洁" : "文章显示模式：默认",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}