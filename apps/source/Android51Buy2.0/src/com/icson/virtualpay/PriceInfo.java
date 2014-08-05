package com.icson.virtualpay;

import org.json.JSONException;
import org.json.JSONObject;

public class PriceInfo {
	private int mPayType;
	private String mPayTypeName;
	private String mPrice;
	private String mMessage;

	public int getPayType() {
		return mPayType;
	}

	public void setPayType(int payType) {
		this.mPayType = payType;
	}

	public String getPayTypeName() {
		return mPayTypeName;
	}

	public void setPayTypeName(String payTypeName) {
		this.mPayTypeName = payTypeName;
	}

	public String getPrice() {
		return mPrice;
	}

	public void setPrice(String price) {
		this.mPrice = price;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public void parse(JSONObject json) throws JSONException {
		if(json == null) {
			return;
		}
		
		setMessage(json.optString("msg", ""));
		setPayType(json.optInt("payType", 0));
		setPayTypeName(json.optString("payTypeName", ""));
		setPrice(json.optString("price", ""));
	}
}
