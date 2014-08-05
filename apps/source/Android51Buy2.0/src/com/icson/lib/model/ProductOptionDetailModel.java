package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductOptionDetailModel {
	/*
	 * name: "32G单卡", product_id: 1288666, selected: "0"
	 */

	private String mName;
	private long mProductId;
	private int mSelectStatus;

	public static final int STATUS_SELECTED = 1;
	public static final int STATUS_DISELECTED = 0;
	public static final int STATUS_SELECT_NONE = -1;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public long getProductId() {
		return mProductId;
	}

	public void setProductId(long productId) {
		this.mProductId = productId;
	}

	public int getSelectStatus() {
		return mSelectStatus;
	}

	public void setSelectStatus(int selectStatus) {
		this.mSelectStatus = selectStatus;
	}
	
	public void parse(JSONObject object) throws JSONException {
		if(object == null) {
			return;
		}
		
		setName(object.optString("name", ""));
		setProductId(object.optLong("product_id", 0));
		setSelectStatus(object.optInt("selected", 0));
	}

}
