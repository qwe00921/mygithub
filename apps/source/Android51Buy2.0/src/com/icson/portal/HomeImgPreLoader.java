package com.icson.portal;

import android.content.Context;

import com.icson.util.Config;
import com.icson.util.ImageLoader;

/**
 * 
*   
* Class Name:SlotVersionParser 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-18 下午06:38:45 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class HomeImgPreLoader {
	
	private static ImageLoader   mWholeLoader;
	
	public static ImageLoader getWholeLoader(Context aContext) {
		if(null==mWholeLoader)
		{
			mWholeLoader = new ImageLoader(aContext, Config.PIC_CACHE_DIR, true);
		}
		return mWholeLoader;
	}
	
	public static void cleanImageLoader()
	{
		if(null!=mWholeLoader)
		{
			mWholeLoader.cleanup();
			mWholeLoader = null;
		}
	}
}	
	
	


