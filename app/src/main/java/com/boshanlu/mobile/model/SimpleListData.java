package com.boshanlu.mobile.model;

public class SimpleListData {
    private String key;
    private String value;
    private String extradata;

    public SimpleListData(String key, String value, String extradata) {
        this.key = key;
        this.value = value;
        this.extradata = extradata;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtradata() {
        return extradata;
    }

}
