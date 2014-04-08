package com.sungy.onegame.mclass;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {
	
	//正中间的toast
	public static void showCenterToast(Context context,String message,int longtime){
	   Toast toast = Toast.makeText(context,
				message, longtime);
	   toast.setGravity(Gravity.CENTER, 0, 0);
	   toast.show();
	}

	
	//默认的toast
	public static void showDefaultToast(Context context,String message,int longtime){
		Toast.makeText(context, message,longtime).show();
	}
}
