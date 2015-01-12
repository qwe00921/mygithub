package com.niuan.wificonnector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.niuan.wificonnector.lib.ui.SingleFragmentActivity;

public class WifiListActivity extends SingleFragmentActivity {

	public static void startActivity(Context context) {
		Intent intent = new Intent(context, WifiListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected Fragment initFragment() {
		return new WifiListFragment();
	}

}
