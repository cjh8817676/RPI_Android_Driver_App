package com.example.jni_spi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SpeedometerView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private float speed = 0;
    private static final float MAX_SPEED = 150f;
    private static final float START_ANGLE = 150f;
    private static final float SWEEP_ANGLE = 240f;

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
    }

    public void setSpeed(float speed) {
        this.speed = Math.min(speed, MAX_SPEED);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int padding = 40;

        arcRect.set(padding, padding, width - padding, height - padding);

        // Draw background arc
        paint.setColor(0xFFCCCCCC);
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, paint);

        // Draw speed arc
        paint.setColor(0xFF0000FF);
        float speedAngle = (speed / MAX_SPEED) * SWEEP_ANGLE;
        canvas.drawArc(arcRect, START_ANGLE, speedAngle, false, paint);

        // Draw speed text
        paint.setTextSize(50f);
        paint.setStyle(Paint.Style.FILL);
        String speedText = String.format("%.0f km/h", speed);
        float textWidth = paint.measureText(speedText);
        canvas.drawText(speedText, width/2f - textWidth/2f, height * 0.75f, paint);
    }
}