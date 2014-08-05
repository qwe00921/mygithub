package com.icson.util.activity;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BaseWebView extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		//app.51buy.com/omg 备用，用于服务器主控，客户端不对此类url拦截处理
		if (url.contains("app.51buy.com/omg")) {
			view.getContext().startActivity(
					new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		} else {
			view.loadUrl(url);
		}
		return true;
	}
}
