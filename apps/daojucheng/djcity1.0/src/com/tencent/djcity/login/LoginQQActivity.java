package com.tencent.djcity.login;

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

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.CheckBox;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.activity.BaseActivity;

public class LoginQQActivity extends BaseActivity implements  OnClickListener {
	private static final String LOG_TAG = LoginQQActivity.class.getName();
	private EditText mAccount;
	private EditText mPassword;
	private Button  mLoginButton;
	private CheckBox mLoginRemember;
	//private View mLoginRules;
	
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
		
		//mLoginRules = findViewById(R.id.login_read_view);
		//mLoginRules.setVisibility(View.GONE);
		mLoginRemember = (CheckBox) findViewById(R.id.login_checkbox_pwd);
		mLoginRemember.setVisibility(View.GONE);
		
		Handler mHander = new Handler();
		UiUtils.showSoftInputDelayed(this, mAccount, mHander);
		initDefalutAccount();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLoginHelper.SetListener(mListener); // �???��?????�???��????��??�???��???????��??就没??????�?�???��??�???��??
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
		// ??��??
		WUserSigInfo sigInfo = new WUserSigInfo();
		sigInfo._domains.add(ReloginWatcher.mDomain);
		
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
				//??��??�?�???????示�??
				String prompt_value = ReloginWatcher.getImagePrompt(userAccount, mLoginHelper.GetPicturePrompt(userAccount));
				
				// 跳转??��??�????页�??
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
				//示�??  ??��??A2票�??
				//�??????��?�修??��??�?�?A2�?�?失�??�?�????expire_time??��????????失�??
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_A2);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				loginSucess(userAccount, userSigInfo);
			} else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginQQActivity.this, "???�????�?�?�???????�?");
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
				//示�??  ??��??st票�??
				//Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_ST);
				//util.LOGI("a2:" + util.buf_to_string(ticket._sig) + " a2_key:" + util.buf_to_string(ticket._sig_key)
				//		+ " create_time:" + ticket._create_time + " expire_time:" + ticket._expire_time);
				
				loginSucess(userAccount, userSigInfo);
			}else if(ret == 0xF) {
				//??????A2�????�?????????��?�修??��??�?A2失�??
				UiUtils.makeToast(LoginQQActivity.this, R.string.login_activity_input_pwd_error);
			}
			else if (ret == util.E_NO_RET) {
				UiUtils.makeToast(LoginQQActivity.this, "???�????�?�?�???????�?");
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
						UiUtils.makeToast(this, "???�????�?�?�???????�?");
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
		Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_PSKEY);
		byte [] pskeyByte = ticket._pskey_map.get(ReloginWatcher.mDomain);
		String strPskey =  new String(pskeyByte);
		
		ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_SKEY);
		String strSkey =  new String(ticket._sig);
		
		Account account = new Account();
		account.setUin(Long.valueOf(info._uin));
		account.setType(Account.TYPE_QQ);
		account.setPskey(strPskey);
		account.setSkey(strSkey);
		account.setNickName("");
		account.setRowCreateTime(new Date().getTime());
		ILogin.setActiveAccount(account);
		ILogin.saveIdentity(account);
		
		Preference.getInstance().setQQAccount(""+info._uin);
		
		setResult(FLAG_RESULT_LOGIN_SUCCESS);
		finish();
	}
	
}