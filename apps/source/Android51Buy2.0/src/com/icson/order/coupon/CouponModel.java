package com.icson.order.coupon;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

@SuppressWarnings("serial")
public class CouponModel extends BaseModel implements Serializable{
	
	// 用户优惠券
	public String code;
	public int evtno;
	public String content;
	public long coupon_amt;
	public String valid_time_from;
	public String valid_time_to;
	public int status;
	
	public boolean isUseNow;
	
	public void parser(JSONObject o) throws JSONException{
		code = o.optString("code");
		evtno = o.optInt("evtno");
		content = o.optString("content");
		coupon_amt = o.optLong("coupon_amt");
		valid_time_from = o.optString("valid_time_from");
		valid_time_to = o.optString("valid_time_to");
		status = o.optInt("status");
		
	}
	
}
