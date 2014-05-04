package com.sungy.onegame.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.sungy.onegame.R;

public class AboutUsActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_us);
	}
}
