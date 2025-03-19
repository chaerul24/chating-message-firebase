package com.modern.chating.bubble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.modern.chating.R;

public class ChatBubbleView extends View {
    private Paint paint;
    private Path path;
    private int bgColor; // Warna latar belakang bubble
    private boolean isLeftEkor = false; // Default ekor di kanan

    public ChatBubbleView(Context context) {
        super(context);
        init(context, null);
    }

    public ChatBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ChatBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        path = new Path();


        paint.setColor(bgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = 32f; // Radius sudut

        path.reset();

        // Bubble utama dengan sudut melengkung
        RectF bubbleRect = new RectF(0, 0, width, height - 20);
        path.addRoundRect(bubbleRect, radius, radius, Path.Direction.CW);

        // Tambahkan ekor berdasarkan posisi
        if (isLeftEkor) {
            path.moveTo(0, height - 20);
            path.quadTo(-20, height - 10, 0, height);
            path.quadTo(10, height - 10, 0, height - 20);
        } else { // Default di kanan
            path.moveTo(width, height - 20);
            path.quadTo(width + 20, height - 10, width, height);
            path.quadTo(width - 10, height - 10, width, height - 20);
        }

        path.close();
        canvas.drawPath(path, paint);
    }

    // Setter buat ubah warna lewat kode
    public void setBgColor(int color) {
        bgColor = color;
        paint.setColor(bgColor);
        invalidate(); // Refresh tampilan
    }

    // Setter buat ubah posisi ekor lewat kode
    public void setEkorLeft(boolean isLeft) {
        isLeftEkor = isLeft;
        invalidate(); // Refresh tampilan
    }
}
