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


package com.tencent.djcity.home;

import org.json.JSONObject;

public class ProductInfo {
	public ProductInfo() {
		
	}
	
//	public static ProductInfo fromAttrs(String strCharId, String strPicUrl, String strMsg, String strInfo, String strComments) {
//		ProductInfo info = new ProductInfo();
//		info.mCharId = strCharId;
//		info.mPicUrl = strPicUrl;
//		info.mMsg = strMsg;
//		info.mInfo = strInfo;
//		info.mComments = strComments;
//		return info;
//	}
	
	public static ProductInfo fromJson(JSONObject aObject) {
		if( null == aObject )
			return null;
		
		ProductInfo info = new ProductInfo();
		info.appId = aObject.optString("appId");
		info.appName = aObject.optString("appName");
		info.appShort = aObject.optString("appShort");
		info.busId = aObject.optString("busId");
		info.limitPerOrder = aObject.optString("limitPerOrder");
		info.propDesc = aObject.optString("propDesc");
		info.propId = aObject.optString("propId");
		info.propImg = aObject.optString("propImg");
		info.propName = aObject.optString("propName");
		info.type = aObject.optString("type");
		
		return info;//ProductInfo.fromAttrs(aObject.optString("charId"), aObject.optString("picUrl"), aObject.optString("msg"), aObject.optString("info"), aObject.optString("comments"));
	}
	
//	/**
//	 * getPicUrl
//	 * @return
//	 */
//	public String getPicUrl() {
//		return mPicUrl;
//	}
	
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof ProductInfo)
        {
        	if(((ProductInfo) object).type.equals(this.type) &&
            	((ProductInfo) object).propId.equals(this.propId) &&
            	((ProductInfo) object).propName.equals(this.propName) &&
            	((ProductInfo) object).busId.equals(this.busId) &&
            	((ProductInfo) object).appName.equals(this.appName) &&
            	((ProductInfo) object).appShort.equals(this.appShort) &&
            	((ProductInfo) object).propDesc.equals(this.propDesc) &&
            	((ProductInfo) object).propImg.equals(this.propImg) &&
            	((ProductInfo) object).limitPerOrder.equals(this.limitPerOrder) &&
            	((ProductInfo) object).appId.equals(this.appId)
            	)
            	return true;
        }

        return false;
    }

	@Override
	public int hashCode() {
		int result = 17;
//		result += 37 * result + getStringHashCode(mCharId);
//		result += 37 * result + getStringHashCode(mPicUrl);
//		result += 37 * result + getStringHashCode(mMsg);
//		result += 37 * result + getStringHashCode(mInfo);
//		result += 37 * result + getStringHashCode(mComments);
		result += 37 * result + getStringHashCode(type);
		result += 37 * result + getStringHashCode(propId);
		result += 37 * result + getStringHashCode(propName);
		result += 37 * result + getStringHashCode(busId);
		result += 37 * result + getStringHashCode(appId);
		result += 37 * result + getStringHashCode(appName);
		result += 37 * result + getStringHashCode(appShort);
		result += 37 * result + getStringHashCode(propDesc);
		result += 37 * result + getStringHashCode(propImg);
		result += 37 * result + getStringHashCode(limitPerOrder);
		
		return result;
	}
	
	public int getStringHashCode(String str)
	{
		return null != str ? str.hashCode() : 0;
		
	}
//	private String mCharId="";  // Product Char ID
//	private String mPicUrl="";  // Picture URL
//	String mMsg="";
//	String mInfo="";
//	String mComments="";
	
	
	private String type;
	private String propId;
	private String propName;
	private String busId;
	private String appId;
	private String appName;
	private String appShort;
	private String propDesc;
	private String propImg;
	private String limitPerOrder;
	
//	type: "3",
//	propId: "468",
//	propName: "艾米&芭比（7天）",
//	busId: "nz",
//	appId: "",
//	appName: "逆战",
//	appShort: "nz",
//	propDesc: "火爆抢购",
//	propImg: "http://ossweb-img.qq.com/images/daoju/mq/nz/aimibabi260.jpg",
//	limitPerOrder: "1",
//	propDetail: {
//	picInfo: {
//	sGoodsPic2: null,
//	sGoodsPic3: ""
//	},
//	picDesc: [
//	{
//	img: "http://ossweb-img.qq.com/images/daoju/mq/nz/babi130.jpg",
//	desc: "芭比（7天）*1"
//	},
//	{
//	img: "http://ossweb-img.qq.com/images/daoju/mq/nz/aimi130.jpg",
//	desc: "艾米（7天）*1"
//	}
//	],
//	timeDesc: {
//	-99: "礼包"
//	}
//	},
//	todayLimit: "0",
//	totalLimit: "0",
//	selectArea: "1",
//	selectRole: "0",
//	valiDate: [
//	{
//	day: 0,
//	code: "468",
//	oldPrice: "600",
//	curPrice: "200",
//	wechatPrice: "10",
//	left: 0,
//	bought: "0",
//	todayBought: 0
//	}
//	]
//	},
}
