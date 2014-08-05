package com.icson.lib;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.icson.statistics.StatisticsUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.HttpUtil;

public class ITrack {
	public static String REFER;
	public static int REFER_ID;
	public static String PAGE;
	public static int PAGE_ID;

	public static final String REQUEST_REFER = "request_refer";					//前一个页面名称
	public static final String REQUEST_PATH = "request_path";					//当前页面名称
	public static final String REQUEST_REFER_ID = "request_refer_id";			//前一个页面ID
	public static final String REQUEST_TAG = "request_tag";
	public static final String REQUEST_PATH_ID = "request_path_id";				//当前页面ID
	
	public static final String REQUEST_TYPE = "request_type";					//上报类型：1：页面展示；2：点击； 3：客户端设备日志上报
	public static final String REQUEST_PAGE_ID = "request_page_id";				//当前页面ID
	public static final String REQUEST_LOCATION_ID = "request_location_id";		//区域ID
//	public static final String REQUEST_NEXT_PAGE_ID = "request_next_page_id";	//下一个页面ID
	public static final String REQUEST_EXT_INFO = "request_extra_info";			//
	public static final String REQUEST_PID = "request_pid";						//商品ID
	
	
	public static final String REPORT_TYPE_PV 		= "1";						//商品ID
	public static final String REPORT_TYPE_CLICK 	= "2";						//商品ID
	public static final String REPORT_TYPE_DEVICE 	= "3";						//商品ID
	
	
	private static Handler mHandler;
	private static long AJAX_DELAYMILLIS = 1000 * 5;
	private static boolean DeviceInfoFetched = false;
	private static final int FETCH_NETTYPE_TIME_OFFSET = 1000 * 1;
	private static long LATEST_FETCH_NETTYPE_TIME = 0;
	private static String mNetType;

	public static void send(final ITrack.Option option) {
		if (mHandler == null) {
			mHandler = new TrackHandler();
		}

		Message m = Message.obtain();
		m.obj = option;
		mHandler.sendMessageDelayed(m, AJAX_DELAYMILLIS);
	}

	/*
	http://stat.51buy.com/stat.fcg?
		type=1
		uid=0
		pageid=1000
		plevel=1
		tag=3.1159406004000
		url=http://www.51buy.com/
		refer=http://base.51buy.com/myorder.html
		referid=11594060
		guid=2FA3DFDB2E1778
		whid=1
		resolution=1920*1080
		color=32
		pid=0
		*/
	
   /*
	* send data， URL：
	* http://stat.51buy.com/stat.fcg?
	*	type=1
	*	uid=0
	*	pageid=1000
	*	plevel=1
	*	tag=3.1159406004000
	*	url=http://www.51buy.com/
	*	refer=http://base.51buy.com/myorder.html
	*	referid=11594060
	*	guid=2FA3DFDB2E1778
	*	whid=1
	*	resolution=1920*1080
	*	color=32
	*	pid=0
	*/
	private static void execute(ITrack.Option option) {
		getTrackPackage();
		Ajax mAjax = ServiceConfig.getAjax(Config.URL_APP_TRACK);
		if( null == mAjax )
			return ;
		
		String strUin = ToolUtil.getUinForReport();
		HashMap<String,Object> pData = new HashMap<String,Object>();
		if( option.pType.equals(ITrack.REPORT_TYPE_PV) || option.pType.equals(ITrack.REPORT_TYPE_CLICK)) {
			pData.put("type", option.pType);
			pData.put("whid", 1990);//1990==android phone client
			pData.put("guid", TrackPackage.ISMI);
			pData.put("uid", ILogin.getLoginUid());
			pData.put("uin", strUin);
			pData.put("pageid", option.pPageId);
			pData.put("plevel", option.pPageLevel);
			pData.put("tag", IcsonApplication.getTag());
			if(!TextUtils.isEmpty(option.pPid)) {
				pData.put("pid", option.pPid);
			}
			if(!TextUtils.isEmpty(option.pExtraInfo)) {
				pData.put("ext", option.pExtraInfo);
			}
			pData.put("area", FullDistrictHelper.getDistrictId());
			
		}else if(option.pType.equals(ITrack.REPORT_TYPE_DEVICE)) {
			pData.put("type", option.pType);
			pData.put("whid", 1990);//1990==android phone client
			pData.put("guid", TrackPackage.ISMI);
			pData.put("uid", ILogin.getLoginUid());
			pData.put("uin", strUin);
			if(!TextUtils.isEmpty(option.pExtraInfo)) {
				pData.put("ext", option.pExtraInfo);
			}
			pData.put("url", TrackPackage.APP_VERSION);  //客户端版本（如：v2.3.0）
			pData.put("resolution", TrackPackage.SCREEN_WIDTH+"*"+TrackPackage.SCREEN_HEIGHT); //手机屏幕分辨率
			pData.put("color", TrackPackage.CHANNEL); //channel id
			pData.put("network", TrackPackage.NETTYPE);//网络类型
		}
		
		mAjax.setData(pData);
		mAjax.setRequestHeader("User-Agent", getUserAgent());
		mAjax.send();
	}
	

