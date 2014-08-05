package com.icson.item;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ui.UiUtils;
import com.icson.shoppingcart.ProductCouponGiftModel.CouponGiftModel;
import com.icson.util.activity.BaseActivity;

public class ItemCouponGiftActivity extends BaseActivity {
	public final static String COUPON_GIFT_KEY = "coupon_model";
	private CouponGiftModel mCouponModel;
	private TextView mPicAmt;
	private TextView mCouponName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_item_coupon_gift);
		
		Intent pIntent = getIntent();
		if(null == pIntent) {
			UiUtils.makeToast(this, getString(R.string.global_error_warning));
			return;
		}
		
		mCouponModel = (CouponGiftModel) pIntent.getSerializableExtra(COUPON_GIFT_KEY);
		if(null == mCouponModel) {
			return;
		}
		
		mPicAmt = (TextView) findViewById(R.id.coupon_gift_pic_amt);
		mCouponName = (TextView) findViewById(R.id.coupon_gift_name);
		
		String strAmt = String.valueOf( mCouponModel.getCouponAmt()/100 );
		mPicAmt.setText(strAmt);
		
		int pCouponNum = mCouponModel.getCouponNum();
		String strCouponNum = pCouponNum <= 0 ? "" : "   x" + pCouponNum;
		mCouponName.setText(mCouponModel.getCouponName() + strCouponNum);
	
	}

	@Override
	protected void onDestroy() {
		mCouponModel = null;
		super.onDestroy();
	}

	@Override
	public String getActivityPageId() {
		// TODO Auto-generated method stub
		return "000000";
	}
	
}
