package com.icson.home;


import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.ajax.Parser;

/*
 * 
 */
public class FullDistrictParser extends Parser<byte[], FullDistrictModel>{
	String strData = null;

	@Override
	public FullDistrictModel parse(byte[] input, String charset)
			throws Exception {
		
		return parse(new String(input, 0, input.length, charset));
	}
	
	public FullDistrictModel parse(String str) throws JSONException {
		clean();
		
		JSONObject json = new JSONObject(str);
		if( 0 != json.getInt("errno")) {
			this.mIsSuccess = false;
			return null;
		}
		strData = str;
		
		JSONObject data = json.getJSONObject("data");
		FullDistrictModel model = new FullDistrictModel();
		model.parse(data);
		
		this.mIsSuccess = true;
		return model;
	}
	
	
	public String getData(){
		return strData;
	}

}
