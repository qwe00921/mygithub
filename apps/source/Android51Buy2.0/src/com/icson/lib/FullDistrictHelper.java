package com.icson.lib;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.model.AreaPackageModel;

public class FullDistrictHelper {
	public final static String SITE_ID 		= "site_id";
	public final static String PROVINCE_ID 	= "province_id";
	public final static String PROVINCE_IP_ID = "province_ip_id";
	public final static String PROVINCE_NAME 	= "province_name";
	public final static String CITY_ID 		= "city_id";
	public final static String CITY_NAME 		= "city_name";
	public final static String DISTRICT_ID 	= "district_id";
	public final static String DISTRICT_NAME	= "district_name";
	
	private static FullDistrictItem mDistrictItem;
	
//	public void setFullDistrict(int districtid) {
//		AreaPackageModel mAreaPackageModel = new AreaPackageModel(districtid);
//		
//	}
	public static void clear()
	{
		mDistrictItem = null;
	}
	public static void setFullDistrict(AreaPackageModel pAreaPackageModel) {
		setFullDistrict(pAreaPackageModel, 0);
	}
	
	public static void setFullDistrict(AreaPackageModel pAreaPackageModel, int siteid) {
		if(null == pAreaPackageModel) {
			return ;
		}
		
		if(null == mDistrictItem) {
			mDistrictItem = new FullDistrictItem();
		}
		
		ProvinceModel provinceModel = pAreaPackageModel.getProvinceModel();
		if( null == provinceModel){
			return;
		}else{
			mDistrictItem.mProvinceId = provinceModel.getProvinceId();
			mDistrictItem.mProvinceIPId = provinceModel.getProvinceIPId();
			mDistrictItem.mProvinceName = provinceModel.getProvinceName();
		}
		
		CityModel cityModel = pAreaPackageModel.getCityModel();
		if(null == cityModel) {
			return;
		}else{
			mDistrictItem.mCityId = cityModel.getCityId();
			mDistrictItem.mCityName = cityModel.getCityName();
		}
		
		ZoneModel zoneModel = pAreaPackageModel.getDistrictModel();
		if(null == zoneModel) {
			return;
		}else{
			mDistrictItem.mDistrictId = zoneModel.getZoneId();
			mDistrictItem.mDistrictName = zoneModel.getZoneName();
		}
		
		if( siteid > 0 ) {
			mDistrictItem.mSiteId = siteid;
		}else{
			int newSiteId = DispatchFactory.getSiteId(provinceModel.getProvinceName());
			mDistrictItem.mSiteId = newSiteId;
		}
		
		saveDistictToDB();
	}
	
	public static void setFullDistrict(int provinceid, int cityid, int districtid) {
		setFullDistrict(provinceid, cityid, districtid, 0);
	}
	
	@SuppressWarnings("unused")
	public static void setFullDistrict(int provinceid, int cityid, int districtid, int siteid) {
		if(provinceid <=0 || cityid <= 0 || districtid <= 0) {
			return;
		}
		
		if(null == mDistrictItem) {
			mDistrictItem = new FullDistrictItem();
		}
		
		AreaPackageModel pAreaPackageModel = new AreaPackageModel(provinceid, cityid, districtid);
		if(null == pAreaPackageModel) {
			return ;
		}
		
		ProvinceModel provinceModel = pAreaPackageModel.getProvinceModel();
		if( null == provinceModel){
			return;
		}else{
			mDistrictItem.mProvinceId = provinceModel.getProvinceId();
			mDistrictItem.mProvinceIPId = provinceModel.getProvinceIPId();
			mDistrictItem.mProvinceName = provinceModel.getProvinceName();
		}
		
		CityModel cityModel = pAreaPackageModel.getCityModel();
		if(null == cityModel) {
			
		}else{
			mDistrictItem.mCityId = cityModel.getCityId();
			mDistrictItem.mCityName = cityModel.getCityName();
		}
		
		ZoneModel zoneModel = pAreaPackageModel.getDistrictModel();
		if(null == zoneModel) {
			return;
		}else{
			mDistrictItem.mDistrictId = zoneModel.getZoneId();
			mDistrictItem.mDistrictName = zoneModel.getZoneName();
		}
		
		if( siteid > 0 ) {
			mDistrictItem.mSiteId = siteid;
		}else{
			int newSiteId = DispatchFactory.getSiteId(provinceModel.getProvinceName());
			mDistrictItem.mSiteId = newSiteId;
		}
		
		saveDistictToDB();
	}
	
	
	public static void setFullDistrict(FullDistrictItem pDistrictItem){
		if(null == pDistrictItem || 0 >= pDistrictItem.mProvinceId || 0 >= pDistrictItem.mCityId || 0 >= pDistrictItem.mDistrictId) {
			return;
		}
		
		mDistrictItem = pDistrictItem;
		saveDistictToDB();
	}
	
