package com.boshanlu.mobile.model;

/**
 * Created by free2 on 16-3-30.
 * 聊天数据
 */
public class ChatListData {

    private int type;//chat_bg_left or chat_bg_right
    private String UserImage;

    private String content;
    private String time;

    public ChatListData(int type, String userImage, String content, String time) {
        this.type = type;
        UserImage = userImage;
        this.content = content;
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserImage() {
        return UserImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
