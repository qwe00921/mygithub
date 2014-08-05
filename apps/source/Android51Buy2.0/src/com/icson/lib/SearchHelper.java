package com.icson.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.icson.category.CategoryModel.NodeCategoryModel;
import com.icson.lib.model.SearchModel;
import com.icson.util.Log;

public class SearchHelper {

	private static final String LOG_TAG = SearchHelper.class.getName();

	
	public static SearchModel getSearchModel(String url) {

		SearchModel model = new SearchModel();

		Matcher matcher = Pattern.compile("(\\d*)-([0-9tT]*)-([0-9]*)-(\\d?\\d?)-([0-9]*)-([0-9]*)-([0-9]*)-([0-9a-zA-Z]*)-([0-9]*)\\.html(?:\\?q=(.*))?$").matcher(url);
		while (matcher.find()) {
			if (!matcher.group(1).equals("")) {
				model.setPath(matcher.group(1));
			}
			if (!matcher.group(2).equals("")) {
				model.setPrice(matcher.group(2));
			}

			// model.setPrice( matcher.group(2));
			if (!matcher.group(3).equals("")) {
				model.setSort(Integer.valueOf(matcher.group(3)));
			}

			// model.setPrice( matcher.group(2));
//			if (!matcher.group(4).equals("")) {
//				model.setDesc(Integer.valueOf(matcher.group(4)) % 10 == 1 ? SearchModel.SORT_TYPE_DESC : SearchModel.SORT_TYPE_ASC);
//			}

			// model.setViewMode( Integer.valueOf(matcher.group(4)));
			if (!matcher.group(5).equals("")) {
				model.setPageSize(Integer.valueOf(matcher.group(5)));
			}
			// model.setDay( Integer.valueOf(matcher.group(6)));
			if (!matcher.group(7).equals("")) {
				model.setCurrentPage(Integer.valueOf(matcher.group(7)));
			} else {
				model.setCurrentPage(1);
			}

			model.setOption(matcher.group(8));

			String keyWord = matcher.group(10);
			if (keyWord != null && !keyWord.equals("")) {
				String str = null;
				try {
					str = java.net.URLDecoder.decode(keyWord, "GBK");
				} catch (Exception ex) {
					Log.e(LOG_TAG, ex);
					str = null;
				}

				if (str != null) {
					model.setKeyWord(str);
				}
			}

		}

		return model;
	}
	
	
	public static SearchModel getSearchModel(NodeCategoryModel node) {

		SearchModel model = new SearchModel();
		model.setPath(TextUtils.isEmpty(node.path) ? "" : node.path);
		model.setOption(TextUtils.isEmpty(node.option) ? "" : node.option);
		model.setAreaCode((TextUtils.isEmpty(node.areacode) || !TextUtils.isDigitsOnly(node.areacode)) ? 0 : Integer.valueOf(node.areacode) );
		model.setKeyWord(TextUtils.isEmpty(node.keyword) ? "" : node.keyword);
		model.setClassId(TextUtils.isEmpty(node.classId) ? "" : node.classId);
		model.setSort((TextUtils.isEmpty(node.sort) || !TextUtils.isDigitsOnly(node.sort)) ? 0 : Integer.valueOf(node.sort));
		model.setCurrentPage((TextUtils.isEmpty(node.page) || !TextUtils.isDigitsOnly(node.page)) ? 0 : Integer.valueOf(node.page));
		model.setPageSize((TextUtils.isEmpty(node.pageSize) || !TextUtils.isDigitsOnly(node.pageSize)) ? 0 : Integer.valueOf(node.pageSize));
		model.setPrice(TextUtils.isEmpty(node.price) ? "" : node.price);
		
		return model;
	}

	public static String getSearchUrlParamter(SearchModel model) {
		if (model.getCurrentPage() == 0) {
			model.setCurrentPage(1);
		}

		String url = "";

		url += "&p=" + model.getCurrentPage();

		if (model.getPageSize() != 0) {
			url += "&pp=" + model.getPageSize();
		}
		
		if( !TextUtils.isEmpty(model.getPath()) ){
			url += "&path=" + model.getPath();
		}
		
		if( !TextUtils.isEmpty(model.getClassId())){
			url += "&classid=" + model.getClassId();
		}

		if (model.getSort() != 0) {
			url += "&sort=" + model.getSort();
		}

		if (!TextUtils.isEmpty(model.getOption()) ) {
			url += "&option=" + model.getOption();
		}

		if (!TextUtils.isEmpty(model.getPrice())) {
			url += "&price=" + model.getPrice();
		}

		if (0 != model.getHasGood()) {
			url += "&sf=" + model.getHasGood();
		}
		
		if (!TextUtils.isEmpty(model.getKeyWord())) {
			try {
				url += "&q=" + java.net.URLEncoder.encode(model.getKeyWord(), "GBK");
			} catch (Exception ex) {
				Log.e(LOG_TAG, "|java.net.URLEncoder.encode error|" +  ex);
			}
		}
		
		url +="&districtId=" + FullDistrictHelper.getDistrictId();

		int whid = (model.getAreaCode() != 0) ? model.getAreaCode() : ILogin.getSiteId();
		url += "&areacode=" + whid;
		
		
		return url;
	}

}
