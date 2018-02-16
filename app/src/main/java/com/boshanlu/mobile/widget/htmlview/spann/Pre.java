package com.boshanlu.mobile.widget.htmlview.spann;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.LineBackgroundSpan;

public class Pre extends CharacterStyle implements LineBackgroundSpan {
    //padding: 16px;
    //overflow: auto;
    //font-size: 85%;
    //line-height: 1.45;
    //background-color: #f7f7f7;
    //border-radius: 3px;
    //monospace

    private static final float FONT_SIZE = 0.85f;
    private static final int BG_COLOR = 0xfff3f3f3;


    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTextSize(tp.getTextSize() * FONT_SIZE);
        tp.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        int color = p.getColor();
        p.setColor(BG_COLOR);
        c.drawRoundRect(left, top, right, bottom, 4, 4, p);
        p.setColor(color);
    }
}
