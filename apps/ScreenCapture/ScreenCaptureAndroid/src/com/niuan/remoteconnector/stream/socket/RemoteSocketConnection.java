package com.niuan.remoteconnector.stream.socket;

import java.net.Socket;

import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.stream.RemoteStreamConnection;

public class RemoteSocketConnection extends RemoteStreamConnection{
	public RemoteSocketConnection(RemoteConnector connector) {
		super(connector);
		// TODO Auto-generated constructor stub
	}

	private Socket mSocket;
	
	public Socket getSocket() {
		return mSocket;
	}

	public void setSocket(Socket socket) {
		this.mSocket = socket;
	}
	
	
}
