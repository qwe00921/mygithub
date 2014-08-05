package com.icson.home;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

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

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.item.ItemProductModel;
import com.icson.item.ItemProductParser;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IShoppingCart;
import com.icson.lib.model.SearchModel;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.NavigationBar;
import com.icson.lib.ui.NavigationBar.OnNavBackListener;
import com.icson.lib.ui.UiUtils;
import com.icson.list.ListActivity;
import com.icson.login.LoginActivity;
import com.icson.login.LoginQQActivity;
import com.icson.login.ReloginWatcher;
import com.icson.main.MainActivity;
import com.icson.order.OrderConfirmActivity;
import com.icson.preference.Preference;
import com.icson.shoppingcart.ESShoppingCartActivity;
import com.icson.shoppingcart.ShoppingCartActivity;
import com.icson.shoppingcart.ShoppingCartCommunication;
import com.icson.statistics.StatisticsEngine;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

/*
 * 该类为APP内嵌HTML5页面提供接口。
 * 支持的接口有：进入商详，拉取购物车，加入购物车，立即购买，登录，设置标题, 分享，打电话。
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
	
	private long mUid;
	private String mSkey;
	private String mToken;
	private String mGuid;
	private int mVersion;
	private int mWsid;
	private WebView mWebView;
	private Ajax mAjax;
	private NavigationBar mNaviBar;
	private String mOrigUrl;


	private ItemProductModel mItemProductModel;
	private ShoppingCartProductModel mShoppingCartProductModel;
	private String pageId;

	// 立即购买且有促销规则
	private boolean isbuyImmediately_Rules = false;
	private int mBuyNum;
	int channelID = 0;
	private String strH5AddCartPageId;
	private String strH5AddCarLocationId;
	
	private static final int AJAX_ADDCART = 1;
	private static final int AJAX_BUYNOW = 2;
	private static final int REQUEST_FLAG_ADD_CART = 1;
	private static final int REQUEST_FLAG_LOGIN = 2;
	private static final int REQUEST_FLAG_LOGIN_RELOAD = 3;
	
	private Handler mHandler;
	private String mH5Tag;   //H5返回的tag
	private String mH5DAP;	//H5返回的dap
	private String mH5CPPTSS;	//H5返回的cp-ptss
	private String mH5YTrack;	//H5返回的ytrack
	private Pattern mPattern;
	
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
				
		isReportPV = false;
		mOrigUrl = aintent.getStringExtra(LINK_URL);
		String title = aintent.getStringExtra(ACTIVITY_TITLE);
		boolean showNav = aintent.getBooleanExtra(SHOW_NAV_BAR, true);
		boolean enableZoom = aintent.getBooleanExtra(ENABLE_ZOOM, true);
		pageId = getString(R.string.tag_HTML5LinkActivity);
		
		if(TextUtils.isEmpty(mOrigUrl)) {
			UiUtils.makeToast(this, R.string.params_empty);
			finish();
		}
		
		mNaviBar = (NavigationBar) findViewById(R.id.html5_navbar);
		if (showNav) {
			mNaviBar.setVisibility(View.VISIBLE);
			loadNavBar(R.id.html5_navbar);
			setNavBarText(title);
		} else {
			mNaviBar.setVisibility(View.GONE);
		}

		mNaviBar.setOnNavBackListener(new OnNavBackListener(){
			@Override
			public void onNavBackClick() {
				ToolUtil.reportStatisticsClick(getActivityPageId(), "19999");
				pressBack();
			}
		});
		
		boolean showClose = aintent.getBooleanExtra(SHOW_CLOSE, false);
		if (showClose) {
			mNaviBar.setRightInfo("关闭", new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}
		
		setupWebCookie();
		
		mWebView = (WebView) findViewById(R.id.global_container);
		WebSettings mWebSettings = mWebView.getSettings();
		mWebSettings.setBuiltInZoomControls(enableZoom);
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setDomStorageEnabled(true);
		mWebSettings.setSupportZoom(true);
		
		//handle UserAgent for data upload
		String strUserAgent = mWebSettings.getUserAgentString();
		String strExtra = "Yixun/Android/" + mVersion;
		if(TextUtils.isEmpty(strUserAgent) || (null != strUserAgent && !strUserAgent.contains(strExtra))) {
			strUserAgent = strUserAgent + " " + strExtra;
			mWebSettings.setUserAgentString(strUserAgent);
		}
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
		
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		mWebView.addJavascriptInterface(new HookInterface(), "touch");
		mWebView.setWebViewClient(new WebViewClient() {
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("tel:")){
					callTel(url);
					return true;
				} else if( url.startsWith("wap2app:") ) {
					HomeActivity.processUrlInfo(HTML5LinkActivity.this, Activity.RESULT_OK, url, false);
					return true;
				}else if(url.startsWith("icson://copyString?"))
				{
					String copyStr = url.substring(("icson://copyString?").length());
					String strKey = copyStr.substring(copyStr.indexOf("=")+1); 
					ClipboardManager mCm = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
					mCm.setText(strKey);
					UiUtils.makeToast(HTML5LinkActivity.this,R.string.preferences_copy_to_clipboard_title);
					return true;
				}else if(url.startsWith("yixunapp://back"))
				{
					pressBack();
					return true;
				}
				else if(url.startsWith("yixunapp://qqLogin"))
				{
					ILogin.clearAccount();

					// Log request for logout.
					StatisticsEngine.updateInfo(ILogin.getLoginUid(), 2);
					
					AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
					
					ToolUtil.startActivity(HTML5LinkActivity.this, LoginQQActivity.class, null, HTML5LinkActivity.REQUEST_FLAG_LOGIN_RELOAD);
					//ToolUtil.sendTrack(this.getClass().getName(), pageId, LoginActivity.class.getName(), getString(R.string.tag_LoginActivity), "05012");

					return true;
				}
				if(null != view) {
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
		
		mWebView.setWebChromeClient(new WebChromeClient(){
			/*
			 * override javaScript funtion: alert
			 */
			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				if(HTML5LinkActivity.this.isBeenSeen())
				{
					Dialog pDialog = UiUtils.showDialog(view.getContext(), getString(R.string.caption_hint), message, R.string.btn_ok, new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						result.confirm();
					}
					});
				
					pDialog.setCancelable(false);
				}
				return true;
			}

			@Override
			public boolean onJsBeforeUnload(WebView view, String url,
					String message, JsResult result) {
				return super.onJsBeforeUnload(view, url, message, result);
			}

			/*
			 * override javaScript funtion: confirm
			 */
			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				if(HTML5LinkActivity.this.isBeenSeen())
				{
				Dialog pDialog = UiUtils.showDialog(view.getContext(), getString(R.string.caption_hint), message, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if( AppDialog.BUTTON_POSITIVE == nButtonId )
							result.confirm();
						else if( AppDialog.BUTTON_NEGATIVE == nButtonId )
							result.cancel();
					}
				});
				
				pDialog.setCancelable(false);
				}
				return true;
			}

			/*
			 * override javaScript funtion: prompt
			 */
			@Override
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, final JsPromptResult result) {
				final EditText pText = new EditText(view.getContext());
				pText.setSingleLine();
				pText.setText(defaultValue);
				if(HTML5LinkActivity.this.isBeenSeen())
				{
				Dialog pDialog = UiUtils.showDialog(view.getContext(), getString(R.string.caption_hint), message, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if( AppDialog.BUTTON_POSITIVE == nButtonId ) {
							result.confirm(pText.getText().toString());
						} else if ( AppDialog.BUTTON_NEGATIVE == nButtonId ) {
							result.cancel();
						}
					}
				});
				
				pDialog.setCancelable(false);
				}
				return true;
			}
		});
		
		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.loadUrl(mOrigUrl);
	}
	
	
	private void setupWebCookie() {
		mUid = ILogin.getLoginUid();					//User Id
		mSkey = ILogin.getLoginSkey();					//skey
		mToken = ILogin.getLoginToken();				//token
		mVersion = IcsonApplication.mVersionCode;		//App version
		mWsid = ILogin.getSiteId();						// Site Id
		mGuid = StatisticsUtils.getDeviceUid(this);		//Device id
		String strUin = ToolUtil.getUinForReport();
		String strTag = IcsonApplication.getTag();
		String strYTrack = IcsonApplication.getPageRoute();
		int nDistrictId = FullDistrictHelper.getDistrictId();
		
		String qq = Preference.getInstance().getQQAccount();
		String skey = "";
		String lskey = "";
		if (!TextUtils.isEmpty(qq)) {
			skey = ReloginWatcher.getInstance(this).getSkeyByLocalSig(qq);
			lskey = ReloginWatcher.getInstance(this).getLskeyByLocalSig(qq);
		}

		CookieSyncManager pManager = CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		
		//兼容android2.3以下的版本
		cookieManager.setCookie("51buy.com", "uid=" + mUid + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "skey=" + mSkey + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "token=" + mToken + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "version=" + mVersion + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "wsid=" + mWsid + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "guid=" + mGuid + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "districtid=" + nDistrictId + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "uin=" + strUin + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "whid=1990;domain=51buy.com");
		cookieManager.setCookie("51buy.com", "tag=" + strTag + ";domain=51buy.com");
		cookieManager.setCookie("51buy.com", "ytrack=" + strYTrack + ";domain=51buy.com");
		
		cookieManager.setCookie("yixun.com", "uid=" + mUid + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "skey=" + mSkey + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "token=" + mToken + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "version=" + mVersion + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "wsid=" + mWsid + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "guid=" + mGuid + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "districtid=" + nDistrictId + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "uin=" + strUin + ";domain=yixun.com");
		cookieManager.setCookie("yixun.com", "whid=1990;domain=yixun.com");
		cookieManager.setCookie("51buy.com", "tag=" + strTag + ";domain=yixun.com");
		cookieManager.setCookie("51buy.com", "ytrack=" + strYTrack + ";domain=yixun.com");
		
		cookieManager.setCookie("qq.com", "luin=" + qq + ";domain=qq.com");
		cookieManager.setCookie("qq.com", "uin=" + qq + ";domain=qq.com");
		cookieManager.setCookie("qq.com", "skey=" + skey + ";domain=qq.com");
		cookieManager.setCookie("qq.com", "lskey=" + lskey + ";domain=qq.com");
		
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
		
			int nPreSiteId = getSiteIdFromCookie("yixun.com");
			int nCurrentSiteId = ILogin.getSiteId();
			if(0 != nPreSiteId  && nPreSiteId != nCurrentSiteId) {
				setupWebCookie();
				mWebView.reload();
			}
		}
		
		super.onResume();
	}

	private int getSiteIdFromCookie(String url) {
		int nSiteId = 0;
		String strSite = "wsid";
		
		if(!TextUtils.isEmpty(url)){
			CookieManager cookieManager = CookieManager.getInstance();
			String strCookie = cookieManager.getCookie(url);
			
			if(null != strCookie && strCookie.contains(strSite)) {
				String [] arr = strCookie.split(";");
				for(String str : arr) {
					if(str.contains(strSite)) {
						int nStart = str.indexOf(strSite) + strSite.length() + 1;
						String strSiteID = str.substring(nStart);
						if(!TextUtils.isEmpty(strSiteID) && TextUtils.isDigitsOnly(strSiteID)) {
							nSiteId = Integer.parseInt(strSiteID);
						}
						break;
					}
				}
			}
		}
		
		return nSiteId;
		
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
			ToolUtil.reportStatisticsClick(getActivityPageId(), "19999");
			pressBack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private void pressBack(){
		String url= "";
		// Check whether web view can go back.
		if(null == mWebView)
			finish();
		
		url = mWebView.getUrl();
		if (null != url && url.contains("#item=getAllItemJson")) { //hacked home page
			finish();
			return;
		}
		else if(null!=url && url.contains("return=index"))
		{
			mWebView.clearHistory();
			mWebView.loadUrl(mOrigUrl);
			return;
		}
		
		String strBackTag = AppStorage.getData(AppStorage.SCOPE_WAP, AppStorage.KEY_WAP_BACK);
		final boolean bGoBackFlag = !TextUtils.isEmpty(strBackTag) && strBackTag.equals("1");
		if( (bGoBackFlag) && (null != mWebView) && (mWebView.canGoBack())) {
			mWebView.goBack();
			
		} else {
			finish();
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_FLAG_ADD_CART:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				addToShoppingCart(mItemProductModel, mBuyNum);
			}
			break;
			
		case REQUEST_FLAG_LOGIN:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				JSONObject accountMsg = accontMsgToJSON();
				if(null != mWebView) {
					mWebView.loadUrl("javascript:loginCallBack(" + accountMsg + ")");
				}
			}
			break;
		case REQUEST_FLAG_LOGIN_RELOAD:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				setupWebCookie();
				if(null != mWebView) {
					mWebView.reload();
					//mWebView.loadUrl(mOrigUrl);
				}
			}
			break;
		}
	}

	public final class HookInterface {
		/*
		 * 打开商详页，为app内嵌HTML5页面提供接口
		 * @param long productId 商品id
		 * @param int channelId 渠道id
		 */
		public void productItem(final long productId, final int channelId) {
			productItem(productId, channelId, pageId, "05010");
		}
		
		/*
		 * 打开商详页，为app内嵌HTML5页面提供接口
		 * @param long productId 商品id
		 * @param int channelId 渠道id
		 * @param String strPageId 页面id
		 * @param String strLocationId 位置id
		 */
		public void productItem(final long productId, final int channelId, final String strPageId, final String strLocationId) {
			if (mHandler == null){
				mHandler = new MyHandler(HTML5LinkActivity.this);
			}
			mHandler.post(new Runnable() {
				public void run() {
					Bundle param = new Bundle();
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID, productId);
					param.putInt(ItemActivity.REQUEST_CHANNEL_ID, channelId);

					ToolUtil.startActivity(HTML5LinkActivity.this, ItemActivity.class, param);
					ToolUtil.sendTrack(this.getClass().getName(), strPageId, ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), strLocationId, String.valueOf(productId));
				}
			});
		}
		
		
		public void productItem(final String productId, final String channelId, final String exInfo) {
			if (mHandler == null){
				mHandler = new MyHandler(HTML5LinkActivity.this);
			}
			mHandler.post(new Runnable() {
				public void run() {
					handleH5ExInfo(exInfo);
					updateTagAndYTrack(mH5Tag, mH5YTrack);
					
					Bundle param = new Bundle();
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID, Long.valueOf(productId));
					param.putInt(ItemActivity.REQUEST_CHANNEL_ID, Integer.valueOf(channelId));
					param.putString(ItemActivity.REQUEST_DAP, mH5DAP);

					ToolUtil.startActivity(HTML5LinkActivity.this, ItemActivity.class, param);
				}
			});
		}

		/*
		 * 打开购物车，为app内嵌HTML5页面提供接口
		 */
		public void cart() {
			if (mHandler == null){
				mHandler = new Handler();
			}
			mHandler.post(new Runnable() {
				public void run() {
					ShoppingCartActivity.loadShoppingCart(HTML5LinkActivity.this, true, true);
					ToolUtil.sendTrack(this.getClass().getName(), pageId, ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "05011");
				}
			});
		}
		
		public void cart(final String exInfo) {
			if (mHandler == null){
				mHandler = new Handler();
			}
			mHandler.post(new Runnable() {
				public void run() {
					handleH5ExInfo(exInfo);
					updateTagAndYTrack(mH5Tag, mH5YTrack);
					
					ShoppingCartActivity.loadShoppingCart(HTML5LinkActivity.this, true, true);
				}
			});
		}
		
		public void addCart(final long productId, final int num, final int channelId) {
			addCart(productId, num, channelId, pageId, "0519");
		}
		
		/*
		 * 加入购物车,为app内嵌HTML5页面提供接口
		 * @param long productId 商品id
		 * @param int num 购买数量
		 * @param int channelId 渠道id
		 * @param String strPageId 页面id
		 * @param String strLocationId 位置id
		 */
		public void addCart(final long productId, final int num, final int channelId, final String strPageId, final String strLocationId ) {
			channelID = channelId;
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					strH5AddCartPageId = strPageId;
					strH5AddCarLocationId = strLocationId;
					execute(productId, num, channelId, AJAX_ADDCART);
				}
			});
		}
		
		public void addCart(final String productId, final String num, final String channelId, final String exInfo) {
			channelID = Integer.valueOf(channelId);
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					handleH5ExInfo(exInfo);
					updateTagAndYTrack(mH5Tag, mH5YTrack);
					execute(Long.valueOf(productId), Integer.valueOf(num), channelID, AJAX_ADDCART);
				}
			});
		}
		
		private void execute(long productId, int num, int channelId, int nAjaxId) {
			if (mItemProductModel == null){
				mItemProductModel = new ItemProductModel();
			}

			if(num <= 0) {
				mBuyNum = 1;
			}
			mBuyNum = num;
			
			String strInfo = "&pid=" + productId;
			if(channelId > 0){//渠道id
				strInfo += "&channelId=" + channelId;
			}
			//配送时间相关
			strInfo += "&provinceid=" + FullDistrictHelper.getProvinceIPId();
			strInfo += "&district=" + FullDistrictHelper.getDistrictId();
			
			mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_DETAIL, strInfo);
			if( null == mAjax )
				return ;
			
			mAjax.setId(nAjaxId);
			sendRequest(productId, channelId);
		}
		
		/*
		 * 立即购买,为app内嵌HTML5页面提供接口
		 * @param long productId 商品id
		 * @param int num 购买数量
		 * @param int channelId 渠道id
		 * 
		 */
		public void buy(final long productId, final int num, final int channelId){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					execute(productId, num, channelId, AJAX_BUYNOW);
				}
			});
		}
		
		public void buy(final String productId, final String num, final String channelId, final String exInfo){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					handleH5ExInfo(exInfo);
					updateTagAndYTrack(mH5Tag, mH5YTrack);
					execute(Long.valueOf(productId), Integer.valueOf(num), Integer.valueOf(channelId), AJAX_BUYNOW);
				}
			});
		}
		
		/*
		 * 登录接口,为app内嵌HTML5页面提供接口
		 * 若用户已经登录，则执行javascript函数，函数参数：JSONObject（uid，skey，token）；
		 * 否则，帮用户登录，执行javascript函数，函数参数：JSONObject（uid，skey，token）。
		 * 
		 */
		public void login(){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					if (ILogin.getLoginUid() == 0) {
						UiUtils.makeToast(HTML5LinkActivity.this, R.string.need_login);
						ToolUtil.startActivity(HTML5LinkActivity.this, LoginActivity.class, null, HTML5LinkActivity.REQUEST_FLAG_LOGIN);
						ToolUtil.sendTrack(this.getClass().getName(), pageId, LoginActivity.class.getName(), getString(R.string.tag_LoginActivity), "05012");
						return;
					}
					
					JSONObject accountMsg = accontMsgToJSON();
					if(null != mWebView) {
						mWebView.loadUrl("javascript:loginCallBack(" + accountMsg + ")");
					}
				}
				
			});
		}
		
		public void login(final String exInfo){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					if (ILogin.getLoginUid() == 0) {
						handleH5ExInfo(exInfo);
						updateTagAndYTrack(mH5Tag, mH5YTrack);
						
						UiUtils.makeToast(HTML5LinkActivity.this, R.string.need_login);
						ToolUtil.startActivity(HTML5LinkActivity.this, LoginActivity.class, null, HTML5LinkActivity.REQUEST_FLAG_LOGIN);
						
						return;
					}
					
					JSONObject accountMsg = accontMsgToJSON();
					if(null != mWebView) {
						mWebView.loadUrl("javascript:loginCallBack(" + accountMsg + ")");
					}
				}
				
			});
		}
		
		
		/*
		 * 打开搜索列表页接口，为app内嵌HTML5页面提供接口
		 * 
		 * @param String key  关键字
		 * @param String path 导航id路径
		 * @param String classid  品类id
		 * @param int sort 排序方式
		 * @param int page 第几页
		 * @param String price 价格区间
		 * @param String attrinfo 属性过滤
		 * @param int areacode 仓库地区代码
		 * @param int channelId 用于指定多价的商品
		 * 
		 * added by marcoyao  2013.10.12
 		 */
		public void searchList(final String key,final String path,final String classid,final int sort,
				final int page,final String price,final String attrinfo,final int areacode,final int channelId)
		{
			if( TextUtils.isEmpty(key) &&
				TextUtils.isEmpty(path)&&
				TextUtils.isEmpty(classid)
				)
			{
				return ;
			}
			SearchModel model = new SearchModel();
			if(!TextUtils.isEmpty(key))
				model.setKeyWord(key);
			
			if(!TextUtils.isEmpty(path))
				model.setPath(path);
			
			if(!TextUtils.isEmpty(classid))
				model.setClassId(classid);
			
			if(sort >=0 )
				model.setSort(sort);
			
			if(page >= 0)
				model.setCurrentPage(page);
			
			if(!TextUtils.isEmpty(price))
				model.setPrice(price);
			
			if(!TextUtils.isEmpty(attrinfo))
				model.setOption(attrinfo);
			
			if(areacode > 0)
				model.setAreaCode(areacode);	
			
			Bundle param = new Bundle();
			param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, model);
			param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, key);
			ToolUtil.startActivity(HTML5LinkActivity.this, ListActivity.class, param);
		}
		
		public void searchList(final String searchInfo, final String exInfo)
		{
			if(TextUtils.isEmpty(searchInfo)) {
				return;
			}
			
			String str = URLDecoder.decode(searchInfo);
			JSONObject json = null;
			try {
				 json = new JSONObject(str);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String key = json.optString("key", "");
			String path = json.optString("path", "");
			String classid = json.optString("classid", "");
			String sort = json.optString("sort");
			String page = json.optString("page");
			String price = json.optString("price", "");
			String attrinfo = json.optString("attrinfo", "");
			String areacode = json.optString("areacode");
			if( TextUtils.isEmpty(key) &&
				TextUtils.isEmpty(path)&&
				TextUtils.isEmpty(classid)
				)
			{
				return ;
			}
			SearchModel model = new SearchModel();
			if(!TextUtils.isEmpty(key))
				model.setKeyWord(key);
			
			if(!TextUtils.isEmpty(path))
				model.setPath(path);
			
			if(!TextUtils.isEmpty(classid))
				model.setClassId(classid);
			
			if(!TextUtils.isEmpty(sort) )
				model.setSort(Integer.valueOf(sort));
			
			if(!TextUtils.isEmpty(page) )
				model.setSort(Integer.valueOf(page));
			
			if(!TextUtils.isEmpty(price))
				model.setPrice(price);
			
			if(!TextUtils.isEmpty(attrinfo))
				model.setOption(attrinfo);
			
			if(!TextUtils.isEmpty(areacode) )
				model.setSort(Integer.valueOf(areacode));
			
			handleH5ExInfo(exInfo);
			updateTagAndYTrack(mH5Tag, mH5YTrack);
			
			Bundle param = new Bundle();
			param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, model);
			param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, key);
			ToolUtil.startActivity(HTML5LinkActivity.this, ListActivity.class, param);
		}
		
		
		/*
		 * 设置该Activity标题,为app内嵌HTML5页面提供接口
		 * @param String title 标题
		 * 
		 */
		public void setTitle(final String title){
			String activityTitle = title;
			if (mHandler == null){
				mHandler = new Handler();
			}
			
			if(activityTitle == null || activityTitle.length() == 0){
				activityTitle = "易迅网";
			}
			
			Message message = Message.obtain();
			Bundle bundle = new Bundle();
			bundle.putString("title", activityTitle);
			message.setData(bundle);
			mHandler.sendMessage(message);
		}
		
		/*
		 * 为HTML5 页面提供通用获取数据接口
		 */
		public void getData(final String strScope, final String strKey){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					String strValue = AppStorage.getData(strScope, strKey);
					JSONObject json = new JSONObject();
					try {
						json.put(strKey, strValue);
					} catch (JSONException ex) {
						ex.printStackTrace();
					}
					
					if(null != mWebView) {
						mWebView.loadUrl("javascript:getDataCallBack(" + json + ")");
					}
				}
			});
		}
		
		/*
		 * 为HTML5 页面提供通用获取数据接口
		 */
		public void getData(final String strKey){
			getData(null, strKey);
		}
		
		/*
		 * 为HTML5 页面提供通用设置接口
		 */
		public void setData(final String strScope, final String strKey, final String strVal, final boolean bPermanent){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					AppStorage.setData(strScope, strKey, strVal, bPermanent);
				}
			});
		}
		
		/*
		 * 为HTML5 页面提供通用设置接口
		 */
		public void setData(final String strKey, final String strVal, final boolean bPermanent){
			setData(null, strKey, strVal, bPermanent);
		}
		
		/*
		 * 为HTML5 页面提供分享接口
		 * @param String strPicUrl: picture url
		 * @param String strLinkUrl:link url
		 * @param String strTitle: title
		 * @param String strDesc: description
		 */
		public void share(final String strPicUrl, int picWidth, int picHeight, final String strLinkUrl, final String strTitle, final String strDesc){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					if(HTML5LinkActivity.this.isBeenSeen())
					{
						if(AppUtils.checkWX(HTML5LinkActivity.this))
						{
							AppUtils.shareSlotInfo(HTML5LinkActivity.this, strTitle, strLinkUrl, strPicUrl, new AppUtils.DescProvider() {
						//AppUtils.shareAppInfo(HTML5LinkActivity.this, strTitle, strLinkUrl, strPicUrl, new AppUtils.DescProvider() {
								@Override
								public String getDesc(String strPackageName) {
									if( (TextUtils.isEmpty(strPackageName)) )
										return "";
							
									String strContent = null;
									if(strPackageName.equals("com.tencent.mm")) {
										strContent = strDesc;
									} else {
										strContent = "【" + strTitle + "】" + strDesc + strLinkUrl;
									}
									return strContent;
								}
							});
						}//end of checkWX
					}
				}
			});
		}
		
		/**
		 * 为HTML5 页面提供拨打电话接口
		 * @param String strPhoneNum: phone number
		 */
		public void phone(final String strPhoneNum){
			if (mHandler == null){
				mHandler = new Handler();
			}
				
			mHandler.post(new Runnable() {
				public void run() {
					Intent pIntent = new Intent(Intent.ACTION_DIAL,  Uri.parse("tel:" + strPhoneNum));
					AppUtils.checkAndCall(HTML5LinkActivity.this,pIntent);
				}
			});
		}
		
	}
	
	private void handleH5ExInfo(String exInfo){
		if(TextUtils.isEmpty(exInfo)) {
			return;
		}
		
		String strExInfo = URLDecoder.decode(exInfo);
		try {
			JSONObject json = new JSONObject(strExInfo);
			mH5Tag = json.optString("tag", "");
			mH5DAP = json.optString("dap", "");
			mH5CPPTSS = json.optString("cp-ptss", "");
			mH5YTrack = json.optString("ytrack", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void updateTagAndYTrack(String tag, String ytrack) {
		//update tag
		if(!TextUtils.isEmpty(tag)) {
			IcsonApplication.setTag(tag);
		}
		
		//check ytrack is right or not
		if(isYTrack(ytrack)) {
			String index0 = "0";
			String index1 = "0";
			String index2 = "0";
			String index3 = "0";
			String pLevel = "0";
			
			String[] parts = ytrack.split("\\.");
			pLevel = parts[1];
			
			String[] indexs = parts[0].split("-");
			index0 = indexs[0];
			index1 = indexs[1];
			index2 = indexs[2];
			index3 = indexs[3];
			
			//update ytrack
			IcsonApplication.setPageRoute(index0, index1, index2, index3, pLevel);
		}
	}
	
	private boolean isYTrack(String ytrack){
		if(TextUtils.isEmpty(ytrack)) {
			return false;
		}
		
		if(mPattern == null) {
			mPattern = Pattern.compile("(\\d+)-(\\d+)-(\\d+)-(\\d+)\\.\\d");
		}
		
		Matcher matcher = mPattern.matcher(ytrack);
		if(matcher.find()) {
			return true;
		}
		
		return false;
	}
	/*
	 * 拉取商品详情
	 * @param int productId 商品id
	 * @param int channelId 渠道id
	 * 
	 */
	public void sendRequest(final long productId, final int channelId) {
		showProgressLayer();
		mAjax.setOnSuccessListener(new OnSuccessListener<ItemProductModel>()
		{
			@Override
			public void onSuccess(ItemProductModel v, Response response) 
			{
				closeProgressLayer();
				
				if(null == v){
					closeLoadingLayer(true);
					UiUtils.makeToast(HTML5LinkActivity.this, Config.NORMAL_ERROR);
					return;
				}
				
				mItemProductModel = v;
				if(mAjax.getId() == AJAX_ADDCART)
				{
					isbuyImmediately_Rules = false;
					addToShoppingCart(mItemProductModel, mBuyNum);
				}
				else if(mAjax.getId() == AJAX_BUYNOW)
				{
					if(mItemProductModel.isESProduct())
					{
						//节能补贴商品，进入节能补贴活动选择页面
						addToESShoppingCart(mItemProductModel, mBuyNum);
					}
					else if(mItemProductModel.getRules() == 0)
					{
						//如果没有促销活动，那么立即购买
						if(mItemProductModel.getLowestNum()!=0 && mBuyNum < mItemProductModel.getLowestNum())
						{
							UiUtils.makeToast(HTML5LinkActivity.this, "本商品" + mItemProductModel.getLowestNum() + "件起售");
							return ;
						}
						
						Bundle param = new Bundle();
						param.putLong(OrderConfirmActivity.REQUEST_PRODUCT_ID, mItemProductModel.getProductId());
						param.putInt(OrderConfirmActivity.REQUEST_PRODUCT_BUYNUM, mBuyNum);

						//场景多价
						param.putInt(OrderConfirmActivity.REQUEST_CHANNEL_ID, channelID);
						ToolUtil.checkLoginOrRedirect(HTML5LinkActivity.this, OrderConfirmActivity.class, param, -1);
					
					}else
					{//如果有促销规则，那么进入购物车
						isbuyImmediately_Rules = true;
						addToShoppingCart(mItemProductModel, mBuyNum);
					}
				}
			}
		});
		mAjax.setParser(new ItemProductParser());
		mAjax.setOnErrorListener(new OnErrorListener()
		{
			@Override
			public void onError(Ajax ajax, Response response) 
			{
				closeProgressLayer();
				UiUtils.makeToast(HTML5LinkActivity.this, Config.NET_RROR);
			}
		});
		mAjax.send();
	}

	/*
	 * 加入购物车
	 * @param ItemProductModel mItemProductModel 商品
	 * @param int num 购买数量
	 */
	public void addToShoppingCart(final ItemProductModel mItemProductModel, final int num) {
		if (ILogin.getLoginUid() == 0) {
			UiUtils.makeToast(this, R.string.need_login);
			ToolUtil.startActivity(this, LoginActivity.class, null, HTML5LinkActivity.REQUEST_FLAG_ADD_CART);
			ToolUtil.sendTrack(this.getClass().getName(), pageId, LoginActivity.class.getName(), getString(R.string.tag_LoginActivity), "05013");
			return ;
		}

		if(mItemProductModel.getLowestNum()!=0 && num < mItemProductModel.getLowestNum()){
			UiUtils.makeToast(this, "本商品"+mItemProductModel.getLowestNum()+"件起售");
			return ;
		}
		//节能补贴商品
		if(mItemProductModel.isESProduct()){
			addToESShoppingCart(mItemProductModel, num);
			return ;
		}
		int haveAdd = IShoppingCart.getBuyCount(mItemProductModel.getProductId());

		int wantBuyCount = haveAdd + num;

		if (mItemProductModel.getNumLimit() != 0 && wantBuyCount > mItemProductModel.getNumLimit()) {
			UiUtils.makeToast(this, this.getString(R.string.buy_max_msg, mItemProductModel.getNumLimit(),haveAdd));
			return;
		}
		mShoppingCartProductModel = new ShoppingCartProductModel();
		mShoppingCartProductModel.setProductId(mItemProductModel.getProductId());
		mShoppingCartProductModel.setProductCharId(mItemProductModel.getProductCharId());
		mShoppingCartProductModel.setBuyCount(wantBuyCount);
		
		if(ILogin.getLoginUid() != 0){//添加到线上购物车
			addProductToShoppingCart(mItemProductModel, num);
		}
	}

	/*
	 * 加入节能补贴购物车
	 * @param ItemProductModel mItemProductModel 商品
	 * @param int num 购买数量
	 */
	private void addToESShoppingCart(final ItemProductModel mItemProductModel, final int num) {
		if (mItemProductModel.getLowestNum() != 0&& num < mItemProductModel.getLowestNum()) {
			UiUtils.makeToast(this, "本商品" + mItemProductModel.getLowestNum() + "件起售");
			return;
		}
		Bundle param = new Bundle();
		param.putInt("BuyCount", num);
		param.putSerializable("esRule", mItemProductModel.getESPromoRuleModel());
		param.putLong("esProduct", mItemProductModel.getProductId());
		ToolUtil.checkLoginOrRedirect(this, ESShoppingCartActivity.class, param, -1);
	}

	/*
	 * 调用购物车后台接口
	 * @param ItemProductModel product 商品
	 * @param int num 购买数量
	 */
	private void addProductToShoppingCart(ItemProductModel product, final int num) {
		final long uid = ILogin.getLoginUid();
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_ADD_PRODUCTS);
		if( null == ajax )
			return ;

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("district", FullDistrictHelper.getDistrictId());
		data.put("uid", uid);
		data.put("chid", channelID);
		data.put("ids", product.getProductId() + "|" + num + "|" + product.getProductId() + "|0|" + IcsonApplication.getPageRoute() + "|0|" + channelID);
		ajax.setData(data);
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					// 如果有促销规则，就进入购物车
					if (isbuyImmediately_Rules) {
						ShoppingCartActivity.loadShoppingCart(HTML5LinkActivity.this, true, true);
						ToolUtil.sendTrack(this.getClass().getName(), pageId, ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "05014");
					} else {// 添加到购物车，需要显示对话框
						showDialog();
					}
				} else {
					String strErrMsg = v.optString("data");
					if (TextUtils.isEmpty(strErrMsg)) {
						strErrMsg = getString(R.string.add_cart_error);
					}
					UiUtils.makeToast(HTML5LinkActivity.this, strErrMsg);
				}
			}
		});
		this.addAjax(ajax);
		ajax.send();
	}
	
	/*
	 * 
	 * 把用户信息转换为JSONObject格式
	 * @return JSONObject
	 * 
	 */
	private JSONObject accontMsgToJSON(){
		long uid = ILogin.getLoginUid();
		String skey = ILogin.getLoginSkey();
		String token = ILogin.getLoginToken();
		
		JSONObject account = new JSONObject();  
		try{
			account.put("uid", uid);  
			account.put("skey", skey);  
			account.put("token", token);  
		} catch (JSONException ex) { 
			Log.e(LOG_TAG, ex);
		}
		
		return account;
	}

	private void showDialog() {
		// 本地存储
		IShoppingCart.set(mShoppingCartProductModel);
		// 更新icon
		ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(this);
		mShoppingCartCommunication.notifyDataSetChange();
		if(!isBeenSeen())
			return;
		
		Dialog dialog = UiUtils.showDialog(this, R.string.add_cart_ok, R.string.add_cart_msg, R.string.go_checkout, R.string.not_checkout, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					MainActivity.startActivity(HTML5LinkActivity.this, MainActivity.TAB_CART);
					ToolUtil.sendTrack(this.getClass().getName(), strH5AddCartPageId, ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), strH5AddCarLocationId);
				}
			}
		});

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
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
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_HTML5LinkActivity);
	}
}
