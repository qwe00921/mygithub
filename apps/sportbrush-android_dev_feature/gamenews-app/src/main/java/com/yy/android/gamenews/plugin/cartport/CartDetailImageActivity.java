package com.yy.android.gamenews.plugin.cartport;

import com.yy.android.gamenews.util.StatsUtil;

import android.content.Context;
import android.content.Intent;

public class CartDetailImageActivity extends SingleFragmentActivity {

	public static void startCartDetailActivity(Context context, long mId,
			String brandName) {

		startActivity(context, mId, brandName, TYPE_CAR_COLUMN);
	}

	protected static void startActivity(Context context, long id, String title,
			int type) {

		Intent intent = new Intent(context, CartDetailImageActivity.class);
		intent.putExtra(TAG_FGMT_CARTID, id);
		intent.putExtra(TAG_FGMT_CAR_COLUMN, title);
		intent.putExtra(TAG_FGMT_TYPE, type);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		StatsUtil.statsReport(context, "into_cart_image", "desc",
				"into_cart_image");
		StatsUtil
				.statsReportByHiido("into_cart_image", "into_cart_image");
		StatsUtil.statsReportByMta(context, "into_cart_image",
				"into_cart_image");
		
	}

}
