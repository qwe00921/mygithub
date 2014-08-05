package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductOptionColorModel extends BaseModel{
	
	private boolean isSelected;
	
	private long productId;
	
	private String color;
	
	private String productCharId;

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getProductCharId() {
		return productCharId;
	}

	public void setProductCharId(String productCharId) {
		this.productCharId = productCharId;
	}
	
	public void parse(JSONObject jsonObject) throws JSONException{
		setSelected( jsonObject.has("selected") ?  jsonObject.getInt("selected") == 1 : false);
		setProductId( jsonObject.has("product_id") ?  jsonObject.getLong("product_id") : 0 );
		setColor( jsonObject.has("color") ?  jsonObject.getString("color") : "");
		setProductCharId( jsonObject.has("product_char_id") ?  jsonObject.getString("product_char_id") : "");
	}
}
