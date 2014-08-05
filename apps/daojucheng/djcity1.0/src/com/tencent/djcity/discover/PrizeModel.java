package com.tencent.djcity.discover;

import org.json.JSONException;
import org.json.JSONObject;

public class PrizeModel {

	private String name;
	private int quantity;
	private String time;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void parse(JSONObject object) {
		if(object == null) {
			return;
		}
		
		setName(object.optString("sGoodsName"));
		setQuantity(object.optInt("iQuantity"));
		setTime(object.optString("dtScratchTime "));
	}
}
