package com.sungy.onegame.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sungy.onegame.mclass.*;
import com.sungy.onegame.view.ImageTextButton;

import com.sungy.onegame.MainActivity;
import com.sungy.onegame.R;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.RelativeLayout;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class FavoritesFragment extends Fragment implements FragmentInterface{
	private GridView favoritesList;
	private MyAdapter mAdapter;
	//private Switch switcher;
	private ProgressBar progressBar;
	private ImageTextButton deleteButton, cancelButton;
	private static RelativeLayout buttonRL;
	private Toast toast;
	//private ProgressDialog progressDialog;
	private final String TAG = "FavoritesFragment";
	private String userid;
	private static boolean editmode = false;
	private boolean ifdelete = false;
	private static boolean ifcancel = false;
	private boolean deletefinish = true;
	
	private ArrayList<FavoriteGame> list = new ArrayList<FavoriteGame>();
	//private List<NameValuePair> data = new ArrayList<NameValuePair>();
	private String[] MonthEnglish = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	class FavoriteGame {
		public FavoriteGame(String id, String cid, Date datetime, String gname, String time){
			collect_id = cid;
			game_name = gname;
			collect_time = time;
			this.id = id;
			this.datetime = datetime;
			//bitmap = null;
		}
		
		public void setUrl(String url){
			this.url = url;
		}
		
		//public void setBitmap(Bitmap bitmap){
		//	this.bitmap = Bitmap.createBitmap(bitmap);
		//}
		
		public String collect_id;
		public String id;
		public String url;
		public String game_name;
		public String collect_time;
		public Date datetime;
		//public Bitmap bitmap;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.favorites, null);
		/*ImageView left = (ImageView) view.findViewById(R.id.favorite_left);
		left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Log.d(TAG, "showLeft");
				((MainActivity) getActivity()).showLeft();
			}
		});
		*/
		//userid
		userid = Global.getUserId();
		
		//progressDialog = ProgressDialog.show(getActivity(), "加载中", "请稍后,正在加载...");
		View toastRoot = getActivity().getLayoutInflater().inflate(R.layout.progressbar_toast, null);
		progressBar = (ProgressBar)toastRoot.findViewById(R.id.fprogressBar);
		progressBar.setVisibility(View.VISIBLE);
		RelativeLayout rl = (RelativeLayout)toastRoot.findViewById(R.id.progress_toast_layout);
		rl.getBackground().setAlpha(0);
    	toast = new Toast(getActivity());
    	toast.setView(toastRoot);
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.setDuration(Toast.LENGTH_LONG);
    	toast.show();
    	
		buttonRL = (RelativeLayout)view.findViewById(R.id.fedit);
		deleteButton = (ImageTextButton)view.findViewById(R.id.fdelete);
		cancelButton = (ImageTextButton)view.findViewById(R.id.fcancel);
		deleteButton.setImgResource(R.drawable.clip);
		deleteButton.setText("限免信息推送");
		cancelButton.setImgResource(R.drawable.pencil);
		cancelButton.setText("编辑");
		deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(ifdelete) {
					if(!deletefinish)
						return;
					else
						deletefinish = false;
					NameValuePair pair0, pair1;
					String gameId, cid;
					boolean delete_success = true;
					ArrayList<FavoriteGame> sub = new ArrayList<FavoriteGame>();
					ArrayList<FavoriteGame> sub1 = new ArrayList<FavoriteGame>();
					ArrayList<Integer> allSelected = mAdapter.getAllSelected();
					for(int index = 0; index<allSelected.size(); index++) {
						Integer i = allSelected.get(index);
						sub.add(list.get(i));
	
						gameId = list.get(i).id;
						cid = list.get(i).collect_id;
						pair0 = new BasicNameValuePair("id", cid);
						pair1 = new BasicNameValuePair("game_id", gameId);
						List<NameValuePair> data = new ArrayList<NameValuePair>();
						data.add(pair0);
						data.add(pair1);
						String str = HttpUtils.doPost(Global.COLLECT_CANCLECOLLECT, data);
						//System.out.println(str);
						try {
							JSONObject json = new JSONObject(str);
							String message = json.getString( "message" );
							if(!message.equals("success"))
								delete_success = false;
							Log.e(TAG, "delete Favorite game message: "+message);			
							
						} catch (JSONException e) {
							Log.e(TAG, "deleteFavoriteERROR");
							e.printStackTrace();
						}
					}
	
					list.removeAll(sub);
					Log.e("xxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxx: "+((Integer)list.size()).toString());
					mAdapter.initData();
			    	mAdapter.notifyDataSetChanged();
			    	
					View toastRoot = getActivity().getLayoutInflater().inflate(R.layout.my_toast, null);
					TextView tv = (TextView)toastRoot.findViewById(R.id.toast_text);
					if(delete_success) {
						if(allSelected.size()!=0)
							tv.setText("删除成功！");
						else
							tv.setText("请选择！");
					}
					else
						tv.setText("删除失败！");
	        		RelativeLayout rl = (RelativeLayout)toastRoot.findViewById(R.id.toast_layout);
	        		rl.getBackground().setAlpha(50);
			    	Toast mytoast = new Toast(getActivity());
			    	mytoast.setView(toastRoot);
			    	mytoast.setGravity(Gravity.CENTER, 0, 0);
			    	mytoast.setDuration(Toast.LENGTH_SHORT);
			    	mytoast.show();
			    	deletefinish = true;
				}
				else {}
			}
		});
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//handler.sendEmptyMessage(1);
				if(ifcancel) {
					//ArrayList<Integer> selectArr = mAdapter.getAllSelected();
					for(int i = 0; i<mAdapter.getCount(); i++){
						View temp = favoritesList.getChildAt(i);
						if(temp != null) {
							ViewHolder viewHolder = (ViewHolder)temp.getTag();
							//viewHolder.cb.toggle();
							viewHolder.cb.setVisibility(View.INVISIBLE);
						}
					}
					editmode = false;
					ifcancel = false;
					ifdelete = false;
					deleteButton.setImgResource(R.drawable.clip);
					deleteButton.setText("限免信息推送");
					cancelButton.setText("编辑");
					mAdapter.initData();
					//FavoritesFragment.handl_visible.sendEmptyMessage(0);
				}
				else {
					for(int i = 0; i<mAdapter.getCount(); i++){
						View temp = favoritesList.getChildAt(i);
						if(temp != null) {
							ViewHolder viewHolder = (ViewHolder)temp.getTag();
							//viewHolder.cb.toggle();
							viewHolder.cb.setVisibility(View.VISIBLE);
						}
					}
					editmode = true;
					ifcancel = true;
					ifdelete = true;
					deleteButton.setImgResource(R.drawable.delete);
					deleteButton.setText("删除");
					cancelButton.setText("取消编辑");
				}
			}
		});
		
		/*switcher = (Switch)view.findViewById(R.id.switch1);
		switcher.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
                if (isChecked) {
                	Log.d(TAG, "state is on");
                    //state is on
                } else {  
                	Log.d(TAG, "state is off");
                    //state is off
                }  
            }  
        });*/
        
		favoritesList = (GridView)view.findViewById(R.id.favorites_list);
		handl_getdata.sendEmptyMessage(1);
		mAdapter = new MyAdapter(list, favoritesList, getActivity());
		favoritesList.setAdapter(mAdapter);
		favoritesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	if(editmode){
	                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
	            	ViewHolder holder = (ViewHolder) arg1.getTag();
	                // 改变CheckBox的状态
	                holder.cb.toggle();
	                // 将CheckBox的选中状况记录下来
	                MyAdapter.getIsSelected().put(arg2, holder.cb.isChecked());            
            	}
            	else{
            		String id = list.get(arg2).id;
            		int index = Global.getDetailList().get(id);
            		Bundle bundle = new Bundle();
            		bundle.putInt("index", index);
            		Intent i = new Intent(getActivity(), DetailActivity.class);
            		i.putExtras(bundle);
            		startActivity(i);
            	}
            }
        });
		return view;
	}
	
	Handler handl_getdata =new Handler(){
	    //当有消息发送出来的时候就执行Handler的这个方法
    	@Override	    
	    public void handleMessage(Message msg){
		    super.handleMessage(msg);
		    switch (msg.what) {
            case 1:
            	getGameData();
            	break;
            }
	    }
    };
    
	static Handler handl_visible =new Handler(){
	    //当有消息发送出来的时候就执行Handler的这个方法
    	@Override	    
	    public void handleMessage(Message msg){
		    super.handleMessage(msg);
		    switch (msg.what) {
		    case 0:  
                buttonRL.setVisibility(View.INVISIBLE);
                editmode = false;
                break;
            case 1:
            	buttonRL.setVisibility(View.VISIBLE);
            	editmode = true;
            	break;
            }
	    }
    };
    
	@SuppressLint("HandlerLeak") private Handler handler =new Handler(){
	    //当有消息发送出来的时候就执行Handler的这个方法
    	@Override	    
	    public void handleMessage(Message msg){
		    super.handleMessage(msg);
		    switch (msg.what) {
            case 1:  
                update();
                break;  
            }
	    }
    };
    
    private void update()
    {
    	Log.d(TAG, "Update");
    	//mAdapter.initData();
    	mAdapter.notifyDataSetChanged();
    	toast.cancel();//progressDialog.dismiss();
    	Log.d(TAG, "Update Completed");
    }
    
    class MyComparator implements Comparator{
		@Override
		public int compare(Object a, Object b) {
			Date d0 = ((FavoriteGame)a).datetime;
			Date d1 = ((FavoriteGame)b).datetime;
			if(d0.before(d1))
				return 1;
			else if(d0.after(d1))
				return -1;
			return 0;
		}
    }
    
	private void getGameData()
	{
		Log.e(TAG, "getGameData is excuting.");
		list.clear();
		
		
		//获取收藏列表
		new Thread(){
			@Override
			public void run()
			{
				NameValuePair pair0, pair1, pair2;
				String pageSize = "10", pageNo = "1";		
				
				pair0 = new BasicNameValuePair("user_id", userid);
				pair1 = new BasicNameValuePair("pageSize", pageSize);
				pair2 = new BasicNameValuePair("pageNo", pageNo);
				List<NameValuePair> data = new ArrayList<NameValuePair>();
				data.add(pair0);
				data.add(pair1);
				data.add(pair2);
				
				String str = HttpUtils.doPost(Global.COLLECT_GETBYUSERID, data);
				JSONObject json;
				String message = "";
				JSONArray listData = null;
				Log.e(TAG, str);
				try {
					json = new JSONObject(str);
					message = json.getString( "message" );
					Log.d(TAG, "message: "+message);			
					listData = json.getJSONArray( "listData" );			
					//Log.d(TAG, "listData: "+json.getString("listData"));
				} catch (JSONException e) {
					Log.e(TAG, "ERROR");
					e.printStackTrace();
				}
				if(null == listData)
	            	return;
				for(int i = 0; i<listData.length(); i++)
				{
					String cid, tempid, tempdate, gname, collect_time;
					try{
						cid = listData.getJSONObject(i).getString("id");
						tempid = listData.getJSONObject(i).getString("game_id");
						tempdate = listData.getJSONObject(i).getString("collect_time");
						gname = listData.getJSONObject(i).getString("game_name");
						String pattern = "yyy-MM-dd HH:mm:ss"; //首先定义时间格式
				        SimpleDateFormat format = new SimpleDateFormat(pattern);
				        Date datetime = new Date();
				        try{
				        	datetime = format.parse(tempdate);
				        }catch(ParseException e) {
				            e.printStackTrace();
				        }
				        collect_time = ((Integer)datetime.getDate()).toString() + " ";
				        collect_time += MonthEnglish[datetime.getMonth()];
				        
				        Log.e(TAG, "game_id: "+tempid+" "+tempdate);
						list.add(new FavoriteGame(tempid, cid, datetime, gname, collect_time));
					} catch (JSONException e){
						Log.e(TAG, "ERROR");
						e.printStackTrace();
					}
					
				}
				Collections.sort(list, new MyComparator());
				mAdapter.initData();
				/*Log.e(TAG, "After sort:");
				for(FavoriteGame i : favoriteGame)
					Log.e(TAG, i.datetime.toString());*/
				//获取游戏图片url
				for(int i = 0; i<list.size(); i++)
				{
					final String gameId = list.get(i).id;
					final int index = i;					
					new Thread(){
						@Override
						public void run()
						{
							List<NameValuePair> data = new ArrayList<NameValuePair>();
							NameValuePair pair3 = new BasicNameValuePair("id", gameId);
							data.add(pair3);
							String str = null;
							str = HttpUtils.doPost(Global.GAME_GETGAMEBYID, data);
							if(null == str)
							{
								Log.e("TAG", "Post GameById str null");
								return;
							}
							//System.out.println(str);
							try {
								JSONObject json = new JSONObject(str);
								String message = json.getString( "message" );
								Log.d(TAG, "message: "+message);
								if(message.equals("success")) {
									JSONObject game = json.getJSONObject( "rowdata" );
									Log.e(TAG, "gameId: "+data.get(0).getValue());				
									Log.e(TAG, "url: "+game.getString("image"));
									String url = game.getString("image");
									list.get(index).setUrl(url);
								}
								//list.get(index).setBitmap(returnBitMap(url));
								handler.sendEmptyMessage(1);
								
							} catch (JSONException e) {
								Log.e(TAG, "getUrlERROR");
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		}.start();
	}
	
	public Bitmap returnBitMap(String url) { 
    	URL myFileUrl = null; 
    	Bitmap bitmap = null; 
    	try { 
    		myFileUrl = new URL(url); 
    	} catch (MalformedURLException e) {
    		Log.e("TAG", "returnBitMap URL error");
    		e.printStackTrace(); 
    	} 
    	try { 
	    	HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection(); 
	    	conn.setDoInput(true); 
	    	conn.connect(); 
	    	InputStream is = conn.getInputStream(); 
	    	bitmap = BitmapFactory.decodeStream(is); 
	    	is.close(); 
    	} catch (IOException e) { 
    		Log.e("TAG", "returnBitMap bitmap null error");
    		e.printStackTrace(); 
    	} 
    	return bitmap; 
    } 
	
	public static boolean getIfcancel() {
		return ifcancel;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onBackPressed() {
		((MainActivity) getActivity()).showLeft();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			((MainActivity) getActivity()).showLeft();
		return true;
	}
}
