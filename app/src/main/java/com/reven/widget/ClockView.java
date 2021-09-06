package com.reven.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.utils.BaseThreadPool;
import com.utils.TimeUtils;
import com.xm.ui.widget.drawgeometry.model.GeometryInfo;
import com.xm.ui.widget.drawgeometry.model.GeometryPoints;
import com.xm.ui.widget.drawgeometry.utils.GeometryUtils;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.xm.ui.widget.drawgeometry.utils.GeometryUtils.getSameAngleDifferRadius;

public class ClockView extends View {
    private GeometryPoints  pointsCircle;
    private int pointRadius = 50;
    private String showTimeInfo;
    private float time;
    private float curAngle;
    private boolean isNeedVibrate;
    public ClockView(Context context) {
        super(context);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        Path path = getCirclePath(new GeometryPoints(getWidth() / 2, (int)getHeight() / 2),0.3f,100f,3,100f);
        canvas.drawPath(path,paint);

        paint.setTextSize(150f);

        showTimeInfo = TimeUtils.formatTimes((int) time);
        float textHeightOffset = -(paint.ascent() + paint.descent()) * 0.5f;
        float textWidth = paint.measureText(showTimeInfo);
        canvas.drawText(showTimeInfo,(getWidth() - textWidth) / 2, getHeight() / 2 + textHeightOffset,paint);

        paint.setColor(Color.RED);

        canvas.drawCircle(pointsCircle.x,pointsCircle.y,pointRadius,paint);
    }


    private Path getCirclePath(GeometryPoints centerPoints,float lineWidth,float lineHeight,int angleCount,float padding) {
        Path path = new Path();
        path.reset();
        int i = 0;
        float circleAngle = (float) GeometryInfo.getAngle(pointsCircle.x - centerPoints.x,pointsCircle.y - centerPoints.y);
        while (i <= 360) {
            float angle;
            float afterPadding = padding;
            angle = (i + lineWidth + 360) % 360;

            if (Math.abs(circleAngle - angle) <= 15) {
                afterPadding = padding - (15 - Math.abs(circleAngle - angle)) * 3;
            }else if ((360 - Math.abs(circleAngle - angle)) <= 15) {
                afterPadding = padding - (15 - (360 - Math.abs(circleAngle - angle))) * 3;
            }

            GeometryPoints geometryPoints = getSameAngleDifferRadius(angle,centerPoints.x - lineHeight - padding,centerPoints);
            path.moveTo(geometryPoints.x,geometryPoints.y);

            angle = (i - lineWidth + 360) % 360;
            geometryPoints = getSameAngleDifferRadius(angle,centerPoints.x - lineHeight - padding,centerPoints);
            path.lineTo(geometryPoints.x,geometryPoints.y);

            angle = (i - lineWidth - 0.5f + 360) % 360;
            geometryPoints = getSameAngleDifferRadius(angle,centerPoints.x - afterPadding,centerPoints);
            path.lineTo(geometryPoints.x,geometryPoints.y);

            angle = (i + lineWidth + 0.5f + 360) % 360;
            geometryPoints = getSameAngleDifferRadius(angle,centerPoints.x - afterPadding,centerPoints);
            path.lineTo(geometryPoints.x,geometryPoints.y);

            angle = (i + lineWidth + 360) % 360;
            geometryPoints = getSameAngleDifferRadius(angle,centerPoints.x - lineHeight - padding,centerPoints);
            path.lineTo(geometryPoints.x,geometryPoints.y);

            i += angleCount;
        }

        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (GeometryUtils.isRotate(new GeometryPoints(event.getX(),event.getY()),pointsCircle)) {
                    stopCountDown();
                    return true;
                }else {
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                float angle = (float) GeometryUtils.getAngle(event.getX() - getWidth() / 2,event.getY() - getHeight() / 2);
                float time = this.time;

                if ((angle - curAngle % 360) > 300) {
                    if (time < 10) {
                        break;
                    }else {
                        curAngle -= (360 - angle);
                    }
                }

                if (angle < 100 && (curAngle % 360) > 300) {
                    curAngle += angle;
                }else {
                    curAngle += (angle - curAngle % 360);
                }


                time = curAngle / 6f;

                if (time < 0) {
                    time = 0;
                }


                if ((int)this.time != (int) time)  {
                    pointsCircle = GeometryUtils.getSameAngleDifferRadius(angle,
                            getWidth() / 2 - pointRadius - 200f,
                            new GeometryPoints(getWidth() / 2, getHeight() / 2));

                    if ((int) time % 2 == 0) {
                        vibrate();
                    }

                    postInvalidate();
                }

                synchronized (this) {
                    this.time = time;
                }

                System.out.println("angle:" + angle + "  curAngle:" + curAngle);

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (this.time > 0) {
                    startCountDown();
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBackgroundColor(Color.BLACK);
        pointsCircle = new GeometryPoints(getWidth() / 2,200f + ((getHeight() - getWidth()) / 2 + pointRadius));
    }


    private void vibrate() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(40);
    }

    private void startCountDown() {
        BaseThreadPool.getInstance().doTask(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    time--;
                }

                if(time >= 0) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            float angle = (time * 6f) % 360;
                            pointsCircle = GeometryUtils.getSameAngleDifferRadius(angle,
                                    getWidth() / 2 - pointRadius - 200f,
                                    new GeometryPoints(getWidth() / 2, getHeight() / 2));
                            postInvalidate();
                        }
                    });
                }else {
                    stopCountDown();
                }
            }
        },1,1);
    }

    private void stopCountDown() {
        BaseThreadPool.getInstance().cancelTask();
    }
}
