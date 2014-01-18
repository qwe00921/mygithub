package com.niuan.screencapture.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnectionObserver;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.RemoteConnectorManager;
import com.niuan.remoteconnector.data.DataConverterManager;
import com.niuan.remoteconnector.stream.socket.SocketConnectionInfo;
import com.niuan.remoteconnector.util.Log;
import com.niuan.screencapture.client.GraphicConfig;
import com.niuan.screencapture.client.MouseEvent;
import com.niuan.screencapture.client.Point;
import com.niuan.screencapture.util.ImageCaptureCache;
import com.niuan.screencapture.util.ObjectCache;
import com.niuan.screencapture.util.ScreenCaptureHelper;
import com.niuan.screencapture.util.ServerConfig;

public class ScreenShotServer {

	private static final String TAG = "ScreenShotServer";
	private ObjectCache<BufferedImage> mImageCache = new ImageCaptureCache();
	private GraphicConfig mConfig;
	private GraphicConfig mClientRequestConfig;
	
	public ScreenShotServer() {
		DataConverterManager.init();		
		mConfig = ScreenCaptureHelper.getDefaultConfig();
		
		RemoteConnectorManager manager = RemoteConnectorManager.getInstance();
		SocketConnectionInfo info = new SocketConnectionInfo();
		info.setConnectionType(RemoteConnectionInfo.ConnectionType.S_SOCKET);
		info.setSocketPort(ServerConfig.getServerPort());
		final RemoteConnector connector = manager.getRemoteServiceConnector(info);
		
		try {
			mRobot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		connector.setRemoteServiceObserver(new RemoteConnectionObserver() {
			
			@Override
			public void onUpdate(Object tag, Object obj) {
				Log.d(TAG, "[onUpdateAsync] obj = " + obj);
				
				if(obj != null) {
					if(obj instanceof MouseEvent) {
						MouseEvent event = (MouseEvent)obj;
						switch(event.getEvent()) {
							case MouseEvent.LEFT_CLICK: {
								Log.d(TAG, "left_click");
								leftClick(event.getPoint1());
								
								break;
							}
							case MouseEvent.RIGHT_CLICK: {
								Log.d(TAG, "right_click");
								
								rightClick(event.getPoint1());
								break;
							}
							
							case MouseEvent.MOVE: {
									
								mouseMove(event.getPoint1());
								break;
							}
							
							case MouseEvent.LEFT_DOUBLE_CLICK: {
								leftDoubleClick(event.getPoint1());
								break;
							}
							
						}
					} else if(obj instanceof GraphicConfig) {
						synchronized(this) {
							mClientRequestConfig = (GraphicConfig) obj;
						}
					}
				}
			}
			
			@Override
			public void onServiceDisconnected() {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void onServiceConnected() {
				connector.startAsync();
				
//				Thread tt = new Thread() {
//					@Override
//					public void run() {
						try {
							connector.sendData(ScreenCaptureHelper.getDefaultConfig());
						} catch (RemoteConnectionException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
//					}
//				};
				
				Thread t = new Thread() {
					public void run() {
						while(true) {
							try {
								
								if(mClientRequestConfig != null) {
									synchronized(this) {
										try {
											connector.sendData(mClientRequestConfig);
										} catch (RemoteConnectionException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										mConfig = mClientRequestConfig;
										mClientRequestConfig = null;
									}
								}
								
								connector.sendData(ScreenCaptureHelper.snapshot(mConfig));
							} catch (RemoteConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
				};
				
				t.start();
			}
			
		});
		
		try {
			connector.connect();
		} catch (RemoteConnectionException e) {
			return;
		} 
	}
	
	private void dragStart(Point point) {
		
	}
	
	private void drag(Point point) {
		
	}
	
	private void dragEnd(Point point) {
		
	}
	
	private Robot mRobot;
	private void mouseMove(Point point) {
		if(mRobot != null) {
			if(point != null) {
				mRobot.mouseMove((int)point.getX(), (int)point.getY());
			}
		}
	}
	
	private void leftClick(Point point) {
		if(point != null) {
			mouseMove(point);
			leftClick();
		}
	}
	
	private void leftDoubleClick(Point point) {
		if (point != null) {
			mouseMove(point);
			leftDoubleClick();
		}
	}
	
	private void leftClick() {
		if(mRobot != null) {
			mRobot.mousePress(InputEvent.BUTTON1_MASK);
			mRobot.mouseRelease(InputEvent.BUTTON1_MASK);
		}
	}
	
	private void leftDoubleClick() {
		leftClick();
		leftClick();
	}
	
	private void rightClick(Point point) {


		if(point != null) {
			mouseMove(point);
			rightClick();
		}
	}
	
	private void keyPress() {
		if(mRobot != null)	 {

//			mRobot.keyPress(KeyEvent.);
		}
	}
	
	private void rightClick() {
		if(mRobot != null)	 {

			mRobot.mousePress(InputEvent.BUTTON3_MASK);
			mRobot.mouseRelease(InputEvent.BUTTON3_MASK);
		}
	}
	
	public static void main(String[] args) {
		new ScreenShotServer();
	}
}
