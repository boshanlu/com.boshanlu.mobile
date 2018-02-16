package com.boshanlu.mobile.model;

/**
 * Created by free2 on 16-3-21.
 * 我的帖子
 * 我的回复 list data
 */
public class MessageData {

    private ListType type;
    private String title;
    private String titleUrl;
    private String authorImage;
    private String time;  //在我的回复当作内容
    private String content;
    private boolean isRead = true;

    //我的消息 ////回复我的
    public MessageData(ListType type, String title, String titleUrl, String authorImage, String time, boolean isRead, String content) {
        this.type = type;
        this.title = title;
        this.titleUrl = titleUrl;
        this.authorImage = authorImage;
        this.time = time;
        this.content = content;
        this.isRead = isRead;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getcontent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public String getauthorImage() {
        return authorImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ListType getType() {
        return type;
    }
}
