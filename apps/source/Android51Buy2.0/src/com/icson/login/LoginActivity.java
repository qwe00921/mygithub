package com.icson.login;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.model.Account;
import com.icson.lib.ui.NavigationBar.OnNavBackListener;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.more.MoreActivity;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Cookie;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class LoginActivity extends BaseActivity implements OnClickListener, com.icson.login.WechatLogin.OnWXLoginResponseListener{
	private static final String LOG_TAG =  LoginActivity.class.getName();
	public static final String REQUEST_PACKAGE_NAME = "request_package_name";
	public static final String REQUEST_CLASS_NAME = "request_class_name";
	public static final String REQUEST_BUNDLE = "request_bundle";
	public static final String REQUEST_OPEN_IN_FRAME = "request_open_in_frame";
	public static final int FLAG_RESULT_LOGIN_SUCCESS = 1;
	
	public static final int FLAG_REQUEST_LOGIN_TO_QQ = 1;
	public static final int FLAG_REQUEST_LOGIN_TO_ALIPAY = 2;
	public static final int FLAG_REQUEST_LOGIN_TO_YIXUN = 3;
	private TextView mAliLoginButton;
	private TextView mYixunLoginButton;
	private View mWechatLoginButton;
	private View mQQLoginButton;

	private RSACrypt mRsa;
	private byte[] mPrivKey;
	private boolean bQuickLogin;
	public WtloginHelper mLoginHelper = null;
	public WtloginListener mListener = null;
	public long            QQuin;
	private Ajax           mWtloginAjax;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_login);
		
		loadNavBar(R.id.login_navigation_bar);
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ToolUtil.startActivity(LoginActivity.this, MoreActivity.class);
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_LoginActivity), MoreActivity.class.getName(), getString(R.string.tag_MoreActivity), "04011");
			}
		});
		
		mNavBar.setOnNavBackListener(new OnNavBackListener() {	
			@Override
			public void onNavBackClick() {
//				UiUtils.hideSoftInput(LoginActivity.this, (EditText)LoginActivity.this.getCurrentFocus());
				ToolUtil.reportStatisticsClick(getActivityPageId(), "19999");
				processBack();
			}
		});
		
		mQQLoginButton = (View) findViewById(R.id.login_qq);
		mQQLoginButton.setOnClickListener(this);
		
		mWechatLoginButton = (View) findViewById(R.id.login_wechat);
		mWechatLoginButton.setOnClickListener(this);
		
		mYixunLoginButton = (TextView) findViewById(R.id.login_yixun);
		mYixunLoginButton.setOnClickListener(this);
		
		mAliLoginButton = (TextView) findViewById(R.id.login_alipay);
		mAliLoginButton.setOnClickListener(this);
		
		bQuickLogin = false;
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		//已经登录 不再出现次界面
		if(0 != ILogin.getLoginUid())
		{
			processBack();
			return;
		}
		
		WechatLogin.setOnWXLoginResponseListener(this, LoginActivity.this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.login_alipay:
				login_alipay();
				break;
			case R.id.login_yixun:
				login_yixun();
				break;
			case R.id.login_wechat:
				ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
				AppUtils.sendWXLogin(LoginActivity.this);
				break;
			case R.id.login_qq:
				bQuickLogin = checkQuickLogin();
				if(!bQuickLogin)
					login_qq();
				break;
			default:
				break;
		}
	}

	/*
	 * 微信登陆：拿到微信授权的code之后，请求服务器
	 * 
	 */
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
	
	private void handleLoginSucess(JSONObject json, Response response) {
		String uid = null, skey = null, token = null;
		try {
			JSONObject data = json.getJSONObject("data");
			uid = data.optString("uid", "0");
			skey = data.optString("skey", "");
			token = data.optString("token", "");
		}// 如果返回errno =0，并且json中没有data，那么尝试从cookie中获取数据
		catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			Cookie cookie = response.getCookie();
			uid = cookie.get("uid");
			skey = cookie.get("skey");
			token = cookie.get("token");
		}

		if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(skey)) {
			UiUtils.makeToast(this, R.string.login_activity_error);
			return;
		}
		
		Account account = new Account();
		account.setUid(Long.valueOf(uid));
		account.setType(Account.TYPE_WECHAT);
		account.setSkey(skey);
		account.setNickName("");
		account.setToken(token);
		account.setRowCreateTime(new Date().getTime());
		ILogin.setActiveAccount(account);
		ILogin.saveIdentity(account);
		
		Preference.getInstance().setLastUID(uid);
		
		onLoginSuccess();
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
	
	private void login_qq() {
		ToolUtil.startActivity(this, LoginQQActivity.class, null, FLAG_REQUEST_LOGIN_TO_QQ);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_LoginActivity), LoginQQActivity.class.getName(), getString(R.string.tag_LoginQQActivity), "04014");
		ToolUtil.reportStatisticsClick(getActivityPageId(), "21002");

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( (requestCode == FLAG_REQUEST_LOGIN_TO_ALIPAY  && resultCode == LoginAliPayActivity.FLAG_RESULT_LOGIN_SUCCESS) 
				||(requestCode == FLAG_REQUEST_LOGIN_TO_QQ  && resultCode == LoginQQActivity.FLAG_RESULT_LOGIN_SUCCESS) 
				|| (requestCode == FLAG_REQUEST_LOGIN_TO_YIXUN  && resultCode == LoginIcsonActivity.FLAG_RESULT_LOGIN_SUCCESS) ) {
			onLoginSuccess();
		}
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
		
		// Report the device information.
		StatisticsEngine.updateInfo(ILogin.getLoginUid(), 1);
		//When login successful, report Data
		ToolUtil.reportStatisticsDevice("reportDeviceType:3|");
		
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
					 * android sdk 2.3 版本调用 LocalActivityManager.destroyActivity(LoginActivity.class.getName(), true)之后，再调用LocalActivityManager.startActivity 返回null，这是系统问题
					 * 该版本（2.2.4）只针对4.0以上版本释放LoginActivity，2.3以下版本不做处理
					 * 还有另外一种解决方法，见函数 private boolean destroy(MainActivity parent, String id)
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
	 * 该函数主要解决问题：android sdk 2.3 版本调用 LocalActivityManager.destroyActivity(LoginActivity.class.getName(), true)之后，再调用LocalActivityManager.startActivity 返回null
	 * android sdk 2.3 ：
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
	 * android 4.0以上版本
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
	 * 两个版本代码区别，在于finish＝true时的处理
	 * 
	 * 该函数使用时设置finish＝flase，然后再调用该函数
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
							
					// 公钥  :publickey
					// Appid:dstAppid
					// subAppid: subDstAppid（业务填1）
					// Sdk版本号：dstSsoVer   5
					// App版本号：dstAppVer
					//动作：key:key_action, value:action_quick_login
					
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
				//获取验证码提示语
				String prompt_value = ReloginWatcher.getImagePrompt(userAccount, mLoginHelper.GetPicturePrompt(userAccount));
				
				// 跳转到验证码页面
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
				//示例  获取A2票据
				//如果用户修改秘密，A2马上失效，不能expire_time判断是否失效
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_A2);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				loginSucess(userAccount, userSigInfo);
			} else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginActivity.this, "是不是网络不通啊？");
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
				//示例  获取st票据
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_ST);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				
				loginSucess(userAccount, userSigInfo);
			}else if(ret == 0xF) {
				//可能A2过期，或者用户修改秘密A2失效
				UiUtils.makeToast(LoginActivity.this, R.string.login_activity_input_pwd_error);
			}
			else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginActivity.this, "是不是网络不通啊？");
			} else {
				mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
				ReloginWatcher.showErrDialog(LoginActivity.this,errMsg);
			}
		}
		};
		
		
		mLoginHelper.SetListener(mListener);
		
	}

	@Override
	public void OnWXLoginResponse(String code, String state) {
		wxLoginCallBack(code, state);
	}

	@Override
	protected void onDestroy() {
		if(null!=mWtloginAjax)
		{
			mWtloginAjax.abort();
			mWtloginAjax = null;
		}
		mListener = null;
		if(null!=mLoginHelper)
		{
			mLoginHelper.SetListener(null);
			mLoginHelper = null;
		}
		WechatLogin.unset(this);
		mRsa = null;
		
		super.onDestroy();
	}

	private void loginSucess(String userAccount, WUserSigInfo userSigInfo) {
		WloginSimpleInfo info = new WloginSimpleInfo();
		mLoginHelper.GetBasicUserInfo(userAccount, info);
		Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_LSKEY);
		String strLskey =  new String(ticket._sig);
		
		ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_SKEY);
		String skey =  new String(ticket._sig);
		
		// Report QQ account to mta.
		StatisticsEngine.reportQQ(this, userAccount);

		QQuin = info._uin;
		String strInfo = QQuin + "&skey=" + skey +
				"&lskey=" + strLskey;
		mWtloginAjax = ServiceConfig.getAjax(Config.URL_WT_LOGIN, strInfo);
		if (null == mWtloginAjax)
			return;

		showProgressLayer(getString(R.string.login_activity_loading));
		
		TelephonyManager ts = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String guid = ts.getSubscriberId();
		if (guid == null) {
			guid = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		}
		mWtloginAjax.setData("device_id", ToolUtil.toMD5(guid));
		mWtloginAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){

			@Override
			public void onSuccess(JSONObject json, Response response) {
				closeProgressLayer();
				int errno;
				try {
					errno = json.getInt("errno");
				} catch (JSONException e) {
					Log.e(LOG_TAG, "onSuccess|" + ToolUtil.getStackTraceString(e));
					UiUtils.makeToast(LoginActivity.this, R.string.login_activity_error);
					return;
				}

				if (errno != 0) {
					String message = null;
					try {
						message = json.getString("data");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					UiUtils.makeToast(LoginActivity.this, message);
					return;
				}

				String uid = null, skey = null, token = null;
				try {
					JSONArray data = json.getJSONArray("data");
					uid = data.getString(0);
					skey = data.getString(1);
					token = data.getString(2);
				}// 如果返回errno =0，并且json中没有data，那么尝试从cookie中获取数据
				catch (Exception ex) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
					Cookie cookie = response.getCookie();
					uid = cookie.get("uid");
					skey = cookie.get("skey");
					token = cookie.get("token");
				}

				if (null == uid || null == skey) {
					UiUtils.makeToast(LoginActivity.this, R.string.login_activity_error);
					return;
				}

				Account account = new Account();
				account.setUid(Long.valueOf(uid));
				account.setType(Account.TYPE_QQ);
				account.setSkey(skey);
				account.setNickName("");
				account.setToken(token);
				account.setRowCreateTime(new Date().getTime());
				ILogin.setActiveAccount(account);
				ILogin.saveIdentity(account);
				
				Preference.getInstance().setQQAccount(""+QQuin);
				Preference.getInstance().setLastUID(uid);
				
				onLoginSuccess();
			}});
		mWtloginAjax.setOnErrorListener(this);
		mWtloginAjax.send();
	}
	
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_LoginActivity);
	}
}
