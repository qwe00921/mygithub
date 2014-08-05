package com.icson.order;

import android.content.DialogInterface;
import android.text.TextUtils;

import com.icson.R;
import com.icson.lib.model.OrderModel;
import com.icson.lib.pay.PayCore;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;

public class OrderSuccessView implements PayResponseListener {

	private OrderModel mOrderModel;

	private PayCore mPayCore;

	private OrderConfirmActivity mActivity;

	public OrderSuccessView(OrderConfirmActivity activity, OrderModel orderModel) {
		mActivity = activity;
		mOrderModel = orderModel;
	}

	@Override
	public void onSuccess(String... message) {
		UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_pay_success, R.string.btn_ok, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				mActivity.orderFinish();
			}
		});
	}

	@Override
	public void onError(String... message) {
		String str = ((message == null || message[0] == null) ? "未知错误" : message[0]);
		UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_pay_failed), str, R.string.btn_retry, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					mPayCore.submit();
				} else if (nButtonId == AppDialog.BUTTON_NEGATIVE) {
					mActivity.orderFinish();
				}
			}
		});
	}

	public void success() {
		if(mOrderModel.isPackage())
			mPayCore = mOrderModel.isPayTypeIsOnline() && mOrderModel.getCash() > 0 ? PayFactory.getInstance(mActivity, mOrderModel.getPayType(), mOrderModel.getPackageOrderId(),false) : null;
		else
			mPayCore = mOrderModel.isPayTypeIsOnline() && mOrderModel.getCash() > 0 ? PayFactory.getInstance(mActivity, mOrderModel.getPayType(), mOrderModel.getOrderCharId(),false) : null;
		if (mPayCore == null) {
			String strMsg = mActivity.getString(R.string.message_order_success);
			if( !TextUtils.isEmpty(mOrderModel.getCouponSendRule()) ) {
				strMsg = strMsg + "。" + mOrderModel.getCouponSendRule();
			}
			UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_hint), strMsg, R.string.btn_ok, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					mActivity.orderFinish();
				}
			});
			return;
		}

		mPayCore.setPayResponseListener(this);
		//UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.caption_order_success, 
		//		R.string.btn_pay_now, R.string.btn_pay_notnow,new AppDialog.OnClickListener()
		
		String strMsg = mActivity.getString(R.string.message_pay_order);
		if( !TextUtils.isEmpty(mOrderModel.getCouponSendRule()) ) {
			strMsg = strMsg + "。" + mOrderModel.getCouponSendRule();
		}
		UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_order_success), strMsg,
				R.string.btn_pay_notnow, R.string.btn_pay_now, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if(nButtonId == DialogInterface.BUTTON_POSITIVE)
				{
					mActivity.orderFinish();
				}
				else
					mPayCore.submit();
			}
		});
	}

}
