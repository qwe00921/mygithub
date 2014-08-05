package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class UserModel extends BaseModel implements Serializable{

	private String icsonid;
	private int level;
	private long point;
	private String cash_point;
	private String levelDesc;
	private int mCouponNum;
	private int mFavorNum;
	
	public  int mIsNewUser;
	public  String mNewUserCouponImg;

	public String getIcsonid() {
		return icsonid;
	}

	public void setIcsonid(String icsonid) {
		this.icsonid = icsonid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getPoint() {
		return point;
	}

	public void setPoint(long point) {
		this.point = point;
	}

	public String getCashPoint() {
		return cash_point;
	}

	public void setCashPoint(String point) {
		this.cash_point = point;
	}

	public String getLevelDesc() {
		return levelDesc;
	}

	public void setLevelDesc(String levelDesc) {
		this.levelDesc = levelDesc;
	}
	
	public void setCouponNum(int num){
		this.mCouponNum = num;
	}
	
	public int getCouponNum() {
		return this.mCouponNum;
	}
	
	public void setFavorNum(int num){
		this.mFavorNum = num;
	}
	
	public int getFavorNum(){
		return this.mFavorNum;
	}

	public void parse(JSONObject v) throws JSONException {
		setIcsonid(v.optString("icsonid"));
		setPoint(v.optLong("point"));
		setCashPoint(v.optString("cash_point","0"));
		setLevel(v.optInt("level"));
		setLevelDesc(v.optString("levelDesc"));
		setCouponNum(v.optInt("coupon_num", 0));
		setFavorNum(v.optInt("favor_num", 0));
		
		mIsNewUser = v.optInt("is_new", 0);
		mNewUserCouponImg = v.optString("image", "");
	}

}
