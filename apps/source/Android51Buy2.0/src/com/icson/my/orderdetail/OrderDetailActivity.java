package com.icson.my.orderdetail;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.icson.R;
import com.icson.amap.CargoMapActivity;
import com.icson.lib.model.OrderModel;
import com.icson.lib.pay.cft.CFTPayActivity;
import com.icson.lib.ui.UiUtils;
import com.icson.my.orderlist.VPOrderModel;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class OrderDetailActivity extends BaseActivity {
	private static final String LOG_TAG = OrderDetailActivity.class.getName();
	public static final String 	REQUEST_ORDER_CHAR_ID 	= "request_order_char_id";
	public static final String 	REQUEST_ORDER_STATUS 	= "request_order_status";
	public static final String 	REQUEST_VP_ORDER 		= "request_vp_order";
	public static final String 	RESULT_ORDER_MODEL 		= "result_order_model";
	
	public static final int RESULT_FLAG_ORDER_STATUS 	= 1;
	public static final int REQUEST_CODE 				= 5239123;
	private boolean 		mIsOperated 				= false;
	
	private OrderDetailView mOrderDetailView;
	private String 			orderCharId;
	private VPOrderModel 	mVPOrderModel;
	

	public void setIsOperate(boolean mIsOperated) {
		this.mIsOperated = mIsOperated;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		orderCharId = getIntent().getStringExtra(REQUEST_ORDER_CHAR_ID);
		
		setContentView(R.layout.activity_my_orderdetail);
		
		if (orderCharId != null) {
			//普通订单
			initOrderDetailView();
		}else if (getIntent().getSerializableExtra(REQUEST_VP_ORDER) != null) {
			mVPOrderModel = (VPOrderModel) getIntent().getSerializableExtra(REQUEST_VP_ORDER);
			//虚拟订单
			initVPOrderDetailView();
			
		}else {//异常情况
			Log.e(LOG_TAG, "onCreate|orderCharId is null.");
			UiUtils.makeToast(this, "打开订单失败",true);
			finish();
			return;
		}
	}
	private void initVPOrderDetailView() {
		setContentView(R.layout.activity_my_vp_orderdetail);
		loadNavBar(R.id.orderdetail_vp_navigation_bar);

		final VPOrderDetailView mVPOrderDetailView = new VPOrderDetailView(this, mVPOrderModel);
		findViewById(R.id.orderdetail_button_pay).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mVPOrderDetailView.pay();
				}
		});
	}

	public void initOrderDetailView() {
		
		loadNavBar(R.id.orderdetail_navigation_bar);
		mOrderDetailView = new OrderDetailView(this);
		mOrderDetailView.getOrderInfo(orderCharId);
		mOrderDetailView.getOrderFlow(orderCharId);

		findViewById(R.id.orderdetail_button_cancel).setOnClickListener(this);
		findViewById(R.id.orderdetail_button_pay).setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		// 取消订单
		final int viewId = view.getId();
		switch (viewId) {
		case R.id.orderdetail_button_cancel:
			mOrderDetailView.cancelOrder(false);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_OrderDetailActivity), OrderDetailActivity.class.getName(), getString(R.string.tag_OrderDetailActivity), "03011");
			break;
		case R.id.orderdetail_button_pay:
			mOrderDetailView.pay();
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_OrderDetailActivity), OrderDetailActivity.class.getName(), getString(R.string.tag_OrderDetailActivity), "02011");
			break;
		}
	}

	@Override
	public void onDestroy() {
		orderCharId = null;
		mVPOrderModel = null;
		
		if (mOrderDetailView != null) {
			mOrderDetailView.destroy();
			mOrderDetailView = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mOrderDetailView != null && mIsOperated
					&& mOrderDetailView.getOrderModel() != null) {
				Intent intent = getIntent();
				intent.putExtra(RESULT_ORDER_MODEL,
						mOrderDetailView.getOrderModel());
				setResult(RESULT_FLAG_ORDER_STATUS, intent);
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// OrderProductModel model = (OrderProductModel)
			// getIntent().getSerializableExtra(OrderEvaluateActivity.ORDER_PRODUCT_MODEL);
			if(orderCharId != null){
				initOrderDetailView();
			}else if(mVPOrderModel != null){
				mVPOrderModel.setStatus(1);
				mVPOrderModel.setStatus_name("已支付");
				initVPOrderDetailView();
			}else{
				finish();
			}
			
		}//财付通支付结果处理
		else if(requestCode == CFTPayActivity.REQUEST_CFT_PAY){
			
			if(resultCode == RESULT_OK){
				UiUtils.makeToast(this, "支付成功, 订单状态稍有迟延，请稍等.",true);
				if(orderCharId != null){
					initOrderDetailView();
				}else if(mVPOrderModel != null){
					mVPOrderModel.setStatus(1);
					mVPOrderModel.setStatus_name("已支付");
					initVPOrderDetailView();
				}else{
					finish();
				}
			}else{
				UiUtils.makeToast(this, "财付通支付失败！");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startMap() {

		// Show map view.
		OrderModel pModel = mOrderDetailView.getOrderModel();
		if( null != pModel ) {
			String strTelephone = TextUtils.isEmpty(pModel.getReceiverMobile()) ? pModel.getReceiverTel() : pModel.getReceiverMobile();
			CargoMapActivity.showMap(this, pModel.getReceiver(), strTelephone, pModel.getReceiverAddress(), pModel.getOrderCharId());
		}		
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_OrderDetailActivity);
	}
}
