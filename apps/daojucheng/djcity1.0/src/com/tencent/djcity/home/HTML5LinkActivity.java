package com.tencent.djcity.home;

import com.tencent.djcity.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.NavigationBar.OnLeftButtonClickListener;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.login.ReloginWatcher;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;



/*
 * 该类为APP内嵌HTML5页面提供接口。
 * 支持的接口有：进入商详
 * 
 * modified by qingliang, 2013-03-29
 * 
 */

public class HTML5LinkActivity extends BaseActivity{

	private String LOG_TAG = HTML5LinkActivity.class.getName();
	public static final String LINK_URL = "link_url";
	public static final String ACTIVITY_TITLE = "activity_title";
	public static final String SHOW_NAV_BAR = "show_nav";
	public static final String SHOW_CLOSE = "show_close";
	public static final String ENABLE_ZOOM = "enable_zoom";
	
	private int mVersion;
	private WebView mWebView;
	private NavigationBar mNaviBar;
	private String mOrigUrl;
	
	private Handler mHandler;
	
	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent aintent = getIntent();
		if(null == aintent) {
			finish();
			return;
		}
		
		setContentView(R.layout.html5_link_activity);
		
		mHandler = new MyHandler(this);
				
		mOrigUrl = aintent.getStringExtra(LINK_URL);
		String title = aintent.getStringExtra(ACTIVITY_TITLE);
		
		if(TextUtils.isEmpty(mOrigUrl)) {
			UiUtils.makeToast(this, R.string.params_empty);
			finish();
		}
		
		mNaviBar = (NavigationBar) findViewById(R.id.html5_navbar);
		loadNavBar(R.id.html5_navbar);
		setNavBarText(title);
		

		mNaviBar.setOnLeftButtonClickListener(new OnLeftButtonClickListener(){
			@Override
			public void onClick() {
				
				pressBack();
			}
		});
		
		
		setCookie();
		
		mWebView = (WebView) findViewById(R.id.global_container);
		WebSettings mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setDomStorageEnabled(true);
		mWebSettings.setSupportZoom(true);
		

