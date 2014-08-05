package com.icson.lib.pay.cft;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;

import com.icson.lib.ILogin;
import com.icson.lib.pay.PayCore;
import com.icson.statistics.StatisticsConfig;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class CFTPay extends PayCore implements OnSuccessListener<JSONObject>, OnErrorListener {

	private static final int REQUEST_FLAG_PARAM = 1;


	public CFTPay(BaseActivity activity, String orderCharId,boolean isVp) {
		super(activity, orderCharId,isVp);
	}

	private static final String LOG_TAG = CFTPay.class.getName();

	@Override
	public void submit() {
		if (checkParam() == false) {
			return;
		}

		mActivity.showProgressLayer("正在获取订单信息， 请稍候...");
		String strInfo = "" + mOrderCharId +(isVP ? "_1" : "");
		Ajax ajax = ServiceConfig.getAjax(Config.URL_PAY_TRADE, strInfo);
		if( null == ajax )
			return ;
		
		ajax.setId(REQUEST_FLAG_PARAM);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		mActivity.addAjax(ajax);
		ajax.send();
	}
	
	@Override
	public void onError(final Ajax ajax, final Response response) {
		mActivity.closeProgressLayer();

		switch (response.getId()) {

		case REQUEST_FLAG_PARAM:
			StatisticsEngine.alert("pay", StatisticsConfig.PRIORITY_WARN, response.getHttpStatus(), "", mOrderCharId, ILogin.getLoginUid());
			performError("订单信息解析错误");
			break;

		}
	}


	@Override
	public void onSuccess(JSONObject v, Response response) {
		switch (response.getId()) {
		
		case REQUEST_FLAG_PARAM:
			mActivity.closeProgressLayer();
			if (this.checkIcsonResponse(v)) {
				String url = null;
				try {
					JSONObject data = v.getJSONObject("data");
					url = data.getString("url");
				} catch (Exception ex) {
					Log.e(LOG_TAG, ex);
				}

				if (url == null) {
					performError("订单信息解析错误");
					return;
				}

				startWebView(url);

			}
			break;
		}

	}


	private void startWebView(String url) {
		try{
			if(android.os.Build.VERSION.SDK_INT < 8){
				mActivity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
				return ;
			}
		}catch(Exception e){}
		Intent pay = new Intent(mActivity, CFTPayActivity.class);
		pay.putExtra(CFTPayActivity.CFT_PAY_URL, url);
		mActivity.startActivityForResult(pay, CFTPayActivity.REQUEST_CFT_PAY);
		
	}

}
