package com.icson.hotlist;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

public class HotlistModel extends BaseModel {

	private ArrayList<HotCate> mHotList;
	


	public ArrayList<HotCate> getHotList() {
		return mHotList;
	}

	public void parse(JSONObject json) throws JSONException {
		mHotList = new ArrayList<HotCate>();
		if (null!= json)
		{
			JSONArray arrs = json.getJSONArray("data");
			
			for (int i = 0, len = arrs.length(); i < len; i++) {
				HotCate model = new HotCate();
				model.parse(arrs.getJSONObject(i));
				mHotList.add(model);
			}
		}

	}

	public static class HotCate extends BaseModel
	{
		private ArrayList<HotProductModel> products;
		private int    cateId;
		private String cateName;
		private String cateTitle;
		
		public int getCateId()
		{
			return cateId;
		}
		public String getCateName()
		{
			return cateName;
		}
		public String getCateTitle()
		{
			return cateTitle;
		}
		
		public ArrayList<HotProductModel> getHotCateProducts()
		{
			return products;
		}
		
		public void parse(JSONObject json) throws JSONException {
			cateId = json.optInt("cateID");
			cateName = json.optString("cateName","");
			cateTitle = json.optString("cateTitle","");
			if(null == products)
				products = new ArrayList<HotProductModel>();
			
			if (!ToolUtil.isEmptyList(json, "products")) {
				JSONObject proObj = json.getJSONObject("products");	
				JSONArray arrs = proObj.names();
				for (int i = 0, len = arrs.length(); i < len; i++) {
					HotProductModel model = new HotProductModel();
					model.parse(proObj.optJSONObject(arrs.getString(i)));
					products.add(model);
				}
			}
		}
		
	}
	public static class HotProductModel extends BaseModel implements Serializable {
		private static final long serialVersionUID = 1L;
		// 商品ID
		private long productId;
		// 商品编号
		private String productCharId;
		// name
		private String promoName;
		// 易迅价
		private double showPrice;
		// 市场价
		private double marketPrice;
		// 购买数量
		private int buyNum;
		
		public long getProductId() {
			return productId;
		}
		public String getProductCharId() {
			return productCharId;
		}
		public String getPromoName() {
			return promoName;
		}
		public double getShowPrice() {
			return showPrice;
		}
		public double getMarketPrice() {
			return marketPrice;
		}
		public int getBuyNum() {
			return buyNum;
		}
		
		public void parse(JSONObject json) throws JSONException {
			productId = (json.optLong("product_id", 0));
			productCharId = json.optString("product_char_id", "");
			promoName = (json.getString("name"));
			showPrice = json.optDouble("show_price", 0);
			marketPrice = json.optDouble("market_price", 0);
			buyNum = json.optInt("buy_num");
		}

	}
}
