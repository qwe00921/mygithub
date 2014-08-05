package com.icson.login;

import java.util.Date;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.IVersion;
import com.icson.lib.model.Account;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.WebViewActivity;

public class LoginAliPayActivity extends WebViewActivity {
	public static final int FLAG_RESULT_LOGIN_SUCCESS = 1;
	private Handler mHandler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_alipay_activity);
		this.loadNavBar(R.id.login_ali_navigation_bar);

		setWebView(R.id.global_container);
		getWebView().addJavascriptInterface(new HookInterface(), "hook");
		getWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		String strInfo = "&_=" + ToolUtil.getCurrentTime()+"&vcode="+IVersion.getVersionCode();
		loadUrl(ServiceConfig.getUrl(Config.URL_ALIPAY_LOGIN, strInfo));
		getWebView().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				getWebView().requestFocus();
				return false;
			}
		});
	}

	@Override
	public boolean getWebViewCanGoBack() {
		return false;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		showLoadingLayer();
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		closeLoadingLayer();
	}

	public final class HookInterface {
		public void setLoginInfo(final String uid, final String sKey, final String token) {
			mHandler.post(new Runnable() {
				public void run() {
					Account account = new Account();
					account.setUid(Long.valueOf(uid));
					account.setType(Account.TYPE_ALI);
					account.setSkey(sKey);
					account.setNickName("");
					account.setToken(token);
					account.setRowCreateTime(new Date().getTime());
					ILogin.setActiveAccount(account);
					ILogin.saveIdentity(account);
					setResult(FLAG_RESULT_LOGIN_SUCCESS);
					finish();
				}
			});
		}
	}

	public String getActivityPageId() {
		return getString(R.string.tag_LoginAliPayActivity);
	}
}
