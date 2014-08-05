package com.icson.util;


public class Config {
	
	//是否是debug版本
	public static final boolean DEBUG = true;
	//是否是给客户的测试版本
	public static final boolean isCustomerTestVersion = false;
	
	public static final String COMPILE_TIME = "04/15/2014";
	//微信插件id
	public static final String APP_ID = "wx6964eb0b10aa369b";
//	public static final String APP_ID = "wxb2acd799238987b6";
	//FORM_ID = "wx6964eb0b10aa369b";
	//DEBUG_ID = "wxb2acd799238987b6";
	//public static final String BEACON_APPKEY =  "0E50029ESC0GJOOV";
 
	public static final String NORMAL_ERROR = "悲剧, 出错了~";

	public static final String SERVER_ERROR = "服务器端错误";

	public static final String NET_RROR = "网络错误";

	public static final int NOT_LOGIN = 500;

	public static final String CART_NUM = "cart_num";

	public static final String BROADCAST_SHOPPING = "com.icson.shoppingcartreceiver";

	public static final String BROADCAST_TRACE = "com.icson.tracereceiver";

	public static final String BROADCAST_NEW_VERSION = "com.icson.newversion";
	
	public static final String BROADCAST_FROM_WXSHARE = "com.icson.wxreceiver.share";
	
	public static final String BROADCAST_FROM_WXLOGIN = "com.icson.wxloginreceiver";
	
	public static final String SLEF_BROADCAST_PERMISSION = "com.icson.permission.self_broadcast";
	
	// 同时请求图片的最大个数
	public static final int MAX_ASYNC_IMAGE_NUM = 20;

	// 内存缓存图片最大个数
	public static final int MAX_SDRAM_PIC_NUM = 30;

	// 手机数据库版本号
	public static final int INNER_DATABASE_VERSION = 2;

	// 手机数据库名称
	public static final String INNER_DATABASE_NAME = "icson_51buy.db";

	// 内存卡数据库名称
	public static final int SD_DATABASE_VERSION = 8;

	// 内存卡数据库名称
	public static final String SD_DATABASE_NAME = "icson_51buy.db";

	// 内存卡数据库版本标识
	public static final String SD_DATABASE_VERSION_NAME = "sdcard_database_version";
 
	// 网络get请求超时时间
	public static final int GET_DATA_TIME_OUT = 20 * 1000;

	// 网络post请求超时时间
	public static final int POST_DATA_TIME_OUT = 5 * 1000;

	// 网络连接超时时间
	public static final int CONNECT_TIME_OUT = 5 * 1000;

	//活动錧cache时间
	public static final int CHANNEL_CACHE_TIME = 5 * 60;
	
	// 导航缓存时间
	public static final int CATEGORY_CACHE_TIME = 3600 * 24 * 5;

	public static final String LOADING_MESSAGE = "正在加载, 请稍后...";

	public static final String TMPDIRNAME = "51buy";

	public static final int[] VALIDATESITECONFIG = { 1, 1001, 2001 , 3001, 4001,5001};

	public static final String COOKIE_SITE = "wsid";
	
	public static final String COOKIE_SITE_SC = "ws_c";

	public static final String LOG_NAME = "fatal_error.log";

	// Folder name for local image cache
	public static final String PIC_CACHE_DIR    = "pic_cache";
	
	public static final String CHANNEL_PIC_DIR  = "channel_pic";
	
	public static final String MY_FAVORITY_DIR  = "my_favor_pic";
	
	public static final String MY_ORDERLIST_DIR = "my_orderlist_pic";
	
	public static final String QIANG_PIC_DIR    = "qiang_pic";
	
	public static final String TUAN_PIC_DIR     = "tuan_pic";
	
	public static final String SLOT_STORAGE_DIR    = "slot_info";
	
	public static final String GJP_URL = "http://m.51buy.com/touch-gui.html?source=gui";
	public static final String JGBH_URL = "http://m.51buy.com/touch-gui.html?source=price";
	
