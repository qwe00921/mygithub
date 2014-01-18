package com.niuan.remoteconnector.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;

import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnectionObserver;
import com.niuan.remoteconnector.util.Log;
import com.niuan.remoteconnector.util.WaitableThread;
import com.niuan.remoteconnector.util.WaitableThread.Status;

public abstract class RemoteStreamConnector {
	protected RemoteConnectionObserver mRemoteServiceObserver;
	private RemoteConnectionInfo mRemoteServiceInfo;
	private static final String TAG = "RemoteConnector";

	private RemoteStreamDataProcessor mRemoteDataProcessor;
	
	/**
	 * Start get data and send to remote
	 * These operations are run in another thread so this method will return immediately
	 */
	public final void startAsync() {
	}
	
	public final void stop() {
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
	
	/**
	 * Method to subjectively send data to server, the object sent to data will be processed by RemoteDataHolder
	 * that is set by setDataHolder.
	 * 
	 * @param object the object will be sent to server
	 * @throws IOException
	 */
	protected void sendData(Object object, OutputStream out) throws IOException {
		if(isConnected()) {
			if(object instanceof byte[]) {
				byte[] data = (byte[]) object;
				out.write(data);
				
				return;
			}
			if(object != null) {
				mRemoteDataProcessor.write(out, object);
			}
		} else {

			//TODO: Throw Exception, try sending data when connection is broken or not connected.
		}
	}
	
	public abstract void sendData(Object data) throws IOException;
	
	/**
	 * Subjectively request get data from server, this method use RemoteDataHolder that is set by setDataHolder
	 * to read object from remote.
	 * @return Object  the object that remote sent to 
	 * @throws IOException
	 */
	public Object getData(InputStream in) throws IOException {
		return mRemoteDataProcessor.read(in);
	}
	
	protected abstract void doConnect() throws IOException;
	protected abstract void doDisconnect() throws IOException;
	
	public final void connect() throws IOException{
		doConnect();
	}
	
	public final void disconnect() throws IOException {
		doDisconnect();
	}
	
	public void init(RemoteConnectionInfo info) {
		mRemoteServiceInfo = info;
		
		mRemoteDataProcessor = new RemoteStreamDataProcessor();
		
//		mSendDataThread = new SendDataLoopThread();
//		mSendDataThread.start();
//		
//		mGetDataThread = new GetDataLoopThread();
//		mGetDataThread.start();
	}
	public abstract void reset();
	
	protected RemoteConnectionInfo getServiceInfo() {
		return mRemoteServiceInfo;
	}

	public void setRemoteServiceObserver(RemoteConnectionObserver lis) {
		mRemoteServiceObserver = lis;
	}
	
//	protected abstract void update(Object object, GetDataStatus status) throws IOException;
//	
//	protected abstract Object request() throws IOException;
	
//	private class SendDataLoopThread extends WaitableThread {
//
//		public void execute() {
//			Object data = null;
//			GetDataStatus status = GetDataStatus.FAIL;
//			
//			if(mRemoteServiceObserver != null) {
//				data = mRemoteServiceObserver.onRequestAsync();
//			}
//			if(data != null) {
//				status = GetDataStatus.SUCCESS;
//			} else {
//				status = GetDataStatus.FAIL;
//			}
//			
//			try {
//				sendData(data);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}
	
//	protected void sendDataToRemote() {
//		Object data = null;
////		GetDataStatus status = GetDataStatus.FAIL;
//		
//		if(mRemoteServiceObserver != null) {
//			data = mRemoteServiceObserver.onRequestAsync();
//		}
////		if(data != null) {
////			status = GetDataStatus.SUCCESS;
////		} else {
////			status = GetDataStatus.FAIL;
////		}
//		
//		try {
//			sendData(data);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
	protected void onReceiveDataFromRemote(Object tag, Object value) {
		if(mRemoteServiceObserver != null) {
			mRemoteServiceObserver.onUpdate(tag, value);
		}
	}
	
//	protected void getData(Object tag, Object value) {
//
//		if(mRemoteServiceObserver != null) {
//			mRemoteServiceObserver.onUpdateAsync(data, status);
//		}
//	}
//	
//	private class GetDataLoopThread extends WaitableThread {
//
//		public void execute() {
//			Object data = null;
//			GetDataStatus status = GetDataStatus.FAIL;
//			
//			InputStream[] isArray = getInputStream();
//			if(isArray != null) {
//				for(InputStream in : isArray) {
//					try {
//						data = getData(in);
//						if(data != null) {
//							status = GetDataStatus.SUCCESS;
//						} else {
//							status = GetDataStatus.FAIL;
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//						status = GetDataStatus.NETWORK_ERROR;
//						onInputStreamException(e, in);
//					}
//
//					if(mRemoteServiceObserver != null) {
//						mRemoteServiceObserver.onUpdateAsync(data, status);
//					}
//					if(status == GetDataStatus.NETWORK_ERROR) {
//						updateStatus(Status.PAUSE);
//					}
//				}
//			}
//		}
//	}
	
	protected void onStreamException(Exception exception, ConnectedSocket socket) {
		
	}
	
	protected class GetDataThread extends WaitableThread {
		
		private ConnectedSocket mSocket;
		public GetDataThread(ConnectedSocket socket) {
			mSocket = socket;
		}
		public void execute() {
			Object data = null;

			try {
				data = getData(mSocket.getInputStream());
				onReceiveDataFromRemote(mSocket.getTag(), data);
			} catch (IOException e) {
				e.printStackTrace();
				onStreamException(e, mSocket);
				updateStatus(Status.PAUSE);
			}
			
		}
	}
	
	protected class SendDataThread extends WaitableThread {

		
		private int MAX_CACHE_SIZE = 1;
		private ConnectedSocket mSocket;
		public SendDataThread(ConnectedSocket socket) {
			mSocket = socket;
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
				sendData(object, mSocket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(isEmpty) {
				updateStatus(Status.PAUSE);
			}
			
		}
	}
	
	protected class ConnectedSocket {
		private Socket mSocket;
		private InputStream mInputStream;
		private OutputStream mOutputStream;
		private Object mTag;
		private GetDataThread mGetDataThread;
		private SendDataThread mSendDataThread;
		
		public ConnectedSocket(Socket socket) throws IOException {
			mSocket = socket;
			mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
			mInputStream = new BufferedInputStream(socket.getInputStream());
			
			mTag = new Object();
			mGetDataThread = new GetDataThread(this);
			mGetDataThread.start();
			
			mGetDataThread.updateStatus(Status.RESUME);
			
			mSendDataThread = new SendDataThread(this);
			mSendDataThread.start();
		}
		
		public void sendDataAsync(Object object) {
			mSendDataThread.send(object);
		}
		
		public InputStream getInputStream() {
			return mInputStream;
		}
		
		public OutputStream getOutputStream() {
			return mOutputStream;
		}
		
		public Object getTag() {
			return mTag;
		}
		
		public Socket getSocket() {
			return mSocket;
		}
		
		public void destory() {
			if(mGetDataThread != null) {
				mGetDataThread.updateStatus(Status.EXIT);
			}
		}
		
		@Override
		public boolean equals(Object object) {
			
			if(object instanceof ConnectedSocket) {
				ConnectedSocket target = (ConnectedSocket)object;
				if(target.mTag.equals(mTag)) {
					return true;
				}
			}
			return super.equals(object);
		}
	}
}
