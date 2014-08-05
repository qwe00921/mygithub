/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: DeliverInfoModel.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-4-16
 */
package com.icson.lib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.amap.api.maps.model.LatLng;
import com.icson.amap.mapUtils.AMapConstants;

/**  
 *   
 * Class Name:DeliverInfoModel
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-4-16 下午06:12:22 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class DeliverInfoModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;
	private long   mOrderId;
	
	private String mImgUrl;
	private String mPhoneNo;
	private String mName;
	private LatLng mStartPos;
	private LatLng mDestPos;
	private LatLng mCurPos;
	private String mSubstationName;
	private String mDestAddressName;
	private String mSlogan;
	private List<LatLng> mPosList;
	private String mTime;
	private String mCountdown;
	public int    mCheckDistance;
	public long   mExpireHour;
	
	

	public DeliverInfoModel()
	{
		setImgUrl("");
		setPhoneNo("");
		setName("");
		mStartPos = AMapConstants.SHANGHAI;
		mDestPos  = AMapConstants.CAOHEJING;
		mCurPos   = AMapConstants.TEST;
		mPosList  = null;
		mSlogan = "";
		mTime = "";
		mCountdown = "";
		mCheckDistance = -1;
	}

	public void setTime(String strTime) {
		mTime = strTime;
	}
	
	public String getTime() {
		return mTime;
	}
	
	public void setCountdown(String strCountdown) {
		mCountdown = strCountdown;
	}
	
	public String getCountdown() {
		return mCountdown;
	}

	public void setName(String aName) {
		this.mName = aName;
	}


	public String getName() {
		return mName;
	}


	public void setImgUrl(String aImgUrl) {
		this.mImgUrl = aImgUrl;
	}


	public String getImgUrl() {
		return mImgUrl;
	}


	public void setPhoneNo(String aPhoneNo) {
		this.mPhoneNo = aPhoneNo;
	}


	public String getPhoneNo() {
		return mPhoneNo;
	}

				//aLat,aLon
	//Shanghai (31.165859, 121.396006)
	public void setStartPos(String aLat, String aLon) {
		this.mStartPos = new LatLng(Double.valueOf(aLat),Double.valueOf(aLon));
	}


	public LatLng getStartPos() {
		return mStartPos;
	}


	public void setDestPos(String aLat, String aLon) {
		this.mDestPos = new LatLng(Double.valueOf(aLat),Double.valueOf(aLon));
	}


	public LatLng getDestPos() {
		return mDestPos;
	}


	public void setCurPos(String aLat, String aLon) {
		this.mCurPos = new LatLng(Double.valueOf(aLat),Double.valueOf(aLon));
	}


	public LatLng getCurPos() {
		return mCurPos;
	}


	public void addToPosList(String aLat, String aLon) {
		if(null== mPosList)
			mPosList = new ArrayList<LatLng>();
		
		mPosList.add(new LatLng(Double.valueOf(aLat),Double.valueOf(aLon)));
	}


	public List<LatLng> getPosList() {
		return mPosList;
	}


	public void setSubstationName(String aSubstationName) {
		this.mSubstationName = aSubstationName;
	}


	public String getSubstationName() {
		return mSubstationName;
	}


	public void setOrderId(long aOrderId) {
		this.mOrderId = aOrderId;
	}


	public long getOrderId() {
		return mOrderId;
	}


	public void setDestAddressName(String aDestAddressName) {
		this.mDestAddressName = aDestAddressName;
	}


	public String getDestAddressName() {
		return mDestAddressName;
	}


	public void setSlogan(String aSlogan) {
		this.mSlogan = aSlogan;
	}


	public String getSlogan() {
		return mSlogan;
	}

}
	
	