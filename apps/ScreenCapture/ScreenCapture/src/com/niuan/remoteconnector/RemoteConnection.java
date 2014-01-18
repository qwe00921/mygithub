package com.niuan.remoteconnector;

import com.niuan.remoteconnector.util.WaitableThread.Status;



public class RemoteConnection {

	private DaemonGetter mGetDataThread;
	private DaemonSender mSendDataThread;
	
	public RemoteConnection(RemoteConnector connector) {
		
		mGetDataThread = new DaemonGetter(connector, this);
		mGetDataThread.start();
		
		mGetDataThread.updateStatus(Status.RESUME);
		
		mSendDataThread = new DaemonSender(connector, this);
		mSendDataThread.start();
	}
	
	public void sendDataAsync(Object object) {
		mSendDataThread.send(object);
	}
	
	public void destory() {
		if(mGetDataThread != null) {
			mGetDataThread.updateStatus(Status.EXIT);
		}
	}
}
