package com.sungy.onegame;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sungy.onegame.activity.DetailActivity;
import com.sungy.onegame.mclass.DisplayUtil;
import com.sungy.onegame.mclass.OneGameGame;

public class FlipOneGameItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ViewHolder holder;
	private Context mContext;
	
	//缓存图片
	private Map<Integer,Bitmap> bitmaps;
	
	class ViewHolder{
		TextView game_title,game_praiseNo,game_abstract,game_day;
		ImageView game_praise,game_image;
		String game_image_url;
		LinearLayout gameContainer , footer;
		TextView date_month,date_day,date_week,date_week2;
		
	}
	
	//月份缩写
	private final String[] MONTHS = new String[]{
			"Jan","Feb","Mar","Apr","May","Jun",
			"Jul","Aug","Sep","Oct","Nov","Dec"
	};
	//星期中文
	private final String[] WEEKS_CN = new String[]{
		"星期一","星期二","星期三","星期四","星期五","星期六","星期日"	
	};
	//星期英文
	private final String[] WEEKS_EN = new String[]{
		"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"	
	};
	
	//是否已经点击跳转
	private boolean isStartActivity = false;
	//屏幕高度
	private int screenHeight;
	//状态栏高度
	private int stausBarHeight;
	//顶部高度
	private int headerHeight;
	//适屏比例
	private float containerPadingTopScale = 0.043f;
	private float imageScale = 0.409f;
	private float titleSizeScale = 0.051f;
	private float titlePaddingTopScale = 0.07f;
	private float abstracttSizeScale = 0.033f;
	private float abstracttPaddingTopScale =  0.01f;
	private float abstracttPaddingBottomScale =  0.01f;
	private float footerScale =  0.102f;
	private float praiseNoSizeScale =  0.041f;
	private float daySizeScale =  0.051f;
	private float dateMonthSizeScale =  0.031f;
	private float dateDaySizeScale =  0.062f;
	private float dateWeekSizeScale =  0.025f;
	private float dateWeekScale =  0.020f;

	public FlipOneGameItemAdapter(Context context,int screenHeight,int stausBarHeight,int headerHeight) {
		Log.d("screenHeight", screenHeight + "   "+ stausBarHeight+"   "+headerHeight);
		this.screenHeight = screenHeight;
		this.stausBarHeight = stausBarHeight;
		this.headerHeight = headerHeight;
		inflater = LayoutInflater.from(context);
		mContext = context;
		bitmaps = new HashMap<Integer,Bitmap>();
	}

	@Override
	public int getCount() {
		return SampleListFragment.gameList.size();
	}

	@Override
	public Object getItem(int position) {
		return SampleListFragment.gameList.get(position);
	}
	
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if (arg1 == null){
			arg1 = inflater.inflate(R.layout.game_item2, null);
			
			holder = new ViewHolder();
//			holder.game_day = (TextView)arg1.findViewById(R.id.day);
			holder.game_title = (TextView)arg1.findViewById(R.id.title);
			holder.game_abstract = (TextView)arg1.findViewById(R.id.abstractt);
			holder.game_praiseNo = (TextView)arg1.findViewById(R.id.praiseNo);
			
			holder.game_praise = (ImageView)arg1.findViewById(R.id.praise);
			holder.game_image = (ImageView)arg1.findViewById(R.id.image);
			
			holder.gameContainer = (LinearLayout) arg1.findViewById(R.id.game_container);
			holder.footer = (LinearLayout) arg1.findViewById(R.id.footer);
			
			holder.date_month = (TextView)arg1.findViewById(R.id.date_month);
			holder.date_day = (TextView)arg1.findViewById(R.id.date_day);
			holder.date_week = (TextView)arg1.findViewById(R.id.date_week);
			holder.date_week2 = (TextView)arg1.findViewById(R.id.date_week2);
			
			arg1.setTag(holder);
		}else{
			holder = (ViewHolder)arg1.getTag();
		}
		
		//高度适应屏幕
		int height = screenHeight - stausBarHeight - headerHeight;
		LayoutParams lp ;
		holder.gameContainer.setPadding(holder.gameContainer.getPaddingLeft(), (int) (height*containerPadingTopScale), holder.gameContainer.getPaddingRight(), 0);
		lp = (LayoutParams) holder.game_image.getLayoutParams();
		lp.height = (int) (height*imageScale);
		holder.game_image.setLayoutParams(lp);
		holder.game_title.setTextSize(DisplayUtil.px2sp(mContext, height*titleSizeScale));
		holder.game_title.getPaint().setFakeBoldText(true);
		holder.game_title.setPadding(holder.game_title.getPaddingLeft(), (int) (height*titlePaddingTopScale), holder.game_title.getPaddingRight(), holder.game_title.getPaddingBottom());
		holder.game_abstract.setTextSize(DisplayUtil.px2sp(mContext, height*abstracttSizeScale));
		Log.d("size",height*abstracttSizeScale+"");
		lp = (LayoutParams) holder.game_abstract.getLayoutParams();
		lp.topMargin = (int) (height*abstracttPaddingTopScale);
		lp.bottomMargin = (int) (height*abstracttPaddingBottomScale);
		holder.game_abstract.setLayoutParams(lp);
		lp = (LayoutParams) holder.footer.getLayoutParams();
		lp.height = (int) (height*footerScale);
		holder.footer.setLayoutParams(lp);
		holder.game_praiseNo.setTextSize(DisplayUtil.px2sp(mContext, height*praiseNoSizeScale));
