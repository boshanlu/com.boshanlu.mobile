package com.boshanlu.mobile.model;

/**
 * Created by free2 on 16-3-19.
 * 单个板块数据
 */
public class ForumListData {
    //title,img,url,actualnew
    public String title;
    public int fid;
    public String todayNew;
    //是不是头
    public boolean isheader;


    public ForumListData(boolean isheader, String title, int fid) {
        this.title = title;
        this.fid = fid;
        this.isheader = isheader;
    }
}
