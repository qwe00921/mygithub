package com.icson.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.category.CategoryActivity;
import com.icson.home.FullDistrictModel;
import com.icson.home.FullDistrictParser;
import com.icson.home.HomeActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.IShoppingCart;
import com.icson.lib.IVersion;
import com.icson.lib.control.VersionControl;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.model.Account;
import com.icson.lib.model.VersionModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.login.ReloginWatcher;
import com.icson.more.SelectCityActivity;
import com.icson.my.main.MyIcsonActivity;
import com.icson.portal.PortalUpdater;
import com.icson.preference.Preference;
import com.icson.push.PushAssistor;
import com.icson.search.SearchActivity;
import com.icson.service.IcsonService;
import com.icson.shoppingcart.ShoppingCartActivity;
import com.icson.shoppingcart.ShoppingCartCommunication;
import com.icson.shoppingcart.ShoppingCartCommunication.OnShoppingCartChangeListener;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.tencent.feedback.eup.CrashReport;
import com.tencent.feedback.eup.CrashStrategyBean;
import com.tencent.feedback.ua.UserAction;

public class MainActivity extends StackActivityGroup implements OnCheckedChangeListener, OnShoppingCartChangeListener {
	public static final String REQUEST_TAB_NAME = "request_tab_name";
	public static final String REQUEST_EXIT_FLAG = "request_exit_flag";
	public static final String REQUEST_EXTER_KEY = "request_exter_key";
	public static final int TAB_HOME = R.id.radio_home;
	public static final int TAB_MY = R.id.radio_my;
	public static final int TAB_CATEGORY = R.id.radio_category;
	public static final int TAB_CART = R.id.radio_cart;
	public static final int TAB_SEARCH = R.id.radio_search;
	
	// Request flag.
	public static final int REQUEST_ID_WELCOME = 1;
	//public static final int REQUEST_ID_SELCITY = 2;
	
	private RadioGroup mRadioGroup;
	private int lastClickMenuId = 0;
	private TextView mTextViewCart;
	private ShoppingCartCommunication mShoppingCartCommunication;
	private Ajax mVersionAjax;
	private Ajax mThirdLoginAjax;
	private Ajax mAreaAjax;
	//private portalUpdater mPortalUpader;
	//private Ajax mPortalAjax;
	//private UpdatePortalParser mPortalInfoParser;
	//private PortalInfoModel mPortalInfo;
	private FullDistrictParser mFullDistrictParser = null;
	private Bundle mLatest;
	private int mInitTabId;
	private List<Integer> mTabs = new ArrayList<Integer>();
	private AppDialog mExitDialog;
	private SparseArray<String> mTabPageIds = new SparseArray<String>();
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);

		CrashReport.initCrashReport(IcsonApplication.app);
		String userId = ILogin.getLoginUid() + ""; // 用户ID
		CrashReport.setUserId(IcsonApplication.app, userId);
		
