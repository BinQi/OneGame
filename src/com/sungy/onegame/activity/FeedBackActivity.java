package com.sungy.onegame.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sungy.onegame.R;
import com.sungy.onegame.mclass.Global;
import com.sungy.onegame.mclass.HttpUtils;

public class FeedBackActivity extends Activity{
	private static final String[] problemTypes = {"类型1","类型2","类型3","类型4","类型5","类型6"};
	private Spinner spinner;
	private EditText problem, advice;
	private Button post;
	private ImageView back;
	private String pType;
	private String userid;
	private String username;
	private String str;
	private static final String TAG = "FeedBackActivity";	
	
	private ArrayAdapter<String> adapter;
	private NameValuePair pair0, pair1, pair2, pair3, pair4;
	private List<NameValuePair> data = new ArrayList<NameValuePair>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.activity_feed_back);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title); 
			
		spinner = (Spinner)findViewById(R.id.Spinner);
		spinner.setPrompt("请选择");  
		problem = (EditText)findViewById(R.id.problem);
		advice = (EditText)findViewById(R.id.advice);
		post = (Button)findViewById(R.id.post);
		back = (ImageView)findViewById(R.id.back);
		
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		post.setOnClickListener(new View.OnClickListener(){			
            @Override  
            public void onClick(View v) {  
                //Toast.makeText(getApplicationContext(), "谢谢你的反馈", Toast.LENGTH_SHORT).show();
            	String problemContent = problem.getText().toString();
            	String adviceContent = advice.getText().toString();
            	if(0 == problemContent.length() || 0 == adviceContent.length())
            	{
            		View toastRoot = getLayoutInflater().inflate(R.layout.my_toast, null);
            		TextView tv = (TextView)toastRoot.findViewById(R.id.toast_text);
            		tv.setText("请填写完整");
            		RelativeLayout rl = (RelativeLayout)toastRoot.findViewById(R.id.toast_layout);
            		rl.getBackground().setAlpha(50);
	            	Toast toast = new Toast(getApplicationContext());
	            	toast.setView(toastRoot);
	            	toast.setGravity(Gravity.CENTER, 0, 0);
	            	toast.show();
            	}
            	else
            	{
            		sendData(pType, problemContent, adviceContent);
            		while(null == str){}
            		if(str != "error" && str != "exception")
            		{
            			str = null;
		            	View toastRoot = getLayoutInflater().inflate(R.layout.my_toast, null);
		            	Toast toast = new Toast(getApplicationContext());
		            	RelativeLayout rl = (RelativeLayout)toastRoot.findViewById(R.id.toast_layout);
                		rl.getBackground().setAlpha(50);
		            	toast.setView(toastRoot);
		            	toast.setGravity(Gravity.CENTER, 0, 0);
		            	toast.show();
		            	finish();
            		}
            		else{
            			View toastRoot = getLayoutInflater().inflate(R.layout.my_toast, null);
                		TextView tv = (TextView)toastRoot.findViewById(R.id.toast_text);
                		tv.setText("发送失败!");
                		RelativeLayout rl = (RelativeLayout)toastRoot.findViewById(R.id.toast_layout);
                		rl.getBackground().setAlpha(50);
    	            	Toast toast = new Toast(getApplicationContext());
    	            	toast.setView(toastRoot);
    	            	toast.setGravity(Gravity.CENTER, 0, 0);
    	            	toast.show();
            		}
            	}
            }  
		});
		//将可选内容与ArrayAdapter连接起来  
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, problemTypes);  
          
        //设置下拉列表的风格  
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
        
        
        //将adapter 添加到spinner中  
        spinner.setAdapter(adapter);  
          
        //添加事件Spinner事件监听    
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());  
        
        //设置默认值  
        spinner.setVisibility(View.VISIBLE);  
	}
	
	//发送请求
	private void sendData(String type, String pro, String adv)
	{
		userid = Global.getUserId();
		username = Global.getUserNmae();
		pair0 = new BasicNameValuePair("userid", userid);
		pair1 = new BasicNameValuePair("username", username);
		pair2 = new BasicNameValuePair("feedback_type", type);
		pair3 = new BasicNameValuePair("feedback_info", pro);
		pair4 = new BasicNameValuePair("feedback_suggestion", adv);
    	data.add(pair0);
    	data.add(pair1);
    	data.add(pair2);
    	data.add(pair3);
    	data.add(pair4);
    	new Thread()
    	{
    		@Override
    		public void run()
    		{
    			str = HttpUtils.doPost(Global.FEEDBACK_FEEDBACK, data);
    		}
    	}.start();
	}
	
	//使用数组形式操作  
    class SpinnerSelectedListener implements OnItemSelectedListener{  
  
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,  
                long arg3) {
        	pType = problemTypes[arg2];
        }  
  
        public void onNothingSelected(AdapterView<?> arg0) {  
        }  
    }  
}
