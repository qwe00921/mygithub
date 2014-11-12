package com.yy.android.gamenews.ui;

import java.io.IOException;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.duowan.android.base.model.BaseModel;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.ui.common.UiUtils;
import com.yy.android.gamenews.util.AppDetailUpgradeTask;
import com.yy.android.gamenews.util.AppInitTask;
import com.yy.android.gamenews.util.AppInitTask.OnAppInitTaskListener;
import com.yy.android.gamenews.util.FileUtil;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.sportbrush.R;

public class WelcomeActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = WelcomeActivity.class.getSimpleName();
	private boolean mFromNotice = false;
	private AppInitTask mAppInitTask;
	private Button mDev;
	private Button mPre;
	private Button mIdc;
	private String ipUrl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.d(LOG_TAG, "[onCreate] +");
		// StatService.trackCustomEvent(this, "onCreate", "");
		setContentView(R.layout.activity_welcome);
		String channelName = getString(R.string.channelname);
		if ("test".equals(channelName) || "dev".equals(channelName)) {
			initTest();
		} else {
			init();
		}
	}

	private void initTest() {
		String ip = Preference.getInstance().getTestIp();
		String url = Preference.getInstance().getTestUrl();
		if (TextUtils.isEmpty(url)) {
			mDev = (Button) findViewById(R.id.dev);
			mPre = (Button) findViewById(R.id.pre);
			mIdc = (Button) findViewById(R.id.idc);
			mDev.setVisibility(View.VISIBLE);
			mPre.setVisibility(View.VISIBLE);
			mIdc.setVisibility(View.VISIBLE);
			mDev.setOnClickListener(this);
			mPre.setOnClickListener(this);
			mIdc.setOnClickListener(this);
		} else {
			BaseModel.HOST = url;
			showEnvToast(ip);
			init();
		}

	}

	private void init() {
		mAppInitTask = new AppInitTask(this);
		mAppInitTask.setOnAppInitTaskListener(mOnAppInitTaskListener);
		mAppInitTask.execute();
		copyAssetsData();
		handleIntent();

		String[] channelNames = getResources().getStringArray(
				R.array.custom_channel_name);
		if (channelNames != null && channelNames.length > 0) {
			for (String customChannelName : channelNames) {
				String currentChannelName = getString(R.string.channelname);
				if (customChannelName != null
						&& customChannelName.trim().equals(currentChannelName)) {
					findViewById(R.id.img_custom).setVisibility(View.VISIBLE);
					break;
				}
			}
		}
	}

	private void copyAssetsData() {
		AppDetailUpgradeTask mAppDetailUpgradeTask = new AppDetailUpgradeTask(
				this);
		mAppDetailUpgradeTask.execute();
	}

	private OnAppInitTaskListener mOnAppInitTaskListener = new OnAppInitTaskListener() {
		public void onTaskFinished() {
			if (!mFromNotice) {
				startActivity(new Intent(WelcomeActivity.this,
						MainActivity.class));
				finish();
			}
		};
	};

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mAppInitTask != null) {
			mAppInitTask.endTask();
		}
		super.onDestroy();
	}

	public void handleIntent() {
		mFromNotice = false;
		Intent intent = getIntent();
		if (intent != null) {
			int type = intent.getIntExtra(Constants.PUSH_TYPE, -1);
			long id = intent.getLongExtra(Constants.PUSH_ID, -1);
			String url = intent.getStringExtra(Constants.PUSH_URL);

			if (type != -1 && (id != -1 || url != null)) {
				Intent intent2 = new Intent(WelcomeActivity.this,
						MainActivity.class);
				if (id != -1) {
					intent2.putExtra(Constants.PUSH_ID, id);
				}
				if (url != null) {
					intent2.putExtra(Constants.PUSH_URL, url);
				}
				intent2.putExtra(Constants.PUSH_TYPE, type);
				startActivity(intent2);
				mFromNotice = true;
				finish();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dev:
			ipUrl = Constants.APP_DEV_IP;
			break;
		case R.id.pre:
			ipUrl = Constants.APP_PRE_IP;
			break;
		case R.id.idc:
			ipUrl = Constants.APP_IDC_IP;
			break;
		default:
			break;
		}
		mDev.setClickable(false);
		mPre.setClickable(false);
		mIdc.setClickable(false);
		EnvironmentAsynTask asyntast = new EnvironmentAsynTask(ipUrl);
		asyntast.execute();
	}

	public class EnvironmentAsynTask extends AsyncTask<Void, Void, Boolean> {

		private String mUrl;
		private Dialog urlDialogShow;

		public EnvironmentAsynTask(String url) {
			this.mUrl = url;
		}

		@Override
		protected void onPreExecute() {
			urlDialogShow = UiUtils.cleanCacheDialogShow(WelcomeActivity.this,
					getResources().getString(R.string.app_url_loading_title));
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				return sendUrlMessage(mUrl);
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			UiUtils.dialogDismiss(urlDialogShow);
			if (result) {
				showEnvToast(ipUrl);
				init();
			} else {
				ToastUtil.showToast("获取当前环境失败，请选择其它环境");
				mDev.setClickable(true);
				mPre.setClickable(true);
				mIdc.setClickable(true);
			}
		}

		private boolean sendUrlMessage(String url) throws IOException {

			byte[] data = FileUtil.download(url);
			if (data != null) {
				url = new String(data, "utf-8");
				BaseModel.HOST = url;
				Log.d(TAG, "BaseModel.HOST = " + BaseModel.HOST);
				Preference.getInstance().setTestUrl(url);
				Preference.getInstance().setTestIp(ipUrl);
				return true;
			} else {
				return false;
			}

		}
	}

	private void showEnvToast(String url) {
		String message = null;
		if (Constants.APP_DEV_IP.equals(url)) {
			message = "测试环境";
		} else if (Constants.APP_PRE_IP.equals(url)) {
			message = "预发布环境";
		} else if (Constants.APP_IDC_IP.equals(url)) {
			message = "正式环境";
		}
		ToastUtil.showToast(message);
	}

}
