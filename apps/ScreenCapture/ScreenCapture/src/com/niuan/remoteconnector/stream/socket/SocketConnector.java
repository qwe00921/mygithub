package com.niuan.remoteconnector.stream.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.niuan.remoteconnector.RemoteConnection;
import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.data.RemoteDataProcessor;
import com.niuan.remoteconnector.stream.RemoteStreamDataProcessor;
import com.niuan.remoteconnector.util.Log;

public class SocketConnector extends RemoteConnector {

	private RemoteSocketConnection mConnection;

	private static final String TAG = "SocketConnector";
	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return mConnection != null && mConnection.getSocket().isConnected();
	}

	@Override
	public boolean isConnectionAvaliable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(RemoteConnectionInfo info) {
		super.init(info);
		if(info instanceof SocketConnectionInfo) {
			
		} else {
			//TODO: Throw exception, not a socket service info.
		}
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendData(Object object) throws RemoteConnectionException {
		sendData(mConnection, object);
	}

	@Override
	protected void doConnect() throws RemoteConnectionException {
		SocketConnectionInfo info = getServiceInfo();
		Socket socket;
		try {
			socket = new Socket(info.getSocketAddress(), info.getSocketPort());
			mConnection = new RemoteSocketConnection(this);
			mConnection.setInputStream(new BufferedInputStream(socket.getInputStream()));
			mConnection.setOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			mConnection.setSocket(socket);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void doDisconnect() throws RemoteConnectionException {
		if(mConnection == null) {
			Log.e(TAG, "");
			return;
		}
		try {
			mConnection.getSocket().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected SocketConnectionInfo getServiceInfo() {
		return (SocketConnectionInfo) super.getServiceInfo();
	}

	private RemoteDataProcessor mRemoteDataProcessor;
	@Override
	protected RemoteDataProcessor getRemoteDataProcessor() {
		if(mRemoteDataProcessor == null) {
			mRemoteDataProcessor = new RemoteStreamDataProcessor();
		}
		return mRemoteDataProcessor;
	}

	@Override
	protected void onStreamException(Exception exception,
			RemoteConnection connection) {
		// TODO Auto-generated method stub
		
	}

}
