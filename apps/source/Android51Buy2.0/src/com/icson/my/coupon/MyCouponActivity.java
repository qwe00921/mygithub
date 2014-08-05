package com.icson.my.coupon;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.my.main.MyIcsonActivity;
import com.icson.order.coupon.CouponAdapter;
import com.icson.order.coupon.CouponModel;
import com.icson.order.coupon.CouponParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyCouponActivity extends BaseActivity implements OnErrorListener {
	private boolean firstExec = true;
	private ArrayList<CouponModel> models;
	private CouponAdapter mCouponAdapter;
	private ListView mListView;
	private CouponParser mParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_couponlist);
		loadNavBar(R.id.coupont_navigation_bar);
		
		mListView = (ListView) findViewById(R.id.coupon_listview);
		mParser = new CouponParser();
		init();
	}
	
	@Override
	protected void onDestroy() {
		
		
		mListView = null;
		mCouponAdapter = null;
		models = null;
		super.onDestroy();
	}

	public void init() {
		ToolUtil.sendTrack(MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), MyCouponActivity.class.getName(), getString(R.string.tag_MyCouponActivity), "01012");
		if (!firstExec) {
			return;
		}

		firstExec = false;
		final long uid = ILogin.getLoginUid();
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_GET_USER_COUPON, uid);
		if( null == ajax )
			return ;
		ajax.setParser(mParser);
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<CouponModel>>() {
			@Override
			public void onSuccess(ArrayList<CouponModel> v, Response response) {
				closeLoadingLayer();
				if( !mParser.isSuccess() ) {
					UiUtils.makeToast(MyCouponActivity.this, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
					return;
				}
				
				models = v;
				refreshCoupons();
			}
		});
		addAjax(ajax);
		showLoadingLayer();
		ajax.send();
	}

	protected void refreshCoupons() {
		if(null == models || 0 == models.size()){
			findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
		}else{
			mCouponAdapter = new CouponAdapter(this, models,false);
			mListView.setAdapter(mCouponAdapter);
		}
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		UiUtils.makeToast(this, "加载失败, 请重试!");
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MyCouponActivity);
	}
}
