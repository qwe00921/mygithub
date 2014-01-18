package com.niuan.screencapture.client.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ScrollView;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnectionInfo;
import com.niuan.remoteconnector.RemoteConnectionObserver;
import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.remoteconnector.RemoteConnectorManager;
import com.niuan.remoteconnector.stream.socket.SocketConnectionInfo;
import com.niuan.screencapture.client.GraphicConfig;
import com.niuan.screencapture.client.MouseEvent;
import com.niuan.screencapture.client.Point;
import com.niuan.screencapture.client.Size;
import com.niuan.screencapture.client.android.view.ImageUtils;
import com.niuan.screencapture.client.android.view.ScreenView;
import com.niuan.screencapture.client.android.view.ScreenView.OnScreenChangeListener;
import com.niuan.screencapture.util.ServerConfig;

public class ScreenViewerActivity extends Activity {

	private static final int MSG_UPDATE_IMAGE = 1;
	private static final String TAG = "MainActivity";
	private ScreenView mScreenView;
	private ScrollView mScrollView;
	
	private SocketConnectionInfo mConnectionInfo;
	
	private RemoteConnector mRemoteConnector;
	private GraphicConfig mServerDefaultConfig;
	
	private Size testSize;
	private Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case MSG_UPDATE_IMAGE: {
					Log.d(TAG, "MSG_UPDATE_IMAGE");
					Bitmap bmp = (Bitmap) msg.obj;
					if(bmp != null) {
						Log.d(TAG, "MSG_UPDATE_IMAGE bmp = " + bmp);
						int bmpWidth = bmp.getWidth();
						int bmpHeight = bmp.getHeight();
						if(bmpWidth != mRemoteBmpWidth || bmpHeight != mRemoteBmpHeight) {
							mRemoteBmpWidth = bmpWidth;
							mRemoteBmpHeight = bmpHeight;
							mBitmapSize = new Size(bmpWidth, bmpHeight);
						}
						mScreenView.setImageBitmap(null);
						
						Bitmap fixedBmp = null;
						if(mServerDefaultConfig != null) {
							if(mCurrentConfig == null) {
								mCurrentConfig = mServerDefaultConfig;
							}
							if(mBitmapSize != null && mBitmapSize.equals(mCurrentConfig.getSize())) {
								fixedBmp = getFixedBmp(mCurrentConfig.getStartPoint(), mServerDefaultConfig.getSize(), bmp);
							} else {
								fixedBmp = bmp;
							}
						} else {
							fixedBmp = bmp;
						}
							
						mScreenView.setImageBitmap(fixedBmp);
					}
				}
				case 2: {
	
					if(mConnectionInfo == null) {
						mConnectionInfo = new SocketConnectionInfo();
						mConnectionInfo.setConnectionType(RemoteConnectionInfo.ConnectionType.C_SOCKET);
						mConnectionInfo.setSocketAddress(ServerConfig.getServerAddr());
						mConnectionInfo.setSocketPort(ServerConfig.getServerPort());
					}
					RemoteConnectorManager manager = RemoteConnectorManager.getInstance();
					mRemoteConnector = manager.getRemoteServiceConnector(mConnectionInfo);
					mRemoteConnector.setRemoteServiceObserver(new RemoteConnectionObserver() {
						
						@Override
						public void onUpdate(Object tag, Object obj) {
							
							if(obj instanceof Bitmap) {

								Bitmap bmp = (Bitmap)obj;
								Message msg = mUIHandler.obtainMessage(MSG_UPDATE_IMAGE);
								msg.obj = bmp;
								mUIHandler.sendMessage(msg);
							} else if(obj instanceof GraphicConfig) {
								
								GraphicConfig config = (GraphicConfig)obj;
								
								if(mRequestConfig != null) {
									if(mRequestConfig.equals(config)) {
										mCurrentConfig = mRequestConfig;
										mRequestConfig = null;
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
							// TODO Auto-generated method stub
							
						}
	
					});
					
					mRemoteConnector.startAsync();
				}
			}
		};
	};

	private Size mBitmapSize = null;//new Size(1600, 900);
	private Size mLocalSize;
	
	private float mLocalDeviceWidth;
	private float mLocalDeviceHeight;
	private float mRemoteBmpWidth;
	private float mRemoteBmpHeight;
	
	private GraphicConfig mCurrentConfig;
	private GraphicConfig mRequestConfig;
	
