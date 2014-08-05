package com.icson.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

@SuppressWarnings("serial")
public class ProductCouponGiftModel extends BaseModel implements Serializable{
	private long mProductId;
	private int mCouponId;
	private ArrayList<CouponGiftModel> mCouponModels;
	
	public void setProductId(long productId) {
		this.mProductId = productId;
	}
	
	public long getProductId() {
		return this.mProductId;
	}
	
	public void setCouponId(int couponId) {
		this.mCouponId = couponId;
	}
	
	public int getCouponId() {
		return this.mCouponId;
	}
	
	public void setCouponModels(ArrayList<CouponGiftModel> models) {
		this.mCouponModels = models;
	}
	
	public ArrayList<CouponGiftModel> getCouponModels(){
		return this.mCouponModels;
	}
	
	
	public void parse(JSONObject json) throws JSONException {
		setCouponId(json.optInt("id", 0));
		
		if(!ToolUtil.isEmptyList(json, "coupon_list")) {
			final JSONArray arrs = json.getJSONArray("coupon_list");
			ArrayList<CouponGiftModel> models = new ArrayList<CouponGiftModel>();
			
			final int nLength = arrs.length();
			for(int nId = 0; nId < nLength; nId ++ ) {
				CouponGiftModel model = new CouponGiftModel();
				model.parse(arrs.getJSONObject(nId));
				models.add(model);
			}
			
			setCouponModels(models);
		}
	}
	
	
	public class CouponGiftModel extends BaseModel implements Serializable{
		private int mBatchId;
		private String mCouponName;
		private long mCouponAmt;
		private int mNum;
		
		public void setBatchId(int pBatchId) {
			this.mBatchId = pBatchId;
		}
		
		public int getBatchId() {
			return this.mBatchId;
		}
		
		public void setCouponName(String strName) {
			this.mCouponName = strName;
		}
		
		public String getCouponName() {
			return this.mCouponName;
		}
		
		public void setCouponAmt(long pCouponAmt) {
			this.mCouponAmt = pCouponAmt;
		}
		
		public long getCouponAmt() {
			return this.mCouponAmt;
		}
		
		public void setCouponNum(int pNum) {
			this.mNum = pNum;
		}
		
		public int getCouponNum(){
			return this.mNum;
		}
		
		
		public void parse(JSONObject json) throws JSONException {
			setBatchId(json.optInt("batch", 0));
			setCouponName(json.optString("coupon_name", ""));
			setCouponAmt(json.optLong("coupon_amt", 0));
			setCouponNum(json.optInt("num", 1));
		}
	}

}
