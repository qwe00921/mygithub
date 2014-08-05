package com.icson.lib.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.FavorProductListModel;
import com.icson.lib.model.FavorProductModel;
import com.icson.lib.model.PageModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class FavorProductListParser extends Parser<byte[], FavorProductListModel> {

	private boolean mIsSuccess = false;

	private String errMsg;

	public boolean isSuccess() {
		return mIsSuccess;
	}

	public String getErrorMsg() {
		return errMsg == null ? "" : errMsg;
	}

	public FavorProductListModel parse(byte[] bytes, String charset) throws Exception {

		mIsSuccess = false;

		JSONParser parser = new JSONParser();

		final JSONObject json = parser.parse(bytes, charset);

		if (json.getInt("errno") != 0) {
			errMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		JSONObject data = json.getJSONObject("data");

		FavorProductListModel mFavorProductListModel = new FavorProductListModel();

		ArrayList<FavorProductModel> productModels = new ArrayList<FavorProductModel>();
		if (!ToolUtil.isEmptyList(data, "list")) {
			JSONArray arrs = data.getJSONArray("list");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				FavorProductModel model = new FavorProductModel();
				model.parse(arrs.getJSONObject(i));
				productModels.add(model);
			}
		}

		mFavorProductListModel.setFavorProductModels(productModels);

		if (!ToolUtil.isEmptyList(data, "page")) {
			PageModel pageModel = new PageModel();
			pageModel.parse(data.getJSONObject("page"));
			mFavorProductListModel.setPageModel(pageModel);
		}

		mIsSuccess = true;

		return mFavorProductListModel;
	}

}