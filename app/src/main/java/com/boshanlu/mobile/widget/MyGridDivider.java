package com.boshanlu.mobile.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by free2 on 16-7-17.
 * grid layout 分割线
 */
public class MyGridDivider extends RecyclerView.ItemDecoration {
    private final Paint maint = new Paint();
    private int devideWidth = 2;

    public MyGridDivider(int devideWidth, int color) {

        this.devideWidth = devideWidth;
        maint.setColor(color);
        maint.setStyle(Paint.Style.STROKE);
        maint.setAntiAlias(true);
        maint.setStrokeWidth(devideWidth * 0.8f);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int left = child.getLeft();
            final int right = child.getRight();
            final int top = child.getTop();
            final int bottom = child.getBottom();
            c.drawRect(left + devideWidth / 2, top + devideWidth / 2, right - devideWidth / 2, bottom - devideWidth / 2, maint);
        }
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(-devideWidth / 2, -devideWidth / 2, devideWidth / 2, devideWidth / 2);
    }
}
