package com.icson.lib.pay.ali;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.pay.PayCore;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.statistics.StatisticsConfig;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PayAli extends PayCore implements OnSuccessListener<JSONObject>, OnErrorListener {

	private static final String mFileName = "temp.apk";

	private static final int RQF_PAY = 1;

	private static final int REQUEST_FLAG_PARAM = 1;

	private static final int REQUEST_FLAG_VERSION = 2;

	private static final int REQUEST_FLAG_DOWNLOAD = 3;
	
	public static  final String ASSETS_APK = "alipay-newmsp-5.0.8-pro-1-201311151035.apk";

	public PayAli(BaseActivity activity, String orderCharId,boolean isVp) {
		super(activity, orderCharId,isVp);
	}

	private static final String LOG_TAG = PayAli.class.getName();

	/*
	 * step1   REQUEST_FLAG_PARAM  统一支付返回信息
	 * step2   callAliInterface    MobileSecurePayer.pay
	 *           如果失败   才 checkIntall    要更新--去下载
	 *                                     否则    -- 安装temp.apk
	 * 
	 */
	@Override
	public void submit() {
		if (checkParam() == false) {
			return;
		}

		mActivity.showProgressLayer("正在获取订单信息， 请稍候...");
		String strInfo = "" +  mOrderCharId +(isVP ? "_1" : "");
		//Ajax ajax = com.icson.util.AjaxUtil.get("http://beta.m.51buy.com/pay/json.php?vtl=0&orderid=" + strInfo);
		Ajax ajax = ServiceConfig.getAjax(Config.URL_PAY_TRADE, strInfo);
		if( null == ajax )
			return ;
		
		//ajax.setParser(new com.icson.util.ajax.JSONParser());
		String thirdsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
		if(null!=thirdsource && thirdsource.equals("alipayapp"))
		{
			String  extern_token = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "ali_access_code");
			if(!TextUtils.isEmpty(extern_token))
				ajax.setData("extern_token",extern_token);
		}
		ajax.setId(REQUEST_FLAG_PARAM);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	private String getSignType() {
		String getSignType = "sign_type=" + "\"" + "RSA" + "\"";
		return getSignType;
	}

	public boolean checkIntall() {

		String path = mActivity.getCacheDir().getAbsolutePath() + "/" + mFileName;
		String version = ToolUtil.getApkVersionName(path);
		//not in path,  load apk from assets
		if(null==version)
		{
			retrieveApkFromAssets(PayAli.ASSETS_APK, path);
			version = ToolUtil.getApkVersionName(path);
		}
		mActivity.showProgressLayer("正在检测安全支付服务版本");
		JSONObject req = new JSONObject();
		try {
			req.put("action", "update");
			JSONObject data = new JSONObject();
			data.put("platform", "android");
			data.put("version", version == null ? "0.0" : version);
			data.put("partner", "");

			req.put("data", data);
		} catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			performError("参数设置失败");
			return false;
		}

		Ajax ajax = ServiceConfig.getAjax(Config.URL_MSP_ALIPAY);
		if( null == ajax )
			return false;
		
		ajax.setId(REQUEST_FLAG_VERSION);
		ajax.setData("requestData", req.toString());
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		mActivity.addAjax(ajax);
		ajax.send();
		return false;
	}

	@Override
	public void onError(final Ajax ajax, final Response response) {
		mActivity.closeProgressLayer();

		switch (response.getId()) {
		case REQUEST_FLAG_VERSION:
			performError("检测支付服务版本失败");
			break;

		case REQUEST_FLAG_PARAM:
			StatisticsEngine.alert("pay", StatisticsConfig.PRIORITY_WARN, response.getHttpStatus(), "", mOrderCharId, ILogin.getLoginUid());
			performError("订单信息解析错误");
			break;

		case REQUEST_FLAG_DOWNLOAD:
			performError("下载支付宝组件失败");
			break;
		}
	}

	private void callAliInterface(String orderInfo, String sign) {
		try {
			mActivity.showProgressLayer("正在支付...");
			String info = orderInfo + "&sign=" + "\"" + URLEncoder.encode(sign) + "\"" + "&" + getSignType();
			Log.d(LOG_TAG, info);
			MobileSecurePayer msp = new MobileSecurePayer();
			boolean isPaying = msp.pay(info, mHandler, RQF_PAY, mActivity);
			if(!isPaying){
				mActivity.closeProgressLayer();
				checkIntall();
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			performError("远程服务调用失败");
		}
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		switch (response.getId()) {
		case REQUEST_FLAG_VERSION:
			mActivity.closeProgressLayer();
			try {
				if (v.getString("needUpdate").equalsIgnoreCase("true")) {
					downLoadPackage(v.getString("updateUrl"));
				} else {
					install(new File(mActivity.getCacheDir().getAbsolutePath() + "/" + mFileName));
				}
			} catch (Exception ex) {
				Log.e(LOG_TAG, ex);
				performError("支付宝版本检测失败");
			}
			break;

		case REQUEST_FLAG_PARAM:
			mActivity.closeProgressLayer();
			if (this.checkIcsonResponse(v)) {
				String orderInfo = null, sign = null;
				try {
					JSONObject data = v.getJSONObject("data");
					orderInfo = data.getString("content");
					sign = data.getString("sign");
				} catch (Exception ex) {
					Log.e(LOG_TAG, ex);
				}

				if (orderInfo == null || sign == null) {
					performError("订单信息解析错误");
					return;
				}

				callAliInterface(orderInfo, sign);

			}
			break;
		}

	}

	private void downLoadPackage(String url) {
		mActivity.showProgressLayer("正在下载支付宝控件, 请稍候...");

		Ajax ajax = AjaxUtil.downLoad(url, mActivity.getCacheDir().getAbsolutePath() + "/" + mFileName, new OnSuccessListener<File>() {
			@Override
			public void onSuccess(File v, Response response) {
				mActivity.closeProgressLayer();
				install(v);
			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				mActivity.closeProgressLayer();
				UiUtils.makeToast(mActivity, R.string.message_download_failed);
			}
		});
		ajax.setId(REQUEST_FLAG_DOWNLOAD);
		ajax.setOnErrorListener(this);
		ajax.send();
		mActivity.addAjax(ajax);
	}

	private void install(File v) {
		UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_install_finish, R.string.btn_continue, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					submit();
				} else if (nButtonId == AppDialog.BUTTON_NEGATIVE) {
					performError("支付未完成");
				}
			}
		});

		ToolUtil.shell("chmod 777 " + v.getAbsoluteFile());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + v.getAbsoluteFile()), "application/vnd.android.package-archive");
		mActivity.startActivity(intent);
	}

	/**
	 * 
	* method Name:retrieveApkFromAssets    
	* method Description:  
	* @param fileName
	* @param path
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	public boolean retrieveApkFromAssets(String fileName, String path) {
		boolean bRet = false;
		InputStream is = null;
		FileOutputStream fos = null;
		BufferedOutputStream bufos = null;
		try {
			is = mActivity.getAssets().open(fileName);

			File file = new File(path);
			file.createNewFile();
			fos = new FileOutputStream(file);
			bufos = new BufferedOutputStream(fos);
			
			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				bufos.write(temp, 0, i);
			}

			bufos.flush();
			bRet = true;

		} catch (IOException e) {
			e.printStackTrace();
		}finally
		{
			try
			{
				if(null!=bufos)
					bufos.close();
				if(null!=is)
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return bRet;
	}
	
	// the handler use to receive the pay result.
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String strRet = (String) msg.obj;
				String strDefalutErrMsg =  mActivity.getResources().getString(R.string.pay_send_failed);

				switch (msg.what) {
				case RQF_PAY: {
					mActivity.closeProgressLayer();
					Log.d(LOG_TAG, strRet);
					try {
						String strResultStatus = "resultStatus={";
						String strMemo = "memo={";
						String strMatchResultStatus = "";
						String strMatchMemo = "";
						String [] items = strRet.split(";");
						for(int idx = 0 ; idx < items.length; idx++)
						{
							if(items[idx].startsWith(strResultStatus))
							{
								int resultStatusStart = items[idx].indexOf(strResultStatus) + strResultStatus.length();
								int resultStatusEnd = items[idx].indexOf("}");
								strMatchResultStatus = items[idx].substring(resultStatusStart, resultStatusEnd);
							}
							else if(items[idx].startsWith(strMemo))
							{
								int imemoStart = items[idx].indexOf(strMemo);
								imemoStart += strMemo.length();
								int imemoEnd = items[idx].indexOf("}");
								strMatchMemo = items[idx].substring(imemoStart, imemoEnd);
							}
						}
						
						if (!strMatchResultStatus.equals("9000")) {
							strMatchMemo = TextUtils.isEmpty(strMatchMemo) ? strDefalutErrMsg : strMatchMemo;
							performError(strMatchMemo);
						} else {
							performSuccss();
						}
					} catch (Exception ex) {
						Log.e(LOG_TAG, ex);
						performError(strDefalutErrMsg);
					}
				}
					break;
				}

				super.handleMessage(msg);
			} catch (Exception e) {
				Log.e(LOG_TAG, e);
			}
		}
	};
}
