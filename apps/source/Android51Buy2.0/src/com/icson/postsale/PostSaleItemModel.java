package com.icson.postsale;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.Log;

public class PostSaleItemModel extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long productID;
	public String productCharId;
	public String productName;
	public int productNum;

	public long getProductID() {
		return productID;
	}

	public void setProductID(long productID) {
		this.productID = productID;
	}

	public String getProductCharId() {
		return productCharId;
	}

	public void setProductCharId(String productCharId) {
		this.productCharId = productCharId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getProductNum() {
		return productNum;
	}

	public void setProductNum(int productNum) {
		this.productNum = productNum;
	}
	
	private static final String TAG = PostSaleItemModel.class.getSimpleName();
	public void parse(JSONObject json) throws JSONException {
		if (json == null) {
			if (json == null) {
				Log.w(TAG, "[parse] json is null!");
				return;
			}
		}

		setProductCharId(json.optString(Constants.KEY_ITEMS_PRODUCT_CHARID, ""));
		setProductName(json.optString(Constants.KEY_ITEMS_PRODUCT_NAME, ""));
		setProductID(json.optLong(Constants.KEY_ITEMS_PRODUCT_ID, 0));
		setProductNum(json.optInt(Constants.KEY_ITEMS_PRODUCT_NUM));

	}

}
