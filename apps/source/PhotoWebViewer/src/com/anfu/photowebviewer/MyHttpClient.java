package com.anfu.photowebviewer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class MyHttpClient {
	private DefaultHttpClient mHttpClient;
	private HttpEntity mHttpEntity;
	private HttpResponse mHttpResponse;
	public static String PHPSESSID;
	private static Cookie s_CurrentCookie;
	public static User s_CurrentUser;

	public MyHttpClient() {
	}
	
	public static void logout() {
		s_CurrentCookie = null;
		PHPSESSID = null;
		s_CurrentUser = null;
	}
	
	public static boolean isSessionExpired() {
		return s_CurrentCookie == null || s_CurrentCookie.isExpired(new Date());
	}

	public String post(String path, List<NameValuePair> params) {
		String ret = "none";
		HttpPost mHttpPost = null;
		try {
			mHttpPost = new HttpPost(path);
			mHttpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			mHttpPost.setEntity(mHttpEntity);
			// 第一次一般是还未被赋值，若有值则将SessionId发给服务器
			if (null != PHPSESSID) {
				mHttpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID);
			}
			if (mHttpClient == null) {
				mHttpClient = new DefaultHttpClient();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			mHttpResponse = mHttpClient.execute(mHttpPost);
			if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = mHttpResponse.getEntity();
				ret = EntityUtils.toString(entity);
				CookieStore mCookieStore = mHttpClient.getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				for (int i = 0; i < cookies.size(); i++) {
					// 这里是读取Cookie['PHPSESSID']的值存在静态变量中，保证每次都是同一个值
					if ("PHPSESSID".equals(cookies.get(i).getName())) {
						PHPSESSID = cookies.get(i).getValue();
						break;
					}

				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
	
	public String get(String path, List<NameValuePair> params) {
		String ret = "none";
		HttpGet mHttpPost = null;
		try {
			mHttpPost = new HttpGet(path);
			mHttpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			// 第一次一般是还未被赋值，若有值则将SessionId发给服务器
			if (null != PHPSESSID) {
				mHttpPost.setHeader("Cookie", "PHPSESSID=" + PHPSESSID);
			}
			if (mHttpClient == null) {
				mHttpClient = new DefaultHttpClient();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			mHttpResponse = mHttpClient.execute(mHttpPost);
			if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = mHttpResponse.getEntity();
				ret = EntityUtils.toString(entity);
				CookieStore mCookieStore = mHttpClient.getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				for (int i = 0; i < cookies.size(); i++) {
					// 这里是读取Cookie['PHPSESSID']的值存在静态变量中，保证每次都是同一个值
					if ("PHPSESSID".equals(cookies.get(i).getName())) {
						s_CurrentCookie = cookies.get(i);
						PHPSESSID = cookies.get(i).getValue();
						break;
					}

				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}
	
	public static void synCookies(Context context, String url) {  
	    CookieSyncManager.createInstance(context);  
	    CookieManager cookieManager = CookieManager.getInstance();  
	    cookieManager.setAcceptCookie(true);  
	    String str = s_CurrentCookie.getName() + "=" + s_CurrentCookie.getValue() + "; domain=" + s_CurrentCookie.getDomain();  
	    cookieManager.setCookie(url, str);//cookies是在HttpClient中获得的cookie  
	    CookieSyncManager.getInstance().sync();  
	} 
}
