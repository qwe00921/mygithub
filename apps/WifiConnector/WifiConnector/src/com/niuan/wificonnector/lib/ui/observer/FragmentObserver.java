package com.niuan.wificonnector.lib.ui.observer;

import com.niuan.wificonnector.lib.ui.AppEvent;
import com.niuan.wificonnector.lib.ui.FragmentIndex;

public interface FragmentObserver {
	public void onFragmentEvent(AppEvent event, FragmentIndex fi, Object... params);
}