	public static class Option {
//		public String refer;
//		public String referId;
//		public String path;
//		public String pathId;
		public String pType;			//1:页面跳转；2:点击
		public String pPageId;
		public String pLocationId;
		public String pPid;			//商品id,默认0
		public String pPageLevel;	//当前页面等级(0渠道，1首页+菜单导航，2菜单进入的二级页面，3细分类)
		public String pExtraInfo;
		
		public Option(String type, String pageId, String locationId, String pid, String pLevel, String extInfo) {
			this.pType = type;
			this.pPageId = pageId;
			this.pLocationId = locationId;
			this.pPageLevel = pLevel;
			this.pPid = pid;
			this.pExtraInfo = extInfo;
		}
		
		
//		public Option(String refer, String referId, String path, String pathId, String locationId, String type, String pid, String plevel, String strReferLevel, String tag){
//			this.refer = refer;
//			this.referId = referId;
//			this.path = path;
//			this.pathId = pathId;
//			this.locationId = locationId;
//			this.type = ( null == type) ? "1" : type;
//			this.pid =  (null == pid) ? "" : pid;
//			this.pPathLevel = ( null == plevel ) ? "2" : plevel;
//			this.pReferLevel = (null == strReferLevel) ? "2" : strReferLevel;
//			
//			if(null != tag && !tag.equals("")) {
//				this.tag = tag;
//			}else{
//				this.tag = this.referId + this.locationId;
//			}
//		}
		
	}

	private static class TrackHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			execute((ITrack.Option) msg.obj);
		}
	}

	/**
	 * 获取SIM卡的IMSI码 SIM卡唯一标识：IMSI 国际移动用户识别码 （IMSI：International Mobile
	 * Subscriber Identification Number）是区别移动用户的标志， 储存在SIM卡中，可用于区别移动用户的有效信息。
	 * IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
	 * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成， 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,
	 * 中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。 唯一地识别国内GSM移动通信网中移动客户。
	 * 所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
	 */
	public static void getTrackPackage() {
		TrackPackage.NETTYPE = getNetType();
		if (DeviceInfoFetched) {
			return;
		}

		DeviceInfoFetched = true;
		TelephonyManager ts = (TelephonyManager) IcsonApplication.app.getSystemService(Context.TELEPHONY_SERVICE);
		String guid =  ts.getSubscriberId();
		
		final int nMinLength = 2;
		if( (TextUtils.isEmpty(guid)) || (guid.length() <= nMinLength) ){
			guid = Secure.getString(IcsonApplication.app.getContentResolver(), Secure.ANDROID_ID);;
		}
		
		if( (TextUtils.isEmpty(guid)) || (guid.length() <= nMinLength) ){
			// Try another way to retrieve device id.
			TelephonyManager pManager = (TelephonyManager) IcsonApplication.app.getSystemService(Context.TELEPHONY_SERVICE);
			guid = pManager.getDeviceId();
		}
		
		if( (TextUtils.isEmpty(guid)) || (guid.length() <= nMinLength) ){
			guid = StatisticsUtils.getDeviceUid(IcsonApplication.app);
		}
		
		TrackPackage.ISMI = guid;
		TrackPackage.SCREEN_WIDTH = ToolUtil.getEquipmentWidth(IcsonApplication.app);
		TrackPackage.SCREEN_HEIGHT = ToolUtil.getEquipmentHeight(IcsonApplication.app);
		TrackPackage.APP_VERSION = IVersion.getVersionName();
		TrackPackage.MOBILE_NAME = android.os.Build.MODEL;
		TrackPackage.SYSTEM_VERSION = android.os.Build.VERSION.RELEASE;
		TrackPackage.CHANNEL = getChannleId();

	}

	private static String getChannleId() {
		return ToolUtil.getChannel();
	}
	
	private static String getUserAgent() {
		return 	"ismi:" + TrackPackage.ISMI + "|netType:" + TrackPackage.NETTYPE + "|resolution:" + TrackPackage.SCREEN_WIDTH + "*" + TrackPackage.SCREEN_HEIGHT + "|appVersoin:" + TrackPackage.APP_VERSION + "|mobile:" + TrackPackage.MOBILE_NAME + "|androidVersion:" + TrackPackage.SYSTEM_VERSION + "|compile:" + Config.COMPILE_TIME;
	}

	private static class TrackPackage {
		public static String ISMI;
		public static String NETTYPE;
		public static int SCREEN_WIDTH;
		public static int SCREEN_HEIGHT;
		public static String APP_VERSION;
		public static String MOBILE_NAME;
		public static String SYSTEM_VERSION;
		public static String CHANNEL;
	}

	private static String getNetType() {
		long now = ToolUtil.getCurrentTime();

		if (now - LATEST_FETCH_NETTYPE_TIME < FETCH_NETTYPE_TIME_OFFSET) {
			return mNetType;
		}

		LATEST_FETCH_NETTYPE_TIME = now;
		final int netType = HttpUtil.getNetType(IcsonApplication.app);
		mNetType = netType == HttpUtil.WIFI ? "wifi" : netType == HttpUtil.NET ? "net" : netType == HttpUtil.WAP ? "wap" : "none";

		return mNetType;

	}
	
	
}
