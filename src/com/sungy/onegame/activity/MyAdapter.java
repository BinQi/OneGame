package com.sungy.onegame.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.sungy.onegame.R;

import com.sungy.onegame.activity.FavoritesFragment.FavoriteGame;
import com.sungy.onegame.mclass.DownLoadUtils;
import com.sungy.onegame.mclass.FileUtil;

public class MyAdapter extends BaseAdapter{
    private ArrayList<FavoriteGame> list;
    private static HashMap<Integer,Boolean> isSelected;
    private Context context;
    private LayoutInflater inflater = null;
    private int count = 0;
    private Bitmap bitMap;
    private Boolean ifcontinue;
    
    public MyAdapter(ArrayList<FavoriteGame> list, Context context) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();
        
        initData();
    }

    public void initData(){
    	isSelected.clear();
        for(int i=0; i<list.size();i++) {
            getIsSelected().put(i,false);
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	//Log.e("TAG","MyAdapter getViewwwwwwwwwwwwwwwwww");
        ViewHolder holder = null;
        if (convertView == null) {
         
            holder = new ViewHolder();
        
            convertView = inflater.inflate(R.layout.favorites_list_item, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.favorites_list_image);
            holder.cb = (CheckBox) convertView.findViewById(R.id.favorites_checkbox);
            holder.index = count;
            initData();
            holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            	@Override
            	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            		View v = (View)buttonView.getParent();
            		ViewHolder h = (ViewHolder)v.getTag();
            		FavoritesFragment.handl_visible.sendEmptyMessage(1);
            		Log.e("MyAdapter", "aaaaaaaaa");            		
            		isSelected.put(h.index, h.cb.isChecked());
            		Log.d("MyAdapter", "item"+((Integer)h.index).toString()+" is click");
            		ArrayList<Integer> alist = getAllSelected();
                    for(int i : alist)
                    	System.out.println(i);
            	}
            });
            count++;
            convertView.setTag(holder);
        } else {
          
            holder = (ViewHolder) convertView.getTag();
        }

        ifcontinue = false;
        final int index = position;
        new Thread()
        {
        	@Override
			public void run()
        	{
        		String url = null;
        		url = list.get(index).url;
        		if(url != null)
        			bitMap = downloadImage(url, context);
        		ifcontinue = true;
        	}
        }.start();
        while(!ifcontinue){}
        if(bitMap != null)
        	holder.iv.setImageBitmap(bitMap);
        else
        	holder.iv.setImageResource(R.drawable.defaultno);
        bitMap = null;
        ifcontinue = false;
   
        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }
    
    public Bitmap downloadImage(String url,Context context) {
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
            	String path = FileUtil.setMkdir(context)+File.separator+url.substring(url.lastIndexOf("/")+1);
                File f = new File(path);
                if(!f.exists()) {
                	Log.e("XXX", "downing image");
	            	FileOutputStream fos = new FileOutputStream(path); 
	                byte[] bytes = new byte[1024]; 
	                int len = -1; 
	                while((len = is.read(bytes))!=-1) 
	                { 
	                    fos.write(bytes, 0, len); 
	                } 
	                fos.close(); 
                }
                if(path.endsWith(".jpg")||path.endsWith(".png")||path.endsWith(".jpeg")){ 
                    FileInputStream fis = new FileInputStream(path); 
                    return BitmapFactory.decodeStream(fis);
                }  
                is.close();     
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
		return null;  
	}
    
    public Bitmap returnBitMap(String url) { 
    	//Log.e("TAG", "URL: "+url);
    	URL myFileUrl = null; 
    	Bitmap bitmap = null; 
    	try { 
    		myFileUrl = new URL(url); 
    	} catch (MalformedURLException e) { 
    		Log.e("TAG", "returnBitMap URL error");
    		e.printStackTrace(); 
    	} 
    	try { 
    		if(myFileUrl == null){
    			//Log.e("TAG", "nullllllllllllllllllllllllllllll");
    			return null;
    		}
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
    
    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }
    
    public ArrayList<Integer> getAllSelected() {
    	ArrayList<Integer> res = new ArrayList<Integer>();
    	for(int i = 0; i<isSelected.size(); i++)
    		if(isSelected.get(i))
    			res.add(i);
    	return res;
    }
    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
        MyAdapter.isSelected = isSelected;
    }

}
