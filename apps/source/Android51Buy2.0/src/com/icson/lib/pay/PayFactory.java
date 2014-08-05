package com.icson.lib.pay;

import com.icson.lib.AppStorage;
import com.icson.lib.pay.ali.PayAli;
import com.icson.lib.pay.cft.CFTPay;
import com.icson.lib.pay.wx.PayWx;
import com.icson.util.activity.BaseActivity;

public class PayFactory {

	public static final int PAY_ALI = 21;
	public static final int PAY_CFT = 8;
	public static final int PAY_WX = 502;

	public static PayCore getInstance(BaseActivity activity, int payType, String orderCharId,boolean isVP) {
		PayCore mPayCore = null;

		switch (payType) {
		case PAY_ALI:
			mPayCore = new PayAli(activity, orderCharId,isVP);
			break;
		case PAY_CFT:
			mPayCore = new CFTPay(activity, orderCharId,isVP);
			break;
		case PAY_WX:
			mPayCore = new PayWx(activity, orderCharId,isVP);
			break;
		default:
		//	Log.e(LOG_TAG, "getInstance|payType: " + payType + " is not found");
			break;
		}
		
		if( null != mPayCore ) {
			AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
		}

		return mPayCore;
	}
	
	public interface PayResponseListener {
		void onSuccess(String... message);

		void onError(String... message);
	}
}
