package com.sungy.onegame.mclass;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;

public class HttpUtils {
	
	public static String encoding = HTTP.UTF_8; //锟斤拷锟斤拷锟斤拷锟斤拷锟街凤拷默锟斤拷锟斤拷utf8

	/**
	 * 锟斤拷锟教拷锟斤拷锟絧ost锟斤拷锟斤拷
	 * @param url 	锟斤拷锟斤拷牡锟街�
	 * @param params  锟斤拷锟斤拷锟絧ost锟斤拷锟斤拷
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String doPost(String url , List<NameValuePair> params){
		HttpPost httpRequest = null; 
		HttpResponse httpResponse = null;
		String strResult = null;
		/*锟斤拷锟斤拷HttpPost锟斤拷锟斤拷*/ 
		httpRequest=new HttpPost(url);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		
		  try { 
	            //锟斤拷锟斤拷HTTP request 
	            httpRequest.setEntity(new UrlEncodedFormEntity(params,encoding)); 
	            //取锟斤拷HTTP response 
	            httpResponse=new DefaultHttpClient().execute(httpRequest); 
	            //锟斤拷状态锟斤拷为200,锟斤拷锟斤拷
	            if(httpResponse.getStatusLine().getStatusCode()==200){ 
	                //取锟斤拷锟斤拷应锟街达拷 
	                strResult = EntityUtils.toString(httpResponse.getEntity()); 
	            }else{ 
	            	strResult = "error";
	            } 
	        } catch (Exception e) { 
	        	strResult = "exception";
	        	e.printStackTrace();
	        } 
	 return strResult;
	}
	
	/**
	 * 用新线程post请求数据
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doPostWithoutStrict (String url , List<NameValuePair> params){
		HttpPost httpRequest = null; 
		HttpResponse httpResponse = null;
		String strResult = null;
		httpRequest=new HttpPost(url);
		
		  try { 
	            //锟斤拷锟斤拷HTTP request 
	            httpRequest.setEntity(new UrlEncodedFormEntity(params,encoding)); 
	            //取锟斤拷HTTP response 
	            httpResponse=new DefaultHttpClient().execute(httpRequest); 
	            //锟斤拷状态锟斤拷为200,锟斤拷锟斤拷
	            if(httpResponse.getStatusLine().getStatusCode()==200){ 
	                //取锟斤拷锟斤拷应锟街达拷 
	                strResult = EntityUtils.toString(httpResponse.getEntity()); 
	            }else{ 
	            	strResult = "error";
	            } 
	        } catch (Exception e) { 
	        	strResult = "exception";
	        	e.printStackTrace();
	        } 
	 return strResult;
	}
	
	
	// 判断网络是否可用
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}

