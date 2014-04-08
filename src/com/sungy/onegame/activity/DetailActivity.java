package com.sungy.onegame.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.sungy.onegame.LeftFragment;
import com.sungy.onegame.MainActivity;
import com.sungy.onegame.R;
import com.sungy.onegame.SampleListFragment;
import com.sungy.onegame.mclass.DownLoadUtils;
import com.sungy.onegame.mclass.FileUtil;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.OneGameComment;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.view.LoadingImageView;

public class DetailActivity extends Activity implements OnClickListener{
	private final static String TAG = "DetailActivity";
	private int index;
	private String detail_image_url;
	public static List<OneGameComment> commentList;
	
	private ImageView detailBack;	
	private ImageView detailPraise;	
	private ImageView detailImage;
	
	private ImageView detailShare;
	private ImageView detailCollect;
	private ImageView detailComment;
	private ImageView detailDownload;
	
	private TextView detailTitle;
	private TextView detailPraiseNo;
	private TextView detailIntruction;
	
	private ListView detailComments;
	private String shareTextString;
	
	//new
	private Bitmap[] detailImages = null;
	private String detailImageStr = "";
	private String[] detailImageUrls = null;
	Gallery gallery = null;
	ImageAdapter adapter = null;
	
	CommentItemAdapter commentAdapter = null;
	
	private RelativeLayout layout ;
	private LoadingImageView loading ;
	
	//是否收藏
	private int isCollect = 0;	//1代表有，2代表没有，0代表未初始化
	private String collectId = "0";
	//是否点赞
	private int isPraise = 0; 	//1代表有，2代表没有，0代表未初始化
	private String praiseId = "0";
	
