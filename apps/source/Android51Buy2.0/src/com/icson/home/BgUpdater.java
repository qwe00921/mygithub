package com.icson.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.icson.lib.AppStorage;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;

public class BgUpdater {
	
	private static ImageLoader mImgLoader;
	private static ImageLoadListener mImgListener;
	private static Bitmap      mBg;
	private static BitmapDrawable      mBgDrawable;
	private static String      mLastUrl;
	public interface BGListener{
		public void onBgLoaded();
	};
	
	/**
	 * 
	 * @param aContext  HomeActivity.this so wont die,ever
	 * @param imgUrl
	 * @param startTime
	 * @param expireTime
	 * @return
	 */
	public static Bitmap checkVaildBgBitmap(final Context aContext,final String imgUrl, final long startTime, final long expireTime,
			final BGListener bglistener)
	{
		if(TextUtils.isEmpty(imgUrl))
			return null;
		
		final long nCurrentSecond = System.currentTimeMillis()/1000;
		if(nCurrentSecond >= startTime && nCurrentSecond < expireTime && !TextUtils.isEmpty(imgUrl))
		{
			if(null == mImgLoader)
			{
				mImgLoader = new ImageLoader(aContext, Config.PIC_CACHE_DIR, false);
				mImgListener = new ImageLoadListener(){

					@Override
					public void onLoaded(Bitmap aBitmap, String strUrl) {
						if(!mImgLoader.isEmptyBitmap(strUrl))
						{
							mBg = aBitmap;
							AppStorage.setData("bg_url", strUrl, true);
							mLastUrl = strUrl;
							
							if(null!=bglistener)
								bglistener.onBgLoaded();
						}
					}

					@Override
					public void onError(String strUrl) {
						// TODO Auto-generated method stub
						
					}}; 
			}
			
			if(TextUtils.isEmpty(mLastUrl))
			{
				mLastUrl = AppStorage.getData("bg_url");
			}
			//different url. Clear old file cache
			if(!TextUtils.isEmpty(mLastUrl) && !imgUrl.equals(mLastUrl))
			{
				mImgLoader.delFile(mLastUrl);
				mBgDrawable = null;
				if(null!=mBg && !mBg.isRecycled())
					mBg.recycle();
				mBg = null;
			}
			
			//return cache
			if(null != mBg)
				return mBg;
			
			
			
			if(!mImgLoader.isEmptyBitmap(imgUrl))
			{
				mBg = mImgLoader.get(imgUrl,mImgListener);
			}
			else
			{
				if(null!=mBg && !mBg.isRecycled())
					mBg.recycle();
				mBg = null;
			}
			
			
			return mBg;
		}
		else
		{
			if(null!=mBg && !mBg.isRecycled())
				mBg.recycle();
			mBg = null;
			mBgDrawable = null;
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Drawable getBgDrawable()
	{
		if(null == mBgDrawable && null!=mBg)
			mBgDrawable = new BitmapDrawable(mBg);
		return mBgDrawable;
	}
	
}