	public static FullDistrictItem getFullDistrict() {
		if(null != mDistrictItem) {
			return mDistrictItem;
		}
		
		getDistrictFromDB();
		
		if(null == mDistrictItem) {
			mDistrictItem = new FullDistrictItem(DispatchFactory.PROVINCE_ID_SH, DispatchFactory.PROVINCE_IPID_SH, DispatchFactory.PROVINCE_NAME_SH,
					DispatchFactory.CITY_ID_SH, DispatchFactory.CITY_NAME_SH, DispatchFactory.DISTRICT_ID_SH, DispatchFactory.DISTRICT_NAME_SH, DispatchFactory.SITE_SH);
		}
		
		return mDistrictItem;
	}
	
	
	public static int getCityId(){
		mDistrictItem = getFullDistrict();
		if(null == mDistrictItem) {
			return DispatchFactory.CITY_ID_SH;
		}
		
		return mDistrictItem.mCityId;
	}
	
	public static int getSiteId(){
		mDistrictItem = getFullDistrict();
		if(null == mDistrictItem) {
			return DispatchFactory.SITE_SH;
		}
		
		return mDistrictItem.mSiteId;
	}
	
	
	public static int getProvinceIPId(){
		mDistrictItem = getFullDistrict();
		if(null == mDistrictItem) {
			return DispatchFactory.PROVINCE_IPID_SH;
		}
		
		return mDistrictItem.mProvinceIPId;
	}
	
	public static String getProvinceName(){
		mDistrictItem = getFullDistrict();
		if(null == mDistrictItem) {
			return DispatchFactory.PROVINCE_NAME_SH;
		}
		
		return mDistrictItem.mProvinceName;
	}
	
	public static int getDistrictId(){
		mDistrictItem = getFullDistrict();
		if(null == mDistrictItem) {
			return DispatchFactory.DISTRICT_ID_SH;
		}
		
		return mDistrictItem.mDistrictId;
	}
	
	
	private static void saveDistictToDB(){
		if(null == mDistrictItem) {
			return;
		}
		
		String strData = "";
		JSONObject json = new JSONObject();
		try {
			json.put(SITE_ID, mDistrictItem.mSiteId);
			json.put(PROVINCE_ID, mDistrictItem.mProvinceId);
			json.put(PROVINCE_IP_ID, mDistrictItem.mProvinceIPId);
			json.put(PROVINCE_NAME, mDistrictItem.mProvinceName);
			json.put(CITY_ID, mDistrictItem.mCityId);
			json.put(CITY_NAME, mDistrictItem.mCityName);
			json.put(DISTRICT_ID, mDistrictItem.mDistrictId);
			json.put(DISTRICT_NAME, mDistrictItem.mDistrictName);
			
			strData = json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		IPageCache mPageCache= new IPageCache();
		
		mPageCache.set(CacheKeyFactory.CACHE_CITY_ID, String.valueOf(mDistrictItem.mProvinceIPId), 0);
		mPageCache.set(CacheKeyFactory.CACHE_DEFAULT_ADDRESS_DISTRICT, strData, 0);
	}
	
	private static void getDistrictFromDB(){
		IPageCache mPageCache= new IPageCache();
		
		String strData = mPageCache.get(CacheKeyFactory.CACHE_DEFAULT_ADDRESS_DISTRICT);
		if(TextUtils.isEmpty(strData)) {
			mDistrictItem = null;
			return;
		}
		
		try {
			JSONObject json = new JSONObject(strData);
			if(null == mDistrictItem) {
				mDistrictItem = new FullDistrictItem();
			}
			mDistrictItem.mSiteId = json.optInt(SITE_ID);
			mDistrictItem.mProvinceId = json.optInt(PROVINCE_ID);
			mDistrictItem.mProvinceIPId = json.optInt(PROVINCE_IP_ID);
			mDistrictItem.mProvinceName = json.optString(PROVINCE_NAME);
			mDistrictItem.mCityId = json.optInt(CITY_ID);
			mDistrictItem.mCityName = json.optString(CITY_NAME);
			mDistrictItem.mDistrictId = json.optInt(DISTRICT_ID);
			mDistrictItem.mDistrictName = json.optString(DISTRICT_NAME);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static class FullDistrictItem{
		public int mSiteId;
		public int mProvinceId;
		public int mProvinceIPId;
		public String mProvinceName;
		public int mCityId;
		public String mCityName;
		public int mDistrictId;
		public String mDistrictName;
		
		
		public FullDistrictItem(){
			this.mSiteId = 0;
			this.mProvinceId = 0;
			this.mProvinceIPId = 0;
			this.mProvinceName = null;
			this.mCityId = 0;
			this.mCityName = null;
			this.mDistrictId = 0;
			this.mDistrictName = null;
		}
		
		public FullDistrictItem(int provinceId, int provinceIPId, String provinceName, int cityId, String cityName, int districtId, String districtName, int siteId) {
			this.mProvinceId = provinceId;
			this.mProvinceIPId = provinceIPId;
			this.mProvinceName = provinceName;
			this.mCityId = cityId;
			this.mCityName = cityName;
			this.mDistrictId = districtId;
			this.mDistrictName = districtName;
			this.mSiteId = siteId;
		}
		
		public FullDistrictItem(int provinceId, int provinceIPId, String provinceName, int cityId, String cityName, int districtId, String districtName) {
			this.mProvinceId = provinceId;
			this.mProvinceIPId = provinceIPId;
			this.mProvinceName = provinceName;
			this.mCityId = cityId;
			this.mCityName = cityName;
			this.mDistrictId = districtId;
			this.mDistrictName = districtName;
			
			int newSiteId = DispatchFactory.getSiteId(provinceName);
			this.mSiteId = newSiteId;
		}
		
	}
	

}
