/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: FreeGiftsActivity.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: Dec 20, 2013
 * 
 */
package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.shoppingcart.FreeGiftsAdapter.OnChooseGiftListener;
import com.icson.util.AjaxUtil;
import com.icson.util.IcsonApplication;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class FreeGiftsActivity extends BaseActivity implements OnGroupExpandListener, OnChildClickListener, OnChooseGiftListener,OnSuccessListener<ArrayList<PromoRuleModel>>{
	private ArrayList<PromoRuleModel> mRuleModels;
	private TextView mChooseInfo;
	private ExpandableListView mListView;
	private Button mCommitButton;
	private FreeGiftsAdapter mAdapter;
	private PromoRuleParser mParser;
	private TextView mTitle;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_free_gifts);
		this.loadNavBar(R.id.free_gifts_navbar);
		
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
		mAdapter.setOnChooseGiftListener(this);
		
		mListView = (ExpandableListView) findViewById(R.id.free_gifts_expandablelistview);
		mListView.setAdapter(mAdapter);
		mListView.setOnGroupExpandListener(this);
		mListView.setOnChildClickListener(this);
		
		mTitle = (TextView) findViewById(R.id.free_gifts_intro);
		mTitle.setText(Html.fromHtml(getString(R.string.free_gifts_title)));
		
		mChooseInfo = (TextView) findViewById(R.id.free_gifts_choose);
		
		mCommitButton = (Button) findViewById(R.id.free_gifts_button);
		mCommitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				submitGifts();
			}
		});
		
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
	
	/*
	 * Summit products of FreeGifts 
	 */
	private void submitGifts( ) {
		//First find products chosen by user locally
		ArrayList<PromoItem> promoItems = new ArrayList<PromoItem>();
		if(null != mRuleModels) {
			for( PromoRuleModel ruleModel : mRuleModels) {
				ArrayList<ProductOfPromoRuleModel> products = ruleModel.getProducts();
				if(null != products) {
					for(ProductOfPromoRuleModel product : products) {
						if(FreeGiftsAdapter.PRODUCT_SELECTED == product.getLocalSelectedStatus()) {
							String strPromoId = String.valueOf(ruleModel.getRuleId());
							String strPid = String.valueOf(product.getProductId());
							PromoItem item = new PromoItem(strPid, strPromoId);
							promoItems.add(item);
						}
					}
				}
			}
		}
		
		if(0 == promoItems.size()) {
			UiUtils.makeToast(this, "您没有选择赠品哦", true);
			finish();
		}else{
			//submit products
			//	Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_ADD_PRODUCTS);
			Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/addpromotionproduct?mod=cart");
			if (null == ajax)
				return;
			String strIDS = "";
			for(PromoItem item : promoItems) {
				// 商品id|数量|主商品id|多价格id|购买路径|商品类型:0普通1套餐2加价购3满赠|场景id
				strIDS += item.getProductId() + "|1|0|0|" + IcsonApplication.getPageRoute() + "|3|0|" + item.getPromoRuleId() + ",";
			}
			strIDS.trim();
			strIDS = strIDS.substring(0, strIDS.length()-1);
			
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("district", FullDistrictHelper.getDistrictId());
			data.put("uid", ILogin.getLoginUid());
			data.put("ids",strIDS);
			ajax.setData(data);
			ajax.setParser(new JSONParser());
			ajax.setOnErrorListener(this);
			ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
				@Override
				public void onSuccess(JSONObject v, Response response) {
					closeProgressLayer();
					final int errno = v.optInt("errno", -1);
					if (errno == 0) {
						UiUtils.makeToast(FreeGiftsActivity.this, getString(R.string.free_gifts_success), true);
						finish();
					} else {
						String strErrMsg = v.optString("data", getString(R.string.free_gifts_fail));
						UiUtils.makeToast(FreeGiftsActivity.this, strErrMsg);
					}
				}
			});
			
			showProgressLayer();
			ajax.send();
		}
	}

	@Override
	protected void onDestroy() {
		mParser = null;
		mRuleModels = null;
		mListView = null;
		mChooseInfo = null;
		mCommitButton = null;
		mParser = null;
		mAdapter = null;
		mTitle = null;
		
		super.onDestroy();
	}

	@Override
	public void onSuccess(ArrayList<PromoRuleModel> models, Response response) {
		closeLoadingLayer();
		if(mParser.isSuccess() && null != models) {
			mRuleModels.clear();
			//筛选促销规则是满赠的
			for(PromoRuleModel model : models) {
				if(PromoRuleModel.BENEFIT_TYPE_FREEGIFT == model.getBenefitType()){
					mRuleModels.add(model);
					//默认打开第一组规则
					mListView.expandGroup(0);
				}
			}
			
			mAdapter.notifyDataSetChanged();
			updateChooseStatusView();
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mAdapter.chooseGift(groupPosition, childPosition);
		mAdapter.notifyDataSetChanged();
		
		return true;
	}


	@Override
	public void onGroupExpand(int groupPosition) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onChooseListener() {
		updateChooseStatusView();
	}
	
	/*
	 * update choose number view
	 */
	private void updateChooseStatusView(){
		int nTotalNum = 0;
		int nChooseNum = 0;
		if(null != mRuleModels) {
			nTotalNum = mRuleModels.size();
			for( PromoRuleModel ruleModel : mRuleModels) {
				ArrayList<ProductOfPromoRuleModel> products = ruleModel.getProducts();
				if(null != products) {
					for(ProductOfPromoRuleModel product : products) {
						if(FreeGiftsAdapter.PRODUCT_SELECTED == product.getLocalSelectedStatus() || FreeGiftsAdapter.PRODUCT_SELECTED == product.getSelectedStatus()) {
							nChooseNum ++ ;
						}
					}
				}
			}
		}
		
		mChooseInfo.setText("已选" + nChooseNum +"件/" + nTotalNum + "件");
	}
	
	public class PromoItem{
		private String pProductId;
		private String pPromoRuleId;
		
		public PromoItem(String pid, String pRuleId) {
			pProductId = pid;
			pPromoRuleId = pRuleId;
		}
		
		public String getProductId() {
			return this.pProductId;
		}
		
		public String getPromoRuleId() {
			return this.pPromoRuleId;
		}
	}

	@Override
	public String getActivityPageId() {
		int qingliang;
		return null;
	}
	
}
