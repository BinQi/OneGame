package com.sungy.onegame.view;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.DashPathEffect;
import android.graphics.Paint;  
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;  
import android.util.AttributeSet;  
import android.widget.EditText;  
  
/** 
 * @author Linmiansheng 
 */  
public class NoteEditText extends EditText {  
      
//  private static final String TAG = "com.lms.todo.views.CustomEditText";  
    private Rect mRect;  
    private Paint mPaint;  
      
    private final int padding = 10;  
      
    private int lineHeight;  
    private int viewHeight,viewWidth;  
  
    public NoteEditText(Context context) {  
        this(context, null);  
    }  
  
    public NoteEditText(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
    }  
  
    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        init(context, attrs);  
    }  
  
    private void init(Context context, AttributeSet attrs) {  
        mRect = new Rect();  
        mPaint = new Paint();  
        mPaint.setStyle(Paint.Style.STROKE);  
        mPaint.setColor(Color.GRAY); 
  
        mPaint.setAntiAlias(true);  
          
        setFocusable(true);  
        setFocusableInTouchMode(true);          
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
        int count = getLineCount();  
        Rect r = mRect;           
        Paint paint = mPaint;  
        int lineHeight = 0;  
        int i = 0;          
        while (i < count) {  
            lineHeight = getLineBounds(i, r);  
            Path path = new Path();
            path.moveTo(r.left, lineHeight + padding);
            path.lineTo(r.right, lineHeight + padding);
            PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
            paint.setPathEffect(effects);
            canvas.drawPath(path, paint);  
            i++;  
        }  
        int maxLines = 15;  
        int avgHeight = lineHeight / count;  
        int currentLineHeight = lineHeight;  
          
        while(i < maxLines){  
            currentLineHeight = currentLineHeight + avgHeight + padding;
            Path path = new Path();
            path.moveTo(r.left, currentLineHeight);
            path.lineTo(r.right, currentLineHeight);
            PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
            paint.setPathEffect(effects);
            canvas.drawPath(path, paint);  
            //canvas.drawLine(r.left, currentLineHeight, r.right, currentLineHeight, paint);  
            i++;  
        }  
        super.onDraw(canvas);        
    }  
}  