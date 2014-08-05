package com.icson.order.shippingtype;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.ToolUtil;

public class ShippingTypeModel extends BaseModel {
	private int id;

	private String name;

	private double PremiumRate;

	private double PremiumBase;

	private double FreeShipBase;

	private int OrderNumber;

	private String ShipTypeDesc;

	private double shippingPrice;

	private double shippingPriceCut;
	
	private double shippingCost;//运费
	private int free_type;//类型
	private double free_price_limit;//免运费订单最低额
	
	public double getShippingCost() {
		return shippingCost;
	}

	public int getFreeType() {
		return free_type;
	}

	public double getFreeShippingLimit() {
		return free_price_limit;
	}

	//CombineShipping
	private ArrayList<ShippingTypeTimeModel> mCombineShippingTimeList;
	
	public ArrayList<ShippingTypeTimeModel> getCombineShippingTimeList() {
		return mCombineShippingTimeList;
	}
	
	public void setCombineShippingTimeList(ArrayList<ShippingTypeTimeModel> aShippingTimeList) {
		this.mCombineShippingTimeList = aShippingTimeList;
	}
	
	private ArrayList<SubShippingTypeModel> mSubShippingTypeModelList;

	public ArrayList<SubShippingTypeModel> getSubShippingTypeModelList() {
		return mSubShippingTypeModelList;
	}

	public void setSubShippingTypeModelList(ArrayList<SubShippingTypeModel> mSubShippingTypeModelList) {
		this.mSubShippingTypeModelList = mSubShippingTypeModelList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPremiumRate() {
		return PremiumRate;
	}

	public void setPremiumRate(double premiumRate) {
		PremiumRate = premiumRate;
	}

	public double getPremiumBase() {
		return PremiumBase;
	}

	public void setPremiumBase(double premiumBase) {
		PremiumBase = premiumBase;
	}

	public double getFreeShipBase() {
		return FreeShipBase;
	}

	public void setFreeShipBase(double freeShipBase) {
		FreeShipBase = freeShipBase;
	}

	public int getOrderNumber() {
		return OrderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		OrderNumber = orderNumber;
	}

	public String getShipTypeDesc() {
		return ShipTypeDesc;
	}

	public void setShipTypeDesc(String shipTypeDesc) {
		ShipTypeDesc = shipTypeDesc;
	}

	public double getShippingPrice() {
		return shippingPrice;
	}

	public void setShippingPrice(double shippingPrice) {
		this.shippingPrice = shippingPrice;
	}

	public double getShippingPriceCut() {
		return shippingPriceCut;
	}

	public void setShippingPriceCut(double shippingPriceCut) {
		this.shippingPriceCut = shippingPriceCut;
	}

	@SuppressWarnings("unchecked")
	public void parse(JSONObject json) throws Exception{
		setPremiumBase(json.optDouble("PremiumBase"));
		setFreeShipBase( json.optDouble("FreeShipBase") );
		setOrderNumber(json.optInt("OrderNumber"));
		
		setId(json.getInt("ShippingId"));
		setName( json.getString("ShipTypeName") );
		setPremiumRate(json.getDouble("PremiumRate"));
		setShipTypeDesc(json.getString("ShipTypeDesc"));
		setShippingPrice(json.optDouble("shippingPrice",0));
		setShippingPriceCut(json.optDouble("shippingPriceCut",0));
		
		shippingCost = json.optDouble("shippingCost", 0);//运费
		free_type = json.optInt("free_type",0);//类型
		free_price_limit = json.optDouble("free_price_limit", 0);//免运费订单最低额

		//CombineShippingTime
		if( !ToolUtil.isEmptyList(json, "CombineShipList") ){
			mCombineShippingTimeList = new ArrayList<ShippingTypeTimeModel>();
			final JSONArray shippingTimes = json.getJSONArray("CombineShipList");
			for (int i = 0, len = shippingTimes.length(); i < len; i++) {
				JSONObject v = shippingTimes.getJSONObject(i);
				ShippingTypeTimeModel model = new ShippingTypeTimeModel();
				model.parse(v);
				if(model.getState()== ShippingTypeTimeModel.STATUS_OK)
					mCombineShippingTimeList.add(model);
			}
		}
		
		if( !ToolUtil.isEmptyList(json, "subOrder") ){
			mSubShippingTypeModelList = new ArrayList<SubShippingTypeModel>();
			final JSONObject v = json.getJSONObject("subOrder");
			Iterator<String> keys = v.keys();
			while(keys.hasNext()){
				final String key = keys.next();
				
				SubShippingTypeModel model = new SubShippingTypeModel();
				model.parse( key, v.getJSONObject(key) );
				mSubShippingTypeModelList.add(model);
			}
			
			setSubShippingTypeModelList(mSubShippingTypeModelList);
		}
	}
}
