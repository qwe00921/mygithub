//package com.niuan.remoteconnector.stream.socket;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//
//import com.niuan.remoteconnector.util.WaitableThread.Status;
//
//public class ConnectedSocket {
//	private Socket mSocket;
//	private InputStream mInputStream;
//	private OutputStream mOutputStream;
//	private Object mTag;
//	private GetDataThread mGetDataThread;
//	private SendDataThread mSendDataThread;
//	
//	public ConnectedSocket(Socket socket) throws IOException {
//		mSocket = socket;
//		mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
//		mInputStream = new BufferedInputStream(socket.getInputStream());
//		
//		mTag = new Object();
//		mGetDataThread = new GetDataThread(this);
//		mGetDataThread.start();
//		
//		mGetDataThread.updateStatus(Status.RESUME);
//		
//		mSendDataThread = new SendDataThread(this);
//		mSendDataThread.start();
//	}
//	
//	public void sendDataAsync(Object object) {
//		mSendDataThread.send(object);
//	}
//	
//	public InputStream getInputStream() {
//		return mInputStream;
//	}
//	
//	public OutputStream getOutputStream() {
//		return mOutputStream;
//	}
//	
//	public Object getTag() {
//		return mTag;
//	}
//	
//	public Socket getSocket() {
//		return mSocket;
//	}
//	
//	public void destory() {
//		if(mGetDataThread != null) {
//			mGetDataThread.updateStatus(Status.EXIT);
//		}
//	}
//	
//	@Override
//	public boolean equals(Object object) {
//		
//		if(object instanceof ConnectedSocket) {
//			ConnectedSocket target = (ConnectedSocket)object;
//			if(target.mTag.equals(mTag)) {
//				return true;
//			}
//		}
//		return super.equals(object);
//	}
//	
//	public void setSocketRequestObserver(SocketConnectionObserver observer) {
//		mGetDataThread.setSocketRequestObserver(observer);
//	}
//	
//	public interface SocketConnectionObserver {
//		public void onReceiveDataFromRemote(Object value);
//		public void onException(Exception e);
//	}
//}