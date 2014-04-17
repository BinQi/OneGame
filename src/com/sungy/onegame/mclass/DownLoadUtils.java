package com.sungy.onegame.mclass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;

public class DownLoadUtils {

	//从网络加载图片
	public static Bitmap downloadImage(String url,Context context) {
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
                FileOutputStream fos = new FileOutputStream(getPath(url,context)); 
                byte[] bytes = new byte[1024]; 
                int len = -1; 
                while((len = is.read(bytes))!=-1) 
                { 
                    fos.write(bytes, 0, len); 
                } 
                if(getPath(url,context).endsWith(".jpg")||getPath(url,context).endsWith(".png")||getPath(url,context).endsWith(".jpeg")){ 
                    FileInputStream fis = new FileInputStream(getPath(url,context)); 
                    return BitmapFactory.decodeStream(fis);
                }  
                is.close(); 
                fos.close(); 
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
		return null;  
	}
	
	private static String getPath(String str,Context context) throws IOException 
    { 
        String path = FileUtil.setMkdir(context)+File.separator+str.substring(str.lastIndexOf("/")+1); 
        return path; 
    } 
	
	//从网络加载图片
	public static Bitmap downloadIcon(String url) {
		try {
			URL mUrl = new URL(url);
			HttpURLConnection mConnection = (HttpURLConnection) mUrl
					.openConnection();
			InputStream mStream = mConnection.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(mStream);
			return bitmap;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//加载文件并保存手机中
	public static String downloadFile(String url, Context context) {
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
            	String s = getPath(url, context);
            	FileOutputStream fos = new FileOutputStream(s); 
                byte[] bytes = new byte[1024]; 
                int len = -1; 
                while((len = is.read(bytes))!=-1) 
                { 
                    fos.write(bytes, 0, len); 
                } 
                
                is.close(); 
                fos.close(); 
                return s;
            } 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }  
		return "";
	}
}
