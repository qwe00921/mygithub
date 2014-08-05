package com.icson.item;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class ProductParamParser extends Parser<byte[], ArrayList<ItemParamModel>> {

	@Override
	public ArrayList<ItemParamModel> parse(byte[] input, String charset) throws Exception {

		clean();

		JSONParser parser = new JSONParser();
		final JSONObject json = parser.parse(input, charset);
		if (json.getInt("errno") != 0) {
			mIsSuccess = false;
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		ArrayList<ItemParamModel> models = new ArrayList<ItemParamModel>();

		JSONArray data = json.getJSONArray("data");
		for (int i = 0, len = data.length(); i < len; i++) {
			JSONArray item = data.getJSONArray(i);

			ItemParamModel model = new ItemParamModel();
			model.setName(item.getString(0));
			JSONArray subs = item.getJSONArray(1);
			ArrayList<ItemSubParamModel> subModels = new ArrayList<ItemSubParamModel>();
			for (int j = 0, jLen = subs.length(); j < jLen; j++) {
				JSONArray sub = subs.getJSONArray(j);
				ItemSubParamModel subModel = new ItemSubParamModel();
				String key= sub.getString(0);
				key = key.replace("（", "(").replace("(", " (").replace("）", ")");
				subModel.setKey(key);
				subModel.setValue(sub.getString(1));
				subModels.add(subModel);
			}

			model.setParamSubModels(subModels);

			models.add(model);
		}

		mIsSuccess = true;

		return models;

	}

}
