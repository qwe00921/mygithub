package com.icson.lib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.icson.lib.WanggouProHelper;
import com.icson.shoppingcart.ProductCouponGiftModel;
import com.icson.util.ToolUtil;

public class ShoppingCartProductModel extends ProductModel implements Serializable {

	/**
	 * 
	 */
	
	public static final int PRODUCT_BENEFIT_LESS_PRICE_BUY = 2; //商品级别，加价购商品优惠类型
	public static final int PRODUCT_BENEFIT_FREE_GFIT = 3;   //商品级别，满赠商品优惠类型
	
	private static final long serialVersionUID = 1L;

	private ArrayList<ShoppingCartGiftModel> mGifts = new ArrayList<ShoppingCartGiftModel>();
	private ProductCouponGiftModel mCouponGift;
	private int buyCount;
	private long energy_save_discount;
	private int mPromoType;
	private int mRuleId;
	public String OTag;
	public Double item_total_cut;
	public long  match_num;
	public Double match_cut;
	public long  sale_mode;
	public long  chid;
	public long  main_product_id;
	public String c3_ids;
	public Double market_price;
	
	public int getPromoType() {
		return this.mPromoType;
	}

	public void setPromoType(int promoType) {
		this.mPromoType = promoType;
	}
	
	public int getRuleId() {
		return this.mRuleId;
	}

	public void setRuleId(int ruleId) {
		this.mRuleId = ruleId;
	}
	
	private String mWangGouUrl;
	private Boolean is_WangGou;

	public void setIsWangGou(Boolean wg_flag)
	{
		this.is_WangGou = wg_flag;
	}
	public Boolean IsWangGou() {
		return this.is_WangGou;
	}
	
	public void setWangGouURL(String wgurl)
	{
		this.mWangGouUrl = wgurl;
	}
	public String getWangGouURL() {
		return this.mWangGouUrl;
	}
	
	public int getBuyCount() {
		return buyCount;
	}
	
	public long getEnergySaveDiscount() {
		return energy_save_discount;
	}
	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	public ArrayList<ShoppingCartGiftModel> getShoppingCartGiftModels() {
		return mGifts;
	}
	
	public void setCouponGiftModel(ProductCouponGiftModel model) {
		this.mCouponGift = model;
	}
	
	public ProductCouponGiftModel getCouponGiftModel() {
		return this.mCouponGift;
	}
	
	@Override
	public void parse(JSONObject json) throws JSONException {
		super.parse(json);

		OTag = json.optString("OTag");
		item_total_cut = json.optDouble("item_total_cut");
		match_num = json.optLong("match_num");
		match_cut = json.optDouble("match_cut");
		chid = json.optLong("chid");
		sale_mode = json.optLong("sale_mode");
		main_product_id = json.optLong("main_product_id");
		c3_ids = json.optString("c3_ids");
		market_price = json.optDouble("market_price");
		this.setShowPrice(json.getDouble("price"));
		this.setBuyCount(json.getInt("buy_count"));
		setPromoType(json.optInt("promotion_type", 0));
		this.setRuleId(json.optInt("rule_id", 0));
		//多价格处理
		/*if(json.optDouble("discount_price")>0){
			this.setPrice_id(json.optInt("price_id"));
			this.setDiscount_price(json.optDouble("discount_price"));
			this.setDiscount_p_name(json.optString("discount_p_name"));
			this.setShowPrice(json.getDouble("discount_price"));
		}*/
		energy_save_discount = json.optLong("energy_save_discount",0);
		
		//网购商品处理
		setIsWangGou(0 == json.optInt("is_wanggou") ? false : true);
		String wanggouurl = json.optString("mapLogUrl", "");
		if(!TextUtils.isEmpty(wanggouurl))
		{
			setWangGouURL(json.optString("mapLogUrl"));
		}
		
		if (!ToolUtil.isEmptyList(json, "gift")) {
			JSONObject giftsJSON = json.getJSONObject("gift");

			@SuppressWarnings("unchecked")
			Iterator<String> keys = giftsJSON.keys();

			while (keys.hasNext()) {
				String key = keys.next();

				JSONObject item = giftsJSON.getJSONObject(key);

				ShoppingCartGiftModel model = new ShoppingCartGiftModel();

				model.parse(item);
				mGifts.add(model);
			}
		}
	}

	@Override
	public int getGiftCount() {
		if (mGifts == null) {
			return 0;
		} 

		int num = 0;

		for (ShoppingCartGiftModel model : mGifts) {
			num += model.getNum();
		}

		return num;
	}
	
	public String getSCWanggouUrl(int size) {
		return WanggouProHelper.getAdapterPicUrl(getWangGouURL(), size, 0);
	}
	
}
