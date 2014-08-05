package com.icson.lib.inc;

import java.util.ArrayList;

import android.text.TextUtils;

import com.icson.home.DispatchesParser;
import com.icson.lib.IPageCache;

public class DispatchFactory {

	private static ArrayList<DispatchItem> dataSource = new ArrayList<DispatchItem>();

	public static final int PROVINCE_IPID_SH = 31;
	public static final int PROVINCE_ID_SH = 2621;
	public static final String PROVINCE_NAME_SH = "上海";
	public static final int CITY_ID_SH = 2622;
	public static final String CITY_NAME_SH = "上海市";
	public static final int DISTRICT_ID_SH = 2626;
	public static final String DISTRICT_NAME_SH = "徐汇区";
	// Configuration for site id.
	public static final int SITE_SH  = 1;
	public static final int SITE_SZ  = 1001;
	public static final int SITE_BJ  = 2001;
	public static final int SITE_WH  = 3001;
	public static final int SITE_CQ  = 4001;
	private static final int SITE_XA  = 5001; // 5001 for Xi'An
	
	// Load default configuration.
	public static void loadDefault()
	{
		dataSource.clear();
		dataSource.add(new DispatchItem(34, "安徽", SITE_SH, 3, 1, 2));
		dataSource.add(new DispatchItem(11, "北京", SITE_BJ, 3792, 131, 3769));
		dataSource.add(new DispatchItem(50, "重庆", SITE_CQ, 182, 158, 159));
		dataSource.add(new DispatchItem(35, "福建", SITE_SZ, 5150, 201, 202));
		dataSource.add(new DispatchItem(62, "甘肃", SITE_XA, 5763, 299, 300));
		dataSource.add(new DispatchItem(44, "广东", SITE_SZ, 421, 403, 420));
		dataSource.add(new DispatchItem(45, "广西", SITE_SZ, 601, 556, 600));
		dataSource.add(new DispatchItem(52, "贵州", SITE_CQ, 695, 693, 694));
		dataSource.add(new DispatchItem(46, "海南", SITE_SZ, 5537, 789, 790));
		dataSource.add(new DispatchItem(13, "河北", SITE_BJ, 816, 814, 815));
		dataSource.add(new DispatchItem(23, "黑龙江", SITE_BJ, 1001, 999, 1000));
		dataSource.add(new DispatchItem(41, "河南", SITE_WH, 3490, 1144, 1145));
		dataSource.add(new DispatchItem(42, "湖北", SITE_WH, 1325, 1323, 1324));
		dataSource.add(new DispatchItem(43, "湖南", SITE_WH, 5162, 1454, 1455));
		dataSource.add(new DispatchItem(32, "江苏", SITE_SH, 1601, 1591, 1592));
		dataSource.add(new DispatchItem(36, "江西", SITE_WH, 1720, 1718, 1719));
		dataSource.add(new DispatchItem(22, "吉林", SITE_BJ, 1832, 1830, 1831));
		dataSource.add(new DispatchItem(21, "辽宁", SITE_BJ, 5671, 1900, 1901));
		dataSource.add(new DispatchItem(15, "内蒙古", SITE_BJ, 2018, 2016, 2017));
		dataSource.add(new DispatchItem(64, "宁夏", SITE_XA, 2132,2130, 2131));
		dataSource.add(new DispatchItem(63, "青海", SITE_XA, 2162, 2160, 2161));
		dataSource.add(new DispatchItem(61, "陕西", SITE_XA, 5053, 2212, 2213));
		dataSource.add(new DispatchItem(37, "山东", SITE_BJ, 5870, 2329, 2330));
		dataSource.add(new DispatchItem(14, "山西", SITE_BJ, 2492, 2490, 2491));
		dataSource.add(new DispatchItem(31, "上海", SITE_SH, 2626, 2621, 2622));
		dataSource.add(new DispatchItem(51, "四川", SITE_CQ, 2674, 2652, 2653));
		dataSource.add(new DispatchItem(12, "天津", SITE_BJ, 2860, 2858, 2859));
		dataSource.add(new DispatchItem(65, "新疆", SITE_XA, 2880, 2878, 2879));
		dataSource.add(new DispatchItem(54, "西藏", SITE_CQ, 2998, 2996, 2997));
		dataSource.add(new DispatchItem(53, "云南", SITE_CQ, 3560, 3077, 3078));
		dataSource.add(new DispatchItem(33, "浙江", SITE_SH, 3227, 3225, 3226));
	}
	
	public static void addItem(DispatchItem pItem)
	{
		dataSource.add(pItem);
	}
	
	/*
	 * loadDispatch()
	 * 加载省份
	 * added by marcoyao
	 * 2013.11.7
	 */
	public static void loadDispatch()
	{
		IPageCache mPageCache = new IPageCache();
		String strContent = mPageCache.getNoDelete(CacheKeyFactory.CACHE_DISPATCHES_INFO);
		if(null != strContent)
		{
			//存在缓存信息，解析之
			try{
				DispatchesParser dispatchParser = new DispatchesParser();
				ArrayList<DispatchItem> pItems = dispatchParser.parse(strContent);
				addItems(pItems);
			}
			catch (Exception e) {
				// 解析本地缓存异常时加载默认
				loadDefault();
			}
		}
		else {
			//若不存在缓存信息执行默认
			loadDefault();
		}
	}
	
