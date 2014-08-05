package com.icson.event;

import java.util.ArrayList;

public class Event1Model extends EventBaseModel {
	private String title;
	private String advertiseUrl;
	private String listUrl;
	private ArrayList<EventProductModel> mProductModels;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAdvertiseUrl() {
		return advertiseUrl;
	}

	public void setAdvertiseUrl(String advertiseUrl) {
		this.advertiseUrl = advertiseUrl;
	}

	public String getListUrl() {
		return listUrl;
	}

	public void setListUrl(String listUrl) {
		this.listUrl = listUrl;
	}

	public ArrayList<EventProductModel> getProductModels() {
		return mProductModels == null ? new ArrayList<EventProductModel>() : mProductModels;
	}

	public void setProductModels(ArrayList<EventProductModel> mProductModels) {
		this.mProductModels = mProductModels;
	}
}
