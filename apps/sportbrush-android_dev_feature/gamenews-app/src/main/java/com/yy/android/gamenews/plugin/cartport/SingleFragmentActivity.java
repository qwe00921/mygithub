package com.yy.android.gamenews.plugin.cartport;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.yy.android.gamenews.ui.BaseActivity;
import com.yy.android.sportbrush.R;

public class SingleFragmentActivity extends BaseActivity {
	private static final String TAG = SingleFragmentActivity.class
			.getSimpleName();
	public static final String TAG_NAME_FRAGMENT = "fragment";
	public static final String TAG_FGMT_TYPE = "type";
	public static final String TAG_FGMT_CARTID = "brandId";
	public static final String TAG_FGMT_BRANDNAME = "brandName";
	public static final String TAG_FGMT_CAR_COLUMN = "carColumn";
	public static final int TYPE_CARTPORT = 1001;
	public static final int TYPE_CARTDETAIL = 1002;
	public static final int TYPE_CAR_COLUMN = 1003;

	private int mType;
	private int mBrandId;
	private Fragment mFragment;

	protected Fragment getFragmentByType(int type) {
		Fragment fgmt = null;
		Intent intent = getIntent();
		switch (type) {
		case TYPE_CARTPORT: {
			fgmt = new CartportFragment();
			break;
		}
		case TYPE_CARTDETAIL: {
			fgmt = new CartDetailFragment();
			break;
		}
		case TYPE_CAR_COLUMN: {
			fgmt = new CartDetailImageFragment();
			break;
		}
		}
		Bundle params = intent.getExtras();
		if (fgmt != null) {
			fgmt.setArguments(params);
		}
		return fgmt;
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Log.v(TAG, "onCreate");
		Intent intent = getIntent();
		setContentView(R.layout.activity_single_fragment);

		mFragment = getSupportFragmentManager().findFragmentByTag(
				TAG_NAME_FRAGMENT);
		if (intent != null) { // onSaveInstanceState里保存的当前选择的tab
			mType = intent.getIntExtra(TAG_FGMT_TYPE, 0);
			mBrandId = intent.getIntExtra(TAG_FGMT_CARTID, 0);
		}

	}

	@Override
	public void onResume() {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		if (mFragment == null) {
			mFragment = getFragmentByType(mType);
			transaction.add(R.id.container, mFragment, TAG_NAME_FRAGMENT);
		}
		transaction.show(mFragment);
		transaction.commitAllowingStateLoss();
		super.onResume();
	}

	public FragmentMessageListener getListener() {
		if (mFragment instanceof FragmentMessageListener) {
			return (FragmentMessageListener) mFragment;
		}
		return null;
	}
}
