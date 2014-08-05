package com.icson.order.coupon;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class CouponListActivity extends BaseActivity implements
		OnClickListener, OnSuccessListener<CouponModel> {

	public static final String COUPON_MODEL = "coupon_model";
	public static final String AMT = "amt";
	public static final String DISTRICT = "district";
	public static final String PAYTYPE = "paytype";
	public static final String ITEMS = "items";
	public static final String IS_OFFLINE = "is_offline";
	
	private TextView mCouponListLabel;

	protected CouponParser mParser;
	ArrayList<CouponModel> models;
	CouponAdapter mCouponAdapter;
	CouponModel mCouponModel;
	CouponModel mValidCouponModel;
	double amt;
	int district;
	int paytype;
	String items;
	boolean isOffLine;

	ListView mListView;
	//手动输入code
	EditText inputCode;
	Button inputBtn;
	TextView mEmptyText;
	OnTouchListener hideSoftInputListener;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.activity_list_coupon);
		this.loadNavBar(R.id.order_coupon_list_navigation_bar);
		
		init();
	}
	
	/*
	 * Get user's coupons from server
	 */

	private void init() {
		mCouponListLabel = (TextView) findViewById(R.id.selecte_label);
		mListView = (ListView) findViewById(R.id.list_listview);
		mEmptyText = (TextView) findViewById(R.id.list_relative_empty);
		hideSoftInputListener = new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					UiUtils.hideSoftInput(CouponListActivity.this, inputCode);
				}
				return false;
			}
		};
		
		mListView.setOnTouchListener(hideSoftInputListener);	
		mEmptyText.setOnTouchListener(hideSoftInputListener);
		
		mCouponModel = (CouponModel) getIntent().getSerializableExtra(
				COUPON_MODEL);
		mValidCouponModel = mCouponModel;
		
		amt = getIntent().getDoubleExtra(AMT, 0);
		district = getIntent().getIntExtra(DISTRICT, 0);
		paytype = getIntent().getIntExtra(PAYTYPE, 0);
		items = getIntent().getStringExtra(ITEMS);
		isOffLine = getIntent().getBooleanExtra(IS_OFFLINE,false);
		
		models = new ArrayList<CouponModel>();
		mParser = new CouponParser();

		final long uid = ILogin.getLoginUid();
		
		//Config.URL_GET_USER_COUPON 和	URL_ORDER_GETCOUPON都是拉取优惠券列表的接口，前一个在我的易迅里使用，后一个在订单确认页使用
		//目前先和iphone保持一致，使用Config.URL_GET_USER_COUPON
		Ajax ajax = ServiceConfig.getAjax(Config.URL_GET_USER_COUPON, uid);
		if( null == ajax )
			return ;
		
		ajax.setData("uid", uid);
		ajax.setParser(mParser);
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<CouponModel>>() {
			@Override
			public void onSuccess(ArrayList<CouponModel> v, Response response) {
				closeLoadingLayer(true);
				if( !mParser.isSuccess() ) {
					UiUtils.makeToast(CouponListActivity.this, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
					return;
				}
				
				models = v;
				refreshCoupons();
			}
		});
		addAjax(ajax);
		showLoadingLayer();
		ajax.send();

		inputCode = (EditText)findViewById(R.id.code_EditText);
		//if(mCouponModel!=null){
		//	inputCode.setText(mCouponModel.code);
		//	inputCode.setSelection(mCouponModel.code.length());
		//}
		
		inputBtn = (Button)findViewById(R.id.useBtn);
		inputBtn.setOnClickListener(this);
	}
	
	protected void refreshCoupons() {
		if (models == null) {
			mEmptyText.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			return;
		}
		
		mEmptyText.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		mCouponListLabel.setText(Html.fromHtml("我的优惠券 <color=\"999999\">(" + models.size() + ")</color>"));
		
		//当前使用的 优惠券
		if(mCouponModel != null){
			for (CouponModel model : models) {
				if (model.code.equals(mCouponModel.code)) {
					model.isUseNow = true;
					break;
				}
			}
		}
		
		mCouponAdapter = new CouponAdapter(this, models, true);
		mListView.setAdapter(mCouponAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				
				mCouponModel = models.get(position);
				mCouponModel.isUseNow = true;
				for(CouponModel model : models){
					if(model != mCouponModel)
						model.isUseNow = false;
				}
				mCouponAdapter.notifyDataSetChanged();
				dowith(mCouponModel.code);
			}
		});
		
		if (models.size() == 0) {
			findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
		}
		ToolUtil.sendTrack(CouponListActivity.class.getName(), getString(R.string.tag_CouponListActivity), CouponListActivity.class.getName(), getString(R.string.tag_CouponListActivity), "05011");
	}

	/*
	 * Check whether selected coupon satisfies  or not
	 * 
	 */
	protected void dowith(String code) {
		if(code == null || "".equals(code)){
			UiUtils.makeToast(this, "请输入优惠券代码");
			return ;
		}
		// 开始检测优惠券是否可以使用
		// http://app.51buy.com/json.php?mod=aorder&act=checkcoupon&uid=2252412
		// post :$_POST['couponcode'],
		// $_POST['district'] 送货地址mModel.getDistrict(),
		// $_POST['paytype'],支付方式mModel.getPayType()
		// $_POST['items']此为商品列表model.getProducts()

		final long uid = ILogin.getLoginUid();
		// final String url =
		// "http://test.m.51buy.com/app/json.php?mod=acoupon&act=list&uid=" +
		// uid;

		Ajax ajax = ServiceConfig.getAjax(Config.URL_CHECK_USER_COUPON, uid);
		if( null == ajax )
			return ;
		ajax.setData("couponcode", code);
		ajax.setData("district", district);
		ajax.setData("paytype", paytype);
		ajax.setData("items", items);
		ajax.setData("isOffLine", isOffLine);

		ajax.setParser(new CouponCheckParser());
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(this);
		addAjax(ajax);
		ajax.send();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 收货地址
//		case R.id.btnCancel:
//			setResult(RESULT_CANCELED);
//
//			finish();
//			break;
		// 用户输入优惠券 
		case R.id.useBtn:
			dowith(inputCode.getEditableText().toString().trim());
			break;
		}
	}

	@Override
	public void onSuccess(CouponModel v, Response response) {

		if (v != null) {
			if(mCouponModel==null || !mCouponModel.code.equals(v.code)){
				mCouponModel = v;
			}
			//don't put key into here
			//inputCode.setText(mCouponModel.code);
			//inputCode.setSelection(mCouponModel.code.length());
			Intent data = new Intent();
			data.putExtra(COUPON_MODEL, mCouponModel);
			setResult(RESULT_OK, data);
			finish();
		} else {
			UiUtils.makeToast(this, R.string.message_coupon_not_match);
			mCouponModel.isUseNow = false;
			//当前有效的优惠券
			if(mValidCouponModel != null){
				for (CouponModel model : models) {
					if (model.code.equals(mValidCouponModel.code)) {
						model.isUseNow = true;
						break;
					}
				}
			}
			mCouponAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_CouponListActivity);
	}
}
