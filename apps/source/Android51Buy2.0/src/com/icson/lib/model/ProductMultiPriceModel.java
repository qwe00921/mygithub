package com.icson.lib.model;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class ProductMultiPriceModel extends BaseModel implements Serializable{

	public int price_id;
	public double price;
	public String price_name;
	public String valid_time_to;
	public int count_type;
	public boolean isSatisfy;
	
	public void parse(JSONObject jsonObject) throws JSONException{
		price_id = jsonObject.optInt("price_id");
		price = jsonObject.optDouble("price");
		price_name = jsonObject.optString("price_name");
		valid_time_to = jsonObject.optString("valid_time_to");
		count_type = jsonObject.optInt("count_type");
		isSatisfy = jsonObject.optBoolean("isSatisfy");
	}
}