//		if (Config.DEBUG) // 正式发布时记得要关闭
//        {
//			CrashReport.setLogAble(true, false);
//        }
		
		setExceptionStrategy();
		
		IcsonApplication.start();
		// Set context for service config.
		ServiceConfig.setContext(this.getApplicationContext());
		initPageIds();

		mRadioGroup = (RadioGroup) findViewById(R.id.main_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(this);

		// 注册icon提醒
		mShoppingCartCommunication = new ShoppingCartCommunication(this);
		mShoppingCartCommunication.setOnShoppingCartChangeListener(this);

		// 为了显示购物车商品数量icon, 主动触发一次
		mShoppingCartCommunication.notifyDataSetChange();

		mTextViewCart = (TextView) findViewById(R.id.x_main_cart_num);
		
		checkVersion();
		
		// Check configuration.
		ServiceConfig.checkConfig();
		
		//clean in portalUpdater
		PortalUpdater.updatePortalInfo(this.getApplicationContext());

		// Initialize MTA SDK configuration
		StatisticsEngine.initMta(Config.DEBUG);
		// Recored application launch information.
		StatisticsEngine.trackEvent(this, "app_launch");
		
		// Report the device information.
		StatisticsEngine.updateInfo(ILogin.getLoginUid(), 0);
		
		// Initialize for beacon
		UserAction.initUserAction(this);
		// Set user id
		UserAction.setUserID(Long.toString(ILogin.getLoginUid()));
		// Report when application starts from background
		UserAction.setAutoLaunchEventUsable(true);
		
		// Start service.
		PushAssistor.setTask(this, true);
		
		//get area information
		initAreaInfo();
		
		// Update tab now.
		boolean bDelayTab = false;
		
		// Check whether to show welcome.
		bDelayTab = showWelcome();//showWelcome()在这里没有实现，只是返回了一个false
		
		if( !bDelayTab ) {
			// Check whether to show city select.
			bDelayTab = selectCity();
		}
		
		Intent pIntent = this.getIntent();
		this.handleIntent(pIntent, true);
		
		// Save version information
		if (IcsonApplication.mVersionCode > Preference.getInstance().getProjVersion()) {
			Preference.getInstance().setProjVersion(IcsonApplication.mVersionCode);
			// pPreference.savePreference();
		}
		
	}
	
	private void initPageIds() {
		mTabPageIds.put(TAB_HOME, getString(R.string.tag_Home));
		mTabPageIds.put(TAB_MY, getString(R.string.tag_MyIcsonActivity));
		mTabPageIds.put(TAB_CATEGORY, getString(R.string.tag_CategoryActivity));
		mTabPageIds.put(TAB_CART, getString(R.string.tag_ShoppingCartActivity));
		mTabPageIds.put(TAB_SEARCH, getString(R.string.tag_SearchActivity));
	}

	private boolean selectCity() {
		IPageCache cache = new IPageCache();
		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);//这里的id就是content的内容，只是里面存储的是一个城市编号的字符串形式
		cache = null;
		int nCityId = (id != null ? Integer.valueOf( id ) : 0);
		
		if( 0 == nCityId ) {
			// Set default city list.
			DispatchFactory.loadDispatch();
			
			Bundle pBundle = new Bundle();
			
			//choose startActivity not startActivityForResult, because of quick close of selectCityActivity
			//xingyao 
			pBundle.putString(SelectCityActivity.SOURCE_SELECT_CITY, SelectCityActivity.SELECT_CITY_FROM_HOME);
			UiUtils.startActivity(this, SelectCityActivity.class, pBundle, 0, false);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_Home), SelectCityActivity.class.getName(), getString(R.string.tag_SelectCityActivity), "02011");
			
			return true;
		}
		
		return false;
	}

	
	private boolean showWelcome() {
//		if(IcsonApplication.mVersionCode > Preference.getInstance().getProjVersion())
//		{
//			UiUtils.startActivity(this, WelcomeActivity.class, REQUEST_ID_WELCOME, true);
//			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_Home), WelcomeActivity.class.getName(), getString(R.string.tag_WelcomeActivity), "02012");
//			
//			return true;
//		}
		
		return false;
	}
	
	
	@Override
	public void onNewIntent(Intent intent) {
		handleIntent(intent, true);
	}
	
	private void handleIntent(Intent aIntent, boolean bUpdateTabNow) {
		if (null == aIntent || aIntent.getBooleanExtra(REQUEST_EXIT_FLAG, false)) {
			finish();
			return;
		}
		
		mLatest = aIntent.getExtras();
		
		if(null != mLatest && mLatest.containsKey("alipay_user_id"))
		{
			String uid = mLatest.getString("alipay_user_id");
			String authcode = mLatest.getString("auth_code");
			String appid = mLatest.getString("app_id");
			AutoLogin3rdUid(appid,uid,authcode);
			/*String info = "alipay_user_id:" + uid 
			+ " auth_code:"+authcode + " appid" + appid;
			
			UiUtils.showDialog(this, "FromAlipay", info,
					 R.string.btn_delete,R.string.btn_cancel,
					new AppDialog.OnClickListener(){
						@Override
						public void onDialogClick(int nButtonId) {
							
						}});
			*/
			
		}
		
		//默认选择的是主页，也就是相当于开始的时候模拟了点击主页的事件
		mInitTabId = aIntent.getIntExtra(REQUEST_TAB_NAME, TAB_HOME);
		
		if( bUpdateTabNow ) {
			this.updateTabStatus(mInitTabId);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ID_WELCOME) {
			if( !selectCity() ) {
				this.updateTabStatus(mInitTabId);
			}
		}
		//else if ( requestCode == REQUEST_ID_SELCITY ) {
		//	this.updateTabStatus(mInitTabId);
		//}
		else
			super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void updateTabStatus(int nTabId) {
		View view = findViewById(nTabId);
		if (view != null && view instanceof RadioButton) {
			if (lastClickMenuId == view.getId()) {
				onCheckedChanged(mRadioGroup, lastClickMenuId);
			} else {
				((RadioButton) view).setChecked(true);
			}
		}
	}
	
	/**
	 * 将上一次点击的图标设暗，将这一次点击的图标设亮，并更新lastClickMenuId的值为新点击的按钮编号
	 * @param checkedId：刚点击的按钮的编号
	 */
	private void hightLightTab(int checkedId) {
		View view = mRadioGroup.findViewById(lastClickMenuId);

		if (view != null && view instanceof RadioButton) {
			((RadioButton) view).setTextColor(getResources().getColor(R.color.main_menu));
		}

		view = mRadioGroup.findViewById(checkedId);
		if (view != null && view instanceof RadioButton) {
			((RadioButton) view).setTextColor(getResources().getColor(R.color.main_menu_s));
		}

		lastClickMenuId = checkedId;
		
		// Save tab
		this.saveTab(checkedId);
	}
	
	/**
	 * 主要就是将新点击的按钮编号加入到历史点击序列中，并加入一些规则
	 * @param nTabId
	 */
	private void saveTab(int nTabId) {
		// 如果点击了home按钮，那么就清除掉历史点击列表，并将home按钮的编号加到列表中。
		if( TAB_HOME == nTabId ) {
			mTabs.clear();
			
			// Add the tab home.
			mTabs.add(nTabId);
			
			return ;
		}
		
		final int nSize = (null != mTabs ? mTabs.size() : 0);
		// Check whether id already exists.
		int nPos = -1;
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			final int nCurrent = mTabs.get(nIdx);
			if( nCurrent == nTabId ) {
				nPos = nIdx;
			}
		}
		
		if( 0 > nPos ) {
			// Not exists, append the new item.
			mTabs.add(nTabId);
		} else if( nPos == nSize - 2 ) {
			// Remove previous and append the new one.
			mTabs.remove(nPos + 1);
		} else if(nPos == nSize - 1) {
			// Do nothing, already the same state.
		}else {
			mTabs.remove(nPos);
			mTabs.add(nTabId);
		}
	}
	
	public boolean handleBack() {
		final int nSize = (null != mTabs ? mTabs.size() : 0);
		if( 1 >= nSize ) {
			if(null==mExitDialog)
			{
				// Already the home tab, try to exits.
				mExitDialog = UiUtils.showDialog(this, R.string.caption_hint, R.string.message_exit, R.string.btn_stay, R.string.btn_exit, new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if (nButtonId == AppDialog.BUTTON_NEGATIVE) {
							mExitDialog.dismiss();
							finish();
						}
					}
				});
			}
			else
				mExitDialog.show();
		} else {
			// Trace back to previous item.
			mTabs.remove(nSize - 1);
			
			// Get the previous item.
			final int nPrev = mTabs.get(nSize - 2);
			updateTabStatus(nPrev);
		}
		
		return true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		boolean changeFlag = false;
		if(lastClickMenuId !=0 && lastClickMenuId != checkedId) {
			changeFlag = true;
		}
		
		switch (checkedId) {
		case TAB_HOME:
			clickHome(changeFlag);
			break;
		case TAB_CATEGORY:
			clickCategory(changeFlag);
			break;
		case TAB_MY:
			clickMyIcson(changeFlag);
			break;
		case TAB_CART:
			clickCart(changeFlag);
			break;
		case TAB_SEARCH:
			clickSearch(changeFlag);
			break;
		}
	}

	private void clickHome(boolean isChanged) 
	{
		String strPageId = mTabPageIds.get(lastClickMenuId);
		if(isChanged) {
			ToolUtil.reportStatisticsClick(strPageId, "10000");
		}
		
		hightLightTab(TAB_HOME);
		startSubActivity(HomeActivity.class, mLatest);
		mLatest = null;

		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_menu), HomeActivity.class.getName(), getString(R.string.tag_Home), "01011");
	}

	private void clickCategory(boolean isChanged) {
		String strPageId = mTabPageIds.get(lastClickMenuId);
		if(isChanged) {
			ToolUtil.reportStatisticsClick(strPageId, "10001");
		}
		hightLightTab(TAB_CATEGORY);
		startSubActivity(CategoryActivity.class);
		
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_menu), CategoryActivity.class.getName(), getString(R.string.tag_CategoryActivity), "01013");
	}

	private void clickMyIcson(boolean isChanged) {
		String strPageId = mTabPageIds.get(lastClickMenuId);
		if(isChanged) {
			ToolUtil.reportStatisticsClick(strPageId, "10003");
		}
		hightLightTab(TAB_MY);
		ToolUtil.checkLoginOrRedirect(this, MyIcsonActivity.class, null, true);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_menu), MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), "01012");
	}

	private void clickCart(boolean isChanged) {
		String strPageId = mTabPageIds.get(lastClickMenuId);
		if(isChanged) {
			ToolUtil.reportStatisticsClick(strPageId, "10002");
		}
		hightLightTab(TAB_CART);
		startSubActivity(ShoppingCartActivity.class);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_menu), ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "01014");
	}

	private void clickSearch(boolean isChanged) {
		String strPageId = mTabPageIds.get(lastClickMenuId);
		if(isChanged) {
			ToolUtil.reportStatisticsClick(strPageId, "10004");
		}
		hightLightTab(TAB_SEARCH);
		startSubActivity(SearchActivity.class);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_menu), SearchActivity.class.getName(), getString(R.string.tag_SearchActivity), "01015");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mVersionAjax != null) {
			mVersionAjax.abort();
			mVersionAjax = null;
		}
		
		PortalUpdater.clear();
		
		if(null!= mThirdLoginAjax)
		{
			mThirdLoginAjax.abort();
			mThirdLoginAjax = null;
		}
		
		if(null!=mAreaAjax)
		{
			mAreaAjax.abort();
			mAreaAjax = null;
		}
		
		ServiceConfig.setContext(null);
		
		// Save the preference.
		Preference.getInstance().savePreference();

		mShoppingCartCommunication.destroy();
		mTextViewCart = null;
		if(null!=mExitDialog && mExitDialog.isShowing())
		{
			mExitDialog.dismiss();
			mExitDialog = null;
		}
		IcsonApplication.exit();
	}

	@Override
	public void OnShoppingCartChange(int num) {
		if (mTextViewCart == null)
			return;
		mTextViewCart.setText(String.valueOf(num));
		final int status = num > 0 ? View.VISIBLE : View.GONE;
		mTextViewCart.setVisibility(status);
		if (status == View.VISIBLE) {
			final FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) mTextViewCart.getLayoutParams();
			int xOffset = (int) ((230f / 320) * ToolUtil.getAppWidth());
			if (param.leftMargin != xOffset) {
				param.leftMargin = xOffset;
				mTextViewCart.setLayoutParams(param);
			}
		}
	}

	@Override
	public ViewGroup getContainer() {
		return (ViewGroup) findViewById(R.id.main_container);
	}

	private void checkVersion() {
		VersionControl control = new VersionControl();
		mVersionAjax = control.getlatestVersionInfo(false, new OnSuccessListener<VersionModel>() {
			@Override
			public void onSuccess(VersionModel v, Response response) {
				if (v.getVersion() > IVersion.getVersionCode()) {
					IVersion.notify(MainActivity.this, v);
				}
				
				if (mVersionAjax != null) {
					mVersionAjax.abort();
					mVersionAjax = null;
				}
			}
		}, null);
	}

	public static void startActivity(Activity from, int whichTab, boolean isExit, String strKey, String strUrl, Bundle pExtras) 
	{
		Intent intent = new Intent(from, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(MainActivity.REQUEST_TAB_NAME, whichTab);
		intent.putExtra(MainActivity.REQUEST_EXIT_FLAG, isExit);
		if( !TextUtils.isEmpty(strKey) && !TextUtils.isEmpty(strUrl) ) {
			AppStorage.setData(AppStorage.SCOPE_DEFAULT, MainActivity.REQUEST_EXTER_KEY, strKey, false);
			AppStorage.setData(AppStorage.SCOPE_DEFAULT, strKey, strUrl, false);
		}
		if( null != pExtras ) {
			intent.putExtras(pExtras);
		}
		from.startActivityForResult(intent, -1);
	}
	
	public static void startActivity(Activity from, int whichTab, boolean isExit) {
		startActivity(from, whichTab, isExit, null, null, null);
	}

	public static void startActivity(Activity from, int whichTab) {
		startActivity(from, whichTab, false, null, null, null);
	}
	
	public static void exitApp(Activity from)
	{
		Intent intent = new Intent(from, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(MainActivity.REQUEST_TAB_NAME, MainActivity.TAB_HOME);
		intent.putExtra(MainActivity.REQUEST_EXIT_FLAG, true);
		from.startActivityForResult(intent, -1);
	}
	
	/**
	 * 根据传入的用户名，appid，授权码
	 * @param appid
	 * @param uid
	 * @param authcode
	 */
	private void AutoLogin3rdUid(String appid, String uid,String authcode)
	{
		//Account curAccount = ILogin.getActiveAccount();
		//yixun 账户不被覆盖
		//2013-08-20 yixun 账户也覆盖 为了获得access_code 来保证跳入用户选择支付宝也能登录态跳出
		//if(null!=curAccount && curAccount.getType()==Account.TYPE_YIXUN)
		//{
		//	return;
		//}
		
		mThirdLoginAjax = ServiceConfig.getAjax(Config.URL_UNION_LOGIN);
		if( null == mThirdLoginAjax )
			return ;
		
		mThirdLoginAjax.setData("yid", "alipayapp");
		mThirdLoginAjax.setData("app_id", appid);
		mThirdLoginAjax.setData("auth_code", authcode);
		mThirdLoginAjax.setData("alipay_user_id", uid);
		mThirdLoginAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){

			/* {"errno":0,
			 *   "data":{"uid":12345678,
			 *           "skey":"ic3B825684",
			 *           "cps_cookies":"123",
			 *           "token":"ca646f10da00b8ad33ab1ede30ab8ce2"
			 *           }
			 * }
			 */
			@Override
			public void onSuccess(JSONObject v, Response response) {
				if(null==v)
					return;
				
				int errno;
				try {
					errno = v.getInt("errno");
					if(errno==0)
					{
						JSONObject retData = v.optJSONObject("data");
						if(null!=retData)
						{
							ILogin.clearAccount();
							// Clear QQ account information.
							ReloginWatcher.getInstance(MainActivity.this).clearAccountInfo();
							
							// 清除本地购物车
							IShoppingCart.clear();
							// 更新icon
							ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(MainActivity.this);
							mShoppingCartCommunication.notifyDataSetChange();
							AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
							
							
							Account account = new Account();
							account.setUid(retData.optLong("uid"));
							account.setType(Account.TYPE_ALI);
							account.setSkey(retData.optString("skey"));
							account.setNickName("");
							account.setToken(retData.optString("token"));
							//access_code storage
							String  access_code = retData.optString("access_token");
							if(!TextUtils.isEmpty(access_code))
								AppStorage.setData(AppStorage.SCOPE_DEFAULT, "ali_access_code", access_code, false);
							//maybe useless
							account.setRowCreateTime(new Date().getTime());
							ILogin.setActiveAccount(account);
							ILogin.saveIdentity(account);
							AppStorage.setData(AppStorage.SCOPE_DEFAULT, "thirdcallsource", "alipayapp", false);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}finally
				{
					if(null!=mThirdLoginAjax)
					{
						mThirdLoginAjax.abort();
						mThirdLoginAjax = null;
					}
				}
			
			}});
			mThirdLoginAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(final Ajax ajax, Response response) {
				if(null!=mThirdLoginAjax)
				{
					mThirdLoginAjax.abort();
					mThirdLoginAjax = null;
				}
			}});
		
		mThirdLoginAjax.send();
	}
	
	/*
	 * get area information.从后台得到所有所有的地区信息，然后存储到cache中（这里的cache是数据库文件）。
	 * 这里会通过md5来验证数据是否有变化，如果没变化就不会从后台重新获取这些信息，直接用cache中的。
	 */
	private void initAreaInfo(){
		//Start service
		startService();
		
		//Get full district information
		IPageCache cache = new IPageCache();
		String strMD5 = cache.get(CacheKeyFactory.CACHE_FULL_DISTRICT_MD5);
		
		mAreaAjax = ServiceConfig.getAjax(Config.URL_FULL_DISTRICT);
		if( null == mAreaAjax )
			return ;
		
		if(null == mFullDistrictParser)
			mFullDistrictParser = new FullDistrictParser();
		if(!TextUtils.isEmpty(strMD5)) {
			mAreaAjax.setData("fileMD5", strMD5);
		}
		
		mAreaAjax.setParser(mFullDistrictParser);
		mAreaAjax.setOnSuccessListener(new OnSuccessListener<FullDistrictModel>(){
			@Override
			public void onSuccess(FullDistrictModel v, Response response) {
				if(mFullDistrictParser.isSuccess()) {
					IPageCache cache = new IPageCache();
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT, mFullDistrictParser.getData(), 0);
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT_MD5, v.getMD5Value(), 0);
				}
				
				if(null!=mAreaAjax)
				{
					mFullDistrictParser = null;
					mAreaAjax.abort();
					mAreaAjax = null;
				}
			}
		});
		
		mAreaAjax.setOnErrorListener(new OnErrorListener(){
			@Override
			public void onError(Ajax ajax, Response response) {
				if(null!=mAreaAjax)
				{
					mFullDistrictParser = null;
					mAreaAjax.abort();
					mAreaAjax = null;
				}
				
			}
		});
		
		mAreaAjax.send();
	}
	
	private void startService() {
		try{
			startService(new Intent(this, IcsonService.class));
		}catch (SecurityException ex)
		{
			Log.e("MessageService", ex);
		}
	}
	
	
	private void setExceptionStrategy(){
		/*当前异常处理策略 */
		CrashStrategyBean pStrategy = new CrashStrategyBean();
		/** 
		 * 开启异常合并功能，异常将累计1天，相同堆栈的异常将会合并为一条进行上报（不影响异常实际发生的次数），
		 * 默认是
		 *关闭的 
		 */
		pStrategy.setMerged(true);
		/**
		* 开启异常堆栈sd卡存储功能 ,每一条异常的堆栈会额外写入
		* /sdcard/Tencent/包id/eupLog.txt中。默认该功能关闭
		* eupLog.txt的最大体积默认是5k，可通过下面接口修改
		*/
		pStrategy.setStoreCrashSdcard(true);
		pStrategy.setCrashSdcardMaxSize(10000);// euplog.txt的体积，默认5k
		/** db中异常存储的最大条数，默认是10条 */
		pStrategy.setMaxStoredNum(20);
		/** 非wifi情况下，一次上报，最多携带异常条数 ,默认是1条，一条大概1k,太大会影响上报成功率 */
		pStrategy.setMaxUploadNum_GPRS(2);
		/** wifi情况下，一次上报，最多携带的异常条数，默认是3条 */
		pStrategy.setMaxUploadNum_Wifi(10);
	}
}