	private void updateBmpConfig() {
		if(mRemoteConnector != null && mRemoteConnector.isConnected()) {

			GraphicConfig config = new GraphicConfig();
			Point startPoint = getRemotePoint(new Point(0, 0));
			Size remoteSize = getRemoteSize(mLocalSize);
			
			config.setConfigType(GraphicConfig.TYPE_CLIENT_REQUEST);
			config.setSize(remoteSize);
			config.setStartPoint(startPoint);
			mRequestConfig = config;
			
			try {
				mRemoteConnector.sendData(config);
			} catch (RemoteConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mScreenView = (ScreenView) findViewById(R.id.screen_root);

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		mLocalDeviceWidth = wm.getDefaultDisplay().getWidth();//屏幕宽度
		mLocalDeviceHeight = wm.getDefaultDisplay().getHeight();//屏幕高度
		mLocalSize = new Size((int)mLocalDeviceWidth, (int)mLocalDeviceHeight); 
		mServerDefaultConfig = ServiceConnectHelper.getServerDefaultConfig();
		
		if(mServerDefaultConfig != null) {
			if(mServerDefaultConfig.getConfigType() == GraphicConfig.TYPE_SERVER_INIT) {
				mScreenView.initScreenSize(mServerDefaultConfig.getSize(), mLocalSize);
			}
		}
		
		mScreenView.setMouseGestureListener(new MouseGestureListener());
		mScreenView.setOnScreenChangeListener(new OnScreenChangeListener() {
			
			@Override
			public void onTranslated() {
				updateBmpConfig();
			}
			
			@Override
			public void onScaled() {
				updateBmpConfig();
			}
		});
		
		mUIHandler.sendEmptyMessage(2);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	class MouseGestureListener extends SimpleOnGestureListener {
//		ScreenDisplayHelper mScreenDisplayHelper;
		
		public MouseGestureListener() {
//			mScreenDisplayHelper = new ScreenDisplayHelper();
//			mScreenDisplayHelper.setupConfig(1600, 900, 1980, 1080);
		}
		
		private static final String TAG = "MouseGestureListener";
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.d(TAG, "onDoubleTap");
			if(mRemoteConnector != null) {
				MouseEvent mouseEvent = new MouseEvent();
				mouseEvent.setEvent(MouseEvent.LEFT_DOUBLE_CLICK);
				Point localPoint = new Point(e.getX(), e.getY());
				
				Point remotePoint = getRemotePoint(localPoint);
				mouseEvent.setPoint1(remotePoint);
				try {
					mRemoteConnector.sendData(mouseEvent);
				} catch (RemoteConnectionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			return super.onDoubleTap(e);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown");
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.d(TAG, "onFling");
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			Log.d(TAG, "onLongPress");
			if(mRemoteConnector != null) {
				MouseEvent mouseEvent = new MouseEvent();
				mouseEvent.setEvent(MouseEvent.RIGHT_CLICK);
				Point localPoint = new Point(e.getX(), e.getY());
				
				Point remotePoint = getRemotePoint(localPoint);
				mouseEvent.setPoint1(remotePoint);
				try {
					mRemoteConnector.sendData(mouseEvent);
				} catch (RemoteConnectionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			super.onLongPress(e);
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
//			Log.d(TAG, "onScroll");
//			if(mRemoteConnector != null) {
//				try {
//					MouseEvent mouseEvent = new MouseEvent();
//					mouseEvent.setEvent(MouseEvent.MOVE);
//					Point localPoint1 = new Point(e1.getX(), e1.getY());
//					
//					Point remotePoint1 = getRemtePoint(localPoint1);
//					
//					mouseEvent.setPoint1(remotePoint1);
//					
//					Point localPoint2 = new Point(e2.getX(), e2.getY());
//					
//					Point remotePoint2 = getRemtePoint(localPoint2);
//					mouseEvent.setPoint2(remotePoint2);
//					
//					mRemoteConnector.sendData(mouseEvent);
//				} catch (IOException ex) {
//					// TODO Auto-generated catch block
//					ex.printStackTrace();
//				}
//			}
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			Log.d(TAG, "onShowPress");
			super.onShowPress(e);
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.d(TAG, "onSingleTapConfirmed");
			if(mRemoteConnector != null) {
				MouseEvent mouseEvent = new MouseEvent();
				mouseEvent.setEvent(MouseEvent.LEFT_CLICK);
				Point localPoint = new Point(e.getX(), e.getY());
				Point remotePoint = getRemotePoint(localPoint);

				mouseEvent.setPoint1(remotePoint);
				try {
					mRemoteConnector.sendData(mouseEvent);
				} catch (RemoteConnectionException e1) {
					e1.printStackTrace();
				}
			}
			return super.onSingleTapConfirmed(e);
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.d(TAG, "onSingleTapUp");
			return super.onSingleTapUp(e);
		}
		
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.d(TAG, "onDoubleTapEvent");
			return super.onDoubleTapEvent(e);
		}
		
	}
	
	private Point getBitmapStartPoint() {
		Matrix matrix = mScreenView.getImageMatrix();
		float[] newValues = new float[9]; 
		matrix.getValues(newValues);
		float left = newValues[2];
		float top = newValues[5];
		Point scaledBmpStartPoint = new Point(left, top);
		
		return scaledBmpStartPoint;
	}
	
	private Point getRemotePoint(Point localPoint) {
		
		Matrix matrix = mScreenView.getImageMatrix();
		float[] newValues = new float[9]; 
		matrix.getValues(newValues);
		float scaleX = newValues[0];
		float scaleY = newValues[4];
		float left = newValues[2];
		float top = newValues[5];
		Point scaledBmpStartPoint = new Point(left, top);
		Point remotePoint = ImageUtils.getPointInOriginalBmp(localPoint, mServerDefaultConfig.getSize(), scaleX, scaleY, scaledBmpStartPoint);
		Log.d(TAG, "[getRemotePoint], remotePoint = " + remotePoint);
		
		return remotePoint;
	}
	
	private Size getRemoteSize(Size localSize) {
		Matrix matrix = mScreenView.getImageMatrix();
		float[] newValues = new float[9]; 
		matrix.getValues(newValues);
		float scaleX = newValues[0];
		float scaleY = newValues[4];
		
		Size remoteSize = new Size((int)(localSize.getWidth() / scaleX), (int)(localSize.getHeight() / scaleY));
		
		return remoteSize;
	}
	
	private Bitmap getFixedBmp(Point point, Size totalSize, Bitmap realBmp) {
		if (point == null) {
			return realBmp;
		}
		Bitmap bitmap = Bitmap.createBitmap((int) totalSize.getWidth(),
				(int) totalSize.getHeight(), realBmp.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(realBmp, point.getX(), point.getY(), null);
		return bitmap;
	}
}
