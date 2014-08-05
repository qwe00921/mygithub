package com.icson.statistics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;

import com.icson.util.Log;

public class StatisticsConfig 
{	
	/**
	 * Priority definition for local statistics.
	 */
	public static final int PRIORITY_FATAL = 0;
	public static final int PRIORITY_ERROR = (PRIORITY_FATAL + 1);
	public static final int PRIORITY_WARN  = (PRIORITY_FATAL + 2);
	public static final int PRIORITY_INFO  = (PRIORITY_FATAL + 3);
	public static final int PRIORITY_DEBUG = (PRIORITY_FATAL + 4);
	
	/**
	 * loadInfo
	 */
	public void loadInfo(Context aContext)
	{
		if( null == aContext )
			return ;
		
		mPlatform = "android";
		mAppName = "icson";
		
		// OS version.
		mOsVersion = Build.VERSION.SDK + "(" + Build.VERSION.RELEASE + ")";
		
		// Model.
		mDeviceModel = Build.MODEL;
		
		// Retrieve the version code.
		PackageInfo pInfo = null;
		try 
		{
			pInfo = aContext.getPackageManager().getPackageInfo(aContext.getPackageName(), 0);
		}
		catch (NameNotFoundException aException) 
		{
			// TODO Auto-generated catch block
			aException.printStackTrace();
			pInfo = null;
		}
		mVersionCode = (null != pInfo ? pInfo.versionCode : 0);
		
		// Get the device id.
		mDeviceUid = StatisticsUtils.getDeviceUid(aContext);
		
		try 
		{
			InputStream input = aContext.getAssets().open("channel", AssetManager.ACCESS_STREAMING);
			if (input != null)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(input));
				String line;
				if ((line = in.readLine()) != null) {
					mChannel = line;
				}
				
				input.close();
				input = null;
			}

		}
		catch (IOException ex) 
		{
			Log.e(StatisticsConfig.class.toString(), ex);
			mChannel = "";
		}
	}
	
	/**
	 * toBuffer
	 * @return
	 */
	public StringBuffer toBuffer(int nPriority)
	{
		StringBuffer pBuffer = new StringBuffer();
		pBuffer.append(mChannel);
		pBuffer.append("|");
		pBuffer.append(mPlatform);
		pBuffer.append("|");
		pBuffer.append(mOsVersion);
		pBuffer.append("|");
		pBuffer.append(mAppName);
		pBuffer.append("|");
		pBuffer.append(mVersionCode);
		pBuffer.append("|");
		pBuffer.append(mDeviceModel);
		pBuffer.append("|");
		pBuffer.append(mDeviceUid);
		pBuffer.append("|");
		pBuffer.append(nPriority);
		
		return pBuffer;
	}
	
	/**
	 * getPeriod
	 * @param nPriority
	 * @return
	 */
	public static long getPeriod(int nPriority)
	{
		final long aPeriods[] = {10 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000, 7 * 24 * 60 * 60 * 1000};
		final int nLength = aPeriods.length;
		if( (0 > nPriority) || (nPriority >= nLength) )
			return 0;
		
		return aPeriods[nPriority];
	}
	
	// Member instances.
	String  mChannel;
	String  mPlatform;
	String  mOsVersion;
	String  mAppName;
	int     mVersionCode;
	String  mDeviceModel;
	String  mDeviceUid;
}
