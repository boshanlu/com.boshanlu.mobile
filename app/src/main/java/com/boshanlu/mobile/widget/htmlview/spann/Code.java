package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class Code extends CharacterStyle {
    private static final float FONT_SIZE = 0.85f;
    private static final int BG_COLOR = 0xfff1f1f1;

    //padding: 0;
    //padding-top: 0.2em;
    //padding-bottom: 0.2em;
    //margin: 0;
    //font-size: 85%;
    //background-color: rgba(0,0,0,0.04);
    //border-radius: 3px;

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.bgColor = BG_COLOR;
        tp.setTextSize(tp.getTextSize() * FONT_SIZE);
        tp.setTypeface(Typeface.MONOSPACE);
    }
}
