package com.tencent.djcity.more;

import org.json.JSONException;
import org.json.JSONObject;

public class GameNameModel {

	private String bizCode;
	private String bizName;
	private String imageUrl;
	private boolean isHot;
	private int roleFlag;

	public String getBizCode() {
		return bizCode;
	}

	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}

	public String getBizName() {
		return bizName;
	}

	public void setBizName(String bizName) {
		this.bizName = bizName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public boolean isHot() {
		return isHot;
	}

	public void setHot(boolean isHot) {
		this.isHot = isHot;
	}

	public int getRoleFlag() {
		return roleFlag;
	}

	public void setRoleFlag(int roleFlag) {
		this.roleFlag = roleFlag;
	}

	public void parse(JSONObject json) throws JSONException {
		if(json == null) {
			return;
		}
		
		setRoleFlag(json.optInt("roleFlag", 0));
		setBizCode(json.optString("bizCode", ""));
		setBizName(json.optString("bizName", ""));
		setImageUrl(json.optString("icon", ""));
		setHot(json.optInt("hot", 0) == 1);
	}

}
