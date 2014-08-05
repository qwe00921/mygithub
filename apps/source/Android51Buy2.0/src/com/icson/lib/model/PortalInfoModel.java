/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: PortalInfoModel.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-4-16
 */
package com.icson.lib.model;

import java.io.Serializable;

/**  
 *   
 * Class Name:PortalInfoModel
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-4-16 下午06:12:22 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class PortalInfoModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String sFileName = "portal_img";
	public static final String sFileTmpName = "portal_tmp";
	private String imgUrl;
	private int    mBeginTime;
	private int    mExpireTime;
	private int    mShowDuration;
	private int    mSpver;
	

	public PortalInfoModel()
	{
		imgUrl = "";
		mBeginTime = 0;
		mExpireTime = 0;
		mShowDuration = 0;
		mSpver = 0;
	}
	
	public void setImgUrl(String aImgUrl) {
		this.imgUrl = aImgUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setExpireTime(int aTime) {
		this.mExpireTime = aTime;
	}

	public int getExpireTime() {
		return mExpireTime;
	}

	public void setBeginTime(int aTime) {
		this.mBeginTime = aTime;
	}

	public int getBeginTime() {
		return mBeginTime;
	}

	public void setShowDuration(int aDuration) {
		this.mShowDuration = aDuration;
	}

	public int getShowDuration() {
		return mShowDuration;
	}

	public void setSpver(int aSpversion) {
		this.mSpver = aSpversion;
	}

	public int getSpver() {
		return mSpver;
	}

	public String infoToString() {
		return "" + mSpver + ";" + mShowDuration + ";" + mBeginTime + ";" + mExpireTime;
	}
	
	public void infoFromString(String aInfo) {
		if (aInfo == null)
			return;
		String items[] = aInfo.split(";");
		if(items.length < 4 )
			return;
		mSpver = Integer.valueOf(items[0]);
		mShowDuration = Integer.valueOf(items[1]);
		mBeginTime = Integer.valueOf(items[2]);
		mExpireTime = Integer.valueOf(items[3]);
	}

}
	
	