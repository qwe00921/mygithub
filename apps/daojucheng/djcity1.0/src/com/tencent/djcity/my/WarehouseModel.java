package com.tencent.djcity.my;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.lib.model.BaseModel;

public class WarehouseModel extends BaseModel {
	private String mGoodClass;
	private String mGoodName;
	private String mGoodPicUrl;
	private int mQuantity;
	
	public void parse(JSONObject json) throws JSONException {
		mGoodClass = json.optString("sItemClass", "");
		mGoodName = json.optString("sItemName", "");
		mGoodPicUrl = json.optString("sItemPic", "");
		mQuantity = json.optInt("iQuantity", 0);
	}
	
	
	public int getQuantity(){
		return mQuantity;
	}
	
	public String getGoodName(){
		return mGoodName;
	}
	
	public String getGoodPicUrl(){
		return mGoodPicUrl;
	}
	
	public String getGoodClass(){
		return mGoodClass;
	}
	

}
