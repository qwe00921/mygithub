package com.icson.statistics;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.icson.lib.FullDistrictHelper;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;

public class UserInfoUploader implements OnSuccessListener<Object>, OnErrorListener
{
	/**
	 * UserInfoUploader
	 */
	public UserInfoUploader(Context aContext)
	{
		mContext = new WeakReference<Context>(aContext);
	}
	
	/**
	 * updateInfo
	 * @param nUid
	 */
	public void updateInfo(StatisticsConfig aConfig, long nUid, int nStatus)
	{
		Context pContext = mContext.get().getApplicationContext();
		if( null != pContext )
		{
			mAjax = ServiceConfig.getAjax(Config.URL_USERINFO_UPDATE);
			if( null == mAjax )
				return ;
			mAjax.setOnSuccessListener(this);
			mAjax.setOnErrorListener(this);
			mAjax.setParser(new InnerParser());
			
			// Set the device information.
			mAjax.setData("uid", nUid);
			mAjax.setData("udid", StatisticsUtils.getDeviceUid(pContext));
			if( nStatus > 0 )
			{
				mAjax.setData("status", nStatus);
			}
			mAjax.setData("device_type", 100); // 100 - 199, android platforms.
			mAjax.setData("app_src", ToolUtil.isSimulator() ? "simulator" : aConfig.mChannel);
			mAjax.setData("app_version", aConfig.mVersionCode);
			mAjax.setData("os_verion", aConfig.mOsVersion);
			mAjax.setData("device_token", StatisticsUtils.getIMEI(pContext));
			mAjax.setData("verify_key", StatisticsUtils.getTestId(pContext));
			
			//一／二级地址信息上报
			mAjax.setData("geo_graphic", ""+FullDistrictHelper.getProvinceIPId());
			mAjax.setData("zone", ""+FullDistrictHelper.getCityId());
			
			// Network type.
			NetworkInfo pActive = StatisticsUtils.getAvailableInfo(mContext.get().getApplicationContext());
			String strType = (null != pActive ? pActive.getTypeName() : "");
			if( !TextUtils.isEmpty(strType) )
			{
				mAjax.setData("net_type", strType);
			}
			
			// Send the request.
			mAjax.send();
		}
	}
	
	@Override
	public void onError(Ajax aAjax, Response aResponse) 
	{
		Log.d(UserInfoUploader.class.toString(), aResponse.getUrl());
		cleanup();
	}

	@Override
	public void onSuccess(Object aObject, Response aResponse)
	{
		String strMessage = (String)aObject;
		if( !TextUtils.isEmpty(strMessage) )
		{
			Log.d(UserInfoUploader.class.toString(), strMessage);
		}
		cleanup();
	}
	
	
	/**
	 * cleanup
	 */
	public void cleanup()
	{
		if( null != mAjax )
		{
			mAjax.abort();
			mAjax = null;
		}
	}
	
	private Ajax                    mAjax;
	private WeakReference<Context>  mContext;
	
	/**
	 * class for InnerParser
	 */
	class InnerParser extends Parser<byte[], String>  
	{
		@Override
		public String parse(byte[] aBytes, String strCharset) throws Exception 
		{
			JSONParser parser = new JSONParser();
			parser.parse(aBytes, strCharset);
			
			mIsSuccess = true;
			return parser.getString();
		}
	}

}
