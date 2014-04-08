package com.sungy.onegame.mclass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	// 计算日期差
	public static long getQuot(String time1, String time2) {
		long quot = 0;
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date1 = ft.parse(time1);
			Date date2 = ft.parse(time2);
			quot = date1.getTime() - date2.getTime();
			quot = quot / 1000 / 60 / 60 / 24;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return quot;
	}
	
	//获取yyyy-mm-dd格式的当前时间
	public static String getDate(){
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        Date dd = new Date();
        return ft.format(dd);
    }
}
