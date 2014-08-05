package com.icson.shoppingcart;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class PromoApplyRuleModel extends PromoRuleModel{
	private long discount;
	
	private String name;
	
	public void setDiscount(long dis) {
		this.discount = dis;
	}
	
	public long getDiscount() {
		return discount;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void parse(JSONObject json) throws JSONException {
		super.parse(json);
		setDiscount(json.optLong("discount", 0));
		setName(json.optString("name", ""));
	}

}
