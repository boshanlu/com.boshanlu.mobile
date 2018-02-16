package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;
import android.text.style.MetricAffectingSpan;

import com.boshanlu.mobile.widget.htmlview.HtmlView;


public class Heading extends MetricAffectingSpan implements LineHeightSpan, LineBackgroundSpan {
    private static final float[] TEXT_SIZE = new float[]{
            1.4f, 1.3f, 1.2f, 1.1f, 1.0f, 0.9f,
    };

    private static final int[] TEXT_COLOR = new int[]{
            0xff333333, 0xff333333, 0xff333333,
            0xff333333, 0xff333333, 0xff777777
    };

    private static final float LINE_HEIGHT = 1.25f / HtmlView.LINE_HEIGHT;

    private int level;

    public Heading(int level) {
        this.level = level;
    }

    public static void applyStyle(Paint paint, int style) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int want = oldStyle | style;

        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        } else {
            tf = Typeface.create(old, want);
        }

        int fake = want & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTextSize(tp.getTextSize() * TEXT_SIZE[level - 1]);
        tp.setColor(TEXT_COLOR[level - 1]);
        applyStyle(tp, Typeface.BOLD);
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        tp.setTextSize(tp.getTextSize() * TEXT_SIZE[level - 1]);
        tp.setColor(TEXT_COLOR[level - 1]);
        applyStyle(tp, Typeface.BOLD);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        fm.top *= LINE_HEIGHT;
        fm.ascent *= LINE_HEIGHT;
        fm.descent *= LINE_HEIGHT;
        fm.bottom *= LINE_HEIGHT;
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        //h1 h2 和最后一行
        if (level <= 2 && (((Spanned) text).getSpanEnd(this) == end - 1)) {
            int color = p.getColor();
            float width = p.getStrokeWidth();

            p.setColor(0xffeeeeee);
            p.setStrokeWidth(HtmlView.FONT_SIZE / 14);
            c.drawLine(left, baseline + HtmlView.FONT_SIZE / 2,
                    right, baseline + HtmlView.FONT_SIZE / 2, p);

            p.setColor(color);
            p.setStrokeWidth(width);
        }
    }
}
