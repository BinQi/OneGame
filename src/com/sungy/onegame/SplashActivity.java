package com.sungy.onegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {  
	  
    private final int SPLASH_DISPLAY_LENGHT = 6000; // 延迟六秒  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.splash);  
  
        new Handler().postDelayed(new Runnable() {  
            public void run() {   
                SplashActivity.this.finish();  
            }  
  
        }, SPLASH_DISPLAY_LENGHT);  
  
        Intent mainIntent = new Intent(SplashActivity.this,  
                MainActivity.class);  
        SplashActivity.this.startActivity(mainIntent); 
    }  
}  
