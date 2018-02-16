package com.boshanlu.mobile;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.boshanlu.mobile.checknet.NetworkReceiver;
import com.boshanlu.mobile.database.MyDB;
import com.boshanlu.mobile.database.SQLiteHelper;
import com.boshanlu.mobile.myhttp.HttpUtil;

public class App extends Application {

    //发布地址tid
    public static final String POST_TID = "4289";
    //论坛基地址2个地址 第一个校园网才能访问，第二个都可以
    public static final String BASE_URL = "https://boshanlu.com/";
    /**
     * config
     * todo 把一些常量移到这儿来
     */

    //记录上次未读消息的id
    public static final String MY_SHP_NAME = "ruisi_shp";
    public static final String NOTICE_MESSAGE_REPLY_KEY = "message_notice_reply";
    public static final String NOTICE_MESSAGE_AT_KEY = "message_notice_at";
    public static final String THEME_KEY = "my_theme_key";
    public static final String AUTO_DARK_MODE_KEY = "auto_dark_mode";
    public static final String START_DARK_TIME_KEY = "start_dart_time";
    public static final String END_DARK_TIME_KEY = "end_dark_time";
    public static final String USER_UID_KEY = "user_uid";
    public static final String USER_NAME_KEY = "user_name";
    public static final String HASH_KEY = "forum_hash";
    public static final String USER_GRADE_KEY = "user_grade";
    public static final String IS_REMBER_PASS_USER = "login_rember_pass";
    public static final String LOGIN_NAME = "login_name";
    public static final String LOGIN_PASS = "login_pass";
    public static final String CHECK_UPDATE_KEY = "check_update_time";
    public static final String LOGIN_URL = "member.php?mod=logging&action=login";
    public static final String LOGIN = App.BASE_URL + "member.php?mod=logging&action=login&mobile=2";
    public static final String CHECK_POST_URL = "forum.php?mod=ajax&action=checkpostrule&ac=newthread&mobile=2";
    public static final String CHECK_UPDATE_URL = "forum.php?mod=viewthread&tid=" + App.POST_TID + "&mobile=2";
    public Context context;

    private NetworkReceiver receiver;


    public static boolean ISLOGIN(Context context) {
        return !TextUtils.isEmpty(App.getUid(context));
    }

    public static String getUid(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_UID_KEY, "");
    }

    public static void setUid(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(USER_UID_KEY, "");
        editor.apply();
    }

    public static void setHash(Context context, String hash) {
        if (TextUtils.isEmpty(hash)) {
            return;
        }
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(HASH_KEY, hash);
        editor.apply();
    }

    public static String getName(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_NAME_KEY, "");
    }

    public static String getGrade(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getString(USER_GRADE_KEY, "");
    }

    public static int getCustomTheme(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getInt(THEME_KEY, 0);
    }

    public static void setCustomTheme(Context context, int theme) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(THEME_KEY, theme);
        editor.apply();
    }

    public static boolean isAutoDarkMode(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        return shp.getBoolean(AUTO_DARK_MODE_KEY, true);
    }

    public static void setAutoDarkMode(Context context, boolean value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(AUTO_DARK_MODE_KEY, value);
        editor.apply();
    }

    public static void setDarkModeTime(Context context, boolean isStart, int value) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        if (isStart) {
            editor.putInt(START_DARK_TIME_KEY, value);
        } else {
            editor.putInt(END_DARK_TIME_KEY, value);
        }
        editor.apply();
    }

    public static int[] getDarkModeTime(Context context) {
        SharedPreferences shp = context.getSharedPreferences(MY_SHP_NAME, MODE_PRIVATE);
        int[] ret = new int[2];
        ret[0] = shp.getInt(START_DARK_TIME_KEY, 21);
        ret[1] = shp.getInt(END_DARK_TIME_KEY, 6);
        return ret;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        //初始化http
        HttpUtil.init(getApplicationContext());

        //清空消息数据库
        MyDB myDB = new MyDB(context);
        //最多缓存2000条历史纪录
        myDB.deleteOldHistory(2000);

        regReciever();
    }

    @Override
    public void onTerminate() {
        //关闭数据库
        new SQLiteHelper(context).close();
        unRegRecieve();

        context = null;
        super.onTerminate();
    }

    public Context getContext() {
        return context;
    }

    public void regReciever() {
        if (receiver != null) return;
        //注册网络变化广播
        receiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);
    }

    public void unRegRecieve() {
        //注册网络变化广播
        if (receiver != null) {
            Log.d("onDestroy", "取消注册广播");
            unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
