package com.sungy.onegame.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sungy.onegame.R;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.OneGameGame;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.onegameprovider.OneGameColumn;
import com.sungy.onegame.onegameprovider.OneGameProvider;

public class SearchActivity extends Activity {
	public final static String TAG = "SearchActivity";
	private GridView searchGridView;
	public static List<OneGameGame> searchList;
	private GridViewAdapter gridAdapter ;
	//下拉加载线程
	private Thread pullDownThread ;
	//是否下拉加载中
	private boolean isPullDownLoading = false;
	//是否全部加载完毕
	private boolean isPullDownFinish = false;
	
	//加载中Layout
	private RelativeLayout loadingLayout ;
	
	//搜索按钮
	private ImageView searchBtn;
	//搜索文本
	private TextView searchText;
	//是否搜索模式
	private boolean isSearchMode = false;
	
	//字体
    private Typeface tf;
	//字体路径
    private String typeFaceDir = "fonts/font.ttf";
	
	public final static int CANCLE_LOADING_IMAGE = 0;
	public final static int REFRESH_ADAPTER = 1;
	public final static int PULL_DOWN_FINISH = 2;
	public final static int SEARCH_NODATA = 3;
	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CANCLE_LOADING_IMAGE:
				cancleLoadingImage();
				break;
			
			case REFRESH_ADAPTER:
				if(gridAdapter != null){
					gridAdapter.notifyDataSetChanged();
				}
				break;
				
			case PULL_DOWN_FINISH:
				ToastUtils.showDefaultToast(SearchActivity.this, "没有更多数据", Toast.LENGTH_SHORT);
				break;
				
			case SEARCH_NODATA:
				ToastUtils.showDefaultToast(SearchActivity.this, "没有相关游戏", Toast.LENGTH_SHORT);
				break;
				
