package com.yy.android.gamenews.util;

import android.view.View;
import android.webkit.WebView;

public class WebViewCacheUtil {
	/**
	 * http://mtq.yy.com/#game.index?gid=383
	 * http://mtq.yy.com/#detail.index?tid=14487&type=issue
	 * 
	 * 游戏刷子预加载地址
	 * 
	 * @param webView
	 * @param url
	 */
	public static void webViewCache(WebView webView, String url) {
		webView.setVisibility(View.GONE);
		webView.loadUrl(url);
	}

}
