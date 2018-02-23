package com.boshanlu.mobile.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.boshanlu.mobile.App;
import com.boshanlu.mobile.R;
import com.boshanlu.mobile.adapter.MainPageAdapter;
import com.boshanlu.mobile.fragment.BaseLazyFragment;
import com.boshanlu.mobile.fragment.FrageForums;
import com.boshanlu.mobile.fragment.FrageHotsNews;
import com.boshanlu.mobile.fragment.FrageMessage;
import com.boshanlu.mobile.fragment.FragmentMy;
import com.boshanlu.mobile.myhttp.HttpUtil;
import com.boshanlu.mobile.myhttp.ResponseHandler;
import com.boshanlu.mobile.utils.GetId;
import com.boshanlu.mobile.widget.MyBottomTab;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yang on 16-3-17.
 * 这是首页 管理3个fragment
 * 1.板块列表{@link HomeActivity}
 * 2.新帖{@link FrageHotsNews}
 */
public class HomeActivity extends BaseActivity
        implements MyBottomTab.OnTabChangeListener, ViewPager.OnPageChangeListener {

    //间隔5天检查更新一次
    private static final int UPDATE_TIME = 1000 * 3600 * 24 * 5;
    private static int interval = 180_000;//180s
    boolean ishaveReply = false;
    boolean ishavePm = false;
    private long mExitTime;
    private Timer timer = null;
    private MyBottomTab bottomTab;
    private long lastCheckMsgTime = 0;
    private MyHandler messageHandler;
    private SharedPreferences sharedPreferences;
    private boolean isNeedCheckUpdate = false;
    private ViewPager viewPager;
    private List<BaseLazyFragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViewpager();

        bottomTab = findViewById(R.id.bottom_bar);
        bottomTab.setOnTabChangeListener(this);

        Calendar c = Calendar.getInstance();
        int HOUR_OF_DAY = c.get(Calendar.HOUR_OF_DAY);
        if (HOUR_OF_DAY < 9 && HOUR_OF_DAY > 1) {
            //晚上一点到早上9点间隔,不同时间段检查消息间隔不同 减轻服务器压力
            //240s
            interval = interval * 2;
        }
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        long time = sharedPreferences.getLong(App.CHECK_UPDATE_KEY, 0);
        if (System.currentTimeMillis() - time > UPDATE_TIME) {
            isNeedCheckUpdate = true;
        }
        messageHandler = new MyHandler(bottomTab, this);
    }

    private void initViewpager() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(this);
        fragments.add(new FrageForums());
        fragments.add(new FrageHotsNews());
        fragments.add(FrageMessage.newInstance());
        fragments.add(new FragmentMy());
        MainPageAdapter adapter = new MainPageAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void tabClicked(View v, int position, boolean isChange) {
        if (isChange) {
            if (fragments.get(position) instanceof FrageMessage) {
                FrageMessage m = (FrageMessage) fragments.get(position);
                // TODO
                m.updateNotifiCations(ishaveReply, ishavePm, false);
            }
            switchTab(position);
        } else {
            fragments.get(position).ScrollToTop();
        }
    }

    //检查消息程序
    @Override
    protected void onStart() {
        super.onStart();
        if (App.ISLOGIN(this)) {
            startCheckMessage();
        }

        if (isNeedCheckUpdate) {
            checkUpdate();
        }
    }

    public void startCheckMessage() {
        //60s进行一次
        long need = interval - (System.currentTimeMillis() - lastCheckMsgTime);
        if (need < 800) {
            need = 800;
        }
        if (timer == null) {
            timer = new Timer(true);
        }
        MyTimerTask task = new MyTimerTask();
        timer.schedule(task, need, interval);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void switchTab(int pos) {
        if (pos == 2) {
            bottomTab.setMessage(false);
            bottomTab.invalidate();
        }
        viewPager.setCurrentItem(pos, false);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomTab.setSelect(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * check update
     */
    private void checkUpdate() {
        PackageManager manager;
        PackageInfo info = null;
        manager = getPackageManager();
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int version_code = 1;
        if (info != null) {
            version_code = info.versionCode;
        }
        final int finalVersion_code = version_code;
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
                    if (code > finalVersion_code) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong(App.CHECK_UPDATE_KEY, System.currentTimeMillis());
                        editor.apply();
                        isNeedCheckUpdate = false;
                        new AlertDialog.Builder(HomeActivity.this).
                                setTitle("检测到新版本").
                                setMessage(title).
                                setPositiveButton("查看", (dialog, which) -> PostActivity.open(HomeActivity.this, App.CHECK_UPDATE_URL, "admin"))
                                .setNegativeButton("取消", null)
                                .setCancelable(true)
                                .create()
                                .show();
                    }
                }
            }
        });
    }

    /**
     * check unread message
     */
    private void dealMessage(boolean isReply, String res) {
        int hashIndex = res.indexOf("formhash");
        if (hashIndex > 0) {
            String hash = res.substring(hashIndex + 9, hashIndex + 17);
            App.setHash(HomeActivity.this, hash);
        }

        Document document = Jsoup.parse(res);
        //回复
        if (isReply) {
            Elements elemens = document.select(".nts").select("dl.cl");
            if (elemens.size() > 0) {
                int last_message_id = getSharedPreferences(App.MY_SHP_NAME, MODE_PRIVATE)
                        .getInt(App.NOTICE_MESSAGE_REPLY_KEY, 0);
                int noticeId = Integer.parseInt(elemens.get(0).attr("notice"));
                ishaveReply = last_message_id < noticeId;
            }
        } else {
            Elements lists = document.select(".pmbox").select("ul").select("li");
            if (lists.size() > 0) {
                ishavePm = lists.get(0).select(".num").text().length() > 0;
            }
        }

        if (ishaveReply || ishavePm) {
            messageHandler.sendEmptyMessage(0);
        } else {
            messageHandler.sendEmptyMessage(-1);
        }
    }

    private void mkNotify() {
        boolean isNotify = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this)
                .getBoolean("setting_show_notify", false);
        if (!isNotify) {
            return;
        }
        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("未读消息提醒")
                .setContentText("你有未读的消息哦,去我的消息页面查看吧！")
                .setAutoCancel(true);
        final NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(10, builder.build());
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 1500) {
                Toast.makeText(this, "再按一次退出博山庐手机客户端", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ThemeActivity.requestCode && resultCode == RESULT_OK) {
            //切换主题
            Log.d("main", "切换主题");
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        App app = (App) getApplication();
        app.unRegRecieve();
        super.onDestroy();
    }

    //deal unread message show red point
    private static class MyHandler extends Handler {
        private final WeakReference<MyBottomTab> mytab;
        private final WeakReference<HomeActivity> act;

        private MyHandler(MyBottomTab tab, HomeActivity aa) {
            mytab = new WeakReference<>(tab);
            act = new WeakReference<>(aa);
        }

        @Override
        public void handleMessage(Message msg) {
            MyBottomTab t = mytab.get();
            HomeActivity a = act.get();
            switch (msg.what) {
                //-1 - 无消息 0-有
                case -1:
                    Log.d("message", "无未读消息");
                    t.setMessage(false);
                    break;
                case 0:
                    a.mkNotify();
                    Log.d("message", "有未读消息");
                    t.setMessage(true);
                    break;
            }
        }
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            String url_reply = "home.php?mod=space&do=notice&view=mypost&type=post&mobile=2";
            String url_pm = "home.php?mod=space&do=pm&mobile=2";
            HttpUtil.SyncGet(HomeActivity.this, url_reply, new ResponseHandler() {
                @Override
                public void onSuccess(byte[] response) {
                    dealMessage(true, new String(response));
                }
            });
            lastCheckMsgTime = System.currentTimeMillis();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            HttpUtil.SyncGet(HomeActivity.this, url_pm, new ResponseHandler() {
                @Override
                public void onSuccess(byte[] response) {
                    dealMessage(false, new String(response));
                }
            });
        }
    }
}
