package com.niuan.wificonnector.lib.ui;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.niuan.wificonnector.lib.ui.observer.ActivityObserver;
import com.niuan.wificonnector.lib.ui.observer.FragmentObserver;

public class BaseDialogFragment extends DialogFragment implements
		ActivityObserver {

	@Override
	public void onActivityEvent(AppEvent event, Object... params) {

	}

	private FragmentObserver mObserver;

	public FragmentObserver getFragmentObserver() {
		return mObserver;
	}

	@Override
	public void onAttach(Activity activity) {
		if (activity instanceof BaseActivity) {
			mObserver = (BaseActivity) activity;
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		mObserver = null;
		super.onDetach();
	}

	private FragmentIndex mNotifyIndex;

	public FragmentIndex getNotifyIndex() {
		return mNotifyIndex;
	}

	public void registerFragmentObserver(FragmentIndex index) {
		mNotifyIndex = index;
	}
}
