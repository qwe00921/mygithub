package com.icson.qiang;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

public class QiangTomorrowModel extends BaseModel {

	private ArrayList<QiangTomorrowProductModel> mQiangTomorrowModels;

	public ArrayList<QiangTomorrowProductModel> getQiangTomorrowProductModels() {
		return mQiangTomorrowModels;
	}
	
	public void parse(JSONObject json) throws JSONException {
		
		mQiangTomorrowModels = new ArrayList<QiangTomorrowProductModel>();
		if (!ToolUtil.isEmptyList(json, "data")) {
			JSONArray arrs = json.getJSONArray("data");

			for (int i = 0, len = arrs.length(); i < len; i++) {
				QiangTomorrowProductModel model = new QiangTomorrowProductModel();
				model.parse(arrs.getJSONObject(i));
				mQiangTomorrowModels.add(model);
			}
		}

	}

	public static class QiangTomorrowProductModel extends BaseModel implements
			Serializable {
		private static final long serialVersionUID = 1L;
		// 品名（抢购用）
		private String promoName;
		// 商品ID
		private long productId;
		// 商品编号
		private String productCharId;
		// 易迅价
		private String promotionWord;//promotion_word = "惊喜价 请期待"
		
		public long getProductId() {
			return productId;
		}

		public String getPromoName() {
			return promoName;
		}

		public String getProductCharId() {
			return productCharId;
		}

		public String getPromotionWord() {
			return promotionWord;
		}

		public void parse(JSONObject json) throws JSONException {
			promoName = (json.getString("name"));
			productId = (json.optLong("product_id", 0));
			productCharId = json.optString("product_char_id", "");
			promotionWord = json.optString("promotion_word", "");
		}

	}
}
