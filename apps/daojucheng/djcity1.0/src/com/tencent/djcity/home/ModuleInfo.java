/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: EventInfo.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 19, 2013
 */

package com.tencent.djcity.home;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class ModuleInfo {
	/**
	 * Function type definition.
	 */
	private static final int MODULE_ID_NONE         = 0;
	public static final int  MODULE_ID_COUPON       = (MODULE_ID_NONE + 1);  //浼樻儬鍒�	
	public static final int  MODULE_ID_LOTTERY      = (MODULE_ID_NONE + 2);  //鎶藉
	public static final int  MODULE_ID_PRODUCT_LIST = (MODULE_ID_NONE + 3);  //鎸囧畾涓�壒鍟嗗搧,鍙互璁剧疆鎵嬫満澶氫环
	public static final int  MODULE_ID_PRODUCT      = (MODULE_ID_NONE + 4);  //鎸囧畾涓�釜鍟嗗搧
	public static final int  MODULE_ID_OUTTER_LINK  = (MODULE_ID_NONE + 5);  //澶栭儴閾炬帴   html5椤甸潰
	public static final int  MODULE_ID_TUANGOU      = (MODULE_ID_NONE + 6);  //鍥㈣喘
	public static final int  MODULE_ID_MORNING      = (MODULE_ID_NONE + 7);  //鏃╁競
	public static final int  MODULE_ID_BLACK        = (MODULE_ID_NONE + 8);  //澶╅粦榛�	
	public static final int  MODULE_ID_WEEKEND      = (MODULE_ID_NONE + 9);  //鍛ㄦ湯娓呬粨
	public static final int  MODULE_ID_RECHARGE     = (MODULE_ID_NONE + 10); // 鍏呭�
	public static final int  MODULE_ID_INNER_LINK   = (MODULE_ID_NONE + 11); // html5鍐呭祵
	public static final int  MODULE_ID_RECOMM       = (MODULE_ID_NONE + 12); // Recommend application list.
	public static final int  MODULE_ID_QIANG        = (MODULE_ID_NONE + 13); //鎶㈣喘
	public static final int  MODULE_ID_EVENT        = (MODULE_ID_NONE + 14); //杩愯惀棣�	
	public static final int  MODULE_ID_MSGCENTER    = (MODULE_ID_NONE + 15); //娑堟伅涓績
	public static final int  MODULE_ID_POPULAR      = (MODULE_ID_NONE + 16); //鐑攢姒�	
	public static final int  MODULE_ID_SLOTMACHINE  = (MODULE_ID_NONE + 17); //澶╁ぉ鎽�	
	public static final int  MODULE_ID_ORDERS       = (MODULE_ID_NONE + 18); //璁㈠崟鍒楄〃
	public static final int  MODULE_ID_MORE         = (MODULE_ID_NONE + 19); //鏇村
	public static final int  MODULE_ID_FEEDBACK     = (MODULE_ID_NONE + 20); //鎰忚鍙嶉
	
	// 鍏煎1.0鐗堟湰
	public static final int  MODULE_ID_VPAY         = (MODULE_ID_NONE + 101); //鍏呭�
	public static final int  MODULE_ID_MESSAGES     = (MODULE_ID_NONE + 102); //娑堟伅涓績
	public static final int  MODULE_ID_QR_RECHARGE  = (MODULE_ID_NONE + 203); //浜岀淮鐮佹壂鎻忚繘鍏呭�椤甸潰
	
	
	/**
	 * Constructor of EventInfo
	 */
	public ModuleInfo() {
	}
	
	public static ModuleInfo fromJson(JSONObject aObject) throws JSONException {
		if( null == aObject )
			return null;
		
		ModuleInfo pEntity = new ModuleInfo();
		
		// Visible attributes
		pEntity.mSubtitle = aObject.optString("subtitle");
		pEntity.mPromotion = aObject.optString("promotion");
		pEntity.mHint = aObject.optString("hint");
		pEntity.mPicUrl = aObject.optString("picUrl");
		pEntity.mTag = aObject.optString("tag", "");
		pEntity.mProductId = aObject.optLong("productId", 0);
		
		// Action attributes.
		pEntity.mLinkUrl = aObject.optString("linkUrl");
		pEntity.mParams = aObject.optString("params");
		pEntity.mEvent = aObject.optInt("event");
		pEntity.mTemplate = aObject.optInt("template");
		pEntity.mChannelId = aObject.optInt("chId");
		
		// Update mod information.
		pEntity.mModule = aObject.optInt("mod", TextUtils.isEmpty(pEntity.mLinkUrl) ? MODULE_ID_EVENT : MODULE_ID_INNER_LINK);
		
		// Sub-items.
		JSONArray aItems = aObject.optJSONArray("products");
		final int nSize = null != aItems ? aItems.length() : 0;
		if( nSize > 0 ) {
			pEntity.mItems = new ArrayList<ProductInfo>();
			for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
				ProductInfo entity = ProductInfo.fromJson(aItems.getJSONObject(nIdx));
				if( null != entity ) {
					pEntity.mItems.add(entity);
				}
			}
		}
		
		// Check there is a single product attached to the item.
		JSONObject pProduct = aObject.optJSONObject("product");
		pEntity.mProduct = ProductInfo.fromJson(pProduct);
		
		return pEntity;
	}
	
	@Override
    public boolean equals(Object object)
    {
		if (object != null && object instanceof ModuleInfo)
        {
        	//one is not null
        	if(( null == ((ModuleInfo) object).mProduct && null != mProduct ) ||
        		( null != ((ModuleInfo) object).mProduct && null == mProduct ) ||
        		( null == ((ModuleInfo) object).mItems && null != mItems ) ||
        		( null != ((ModuleInfo) object).mItems && null == mItems ) )
        		return false;
        	
        	if(null!= mProduct &&  !mProduct.equals(((ModuleInfo) object).mProduct) )
        		return false;
        	
        	if(null!= mItems &&  !listEqual(((ModuleInfo) object).mItems))
        		return false;
        	
        	if(((ModuleInfo) object).mSubtitle.equals(this.mSubtitle) &&
            	((ModuleInfo) object).mPromotion.equals(this.mPromotion) &&
            	((ModuleInfo) object).mHint.equals(this.mHint) &&
            	((ModuleInfo) object).mPicUrl.equals(this.mPicUrl) &&
            	((ModuleInfo) object).mLinkUrl.equals(this.mLinkUrl) &&
            	((ModuleInfo) object).mParams.equals(this.mParams) &&
            	((ModuleInfo) object).mTag.equals(this.mTag) &&
            	((ModuleInfo) object).mModule == mModule &&
            	((ModuleInfo) object).mProductId ==  mProductId &&
            	((ModuleInfo) object).mChannelId ==  mChannelId &&
            	((ModuleInfo) object).mEvent ==  mEvent &&
            	((ModuleInfo) object).mTemplate ==  mTemplate)
        	
        		return true;
        }
        
        return false;
    }
	
	@Override
	public int hashCode() {
		int result = 17;
		result += 37*result + getStringHashCode(mSubtitle);
		result += 37*result + getStringHashCode(mPromotion);
		result += 37*result + getStringHashCode(mHint);
		result += 37*result + getStringHashCode(mPicUrl);
		result += 37*result + getStringHashCode(mLinkUrl);
		result += 37*result + getStringHashCode(mParams);
		result += 37*result + getStringHashCode(mTag);
		result += 37*result + mModule;
		result += 37*result + (int)(mProductId ^ (mProductId >>> 32));
		result += 37*result + mChannelId;
		result += 37*result + mEvent;
		result += 37*result + mTemplate;
		result += 37*result + (null != mProduct ? mProduct.hashCode() : 0);
		if(mItems != null)
		{
			for(int i = 0;i < mItems.size();i++)
			{
				ProductInfo tmp = mItems.get(i);
				result += 37*result + (null != tmp ? tmp.hashCode() : 0);
			} 
		}
		return result;
		
		
	}
	
	public int getStringHashCode(String str)
	{
		return null != str ? str.hashCode() : 0;
		
	}
	
	/**
	 * 
	* method Name:listEqual    
	* method Description:  
	* @param aItems
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	private boolean listEqual(List<ProductInfo> aItems)
	{
		if(null == aItems)
			return mItems.size()<=0 ? true : false;
		for(ProductInfo item : aItems)
		{
			if(!this.mItems.contains(item))
				return false;
		}
		return true;
	}
	
	String mSubtitle="";
	String mPromotion="";
	String mHint="";
	public String mPicUrl="";
	public String mLinkUrl="";
	public String mParams="";
	String mTag="";
	public int    mModule;    // Function module id.
	long   mProductId; // Product id.
	int    mChannelId; // Channel id.
	int    mEvent; // Event id
	int    mTemplate; // Template id.
	ProductInfo mProduct;
	public List<ProductInfo> mItems;
}
