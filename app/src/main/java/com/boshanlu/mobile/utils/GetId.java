package com.boshanlu.mobile.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.boshanlu.mobile.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by free2 on 16-3-19.
 * 通过链接获得tid
 * uid
 * 各种id
 */
public class GetId {

    public static String getId(String url) {
        return getId("", url);
    }

    public static String getId(String prefix, String url) {
        Pattern pattern = null;
        int len = 0;
        if (TextUtils.isEmpty(prefix)) {
            pattern = Pattern.compile("[0-9]+");
        } else {
            len = prefix.length();
            pattern = Pattern.compile(prefix + "[0-9]+");
        }
        Matcher matcher = pattern.matcher(url);

        String id = "";
        if (matcher.find()) {
            id = url.substring(matcher.start() + len, matcher.end());
        }
        return id;
    }

    public static int getFloor(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        int floor = 0;
        if (text.contains("沙发")) {
            floor = 1;
        } else if (text.contains("板凳")) {
            floor = 2;
        } else if (text.contains("地板")) {
            floor = 3;
        } else {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(text.substring(matcher.start(), matcher.end()));
            }
        }

        return floor;
    }


    public static String getHash(String url) {
        try {
            //fid=[0-9]+
            Pattern pattern = Pattern.compile("formhash=.*&");
            Matcher matcher = pattern.matcher(url);
            String hash = "";
            if (matcher.find()) {
                hash = url.substring(matcher.start() + 9, matcher.end() - 1);
            }

            return hash;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


    public static int getNumber(String text) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(text);
        String num = "0";
        if (matcher.find()) {
            num = text.substring(matcher.start(), matcher.end());
        }
        return Integer.parseInt(num);
    }


    public static int getPage(String url) {
        //forum.php?mod=redirect&goto=findpost&ptid=846689&pid=21330831
        Pattern pattern = Pattern.compile("page=[0-9]+");
        Matcher matcher = pattern.matcher(url);
        int page = 1;
        if (matcher.find()) {
            page = Integer.parseInt(url.substring(matcher.start() + 5, matcher.end()));
        }
        return page;
    }

    public static int getFroumFid(String url) {
        String fid = getId("fid=", url);
        if (TextUtils.isEmpty(fid)) {
            return -1;
        } else {
            if (fid.equals("106")) {
                fid = "110";
            } else if (fid.equals("553")) {
                fid = "554";
            }
            return Integer.parseInt(fid);
        }
    }

    //htmlcolor 转换成android 的 int color
    public static int getColor(Context c, String str) {

        // style="color: #EC1282;">
        int color = ContextCompat.getColor(c, R.color.text_color_pri);
        if (str.contains("color")) {
            int start = str.indexOf("color");
            int end = str.indexOf(";", start);
            String temp = str.substring(start, end);

            int start_c = temp.indexOf("#");

            String colorStr = temp.substring(start_c).trim();
            try {
                color = Color.parseColor(colorStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (str.startsWith("#")) {
            try {
                color = Color.parseColor(str);
            } catch (Exception e) {
                Log.e("color", color + "");
                e.printStackTrace();
            }
        }
        return color;
    }
}
