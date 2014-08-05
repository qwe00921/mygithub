package com.icson.item;

import java.util.ArrayList;

import com.icson.lib.model.BaseModel;

public class ItemParamModel extends BaseModel {
	private String name;

	private ArrayList<ItemSubParamModel> mItemSubParamModels;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ItemSubParamModel> getItemParamSubModels() {
		return mItemSubParamModels;
	}

	public void setParamSubModels(ArrayList<ItemSubParamModel> mItemSubParamModels) {
		this.mItemSubParamModels = mItemSubParamModels;
	}
}
