package com.niuan.wificonnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.niuan.wificonnector.util.StringUtil;
import com.niuan.wificonnector.util.thread.BackgroundTask;

public class WifiScanner {

	private static final WifiScanner mInstance = new WifiScanner();

	public static WifiScanner getInstance() {
		return mInstance;
	}

	public void scanWifiAsync(final Context context,
			final OnScanListener listener) {
		new BackgroundTask<Void, Void, ArrayList<WifiDataHolder>>() {
			@Override
			protected ArrayList<WifiDataHolder> doInBackground(Void... params) {
				return getWifiList(context);
			}

			protected void onPostExecute(ArrayList<WifiDataHolder> result) {
				if (listener != null) {
					listener.onScanFinished(result);
				}
			};

		}.execute();
	}

	public ArrayList<WifiDataHolder> getWifiList(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		List<WifiConfiguration> configList = wm.getConfiguredNetworks();

		WifiInfo info = wm.getConnectionInfo();
		String connectedSSID = info.getSSID();
		String connectedBSSID = info.getBSSID();

		List<ScanResult> resultList = wm.getScanResults();

		sort(resultList);

		ArrayList<WifiDataHolder> list = wrapScanResult(connectedSSID,
				connectedBSSID, resultList);

		return list;
	}

	private ArrayList<WifiDataHolder> wrapScanResult(String connectedSSID,
			String connectedBSSID, List<ScanResult> resultList) {
		ArrayList<WifiDataHolder> list = new ArrayList<WifiDataHolder>();

		WifiDataHolder holder = new WifiDataHolder();

		holder.setConnected(false);
		holder.setSsid("STARBUCKS");
		holder.setLevel(75);
		holder.setIsSafe(true);
		holder.setSecurType(SecurityType.NOPASS);
		list.add(holder);

		holder = new WifiDataHolder();

		holder.setConnected(false);
		holder.setSsid("7DAYSINN");
		holder.setIsSafe(true);
		holder.setLevel(75);
		holder.setSecurType(SecurityType.NOPASS);
		list.add(holder);
		// ScanResult result = new ScanResult();

		WifiDataHolder data = null;
		ScanResult lastResult = null;
		for (ScanResult result : resultList) {

			String SSID = result.SSID;
			if (TextUtils.isEmpty(SSID)) {
				continue;
			}
			String BSSID = result.BSSID;
			List<ScanResult> subList = null;
			if (lastResult != null) {
				String lastSSID = lastResult.SSID;
				if (lastSSID.equals(SSID)) {
					if (data != null) {
						subList = data.getResultList();
					}
				}
			}
			if (subList == null) {
				data = new WifiDataHolder();
				subList = new ArrayList<ScanResult>();
				data.setResultList(subList);
				data.setLevel(result.level);
				data.setSsid(SSID);
				String compareSSID = String.format("\"%s\"", SSID);
				if (compareSSID.equals(connectedSSID)) {
					data.setConnected(true);
					list.add(0, data);
				} else {
					list.add(data);
				}

				String cap = result.capabilities;
				if (cap != null) {
					cap = cap.toLowerCase();
					boolean isWep = cap.contains("wep");
					boolean isWpa = cap.contains("psk");
					boolean isEAP = cap.contains("eap");
					data.setSecurType(isWep ? SecurityType.WEP
							: isWpa ? SecurityType.PSK
									: isEAP ? SecurityType.EAP
											: SecurityType.NOPASS);
				}
			}
			if (BSSID.equals(connectedBSSID)) {
				subList.add(0, result);
			} else {
				subList.add(result);
			}
			lastResult = result;
		}
		return list;
	}

	private void sort(List<ScanResult> resultList) {
		Collections.sort(resultList, new Comparator<ScanResult>() {

			@Override
			public int compare(ScanResult lhs, ScanResult rhs) {
				String l_ssid = lhs.SSID;
				String r_ssid = rhs.SSID;

				char lHeadChar = StringUtil.getFirstChar(l_ssid);
				char rHeadChar = StringUtil.getFirstChar(r_ssid);
				if (lHeadChar == rHeadChar) {
					if (l_ssid.hashCode() == r_ssid.hashCode()) {
						if (lhs.level == rhs.level) {
							return 0;
						}
						return lhs.level > rhs.level ? 1 : -1;
					}
					return l_ssid.hashCode() > r_ssid.hashCode() ? 1 : -1;
				} else {
					return lHeadChar > rHeadChar ? 1 : -1;
				}

			}
		});
	}

	public interface OnScanListener {
		public void onScanFinished(ArrayList<WifiDataHolder> list);
	}

	// WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type)
	// {
	// WifiConfiguration config = new WifiConfiguration();
	// config.allowedAuthAlgorithms.clear();
	// config.allowedGroupCiphers.clear();
	// config.allowedKeyManagement.clear();
	// config.allowedPairwiseCiphers.clear();
	// config.allowedProtocols.clear();
	// config.SSID = "\"" + SSID + "\"";
	// if(Type == Data.WIFICIPHER_NOPASS)
	// {
	// config.wepKeys[0] = "";
	// config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	// config.wepTxKeyIndex = 0;
	// }
	// if(Type == Data.WIFICIPHER_WEP)
	// {
	// config.hiddenSSID = true;
	// config.wepKeys[0]= "\""+Password+"\"";
	// config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
	// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
	// config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	// config.wepTxKeyIndex = 0;
	// }
	// if(Type == Data.WIFICIPHER_WPA)
	// {
	// config.preSharedKey = "\""+Password+"\"";
	// config.hiddenSSID = true;
	// config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	// config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	// config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	// config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	// config.status = WifiConfiguration.Status.ENABLED;
	// }
	// return config;
	// }

}
