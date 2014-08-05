/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: DeliverInfoParser.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-4-16
 */
package com.icson.lib.parser;

import org.json.JSONObject;

import com.icson.lib.model.DeliverInfoModel;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class DeliverInfoParser extends Parser<byte[], DeliverInfoModel> {

	public DeliverInfoModel parse(byte[] bytes, String charset) throws Exception {
		JSONParser parser = new JSONParser();
		
		DeliverInfoModel models = null;
		
		final JSONObject root = parser.parse(bytes, charset);
		
		//code {"msg":"No new version","errno":203}
		final int errno = null != root ? root.optInt("errno", -1) : -1;
		if(errno!=0)
		{
			return null;
		}
		/**{"errno":0,"errMsg":"",
		 * "profile":{"avator":"","order_id":"1288","name":"吴宝华","phone":"15821802342"},
		 * "branch":{"name":"上海静安大田分站","starttime":"2013-4-2 7:53:46","lat":"31.2393258702","lon":"31.16348"},
		 * "recvAddr":{"name":"古美路1528号","lat":"31.16348","lon":"121.398006"},
		 * "location":{"lat":"31.1417","lon":"121.276","time":"2013-4-17 9:00:47"},
		 * "slogan:":"",
		 * "redress":{"distance",10000, "expireHour":24}
		 * }
		 */

		JSONObject data = root.optJSONObject("data");
		models = new DeliverInfoModel();
		JSONObject item = data.optJSONObject("profile");
		if(null != item)
		{
			models.setName(item.optString("name"));
			models.setOrderId(Long.valueOf(item.optString("order_id")));
			models.setPhoneNo(item.optString("phone"));
			models.setImgUrl(item.optString("avator"));
		}
		item = data.optJSONObject("branch");
		String strLat,strLon;
		if(null != item)
		{
			models.setSubstationName(item.optString("name"));
			strLat = item.optString("lat");
			strLon = item.optString("lon");
			models.setStartPos(strLat, strLon);
		}
		item = data.optJSONObject("recvAddr");
		if(null!= item)
		{
			models.setDestAddressName(item.optString("name"));
			strLat = item.optString("lat");
			strLon = item.optString("lon");
			models.setDestPos(strLat, strLon);
		}
		item = data.optJSONObject("location");
		if(null!= item)
		{
			strLat = item.optString("lat");
			strLon = item.optString("lon");
			models.setCurPos(strLat, strLon);
			
			models.setTime(item.optString("time"));
			models.setCountdown(item.optString("countdown"));
		}
		
		String strSlogan = data.optString("slogan");
		models.setSlogan(strSlogan);
		
		item = data.optJSONObject("redress");
		if(null!=item)
		{
			models.mCheckDistance = item.optInt("distance",-1);
			models.mExpireHour = item.optInt("expireHour");
		}
		return models;
	}
}
