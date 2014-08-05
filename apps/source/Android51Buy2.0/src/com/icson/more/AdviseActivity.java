package com.icson.more;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.IVersion;
import com.icson.lib.model.BaseModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.AutoHeightImageView;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderPickListActivity;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.UploadPhotoUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.HttpUtil;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;


public class AdviseActivity extends BaseActivity {
	
	public static class FeedBackItemModel extends BaseModel {
		public int subtype;
		public String name;
		public int selecttype;

		public void parse(JSONObject json) throws JSONException {
			name = json.optString("name");
			subtype = json.optInt("subtype");
			selecttype = json.optInt("selecttype");
		}
	}
	
	public static final int PICK_ORDER_ACTIVITY = 2001;
	public static final int GO_CROP_ACTIVITY = 2002;
	
	private Button		mButton;
	private EditText 	mAdviseContent;
	private TextView 	mTypeContent;
	private TextView 	mPicHint;
	private EditText 	mUserPhone;
	private ImageView   mTypeImage;
	private ImageView   mOrderImage;
	private RelativeLayout		mOrderLayout;
	private LinearLayout		mGlobalLayout;
	private String              mRoot;     // Root path depends on SD card exits.
	
	private int					   mCurNodeIdx = 0;
	private ArrayList<FeedBackItemModel> nodes = new ArrayList<FeedBackItemModel>();
	private ArrayList<ImageView>   mPicImages = new ArrayList<ImageView>();
	private int					   mCurPicIdx = 0;
	private boolean				   mIsProcessing = false;

	private ImageLoader 			mImageLoader;
	private String					mOrderCharId = null;
	private ArrayList<String>		mProdUrls;
	
	static int sRandNameIdx = 0;
	
//	private final int 	maxAdviseNum = 500;
//	private TextView 	mAdviseWordNum;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_more_contactus);
		
		mImageLoader = new ImageLoader(this, true);
		
		createPath("fbImage");
		
		// Load navigation bar.
		this.loadNavBar(R.id.advise_navbar);
		
		mPicHint = (TextView) findViewById(R.id.picHintTitle);
		
		mCurPicIdx = 0;
		mPicImages.add((ImageView) findViewById(R.id.picImage1));
		mPicImages.add((ImageView) findViewById(R.id.picImage2));
		mPicImages.add((ImageView) findViewById(R.id.picImage3));
		mPicImages.add((ImageView) findViewById(R.id.picImage4));
		mPicImages.add((ImageView) findViewById(R.id.picImage5));
		for (ImageView img : mPicImages) {
			img.setOnClickListener(this);
		}
		layoutImage();

		mTypeImage = (ImageView) findViewById(R.id.typeImageBg);
		mTypeImage.setOnClickListener(this);
		
		mOrderImage = (ImageView) findViewById(R.id.orderImageBg);
		mOrderImage.setOnClickListener(this);
		
		mTypeContent = (TextView) findViewById(R.id.typeTextContent);
		
		mOrderLayout = (RelativeLayout) findViewById(R.id.feedback_order_container);
		mGlobalLayout = (LinearLayout) findViewById(R.id.global_container);
		
		mGlobalLayout.removeView(mOrderLayout);
		
		mAdviseContent = (EditText) findViewById(R.id.feedback_content_editText);
		mUserPhone = (EditText) findViewById(R.id.advise_phone_content);
		
		mButton = (Button) findViewById(R.id.button_confirm);
		mButton.setText("提交");
		mButton.setPadding(ToolUtil.dip2px(this, 15), 0, ToolUtil.dip2px(this, 15), 0);
		mButton.setVisibility(View.VISIBLE);
		mButton.setOnClickListener(this);
		
