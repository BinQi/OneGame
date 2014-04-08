package com.sungy.onegame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sungy.onegame.mclass.FileUtil;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;

public class OneGameItemAdapter extends BaseAdapter{
	private Context mContext = null;
	private LayoutInflater inflater;
	private ViewHolder holder;

	OneGameItemAdapter(Context c){
		mContext = c;
		inflater = LayoutInflater.from(c);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return SampleListFragment.gameList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return SampleListFragment.gameList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(final int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(arg1==null){
			holder = new ViewHolder();
			arg1 = inflater.inflate(R.layout.game_item, null);

			holder.game_day = (TextView)arg1.findViewById(R.id.day);
			holder.game_title = (TextView)arg1.findViewById(R.id.title);
			holder.game_abstract = (TextView)arg1.findViewById(R.id.abstractt);
			holder.game_praiseNo = (TextView)arg1.findViewById(R.id.praiseNo);
			
			holder.game_praise = (ImageView)arg1.findViewById(R.id.praise);
			holder.game_image = (ImageView)arg1.findViewById(R.id.image);
			
			arg1.setTag(holder);
		}else{
			holder = (ViewHolder)arg1.getTag();
		}

		//显示正在加载
		holder.game_image.setImageResource(R.drawable.defalut);
		holder.game_day.setText(SampleListFragment.gameList.get(arg0).getPublish_time().substring(5, 10));
		holder.game_title.setText(SampleListFragment.gameList.get(arg0).getGame_name());
		String str = SampleListFragment.gameList.get(arg0).getIntroduction();
		holder.game_abstract.setText(str);
		holder.game_praiseNo.setText(Integer.toString(SampleListFragment.gameList.get(arg0).getPraise_num()));

		holder.game_image_url = SampleListFragment.gameList.get(arg0).getImage();	
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				downloadImage(holder.game_image_url);
//			}
//		});
		downloadImage(holder.game_image_url);
		
		holder.game_praise.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {			
			}
		});
				
		return arg1;
	}
	
	
	@SuppressLint("NewApi")
	private void downloadImage(String url) {
		// TODO Auto-generated method stub
		if(fileIsExist(url)){
			FileInputStream fis;
			try {
				fis = new FileInputStream(getPath(url));
				holder.game_image.setImageBitmap(BitmapFactory.decodeStream(fis)); 
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}             
		}
		
	}

	private boolean fileIsExist(String url) {
		// TODO Auto-generated method stub
		File f;
		try {
			f = new File(getPath(url));
			if(!f.exists()){
                return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return true;
	}
	private String getPath(String str) throws IOException 
    { 
        String path = FileUtil.setMkdir(mContext)+File.separator+str.substring(str.lastIndexOf("/")+1); 
        return path; 
    } 
	

	class ViewHolder{
		TextView game_title,game_praiseNo,game_abstract,game_day;
		ImageView game_praise,game_image;
		String game_image_url;
		
	}
	
	//点赞
	private void addPraise(String user_id,String user_name,String game_name,String game_id){
		//post请求的参数
		List <NameValuePair> params = new ArrayList<NameValuePair>(); 
		params.add(new BasicNameValuePair("user_id",user_id)); 
		params.add(new BasicNameValuePair("user_name",user_name)); 
		params.add(new BasicNameValuePair("game_name",game_name)); 
		params.add(new BasicNameValuePair("game_id",game_id)); 
		String str = HttpUtils.doPost(Global.PRAISE_PRAISE, params);
	}
}
