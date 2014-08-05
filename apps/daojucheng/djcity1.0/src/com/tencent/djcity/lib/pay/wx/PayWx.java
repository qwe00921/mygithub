package com.tencent.djcity.lib.pay.wx;

import java.util.Random;

import org.json.JSONObject;

import android.util.Log;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.pay.PayCore;
import com.tencent.djcity.lib.pay.PayFactory;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnErrorListener;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;
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
* Modify Date: 2013-6-8 ÃÂ¤ÃÂ¸ÃÂÃÂ¥ÃÂÃÂ02:20:59 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class PayWx extends PayCore implements OnSuccessListener<JSONObject>, OnErrorListener {

	private static final int REQUEST_FLAG_PARAM = 1;
	
	private PayReq aReq = new PayReq();
	private BaseActivity mActivity;
	//private PayReq;

	public PayWx(BaseActivity activity, String orderUrl) {
		super(activity, orderUrl);
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

		mActivity.showProgressLayer(mActivity.getString(R.string.prepare_for_pay));
		
		//String strInfo = "" + mOrderCharId +(isVP ? "_1" : "");
		//AppStorage.setData("WXOrder", strInfo, true);
		Account act = ILogin.getActiveAccount();
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/djapp_buy.php?" + mOrderUrl);
		ajx.setData("paytype", PayFactory.PAY_WX);
		
		if(null==ajx || null == act)
		{
			UiUtils.makeToast(mActivity, "Logout");
			return;
		}
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
			
		//mTimestamp = System.currentTimeMillis();
		//Random ran =new Random(mTimestamp);
		//mNonceStr = "" + ran.nextInt(Integer.MAX_VALUE);
		
		//ajax.setData("appid", Config.APP_ID);
		//ajax.setData("time_stamp", mTimestamp);
		//ajax.setData("nonce_num", mNonceStr);
		mActivity.addAjax(ajx);
		ajx.send();
	}
	
	
	@Override
	public void onError(final Ajax ajax, final Response response) {
		mActivity.closeProgressLayer();

		switch (response.getId()) {

		case REQUEST_FLAG_PARAM:
			performError("参数错误");
			break;

		}
	}


	@Override
	public void onSuccess(JSONObject v, Response response) {
		mActivity.closeProgressLayer();
		int ret = v.optInt("result",-1);
		if(ret!=0)
		{
			UiUtils.makeToast(mActivity, v.optString("msg"));
			return;
		}
		
		/**
		 * former
		 * {"errno":0,
		 *  "data": 
		 *  	{"package":"Sign=1A40402B37C8005D629F4A7E65420978",
		 *  	 "sign":"BA8898B3CACE2D4934E7B34D62E602718200925A",
		 *  	 "token":"137b23cff35afc4709",
		 *  	 "partner":"1900000109"}}
		 *  
		 *  
		 *  now
		 *  {"timestamp":"1398232406",
		 *  "sign":"c5f2f6362e1ff2d8ea453208790c424192848676",
		 *  "result":"0",
		 *  "noncestr":"1398232406034185-9350",
		 *  "payPrice":"100",
		 *  "sSerialNum":"CF-20140423135326026-52",
		 *  "prepayid":"1101000000140423ebd2edf49cf84e3a",
		 *  "package":"Sign=WXpay",
		 *  "partner":"1000022901",
		 *  "msg":"ok"}
		 *   
		 */
		
		String sSerialNum = v.optString("sSerialNum");
		AppStorage.setData("WXOrder", sSerialNum, false);
		
		aReq.prepayId =  v.optString("prepayid");
		aReq.appId = Config.APP_ID;
		aReq.nonceStr = v.optString("noncestr");
		aReq.partnerId = v.optString("partner");
		aReq.timeStamp = v.optString("timestamp");
		aReq.options = new com.tencent.mm.sdk.modelpay.PayReq.Options();
		aReq.options.callbackClassName = "com.tencent.djcity.wxapi.WXEntryActivity";
		
		aReq.sign = v.optString("sign");
		aReq.packageValue = v.optString("package");
		
		WXAPIFactory.createWXAPI(mActivity, Config.APP_ID).sendReq(aReq);
		
		Log.v(LOG_TAG, "send " + mOrderUrl + " to WX sign:" + aReq.sign
				+ " prepayId:" + aReq.prepayId + " packageValue:" + aReq.packageValue 
				+" partnerId" + aReq.partnerId);
		
	}
	

}


	


