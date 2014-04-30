package com.sungy.onegame.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

import com.sungy.onegame.R;
import com.sungy.onegame.SampleListFragment;
import com.sungy.onegame.mclass.DownLoadUtils;
import com.sungy.onegame.mclass.FileUtil;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;
import com.sungy.onegame.mclass.OneGameComment;
import com.sungy.onegame.mclass.OneGameGame;
import com.sungy.onegame.mclass.ToastUtils;
import com.sungy.onegame.onegameprovider.OneGameColumn;
import com.sungy.onegame.onegameprovider.OneGameProvider;
import com.sungy.onegame.view.LoadingImageView;

public class DetailActivityForResult extends Activity implements OnClickListener{
	private final static String TAG = "DetailActivityForResult";
	private OneGameGame game;
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
	private LinearLayout detailIntruction;
	
	private ListView detailComments;
	private String shareTextString;
	
	private ScrollView container;
	
	//详细图片
	private Bitmap[] detailImages = null;
	private String detailImageStr = "";
	private String[] detailImageUrls = null;
	private ImageView[] detailImageViews = null;
	private boolean isLoadImageViews = false;
	private Bitmap defaultBitmap = null;
	//测评内容
	private String intruction;
	private LayoutInflater inflater;
	//加载评论的页数
	private int loadCommentPageNo = 1;
	//是否正在加载评论
	private boolean isLoadingComment = false;
	//暂无评论提示
	private TextView noComment = null;
	private int commentListHeight = 0;
	
	CommentItemAdapterForResult commentAdapter = null;
	
	private RelativeLayout layout ;
	private LoadingImageView loading ;
	
	//是否收藏
	private int isCollect = 0;	//1代表有，2代表没有，0代表未初始化
	private String collectId = "0";
	//是否点赞
	private int isPraise = 0; 	//1代表有，2代表没有，0代表未初始化
	private String praiseId = "0";
	
	//顶部
	private RelativeLayout header = null;
	//底部
	private LinearLayout footer = null;
	//记录点击时的位置
	private float touchY = 0f;
	//是否已经隐藏顶部和底部
	private boolean isHidedHeadFoot = false;
	//是否在动画中
	private boolean isInAnim = false;
	//动画时间(请看anim文件夹中的动画文件)
	private int animMillis = 2000;	
	private Timer timer = null;
	
	//字体
    private Typeface tf;
    //字体路径
    private String typeFaceDir = "fonts/font.ttf";
	//星期中文
	private final String[] WEEKS_CN = new String[]{
			"星期日","星期一","星期二","星期三","星期四","星期五","星期六"	
		};
	
