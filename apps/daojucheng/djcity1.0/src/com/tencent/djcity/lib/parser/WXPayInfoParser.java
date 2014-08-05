/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: WXPayInfoParser.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-6-13
 */
package com.tencent.djcity.lib.parser;

import org.json.JSONObject;

import com.tencent.djcity.lib.model.WxInfoModel;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.Parser;
/**
 * 
*   
* Class Name:WXPayInfoParser 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-13 ä¸????11:38:12 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class WXPayInfoParser extends Parser<byte[], WxInfoModel> {

	public WxInfoModel parse(byte[] bytes, String charset) throws Exception {
	
		WxInfoModel models = null;

		clean();

		JSONParser parser = new JSONParser();

		final JSONObject root = parser.parse(bytes, charset);
		
		/**
		 * {"errno":0,
		 *  "data": 
		 *  	{"package":"Sign=1A40402B37C8005D629F4A7E65420978",
		 *  	 "sign":"BA8898B3CACE2D4934E7B34D62E602718200925A",
		 *  	 "token":"137b23cff35afc4709",
		 *  	 "partner":"1900000109"}}
		 */
		
		//code {"msg":"No new version","errno":203}
		final int errno = null != root ? root.optInt("errno", -1) : -1;
		if(errno!=0)
		{
			return null;
		}
		
		JSONObject data = root.optJSONObject("data");
		if(null != data)
		{
			models = new WxInfoModel();
			
			models.setPackage(data.optString("package"));
			models.setPartner(data.optString("partner"));
			models.setSign(data.optString("sign"));
			models.setToken(data.optString("token"));
		}
		
		return models;
	}
}
