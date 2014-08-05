package com.icson.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.icson.R;
import com.icson.hotlist.RecentCates;
import com.icson.lib.AppStorage;
import com.icson.lib.ITrack;
import com.icson.lib.UExceptionHandler;
import com.icson.lib.ui.UiUtils;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.db.DbFactory;

public class IcsonApplication extends Application {
	private static final String LOG_TAG =  IcsonApplication.class.getName();
	public static AppStorage mStorage = null;
	public static RecentCates mRecentCates = null;
	public static StatisticsEngine mEngine;
	public static IcsonApplication app;
	public static boolean APP_RUNNING = false;
	public static int mVersionCode;
	public static String mReportTag = "";  //全局tag，用于数据上报
	
	private static TraceReceiver mTraceReceiver;
	private static int 	  Last_level = 0;
	private static String index0 = "0";
	private static String index1 = "0";
	private static String index2 = "0";
	private static String index3 = "0";
	
	@Override
	public void onCreate() {
		super.onCreate();
		IcsonApplication.app = this;
		
		//CrashReport.initCrashReport(this);
		//String userId = ILogin.getLoginUid() + ""; // 用户ID
		//CrashReport.setUserId(this, userId);
		
//		if (Config.DEBUG) // 正式发布时记得要关闭
//        {
//			CrashReport.setLogAble(true, false);
//        }
		
		//setExceptionStrategy();

		/*
		if (Config.DEBUG) // 正式发布时记得要关闭
        {
             Constants.IS_DEBUG = true; // 输出eup log
             Constants.IS_CORE_DEBUG = true; // 输出更详细的 eup log
             Constants.IS_USETESTSERVER = true; // 使用测试服务器，避免污染正式环境

             // 初始接入时SDK如果发现使用问题时，会抛出异常，SDK接入时建议开启，可以避免使用上的出错。
             Constants.Is_AutoCheckOpen = true;
        }
		
		String userId = ILogin.getLoginUid()+""; // 唯一标识一个用户
        //SDK上报时都是用UploadHandler中的doUpload方法，用户可以根据自己的需要实现自己的上报器进而监控或控制所有的SDK上报，
        //SDK的默认实现可以通过Analytics.getDefaultUpload(this)获取。
         UploadHandler hanlder = ExceptionUpload.getDefaultUpload(this);

         // 监听每次上报结果的MonitorUploadHandler
         // hanlder =createMonitorUploadHandler(this);
         
          // APP使用了Eup 或 Eup_Gray的jar包：
           //初始化1：
          ExceptionUpload eup = ExceptionUpload.getInstance(this, userId, true, hanlder); //默认不开启异常合并功能
          
           //初始化2：
          // ExceptionUpload eup = ExceptionUpload.getInstance(this, userId, isStartAfterQuery, hanlder,true); //开启异常合并功能

          // 配置异常上报，注意配置需要在开启前，否则可能无法及时生效
          setExceptionUpload();

          // 配置异常上报，注意配置需要在开启前，否则可能无法及时生效
          eup.setIsUseEup(true);
          */
		
	}
	
	

//	private void setExceptionUpload() {
//		/* isDefaultEup true表示异常上报时不通知用户直接静默上报，false则会弹框提醒用户由用户选择是否上报。 */
//        ExceptionUpload.setDefaultEUP(true);
//
//        /* 有用户希望能在发生异常时，做一些处理，可以通过这个接口设置 */
//        ExceptionUpload.setYourUncaughtExceptionHandler(new UncaughtExceptionHandler()
//        {
//
//             @Override
//             public void uncaughtException(Thread thread, Throwable ex)
//             {
//                  // 处理。。。。。
//                  // 打一次异常堆栈吧。。。
//                  ex.printStackTrace();
//             }
//        });
//
//        /*
//        * 有些用户希望能把异常发生的堆栈信息能存储在用户的sdcard中，方便对方投诉时直接向用户获取， 如果你有这个需求，只需要如下操作：
//        * isStoreEupLogSdcard = true,
//        * 并确保自己的应用具有此权限：android.permission.WRITE_EXTERNAL_STORAGE
//        * 异常的堆栈将会按日期顺序保存在sdcard中，并且设置了文件大小限制，不会导致文件不断增大。
//        */
//        Constants.isStoreEupLogSdcard = true;
//	}

