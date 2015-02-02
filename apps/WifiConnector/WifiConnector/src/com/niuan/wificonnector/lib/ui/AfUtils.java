package com.niuan.wificonnector.lib.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class AfUtils {

	public static FragmentIndex buildIndex(Fragment fragment,
			FragmentActivity activity) {
		FragmentIndex fi = new FragmentIndex();
		List<Integer> mIndexStack = new ArrayList<Integer>();
		while (fragment != null) {
			FragmentManager manager = null;
			Fragment parentFragment = fragment.getParentFragment();
			if (parentFragment != null) {
				manager = parentFragment.getChildFragmentManager();
			} else {
				manager = activity.getSupportFragmentManager();
			}
			List<Fragment> list = manager.getFragments();
			if (list != null) {
				int index = list.indexOf(fragment);
				mIndexStack.add(index);
			}
			fragment = parentFragment;
		}
		fi.index = mIndexStack;
		return fi;
	}

	private static final String TAG = "AfUtils";

	public static Fragment getFragmentByIndex(FragmentActivity activity,
			FragmentIndex fi) {
		Fragment fragment = null;
		List<Integer> mIndexStack = fi.index;
		FragmentManager manager = activity.getSupportFragmentManager();
		for (int i = mIndexStack.size() - 1; i >= 0; i--) {
			Integer index = mIndexStack.get(i);
			if (index != null) {
				if (manager == null) {
					Log.w(TAG, "[getFragmentByIndex] manager = null, index = "
							+ index + ", return fragment = " + fragment);
					break;
				}
				List<Fragment> list = manager.getFragments();
				if (list != null && list.size() > index && index >= 0) {
					fragment = list.get(index);
				}
				if (fragment != null) {
					manager = fragment.getChildFragmentManager();
				}
			}
		}

		return fragment;
	}
}
