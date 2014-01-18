package com.niuan.remoteconnector.stream.socket;

import com.niuan.remoteconnector.RemoteConnectionInfo;

public class SocketConnectionInfo extends RemoteConnectionInfo {

	private String mSocketAddress;
	private int mSocketPort;

	public String getSocketAddress() {
		return mSocketAddress;
	}

	public void setSocketAddress(String socketAddress) {
		this.mSocketAddress = socketAddress;
	} 

	public int getSocketPort() {
		return mSocketPort;
	}

	public void setSocketPort(int socketPort) {
		this.mSocketPort = socketPort;
	}

}
