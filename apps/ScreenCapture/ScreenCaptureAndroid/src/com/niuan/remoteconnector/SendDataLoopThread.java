//package com.niuan.remoteconnector;
//
//import java.io.IOException;
//
//import com.niuan.remoteconnector.DataProcessObserver.Sender;
//import com.niuan.remoteconnector.RemoteConnector.GetDataStatus;
//
//public class SendDataLoopThread {
//	private Sender mSender;
//	public SendDataLoopThread(Sender sender) {
//		
//	}
//	
//	public void execute() {
//		Object data = null;
//		GetDataStatus status = GetDataStatus.FAIL;
//		
//		if(mSender != null) {
//			data = mSender.onRequest(null);
//		}
//		if(data != null) {
//			status = GetDataStatus.SUCCESS;
//		} else {
//			status = GetDataStatus.FAIL;
//		}
//		
//		try {
//			sendData(data);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//}
