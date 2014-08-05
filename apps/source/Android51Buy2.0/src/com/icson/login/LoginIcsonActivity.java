package com.icson.login;

//import java.security.SecureRandom;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.lib.ILogin;
import com.icson.lib.model.Account;
import com.icson.lib.ui.CheckBox;
import com.icson.lib.ui.UiUtils;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Cookie;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class LoginIcsonActivity extends BaseActivity implements OnClickListener, OnSuccessListener<JSONObject> {
	private static final String LOG_TAG =  LoginIcsonActivity.class.getName();
	public static final int FLAG_RESULT_LOGIN_SUCCESS = 1;
	private EditText mAccount;
	private EditText mPassword;
	private CheckBox mLoginRemember;
	private View mLoginRules;
	private Button  mLoginButton;;
	private TextView mRuleText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login_qq);
		loadNavBar(R.id.login_qq_navigation_bar);
		mNavBar.setText(R.string.login_yixun);
		
		mAccount = (EditText) findViewById(R.id.input_name);
		mAccount.setHint(R.string.login_name_hint);
		mPassword = (EditText) findViewById(R.id.input_pswd);
		mPassword.setHint(R.string.login_password_hint);
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
		mRuleText = (TextView) findViewById(R.id.login_rules);
		mRuleText.setOnClickListener(this);
		
		mLoginRules = findViewById(R.id.login_read_view);
		mLoginRules.setVisibility(View.VISIBLE);
		mLoginRemember = (CheckBox) findViewById(R.id.login_checkbox_pwd);
		mLoginRemember.setVisibility(View.VISIBLE);
		
		Handler mHander = new Handler();
		UiUtils.showSoftInputDelayed(this, mAccount, mHander);
		initDefalutAccount();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.login_button:
				processLogin();
				break;
				
			case R.id.login_rules:
				Bundle bundle = new Bundle();
				bundle.putString(HTML5LinkActivity.LINK_URL,"http://u.yixun.com/h5agreement");
				UiUtils.startActivity(this, HTML5LinkActivity.class, bundle, true);
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
		mLoginRemember = null;
	}

	private void initDefalutAccount() {
		String account = Preference.getInstance().getYiXunAccount();
		if(null != mAccount) {
			mAccount.setText("");
			if(!account.equals("")){
				mAccount.setText(account);
			}
		}
		
		if(null != mPassword) {
			mPassword.setText("");
		}
	}
	
	private void processLogin() {
		if( null == mAccount || null == mPassword )
			return ;
		
		UiUtils.hideSoftInput(this, mAccount);
		String name = mAccount.getText().toString().trim();
		String pwd = mPassword.getText().toString().trim();

		if (TextUtils.isEmpty(name)) {
			UiUtils.makeToast(this, R.string.login_activity_input_username);
			return;
		}

		if (TextUtils.isEmpty(pwd)) {
			UiUtils.makeToast(this, R.string.login_activity_input_pwd);
			return;
		}
		//trustSSLHost();
		showProgressLayer(getString(R.string.login_activity_loading));
		Ajax ajax = ServiceConfig.getAjax(Config.URL_APP_LOGIN);
		if( null == ajax )
			return ;
		
		ajax.setData("account", name);
		ajax.setData("password", pwd);
		ajax.setData("from", "app");
		ajax.setData("device_id", StatisticsUtils.getDeviceUid(this));
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
			} catch(JSONException e) {
				e.printStackTrace();
			}
			UiUtils.makeToast(this, message);
			return;
		}

		String uid = null, skey = null, token = null;
		try{
			JSONArray data = json.getJSONArray("data");
			uid = data.getString(0);
			skey = data.getString(1);
			token = data.getString(2);
		}//如果返回errno =0，并且json中没有data，那么尝试从cookie中获取数据
		catch(Exception ex){
			Log.e(LOG_TAG, ex);
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

		account.setUid(Integer.valueOf(uid));
		account.setType(Account.TYPE_YIXUN);
		account.setSkey(skey);
		account.setToken( token  == null ? "" : token );
		account.setNickName("");
		account.setRowCreateTime(new Date().getTime());
		ILogin.setActiveAccount(account);

		Preference.getInstance().setYiXunAccount(mAccount.getText().toString());
		Preference.getInstance().setLastUID(uid);

		//Preference.getInstance().savePreference();
		
		if (mLoginRemember.isChecked()) {
			ILogin.saveIdentity(account);
		}
		
		setResult(FLAG_RESULT_LOGIN_SUCCESS);
		finish();
		
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_LoginIcsonActivity);
	}
}
