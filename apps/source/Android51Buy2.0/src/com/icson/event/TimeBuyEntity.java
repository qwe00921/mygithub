package com.icson.event;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class TimeBuyEntity extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5017506888088841500L;

	public void parse(JSONObject json) throws JSONException {

		name = json.optString("name");
		productId = json.optLong("product_id");
		productCharId = json.optString("product_char_id");
		promotionWord = json.optString("promotion_word");
		show_label = json.optString("show_label");
		snap_label = json.optString("snap_label");
		channel_id = json.optInt("channel_id");
		show_price = json.optDouble("show_price");
		snap_price = json.optDouble("snap_price");

		reviews = json.optInt("review");
		nSaleType = json.optInt("sale_type", 0);
	}

	public String getShowLabel() {
		return show_label;
	}
	public String getSnapLabel() {
		return snap_label;
	}

	public int getReviews() {
		return reviews;
	}

	public int getChannelId() {
		return channel_id;
	}

	public String getName() {
		return name;
	}

	public String getNameNoHTML() {

		if (name == null)
			return "";

		return name.replaceAll("<[^>]+>([^<]*)</[^>]+>", "$1");
	}

	public long getProductId() {
		return productId;
	}

	public String getProductCharId() {
		return productCharId;
	}

	public String getPromotionWord() {
		return promotionWord;
	}

	public double getShowPrice() {
		return show_price;
	}

	public double getSnapPrice() {
		return snap_price;
	}
	
	public int getSaleType(){
		return this.nSaleType;
	}

	// 品名
	private String name;
	// 商品ID
	private long productId;
	// 商品编号
	private String productCharId;
	// 促销语
	private String promotionWord;
	private int channel_id;
	private String show_label;
	private double show_price;
	private String snap_label;
	private double snap_price;
	// Reviewer.
	private int reviews;
	//nSaleType, 1:正在销售, 2:售謦, 3:尚未开始 
	private int nSaleType;
}