	public static final int PROINFO_WIDTH = 680;
	public static final int PROINFO_HEIGHT = 290;

	// 图片保存在SD卡的最长时间
	public static final long PIC_CACHE_DIR_TIME = 5 * 24 * 3600 * 1000;
//	public static final long PIC_CACHE_DIR_TIME = 5 * 1000;
	
	//sd卡最小剩余空间检查, 单位M
	public static final long MIN_SD_SIZE_SPARE = 5;
	
	// Max cache for gallery count.
	public static final int MAX_GALLERY_CACHE = 8;
	
	// Extra data key definition.
	public static final String EXTRA_BARCODE = "barcode";
	public static final String EXTRA_PUSHMSG = "pushmsg";
	public static final String EXTRA_WEIXIN = "weixin_url";
	//public static final String EXTRA_ALI_USERID = "alipay_user_id";
	
	////////// Server api configuration.
	public static final String URL_AFTERSALE_ORDER_LIST = "URL_AFTERSALE_ORDER_LIST";
	public static final String URL_AFTERSALE_ORDER_DETAIL = "URL_AFTERSALE_ORDER_DETAIL";
	public static final String URL_AFTERSALE_ORDER_PROMPT = "URL_AFTERSALE_ORDER_PROMPT";
	public static final String URL_CATEGORY_LIST = "URL_CATEGORY_LIST";
	public static final String URL_EVENT_PAGE = "URL_EVENT_PAGE";
	public static final String URL_EVENT_MORNING = "URL_EVENT_MORNING";
	public static final String URL_EVENT_THH = "URL_EVENT_THH";
	public static final String URL_EVENT_WEEKEND = "URL_EVENT_WEEKEND";
	public static final String URL_SEARCH_FILTER = "URL_SEARCH_FILTER";
	public static final String URL_EVENT_HOMEVPAY = "URL_EVENT_HOMEVPAY";
	public static final String URL_DISPATCH_SITE = "URL_DISPATCH_SITE";
	public static final String URL_PRODUCT_DETAIL = "URL_PRODUCT_DETAIL";
	public static final String URL_ADD_PRODUCT_NOTICE = "URL_ADD_PRODUCT_NOTICE";
	public static final String URL_PRODUCT_INTRO = "URL_PRODUCT_INTRO";
	public static final String URL_PRODUCT_PARAMETERS = "URL_PRODUCT_PARAMETERS";
	public static final String URL_CHECK_VERSION = "URL_CHECK_VERSION";
	public static final String URL_SEARCH_PAGE = "URL_SEARCH_PAGE";
	public static final String URL_ALIPAY_LOGIN = "URL_ALIPAY_LOGIN";
	public static final String URL_WT_LOGIN = "URL_WT_LOGIN";
	public static final String URL_WECHAT_LOGIN = "URL_WECHAT_LOGIN";
	public static final String URL_LOTTERY_GETINFO = "URL_LOTTERY_GETINFO";
	public static final String URL_LOTTERY_DRAWNOW = "URL_LOTTERY_DRAWNOW";
	public static final String URL_LOTTERY_GETMYCODE = "URL_LOTTERY_GETMYCODE";
	public static final String URL_LOTTERY_GETWONCODE = "URL_LOTTERY_GETWONCODE";
	public static final String URL_GET_MESSAGES = "URL_GET_MESSAGES";
	public static final String URL_SET_MESSAGE_STATUS = "URL_SET_MESSAGE_STATUS";
	public static final String URL_FB_GET_TYPE = "URL_FB_GET_TYPE";
	public static final String URL_FEEDBACK_ADD = "URL_FEEDBACK_ADD";
	public static final String URL_FB_ADD_NEW = "URL_FB_ADD_NEW";
	public static final String URL_FB_IMAGE_STREAM_UPLOAD = "URL_FB_IMAGE_STREAM_UPLOAD";
	public static final String URL_RECOMMEND_LOADLIST = "URL_RECOMMEND_LOADLIST";
	public static final String URL_SEARCH_GETBYIDS = "URL_SEARCH_GETBYIDS";
	public static final String URL_FB_GET_HISTORY = "URL_FB_GET_HISTORY";
	public static final String URL_EVENT_COUPON = "URL_EVENT_COUPON";
	public static final String URL_GET_USER_COUPON = "URL_GET_USER_COUPON";
	public static final String URL_ITEM_GETVOTES = "URL_ITEM_GETVOTES";
	public static final String URL_CHECK_USER_COUPON = "URL_CHECK_USER_COUPON";
	public static final String URL_PREORDER_SHIPPINGTYPE = "URL_PREORDER_SHIPPINGTYPE";
	public static final String URL_GET_USER_CAN_USE_POINT = "URL_GET_USER_CAN_USE_POINT";
	public static final String URL_SMSCODE_GET = "URL_SMSCODE_GET";
	public static final String URL_ORDER_LISTPAGE = "URL_ORDER_LISTPAGE";
	public static final String URL_ORDER_DETAIL = "URL_ORDER_DETAIL";
	public static final String URL_ORDER_SHIP_PAYTYPE = "URL_ORDER_SHIP_PAYTYPE";
	public static final String URL_PUSHNOTIFY_GET = "URL_PUSHNOTIFY_GET";
	public static final String URL_EVENT_QIANG = "URL_EVENT_QIANG";
	public static final String URL_EVENT_QIANG_NEXT = "URL_EVENT_QIANG_NEXT";
	public static final String URL_HOT_SEARCH_WORDS = "URL_HOT_SEARCH_WORDS";
	public static final String URL_POST_LOG = "URL_POST_LOG";
	public static final String URL_LIST_ORDER_ONEKEYBUY = "URL_LIST_ORDER_ONEKEYBUY";
	public static final String URL_UPLOAD_ALERT = "URL_UPLOAD_ALERT";
	public static final String URL_USERINFO_UPDATE = "URL_USERINFO_UPDATE";
	public static final String URL_EVENT_TUAN = "URL_EVENT_TUAN";
	public static final String URL_DOWNLOAD_VOICESEARCH = "URL_DOWNLOAD_VOICESEARCH";
	public static final String URL_RECHARGE_MOBILE_PAYMENT = "URL_RECHARGE_MOBILE_PAYMENT";
	public static final String URL_RECHARGE_MOBILE_INFO = "URL_RECHARGE_MOBILE_INFO";
	public static final String URL_RECHARGE_MOBILE_MONEY = "URL_RECHARGE_MOBILE_MONEY";
	public static final String URL_ALERT_INFO = "URL_ALERT_INFO";
	public static final String URL_EVENT_TIMEBUY = "URL_EVENT_TIMEBUY";
	public static final String URL_ORDER_CONFIRM = "URL_ORDER_CONFIRM";
	public static final String URL_MSGOP_SPLASH = "URL_MSGOP_SPLASH";
	public static final String URL_HOT_PORDUCTS = "URL_HOT_PORDUCTS";
	public static final String URL_RECHARGE_INFO = "URL_RECHARGE_INFO";
	
