package com.icson.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;

import android.content.res.Resources;
import android.text.TextUtils;

import com.icson.R;
import com.icson.home.FullDistrictModel;
import com.icson.home.ProvinceModel;
import com.icson.home.FullDistrictParser;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;

public class IShippingArea {
	private static final String LOG_TAG = IShippingArea.class.getName();
	private static ArrayList<ProvinceModel> areaModels;
	private static FullDistrictParser parser;

	public static ArrayList<ProvinceModel> getAreaModels() {
		if (null != areaModels) {
			return areaModels;
		}

		IPageCache cache = new IPageCache();
		String str = cache.get(CacheKeyFactory.CACHE_FULL_DISTRICT);
		if (!TextUtils.isEmpty(str)) {
			try{
				if(null == parser)
					parser = new FullDistrictParser();
				FullDistrictModel model  = parser.parse(str);
				areaModels = model.getProvinceModels();
			}catch (JSONException ex) {
				Log.e(LOG_TAG, ex);
				areaModels = null;
			}finally
			{
				parser = null;
			}
			
		}
			
		if(null == areaModels) {
			readFromRawFile();
		}

		return areaModels;
	}
	
	
	public static void setAreaModel(ArrayList<ProvinceModel> models) {
		if(null == models) {
			return;
		}
		
		areaModels = models;
	}
	
	private static void readFromRawFile(){
		Resources pResources = IcsonApplication.app.getResources();
		InputStream pInputStream = pResources.openRawResource(R.raw.fulldistrict);
		
		try {
			byte[] aBytes = new byte[pInputStream.available()];
			pInputStream.read(aBytes);
			String strRaw = new String(aBytes);
			if (strRaw != null && !strRaw.equals("")) {
				if(null == parser)
					parser = new FullDistrictParser();
				FullDistrictModel model  = parser.parse(strRaw);
				
				if(parser.isSuccess()) {
					areaModels = model.getProvinceModels();
					
					IPageCache cache = new IPageCache();
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT, parser.getData(), 0);
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT_MD5, model.getMD5Value(), 0);
				}
			}
		} catch (IOException ex) {
			Log.e(LOG_TAG, ex);
			areaModels = null;
		} catch (JSONException ex) {
			Log.e(LOG_TAG, ex);
			areaModels = null;
		} finally {
			if(null != pInputStream) {
				try {
					pInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				pInputStream = null;
				
				parser = null;
			}
		}
		
	}
	
	/*
	 * 
	 */
	public static void clean()
	{
		if(null!=areaModels)
			areaModels.clear();
		areaModels = null;
		parser = null;
	}
}
