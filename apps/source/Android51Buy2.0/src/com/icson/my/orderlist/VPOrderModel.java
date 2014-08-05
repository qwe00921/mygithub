package com.icson.my.orderlist;

import org.json.JSONObject;

import com.icson.lib.model.OrderModel;

@SuppressWarnings("serial")
public class VPOrderModel extends OrderModel {

	/*
	 * "order_char_id":"1021554807", "order_id":21554807,
	 * "order_date":"2012-10-17 16:48:57", "order_cost":"98.75", "status":-1,
	 * "status_name":"已作废", "order_create_time":"1350463737",
	 * "receiver":"18616995768", "pay_type":21, "pay_type_name":"支付宝",
	 * "product_list_str":"上海联通100元充值卡"
	 */

	private String product_list_str;

	private double card_money;
	
	public String getProduct_list_str() {
		return product_list_str;
	}
	
	public double getCard_money() {
		
		return card_money;
	}
	public void setCard_money(double money) {
		card_money = money;
	}

	public void setProduct_list_str(String product_list_str) {
		this.product_list_str = product_list_str;
	}

	public void parse(JSONObject v) throws Exception {

		setOrderCharId(v.getString("order_char_id"));
		setOrderId(v.getString("order_id"));
		setOrderDate(v.getLong("order_date"));
		setOrderCost(v.getDouble("order_cost"));
		setCard_money(v.getDouble("card_money"));
		setStatus(v.getInt("status"));
		setStatus_name(v.optString("status_name"));
		setReceiver(v.getString("receiver"));
		setPayType(v.getInt("pay_type"));
		setPayTypeName(v.optString("pay_type_name", ""));
		setProduct_list_str(v.optString("product_list_str", ""));
		setNeedPay(getStatus() == 0);
		setCanCancel(false);

	}
}
