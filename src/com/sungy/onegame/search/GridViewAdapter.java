package com.sungy.onegame.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sungy.onegame.R;
import com.sungy.onegame.activity.DetailActivityForResult;
import com.sungy.onegame.mclass.DownLoadUtils;
import com.sungy.onegame.mclass.OneGameGame;

public class GridViewAdapter extends BaseAdapter {

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private ViewHolder holder = null;
	
	//缓存图片
	private Map<Integer,Bitmap> bitmaps;
	//背景图ids
	private int[] bgItems = new int[]{R.drawable.bg_item0,R.drawable.bg_item1,R.drawable.bg_item2};

	//英文月份
	private String[] MonthEnglish = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	//字体
    private Typeface tf;
	//字体路径
    private String typeFaceDir = "fonts/font.ttf";
	protected boolean isStartActivity = false;
	
	//线程池
	private ExecutorService executorService = Executors.newFixedThreadPool(5);
	
	public GridViewAdapter(Context c){
		this.mContext = c;
		this.mInflater = LayoutInflater.from(c);
		
		//字体
        AssetManager mgr = mContext.getAssets();//得到AssetManager
        tf = Typeface.createFromAsset(mgr, typeFaceDir);//根据路径得到Typeface
        
        bitmaps = new HashMap<Integer,Bitmap>();
	}
	public int getCount() {
		// TODO Auto-generated method stub
		return SearchActivity.searchList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView image;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.search_item, null);
			holder.searchImage = (ImageView)convertView.findViewById(R.id.search_image);
			holder.searchDate = (TextView)convertView.findViewById(R.id.search_date);
			holder.searchName = (TextView)convertView.findViewById(R.id.search_name);
			convertView.setTag(holder);
			
			image = holder.searchImage;
		}
		else{
			holder = (ViewHolder)convertView.getTag();
			image = holder.searchImage;
		}
		
		//背景图
		convertView.setBackgroundResource(bgItems[position%3]);
		
		String date = SearchActivity.searchList.get(position).getPublish_time();
		String day = date.substring(8, 10);
		String month = MonthEnglish[Integer.valueOf(date.substring(5, 7))-1];
		date = day+"  "+month;
		
		holder.searchDate.setText(date);
		holder.searchDate.setTypeface(tf);
		holder.searchName.setText(SearchActivity.searchList.get(position).getGame_name());
		holder.searchName.setTypeface(tf);
		
		//显示默认图片
//		holder.searchImage.setImageResource(R.drawable.defalut);
		image.setImageResource(R.drawable.defalut);
		holder.game_image_url = SearchActivity.searchList.get(position).getImage();
//		//如果是本地图片，则加载
//		if(!holder.game_image_url.contains("http://")){
//			//检查缓存中是否存在图片
//			if(bitmaps.containsKey(SearchActivity.searchList.get(position).getId())){
//				holder.searchImage.setImageBitmap(bitmaps.get(SearchActivity.searchList.get(position).getId()));
//			}else{
//				Bitmap bitmap = BitmapFactory.decodeFile(holder.game_image_url);
//				bitmaps.put(SearchActivity.searchList.get(position).getId(), bitmap);
//				holder.searchImage.setImageBitmap(bitmap);
//				bitmap = null;
//			}
//		}
		
		final OneGameGame game = SearchActivity.searchList.get(position);
		
		//检查缓存中是否存在图片
		if(bitmaps.containsKey(SearchActivity.searchList.get(position).getId())){
//			holder.searchImage.setImageBitmap(bitmaps.get(SearchActivity.searchList.get(position).getId()));
			image.setImageBitmap(bitmaps.get(SearchActivity.searchList.get(position).getId()));
		}else{
			//如果是本地图片
			if(!holder.game_image_url.contains("http://")){
				Bitmap bitmap = BitmapFactory.decodeFile(holder.game_image_url);
				bitmaps.put(SearchActivity.searchList.get(position).getId(), bitmap);
//				holder.searchImage.setImageBitmap(bitmap);
				image.setImageBitmap(bitmap);
				bitmap = null;
				
			}else{	//如果是远程图片
				//线程池管理
				executorService.submit(new Runnable() {
					
					@Override
					public void run() {
						Bitmap bitmap = DownLoadUtils.downloadIcon(holder.game_image_url);
						bitmaps.put(game.getId(), bitmap);
						bitmap = null;
					}
				});
			}
		}
		
		//跳转
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isStartActivity){
					isStartActivity  = true;		
					
					//提示加载中
					((SearchActivity)mContext).addLoadingImage();
					
					Bundle b = new Bundle();
					b.putSerializable("game",game);
					Intent intent = new Intent(mContext, DetailActivityForResult.class);
					intent.putExtras(b);
					mContext.startActivity(intent);
					((Activity) mContext).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
					
					//撤销加载中
					new Timer(true).schedule(new TimerTask() {

						@Override
						public void run() {
							((SearchActivity)mContext).getMyHandler().sendEmptyMessage(SearchActivity.CANCLE_LOADING_IMAGE);
							isStartActivity = false;
						}
					}, 1500);
				}
			}
		});

				
		return convertView;
	}
	class ViewHolder{
		ImageView searchImage;
		TextView searchDate,searchName;
		String game_image_url;
	}
	
	
	/**
	 * 释放内存
	 */
	public void destory(){
		Iterator<Integer> it = bitmaps.keySet().iterator();
		Bitmap bitmap = null;
		while(it.hasNext()){
			bitmap = bitmaps.get(it.next());
			bitmap = null;
		}
		if(bitmaps != null &&	bitmaps.size() !=0){
			bitmaps.clear();
		}
	}

}

