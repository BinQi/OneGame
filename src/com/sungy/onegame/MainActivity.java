package com.sungy.onegame;

import java.util.Timer;
import java.util.TimerTask;

import com.sungy.onegame.activity.DetailActivity;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.view.TVOffAnimation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	private SlidingMenu mSlidingMenu;
	private LeftFragment leftFragment;
	private SampleListFragment centerFragment;
	private FragmentTransaction ft;
	
	//记录返回按键次数
	private int backCount = 0;
	private Timer timer ;
	
	private ImageView image;
	private static final int STOPSPLASH = 0;  
	private static final int RESET_BACKCOUNT = 1;  
	private static final int ACTIVITY_OFF = 2;  
    // time in milliseconds  
    private static final long SPLASHTIME = 4000;  

  
    private Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
	            case STOPSPLASH:  
	//                SystemClock.sleep(4000);   
	            	image.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left));
	                image.setVisibility(View.GONE);  
	                break;
	            case RESET_BACKCOUNT:
	            	timer.cancel();
	            	backCount = 0;
	            	break; 
	            case ACTIVITY_OFF:
					android.os.Process.killProcess(android.os.Process.myPid());
	            	break;
            }  
            super.handleMessage(msg);  
        }  
    };  
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mSlidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
		mSlidingMenu.setLeftView(getLayoutInflater().inflate(
				R.layout.left_frame, null));
		mSlidingMenu.setCenterView(getLayoutInflater().inflate(
				R.layout.center_frame, null));
		
		ft = this.getSupportFragmentManager().beginTransaction();
		leftFragment = new LeftFragment();
		ft.replace(R.id.left_frame, leftFragment);
		
		centerFragment = new SampleListFragment();
		ft.replace(R.id.center_frame, centerFragment);
		ft.commit();

		leftFragment.setListener();
		Global.setListener(leftFragment.getListner());

		//启动界面
		image = new ImageView(this);
		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		image.setLayoutParams(par);
		image.setImageResource(R.drawable.splsh_bg_conew1);
		image.setScaleType(ImageView.ScaleType.FIT_XY);
		mSlidingMenu.addView(image);	
        Message msg = new Message();  
        msg.what = STOPSPLASH;  
        handler.sendMessageDelayed(msg, SPLASHTIME);  
	}

	public void showLeft() {
		mSlidingMenu.showLeftView();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			new AlertDialog.Builder(this)
//					.setTitle("提示")
//					.setMessage("您确定要退出？")
//					.setNegativeButton("取消",
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//								}
//							})
//					.setPositiveButton("确定",
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int whichButton) {
////									finish();
//									android.os.Process.killProcess(android.os.Process.myPid());
//								}
//							}).show();
			backCount++;
			if(backCount == 1){
				ToastUtils.showDefaultToast(this, "再按一次退出", Toast.LENGTH_SHORT);
				TimerTask task = new TimerTask(){  
					public void run() {  
					Message message = new Message();      
					message.what = RESET_BACKCOUNT;      
					handler.sendMessage(message);    
					}  
				}; 
				timer = new Timer(true);
				timer.schedule(task,2000);
			}else {
				mSlidingMenu.startAnimation(new TVOffAnimation());
				TimerTask task = new TimerTask(){  
					public void run() {  
					Message message = new Message();      
					message.what = ACTIVITY_OFF;      
					handler.sendMessage(message);    
					}  
				}; 
				timer = new Timer(true);
				timer.schedule(task,500);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
}