	//handler
	public final int REFLASH_GALLERY = 1;
	public final int INITVIEW = 2;
	public final int COLLECTED = 3;
	public final int INITDATA = 4;
	public final int COMMENT_DATA_COMPLEMENT = 5;
	public final int RELOAD_COMMENT = 6;
	public final int TOPIMAGE = 7;
	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == REFLASH_GALLERY){		//刷新图片长廊
				if(gallery!=null){
					gallery.postInvalidate();
				}
				if(adapter !=null){
					adapter.notifyDataSetChanged();
					adapter.resetView(msg.arg1);
				}
			}else if(msg.what == INITVIEW){			//初始化View
				initView();
			}else if(msg.what == COLLECTED){		//设置已收藏图片
				detailCollect.setImageResource(R.drawable.detail_icon_fav1_normal);
			}else if(msg.what == INITDATA){			//初始化数据
				initData();
				initComment();
			}else if(msg.what == COMMENT_DATA_COMPLEMENT){		//评论数据加载完回调
				commentAdapter = new CommentItemAdapter(getApplicationContext());
				detailComments.setAdapter(commentAdapter);
				setListViewHeightBaseOnChildren(detailComments);
				
				detailImage.setFocusable(true);
				detailImage.setFocusableInTouchMode(true);
				detailImage.requestFocus();
				detailImage.requestFocusFromTouch();			
				//撤出loading
				loading.setVisibility(View.GONE);
				
			}else if(msg.what == RELOAD_COMMENT){			//重新加载评论数据回调
				commentAdapter.notifyDataSetChanged();
				setListViewHeightBaseOnChildren(detailComments);
				
			}else if(msg.what == TOPIMAGE){					//顶部图片加载
				detailImage.setImageBitmap(BitmapFactory.decodeStream((InputStream) msg.obj));
				msg.obj = null;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_detail);
		
		Intent i = getIntent();
		index = i.getExtras().getInt("index");
		initView();	
		new Timer(true).schedule(new TimerTask() {
			
			@Override
			public void run() {
				myHandler.sendEmptyMessage(INITDATA);	
			}
		}, 500);
	}

	public void share(String gameurl,final String gamename,final String gameid) {
		ShareSDK.initSDK(DetailActivity.this);
		OnekeyShare oks = new OnekeyShare();
 
		// 分享时Notification的图标和文字，不必改
		oks.setNotification(R.drawable.ic_launcher,
				getString(R.string.app_name));
		// title标题，QQ空间一定要用
		oks.setTitle("OneGame游戏分享");
		// // titleUrl是标题的网络链接，QQ空间一定要用
		oks.setTitleUrl("http://sharesdk.cn");
		// text是分享文本，所有平台都需要这个字段
		oks.setText("OneGame推荐的这款游戏很好玩，特别推荐给您!游戏名:"+gamename+" :下载链接："+gameurl);
//		  imagePath是图片的本地路径
		 oks.setImagePath("/sdcard/mypic.png");

//		 site是分享此内容的网站名称，QQ空间一定要用
		oks.setSite(getString(R.string.app_name));
		 // siteUrl是分享此内容的网站地址，QQ空间一定要用
		oks.setSiteUrl("http://sharesdk.cn");

		// 去除注释可通过OneKeyShareCallback来捕获快捷分享的处理结果
		// 通过OneKeyShareCallback来修改不同平台分享的内容
		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {

			@Override
			public void onShare(Platform platform, ShareParams paramsToShare) {
				shareTextString=paramsToShare.getText();
			}
			
		});
		oks.setCallback(new PlatformActionListener() {
			
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				String user_id=arg0.getDb().getUserId();
				String user_name=arg0.getDb().getUserName();
				String share_plat=arg0.getName();
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_name",
						user_name));
				params.add(new BasicNameValuePair("user_id",
						Global.getUserId()));
				params.add(new BasicNameValuePair("game_name",
						gamename));
				params.add(new BasicNameValuePair("game_id",
						gameid));
				params.add(new BasicNameValuePair("share_plat",
						share_plat));
				if(shareTextString.length()>250){
					shareTextString = shareTextString.substring(0,249);
				}
				params.add(new BasicNameValuePair("share_content", shareTextString));
				
				String str = HttpUtils.doPost(Global.SHARE_SHARE, params);
				Log.i("message", str);
			}
			
			@Override
			public void onCancel(Platform arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});
		oks.show(this);
	}
	
	private List<OneGameComment> getCommentList() {
		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("pageSize","10"));
		params.add(new BasicNameValuePair("pageNo","1"));
		params.add(new BasicNameValuePair("game_id",Integer.toString(SampleListFragment.gameList.get(index).getId())));
		String str = HttpUtils.doPostWithoutStrict(Global.COMMENT_GETBYUSERID, params);
		Log.d("json", str);
		
		List<OneGameComment> comments = new ArrayList<OneGameComment>();
		JSONObject json;
		JSONArray info = new JSONArray();
		
		try {			
			json = new JSONObject(str);
			info = json.getJSONArray( "listData" );
			
			for(int i = 0; i < info.length() ; i++){ 				
				JSONObject jsonObj = ((JSONObject)info.opt(i)); 
				
				OneGameComment comment = new OneGameComment();
				comment.setUser_name(jsonObj.getString("user_name"));
				comment.setComment_time(jsonObj.getString("comment_time"));
				comment.setComment(jsonObj.getString("comment"));
				comment.setUser_image(jsonObj.getString("user_image"));
				
				comments.add(comment);
			} 

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comments;
	}

	private void initView() {
		layout = (RelativeLayout)findViewById(R.id.detail_layout);
		loading = (LoadingImageView)findViewById(R.id.loading);
		
		detailBack = (ImageView) findViewById(R.id.detail_back);
		detailPraise = (ImageView) findViewById(R.id.detail_praise);
		detailImage = (ImageView) findViewById(R.id.detail_image);
		detailShare = (ImageView) findViewById(R.id.detail_share);
		detailCollect = (ImageView) findViewById(R.id.detail_collect);
		detailComment = (ImageView) findViewById(R.id.detail_comment);
		detailDownload = (ImageView) findViewById(R.id.detail_download);
		
		detailPraiseNo = (TextView) findViewById(R.id.detail_praiseNo);
		detailIntruction = (TextView) findViewById(R.id.detail_instruction);
		
		detailComments = (ListView) findViewById(R.id.detail_comments);
		
		Log.e(TAG, "加载部分UI完毕");
		
		detailPraiseNo.setText(Integer.toString(SampleListFragment.gameList.get(index).getPraise_num()));
		detailIntruction.setText(SampleListFragment.gameList.get(index).getDetail());
		detail_image_url = SampleListFragment.gameList.get(index).getImage();	
		detailImage.setImageResource(R.drawable.defalut); 	//默认图片
		
		//加载detailImage
		detailImageStr = SampleListFragment.gameList.get(index).getDetail_image();
		detailImageUrls = detailImageStr.split(";");
		detailImages = new Bitmap[detailImageUrls.length];
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.loading3);
		int l = 0;
		for(l=0;l<detailImageUrls.length;l++){
			detailImages[l] = bitmap;
		}
		gallery = (Gallery)findViewById(R.id.gallery);
		adapter = new ImageAdapter(this);
		gallery.setAdapter(adapter);
		
		Log.e(TAG, "加载多图片");
		
		if(Global.isLogin()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					//加载赞和收藏
					String userid = Global.getUserId();
					String gameid = Integer.toString(SampleListFragment.gameList.get(index).getId());
					List <NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("user_id",userid)); 
					params.add(new BasicNameValuePair("game_id",gameid)); 
					String str;
					JSONObject json;
					try {
						str = HttpUtils.doPostWithoutStrict(Global.COLLECT_ISCOLLECT, params);
						Log.d(TAG, str);
						json = new JSONObject(str);
						isCollect = (json.getString("message").equals("true"))?1:2;
						collectId = json.getString("id");
						
						str = HttpUtils.doPostWithoutStrict(Global.PRAISE_ISRAISE, params);
						json = new JSONObject(str);
						isPraise = (json.getString("message").equals("true"))?1:2;
						praiseId = json.getString("id");
						
						//切换图片
						if(isCollect == 1){
							myHandler.sendEmptyMessage(COLLECTED);
//							detailCollect.setImageResource(R.drawable.detail_icon_fav1_normal);
						}
						Log.d(TAG, isCollect+"  "+isPraise);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		Log.e(TAG, "加载赞与收藏");
		
		detailBack.setOnClickListener(this);
		detailPraise.setOnClickListener(this);
		detailShare.setOnClickListener(this);
		detailCollect.setOnClickListener(this);
		detailComment.setOnClickListener(this);
		detailDownload.setOnClickListener(this);
		
		Log.e(TAG, "加载部分UI3完毕");
	}
	
	//初始化数据
	private void initData(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				downloadImage(detail_image_url);//加载顶部图片
			}
		}).start();
		Log.e(TAG, "加载大图片");
		
		new Thread(new Runnable() {
			@Override
			public void run() {	
				int l = 0;
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.loading3);
				Bitmap bitmap2;
				for(l=0;l<detailImageUrls.length;l++){
					bitmap2 = DownLoadUtils.downloadImage(detailImageUrls[l], getApplicationContext());
					detailImages[l] = null;
					if(bitmap2==null){
						bitmap2 = bitmap;
					}
					detailImages[l] = bitmap2;
					//刷新Gallery
					Message message = new Message();
					message.what = REFLASH_GALLERY;
					message.arg1 = l;
					myHandler.sendMessage(message);
					Log.d("detailImage", l+"加载完毕");
				}
				bitmap2 = null;
				bitmap = null;
			}
		}).start();
	}

	//初始化评论
	private void initComment(){
		new Thread(new Runnable() {
			@Override
			public void run() {	
				Log.e(TAG, "加载评论");
				commentList = getCommentList();
				Log.e(TAG, "加载评论完毕");

				myHandler.sendEmptyMessage(COMMENT_DATA_COMPLEMENT);
//				commentAdapter = new CommentItemAdapter(getApplicationContext());
//				detailComments.setAdapter(commentAdapter);
//				setListViewHeightBaseOnChildren(detailComments);
//				detailComments.postInvalidate();
		//		Button button = (Button)findViewById(R.id.more_comment);
				//显示更多评论按钮
		//		if(commentList.size() == 10){
		//			button.setVisibility(View.VISIBLE);
		//		}
			}
		}).start();
	}
	
	@SuppressLint("NewApi")
	private void downloadImage(String url) {
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
                FileOutputStream fos = new FileOutputStream(getPath(url)); 
                byte[] bytes = new byte[1024]; 
                int len = -1; 
                while((len = is.read(bytes))!=-1) 
                { 
                    fos.write(bytes, 0, len); 
                } 
                if(getPath(url).endsWith(".jpg")||getPath(url).endsWith(".png") ||getPath(url).endsWith(".jpeg")){ 
                    FileInputStream fis = new FileInputStream(getPath(url)); 
//                    detailImage.setImageBitmap(BitmapFactory.decodeStream(fis));
                    Message message = new Message();
                    message.what = TOPIMAGE;
                    message.obj = fis;
                    myHandler.sendMessage(message);
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
        String path = FileUtil.setMkdir(this)+File.separator+str.substring(str.lastIndexOf("/")+1); 
        return path; 
    } 

	//ScrollView下的listview需要调整高度
	public void setListViewHeightBaseOnChildren(ListView listView) {
		Log.e(TAG, "高度计算1");
		CommentItemAdapter listAdapter = (CommentItemAdapter)listView.getAdapter();   
        if (listAdapter == null) {  
            return;  
        }  
        Log.e(TAG, "高度计算2");
        if(commentList.size() == 0){
        	ViewGroup.LayoutParams params = listView.getLayoutParams(); 
        	params.height = 300;
        	listView.setLayoutParams(params);  
        	return;
        }
        Log.e(TAG, "高度计算3");
        int totalHeight = 0;
        View one = listAdapter.getView(-1, null, listView);  //计算高度
        one.measure(0, 0);  
        int textWidth = one.getMeasuredWidth();
        int viewHeight = one.getMeasuredHeight();
        Log.e(TAG, "高度计算4");
        for (int i = 0; i < listAdapter.getCount(); i++) {  
            View listItem = listAdapter.getView(i, null, listView);  
            listItem.measure(0, 0);  
            totalHeight += listItem.getMeasuredHeight() + listAdapter.getTextHeight(i,textWidth);  
//            totalHeight += viewHeight + listAdapter.getTextHeight(i,textWidth);  
        }
        Log.e(TAG, "高度计算5");
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();  
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params); 
        Log.e(TAG, "高度计算6");
	}
	
	@Override
	public void onClick(View v) {
		final String useid = Global.getUserId();
		final String usename = Global.getUserNmae();
		final String gameid = SampleListFragment.gameList.get(index).getId()+"";
		final String gamename = SampleListFragment.gameList.get(index).getGame_name();
		final String gameurl = SampleListFragment.gameList.get(index).getDownload_url();
		final String userimage = Global.getUserImage();
		if (v == detailBack) {
			destroy();
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
		}else if(v == detailPraise){	
			//点赞
			if(Global.checkLogin(this)){
				if(isPraise == 2){
					ToastUtils.showDefaultToast(getApplicationContext(), "点赞中...", Toast.LENGTH_SHORT);
					praise(useid, usename, gamename, gameid);
					SampleListFragment.gameList.get(index).setPraise_num(SampleListFragment.gameList.get(index).getPraise_num()+1);
					detailPraiseNo.setText(Integer.toString(Integer.parseInt((String) detailPraiseNo.getText())+1));
				}else if(isPraise == 1){
					canclePraise(praiseId, gameid);
					SampleListFragment.gameList.get(index).setPraise_num(SampleListFragment.gameList.get(index).getPraise_num()-1);
					detailPraiseNo.setText(Integer.toString(Integer.parseInt((String) detailPraiseNo.getText())-1));
				}
			}
		}else if(v == detailShare){
			if(Global.checkLogin(this)){
				share(gameurl,gamename,gameid);
			}
		}else if(v == detailCollect){
			if(Global.checkLogin(this)){
				if(isCollect == 2){
					ToastUtils.showDefaultToast(getApplicationContext(), "收藏中...", Toast.LENGTH_SHORT);
					collect(useid, usename, gamename, gameid);
					detailCollect.setImageResource(R.drawable.detail_icon_fav1_normal);
				}else if(isCollect == 1){
					cancleCollect(collectId,gameid);
					ToastUtils.showDefaultToast(getApplicationContext(), "已取消收藏", Toast.LENGTH_SHORT);
					detailCollect.setImageResource(R.drawable.detail_icon_fav_normal);
				}
			}
		}else if(v == detailComment){
			if(Global.checkLogin(this)){
				LayoutInflater factory = LayoutInflater.from(getApplicationContext());
				View DialogView = factory.inflate(R.layout.comment_dialog, null);
				final TextView comment = (TextView)DialogView.findViewById(R.id.comment);
				AlertDialog dlg = new AlertDialog.Builder(this)
		    	.setTitle("发表评论")
		    	.setView(DialogView)
		    	.setPositiveButton("评论",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,int whichButton) {
										//获取评论
										String commentStr = comment.getText().toString();
										if(commentStr.trim().equals("")){
											ToastUtils.showDefaultToast(getApplicationContext(), "评论不能为空", Toast.LENGTH_SHORT);
											return;
										}
										comment(useid, usename, gamename, gameid,commentStr,userimage);
										ToastUtils.showDefaultToast(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT);
										//重新加载评论
//										reloadComment();
										newComment(useid, usename, commentStr,userimage);
									}
								})
				.create();
		    	dlg.show();
			}
		}else if(v == detailDownload){
			Toast.makeText(this, "下载完毕", Toast.LENGTH_SHORT).show();
		}
	}
	
	//点赞
	private void praise(final String user_id,final String user_name,final String game_name,final String game_id){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//post请求的参数
				List <NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("user_id",user_id)); 
				params.add(new BasicNameValuePair("user_name",user_name)); 
				params.add(new BasicNameValuePair("game_name",game_name)); 
				params.add(new BasicNameValuePair("game_id",game_id)); 
				String str = HttpUtils.doPostWithoutStrict(Global.PRAISE_PRAISE, params);
				
				try {
					JSONObject json = new JSONObject(str);
					isPraise = (json.getString("message").equals("success"))?1:2;
					praiseId = json.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	//取消点赞
	private void canclePraise(final String praise_id,final String game_id){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//post请求的参数
				List <NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("id",praise_id)); 
				params.add(new BasicNameValuePair("game_id",game_id)); 
				String str = HttpUtils.doPostWithoutStrict(Global.PRAISE_CANCLEPRAISE, params);
				
				isPraise = 2;
			}
		}).start();
	}
	
	//收藏
	private void collect(final String user_id,final String user_name,final String game_name,final String game_id){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//post请求的参数
				List <NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("user_id",user_id)); 
				params.add(new BasicNameValuePair("user_name",user_name)); 
				params.add(new BasicNameValuePair("game_name",game_name)); 
				params.add(new BasicNameValuePair("game_id",game_id)); 
				String str = HttpUtils.doPostWithoutStrict(Global.COLLECT_COLLECT, params);
				
				try {
					JSONObject json = new JSONObject(str);
					isCollect = (json.getString("message").equals("success"))?1:2;
					collectId = json.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	//取消收藏
	private void cancleCollect(final String collect_id,final String game_id){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//post请求的参数
				List <NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("id",collect_id)); 
				params.add(new BasicNameValuePair("game_id",game_id)); 
				String str = HttpUtils.doPostWithoutStrict(Global.COLLECT_CANCLECOLLECT, params);
				
				isCollect = 2;
			}
		}).start();
	}	
	
	//评论
	private void comment(final String user_id,final String user_name,final String game_name,final String game_id,final String comment,final String userimage){
		new Thread(new Runnable() {
			@Override
			public void run() {
				//post请求的参数
				List <NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("user_id",user_id)); 
				params.add(new BasicNameValuePair("user_name",user_name)); 
				params.add(new BasicNameValuePair("game_name",game_name)); 
				params.add(new BasicNameValuePair("game_id",game_id)); 
				params.add(new BasicNameValuePair("comment",comment)); 
				params.add(new BasicNameValuePair("user_image",userimage)); 
				String str = HttpUtils.doPostWithoutStrict(Global.COMMENT_COMMENT, params);
			}
		}).start();
	}

	
	public class ImageAdapter extends BaseAdapter {  
	    private Context mContext;  
	        public ImageAdapter(Context context) {  
	        mContext = context;  
	    }  
	  
	    public int getCount() {   
	        return detailImageUrls.length;  
	    }  
	  
	    public Object getItem(int position) {  
	        return position;  
	    }  
	  
	    public long getItemId(int position) {  
	        return position;  
	    }  
	  
	    public View getView(int position, View convertView, ViewGroup parent) {  
	        ImageView image = new ImageView(mContext);  
	        image.setImageBitmap(detailImages[position]);
	        image.setAdjustViewBounds(true);  
	        image.setLayoutParams(new Gallery.LayoutParams(  
	        		Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));  
	        return image;  
	    }
	    
	    public void resetView(int position){
	    	getView(position, null, null);
	    }
	}  
	
	//回收
	public void destroy(){
		for(int i = 0 ;i<detailImages.length;i++){
			detailImages[i].recycle();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			destroy();
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	//重新加载评论
	private void reloadComment(){
		commentList = null;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				commentList = getCommentList();	
				myHandler.sendEmptyMessage(RELOAD_COMMENT);
//				commentAdapter.notifyDataSetChanged();
			}
		}).start();
	}
	
	//添加新评论
	private void newComment(String useid,String usename,String commentStr,String userimage){
		List<OneGameComment> newlist = new ArrayList<OneGameComment>();
		
		OneGameComment comment = new OneGameComment();
		comment.setUser_name(usename);
		comment.setComment_time(new Date().toGMTString());
		comment.setComment(commentStr);
		comment.setUser_image(userimage);
		
		newlist.add(comment);
		copyList(newlist, commentList);
		commentList = newlist;
		commentAdapter.notifyDataSetChanged();
		setListViewHeightBaseOnChildren(detailComments);
	}

	private void copyList(List<OneGameComment> a , List<OneGameComment> b){
		OneGameComment comment ;
		for(int i = 0 ;i<b.size();i++){
			comment = b.get(i);
			a.add(comment);
		}
	}
	
}
