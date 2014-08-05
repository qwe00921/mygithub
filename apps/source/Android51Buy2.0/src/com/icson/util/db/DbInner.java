package com.icson.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ToolUtil;

public class DbInner extends Database {

	@Override
	public void init() {
		if (core == null || !core.isOpen()) {
			InnerHelper mHelper = new InnerHelper(IcsonApplication.app);
			try{
				core = mHelper.getWritableDatabase();
			}catch(SQLiteException ex)
			{
				Log.e("dbinner", "getWritableDatabase" + ToolUtil.getStackTraceString(ex));	
			}
		}
	}

	private class InnerHelper extends SQLiteOpenHelper {
		private static final String LOG_TAG = "InnerHelper";

		public InnerHelper(Context context) {
			super(context, Config.INNER_DATABASE_NAME, null, Config.INNER_DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				db.execSQL("create table if not exists t_page_cache(id varchar(50) primary key, content TEXT, row_create_time INTEGER, row_expire_time INTEGER)");
			} catch (Exception ex) {
				Log.e(LOG_TAG, "onCreate|page_cache|" + ToolUtil.getStackTraceString(ex));
				;
			}

			try {
				db.execSQL("create table if not exists t_login(uid INTEGER primary key, skey varchar(30), token varchar(30),  nick_name varchar(30), row_create_time INTEGER)");
			} catch (Exception ex) {
				Log.e(LOG_TAG, "onCreate|t_login|" + ToolUtil.getStackTraceString(ex));
				;
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

	/*		try {
				db.execSQL("DROP TABLE IF EXISTS page_cache");
			} catch (Exception ex) {
				Log.e(LOG_TAG, "onUpgrade|page_cache|" + ToolUtil.getStackTraceString(ex));
				;
			}
*/
			
			if( arg1 == 1  ){
				try {
					db.execSQL("DROP TABLE IF EXISTS t_login");
				} catch (Exception ex) {
					Log.e(LOG_TAG, "onUpgrade|t_login|" + ToolUtil.getStackTraceString(ex));
					;
				}
	
				onCreate(db);
			}
		}
	}
}
