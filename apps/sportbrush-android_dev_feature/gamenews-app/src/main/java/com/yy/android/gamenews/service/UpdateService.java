package com.yy.android.gamenews.service;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.yy.android.sportbrush.R;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.util.DownloadUtil;
import com.yy.android.gamenews.util.DownloadUtil.DownloadCallback;

public class UpdateService extends IntentService {

	public UpdateService() {
		super("UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final long startTime = System.currentTimeMillis();
		String downloadUrl = intent.getStringExtra("downloadUrl");
//		int versionCode = intent.getIntExtra("versionCode", 0);
		final String storeFileName = "gamenews" + ".apk";
		if (DownloadUtil.isDowning(storeFileName)) {
			return;
		}
		DownloadCallback callback = new DownloadCallback() {
			public void onDownload(int downloadBytes, int totalBytes) {
				if (downloadBytes < totalBytes) {
					showProgressNotify(startTime, downloadBytes, totalBytes);
				} else {
					showDoneNotify(storeFileName);
					checkDownloaded(storeFileName);
				}
			}

			public void onFail() {
				showDoneNotify(null);
			}
		};
		showProgressNotify(startTime, 0, 0);
		DownloadUtil.download(downloadUrl, storeFileName, callback);
	}

	private boolean checkDownloaded(String storeFileName) {
		String fileName = DownloadUtil.getFile(storeFileName);
		if (fileName == null) {
			return false;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		this.startActivity(intent);
		return true;
	}

	@SuppressLint("InlinedApi")
	private PendingIntent openApk(String fileName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		return PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@SuppressLint("InlinedApi")
	private PendingIntent openMain() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		return PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private void showProgressNotify(long startTime, int downloadBytes,
			int totalBytes) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this).setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.global_downloading))
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setWhen(startTime)
				.setProgress(totalBytes, downloadBytes, totalBytes == 0)
				.setOngoing(true).setContentIntent(openMain());
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, builder.build());
	}

	private void showDoneNotify(String storeFileName) {
		String fileName = storeFileName != null ? DownloadUtil
				.getFile(storeFileName) : null;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(
						getString(fileName != null ? R.string.global_downloaded
								: R.string.apk_download_failed))
				.setSmallIcon(android.R.drawable.stat_sys_download_done)
				.setAutoCancel(true);
		if (fileName != null) {
			builder.setContentIntent(openApk(fileName));
		} else {
			builder.setContentIntent(openMain());
		}
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, builder.build());
	}
}
