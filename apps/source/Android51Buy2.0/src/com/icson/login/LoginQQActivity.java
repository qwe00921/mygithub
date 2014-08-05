package com.icson.login;

import java.util.Date;

import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginHelper.SigType;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.sharemem.WloginSimpleInfo;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.LogCallBack;
import oicq.wlogin_sdk.tools.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.model.Account;
import com.icson.lib.ui.CheckBox;
import com.icson.lib.ui.UiUtils;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Cookie;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class LoginQQActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnClickListener {
	private static final String LOG_TAG = LoginQQActivity.class.getName();
	private EditText mAccount;
	private EditText mPassword;
	private Button  mLoginButton;
	private CheckBox mLoginRemember;
	private View mLoginRules;
	
	public static final int FLAG_RESULT_LOGIN_SUCCESS = 1;
	public WtloginHelper mLoginHelper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (null == mLoginHelper) {
			mLoginHelper = ReloginWatcher.getInstance(this).getWtloginHelper();
			util.LOGCAT_OUT = Config.DEBUG;
		}
		
		mLoginHelper.SetListener(mListener);
		
		setContentView(R.layout.activity_login_qq);
		loadNavBar(R.id.login_qq_navigation_bar);
		mNavBar.setText(R.string.login_qq);
		
		mAccount = (EditText) findViewById(R.id.input_name);
		mAccount.setHint(R.string.login_qq_name_hint);
		mPassword = (EditText) findViewById(R.id.input_pswd);
		mPassword.setHint(R.string.login_qq_password_hint);
		mPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (null != event && event.getAction() != KeyEvent.ACTION_DOWN)
					return false;

				if (actionId == EditorInfo.IME_ACTION_SEND
						|| actionId == EditorInfo.IME_NULL) {
					processLogin();
					return true;
				}
				
				return false;
			}
		});
		
		mLoginButton = (Button) findViewById(R.id.login_button);
		mLoginButton.setOnClickListener(this);
		
		mLoginRules = findViewById(R.id.login_read_view);
		mLoginRules.setVisibility(View.GONE);
		mLoginRemember = (CheckBox) findViewById(R.id.login_checkbox_pwd);
		mLoginRemember.setVisibility(View.GONE);
		
		Handler mHander = new Handler();
		UiUtils.showSoftInputDelayed(this, mAccount, mHander);
		initDefalutAccount();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLoginHelper.SetListener(mListener); // 不在这里调用这句话，登录的时候就没有办法调用回调函数
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.login_button:
				processLogin();
				break;
			default:
				break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mAccount = null;
		mPassword = null;
		mLoginHelper.SetListener(null);
		mLoginHelper = null;
		
	}

	
	
	public void initDefalutAccount() {
		String strAccount = Preference.getInstance().getQQAccount();
		if(null != mAccount) {
			mAccount.setText("");
			if(!strAccount.equals("")){
				mAccount.setText(strAccount);
			}
		}
		
		if(null != mPassword) {
			mPassword.setText("");
		}
	}

	
	public class CALL_BACK extends LogCallBack {
		public void OnLog(JSONObject obj) {
			Log.d("", obj.toString());
		}
	}

	public void processLogin() {
		if (TextUtils.isEmpty(mAccount.getText().toString().trim())) {
			UiUtils.makeToast(this, R.string.login_activity_input_QQ);
			return;
		} else if (TextUtils.isEmpty(mPassword.getText().toString().trim())) {
			UiUtils.makeToast(this, R.string.login_activity_input_pwd);
			return;
		}

		UiUtils.hideSoftInput(this, mAccount);

		showProgressLayer(getString(R.string.login_activity_loading));
		// 登陆
		WUserSigInfo sigInfo = new WUserSigInfo();
		//WloginLastLoginInfo info = mLoginHelper.GetLastLoginInfo();

		//mLoginHelper.SetTimeOut(0);
		// mLoginHelper.SetSigMap(0x332f2);

		int ret = 0;
		if (mLoginHelper.IsNeedLoginWithPasswd(mAccount.getText().toString(), ReloginWatcher.mAppid)) {
			if (mLoginHelper.IsUserHaveA1(mAccount.getText().toString(), ReloginWatcher.mAppid)) {						
				ret = mLoginHelper.GetStWithPasswd(mAccount.getText().toString(),ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, "", sigInfo);
			} else {
				ret = mLoginHelper.GetStWithPasswd(mAccount.getText().toString(), 
						ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, 
						mPassword.getText().toString(), sigInfo);
			}
		} else {
			ret = mLoginHelper.GetStWithoutPasswd(mAccount.getText().toString(), ReloginWatcher.mAppid, 
					ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, sigInfo);
		}
		//need judge the ret
		if (ret != util.E_PENDING)
		{
			UiUtils.makeToast(this, R.string.wtlogin_errinfo_default);
		}
		
	}
	
	WtloginListener mListener = new WtloginListener() {
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
	      		intent.setClass(LoginQQActivity.this, QQVerificationActivity.class);
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
				UiUtils.makeToast(LoginQQActivity.this, "是不是网络不通啊？");
			} else {
				// Get the error code.
				if( (util.S_PWD_WRONG == ret) || (util.E_A1_DECRYPT == ret) || (util.E_NAME_INVALID == ret) || (util.E_TLV_DECRYPT == ret) ) {
					mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
					mPassword.setText("");
				}
				
      			ReloginWatcher.showErrDialog(LoginQQActivity.this,errMsg);
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
				UiUtils.makeToast(LoginQQActivity.this, R.string.login_activity_input_pwd_error);
			}
			else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginQQActivity.this, "是不是网络不通啊？");
			} else {
				mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
				mPassword.setText("");
				ReloginWatcher.showErrDialog(LoginQQActivity.this,errMsg);
			}
		}

	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == QQVerificationActivity.REQUEST_CODE  && null != data){
			String userAccount="";
			ErrMsg errMsg = null;
			WUserSigInfo userSigInfo = null;
			switch(requestCode)
			{
				case 2:
				    Bundle bundle = data.getExtras();
			        userAccount = bundle.getString("ACCOUNT");
			        errMsg = (ErrMsg)bundle.getParcelable("ERRMSG");
			        userSigInfo = (WUserSigInfo)bundle.getParcelable("USERSIG");
			        util.LOGI("userSigInfo " + userSigInfo._seqence);
			        
					if (resultCode == util.S_SUCCESS) 
						loginSucess(userAccount,userSigInfo);
					else if (resultCode == util.E_NO_RET) 
						UiUtils.makeToast(this, "是不是网络不通啊？");
					else 
					{
						mLoginHelper.ClearUserLoginData(userAccount, ReloginWatcher.mAppid);
						ReloginWatcher.showErrDialog(LoginQQActivity.this,errMsg);
					}
					break;
				default:
					break;
			} 
		}
		
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

		String strInfo = info._uin + "&skey=" + skey +
				"&lskey=" + strLskey;
		Ajax ajax = ServiceConfig.getAjax(Config.URL_WT_LOGIN, strInfo);
		if (null == ajax)
			return;

		showProgressLayer(getString(R.string.login_activity_loading));
		TelephonyManager ts = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String guid = ts.getSubscriberId();
		if (guid == null) {
			guid = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		}
		ajax.setData("device_id", ToolUtil.toMD5(guid));
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		ajax.send();

	}

	@Override
	public void onSuccess(JSONObject json, Response response) {
		closeProgressLayer();
		int errno;
		try {
			errno = json.getInt("errno");
		} catch (JSONException e) {
			Log.e(LOG_TAG, "onSuccess|" + ToolUtil.getStackTraceString(e));
			UiUtils.makeToast(this, R.string.login_activity_error);
			return;
		}

		if (errno != 0) {
			String message = null;
			try {
				message = json.getString("data");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			UiUtils.makeToast(this, message);
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
			UiUtils.makeToast(this, R.string.login_activity_error);
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
		
		Preference.getInstance().setQQAccount(mAccount.getText().toString());
		Preference.getInstance().setLastUID(uid);

		setResult(FLAG_RESULT_LOGIN_SUCCESS);
		finish();
	}

	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_LoginQQActivity);
	}
}