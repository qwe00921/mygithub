package com.icson.lib.model;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.IcsonProImgHelper;
import com.icson.util.ToolUtil;

/**
 * @author xuemingwang
 * 
 */
public class ProductModel extends BaseModel implements Serializable {
	private static final long serialVersionUID = 1L;
	public final static int SALE_AVAILABLE = 1;
	public final static int SALE_EMPTY = 2;
	public final static int SALE_UNAVAILABLE = 3;

	// 是否正在团购
	private boolean isTuanIng;
	// 是否有促销规则
	private int rules;
	// 品名
	private String name;
	// 商品ID
	private long productId;
	// 场景CHANNEL_ID
	private int channelId;

	// 商品编号
	private String productCharId;
	// 促销语
	private String promotionWord;
	// 市场价
	private double marketPrice;
	// 易迅价
	private double showPrice;
	// 多价格
	private int price_id;
	private double discount_price;
	private String discount_p_name;
	// 是否可以使用优惠券
	private boolean canUseCoupon;
	// 是否可以增值税发票
	private boolean canVAT;
	// 是否贵就赔
	private boolean isGJP;
	private String GJP_url;
	// 是否价格保护
	private boolean isJGBH;
	private String JGBH_url;
	// 是否节能补贴
	private boolean isES;
	// 图片个数
	private int picNum;
	// 库存情况
	private String stock;
	// 配送限制
	private String restrictedTransType;
	// 限买个数
	private int NumLimit;
	// 最低购买个数
	private int lowestNum;
	// 销售状态(1 : 在售, 2: 已售完：2, 3: 暂不销售 )
	private int saleType;
	// number of gift
	private int gift_count;
	// 商品简介
	private String intro;
	private long mainProductId;

	private String dap;//看了看，买了买跟踪
	
	private String productConflictDesc;//不可购买的具体出错原因
	private int productConflictState;//是否可正常销售，0可以，非0不可以
	
	private String mainpic; //图片url
	//存储图片的索引 如[2,3,6]
	private int[] indexArray = null;
	
	public String getMainPic() {
		return mainpic;
	}

	public void setMainPic(String mainpic) {
		this.mainpic = mainpic;
	}

	public String getDAP() {
		return dap;
	}

	public boolean isTuanIng() {
		return isTuanIng;
	}

	public void setTuanIng(boolean isTuanIng) {
		this.isTuanIng = isTuanIng;
	}

	public boolean isGJP() {
		return isGJP;
	}

	public void setGJP(boolean isGJP) {
		this.isGJP = isGJP;
	}

	public String getGJPURL() {
		return GJP_url;
	}

	public boolean isJGBH() {
		return isJGBH;
	}

	public void setJGBH(boolean isJGBH) {
		this.isJGBH = isJGBH;
	}

	public boolean isES() {
		return isES;
	}

	public void setES(boolean isES) {
		this.isES = isES;
	}

	public boolean canUseVAT() {
		return canVAT;
	}

	public void setVAT(boolean canVAT) {
		this.canVAT = canVAT;
	}

	public boolean canUseCoupon() {
		return canUseCoupon;
	}

	public String getJGBHURL() {
		return JGBH_url;
	}

	public void setUseCoupon(boolean canUseCoupon) {
		this.canUseCoupon = canUseCoupon;
	}

	public long getMainProductId() {
		return mainProductId;
	}

	public void setMainProductId(long mainProductId) {
		this.mainProductId = mainProductId;
	}

	public String getName() {
		return name;
	}

	public String getNameNoHTML() {

		if (name == null)
			return "";

		return name.replaceAll("<[^>]+>([^<]*)</[^>]+>", "$1");
		/*
		 * Pattern p = Pattern.compile("<[^>]+>([^<]*)</[^>]+>"); Matcher m =
		 * p.matcher( str ); while (m.find()) { str =
		 * str.replaceFirst("<[^>]+>([^<]*)</[^>]+>", m.group(1).toString()); }
		 * 
		 * return str;
		 */
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getProductCharId() {
		return productCharId;
	}

	public void setProductCharId(String productCharId) {
		this.productCharId = productCharId;
	}

	public String getPromotionWord() {
		return promotionWord;
	}

	public void setPromotionWord(String promotionWord) {
		this.promotionWord = promotionWord;
	}

	public double getMarketPrice() {
		return marketPrice;
	}

	public String getMarketPriceStr() {
		return ToolUtil.toPrice(marketPrice);
	}

	public void setMarketPrice(double marketPrice) {
		this.marketPrice = marketPrice;
	}

	public double getShowPrice() {
		return showPrice;
	}

	public String getShowPriceStr() {
		return ToolUtil.toPrice(showPrice, 2);
	}

	public void setShowPrice(double showPrice) {
		this.showPrice = showPrice;
	}

	public String getAdapterProductUrl(int dip) {
		return getAdapterProductUrl(dip, 0);
	}

	public String getAdapterProductUrl(int dip, int index) {
		return IcsonProImgHelper.getAdapterPicUrl(productCharId, dip, index);
	}

	public String getProductUrl(int size) {
		return getProductUrl(size, 0);
	}

	public String getProductUrl(int size, int index) {
		return IcsonProImgHelper.getPicUrl(productCharId, size, index);
	}

	public int getPicNum() {
		return picNum;
	}

	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}

