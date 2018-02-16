package com.boshanlu.mobile.model;

/**
 * Created by yangluo on 2017/3/23.
 * 一个小的板块
 */

public class Forum {
    public String name;
    public int fid;
    public boolean login;

    public Forum() {
    }

    public Forum(int fid, String name) {
        this.name = name;
        this.fid = fid;
    }

    public Forum(String name, int fid, boolean login) {
        this.name = name;
        this.fid = fid;
        this.login = login;
    }
}
