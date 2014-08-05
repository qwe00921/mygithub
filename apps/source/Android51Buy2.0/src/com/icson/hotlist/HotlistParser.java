package com.icson.hotlist;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class HotlistParser extends Parser<byte[], HotlistModel> {

	private JSONParser parser;

	public HotlistParser() {
		parser = new JSONParser();
	}

	@Override
	public HotlistModel parse(byte[] bytes, String charset) throws Exception {
		clean();
		
		final JSONObject root = parser.parse(bytes, charset);

		if (root.getInt("errno") != 0) {
			mErrMsg = root.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		HotlistModel model = new HotlistModel();
		if (!ToolUtil.isEmptyList(root, "data")) {
			model.parse(root);
		}
		
		this.mIsSuccess = true;
		return model;
	}
}