			default:
				break;
			}
			super.handleMessage(msg);
		}
	
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_search);
		
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		
		searchGridView = (GridView) findViewById(R.id.search_gridview);	
		searchList = new ArrayList<OneGameGame>();
		initData();	
		gridAdapter = new GridViewAdapter(this);
		searchGridView.setAdapter(gridAdapter);
		searchGridView.setOnScrollListener(new GridOnScrollListener());
		
		//字体
        AssetManager mgr = getAssets();//得到AssetManager
        tf = Typeface.createFromAsset(mgr, typeFaceDir);//根据路径得到Typeface
		
		searchBtn = (ImageView) findViewById(R.id.search_btn);
		searchText = (TextView) findViewById(R.id.search_text);
		searchText.setTypeface(tf);
		//点击搜索
		searchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = "";
				text = searchText.getText().toString();
				if(text.trim().equals("")){
					ToastUtils.showDefaultToast(SearchActivity.this, "搜索值不能为空", Toast.LENGTH_SHORT);
					return;
				}
				searchGame(text);
			}
		});
		
		//返回按钮
		ImageView back = (ImageView) findViewById(R.id.search_back);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
				destory();
			}
		});
		
	}
	
	/**
	 * 初始化数据
	 */
	private void initData(){
		//从数据库提取已保存的数据
		getMapFromDB();
	}
	
	
	
	/**
	 * 从数据库读取数据
	 */
	private void getMapFromDB() {
		Cursor cur = getContentResolver().query(
				OneGameProvider.CONTENT_URI, null, null, null,
				OneGameColumn.ONEGAMEPUBLISHTIME + " ASC");
		int c = cur.getCount();
		int oneGameIdColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEID);
		int oneGamePublishTimeColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEPUBLISHTIME);
		int oneGameNameColumn = cur.getColumnIndex(OneGameColumn.ONEGAMENAME);
		int oneGameIntroductionColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEINTRODUCTION);
		int oneGameDetailColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEDETAIL);
		int oneGamePraiseNoColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEPRAISENUM);
		int oneGameImageColumn = cur.getColumnIndex(OneGameColumn.ONEGAMEIMAGE);
		// new
		int oneGameDownloadUrlColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEDOWNLOADURL);
		int oneGameDetailImageColumn = cur
				.getColumnIndex(OneGameColumn.ONEGAMEDETAILIMAGE);

		for (int i = c; i > 0; i--) {
			cur.moveToPosition(i - 1);

			OneGameGame game = new OneGameGame();

			int gameId = cur.getInt(oneGameIdColumn);
			String gamePublishTime = cur.getString(oneGamePublishTimeColumn);
			String gameName = cur.getString(oneGameNameColumn);
			String gameIntroduction = cur.getString(oneGameIntroductionColumn);
			String gameDetail = cur.getString(oneGameDetailColumn);
			int gamePraiseNo = cur.getInt(oneGamePraiseNoColumn);
			String gameImage = cur.getString(oneGameImageColumn);
			// new
			String downloadUrl = cur.getString(oneGameDownloadUrlColumn);
			String detailImage = cur.getString(oneGameDetailImageColumn);

			game.setId(gameId);
			game.setPublish_time(gamePublishTime);
			game.setGame_name(gameName);
			game.setIntroduction(gameIntroduction);
			game.setDetail(gameDetail);
			game.setPraise_num(gamePraiseNo);
			game.setImage(gameImage);
			// new
			game.setDownload_url(downloadUrl);
			game.setDetail_image(detailImage);

			searchList.add(game);
		}
		if (cur != null) {
			cur.close();
		}
	}

	
	/**
	 * 添加加载中提示
	 */
	public void addLoadingImage(){
		loadingLayout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 撤销加载中提示
	 */
	public void cancleLoadingImage(){
		loadingLayout.setVisibility(View.GONE);
	}

	public Handler getMyHandler() {
		return myHandler;
	}

	public void setMyHandler(Handler myHandler) {
		this.myHandler = myHandler;
	}
	
	
	//gridView 滑动监听器
	private class GridOnScrollListener implements OnScrollListener{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			 if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
		          //滚动到底部   
		          if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
		        	  Log.d(TAG, "滑动到底部");
		        	if(!isPullDownLoading	&&	!isPullDownFinish	&&	!isSearchMode){
		        		isPullDownLoading = true;
		        		
		        		//显示加载中提示
		        		addLoadingImage();
		        		
						pullDownThread = new Thread(new Runnable() {

							@Override
							public void run() {
								if(!pullDownLoadDatea()){
									//如果没有数据，则提示没有数据，并设置不能再下拉加载
									myHandler.sendEmptyMessage(PULL_DOWN_FINISH);
									isPullDownFinish = true;
								}
								
								isPullDownLoading = false;
								//撤销加载中提示
								myHandler.sendEmptyMessage(CANCLE_LOADING_IMAGE);
							}
						});
						pullDownThread.start();
		        	}
		          }
			 }
		}
		
	}
	
	/**
	 * 下拉加载数据
	 * @return 若有新数据，return true，否则 return false
	 */
	private boolean pullDownLoadDatea(){
		// 获取现在数据最后那条数据的日期
		OneGameGame game = searchList.get(searchList.size() - 1);
		String lastPublishTime = game.getPublish_time();
		
		// 加载从最后日期之后开始的九条数据
		// 从后台获取数据
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pageSize", "9"));
		params.add(new BasicNameValuePair("pageNo", "1"));
		params.add(new BasicNameValuePair("publish_time", lastPublishTime));
		String str = HttpUtils.doPostWithoutStrict(Global.GAME_GETONEDAYLIST,params);
		return parseJson2List(str);
	}
	
	
	/**
	 * 解释json并且把数据添加到searchList
	 * @param str
	 * @return 若有新数据，return true，否则 return false
	 */
	private boolean parseJson2List(String str){
		if(str!="error"&&str!="exception"){
			JSONObject json;
			JSONArray info = new JSONArray();
			final ContentResolver cr = getContentResolver();
			try {			
				json = new JSONObject(str);
				info = json.getJSONArray( "listData" );
				
				if(info.length() == 0){
					return false;
				}
				Log.d(TAG, info.length()+"");
				
				for(int i = 0; i < info.length() ; i++){ 				
					final JSONObject jsonObj = ((JSONObject)info.opt(i)); 
					final int gameId = jsonObj.getInt("id");
					
					//存入searchList
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
					
					searchList.add(game);
					
					//刷新适配器
					myHandler.sendEmptyMessage(REFRESH_ADAPTER);
				} 
				
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
		
	}
	
	
	/**
	 * 搜索游戏
	 */
	public void searchGame(final String name){
		isSearchMode = true;
		//清除现在的数据
		searchList.clear();
		gridAdapter.notifyDataSetChanged();
		
		//显示加载中提示
		addLoadingImage();		
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(!searchGameImpl(name)){
					//如果没有数据，则提示没有数据
					myHandler.sendEmptyMessage(SEARCH_NODATA);
				}
				//撤销加载中提示
				myHandler.sendEmptyMessage(CANCLE_LOADING_IMAGE);
			}
		}).start();
	}
	
	/**
	 * 搜索游戏核心实现
	 */
	public boolean searchGameImpl(String name){
		// 加载三十条数据
		// 从后台获取数据
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pageSize", "30"));
		params.add(new BasicNameValuePair("pageNo", "1"));
		params.add(new BasicNameValuePair("game_name", name));
		String str = HttpUtils.doPostWithoutStrict(Global.GAME_SEARCHGAMELIST,params);
		return parseJson2List(str);
	}

	/**
	 * 释放内存
	 */
	public void destory(){
		if(pullDownThread!=null	&&	pullDownThread.isAlive()){
			pullDownThread.interrupt();
		}
		
		gridAdapter.destory();
		
		if(searchList != null){
			searchList.clear();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		destory();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	
}
