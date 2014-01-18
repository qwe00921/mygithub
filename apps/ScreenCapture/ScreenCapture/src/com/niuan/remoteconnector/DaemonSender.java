package com.niuan.remoteconnector;

import java.util.Vector;

import com.niuan.remoteconnector.util.WaitableThread;

public class DaemonSender extends WaitableThread 
{
	private int MAX_CACHE_SIZE = 1;
	private RemoteConnector mConnector;
	private RemoteConnection mConnection;
	public DaemonSender(RemoteConnector connector, RemoteConnection connection) {
		mConnector = connector;
		mConnection = connection;
	}
	
	private Vector<Object> mObjectVector = new Vector<Object>();
	
	public void send(Object object) {
		synchronized(this) {
			int size = mObjectVector.size();
			if(size > MAX_CACHE_SIZE) {
				mObjectVector.remove(size - 1);
			}
			mObjectVector.add(object);
		}
		
		if(getStatus() == Status.PAUSE) {
			updateStatus(Status.RESUME);
		}
	}
	
	@Override
	public void execute() {
		
		Object object = null;
		boolean isEmpty = false;
		synchronized(this) {
			object = mObjectVector.remove(0);
			isEmpty = mObjectVector.size() == 0;
		}
		
		try {
			mConnector.sendData(mConnection, object);
		} catch (RemoteConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mConnector.onStreamException(e, mConnection);
		}
		
		if(isEmpty) {
			updateStatus(Status.PAUSE);
		}
		
	}
}
