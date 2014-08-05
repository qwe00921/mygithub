package com.tencent.djcity.util.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;

/*
 * è¯¥æ?½è±¡ç±»ä¸»è¦????ä¾?äº?å¯¹æ?°æ??åº????å¸¸è?????CRUD???ä¸?äº????ä½?ï¼?å¹¶ä??è¯¥æ?½è±¡ç±»å¹¶æ²¡æ??SQLiteDatabase???å¯¹è±¡ï¼?è¯¥å?¹è±¡???è¦?å­?ç±»æ??ä¾????è¯¥æ?½è±¡ç±»æ??ä¾????ä¸?ä¸???½è±¡??¹æ??init(),
 * ???ä»¥ç?¨æ?¥è??è¡?ä¸?äº????å§???????å·¥ä??ï¼???????è¦???????è¦????ä¾?SQLiteDatabaseå¯¹è±¡???
 * */
public abstract class Database {
	
	public int errCode = 0;
	public String errMsg = "";
	
	private static final String LOG_TAG = Database.class.getName();
	
	protected volatile SQLiteDatabase core = null;

	public  abstract void init();
	
	public  void close(){
		if( null == core ) return;
		try{
			if( core.isOpen() ){
				core.close();
			}
		}
		catch(Exception ex){
			errCode = 1001;
			errMsg = LOG_TAG + "|close|" + ToolUtil.getStackTraceString(ex);
			Log.e(LOG_TAG, errMsg);
		}
		
		core = null;
	}
	
	public void clearError(){
		errCode = 0;
		errMsg = "";
	}
	
	public Cursor getCursor(String sql, String[] params){
		
		init();
		
		Cursor cursor = null;
		
		try{
			cursor = core.rawQuery(sql, params);
		}
		catch(Exception ex){
			errCode = 1002;
			errMsg = LOG_TAG + "|query|" + ToolUtil.getStackTraceString(ex);
			cursor = null;
			Log.e(LOG_TAG, errMsg);
		}
			
		return cursor;
		
	}
	
	public  HashMap<String, String> getOneRow(String sql, String[] params){
		
		clearError();
		
		Cursor cursor = getCursor(sql, params);
		
		if( null == cursor )	return null;
		
		HashMap<String, String> item = null;
		
		try{
			if(cursor.moveToNext()){
				item = new HashMap<String, String>();

				for(int i = 0, len = cursor.getColumnCount(); i < len; i++){
					item.put( cursor.getColumnName(i).toLowerCase(Locale.getDefault()), cursor.getString(i) );
				}
			}
			
			cursor.close();
		}
		catch(Exception ex){
			errCode = 1003;
			errMsg = LOG_TAG + "|query|" + ToolUtil.getStackTraceString(ex);
			Log.e(LOG_TAG, errMsg);
			if( null != cursor ) cursor.close();
		}
		
		return item;
	}
	
	public String getValue(String sql, String[] params){
		
		clearError();
		
		String result = null;
		
		Cursor cursor = getCursor(sql, params);
		
		if( null == cursor )	return null;
		
		try{
			if(cursor.moveToNext()){
				result = cursor.getString(0);
			}
			
			cursor.close();
		}
		catch(Exception ex){
			errCode = 1004;
			errMsg = LOG_TAG + "|getValue|" + ToolUtil.getStackTraceString(ex);
			Log.e(LOG_TAG, errMsg);
			if( null != cursor ) cursor.close();
		}
		
		return result;
	}
	
	public ArrayList< HashMap<String, String> >query(String sql, String[] params){
		
		clearError();
		
		Cursor cursor = getCursor(sql, params);
		
		if( null == cursor )	return null;
		
		ArrayList< HashMap<String, String> > result = new ArrayList< HashMap<String, String> >(); 	
		
		try{
			while(cursor.moveToNext()){
				HashMap<String, String> item = new HashMap<String, String>();

				for(int i = 0, len = cursor.getColumnCount(); i < len; i++){
					item.put( cursor.getColumnName(i).toLowerCase(Locale.getDefault()), cursor.getString(i) );
				}
				
				result.add( item );
			}
			
			cursor.close();
		}
		catch(Exception ex){
			errCode = 1005;
			errMsg = LOG_TAG + "|query|" + ToolUtil.getStackTraceString(ex);
			Log.e(LOG_TAG, errMsg);
			if( null != cursor ) cursor.close();
			
			result = null;
		}
		
		return result;
	}
	
	/**
	 * è¯¥å?½æ?°ä¸»è¦??????§è??ä¸???¡é????¥è?¢ç±»???sqlè¯???¥ï??å¦????è¯???¥è?½å??æ­£å¸¸??§è????£ä??å°±è?????trueï¼???????è¿????false
	 * @param sqlï¼?å¾???§è?????sqlè¯????
	 * @param paramsï¼?sqlè¯???¥ç???????°å??
	 * @return
	 */
	public boolean execute(String sql, Object[] params){
		
		init();
		
		clearError();
		
		boolean result = false;
	
		try{
			if( null == params ){
				core.execSQL(sql);
			}
			else{
				core.execSQL(sql, params);
			}
			
			result = true;
		}
		catch(Exception ex){
			errCode = 1006;
			errMsg = LOG_TAG + "|execute|" + ToolUtil.getStackTraceString(ex);
			Log.e(LOG_TAG, errMsg);
		}
		
		return result;
	}
	
	public long replace( String table, ContentValues values ){
		init();
		clearError();
		return core.replace(table, null, values);
	}
	
	public long insert( String table, ContentValues values ){
		init();
		clearError();
		return core.insert(table, null, values);
	}
	
	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		init();
		clearError();
		return core.update(table, values, whereClause, whereArgs);
	}
	
	public int update(String table, ContentValues values, String whereClause, String whereArg) {
		init();
		clearError();		
		return core.update(table, values, whereClause, new String[]{ whereArg } );
	}
	
	public int update(String table, ContentValues values, String whereClause, int whereArg) {
		init();
		clearError();
		return core.update(table, values, whereClause, new String[]{ String.valueOf( whereArg ) } );
	}
	
	public int update(String table, ContentValues values, String whereClause) {
		init();
		clearError();
		String [] param = null;
		return core.update(table, values, whereClause, param);
	}
	
	public SQLiteDatabase getCore(){
		return core;
	}
	
	public String getValue(String sql, String param){
		return getValue(sql, new String[]{param});
	}
	
	public  HashMap<String, String> getOneRow(String sql, String param){
		return getOneRow(sql, new String[]{param});
	}
	
	public ArrayList< HashMap<String, String> >query(String sql, String param){
		return query(sql, new String[]{param} );
	}
	
	
	public ArrayList< HashMap<String, String> >query(String sql, int param){
		return query(sql, new String[]{ String.valueOf(param) } );
	}
	
	public HashMap<String, String> getOneRow(String sql, int param){
		return getOneRow(sql, new String[]{ String.valueOf(param) } );
	}		
	
	public String getValue(String sql){
		String[] param = null;
		return getValue(sql, param);
	}
	
	public  HashMap<String, String> getOneRow(String sql){
		return getOneRow(sql,  new String[]{} );
	}
	
	public ArrayList< HashMap<String, String> >query(String sql){
		String[] param = null;
		return query(sql, param);
	}
	
	public boolean execute(String sql){
		return execute(sql, null);
	}
	
	public boolean execute(String sql, Object param){
		Object[] obj = {param};

		return execute(sql, obj);
	}
}