	public static void start() {
		if (APP_RUNNING == true) {
			return;
		}
		
		APP_RUNNING = true;

		try {
			UExceptionHandler UEHandler = new UExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(UEHandler);
		} catch (SecurityException ex) {
			android.util.Log.e(LOG_TAG, "onCreate|" + ex.getMessage());
		}

		mTraceReceiver = new TraceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.BROADCAST_TRACE);
		IcsonApplication.app.registerReceiver(mTraceReceiver, filter,Config.SLEF_BROADCAST_PERMISSION,null);
		
		// Retrieve the version code.
		Context pContext = IcsonApplication.app.getApplicationContext();
		IcsonApplication.getVersionCode(pContext);
		
		// Create a new instance for statistics engine.
		mEngine = new StatisticsEngine(pContext);
		
		// Create new instance of AppStorage
		mStorage = new AppStorage(pContext);
		
		mRecentCates = new RecentCates();
		
		mReportTag = "";
		
		//When app starts firstly, report Data
		ToolUtil.reportStatisticsDevice("reportDeviceType:1|");
	}
	
	public static void getVersionCode(Context aContext) {
		if( (mVersionCode > 0) || (null == aContext) )
			return ;
		
		PackageInfo pInfo = null;
		try 
		{
			pInfo = aContext.getApplicationContext().getPackageManager().getPackageInfo(aContext.getApplicationContext().getPackageName(), 0);
		}
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
			pInfo = null;
		}
		mVersionCode = (null != pInfo ? pInfo.versionCode : 0);
	}

	public static void exit() {
		APP_RUNNING = false;
		
		//clear third call source
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "thirdcallsource", "", false);
		
		if( null != mEngine )
		{
			mEngine.cleanup();
			mEngine = null;
		}
		
		if( null != mStorage ) {
			mStorage.save();
			mStorage = null;
		}
		
		if( null != mRecentCates ) {
			mRecentCates.saveContent();
			mRecentCates = null;
		}
		
		DbFactory.closeDataBase();
		if( null != mTraceReceiver ){
			IcsonApplication.app.unregisterReceiver(mTraceReceiver);
			mTraceReceiver = null;
		}
		
		//clear static stuff
		UiUtils.clear();
		
		mReportTag = "";
				
		//ReloginWatcher.clear();
				
		//FullDistrictHelper.clear();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		DbFactory.closeDataBase();
		
		
	}
	
	/*
	 * update tag and y_track
	 * tag = pageLevel(页面层级 1位).pageId（页面Id）+locationId（位置Id 5位）
	 * y_track: 页面路径 0-1-2-3.Last_level
	 * 
	 */
	public static void updateTagAndPageRoute(String strPageId, String strLocId) {
		int pPageLevel = getPageLevel(strPageId);
		
		StringBuilder builder = new StringBuilder();
		builder.append(pPageLevel);
		builder.append(".");
		builder.append(strPageId);
		builder.append(strLocId);
		
		mReportTag = builder.toString();
		updatePageRoute(pPageLevel, strPageId+strLocId);
	}
	
	public static void updateTagAndPageRoute(String strYtag) {
		if(TextUtils.isEmpty(strYtag)) {
			return;
		}
		//update tag
		mReportTag = strYtag;
		
		//update y_track
		String[] ytags  = strYtag.split("\\.");
		if( ytags.length > 1 ){
			updatePageRoute(Integer.parseInt(ytags[0]), ytags[1]);
		}
	}
	
	private static void updatePageRoute(int nPageLevel, String strTag) {
		switch (nPageLevel) {
			case 0:
				index0 = strTag;
				index1 = "0";
				index2 = "0";
				index3 = "0";
				break;
			case 1:
				index1 = strTag;
				index2 = "0";
				index3 = "0";
				break;
			case 2:
				index2 = strTag;
				index3 = "0";
				break;
			case 3:
				index3 = strTag;
				break;
			default:
				break;
		}
		
		if(nPageLevel >= 0)
			Last_level = nPageLevel;
	}
	
	public static void setPageRoute(String indexZero, String indexOne, String indexTwo, String indexThree, String lastLevel) {
		index0 = indexZero;
		index1 = indexOne;
		index2 = indexTwo;
		index3 = indexThree;
		
		int nLevel = Integer.valueOf(lastLevel);
		if(nLevel >= 0)
			Last_level = nLevel;
	}
	
	/** 
	 * 记录下用户 页面路径 0-1-2-3.Last_level
	 */
	public static void setPageRoute(String tag, String referId){
		int level = getPageLevel(referId);
		switch (level) {
		case 0:
			index0 = tag;
			index1 = "0";
			index2 = "0";
			index3 = "0";
			break;
		case 1:
			index1 = tag;
			index2 = "0";
			index3 = "0";
			break;
		case 2:
			index2 = tag;
			index3 = "0";
			break;
		case 3:
			index3 = tag;
			break;
		default:
			break;
		}
		if(level >= 0)
			Last_level = level;
		 
		//Log.e("yTrack", getPageRoute());
	}
	
	/*
	 * 页面级别
	 * @param String strPageId 页面Id
	 * 
	 * 0级：外部渠道
	 * 1级：首页
	 * 2级：其他页面
	 * 3级：商详，订单确认
	 * 
	 */
	private static  int getPageLevel(String strPageId) {
		int nLevel = -1;
		if(strPageId.equals(IcsonApplication.app.getString(R.string.tag_push_message)) 
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_weixin)) 
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_cps)) ){
			nLevel = 0;
		}else if(strPageId.equals(IcsonApplication.app.getString(R.string.tag_Home))){
			//tag_menu == 1000 == tag_home too
			nLevel = 1;
		}else if( strPageId.equals(IcsonApplication.app.getString(R.string.tag_SearchActivity)) 
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_QiangActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_TuanActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_EventMorningActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_EventThhActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_EventWeekendActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_HotlistActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_ViewHistoryActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_MessageActivity))
				|| strPageId.equals(IcsonApplication.app.getString(R.string.tag_YiQiangActivity)) ){
			nLevel = 2;
		}else{
			nLevel = 3;
		}
		
		return nLevel;
	}

	/** 
	 * 返回用户操作 下单路径 0-1-2-3.Last_level
	 */
	public static String getPageRoute(){
		StringBuilder sb = new StringBuilder();
		sb.append(index0)
		.append("-")
		.append(index1)
		.append("-")
		.append(index2)
		.append("-")
		.append(index3)
		.append(".")
		.append(Last_level);
		
		return sb.toString();
	}
	
	public static String getTag(){
		return mReportTag;
	}
	
	public static void setTag(String tag){
		mReportTag = tag;
	}
	
	
	private static class TraceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			String refer = intent.getStringExtra(ITrack.REQUEST_REFER);
