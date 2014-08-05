package com.icson.lib;

import java.util.ArrayList;

import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.ProductModel;

public class ICollect {

	private static String mKey = CacheKeyFactory.CACHE_VIEW_COLLECT_ITEMS;

	public static void set(ProductModel model) {

		IPageCache cache = new IPageCache();
		@SuppressWarnings("unchecked")
		ArrayList<ProductModel> models = cache.getObject(mKey, ArrayList.class);

		if (models == null) {
			models = new ArrayList<ProductModel>();
		}

		for (ProductModel mProductModel : models) {
			if (mProductModel.getProductId() == model.getProductId()) {
				models.remove(mProductModel);
				break;
			}
		}

		models.add(model);

		cache.setObject(mKey, models, 0);
	}

	@SuppressWarnings("unchecked")
	public static void remove(long productId) {
		IPageCache cache = new IPageCache();
		ArrayList<ProductModel> models = cache.getObject(mKey, ArrayList.class);

		if (models == null) {
			models = new ArrayList<ProductModel>();
		}

		for (ProductModel mProductModel : models) {
			if (mProductModel.getProductId() == productId) {
				models.remove(mProductModel);
				break;
			}
		}
		cache.setObject(mKey, models, 0);
	}

	public static ProductModel get(long productId) {
		ArrayList<ProductModel> models = getList();

		if (models != null) {
			for (ProductModel mProductModel : models) {
				if (mProductModel.getProductId() == productId) {
					return mProductModel;
				}
			}
		}

		return null;
	}

	public static void clear() {
		IPageCache cache = new IPageCache();
		cache.remove(mKey);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ProductModel> getList() {
		IPageCache cache = new IPageCache();
		return cache.getObject(mKey, ArrayList.class);
	}
	
/*	private String toString(ArrayList<ItemProductModel> models){
		if (models == null) 	return "";
		
		JSONArray arr = new JSONArray();
		
		try{
			for (ItemProductModel mProductModel : models) {
				 JSONObject json = new JSONObject();
				 json.put("product_id", mProductModel.getProductId());
				 json.put("product_char_id", mProductModel.getProductCharId());
				 json.put("product_char_id", mProductModel.getPromotionWord());
				 json.put("name", mProductModel.getName());
				 json.put("show_price", mProductModel.getShowPrice());
				 json.put("market_price", mProductModel.getMarketPrice());
				 json.put("gift_count", mProductModel.getGiftCount());
				 
				 JSONObject jReview = new JSONObject();
				 
				 ReviewCountModel review = mProductModel.getReviewCountModel(); 
				 jReview.put("satisfied_num",  review == null ? 0 : review.getSatisfied());
				 jReview.put("general_num",  review == null ? 0 : review.getGeneral());
				 jReview.put("unsatisfied_num",  review == null ? 0 : review.getUnsatisfied());
				 jReview.put("star_length",  review == null ? 0 : review.getStarLength());
				 
				 json.put("review", jReview);
			 	
			}	
		}
		catch(Exception ex){
			
		}
	}*/
}
