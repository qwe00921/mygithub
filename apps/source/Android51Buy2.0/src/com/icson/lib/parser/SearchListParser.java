package com.icson.lib.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.PageModel;
import com.icson.lib.model.SearchCategoryModel;
import com.icson.lib.model.SearchProductListModel;
import com.icson.lib.model.SearchProductModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class SearchListParser extends Parser<byte[], SearchProductListModel> {
	private boolean mIsSuccess = false;
	private String errMsg = "";

	public SearchProductListModel parse(byte[] bytes, String charset) throws Exception {
		mIsSuccess = false;
		
		SearchProductListModel mSearchProductListModel = new SearchProductListModel();
		JSONParser parser = new JSONParser();
		
		final JSONObject json = parser.parse(bytes, charset);
		if (json.getInt("errno") != 0) {
			errMsg = json.optString("data", Config.NORMAL_ERROR);
			return mSearchProductListModel;
		}

		JSONObject data = json.getJSONObject("data");

		ArrayList<SearchProductModel> productModels = new ArrayList<SearchProductModel>();
		if (!ToolUtil.isEmptyList(data, "list")) {
			JSONArray arrs = data.getJSONArray("list");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				SearchProductModel model = new SearchProductModel();
				model.parse(arrs.getJSONObject(i));
				//如果价格不异常（99999900），那么加入列表
				if(model.getShowPrice() != 99999900)
					productModels.add(model);
			}
		}

		mSearchProductListModel.setSearchProductModels(productModels);

		if (!ToolUtil.isEmptyList(data, "page")) {
			PageModel pageModel = new PageModel();
			pageModel.parse(data.getJSONObject("page"));
			mSearchProductListModel.setPageModel(pageModel);
		}

		if (!ToolUtil.isEmptyList(data, "classes")) {
			ArrayList<SearchCategoryModel> models = new ArrayList<SearchCategoryModel>();
			JSONArray arrs = data.getJSONArray("classes");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				JSONObject c1 = arrs.getJSONObject(i);
				SearchCategoryModel model = new SearchCategoryModel();
				model.setPath(c1.getString("id"));
				model.setName(c1.getString("className"));
				model.setNum(c1.getInt("count"));
				models.add(model);
				
			}
			mSearchProductListModel.setSearchCategoryModels(models);
		}

		mIsSuccess = true;

		return mSearchProductListModel;
	}

	public boolean isSuccess() {
		return mIsSuccess;
	}

	public String getErrorMessage() {
		return errMsg;
	}

}