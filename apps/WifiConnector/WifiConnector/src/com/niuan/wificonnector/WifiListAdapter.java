package com.niuan.wificonnector;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.niuan.wificonnector.lib.list.adapter.WrappedListAdapter;

public class WifiListAdapter extends
		WrappedListAdapter<WifiDataHolder, WifiViewHolder> {

	public WifiListAdapter(Context context) {
		super(context);
	}

	@Override
	public int getItemResourceId(int position) {
		return R.layout.list_item_wifi_info;
	}

	@Override
	protected WifiViewHolder initViewHolder(View convertView) {
		WifiViewHolder holder = new WifiViewHolder();

		holder.mName = (TextView) convertView.findViewById(R.id.wifi_info_name);
		holder.mLevel = (ImageView) convertView
				.findViewById(R.id.wifi_info_level);
		holder.mLinked = (ImageView) convertView
				.findViewById(R.id.wifi_info_linked);
		return holder;
	}

	@Override
	protected void updateView(WifiDataHolder data, WifiViewHolder view) {

		boolean isLocked = data.getSecurType() != SecurityType.NOPASS;

		String ssid = data.getSsid();
		if(data.isSafe()) {
			ssid += "(安全WIFI)";
		} 
		view.mName.setText(ssid);

		int imgRes = 0;
		int level = Math.abs(data.getLevel());
		if (isLocked) {
			if (level < 25) {
				imgRes = R.drawable.ic_wifi_lock_signal_1;
			} else if (level < 50) {
				imgRes = R.drawable.ic_wifi_lock_signal_2;
			} else if (level < 75) {
				imgRes = R.drawable.ic_wifi_lock_signal_3;
			} else if (level < 100) {
				imgRes = R.drawable.ic_wifi_lock_signal_4;
			}
		} else {
			if (level < 25) {
				imgRes = R.drawable.ic_wifi_open_signal_1;
			} else if (level < 50) {
				imgRes = R.drawable.ic_wifi_open_signal_2;
			} else if (level < 75) {
				imgRes = R.drawable.ic_wifi_open_signal_3;
			} else if (level < 100) {
				imgRes = R.drawable.ic_wifi_open_signal_4;
			}
		}

		view.mLevel.setImageResource(imgRes);

		if (data.isConnected()) {
			view.mLinked.setVisibility(View.VISIBLE);
		} else {
			view.mLinked.setVisibility(View.GONE);
		}
		
	}

}
