package com.icson.lib.pay.cft;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class CFTPayActivity extends BaseActivity {

	public static final int REQUEST_CFT_PAY = 100008;
	public static final String CFT_PAY_URL = "cft_pay_url";
	private WebView webView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tenpay);
		
		// Load navigation bar.
		this.loadNavBar(R.id.tenpay_navbar);
		
		//支付的wap页面
		String url = getIntent().getStringExtra(CFT_PAY_URL);
		
		webView = (WebView)findViewById(R.id.global_container);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				//icson://pay?type=cft 代表支付成功
				if (url.contains("icson://pay?type=cft")) {
					setResult(RESULT_OK);
					finish();
				} else {
					view.loadUrl(url);
				}
				return true;
			}
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showLoadingLayer();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				closeLoadingLayer();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			}
		});
		
		webView.loadUrl(url);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) ) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy()
	{
		if(null != webView)
		{
			ViewGroup pViewGroup = (ViewGroup) webView.getParent();
			if(null != pViewGroup) {
				pViewGroup.removeView(webView);
				webView.destroy();
			}
			webView = null;
		}
		super.onDestroy();
	}
	
	@Override
	public String getActivityPageId() {
		return "000000";
	}
}
