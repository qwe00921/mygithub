package com.yy.android.gamenews.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;
import android.os.Environment;

public class DownloadUtil {
	private static final String TAG = "DownloadUtil";

	public static interface DownloadCallback {
		public void onDownload(int downloadBytes, int totalBytes);

		public void onFail();
	}

	private static Set<String> downSet = new HashSet<String>();

	public static void download(String downloadUrl, String storeFileName,
			DownloadCallback callback) {
		InputStream ins = null;
		String tmpStoreFileName = storeFileName + "_bak";
		clearStoreFile(storeFileName, tmpStoreFileName);
		OutputStream out = openStoreFile(tmpStoreFileName);
		try {
			synchronized (downSet) {
				downSet.add(storeFileName);
			}
			HttpResponse response = null;
			AndroidHttpClient httpClient = AndroidHttpClient
					.newInstance("downloader");
			for (int i = 0; i < 5; i++) {
				HttpGet httpGet = new HttpGet(downloadUrl);
				response = httpClient.execute(httpGet);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
					Header[] locHs = response.getHeaders("Location");
					if ((null != locHs) && (locHs.length >= 1)) {
						downloadUrl = locHs[0].getValue();
					}
				} else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					break;
				}
			}

			Header[] hs = response.getHeaders("Content-Length");
			int fileSize = Integer.MAX_VALUE;
			if ((null != hs) && (hs.length >= 1)) {
				fileSize = Integer.parseInt(hs[0].getValue().trim());
			}
			ins = response.getEntity().getContent();
			byte[] buffer = new byte[1024 * 64];
			int totalRead = 0;
			int lastRatio = 0;
			if(callback != null) {
				callback.onDownload(totalRead, fileSize);
			}
			while (totalRead < fileSize) {
				int readed = ins.read(buffer);
				if (-1 == readed) {
					break;
				} else {
					out.write(buffer, 0, readed);
					totalRead += readed;
					if (null != callback) {
						if (totalRead >= fileSize) {
							renameFile(tmpStoreFileName, storeFileName);
							callback.onDownload(totalRead, fileSize);
						} else {
							int ratio = (int) (totalRead * 100L / fileSize);
							if (ratio >= (lastRatio + 1)) {
								lastRatio = ratio;
								callback.onDownload(totalRead, fileSize);
							}
						}

					}
				}
			}
			if (fileSize < totalRead) {
				if(callback != null) {
					callback.onFail();
				}
			}
		} catch (IOException e) {
			if(callback != null) {
				callback.onFail();
			}
		} finally {
			synchronized (downSet) {
				downSet.remove(storeFileName);
			}
			if (null != ins) {
				try {
					ins.close();
				} catch (IOException e) {
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

	}

	private static void renameFile(String fromFileName, String toFileName) {
		String baseDir = getBaseDir();
		File from = new File(baseDir + "/" + fromFileName);
		File to = new File(baseDir + "/" + toFileName);
		from.renameTo(to);
	}

	private final static String BASE_PATH = "/duowan/shouyou/";
	private final static String SHARE_RECEIVE_PATH = "/duowan/shouyou_receiveFiles/";

	private static String getBaseDir() {
		File baseDir = Environment.getExternalStorageDirectory();
		String basePath = baseDir.getPath() + BASE_PATH;
		File dir = new File(basePath);
		if (false == dir.exists()) {
			dir.mkdirs();
		}
		return dir.getPath();
	}

	private static String getShareReceiveBaseDir() {
		File baseDir = Environment.getExternalStorageDirectory();
		String basePath = baseDir.getPath() + SHARE_RECEIVE_PATH;
		File dir = new File(basePath);
		if (false == dir.exists()) {
			dir.mkdirs();
		}
		return dir.getPath();
	}

	public static String getShareReceivePath() {
		return SHARE_RECEIVE_PATH;
	}

	private static OutputStream openStoreFile(String fileName) {
		String baseDir = getBaseDir();
		File f = new File(baseDir + "/" + fileName);
		try {
			FileOutputStream fos = new FileOutputStream(f);
			return fos;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	private static void clearStoreFile(String fileName, String tmpFileName) {
		String baseDir = getBaseDir();
		File f = new File(baseDir + "/" + fileName);
		f.delete();
		f = new File(baseDir + "/" + tmpFileName);
		f.delete();
	}

	public static String getFile(String fileName) {
		String baseDir = getBaseDir();
		File f = new File(baseDir + "/" + fileName);
		if (f.exists()) {
			return f.getPath();
		}
		return null;
	}

	public static File getRealFile(String fileName) {
		return new File(getBaseDir(), fileName);
	}

	public static File getRealShareReceiveFile(String fileName) {
		return new File(getShareReceiveBaseDir(), fileName);
	}

	public static String getFileName(int appId, int companyId, int versionCode,
			String fileType) {
		return "" + appId + "_" + companyId + "_" + versionCode + "."
				+ fileType;
	}

	public static boolean isDowning(String storeFileName) {
		synchronized (downSet) {
			return downSet.contains(storeFileName);
		}
	}

//	private static LongSparseArray<PackageVersionInfo> loadDownloadList(
//			SharedPreferences pref) {
//		Map<String, ?> items = pref.getAll();
//		if (items.isEmpty()) {
//			return null;
//		}
//		LongSparseArray<PackageVersionInfo> result = new LongSparseArray<PackageVersionInfo>(
//				items.size());
//		for (String key : items.keySet()) {
//			PackageVersionInfo info = new PackageVersionInfo();
//			String[] parts = key.split("\\|");
//			info.appId = Integer.valueOf(parts[0]);
//			info.companyId = Integer.valueOf(parts[1]);
//			info.remoteVersionCode = Integer.valueOf(parts[2]);
//			String value = (String) items.get(key);
//			String[] valueParts = value.split("\\|");
//			info.downloadId = Long.valueOf(valueParts[0]);
//			info.downloadStartTime = Long.valueOf(valueParts[1]);
//			result.put(info.downloadId, info);
//		}
//		return result;
//	}
//
//	public static Long getDownloadStartTime(long id) {
//		Context context = MyApplication.getMyContext();
//		SharedPreferences pref = context.getSharedPreferences(
//				Config.DOWNLOAD_PREF, 0);
//		LongSparseArray<PackageVersionInfo> result = loadDownloadList(pref);
//		if (result != null) {
//			for (int i = 0; i < result.size(); i++) {
//				PackageVersionInfo info = result.valueAt(i);
//				if (info.downloadId == id) {
//					return info.downloadStartTime;
//				}
//			}
//		}
//		return null;
//	}
//
//	private static void updateWithDownloadInfo(
//			LongSparseArray<PackageVersionInfo> result, DownloadManager dm) {
//		if (result.size() == 0) {
//			return;
//		}
//		long[] ids = new long[result.size()];
//		for (int i = 0; i < ids.length; i++) {
//			ids[i] = result.keyAt(i);
//		}
//		Cursor cursor = dm
//				.query(new DownloadManager.Query().setFilterById(ids));
//		try {
//			while (cursor.moveToNext()) {
//				long id = (long) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
//				PackageVersionInfo info = result.get(id);
//				info.downloadStatus = (int) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
//				info.downloadReason = (int) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
//				info.downloadBytes = (int) cursor
//						.getLong(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//				info.downloadTotalBytes = (int) cursor
//						.getLong(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//				info.downloadLocalUri = cursor
//						.getString(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
//				info.downloadUrl = cursor.getString(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
//			}
//		} finally {
//			cursor.close();
//		}
//	}
//
//	public static void shareUpdateWithDownloadInfo(
//			LongSparseArray<PackageVersionInfo> result) {
//		Context context = MyApplication.getMyContext();
//		DownloadManager dm = new DownloadManager(context,
//				"com.duowan.tq.mobile");
//		if (result.size() == 0) {
//			return;
//		}
//		long[] ids = new long[result.size()];
//		for (int i = 0; i < ids.length; i++) {
//			ids[i] = result.keyAt(i);
//		}
//		Cursor cursor = dm
//				.query(new DownloadManager.Query().setFilterById(ids));
//		try {
//			while (cursor.moveToNext()) {
//				long id = (long) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
//				PackageVersionInfo info = result.get(id);
//				info.downloadStatus = (int) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
//				info.downloadReason = (int) cursor.getLong(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
//				info.downloadBytes = (int) cursor
//						.getLong(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//				info.downloadTotalBytes = (int) cursor
//						.getLong(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//				info.downloadLocalUri = cursor
//						.getString(cursor
//								.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
//				info.downloadUrl = cursor.getString(cursor
//						.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
//			}
//		} finally {
//			cursor.close();
//		}
//	}
//
//	public static void shareCancelDownload(long... ids) {
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		dm.remove(ids);
//	}
//
//	private static Pair<List<String>, List<Long>> removeNoDownloadInfo(
//			LongSparseArray<PackageVersionInfo> result) {
//		List<String> keys = new ArrayList<String>(result.size());
//		List<Long> ids = new ArrayList<Long>(result.size());
//		for (int i = result.size() - 1; i >= 0; i--) {
//			PackageVersionInfo info = result.valueAt(i);
//			String key = String.format(Locale.US, "%d|%d|%d", info.appId,
//					info.companyId, info.remoteVersionCode);
//			if (info.downloadStatus == null) {
//				keys.add(key);
//				ids.add(info.downloadId);
//				result.removeAt(i);
//			}
//		}
//		return new Pair<List<String>, List<Long>>(keys, ids);
//	}
//
//	public static void removeFromDownloadList(int appId, int companyId,
//			int remoteVersionCode) {
//		Context context = MyApplication.getMyContext();
//		SharedPreferences pref = context.getSharedPreferences(
//				Config.DOWNLOAD_PREF, 0);
//		String key = String.format(Locale.US, "%d|%d|%d", appId, companyId,
//				remoteVersionCode);
//		pref.edit().remove(key).commit();
//	}
//
//	private static void removeFromDownloadList(List<String> toRemove,
//			SharedPreferences pref) {
//		if (!toRemove.isEmpty()) {
//			Editor editor = pref.edit();
//			for (String key : toRemove) {
//				editor.remove(key);
//			}
//			editor.commit();
//		}
//	}
//
//	public static void removeFromDownloadManager(long... ids) {
//		if (ids == null) {
//			return;
//		}
//		Context context = MyApplication.getMyContext();
//		StatService.onEvent(context, "download", "cancel", ids.length);
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		dm.remove(ids);
//	}
//
//	private static void removeFromDownloadManager(List<Long> toRemove,
//			DownloadManager dm) {
//		if (!toRemove.isEmpty()) {
//			long[] ids = new long[toRemove.size()];
//			for (int i = 0; i < toRemove.size(); i++) {
//				ids[i] = toRemove.get(i);
//			}
//			dm.remove(ids);
//		}
//	}
//
//	private static void updateWithAppInfo(
//			LongSparseArray<PackageVersionInfo> result,
//			SparseArray<AppInfoData> appInfoCache) {
//		for (int i = 0; i < result.size(); i++) {
//			PackageVersionInfo info = result.valueAt(i);
//			AppInfoData app = appInfoCache.get(info.appId);
//			if (app == null) {
//				continue;
//			}
//			AppCompanyRelateData company = app.companyRelateMap
//					.get(info.companyId);
//			if (company == null) {
//				continue;
//			}
//			VersionInfoData version = null;
//			for (VersionInfoData v : company.versionArray) {
//				if (v.versionCode.equals(info.remoteVersionCode)) {
//					info.remoteVersionName = v.versionName;
//					info.remoteTotalBytes = v.fileSize;
//					version = v;
//					break;
//				}
//			}
//			if (version == null || version.downloadUrl == null
//					|| !version.downloadUrl.equals(info.downloadUrl)) {
//				continue;
//			}
//			info.appName = app.appName;
//			info.icon = app.icon;
//			info.privCount = app.privCount;
//			info.packageName = company.androidPackageName;
//			info.localVersionCode = -1;
//			info.companyName = company.companyName;
//		}
//	}
//
//	private static HttpUriRequest generateCacheAppInfoRequest(
//			LongSparseArray<PackageVersionInfo> result,
//			SparseArray<AppInfoData> appInfoCache) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < result.size(); i++) {
//			int appId = result.valueAt(i).appId;
//			if (appInfoCache.get(appId) != null) {
//				continue;
//			}
//			if (sb.length() > 0) {
//				sb.append(',');
//			}
//			sb.append(appId);
//		}
//		String appids = sb.toString();
//		if (appids.isEmpty()) {
//			return null;
//		}
//
//		LinkedList<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("appids", appids));
//		HttpPost request = new HttpPost(Config.API_URL + "/apps");
//		try {
//			HttpEntity entity = new UrlEncodedFormEntity(params);
//			request.setEntity(entity);
//		} catch (UnsupportedEncodingException e) {
//			Log.e(TAG, e.getMessage());
//			return null;
//		}
//		return request;
//	}
//
//	private static void updateAppInfoCache(
//			SparseArray<AppInfoData> appInfoCache, ApiResponse response) {
//		if (response.result == ResultCode.SUCCESS) {
//			AppInfoArrayData data = (AppInfoArrayData) response.data;
//			for (AppInfoData item : data.appInfoArray) {
//				appInfoCache.put(item.appId, item);
//			}
//		}
//	}
//
//	private static void removeNoAppInfo(
//			LongSparseArray<PackageVersionInfo> result) {
//		for (int i = result.size() - 1; i >= 0; i--) {
//			PackageVersionInfo info = result.valueAt(i);
//			if (info.appName == null) {
//				result.removeAt(i);
//			}
//		}
//	}
//
//	private static class TimeComparator implements
//			Comparator<PackageVersionInfo> {
//		public int compare(PackageVersionInfo lhs, PackageVersionInfo rhs) {
//			int lStatus = lhs.downloadStatus == DownloadManager.STATUS_SUCCESSFUL ? 1
//					: 0;
//			int rStatus = rhs.downloadStatus == DownloadManager.STATUS_SUCCESSFUL ? 1
//					: 0;
//			if (lStatus != rStatus) {
//				return lStatus - rStatus;
//			}
//			return lhs.downloadStartTime < rhs.downloadStartTime ? 1
//					: (lhs.downloadStartTime > rhs.downloadStartTime ? -1 : 0);
//		}
//	}
//
//	private static TimeComparator mTimeComparator = new TimeComparator();
//
//	private static List<PackageVersionInfo> sortResult(
//			LongSparseArray<PackageVersionInfo> result) {
//		List<PackageVersionInfo> list = new ArrayList<PackageVersionInfo>(
//				result.size());
//		for (int i = 0; i < result.size(); i++) {
//			list.add(result.valueAt(i));
//		}
//		Collections.sort(list, mTimeComparator);
//		return list;
//	}
//
//	public static List<PackageVersionInfo> listDownload(AppInfoData app) {
//		Context context = MyApplication.getMyContext();
//		SharedPreferences pref = context.getSharedPreferences(
//				Config.DOWNLOAD_PREF, 0);
//		LongSparseArray<PackageVersionInfo> result = DownloadUtil
//				.loadDownloadList(pref);
//		if (result == null) {
//			return null;
//		}
//
//		for (int i = result.size() - 1; i >= 0; i--) {
//			if (!result.valueAt(i).appId.equals(app.appId)) {
//				result.removeAt(i);
//			}
//		}
//		if (result.size() == 0) {
//			return null;
//		}
//
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		DownloadUtil.updateWithDownloadInfo(result, dm);
//		Pair<List<String>, List<Long>> toRemove = DownloadUtil
//				.removeNoDownloadInfo(result);
//		SparseArray<AppInfoData> appInfoCache = new SparseArray<AppInfoData>(1);
//		appInfoCache.put(app.appId, app);
//		DownloadUtil.updateWithAppInfo(result, appInfoCache);
//		DownloadUtil.removeNoAppInfo(result);
//		DownloadUtil.removeFromDownloadList(toRemove.first, pref);
//		DownloadUtil.removeFromDownloadManager(toRemove.second, dm);
//		return DownloadUtil.sortResult(result);
//	}
//
//	public static PackageVersionInfo getDownloadForCode(
//			UserCodeInfoData userCodeInfo) {
//		List<PackageVersionInfo> downloads = DownloadUtil
//				.listDownload(userCodeInfo.appInfo);
//		if (downloads == null) {
//			return null;
//		}
//		for (PackageVersionInfo item : downloads) {
//			if (item.companyId == userCodeInfo.privInfo.companyId) {
//				return item;
//			}
//		}
//		return null;
//	}
//
//	public static void setDownloadViews(PackageVersionInfo item,
//			ProgressBar progress, TextView description) {
//		if (item.downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
//			progress.setMax(100);
//			progress.setProgress(100);
//			progress.setIndeterminate(false);
//		} else if (item.downloadTotalBytes > 0) {
//			int percent = (int) (100L * item.downloadBytes / item.downloadTotalBytes);
//			progress.setMax(100);
//			progress.setProgress(percent);
//			progress.setIndeterminate(false);
//		} else {
//			progress.setIndeterminate(true);
//		}
//		if (item.downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
//			description.setText(R.string.download_STATUS_SUCCESSFUL);
//		} else if (item.downloadStatus == DownloadManager.STATUS_FAILED) {
//			if (item.downloadReason == DownloadManager.ERROR_CANNOT_RESUME) {
//				description.setText(R.string.download_ERROR_CANNOT_RESUME);
//			} else if (item.downloadReason == DownloadManager.ERROR_DEVICE_NOT_FOUND) {
//				description.setText(R.string.download_ERROR_DEVICE_NOT_FOUND);
//			} else if (item.downloadReason == DownloadManager.ERROR_FILE_ALREADY_EXISTS) {
//				description
//						.setText(R.string.download_ERROR_FILE_ALREADY_EXISTS);
//			} else if (item.downloadReason == DownloadManager.ERROR_FILE_ERROR) {
//				description.setText(R.string.download_ERROR_FILE_ERROR);
//			} else if (item.downloadReason == DownloadManager.ERROR_HTTP_DATA_ERROR) {
//				description.setText(R.string.download_ERROR_HTTP_DATA_ERROR);
//			} else if (item.downloadReason == DownloadManager.ERROR_INSUFFICIENT_SPACE) {
//				description.setText(R.string.download_ERROR_INSUFFICIENT_SPACE);
//			} else if (item.downloadReason == DownloadManager.ERROR_TOO_MANY_REDIRECTS) {
//				description.setText(R.string.download_ERROR_TOO_MANY_REDIRECTS);
//			} else if (item.downloadReason == DownloadManager.ERROR_UNHANDLED_HTTP_CODE) {
//				description
//						.setText(R.string.download_ERROR_UNHANDLED_HTTP_CODE);
//			} else if (item.downloadReason == DownloadManager.ERROR_UNKNOWN) {
//				description.setText(R.string.download_ERROR_UNKNOWN);
//			} else {
//				description.setText(MyApplication.getMyResources().getString(
//						R.string.download_error_code, item.downloadReason));
//			}
//		} else if (item.downloadStatus == DownloadManager.STATUS_RUNNING) {
//			description.setText(R.string.download_STATUS_RUNNING);
//		} else if (item.downloadStatus == DownloadManager.STATUS_PENDING) {
//			description.setText(R.string.download_STATUS_PENDING);
//		} else if (item.downloadStatus == DownloadManager.STATUS_PAUSED) {
//			if (item.downloadReason == DownloadManager.PAUSED_QUEUED_FOR_WIFI) {
//				description.setText(R.string.download_PAUSED_QUEUED_FOR_WIFI);
//			} else if (item.downloadReason == DownloadManager.PAUSED_WAITING_FOR_NETWORK) {
//				description
//						.setText(R.string.download_PAUSED_WAITING_FOR_NETWORK);
//			} else if (item.downloadReason == DownloadManager.PAUSED_WAITING_TO_RETRY) {
//				description.setText(R.string.download_PAUSED_WAITING_TO_RETRY);
//			} else if (item.downloadReason == DownloadManager.PAUSED_UNKNOWN) {
//				description.setText(R.string.download_PAUSED_UNKNOWN);
//			} else if (item.downloadReason == DownloadManager.PAUSED_BY_APP) {
//				description.setText(R.string.download_PAUSED_BY_APP);
//			}
//		}
//	}
//
//	public static void openApk(Uri file) {
//		Context context = MyApplication.getMyContext();
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.setDataAndType(file, "application/vnd.android.package-archive");
//		context.startActivity(intent);
//	}
//
//	public static long shareDownload(Uri uri, String gameName) {
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//
//		if (!gameName.endsWith(".apk")) {
//			gameName += ".apk";
//		}
//		File file = DownloadUtil.getRealShareReceiveFile(gameName);
//		if (file.exists()) {
//			file.delete();
//		}
//
//		DownloadManager.Request request = new DownloadManager.Request(uri);
//		request.setTitle(gameName);
//		request.setDestinationUri(Uri.fromFile(DownloadUtil
//				.getRealShareReceiveFile(gameName)));
//		request.setMimeType("application/vnd.android.package-archive");
//		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
//		long id = dm.enqueue(request);
//		StatService.onEvent(MyApplication.getMyContext(), "share_download",
//				"start", 1);
//		return id;
//	}
//
//	private static final int HTTP_TEMP_REDIRECT = 307;
//
//	public static String handleRedirectUrl(String requestUrl) {
//		int redirectionCount = 0;
//		URL url;
//		try {
//			url = new URL(requestUrl);
//		} catch (MalformedURLException e1) {
//			return null;
//		}
//
//		while (redirectionCount++ < 5) {
//			// Open connection and follow any redirects until we have a useful
//			// response with body.
//			HttpURLConnection conn = null;
//			try {
//				conn = (HttpURLConnection) url.openConnection();
//				conn.setInstanceFollowRedirects(false);
//				conn.setConnectTimeout(20000);
//				conn.setReadTimeout(20000);
//
//				final int responseCode = conn.getResponseCode();
//				switch (responseCode) {
//				case HTTP_OK:
//					return requestUrl;
//
//				case HTTP_MOVED_PERM:
//				case HTTP_MOVED_TEMP:
//				case HTTP_SEE_OTHER:
//				case HTTP_TEMP_REDIRECT:
//					final String location = conn.getHeaderField("Location");
//					requestUrl = location;
//					url = new URL(url, location);
//					if (responseCode == HTTP_MOVED_PERM) {
//						// Push updated URL back to database
//						String requestUri = url.toString();
//					}
//					continue;
//
//				default:
//					return null;
//				}
//			} catch (IOException e) {
//				Log.e(TAG, e.toString());
//				// do nothing , give it to system downloadmanager
//			} finally {
//				if (conn != null) {
//					conn.disconnect();
//				}
//			}
//		}
//		// Too many redirects
//		return null;
//	}
//
//	private static String generateFileName(String gameName) {
//		if (gameName.endsWith(".apk")) {
//			gameName = gameName.substring(0, gameName.length() - 4);
//		}
//		File dir = Environment
//				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//		String name = gameName + ".apk";
//		File file = new File(dir, name);
//		int i = 1;
//		while (file.exists()) {
//			i++;
//			name = gameName + String.valueOf(i) + ".apk";
//			file = new File(dir, name);
//		}
//		return name;
//	}
//
//	public static long downloadFromExternal(Uri uri, String gameName) {
//		long id = 0;
//		android.app.DownloadManager dm = (android.app.DownloadManager) MyApplication
//				.getMyContext().getSystemService(Context.DOWNLOAD_SERVICE);
//
//		gameName = generateFileName(gameName);
//
//		android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(
//				uri);
//		request.setTitle(gameName);
//		request.setDestinationInExternalPublicDir(
//				Environment.DIRECTORY_DOWNLOADS, gameName);
//		setNotificationVisibility(request);
//		request.setMimeType("application/vnd.android.package-archive");
//		id = dm.enqueue(request);
//		return id;
//	}
//
//	public static void download(PackageVersionInfo info) {
//		DownloadDialog.Item item = new DownloadDialog.Item();
//		item.appId = info.appId;
//		item.appName = info.appName;
//		item.companyId = info.companyId;
//		item.downloadUrl = info.downloadUrl;
//		item.versionCode = info.remoteVersionCode;
//		item.companyName = info.companyName;
//		download(item);
//	}
//
//	public static int pauseDownload(long id) {
//		// get item related id
//		StatService.onEvent(MyApplication.getMyContext(), "download", "pause",
//				1);
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		return dm.pauseDownload(id);
//
//	}
//
//	public static int continueDownload(long id) {
//		// get item related id
//		StatService.onEvent(MyApplication.getMyContext(), "download",
//				"continue", 1);
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		return dm.continueDownload(id);
//	}
//
//	public static void download(Item item) {
//		Context context = MyApplication.getMyContext();
//		SharedPreferences pref = context.getSharedPreferences(
//				Config.DOWNLOAD_PREF, 0);
//
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//
//		String key = String.format(Locale.US, "%d|%d|%d", item.appId,
//				item.companyId, item.versionCode);
//		String value = pref.getString(key, null);
//		if (value != null) {
//			String[] parts = value.split("\\|");
//			long id = Long.valueOf(parts[0]);
//			boolean exist = false;
//			Cursor cursor = dm.query(new DownloadManager.Query()
//					.setFilterById(id));
//			try {
//				exist = cursor.moveToFirst();
//				if (exist) {
//					int status = (int) cursor
//							.getLong(cursor
//									.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
//					if (status != DownloadManager.STATUS_FAILED) {
//						return;
//					}
//				}
//			} finally {
//				cursor.close();
//			}
//			if (exist) {
//				dm.remove(id);
//			}
//			pref.edit().remove(key).commit();
//		}
//
//		String fileName = DownloadUtil.getFileName(item.appId, item.companyId,
//				item.versionCode, "apk");
//		File file = DownloadUtil.getRealFile(fileName);
//		if (file.exists()) {
//			file.delete();
//		}
//
//		DownloadManager.Request request = new DownloadManager.Request(
//				Uri.parse(item.downloadUrl));
//		request.setTitle(item.appName);
//		request.setDestinationUri(Uri.fromFile(DownloadUtil
//				.getRealFile(fileName)));
//		setNotificationVisibility(request);
//		request.setMimeType("application/vnd.android.package-archive");
//		long id = dm.enqueue(request);
//		pref.edit()
//				.putString(
//						key,
//						String.format(Locale.US, "%d|%d", id,
//								System.currentTimeMillis())).commit();
//		Toast.makeText(context, R.string.start_download, Toast.LENGTH_SHORT)
//				.show();
//
//		new AddDownloadTask().execute(item.appId);
//
//		StatService.onEvent(context, "download", "start", 1);
//	}
//
//	/*
//	 * Description: 下载指定url的文件
//	 * 
//	 * @param downloadUrl: 下载文件的url地址
//	 * 
//	 * @param appName: 下载的文件名字，通知消息使用
//	 * 
//	 * @param storeFileName : 文件保存名
//	 */
//	public static void download(String downloadUrl, String appName,
//			String storeFileName) {
//		Context context = MyApplication.getMyContext();
//
//		DownloadManager dm = MyApplication.getMyDownloadManager();
//		if (!storeFileName.endsWith(".apk")) {
//			storeFileName += ".apk";
//		}
//		File file = DownloadUtil.getRealFile(storeFileName);
//		if (file.exists()) {
//			file.delete();
//		}
//		DownloadManager.Request request = new DownloadManager.Request(
//				Uri.parse(downloadUrl));
//		request.setTitle(appName);
//		request.setDestinationUri(Uri.fromFile(DownloadUtil
//				.getRealFile(storeFileName)));
//		setNotificationVisibility(request);
//		request.setMimeType("application/vnd.android.package-archive");
//		dm.enqueue(request);
//		Toast.makeText(context, R.string.start_download, Toast.LENGTH_SHORT)
//				.show();
//	}
//
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	private static void setNotificationVisibility(
//			DownloadManager.Request request) {
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
//					| DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//		}
//	}
//
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	private static void setNotificationVisibility(
//			android.app.DownloadManager.Request request) {
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
//					| DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//		}
//	}
//
//	public static class ListDownloadTask extends
//			HttpAsyncTask<Void, Void, List<PackageVersionInfo>> {
//		private SparseArray<AppInfoData> mAppInfoCache;
//
//		public ListDownloadTask(SparseArray<AppInfoData> appInfoCache) {
//			mAppInfoCache = appInfoCache;
//		}
//
//		@Override
//		protected List<PackageVersionInfo> doInBackground(Void... unused) {
//			Context context = MyApplication.getMyContext();
//			SharedPreferences pref = context.getSharedPreferences(
//					Config.DOWNLOAD_PREF, 0);
//			LongSparseArray<PackageVersionInfo> result = DownloadUtil
//					.loadDownloadList(pref);
//			if (result == null) {
//				return null;
//			}
//
//			DownloadManager dm = MyApplication.getMyDownloadManager();
//			DownloadUtil.updateWithDownloadInfo(result, dm);
//			Pair<List<String>, List<Long>> toRemove = DownloadUtil
//					.removeNoDownloadInfo(result);
//			HttpUriRequest request = DownloadUtil.generateCacheAppInfoRequest(
//					result, mAppInfoCache);
//			if (request != null) {
//				ApiResponse response = call(request);
//				if (response == null) {
//					return null;
//				}
//				DownloadUtil.updateAppInfoCache(mAppInfoCache, response);
//			}
//			DownloadUtil.updateWithAppInfo(result, mAppInfoCache);
//			DownloadUtil.removeNoAppInfo(result);
//			DownloadUtil.removeFromDownloadList(toRemove.first, pref);
//			DownloadUtil.removeFromDownloadManager(toRemove.second, dm);
//			return DownloadUtil.sortResult(result);
//		}
//	}
//
//	private static class AddDownloadTask extends
//			HttpAsyncTask<Integer, Void, ApiResponse> {
//		private static final String TAG = "AddDownloadTask";
//
//		@Override
//		protected ApiResponse doInBackground(Integer... appId) {
//			LinkedList<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
//			params.add(new BasicNameValuePair("appid", String.valueOf(appId[0])));
//			HttpPost request = new HttpPost(Config.API_URL + "/apps/download");
//			try {
//				HttpEntity entity = new UrlEncodedFormEntity(params);
//				request.setEntity(entity);
//			} catch (UnsupportedEncodingException e) {
//				Log.e(TAG, e.getMessage());
//				return ApiObjUtil.newFailResponse();
//			}
//			return call(request);
//		}
//
//		@Override
//		protected void onPostExecute(ApiResponse result) {
//			if (result.result == ResultCode.SUCCESS) {
//				UserInfo.getInstance().loadMyBooks();
//			}
//		}
//	}
}
