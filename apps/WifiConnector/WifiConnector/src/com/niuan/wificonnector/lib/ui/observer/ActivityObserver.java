package com.niuan.wificonnector.lib.ui.observer;

import com.niuan.wificonnector.lib.ui.AppEvent;

public interface ActivityObserver {

	public void onActivityEvent(AppEvent event, Object... params);
}
