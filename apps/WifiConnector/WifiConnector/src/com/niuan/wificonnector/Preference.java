package com.niuan.wificonnector;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
	// Preference
	public static final String PREF_NAME = "gamenews_pref";

	private SharedPreferences mPref;

	/**
	 * mCacheMap 把preference的数据保存到内存中
	 */
	private Map<String, Object> mCacheMap = new HashMap<String, Object>();
	private static final String LOG_TAG = Preference.class.getSimpleName();
	private static Preference mInstance = new Preference();
	private boolean isInited;

	public void init(Context context) {
		if (mPref == null) {
			mPref = context.getSharedPreferences(PREF_NAME,
					Context.MODE_PRIVATE);
		}
		isInited = true;
	}

	public boolean isInited() {
		return isInited;
	}

	public static Preference getInstance() {
		return mInstance;
	}

	public SharedPreferences getPreference() {

		return mPref;
	}

	private static final String KEY_USER = "user";

	public void saveUser(String user) {
		mPref.edit().putString(KEY_USER, user).commit();
	}

	public String getUser() {
		return mPref.getString(KEY_USER, "");
	}
}
