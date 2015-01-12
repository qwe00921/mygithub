package com.niuan.wificonnector.lib.list.adapter;

import java.util.List;

import com.niuan.wificonnector.lib.list.adapter.holder.DataHolder;
import com.niuan.wificonnector.lib.list.adapter.holder.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class WrappedListAdapter<E extends DataHolder, V extends ViewHolder>
		extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<E> mItemList;

	public WrappedListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	public void setDataSource(List<E> list) {
		mItemList = list;
	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public int getCount() {
		return mItemList == null ? 0 : mItemList.size();
	}

	@Override
	public E getItem(int position) {
		return mItemList == null ? null : mItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mItemList == null ? 0 : mItemList.get(position).getItemId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		V holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(getItemResourceId(position), null);
			holder = initViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (V) convertView.getTag();

		}

		E data = getItem(position);
		updateView(data, holder);
		return convertView;
	}

	public abstract int getItemResourceId(int position);

	protected abstract V initViewHolder(View convertView);

	protected abstract void updateView(E data, V view);

}
