package com.icson.event;

import java.util.ArrayList;

import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class Event2Model extends EventBaseModel {
	private String title;
	private String advertiseUrl;
	private String listUrl;

	private ArrayList<Event2SubModel> mEvent2SubModels;

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

	public ArrayList<Event2SubModel> getEvent2SubModels() {
		return mEvent2SubModels;
	}

	public void setEvent2SubModels(ArrayList<Event2SubModel> mEvent2SubModels) {
		this.mEvent2SubModels = mEvent2SubModels;
	}

	public static class Event2SubModel extends BaseModel {
		public String getPicUrl() {
			return picUrl;
		}

		public void setPicUrl(String picUrl) {
			this.picUrl = picUrl;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getListUrl() {
			return listUrl;
		}

		public void setListUrl(String listUrl) {
			this.listUrl = listUrl;
		}

		private String picUrl;
		private String title;
		private String desc;
		private String listUrl;

		public void parse(JSONObject json) throws Exception {
			setPicUrl(json.optString("pic_url", "").trim());
			setTitle(json.getString("title"));
			setListUrl(json.optString("list_url", "").trim());
			setDesc(json.getString("desc"));
		}
	}
}
