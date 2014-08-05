package com.icson.lib.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.OrderFlowModel;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class OrderFlowParser extends Parser<byte[], OrderFlowModel> {

	@Override
	public OrderFlowModel parse(byte[] input, String charset) throws Exception {

		JSONParser parser = new JSONParser();

		JSONObject v = parser.parse(input, charset);

		if (v.getInt("errno") != 0) {
			throw new Exception("errno is not 0.");
		}

		OrderFlowModel mOrderFlowModel = new OrderFlowModel();
		final JSONObject data = v.getJSONObject("data");

		if (!ToolUtil.isEmptyList(data, "items")) {
			JSONArray arrs = data.getJSONArray("items");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				OrderFlowModel.Item item = new OrderFlowModel.Item();
				JSONObject jItem = arrs.getJSONObject(i);
				item.setTime(jItem.getString("time"));
				item.setContent(jItem.getString("content"));
				mOrderFlowModel.setItem(item);
			}
		}

		if (!ToolUtil.isEmptyList(data, "total") && data.optInt("third_sysno", 0) == 0) {
			mOrderFlowModel.setTotal(data.optString("total"));
		}

		mOrderFlowModel.setThirdSysno(data.optString("third_sysno"));
		mOrderFlowModel.setThirdType(data.optInt("third_type", 0));
		mOrderFlowModel.setShowMap(data.optInt("has_loc", 0) != 0);

		return mOrderFlowModel;
	}
}
