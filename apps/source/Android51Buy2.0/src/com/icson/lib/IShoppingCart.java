package com.icson.lib;

import java.util.ArrayList;

import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.ShoppingCartProductModel;

public class IShoppingCart {

	private static String mKey = CacheKeyFactory.CACHE_SHOPPINGCART_ITEMS;

	public static void set(ShoppingCartProductModel mShoppingCartProductModel) {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		String str = mShoppingCartProductModel.getProductId() + "|" + mShoppingCartProductModel.getBuyCount() + "|" + mShoppingCartProductModel.getMainProductId();
		if (content != null && !content.equals("")) {
			String[] pieces = content.split("\\,");
			for (String piece : pieces) {
				String[] item = piece.split("\\|");
				if (Long.valueOf(item[0]) != mShoppingCartProductModel.getProductId()) {
					str += ((str.equals("") ? "" : ",") + piece);
				}
			}
		}
		
		cache.set(mKey, str, 0);
	}

	public static int getBuyCount(long productId) {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		if (content == null || content.equals("")) {
			return 0;
		}

		String[] pieces = content.split("\\,");

		for (String piece : pieces) {
			String item[] = piece.split("\\|");
			if (Long.valueOf(item[0]) == productId) {
				return Integer.valueOf(item[1]);
			}
		}

		return 0;
	}

	public static ArrayList<ShoppingCartProductModel> getList() {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		ArrayList<ShoppingCartProductModel> models = new ArrayList<ShoppingCartProductModel>();
		if (content == null || content.equals("")) {
			return models;
		}
		
		String[] pieces = content.split("\\,");

		for (String piece : pieces) {
			String item[] = piece.split("\\|");
			ShoppingCartProductModel model = new ShoppingCartProductModel();
			model.setProductId(Long.valueOf(item[0]));
			model.setBuyCount(Integer.valueOf(item[1]));
			model.setMainProductId(Long.valueOf(item[2]));

			models.add(model);
		}

		return models;
	}

	public static void remove(long productId) {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		if (content == null || content.equals(""))
			return;
		
		String[] pieces = content.split("\\,");

		String str = "";

		for (String piece : pieces) {
			String[] item = piece.split("\\|");
			if (Long.valueOf(item[0]) != productId) {
				str += ((str.equals("") ? "" : ",") + piece);
			}
		}

		cache.set(mKey, str, 0);
	}

	public static void set(ArrayList<ShoppingCartProductModel> models) {
		IPageCache cache = new IPageCache();

		String str = "";

		for (ShoppingCartProductModel model : models) {
			str += ((str.equals("") ? "" : ",") + model.getProductId() + "|" + model.getBuyCount() + "|" + model.getMainProductId());
		}
		
		cache.set(mKey, str, 0);
	}

	public static void clear() {
		IPageCache cache = new IPageCache();
		cache.remove(mKey);
	}

	public static int getProductCount() {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		if (content == null || content.equals("")) {
			return 0;
		}

		String[] pieces = content.split("\\,");

		int count = 0;
		for (String piece : pieces) {
			String item[] = piece.split("\\|");
			count += (item.length == 3 ? Integer.valueOf(item[1]) : 0);
		}

		return count;
	}
}
