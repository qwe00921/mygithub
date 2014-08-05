package com.icson.lib.pay;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.UiUtils;
import com.icson.statistics.StatisticsConfig;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.activity.BaseActivity;

public abstract class PayCore {

	private static final String LOG_TAG = PayCore.class.getName();

	protected BaseActivity mActivity;

	protected String mOrderCharId;
	protected boolean isVP;

	protected PayResponseListener mPayResponseListener;
	
	public abstract void submit();
	
	public PayCore(BaseActivity activity, String orderCharId,boolean isVP) {
		mActivity = activity;
		mOrderCharId = orderCharId;
		this.isVP = isVP;
	}
	
	public PayCore(BaseActivity activity, String orderCharId) {
		this(activity,orderCharId,false);
	}

	public void setOrderCharId(String orderCharId) {
		mOrderCharId = orderCharId;
	}

	public String getOrderCharId() {
		return mOrderCharId;
	}

	public boolean checkParam() {
		if (ILogin.getLoginUid() == 0) {
			performError("您已退出登录");
			return false;
		}

		if (mOrderCharId == null || mOrderCharId.equals("")) {
			performError("订单号为空");
			return false;
		}

		return true;
	}

	protected boolean checkIcsonResponse(JSONObject json) {
		boolean result = true;
		int errno = 0;
		try {
			errno = json.getInt("errno");
			if (errno != 0) 
			{
				UiUtils.makeToast(mActivity, json.optString("msg", Config.NORMAL_ERROR));
				result = false;
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			performError(json.optString("msg", "网络异常"));
			result = false;
		}
		
		StatisticsEngine.alert("pay", StatisticsConfig.PRIORITY_WARN, errno, json.optString("msg"), mOrderCharId, ILogin.getLoginUid());

		return result;
	}

	public void setPayResponseListener(PayResponseListener listener) {
		mPayResponseListener = listener;
	}

	public PayResponseListener getPayResponseListener() {
		return mPayResponseListener;
	}

	protected void performSuccss(String... message) {
		if (mPayResponseListener != null) {
			mPayResponseListener.onSuccess(message);
		}
	}

	protected void performError(String... message) {
		if (mPayResponseListener != null) {
			mPayResponseListener.onError(message);
		}
	}
}
