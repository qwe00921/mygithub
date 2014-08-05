package com.icson.event;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class Event3Parser extends Parser<byte[], Event3Model> {

	private JSONParser parser;

	public Event3Parser() {
		parser = new JSONParser();
	}

	@Override
	public Event3Model parse(byte[] bytes, String charset) throws Exception {
		return parse(parser.parse(bytes, charset));
	}

	public String getString() {
		return parser.getString();
	}

	public Event3Model parse(String str) throws Exception {
		return parse(new JSONObject(str));
	}

	private Event3Model parse(JSONObject json) throws Exception {

		Event3Model result = new Event3Model();

		if (ToolUtil.isEmptyList(json, "data") || ToolUtil.isEmptyList(json.getJSONObject("data"), "data")) {
			return result;
		}

		JSONObject data = json.getJSONObject("data");

		result.setEventId(data.getLong("id"));

		result.setTemplateId(data.getInt("template_id"));
		
		data = data.getJSONObject("data");

		result.setTitle(data.optString("title"));
		//add paytype by xingyao
		result.setPayType(data.optInt("pay_type"));
		result.setAdvertiseUrl(data.optString("advertise_url", "").trim());
		result.setListUrl(data.optString("list_url", "").trim());
		result.setBackground(data.optInt("background", 0xffe4f2ff));
		
		if (!ToolUtil.isEmptyList(data, "products")) {
			JSONArray arrs = data.getJSONArray("products");
			ArrayList<EventProductModel> models = new ArrayList<EventProductModel>();
			for (int i = 0, len = arrs.length(); i < len; i++) {
				EventProductModel model = new EventProductModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}

			result.setProductModels(models);
		}

		return result;
	}
}