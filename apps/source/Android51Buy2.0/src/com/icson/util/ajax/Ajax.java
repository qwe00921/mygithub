package com.icson.util.ajax;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ToolUtil;

/**
 * 主要定义了一些ajax的参数，ajax执行期间需要的监听器，以及这些参数的设置和得到的方法，和监听器的设置和获得的方法。这个类类似于一个简单的javaBean
 * 1.该类的一个主要的方法是send()方法，是ajax发送请求需要调用的方法，该方法会调用AjaxTask类的execute()方法， 从而开启ajax整个生命周期的执行流程。
 */
public class Ajax {

	private final static AtomicInteger mCount = new AtomicInteger(1);

	public final static int GET = 1;

	public final static int POST = 2;

	public final static int STREAM = 3;

	private static final String LOG_TAG =  Ajax.class.getName();

	private HttpRequest mHttpRequest;

	@SuppressWarnings("rawtypes")
	private Parser mParser;

	private String mUrl;

	private AjaxTask mTask;

	private OnBeforeListener mOnBeforeListener;

	@SuppressWarnings("rawtypes")
	private OnSuccessListener mOnSuccessListener;

	private OnErrorListener mOnErrorListener;

	private OnCancelListener mOnCancelListener;

	private OnProgressListener mOnProgressListener;

	private OnFinishListener mOnFinishListener;

	private Response mResponse;
	
	private int ajaxMethod;
	
	private Cookie mRequesetCookie;

	public Ajax(int ajaxMethod) {
		this.ajaxMethod = ajaxMethod;
		final Context context = IcsonApplication.app;
		mHttpRequest = ajaxMethod == POST ? new HttpPost(context) : (ajaxMethod == STREAM ? new HttpStream(context) : new HttpGet(context));
		mResponse = new Response();
		int id = mCount.getAndIncrement();
		setId(id);
	}

	public void setTimeout(int second) {
		if (ajaxMethod == GET) {
			mHttpRequest.setGetDataTimeout(second * 1000);
		} else {
			mHttpRequest.setPostDataTimeout(second * 1000);
		}
	}
	
	public void setTag(Object tag){
		mResponse.setTag(tag);
	}

	@SuppressWarnings("rawtypes")
	public void setParser(Parser parser) {
		mParser = parser;
	}

	@SuppressWarnings("rawtypes")
	public Parser getParser() {
		return mParser;
	}

	public void setOnBeforeListener(OnBeforeListener listener) {
		mOnBeforeListener = listener;
	}

	public void setOnSuccessListener(OnSuccessListener<?> listener) {
		mOnSuccessListener = listener;
	}

