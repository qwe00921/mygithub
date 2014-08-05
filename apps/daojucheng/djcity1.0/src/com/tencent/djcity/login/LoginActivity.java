package com.tencent.djcity.login;

import java.util.Date;

import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginHelper.SigType;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.sharemem.WloginSimpleInfo;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.RSACrypt;
import oicq.wlogin_sdk.tools.util;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.main.MainActivity;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;

public class LoginActivity extends BaseActivity implements OnClickListener{
	private static final String LOG_TAG =  LoginActivity.class.getName();
	public static final String REQUEST_PACKAGE_NAME = "request_package_name";
	public static final String REQUEST_CLASS_NAME = "request_class_name";
	public static final String REQUEST_BUNDLE = "request_bundle";
	public static final String REQUEST_OPEN_IN_FRAME = "request_open_in_frame";
	public static final int FLAG_RESULT_LOGIN_SUCCESS = 1;
	
	public static final int FLAG_REQUEST_LOGIN_TO_QQ = 1;
	public static final int FLAG_REQUEST_LOGIN_TO_ALIPAY = 2;
	public static final int FLAG_REQUEST_LOGIN_TO_YIXUN = 3;
	private TextView mLoginwichPasswd;
	private View mQQLoginButton;

	private RSACrypt mRsa;
	private byte[] mPrivKey;
	public WtloginHelper mLoginHelper = null;
	public WtloginListener mListener = null;
	public long            QQuin;
	private Boolean        bQuickLogin = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!util.CheckMayFastLogin(LoginActivity.this))
		{
			bQuickLogin = false;
			this.login_qq();
		}
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_login);
		
		//loadNavBar(R.id.login_navigation_bar);
		
		/*
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ToolUtil.startActivity(LoginActivity.this, MoreActivity.class);
			}
		});
		
		mNavBar.setOnNavBackListener(new OnNavBackListener() {	
			@Override
			public void onNavBackClick() {
				processBack();
			}
		});
		*/
		mQQLoginButton = (View) findViewById(R.id.quick_login);
		if(null!=mQQLoginButton)
			mQQLoginButton.setOnClickListener(this);
		
		
		mLoginwichPasswd = (TextView) findViewById(R.id.login_with_passwd_tv);
		if(null!=mLoginwichPasswd)
			mLoginwichPasswd.setOnClickListener(this);
		
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		if(0 != ILogin.getLoginUin())
		{
			processBack();
			return;
		}
		
		//WechatLogin.setOnWXLoginResponseListener(this, LoginActivity.this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.login_with_passwd_tv:
				login_qq();
				break;
			case R.id.quick_login:
				if(!checkQuickLogin())
					login_qq();
				break;
			default:
				break;
		}
	}

	/*
	private void wxLoginCallBack(String code, String state){
//		mAjax = AjaxUtil.get("http://mb.51buy.com/json.php?mod=login&act=weixinlogin");
		Ajax ajax = ServiceConfig.getAjax(Config.URL_WECHAT_LOGIN);
		if (null == ajax)
			return;

		showProgressLayer(getString(R.string.login_activity_loading));
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				if(null == v) {
					UiUtils.makeToast(LoginActivity.this, R.string.login_activity_error);
					return;
				}
				
				int errNo = v.optInt("errno", -1);
				String errMsg = "";
				if( 0 == errNo ){
					handleLoginSucess(v, response);
				}else{
					errMsg = v.optString("data");
					if(TextUtils.isEmpty(errMsg)) {
						errMsg = getString(R.string.login_activity_error);
					}
					UiUtils.makeToast(LoginActivity.this, errMsg);
				}
				
			}
		});
		ajax.setData("code", code);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		ajax.send();
	}
	
	private void login_alipay() {
		ToolUtil.startActivity(this, LoginAliPayActivity.class, null, FLAG_REQUEST_LOGIN_TO_ALIPAY);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_LoginActivity), LoginAliPayActivity.class.getName(), getString(R.string.tag_LoginAliPayActivity), "04012");
		ToolUtil.reportStatisticsClick(getActivityPageId(), "21004");
	}
	
	private void login_yixun() {
		ToolUtil.startActivity(this, LoginIcsonActivity.class, null, FLAG_REQUEST_LOGIN_TO_YIXUN);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_LoginActivity), LoginIcsonActivity.class.getName(), getString(R.string.tag_LoginIcsonActivity), "04013");
		ToolUtil.reportStatisticsClick(getActivityPageId(), "21003");

	}
	*/
	private void login_qq() {
		ToolUtil.startActivity(this, LoginQQActivity.class, null, FLAG_REQUEST_LOGIN_TO_QQ);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( requestCode == FLAG_REQUEST_LOGIN_TO_QQ  && resultCode == LoginQQActivity.FLAG_RESULT_LOGIN_SUCCESS)
			onLoginSuccess();
		else if(null!=data && bQuickLogin)
		{
			int ret = data.getExtras().getInt("quicklogin_ret");
			if(ret!=0)
	    	{
	    		UiUtils.makeToast(LoginActivity.this, R.string.login_fail_title);
	    		return;
	    	}
	    	
	    	String uin = data.getExtras().getString("quicklogin_uin");
	    	byte[] buff = data.getExtras().getByteArray("quicklogin_buff");
	    	if (buff == null)
	    	{
	    		UiUtils.makeToast(LoginActivity.this, R.string.login_fail_title);
	    		return;
	    	}
	    	
    		WUserSigInfo info = new WUserSigInfo();
    		
    		info._fastLoginBuf = mRsa.DecryptData(mPrivKey, buff);
    		info._domains.add(ReloginWatcher.mDomain);
    		
    		showProgressLayer(getString(R.string.login_activity_loading));
    		mLoginHelper.GetStWithPasswd(uin, ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, "", info);
		}
			
	}
	
	public void onLoginSuccess() {

		Intent intent = getIntent();

		Bundle data = intent.getBundleExtra(REQUEST_BUNDLE);
		String packageName = intent.getStringExtra(REQUEST_PACKAGE_NAME);
		String className = intent.getStringExtra(REQUEST_CLASS_NAME);
		boolean openInFrame = intent.getBooleanExtra(REQUEST_OPEN_IN_FRAME, false);

		boolean loginFromRedirect = (packageName != null && className != null);

		setResult(FLAG_RESULT_LOGIN_SUCCESS);
		
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_CART_RELOAD, "1", false);

		if (loginFromRedirect) {
			Intent rIntent = new Intent();
			rIntent.setClassName(packageName, className);
			if (data != null) {
				rIntent.putExtras(data);
			}
			if (openInFrame) {
				Activity parent = getParent();
				if (parent != null && parent instanceof MainActivity) {
					MainActivity mParent = (MainActivity) parent;
					/*
					 * android sdk 2.3 ??????éï¿½???? LocalActivityManager.destroyActivity(LoginActivity.class.getName(), true)éï¿½????éï¿½????éï¿½????LocalActivityManager.startActivity éï¿½????nulléï¿½?éï¿½????ç»¯ä¼æ·?????éï¿½?
					 * çã¯æ·?????éï¿½?2.2.4éï¿½???????éï¿½?4.0æµ ã¯æ·??????????????LoginActivityéï¿½?2.3æµ ã¯æ·????????éï¿½????éï¿½????
					 * éï¿½???????éï¿½?éï¿½?éï¿½?çï½æ·?éæ¤æ·?éæ¤æ·??éï¿½?éï¿½???éæ¤æ·?? private boolean destroy(MainActivity parent, String id)
					 */
					if(android.os.Build.VERSION.SDK_INT > 10) {
						LocalActivityManager pManagaer = mParent.getLocalActivityManager();
						pManagaer.destroyActivity(LoginActivity.class.getName(), true);
					}
					mParent.startSubActivity(rIntent);
				} else {
					Log.e(LOG_TAG, "onSuccess|redirect failed: parent is null or parent instance is not MainActivity");
				}
			} else {
				startActivity(rIntent);
				finish();
			}
		} else {
			finish();
		}
	}
	
	/*
	 * çã¯æ·?éæ¤æ·?éæå¯éï¿½?çï½æ·?éæ¤æ·??éï¿½?éï¿½?android sdk 2.3 ??????éï¿½???? LocalActivityManager.destroyActivity(LoginActivity.class.getName(), true)éï¿½????éï¿½????éï¿½????LocalActivityManager.startActivity éï¿½????null
	 * android sdk 2.3 éï¿½?
	 * public Window More ...destroyActivity(String id, boolean finish) {
     *   LocalActivityRecord r = mActivities.get(id);
     *   Window win = null;
	 *      if (r != null) {
	 *           win = performDestroy(r, finish);
	 *          if (finish) {
	 *               mActivities.remove(r);
	 *           }
	 *       }
	 *       return win;
	 *   }
	 *   
	 * android 4.0æµ ã¯æ·????????
	 * public Window destroyActivity(String id, boolean finish) {
     *     	LocalActivityRecord r = mActivities.get(id);
     *		Window win = null;
     *   	if (r != null) {
     *      	win = performDestroy(r, finish);
     *       	if (finish) {
     *           mActivities.remove(id);
     *           mActivityArray.remove(r);
     *       	}
     *  	}
     *   return win;
     *	}
	 * 
	 * æ¶ãé??????æµ ï½æ·????éæ¤æ·??éï¿½???éæ¤æ·??finishéï¿½?true??éæ¤æ·??éï¿½????
	 * 
	 * çã¯æ·?éæ¤æ·?éæå¨??éæ¤æ·?éæ¤æ·?éççfinishéï¿½?flaseéï¿½???éæ¤æ·?????éï¿½???éæ¤æ·?éæ¤æ·?éæ¤æ·??
	 * 
	 */
	/*	
	private boolean destroy(MainActivity parent, String id) {
		final LocalActivityManager activityManager = parent.getLocalActivityManager();
		if(activityManager != null){
			activityManager.destroyActivity(id, false);
			// http://code.google.com/p/android/issues/detail?id=12359
			// http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/app/LocalActivityManager.java
			try {
				final Field mActivitiesField = LocalActivityManager.class.getDeclaredField("mActivities");
				if(mActivitiesField != null){
					mActivitiesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					final Map<String, Object> mActivities = (Map<String, Object>)mActivitiesField.get(activityManager);
					if(mActivities != null){
						mActivities.remove(id);
					}
					final Field mActivityArrayField = LocalActivityManager.class.getDeclaredField("mActivityArray");
					if(mActivityArrayField != null){
						mActivityArrayField.setAccessible(true);
						@SuppressWarnings("unchecked")
						final ArrayList<Object> mActivityArray = (ArrayList<Object>)mActivityArrayField.get(activityManager);
						if(mActivityArray != null){
							for(Object record : mActivityArray){
								final Field idField = record.getClass().getDeclaredField("id");
								if(idField != null){
									idField.setAccessible(true);
									final String _id = (String)idField.get(record);
									if(id.equals(_id)){
										mActivityArray.remove(record);
										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	*/

	private boolean checkQuickLogin()
	{
		boolean ret = false;
		if(util.CheckMayFastLogin(LoginActivity.this))
		{
			if (null == mLoginHelper) {
				initLoginHelper();
			}
			
			showProgressLayer(getString(R.string.login_activity_loading));
			try{
				Intent intent = new Intent(); 
				intent.setClassName("com.tencent.mobileqq", "com.tencent.open.agent.AgentActivity");
				//intent.setComponent(new ComponentName("oicq.wtlogin_sdk_demo", "oicq.wtlogin_sdk_demo.QuickLogin"));
					
				if(null == mRsa)
					mRsa = new RSACrypt(LoginActivity.this);
				mRsa.GenRSAKey();
				mPrivKey = mRsa.get_priv_key();
				byte[] mPubKey = mRsa.get_pub_key();
							
					// ??????  :publickey
					// Appid:dstAppid
					// subAppid: subDstAppidéï¿½?éï¿½???éèï½1éï¿½?
					// Sdk????????éæ¤æ·??dstSsoVer   5
					// App????????éæ¤æ·??dstAppVer
					//??éæ¤æ·??éï¿½?key:key_action, value:action_quick_login
					
				Bundle bundle=new Bundle();
				bundle.putByteArray("publickey", mPubKey);
				bundle.putLong("dstAppid", ReloginWatcher.mAppid);
				bundle.putLong("dstSsoVer", 5L);
				bundle.putLong("subDstAppid", 1L);
				bundle.putByteArray("dstAppVer", new String(""+IcsonApplication.mVersionCode).getBytes());
				intent.putExtra("key_params", bundle);
				intent.putExtra("key_action", "action_quick_login");
					
				LoginActivity.this.startActivityForResult(intent, 0x100);
				ret = true;
			}catch(Exception e)
			{ 
			} 
		}
		return ret;
	}
	
	private void initLoginHelper() {
		mLoginHelper = ReloginWatcher.getInstance(this).getWtloginHelper();
		util.LOGCAT_OUT = Config.DEBUG;
		mListener = new WtloginListener()
		{
		public void OnGetStWithPasswd(String userAccount, long dwSrcAppid,
				int dwMainSigMap, long dwSubDstAppid, String userPasswd,
				WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
			
			closeProgressLayer();
			if (ret == util.S_GET_IMAGE) {
				byte[] image_buf = new byte[0];
				image_buf = mLoginHelper.GetPictureData(userAccount);
				if (image_buf == null) {
					return;
				}
				//??éæ¤æ·??éï¿½?éï¿½???????ç»ç´æ·??
				String prompt_value = ReloginWatcher.getImagePrompt(userAccount, mLoginHelper.GetPicturePrompt(userAccount));
				
				// çºå® æµ??éæ¤æ·??éï¿½????æ¤¤ç¢æ·??
				Intent intent = new Intent();
	      		intent.setClass(LoginActivity.this, QQVerificationActivity.class);
	      		Bundle bundle = new Bundle();
  				bundle.putByteArray("CODE", image_buf);
  				bundle.putString("PROMPT", prompt_value);
  				bundle.putString("ACCOUNT", userAccount);
  				intent.putExtras(bundle);
				startActivityForResult(intent,
						QQVerificationActivity.REQUEST_CODE);
			} else if (ret == util.S_SUCCESS) {
				//ç»ç´æ·??  ??éæ¤æ·??A2ç»ï¿½éï¿½??
				//éï¿½??????éæ¤æ·?éææ¨??éæ¤æ·??éï¿½?éï¿½?A2éï¿½?éï¿½?æ¾¶ææ·??éï¿½?éï¿½????expire_time??éæ¤æ·????????æ¾¶ææ·??
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_A2);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				loginSucess(userAccount, userSigInfo);
			} else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginActivity.this, "???éï¿½????éï¿½?éï¿½?éï¿½???????éï¿½?");
			} else {
				// Get the error code.
				if( (util.S_PWD_WRONG == ret) || (util.E_A1_DECRYPT == ret) || (util.E_NAME_INVALID == ret) || (util.E_TLV_DECRYPT == ret) ) {
					mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
				}
				
				ReloginWatcher.showErrDialog(LoginActivity.this,errMsg);
      		}
		}
		
		public void OnGetStWithoutPasswd(String userAccount, long dwSrcAppid,
				long dwDstAppid, int dwMainSigMap, long dwSubDstAppid,
				WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
			
			closeLoadingLayer();
			if (ret == util.S_SUCCESS) {
				//ç»ç´æ·??  ??éæ¤æ·??stç»ï¿½éï¿½??
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_ST);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				
				loginSucess(userAccount, userSigInfo);
			}else if(ret == 0xF) {
				//??????A2éï¿½????éï¿½?????????éæ¤æ·?éææ¨??éæ¤æ·??éï¿½?A2æ¾¶ææ·??
				UiUtils.makeToast(LoginActivity.this, R.string.login_activity_input_pwd_error);
			}
			else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginActivity.this, "???éï¿½????éï¿½?éï¿½?éï¿½???????éï¿½?");
			} else {
				mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
				ReloginWatcher.showErrDialog(LoginActivity.this,errMsg);
			}
		}
		};
		
		
		mLoginHelper.SetListener(mListener);
		
	}

	@Override
	protected void onDestroy() {
		mListener = null;
		if(null!=mLoginHelper)
		{
			mLoginHelper.SetListener(null);
			mLoginHelper = null;
		}
		//WechatLogin.unset(this);
		mRsa = null;
		
		super.onDestroy();
	}

	private void loginSucess(String userAccount, WUserSigInfo userSigInfo) {
		WloginSimpleInfo info = new WloginSimpleInfo();
		mLoginHelper.GetBasicUserInfo(userAccount, info);
		Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_PSKEY);
		byte [] pskeyByte = ticket._pskey_map.get(ReloginWatcher.mDomain);
		String strPskey =  new String(pskeyByte);
		
		ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_SKEY);
		String strSkey =  new String(ticket._sig);
		
		QQuin = info._uin;
		
		Account account = new Account();
		account.setUin(Long.valueOf(QQuin));
		account.setType(Account.TYPE_QQ);
		account.setPskey(strPskey);
		account.setSkey(strSkey);
		account.setNickName("");
		account.setRowCreateTime(new Date().getTime());
		ILogin.setActiveAccount(account);
		ILogin.saveIdentity(account);
				
		Preference.getInstance().setQQAccount(""+QQuin);
				
		onLoginSuccess();
	}
	
	
	
}
