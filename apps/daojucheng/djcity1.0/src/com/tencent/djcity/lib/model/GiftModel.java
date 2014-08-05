package com.tencent.djcity.lib.model;

import java.io.Serializable;


public class GiftModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int  GIFT_FETCHED = 1;
	public static final int  GIFT_NEW = 0;
	
	private String mPicUrl;
	private String mName;
	private String mTimestamp;
	private int    mState;
	
	public GiftModel()
	{
		setPicUrl("");
		setName("");
		setTime("");
		mState = GIFT_NEW;
	}

	public void setPicUrl(String aUrl) {
		this.mPicUrl = aUrl;
	}

	public String getPicUrl() {
		return mPicUrl;
	}

	public void setName(String aName) {
		this.mName = aName;
	}

	public String getName() {
		return mName;
	}

	public void setTime(String aTime) {
		this.mTimestamp = aTime;
	}

	public String getTime() {
		return mTimestamp;
	}

	public void fetchedGift()
	{
		mState = GIFT_FETCHED;
	}
	
	public boolean isNew()
	{
		return mState == GIFT_NEW;
	}
	
}
	
	