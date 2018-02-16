package com.boshanlu.mobile.model;

import com.boshanlu.mobile.App;

/**
 * Created by free2 on 16-6-22.
 * gallery data
 */
public class GalleryData {
    private String imgurl;
    private String title;
    private String titleUrl;

    public GalleryData(String imgurl, String title, String titleUrl) {
        if (imgurl.startsWith("./")) {
            imgurl = App.BASE_URL + imgurl.substring(2);
        }
        this.imgurl = imgurl;
        this.title = title;
        this.titleUrl = titleUrl;
    }

    public String getImgurl() {
        return imgurl;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleUrl() {
        return titleUrl;
    }
}
