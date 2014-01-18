package com.niuan.remoteconnector;

import com.niuan.remoteconnector.data.RemoteDataProcessor;
import com.niuan.remoteconnector.util.Log;
import com.niuan.remoteconnector.util.WaitableThread;
import com.niuan.remoteconnector.util.WaitableThread.Status;

public abstract class RemoteConnector {
	protected RemoteConnectionObserver mRemoteServiceObserver;
	private RemoteConnectionInfo mRemoteServiceInfo;
	private static final String TAG = "RemoteConnector";

	private RemoteDataProcessor mRemoteDataProcessor;
	
	/**
	 * Start get data and send to remote
	 * These operations are run in another thread so this method will return immediately
	 */
	public final void startAsync() {
	}
	
	public final void stop() throws RemoteConnectionException {
	}
	
	protected void resumeThread(WaitableThread waitableThread) {
		if(waitableThread != null) {
			waitableThread.updateStatus(Status.RESUME);
		} else {
			Log.e(TAG, "[resumeThread] waitableThread == null");
		}
	}
	
	protected void pauseThread(WaitableThread waitableThread) {
		if(waitableThread != null) {
			waitableThread.updateStatus(Status.PAUSE);
		} else {
			Log.e(TAG, "[pauseThread] waitableThread == null");
		}
	}
	public abstract boolean isConnected();
	public abstract boolean isConnectionAvaliable();
	
	protected void sendData(RemoteConnection connection, Object object) throws RemoteConnectionException {
		if(isConnected()) {
			if(object != null) {
				mRemoteDataProcessor.write(connection, object);
			}
		} else {
			
		}
	}
	protected abstract void onStreamException(Exception exception, RemoteConnection connection);
	public abstract void sendData(Object data) throws RemoteConnectionException ;
	
	public Object getData(RemoteConnection connection) throws RemoteConnectionException {
		return mRemoteDataProcessor.read(connection);
	}
	
	protected abstract void doConnect() throws RemoteConnectionException ;
	protected abstract void doDisconnect() throws RemoteConnectionException ;
	
	public final void connect() throws RemoteConnectionException {
		doConnect();
	}
	
	public final void disconnect() throws RemoteConnectionException {
		doDisconnect();
	}
	
	public void init(RemoteConnectionInfo info) {
		mRemoteServiceInfo = info;
		
		mRemoteDataProcessor = getRemoteDataProcessor();
	}
	
	protected abstract RemoteDataProcessor getRemoteDataProcessor();
	
	public abstract void reset();
	
	protected RemoteConnectionInfo getServiceInfo() {
		return mRemoteServiceInfo;
	}

	public void setRemoteServiceObserver(RemoteConnectionObserver lis) {
		mRemoteServiceObserver = lis;
	}
	
	protected void onReceiveDataFromRemote(RemoteConnection tag, Object value) {
		if(mRemoteServiceObserver != null) {
			mRemoteServiceObserver.onUpdate(tag, value);
		}
	}
}