		mWebView.setOnTouchListener ( new View.OnTouchListener () {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction ()) {
                case MotionEvent.ACTION_DOWN :
                case MotionEvent.ACTION_UP :
                    if (!v.hasFocus ()) {
                       v.requestFocus ();
                    }
                    break ;
                }
                return false ;
			}
        });
		
		mWebView.addJavascriptInterface(new HookInterface(), "touch");
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
		
		mWebView.setDownloadListener(new DownloadListener()
        {
			public void onDownloadStart(String url, String userAgent,String contentDisposition,String mimetype,long contentLength) 
            {	// add by xuemingwang, 2013-07-10
				try{
					if (android.os.Build.VERSION.SDK_INT < 9) {
						// 抛给系统下载
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(url));
						startActivity(intent);
					} else {
						// 抛给DownloadManager下载,要求android2.3+
						DownloadManager downloadManager = (DownloadManager)getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
						DownloadManager.Request down = new DownloadManager.Request(Uri.parse(url));
						down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
						down.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"download_temp.apk");
						downloadManager.enqueue(down);
					}
	            }catch(Exception ex){
	            	Log.e(LOG_TAG, ex);
	            }
            }
        });
				
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.loadUrl(mOrigUrl);
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
	
	
	// add by xuemingwang, 2013-07-10
		//接受下载完成后的intent
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if(downId == -1)
					return ;
				
				String fileUri = null;
				Query myDownloadQuery = new Query();
				myDownloadQuery.setFilterById(downId);
				DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor myDownload = downloadManager.query(myDownloadQuery);
				if (myDownload.moveToFirst()) {
					int fileUriIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
					fileUri = myDownload.getString(fileUriIdx);
				}
				myDownload.close();
				if (fileUri != null) {
					ToolUtil.installApk(HTML5LinkActivity.this, new File(Uri.parse(fileUri).getPath()));
				}
			}
		}
	};

	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		if(null!=mWebView){
			mWebView.setVisibility(View.VISIBLE);
//		
//			int nPreSiteId = getSiteIdFromCookie("yixun.com");
//			int nCurrentSiteId = ILogin.getSiteId();
//			if(0 != nPreSiteId  && nPreSiteId != nCurrentSiteId) {
//				setupWebCookie();
//				mWebView.reload();
//			}
		}
		
		super.onResume();
	}


	protected void onPause()
	{
		if(null!=mWebView)
			mWebView.setVisibility(View.INVISIBLE);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// 避免在activity onDestroy之后继续执行消息 
		if(mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		
		if (receiver != null)
			unregisterReceiver(receiver);
		
		if(null != mWebView)
		{
			ViewGroup pViewGroup = (ViewGroup) mWebView.getParent();
			if(null != pViewGroup) {
				pViewGroup.removeView(mWebView);
				mWebView.destroy();
			}
			mWebView = null;
		}
		
		super.onDestroy();
	}
	
	protected void callTel(String url) {
		Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
		AppUtils.checkAndCall(this,pIntent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			pressBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void pressBack(){
		// Check whether web view can go back.
		if(null == mWebView)
			finish();
		
		
		final boolean bGoBackFlag = true;
		if( (bGoBackFlag) && (null != mWebView) && (mWebView.canGoBack())) {
			mWebView.goBack();
		} else {
			finish();
		}
	}
	

	public final class HookInterface {
		/*
		 * 打开商详页，为app内嵌HTML5页面提供接口
		 * @param long strPropId 道具id
		 */
		public void productItem(final String strPropId) {
			if (mHandler == null){
				mHandler = new MyHandler(HTML5LinkActivity.this);
			}
			
			mHandler.post(new Runnable() {
				public void run() {
					Bundle param = new Bundle();
					param.putString(ItemActivity.KEY_PROP_ID, strPropId);
					
					ToolUtil.startActivity(HTML5LinkActivity.this, ItemActivity.class, param);
				}
			});
		}
		
		

	
	/*
	 * 
	 * 把用户信息转换为JSONObject格式
	 * @return JSONObject
	 * 
	 */
//	private JSONObject accontMsgToJSON(){
//		long uid = ILogin.getLoginUid();
//		String skey = ILogin.getLoginSkey();
//		String token = ILogin.getLoginToken();
//		
//		JSONObject account = new JSONObject();  
//		try{
//			account.put("uid", uid);  
//			account.put("skey", skey);  
//			account.put("token", token);  
//		} catch (JSONException ex) { 
//			Log.e(LOG_TAG, ex);
//		}
//		
//		return account;
//	}

//	private void showDialog() {
//		// 本地存储
//		IShoppingCart.set(mShoppingCartProductModel);
//		// 更新icon
//		ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(this);
//		mShoppingCartCommunication.notifyDataSetChange();
//		if(!isBeenSeen())
//			return;
//		
//		Dialog dialog = UiUtils.showDialog(this, R.string.add_cart_ok, R.string.add_cart_msg, R.string.go_checkout, R.string.not_checkout, new AppDialog.OnClickListener() {
//			@Override
//			public void onDialogClick(int nButtonId) {
//				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
//					MainActivity.startActivity(HTML5LinkActivity.this, MainActivity.TAB_CART);
//					ToolUtil.sendTrack(this.getClass().getName(), strH5AddCartPageId, ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), strH5AddCarLocationId);
//				}
//			}
//		});
//
//		dialog.setCancelable(true);
//		dialog.setCanceledOnTouchOutside(true);
//	}
	}
	
	
	private static class MyHandler extends Handler{
		private final WeakReference<Activity> mActivity;
		
		public MyHandler(Activity pActivity) {
			this.mActivity = new WeakReference<Activity>(pActivity);
		}
		public void handleMessage(Message msg) {     
            super.handleMessage(msg); 
            Bundle bundle = msg.getData();
            String title = bundle.getString("title");
            
            Activity activity = mActivity.get();
            if(activity != null)
            {
            
            	((HTML5LinkActivity) activity).setNavBarText(title);
            }
       } 
		
	}
	
}
