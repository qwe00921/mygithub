package com.icson.login;

import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginListener;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ui.UiUtils;
import com.icson.util.AppUtils;
import com.icson.util.activity.BaseActivity;

public class QQVerificationActivity extends BaseActivity {
	private ImageView code;
	private ImageView refresh;
	private EditText inputCode;
	private Button btnCode;
	private String account;
	private TextView mContact;
	public static final int REQUEST_CODE = 1001;
	private WtloginHelper mLoginHelper = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qq_verification);
		
		if( null == mLoginHelper ) {
			mLoginHelper = ReloginWatcher.getInstance(this).getWtloginHelper();
		}
		
		mLoginHelper.SetListener(mListener);

		code = (ImageView) findViewById(R.id.code);
		inputCode = (EditText) findViewById(R.id.inputCode);
		btnCode = (Button) findViewById(R.id.btnCode);
		btnCode.setOnClickListener(onClick);
		refresh = (ImageView) findViewById(R.id.refresh);
		refresh.setOnClickListener(onClick);
//		refresh.setOnTouchListener(onTouchListener);

		Bundle bundle = new Bundle();
		Intent pIntent = getIntent();
		if(null == pIntent) {
			UiUtils.makeToast(this, getString(R.string.global_error_warning));
			return;
		}
		
		bundle = pIntent.getExtras();
		if(null == bundle) {
			UiUtils.makeToast(this, getString(R.string.global_error_warning));
			return;
		}
		account = bundle.getString("ACCOUNT");
		byte[] tmp = bundle.getByteArray("CODE");
		Bitmap bm = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
		code.setImageBitmap(bm);
		
		// 客服电话
		mContact = ((TextView) findViewById(R.id.qq_ver_textview_contact));
		mContact.setText(Html.fromHtml(getString(R.string.login_activity_tel)));
		mContact.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent pIntent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:4008281878"));
				AppUtils.checkAndCall(QQVerificationActivity.this,pIntent);
			}
		});
	}

	@Override
	protected void onDestroy()
	{
		mLoginHelper.SetListener(null);
		mLoginHelper = null;
		super.onDestroy();
	}
//	private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
//		public boolean onTouch(View v, MotionEvent event) {
//			int id = v.getId();
//			switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN: {
//				if (id == R.id.refresh)
//					refresh.setTextColor(QQVerificationActivity.this
//							.getResources().getColor(R.color.textFocus));
//			}
//				break;
//			case MotionEvent.ACTION_UP: {
//				if (id == R.id.refresh)
//					refresh.setTextColor(QQVerificationActivity.this
//							.getResources().getColor(R.color.white));
//			}
//				break;
//			case MotionEvent.ACTION_MOVE:
//				break;
//			}
//			return false;
//		}
//	};

	private View.OnClickListener onClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCode: {
				WUserSigInfo sigInfo = new WUserSigInfo();
				mLoginHelper.CheckPictureAndGetSt(account,
						inputCode.getText().toString().getBytes(), sigInfo);
			}
				break;
			case R.id.refresh: {
				WUserSigInfo sigInfo = new WUserSigInfo();
				mLoginHelper.RefreshPictureData(account, sigInfo);
			}
				break;
			case R.id.qq_ver_textview_contact:
				Intent pIntent= new Intent(Intent.ACTION_DIAL,Uri.parse("tel:4008281878"));
				AppUtils.checkAndCall(QQVerificationActivity.this,pIntent);
				break;
			default:
				break;
			}
		}
	};

	WtloginListener mListener = new WtloginListener() {
		public void OnCheckPictureAndGetSt(String userAccount,
				byte[] userInput, WUserSigInfo userSigInfo, int ret,ErrMsg errMsg) {

			if (ret == util.S_GET_IMAGE) {
				byte[] image_buf = new byte[0];
				image_buf = mLoginHelper.GetPictureData(userAccount);
				if (image_buf == null) {
					return;
				}
				
				//String prompt_value = ReloginWatcher.getImagePrompt(userAccount, mLoginHelper.GetPicturePrompt(userAccount));
				//if (prompt_value != null && prompt_value.length() > 0) {
				//	promptView.setText(prompt_value);
				//}
				Bitmap bm = BitmapFactory.decodeByteArray(image_buf, 0,
						image_buf.length);
				code.setImageBitmap(bm);
				UiUtils.makeToast(QQVerificationActivity.this, "验证码有误，请尝试重新输入。");
				inputCode.setText("");
			} else {
				Intent intent = new Intent();
	      		Bundle bundle = new Bundle();
  				bundle.putString("ACCOUNT", userAccount);
  				bundle.putParcelable("ERRMSG", errMsg);
  				bundle.putParcelable("USERSIG", userSigInfo);
  				intent.putExtras(bundle);
				QQVerificationActivity.this.setResult(ret, intent);
				QQVerificationActivity.this.finish();
				return;
			}
		}

		public void OnRefreshPictureData(String userAccount, WUserSigInfo userSigInfo, byte[] pictureData, int ret, ErrMsg errMsg)
		{
			if (ret == util.S_SUCCESS) {
				byte[] image_buf = new byte[0];
				image_buf = mLoginHelper.GetPictureData(userAccount);
				if (image_buf == null) {
					return;
				}
				Bitmap bm = BitmapFactory.decodeByteArray(image_buf, 0,
						image_buf.length);
				code.setImageBitmap(bm);
			}
		}
	};
	
	
	@Override
	public String getActivityPageId() {
		return "000000";
	}
}
