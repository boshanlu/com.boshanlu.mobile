package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;

import com.boshanlu.mobile.widget.htmlview.HtmlView;


public class Quote extends CharacterStyle implements LeadingMarginSpan, LineHeightSpan {
    //padding: 0 1em;
    //color: #777;
    //border-left: 0.25em solid #ddd;
    //margin-bottom: 16px;
    private static final float STRIPE_WIDTH = HtmlView.FONT_SIZE / 5;
    private static final float GAP_WIDTH = HtmlView.FONT_SIZE * 0.6f;
    private static final int TEXT_COLOR = 0xff777777;
    private static final float LINE_HEIGHT = 1.25f / HtmlView.LINE_HEIGHT;

    public Quote() {
    }

    public int getLeadingMargin(boolean first) {
//        return (int) (STRIPE_WIDTH + GAP_WIDTH);
        return (int) GAP_WIDTH;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
//        画竖线
//        Paint.Style style = p.getStyle();
//        int color = p.getColor();
//
//        p.setStyle(Paint.Style.FILL);
//        p.setColor(0xffA1A1A1);
//
//        if (!first) {
//            top = top - (bottom - baseline);
//        } else {
//            top = (int) (top - STRIPE_WIDTH) + 5;
//        }
//
//        c.drawRect(x, top, x + dir * STRIPE_WIDTH, baseline + 5, p);
//
//        p.setStyle(style);
//        p.setColor(color);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setColor(TEXT_COLOR);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        fm.ascent *= LINE_HEIGHT;
        fm.descent *= LINE_HEIGHT;
        fm.top *= LINE_HEIGHT;
        fm.bottom *= LINE_HEIGHT;
    }
}
