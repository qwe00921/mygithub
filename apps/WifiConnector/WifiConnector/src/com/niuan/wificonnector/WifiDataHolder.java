package com.niuan.wificonnector;

import java.util.List;

import android.net.wifi.ScanResult;

import com.niuan.wificonnector.lib.list.adapter.holder.DataHolder;

public class WifiDataHolder extends DataHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6236810976188431915L;

	private String ssid;

	private List<ScanResult> resultList;

	private SecurityType securType;

	private int level;

	private boolean isConnected;

	public List<ScanResult> getResultList() {
		return resultList;
	}

	public void setResultList(List<ScanResult> resultList) {
		this.resultList = resultList;
	}

	private boolean isSafe;

	public void setIsSafe(boolean isSafe) {
		this.isSafe = isSafe;
	}

	public boolean isSafe() {
		return isSafe;
	}

	public SecurityType getSecurType() {
		return securType;
	}

	public void setSecurType(SecurityType securType) {
		this.securType = securType;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[\n");
		for (ScanResult result : resultList) {
			builder.append("[").append(result.toString()).append("]\n");
		}
		builder.append("]\n");
		return builder.toString();
	}
}
