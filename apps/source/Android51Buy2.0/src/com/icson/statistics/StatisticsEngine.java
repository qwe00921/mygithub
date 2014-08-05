package com.icson.statistics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.Vector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.GzipHelper;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;

public class StatisticsEngine implements Runnable, OnSuccessListener<Object>, OnErrorListener
{
	/**
	 * Default constructor of StatisticsEngine
	 * @param aContext indicates context reference for engine
	 */
	public StatisticsEngine(Context aContext) 
	{
		mStorage = new StorageHelper(aContext);
		mContext = new WeakReference<Context>(aContext);
		mCaches = new Vector<?>[PRIORITY_COUNT];
		mHandler = new Handler();
		mLooping = false;
		mInfoUploader = new UserInfoUploader(aContext);
		this.loadConfig();
		mPriority = StatisticsConfig.PRIORITY_FATAL;
	}
	
	public static void onActivityResume(Context aContext) {
		StatisticsEngine engine = (null != IcsonApplication.mEngine ? IcsonApplication.mEngine : null);
		if( null != engine && engine.mMtaInit )
			StatService.onResume(aContext);
	}
	
	public static void onActivityPause(Context aContext) {
		StatisticsEngine engine = (null != IcsonApplication.mEngine ? IcsonApplication.mEngine : null);
		if( null != engine && engine.mMtaInit )
			StatService.onPause(aContext);
	}
	
	public static void reportQQ(Context aContext, String strAccount) {
		StatisticsEngine engine = (null != IcsonApplication.mEngine ? IcsonApplication.mEngine : null);
		if( (null != engine) && (engine.mMtaInit) && (!TextUtils.isEmpty(strAccount)) )
			StatService.reportQQ(aContext, strAccount);
	}
	
	/**
	 * track event.
	 * @param aContext
	 * @param strEventId
	 */
	public static void trackEvent(Context aContext, String strEventId) {
		StatisticsEngine.trackEvent(aContext, strEventId, null);
	}
	
	/**
	 * track event.
	 * @param aContext
	 * @param strEventId
	 */
	public static void trackEvent(Context aContext, String strEventId, Object aExtra) {
		StatisticsEngine engine = (null != IcsonApplication.mEngine ? IcsonApplication.mEngine : null);
		if( null != engine )
			engine.reportEvent(aContext, strEventId, aExtra);
	}
	
	private void reportEvent(Context aContext, String strEventId, Object aExtra) {
		if( null == aContext || TextUtils.isEmpty(strEventId) )
			return ;
		
		Properties prop = new Properties();
		prop.setProperty("token", aContext.getClass().toString());
		if( null != mConfig ) {
			prop.setProperty("deviceId", mConfig.mDeviceUid);
			prop.setProperty("model", mConfig.mDeviceModel);
			prop.setProperty("osInfo", mConfig.mOsVersion);
		}
		final long loginUid = ILogin.getLoginUid();
		if( loginUid > 0 ) {
			prop.setProperty("uid", ((Long)loginUid).toString());
		}
		if( null != aExtra ) {
			prop.setProperty("extra", aExtra.toString());
		}
		StatService.trackCustomKVEvent(aContext, strEventId, prop);
	}
	
	public static void initMta(boolean debugMode) {
		if (debugMode) { // 调试时建议设置的开关状态
            // 查看MTA日志及上报数据内容
            StatConfig.setDebugEnable(true);
            // 禁用MTA对app未处理异常的捕获，方便开发者调试时，及时获知详细错误信息。
            // StatConfig.setAutoExceptionCaught(false);
            // StatConfig.setEnableSmartReporting(false);
            // Thread.setDefaultUncaughtExceptionHandler(new
            // UncaughtExceptionHandler() {
            // 调试时，使用实时发送
            StatConfig.setStatSendStrategy(StatReportStrategy.INSTANT);
        } else { // 发布时，建议设置的开关状态，请确保以下开关是否设置合理
            // 禁止MTA打印日志
            StatConfig.setDebugEnable(false);
            // 根据情况，决定是否开启MTA对app未处理异常的捕获
            StatConfig.setAutoExceptionCaught(true);
            // 选择默认的上报策略
            StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);
        }
		
