package com.sungy.onegame.onegameprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OneGameHelper extends SQLiteOpenHelper
{
	public static final String DATABASE_NAME = "onegame.db";//��ݿ���
	public static final int DATABASE_VERSION = 2;				 //�汾
	public static final String RECORDS_TABLE = "games";	 //����
	//������
	private static final String DATABASE_CREATE = 
		"CREATE TABLE " + RECORDS_TABLE +" ("					
		+ OneGameColumn._ID+" integer primary key autoincrement,"
		+ OneGameColumn.ONEGAMEID+" integer,"
		+ OneGameColumn.ONEGAMENAME+" text,"
		+ OneGameColumn.ONEGAMEPUBLISHTIME+" text,"
		+ OneGameColumn.ONEGAMEORIGINALIMAGE+" text,"
		+ OneGameColumn.ONEGAMEIMAGE+" text,"
		+ OneGameColumn.ONEGAMEINTRODUCTION+" text,"
		+ OneGameColumn.ONEGAMEDETAIL+" text,"
		+ OneGameColumn.ONEGAMEDETAILIMAGE+" text,"
		+ OneGameColumn.ONEGAMEDETAILORIGINALIMAGE+" text,"
		+ OneGameColumn.ONEGAMECOMMENTNUM+" integer,"
		+ OneGameColumn.ONEGAMEPRAISENUM+" integer,"
		+ OneGameColumn.ONEGAMEDOWNLOADURL+" text,"
		+ OneGameColumn.ONEGAMEISDELETE+" integer,"
		+ OneGameColumn.ONEGAMECOLLECTNUM+" integer,"
		+ OneGameColumn.ONEGAMESHARENUM+" integer);";
	public OneGameHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(DATABASE_CREATE);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE);
		onCreate(db);
	}
}
