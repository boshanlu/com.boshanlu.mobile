package com.boshanlu.mobile.model;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by yangluo on 2017/3/23.
 * 一个小的板块
 */

public class Forum {
    public String name;
    public int fid;
    public boolean login;
    public List<Forum> subForum = new ArrayList<>();

    public Forum() {
    }

    public Forum(int fid, String name) {
        this.name = name;
        this.fid = fid;
    }

    public Forum(String name, int fid, boolean login, List subForum) {
        this.name = name;
        this.fid = fid;
        this.login = login;
        this.subForum = subForum;
    }
}
