package com.icson.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.icson.R;
import com.icson.lib.ui.UiUtils;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.HttpFile;
import com.icson.util.cache.FileStorage;
import com.icson.util.cache.StorageFactory;

public class DownLoadService extends Service {

	private static final String LOG_TAG = DownLoadService.class.getName();

	public static final String REQUEST_URL = "request_url";
	public static final String REQUEST_TITLE = "request_title";

	private static final int NOTIFICATION_ID = 1;

	private Notification notification = null;

	private NotificationManager manager = null;

	private boolean mIsActive = false;

	private Handler mHandler;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (mIsActive || null == intent) {
			return;
		}
		super.onStart(intent, startId);

		
		String url = intent.getStringExtra(REQUEST_URL);
		String title = intent.getStringExtra(REQUEST_TITLE);
		
		if (url == null ) {
			return;
		}
		if(title == null)
			title = "易迅网";

		startDownLoad(title,url);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		
		if (manager != null) {
			manager.cancel(NOTIFICATION_ID);
			manager = null;
		}
		mHandler = null;
		notification = null;
		super.onDestroy();
	}

	public void startDownLoad(final String title,String url) {
		mIsActive = true;

		//清除过期文件
		final String DOWNLOAD_PATH = "download";
		
		FileStorage fileStorage = StorageFactory.getFileStorage(this);
		fileStorage.removeFolder(DOWNLOAD_PATH);
		fileStorage.createPath(DOWNLOAD_PATH);

		notification = new Notification(android.R.drawable.stat_sys_download, "正在下载", System.currentTimeMillis());
		notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notify_content);
		notification.contentView.setProgressBar(R.id.notify_progresbar, 100, 0, false);
		notification.contentView.setTextViewText(R.id.notify_title, title);
		notification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID, notification);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (notification == null)
					return;

				switch (msg.what) {
				case HttpFile.NET_MSG_GETLENTH:
					int current = msg.arg1,
					max = msg.arg2;
					notification.contentView.setProgressBar(R.id.notify_progresbar, max, current, false);
					notification.contentView.setTextViewText(R.id.notify_title, title +" (进度:" + (int) ((current * 100) / max) + "%) ");
					manager.notify(NOTIFICATION_ID, notification);
					if (current >= max) {
						manager.cancel(NOTIFICATION_ID);
					}
					break;
				case HttpFile.NET_MSG_FINISH:
					ToolUtil.installApk(DownLoadService.this, (File) msg.obj);
					stopSelf();
					break;
				case HttpFile.NET_MSG_ERROR:
					UiUtils.makeToast(DownLoadService.this, "下载失败");
					stopSelf();
					break;
				}
			}
		};

		Log.d(LOG_TAG, url);
		HttpFile.downLoadFile(this, mHandler, url, fileStorage.createFile(DOWNLOAD_PATH + "/tmp.apk"));
	}
}
