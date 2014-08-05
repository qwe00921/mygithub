package com.tencent.djcity.my;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.activity.BaseActivity;

public class MyRoleActivity extends BaseActivity {

	private ImageView mGameIcon;
	private TextView mGameInfo;
	
	private ImageLoader mImageLoader;
	private WebView mWebView;
	private String mUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_role);
		
		initUI();
		this.loadNavBar(R.id.myrole_navbar);
		
		mImageLoader = new ImageLoader(this, Config.CHANNEL_PIC_DIR, true);
		
		setGameInfo();
		
		String strBizCode = "";
		String strAreaID = "";
		String strRoleID = "";
		GameInfo mGameInfo = GameInfo.getGameInfoFromPreference();
		if(null != mGameInfo) {
			strBizCode = mGameInfo.getBizCode();
			
			strAreaID = String.valueOf(mGameInfo.getAreaId());
			
			strRoleID = mGameInfo.getRoleId();
		}
		
		//http://daoju.qq.com/v3/mapp/center.html?biz=lol&area=13&char_no=123412341231
		mUrl = "http://daoju.qq.com/v3/mapp/center.html?biz=" + strBizCode + "&area=" + strAreaID + "&char_no=" + strRoleID;
		
		loadUrl();
	}
	
	private void initUI(){
		mGameIcon = (ImageView) findViewById(R.id.game_icon);
		mGameInfo = (TextView) findViewById(R.id.game_name);
		
		mWebView = (WebView) findViewById(R.id.myrole_webview);

	}
	
	
	private void setGameInfo(){
		String strUrl = null;
		String strGameInfo = "";
		GameInfo info = GameInfo.getGameInfoFromPreference();
		if(info != null) {
			strUrl = info.getBizImg();
			strGameInfo = info.getDescription();
		}
		
		if(TextUtils.isEmpty(strUrl) && TextUtils.isEmpty(strGameInfo)) {
			findViewById(R.id.warehouse_gameinfo).setVisibility(View.GONE);
		}
		
		if(TextUtils.isEmpty(strUrl)) {
			mGameIcon.setVisibility(View.GONE);
		}else{
			final Bitmap data = mImageLoader.get(strUrl);
			if (data != null) {
				mGameIcon.setImageBitmap(data);
				return;
			}
			
			mGameIcon.setImageBitmap(mImageLoader.getLoadingBitmap(this));
			mImageLoader.get(strUrl, new ImageLoadListener() {
				
				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
					mGameIcon.setImageBitmap(aBitmap);
				}
				
				@Override
				public void onError(String strUrl) {
					
				}
			});
		}
		
		mGameInfo.setText(strGameInfo);
	}
	
	private void loadUrl(){
		WebSettings mWebSettings = mWebView.getSettings();
		mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); 
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setDomStorageEnabled(true);
		mWebSettings.setSupportZoom(true);
		
		setCookie();
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showLoadingLayer();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				closeLoadingLayer();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
			}
		});
		
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.loadUrl(mUrl);
		
	}
	
	private void setCookie(){
		CookieSyncManager pManager = CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		
		Account act = ILogin.getActiveAccount();
		
		String strUin = act.getCookieUin();
		String strSkey = act.getSkey();
		String strPSkey = act.getPskey();
		
		cookieManager.setCookie("qq.com", "uin=" + strUin + ";domain=qq.com");
		cookieManager.setCookie("qq.com", "skey=" + strSkey + ";domain=qq.com");
		
		cookieManager.setCookie("game.qq.com", "p_uin=" + strUin + ";domain=game.qq.com");
		cookieManager.setCookie("game.qq.com", "p_skey=" + strPSkey + ";domain=game.qq.com");
		
		pManager.sync();
	}
	

	
	@Override
	protected void onDestroy() {
		mWebView = null;
		mUrl = null;
		
		super.onDestroy();
	}
	
}
