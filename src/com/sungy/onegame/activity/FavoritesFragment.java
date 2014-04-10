package com.sungy.onegame.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sungy.onegame.mclass.*;

import com.sungy.onegame.MainActivity;
import com.sungy.onegame.R;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.RelativeLayout;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class FavoritesFragment extends Fragment {
	static ListView favoritesList;
	private MyAdapter mAdapter;
	private Switch switcher;
	private Button deleteButton, cancelButton;
	private static RelativeLayout buttonRL;
	
	private final String TAG = "FavoritesFragment";
	private String userid;
	private String str;
	
	private ArrayList<String> list = new ArrayList<String>();
	private List<NameValuePair> data = new ArrayList<NameValuePair>();
	private int checkNum;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.favorites, null);
		ImageView left = (ImageView) view.findViewById(R.id.favorite_left);
		left.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Log.d(TAG, "showLeft");
				((MainActivity) getActivity()).showLeft();
			}
		});
		
		//userid
		userid = Global.getUserId();
		buttonRL = (RelativeLayout)view.findViewById(R.id.fedit);
		deleteButton = (Button)view.findViewById(R.id.fdelete);
		cancelButton = (Button)view.findViewById(R.id.fcancel);
		deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//TODO delete item and refresh
			}
		});
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//handler.sendEmptyMessage(1);
				ArrayList<Integer> selectArr = mAdapter.getAllSelected();
				for(Integer i : selectArr){
					ViewHolder viewHolder = (ViewHolder)favoritesList.getChildAt(i).getTag();
					viewHolder.cb.toggle();
				}
				mAdapter.initData();
				FavoritesFragment.handl_visible.sendEmptyMessage(0);
			}
		});
		
		switcher = (Switch)view.findViewById(R.id.switch1);
		switcher.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  
                // TODO Auto-generated method stub  
                if (isChecked) {
                	Log.d(TAG, "state is on");
                    //state is on
                } else {  
                	Log.d(TAG, "state is off");
                    //state is off
                }  
            }  
        });
        
		favoritesList = (ListView)view.findViewById(R.id.favorites_list);
		getGameData();
		mAdapter = new MyAdapter(list, getActivity());
		favoritesList.setAdapter(mAdapter);
		favoritesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
            	ViewHolder holder = (ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                // 将CheckBox的选中状况记录下来
                MyAdapter.getIsSelected().put(arg2, holder.cb.isChecked()); 
                // 调整选定条目
                if (holder.cb.isChecked() == true) {
                    checkNum++;
                } else {
                    checkNum--;
                }
                // 用TextView显示
               Log.d(TAG, "已选中"+checkNum+"项");              
            }
        });
		return view;
	}
	
	static Handler handl_visible =new Handler(){
	    //当有消息发送出来的时候就执行Handler的这个方法
    	@Override	    
	    public void handleMessage(Message msg){
		    super.handleMessage(msg);
		    switch (msg.what) {
		    case 0:  
                buttonRL.setVisibility(View.INVISIBLE);
                for(int i = 0; i<favoritesList.getChildCount(); i++){
            		ViewHolder viewHolder = (ViewHolder)favoritesList.getChildAt(i).getTag();
            		viewHolder.cb.setFocusable(false);
            	}
                break;
            case 1:
            	buttonRL.setVisibility(View.VISIBLE);
            	for(int i = 0; i<favoritesList.getChildCount(); i++){
            		ViewHolder viewHolder = (ViewHolder)favoritesList.getChildAt(i).getTag();
            		viewHolder.cb.setFocusable(true);
            	}
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
    	mAdapter.initData();
    	mAdapter.notifyDataSetChanged();
    	Log.d(TAG, "Update Completed");
    }
    
	private void getGameData()
	{
		Log.e(TAG, "getGameData is excuting.");
		NameValuePair pair0, pair1, pair2;
		String pageSize = "10", pageNo = "1";		
		
		pair0 = new BasicNameValuePair("user_id", userid);
		pair1 = new BasicNameValuePair("pageSize", pageSize);
		pair2 = new BasicNameValuePair("pageNo", pageNo);
		data.clear();
		data.add(pair0);
		data.add(pair1);
		data.add(pair2);
		
		//获取收藏列表
		new Thread(){
			@Override
			public void run()
			{
				ArrayList<String> favoriteGameId = new ArrayList<String>();
				NameValuePair pair3;
				str = HttpUtils.doPost(Global.COLLECT_GETBYUSERID, data);
				JSONObject json;
				String message = "";
				JSONArray listData = null;
				//System.out.println(str);
				try {
					json = new JSONObject(str);
					message = json.getString( "message" );
					Log.d(TAG, "message: "+message);			
					listData = json.getJSONArray( "listData" );			
					//Log.d(TAG, "listData: "+json.getString("listData"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "ERROR");
					e.printStackTrace();
				}
				if(null == listData)
	            	return;
				for(int i = 0; i<listData.length(); i++)
				{
					String temp;
					try{
						temp = listData.getJSONObject(i).getString("game_id");
						Log.d(TAG, "game_id: "+temp);
						favoriteGameId.add(temp);
					} catch (JSONException e){
						Log.e(TAG, "ERROR");
						e.printStackTrace();
					}
					
				}
				//获取游戏图片url
				for(int i = 0; i<favoriteGameId.size(); i++)
				{
					String gameId = favoriteGameId.get(i);
					pair3 = new BasicNameValuePair("id", gameId);
					data.clear();
					str = null;
					data.add(pair3);
					
					final int index = i;
					final int end = favoriteGameId.size() - 1;
					new Thread(){
						@Override
						public void run()
						{
							str = HttpUtils.doPost(Global.GAME_GETGAMEBYID, data);
							//System.out.println(str);
							try {
								JSONObject json = new JSONObject(str);
								String message = json.getString( "message" );
								Log.d(TAG, "message: "+message);			
								JSONObject game = json.getJSONObject( "rowdata" );			
								//Log.d(TAG, "rowdata: "+json.getString("rowdata"));								
								list.add(game.getString("image"));
								if(index == end)
									handler.sendEmptyMessage(1);
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								Log.e(TAG, "getUrlERROR");
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		}.start();
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}
