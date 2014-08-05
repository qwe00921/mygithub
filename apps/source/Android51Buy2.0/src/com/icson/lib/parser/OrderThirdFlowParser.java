package com.icson.lib.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.OrderFlowModel;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class OrderThirdFlowParser extends Parser<byte[], OrderFlowModel> {

	@Override
	public OrderFlowModel parse(byte[] input, String charset) throws Exception {

		JSONParser parser = new JSONParser();

		JSONObject v = parser.parse(input, charset);

		if (v.getInt("errno") != 0) {
			throw new Exception("errno is not 0.");
		}

		OrderFlowModel mOrderFlowModel = new OrderFlowModel();

		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONArray arrs = v.getJSONArray("data");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				OrderFlowModel.Item item = new OrderFlowModel.Item();
				JSONObject jItem = arrs.getJSONObject(i);
				item.setTime(jItem.getString("time"));
				item.setContent(jItem.getString("content"));
				mOrderFlowModel.setItem(item);
			}
		}

		return mOrderFlowModel;
	}
}
