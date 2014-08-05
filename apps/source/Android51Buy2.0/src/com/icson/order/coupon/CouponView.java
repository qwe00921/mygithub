package com.icson.order.coupon;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.icson.R;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class CouponView extends OrderBaseView<CouponModel, ArrayList<CouponModel>> {

	private static final String LOG_TAG = CouponView.class.getName();

	public static final int FLAG_REQUDST_COUPON = 122320;

	private CouponModel mCouponModel;

	public CouponView(OrderConfirmActivity activity) {
		super(activity);
		mParser = new CouponParser();
	}

	public CouponModel getCouponModel() {
		return mCouponModel;
	}


	private void updateCoupon() {
		
		TextField couponView = (TextField)mActivity.findViewById(R.id.orderconfirm_coupon);
		couponView.setContent(mCouponModel == null ? "" : "- " + mActivity.getString(R.string.rmb) + ToolUtil.toPrice(mCouponModel.coupon_amt));
	}

	public void showCouponsList(double amt,int district,int paytype,String items ,boolean isOffLine) {
		
		if(district == -1){
			UiUtils.makeToast(mActivity, "请先选择配送方式");
			return;
		}
		if(paytype == -1){
			UiUtils.makeToast(mActivity, "请先选择支付方式");
			return;
		}
		final Bundle params = new Bundle();
		params.putSerializable(CouponListActivity.COUPON_MODEL, mCouponModel);
		params.putDouble(CouponListActivity.AMT, amt);
		params.putInt(CouponListActivity.DISTRICT, district);
		params.putInt(CouponListActivity.PAYTYPE, paytype);
		params.putString(CouponListActivity.ITEMS, items);
		params.putBoolean(CouponListActivity.IS_OFFLINE, isOffLine);
		ToolUtil.checkLoginOrRedirect(mActivity, CouponListActivity.class, params, FLAG_REQUDST_COUPON);
		ToolUtil.sendTrack( mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderConfirmActivity), 
				CouponListActivity.class.getName(), mActivity.getString(R.string.tag_CouponListActivity), "05013");
	}

	public void onCouponConfirm(Intent intent) {
		if (null==intent || 
				(null != intent && intent.getSerializableExtra(CouponListActivity.COUPON_MODEL) == null)) {
			Log.e(LOG_TAG, "onUserPointConfirm|UserPointModel is null.");
			return;
		}

		mCouponModel = (CouponModel) intent.getSerializableExtra(CouponListActivity.COUPON_MODEL);
		updateCoupon();
	}
	
	public boolean setCoupon(OrderPackage pack) {
		if (mCouponModel == null) {
			pack.put("couponCode", "");
		}else{
			pack.put("couponCode", mCouponModel.code);
		}
		return true;
	}

	public void destroy() {
		mActivity = null;
	}

	@Override
	public void onSuccess(ArrayList<CouponModel> v, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Ajax ajax, Response response) {
		// TODO Auto-generated method stub

	}
}