package com.icson.qiang;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class QiangTomorrowParser extends Parser<byte[], QiangTomorrowModel> {
	
	private JSONParser parser;
	
	public QiangTomorrowParser(){
		parser = new JSONParser();
	}

	@Override
	public QiangTomorrowModel parse(byte[] bytes, String charset) throws Exception {
		return parse(parser.parse(bytes, charset));
	}
	
	public String getString(){
		return parser.getString();
	}
	
	public  QiangTomorrowModel parse(String str) throws Exception {
		return parse(new JSONObject(str));
	}
	
	private QiangTomorrowModel parse(JSONObject json) throws Exception{
		clean();
		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}
		
		QiangTomorrowModel result = new QiangTomorrowModel();
		result.parse(json);
		
		mIsSuccess = true;
		return result;
	}
}
