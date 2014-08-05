package com.tencent.djcity.my;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.lib.model.BaseModel;

public class OrderItemModel extends BaseModel {
	private long mPayAmount;
	private String mSerialNum;
	private String mGoodName;
	private String mGoodPic;
	private int mStatus;
	
	public void parse(JSONObject json) throws JSONException {
		mPayAmount = json.optLong("iPayAmount", 0);
		mSerialNum = json.optString("sSerialNum", "");
		mGoodName = json.optString("sGoodsName", "");
		mGoodPic = json.optString("sGoodsPic", "");
		mStatus = json.optInt("iStatus", 0);
	}
	
	public long getPayAmount(){
		return mPayAmount;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public String getGoodName(){
		return mGoodName;
	}
	
	public String getGoodPic(){
		return mGoodPic;
	}
	
	public String getSerialNum(){
		return mSerialNum;
	}
	

}
