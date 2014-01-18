package com.niuan.remoteconnector;

public interface RemoteConnectionObserver {
	public void onUpdate(Object tag, Object obj);
	public void onServiceDisconnected();
	public void onServiceConnected();
}
