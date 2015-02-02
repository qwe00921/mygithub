package com.niuan.wificonnector.lib.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.niuan.wificonnector.lib.ui.observer.ActivityObserver;
import com.niuan.wificonnector.lib.ui.observer.FragmentObserver;

public class BaseActivity extends FragmentActivity implements FragmentObserver {

	private boolean mIsInstanceSaved;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mIsInstanceSaved = true;
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		mIsInstanceSaved = false;
		super.onResume();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mIsInstanceSaved = false;
		super.onRestoreInstanceState(savedInstanceState);
	}

	public boolean isInstanceSaved() {
		return mIsInstanceSaved;
	}

	@Override
	public void onFragmentEvent(AppEvent event, FragmentIndex fi,
			Object... params) {
		if (fi != null) {
			Fragment fragment = AfUtils.getFragmentByIndex(this, fi);
			if (fragment instanceof ActivityObserver) {
				((ActivityObserver) fragment).onActivityEvent(event, params);
				return;
			}
		}

	}
}
