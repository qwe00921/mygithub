package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.IcsonProImgHelper;

@SuppressWarnings("serial")
public class SearchProductModel extends BaseModel implements Serializable {
	private String mProductName;		// 品名
	private long mProductId;			// 商品ID
	private String mProductCharId;		// 商品编号
	private String mPromotionWord;		// 促销语
	private double marketPrice;			// 市场价
	private double showPrice;			// 易迅价
	private int mDiscussCount;			// 评论数
	private boolean isHasGift;			//赠品数量
	private int mOnlineQuantity;		// 商品库存数量
	private int mReachable;				// 可不可配送   0:可以配送

	public void setOnlineQuantity(int onlineQty) {
		this.mOnlineQuantity = onlineQty;
	}
	
	public int getOnlineQuantity(){
		return this.mOnlineQuantity;
	}
	
	public void setReachable(int reachable) {
		this.mReachable = reachable;
	}
	
	public int getReachable(){
		return this.mReachable;
	}
	
	public void setProductName(String name) {
		this.mProductName = name;
	}
	
	public String getProductName(){
		return this.mProductName;
	}
	
	public void setProductId(long id) {
		this.mProductId = id;
	}
	
	public long getProductId(){
		return this.mProductId;
	}
	
	public void setProductCharId(String productCharId) {
		this.mProductCharId = productCharId;
	}
	
	public String getProductCharId(){
		return this.mProductCharId;
	}
	
	public void setPromotionWord(String promotionWord) {
		this.mPromotionWord = promotionWord;
	}
	
	public String getPromotionWord(){
		return this.mPromotionWord;
	}
	
	public void setMarketPrice(double marketPrice) {
		this.marketPrice = marketPrice;
	}
	
	public double getMarketPrice(){
		return this.marketPrice;
	}
	
	public void setShowPrice(double showPrice) {
		this.showPrice = showPrice;
	}
	
	public double getShowPrice(){
		return this.showPrice;
	}
	
	public void setDiscussCount(int discussCount) {
		this.mDiscussCount = discussCount;
	}
	
	public int getDiscussCount(){
		return this.mDiscussCount;
	}
	
	public void setIsGift(boolean isGift){
		this.isHasGift = isGift;
	}
	
	public boolean getIsGift(){
		return this.isHasGift;
	}
	
	public String getAdapterProductUrl(int dip) {
		return getAdapterProductUrl(dip, 0);
	}

	public String getAdapterProductUrl(int dip, int index) {
		return IcsonProImgHelper.getAdapterPicUrl(mProductCharId, dip, index);
	}
	
	public void parse(JSONObject v) throws JSONException {
		setProductName(v.optString("productTitle", ""));
		setPromotionWord(v.optString("promotionDesc", ""));
		setProductCharId(v.optString("productID", ""));
		setProductId(v.optLong("sysNo", 0));
		setMarketPrice(v.optDouble("marketprice", 0));
		setShowPrice(v.optDouble("price", 0));
		setDiscussCount(v.optInt("evaluationNum", 0));
		setIsGift( v.optInt("gift", 0) != 0);
		setOnlineQuantity(v.optInt("onlineQty", 0));
		setReachable( v.optInt("reachable", 0));
	}
}
