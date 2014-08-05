/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: ProductInfo.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 19, 2013
 */


package com.icson.home;

import org.json.JSONObject;

import com.icson.lib.IcsonProImgHelper;

import android.text.TextUtils;

public class ProductInfo {
	public ProductInfo() {
		
	}
	
	public static ProductInfo fromAttrs(String strCharId, String strPicUrl, String strMsg, String strInfo, String strComments) {
		ProductInfo info = new ProductInfo();
		info.mCharId = strCharId;
		info.mPicUrl = strPicUrl;
		info.mMsg = strMsg;
		info.mInfo = strInfo;
		info.mComments = strComments;
		return info;
	}
	
	public static ProductInfo fromJson(JSONObject aObject) {
		if( null == aObject )
			return null;
		
		return ProductInfo.fromAttrs(aObject.optString("charId"), aObject.optString("picUrl"), aObject.optString("msg"), aObject.optString("info"), aObject.optString("comments"));
	}
	
	/**
	 * getPicUrl
	 * @return
	 */
	public String getPicUrl() {
		if( !TextUtils.isEmpty(mCharId) )
			return IcsonProImgHelper.getAdapterPicUrl(mCharId, 110);
		
		return mPicUrl;
	}
	
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof ProductInfo)
        {
        	if(((ProductInfo) object).mMsg.equals(this.mMsg) &&
            	((ProductInfo) object).mInfo.equals(this.mInfo) &&
            	((ProductInfo) object).mComments.equals(this.mComments) &&
            	((ProductInfo) object).mPicUrl.equals(this.mPicUrl) &&
            	((ProductInfo) object).mCharId.equals(this.mCharId))
            	return true;
        }

        return false;
    }

	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + getStringHashCode(mCharId);
		result += 37 * result + getStringHashCode(mPicUrl);
		result += 37 * result + getStringHashCode(mMsg);
		result += 37 * result + getStringHashCode(mInfo);
		result += 37 * result + getStringHashCode(mComments);
		
		return result;
	}
	
	public int getStringHashCode(String str)
	{
		return null != str ? str.hashCode() : 0;
		
	}
	private String mCharId="";  // Product Char ID
	private String mPicUrl="";  // Picture URL
	String mMsg="";
	String mInfo="";
	String mComments="";
}
