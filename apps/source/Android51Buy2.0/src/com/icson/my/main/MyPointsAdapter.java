package com.icson.my.main;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class MyPointsAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<MyPointsModel> dataSource;
	private BaseActivity mActivity;

	public MyPointsAdapter(BaseActivity activity,
			ArrayList<MyPointsModel> OrderModelList) {
		mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
		this.dataSource = OrderModelList;
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

		MyPointsModel myPoints = (MyPointsModel) getItem(position);

		ItemHolder holder = null;
		if(null == convertView)
		{
			holder = new ItemHolder();
			convertView = mInflater.inflate(R.layout.my_pointslist_item, null);
	
			holder.title = (TextView) convertView
					.findViewById(R.id.mypointslist_textview_title);
			holder.points = (TextView) convertView
					.findViewById(R.id.mypointslist_textview_points);
			holder.msg = (TextView) convertView
					.findViewById(R.id.mypointslist_textview_msg);
			holder.time = (TextView) convertView
					.findViewById(R.id.mypointslist_textview_time);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ItemHolder)convertView.getTag();
		}
		holder.title.setText(myPoints.getTitle());
		holder.points.setText(myPoints.getPointsStr());
		holder.msg.setText(myPoints.getMsg());
		holder.time.setText("成交时间: " + myPoints.getTime());

		return convertView;
	}

	private class ItemHolder{
		TextView title;
		TextView points;
		TextView msg;
		TextView time;
	}
}
