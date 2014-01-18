package com.niuan.screencapture.client.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.niuan.remoteconnector.RemoteConnector;
import com.niuan.screencapture.client.android.ServiceConnectHelper.OnConnectSuccessListener;

public class ConnectServerActivity extends Activity {

	
	private EditText mEtAddress;
	private EditText mEtPort;
	private Button mBtnSubmit;
	private Button mBtnGetScreenShot;
	private ImageView mImageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_connect);
		SharedPreferences pref = getSharedPreferences("server", MODE_PRIVATE);
		String address = pref.getString("address", "192.168.99.00");
		int port = pref.getInt("port", 10000);
		mEtAddress = (EditText)findViewById(R.id.input_address);
		mEtPort = (EditText)findViewById(R.id.input_port);
		mBtnSubmit = (Button)findViewById(R.id.btn_submit);
		mBtnGetScreenShot = (Button)findViewById(R.id.btn_getscreenshot);
		mImageView = (ImageView)findViewById(R.id.img_screenshot);
		mEtAddress.setText(address); 
		mEtPort.setText(port + "");
		mBtnSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String address = mEtAddress.getText().toString();
				String port = mEtPort.getText().toString();
				new AsynConnectServerTask().execute(address, port);
			}
		});
		
		mBtnGetScreenShot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bitmap bmp = FrameBufferReader.getScreenshot(ConnectServerActivity.this);
				mImageView.setImageBitmap(bmp);
			}
		});
		
	}
	
	private class AsynConnectServerTask extends AsyncTask<String, Void, RemoteConnector> {

		private String mAddress;
		private int mPort;
		@Override
		protected RemoteConnector doInBackground(String... params) {
			if(params == null || params.length < 2) {
				return null;
			}
			
			mAddress = params[0]; //ServerConfig.getServerAddr()
			String port = params[1];
			mPort = Integer.parseInt(port); //ServerConfig.getServerPort()
			
			
//			RemoteConnectorManager manager = RemoteConnectorManager.getInstance();
//			SocketConnectionInfo info = new SocketConnectionInfo();
//			info.setConnectionType(SocketConnectionInfo.ConnectionType.C_SOCKET);
//			info.setSocketAddress(mAddress);
//			info.setSocketPort(mPort);
//			
			
			ServiceConnectHelper.connect(mAddress, mPort, new OnConnectSuccessListener() {
				
				@Override
				public void onSuccess() {
					
					SharedPreferences pref = getSharedPreferences("server", MODE_PRIVATE);
					Editor edit = pref.edit();
					edit.putString("address", mAddress);
					edit.putInt("port", mPort);
					edit.apply();
					Intent intent = new Intent(getApplicationContext(), ScreenViewerActivity.class);
					startActivity(intent);
					
					finish();
				}
			});
			
//			RemoteConnector connector = manager.getRemoteServiceConnector(info);
//			connector.init(info);
//			try {
//				connector.connect();
//			} catch (IOException e) {
//				e.printStackTrace();
//				connector = null;
//			}
//			
			
			return null;
		}
	}
	
}
