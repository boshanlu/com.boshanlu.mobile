package com.boshanlu.mobile.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.activity.AboutActivity;
import com.boshanlu.mobile.activity.BaseActivity;
import com.boshanlu.mobile.activity.FragementActivity;
import com.boshanlu.mobile.activity.FriendActivity;
import com.boshanlu.mobile.activity.HomeActivity;
import com.boshanlu.mobile.activity.LoginActivity;
import com.boshanlu.mobile.activity.SettingActivity;
import com.boshanlu.mobile.activity.SignActivity;
import com.boshanlu.mobile.activity.ThemeActivity;
import com.boshanlu.mobile.activity.UserDetailActivity;
import com.boshanlu.mobile.model.FrageType;
import com.boshanlu.mobile.utils.IntentUtils;
import com.boshanlu.mobile.utils.RuisUtils;
import com.boshanlu.mobile.widget.CircleImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: 16-8-23  打开的时候检查是否签到显示在后面
 * 基础列表后面可以显示一些详情，如收藏的数目等...
 */
public class FragmentMy extends BaseLazyFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final int[] icons = new int[]{
            R.drawable.ic_autorenew_black_24dp,
            R.drawable.ic_palette_black_24dp,
            R.drawable.ic_settings_24dp,
            R.drawable.ic_info_24dp,
            R.drawable.ic_menu_share_24dp,
            R.drawable.ic_favorite_white_12dp,
    };
    private final String[] titles = new String[]{
            "签到中心",
            "主题设置",
            "设置",
            "关于本程序",
            "分享博山庐手机客户端",
            "到商店评分",
    };
    private String username, uid;
    private CircleImageView userImg;
    private TextView userName, userGrade;
    //记录上次创建时候是否登录
    private boolean isLoginLast = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        username = App.getName(getActivity());
        uid = App.getUid(getActivity());

        userImg = mRootView.findViewById(R.id.user_img);
        userName = mRootView.findViewById(R.id.user_name);
        userGrade = mRootView.findViewById(R.id.user_grade);
        userImg.setOnClickListener(this);
        mRootView.findViewById(R.id.history).setOnClickListener(this);
        mRootView.findViewById(R.id.star).setOnClickListener(this);
        mRootView.findViewById(R.id.friend).setOnClickListener(this);
        mRootView.findViewById(R.id.post).setOnClickListener(this);

        ListView listView = mRootView.findViewById(R.id.function_list);
        List<Map<String, Object>> fs = new ArrayList<>();
        for (int i = 0; i < icons.length; i++) {
            Map<String, Object> d = new HashMap<>();
            d.put("icon", icons[i]);
            d.put("title", titles[i]);
            fs.add(d);
        }
        listView.setOnItemClickListener(this);
        listView.setAdapter(new SimpleAdapter(getActivity(), fs, R.layout.item_function, new String[]{"icon", "title"}, new int[]{R.id.icon, R.id.title}));
        return mRootView;
    }

    @Override
    public void onFirstUserVisible() {
        isLoginLast = App.ISLOGIN(getActivity());
        refreshAvatarView();
    }

    @Override
    public void onUserVisible() {
        if (isLoginLast != App.ISLOGIN(getActivity())) {
            isLoginLast = !isLoginLast;
            refreshAvatarView();
        }
    }

    @Override
    public void ScrollToTop() {
        //do noting
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my;
    }

    private void refreshAvatarView() {
        if (isLoginLast) {
            uid = App.getUid(getActivity());
            userName.setText(App.getName(getActivity()));
            userGrade.setVisibility(View.VISIBLE);
            userGrade.setText(App.getGrade(getActivity()));

            RuisUtils.LoadMyAvatar(new WeakReference<>(getActivity()),
                    uid,
                    new WeakReference<>(userImg), "m");
        } else {
            userName.setText("点击头像登陆");
            userGrade.setVisibility(View.GONE);
            userImg.setImageResource(R.drawable.image_placeholder);
        }
    }

    @Override
    public void onClick(View view) {
        BaseActivity b = (BaseActivity) getActivity();
        switch (view.getId()) {
            case R.id.user_img:
                if (App.ISLOGIN(getActivity())) {
                    UserDetailActivity.openWithAnimation(
                            getActivity(), username, userImg, uid);
                } else {
                    switchActivity(LoginActivity.class);
                }
                break;
            case R.id.post:
                if (b.isLogin()) {
                    FragementActivity.open(getActivity(), FrageType.TOPIC);
                }
                break;
            case R.id.star:
                if (b.isLogin()) {
                    FragementActivity.open(getActivity(), FrageType.START);
                }
                break;
            case R.id.history:
                FragementActivity.open(getActivity(), FrageType.HISTORY);
                break;
            case R.id.friend:
                if (b.isLogin()) {
                    switchActivity(FriendActivity.class);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                HomeActivity a = (HomeActivity) getActivity();
                if (a.isLogin()) {
                    switchActivity(SignActivity.class);
                }
                break;
            case 1:
                Intent i = new Intent(getActivity(), ThemeActivity.class);
                getActivity().startActivityForResult(i, ThemeActivity.requestCode);
                break;
            case 2:
                switchActivity(SettingActivity.class);
                break;
            case 3:
                switchActivity(AboutActivity.class);
                break;
            case 4:
                String data = "这个博山庐手机客户端非常不错，分享给你们。" +
                        "\n下载地址: https://boshanlu.com/forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2";
                IntentUtils.shareApp(getActivity(), data);
                break;
            case 5:
                if (!IntentUtils.openStore(getActivity())) {
                    Toast.makeText(getActivity(), "确保你的手机安装了相关应用商城", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
