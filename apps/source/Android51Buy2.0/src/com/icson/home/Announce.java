package com.icson.home;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class Announce extends BaseModel {

	public Announce() {
	}

	public String getMsg() {
		return mMsg;
	}

	public int getAnnounceId() {
		return mId;
	}

	public boolean isOutTime() {
		return mEndTime < (System.currentTimeMillis() / 1000);
	}

	private int mId;// 公告id
	private long mEndTime;// 过时时间
	private String mMsg;// 公告内容

	void parse(JSONObject v) throws JSONException {
		mId = v.optInt("id");
		mMsg = v.optString("msg");
		mEndTime = v.optLong("end");
	}
}
