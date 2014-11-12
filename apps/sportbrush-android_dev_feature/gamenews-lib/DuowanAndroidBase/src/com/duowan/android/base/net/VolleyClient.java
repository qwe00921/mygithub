package com.duowan.android.base.net;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.duowan.android.base.BaseActivity;
import com.duowan.android.base.event.UniPacketErrorEvent;
import com.duowan.android.base.event.VolleyErrorEvent;
import com.duowan.android.base.event.VolleyInitEvent;
import com.duowan.android.base.net.toolbox.WupRequest;
import com.duowan.jce.wup.UniPacket;

import de.greenrobot.event.EventBus;

/**
 * @author yy:909012690@lishaoqi
 * @version 创建时间：2014-3-10 下午3:14:12
 */
public class VolleyClient {

	private final static String TAG = "Volley";
	private static final String DEFAULT_CACHE_DIR = "volley";

	private static RequestQueue mRequestQueue;

	public static void init(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
	}

	public static void init(Context context, int maxCacheSizeInBytes) {

		File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

		String userAgent = "volley/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
		} catch (NameNotFoundException e) {
		}

		HttpStack stack = null;
		if (Build.VERSION.SDK_INT >= 9) {
			stack = new HurlStack();
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See:
			// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			stack = new HttpClientStack(
					AndroidHttpClient.newInstance(userAgent));
		}

		Network network = new BasicNetwork(stack);
		if (maxCacheSizeInBytes <= 0) {
			mRequestQueue = new RequestQueue(new DiskBasedCache(cacheDir),
					network);
		} else {
			mRequestQueue = new RequestQueue(new DiskBasedCache(cacheDir,
					maxCacheSizeInBytes), network);
		}

		mRequestQueue.start();
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue == null)
			EventBus.getDefault().post(new VolleyInitEvent());
		return mRequestQueue;
	}

	public static void newRequestQueue(String host, final UniPacket uniPacket,
			final Listener responseListener) {
		newRequestQueue(host, uniPacket, null, null, responseListener);
	}

	public static void newRequestQueue(String host, final UniPacket uniPacket,
			final String cacheKey, final String tag,
			final Listener responseListener) {
		newRequestQueue(host, uniPacket, cacheKey, tag, 2500, responseListener);
	}

	/**
	 * WUP（wireless uni-protocol）无线统一协议 基于jce编码的命令字(Command)层协议封装
	 * UniPacket实现请求与回应包对象的封装
	 * 
	 * <p>
	 * post {@link UniPacketErrorEvent} to
	 * {@link BaseActivity#onEventMainThread(UniPacketErrorEvent)}
	 * </p>
	 * <p>
	 * post {@link VolleyErrorEvent} to
	 * {@link BaseActivity#onEventMainThread(VolleyErrorEvent)}
	 * </p>
	 * 
	 * @param uniPacket
	 * @param cacheKey
	 * @param responseListener
	 */
	public static void newRequestQueue(String host, final UniPacket uniPacket,
			final String cacheKey, final String tag, final int timeout,
			final Listener responseListener) {
		log(uniPacket, "request host: " + host);
		WupRequest request = new WupRequest(host, uniPacket,
				new Response.Listener<UniPacket>() {
					@Override
					public void onResponse(UniPacket response) {
						log(response, "response");

						if (responseListener == null)
							return;

						try {
							responseListener.onResponse(response);
						} catch (Exception e) {
							if (com.duowan.android.base.BuildConfig.DEBUG) {
								Log.e(TAG, "UniPacket response parse error !",
										e);
							}
							EventBus.getDefault()
									.post(new com.duowan.android.base.event.UniPacketErrorEvent(
											e.getMessage()));
							responseListener.onError(e);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						EventBus.getDefault()
								.post(new com.duowan.android.base.event.VolleyErrorEvent(
										error));
						if (responseListener != null)
							responseListener.onError(error);
					}
				});
		request.setRetryPolicy(new DefaultRetryPolicy(timeout,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		request.setCacheKey(cacheKey);
		request.setTag(tag);
		newRequestQueue(request);
	}

	/**
	 * WUP（wireless uni-protocol）无线统一协议 基于jce编码的命令字(Command)层协议封装
	 * UniPacket实现请求与回应包对象的封装
	 * 
	 * <p>
	 * post {@link UniPacketErrorEvent} to
	 * {@link BaseActivity#onEventMainThread(UniPacketErrorEvent)}
	 * </p>
	 * <p>
	 * post {@link VolleyErrorEvent} to
	 * {@link BaseActivity#onEventMainThread(VolleyErrorEvent)}
	 * </p>
	 * 
	 * @param host
	 * @param uniPacket
	 * @param cacheKey
	 * @param tag
	 * @param timeout
	 * @param cacheHitButRefreshed
	 * @param cacheExpired
	 * @param responseListener
	 */
	public static void newRequestQueue(String host, final UniPacket uniPacket,
			final String cacheKey, final String tag, final int timeout,
			long cacheHitButRefreshed, long cacheExpired,
			final Listener responseListener) {
		log(uniPacket, "request host: " + host);
		WupRequest request = new WupRequest(host, uniPacket,
				new Response.Listener<UniPacket>() {
					@Override
					public void onResponse(UniPacket response) {
						log(response, "response");

						if (responseListener == null)
							return;

						try {
							responseListener.onResponse(response);
						} catch (Exception e) {
							if (com.duowan.android.base.BuildConfig.DEBUG) {
								Log.e(TAG, "UniPacket response parse error !",
										e);
							}
							EventBus.getDefault()
									.post(new com.duowan.android.base.event.UniPacketErrorEvent(
											e.getMessage()));
							responseListener.onError(e);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						EventBus.getDefault()
								.post(new com.duowan.android.base.event.VolleyErrorEvent(
										error));
						if (responseListener != null)
							responseListener.onError(error);
					}
				});
		request.setRetryPolicy(new DefaultRetryPolicy(timeout,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		request.setCacheKey(cacheKey);
		request.setTag(tag);
		request.setCacheHitButRefreshed(cacheHitButRefreshed);
		request.setCacheExpired(cacheExpired);
		newRequestQueue(request);
	}

	public static void newRequestQueue(Request<?> request) {
		RequestQueue queue = getRequestQueue();
		if (queue != null) {
			queue.add(request);
		}
	}

	private static void log(UniPacket uniPacket, String tag) {
		if (com.duowan.android.base.BuildConfig.DEBUG) {
			if (uniPacket == null)
				return;
			try {
				StringBuilder str = new StringBuilder();
				uniPacket.display(str, 0);
				Log.d(TAG, "UniPacket " + tag + ": "
						+ str.toString().replaceAll("\n", ", "));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static interface Listener {
		public void onResponse(UniPacket response);

		public void onError(Exception e);
	}
}
