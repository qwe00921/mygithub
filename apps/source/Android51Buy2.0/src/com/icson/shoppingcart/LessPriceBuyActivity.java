/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: LessPriceBuyActivity.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: Dec 20, 2013
 * 
 */
package com.icson.shoppingcart;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Html;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.util.AjaxUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class LessPriceBuyActivity extends BaseActivity implements OnGroupExpandListener, OnSuccessListener<ArrayList<PromoRuleModel>>{
	private ArrayList<PromoRuleModel> mRuleModels;
	private ExpandableListView mListView;
	private FreeGiftsAdapter mAdapter;
	private PromoRuleParser mParser;
	private TextView mTitle;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_less_price_buy);
		this.loadNavBar(R.id.lessprice_buy_navbar);
		
		initUI();
		initData();
	}
	
	/*
	 * initiate UI and initiate params
	 */
	private void initUI(){
		mParser = new PromoRuleParser();
		mRuleModels = new ArrayList<PromoRuleModel>();
		mAdapter = new FreeGiftsAdapter(this, mRuleModels);
		
		mListView = (ExpandableListView) findViewById(R.id.lessprice_buy_expandablelistview);
		mListView.setAdapter(mAdapter);
		mListView.setOnGroupExpandListener(this);
		
		mTitle = (TextView) findViewById(R.id.lessprice_buy_intro);
		mTitle.setText(Html.fromHtml(getString(R.string.less_price_buy_title)));
	}
	
	/*
	 * Get data from server
	 */
	private void initData(){
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/getpromotionrule?mod=cart");
		if( null == ajax )
			return ;
		showLoadingLayer();
		ajax.setData("district", FullDistrictHelper.getDistrictId());
		ajax.setData("whId", ILogin.getSiteId());
		ajax.setData("source", "3001");
		ajax.setData("cmd", "603");
		ajax.setData("ism", "0");
		ajax.setData("isPackage", "0");
		ajax.setData("uid", ILogin.getLoginUid());
		
		ajax.setOnSuccessListener(this);
		ajax.setParser(mParser);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		ajax.send();
	}
	

	@Override
	protected void onDestroy() {
		mParser = null;
		mRuleModels = null;
		mListView = null;
		mTitle = null;
		mAdapter = null;
		
		super.onDestroy();
	}

	@Override
	public void onSuccess(ArrayList<PromoRuleModel> models, Response response) {
		closeLoadingLayer();
		if(mParser.isSuccess() && null != models) {
			mRuleModels.clear();
			for(PromoRuleModel model : models) {
				if(PromoRuleModel.BENEFIT_TYPE_LESSPRICEBUY == model.getBenefitType()){
					mRuleModels.add(model);
					//默认打开第一组规则
					mListView.expandGroup(0);
				}
			}
			
			mAdapter.notifyDataSetChanged();
		}
	}


	@Override
	public void onGroupExpand(int groupPosition) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public String getActivityPageId() {
		int qingliang;
		return null;
	}
}
