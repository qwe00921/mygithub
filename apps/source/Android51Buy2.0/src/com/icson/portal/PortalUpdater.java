package com.icson.portal;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.icson.lib.model.PortalInfoModel;
import com.icson.lib.parser.UpdatePortalParser;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PortalUpdater {
	
	private static UpdatePortalParser mPortalInfoParser;
	
	private static Ajax mPortalAjax;
	static private PortalInfoModel mPortalInfo;
	
	
	public static void clear()
	{
		if(null!=mPortalAjax)
		{
			mPortalAjax.abort();
			mPortalAjax = null;
		}
		
		mPortalInfoParser = null;	
	}
	
	
	/**
	 * 
	* method Name:updatePortalInfo    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public static void updatePortalInfo(final Context aContext)
	{
		if(null == aContext)
			return;
		
		String strInfo = Preference.getInstance().getPortalInfo();
		if(mPortalInfo == null)
			mPortalInfo = new PortalInfoModel();
		mPortalInfo.infoFromString(strInfo);
		
		mPortalAjax = ServiceConfig.getAjax(Config.URL_MSGOP_SPLASH);
		//ServiceConfig.getAjax(Config.URL_PORTAL_VERSION);
		if( null == mPortalAjax )
			return;
		//Version + Image Parser
		if(null == mPortalInfoParser)
			mPortalInfoParser = new UpdatePortalParser();
		mPortalAjax.setParser(mPortalInfoParser);
		mPortalAjax.setData("deviceid", StatisticsUtils.getDeviceUid(aContext));
		mPortalAjax.setData("width", ToolUtil.getEquipmentWidth(IcsonApplication.app));
		mPortalAjax.setData("height", ToolUtil.getEquipmentHeight(IcsonApplication.app));
		mPortalAjax.setData("spver", mPortalInfo.getSpver());
		
		mPortalAjax.setOnSuccessListener(new OnSuccessListener<PortalInfoModel>()
				{
					@Override
					public void onSuccess(PortalInfoModel v,
							Response response) {
						//fail
						if(null==v)
						{
							clear();
							return;
						}
						
						//has new version
						mPortalInfo = null;
						mPortalInfo = (PortalInfoModel)v;
						
						fetchPortalImg(aContext);
						}
						// TODO Auto-generated method stub
						
				 });
		
		mPortalAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
				clear();
				
			}});
		

		mPortalAjax.send();
	}

	
	private static void fetchPortalImg(final Context aContext)
	{
		if(null==mPortalInfo)
			return;
		
		//String aaa = "http://img.d843.com/uploads/allimg/120901/1-120Z1105320.jpg";
		mPortalAjax = AjaxUtil.downLoad(mPortalInfo.getImgUrl(), 
				aContext.getCacheDir().getAbsolutePath() + "/" + PortalInfoModel.sFileTmpName,
				new OnSuccessListener<File>()
				{
					@Override
					public void onSuccess(File path, Response response) {
						//already in tmpfile
						File storeFile = new File(aContext.getCacheDir().getAbsolutePath() + "/" + PortalInfoModel.sFileName);
						path.renameTo(storeFile);
						
						//change Preference after imageFile saved
						Preference.getInstance().setPortalInfo(mPortalInfo.infoToString());
						
						clear();
						}
				}, 
				new OnErrorListener() 
				{
					@Override
					public void onError(Ajax ajax, Response response) {
						
						clear();
						
					}
				});
		
		if( null == mPortalAjax )
			return ;
		
		mPortalAjax.send();
	}
	
	
	/**
	 * 
	 * @param absoluatePath
	 * @return
	 */
	public static Bitmap getAvailPortalBitmap(final Context aContext)
	{
		final long nCurrentSecond = System.currentTimeMillis()/1000;
		File storeFile = new File(aContext.getCacheDir().getAbsolutePath()  + "/" + PortalInfoModel.sFileName);
		if(storeFile.exists())
		{
			String strInfo = Preference.getInstance().getPortalInfo();
			if(null==mPortalInfo)
				mPortalInfo = new PortalInfoModel();
			mPortalInfo.infoFromString(strInfo);
			
			if(mPortalInfo.getBeginTime() > 0 &&
					mPortalInfo.getExpireTime() > nCurrentSecond)
			{
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inPurgeable = true;  
				opt.inInputShareable = true;
				return BitmapFactory.decodeFile(storeFile.getAbsolutePath(),opt);
			}
			else
			{
				storeFile.delete();
				return null;
			}
			
		}else
			return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getShowDuration()
	{
		if(null != mPortalInfo)
			return mPortalInfo.getShowDuration();
		else
			return 0;
	}
}
