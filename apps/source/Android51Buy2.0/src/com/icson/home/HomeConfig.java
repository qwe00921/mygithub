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

package com.icson.home;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.icson.R;
import com.icson.lib.inc.DispatchFactory;
import com.icson.util.IcsonApplication;

public class HomeConfig {
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
	ModuleInfo getBannerInfo(int nPos) {
		final int nSize = null != mBanners ? mBanners.size() : 0;
		if( 0 > nPos || nPos >= nSize )
			return null;
		
		BannerInfo pInfo = mBanners.get(nPos);
		return pInfo.toEvent();
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
			JSONObject pConfig = aRoot.optJSONObject("config");
			if( null != pConfig ) {
				mBgUrl = pConfig.optString("BgUrl");
				mVersion = pConfig.optInt("version");
				mChannel = pConfig.optString("channel");
				mColor = pConfig.optInt("captionColor");
				mCityId = pConfig.optInt("cityId", DispatchFactory.PROVINCE_IPID_SH);
				mSiteId = pConfig.optInt("siteId", DispatchFactory.SITE_SH);
				mLotteryUrl = pConfig.optString("lottery", "http://518.qq.com/go.xhtml?id=6");
				mFooter = new String[MAX_AD_NUM];
				JSONArray aArray = pConfig.optJSONArray("footer");
				final int nLength = (null != aArray ? aArray.length() : 0);
				if( nLength != MAX_AD_NUM ) {
					Context pContext = IcsonApplication.app.getApplicationContext();
					mFooter[0] = pContext.getString(R.string.deliver_free);
					mFooter[1] = pContext.getString(R.string.three_times_a_day);
					mFooter[2] = pContext.getString(R.string.pay_on_arrival);
				} else {
					for( int nIdx = 0; nIdx < nLength; nIdx++ ) {
						mFooter[nIdx] = aArray.getString(nIdx);
					}
				}
			}
			
			// 2. Parse data->banners
			JSONArray aBanners = aRoot.optJSONArray("banners");
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
			
			// 3. Parse data->announce
			JSONObject pAnnounce = aRoot.optJSONObject("announce");
			if( null != pAnnounce ) {
				mAnnounce = new Announce();
				mAnnounce.parse(pAnnounce);
			}
			
			// 4. Parse data->newInfo
			JSONObject pNewInfo = aRoot.optJSONObject("newInfo");
			if( null != pNewInfo ) {
				mNewInfo = ModuleInfo.fromJson(pNewInfo);
			}
			
			// 5. Parse timebuy
			JSONObject pTimeBuy = aRoot.optJSONObject("timebuy");
			if( null != pTimeBuy ) {
				mTimebuyCaption = pTimeBuy.optString("caption");
				JSONObject eQiang = pTimeBuy.optJSONObject("eQiang");
				if( null != eQiang ) {
					mTimebuyUpdated = eQiang.optBoolean("isUpdate");
					mTimebuyVersion = eQiang.optInt("version");
					mBeginSecs = eQiang.optLong("begin");
					mCurrentSecs = eQiang.optLong("now");
					mEndSecs = eQiang.optLong("end");
					
					// Parse information.
					JSONArray aProducts = eQiang.optJSONArray("products");
					mProducts = HomeConfig.getProductList(aProducts);
				}
				
				// Parse for timebuy channels.
				JSONArray aChannels = pTimeBuy.optJSONArray("channels");
				mChannels = HomeConfig.getEventList(aChannels);
			}
			
			// 6. Parse data->events
			JSONObject pEvents = aRoot.optJSONObject("events");
			if( null != pEvents ) {
				mEventCaption = pEvents.optString("caption");
				JSONArray aEvents = pEvents.optJSONArray("list");
				mEvents = HomeConfig.getEventList(aEvents);
			}
			
			// 7. Parse recommend list.
			JSONArray aRecommend = aRoot.optJSONArray("recommend");
			nSize = (null != aRecommend ? aRecommend.length() : 0);
			if( nSize > 0 ) {
				mRecommend = new ArrayList<ModuleInfo>();
				for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
					JSONObject pChild = aRecommend.getJSONObject(nIdx);
					ModuleInfo pInfo = ModuleInfo.fromJson(pChild);
					if( null != pInfo ) {
						mRecommend.add(pInfo);
					}
				}
			}
			
			//8. Main_pic
			JSONObject pMainImg = aRoot.optJSONObject("main_pic");
			if( null != pMainImg ) {
				mHomeBgPicUrl = pMainImg.optString("pic_url");
				mHomeBgPicStartTime = pMainImg.optLong("start_time");
				mHomeBgPicExpireTime = pMainImg.optLong("end_time");
				}
			
			//9. Recharge promotion icon
			JSONObject pRechargePromo = aRoot.getJSONObject("pay_phone_cfg");
			if(null != pRechargePromo) {
				mRechargeIconUrl = pRechargePromo.optString("pic_url", "");
				mRechargeStartTime = pRechargePromo.optLong("start_time", 0);
				mRechargeExpireTime = pRechargePromo.optLong("end_time", 0);
			}
			
			//10. get user guide info
			showUserGuide = aRoot.optInt("isPlan", 1);
			userGuideDuriation = aRoot.optInt("duriation", 5) * 1000;
			
			//11. GuangGuang   stay with mRecommend but in front
			
			JSONObject aGuang = aRoot.optJSONObject("guang");
			if(null!=aGuang)
			{
				if(null == mRecommend)
					mRecommend = new ArrayList<ModuleInfo>();
				ModuleInfo pInfo = ModuleInfo.fromJson(aGuang);
				//pInfo.mLinkUrl = "http://wg.yixun.com/touch_v2/view/wx/guang/i_android.html";
				if( null != pInfo ) 
					mRecommend.add(0,pInfo);
			}
			
			
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
	private static List<ProductInfo> getProductList(JSONArray aArray) throws JSONException {
		final int nSize = null != aArray ? aArray.length() : 0;
		if( 0 >= nSize )
			return null;
		
		List<ProductInfo> aList = new ArrayList<ProductInfo>();
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			JSONObject pObject = aArray.getJSONObject(nIdx);
			ProductInfo pEntity = ProductInfo.fromJson(pObject);
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
	String mBgUrl;
	int    mVersion;
	String mChannel;
	int    mColor; // Caption color
	public int    mCityId; // City id.
	public int    mSiteId; // Site id.
	String mLotteryUrl; // Url for lottery.
	String mFooter[]; //  
	
	// data->banners
	List<BannerInfo> mBanners;
	
	// data->announce
	Announce mAnnounce;
	
	// data->newInfo
	ModuleInfo mNewInfo;
	
	// data->timebuy
	String  mTimebuyCaption;
	int     mTimebuyVersion;
	boolean mTimebuyUpdated;
	long    mBeginSecs;
	long    mCurrentSecs;
	long    mEndSecs;
	List<ProductInfo> mProducts;
	List<ModuleInfo> mChannels;
	
	// data->events
	String mEventCaption;
	public List<ModuleInfo> mEvents;
	
	// Recommend list
	List<ModuleInfo> mRecommend;
	
		
	//Main_pic
	String mHomeBgPicUrl;
	long   mHomeBgPicStartTime;
	long   mHomeBgPicExpireTime;
	
	//Recharge promotion icon
	String mRechargeIconUrl;
	long mRechargeStartTime;
	long mRechargeExpireTime;
	
	int showUserGuide;
	int	userGuideDuriation;
	
	public final static int   MAX_AD_NUM  = 3;
	public final static int   EVENT_GUANGGUANG = 3;
	
}
