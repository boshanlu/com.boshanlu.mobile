package com.boshanlu.mobile.widget.htmlview.spann;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.style.UpdateAppearance;

import com.boshanlu.mobile.widget.htmlview.AttrParser;
import com.boshanlu.mobile.widget.htmlview.HtmlNode;


//如果值为-1代表没有
// TODO: 2017/3/2 支持更多的style
public class StyleSpan extends MetricAffectingSpan
        implements UpdateAppearance {

    private static final float[] TEXT_SIZE = new float[]{
            0.95f, 1.0f, 1.05f, 1.1f, 1.15f, 1.2f, 1.25f
    };

    private int color;
    private int fontSize;

    public StyleSpan(HtmlNode.HtmlAttr attr) {
        this.color = attr.color;
        this.fontSize = attr.fontSize;
    }

    //更新attr <font color="red" size="5"><font color=blue>dffdfd</font></font>
    //当里层font 和外层font 在相同的位置时里层的相同属性优先
    //参数 外层attr
    public void updateStyle(HtmlNode.HtmlAttr attr) {
        if (this.color == AttrParser.COLOR_NONE) {
            this.color = attr.color;
        }

        if (this.fontSize < 0) {
            this.fontSize = attr.fontSize;
        }
    }


    @Override
    public void updateDrawState(TextPaint tp) {
        if (color != AttrParser.COLOR_NONE) {
            tp.setColor(color);
        }

//        if (bgClolr >= 0) {
//            tp.bgColor = bgClolr;
//        }

        if (fontSize > 0) {
            if (fontSize > TEXT_SIZE.length) {
                fontSize = TEXT_SIZE.length;
            }
            tp.setTextSize(tp.getTextSize() * TEXT_SIZE[fontSize - 1]);
        }
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        if (fontSize > 0) {
            if (fontSize > TEXT_SIZE.length) {
                fontSize = TEXT_SIZE.length;
            }
            tp.setTextSize(tp.getTextSize() * TEXT_SIZE[fontSize - 1]);
        }
    }
}
