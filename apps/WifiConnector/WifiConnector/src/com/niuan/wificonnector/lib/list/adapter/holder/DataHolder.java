package com.niuan.wificonnector.lib.list.adapter.holder;

import java.io.Serializable;

import android.os.Parcelable;

public class DataHolder implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int mItemId;

	public int getItemId() {
		return mItemId;
	}

	public void setItemId(int itemId) {
		this.mItemId = itemId;
	}
}
