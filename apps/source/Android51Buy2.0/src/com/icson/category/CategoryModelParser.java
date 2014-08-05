package com.icson.category;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class CategoryModelParser extends Parser<byte[], ArrayList<CategoryModel>> {

	private String mStr;

	public ArrayList<CategoryModel> parse(byte[] bytes, String charset) throws Exception {
		clean();
		
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(bytes, charset);

		if ( 0 != json.getInt("errno")) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		mStr = parser.getString();
	//	return parse(mStr);
		mIsSuccess = true;
		return parse(json);
	}

	public String getString() {
		return mStr;
	}

	public ArrayList<CategoryModel> parse(String str) throws JSONException {
		JSONObject json = new JSONObject(str);
		return this.parse(json);
	}
	
	private ArrayList<CategoryModel> parse(JSONObject json) throws JSONException {
		final ArrayList<CategoryModel> models = new ArrayList<CategoryModel>();
		JSONArray arrs = json.getJSONArray("data");

		for (int i = 0, len = arrs.length(); i < len; i++) {
			
			CategoryModel model = new CategoryModel();
			model.parse(arrs.getJSONObject(i));
			models.add(model);
		}

		return models;
	}

}