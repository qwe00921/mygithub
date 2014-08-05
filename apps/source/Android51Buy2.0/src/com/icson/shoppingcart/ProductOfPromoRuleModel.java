/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: ProductOfPromoRuleModel.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: Dec 20, 2013
 * 
 */
package com.icson.shoppingcart;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

@SuppressWarnings("serial")
public class ProductOfPromoRuleModel extends BaseModel implements Serializable{
	private long mProductId;
	private String mProductCharId;
	private String mProductName;
	private double mPrice;
	private double mPromoPrice;
	private int mSelectedStatus;  		//用户是否已经选择
	private int mStockStatus;			//是否有货
	private int mLocalSelectedStatus;	//本地用户选择的标记
	
	public long getProductId() {
		return mProductId;
	}
	
	public String getProductCharId() {
		return this.mProductCharId;
	}
	
	public String getName(){
		return this.mProductName;
	}
	
	public double getPrice(){
		return this.mPrice;
	}
	
	public double getPromoPrice(){
		return this.mPromoPrice;
	}
	
	public int getSelectedStatus(){
		return this.mSelectedStatus;
	}
	
	public void setSelectedStatus(int selectedStatus) {
		this.mSelectedStatus = selectedStatus;
	}
	
	public int getStockStatus(){
		return this.mStockStatus;
	}
	
	public void setLocalSelectedStatus(int status) {
		this.mLocalSelectedStatus = status;
	}
	
	public int getLocalSelectedStatus() {
		return this.mLocalSelectedStatus;
	}
	
	public void parse(JSONObject json) throws JSONException{
		mProductId = json.optLong("commodityId", 0);
		mProductCharId = json.optString("productCharId", "");
		mProductName = json.optString("name", "");
		mPrice = json.optDouble("price", 0);
		mPromoPrice = json.optDouble("promotion_price", 0);
		mSelectedStatus = json.optInt("selected", 0) ; 
		mStockStatus = json.optInt("stockNum", 0);
	}

}
