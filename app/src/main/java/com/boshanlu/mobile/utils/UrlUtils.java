package com.boshanlu.mobile.utils;

import com.boshanlu.mobile.App;

/**
 * Created by free2 on 16-4-1.
 * 返回各种url;
 */
public class UrlUtils {


    public static String getArticleListUrl(int fid, int page) {
        return "forum.php?mod=forumdisplay&fid=" + fid + "&page=" + page + "&mobile=2";
    }

    public static String getSingleArticleUrl(String tid, int page) {
        String url = "forum.php?mod=viewthread&tid=" + tid;
        if (page > 1) {
            url += "&page=" + page;
        }
        return App.BASE_URL + url + "&mobile=2";
    }

    public static String getAddFrirndUrl(String uid) {
        return "home.php?mod=spacecp&ac=friend&op=add&uid=" + uid + "&inajax=1&mobile=2";
    }

    public static String getLoginUrl() {
        return "member.php?mod=logging&action=login&mobile=2";
    }

    public static String getAvatarUrl(String urlUid, String size) {
        String uid = urlUid;
        if (urlUid.contains("uid")) {
            uid = GetId.getId("uid=", urlUid);
        }
        String avatarSize;
        switch (size) {
            case "s":
                avatarSize = "small";
                break;
            case "b":
                avatarSize = "big";
                break;
            default:
                avatarSize = "middle";
        }
        return App.BASE_URL + "uc_server/avatar.php?uid=" + uid + "&size=" + avatarSize;
    }


    public static String getSignUrl() {
        return "plugin.php?id=dc_signin:sign";
    }

    public static String getUserHomeUrl(String uid) {
        return "home.php?mod=space&uid=" + uid + "&do=profile&mobile=2";
    }

    public static String getStarUrl(String id) {
        return "home.php?mod=spacecp&ac=favorite&type=thread&id=" + id + "&mobile=2&handlekey=favbtn&inajax=1";
    }

    public static String getPostUrl(int fid) {
        return App.BASE_URL + "forum.php?mod=post&action=newthread&fid=" + fid + "&mobile=2";
    }

    public static String getDeleteReplyUrl() {
        return "forum.php?mod=post&action=edit&extra=&editsubmit=yes&mobile=2&geoloc=&handlekey=postform&inajax=1";
    }

    public static String getUploadImageUrl() {
        return "misc.php?mod=swfupload&operation=upload&type=image&inajax=yes&infloat=yes&simple=2&mobile=2";
    }
}
