package com.tencent.djcity.category;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.category.CategoryModel.NodeCategoryModel;
import com.tencent.djcity.category.CategoryModel.SubCategoryModel;
import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.activity.BaseActivity.DestroyListener;

public class CategoryAdapter extends BaseAdapter implements DestroyListener {

	private LayoutInflater mInflater;

	private ArrayList<BaseModel> dataSource;

	private BaseActivity mActivity;

	public CategoryAdapter(BaseActivity activity,
			ArrayList<BaseModel> dataSource) {

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

		BaseModel item = (BaseModel) getItem(position);
		if (convertView == null) {
			if (item instanceof NodeCategoryModel){
				convertView = mInflater.inflate(R.layout.category_node_item, null);
			}else{
				convertView = mInflater.inflate(R.layout.category_item, null);
			}
			holder = new ItemHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.category_textview_name);
			holder.desc = (TextView) convertView
					.findViewById(R.id.category_textview_desc);
			holder.arrow_right = (ImageView)convertView
					.findViewById(R.id.category_right_arrow);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		if (item instanceof CategoryModel) {
			CategoryModel category = (CategoryModel) item;
			holder.name.setText(category.getName());
			holder.desc.setText(category.getDesc());
			holder.arrow_right.setVisibility(View.VISIBLE);
		}else if (item instanceof SubCategoryModel) {
			SubCategoryModel subCategory = (SubCategoryModel) item;
			holder.name.setText(subCategory.getName());
			holder.desc.setText(subCategory.getDesc());
			holder.arrow_right.setVisibility(View.INVISIBLE);
		}else if (item instanceof NodeCategoryModel) {
			NodeCategoryModel node = (NodeCategoryModel) item;
			holder.name.setText(node.name);
			holder.desc.setVisibility(View.GONE);
		}
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
		TextView desc;
		ImageView arrow_right;
	}
}
