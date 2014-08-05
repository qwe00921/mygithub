package com.icson.virtualpay;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.ToolUtil;

public class VirtualPayModel {
	private List<PriceInfo> mPriceInfoList;
	private String mProvince;
	private String mOperator;

	public List<PriceInfo> getPriceInfoList() {
		return mPriceInfoList;
	}

	public void setPriceInfoList(List<PriceInfo> priceInfoList) {
		this.mPriceInfoList = priceInfoList;
	}

	public String getProvince() {
		return mProvince;
	}

	public void setProvince(String province) {
		this.mProvince = province;
	}

	public String getOperator() {
		return mOperator;
	}

	public void setOperator(String operator) {
		this.mOperator = operator;
	}
	
	public void parse(JSONObject json) throws JSONException {
		if(json == null) {
			return;
		}
		
		setProvince(json.optString("province", ""));
		setOperator(json.optString("isp", ""));
		
		if (!ToolUtil.isEmptyList(json, "priceInfo")) {
			JSONArray priceInfoArray = json.optJSONArray("priceInfo");
			
			List<PriceInfo> infoList = new ArrayList<PriceInfo>();
			if (priceInfoArray != null && priceInfoArray.length() > 0) {

				for(int i = 0; i < priceInfoArray.length(); i++) {
					JSONObject object = priceInfoArray.getJSONObject(i);
					PriceInfo info = new PriceInfo();
					info.parse(object);
					infoList.add(info);
				}
			}
			setPriceInfoList(infoList);
		}
	}

}
