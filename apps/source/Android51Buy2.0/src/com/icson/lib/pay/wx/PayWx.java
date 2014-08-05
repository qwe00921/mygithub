package com.icson.lib.pay.wx;

import java.util.Random;

import android.util.Log;

import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.model.WxInfoModel;
import com.icson.lib.parser.WXPayInfoParser;
import com.icson.lib.pay.PayCore;
import com.icson.statistics.StatisticsConfig;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 
*   
* Class Name:PayWx 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-8 下午02:20:59 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class PayWx extends PayCore implements OnSuccessListener<WxInfoModel>, OnErrorListener {

	private static final int REQUEST_FLAG_PARAM = 1;
	
	private WxInfoModel mWXInfo;
	private String       mNonceStr;
	private long 		 mTimestamp;
	private WXPayInfoParser mParser;  
	private BaseActivity mActivity;
	//private PayReq;

	public PayWx(BaseActivity activity, String orderCharId,boolean isVp) {
		super(activity, orderCharId,isVp);
		mActivity = activity;
	}

	private static final String LOG_TAG = PayWx.class.getName();

	

	
	@Override
	public void submit() {
		if(!AppUtils.checkWX(mActivity,Build.PAY_SUPPORTED_SDK_INT))
			return;
		
		if (checkParam() == false) {
			return;
		}

		mActivity.showProgressLayer("正在获取订单信息， 请稍候...");
		
		String strInfo = "" + mOrderCharId +(isVP ? "_1" : "");
		AppStorage.setData("WXOrder", strInfo, true);
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_PAY_TRADE, strInfo);
		//Ajax ajax = com.icson.util.AjaxUtil.get("http://beta.m.51buy.com/pay/json.php?vtl=0&orderid=" + strInfo);
		if( null == ajax )
			return ;
		
		mTimestamp = System.currentTimeMillis();
		Random ran =new Random(mTimestamp);
		mNonceStr = "" + ran.nextInt(Integer.MAX_VALUE);
		
		ajax.setId(REQUEST_FLAG_PARAM);
		ajax.setData("appid", Config.APP_ID);
		ajax.setData("time_stamp", mTimestamp);
		ajax.setData("nonce_num", mNonceStr);
		if(null == mParser)
			mParser = new WXPayInfoParser();
		ajax.setParser(mParser);
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
			performError("支付签名服务错误");
			break;

		}
	}


	@Override
	public void onSuccess(WxInfoModel wxModel, Response response) {
		switch (response.getId()) {
		case REQUEST_FLAG_PARAM:
			mActivity.closeProgressLayer();
			
			mWXInfo = wxModel;
			
			callWXPay(mWXInfo);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 
	* method Name:callWXPay    
	* method Description:  
	* @param v   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void callWXPay(WxInfoModel v)
	{
		PayReq aReq = new PayReq();
		
		aReq.appId = Config.APP_ID;
		aReq.nonceStr = this.mNonceStr;
		aReq.sign = mWXInfo.getSign();
		aReq.prepayId = mWXInfo.getToken();
		aReq.packageValue = mWXInfo.getPackage();
		aReq.partnerId = mWXInfo.getPartner();
		aReq.timeStamp = "" + this.mTimestamp;
		aReq.options = new com.tencent.mm.sdk.modelpay.PayReq.Options();
		aReq.options.callbackClassName = "com.icson.wxapi.WXEntryActivity";
		WXAPIFactory.createWXAPI(mActivity, Config.APP_ID).sendReq(aReq);
		
		Log.v(LOG_TAG, "send " + mOrderCharId + " to WX sign:" + aReq.sign
				+ " prepayId:" + aReq.prepayId + " packageValue:" + aReq.packageValue 
				+" partnerId" + aReq.partnerId);
		
		/*
		if(mExitAfterward)
		{
			if(null == mDelayFinishHandler)
				mDelayFinishHandler = new Handler();
			mDelayFinishHandler.postDelayed(new Runnable(){

				@Override
				public void run() {
					mActivity.finish();
					mDelayFinishHandler.removeCallbacksAndMessages(null);
				}}, 1200);
		}*///not do it this way
	}

}


	


