package com.icson.filter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.SearchFilterAttributeModel;
import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class SearchFilterParser extends Parser<byte[], ArrayList<SearchFilterAttributeModel>> {

	private String mStr;

	public String getString() {
		return mStr;
	}

	public ArrayList<SearchFilterAttributeModel> parse(String content) throws JSONException {
		final ArrayList<SearchFilterAttributeModel> mAttributeModelList = new ArrayList<SearchFilterAttributeModel>();

		if (!content.equals("")) {
			JSONArray arrs = new JSONArray(content);
			for (int i = 0, len = arrs.length(); i < len; i++) {
				SearchFilterAttributeModel mSearchFilterAttributeModel = new SearchFilterAttributeModel();
				mSearchFilterAttributeModel.parse(arrs.getJSONObject(i));
				mAttributeModelList.add(mSearchFilterAttributeModel);
			}
		}

		mIsSuccess = true;

		return mAttributeModelList;
	}

	public ArrayList<SearchFilterAttributeModel> parse(byte[] bytes, String charset) throws Exception {
		clean();

		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(bytes, charset);
		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}
		
		JSONObject data = json.getJSONObject("data");
		mStr = data.getString("attr");

		return parse(mStr);
	}	
}
