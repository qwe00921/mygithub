package com.icson.event;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class EventProductModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = -273008317053271958L;

	// 品名
	private String name;
	// 商品ID
	private long productId;
	// 商品编号
	private String productCharId;
	// 促销语
	private String promotionWord;
	// 市场价
	private double marketPrice;
	// 易迅价
	private double showPrice;
	//场景id
	private int channelId;

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

	public double getMarketPrice() {
		return marketPrice;
	}

	public double getShowPrice() {
		return showPrice;
	}

	public int getChannelId() {
		return channelId;
	}

	public void parse(JSONObject json) throws JSONException {
		productId = json.optLong("product_id", 0);
		name = json.optString("name", "");
		productCharId = json.optString("product_char_id", "");
		marketPrice = json.optDouble("market_price", 0);
		showPrice = json.optDouble("show_price", 0);
		promotionWord = json.optString("promotion_word", "");
		channelId = json.optInt("channelId", 0);
	}
}
