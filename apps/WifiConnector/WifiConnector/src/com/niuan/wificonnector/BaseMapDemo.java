package com.niuan.wificonnector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapView;
import com.niuan.wificonnector.lib.ui.BaseFragment;

/**
 * 演示MapView的基本用法
 */
public class BaseMapDemo extends BaseFragment {
	@SuppressWarnings("unused")
	private static final String LTAG = BaseMapDemo.class.getSimpleName();
	private MapView mMapView;
	private BaiduMap mBaiduMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		mMapView = new MapView(getActivity(), new BaiduMapOptions());
//		mBaiduMap = mMapView.getMap();
//		super.onCreateView(inflater, container, savedInstanceState);
		

		ImageView sView = new ImageView(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		sView.setLayoutParams(params);

		sView.setBackgroundResource(R.drawable.wifi_map);
		return sView;
	}

	@Override
	public void onPause() {
		super.onPause();
		// activity 暂停时同时暂停地图控件
	}

	@Override
	public void onResume() {
		super.onResume();
		// activity 恢复时同时恢复地图控件
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// activity 销毁时同时销毁地图控件
	}

}
