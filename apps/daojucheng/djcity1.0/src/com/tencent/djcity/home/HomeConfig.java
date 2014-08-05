/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: HomeConfig.java
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

import com.tencent.djcity.home.recommend.ProductModel;

public class HomeConfig {
	List<ProductModel> mRecommends;
	List<ProductModel> mNewProducts;
	
	public HomeConfig() {
		
	}
	
	/**
	 * get banners
	 * @return
	 */
	public List<BannerInfo> getBanners() {
		return mBanners;
	}
	
	/**
	 * @param nPos
	 * @return
	 */
	BannerInfo getBannerInfo(int nPos) {
		final int nSize = null != mBanners ? mBanners.size() : 0;
		if( 0 > nPos || nPos >= nSize )
			return null;
		
		BannerInfo pInfo = mBanners.get(nPos);
		return pInfo;
	}
	
	/**
	 * parseConfig
	 * @param aRoot
	 * @return
	 */
	boolean parseConfig(JSONObject aRoot) {
		if( null == aRoot )
			return false;
		
		try {
			// 1. Parse data->config.
//			JSONObject pConfig = aRoot.optJSONObject("config");
//			if( null != pConfig ) {
//				mVersion = pConfig.optInt("version");
//			}
			
			// 2. Parse data->banners
			JSONObject bannerJSON = aRoot.optJSONObject("banner_info");
			bannerSign = bannerJSON.optString("sign");
			JSONArray aBanners = bannerJSON.optJSONArray("list");
			int nSize = null != aBanners ? aBanners.length() : 0;
			if( nSize > 0 ) {
				mBanners = new ArrayList<BannerInfo>();
				for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
					JSONObject pObject = aBanners.getJSONObject(nIdx);
					BannerInfo pInfo = new BannerInfo();
					pInfo.parse(pObject);
					mBanners.add(pInfo);
				}
			}
			
//			// 3. Parse data->announce
//			JSONObject pAnnounce = aRoot.optJSONObject("announce");
//			if( null != pAnnounce ) {
//				mAnnounce = new Announce();
//				mAnnounce.parse(pAnnounce);
//			}
			
//			// 4. Parse data->events
//			JSONObject pEvents = aRoot.optJSONObject("events");
//			if( null != pEvents ) {
//				mEventCaption = pEvents.optString("caption");
//				JSONArray aEvents = pEvents.optJSONArray("list");
//				mEvents = HomeConfig.getEventList(aEvents);
//			}
			
			// 5. Parse timebuy
			// Parse information.
			JSONArray recommendJSON = aRoot.optJSONArray("recommends");
			mRecommends = HomeConfig.getProductList(recommendJSON);
			
			JSONArray newProductJSON = aRoot.optJSONArray("new_products");
			mNewProducts = HomeConfig.getProductList(newProductJSON);
				
		} catch( JSONException aException ) {
			aException.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * @param aArray
	 * @return
	 * @throws JSONException
	 */
	private static List<ProductModel> getProductList(JSONArray aArray) throws JSONException {
		final int nSize = null != aArray ? aArray.length() : 0;
		if( 0 >= nSize )
			return null;
		
		List<ProductModel> aList = new ArrayList<ProductModel>();
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			JSONObject pObject = aArray.getJSONObject(nIdx);
			ProductModel pEntity = ProductModel.fromJson(pObject);
			if( null != pEntity ) {
				aList.add(pEntity);
			}
		}
		
		return aList;
	}
	
	/**
	 * get list.
	 * @param aArray
	 * @return
	 * @throws JSONException 
	 */
	private static List<ModuleInfo> getEventList(JSONArray aArray) throws JSONException {
		final int nSize = null != aArray ? aArray.length() : 0;
		if( 0 >= nSize )
			return null;
		
		List<ModuleInfo> aList = new ArrayList<ModuleInfo>();
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			JSONObject pObject = aArray.getJSONObject(nIdx);
			ModuleInfo pEntity = ModuleInfo.fromJson(pObject);
			if( null != pEntity ) {
				aList.add(pEntity);
			}
		}
		
		return aList;
	}

	
	// data->config
	int    mVersion;
	// data->banners
	List<BannerInfo> mBanners;
	private String bannerSign;
	
	// data->announce
	Announce mAnnounce;
	
	// data->events
	String mEventCaption;
	public List<ModuleInfo> mEvents;
}
