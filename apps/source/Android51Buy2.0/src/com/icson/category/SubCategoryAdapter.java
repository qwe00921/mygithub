package com.icson.category;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.category.CategoryModel.SubCategoryModel;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class SubCategoryAdapter extends BaseAdapter implements DestroyListener {

	private LayoutInflater mInflater;

	private ArrayList<SubCategoryModel> dataSource;

	private BaseActivity mActivity;

	public SubCategoryAdapter(BaseActivity activity,
			ArrayList<SubCategoryModel> dataSource) {

		mInflater = LayoutInflater.from(activity);
		this.dataSource = dataSource;
		mActivity = activity;

		mActivity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		return dataSource.size();
	}

	@Override
	public Object getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		ItemHolder holder = null;

		SubCategoryModel item = (SubCategoryModel) getItem(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.category_sub_item, null);
			holder = new ItemHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.category_textview_name);
			holder.divImg = convertView
					.findViewById(R.id.category_sub_selected);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		SubCategoryModel subCategory = (SubCategoryModel) item;
		holder.name.setText(subCategory.getName());
		holder.divImg.setVisibility(item.isSelected? View.VISIBLE:View.GONE);
		
		return convertView;
	}

	@Override
	public void onDestroy() {
		dataSource = null;
		mActivity = null;
		mInflater = null;
	}

	static class ItemHolder {
		TextView name;
		View divImg;
	}
}
