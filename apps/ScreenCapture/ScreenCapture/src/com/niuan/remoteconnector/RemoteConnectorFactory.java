package com.niuan.remoteconnector;

import com.niuan.remoteconnector.stream.socket.SocketConnector;
import com.niuan.remoteconnector.stream.socket.SocketServerConnector;


public class RemoteConnectorFactory {
	
	private static RemoteConnectorFactory mRemoteServiceConnectorFactory;
	private RemoteConnectorFactory() {};
	
	public static RemoteConnectorFactory getInstance() {
		synchronized(RemoteConnectorFactory.class) {
			if(mRemoteServiceConnectorFactory == null) {
				synchronized(RemoteConnectorFactory.class) {
					mRemoteServiceConnectorFactory = new RemoteConnectorFactory();
				}
			}
			return mRemoteServiceConnectorFactory;
		}
	}

	public RemoteConnector getConnector(RemoteConnectionInfo info) {
		if(info == null) {
			return null;
		}
		
		switch(info.getConnectionType()) {
			case C_BT: {
				break;
			}
			case C_USB: {
				break;
			}
			case C_SOCKET: {
				return new SocketConnector();
			}
			case S_BT: {
				break;
			}
			case S_USB: {
				break;
			}
			case S_SOCKET: {
				return new SocketServerConnector();
			}
		}
		
		return null;
	}
}
