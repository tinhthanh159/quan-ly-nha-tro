package com.example.quan_ly_tro.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View để vẽ chữ ký
 */
public class SignatureView extends View {
    
    private Paint paint;
    private Path path;
    private Bitmap bitmap;
    private Canvas canvas;
    
    private boolean isEdited = false;

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(15f);
        
        setBackgroundColor(Color.WHITE);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas(newBitmap);
        newBitmap.eraseColor(Color.WHITE); 
        
        if (bitmap != null) {
            newCanvas.drawBitmap(bitmap, 0, 0, null);
        }
        
        bitmap = newBitmap;
        canvas = newCanvas;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.drawPath(path, paint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                isEdited = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                return false;
        }
        
        invalidate();
        return true;
    }
    
    /**
     * Xóa chữ ký
     */
    public void clear() {
        path.reset();
        if (bitmap != null) {
            bitmap.eraseColor(Color.WHITE);
        }
        isEdited = false;
        invalidate();
    }
    
    /**
     * Kiểm tra xem đã ký chưa
     */
    public boolean isEdited() {
        return isEdited;
    }
    
    /**
     * Lấy bitmap chữ ký
     */
    public Bitmap getSignatureBitmap() {
        // Chụp lại toàn bộ View để đảm bảo có chữ ký
        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(result);
        draw(c);
        return result;
    }
}
