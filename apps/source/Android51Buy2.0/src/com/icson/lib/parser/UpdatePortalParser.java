/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: UpdatePortalParser.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-4-16
 */
package com.icson.lib.parser;

import org.json.JSONObject;

import com.icson.lib.model.PortalInfoModel;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class UpdatePortalParser extends Parser<byte[], PortalInfoModel> {

	private static final String LOG_TAG = UpdatePortalParser.class.getName();

	public PortalInfoModel parse(byte[] bytes, String charset) throws Exception {
		JSONParser parser = new JSONParser();
		
		PortalInfoModel models = null;
		try {
			JSONObject json = parser.parse(bytes, charset);
			//code {"msg":"No new version","errno":203}
			final int errno = json.optInt("errno", -1);
			if(errno!=0)
			{
				return null;
			}
			
			models = new PortalInfoModel();
			//{"errno":0,
			//  "version":1,
			//"url":"http:\/\/st.icson.com\/static_v1\....
			//"duration":3,
			//"begin":1363881600,"end":1366862400}
			models.setSpver(json.optInt("version",0));
			models.setImgUrl(json.optString("url",""));
			models.setBeginTime(json.optInt("begin",0));
			models.setExpireTime(json.optInt("end",0));
			models.setShowDuration(json.optInt("duration",0));
		} catch (Exception ex) {
			models = null;
			Log.e(LOG_TAG, "parse|" + ToolUtil.getStackTraceString(ex));
		}

		return models;
	}
}
