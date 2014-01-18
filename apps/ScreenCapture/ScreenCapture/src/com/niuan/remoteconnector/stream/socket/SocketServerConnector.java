package com.niuan.remoteconnector.stream.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnection;
import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.data.RemoteDataProcessor;
import com.niuan.remoteconnector.stream.RemoteStreamDataProcessor;
import com.niuan.remoteconnector.util.WaitableThread;

public class SocketServerConnector extends RemoteConnector {
	private ServerSocket mServerSocket;
	private List<RemoteConnection> mClientSocketList = new ArrayList<RemoteConnection>();
	@Override
	public boolean isConnected() {
		return mClientSocketList.size() > 0;
	}

	@Override
	public boolean isConnectionAvaliable() {
		return false;
	}

	@Override
	public void sendData(Object object) {

		for (RemoteConnection out : mClientSocketList) {
//			out.sendDataAsync(object);
			try {
				sendData(out, object);
			} catch (RemoteConnectionException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doConnect() throws RemoteConnectionException {
		resumeThread(mServerThread);
	}

	protected void onStreamException(Exception exception, RemoteConnection connection) {
		
		remove(connection);
	}
	
	private void remove(RemoteConnection socket){
		synchronized(this) {
			if(mClientSocketList == null) {
				return;
			}
			socket.destory();
			mClientSocketList.remove(socket);
		}
	}
	
	@Override
	protected void doDisconnect() throws RemoteConnectionException {
		
	}
	
	@Override
	protected SocketConnectionInfo getServiceInfo() {
		return (SocketConnectionInfo) super.getServiceInfo();
	}


	@Override
	public void init(RemoteConnectionInfo info) {
		super.init(info);
		if(info instanceof SocketConnectionInfo) {

			mServerThread.start();
		} else {
			//TODO: Throw exception, not a socket service info.
		}
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	private ServerThread mServerThread = new ServerThread();
	private class ServerThread extends WaitableThread {

		@Override
		public void execute() {
			try {
				if(mServerSocket == null) {
					SocketConnectionInfo info = getServiceInfo();
					if(info != null) {
						mServerSocket = new ServerSocket(info.getSocketPort());
					}
				}
				Socket socket = mServerSocket.accept();
				RemoteSocketConnection csocket = new RemoteSocketConnection(SocketServerConnector.this);
				csocket.setInputStream(new BufferedInputStream(socket.getInputStream()));
				csocket.setOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				csocket.setSocket(socket);
				
				synchronized(this) {
					
					if(mClientSocketList != null) {
						mClientSocketList.add(csocket);
						if(mClientSocketList.size() == 1) {
							mRemoteServiceObserver.onServiceConnected();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private RemoteDataProcessor mRemoteDataProcessor;
	@Override
	protected RemoteDataProcessor getRemoteDataProcessor() {
		if(mRemoteDataProcessor == null) {
			mRemoteDataProcessor = new RemoteStreamDataProcessor();
		}
		return mRemoteDataProcessor;
	}
}
