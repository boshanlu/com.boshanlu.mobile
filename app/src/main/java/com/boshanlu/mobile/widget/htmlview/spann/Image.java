package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import com.boshanlu.mobile.widget.htmlview.HtmlView;

import java.lang.ref.WeakReference;


public class Image extends ReplacementSpan {

    private static final float LINE_HEIGHT = 1.0f / HtmlView.LINE_HEIGHT;
    private static final int PADDING = (int) HtmlView.FONT_SIZE / 5;
    private Drawable mDrawable;
    private WeakReference<Drawable> mDrawableRef;

    public Image(String source, Drawable d) {
        String source1 = source;
        mDrawable = d;
        mDrawableRef = new WeakReference<>(d);
    }


    public Drawable getDrawable() {
        return mDrawable;
    }

    //是否为内联
    //如果为行内图片就不需要考虑line height
    private boolean isInline() {
        return false;
    }

    private Drawable getCachedDrawable() {
        Drawable d = null;
        if (mDrawableRef != null)
            d = mDrawableRef.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }

        return d;
    }

    //返回宽度
    //可以设置fm 设置高度
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            if (isInline()) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;
            } else {
                //图片单独一行
                //重新计算line height
                fm.ascent = -(int) (rect.bottom * LINE_HEIGHT) - PADDING;
                fm.descent = (int) (HtmlView.FONT_SIZE * (HtmlView.LINE_HEIGHT - 1)) + PADDING;
            }

            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }

        return rect.right;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        if (b instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) b;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.isRecycled()) return;
        }


        canvas.save();
        int transY = 0;
        if (isInline()) {//base line对其
            transY = bottom - b.getBounds().bottom;
            transY -= paint.getFontMetricsInt().descent;
        } else {
            //单独一行上对其
            transY = top + PADDING;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
