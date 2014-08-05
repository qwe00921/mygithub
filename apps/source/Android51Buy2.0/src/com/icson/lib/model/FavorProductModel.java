package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class FavorProductModel extends ProductModel {

	private long favorId;

	public long getFavorId() {
		return favorId;
	}

	public void setFavorId(long favorId) {
		this.favorId = favorId;
	}

	@Override
	public void parse(JSONObject v) throws JSONException {
		super.parse(v);

		setFavorId(v.getLong("favor_id"));
	}
}
