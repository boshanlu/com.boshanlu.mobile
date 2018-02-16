package com.boshanlu.mobile.model;

/**
 * Created by free2 on 16-3-7.
 * <p>
 * 各个板块文章列表数据
 */
public class ArticleListData {

    //主页面文章列表item
    public String tag;
    public String title;
    public String titleUrl;
    public String type; //置顶 金币 普通
    public String author;
    public String authorUrl;
    public String postTime;
    public String viewCount;
    public String replayCount;
    public boolean isRead;
    public boolean ishaveImage;
    public String imUrl;
    public int titleColor = 0xff000000;//文章颜色


    //一般文章构造器
    public ArticleListData(String type, String title, String titleUrl, String author, String authorUrl, String postTime, String viewCount, String replayCount, int titleColor) {
        this.type = type;//置顶 精华 金币。。。
        this.title = title;
        this.titleUrl = titleUrl;
        this.author = author;
        this.authorUrl = authorUrl;
        this.postTime = postTime;
        this.viewCount = viewCount;
        this.replayCount = replayCount;
        this.titleColor = titleColor;
    }

    //手机版构造器
    public ArticleListData(boolean haveImage, String title, String titleUrl, String author, String replayCount, int titleColor) {
        this.ishaveImage = haveImage;//0--have image
        this.title = title;
        this.titleUrl = titleUrl;
        this.author = author;
        this.replayCount = replayCount;
        this.titleColor = titleColor;
    }

    //图片分类构造
    public ArticleListData(String title, String titleUrl, String imageurl, String author, String replyCount) {
        this.title = title;
        this.titleUrl = titleUrl;
        this.imUrl = imageurl;
        this.author = author;
        this.replayCount = replyCount;
    }
}