	public String getStock() {
		return stock;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public String getRestrictedTransType() {
		return restrictedTransType;
	}

	public void setRestrictedTransType(String restrictedTransType) {
		this.restrictedTransType = restrictedTransType;
	}

	public int getNumLimit() {
		return NumLimit < 1 ? 999999 : NumLimit;
	}

	public void setNumLimit(int numLimit) {
		NumLimit = numLimit;
	}

	public int getLowestNum() {
		return lowestNum < 1 ? 1 : lowestNum;
	}

	public void setLowestNum(int lowest_Num) {
		lowestNum = lowest_Num;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public void setSaleType(int saleType) {
		this.saleType = saleType;
	}

	public int getSaleType() {
		return saleType;
	}

	public void setGiftCount(int giftCount) {
		this.gift_count = giftCount;
	}

	public int getGiftCount() {
		return gift_count;
	}

	public int getRules() {
		return rules;
	}

	public void setRules(int rules) {
		this.rules = rules;
	}

	public int getPrice_id() {
		return price_id;
	}

	public void setPrice_id(int price_id) {
		this.price_id = price_id;
	}

	public double getDiscount_price() {
		return discount_price;
	}

	public void setDiscount_price(double discount_price) {
		this.discount_price = discount_price;
	}

	public String getDiscount_p_name() {
		return discount_p_name;
	}

	public void setDiscount_p_name(String discount_p_name) {
		this.discount_p_name = discount_p_name;
	}

	public void setProductConflictDesc(String productConflictDesc) {
		this.productConflictDesc = productConflictDesc;
	}
	public String getProductConflictDesc(){
		return this.productConflictDesc;
	}

	public void setProductConflictState(int productConflictState) {
		this.productConflictState = productConflictState;
	}

	public int getProductConflictState(){
		return this.productConflictState;
	}

	
	public int[] getIndexArray()
	{
		return this.indexArray;
	}
	
	public void setIndexArray(int[] indexArray)
	{
		this.indexArray = indexArray;
	}

	public void parse(JSONObject json) throws JSONException {
		setChannelId(json.optInt("channel_id", 0));
		setProductId(json.optLong("product_id", 0));
		setName(json.optString("name", ""));
		setProductCharId(json.optString("product_char_id", ""));
		setMarketPrice(json.optDouble("market_price", 0));
		setShowPrice(json.optDouble("show_price", 0));
		setPicNum(json.optInt("pic_num", 0));
		setPromotionWord(json.optString("promotion_word", ""));
		setGiftCount(json.optInt("gift_count", 0));
		setIntro(json.optString("intro", ""));
		setStock(json.optString("stock", ""));
		setSaleType(json.optInt("sale_type", SALE_EMPTY));
		setRestrictedTransType(json.optString("restricted_trans_type", ""));
		setNumLimit(json.optInt("num_limit", 999999));
		setLowestNum(json.optInt("lowest_num", 1));
		setTuanIng(json.optInt("tuaning", 0) == 1);
		setRules(json.optInt("rules", 0));
		setUseCoupon(json.optBoolean("canUseCoupon", true));
		setVAT(json.optBoolean("canVAT", true));
		setGJP(json.optBoolean("isGJP", false));
		setJGBH(json.optBoolean("isJGBH", false));
		setES(json.optBoolean("isES", false));
		setProductConflictDesc(json.optString("productConflictDesc", ""));
		setProductConflictState(json.optInt("productConflictState", 0));
		JGBH_url = json.optString("JGBH_url");
		GJP_url = json.optString("GJP_url");
		
		//如果index_array数据项不存在，那么indexArray为空
		if (!ToolUtil.isEmptyList(json, "index_array")) {
			final JSONArray indexes = json.getJSONArray("index_array");
			int length = indexes.length();
			indexArray = new int[length];
			for (int i = 0 ; i < length; i++) {
				int tmp = indexes.optInt(i);
				indexArray[i] = tmp;
				
			}
		}
		
		dap = json.optString("dap");
		setMainPic(json.optString("mainpic", ""));
	}
}
