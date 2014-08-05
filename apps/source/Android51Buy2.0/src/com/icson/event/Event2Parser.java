package com.icson.event;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.event.Event2Model.Event2SubModel;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class Event2Parser extends Parser<byte[], Event2Model> {

	private JSONParser parser;

	public Event2Parser() {
		parser = new JSONParser();
	}

	@Override
	public Event2Model parse(byte[] bytes, String charset) throws Exception {
		return parse(parser.parse(bytes, charset));
	}

	public String getString() {
		return parser.getString();
	}

	public Event2Model parse(String str) throws Exception {
		return parse(new JSONObject(str));
	}

	private Event2Model parse(JSONObject json) throws Exception {

		Event2Model result = new Event2Model();

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

		if (!ToolUtil.isEmptyList(data, "products")) {
			JSONArray arrs = data.getJSONArray("products");
			ArrayList<Event2SubModel> models = new ArrayList<Event2SubModel>();
			for (int i = 0, len = arrs.length(); i < len; i++) {
				Event2SubModel model = new Event2SubModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}

			result.setEvent2SubModels(models);
		}

		return result;
	}
}