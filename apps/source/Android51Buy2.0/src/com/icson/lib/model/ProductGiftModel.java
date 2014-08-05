package com.icson.lib.model;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class ProductGiftModel extends BaseModel implements Serializable{

	private long productId;
	
	private String name;
	
	private String productCharId;
	
	private int picNum;
	
	private double price;
	
	private int type;
	
	private int num;

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductCharId() {
		return productCharId;
	}

	public void setProductCharId(String productCharId) {
		this.productCharId = productCharId;
	}

	public int getPicNum() {
		return picNum;
	}

	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}

	public double getPrice() {
		return price;
	}

	public String getShowPriceStr() {
		return String.valueOf( new java.text.DecimalFormat("#0.00").format( price / 100 ) );
	}
	
	public void setPrice(double price) {
		this.price = price;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	public void parse(JSONObject jsonObject) throws JSONException{
		setProductId( jsonObject.has("product_id") ? jsonObject.getLong("product_id") : 0 );
		setName( jsonObject.has("name") ? jsonObject.getString("name") : "");
		setProductCharId( jsonObject.has("product_char_id") ? jsonObject.getString("product_char_id") : "");
		setPicNum( jsonObject.has("pic_num") ? jsonObject.getInt("pic_num") : 0 );
		setPrice( jsonObject.has("price") ? jsonObject.getDouble("price") : 0);
		setType( jsonObject.has("type") ? jsonObject.getInt("type") : 0);
		setNum( jsonObject.has("num") ? jsonObject.getInt("num") : 0);
	}
}
