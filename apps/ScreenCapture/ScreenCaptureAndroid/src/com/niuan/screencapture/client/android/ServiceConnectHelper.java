package com.niuan.screencapture.client.android;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnectionObserver;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.RemoteConnectorManager;
import com.niuan.remoteconnector.data.DataConverterManager;
import com.niuan.remoteconnector.stream.socket.SocketConnectionInfo;
import com.niuan.screencapture.client.GraphicConfig;

public class ServiceConnectHelper {

	private static GraphicConfig mServerDefaultConfig;
	
	public static final GraphicConfig getServerDefaultConfig() {
		return mServerDefaultConfig;
	}
	
	public static RemoteConnector connect(String address, int port, final OnConnectSuccessListener listener) {
		DataConverterManager.init();
		RemoteConnectorManager manager = RemoteConnectorManager.getInstance();
		SocketConnectionInfo info = new SocketConnectionInfo();
		info.setConnectionType(SocketConnectionInfo.ConnectionType.C_SOCKET);
		info.setSocketAddress(address);
		info.setSocketPort(port);
		RemoteConnector connector = manager.getRemoteServiceConnector(info);
		
		connector.setRemoteServiceObserver(new RemoteConnectionObserver() {
			
			@Override
			public void onUpdate(Object tag, Object obj) {
				if(obj instanceof GraphicConfig) {
					mServerDefaultConfig = (GraphicConfig) obj;
					if(listener != null) {
						listener.onSuccess();	
					}
				}
			}

			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onServiceConnected() {
				// TODO Auto-generated method stub
				
			}
		});
		try {
			connector.connect();
		} catch (RemoteConnectionException e) {
			e.printStackTrace();
			connector = null;
		}
		
		return connector;
	}
	
	public interface OnConnectSuccessListener {
		public void onSuccess();
	}
}