	public void setOnSuccessListener(final OnSuccessListener<JSONObject> listener, boolean loginCheck) {
		if (loginCheck) {
			mOnSuccessListener = new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
					if (v.optInt("errno", -1) == 500) {
						ILogin.clearAccount();
						UiUtils.makeToast(IcsonApplication.app, "您已退出登录，请登录后重试.");
						performOnError();
						return;
					}

					listener.onSuccess(v, response);
				}
			};
		} else {
			mOnSuccessListener = listener;
		}
	}

	public void setOnCancelListener(OnCancelListener listener) {
		mOnCancelListener = listener;
	}

	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	public void setOnFinishListener(OnFinishListener listener) {
		mOnFinishListener = listener;
	}

	public void setOnProgressListener(OnProgressListener listener) {
		mOnProgressListener = listener;
	}

	public void setAjaxListener(AjaxListener<?> listener) {
		if (listener == null)
			return;
		setOnBeforeListener(listener);
		setOnSuccessListener(listener);
		setOnErrorListener(listener);
		setOnCancelListener(listener);
		setOnFinishListener(listener);
	}

	public void setCookie(Cookie cookie) {
		mRequesetCookie = cookie;
		mHttpRequest.setCookie(mRequesetCookie);
	}

	public Cookie getRequesetCookie(){
		return mRequesetCookie;
	}
	public void setCookie(String name, String value) {
		if (mRequesetCookie == null){
			mRequesetCookie = new Cookie();
		}
		mRequesetCookie.set(name, value);
		mHttpRequest.setCookie(mRequesetCookie);
	}

	public OnBeforeListener getOnBeforeListener() {
		return mOnBeforeListener;
	}

	public OnSuccessListener<?> getOnSuccessListener() {
		return mOnSuccessListener;
	}

	public OnCancelListener getOnCancelListener() {
		return mOnCancelListener;
	}

	public OnErrorListener getOnErrorListener() {
		return mOnErrorListener;
	}

	public OnFinishListener getOnFinishListener() {
		return mOnFinishListener;
	}

	public OnProgressListener getOnProgressListener() {
		return mOnProgressListener;
	}

	public void setRequestCharset(String charset) {
		mHttpRequest.setRequestCharset(charset);
	}

	public void setResponseDefaultCharset(String charset) {
		mHttpRequest.setResponseDefaultCharset(charset);
	}

	public void setId(int id) {
		mResponse.setId(id);
		mHttpRequest.setId(id);
	}

	public int getId() {
		return mResponse.getId();
	}

	public void setUrl(String url)
	{
		this.setUrl(url, false);
	}
	
	public void setUrl(String strUrl, boolean bPlatform)
	{
		if( bPlatform )
		{
			// Append information for platform.
			final boolean bWithParams = strUrl.contains("?");
			strUrl += ((bWithParams ? "&" : "?") + ("appSource=android&appVersion=" + IcsonApplication.mVersionCode));
		}
		mUrl = strUrl;
		mResponse.setUrl(strUrl);
		mHttpRequest.setUrl(strUrl);
	}

	public String getUrl() {
		return mUrl;
	}
	
	public String getHost()
	{
		if( TextUtils.isEmpty(mUrl) )
			return "";
		
		final String strTag = "://";
		final int nStart = mUrl.indexOf(strTag);
		if( nStart > 0 ){
			final int nOffset = nStart + strTag.length();
			final int nEnd = mUrl.indexOf("/", nOffset);
			return mUrl.substring(nOffset, nEnd);
		}
		
		return "";
	}
	
	public void updateHost(String strOrigin, String strTarget)
	{
		if( (!TextUtils.isEmpty(strOrigin)) && (!TextUtils.isEmpty(strTarget)) )
		{
			mUrl = mUrl.replace(strOrigin, strTarget);
			mResponse.setUrl(mUrl);
			mHttpRequest.setUrl(mUrl);
		}
	}

	public void setRequestHeader(String key, String value) {
		mHttpRequest.setRequestHeader(key, value);
	}

	public void setData(HashMap<String, Object> data) {
		mHttpRequest.setData(data);
	}

	public void setIfModifiedSince(long milliseconds) {
		mHttpRequest.setIfModifiedSince(milliseconds);
	}

	public void setData(String key, Object value) {
		mHttpRequest.setData(key, String.valueOf(value));
	}

	public void setFile(String key, byte[] content) {
		mHttpRequest.setFile(key, content);
	}
	
	public void setFile(String key, byte[] content, String fileName) {
		mHttpRequest.setFile(key, content, fileName);
	}

	public void send() {
		mTask = new AjaxTask(this);
		mTask.execute();
	}

	public void cancel() {
		mTask.cancel();
	}

	public void performOnBefore() {
		if (mOnBeforeListener != null) {
			mOnBeforeListener.onBefore(mResponse);
		}
	}

	public void performOnError() {
		if (mOnErrorListener != null) {
			mOnErrorListener.onError(this, mResponse);
		}
	}

	@SuppressWarnings("unchecked")
	public void performOnSuccess(Object data) {
		if (mOnSuccessListener != null) {
			mOnSuccessListener.onSuccess(data, mResponse);
		}
	}

	public void performOnCancel() {
		if (mOnCancelListener != null) {
			mOnCancelListener.onCancel(mResponse);
		}
	}

	public void performOnProgress(int downLoaded, int totalSize) {
		if (mOnProgressListener != null) {
			mOnProgressListener.onProgress(mResponse, downLoaded, totalSize);
		}
	}

	public void performOnFinish() {
		if (mOnFinishListener != null) {
			mOnFinishListener.onFinish(mResponse);
		}
	}

	HttpRequest getHttpRequest() {
		return mHttpRequest;
	}

	public void abort() {
		mOnBeforeListener = null;
		mOnSuccessListener = null;
		mOnErrorListener = null;
		mOnCancelListener = null;
		mOnFinishListener = null;
		if (null != mTask) {
			mTask.destory();
			mTask = null;
		}
		mResponse = null;
	}

	/**
	 * 主要完成了对response一些参数的值得设置，以及调用OnSuccessListener里面的onSuccess方法的调用
	 * @param content 通过http请求得到的，经过解析的数据
	 */
	void run(Object content) {
		if (!mTask.isError()) {
			try {
				mResponse.setCookie(mHttpRequest.getCookie());
				mResponse.setCharset(mHttpRequest.getCharset());
				mResponse.setResponseHeader(mHttpRequest.getResponseHeader());
				mResponse.setHttpStatus(mHttpRequest.getHttpStatus());
				performOnSuccess(content);
			} catch (Exception ex) {
				Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
				performOnError();
			}
		} else {
			performOnError();
		}

		performOnFinish();
	}

}
