package com.icson.lib;

import com.icson.lib.inc.CacheKeyFactory;

public class IViewHistory {

	private static String mKey = CacheKeyFactory.CACHE_VIEW_HISTORY_ITEMS;

	private static final int MAX_COUNT = 50;

	public static void set(long productId) {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		content = content == null ? "" : content.trim();
		//此处代码并未赋值，1.0.0到1.0.9版本程序正常，先注释掉
		//content.replaceAll("(^|,)" + productId, "");

		content = (productId + (content.equals("") ? "" : ",") + content);

		content = content.replaceAll("((?:(?:^|,)\\d+){1," + MAX_COUNT + "}).*$", "$1");

		cache.set(mKey, content, 0);
	}

	public static void clear() {
		IPageCache cache = new IPageCache();
		cache.remove(mKey);
	}

	public static long[] getList() {
		IPageCache cache = new IPageCache();
		String content = cache.get(mKey);

		if (content == null || content.trim().equals("")) {
			return null;
		}

		String[] pieces = content.split("\\,");
		long[] ret = new long[pieces.length];
		int i = 0;
		for (String piece : pieces) {
			ret[i++] = Long.valueOf(piece);
		}

		return ret;
	}
}
