package com.icson.amap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.amap.api.maps.model.LatLng;
import com.icson.lib.IPageCache;
/**
 * 
 * @author xingyao
 *
 */
public class CargoRedressCache {

	public  static final String  CARGO_REDRESS_KEY = "cargo_redress_cache";
	private static ArrayList<AddressLatLng> mCache;
	private static final int              CACHE_MAX_SIZE = 16;
	public  static long     mExpireHour;
	/**
	 * 
	 * @param aAddress
	 * @param aLL
	 */
	public static void setAddressLatLng(String aAddress, LatLng aLL)
	{
		if(null != mCache)
		{
			AddressLatLng item = null;
			Iterator<AddressLatLng> it = mCache.iterator();
			while(it.hasNext())
			{
				item = it.next();
				if(item.mAddr.equals(aAddress))
				{
					item.mAddr = aAddress;
					item.mLatLng = aLL;
					item.mTimeMark =  System.currentTimeMillis();
					return;
				}
			}
			item = new AddressLatLng();
			item.mAddr = aAddress;
			item.mLatLng = aLL;
			item.mTimeMark =  System.currentTimeMillis();
			mCache.add(item);
			if(mCache.size() >CACHE_MAX_SIZE)
			{
				mCache.remove(0);
			}
		}
	}
	
	/**
	 * 
	 */
	public static void init(long expireHour)
	{
		mExpireHour = expireHour;
		loadCaches();
		
	}
	
	/**
	 * load from cache as jsonarray
	 */
	private static void loadCaches() {
		if(mExpireHour <= 0)
			return;
		if(null == mCache)
			mCache = new ArrayList<AddressLatLng>();
		IPageCache cache = new IPageCache();
		String content = cache.get(CARGO_REDRESS_KEY);
		if(!TextUtils.isEmpty(content))
		{
			try {
				JSONArray pArray = new JSONArray(content);
				
				mCache.clear();
				long  curTime = System.currentTimeMillis();
				for(int id = 0; id < pArray.length(); id++)
				{
					AddressLatLng item =  new AddressLatLng();
					item.parse(pArray.getJSONObject(id));
					if(item.mTimeMark + mExpireHour * 3600 * 1000 > curTime)
						mCache.add(item);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(mCache, new Comparator<AddressLatLng>(){
			@Override
			public int compare(AddressLatLng lhs, AddressLatLng rhs) {
				return (int) (lhs.mTimeMark - rhs.mTimeMark);
			}
		});
	}
	
	public static void cleanUp()
	{
		saveCaches();
		
		if(null!=mCache)
			mCache.clear();
		mCache = null;
	}
	/**
	 * save as  jsonarray.toString
	 */
	private static void saveCaches()
	{
		if(null == mCache  || mCache.size()<=0)
			return;
		JSONArray pArray = new JSONArray();
		long curTime =  System.currentTimeMillis();
		for(AddressLatLng aitem : mCache)
		{
			if(aitem.mTimeMark + mExpireHour *3600 * 1000 > curTime)
				pArray.put(aitem.toJson());
		}
		
		IPageCache cache = new IPageCache();
		if(pArray.length() <=0)
			cache.remove(CARGO_REDRESS_KEY);
		else
			cache.set(CARGO_REDRESS_KEY, pArray.toString(), 0);
		
	}


	/**
	 * 
	 * @param aAddress
	 * @return
	 */
	public static LatLng getRedressLatLng(String aAddress)
	{
		if(null == mCache || mCache.size() <=0 || mExpireHour <=0)
			return null;
		
		long curTime =  System.currentTimeMillis();
		Iterator<AddressLatLng> it = mCache.iterator();
		while(it.hasNext())
		{
			AddressLatLng item = it.next();
			if(item.mTimeMark + mExpireHour *3600 * 1000 <= curTime)
			{	
				it.remove();
				continue;
			}
			if(item.mAddr.equals(aAddress))
				return item.mLatLng;
		}
		return null;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @author xingyao
	 *
	 */
	public static class AddressLatLng
	{
		public String   mAddr;
		public LatLng   mLatLng;
		public long     mTimeMark;
		
		public AddressLatLng()
		{
			mAddr = "";
			mTimeMark = 0;
		}
		
		public JSONObject toJson(){
			JSONObject pObject = new JSONObject();
			try {
				pObject.put("address", mAddr);
				pObject.put("time", mTimeMark);
				pObject.put("lat", mLatLng.latitude);
				pObject.put("lng", mLatLng.longitude);
				
			} catch (JSONException e) {
				e.printStackTrace();
				pObject = null;
			}
			
			return pObject;
		}
		
		public void parse(JSONObject aObject) {
			mAddr = aObject.optString("address");
			mTimeMark = aObject.optLong("time");
			double lat = aObject.optDouble("lat");
			double lng = aObject.optDouble("lng");
		
			mLatLng = null;
			mLatLng = new LatLng(lat,lng);
		}
		
		@Override
		public boolean equals(Object object)
	    {
			if(object instanceof AddressLatLng)
			{
				
				return ((AddressLatLng) object).mAddr.equals(this.mAddr);
			}
			else
				return false;
	    }
		
		@Override
		public int hashCode() {
			int result = 17;
			result += 37 * result + (null != mAddr ? mAddr.hashCode() : 0);
			result += 37 * result + (int)(mTimeMark ^ (mTimeMark >>> 32));
			return result;
		}
		
	}
	
}


