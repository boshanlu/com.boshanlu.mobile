package com.boshanlu.mobile.widget.htmlview.callback;

/**
 * 通知tmlViewH内容发生了改变重新刷新view
 * 一般是由网络图片加载完成所导致的
 */
public interface ViewChangeNotify {
    void notifyViewChange();
}
