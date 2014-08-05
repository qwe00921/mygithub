package com.icson.virtualpay;

import org.json.JSONObject;

import android.content.DialogInterface;

import com.icson.R;
import com.icson.lib.pay.PayCore;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;

public class VPOrderSuccessView implements PayResponseListener {

	private JSONObject mVPOrder;

	private PayCore mPayCore;

	private VirtualPayActivity mActivity;

	public VPOrderSuccessView(VirtualPayActivity activity, JSONObject data) {
		mActivity = activity;
		mVPOrder = data;
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
				if (nButtonId == DialogInterface.BUTTON_POSITIVE) {
					mPayCore.submit();
				} else if (nButtonId == DialogInterface.BUTTON_NEGATIVE) {
					mActivity.orderFinish();
				}
			}
		});
	}

	public void success() {
		String order_char_id = mVPOrder.optString("order_char_id");
		int pay_type =  mVPOrder.optInt("payType");
		if(order_char_id != null && !order_char_id.equals("")){
			mPayCore = PayFactory.getInstance(mActivity, pay_type, order_char_id,true);
		} else{
			mPayCore = null;
		}
		
		if (mPayCore == null) {
			UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_order_success, R.string.btn_ok, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					mActivity.orderFinish();
				}
			});
			return;
		}

		mPayCore.setPayResponseListener(this);
		UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.caption_order_success, 
				R.string.btn_pay_notnow, R.string.btn_pay_now,new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if(nButtonId == DialogInterface.BUTTON_POSITIVE)
					mActivity.orderFinish();
				else{
					mPayCore.submit();
				}
				
			}
		});
	}

}
