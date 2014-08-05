package com.icson.postsale;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class PostSaleLogModel extends BaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mContent;
	private String mLogTime;

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
	}

	public String getLogTime() {
		return mLogTime;
	}

	public void setLogTime(String logTime) {
		mLogTime = logTime;
	}
	
	public void parse(JSONObject json) throws JSONException {
		setContent(json.optString(Constants.KEY_LOG, ""));
		setLogTime(json.optString(Constants.KEY_LOG_TIME, ""));
	}

}
