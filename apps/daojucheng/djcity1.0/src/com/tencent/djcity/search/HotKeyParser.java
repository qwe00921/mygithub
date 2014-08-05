package com.tencent.djcity.search;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.Parser;

public class HotKeyParser extends Parser<byte[], ArrayList<AutoCompleteModel>> {

	private static final String LOG_TAG = HotKeyParser.class.getName();
	
	@Override
	public ArrayList<AutoCompleteModel> parse(byte[] input, String charset) throws Exception {
		ArrayList<AutoCompleteModel> models = new ArrayList<AutoCompleteModel>();

		try {
			JSONParser parser = new JSONParser();
			JSONObject json = parser.parse(input, charset);
			
			if (json.getInt("errno") != 0) {
				mErrMsg = json.optString("list", Config.NORMAL_ERROR);
				throw new Exception("errno not is no 0.");
			}

			final String key = "list";
			if (json.has(key)) {
				JSONArray arrs = json.getJSONArray(key);
				
				for (int i = 0, len = arrs.length(); i < len; i++) {
					AutoCompleteModel model = new AutoCompleteModel();
					model.parse(arrs.getJSONArray(i));
					models.add(model);
				}
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
		}

		return models;
	}
}
