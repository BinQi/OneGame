package com.sungy.onegame.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sungy.onegame.R;

/**
 * author：惠钧
 * 效果：加载中效果显示
 */
public class LoadingImageView extends ImageView {
	
	private int[] loadingImgs = new int[]{R.drawable.loading_1,
											R.drawable.loading_2,
											R.drawable.loading_3,
											R.drawable.loading_4,
											R.drawable.loading_5,
											R.drawable.loading_6,
											R.drawable.loading_7,
											R.drawable.loading_8};
	private int loadingImgIndex = 0;
	private Timer timer ; 
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				loadingImgIndex++;
				loadingImgIndex = loadingImgIndex%8;
				postInvalidate();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public LoadingImageView(Context context) {
		super(context);
		init();
	}

	public LoadingImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LoadingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		TimerTask task = new TimerTask(){  
			public void run() {  
			Message message = new Message();      
			message.what = 1;      
			handler.sendMessage(message);    
			}  
		}; 
		timer = new Timer(true);
		timer.schedule(task,50, 50);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		setImageResource(loadingImgs[loadingImgIndex]);
	}
	
	public void destory(){
		timer.cancel();
	}
	
	

}