	// 2. base.51buy.com
//	public static final String URL_ADDRESS_LIST = "URL_ADDRESS_LIST";
//	public static final String URL_ADDRESS_ADD = "URL_ADDRESS_ADD";
//	public static final String URL_ADDRESS_UPDATE = "URL_ADDRESS_UPDATE";
//	public static final String URL_ADDRESS_DEL = "URL_ADDRESS_DEL";
//	public static final String URL_INVOICE_LIST = "URL_INVOICE_LIST";
//	public static final String URL_INVOICE_ADD = "URL_INVOICE_ADD";
//	public static final String URL_INVOICE_UPDATE = "URL_INVOICE_UPDATE";
//	public static final String URL_FAVOR_LIST = "URL_FAVOR_LIST";
//	public static final String URL_FAVOR_REMOVE = "URL_FAVOR_REMOVE";
//	public static final String URL_FAVOR_ADD = "URL_FAVOR_ADD";
	public static final String URL_REGISTER = "URL_REGISTER";
	public static final String URL_ACCOUNT_EXISTS = "URL_ACCOUNT_EXISTS";
	public static final String URL_EMAIL_EXISTS = "URL_EMAIL_EXISTS";
//	public static final String URL_USER_PROFILE = "URL_USER_PROFILE";
//	public static final String URL_ORDER_FLOW = "URL_ORDER_FLOW";
//	public static final String URL_DELIVERY_FLOW_INFO = "URL_DELIVERY_FLOW_INFO";
//	public static final String URL_ORDER_CANCEL = "URL_ORDER_CANCEL";
//	public static final String URL_LOGIN_STATUS = "URL_LOGIN_STATUS";
//	public static final String URL_ICSON_LOGIN = "URL_ICSON_LOGIN";
	