		// Get meta data from xml configuration.
		StatisticsEngine engine = (null != IcsonApplication.mEngine ? IcsonApplication.mEngine : null);
		Context context = (null != engine ? engine.mContext.get() : null);
		if( (null == context) || (null == engine.mConfig) )
			return ;
		
		engine.mMtaInit = true;
		
		// Set channel information.
		StatConfig.setInstallChannel(engine.mConfig.mChannel);
		
		try {
			// Get app key.
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			if( null != info ) {
				Bundle bundle = info.metaData;
				String appKey = null != bundle ? bundle.getString("TA_APPKEY") : "";
				if( !TextUtils.isEmpty(appKey) ) {
					StatService.startStatService(context, appKey, com.tencent.stat.common.StatConstants.VERSION);
				}
			}
		} catch (NameNotFoundException aException) {
			aException.printStackTrace();
		} catch (MtaSDkException aException) {
			aException.printStackTrace();
		}
	}
	
	/**
	 * alert
	 * @param strApi
	 * @param nPriority
	 * @param nErrCode
	 * @param strErrMsg
	 * @param strOrderId
	 * @param nUid
	 * @return
	 */
	public static boolean alert(String strApi, int nPriority, int nErrCode, String strErrMsg, String strOrderId, long nUid)
	{
		StatisticsEngine pSelf = IcsonApplication.mEngine;
		if( null == pSelf )
			return false;
		
		return pSelf.report(strApi, nPriority, nErrCode, strErrMsg, nUid, strOrderId, "");
	}
	
	/**
	 * alert
	 * @param strApi
	 * @param nPriority
	 * @param nErrCode
	 * @param strErrMsg
	 * @param strOrderId
	 * @return
	 */
	public static boolean alert(String strApi, int nPriority, int nErrCode, String strErrMsg, String strOrderId)
	{
		StatisticsEngine pSelf = IcsonApplication.mEngine;
		if( null == pSelf )
			return false;
		
		return pSelf.report(strApi, nPriority, nErrCode, strErrMsg, 0, strOrderId, "");
	}
	
	/**
	 * alert
	 * @param strApi
	 * @param nPriority
	 * @param nErrCode
	 * @param strErrMsg
	 * @return
	 */
	public static boolean alert(String strApi, int nPriority, int nErrCode, String strErrMsg)
	{
		StatisticsEngine pSelf = IcsonApplication.mEngine;
		if( null == pSelf )
			return false;
		
		return pSelf.report(strApi, nPriority, nErrCode, strErrMsg, 0, "", "");
	}
	
	/**
	 * alert
	 * @param strApi
	 * @param nPriority
	 * @param nErrCode
	 * @return
	 */
	public static boolean alert(String strApi, int nPriority, int nErrCode)
	{
		StatisticsEngine pSelf = IcsonApplication.mEngine;
		if( null == pSelf )
			return false;
		
		return pSelf.report(strApi, nPriority, nErrCode, "", 0, "", "");
	}
	
	/**
	 * updateInfo
	 * @param nUid
	 */
	public static void updateInfo(long nUid, int nStatus)
	{
		StatisticsEngine pSelf = IcsonApplication.mEngine;
		if( null == pSelf )
			return ;
		
		UserInfoUploader pUploader = pSelf.mInfoUploader;
		if( null != pUploader )
		{
			pUploader.updateInfo(pSelf.mConfig, nUid, nStatus);
		}
	}
	
	/**
	 * cleanup
	 */
	public void cleanup()
	{
		if( null != mHandler )
		{
			mHandler.removeCallbacks(this);
			mHandler = null;
		}
		
		if( null != mInfoUploader )
		{
			mInfoUploader.cleanup();
			mInfoUploader = null;
		}
		
		mUploading = null;
		
		if( null != mAjax )
		{
			mAjax.abort();
			mAjax = null;
		}
		
		if( null != mCaches )
		{
			final int nLength = mCaches.length;
			for( int nIdx = 0; nIdx < nLength; nIdx++ )
			{
				Vector<?> aVector = this.getVector(nIdx, false);
				if( null != aVector )
				{
					aVector.clear();
				}
			}
			
			mCaches = null;
		}
		
	}
	
	/**
	 * report
	 * @return
	 */
	private boolean report(String strApi, int nPriority, int nErrCode, String strErrMsg, long nUid, String strOrderId, String strExtent)
	{
		/*
		if( TextUtils.isEmpty(strApi) || (0 > nPriority) || (nPriority >= PRIORITY_COUNT) )
			return false;
		
		Context pContext = mContext.get();
		if( null == pContext )
			return false;
		
		// Get current time-stamp.
		final long nTimestamp = System.currentTimeMillis();
		
		// Get network type.
		NetworkInfo pActive = StatisticsUtils.getAvailableInfo(pContext);
		String strType = (null != pActive ? pActive.getTypeName() : "");
		NetworkInfo.State state = null != pActive ? pActive.getState() : NetworkInfo.State.UNKNOWN;
		String strState = state.toString();
		
		// Create a new instance of record entity.
		RecordEntity pEntity = new RecordEntity(strApi, nErrCode, strErrMsg, strType, strState, nUid, nTimestamp, strOrderId, strExtent);
		
		// Push to memory instance.
		this.saveEntity(pEntity, nPriority);
		
		// Update current priority for looping.
		if( mPriority > nPriority )
		{
			mPriority = nPriority;
		}
		*/
		
		return true;
	}
	

	/**
	 * run
	 * Runnable callback.
	 */
	@Override
	public void run()
	{
		mLooping = false;
		this.pickupToUpload();
	}
	
	@Override
	public void onSuccess(Object aObject, Response aResponse) 
	{
		String strMessage = (String)aObject;
		if( !TextUtils.isEmpty(strMessage) )
		{
			Log.d(StatisticsEngine.class.toString(), strMessage);
		}
		
		// Notice for uploading successfully.
		this.onUploaded(true);
	}
	
	@Override
	public void onError(Ajax aAjax, Response aResponse)
	{
		this.onUploaded(false);
	}
	
	/**
	 * saveEntity
	 * @param aEntity
	 */
	@SuppressWarnings("unused")
	private void saveEntity(RecordEntity aEntity, int nPriority)
	{
		if( null == aEntity )
			return ;
		
		// Save the entity to memory first.
		getVector(nPriority, true).add(aEntity);
		
		// Set task to process the cache in memory to local storage.
		processCaches();
		
		// Trigger the loop.
		setNextLoop();
	}
	
	/**
	 * processCaches
	 */
	private synchronized void processCaches()
	{
		if( null != mStorage )
		{
			for( int nPriority = StatisticsConfig.PRIORITY_FATAL; nPriority < PRIORITY_COUNT; nPriority++ )
			{
				// Dump the entities to local storage.
				Vector<RecordEntity> aEntities = getVector(nPriority, false);
				if( (null != aEntities) && (aEntities.size() > 0) )
				{
					mStorage.dumpEntities(aEntities, nPriority, mConfig);
					
					// Clear the entities.
					aEntities.clear();
				}
			}
		}
	}
	
	/**
	 * getVector
	 * @param nPriority
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Vector<RecordEntity> getVector(int nPriority, boolean bCheckExists)
	{
		if( (bCheckExists) && (null == mCaches[nPriority]) )
		{
			mCaches[nPriority] = new Vector<RecordEntity>();
		}
		
		return (Vector<RecordEntity>) mCaches[nPriority];
	}
	
	/**
	 * pickupToUpload
	 * @return
	 */
	private synchronized boolean pickupToUpload()
	{
		// 1. Check whether network status is okay.
		boolean bNetworkOkay = StatisticsUtils.isNetworkAvailable(mContext.get());
		if( !bNetworkOkay )
		{
			// Trigger for next loop.
			setNextLoop();
			return false;
		}
		
		// 2. Check whether is already uploading now. 
		if( null != mUploading )
			return false;
		
		StorageHelper.UploadInfo pUploadInfo = (null != mStorage ? mStorage.getUploadInfo() : null);
		if( null == pUploadInfo )
		{
			// Nothing found.
			setNextLoop();
			return false;
		}
		
		// Update information for uploading file and current priority.
		mUploading = pUploadInfo.mFile;
		mPriority = pUploadInfo.mPriority;
		
		if( !this.tryUpload(mUploading) )
		{
			// Reset the uploading file.
			mUploading = null;
			
			// Request for next loop.
			setNextLoop();
			return false;
		}
		
		return true;
	}
	
	/**
	 * tryUpload
	 * @param pUploading
	 * @return
	 */
	private synchronized boolean tryUpload(File pUploading)
	{
		if( null == pUploading )
			return false;

		/*
		String strContent = bCompress ? readData(pUploading) : readContent(pUploading);
		
		if( TextUtils.isEmpty(strContent) )
			return false;
		
		// Send the request to back-end to report the data.
		mAjax = AjaxUtil.post("http://w3sg.m.51buy.com/app/json.php?mod=aalert&act=report");
	//	mAjax = AjaxUtil.post(Config.APP_ICSON_COM + "/json.php?mod=aalert&act=report");
		mAjax.setOnSuccessListener(this);
		mAjax.setOnErrorListener(this);
		mAjax.setParser(new InnerParser());
		*/
		
		byte[] aData = readData(pUploading);
		if( null == aData || 0 >= aData.length )
			return false;
		
		mAjax = ServiceConfig.getAjax(Config.URL_UPLOAD_ALERT);
		if( null == mAjax )
			return false;
		mAjax.setData("file", "datafile");
		mAjax.setParser(new InnerParser());
		mAjax.setOnSuccessListener(this);
		mAjax.setOnErrorListener(this);
		mAjax.setFile("datafile", aData);
		mAjax.send();
		
		return true;
	}
	
	/**
	 * readData
	 * @param aFile
	 * @return
	 */
	private byte[] readData(File aFile)
	{
		if( (null == aFile) || (!aFile.exists()) )
			return null;
		
		byte[] aData = null;
		ByteArrayOutputStream pOutputStream = null;
		FileInputStream pInputStream = null;
		try 
		{
			pInputStream = new FileInputStream(aFile);
			pOutputStream = new ByteArrayOutputStream(1024);
			GzipHelper.compress(pInputStream, pOutputStream);
			aData = pOutputStream.toByteArray();
		}
		catch (Exception aException) 
		{
			aException.printStackTrace();
			aData = null;
		}
		finally 
		{
			try 
			{
				if( null != pOutputStream )
				{
					pOutputStream.close();
					pOutputStream = null;
				}
				if( null != pInputStream )
				{
					pInputStream.close();
					pInputStream = null;
				}
			}
			catch (Exception aException) 
			{
				aException.printStackTrace();
			}
		}

		return aData;
	}
	
	/**
	 * onUploaded
	 */
	private synchronized void onUploaded(boolean bSuccess)
	{
		if( bSuccess )
		{
			// Upload successfully.
			mUploading.delete();
			mUploading = null;
			
			// Send event for next picking up.
			setNextLoop();
		}
		else if ( !tryUpload(mUploading) )
		{
			// Retry to uploading.
			mUploading.delete();
			mUploading = null;
			
			// Send event for next picking up.
			setNextLoop();
		}
	}
	
	/**
	 * setNextLoop
	 */
	private void setNextLoop()
	{
		if( (null == mHandler) || (mLooping) )
			return ;
		
		// Send event for next picking up.
		final long nDelayMs = StatisticsConfig.getPeriod(mPriority);
		mHandler.postDelayed(this, nDelayMs);
		mLooping = true;
	}
	
	/**
	 * loadConfig
	 * Load configuration for statistics engine.
	 */
	private void loadConfig()
	{
		if( null != mConfig )
			return ;
		
		// Load the configuration for statistics information.
		mConfig = new StatisticsConfig();
		mConfig.loadInfo(mContext.get());
	}
	
	private Ajax                   mAjax;
	private File                   mUploading;
	private Handler                mHandler;
	private boolean                mLooping; // Indicates whether looping already started.
	private StorageHelper          mStorage;
	private StatisticsConfig       mConfig;
	private WeakReference<Context> mContext;
	private Vector<?>              mCaches[];
	private UserInfoUploader       mInfoUploader;
	private int                    mPriority;
	private boolean                mMtaInit = false;
	
	// Constant member instances.
	private static final int PRIORITY_COUNT = (StatisticsConfig.PRIORITY_DEBUG + 1);
	
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
