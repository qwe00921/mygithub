package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class PromotePriceModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 3711106398685507480L;

	public static final int PROMOTEPRICE_MORNING = 4;
	public static final int PROMOTEPRICE_STATE_SHOW = 0;

	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getStart_time() {
		return start_time;
	}

	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}

	public long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(long end_time) {
		this.end_time = end_time;
	}

	public int getNotice_time() {
		return notice_time;
	}

	public void setNotice_time(int notice_time) {
		this.notice_time = notice_time;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getPrice_type() {
		return price_type;
	}

	public void setPrice_type(int price_type) {
		this.price_type = price_type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	private int state = 2;//（正常显示0，划线显示1，隐藏2）
	private String name;
	private long start_time;
	private long end_time;
	private int notice_time;
	private double price;
	private int price_type;

	public void parse(JSONObject json) throws JSONException {
		setState(json.optInt("state", 2));
		setName(json.optString("name", ""));
		setPrice(json.optDouble("price", 0));
		setNotice_time(json.optInt("notice_time", 1));
		setPrice_type(json.optInt("price_type", 1));
		setStart_time(json.optLong("start_time", 1));
		setEnd_time(json.optLong("end_time", 1));
		
	}

}
