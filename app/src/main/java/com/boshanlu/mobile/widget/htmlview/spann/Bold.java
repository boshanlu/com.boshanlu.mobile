package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class Bold extends MetricAffectingSpan {


    @Override
    public void updateMeasureState(TextPaint p) {
        Heading.applyStyle(p, Typeface.BOLD);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        Heading.applyStyle(tp, Typeface.BOLD);
    }
}
