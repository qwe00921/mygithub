package com.niuan.wificonnector.util;

import android.app.AlertDialog;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewUtils {

	public static void loadByWebView(final Context context, String url) {
		WebView webView = new WebView(context);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new Handler(context), "handler");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				// Toast.makeText(context, "网页加载完成", 0).show();
				// view.loadUrl("javascript:window.handler.show(document.body.innerHTML);");
				super.onPageFinished(view, url);
			}
		});
		webView.loadUrl(url);
	}

	public static class Handler {
		private Context mContext;

		public Handler(Context context) {
			mContext = context;
		}

		@JavascriptInterface
		public void show(String data) {
			Toast.makeText(mContext, "执行了handler.show方法", 0).show();
			new AlertDialog.Builder(mContext).setMessage(data).create().show();
		}
	}
}
