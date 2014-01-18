package com.niuan.remoteconnector;

import com.niuan.remoteconnector.util.WaitableThread;

public class DaemonGetter extends WaitableThread {
	private RemoteConnector mConnector;
	private RemoteConnection mConnection;
	public DaemonGetter(RemoteConnector connector, RemoteConnection connection) {
		mConnector = connector;
		mConnection = connection;
	}
	public void execute() {
		Object data = null;
		try {
			data = mConnector.getData(mConnection);
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			mConnector.onStreamException(e, mConnection);
		}
		
		mConnector.onReceiveDataFromRemote(mConnection, data);
	}
	
}