	// 3. event.51buy.com
	public static final String URL_GET_COUPON_EVTNO = "URL_GET_COUPON_EVTNO";
	
	// 4. item.51buy.com
	public static final String URL_PRODUCT_REVIEWS = "URL_PRODUCT_REVIEWS";
	public static final String URL_ADD_COMMENT = "URL_ADD_COMMENT";
	
	// 5. www.51buy.com
//	public static final String URL_QUERY_KEYWORD = "URL_QUERY_KEYWORD";
	
	// 6. st.icson.com
	public static final String URL_IMAGE_GUEST = "URL_IMAGE_GUEST";
	public static final String URL_AREA_JS = "URL_AREA_JS";
	
	// 7. buy.51buy.com
//	public static final String URL_ADD_PRODUCT = "URL_ADD_PRODUCT"; // Not used since 1.1.4
	public static final String URL_GET_PRODUCT_COUPON = "URL_GET_PRODUCT_COUPON";
//	public static final String URL_NEW_ORDER = "URL_NEW_ORDER"; // Not used since 1.1.4
//	public static final String URL_CART_UPDATE = "URL_CART_UPDATE"; // Not used since 1.1.4
//	public static final String URL_CART_REMOVE = "URL_CART_REMOVE"; // Not used since 1.1.4
//	public static final String URL_LIST_CART = "URL_LIST_CART"; // Not used since 1.1.4
//	public static final String URL_ORDER_ITEMS = "URL_ORDER_ITEMS"; // Not used since 1.1.4
	public static final String URL_LIST_CART_NONMEMBER = "URL_LIST_CART_NONMEMBER";
//	public static final String URL_LIST_CART_WITH_PROVINCEID = "URL_LIST_CART_WITH_PROVINCEID"; // Not used since 1.1.4
	
	// 8. pay.51buy.com
	public static final String URL_PAY_TRADE = "URL_PAY_TRADE";
	
	// 9. mc.
	public static final String URL_SUBMIT_ORDER = "URL_SUBMIT_ORDER";
	public static final String URL_ORDER_GETLIST = "URL_ORDER_GETLIST";
	public static final String URL_ORDER_GETDETAIL = "URL_ORDER_GETDETAIL";
	public static final String URL_ORDER_GETFLOW = "URL_ORDER_GETFLOW";
	public static final String URL_ORDER_GETTRACE = "URL_ORDER_GETTRACE";
	public static final String URL_REDRESS_GIS = "URL_REDRESS_GIS";
//	public static final String URL_ORDER_GETCOUPON = "URL_ORDER_GETCOUPON";
	public static final String URL_CART_GET_PRODUCT_LIST = "URL_CART_GET_PRODUCT_LIST";
	public static final String URL_CART_ADD_PRODUCTS = "URL_CART_ADD_PRODUCTS";
	public static final String URL_CART_ADD_PRODUCT_NOTLOGIN = "URL_CART_ADD_PRODUCT_NOTLOGIN";
	public static final String URL_CART_REMOVE_PRODUCT = "URL_CART_REMOVE_PRODUCT";
	public static final String URL_CART_UPDATE_PRODUCT = "URL_CART_UPDATE_PRODUCT";
	public static final String URL_CANCEL_ORDER = "URL_CANCEL_ORDER";
	
