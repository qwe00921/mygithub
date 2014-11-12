package com.yy.android.gamenews.plugin.cartport;

import android.content.Context;
import android.content.Intent;

public class CartDetailActivity extends SingleFragmentActivity {

	public static void startCartDetailActivity(Context context, int brandId,
			String brandName) {

		startActivity(context, brandId, brandName, TYPE_CARTDETAIL);
	}

	protected static void startActivity(Context context, int cartId,
			String brandName, int type) {

		Intent intent = new Intent(context, CartDetailActivity.class);
		intent.putExtra(TAG_FGMT_CARTID, cartId);
		intent.putExtra(TAG_FGMT_BRANDNAME, brandName);
		intent.putExtra(TAG_FGMT_TYPE, type);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
