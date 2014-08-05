package com.anfu.photowebviewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.anfu.photowebviewer.AsyncRequestSender.OnSuccessListener;

public class LoginActivity extends Activity implements OnSuccessListener {
	
	private AsyncRequestSender mSender;
	private EditText mUserNameEt;
	private EditText mPasswordEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mUserNameEt = (EditText) findViewById(R.id.et_username);
		mPasswordEt = (EditText) findViewById(R.id.et_password);
		
		mSender = new AsyncRequestSender();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	private User mUser;
	public void onClick(View view) {
		if(view != null) {
			switch(view.getId()) {
				case R.id.btn_login: {
					String userName = mUserNameEt.getText().toString();
					String password = mPasswordEt.getText().toString();
					if(userName == null || "".equals(userName) || null == password || "".equals(password)) {
						showDialog(DIALOG_INPUT);
						return;
					}
					List<NameValuePair> nameValueList = new ArrayList<NameValuePair>();
					nameValueList.add(new BasicNameValuePair("user", userName));
					nameValueList.add(new BasicNameValuePair("pass", password));
					mSender.sendHttpRequest(Config.URL_LOGIN + "?user=" + userName + "&pass=" + password, nameValueList, this);
					mUser = new User();
					mUser.setName(userName);
					
					showDialog(DIALOG_LOGIN);
					break;
				}
				case R.id.btn_exit: {
					showDialog(DIALOG_EXIT);
					break;
				}
			}
		}
	}
	
	private static final int DIALOG_INPUT = 1001;
	private static final int DIALOG_LOGIN_FAIL = 1002;
	private static final int DIALOG_EXIT = 1003;
	private static final int DIALOG_LOGIN = 1004;
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_EXIT: {
				AlertDialog.Builder builder = new Builder(this);
				builder.setMessage("确定要退出吗");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
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
			case DIALOG_INPUT: {
				
				AlertDialog.Builder builder = new Builder(this);
				builder.setMessage("请输入用户名和密码");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				return builder.create();
			}
			case DIALOG_LOGIN_FAIL: {
				AlertDialog.Builder builder = new Builder(this);
				builder.setMessage("登录失败");
				builder.setTitle("提示");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				return builder.create();
			}
			case DIALOG_LOGIN: {
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setCancelable(false);
				dialog.setMessage("正在登录");
				return dialog;
			}
		}
		
		return null;

	}

	private static final String TAG = "MainActivity";
	@Override
	public void onSuccess(String result) {
		Log.d(TAG, "[onSuccess]" + result);
		dismissDialog(DIALOG_LOGIN);
		try {
			JSONObject json = new JSONObject(result);
			
			String status = json.optString("status", "failed");
			if("ok".equals(status)) {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
				MyHttpClient.s_CurrentUser = mUser;
				finish();
			} else {
				showDialog(DIALOG_LOGIN_FAIL);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
