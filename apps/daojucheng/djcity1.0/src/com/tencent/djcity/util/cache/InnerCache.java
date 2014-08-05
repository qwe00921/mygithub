package com.tencent.djcity.util.cache;

import java.io.File;

import com.tencent.djcity.util.Config;

import android.content.Context;

public class InnerCache extends FileStorage {

	private String mRoot;

	public InnerCache(Context context) {
		mRoot = context.getCacheDir() + "/" + Config.TMPDIRNAME;

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
