package com.reven.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawView extends View {
    private Path drawPath = new Path();
    private Paint drawPaint = new Paint();
    public DrawView(Context context) {
        super(context);
        initData();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initData();
    }

    private void initData() {
        drawPaint.setColor(Color.RED);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(event.getX(),event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(event.getX(),event.getY());
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(drawPath,drawPaint);
    }
}