	// 10. mb
	public static final String URL_MB_USER_PROFILE = "URL_MB_USER_PROFILE";
	public static final String URL_MB_USER_POINTS = "URL_MB_USER_POINTS";
	public static final String URL_MB_USER_BALANCE = "URL_MB_USER_BALANCE";
	public static final String URL_ADDRESS_ADDNEW = "URL_ADDRESS_ADDNEW";
	public static final String URL_ADDRESS_DELETE = "URL_ADDRESS_DELETE";
	public static final String URL_ADDRESS_GETLIST = "URL_ADDRESS_GETLIST";
	public static final String URL_ADDRESS_MODIFY = "URL_ADDRESS_MODIFY";
	public static final String URL_FAVOR_ADDNEW = "URL_FAVOR_ADDNEW";
	public static final String URL_FAVOR_GETLIST = "URL_FAVOR_GETLIST";
	public static final String URL_FAVOR_DELETE = "URL_FAVOR_DELETE";
	public static final String URL_APP_LOGIN = "URL_APP_LOGIN";
	public static final String URL_INVOICE_ADDNEW = "URL_INVOICE_ADDNEW";
	public static final String URL_INVOICE_GETLIST = "URL_INVOICE_GETLIST";
	public static final String URL_INVOICE_MODIFY = "URL_INVOICE_MODIFY";
	public static final String URL_INVOICE_DELETE = "URL_INVOICE_DELETE";
	public static final String URL_LOGIN_GETSTATUS = "URL_LOGIN_GETSTATUS";
	public static final String URL_QUERY_SUGGEST = "URL_QUERY_SUGGEST";
	public static final String URL_HOME_GETINFO = "URL_HOME_GETINFO";
	public static final String URL_CATEGORY_TREE = "URL_CATEGORY_TREE";
	public static final String URL_SEARCH_NEW = "URL_SEARCH_NEW";
	public static final String URL_CATEGORY_NEW = "URL_CATEGORY_NEW";
	public static final String URL_GUIDE_GETCOUPON = "URL_GUIDE_GETCOUPON";
	public static final String URL_GUIDE_PLANIMG = "URL_GUIDE_PLANIMG";
	public static final String URL_FULL_DISTRICT = "URL_FULL_DISTRICT";  //三级地址
	
	
	// Others
	public static final String URL_APP_TRACK = "URL_APP_TRACK";
	public static final String URL_MSP_ALIPAY = "URL_MSP_ALIPAY";
	
	
	//slotMachine
	public static final String URL_MB_ROLL_INFO     = "URL_MB_ROLL_INFO";
	public static final String URL_SLOT_ROLL     = "URL_SLOT_ROLL";
	public static final String URL_MB_ROLL_SHARE    = "URL_MB_ROLL_SHARE";
	public static final String URL_REWARD_HISTORY  = "URL_REWARD_HISTORY";
	public static final String URL_SLOT_BULLETIN     = "URL_SLOT_BULLETIN";
	public static final String URL_MB_ROLL_LOGIN_NOTICE  = "URL_MB_ROLL_LOGIN_NOTICE";

	//拆单
	public static final String URL_ORDER_CONFIRM_NEW  = "URL_ORDER_CONFIRM_NEW";
	public static final String URL_ORDER_CONFIRM_SERVICE  = "URL_ORDER_CONFIRM_SERVICE";
	
	//统一登录 
	public static final String URL_UNION_LOGIN = "URL_UNION_LOGIN";
	
	//99max
	public static final int    MAXNUM_PER_ORDER = 99;
	
	public static final int    FIRST_SIGHT_FADING_TIME = 5000;

	
}
