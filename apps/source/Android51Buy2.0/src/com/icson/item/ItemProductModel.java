package com.icson.item;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.home.ProvinceModel;
import com.icson.lib.WanggouProHelper;
import com.icson.lib.model.DiscountModel;
import com.icson.lib.model.IcsonPriceModel;
import com.icson.lib.model.ProductGiftModel;
import com.icson.lib.model.ProductModel;
import com.icson.lib.model.ProductOptionColorModel;
import com.icson.lib.model.ProductOptionModel;
import com.icson.lib.model.ProductOptionSizeModel;
import com.icson.lib.model.PromotePriceModel;
import com.icson.lib.model.ReviewCountModel;
import com.icson.shoppingcart.ProductCouponGiftModel;
import com.icson.util.ToolUtil;

public class ItemProductModel extends ProductModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2634952277141591557L;

	private transient ReviewCountModel mReviewCountModel;

	private transient ArrayList<ProductOptionColorModel> mProductOptionColorModelList = new ArrayList<ProductOptionColorModel>();

	private transient ArrayList<ProductOptionSizeModel> mProductOptionSizeModelList = new ArrayList<ProductOptionSizeModel>();
	
	private transient ArrayList<ProductOptionModel> mProductOptionModelList = new ArrayList<ProductOptionModel>();

	private transient ArrayList<ProductGiftModel> mProductAccessoryModel = new ArrayList<ProductGiftModel>();
	private transient ArrayList<ProductGiftModel> mProductGiftModel = new ArrayList<ProductGiftModel>();
	// 易迅价格icson_price
	private transient IcsonPriceModel mIcsonPriceModel = new IcsonPriceModel();

	// 促销价格promote_price（不同场景价）
	private transient PromotePriceModel mPromotePriceModel = new PromotePriceModel();
	// 优惠信息: （1立减，2折扣，3梯度，4节能，5满立减，6满立送，7满包邮）
	private transient ArrayList<DiscountModel> mPromoRuleModels = new ArrayList<DiscountModel>();

	private transient ArrayList<ProductModel> buyProductModels = new ArrayList<ProductModel>();
	private transient ArrayList<ProductModel> browseProductModels = new ArrayList<ProductModel>();
	private transient ArrayList<ProductModel> mRecommendProductModels = new ArrayList<ProductModel>();
	
	private ProductCouponGiftModel mCouponGiftModel = new ProductCouponGiftModel();
	
	private ProvinceModel mDistrictModel;

	private int           mProSaleModelType;
	public  static final int   PRO_SALE_WANGGOU = 5;
	
	public ReviewCountModel getReviewCountModel() {
		return mReviewCountModel;
	}

	public ArrayList<ProductModel> getBuyProductModels() {
		return buyProductModels;
	}

	public ArrayList<ProductModel> getBrowseProductModels() {
		return browseProductModels;
	}
	
	public ArrayList<ProductModel> getRecommendProductModels() {
		return mRecommendProductModels;
	}

	public void setReviewCountModel(ReviewCountModel mReviewCountModel) {
		this.mReviewCountModel = mReviewCountModel;
	}

	public ArrayList<ProductOptionColorModel> getProductOptionColorModelList() {
		return mProductOptionColorModelList;
	}
	
	public ArrayList<ProductOptionModel> getProductOptionModelList() {
		return mProductOptionModelList;
	}

	public void setProductOptionModelList(
			ArrayList<ProductOptionModel> productOptionModelList) {
		mProductOptionModelList = productOptionModelList;
	}

	public IcsonPriceModel getIcsonPriceModel() {
		return mIcsonPriceModel;
	}
	
	public double getIcsonPrice() {
		return mIcsonPriceModel.getPrice();
	}

	public PromotePriceModel getPromotePriceModel() {
		return mPromotePriceModel;
	}
	
	public double getPromotePrice() {
		return mPromotePriceModel.getPrice();
	}

	// 促销规则
	public ArrayList<DiscountModel> getPromoRuleModelList() {
		return mPromoRuleModels;
	}

	public void setProductOptionColorModelList(
			ArrayList<ProductOptionColorModel> mProductOptionColorModelList) {
		this.mProductOptionColorModelList = mProductOptionColorModelList;
	}

	public ArrayList<ProductOptionSizeModel> getProductOptionSizeModelList() {
		return mProductOptionSizeModelList;
	}

	public void setProductOptionSizeModelList(
			ArrayList<ProductOptionSizeModel> mProductOptionSizeModelList) {
		this.mProductOptionSizeModelList = mProductOptionSizeModelList;
	}

	public ArrayList<ProductGiftModel> getProductGiftModels() {
		return mProductGiftModel;
	}

	public void setProductGiftModel(
			ArrayList<ProductGiftModel> mProductGiftModel) {
		this.mProductGiftModel = mProductGiftModel;
	}

	public ArrayList<ProductGiftModel> getProductAccessoryModels() {
		return mProductAccessoryModel;
	}

	public void setProductAccessoryModel(
			ArrayList<ProductGiftModel> aAccessoryModel) {
		this.mProductAccessoryModel = aAccessoryModel;
	}
	
	
	public ProductCouponGiftModel getCouponGiftModel(){
		return this.mCouponGiftModel;
	}
	
	public ProvinceModel getFullDistrictModel(){
		return this.mDistrictModel;
	}

	public String getItemWanggouUrl(int size, int index) {
		return WanggouProHelper.getAdapterPicUrl(getMainPic(), size, index);
	}
	
	@SuppressWarnings("unchecked")
	public void parse(JSONObject data) throws JSONException {

		// 商品基本信息
		super.parse(data);

		// 评论信息
		if (!ToolUtil.isEmptyList(data, "review")) {
			mReviewCountModel = new ReviewCountModel();
			mReviewCountModel.parse(data.getJSONObject("review"));
		}
		
		// 属性
		mProductOptionModelList.clear();
		if (!ToolUtil.isEmptyList(data, "all_attr_block")) {
			JSONArray products = data.getJSONArray("all_attr_block");
			for (int i = 0; i < products.length(); i++) {
				ProductOptionModel model = new ProductOptionModel();
				model.parse(products.getJSONObject(i));
				mProductOptionModelList.add(model);
			}
		}

		// 颜色
		mProductOptionColorModelList.clear();
		final String COLORKEY = "t_color_block";
		if (!ToolUtil.isEmptyList(data, COLORKEY)) {
			final JSONArray optionColorJson = data.getJSONArray(COLORKEY);

			for (int i = 0, len = optionColorJson.length(); i < len; i++) {
				JSONObject json = optionColorJson.getJSONObject(i);
				ProductOptionColorModel model = new ProductOptionColorModel();
				model.parse(json);
				mProductOptionColorModelList.add(model);
			}
		}

		// 尺寸
		mProductOptionSizeModelList.clear();
		final String SIZEKEY = "t_size_block";
		if (!ToolUtil.isEmptyList(data, SIZEKEY)) {
			final JSONArray optionSizeJson = data.getJSONArray(SIZEKEY);

			for (int i = 0, len = optionSizeJson.length(); i < len; i++) {
				JSONObject json = optionSizeJson.getJSONObject(i);
				ProductOptionSizeModel model = new ProductOptionSizeModel();
				model.parse(json);
				mProductOptionSizeModelList.add(model);
			}
		}
		//促销2.0新的 价格结构
		if (!ToolUtil.isEmptyList(data, "price_info")) {
			JSONObject price_info = data.getJSONObject("price_info");
			// 易迅价
			if (!ToolUtil.isEmptyList(price_info, "icson_price")) {
				mIcsonPriceModel.parse(price_info.getJSONObject("icson_price"));
			}
			// 场景价格
			if (!ToolUtil.isEmptyList(price_info, "promote_price")) {
				mPromotePriceModel.parse(price_info
						.getJSONObject("promote_price"));
			}
			// 优惠详细促销规则
			if (!ToolUtil.isEmptyList(price_info, "discount_info")) {
				JSONArray rules = price_info.getJSONArray("discount_info");

				for (int i = 0, len = rules.length(); i < len; i++) {
					JSONObject json = rules.getJSONObject(i);
					DiscountModel model = new DiscountModel();
					model.parse(json);
					mPromoRuleModels.add(model);
				}

			}
		}
		// 礼物
		final String GIFTKEY = "gifts";
		mProductGiftModel.clear();
		mProductAccessoryModel.clear();
		if (!ToolUtil.isEmptyList(data, GIFTKEY)) {
			final JSONArray giftJSON = data.getJSONArray(GIFTKEY);

			for (int i = 0, len = giftJSON.length(); i < len; i++) {
				JSONObject json = giftJSON.getJSONObject(i);
				ProductGiftModel model = new ProductGiftModel();
				model.parse(json);
				if (model.getType() != 1) {
					mProductGiftModel.add(model);
				}else //组件
					mProductAccessoryModel.add(model);
			}
		}
		
		//单品赠券
		if(!ToolUtil.isEmptyList(data, "coupons")) {
			JSONArray pJSONArray = data.getJSONArray("coupons");
			JSONObject pJSONObject;
			ArrayList<ProductCouponGiftModel> models = new ArrayList<ProductCouponGiftModel>();
			int nLength = pJSONArray.length();
			Iterator<String> iter;
			for(int nId = 0; nId < nLength; nId ++ ) {
				pJSONObject = pJSONArray.getJSONObject(nId);
				iter = pJSONObject.keys();
				while(iter.hasNext()) {
					String key = iter.next();
					ProductCouponGiftModel model = new ProductCouponGiftModel();
					model.setProductId(Long.parseLong(key));
					model.parse(pJSONObject.getJSONObject(key));
					models.add(model);
				}
			}
			
			for(ProductCouponGiftModel giftModel: models) {
				if(giftModel.getProductId() == getProductId()) {
					mCouponGiftModel = giftModel;
				}
			}
			
		}

		// 买了买
		buyProductModels.clear();
		if (!ToolUtil.isEmptyList(data, "buy_products")) {
			JSONArray products = data.getJSONArray("buy_products");
			for (int i = 0; i < products.length(); i++) {
				ProductModel pm = new ProductModel();
				pm.parse(products.getJSONObject(i));
				buyProductModels.add(pm);
			}
		}
		// 看了看
		browseProductModels.clear();
		if (!ToolUtil.isEmptyList(data, "browse_products")) {
			JSONArray products = data.getJSONArray("browse_products");
			for (int i = 0; i < products.length(); i++) {
				ProductModel pm = new ProductModel();
				pm.parse(products.getJSONObject(i));
				browseProductModels.add(pm);
			}
		}
		
		// 无货推荐
		mRecommendProductModels.clear();
		if (!ToolUtil.isEmptyList(data, "recommend_products")) {
			JSONArray products = data.getJSONArray("recommend_products");
			for (int i = 0; i < products.length(); i++) {
				ProductModel pm = new ProductModel();
				pm.parse(products.getJSONObject(i));
				mRecommendProductModels.add(pm);
			}
		}
		
		//默认的三级分类
		if(!ToolUtil.isEmptyList(data, "fullDistrict")){
			JSONObject districtJson = data.getJSONObject("fullDistrict");
			ProvinceModel model = new ProvinceModel();
			
			Iterator<String> iter= districtJson.keys();
			if(iter.hasNext()) {
				String key = iter.next();
				model.parse(districtJson.getJSONObject(key));
			}
			
			this.mDistrictModel = model;
		}
		
		//sale Model  5 == wanggou
		mProSaleModelType = data.optInt("product_sale_model");
	
	}

	/**
	 * 
	 * @return
	 */
	
	public void setSaleModelType(int type)
	{
		 this.mProSaleModelType = type;
	}
	public int getSaleModelType()
	{
		return mProSaleModelType;
	}
	
	public boolean isESProduct() {
		for (DiscountModel rule : mPromoRuleModels) {
			if (rule.getDiscount_type() == DiscountModel.DISCOUNT_ES)
				return true;
		}
		return false;
	}

	public DiscountModel getESPromoRuleModel() {
		for (DiscountModel rule : mPromoRuleModels) {
			if (rule.getDiscount_type() == DiscountModel.DISCOUNT_ES)
				return rule;
		}
		return null;
	}
}
