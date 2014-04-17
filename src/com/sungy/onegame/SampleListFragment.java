package com.sungy.onegame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sungy.onegame.activity.DetailActivity;
import com.sungy.onegame.flipview.FlipCards;
import com.sungy.onegame.flipview.FlipViewController;
import com.sungy.onegame.mclass.DateUtils;
import com.sungy.onegame.mclass.DownLoadUtils;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.OneGameGame;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.onegameprovider.OneGameColumn;
import com.sungy.onegame.onegameprovider.OneGameProvider;
import com.sungy.onegame.view.LoadingImageView;

public class SampleListFragment extends ListFragment {
	public final String TAG = "SampleListFragment";
	private ImageView iv_left;
	private ImageView iv_right;
	public static List<OneGameGame> gameList;
	public static Map<Integer,OneGameGame> gameMap;
	OneGameItemAdapter adapter;
	
	private ListView list;
	private int locItem = 0;
	private int fY = 0;
	
	public final static int STOPSPLASH = 0; 
	public final static int SET_ADAPTER = 1;
	public final static int REFLASH_ADAPTER = 2;
	public final static int MAKE_TOAST = 3;
	public final static int RESET_ISLOADING = 4;
	public final static int ADD_LOADINGIMAGE = 5;
	public final static int CANCLE_LOADINGIMAGE = 6;
	private Handler uiHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == SET_ADAPTER){
//				setListAdapter(adapter);
				flipView.setAdapter(oneGameAdapter);	
			}
			else if(msg.what == STOPSPLASH){
				loading.setVisibility(View.GONE);  
			}else if(msg.what == REFLASH_ADAPTER){
				if(oneGameAdapter != null){
					oneGameAdapter.notifyDataSetChanged();
				}
			}else if(msg.what == MAKE_TOAST){
				ToastUtils.showDefaultToast(getActivity(), (String)msg.obj, Toast.LENGTH_SHORT);
			}else if(msg.what == RESET_ISLOADING){
				isPullDownLoading = false;
			}else if(msg.what == ADD_LOADINGIMAGE){
				addLoadingImage();
			}else if(msg.what == CANCLE_LOADINGIMAGE){
				cancleLoadingImage();
			}
			super.handleMessage(msg);  
		}
		
	};
	
	private LinearLayout loading; 
	private LinearLayout loadingImageWarpper; 
	private LoadingImageView loadingImage; 
    private static final long SPLASHTIME = 1000;  
   
    private RelativeLayout layout;
    private FlipViewController flipView;
    private FlipOneGameItemAdapter oneGameAdapter;
    //为下拉加载用
    private boolean isPullDownLoading = false;
    private FlipCards.OneGameListener listener = new FlipCards.OneGameListener() {
		
		@Override
		public void pullDownLoad() {
			if(!isPullDownLoading){
				isPullDownLoading = true;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						//显示加载中图片
						uiHandler.sendEmptyMessage(ADD_LOADINGIMAGE);
						Message msg2 = new Message();
						if(pullDown()){
							uiHandler.sendEmptyMessage(REFLASH_ADAPTER);
							msg2.obj = "已加载";
							msg2.what = MAKE_TOAST;
							uiHandler.sendMessage(msg2);
						}else{
							msg2.obj = "没有更多数据";
							msg2.what = MAKE_TOAST;
							uiHandler.sendMessage(msg2);
						}
						//去掉加载中图片
						uiHandler.sendEmptyMessage(CANCLE_LOADINGIMAGE);
						Message msg3 = new Message();
						msg3.what = RESET_ISLOADING;
						uiHandler.sendMessageDelayed(msg3, 3*SPLASHTIME);
					}
				}).start();
			}
		}
	};
	
	//屏幕高度
	private int screenHeight;
	//状态栏高度
	private int stausBarHeight;
	//顶部高度
	private int headerHeight;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
		    c = Class.forName("com.android.internal.R$dimen");
		    obj = c.newInstance();
		    field = c.getField("status_bar_height");
		    x = Integer.parseInt(field.get(obj).toString());
		    sbar = getResources().getDimensionPixelSize(x);
		} catch(Exception e1) {
		    e1.printStackTrace();
		}
		
		gameList = new ArrayList<OneGameGame>();
		gameMap = new HashMap<Integer, OneGameGame>();
		View mView = inflater.inflate(R.layout.list, null);
		iv_left = (ImageView) mView.findViewById(R.id.iv_left);
		iv_right = (ImageView) mView.findViewById(R.id.iv_right);
		
		stausBarHeight = sbar; 
		LinearLayout header = (LinearLayout) mView.findViewById(R.id.header); 
		header.measure(0, 0);
		headerHeight = header.getMeasuredHeight();
		
		//ListView
		list = (ListView)mView.findViewById(android.R.id.list);		
        loading = (LinearLayout) mView.findViewById(R.id.loading); 
       
        //卡片
		flipView = new FlipViewController(getActivity());
		if(!isEmptyDB()){
			//加载历史数据
			getMapFromDB();
			oneGameAdapter = new FlipOneGameItemAdapter(getActivity(),screenHeight,stausBarHeight,headerHeight);
			flipView.setAdapter(oneGameAdapter);
			//若想进入时显示历史数据，去掉注释下面那行
			uiHandler.sendEmptyMessage(STOPSPLASH);
		}
		
