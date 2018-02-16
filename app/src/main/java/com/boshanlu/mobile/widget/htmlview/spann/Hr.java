package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import com.boshanlu.mobile.widget.htmlview.HtmlView;


public class Hr implements LineBackgroundSpan {
    //height: 0.25em;
    //padding: 0;
    //margin: 24px 0;
    //background-color: #e7e7e7;
    //border: 0;
    private static final int HEIGHT = (int) (HtmlView.FONT_SIZE / 4);
    private static final int COLOR = 0xffe7e7e7;

    @Override
    public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom, CharSequence charSequence, int start, int end, int num) {
        Paint.Style ostyle = paint.getStyle();
        int oColor = paint.getColor();
        float oWidth = paint.getStrokeWidth();

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(HEIGHT);
        paint.setColor(COLOR);

        canvas.drawLine(left, (top + bottom - 5) / 2.0f, right, (top + bottom - 5) / 2.0f, paint);

        paint.setStrokeWidth(oWidth);
        paint.setStyle(ostyle);
        paint.setColor(oColor);
    }
}
