package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class DiscountModel extends BaseModel implements Serializable {

	public static final int DISCOUNT_ES = 4;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiscount_url() {
		return discount_url;
	}

	public void setDiscount_url(String discount_url) {
		this.discount_url = discount_url;
	}

	public int getDiscount_type() {
		return discount_type;
	}

	public void setDiscount_type(int discount_type) {
		this.discount_type = discount_type;
	}

	private static final long serialVersionUID = 1844367670932032276L;

	private String desc;
	private String name;
	private String discount_url;
	private int discount_type;

	public void parse(JSONObject json) throws JSONException {
		setDiscount_type(json.optInt("discount_type", 0));
		setDiscount_url(json.optString("url", ""));
		setDesc(json.optString("desc", ""));
		setName(json.optString("name", ""));
	}

}
