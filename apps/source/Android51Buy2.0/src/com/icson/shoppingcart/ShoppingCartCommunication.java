package com.icson.shoppingcart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.icson.lib.IShoppingCart;
import com.icson.util.Config;

public class ShoppingCartCommunication {

	private Activity mActivity;

	private OnShoppingCartChangeListener mOnShoppingCartChangeListener;

	private BroadcastReceiver mBroadcastReceiver;

	public ShoppingCartCommunication(Activity activity) {
		mActivity = activity;
	}

	public void setOnShoppingCartChangeListener(final OnShoppingCartChangeListener listener) {
		mOnShoppingCartChangeListener = listener;

		if (mBroadcastReceiver == null) {
			mBroadcastReceiver = new ShoppingCartBroadcastReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Config.BROADCAST_SHOPPING);
			mActivity.registerReceiver(mBroadcastReceiver, filter,Config.SLEF_BROADCAST_PERMISSION,null);
		}
	}

	public void notifyDataSetChange() {
		Intent intent = new Intent();
		intent.setAction(Config.BROADCAST_SHOPPING);
		mActivity.sendBroadcast(intent,Config.SLEF_BROADCAST_PERMISSION);
	}
	
	/**
	 * 这个receiver只是为了去改变购物车显示商品数目那个圆形textView的状态的
	 */
	private class ShoppingCartBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mOnShoppingCartChangeListener == null)
				return;
			final int buyNum = IShoppingCart.getProductCount();
			mOnShoppingCartChangeListener.OnShoppingCartChange(buyNum);
		}
	}

	public interface OnShoppingCartChangeListener {
		void OnShoppingCartChange(int num);
	}

	public void destroy() {
		if (mBroadcastReceiver != null) {
			mActivity.unregisterReceiver(mBroadcastReceiver);
		}

		mBroadcastReceiver = null;
		mActivity = null;
		mOnShoppingCartChangeListener = null;
	}
}
