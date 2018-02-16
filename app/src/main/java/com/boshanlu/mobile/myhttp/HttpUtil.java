package com.boshanlu.mobile.myhttp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.boshanlu.mobile.App;

import java.util.Map;

public class HttpUtil {
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static SyncHttpClient syncHttpClient = new SyncHttpClient();
    private static PersistentCookieStore store;
    private static SharedPreferences preferences;

    private static String getUrl(String url) {
        if (url.startsWith("http")) {
            return url;
        } else {
            return App.BASE_URL + url;
        }
    }


    public static void get(String url, ResponseHandler handler) {
        client.get(getUrl(url), handler);
    }

    public static void post(String url, Map<String, String> map, ResponseHandler handler) {
        if (!map.containsKey("formhash") || TextUtils.isEmpty(map.get("formhash"))) {
            if (preferences != null) {
                String hash = preferences.getString(App.HASH_KEY, "");
                if (!TextUtils.isEmpty(hash)) {
                    map.put("formhash", hash);
                }
            }

        }
        client.post(getUrl(url), map, handler);
    }

    public static void head(String url, Map<String, String> params, ResponseHandler handler) {
        client.head(getUrl(url), params, handler);
    }

    public static void init(Context context) {
        client.setConnectionTimeout(8000);
        if (context != null && store == null) {
            store = new PersistentCookieStore(context.getApplicationContext());
            client.setStore(store);

            preferences = context.getSharedPreferences(App.MY_SHP_NAME, Context.MODE_PRIVATE);
        }

    }

    //同步
    public static void SyncGet(Context context, String url, ResponseHandler handler) {
        init(context);
        syncHttpClient.get(getUrl(url), handler);
    }


    //同步 上传图片
    public static void uploadImage(Context context, final String url, Map<String, String> map, String imageName, byte[] imageData, final ResponseHandler handler) {
        init(context);
        client.uploadImage(getUrl(url), map, imageName, imageData, handler);
    }


    //同步 上传图片
    public static void SyncUpload(Context context, final String url, Map<String, String> map, String imageName, byte[] imageData, final ResponseHandler handler) {
        init(context);
        syncHttpClient.uploadImage(getUrl(url), map, imageName, imageData, handler);
    }

    public static void exit() {
        store.clearCookie();
    }

    public static PersistentCookieStore getStore(Context context) {
        if (store == null) {
            store = new PersistentCookieStore(context);
            return store;
        } else {
            return store;
        }
    }

    public static AsyncHttpClient getClient() {
        return client;
    }
}
