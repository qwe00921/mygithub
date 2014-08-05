package com.tencent.djcity.home;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.lib.model.BaseModel;

public class BannerInfo extends BaseModel {
	public static final String MODULE_INNER_LINK = "h5";
	public static final String MODULE_ITEM = "item";
	
	String  mImage="";//banner图片
	String    mType;
	String    mTargetId;//用于指定的商品
	String  mLinkUrl="";//外部链接

	public BannerInfo() 
	{
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject pObject = new JSONObject();
		pObject.put("picUrl", mImage);
		pObject.put("type", mType);
		pObject.put("productId", mTargetId);
		pObject.put("link", mLinkUrl);
		
		return pObject;
	}
	
	public void parse(JSONObject aObject) {
		mImage = aObject.optString("image");
		mType = aObject.optString("type");
		mLinkUrl = aObject.optString("url");
		mTargetId = aObject.optString("targetId");
	}
	
	/**
	 * @return
	 */
	public ModuleInfo toEvent() {
		ModuleInfo pInfo = new ModuleInfo();
//		pInfo.mEvent = (int)mEventId;
//		pInfo.mChannelId = mTemplateId;
//		pInfo.mPicUrl = mImage;
//		pInfo.mParams = mTitle;
//		pInfo.mLinkUrl = mLinkUrl;
//		pInfo.mProductId = mTargetId;
//		pInfo.mModule = mType;
//		pInfo.mChannelId = mChannelId;
		
		return pInfo;
	}
	
	/**
	 * getPicUrl
	 * @return
	 */
	public String getPicUrl()
	{
		return mImage;
	}
	
	public String getTargetId() {
		return mTargetId;
	}
	
	public String getBannerType() {
		return mType;
	}

	public String getLinkURL() {
		return mLinkUrl;
	}
	
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof BannerInfo)
        {
        	if(((BannerInfo) object).mImage.equals(this.mImage) &&
            	((BannerInfo) object).mLinkUrl.equals(this.mLinkUrl) &&
            	
            	((BannerInfo) object).mType == this.mType &&
            	((BannerInfo) object).mTargetId == this.mTargetId)
            	return true;
        }
        
        return false;
    }
	
	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + getStringHashCode(mImage);
		result += 37 * result + getStringHashCode(mLinkUrl);
		result += 37 * result + getStringHashCode(mType);
		result += 37 * result + getStringHashCode(mTargetId);
		
		return result ;
		
	}
	
	public int getStringHashCode(String str)
	{
		return null != str ? str.hashCode() : 0;
		
	}
}
