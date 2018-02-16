package com.boshanlu.mobile.widget.htmlview.spann;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;


public class Sub extends MetricAffectingSpan {

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
    }
}
