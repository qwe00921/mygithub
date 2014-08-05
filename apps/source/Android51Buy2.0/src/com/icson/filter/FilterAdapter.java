/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: FilterActivity.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: June 18, 2013
 * 
 */
package com.icson.filter;

import java.util.ArrayList;

import com.icson.lib.model.SearchFilterAttributeModel;
import com.icson.lib.model.SearchFilterOptionModel;

import com.icson.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class FilterAdapter extends BaseExpandableListAdapter{
	private Context mContext;
	private ArrayList<SearchFilterAttributeModel> mSearchFilterAttributeModelList;
	private LayoutInflater mLayoutInflater;
	
	/*
	 * constructor
	 * @param context
	 * @param  searchFilterOptionModelList
	 */
	public FilterAdapter(Context context, ArrayList<SearchFilterAttributeModel> searchFilterOptionModelList){
		this.mContext = context;
		this.mSearchFilterAttributeModelList = searchFilterOptionModelList;
		this.mLayoutInflater = LayoutInflater.from(this.mContext);
	}

	@Override
	public int getGroupCount() {
		if (null == mSearchFilterAttributeModelList){
			return 0;
		}
		
		return mSearchFilterAttributeModelList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (null == mSearchFilterAttributeModelList || groupPosition >= mSearchFilterAttributeModelList.size()){
			return 0;
		}
		
		SearchFilterAttributeModel attributeModel = mSearchFilterAttributeModelList.get(groupPosition);
		ArrayList<SearchFilterOptionModel> optionModels = attributeModel.getSearchFilterOptionModels();
		
		return optionModels.size();
	}

	@Override
	public SearchFilterAttributeModel getGroup(int groupPosition) {
		if (null == mSearchFilterAttributeModelList || groupPosition >= mSearchFilterAttributeModelList.size()){
			return null;
		}
		
		return mSearchFilterAttributeModelList.get(groupPosition);
	}

	@Override
	public SearchFilterOptionModel getChild(int groupPosition, int childPosition) {
		if (null == mSearchFilterAttributeModelList || groupPosition >= mSearchFilterAttributeModelList.size()){
			return null;
		}
		
		SearchFilterAttributeModel attributeModel = mSearchFilterAttributeModelList.get(groupPosition);
		ArrayList<SearchFilterOptionModel> optionModels = attributeModel.getSearchFilterOptionModels();
		if(null == attributeModel || null == optionModels || childPosition >= optionModels.size()){
			return null;
		}
		
		return optionModels.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		AttributeHolder pAttributeHolder = null;
		
		if ( null == convertView ) {
			convertView = this.mLayoutInflater.inflate(R.layout.adapter_filter_attr_item, null);
			
			pAttributeHolder = new AttributeHolder();
			pAttributeHolder.pAttributeName = (TextView) convertView.findViewById(R.id.filterattr_textview_attr);
			pAttributeHolder.pOptionDesc = (TextView) convertView.findViewById(R.id.filterattr_textview_option);
			pAttributeHolder.pAttributeImage = (ImageView) convertView.findViewById(R.id.filter_attr_img);
			
			convertView.setTag(pAttributeHolder);
		}
		else
		{
			pAttributeHolder = (AttributeHolder) convertView.getTag();
		}
		
		SearchFilterAttributeModel attributeModel = this.getGroup(groupPosition);
		ArrayList<SearchFilterOptionModel> optionModels = attributeModel.getSearchFilterOptionModels();
		
		pAttributeHolder.pAttributeName.setText(attributeModel.getName());
		
		int nSize = optionModels.size();
		Boolean isSelected = false;
		String strOptions = "";
		for(int nId=0; nId<nSize; nId++){
			SearchFilterOptionModel optionModel = optionModels.get(nId);
			if (optionModel.isSelect()) {
				 if(strOptions.equals("")){
					strOptions =  optionModel.getName() + ")";
					isSelected = true;
				}else{
					strOptions = optionModel.getName() + "、" + strOptions;
				}
			}
		}
		
		if(strOptions.contains(")")){
			strOptions = "(" + strOptions ;
		}
		pAttributeHolder.pOptionDesc.setText(strOptions);
		
		if ( isExpanded ) {
			pAttributeHolder.pAttributeName.setTextColor(mContext.getResources().getColor(R.color.filter_item_checked));
			pAttributeHolder.pOptionDesc.setTextColor(mContext.getResources().getColor(R.color.filter_item_checked));
			pAttributeHolder.pAttributeImage.setImageResource(R.drawable.i_filter_up);
		}else{
			if(!isSelected)
			{
				pAttributeHolder.pAttributeName.setTextColor(mContext.getResources().getColor(R.color.filter_item_unchecked));
				pAttributeHolder.pOptionDesc.setTextColor(mContext.getResources().getColor(R.color.filter_item_unchecked)); 
			}
			else {
				pAttributeHolder.pAttributeName.setTextColor(mContext.getResources().getColor(R.color.filter_item_checked));
				pAttributeHolder.pOptionDesc.setTextColor(mContext.getResources().getColor(R.color.filter_item_checked)); 
			}
			pAttributeHolder.pAttributeImage.setImageResource(R.drawable.i_filter_down);
		}
		
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		final OptionHolder optionHolder;
		
		if ( null == convertView ) {
			convertView = this.mLayoutInflater.inflate(R.layout.adapter_filter_option_item, null);
			optionHolder = new OptionHolder();
			optionHolder.pOptionName = (TextView) convertView.findViewById(R.id.filteroption_textview_name);
			optionHolder.pCheckBox = (CheckBox) convertView.findViewById(R.id.filteroption_checkbox);
			convertView.setTag(optionHolder);
		}
		else
		{
			optionHolder = (OptionHolder) convertView.getTag();
		}
		
		final SearchFilterOptionModel optionModel = this.getChild(groupPosition, childPosition);
		
		optionHolder.pOptionName.setText(optionModel.getName());
		if ( optionModel.isSelect() ){
			optionHolder.pOptionName.setTextColor(mContext.getResources().getColor(R.color.filter_item_checked));
		}else{
			optionHolder.pOptionName.setTextColor(mContext.getResources().getColor(R.color.filter_item_unchecked));
		}
		
		optionHolder.pCheckBox.setChecked(optionModel.isSelect());
		optionHolder.pCheckBox.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				optionModel.setSelect(optionHolder.pCheckBox.isChecked());
				notifyDataSetInvalidated();
			}
		});
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	private static class AttributeHolder{
		TextView pAttributeName;
		TextView pOptionDesc;
		ImageView pAttributeImage;
	}
	
	private static class OptionHolder{
		TextView pOptionName;
		CheckBox pCheckBox;
	}

}
