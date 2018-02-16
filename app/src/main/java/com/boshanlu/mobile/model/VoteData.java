package com.boshanlu.mobile.model;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangl on 2017/5/24.
 * 投票
 */
public class VoteData {
    public String url;
    public List<Pair<String, String>> options;
    public int maxSelection;

    public VoteData(String url, List<Pair<String, String>> options, int maxSelection) {
        this.url = url;
        this.options = options;
        this.maxSelection = maxSelection;
    }

    public static VoteData parase(String s) {
        if (!s.contains("##")) return null;
        String[] ss = s.split("##");

        String url = ss[0];
        int maxSel = Integer.parseInt(ss[1]);
        List<Pair<String, String>> ops = new ArrayList<>();

        for (int i = 2; i < ss.length; i++) {
            ops.add(new Pair<>(ss[i].split("%%")[0], ss[i].split("%%")[1]));
        }

        return new VoteData(url, ops, maxSel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(url + "##" + maxSelection);

        for (Pair<String, String> p : options) {
            sb.append("##").append(p.first).append("%%").append(p.second);
        }

        return sb.toString();
    }
}
