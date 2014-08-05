package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductOptionSizeModel extends BaseModel{
	
	private boolean isSelected;
	
	private long productId;
	
	private String size;
	
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

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
	
	public void parse(JSONObject jsonObject) throws JSONException{
		setSelected( jsonObject.has("selected") ?  jsonObject.getInt("selected") == 1 : false);
		setProductId( jsonObject.has("product_id") ?  jsonObject.getLong("product_id") : 0 );
		setSize( jsonObject.has("size") ?  jsonObject.getString("size") : "");
	}
}
