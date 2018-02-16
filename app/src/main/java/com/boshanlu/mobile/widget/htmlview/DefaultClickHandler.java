package com.boshanlu.mobile.widget.htmlview;

import android.content.Context;
import android.util.Log;

import com.boshanlu.mobile.utils.LinkClickHandler;
import com.boshanlu.mobile.widget.htmlview.callback.SpanClickListener;


/**
 * 链接点击事件处理内
 */
public class DefaultClickHandler implements SpanClickListener {
    private static final String TAG = DefaultClickHandler.class.getSimpleName();

    private Context context;

    public DefaultClickHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onSpanClick(int type, String source) {
        Log.d(TAG, "span click type is " + type + " source is:" + source);
        LinkClickHandler.handleClick(context, source);
    }
}
