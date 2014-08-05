package com.icson.lib.model;

import java.util.ArrayList;

public class SearchProductListModel extends BaseModel {
	private ArrayList<SearchProductModel> mSearchProductModels;
	private ArrayList<SearchCategoryModel> mSearchCategoryModels;;
	private PageModel pageModel;

	public ArrayList<SearchCategoryModel> getSearchCategoryModels() {
		return mSearchCategoryModels;
	}

	public void setSearchCategoryModels(ArrayList<SearchCategoryModel> mSearchCategoryModels) {
		this.mSearchCategoryModels = mSearchCategoryModels;
	}

	public SearchProductListModel(){
		mSearchProductModels = new ArrayList<SearchProductModel>();
		pageModel = new PageModel();
	}
	
	public ArrayList<SearchProductModel> getSearchProductModels() {
		return mSearchProductModels;
	}

	public void setSearchProductModels(ArrayList<SearchProductModel> SearchProductModels) {
		this.mSearchProductModels = SearchProductModels;
	}

	public PageModel getPageModel() {
		return pageModel;
	}

	public void setPageModel(PageModel pageModel) {
		this.pageModel = pageModel;
	}
}
