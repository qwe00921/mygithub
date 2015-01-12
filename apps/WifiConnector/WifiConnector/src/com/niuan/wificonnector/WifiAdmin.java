package com.niuan.wificonnector;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.niuan.wificonnector.util.NetUtils;
import com.niuan.wificonnector.util.StringUtil;

public class WifiAdmin {

	private static final WifiAdmin INSTANCE;
	private Context mContext;
	private WifiManager mWifiManager;

	private WifiAdmin(Context context) {
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		mContext = context;
	}

	static {
		INSTANCE = new WifiAdmin(WifiApplication.getInstance());
	}

	public static WifiAdmin getInstance() {
		return INSTANCE;
	}

	public boolean connect(String ssid) {
		return connect(ssid, null, null, null);
	}

	public boolean connect(String ssid, String bssid, String pwd,
			SecurityType type) {
		int networkId = getConfiguredNetworkId(ssid);
		if (networkId == -1) {

			WifiConfiguration config = createWifiInfo(ssid, bssid, pwd, type);

			if (config != null) {
				networkId = mWifiManager.addNetwork(config);
			}
		}
		return mWifiManager.enableNetwork(networkId, true);
	}

	public boolean isWifiConnected() {
		return NetUtils.isWifiConnected(mContext);
	}

	public void disconnect() {
		mWifiManager.disconnect();
	}

	public boolean disableNetwork(String ssid) {

		int networkId = getConfiguredNetworkId(ssid);

		return mWifiManager.disableNetwork(networkId);
	}

	public void forgetNetWork(String ssid) {
		mWifiManager.removeNetwork(getConfiguredWifiInfo(ssid).networkId);
	}

	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	private int getConfiguredNetworkId(String SSID) {
		WifiConfiguration config = getConfiguredWifiInfo(SSID);
		if (config != null) {
			return config.networkId;
		}
		return -1;
	}

	private WifiConfiguration getConfiguredWifiInfo(String SSID) {
		List<WifiConfiguration> list = getConfigurationList();
		for (WifiConfiguration config : list) {
			if (config.SSID.equals("\"" + SSID + "\"")) {
				return config;
			}
		}
		return null;
	}

	// 得到配置好的网络
	private List<WifiConfiguration> getConfigurationList() {
		return mWifiManager.getConfiguredNetworks();
	}

	private WifiConfiguration createWifiInfo(String SSID, String BSSID,
			String password, SecurityType type) {
		if (type == null) {
			return null;
		}

		WifiConfiguration wfc = new WifiConfiguration();

		wfc.SSID = "\"".concat(SSID).concat("\"");
		wfc.status = WifiConfiguration.Status.DISABLED;
		wfc.BSSID = BSSID;
		wfc.priority = 40;
		switch (type) {
		case EAP: {
			break;
		}
		case NOPASS: {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.clear();
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			break;
		}
		case PSK: {
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

			wfc.preSharedKey = "\"".concat(password).concat("\"");
			break;
		}
		case WEP: {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wfc.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			if (StringUtil.isHex(password))
				wfc.wepKeys[0] = password;
			else
				wfc.wepKeys[0] = "\"".concat(password).concat("\"");
			wfc.wepTxKeyIndex = 0;
			break;
		}
		}

		return wfc;
	}
}