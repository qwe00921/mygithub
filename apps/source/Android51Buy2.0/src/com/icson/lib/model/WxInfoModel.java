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

/**  
 *   
 * Class Name:DeliverInfoModel
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-5-6 下午03:12:22 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class WxInfoModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String mToken;
	private String mSign;
	private String mPackage;
	private String mPartner;
	
	public WxInfoModel()
	{
		setToken("");
		setSign("");
		setPackage("");
		setPartner("");
	}

	public void setToken(String aToken) {
		this.mToken = aToken;
	}

	public String getToken() {
		return mToken;
	}

	public void setSign(String aSign) {
		this.mSign = aSign;
	}

	public String getSign() {
		return mSign;
	}

	public void setPackage(String aPackage) {
		this.mPackage = aPackage;
	}

	public String getPackage() {
		return mPackage;
	}

	public void setPartner(String aPartner) {
		this.mPartner = aPartner;
	}

	public String getPartner() {
		return mPartner;
	}


	
}
	
	