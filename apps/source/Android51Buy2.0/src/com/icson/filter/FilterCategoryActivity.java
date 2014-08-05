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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.model.SearchCategoryModel;
import com.icson.lib.model.SearchModel;
import com.icson.lib.ui.UiUtils;
import com.icson.util.activity.BaseActivity;
public class FilterCategoryActivity extends BaseActivity implements OnItemClickListener{
	public final static String REQUEST_SEARCH_CATEGORY_MODEL = "search_category_model";
	public final static String REQUEST_SEARCH_MODEL = "search_model";
	public final static String TOTAL_COUNT = "total_count";
	
	public final static int REQUEST_FLAG_SEARCH_FILTER = 1;
	public final static int CATEGORY_RESULT_OK = 2;
	
	private FilterCategoryAdapter mFilterCategoryAdapter;
	private ListView mCategoryListView;
	private ArrayList<SearchCategoryModel> mSearchCategoryModels;
	private SearchModel  mSearchModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_filtercategory);
		
		// Load nav bar.
		this.loadNavBar(R.id.filter_category_navbar);
		
		initData();
	}

	@SuppressWarnings("unchecked")
	private void initData() {
		Intent pIntent = getIntent();
		if (null != pIntent.getSerializableExtra(REQUEST_SEARCH_CATEGORY_MODEL)) {
			mSearchCategoryModels = (ArrayList<SearchCategoryModel>) pIntent.getSerializableExtra(REQUEST_SEARCH_CATEGORY_MODEL);
		} 
		if (null != pIntent.getSerializableExtra(REQUEST_SEARCH_MODEL)){
			mSearchModel = (SearchModel) pIntent.getSerializableExtra(REQUEST_SEARCH_MODEL);
		}
		
		if(null == mSearchCategoryModels || null == mSearchModel){
			UiUtils.makeToast(this, R.string.params_empty,true);
			finish();
			return;
		}
		
		int total_num = pIntent.getIntExtra(TOTAL_COUNT, -1);
		SearchCategoryModel model = new SearchCategoryModel();
		model.setName("全部分类");
		model.setNum(total_num);
		mSearchCategoryModels.add(0, model);
		mCategoryListView = (ListView) findViewById(R.id.filter_listview);
		
		String name = mSearchModel.getCategoryName();
		int index = -1;
		int count = mSearchCategoryModels.size();
		if(!TextUtils.isEmpty(name))
			for(int j=0 ; j < count; j++)
			{
				SearchCategoryModel tmp = mSearchCategoryModels.get(j);
				if(tmp.getName().equals(name))
				{
					index = j;
					break;
				}
			}
		
		mFilterCategoryAdapter = new FilterCategoryAdapter(this, mSearchCategoryModels);
		mCategoryListView.setAdapter(mFilterCategoryAdapter);
		if(-1 != index)
			mCategoryListView.setSelection(index);
		mCategoryListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
			SearchCategoryModel model = mSearchCategoryModels.get(position);
			mSearchModel.setPath(model.getPath());
			mSearchModel.setCategoryName(model.getName());
			Intent intent = getIntent();
			Bundle param = new Bundle();
			param.putSerializable(FilterActivity.REQUEST_SEARCH_MODEL, mSearchModel);
			intent.putExtras(param);
			setResult(FilterActivity.FLAG_RESULT_OK, intent);
			finish();
	}
	
	public String getCurrentCategory(){
		return  null == mSearchModel.getCategoryName()? "" : mSearchModel.getCategoryName();
	}
	
	@Override
	protected void onDestroy() {
		
		if(null!=mSearchCategoryModels)
			mSearchCategoryModels.clear();
		mSearchCategoryModels = null;
		mFilterCategoryAdapter = null;
		mSearchModel = null;
		super.onDestroy();
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_FilterCategoryActivity);
	}
}
