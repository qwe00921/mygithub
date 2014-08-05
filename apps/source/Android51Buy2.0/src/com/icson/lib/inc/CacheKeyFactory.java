package com.icson.lib.inc;

public class CacheKeyFactory {
	// 商品分类列表
	public static final String CACHE_BLOCK_CATEGORY = "cache_block_category";

	// 确认订单：发票ID
	public static final String CACHE_ORDER_INVOICE_ID = "cache_order_invoice_id";

	// 确认订单：地址ID
	public static final String CACHE_ORDER_ADDRESS_ID = "cache_order_address_id";
	
	// 确认订单：地址区域ID
	public static final String CACHE_ORDER_DISTRICT_ID = "cache_order_district_id";
	
	// 确认订单：配送方式ID
	public static final String CACHE_ORDER_SHIPPING_TYPE_ID = "cache_order_shippingtype_id";

	// 确认订单：支付方式ID
	public static final String CACHE_ORDER_PAY_TYPE_ID = "cache_order_pay_id";

	// 购物车
	public static final String CACHE_SHOPPINGCART_ITEMS = "shopping_cart_items";
	
	//浏览历史
	public static final String CACHE_VIEW_HISTORY_ITEMS = "view_history_items";

	//收藏
	public static final String CACHE_VIEW_COLLECT_ITEMS = "collect_items";
	
	//dispatches information
	public static final String CACHE_DISPATCHES_INFO = "cache_dispatches_info";
	
	//配送地址信息
	public static final String CACHE_SHIPPING_AREA= "cache_shippingarea_string";
	
	//活动内容
	public static final String CACHE_EVENT= "cache_event_";
	
	//最新版本询问时间
	public static final String LAST_VERSION_QUERY_TIME = "latest_version_query_time";

	//最后选择版本时间
	public static final String LAST_VERSION_SELECT_TIME = "latest_version_select_time";

	//最新版本信息
	public static final String LAST_VERSION_INFO = "latest_version_info";
	
	//首页运营錧
	public static final String HOME_CHANNEL_INFO = "home_channel_info";
	
	//最后配送的城市ID
	public static final String CACHE_CITY_ID = "cache_city_id";
	
	//城市对应的站点ID
//	public static final String CACHE_SITE_ID = "cache_site_id";
	
	//Search history words
	public static final String CACHE_SEARCH_HISTORY_WORDS = "search_history_words_key";
	
	//Full district addresses
	public static final String CACHE_FULL_DISTRICT = "full_district_addresses";
	public static final String CACHE_FULL_DISTRICT_MD5 = "full_district_addresses_md5";
	
	//default address district
	public static final String CACHE_DEFAULT_ADDRESS_DISTRICT = "default_address_district";
}
