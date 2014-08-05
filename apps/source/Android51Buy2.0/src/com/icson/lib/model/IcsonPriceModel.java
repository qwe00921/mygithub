package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class IcsonPriceModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 2476711658890543921L;
	public static final int PROMOTEPRICE_MORNING = 4;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public double getPrice() {
		return price;
	}

	public void setVIP_Price(double price) {
		this.vip_price = price;
	}

	public double getVIP_Price() {
		return vip_price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getVIP_type() {
		return vip_type;
	}

	public void setVIP_type(int price_type) {
		this.vip_type = price_type;
	}

	public boolean Is_login() {
		return is_login;
	}

	public void setLogin(boolean is_login) {
		this.is_login = is_login;
	}

	private double price;
	private int state;// （正常显示0，划线显示1，隐藏2）
	private double vip_price;
	private int vip_type;// （1会员，2绿钻，3，绿钻及会员）
	private boolean is_login;

	public void parse(JSONObject json) throws JSONException {
		setState(json.optInt("state", 0));
		setPrice(json.optDouble("price", 0));
		setVIP_Price(json.optDouble("vip_price", 0));
		setVIP_type(json.optInt("vip_type", 0));
		setLogin(json.optBoolean("is_login", false));

	}

}
