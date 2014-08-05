package com.icson.lib.model;

import java.util.ArrayList;

public class FavorProductListModel extends BaseModel {

	private ArrayList<FavorProductModel> mFavorProductModels;

	private PageModel pageModel;

	public FavorProductListModel(){
		mFavorProductModels = new ArrayList<FavorProductModel>();
		pageModel = new PageModel();
	}
	
	public ArrayList<FavorProductModel> getFavorProductModels() {
		return mFavorProductModels;
	}

	public void setFavorProductModels(ArrayList<FavorProductModel> FavorProductModels) {
		this.mFavorProductModels = FavorProductModels;
	}

	public PageModel getPageModel() {
		return pageModel;
	}

	public void setPageModel(PageModel pageModel) {
		this.pageModel = pageModel;
	}
}
