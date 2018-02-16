package com.boshanlu.mobile.widget.htmlview.spann;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

import com.boshanlu.mobile.widget.htmlview.HtmlView;


public class Li implements LeadingMarginSpan {

    private static final float PADDING_L = HtmlView.FONT_SIZE;
    private static final int BULLET_RADIUS = (int) (HtmlView.FONT_SIZE / 8);
    private static Path sBulletPath = null;
    private final float mGapWidth = HtmlView.FONT_SIZE * 0.6f;

    public int getLeadingMargin(boolean first) {
        return (int) (PADDING_L + mGapWidth);
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldcolor = p.getColor();

            p.setStyle(Paint.Style.FILL);
            int mColor = 0xff333333;
            p.setColor(mColor);

            if (c.isHardwareAccelerated()) {
                if (sBulletPath == null) {
                    sBulletPath = new Path();
                    sBulletPath.addCircle(0.0f, 0.0f, 1.2f * BULLET_RADIUS, Path.Direction.CW);
                }

                c.save();

                c.translate(x + dir * (PADDING_L - BULLET_RADIUS),
                        (top + baseline) / 2.0f + BULLET_RADIUS);
                c.drawPath(sBulletPath, p);
                c.restore();
            } else {
                c.drawCircle(x + dir * (PADDING_L - BULLET_RADIUS),
                        (top + baseline) / 2.0f + BULLET_RADIUS, BULLET_RADIUS, p);
            }

            p.setColor(oldcolor);
            p.setStyle(style);
        }
    }
}
