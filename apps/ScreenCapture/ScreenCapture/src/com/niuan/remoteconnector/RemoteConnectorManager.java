package com.niuan.remoteconnector;

import java.util.HashMap;
import java.util.Map;

public class RemoteConnectorManager {
	
	private Map<RemoteConnectionInfo, RemoteConnector> mConnectionMap;
	
	private static RemoteConnectorManager mRemoteServiceManager = new RemoteConnectorManager();
	
	private RemoteConnectorManager() {
		mConnectionMap = new HashMap<RemoteConnectionInfo, RemoteConnector>();
	}
	
	public static RemoteConnectorManager getInstance() {
		return mRemoteServiceManager;
	}
	
	public RemoteConnector getRemoteServiceConnector(RemoteConnectionInfo info) {

		RemoteConnector connector = null;
		if(mConnectionMap.containsKey(info)) {
			connector = mConnectionMap.get(info);
		}
		if(connector == null) {
			connector = RemoteConnectorFactory.getInstance().getConnector(info);
			
			if(connector != null) {
				connector.init(info);
			}
			mConnectionMap.put(info, connector);
		}
		
		return connector;
	}
	
//	public void connectService(RemoteServiceInfo info, RemoteServiceObserver observer) {
//		RemoteServiceConnector connector = mConnectionMap.get(info);
//		if(connector == null) {
//			connector = RemoteServiceConnectorFactory.getInstance().getConnector(info);
//			mConnectionMap.put(info, connector);
//		}
//		connector.setRemoteServiceObserver(observer);
//		connector.init(info);
//		connector.connect();
//	}
//	
//	public void disconnectService(RemoteServiceInfo info) {
//		RemoteServiceConnector connector = mConnectionMap.get(info);
//		if(connector != null) {
//			connector.disconnect();
//			connector.reset();
//		}
//	}
//	
//	public boolean isConnected() {
//		
//		return false;
//	}
//	public boolean isConnectionAvaliable() {
//		return false;
//	}
	
//	public void init(RemoteServiceInfo info) {
//		
//	}
//	
//	public void reset() {
//		
//	}

//	public abstract void start();
//	public abstract void stop();
//	public abstract void connect();
//	public abstract void disconnect();
//	public abstract boolean isConnected();
//	public abstract boolean isConnectionAvaliable();
//	
//	public abstract void sendData(byte[] data);
//	public abstract void setRemoteServiceObserver(RemoteServiceObserver lis);
}
