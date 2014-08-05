package com.tencent.djcity.main;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.pay.AndroidPay;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.djcity.R;
import com.tencent.djcity.category.CategoryActivity;
import com.tencent.djcity.discover.DiscoverListActivity;
import com.tencent.djcity.home.HomeActivity;
import com.tencent.djcity.item.FakeItemActivity;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.login.ReloginWatcher;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.more.SelectGameActivity;
import com.tencent.djcity.my.MyCityActivity;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.ServiceConfig;
import com.tencent.djcity.util.ToolUtil;

public class MainActivity extends StackActivityGroup implements OnCheckedChangeListener{
	public static final String REQUEST_TAB_NAME = "request_tab_name";
	public static final String REQUEST_EXIT_FLAG = "request_exit_flag";
	public static final String REQUEST_EXTER_KEY = "request_exter_key";
	
	public static final String REQUEST_LOGOUT_FLAG = "request_logout_flag";
	
	public static final int TAB_HOME = R.id.radio_home;
	public static final int TAB_CATEGORY = R.id.radio_category;
	public static final int TAB_EVENT = R.id.radio_event;
	public static final int TAB_DISCOVER = R.id.radio_discover;
	public static final int TAB_MY = R.id.radio_my;
	
	private AppDialog mExitDialog;
	
	private RadioGroup mRadioGroup;
	private List<Integer> mTabs = new ArrayList<Integer>();
	private int lastClickMenuId = 0;
	private int mInitTabId;
	private Bundle mLatest;
	
	private ReloginWatcher mLoginWatcher;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_main);
		
		IcsonApplication.start();
		// Set context for service config.
		ServiceConfig.setContext(this.getApplicationContext());
		
		mRadioGroup = (RadioGroup) findViewById(R.id.main_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		Intent pIntent = this.getIntent();
		this.handleIntent(pIntent);
		
		ReloginWatcher.getInstance(getApplicationContext());
		
		//全局初始化
        AndroidPay.Initialize(MainActivity.this);
        AndroidPay.setEnv("test");
        AndroidPay.setLogEnable(Config.DEBUG);
        

		if(selectGame()) {
			
		}
		
		// 开启logcat输出，方便debug，发布时请关闭
		XGPushConfig.enableDebug(this, true);
		XGPushManager.registerPush(this);
		Preference.getInstance().setProjVersion(IcsonApplication.mVersionCode);
	}
	
	private boolean selectGame() {
		Preference pref = Preference.getInstance();
		GameInfo info = null;
		if(pref != null) {
			info = pref.getGameInfo();
		}
		if(info != null &&  !info.needBind()) {
			return false;
		}
		Intent intent = new Intent(this, SelectGameActivity.class);
		startActivity(intent);
		
		return true;
	}
	
	@Override
	protected void onResume()
	{
		long userAccount = ILogin.getLoginUin();
		if(userAccount >0)
		{
			String skey = ReloginWatcher.getSkeyByLocalSig(""+userAccount);
			String pskey = ReloginWatcher.getPskeyByLocalSig(""+userAccount);
			if(TextUtils.isEmpty(skey))
			{
				ILogin.clearAccount();
			}
			else
			{
				Account account = ILogin.getActiveAccount();
				if(null!=account)
				{
					account.setSkey(skey);
					account.setPskey(pskey);
					account.setType(Account.TYPE_QQ);
					ILogin.setActiveAccount(account);
					ILogin.saveIdentity(account);
				}
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		AndroidPay.Destory();
		
		Preference.getInstance().savePreference();
		IcsonApplication.exit();
		super.onDestroy();
	}
	private void handleIntent(Intent aIntent) {
		
		//姒�妯款�婚��澶�瀚ㄩ�ㄥ��妲告��濠�������灞肩�����杈ㄦЦ��╃�跨��娴�搴＄��婵�瀣�娈����璺猴拷濡����瀚�娴������ｉ��璁冲��妞ょ�垫��娴�瀣╂��
		mInitTabId = aIntent.getIntExtra(REQUEST_TAB_NAME, TAB_HOME);
		
		this.updateTabStatus(mInitTabId);
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
	 * @param nTabId
	 */
	private void saveTab(int nTabId) {
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
	
	/**
	 * 
	 * @return
	 */
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
		// Save tab
		if(lastClickMenuId !=0 && lastClickMenuId != checkedId) {
			changeFlag = true;
		}
		
		lastClickMenuId = checkedId;

		saveTab(checkedId);
		
		switch (checkedId) {
		case TAB_HOME:
			clickHome(changeFlag);
			break;
		case TAB_CATEGORY:
			clickCategory(changeFlag);
			break;
		case TAB_EVENT:
			clickEvent(changeFlag);
			break;
		case TAB_DISCOVER:
			clickDiscover(changeFlag);
			break;
		case TAB_MY:
			clickMyCity(changeFlag);
			break;	
		}
	}
	
	public void showTab(int checkedId) {
		mRadioGroup.check(checkedId);
	}

	private void clickDiscover(boolean changeFlag) {
		ToolUtil.checkLoginOrRedirect(this, DiscoverListActivity.class, null, true);
	}


	private void clickEvent(boolean changeFlag) {
		ToolUtil.checkLoginOrRedirect(this, FakeItemActivity.class, null, true);
		
	}


	private void clickMyCity(boolean changeFlag) {
//		hightLightTab(TAB_MY);
		ToolUtil.checkLoginOrRedirect(this, MyCityActivity.class, null, true);
		
	}


	private void clickCategory(boolean changeFlag) {
		startSubActivity(CategoryActivity.class, mLatest);
		mLatest = null;
	}


	private void clickHome(boolean changeFlag) {
		//hightLightTab(TAB_HOME);
		startSubActivity(HomeActivity.class, mLatest);
		mLatest = null;
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		if(null != intent)
		{
			// logout
			if(intent.getBooleanExtra(MainActivity.REQUEST_LOGOUT_FLAG, false))
			{
				this.finish();
				
				ToolUtil.checkLoginOrRedirect(this, MainActivity.class, null);
				
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * logout 
	 * @param from
	 */
	public static void logout(Activity from)
	{
		ILogin.clearAccount();
		
		Preference.getInstance().clearGameInfo();
		
		Intent intent = new Intent(from, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(MainActivity.REQUEST_LOGOUT_FLAG, true);
		
		from.startActivity(intent);
		//from.startActivityForResult(intent, -1);
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

	@Override
	public ViewGroup getContainer() {
		// TODO Auto-generated method stub
		return (ViewGroup) findViewById(R.id.main_container);
	}

}
