package com.boshanlu.mobile.widget.htmlview.callback;

/**
 * span点击事件
 * eg. link image and so on
 * type: @HtmlTag
 * url: if link->url,if is image->src
 */
public interface SpanClickListener {
    void onSpanClick(int type, String source);
}
