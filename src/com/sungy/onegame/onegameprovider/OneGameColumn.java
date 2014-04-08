package com.sungy.onegame.onegameprovider;

import android.provider.BaseColumns;

public class OneGameColumn implements BaseColumns
{
	public OneGameColumn()
	{
	}
	
	public static final String ONEGAMEID = "OneGameId";				
	public static final String ONEGAMENAME = "OneGameName";              
	public static final String ONEGAMEPUBLISHTIME = "OneGamePublishTime";	            
	public static final String ONEGAMEORIGINALIMAGE = "OneGameOriginalImage";		            
	public static final String ONEGAMEIMAGE = "OneGameImage";	    
	public static final String ONEGAMEINTRODUCTION = "OneGameIntroduction";   
	public static final String ONEGAMEDETAIL = "OneGameDetail";
	public static final String ONEGAMEDETAILIMAGE = "OneGameDetailImage";
	public static final String ONEGAMEDETAILORIGINALIMAGE = "OneGameDetailOriginalImage";
	public static final String ONEGAMECOMMENTNUM = "OneGameCommentNo";
	public static final String ONEGAMEPRAISENUM = "OneGamePraiseNo";
	public static final String ONEGAMEDOWNLOADURL = "OneGameDownloadUrl";
	public static final String ONEGAMEISDELETE = "OneGameIsDelete";
	public static final String ONEGAMECOLLECTNUM = "OneGameCollectNo";
	public static final String ONEGAMESHARENUM = "OneGameShareNo";
	//�� ����ֵ
	public static final int _ID_COLUMN = 0;
	public static final int ONEGAMEID_COLUMN = 1;
	public static final int ONEGAMENAME_COLUMN = 2;
	public static final int ONEGAMEPUBLISHTIME_COLUMN = 3;
	public static final int ONEGAMEORIGINALIMAGE_COLUMN = 4;
	public static final int ONEGAMEIMAGE_COLUMN = 5;
	public static final int ONEGAMEINTRODUCTION_COLUMN = 6;
	public static final int ONEGAMEDETAIL_COLUMN = 7;
	public static final int ONEGAMEDETAILIMAGE_COLUMN = 8;
	public static final int ONEGAMEDETAILORIGINALIMAGE_COLUMN = 9;
	public static final int ONEGAMECOMMENTNUM_COLUMN = 10;
	public static final int ONEGAMEPRAISENUM_COLUMN = 11;
	public static final int ONEGAMEDOWNLOADURL_COLUMN = 12;
	public static final int ONEGAMEISDELETE_COLUMN = 13;
	public static final int ONEGAMECOLLECTNUM_COLUMN = 14;
	public static final int ONEGAMESHARENUM_COLUMN = 15;

	//��ѯ���
	public static final String[] PROJECTION ={
		_ID,
		ONEGAMEID,
		ONEGAMENAME,
		ONEGAMEPUBLISHTIME,
		ONEGAMEORIGINALIMAGE,
		ONEGAMEIMAGE,
		ONEGAMEINTRODUCTION,
		ONEGAMEDETAIL,
		ONEGAMEDETAILIMAGE,
		ONEGAMEDETAILORIGINALIMAGE,
		ONEGAMECOMMENTNUM,
		ONEGAMEPRAISENUM,
		ONEGAMEDOWNLOADURL,
		ONEGAMEISDELETE,
		ONEGAMECOLLECTNUM,
		ONEGAMESHARENUM,
	};
}