//		holder.game_day.setTextSize(DisplayUtil.px2sp(mContext, height*daySizeScale));
		holder.date_month.setTextSize(DisplayUtil.px2sp(mContext, height*dateMonthSizeScale));
		holder.date_day.setTextSize(DisplayUtil.px2sp(mContext, height*dateDaySizeScale));
		holder.date_week.setTextSize(DisplayUtil.px2sp(mContext, height*dateWeekSizeScale));
		holder.date_week2.setTextSize(DisplayUtil.px2sp(mContext, height*dateWeekSizeScale));
		GridLayout.LayoutParams lp2;
		lp2 = (GridLayout.LayoutParams) holder.date_week.getLayoutParams();
		lp2.topMargin = (int) (-height*dateWeekScale);
		holder.date_week.setLayoutParams(lp2);
		lp2 = (GridLayout.LayoutParams) holder.date_week2.getLayoutParams();
		lp2.topMargin = (int) (-height*dateWeekScale);
		holder.date_week2.setLayoutParams(lp2);
		
		OneGameGame game = SampleListFragment.gameList.get(arg0);
		//显示正在加载
		holder.game_image.setImageResource(R.drawable.defalut);
//		holder.date_day.setText(SampleListFragment.gameList.get(arg0).getPublish_time().substring(8, 10));
		String date = SampleListFragment.gameList.get(arg0).getPublish_time();
		String day = date.substring(8, 10);
		String month = MONTHS[Integer.valueOf(date.substring(5, 7))-1];
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date2 = null;
		int w = 0;
		try {
			date2 = sdf.parse(date);
	        cal.setTime(date2);
	        w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String week_cn = WEEKS_CN[w];
		String week_en = WEEKS_EN[w];
		holder.date_day.setText(day);
		holder.date_month.setText(month);
		holder.date_week.setText(week_cn);
		holder.date_week2.setText(week_en);
		
		holder.game_title.setText(game.getGame_name());
		String str = game.getIntroduction();
		holder.game_abstract.setText(str);
		holder.game_praiseNo.setText(Integer.toString(game.getPraise_num()));

		holder.game_image_url = SampleListFragment.gameMap.get(SampleListFragment.gameList.get(arg0).getId()).getImage();
		//如果是本地图片，则加载
		if(!holder.game_image_url.contains("http://")){
			//检查缓存中是否存在图片
			if(bitmaps.containsKey(SampleListFragment.gameList.get(arg0).getId())){
				holder.game_image.setImageBitmap(bitmaps.get(SampleListFragment.gameList.get(arg0).getId()));
			}else{
				Bitmap bitmap = BitmapFactory.decodeFile(holder.game_image_url);
//				holder.game_image.setImageURI(Uri.fromFile(new File(holder.game_image_url)));
				bitmaps.put(SampleListFragment.gameList.get(arg0).getId(), bitmap);
				holder.game_image.setImageBitmap(bitmap);
				bitmap = null;
				Log.d("Uri", Uri.fromFile(new File(holder.game_image_url)).toString());
			}
		}
		
		holder.game_praise.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
			}
		});
		
		//点击跳转
		final int index = arg0;
		arg1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isStartActivity){
					isStartActivity = true;
					
					//添加加载中图片
					((MainActivity)mContext).getCenterFragment().addLoadingImage();
					Bundle b = new Bundle();
					b.putInt("index", index);
					Intent intent = new Intent(mContext, DetailActivity.class);
					intent.putExtras(b);
					mContext.startActivity(intent);
					((Activity) mContext).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
					Log.e("step","开始跳转");
					//几秒后去掉加载中图片
					new Timer(true).schedule(new TimerTask() {
						
						@Override
						public void run() {
//							myHandler.sendEmptyMessage(CANCLE_LOADING);
							((MainActivity)mContext).getCenterFragment().getUiHandler().sendEmptyMessage(SampleListFragment.CANCLE_LOADINGIMAGE);
							isStartActivity = false;
						}
					}, 1500);
				}
			}
		});
				
		return arg1;
	}

}
