package com.icson.paytype;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.pay.PayFactory;

public class PayTypeModel {
	
	private int sortId;
	
	private int payType;
	
	private String PayTypeName;
	
	private boolean isNetBank;
	
	private boolean IsnNet;
	
	private int orderNumber;
	
	private boolean needPrcdFee;
	
	private String payTypeDesc;
	
	public int getSortId() {
		return sortId;
	}

	public void setSortId(int aId) {
		this.sortId = aId;
	}
	
	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public String getPayTypeName() {
		return PayTypeName;
	}

	public void setPayTypeName(String payTypeName) {
		PayTypeName = payTypeName;
	}

	public boolean isNetBank() {
		return isNetBank;
	}

	public void setNetBank(boolean isNetBank) {
		this.isNetBank = isNetBank;
	}

	public boolean isIsnNet() {
		return IsnNet;
	}

	public void setIsnNet(boolean isnNet) {
		IsnNet = isnNet;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public boolean isNeedPrcdFee() {
		return needPrcdFee;
	}

	public void setNeedPrcdFee(boolean needPrcdFee) {
		this.needPrcdFee = needPrcdFee;
	}
	
	public String getPayTypeDesc() {
		return payTypeDesc;
	}

	public void setPayTypeDesc(String payTypeDesc) {
		this.payTypeDesc = payTypeDesc;
	}
	
	public void parse(JSONObject json) throws JSONException{
		setSortId( json.optInt("sortId"));
		setPayType( json.getInt("pay_type") );
		setPayTypeName( getPayType() == PayFactory.PAY_ALI ? "支付宝" : json.getString("PayTypeName") );
		setPayTypeDesc( json.getString("PayTypeDesc") );
		setNetBank( json.optInt("IsNetBank") == 1 );
		setIsnNet( json.optInt("IsNet") == 1 );
		setPayTypeDesc( json.getString("PayTypeDesc") );
		setOrderNumber( json.getInt("OrderNumber") );
		setNeedPrcdFee( json.getInt("needPrcdFee") == 1 );
	}
} 