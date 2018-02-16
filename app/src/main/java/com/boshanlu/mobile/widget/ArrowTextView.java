package com.boshanlu.mobile.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.boshanlu.mobile.R;

/**
 * Created by free2 on 16-3-21.
 * 带小三角的textview 用在消息列表
 */
public class ArrowTextView extends android.support.v7.widget.AppCompatTextView {


    private int color = ContextCompat
            .getColor(getContext(), R.color.bg_secondary);

    private Paint paint = new Paint();
    private Path path = new Path();


    public ArrowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArrowTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ArrowTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(color == 0 ? Color.RED : color);
        paint.setAntiAlias(true);


        float arrowInHeight = 10;
        drawRound(canvas, arrowInHeight);

        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(80, 0);
        path.lineTo(68, arrowInHeight);
        path.lineTo(92, arrowInHeight);
        path.lineTo(80, 0);
        path.close();
        canvas.drawPath(path, paint);

        super.onDraw(canvas);

    }


    private void drawRound(Canvas canvas, float arrowInHeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(0, arrowInHeight, getWidth(), getHeight(), 4, 4, paint);
            return;
        }
        RectF rectF = new RectF(0, arrowInHeight, getWidth(), getHeight());
        canvas.drawRoundRect(rectF, 4, 4, paint);
    }

}
