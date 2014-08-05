package com.icson.my.coupon;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.order.coupon.CouponModel;
import com.icson.order.coupon.CouponParser;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class CouponShowActivity extends BaseActivity implements
		OnClickListener {

	protected CouponParser mParser;
	ArrayList<CouponModel> models;
	int evtno = 0;

	Button getBtn;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		setContentView(R.layout.coupon_get_activity);
		this.loadNavBar(R.id.coupon_show_navbar);

//		((TextView) findViewById(R.id.global_textview_title)).setText("领取优惠券");

		findViewById(R.id.btnMycoupon).setOnClickListener(this);
		init();

	}

	private void init() {
		
		mParser = new CouponParser();
		models = new ArrayList<CouponModel>();

		Ajax ajax = ServiceConfig.getAjax(Config.URL_EVENT_COUPON);
		if( null == ajax )
			return ;
		
		ajax.setParser(mParser);
		ajax.setOnErrorListener(this);
				
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<CouponModel>>() {
			@Override
			public void onSuccess(ArrayList<CouponModel> v, Response response) {
				closeLoadingLayer(true);
				if( !mParser.isSuccess() ) {
					UiUtils.makeToast(CouponShowActivity.this, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
					return;
				}
				models = v;
				showCoupons();
			}
		});
		addAjax(ajax);
		showLoadingLayer();
		ajax.send();

		
	}

	protected void showCoupons() {
		if (models == null) {
			findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
			return;
		}
		
		/* 多个优惠券
		mCouponAdapter = new CouponAdapter(this, models, true);
		mListView.setAdapter(mCouponAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				mCouponModel = models.get(position);
				dowith(mCouponModel.code);
			}
		});*/
		if (models.size() == 0) {
			findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
		}
		
		CouponModel coupon = models.get(0);
		TextView tvCMT = (TextView)findViewById(R.id.tvCMT);
		tvCMT.setText(String.valueOf(coupon.coupon_amt/100));
		
		TextView content = (TextView)findViewById(R.id.tvContent);
		content.setText(coupon.content);
		
		TextView date = (TextView)findViewById(R.id.tvDate);
		date.setText(coupon.valid_time_from+" 至 "+coupon.valid_time_to);
		evtno = coupon.evtno;
		getBtn = (Button )findViewById(R.id.btnGet);
		if(coupon.status < 0){
			getBtn.setText("敬请期待");
			getBtn.setTextColor(getResources().getColor(R.color.global_button_submit_d));
			getBtn.setEnabled(false);
		}else if(coupon.status == 0){
			getBtn.setTextColor(getResources().getColor(R.color.global_button_submit));
			getBtn.setOnClickListener(this);
		}else if(coupon.status > 0){
			getBtn.setText("已领完");
			getBtn.setTextColor(getResources().getColor(R.color.global_button_submit_d));
			getBtn.setEnabled(false);
		}
		
	}

	protected void getCoupon(int evtno) {
		
		final long uid = ILogin.getLoginUid();
		if(uid == 0){
			ToolUtil.checkLoginOrRedirect(this, CouponShowActivity.class);
			finish();
			return ;
		}
		
				
		String strInfo = "" + evtno+"&"+System.currentTimeMillis();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_GET_COUPON_EVTNO, strInfo);
		
		if( null == ajax )
			return ;

		ajax.setParser(new JSONParser());
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject json, Response response) {
				closeLoadingLayer(true);
				//errno = 0 既是成功
				try {
					int errno = json.getInt("errno");
					if(errno == 0||errno == 8){
						getBtn.setText("已领取");
						getBtn.setEnabled(false);
						getBtn.setTextColor(getResources().getColor(R.color.global_button_submit_d));
					}
					if (errno != 0) {
						String mErrMsg = json.optString("data", getString(R.string.message_server_error));
						UiUtils.makeToast(CouponShowActivity.this, mErrMsg);
					}else{
						UiUtils.makeToast(CouponShowActivity.this, "您已成功领取此券，存放于我的优惠券中");
					}
					
				} catch (Exception ex) {
					Log.e(CouponShowActivity.class.getName(), ex);
				}

			}
		});
		addAjax(ajax);
		ajax.send();

	}

	@Override
	public void onClick(View v) {
		String pageId= getString(R.string.tag_CouponShowActivity);
		switch (v.getId()) {
		// 查看我的优惠券列表
		case R.id.btnMycoupon:
			ToolUtil.checkLoginOrRedirect(this, MyCouponActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), pageId, MyCouponActivity.class.getName(), getString(R.string.tag_MyCouponActivity), "01012");
			finish();
			break;
		//领取优惠券
		case R.id.btnGet:
			getCoupon(evtno);
			ToolUtil.sendTrack(this.getClass().getName(), pageId, CouponShowActivity.class.getName(), getString(R.string.tag_CouponShowActivity), "01011");
			break;
		
		}
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_CouponShowActivity);
	}

}
