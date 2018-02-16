package com.boshanlu.mobile.listener;

import android.support.v7.widget.RecyclerView;

/**
 * Created by free2 on 16-3-19.
 * 监听RecyclerView 滚动隐藏view
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
    private int HIDE_THRESHOLD = 15;//隐藏触发距离
    private int SHOW_THRESHOLD = 10;//显示触发距离
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    public HidingScrollListener(int hide) {
        this.HIDE_THRESHOLD = hide;
    }

    public HidingScrollListener(int hide, int show) {
        this.HIDE_THRESHOLD = hide;
        this.SHOW_THRESHOLD = show;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -SHOW_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public abstract void onHide();

    public abstract void onShow();

}
