package leikyahiro.com.microphonerecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Yahor on 20.03.2016.
 * (C) All rights reserved.
 */
public class WaterView extends View {
    private Paint mTextPaint;
    private Paint mDefPaint;
    private Paint mDefWaterPaint;
    private Path path;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int x3;
    private int y3;
    private int x4;
    private int y4;
    private int mLevel = 0;

    public WaterView(Context context) {
        super(context);
        initPaint();
    }

    public void incrementWaterLevel(int level) {
        this.mLevel += level;
        invalidate();
    }

     public void settWaterLevel(int level) {
        this.mLevel = level;
    }

    private void initPaint() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(12);

        mDefPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDefPaint.setStrokeWidth(10);
        mDefPaint.setColor(Color.BLACK);
        mDefPaint.setStyle(Paint.Style.STROKE);

        mDefWaterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDefWaterPaint.setStrokeWidth(10);
        mDefWaterPaint.setColor(Color.parseColor("#33ccff"));
        mDefWaterPaint.setStyle(Paint.Style.FILL);
        path = new Path();
    }

    public WaterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public WaterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        x1 = 0;
        y1 = 50;
        x2 = 0;
        y2 = getHeight();
        x3 = getWidth();
        y3 = getHeight();
        x4 = getWidth();
        y4 = 50;

        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.close();



        canvas.drawPath(path, mDefPaint);

        canvas.drawRect(x1+10, getCoeff(y2), x4-10, y2-10, mDefWaterPaint);
    }

    private int getCoeff(int y2) {
        int res = y2 - (mLevel + 10 + 60);
        if(res < 0) {
            mLevel-=20;
        }
        return y2 - (mLevel + 10);
    }
}