//			String referId = intent.getStringExtra(ITrack.REQUEST_REFER_ID);
//			String path = intent.getStringExtra(ITrack.REQUEST_PATH);
//			String pathId = intent.getStringExtra(ITrack.REQUEST_PATH_ID);
//			String tag = intent.getStringExtra(ITrack.REQUEST_TAG);
			
//			int pPathLevel = getPageLevel(pathId);
//			int pReferLevel = getPageLevel(referId);
			
			String pType = intent.getStringExtra(ITrack.REQUEST_TYPE);
			String pPageId = TextUtils.isEmpty(intent.getStringExtra(ITrack.REQUEST_PAGE_ID)) ? "" : intent.getStringExtra(ITrack.REQUEST_PAGE_ID);
			String pLocationId = TextUtils.isEmpty(intent.getStringExtra(ITrack.REQUEST_LOCATION_ID)) ? "00000" : intent.getStringExtra(ITrack.REQUEST_LOCATION_ID);
			String pPid = TextUtils.isEmpty(intent.getStringExtra(ITrack.REQUEST_PID)) ? "" : intent.getStringExtra(ITrack.REQUEST_PID);
			String pExtraInfo = TextUtils.isEmpty(intent.getStringExtra(ITrack.REQUEST_EXT_INFO)) ? "" : intent.getStringExtra(ITrack.REQUEST_EXT_INFO);
			
			int pPageLevel = getPageLevel(pPageId);
			
			ITrack.Option option = new ITrack.Option(pType, pPageId, pLocationId, pPid, String.valueOf(pPageLevel), pExtraInfo);
			ITrack.send(option);
		}
	}
}