	/*
	 * add items
	 * @param pItems
	 */
	public static void addItems(ArrayList<DispatchItem> pItems)
	{
		int nSize = ( pItems == null ) ? 0 : pItems.size();
		if( nSize == 0)
		{
			return;
		}
		
		dataSource.clear();
		for ( int nIdx = 0; nIdx < nSize; nIdx++ )
		{
			DispatchItem pItem = pItems.get( nIdx );
			dataSource.add( pItem );
		}
	}
	
	public static void clearItems()
	{
		dataSource.clear();
	}

	public static ArrayList<DispatchItem> getDataSource() {
		return dataSource;
	}

	/**
	 * 
	* method Name:setDefaultCityId    
	* method Description:  Must in dataSource
	* (34, "安徽", SITE_SH,9));
	* (11, "北京", SITE_BJ,3803));
	* (50, "重庆", SITE_CQ,200));
	* (35, "福建", SITE_SZ,203));
	* (62, "甘肃", SITE_BJ,302));
	* (44, "广东", SITE_SZ,421));
	* (45, "广西", SITE_SZ,601));
	* (52, "贵州", SITE_BJ,695));
	* (46, "海南", SITE_SZ,791));
	* (13, "河北", SITE_BJ,816));
	* (23, "黑龙江", SITE_BJ,1001));
	* (41, "河南", SITE_BJ,1146));
	* (42, "湖北", SITE_WH,4046));
	* (43, "湖南", SITE_BJ,1456));
	* (32, "江苏", SITE_SH,1593));
	* (36, "江西", SITE_SZ,1720));
	* (22, "吉林", SITE_BJ,1832));
	* (21, "辽宁", SITE_BJ,1902));
	* (15, "内蒙古", SITE_BJ,2018));
	* (64, "宁夏", SITE_SH,2132));
	* (63, "青海", SITE_SH,2162));
	* (61, "陕西", SITE_BJ,2226));
	* (37, "山东", SITE_BJ,2331));
	* (14, "山西", SITE_BJ,2492));
	* (31, "上海", SITE_SH,2626));
	* (51, "四川", SITE_CQ,2654));
	* (12, "天津", SITE_BJ,2860));
	* (65, "新疆", SITE_SZ,2880));
	* (54, "西藏", SITE_SZ,2998));
	* (53, "云南", SITE_SZ,3079));
	* (33, "浙江", SITE_SH,3227));
	* 
	* @param cityId   
	* void  
	* @exception   
	* @since  1.0.0
	 */
//	private static void setDefaultCityId(int cityId) {
//		DispatchItem current = null;
//		for (DispatchItem item : dataSource) {
//			if (item.id == cityId) {
//				current = item;
//			}
//		}
//
//		if (current != null) {
//			mDefaultCityId = current.id;
//			IPageCache cache = new IPageCache();
//			cache.set(CacheKeyFactory.CACHE_CITY_ID, current.id + "", 0);
//			ILogin.setsiteId(current.siteId);
//		}
//	}

	/**
	 * 
	* method Name:getDefaultCityId    
	* method Description: if city not set-->Shanghai 31  
	* @return   
	* int  
	* @exception   
	* @since  1.0.0
	 */
//	private static int getDefaultCityId() {
//		if( mDefaultCityId != 0 ){
//			return mDefaultCityId;
//		}
//		
//		IPageCache cache = new IPageCache();
//		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);
//		
//		mDefaultCityId = id != null ? Integer.valueOf( id ) : DispatchFactory.PROVINCE_IPID_SH;
//		
//		return mDefaultCityId;
//	}

//	private static String getDefaultCityName() {
//		int id = FullDistrictHelper.getProvinceIPId();
//
//		for (DispatchItem item : dataSource) {
//			if (id == item.id) {
//				return item.name;
//			}
//		}
//
//		return null;
//
//	}
//	private static int getDefaultDistrict() {
//		int id = getDefaultCityId();
//
//		for (DispatchItem item : dataSource) {
//			if (id == item.id) {
//				return item.district;
//			}
//		}
//
//		return 0;
//
//	}
	
//	private static void setDefaultCityId(String strName) {
//		DispatchItem current = null;
//		for (DispatchItem item : dataSource) {
//			if (strName.contains(item.name)) {
//				current = item;
//			}
//		}
//
//		if (current != null) {
//			mDefaultCityId = current.id;
//			IPageCache cache = new IPageCache();
//			cache.set(CacheKeyFactory.CACHE_CITY_ID, current.id + "", 0);
//			ILogin.setsiteId(current.siteId);
//		}
//	}
	
	/**
	 * getSiteId
	 * @param nProviceId
	 * @return
	 */
	public static int getSiteId(String strProvince)
	{
		if(TextUtils.isEmpty(strProvince)) {
			return 0;
		}
		
		DispatchItem current = null;
		for (DispatchItem item : dataSource) 
		{
			if( strProvince.contains(item.name) )
			{
				current = item;
			}
		}
		
		return (null != current ? current.siteId : 0);
	}

	public static class DispatchItem {
		public String name;
		public int id;		//ip_id
		public int siteId;
		public int district;
		public int provinceId;
		public int cityId;

		public DispatchItem(int id, String name, int siteId, int district, int provinceid, int cityid) {
			this.name = name;
			this.id = id;
			this.siteId = siteId;
			this.district = district;
			this.provinceId = provinceid;
			this.cityId = cityid;
		}
	}
}
