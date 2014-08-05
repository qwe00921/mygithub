package com.icson.item;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class ItemProductParser extends Parser<byte[], ItemProductModel> {

	@Override
	public ItemProductModel parse(byte[] bytes, String charset) throws Exception {
		clean();
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(bytes, charset);

		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		ItemProductModel mItemProductModel = new ItemProductModel();
		mItemProductModel.parse(json.getJSONObject("data"));
		mIsSuccess = true;
		
		return mItemProductModel;
	}
}
