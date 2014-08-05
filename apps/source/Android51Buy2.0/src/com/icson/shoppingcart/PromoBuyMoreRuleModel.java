package com.icson.shoppingcart;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class PromoBuyMoreRuleModel extends PromoRuleModel{
	private long buyMore;
	
	public void setBuyMore(long more){
		this.buyMore = more;
	}
	
	public long getBuyMore(){
		return buyMore;
	}
	
	public void parse(JSONObject json) throws JSONException {
		super.parse(json);
		setBuyMore(json.optLong("buy_more", 0));
	}

}
