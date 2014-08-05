package com.tencent.djcity.lib.pay;

import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.pay.midas.PayMidas;
import com.tencent.djcity.lib.pay.wx.PayWx;
import com.tencent.djcity.util.activity.BaseActivity;

public class PayFactory {

	public static final int PAY_MIDAS = 0;
	public static final int PAY_WX = 1;
	
	public static final int PAY_METHODS = 2;

	public static PayCore getInstance(BaseActivity activity, int payType, String orderUrl) {
		PayCore mPayCore = null;

		switch (payType) {
		case PAY_MIDAS:
			mPayCore = new PayMidas(activity, orderUrl);
			break;
		case PAY_WX:
			mPayCore = new PayWx(activity, orderUrl);
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
