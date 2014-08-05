package com.anfu.photowebviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.anfu.photowebviewer.Ftp.UploadListener;

public class MainActivity extends Activity {
	private Ftp mFtp=new Ftp();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void onClick(View view) {
		
		if(view != null) {
			if(view.getId() != R.id.btn_exit) {
				if(MyHttpClient.isSessionExpired()) {
					doLogout();
				}
			}
			switch(view.getId()) {
				case R.id.btn_bulletin: {
					goBulletin();
					break;
				}
				case R.id.btn_exit: {
					doExit();
					break;
				}
				case R.id.btn_logout: {
					doLogout();
					break;
				}
				case R.id.btn_takephoto: {
					goTakePhoto();
					break;
				}
				case R.id.btn_test: {
					goTest();
					break;
				}
				case R.id.btn_training: {
					goTraining();
					break;
				}
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(MyHttpClient.isSessionExpired()) {
			doLogout();
		}
	}

	private void doExit() {
		finish();
	}

	private void goBulletin() {
		Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.KEY_URL, "http://61.129.33.98:38000/mobile/bulletin.php");
		
		startActivity(intent);
	}

	private void doLogout() {
		MyHttpClient.logout();
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private static final int REQUEST_TAKE_PHOTO = 1001;
	private void goTakePhoto() {
		
		String fileName = "temp.png";
    	String filePath = Util.getRootDirectory();
		File f = new File(filePath, fileName);
        Uri u=Uri.fromFile(f);  
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null); 
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);  
        intent.putExtra(MediaStore.EXTRA_OUTPUT, u);  
        startActivityForResult(intent, REQUEST_TAKE_PHOTO); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_TAKE_PHOTO: {
					
			        	String fileName = "temp.png";
			        	String filePath = Util.getRootDirectory();
						
					
					
					User user = MyHttpClient.s_CurrentUser;
					
					String timeString = getCurrentTimeString(System.currentTimeMillis());
					String remoteFileName = user.getName() + "-" + timeString + ".png";
					
					showDialog(DIALOG_PROGRESS);
					mFtp.ftpUploadAsync("61.129.33.98", "65521", "anonymous", "123456", "pub", remoteFileName, filePath, fileName, new UploadListener() {

						@Override
						public void onSuccess() {
							dismissDialog(DIALOG_PROGRESS);
							showDialog(DIALOG_SUCCESS);
						}

						@Override
						public void onFail() {
							dismissDialog(DIALOG_PROGRESS);
							showDialog(DIALOG_UPLOAD_FAIL);
						}
						
					});
					break;
				}
			}
		}
	}
	
	private static final int DIALOG_PROGRESS = 1001;
	private static final int DIALOG_UPLOAD_FAIL = 1002;
	private static final int DIALOG_SUCCESS = 1003;
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_SUCCESS: {
				AlertDialog.Builder builder = new Builder(this);
				builder.setMessage("上传成功");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(DIALOG_SUCCESS);
					}
				});
	
				return builder.create();
				
			}
			case DIALOG_PROGRESS: {
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setCancelable(false);
				dialog.setMessage("正在上传");
				return dialog;
			}
			case DIALOG_UPLOAD_FAIL: {
				AlertDialog.Builder builder = new Builder(this);
				builder.setMessage("上传失败了，要重试吗");
				builder.setTitle("提示");
				builder.setPositiveButton("重试", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						goTakePhoto();
						dialog.dismiss();
					}
				});
	
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	
				return builder.create();
			}
		}
		
		return null;

	}

	private String getCurrentTimeString(long time) {
		SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMddHHmmss");
		String a1 = dateformat1.format(new Date());
		return a1;
	}
	
    public String getPath(Uri uri) 
    { 
        String[] projection = {MediaStore.Images.Media.DATA}; 
        Cursor cursor = managedQuery(uri, projection, null, null, null); 
        int column_index = cursor 
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
        cursor.moveToFirst(); 
        return cursor.getString(column_index); 
    }
    
    private String getDate(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATE_MODIFIED}; 
        Cursor cursor = managedQuery(uri, projection, null, null, null); 
        int column_index = cursor 
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED); 
        cursor.moveToFirst(); 
        return cursor.getString(column_index); 
    }
	
	private void goTest() {
		Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.KEY_URL, "http://61.129.33.98:38000/mobile/testing.php");
		
		startActivity(intent);
	}

	private void goTraining() {
		Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
		intent.putExtra(WebViewActivity.KEY_URL, "http://61.129.33.98:38000/mobile/training.php");
		
		startActivity(intent);
	}
	
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// 退出

		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			finish();
		}

	}
}
