package com.boshanlu.mobile.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by yang on 16-8-22.
 * 请求浏览器打开
 */

public class IntentUtils {

    public static void openBroswer(Context activity, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        activity.startActivity(intent);
    }


    public static void sendMail(Context activity, String username) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri to = Uri.parse("mailto:admin@boshanlu.com");
        intent.setData(to);
        intent.putExtra(Intent.EXTRA_SUBJECT, "博山庐手机客户端bug反馈 " + username);
        intent.putExtra(Intent.EXTRA_TEXT, "");

        activity.startActivity(intent);
    }

    public static boolean openStore(Context context) {
        try {
            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void shareApp(Context context, String data) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, data);
        shareIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(shareIntent, "分享博山庐手机客户端到："));
    }
}
