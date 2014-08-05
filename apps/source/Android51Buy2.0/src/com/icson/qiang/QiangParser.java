package com.icson.qiang;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class QiangParser extends Parser<byte[], QiangModel> {

	private JSONParser parser;

	public QiangParser() {
		parser = new JSONParser();
	}

	@Override
	public QiangModel parse(byte[] bytes, String charset) throws Exception {
		return parse(parser.parse(bytes, charset));
	}

	public String getString() {
		return parser.getString();
	}

	public QiangModel parse(String str) throws Exception {
		return parse(new JSONObject(str));
	}

	private QiangModel parse(JSONObject json) throws Exception {
		clean();

		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		QiangModel model = new QiangModel();
		if (!ToolUtil.isEmptyList(json, "data")) {
			model.parse(json.getJSONObject("data"));
		}
		
		mIsSuccess = true;
		return model;
	}
}