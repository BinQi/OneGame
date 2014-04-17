package com.sungy.onegame.onegameprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class OneGameProvider extends ContentProvider {

	private OneGameHelper dbHelper;
	private SQLiteDatabase contactsDB;
	
	public static final String AUTHORITY = "com.sungy.onegame.provider.OneGameProvider";
	public static final String RECORDS_TABLE = "games";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+RECORDS_TABLE);
	public static final int RECORDS = 1;
	public static final int RECORD_ID = 2;
	private static final UriMatcher uriMatcher;	
	
	static
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY,"games",RECORDS);
		uriMatcher.addURI(AUTHORITY,"games/#",RECORD_ID);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] selectionArgs)
	{
		//ִ�д�����ݿ�
		contactsDB = dbHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri))
		{
			case RECORDS:
				count = contactsDB.delete(RECORDS_TABLE, where, selectionArgs);
				break;
			case RECORD_ID:
				String contactID = uri.getPathSegments().get(1);
				count = contactsDB.delete(RECORDS_TABLE, 
										  OneGameColumn._ID 
										  + "=" + contactID 
										  + (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""),
										  selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri))
		{
			case RECORDS:
				return "vnd.android.cursor.dir/vnd.tron.android.onegamerecord";
			case RECORD_ID:
				return "vnd.android.cursor.item/vnd.tron.android.onegamerecord";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues)
	{
		if (uriMatcher.match(uri) != RECORDS)
		{
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if (initialValues != null)
		{
			values = new ContentValues(initialValues);
		}
		else
		{
			values = new ContentValues();
		}
		// ����Ĭ��ֵ
		if (values.containsKey(OneGameColumn.ONEGAMEID) == false)
		{
			values.put(OneGameColumn.ONEGAMEID, 0);
		}
		if (values.containsKey(OneGameColumn.ONEGAMENAME) == false)
		{
			values.put(OneGameColumn.ONEGAMENAME, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEPUBLISHTIME) == false)
		{
			values.put(OneGameColumn.ONEGAMEPUBLISHTIME, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEORIGINALIMAGE) == false)
		{
			values.put(OneGameColumn.ONEGAMEORIGINALIMAGE, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEIMAGE) == false)
		{
			values.put(OneGameColumn.ONEGAMEIMAGE, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEINTRODUCTION) == false)
		{
			values.put(OneGameColumn.ONEGAMEINTRODUCTION, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEDETAIL) == false)
		{
			values.put(OneGameColumn.ONEGAMEDETAIL, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEDETAILIMAGE) == false)
		{
			values.put(OneGameColumn.ONEGAMEDETAILIMAGE, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEDETAILORIGINALIMAGE) == false)
		{
			values.put(OneGameColumn.ONEGAMEDETAILORIGINALIMAGE, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMECOMMENTNUM) == false)
		{
			values.put(OneGameColumn.ONEGAMECOMMENTNUM, 0);
		}
		if (values.containsKey(OneGameColumn.ONEGAMEPRAISENUM) == false)
		{
			values.put(OneGameColumn.ONEGAMEPRAISENUM, 0);
		}
		if (values.containsKey(OneGameColumn.ONEGAMEDOWNLOADURL) == false)
		{
			values.put(OneGameColumn.ONEGAMEDOWNLOADURL, "");
		}
		if (values.containsKey(OneGameColumn.ONEGAMEISDELETE) == false)
		{
			values.put(OneGameColumn.ONEGAMEISDELETE, 0);
		}
		if (values.containsKey(OneGameColumn.ONEGAMECOLLECTNUM) == false)
		{
			values.put(OneGameColumn.ONEGAMECOLLECTNUM, 0);
		}
		if (values.containsKey(OneGameColumn.ONEGAMESHARENUM) == false)
		{
			values.put(OneGameColumn.ONEGAMESHARENUM, 0);
		}
		//ִ�д�����ݿ�
		contactsDB = dbHelper.getWritableDatabase();
		long rowId = contactsDB.insert(RECORDS_TABLE, null, values);
		if (rowId > 0)
		{
			Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);//����
			return noteUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		dbHelper = new OneGameHelper(getContext());
		//ִ�д�����ݿ�
		contactsDB = dbHelper.getWritableDatabase();
		return (contactsDB == null) ? false : true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();//����SQL��ѯ���ĸ�����
		qb.setTables(RECORDS_TABLE);

		switch (uriMatcher.match(uri))
		{
			case RECORD_ID:
				qb.appendWhere(OneGameColumn._ID + "=" + uri.getPathSegments().get(1));
				break;
			default:
				break;
		}
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
		{
			orderBy = OneGameColumn._ID;
		}
		else
		{
			orderBy = sortOrder;
		}
		//ִ�д�����ݿ�
		contactsDB = dbHelper.getReadableDatabase();
		Cursor c = qb.query(contactsDB, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] selectionArgs)
	{
		contactsDB = dbHelper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri))
		{
			case RECORDS:
				count = contactsDB.update(RECORDS_TABLE, values, where, selectionArgs);
				break;
			case RECORD_ID:
				String contactID = uri.getPathSegments().get(1);
				count = contactsDB.update(RECORDS_TABLE, values, OneGameColumn._ID + "=" + contactID
						+ (!TextUtils.isEmpty(where) ? " AND (" + where + ")" : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
