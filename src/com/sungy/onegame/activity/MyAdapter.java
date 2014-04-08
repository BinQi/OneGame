package com.sungy.onegame.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.sungy.onegame.R;

public class MyAdapter extends BaseAdapter{
    private ArrayList<String> list;
    private static HashMap<Integer,Boolean> isSelected;
    private Context context;
    private LayoutInflater inflater = null;
    
    private Bitmap bitMap;
    
    class ViewHolder{
    	CheckBox cb;
    	ImageView iv;
    }
    public MyAdapter(ArrayList<String> list, Context context) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<Integer, Boolean>();

        initData();
    }

    public void initData(){
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
        ViewHolder holder = null;
        if (convertView == null) {
         
            holder = new ViewHolder();
        
            convertView = inflater.inflate(R.layout.favorites_list_item, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.favorites_list_image);
            holder.cb = (CheckBox) convertView.findViewById(R.id.favorites_checkbox);
          
            convertView.setTag(holder);
        } else {
          
            holder = (ViewHolder) convertView.getTag();
        }

      
        final int index = position;
        new Thread()
        {
        	@Override
			public void run()
        	{
        		bitMap = returnBitMap(list.get(index));
        	}
        }.start();
        while(null == bitMap){}
        holder.iv.setImageBitmap(bitMap);
        bitMap = null;

        holder.cb.setChecked(getIsSelected().get(position));
        return convertView;
    }
    
    public Bitmap returnBitMap(String url) { 
    	URL myFileUrl = null; 
    	Bitmap bitmap = null; 
    	try { 
    		myFileUrl = new URL(url); 
    	} catch (MalformedURLException e) { 
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
