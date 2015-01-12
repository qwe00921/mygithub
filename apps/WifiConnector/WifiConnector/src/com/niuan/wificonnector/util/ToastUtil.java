package com.niuan.wificonnector.util;

import com.niuan.wificonnector.WifiApplication;

import android.widget.Toast;

public class ToastUtil {

	public static void makeToast(String msg) {
		Toast.makeText(WifiApplication.getInstance(), msg, Toast.LENGTH_SHORT)
				.show();
	}

	public static void makeToast(int msg) {
		Toast.makeText(WifiApplication.getInstance(), msg, Toast.LENGTH_SHORT)
				.show();
	}
}