		mGlobalLayout.removeView(mOrderLayout);
		if(getIntent().getExtras()!=null) 
		{
			mOrderCharId = getIntent().getExtras().getString("orderId");
			mProdUrls = (ArrayList<String>) getIntent().getExtras().getStringArrayList("prodCharIds");  
		}
		
//		mAdviseWordNum = (TextView) findViewById (R.id.advise_word_num);
//		mAdviseWordNum.setText(0 + "/" + maxAdviseNum);
//		editText = (EditText) findViewById(R.id.advise_edittext_content);
//		editText.addTextChangedListener(new TextWatcher(){
//			@Override
//			public void beforeTextChanged(CharSequence s, int start,int count, int after) {
//				
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int count,int after) {
//				
//			}
//			
//			@Override
//			public void afterTextChanged(Editable s) {
//				mAdviseWordNum.setText(s.length() + "/" + maxAdviseNum);
//			}	
//		});
//		userPhone = (EditText) findViewById(R.id.advise_phone_content);

		showProgressLayer();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_FB_GET_TYPE);
		if( null != ajax ) {
			ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
					closeProgressLayer();
					final int errno = v.optInt("errno", -1);
					if (errno != 0) {
						UiUtils.makeToast(AdviseActivity.this, v.optString("data", Config.NORMAL_ERROR));
						finish();
						return;
					}

					try {
						JSONArray items = v.optJSONArray("data");

						nodes.removeAll(null);
						final int size = (null != items ? items.length() : 0);
						for (int i = 0; i < size; i++) {
							FeedBackItemModel node = new FeedBackItemModel();
							node.parse(items.getJSONObject(i));
							nodes.add(node);
							if (null != mOrderCharId && node.selecttype == 1) {
								mCurNodeIdx = i;
							}
						}

						AdviseActivity.this.updateNodeInfo(mCurNodeIdx);
						
					} catch (Exception ex) {
						UiUtils.makeToast(AdviseActivity.this, "拉取信息失败，请稍后再试");
						finish();
					}
				}
			});
			ajax.setOnErrorListener(this);
			addAjax(ajax);
			ajax.send();
		}
	}
	
	@Override 
	protected void onDestroy()
	{
		if(null!=mImageLoader)
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
		super.onDestroy();
	}
	private void updateNodeInfo(int idx)
	{
		if (idx >= 0 && idx < nodes.size()) {
			FeedBackItemModel model = nodes.get(idx);
			mTypeContent.setText(model.name);
			mCurNodeIdx = idx;
			
			if (model.selecttype > 0) {		// 1订单类型, 2售后类型
				mGlobalLayout.addView(mOrderLayout, 1);
				updateProdInfo();
			} else {
				mGlobalLayout.removeView(mOrderLayout);
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.button_confirm) {
			String strContent = mAdviseContent.getText().toString().trim();
			
			if (mCurNodeIdx >= nodes.size()) {
				UiUtils.makeToast(this, "未知的错误");
				finish();
				return;
			}
			FeedBackItemModel model = nodes.get(mCurNodeIdx);
			if (model.selecttype > 0 && mOrderCharId == null) {
				UiUtils.makeToast(this, "请选择订单");
				return;
			}

			if (strContent.length() < 5) {
				UiUtils.makeToast(this, "反馈内容5个字以上.");
				return;
			}
			
			final String strPhone = ((String) mUserPhone.getText().toString()).trim();
			if(TextUtils.isEmpty(strPhone)) {
				UiUtils.makeToast(this, "手机号码不能为空哦");
				return;
			}else{
				Matcher matcher = Pattern.compile("^1\\d{10}$").matcher(strPhone);
				if(!matcher.find()){
					matcher = Pattern.compile("^\\d+-\\d+$").matcher(strPhone);
					if (!matcher.find()) {
						UiUtils.makeToast(this, "联系电话格式有误(11位手机号码或021-12345678格式) ");
						return;
					}
				}
			}
			
			final int netType = HttpUtil.getNetType(IcsonApplication.app);
			String sNetType = ( netType == HttpUtil.WIFI ? "WIFI" :(  netType == HttpUtil.NET ? "3G" : ( netType == HttpUtil.WAP ? "WAP" : "NONE" ) ) );
		
			TelephonyManager telManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			String imsi = telManager.getSubscriberId();
			String name = "未插卡";
			 if(imsi!=null){ 
				 if(imsi.startsWith("46000") || imsi.startsWith("46002")){ 
					 name = "中国移动";
				 }else if(imsi.startsWith("46001")){
					 name = "中国联通";
				 }else if(imsi.startsWith("46003")){
					 name = "中国电信";
				 } 
			} 
			 
			sNetType = name + " " + sNetType;
			showProgressLayer();
			Ajax ajax = ServiceConfig.getAjax(Config.URL_FB_ADD_NEW);
			if( null == ajax )
				return ;

			if (model.selecttype > 0) {
				ajax.setData("uid", ILogin.getLoginUid());
				ajax.setData("order_id", mOrderCharId);
			}

			String attaStr = new String();
			for (int i = 0; i < mCurPicIdx; i++) {
				AutoHeightImageView img = (AutoHeightImageView)mPicImages.get(i);
				if (i > 0) {
					attaStr += ";";
				}
				attaStr += img.mCustomInfo.get("url");
			}
			
			ajax.setData("attachment", attaStr);
			ajax.setData("subtype", model.subtype);
			ajax.setData("content", strContent);
			ajax.setData("phone", strPhone);
			String version = "android: " + android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE + " " + ToolUtil.getEquipmentWidth(IcsonApplication.app) + "*" + ToolUtil.getEquipmentHeight(IcsonApplication.app) + "*" + ToolUtil.getDensityDpi() + " " + IVersion.getVersionName() + "(编译于:" + Config.COMPILE_TIME + ")" + " 来源:" + ToolUtil.getChannel() + " " + sNetType;
			ajax.setData("version", version);
			ajax.setData("deviceid", StatisticsUtils.getDeviceUid(this));
			ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
					closeProgressLayer();
					final int errno = v.optInt("errno", -1);

					if (errno != 0) {
						UiUtils.makeToast(AdviseActivity.this, v.optString("data", Config.NORMAL_ERROR));
						return;
					}

					UiUtils.makeToast(AdviseActivity.this, "谢谢您的反馈",true);
					finish();

				}
			});
			ajax.setOnErrorListener(this);
			addAjax(ajax);
			ajax.send();
		}
		else if (view.getId() == R.id.typeImageBg)
		{
			String nameArr[] ;  
			nameArr = new String[nodes.size()] ;  
			for (int i = 0; i < nodes.size(); i++) {
				FeedBackItemModel model = nodes.get(i);
				nameArr[i] = model.name;
			}
			
			UiUtils.showListDialog(this, "选择类型", nameArr, mCurNodeIdx, new RadioDialog.OnRadioSelectListener(){
				@Override
				public void onRadioItemClick(int which) {
					if (which < nodes.size()) {
						FeedBackItemModel model = nodes.get(which);
						mTypeContent.setText(model.name);
						mCurNodeIdx = which;
						
						mGlobalLayout.removeView(mOrderLayout);
						if (model.selecttype > 0) {		// 1订单类型, 2售后类型
							mGlobalLayout.addView(mOrderLayout, 1);
						}
					}
				}
				
			});

		}
		else if (view.getId() == R.id.orderImageBg)
		{
			Bundle params = new Bundle();
			params.putBoolean("orderPickMode", true);
			ToolUtil.checkLoginOrRedirect(this, OrderPickListActivity.class, params, PICK_ORDER_ACTIVITY);
		}
		else if (false == mIsProcessing)
		{
			int clickIdx = 100;
			if (view.getId() == R.id.picImage1) {
				clickIdx = 0;
			} else if (view.getId() == R.id.picImage2) {
				clickIdx = 1;
			} else if (view.getId() == R.id.picImage3) {
				clickIdx = 2;
			} else if (view.getId() == R.id.picImage4) {
				clickIdx = 3;
			} else if (view.getId() == R.id.picImage5) {
				clickIdx = 4;
			}
			
			if (mCurPicIdx > clickIdx) {
				showDelPicDlg(this, clickIdx);
			} else if (clickIdx == mCurPicIdx) {
				UploadPhotoUtil.createUploadPhotoDlg(this).show();
			}
		}
	}
	
	private void layoutImage() 
	{
		for (int i = 0; i < mPicImages.size(); i++) {
			ImageView curImg = mPicImages.get(i);
			if (i == mCurPicIdx) {
				curImg.setVisibility(View.VISIBLE);
				curImg.setImageResource(R.drawable.feedback_add);
			} else if (i > mCurPicIdx) {
				curImg.setVisibility(View.INVISIBLE);
			} else {
				curImg.setVisibility(View.VISIBLE);
			}
		}
		if (mCurPicIdx > 0) {
			mPicHint.setVisibility(View.INVISIBLE);
		} else {
			mPicHint.setVisibility(View.VISIBLE);
		}
	}
	
	private void showDelPicDlg(final BaseActivity context, final int idx) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("选择操作");
		builder.setItems(new String[]{"删除"},
				new DialogInterface.OnClickListener() {
					@SuppressWarnings("unchecked")
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							for (int i = idx; i < mPicImages.size()-1 && i < mCurPicIdx; i++) {
								AutoHeightImageView curImg = (AutoHeightImageView)mPicImages.get(i);
								AutoHeightImageView nextImg = (AutoHeightImageView)mPicImages.get(i+1);
								curImg.mCustomInfo = (HashMap<String, String>) nextImg.mCustomInfo.clone();
								curImg.setImageDrawable(nextImg.getDrawable());
							}
							mCurPicIdx--;
							layoutImage();
						}
					}
				});

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		
		dialog.show();
	}
	
	private String createPath(String strPath)
	{
		if( TextUtils.isEmpty(strPath) )
			return null;
		
		// 1. Firstly, check whether directory already exits or not.
		if (strPath.startsWith(File.separator)) {
			strPath = strPath.substring(1);
		}

		if (strPath.endsWith(File.separator)) {
			strPath = strPath.substring(0, strPath.length() - 1);
		}

		String[] dirs = strPath.split("\\" + File.separator);
		String pre = "";
		for (String dir : dirs) 
		{
			pre += ((pre.equals("") ? "" : File.separator) + dir);
			this.createDir(pre);
		}

		return mRoot;
	}
	
	/**
	 * @param strPath
	 */
	private void createDir(String strPath)
	{
		if ( TextUtils.isEmpty(strPath) )
			return ;
		
		// 1. Get the root path.
		String root = null;
		if( ToolUtil.isSDExists() )
		{
			root = Environment.getExternalStorageDirectory() + "/" + Config.TMPDIRNAME + "/";
		}
		else
		{
			root = this.getCacheDir() + "/" + Config.TMPDIRNAME + "/";
		}

		// 2. Check whether current path exits.
		mRoot = root + strPath;
		File pFile = new File(mRoot);
		if( !pFile.exists() )
		{
			pFile.mkdir();
		}
		
		// Clean up.
		pFile = null;
	}
	
	private void showClipIntentWithData(Uri uri) {
		if (null == uri) return;

		AutoHeightImageView curImg = (AutoHeightImageView) mPicImages.get(mCurPicIdx);
		String localPath = curImg.mCustomInfo.get("localPath");
		if (null == localPath) {
			localPath = mRoot + "/fbImg_" + sRandNameIdx + ".jpg";
			sRandNameIdx++;
			curImg.mCustomInfo.put("localPath", localPath);
		}

		// 不知道为什么无法保存到本地路径，即使return-data设为false也没用
		//Uri imageUri = Uri.parse(localPath);//The Uri to store the big bitmap

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 240);
		intent.putExtra("outputY", 240);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(intent, GO_CROP_ACTIVITY);
	}
	
	private void updateProdInfo() 
	{
		for (int i = 0; i < 3; i++) 
		{
			ImageView imgView = null;
			if (0 == i) imgView = (ImageView) findViewById(R.id.ImageView01);
			else if (1 == i) imgView = (ImageView) findViewById(R.id.ImageView02);
			else if (2 == i) imgView = (ImageView) findViewById(R.id.ImageView03);
			
			if (null != mProdUrls && i < mProdUrls.size()) {
				imgView.setVisibility(View.VISIBLE);
				String url = mProdUrls.get(i);
				Bitmap imgData = mImageLoader.get(url);
				imgView.setImageBitmap(imgData != null ? imgData : ImageHelper.getResBitmap(this, mImageLoader.getLoadingId()));
				if (imgData == null) {
					mImageLoader.get(url, new ImageLoadListener() {
						@Override
						public void onLoaded(Bitmap aBitmap, String strUrl) {
							updateProdInfo();
						}

						@Override
						public void onError(String strUrl) {
							
						}
						
					});
				}
			} else {
				imgView.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	public void onError(final Ajax ajax, final Response response) 
	{
		closeProgressLayer();
		
		if (ajax.getId() == 2001) {
			UiUtils.showDialog(this, "图片上传失败", "是否重试", R.string.btn_retry, R.string.btn_cancel, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE) {
						ajax.send();
					}
				}
				});
			return;
		}
		
		super.onError(ajax, response);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (resultCode != Activity.RESULT_OK) {
			mIsProcessing = false;
			return;
		}
		
		if (requestCode == UploadPhotoUtil.PHOTO_PICKED_WITH_DATA && null != data) 
		{
			// 雷军个傻逼
			mIsProcessing = true;
			showClipIntentWithData(data.getData());
		} 
		else if (requestCode == UploadPhotoUtil.CAMERA_WITH_DATA) 
		{
			mIsProcessing = true;
			Uri imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			
			String path = UploadPhotoUtil.getImgPath(this, requestCode, resultCode, data);
			if(TextUtils.isEmpty(path))
			{	
				UiUtils.makeToast(AdviseActivity.this, "照片路径获取失败");
				return;
			}
			
			File file = new File(path);
			Uri fileUri = Uri.fromFile(file);
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					fileUri));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Cursor cursor = getContentResolver().query(imgUri, null,
							MediaStore.Images.Media.DISPLAY_NAME + "='"
									+ file.getName() + "'",
							null, null);
			Uri uri = null;
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToLast();
				long id = cursor.getLong(0);
				uri = ContentUris.withAppendedId(imgUri, id);
			}
			if(null!=cursor && !cursor.isClosed())
				cursor.close();
			showClipIntentWithData(uri);
		} 
		else if (requestCode == PICK_ORDER_ACTIVITY && null != data)
		{
			mOrderCharId = data.getStringExtra("orderId");
			mProdUrls = (ArrayList<String>) data.getStringArrayListExtra("prodCharIds");  
			updateProdInfo();
		}
		else if(requestCode == GO_CROP_ACTIVITY)
		{
			final AutoHeightImageView curImg = (AutoHeightImageView) mPicImages.get(mCurPicIdx);
			//String localPath = curImg.mCustomInfo.get("localPath");

			final Bitmap bitmap =  (null == data) ? null : (Bitmap)data.getParcelableExtra("data");
			if (null != bitmap) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				
//				File file = new File(localPath);
//			    FileOutputStream fOut = new FileOutputStream(file);
//				bitmap.compress(CompressFormat.JPEG, 100, fOut);
//				fOut.flush();
//				fOut.close();
				
				Ajax ajax = ServiceConfig.getAjax(Config.URL_FB_IMAGE_STREAM_UPLOAD);

				showProgressLayer();
				if( null != ajax ) {
					ajax.setFile("macd", byteArray, "img.jpg");
					ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
						@Override
						public void onSuccess(JSONObject v, Response response) {
							closeProgressLayer();
							final int errno = v.optInt("errno", -1);
							if (errno != 0) {
								UiUtils.makeToast(AdviseActivity.this, v.optString("data", Config.NORMAL_ERROR));
								return;
							}
							
							curImg.mCustomInfo.put("url", v.optString("picurl"));
							curImg.setImageBitmap(bitmap);
							mCurPicIdx++;
							layoutImage();
						}
					});
					ajax.setId(2001);
					ajax.setOnErrorListener(this);
					addAjax(ajax);
					ajax.send();
				}
			}

			mIsProcessing = false;
		}
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_AdviseActivity);
	}
	
}