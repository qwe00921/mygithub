package com.niuan.wificonnector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.niuan.wificonnector.lib.ui.BaseFragment;
import com.niuan.wificonnector.util.ToastUtil;
import com.niuan.wificonnector.util.WebViewUtils;
import com.niuan.wificonnector.util.thread.BackgroundTask;

public class PersonalFragment extends BaseFragment {
	private EditText mPhoneNumber;
	private Button mBtnRegister;
	private Button mBtnLogin;
	private Button mBtnLogout;
	private Preference mPref = Preference.getInstance();
	private View mLayoutRegister;
	private View mLayoutLogin;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_user_center, null);
		mPhoneNumber = (EditText) view.findViewById(R.id.et_phone_number);
		mBtnRegister = (Button) view.findViewById(R.id.btn_register);
		mBtnLogin = (Button) view.findViewById(R.id.btn_login);
//		mBtnLogout = (Button) view.findViewById(R.id.btn_logout);
		mLayoutRegister = view.findViewById(R.id.layout_register);
		mLayoutLogin = view.findViewById(R.id.layout_login);

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

		mBtnLogout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mBtnLogout.setEnabled(false);
				new LogoutTask().execute();
				ToastUtil.makeToast("正在退出登录，请稍候...");
			}
		});
		refresh();

		return view;
	}

	private void refresh() {
		if (!"".equals(mPref.getUser())) {
			mLayoutLogin.setVisibility(View.VISIBLE);
			mLayoutRegister.setVisibility(View.GONE);
		} else {
			mLayoutLogin.setVisibility(View.GONE);
			mLayoutRegister.setVisibility(View.VISIBLE);
		}
	}

	public class RegisterTask extends BackgroundTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebViewUtils.loadByWebView(getActivity(),
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
			selectWifi();
			ToastUtil.makeToast("注册成功!");
			mPref.saveUser(mPhoneNumber.getText().toString());
			super.onPostExecute(result);
		}

	}

	public class LoginTask extends BackgroundTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebViewUtils.loadByWebView(getActivity(),
						"http://ps.exands.com:38007/ux/");
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			selectWifi();
			ToastUtil.makeToast("用户" + mPref.getUser() + "登录成功!");
			super.onPostExecute(result);
		}
	}

	private void selectWifi() {
		WifiListActivity.startActivity(getActivity());
	}

	public class LogoutTask extends BackgroundTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				WebViewUtils.loadByWebView(getActivity(),
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
			mPref.saveUser("");
			ToastUtil.makeToast("用户" + mPref.getUser() + "已退出登录!");
			refresh();
			super.onPostExecute(result);
		}

	}
}
