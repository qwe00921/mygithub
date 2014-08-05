package com.icson.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

@SuppressWarnings("serial")
public class PromoRuleModel extends BaseModel implements Serializable{
	// 促销规则id
	private long ruleId;
	// 促销规则类型
	private int ruleType;
	
	private String desc;
	private String name;
	
	public static final int RULE_TYPE_CASH_AMT = 1;  //满金额
	//benefit_type: 1 立减, 2 送券, 3 折扣, 4 换购商品(加价购), 5 积分, 6 送商品（满赠）
	public static final int BENEFIT_TYPE_CASH 			= 1;
	public static final int BENEFIT_TYPE_COUPON 		= 2;
	public static final int BENEFIT_TYPE_LESSPRICEBUY 	= 4;
	public static final int BENEFIT_TYPE_FREEGIFT 		= 6;
	
	private int benefitType;//benefit_type: 1 立减, 2 送券, 3 折扣, 4 换购商品, 5 积分, 6 送商品
	// 每次优惠的金额
	private long benefits;
	// 该订单优惠的次数
	private int benefitTimes;
	// 每个订单最多优惠的次数
	private int applyTimePerOrder;
	// 每个用户最多优惠的次数
	private int applyTimePerUser;
	
	private long plusCon;
	// 参加活动需要满足的金额
	private long condition;
	
	private String url;
	
	private ArrayList<ProductOfPromoRuleModel> mProductList;
	private int mSelectNum;
	
	public void setRuleId(long id){
		this.ruleId = id;
	}
	
	public long getRuleId(){
		return ruleId;
	}
	
	public void setRuleType(int type){
		this.ruleType = type;
	}
	
	public int getRuleType(){
		return ruleType;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public void setBenefitType(int type){
		this.benefitType = type;
	}
	
	public int getBenefitType(){
		return benefitType;
	}
	
	public void setBenefits(long benifit){
		this.benefits = benifit;
	}
	
	public long getBenefits(){
		return benefits;
	}
	
	public void setBenefitTimes(int times){
		this.benefitTimes = times;
	}
	
	public int getBenefitTimes(){
		return benefitTimes;
	}
	
	public void setApplyTimePerOrder(int times){
		this.applyTimePerOrder = times;
	}
	
	public int getApplyTimePerOrder(){
		return applyTimePerOrder;
	}
	
	public void setApplyTimePerUser(int times){
		this.applyTimePerUser = times;
	}
	
	public int getApplyTimePerUser(){
		return applyTimePerUser;
	}
	
	public void setPlusCon(long con){
		this.plusCon = con;
	}
	
	public long getPlusCon(){
		return plusCon;
	}
	
	public void setCondition(long con){
		this.condition = con;
	}
	
	public long getCondition(){
		return condition;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setProducts(ArrayList<ProductOfPromoRuleModel> models) {
		this.mProductList = models;
	}
	
	public ArrayList<ProductOfPromoRuleModel> getProducts(){
		return this.mProductList;
	}
	
	public void setSelectNum(int num) {
		this.mSelectNum = num;
	}
	
	public int getSelectNum(){
		return this.mSelectNum;
	}
	
	
	public void parse(JSONObject json) throws JSONException {
		setRuleId(json.optInt("rule_id", 0));
		setRuleType(json.optInt("rule_type", 0));
		setDesc(json.optString("desc",""));
		setBenefitType(json.optInt("benefit_type", 0));
		setBenefits(json.optLong("benefits", 0));
		setBenefitTimes(json.optInt("benefit_times", 0));
		setApplyTimePerOrder(json.optInt("apply_time_perorder", 0));
		setApplyTimePerUser(json.optInt("apply_time_peruser", 0));
		setPlusCon(json.optLong("plus_con", 0));
		setCondition(json.optLong("condition", 0));
		setUrl(json.optString("url", ""));		
		setName(json.optString("name", ""));
		setSelectNum(json.optInt("selectNum", 0));
		
		//parse products  information in promotion rules
		if(!ToolUtil.isEmptyList(json, "productList")) {
			JSONArray arr = json.optJSONArray("productList");
			ArrayList<ProductOfPromoRuleModel> models = new ArrayList<ProductOfPromoRuleModel>();
			int nSize = arr.length();
			for(int nId = 0; nId < nSize; nId ++ ) {
				ProductOfPromoRuleModel productModel = new ProductOfPromoRuleModel();
				productModel.parse(arr.getJSONObject(nId));
				models.add(productModel);
			}
			
			setProducts(models);
		}
	}

}
