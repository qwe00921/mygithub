package com.icson.order.shippingtype;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

public class ShippingTypeOrderModel extends BaseModel{
	private int id;

	private double shippingPrice;
	
	private ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModels;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getShippingPrice() {
		return shippingPrice;
	}

	public void setShippingPrice(double shippingPrice) {
		this.shippingPrice = shippingPrice;
	}

	public void parse(JSONObject json) {
		setShippingPrice(json.optDouble("shippingPrice"));
	}
	
	public void parse(int id, JSONObject json) throws Exception{
		setId(id);
		setShippingPrice( json.getDouble("shippingPrice") );
		
		if( !ToolUtil.isEmptyList(json, "timeAvaiable") ){
			mShippingTypeTimeModels = new ArrayList<ShippingTypeTimeModel>();
			
			JSONArray arrs = json.getJSONArray("timeAvaiable");
			for(int i = 0, len = arrs.length() ; i < len; i++){
				JSONObject v = arrs.getJSONObject(i);
				ShippingTypeTimeModel model = new ShippingTypeTimeModel(); 
				model.parse(v);
				if(model.getState()== ShippingTypeTimeModel.STATUS_OK)
					mShippingTypeTimeModels.add(model);
			}
			Collections.sort(mShippingTypeTimeModels, new ShippingTypeTimeModel());
		}
	}
	
	public ArrayList<ShippingTypeTimeModel> getShippingTypeTimeModels() {
		return mShippingTypeTimeModels;
	}

	public void setShippingTypeTimeModels(ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModels) {
		this.mShippingTypeTimeModels = mShippingTypeTimeModels;
	}
}
