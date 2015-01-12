package com.niuan.wificonnector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.niuan.wificonnector.lib.ui.FrameButton;
import com.niuan.wificonnector.lib.ui.FrameFragmentItem;
import com.niuan.wificonnector.lib.ui.FrameFragmentLayout;
import com.niuan.wificonnector.lib.ui.SingleFragmentActivity;
import com.niuan.wificonnector.util.ToastUtil;

public class MainActivity extends SingleFragmentActivity {
//
//	@Override
//	protected void onCreate(Bundle arg0) {
//		super.onCreate(arg0);
//		setContentView(R.layout.activity_main);
//
//		FrameFragmentLayout layout = (FrameFragmentLayout) findViewById(R.id.container);
//		for (int i = 0; i < 5; i++) {
//			FrameFragmentItem item = new FrameFragmentItem();
//			item.btn = new FrameButton(this);
//
//			switch (i) {
//			case 0: {
//
//				item.fragment = new WifiListFragment();
//				item.name = "连接";
//				break;
//			}
//			case 1: {
//				// MapStatus ms = new MapStatus.Builder().overlook(-20).zoom(15)
//				// .build();
//				// BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
//				// .compassEnabled(false).zoomControlsEnabled(false);
//				// SupportMapFragment map = SupportMapFragment.newInstance(bo);
//				//
//				// map.getBaiduMap().setMyLocationEnabled(true);
//				item.fragment = new BaseMapDemo();
//				item.name = "地图";
//				break;
//			}
//			case 2: {
//
//				item.fragment = new SecurityFragment();
//				item.name = "安全";
//				break;
//			}
//			case 3: {
//				item.fragment = new MoreFragment();
//
//				item.name = "百宝箱";
//				break;
//			}
//			case 4: {
//
//				item.fragment = new EmptyFragment();
//				item.name = "更多";
//				break;
//			}
//			}
//			layout.add(item);
//		}
//		layout.show(0);
//	}

	public void onClick(View view) {
		ToastUtil.makeToast("暂未支持");
	}

	@Override
	protected Fragment initFragment() {
		// TODO Auto-generated method stub
		return new MainFragment();
	}
}