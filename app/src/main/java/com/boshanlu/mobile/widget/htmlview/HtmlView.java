package com.boshanlu.mobile.widget.htmlview;

import android.content.Context;
import android.graphics.Point;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.boshanlu.mobile.widget.htmlview.callback.ImageGetter;
import com.boshanlu.mobile.widget.htmlview.callback.SpanClickListener;
import com.boshanlu.mobile.widget.htmlview.callback.ViewChangeNotify;

import java.lang.ref.WeakReference;


public class HtmlView implements ViewChangeNotify {

    public static final float LINE_HEIGHT = 1.4f;
    public static final int URL_COLOR = 0xff4078c0;
    private static final String TAG = HtmlView.class.getSimpleName();
    public static float FONT_SIZE = 40;
    public static int VIEW_WIDTH = 1080;

    private String source;
    private ImageGetter imageGetter;
    private SpanClickListener clickListener;
    private boolean isViewSet;
    private Spanned spanned;

    private WeakReference<TextView> target;
    private Runnable updateRunable = new Runnable() {
        public void run() {
            final TextView t = target.get();
            t.setText(spanned);
            Log.d(TAG, "notifyViewChange postInvalidateDelayed");
        }
    };

    private HtmlView(String source) {
        this.source = source;
        isViewSet = false;
    }

    public static HtmlView parseHtml(String source) {
        return new HtmlView(source);
    }

    public HtmlView setImageGetter(ImageGetter imageGetter) {
        this.imageGetter = imageGetter;
        return this;
    }

    public HtmlView setSpanClickListener(SpanClickListener listener) {
        this.clickListener = listener;
        return this;
    }

    public void into(TextView target) {
        if (this.target == null) {
            this.target = new WeakReference<>(target);
        }

        if (imageGetter == null) {
            WindowManager wm = (WindowManager) target.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Point p = new Point();
            wm.getDefaultDisplay().getSize(p);
            VIEW_WIDTH = p.x - target.getPaddingStart() - target.getPaddingEnd();
            imageGetter = new DefaultImageGetter(target.getContext(), VIEW_WIDTH);
        }

        if (clickListener == null) {
            clickListener = new DefaultClickHandler(target.getContext());
        }

        FONT_SIZE = target.getTextSize();
        spanned = SpanConverter.convert(source, imageGetter, clickListener, this);
        target.setMovementMethod(LinkMovementMethod.getInstance());
        target.setLinkTextColor(URL_COLOR);
        target.setLineSpacing(0, LINE_HEIGHT);
        target.setText(spanned);
        isViewSet = true;
    }

    @Override
    public void notifyViewChange() {
        if (target == null) return;
        final TextView t = target.get();
        if (isViewSet && t != null && spanned != null) {
            //这儿会有索引越界
            t.removeCallbacks(updateRunable);
            t.postDelayed(updateRunable, 200);
        }
    }

}
