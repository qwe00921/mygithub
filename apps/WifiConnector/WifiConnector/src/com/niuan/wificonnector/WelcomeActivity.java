package com.niuan.wificonnector;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.EditText;

import com.niuan.wificonnector.lib.ui.BaseActivity;
import com.niuan.wificonnector.util.ToastUtil;
import com.niuan.wificonnector.util.WebViewUtils;
import com.niuan.wificonnector.util.thread.BackgroundTask;

public class WelcomeActivity extends BaseActivity {

	private EditText mPhoneNumber;
	private ImageButton mBtnRegister;
	private ImageButton mBtnLogin;
	private Preference mPref = Preference.getInstance();
	private View mLayoutRegister;
	private View mLayoutLogin;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_user_center);

		mPhoneNumber = (EditText) findViewById(R.id.et_phone_number);
		mBtnRegister = (ImageButton) findViewById(R.id.btn_register);
		mBtnLogin = (ImageButton) findViewById(R.id.btn_login);
		mLayoutRegister = findViewById(R.id.layout_register);
		mLayoutLogin = findViewById(R.id.layout_login);

		mBtnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new LoginTask().execute();
				mBtnLogin.setEnabled(false);
				ToastUtil.makeToast("正在建立安全连接，请稍候...");
			}
		});
		mBtnRegister.setOnClickListener(new OnClickListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.view.View.OnClickListener#onClick(android.view.View)
			 */
			@Override
			public void onClick(View v) {
				if (mPhoneNumber.length() != 11) {
					ToastUtil.makeToast("请输入正确的手机号码");

					return;
				}

				mBtnRegister.setEnabled(false);

				new RegisterTask().execute();
				ToastUtil.makeToast("注册中，请稍候...");
				// mDialog.show();

			}
		});

		if (!"".equals(mPref.getUser())) {
			mLayoutLogin.setVisibility(View.VISIBLE);
			mLayoutRegister.setVisibility(View.GONE);
		} else {
			mLayoutLogin.setVisibility(View.GONE);
			mLayoutRegister.setVisibility(View.VISIBLE);
		}

		// mDialog = new ProgressDialog(this);
		// mDialog.setIcon(null);
		// mDialog.setTitle("登录中，请稍候...");
	}

	public class RegisterTask extends BackgroundTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebViewUtils.loadByWebView(WelcomeActivity.this,
						"http://ps.exands.com:38007/ux/");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// mDialog.dismiss();
			ToastUtil.makeToast("注册成功!");
			mPref.saveUser(mPhoneNumber.getText().toString());
			startMainActivity();
			super.onPostExecute(result);
		}

	}

	public class LoginTask extends BackgroundTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebViewUtils.loadByWebView(WelcomeActivity.this,
						"http://ps.exands.com:38007/ux/");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// mDialog.dismiss();
			ToastUtil.makeToast("用户" + mPref.getUser() + "登录成功!");
			startMainActivity();
			super.onPostExecute(result);
		}

	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
