package com.icson.order.userpoint;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

@SuppressWarnings("serial")
public class UserPointModel extends BaseModel implements Serializable{

	// 用户积分
	private long minPoint;
	private long maxPoint;
	private long userPoint;
	//用户输入的即将使用的积分
	private long inputPoint;
	
	public long getInputPoint() {
		return inputPoint;
	}
	public void setInputPoint(long inputPoint) {
		this.inputPoint = inputPoint;
	}
	public long getMinPoint() {
		return minPoint;
	}
	public void setMinPoint(long minPoint) {
		this.minPoint = minPoint;
	}
	public long getMaxPoint() {
		return maxPoint;
	}
	public void setMaxPoint(long maxPoint) {
		this.maxPoint = maxPoint;
	}
	public long getUserPoint() {
		return userPoint;
	}
	public void setUserPoint(long userPoint) {
		this.userPoint = userPoint;
	}
	public void parse(JSONObject data) throws JSONException{
		setMinPoint(data.getLong("minPoint"));
		setMaxPoint(data.getLong("maxPoint"));
		setUserPoint(data.getLong("userPoint"));
	}
	
}
