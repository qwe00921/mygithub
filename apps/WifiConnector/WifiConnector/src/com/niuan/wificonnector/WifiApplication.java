package com.niuan.wificonnector;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class WifiApplication extends Application {
	private static WifiApplication INSTANCE;

	public static WifiApplication getInstance() {
		return INSTANCE;
	}

	@Override
	public void onCreate() {
		INSTANCE = this;
		SDKInitializer.initialize(this);
		Preference.getInstance().init(this);
		super.onCreate();
	}
}
