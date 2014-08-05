package com.icson.order.shippingtype;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

public class SubShippingTypeModel extends BaseModel{
	private String subOrderId;

	private double shippingPrice;

	private ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModels;
	
	private ArrayList<ShippingTypeTimeModel> mPreShippingTypeTimeModels;
	
	public String getSubOrderId() {
		return subOrderId;
	}

	public void setSubOrderId(String subOrderId) {
		this.subOrderId = subOrderId;
	}

	public double getShippingPrice() {
		return shippingPrice;
	}

	public void setShippingPrice(double shippingPrice) {
		this.shippingPrice = shippingPrice;
	}

	public void parse(String id, JSONObject json) throws Exception {
		setSubOrderId(id);
		setShippingPrice(json.getDouble("shippingPrice"));

		if (!ToolUtil.isEmptyList(json, "timeAvaiable")) {
			mShippingTypeTimeModels = new ArrayList<ShippingTypeTimeModel>();

			JSONArray arrs = json.getJSONArray("timeAvaiable");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				JSONObject v = arrs.getJSONObject(i);
				ShippingTypeTimeModel model = new ShippingTypeTimeModel();
				model.parse(v);
				if(model.getState()== ShippingTypeTimeModel.STATUS_OK)
					mShippingTypeTimeModels.add(model);
			}
			
			Collections.sort(mShippingTypeTimeModels, new ShippingTypeTimeModel());
		}
		
		JSONObject timeAvaiablePre = null;
		try {
			timeAvaiablePre = json.getJSONObject("timeAvaiablePre");
		} catch(JSONException e) {
			timeAvaiablePre = null;
		}
		if(timeAvaiablePre != null) {
			
			if (!ToolUtil.isEmptyList(timeAvaiablePre, "")) {
				mPreShippingTypeTimeModels = new ArrayList<ShippingTypeTimeModel>();
				JSONArray arrs = timeAvaiablePre.getJSONArray("");
				for (int i = 0, len = arrs.length(); i < len; i++) {
					JSONObject v = arrs.getJSONObject(i);
					ShippingTypeTimeModel model = new ShippingTypeTimeModel();
					model.parse(v);
					if(model.getState()== ShippingTypeTimeModel.STATUS_OK)
						mPreShippingTypeTimeModels.add(model);
				}
				
				Collections.sort(mPreShippingTypeTimeModels, new ShippingTypeTimeModel());
			}
		}
		
		if ( json.optBoolean("isArrivedLimitTime", false)){

		}
		
	}

	public ArrayList<ShippingTypeTimeModel> getmShippingTypeTimeModels() {
		return mShippingTypeTimeModels;
	}

	public void setmShippingTypeTimeModels(ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModels) {
		this.mShippingTypeTimeModels = mShippingTypeTimeModels;
	}

	public ArrayList<ShippingTypeTimeModel> getPreShippingTypeTimeModels() {
		return mPreShippingTypeTimeModels;
	}

	public void setPreShippingTypeTimeModels(
			ArrayList<ShippingTypeTimeModel> preShippingTypeTimeModels) {
		mPreShippingTypeTimeModels = preShippingTypeTimeModels;
	}
	
}
