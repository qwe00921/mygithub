package com.icson.qiang;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ProductModel;
import com.icson.util.ToolUtil;

public class QiangModel extends BaseModel {

	private long now;
	private long end;

	public long getNow() {
		return now;
	}

	public void setNow(long now) {
		this.now = now;
	}
	
	private ArrayList<QiangProductModel> mQiangProductModels;


	public ArrayList<QiangProductModel> getQiangProductModles() {
		return mQiangProductModels;
	}

	private void setEnd(long end) {
		this.end = end;
	}

	public long getEnd() {
		return end;
	}

	public void parse(JSONObject json) throws JSONException {
		setEnd(json.getLong("end"));
		setNow(json.getLong("now"));
		mQiangProductModels = new ArrayList<QiangProductModel>();
		if (!ToolUtil.isEmptyList(json, "products")) {
			JSONArray arrs = json.getJSONArray("products");

			for (int i = 0, len = arrs.length(); i < len; i++) {
				QiangProductModel model = new QiangProductModel();
				model.parse(arrs.getJSONObject(i));
				mQiangProductModels.add(model);
			}
		}

	}

	public static class QiangProductModel extends BaseModel implements Serializable {
		private static final long serialVersionUID = 1L;
		// 品名（抢购用）
		private String promoName;
		// 商品ID
		private long productId;
		// 商品编号
		private String productCharId;
		// 易迅价
		private double showPrice;
		// 库存%
		private int progress;
		// 销售状态(1 : 在售, 2: 已售完：2, 3: 暂不销售 )
		private int   saleType;
		//广告词
		private String promotionWord;
		
		public long getProductId() {
			return productId;
		}
		public String getPromoName() {
			return promoName;
		}
		public String getProductCharId() {
			return productCharId;
		}
		public double getShowPrice() {
			return showPrice;
		}
		
		public int getProgress() {
			return progress;
		}

		public int getSaleType() {
			return saleType;
		}

		public String getPromotionWord() {
			return promotionWord;
		}
		
		public void parse(JSONObject json) throws JSONException {
			progress = (json.getInt("progress"));
			promoName = (json.getString("promo_name"));
			productId = (json.optLong("product_id", 0));
			productCharId = json.optString("product_char_id", "");
			showPrice = json.optDouble("show_price", 0);
			saleType = json.optInt("sale_type", ProductModel.SALE_EMPTY);
			promotionWord = json.optString("promotion_word");
		}
		

	}
}
