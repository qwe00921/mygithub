package com.tencent.djcity.lib.pay.midas;

import java.util.Random;

import org.json.JSONObject;

import android.util.Log;

import com.pay.AndroidPay;
import com.pay.api.APPayGameService;
import com.pay.api.APPayResponseInfo;
import com.pay.api.IAPPayGameServiceCallBack;
import com.tencent.djcity.R;
import com.tencent.djcity.item.FakeItemActivity;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.model.WxInfoModel;
import com.tencent.djcity.lib.parser.WXPayInfoParser;
import com.tencent.djcity.lib.pay.PayCore;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ServiceConfig;
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
* Modify Date: 2013-6-8 下午02:20:59 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class PayMidas extends PayCore implements OnSuccessListener<JSONObject>, OnErrorListener, IAPPayGameServiceCallBack {

	private static final int REQUEST_FLAG_PARAM = 1;
	
	private String userId   	= "";    
    private String userKey 		= "";  
    private String sessionId 	= "";   
    private String sessionType	= "";  
    
    //游戏分区id（若无分区， 默认传1）
    private String zoneId  		= "1";   
    
    private String offerId = "";
    //用户的充值数额（可选，调用相应充值接口即可）
  	private String saveValue 	= "60";   
    
    //平台信息,格式平台-渠道-系统-自定义（详细见说明文档）
	private String pf 			= "qq_m_qq_2014-android-xxxx";   
    private String pfKey		= "pfKey";  
    
  //货币类型   ACCOUNT_TYPE_COMMON:基础货币； ACCOUNT_TYPE_SECURITY:安全货币
    private String acctType     = APPayGameService.ACCOUNT_TYPE_COMMON;
    private String tokenUrl      = "";
    private int    resId        = R.drawable.sample_mofaquan;   //道具图标id, 图标像素要求：48*48,写死没问题！
    
    private BaseActivity mActivity;
	
	public PayMidas(BaseActivity activity, String orderUrl) {
		super(activity, orderUrl);
		mActivity = activity;
	}

	private static final String LOG_TAG = PayMidas.class.getName();

	

	
	@Override
	public void submit() {
		Account act = ILogin.getActiveAccount();
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/djapp_buy.php?" + mOrderUrl);
		if(null==ajx || null == act)
		{
			UiUtils.makeToast(mActivity, "Logout");
			return;
		}
		mActivity.showProgressLayer(mActivity.getString(R.string.prepare_for_pay));
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		
		userId = ""+ act.getUin();
		userKey = act.getSkey();
		sessionId = (act.getType()==Account.TYPE_QQ ) ? "uin" : "";
		sessionType = "skey";
	}
	
	
	@Override
	public void onError(final Ajax ajax, final Response response) {
		mActivity.closeProgressLayer();

		switch (response.getId()) {

		case REQUEST_FLAG_PARAM:
			performError("支付签名服务错误");
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
		setOrdeId(v.optString("sSerialNum"));
		offerId = v.optString("offerId");
		pf 			= v.optString("pf");
		tokenUrl      = v.optString("urlParams");
		
		AndroidPay.setOfferId(offerId);
		
		 APPayGameService.SetDelegate(this); 
 		//拉取购买道具接口
 		APPayGameService.LaunchSaveGoodsView(userId, userKey, sessionId, sessionType,
 				zoneId, pf, pfKey, tokenUrl, resId);
 		
		switch (response.getId()) {
		case REQUEST_FLAG_PARAM:
			mActivity.closeProgressLayer();
			
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public void PayGameNeedLogin() {
		UiUtils.makeToast(mActivity, "Logout~ Needing relogin");
		
		//goto LoginActivity again
	}


	@Override
	public void PayGameServiceCallBack(APPayResponseInfo responseInfo) {
		switch (responseInfo.resultCode)
		{
		//用户取消 2
		case APPayResponseInfo.PAYRESULT_CANCEL:
			performError("You canceled!");
			break;
		//参数错误 3
		case APPayResponseInfo.PAYRESULT_PARAMERROR:
			this.performError("Parames Error");
			break;
		//支付流程成功  0
		case APPayResponseInfo.PAYRESULT_SUCC:
			performSuccss("");
			break;
			//支付流程失败  -1	
		default:
			performError("Error");
			break;
		
		}
		// TODO Auto-generated method stub
		
	}

}


	


