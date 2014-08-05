package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class ShoppingCartGiftModel extends GiftModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int num;
	
	public int getNum(){
		return num;
	}
	
	public void setNum(int num){
		this.num = num;
	}
	
	@Override
	public void parse(JSONObject json) throws JSONException{
		super.parse(json);
		setNum( json.optInt("num") );
	}
}