	//handler
	public final int REFLASH_GALLERY = 1;
	public final int INITVIEW = 2;
	public final int COLLECTED = 3;
	public final int INITDATA = 4;
	public final int COMMENT_DATA_COMPLEMENT = 5;
	public final int RELOAD_COMMENT = 6;
	public final int TOPIMAGE = 7;
	public final int SET_NEWIMAGE = 8;
	public final int CANCLE_LOADINGIMAGE = 9;
	public final int LOAD_COMPLEMENT = 10;
	public final int RESET_ISINANIM = 11;
	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == REFLASH_GALLERY){		//刷新图片长廊
				
			}else if(msg.what == INITVIEW){			//初始化View
				initView();
				
			}else if(msg.what == COLLECTED){		//设置已收藏图片
				detailCollect.setImageResource(R.drawable.detail_icon_fav1_normal);
				
			}else if(msg.what == INITDATA){			//初始化数据
				initData();
				initComment();
				
			}else if(msg.what == COMMENT_DATA_COMPLEMENT){		//评论数据加载完回调
				commentAdapter = new CommentItemAdapterForResult(getApplicationContext());
				detailComments.setAdapter(commentAdapter);
				if(!setListViewHeightBaseOnChildren(detailComments)){
					noComment.setVisibility(View.VISIBLE);
				}else{
					noComment.setVisibility(View.GONE);
				}
				
				if(detailImage != null){
					detailImage.setFocusable(true);
					detailImage.setFocusableInTouchMode(true);
					detailImage.requestFocus();
					detailImage.requestFocusFromTouch();			
				}
				//撤出loading
				if(loading != null)
					loading.setVisibility(View.GONE);
				
			}else if(msg.what == RELOAD_COMMENT){			//重新加载评论数据回调
				Log.e("threadId-handler:", String.valueOf(Thread.currentThread().getId()));
				if(commentAdapter != null)
					commentAdapter.notifyDataSetChanged();
				//如果没有数据，显示无评论提示
				if(!setListViewHeightBaseOnChildren(detailComments)){
					noComment.setVisibility(View.VISIBLE);
				}else{
					noComment.setVisibility(View.GONE);
				}
				
			}else if(msg.what == TOPIMAGE){					//顶部图片加载
				if(detailImage != null){
					detailImage.setImageBitmap(BitmapFactory.decodeStream((InputStream) msg.obj));
				}
				msg.obj = null;
				
			}else if(msg.what == SET_NEWIMAGE){
				int loc = msg.arg1;
				if(detailImageViews[loc] != null){
					detailImageViews[loc].setImageBitmap(detailImages[loc]);
				}
				
			}else if(msg.what == CANCLE_LOADINGIMAGE){		//撤出loading
				loading.setVisibility(View.GONE);
				
			}else if(msg.what == LOAD_COMPLEMENT){			//评论全部加载完，没有更多数据
				ToastUtils.showDefaultToast(DetailActivityForResult.this, "没有更多评论", Toast.LENGTH_SHORT);
				
			}else if(msg.what == RESET_ISINANIM){	//重设isInAnim
				isInAnim = false;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_detail);
		
		//字体
        AssetManager mgr = getAssets();//得到AssetManager
        tf = Typeface.createFromAsset(mgr, typeFaceDir);//根据路径得到Typeface
		
		Intent i = getIntent();
		game = (OneGameGame) i.getExtras().getSerializable("game");
		initView();	
		new Timer(true).schedule(new TimerTask() {
			
			@Override
			public void run() {
				myHandler.sendEmptyMessage(INITDATA);	
			}
		}, 500);	
	}

	public void share(String gameurl,final String gamename,final String gameid) {
		ShareSDK.initSDK(DetailActivityForResult.this);
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
		params.add(new BasicNameValuePair("game_id",Integer.toString(game.getId())));
		String str = HttpUtils.doPostWithoutStrict(Global.COMMENT_GETBYUSERID, params);
		params = null;
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
				comment.setUser_id(jsonObj.getInt("user_id"));
				
				comments.add(comment);
				comment = null;
			} 

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comments;
	}

	private void initView() {
		inflater = LayoutInflater.from(this);
		
		header = (RelativeLayout)findViewById(R.id.detail_header);
		footer = (LinearLayout)findViewById(R.id.detail_footer);
		
		container = (ScrollView)findViewById(R.id.container);
		container.setOnTouchListener(scrollOnTouchListener);
		
		noComment = (TextView)findViewById(R.id.nocomment);
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
		detailIntruction = (LinearLayout) findViewById(R.id.detail_instruction);
		
		detailComments = (ListView) findViewById(R.id.detail_comments);
		
		//设置字体
		noComment.setTypeface(tf);
		detailPraiseNo.setTypeface(tf);
		
		Log.e(TAG, "加载部分UI完毕");
		
		final OneGameGame game = this.game;
		
		detailPraiseNo.setText(Integer.toString(game.getPraise_num()));
//		detailIntruction.setText(game.getDetail());
		detail_image_url = game.getImage();	
		detailImage.setImageResource(R.drawable.defalut); 	//默认图片
		
		//加载detailImage
		detailImageStr = game.getDetail_image();
		detailImageUrls = detailImageStr.split(";");
		detailImages = new Bitmap[detailImageUrls.length];
		defaultBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.defalut2);
		detailImageViews = new ImageView[detailImageUrls.length];
		int l = 0;
		for(l=0;l<detailImageUrls.length;l++){
			detailImages[l] = defaultBitmap;
			detailImageViews[l] = (ImageView) inflater.inflate(R.layout.intruction_image, null);
			detailImageViews[l].setImageBitmap(detailImages[l]);
		}
		
		//设置图文并茂
		intruction = game.getDetail();
		String[] intructionArr = intruction.split("#image#");
		TextView text;
		for(l=0; l<intructionArr.length; l++){
			text = (TextView) inflater.inflate(R.layout.intruction_text, null);
			text.setText(intructionArr[l]);
			detailIntruction.addView(text);
			if(l == intructionArr.length-1){
				break;
			}
			if(l >= detailImageViews.length){
				 continue;
			}
//			detailImageViews[t] = (ImageView) inflater.inflate(R.layout.intruction_image, null);
//			detailImageViews[t].setImageBitmap(detailImages[t]);
			detailIntruction.addView(detailImageViews[l]);
		}
		isLoadImageViews = true;
		
		Log.e(TAG, "加载多图片");
		
		//加载点赞信息
		String key = "praise_"+game.getId();
		if(Global.readPraiseInfo(key, this)){	//已赞
			isPraise = 1;
		}else{
			isPraise = 2;
		}
		
		if(Global.isLogin()){
			new Thread(new Runnable() {
				@Override
				public void run() {
					//加载收藏
					String userid = Global.getUserId();
					String gameid = Integer.toString(game.getId());
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
						
						//切换图片
						if(isCollect == 1){
							myHandler.sendEmptyMessage(COLLECTED);
						}
						Log.d(TAG, isCollect+"  "+isPraise);
						json = null;
					} catch (JSONException e) {
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
		final int gameId = game.getId();
		
		if(!detail_image_url.contains("http://")){
			detailImage.setImageURI(Uri.fromFile(new File(detail_image_url)));
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
					downloadImage(detail_image_url);//加载顶部图片
				}
			}).start();
		}
		Log.e(TAG, "加载大图片");
		
		new Thread(new Runnable() {
			@Override
			public void run() {	
				int l = 0;
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.defalut2);
				Bitmap bitmap2;
				ContentResolver cr = getContentResolver();
				while(!isLoadImageViews){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
				for(l=0;l<detailImageUrls.length;l++){
					
					//图片若不是本地，则要下载到本地
					if(detailImageUrls[l].contains("http://")){
						String path = DownLoadUtils.downloadFile(detailImageUrls[l], getApplicationContext());
						detailImageStr = detailImageStr.replace(detailImageUrls[l], path);
						//修改数据库图片路径
						if(!path.equals("")	&& path!=null){
							ContentValues cv = new ContentValues();
							cv.put(OneGameColumn.ONEGAMEDETAILIMAGE, detailImageStr);
							String[] args = {String.valueOf(gameId)};
							cr.update(OneGameProvider.CONTENT_URI, cv, ""+OneGameColumn.ONEGAMEID+"=?", args);
						}
						detailImageUrls[l] = path;
					}
					//图片若是本地，则要加载
					if(!detailImageUrls[l].contains("http://")){
						detailImages[l] = BitmapFactory.decodeFile(detailImageUrls[l]);
					}
					//设置图片
					Message msg = new Message();
					msg.arg1 = l;
					msg.what = SET_NEWIMAGE;
					myHandler.sendMessage(msg);
					
					Log.d("detailImage", "第"+l+"张图片加载完毕");
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
				isLoadingComment = true;
				commentList = getCommentList();
				Log.e(TAG, "加载评论完毕");
				isLoadingComment = false;
				
				myHandler.sendEmptyMessage(COMMENT_DATA_COMPLEMENT);
			}
		}).start();
	}
	
	private void downloadImage(String url) {
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

	/**
	 * ScrollView下的listview需要调整高度
	 * @param listView
	 * @return	若无数据，返回false
	 */
	public boolean setListViewHeightBaseOnChildren(ListView listView) {
		Log.e(TAG, "高度计算1");
		CommentItemAdapterForResult listAdapter = (CommentItemAdapterForResult)listView.getAdapter();   
        if (listAdapter == null) {  
            return false;  
        }  
        Log.e(TAG, "高度计算2");
        if(commentList.size() == 0){
        	ViewGroup.LayoutParams params = listView.getLayoutParams(); 
        	params.height = 50;
        	listView.setLayoutParams(params);  
        	return false;
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
//            totalHeight += listItem.getMeasuredHeight();  
        }
        Log.e(TAG, "高度计算5");
        
        ViewGroup.LayoutParams params = listView.getLayoutParams();  
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        //增加一些距离作为缓冲
        params.height += getWindowManager().getDefaultDisplay().getHeight()/8;
        listView.setLayoutParams(params); 
        Log.e(TAG, "高度计算6");
        commentListHeight = params.height;
        return true;
	}
	
	@Override
	public void onClick(View v) {
		final String useid = Global.getUserId();
		final String usename = Global.getUserNmae();
		final String gameid = game.getId()+"";
		final String gamename = game.getGame_name();
		final String gameurl = game.getDownload_url();
		final String userimage = Global.getUserImage();
		if (v == detailBack) {
//			destroy();
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			destroy();
		}else if(v == detailPraise){	
			//点赞
//			if(Global.checkLogin(this)){
				if(isPraise == 2){
					ToastUtils.showDefaultToast(getApplicationContext(), "点赞成功", Toast.LENGTH_SHORT);
					praise(useid, usename, gamename, gameid);
					game.setPraise_num(game.getPraise_num()+1);
					detailPraiseNo.setText(Integer.toString(Integer.parseInt((String) detailPraiseNo.getText())+1));
				}else if(isPraise == 1){
					ToastUtils.showDefaultToast(getApplicationContext(), "取消点赞成功", Toast.LENGTH_SHORT);
					canclePraise(praiseId, gameid);
					game.setPraise_num(game.getPraise_num()-1);
					detailPraiseNo.setText(Integer.toString(Integer.parseInt((String) detailPraiseNo.getText())-1));
				}else if(isPraise == 3){
					ToastUtils.showDefaultToast(getApplicationContext(), "我又不是点读机，点多了我会坏的(╯｀□′)╯（┻━┻）", Toast.LENGTH_SHORT);
				}else if(isPraise == 0){
					ToastUtils.showDefaultToast(getApplicationContext(), "正在加载数据，请稍后重试", Toast.LENGTH_SHORT);
				}
//			}
		}else if(v == detailShare){
			if(Global.checkLogin(this)){
				share(gameurl,gamename,gameid);
			}
		}else if(v == detailCollect){
			if(Global.checkLogin(this)){
				if(isCollect == 2){
					ToastUtils.showDefaultToast(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT);
					collect(useid, usename, gamename, gameid);
					detailCollect.setImageResource(R.drawable.detail_icon_fav1_normal);
				}else if(isCollect == 1){
					cancleCollect(collectId,gameid);
					ToastUtils.showDefaultToast(getApplicationContext(), "取消收藏成功", Toast.LENGTH_SHORT);
					detailCollect.setImageResource(R.drawable.detail_icon_fav_normal);
				}else if(isPraise == 3){
					ToastUtils.showDefaultToast(getApplicationContext(), "我又不是点读机，点多了我会坏的(╯｀□′)╯（┻━┻）", Toast.LENGTH_SHORT);
				}else if(isPraise == 0){
					ToastUtils.showDefaultToast(getApplicationContext(), "正在加载数据，请稍后重试", Toast.LENGTH_SHORT);
				}
			}
		}else if(v == detailComment){
			if(Global.checkLogin(this)){
				LayoutInflater factory = LayoutInflater.from(getApplicationContext());
				View DialogView = factory.inflate(R.layout.comment_layout, null);
				//当天时间
				Calendar c = Calendar.getInstance();
		        int mMonth = c.get(Calendar.MONTH);//获取当前月份
		        int mDay = c.get(Calendar.DAY_OF_MONTH);//获取当前月份的日期号码
		        int mWeek = c.get(Calendar.DAY_OF_WEEK);
				String showTime = "";
				showTime = (mMonth+1)+"月"+mDay+"日	"+WEEKS_CN[mWeek-1];
				
				TextView time = (TextView)DialogView.findViewById(R.id.time);
				time.setText(showTime);
				time.setTypeface(tf);
				
				final TextView comment = (TextView)DialogView.findViewById(R.id.comment);
				comment.setTypeface(tf);
				
				final AlertDialog dlg = new AlertDialog.Builder(this)
		    	.setView(DialogView)
				.create();
		    	dlg.show();
		    	
		    	//评论完成按钮
		    	ImageView commentBtn = (ImageView) DialogView.findViewById(R.id.comment_btn);
		    	commentBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//获取评论
						String commentStr = comment.getText().toString();
						if(commentStr.trim().equals("")){
							ToastUtils.showDefaultToast(getApplicationContext(), "评论不能为空", Toast.LENGTH_SHORT);
							return;
						}
						comment(useid, usename, gamename, gameid,commentStr,userimage);
						ToastUtils.showDefaultToast(getApplicationContext(), "评论成功", Toast.LENGTH_SHORT);
						//重新加载评论
//						reloadComment();
						newComment(useid, usename, commentStr,userimage,useid);
						
						//关闭对话框
						dlg.dismiss();
					}
				});
			}
		}else if(v == detailDownload){
			Toast.makeText(this, "正在跳转到下载地址...", Toast.LENGTH_SHORT).show();
			Uri uri = Uri.parse(gameurl);    
			Intent it = new Intent(Intent.ACTION_VIEW, uri);    
			startActivity(it);  
		}
	}
	
	//点赞
	private void praise(final String user_id,final String user_name,final String game_name,final String game_id){
		isPraise = 3;
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
		//写入sharepreference
		String key = "praise_"+game_id;
		Global.writePraiseInfo(key, this, 1);
	}
	
	//取消点赞
	private void canclePraise(final String praise_id,final String game_id){
		isPraise = 3;
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
		//写入sharepreference
		String key = "praise_"+game_id;
		Global.writePraiseInfo(key, this, 0);
	}
	
	//收藏
	private void collect(final String user_id,final String user_name,final String game_name,final String game_id){
		isCollect = 3;
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
		isCollect = 3;
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

	//回收
	public void destroy(){
		for(int i = 0 ;i<detailImages.length;i++){
			if(detailImages[i] != null)
				detailImages[i].recycle();
		}
		if(defaultBitmap != null){
			defaultBitmap.recycle();
		}
		if(commentList != null	&& commentList.size()!=0){
			commentList.clear();
		}
//		for(int i = 0 ;i<detailImageViews.length;i++){
//			if(detailImageViews[i] != null)
//				detailImageViews[i] = null;
//		}
		if(loading != null){
			loading.destory();
		}
		if(commentAdapter != null){
			commentAdapter.destory();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			destroy();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	
	//添加新评论
	private void newComment(String useid,String usename,String commentStr,String userimage,String userid){
		List<OneGameComment> newlist = new ArrayList<OneGameComment>();
		
		OneGameComment comment = new OneGameComment();
		comment.setUser_name(usename);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		comment.setComment_time(format.format(new Date()));
		comment.setComment(commentStr);
		comment.setUser_image(userimage);
		comment.setUser_id(Integer.valueOf(userid));

		newlist.add(comment);
		copyList(newlist, commentList);
		commentList = newlist;
		commentAdapter.notifyDataSetChanged();
		setListViewHeightBaseOnChildren(detailComments);
		newlist = null;
	}

	private void copyList(List<OneGameComment> a , List<OneGameComment> b){
		OneGameComment comment ;
		for(int i = 0 ;i<b.size();i++){
			comment = b.get(i);
			a.add(comment);
		}
	}
	
	
	/**
	 * 加载更多评论
	 */
	private void loadMoreComment(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List <NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("pageSize","10"));
				params.add(new BasicNameValuePair("pageNo",String.valueOf(loadCommentPageNo)));
				params.add(new BasicNameValuePair("game_id",Integer.toString(game.getId())));
				String str = HttpUtils.doPostWithoutStrict(Global.COMMENT_GETBYUSERID, params);
				
				JSONObject json;
				JSONArray info = new JSONArray();
				List<OneGameComment> newlist = new ArrayList<OneGameComment>();
				
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
						comment.setUser_id(jsonObj.getInt("user_id"));
						//添加新评论到ListView
						newlist.add(comment);
					} 
					copyList(commentList,newlist);
					newlist = null;
					Log.e("threadId-load:", String.valueOf(Thread.currentThread().getId()));
					//通知适配器刷新
					myHandler.sendEmptyMessage(RELOAD_COMMENT);
					//去掉加载中图片
					myHandler.sendEmptyMessage(CANCLE_LOADINGIMAGE);
					//若无数据加载，则提醒
					if(info.length()==0)
						myHandler.sendEmptyMessage(LOAD_COMPLEMENT);
					
					//如果已经没有数据可加载，关闭加载评论功能
					if(info.length()!=0){
						isLoadingComment = false;
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	//加载评论的后台异步task
	private class LoadMoreCommentAsyncTask extends AsyncTask<String, Integer, List<OneGameComment>>{

		@Override
		protected List<OneGameComment> doInBackground(String... params) {
			List <NameValuePair> param = new ArrayList<NameValuePair>();
			param.add(new BasicNameValuePair("pageSize","10"));
			param.add(new BasicNameValuePair("pageNo",String.valueOf(loadCommentPageNo)));
			param.add(new BasicNameValuePair("game_id",Integer.toString(game.getId())));
			String str = HttpUtils.doPostWithoutStrict(Global.COMMENT_GETBYUSERID, param);
			
			JSONObject json;
			JSONArray info = new JSONArray();
			List<OneGameComment> newlist = new ArrayList<OneGameComment>();
			
			try {
				json = new JSONObject(str);
				info = json.getJSONArray("listData");

				for (int i = 0; i < info.length(); i++) {
					JSONObject jsonObj = ((JSONObject) info.opt(i));

					OneGameComment comment = new OneGameComment();
					comment.setUser_name(jsonObj.getString("user_name"));
					comment.setComment_time(jsonObj.getString("comment_time"));
					comment.setComment(jsonObj.getString("comment"));
					comment.setUser_image(jsonObj.getString("user_image"));
					comment.setUser_id(jsonObj.getInt("user_id"));
					// 添加新评论到ListView
					newlist.add(comment);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return newlist;
		}

		@Override
		protected void onPostExecute(List<OneGameComment> result) {
			super.onPostExecute(result);
			
			copyList(commentList,result);
			Log.e("threadId-load:", String.valueOf(Thread.currentThread().getId()));
			//通知适配器刷新
			myHandler.sendEmptyMessage(RELOAD_COMMENT);
			//去掉加载中图片
			myHandler.sendEmptyMessage(CANCLE_LOADINGIMAGE);
			//若无数据加载，则提醒
			if(result.size()==0)
				myHandler.sendEmptyMessage(LOAD_COMPLEMENT);
			
			//如果已经没有数据可加载，关闭加载评论功能
			if(result.size()!=0){
				isLoadingComment = false;
			}
			
			result = null;
		}
		
		

	};

	private OnTouchListener scrollOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				touchY = event.getY();
			}else if(event.getAction() == MotionEvent.ACTION_MOVE){
				int scrollY=v.getScrollY();
                int height=v.getHeight();
                int scrollViewMeasuredHeight=container.getChildAt(0).getMeasuredHeight();
                float y = event.getY();
                Log.d(TAG, touchY + "   "+ y+"  "+isHidedHeadFoot);
                if(!isHidedHeadFoot){
                	if(isInAnim){
                		hideHeadAndFootWithoutAnim();
                		isHidedHeadFoot = true;
                	}else{
						// 隐藏顶底部
						isHidedHeadFoot = true;
						hideHeadAndFoot();
                	}
				}
                if(scrollY==0){
                   }
                if((scrollY+height)==scrollViewMeasuredHeight){
                       System.out.println("滑动到了底部 scrollY="+scrollY);
                       System.out.println("滑动到了底部 height="+height);
                       System.out.println("滑动到了底部 scrollViewMeasuredHeight="+scrollViewMeasuredHeight);
                       if(!isLoadingComment){
	                   		isLoadingComment = true;
	   	                	loadCommentPageNo++;
	   	                	//显示加载图片
	   	                	loading.setVisibility(View.VISIBLE);
	   	                	//加载数据
//	   	                	loadMoreComment();
	   	                	//用LoadMoreCommentAsyncTask取代上面那个方法
	   	                	LoadMoreCommentAsyncTask task = new LoadMoreCommentAsyncTask();
	   	                	task.execute("");
	                   	}
                   }
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				 if(isHidedHeadFoot){
					//显示顶底部
             		showHeadAndFoot();
             		isHidedHeadFoot = false;
//             		if(isInAnim	&& timer!=null){
//             			timer.cancel();
//             		}
//         			isInAnim = true;
//         			timer = new Timer();
//         			timer.schedule(new TimerTask() {
//						
//						@Override
//						public void run() {
//							isInAnim = false;
//						}
//					}, animMillis);
             		if(isInAnim){
             			myHandler.removeMessages(RESET_ISINANIM);
             		}
             		isInAnim = true;
             		myHandler.sendEmptyMessageDelayed(RESET_ISINANIM, animMillis);
				 }
			}
			
			return false;
		}
	};
	
	
	//隐藏顶部和底部
	private void hideHeadAndFoot(){
		header.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_top));
		footer.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_bottom));
		header.setVisibility(View.GONE);
		footer.setVisibility(View.GONE);
	}

	//隐藏顶部和底部
	private void hideHeadAndFootWithoutAnim(){
		header.setVisibility(View.GONE);
		footer.setVisibility(View.GONE);
	}
	
	//显示顶部和底部
	private void showHeadAndFoot(){
		header.setVisibility(View.VISIBLE);
		footer.setVisibility(View.VISIBLE);
		header.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_top));
		footer.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom));
	}
}
