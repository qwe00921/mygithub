package com.icson.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.GzipHelper;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnFinishListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.icson.util.cache.FileStorage;
import com.icson.util.cache.InnerCache;
import com.icson.util.cache.SDCache;
import com.icson.util.cache.StorageFactory;
import com.icson.util.db.Database;
import com.icson.util.db.DbFactory;

public class IcsonService extends Service implements OnSuccessListener<Object>, OnFinishListener {
	private static final String LOG_TAG 		= IcsonService.class.getName();
	private static final int AJAX_FLAG_LOG 		= 1;
	private static final int AJAX_FLAG_LOGIN 	= 3;
	private boolean logAjaxFinish 		= false;
	private boolean loginAjaxFinish 		= false;

	private static Ajax loginAjax;
	private static Ajax logAjax;
	private static File mLogFile;
	private Ajax areaAjax;

	public void stop() {
		if (logAjaxFinish   && loginAjaxFinish) {
			stopSelf();
		}
	}

	@Override
	public void onDestroy() {

		if (logAjax != null) {
			logAjax.abort();
			logAjax = null;
		}

		if (loginAjax != null) {
			loginAjax.abort();
			loginAjax = null;
		}

		if (areaAjax != null) {
			areaAjax.abort();
			areaAjax = null;
		}

		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		ServiceConfig.setContext(this.getApplicationContext());
		try {
			// 验证 skey是否过期，只有通过发送请求来决定是否删除
			checkSkey();

			// 清除sd卡文件缓存
			if (ToolUtil.isSDExists()) {
				clearCacheDir(new SDCache(), "/");
			}
			// 清除手机内置文件缓存
			clearCacheDir(new InnerCache(getApplicationContext()), "/");

			// 清除页面缓存
			clearPageCache();

			// 上传错误日志
			FileStorage cache = StorageFactory.getFileStorage(getApplicationContext());
			mLogFile = cache.getFile(Config.LOG_NAME);
			updateErrorLog(mLogFile);
		} catch( Exception aException ) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(aException));
			
			stopSelf();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {

	}

	// 删除过期缓存图片
	private void clearCacheDir(FileStorage storage, String path) {
		long dirtyTime = ToolUtil.getCurrentTime() - Config.PIC_CACHE_DIR_TIME;

		File file = storage.getFile(path);
		if (file == null)
			return;
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();

		for (int i = 0; i < tempList.length; i++) {

			String end = (path.endsWith(File.separator) ? "" : File.separator) + tempList[i];
			
			File temp = storage.getFile(path + end);

			if (temp.isDirectory()) {
				clearCacheDir(storage, path + end);
				if( temp.list().length == 0){
					temp.delete();
				}
			} else if (temp.isFile()) {
				if (temp.lastModified() < dirtyTime) {
					temp.delete();
				}
			}
		}
	}

	// 全表遍历，删除过期的cache, 及时回收垃圾cache
	private void clearPageCache() {
		long dirtyTime = ToolUtil.getCurrentTime();
		Database db = DbFactory.getInstance();
		if (db.execute("delete from t_page_cache where row_expire_time<>0 and row_expire_time<?", dirtyTime) == false) {
			Log.e(LOG_TAG, "inner|clearPageCache|" + db.errMsg);
		}
	}

	public void checkSkey() {
		final long uid = ILogin.getLoginUid();
		if (uid == 0){
			loginAjaxFinish = true;
			stop();
			return;
		}
		
		ServiceConfig.setContext(this.getApplicationContext());
		loginAjax = ServiceConfig.getAjax(Config.URL_LOGIN_GETSTATUS);
		if( null == loginAjax ) {
			loginAjaxFinish = true;
			stop();
			return ;
		}
		loginAjax.setData("uid", ILogin.getLoginUid());
		loginAjax.setId(AJAX_FLAG_LOGIN);
		loginAjax.setOnSuccessListener(this);
		loginAjax.setOnFinishListener(this);
		loginAjax.send();
	}

	private void updateErrorLog(File file) {
		if (file == null || file.length() == 0) {
			logAjaxFinish = true;
			return;
		}
		
		byte[] data = null;
		ByteArrayOutputStream outputstream = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			outputstream = new ByteArrayOutputStream(1024);
			GzipHelper.compress(in, outputstream);
			data = outputstream.toByteArray();
		} catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			data = null;
		} finally {
			try {
				if (outputstream != null) {
					outputstream.close();
					outputstream = null;
				}

				if (in != null) {
					in.close();
					in = null;
				}
			} catch (Exception ex) {
				Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			}
		}

		if (data == null) {
			logAjaxFinish = true;
			return;
		}
		
		ServiceConfig.setContext(this.getApplicationContext());
		logAjax = ServiceConfig.getAjax(Config.URL_POST_LOG);
		if( null == logAjax ) {
			logAjaxFinish = true;
			return ;
		}
		
		logAjax.setData("k", "logfile");
		logAjax.setParser(new JSONParser());
		logAjax.setOnFinishListener(this);
		logAjax.setOnSuccessListener(this);
		logAjax.setId(AJAX_FLAG_LOG);
		logAjax.setFile("logfile", data);
		logAjax.send();
	}

	@Override
	public void onFinish(Response response) {
		switch (response.getId()) {
		case AJAX_FLAG_LOGIN:
			loginAjaxFinish = true;
			stop();
			break;
		case AJAX_FLAG_LOG:
			logAjaxFinish = true;
			stop();
			break;
		}
	}

	@Override
	public void onSuccess(Object org, Response response) {
		JSONObject v = null;
		switch (response.getId()) {
		case AJAX_FLAG_LOGIN:
			v = (JSONObject) org;
			if (v.optInt("errno") == 500) {
				ILogin.clearAccount();
			}
			break;
		case AJAX_FLAG_LOG:
			v = (JSONObject) org;
			if (v.optInt("errno", -1) == 0) {
				logAjax = null;
				if (mLogFile != null) {
					mLogFile.delete();
				}
			}
			break;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
