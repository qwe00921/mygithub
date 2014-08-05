package com.icson.util.cache;

import java.io.File;

import android.os.Environment;

import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;

public class SDCache extends FileStorage {

	private final String LOG_TAG = SDCache.class.getName();

	private String mRoot;

	/**
	 * 该构造函数首先检查了sd card是否存在，如果不存在抛出一个非检查性异常。如果存在，那么在sd card的目录下创建一个目录Config.TMPDIRNAME。
	 */
	public SDCache() {
		if (!ToolUtil.isSDExists()) {
			RuntimeException ex = new RuntimeException("sd card is not exists.");
			Log.e(LOG_TAG, ex);
			throw ex;
		}

		mRoot = Environment.getExternalStorageDirectory() + "/" + Config.TMPDIRNAME;

		File file = new File(mRoot);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	@Override
	public String getRootPath() {
		return mRoot + "/";
	}
}
