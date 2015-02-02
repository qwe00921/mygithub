package com.niuan.wificonnector;

import java.util.ArrayList;
import java.util.Map;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.niuan.wificonnector.WifiScanner.OnScanListener;
import com.niuan.wificonnector.lib.list.adapter.WrappedListAdapter;
import com.niuan.wificonnector.lib.list.adapter.holder.DataHolder;
import com.niuan.wificonnector.lib.ui.AppEvent;
import com.niuan.wificonnector.lib.ui.BaseDialogFragment;
import com.niuan.wificonnector.lib.ui.BaseListFragment;
import com.niuan.wificonnector.lib.ui.FragmentIndex;
import com.niuan.wificonnector.lib.ui.InputDialogFragment;
import com.niuan.wificonnector.net.FetchPasswordRequest;
import com.niuan.wificonnector.net.FetchPasswordResponse;
import com.niuan.wificonnector.net.Proxy;
import com.niuan.wificonnector.net.ResponseListener;
import com.niuan.wificonnector.util.NetUtils;
import com.niuan.wificonnector.util.ToastUtil;

public class WifiListFragment extends
		BaseListFragment<WifiDataHolder, WifiViewHolder> {

	private WifiScanner mScanner = WifiScanner.getInstance();
	private WifiAdmin mAdmin;

	private Handler mHandler = new Handler();
	private Runnable mRefreshRunnable = new Runnable() {

		@Override
		public void run() {
			if (isResumed()) {
				refreshData();
				autoRefresh();
			}

		}
	};

	private void autoRefresh() {
		mHandler.removeCallbacks(mRefreshRunnable);
		mHandler.postDelayed(mRefreshRunnable, 2000);
	}

	public void onCreate(android.os.Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mAdmin = WifiAdmin.getInstance();
	};

	@Override
	public void onResume() {

		super.onResume();
		autoRefresh();
	}

	@Override
	public void requestData(final int refreshType) {

		mScanner.scanWifiAsync(getActivity(), new OnScanListener() {

			@Override
			public void onScanFinished(ArrayList<WifiDataHolder> list) {
				requestFinish(refreshType, list, false, true, false);
			}
		});

	}

	@Override
	protected WrappedListAdapter<WifiDataHolder, WifiViewHolder> initAdapter() {
		return new WifiListAdapter(getActivity());
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
//		view.showContextMenu();
		super.onItemClick(parent, view, position, id);
	}

	private static final String LOG_TAG = "WifiListFragment";

	@Override
	public void onActivityEvent(AppEvent event, Object... params) {
		Log.d(LOG_TAG, "[onEvent], event = " + event);

		super.onActivityEvent(event, params);
	}

	@Override
	public void onFragmentEvent(AppEvent event, FragmentIndex fi,
			Object... params) {

		switch (event) {
		case FRAGMENT_DIALOG_INPUT_OK: {
			WifiDataHolder data = (WifiDataHolder) params[0];
			tryAutoConnect(data);
			break;
		}
		case FRAGMENT_DIALOG_PASSWORD: {
			WifiDataHolder data = (WifiDataHolder) params[0];
			String pswd = (String) params[1];

			connect(data.getSsid(), null, pswd, data.getSecurType());

			ToastUtil.makeToast("验证中...");
			break;
		}
		}

		super.onFragmentEvent(event, fi, params);
	}

	private void tryAutoConnect(final WifiDataHolder data) {

		if (!NetUtils.isWifiConnected(getActivity())) {
			ToastUtil.makeToast("请打开数据连接以获取热点信息");
			return;
		}
		ArrayList<String> macAddress = new ArrayList<String>();

		if (data.getResultList() != null) {
			for (ScanResult result : data.getResultList()) {
				macAddress.add(result.BSSID);
			}
		}

		FetchPasswordRequest req = new FetchPasswordRequest();
		req.macAddress = macAddress;

		ToastUtil.makeToast("正在获取密码..");
		Proxy.sendRequest(getActivity(), req,
				new ResponseListener<FetchPasswordResponse>() {

					@Override
					public void onResponse(FetchPasswordResponse rsp) {
						String SSID = data.getSsid();
						Map<String, String> pswdMap = rsp.pswdMap;
						boolean succ = false;
						for (String BSSID : pswdMap.keySet()) {

							String pswd = pswdMap.get(BSSID);
							SecurityType secType = data.getSecurType();
							succ = connect(SSID, BSSID, pswd, secType);
							if (succ) {
								break;
							}
						}
						if (succ) {
							ToastUtil.makeToast("验证中...");
						} else {

							ToastUtil.makeToast("密码获取失败");
						}

					}

					@Override
					public void onError(Exception e) {

					}
				});
	}

	private boolean connect(String ssid, String bssid, String pwd,
			SecurityType type) {
		return mAdmin.connect(ssid, bssid, pwd, type);
	}

	private static final String CONNECT = "普通连接";
	private static final String DISCONNECT = "断开连接";
	private static final String FORGET = "忘记网络";
	private static final String AUTO_CONNECT = "解锁连接";

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		WifiDataHolder data = getDataByMenuInfo((AdapterContextMenuInfo) menuInfo);
//
//		menu.clear();
//		menu.setHeaderTitle(data.getSsid());
//		if (data.isConnected()) {
//			menu.add(DISCONNECT);
//			menu.add(FORGET);
//		} else {
//			menu.add(CONNECT);
//			menu.add(AUTO_CONNECT);
//		}
//
//		super.onCreateContextMenu(menu, v, menuInfo);
//	}

	private WifiDataHolder getDataByMenuInfo(ContextMenuInfo menuInfo) {

		if (menuInfo instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			WifiDataHolder data = (WifiDataHolder) getListView().getAdapter()
					.getItem(info.position);
			return data;
		}
		return null;
	}

//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		WifiDataHolder data = getDataByMenuInfo((AdapterContextMenuInfo) item
//				.getMenuInfo());
//		String title = item.getTitle().toString();
//		if (CONNECT.equals(title)) {
//			if (mAdmin.connect(data.getSsid())) {
//				// 已经配置过，直接连接
//			} else {
//				// 未配置过，让用户输入用户名和密码
//
//				BaseDialogFragment fragment = InputDialogFragment.newInstance(
//						data.getSsid(), data);
//				fragment.registerFragmentObserver(buildIndex());
//				showDialog(fragment);
//			}
//			return true;
//		} else if (DISCONNECT.equals(title)) {
//			mAdmin.disconnect();
//			return true;
//		} else if (AUTO_CONNECT.equals(title)) {
//			tryAutoConnect(data);
//			return true;
//		} else if (FORGET.equals(title)) {
//			mAdmin.forgetNetWork(data.getSsid());
//		}
//		return super.onContextItemSelected(item);
//	}
//
//	String[] optionsMenu = new String[] { "用户中心", "随身wifi", "wifi省电", "流量统计",
//			"退出应用" };
//
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		for (String str : optionsMenu) {
//			menu.add(str);
//		}
//		super.onCreateOptionsMenu(menu, inflater);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		ToastUtil.makeToast("暂未支持");
//		return super.onOptionsItemSelected(item);
//	}
}