//		LinearLayout layout = (LinearLayout) mView.findViewById(R.id.listlayout);
//		layout.addView(flipView);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.BELOW, R.id.header);
		flipView.setLayoutParams(lp);
		layout = (RelativeLayout) mView.findViewById(R.id.listlayout);
		layout.addView(flipView);
	
		//为下拉加载而用
		flipView.setOneGameListener(listener);
				
		return mView;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(!isEmptyDB()){
					Log.d("load","notempty");
					getGameMap();
					gameList.clear();
					gameMap.clear();
					getMapFromDB();
					uiHandler.sendEmptyMessage(REFLASH_ADAPTER);
					//若想进入时显示历史数据，注释下面三行
//					Message msg = new Message();
//					msg.what = STOPSPLASH;
//					uiHandler.sendMessageDelayed(msg, SPLASHTIME);
				}else{
					Log.d("load","empty");
					getGameMap();
					getMapFromDB();
					oneGameAdapter = new FlipOneGameItemAdapter(getActivity(),screenHeight,stausBarHeight,headerHeight);
					uiHandler.sendEmptyMessage(SET_ADAPTER);
					Message msg = new Message();
					msg.what = STOPSPLASH;
					uiHandler.sendMessageDelayed(msg, SPLASHTIME);
				}
			}
		}).start();
		
		iv_left.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				((MainActivity) getActivity()).showLeft();
			}
		});
		
		iv_right.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
			}
		});

	}
	
	private void getGameMap() {	
		
		//获取数据库保存的最新一条数据的日期
		Cursor cur = getActivity().getContentResolver().query(OneGameProvider.CONTENT_URI, null, null, null, OneGameColumn.ONEGAMEPUBLISHTIME + " ASC");
		long days = 11;
		String lastPublishTime = DateUtils.getDate();
		if(cur.getCount()>0){
			cur.moveToPosition(cur.getCount()-1);
			lastPublishTime = cur.getString(cur.getColumnIndex(OneGameColumn.ONEGAMEPUBLISHTIME)); 
			//计算最后的日期与今天相差天数
			String currentDay = DateUtils.getDate();
			days = DateUtils.getQuot(currentDay, lastPublishTime);
		}
		if(cur != null)
		{
			cur.close();
		}
		Log.e("day", days+"");
		
		//从后台获取数据
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pageSize","10")); 
		params.add(new BasicNameValuePair("pageNo","1")); 
		String str = HttpUtils.doPostWithoutStrict(Global.GAME_CURRENTDAYLIST, params);
		
		//若相差时间小于10天则更新
		if(days<=10){
			parseJson2DBAndReflash(str);
		}else{				
			//如果大于10天则把以前的记录清除，重新加载
			getActivity().getContentResolver().delete(OneGameProvider.CONTENT_URI, null, null);
			parseJson2DBAndReflash(str);
		}
	}

	//跳转activity
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Bundle b = new Bundle();
		b.putInt("index", position);
		Intent intent = new Intent(getActivity(), DetailActivity.class);
		intent.putExtras(b);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		Log.e("step","开始跳转");
	}
	
	public void onResume(){
		super.onResume();
		if(adapter!=null)
			adapter.notifyDataSetChanged();
		if(flipView!=null)
			flipView.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(flipView!=null)
			flipView.onPause();
	}
	
	/**
	 * 解释json到DB并且把数据添加到gamelist和gameMap里
	 * @param str
	 * @return 若有新数据，return true，否则 return false
	 */
	private boolean parseJson2DBAndAddGame(String str){
		if(str!="error"&&str!="exception"){
			JSONObject json;
			JSONArray info = new JSONArray();
			final ContentResolver cr = getActivity().getContentResolver();
			try {			
				json = new JSONObject(str);
				info = json.getJSONArray( "listData" );
				
				if(info.length() == 0){
					return false;
				}
				
				int loc = gameList.size()-1;
				for(int i = 0; i < info.length() ; i++){ 				
					final JSONObject jsonObj = ((JSONObject)info.opt(i)); 
					final int gameId = jsonObj.getInt("id");
					
					ContentValues values = new ContentValues();
		            
					values.put(OneGameColumn.ONEGAMEID,jsonObj.getInt("id"));
					values.put(OneGameColumn.ONEGAMEPUBLISHTIME,jsonObj.getString("publish_time"));
					values.put(OneGameColumn.ONEGAMENAME,jsonObj.getString("game_name"));
					values.put(OneGameColumn.ONEGAMEIMAGE,jsonObj.getString("image"));
					values.put(OneGameColumn.ONEGAMEINTRODUCTION,jsonObj.getString("introduction"));
					values.put(OneGameColumn.ONEGAMEDETAIL,jsonObj.getString("detail"));
					values.put(OneGameColumn.ONEGAMEPRAISENUM, jsonObj.getInt("praise_num"));
					//new
					values.put(OneGameColumn.ONEGAMEDOWNLOADURL, jsonObj.getString("download_url"));
					values.put(OneGameColumn.ONEGAMEDETAILIMAGE, jsonObj.getString("detail_image"));
					
					//存入数据库
					cr.insert(OneGameProvider.CONTENT_URI, values);
					
					//存入list和map
					OneGameGame game = new OneGameGame();  	
					game.setId(gameId);
					game.setPublish_time(jsonObj.getString("publish_time"));
					game.setGame_name(jsonObj.getString("game_name"));
					game.setIntroduction(jsonObj.getString("introduction"));
					game.setDetail(jsonObj.getString("detail"));
					game.setPraise_num(jsonObj.getInt("praise_num"));
					game.setImage(jsonObj.getString("image"));
					//new 
					game.setDownload_url(jsonObj.getString("download_url"));
					game.setDetail_image(jsonObj.getString("detail_image"));
					
					gameMap.put(gameId, game);
					gameList.add(game);
					//为收藏夹所用
					Global.getDetailList().put(String.valueOf(gameId), loc);
					loc++;
					
					new Thread(){
		                @Override
		                public void run() {
		                    try {
								String path = DownLoadUtils.downloadFile(jsonObj.getString("image"),getActivity());
								//修改数据库图片路径
								if(!path.equals("")	&& path!=null){
									ContentValues cv = new ContentValues();
									cv.put(OneGameColumn.ONEGAMEIMAGE, path);
									String[] args = {String.valueOf(gameId)};
									cr.update(OneGameProvider.CONTENT_URI, cv, ""+OneGameColumn.ONEGAMEID+"=?", args);
									//通知gamesList
									if(gameMap.containsKey(gameId)){
										OneGameGame game = gameMap.get(gameId);
										game.setImage(path);
									}
									//通知适配器
									uiHandler.sendEmptyMessage(REFLASH_ADAPTER);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
		                    super.run();
		                }
		            }.start();
				} 
				
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
		
	}
	
	//解析json到DB并且刷新原数据
		private void parseJson2DBAndReflash(String str){
			if(str!="error"&&str!="exception"){
				JSONObject json;
				JSONArray info = new JSONArray();
				final ContentResolver cr = getActivity().getContentResolver();
				try {			
					json = new JSONObject(str);
					info = json.getJSONArray( "listData" );
					
					for(int i = info.length()-1; i >=0 ; i--){ 				
						final JSONObject jsonObj = ((JSONObject)info.opt(i)); 
						final int gameId = jsonObj.getInt("id");
						
						ContentValues values = new ContentValues();
			            
						values.put(OneGameColumn.ONEGAMEID,jsonObj.getInt("id"));
						values.put(OneGameColumn.ONEGAMEPUBLISHTIME,jsonObj.getString("publish_time"));
						values.put(OneGameColumn.ONEGAMENAME,jsonObj.getString("game_name"));
						values.put(OneGameColumn.ONEGAMEIMAGE,jsonObj.getString("image"));
						values.put(OneGameColumn.ONEGAMEINTRODUCTION,jsonObj.getString("introduction"));
						values.put(OneGameColumn.ONEGAMEDETAIL,jsonObj.getString("detail"));
						values.put(OneGameColumn.ONEGAMEPRAISENUM, jsonObj.getInt("praise_num"));
						//new
						values.put(OneGameColumn.ONEGAMEDOWNLOADURL, jsonObj.getString("download_url"));
						values.put(OneGameColumn.ONEGAMEDETAILIMAGE, jsonObj.getString("detail_image"));
			
						
						//判断是否已经存在数据
						String[] selectionArgs = {String.valueOf(gameId)};
						Cursor cur = cr.query(OneGameProvider.CONTENT_URI, new String[]{OneGameColumn.ONEGAMEID},
								""+OneGameColumn.ONEGAMEID+"=?", selectionArgs, null);
					
						if(cur.getCount()==0){			//不存在
							//存入数据库
							cr.insert(OneGameProvider.CONTENT_URI, values);
										            
						}else{		//数据已存在则更新
							
							//更新数据库
							ContentValues cv2 = new ContentValues();
							cv2.put(OneGameColumn.ONEGAMEPRAISENUM, jsonObj.getInt("praise_num"));
							String[] args = {String.valueOf(gameId)};
							cr.update(OneGameProvider.CONTENT_URI, cv2, ""+OneGameColumn.ONEGAMEID+"=?", args);
							cv2 = null;
						}
						if(cur != null)
						{
							cur.close();
						}
						
						//图片若不是本地，则要下载到本地
						cur = cr.query(OneGameProvider.CONTENT_URI, new String[]{OneGameColumn.ONEGAMEID,OneGameColumn.ONEGAMEIMAGE},
								""+OneGameColumn.ONEGAMEID+"=?", selectionArgs, null);
						if(cur.getCount()!=0){
							cur.moveToFirst();
							if(cur.getString(cur.getColumnIndex(OneGameColumn.ONEGAMEIMAGE)).contains("http://")){
								
								new Thread(){
					                @Override
					                public void run() {
					                    try {
											String path = DownLoadUtils.downloadFile(jsonObj.getString("image"),getActivity());
											//修改数据库图片路径
											if(!path.equals("")	&& path!=null){
												ContentValues cv = new ContentValues();
												cv.put(OneGameColumn.ONEGAMEIMAGE, path);
												String[] args = {String.valueOf(gameId)};
												cr.update(OneGameProvider.CONTENT_URI, cv, ""+OneGameColumn.ONEGAMEID+"=?", args);
												//通知gamesList
												if(gameMap.containsKey(gameId)){
													OneGameGame game = gameMap.get(gameId);
													game.setImage(path);
												}
												//通知适配器
												uiHandler.sendEmptyMessage(REFLASH_ADAPTER);
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
					                    super.run();
					                }
					            }.start();
							}
							if(cur != null)
							{
								cur.close();
							}
						}
						
					} 
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	
	//从数据库读取数据
	private void getMapFromDB(){
		Cursor cur = getActivity().getContentResolver().query(OneGameProvider.CONTENT_URI, null, null, null,  OneGameColumn.ONEGAMEPUBLISHTIME + " ASC");
    	int c=cur.getCount();
    	int oneGameIdColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEID);  
    	int oneGamePublishTimeColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEPUBLISHTIME); 
		int oneGameNameColumn = cur.getColumnIndex(OneGameColumn.ONEGAMENAME); 
		int oneGameIntroductionColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEINTRODUCTION);  
		int oneGameDetailColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEDETAIL); 
		int oneGamePraiseNoColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEPRAISENUM);  
		int oneGameImageColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEIMAGE); 
		//new
		int oneGameDownloadUrlColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEDOWNLOADURL); 
		int oneGameDetailImageColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEDETAILIMAGE); 
		
		int loc = 0;
		for (int i = c; i > 0; i--) 
		 	{
			cur.moveToPosition(i-1);

			OneGameGame game = new OneGameGame();  
			
			int gameId = cur.getInt(oneGameIdColumn);  
			String gamePublishTime = cur.getString(oneGamePublishTimeColumn);  
			String gameName = cur.getString(oneGameNameColumn);  
			String gameIntroduction = cur.getString(oneGameIntroductionColumn);  
			String gameDetail = cur.getString(oneGameDetailColumn);  
			int gamePraiseNo = cur.getInt(oneGamePraiseNoColumn);  
			String gameImage = cur.getString(oneGameImageColumn);  
			//new
			String downloadUrl = cur.getString(oneGameDownloadUrlColumn);
			String detailImage = cur.getString(oneGameDetailImageColumn);
			
			game.setId(gameId);
			game.setPublish_time(gamePublishTime);
			game.setGame_name(gameName);
			game.setIntroduction(gameIntroduction);
			game.setDetail(gameDetail);
			game.setPraise_num(gamePraiseNo);
			game.setImage(gameImage);
			//new 
			game.setDownload_url(downloadUrl);
			game.setDetail_image(detailImage);
			
			gameMap.put(gameId, game);
			gameList.add(game);
			//为收藏夹所用
			Global.getDetailList().put(String.valueOf(gameId), loc);
			loc++;
		 	}
			if(cur != null)
			{
				cur.close();
			}
	}
	
	//判断数据库数据是否为空
	public boolean isEmptyDB(){
		Cursor cur = getActivity().getContentResolver().query(OneGameProvider.CONTENT_URI, null, null, null, null);
		int c=cur.getCount();
		if(cur != null)
		{
			cur.close();
		}
		
		if(c == 0){
			return true;
		}else{
			return false;
		}
	}

	public FlipViewController getFlipView() {
		return flipView;
	}

	public void setFlipView(FlipViewController flipView) {
		this.flipView = flipView;
	}
	
	
	/**
	 * 下拉加载
	 * @return 若有新数据，return true，否则 return false
	 */
	public boolean pullDown(){
		//获取现在数据最后那条数据的日期
		OneGameGame game = gameList.get(gameList.size()-1);
		String lastPublishTime = game.getPublish_time();
		//加载从最后日期之后开始的五条数据
		//从后台获取数据
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pageSize","5")); 
		params.add(new BasicNameValuePair("pageNo","1")); 
		params.add(new BasicNameValuePair("publish_time",lastPublishTime)); 
		String str = HttpUtils.doPostWithoutStrict(Global.GAME_GETONEDAYLIST, params);
		return parseJson2DBAndAddGame(str);
	}

	/**
	 * 添加加载中圈圈的图片，并与下面一个函数前后使用
	 */
	public void addLoadingImage(){
		Log.d(TAG, "addLoadingImage instance "+(this));
		Log.d(TAG, "addLoadingImage instance "+(getActivity()));
		loadingImageWarpper = new LinearLayout(getActivity());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.BELOW, R.id.header);
		loadingImageWarpper.setLayoutParams(lp);
		loadingImageWarpper.setOrientation(LinearLayout.VERTICAL);
		loadingImageWarpper.setBackgroundColor(Color.argb(100, 0, 0, 0));
		loadingImageWarpper.setGravity(Gravity.CENTER);
		
		loadingImage = new LoadingImageView(getActivity());
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		lp2.width = 60;
		loadingImage.setLayoutParams(lp2);
		
		loadingImageWarpper.addView(loadingImage);
		layout.addView(loadingImageWarpper);
	}
	
	/**
	 * 取消加载中圈圈的图片，并与上面一个函数前后使用
	 */
	public void cancleLoadingImage(){
		layout.removeView(loadingImageWarpper);
		loadingImage = null;
		loadingImageWarpper = null;
	}

	public Handler getUiHandler() {
		return uiHandler;
	}

	public void setUiHandler(Handler uiHandler) {
		this.uiHandler = uiHandler;
	}
	
	

}
