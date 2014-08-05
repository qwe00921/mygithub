package com.tencent.djcity.lib.pay;

import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.pay.PayFactory.PayResponseListener;
import com.tencent.djcity.util.activity.BaseActivity;

public abstract class PayCore {

	private static final String LOG_TAG = PayCore.class.getName();

	protected BaseActivity mActivity;
	protected String mOrderId;
	protected String mOrderUrl;
	protected boolean isVP;

	protected PayResponseListener mPayResponseListener;
	
	public abstract void submit();
	
	public PayCore(BaseActivity activity, String orderUrl) {
		mActivity = activity;
		mOrderUrl = orderUrl;
	}
	
	public void setOrdeId(String orderId) {
		mOrderId = orderId;
	}
	public String getOrderId() {
		return mOrderId;
	}
	public void setOrderUrl(String orderUrl) {
		mOrderUrl = orderUrl;
	}

	
	public boolean checkParam() {
		if (ILogin.getLoginUin() == 0) {
			performError("您已退出登录");
			return false;
		}

		if (mOrderUrl == null || mOrderUrl.equals("")) {
			performError("订单号为空");
			return false;
		}

		return true;
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
