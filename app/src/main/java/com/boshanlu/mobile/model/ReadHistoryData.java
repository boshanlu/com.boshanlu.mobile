package com.boshanlu.mobile.model;


public class ReadHistoryData {
    public String tid;
    public String title;
    public String readTime;
    public String author;

    public ReadHistoryData(String tid, String title, String readTime, String author) {
        this.tid = tid;
        this.title = title;
        this.readTime = readTime;
        this.author = author;
    }
}
