package com.icson.yiqiang;

import java.util.ArrayList;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.tuan.TuanModel.CateInfo;
import com.icson.util.activity.BaseActivity;

public class TuanCategoryItemAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private ArrayList<CateInfo> dataSource;

	public TuanCategoryItemAdapter(BaseActivity activity,
			ArrayList<CateInfo> dataSource) {

		mInflater = LayoutInflater.from(activity);
		this.dataSource = dataSource;

	}

	@Override
	public int getCount() {
		return dataSource == null ? 0 : dataSource.size();
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

		convertView = mInflater.inflate(R.layout.tuan_category_item, null);
		CateInfo model = dataSource.get(position);

		TextView name = (TextView) convertView
				.findViewById(R.id.category_textview_name);
		name.setText(Html.fromHtml(model.name + " (" + model.count + ")"));
		return convertView;
	}
}
