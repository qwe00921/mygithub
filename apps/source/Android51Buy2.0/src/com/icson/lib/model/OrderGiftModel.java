package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class OrderGiftModel extends GiftModel{

	private int buyNum;

	public void parse(JSONObject json) throws JSONException {
		super.parse(json);
		setBuyNum(json.optInt("buy_num"));
	}

	public int getBuyNum() {
		return buyNum;
	}

	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}
}
