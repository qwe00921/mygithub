/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: FilterCategoryActivity.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: June 20, 2013
 * 
 */

package com.icson.filter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.SearchCategoryModel;

public class FilterCategoryAdapter extends BaseAdapter {
	private ArrayList<SearchCategoryModel> mSearchCategoryModels;
	private Activity mActivity;
	private LayoutInflater mLayoutInflater;
	
	public FilterCategoryAdapter(Activity activity, ArrayList<SearchCategoryModel> models){
		this.mActivity = activity;
		this.mSearchCategoryModels = models;
		this.mLayoutInflater = this.mActivity.getLayoutInflater();
	}

	@Override
	public int getCount() {
		return mSearchCategoryModels.size();
	}

	@Override
	public Object getItem(int position) {
		return mSearchCategoryModels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemHolder pHolder = null;
		if (null == convertView){
			convertView = mLayoutInflater.inflate(R.layout.adapter_filtercategory_item, null);
			pHolder = new ItemHolder();
			//pHolder.pLayout = (RelativeLayout) convertView.findViewById(R.id.filter_category_item_layout);
			pHolder.pName = (TextView) convertView.findViewById(R.id.filter_item_name);
			pHolder.pNum = (TextView) convertView.findViewById(R.id.filter_item_num);
			convertView.setTag(pHolder);
		}else{
			pHolder = (ItemHolder) convertView.getTag();
		}
		
		SearchCategoryModel model = new SearchCategoryModel();
		model = (SearchCategoryModel) this.getItem(position);
		pHolder.pName.setText(model.getName());
		if(model.getNum() >= 0) {
			pHolder.pNum.setText(model.getNum()+"件");
		}else{
			pHolder.pNum.setText("");
		}
		
		if(pHolder.pName.getText().equals(((FilterCategoryActivity)mActivity).getCurrentCategory()))
			pHolder.pName.setTextColor(mActivity.getResources().getColor(R.color.filter_item_checked));
		else
			pHolder.pName.setTextColor(mActivity.getResources().getColor(R.color.global_text_color));
		
		
		return convertView;
	}
	
	private class ItemHolder{
		//RelativeLayout pLayout;
		TextView pName;
		TextView pNum;
	}

}
