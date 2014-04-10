package com.sungy.onegame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sungy.onegame.activity.DetailActivity;
import com.sungy.onegame.mclass.FileUtil;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.OneGameGame;
import com.sungy.onegame.onegameprovider.OneGameColumn;
import com.sungy.onegame.onegameprovider.OneGameProvider;
import com.sungy.onegame.view.LoadingImageView;

public class SampleListFragment extends ListFragment implements OnScrollListener , OnTouchListener{
	private ImageView iv_left;
	private ImageView iv_right;
	public static List<OneGameGame> gameList;
	OneGameItemAdapter adapter;
	
	private ListView list;
	private int locItem = 0;
	private int fY = 0;
	
	private static final int STOPSPLASH = 0; 
	private final static int SET_ADAPTER = 1;
	private Handler uiHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == SET_ADAPTER){
				setListAdapter(adapter);
			}
			else if(msg.what == STOPSPLASH){
				loadingImage.destory();
				loading.setVisibility(View.GONE);  
			}
			super.handleMessage(msg);  
		}
		
	};
	private LinearLayout loading; 
	private LoadingImageView loadingImage; 
    private static final long SPLASHTIME = 1000;  
   
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.list, null);
		iv_left = (ImageView) mView.findViewById(R.id.iv_left);
		iv_right = (ImageView) mView.findViewById(R.id.iv_right);
		
		//ListView
		list = (ListView)mView.findViewById(android.R.id.list);		
        loading = (LinearLayout) mView.findViewById(R.id.loading); 
        loadingImage = (LoadingImageView)mView.findViewById(R.id.loading_image);
       
				
		return mView;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				gameList = getGameList();
				adapter = new OneGameItemAdapter(getActivity());
				uiHandler.sendEmptyMessage(SET_ADAPTER);
		        Message msg = new Message();  
		        msg.what = STOPSPLASH;  
		        uiHandler.sendMessageDelayed(msg, SPLASHTIME);
			}
		}).start();
		
		list.setOnScrollListener(this);
		list.setOnTouchListener(this);
		
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
	
	private List<OneGameGame> getGameList() {
		// TODO Auto-generated method stub		
		List<OneGameGame> games = new ArrayList<OneGameGame>();
//		
//		//获取数据库保存的最后一条数据的日期
//		Cursor cur = getActivity().getContentResolver().query(OneGameProvider.CONTENT_URI, null, null, null, null);
//		long days = 11;
//		String lastPublishTime = DateUtils.getDate();
//		if(cur.getCount()>0){
//			cur.moveToPosition(cur.getCount()-1);
//			lastPublishTime = cur.getString(cur.getColumnIndex(OneGameColumn.ONEGAMEPUBLISHTIME)); 
//			//计算最后的日期与今天相差天数
//			String currentDay = DateUtils.getDate();
//			days = DateUtils.getQuot(currentDay, lastPublishTime);
//		}
//		//若相差时间小于10天则更新
//		if(days<=10){
//			List <NameValuePair> params = new ArrayList<NameValuePair>();
//			params.add(new BasicNameValuePair("day",lastPublishTime)); 
//			String str = HttpUtils.doPost(Global.GAME_GETGAMEFROMDATE, params);
//			parseJson2list(str, games);
//		}else{				
			//如果大于10天则把以前的记录清除，重新加载
			getActivity().getContentResolver().delete(OneGameProvider.CONTENT_URI, null, null);
			List <NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("pageSize","10")); 
			params.add(new BasicNameValuePair("pageNo","1")); 
			String str = HttpUtils.doPostWithoutStrict(Global.GAME_CURRENTDAYLIST, params);
			parseJson2list(str, games);
//		}

		return games;
	}

	@SuppressLint("NewApi")
	protected void downloadFile(String url) {
		// TODO Auto-generated method stub
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		int fileSize = 0; 
		try {
        	URL u = new URL(url);
        	
            URLConnection conn = u.openConnection(); 
            conn.connect(); 
            InputStream is = conn.getInputStream(); 
            fileSize = conn.getContentLength(); 
            if(fileSize<1||is==null) 
            {  
            }else{ 
                //FileOutputStream fos = new FileOutputStream(getPath(url)); 
            	String s = getPath(url);
            	FileOutputStream fos = new FileOutputStream(s); 
                byte[] bytes = new byte[1024]; 
                int len = -1; 
                while((len = is.read(bytes))!=-1) 
                { 
                    fos.write(bytes, 0, len); 
                } 
                
                is.close(); 
                fos.close(); 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }  
	}
	
	private String getPath(String str) throws IOException 
    { 
        String path = FileUtil.setMkdir(getActivity())+File.separator+str.substring(str.lastIndexOf("/")+1); 
        return path; 
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
	}
	
	//解释json到gamelist
	private void parseJson2list(String str,List<OneGameGame> games){
		if(str!="error"&&str!="exception"){
			JSONObject json;
			JSONArray info = new JSONArray();
			try {			
				json = new JSONObject(str);
				info = json.getJSONArray( "listData" );
				
				for(int i = info.length()-1; i >=0 ; i--){ 				
					final JSONObject jsonObj = ((JSONObject)info.opt(i)); 
					Global.getDetailList().put(jsonObj.getString("id"), i);
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
					
					getActivity().getContentResolver().insert(OneGameProvider.CONTENT_URI, values);
					
					new Thread(){
		                @Override
		                public void run() {
		                    try {
								downloadFile(jsonObj.getString("image"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                    super.run();
		                }
		            }.start();
				} 
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Cursor cur = getActivity().getContentResolver().query(OneGameProvider.CONTENT_URI, null, null, null, null);
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
			
			games.add(game);
		 	}
			if(cur != null)
			{
				cur.close();
			}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	//滚动到指定位置
	private void setListViewPos(int pos,ListView mListView) {  
	    if (android.os.Build.VERSION.SDK_INT >= 8) {  
	        mListView.smoothScrollToPosition(pos);  
	    } else {  
	        mListView.setSelection(pos);  
	    }  
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int y = (int)event.getY();
		int deltaY = 0;
        switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN: 
	        	fY = y;
	            break;
        	case MotionEvent.ACTION_MOVE: 
        		 break;
        	case MotionEvent.ACTION_UP: 
                deltaY = y - fY;   //delta的正负就表示往上或往下
//        		Log.d("onTouchUp",""+ deltaY +"   "+fY);
//            	if(deltaY<0){		//向下滚动
//        		locItem++;
//        		if(locItem > list.getCount()-1) locItem = list.getCount()-1;
//        		setListViewPos(locItem,list);
//        	}else{		//向上滚动
//        		locItem--;
//        		if(locItem < 0) locItem = 0;
//        		setListViewPos(locItem,list);
//        	}
            break;
        }
		return false;
	}

}
