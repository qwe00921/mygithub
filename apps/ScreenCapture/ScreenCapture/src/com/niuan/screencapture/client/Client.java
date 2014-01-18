package com.niuan.screencapture.client;




import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnectionObserver;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.RemoteConnectorManager;
import com.niuan.remoteconnector.stream.socket.SocketConnectionInfo;
import com.niuan.screencapture.util.ServerConfig;

public class Client {
	private Socket mSocket;
	private InputStream mInputStream;
	private static final String TAG = "Client";
	JFrame frame = new JFrame();
	JLabel label;
	ImageIcon icon = new ImageIcon();
	public Client() {
		initUI();
//		try {
//			mSocket = new Socket(ServerConfig.getServerAddr(), ServerConfig.getServerPort());
//			mInputStream = mSocket.getInputStream();
//			while(true) {
//				BufferedImage image = ScreenCaptureHelper.read(mInputStream);
////				CaptureUtils.saveToFile(image);
//				showToScreen(image);
//			}
//		} catch (IOException e) {
//		}
		
		
		RemoteConnectorManager manager = RemoteConnectorManager.getInstance();
		SocketConnectionInfo info = new SocketConnectionInfo();
		info.setConnectionType(RemoteConnectionInfo.ConnectionType.C_SOCKET);
		info.setSocketPort(ServerConfig.getServerPort());
		info.setSocketAddress("127.0.0.1");
		RemoteConnector connector = manager.getRemoteServiceConnector(info);
		try {
			connector.connect();
		} catch (RemoteConnectionException e) {
			return;
		} 
		
		connector.setRemoteServiceObserver(new RemoteConnectionObserver() {
			
			@Override
			public void onUpdate(Object tag, Object obj) {
				// TODO Auto-generated method stub
				showToScreen((BufferedImage) obj);
			}
			
			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onServiceConnected() {
				// TODO Auto-generated method stub
				
			}
			
//			@Override
//			public Object onRequestAsync() {
//				Log.d(TAG, "onRequest"); 
//				return ScreenCaptureHelper.snapshot();
//			}
		});
		connector.startAsync();
	}
	
	private int height = 900;
	private int width = 1600;
	private void initUI() {
		frame.setSize(width, height);
		label = new JLabel(icon); 
		label.setSize(width, height);
		frame.add(label);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
	}
	
	private void showToScreen(BufferedImage image) {
		if(image != null) {

			icon.setImage(image);
			frame.repaint();
		}
	}
	
	public static void main(String[] args) {
		new Client();
	}
}
