package com.yy.android.gamenews.plugin.cartport;

import android.content.Context;
import android.content.Intent;

public class CartportActivity extends SingleFragmentActivity {

	public static void startCarportActivity(Context context) {

		startActivity(context, TYPE_CARTPORT);
	}

	protected static void startActivity(Context context, int type) {

		Intent intent = new Intent(context, CartportActivity.class);
		intent.putExtra(TAG_FGMT_TYPE, type);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
