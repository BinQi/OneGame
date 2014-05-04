package com.sungy.onegame;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.view.TVOffAnimation;

public class MainActivity extends FragmentActivity {

	private SlidingMenu mSlidingMenu;
	private LeftFragment leftFragment;
	private SampleListFragment centerFragment;
	private FragmentTransaction ft;

	// 记录是否在左边
	private boolean isInLeft = false;

	// 记录返回按键次数
	private int backCount = 0;
	private Timer timer;

	private ImageView image;
	private static final int STOPSPLASH = 0;
	private static final int RESET_BACKCOUNT = 1;
	private static final int ACTIVITY_OFF = 2;
	// time in milliseconds
	private static final long SPLASHTIME = 4000;
	// 是否在显示启动界面
	private boolean isShowingStarting = true;
	private boolean clickOnce;// 用户引导页的点击一次
	private boolean clickTwice;// 点击二次

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				// SystemClock.sleep(4000);
				image.setAnimation(AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.slide_out_left));
				image.setVisibility(View.GONE);
				guide();
				isShowingStarting = false;
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

		// 启动界面
		image = new ImageView(this);
		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		image.setLayoutParams(par);
		image.setImageResource(R.drawable.splsh_bg);
		image.setScaleType(ImageView.ScaleType.FIT_XY);
		mSlidingMenu.addView(image);
		Message msg = new Message();
		msg.what = STOPSPLASH;
		handler.sendMessageDelayed(msg, SPLASHTIME);

		// 网络是否可用判断
		// IntentFilter intentFilter = new IntentFilter();
		// intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// registerReceiver(connectionReceiver, intentFilter);

	}

	// 用户引导
	private void guide() {
		//判断用户是否第一次启动
		SharedPreferences mPreferences=getSharedPreferences("guide", MODE_PRIVATE);
		boolean once=mPreferences.getBoolean("once", true);
		if (once) {
			final FrameLayout mFrameLayout = new FrameLayout(this);
			FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mFrameLayout.setLayoutParams(mParams);
			mFrameLayout.setBackgroundResource(R.drawable.guide_bg);

			final ImageView guide1 = new ImageView(this);
			guide1.setImageResource(R.drawable.guide1);
			mFrameLayout.addView(guide1);

			clickOnce = false;
			clickTwice = false;
			mFrameLayout.setOnClickListener(new OnClickListener() {
				private ImageView guide2;
				private ImageView guide3;

				@Override
				public void onClick(View v) {
					if (!clickOnce) {
						//启动第二个引导
						guide1.setVisibility(View.GONE);
						guide2 = new ImageView(MainActivity.this);
						guide2.setImageResource(R.drawable.guide2);
						mFrameLayout.addView(guide2);
						clickOnce = true;
					}else if (!clickTwice) {
						//启动第三个引导
						guide2.setVisibility(View.GONE);
						guide3 = new ImageView(MainActivity.this);
						guide3.setImageResource(R.drawable.guide3);
						mFrameLayout.addView(guide3);
						clickTwice=true;
					}else {
						//把遮罩层去掉
						mFrameLayout.setVisibility(View.GONE);
					}
					
				}
			});
			mSlidingMenu.addView(mFrameLayout);
			//保存向导启动一次的数据
			Editor mEditor=mPreferences.edit();
			mEditor.putBoolean("once", false);
			mEditor.commit();
		}
		
	}

	public void showLeft() {
		mSlidingMenu.showLeftView();
		if (isInLeft) {
			isInLeft = false;
		} else {
			isInLeft = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isInLeft) {
				showLeft();
				return true;
			}
			backCount++;
			if (backCount == 1) {
				ToastUtils.showDefaultToast(this, "再按一次退出", Toast.LENGTH_SHORT);
				TimerTask task = new TimerTask() {
					public void run() {
						Message message = new Message();
						message.what = RESET_BACKCOUNT;
						handler.sendMessage(message);
					}
				};
				timer = new Timer(true);
				timer.schedule(task, 2000);
			} else if (backCount == 2) {
				mSlidingMenu.startAnimation(new TVOffAnimation());
				TimerTask task = new TimerTask() {
					public void run() {
						Message message = new Message();
						message.what = ACTIVITY_OFF;
						handler.sendMessage(message);
					}
				};
				timer = new Timer(true);
				timer.schedule(task, 500);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public SampleListFragment getCenterFragment() {
		return centerFragment;
	}

	public void setCenterFragment(SampleListFragment centerFragment) {
		this.centerFragment = centerFragment;
	}

	/**
	 * 网络是否可用Receiver
	 */
	private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo mobNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
				Log.d("connectionReceiver", "unconnect");
				ToastUtils.showDefaultToast(MainActivity.this, "网络不可用",
						Toast.LENGTH_SHORT);
			} else {

				// connect network
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (connectionReceiver != null) {
			unregisterReceiver(connectionReceiver);
		}
	}

	public boolean isInLeft() {
		return isInLeft;
	}

	public void setInLeft(boolean isInLeft) {
		this.isInLeft = isInLeft;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isShowingStarting)
			return true;
		return super.dispatchTouchEvent(ev);
	}

}
