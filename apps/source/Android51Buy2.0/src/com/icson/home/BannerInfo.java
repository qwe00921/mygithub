package com.icson.home;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class BannerInfo extends BaseModel {
	String  mPicUrl="";//banner图片
	int    	mType;
	long    mEventId;//用于商品运营馆
	int     mTemplateId;//用于商品运营馆
	long    mProductId;//用于指定的商品
	int    	mChannelId;//用于指定多价的商品
	String  mTitle=""; //指定标题
	String  mLinkUrl="";//外部链接

	public BannerInfo() 
	{
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject pObject = new JSONObject();
		pObject.put("picUrl", mPicUrl);
		pObject.put("type", mType);
		pObject.put("event", mEventId);
		pObject.put("template", mTemplateId);
		pObject.put("productId", mProductId);
		pObject.put("link", mLinkUrl);
		pObject.put("title", mTitle);
		pObject.put("chId", mChannelId);
		
		return pObject;
	}
	
	public void parse(JSONObject aObject) {
		mPicUrl = aObject.optString("picUrl");
		mType = aObject.has("type") ? aObject.optInt("type") : aObject.optInt("mod");
		mEventId = aObject.optLong("event",0);
		mTemplateId = aObject.optInt("template",0);
		mProductId = aObject.optLong("productId", 0);
		mLinkUrl = aObject.has("link") ? aObject.optString("link") : aObject.optString("linkUrl", "http://m.51buy.com");
		mTitle = aObject.has("title") ? aObject.optString("title", "") : aObject.optString("name");
		mChannelId = aObject.optInt("chId", 0);
	}
	
	/**
	 * @return
	 */
	public ModuleInfo toEvent() {
		ModuleInfo pInfo = new ModuleInfo();
		pInfo.mEvent = (int)mEventId;
		pInfo.mChannelId = mTemplateId;
		pInfo.mPicUrl = mPicUrl;
		pInfo.mParams = mTitle;
		pInfo.mLinkUrl = mLinkUrl;
		pInfo.mProductId = mProductId;
		pInfo.mModule = mType;
		pInfo.mChannelId = mChannelId;
		
		return pInfo;
	}
	
	/**
	 * getPicUrl
	 * @return
	 */
	public String getPicUrl()
	{
		return mPicUrl;
	}
	
	/**
	 * getEventId
	 * @return
	 */
	public long getEventId()
	{
		return mEventId;
	}
	
	public long getProductId() {
		return mProductId;
	}

	public int getChannelId() {
		return mChannelId;
	}
	
	public int getBannerType() {
		return mType;
	}

	public int getTemplateID() {
		return mTemplateId;
	}

	public String getLinkURL() {
		return mLinkUrl;
	}
	
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof BannerInfo)
        {
        	if(((BannerInfo) object).mPicUrl.equals(this.mPicUrl) &&
            	((BannerInfo) object).mLinkUrl.equals(this.mLinkUrl) &&
            	((BannerInfo) object).mTitle.equals(this.mTitle) &&
            	
            	((BannerInfo) object).mType == this.mType &&
            	((BannerInfo) object).mEventId == this.mEventId &&
            	((BannerInfo) object).mTemplateId == this.mTemplateId &&
            	((BannerInfo) object).mProductId == this.mProductId &&
            	((BannerInfo) object).mChannelId == this.mChannelId)
            	return true;
        }
        
        return false;
    }
	
	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + getStringHashCode(mPicUrl);
		result += 37 * result + getStringHashCode(mLinkUrl);
		result += 37 * result + getStringHashCode(mTitle);
		result += 37 * result + mType;
		result += 37 * result + (int)(mEventId^(mEventId >>> 32));
		result += 37 * result + mTemplateId;
		result += 37 * result + (int)(mProductId ^ (mProductId >>> 32));
		result += 37 * result + mChannelId;
		
		return result ;
		
	}
	
	public int getStringHashCode(String str)
	{
		return null != str ? str.hashCode() : 0;
		
	}
}
