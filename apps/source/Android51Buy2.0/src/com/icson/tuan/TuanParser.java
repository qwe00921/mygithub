package com.icson.tuan;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class TuanParser extends Parser<byte[], TuanModel> {

	private JSONParser parser;

	public TuanParser() {
		parser = new JSONParser();
	}

	@Override
	public TuanModel parse(byte[] bytes, String charset) throws Exception {
		return parse(parser.parse(bytes, charset));
	}

	public String getString() {
		return parser.getString();
	}

	public TuanModel parse(String str) throws Exception {
		return parse(new JSONObject(str));
	}

	private TuanModel parse(JSONObject json) throws Exception {

		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			throw new Exception("errno not is no 0.");
		}

		TuanModel model = new TuanModel();
		
		if(!ToolUtil.isEmptyList(json, "data")){
			model.parse(json.getJSONObject("data"));
		}
		
		return model;
	}
}