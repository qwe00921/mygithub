package com.niuan.wificonnector.lib.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.niuan.wificonnector.R;
import com.niuan.wificonnector.RefreshType;
import com.niuan.wificonnector.lib.list.adapter.WrappedListAdapter;
import com.niuan.wificonnector.lib.list.adapter.holder.DataHolder;
import com.niuan.wificonnector.lib.list.adapter.holder.ViewHolder;
import com.niuan.wificonnector.lib.ui.view.RefreshListWrapper;
import com.niuan.wificonnector.lib.ui.view.RefreshableViewWrapper;
import com.niuan.wificonnector.lib.ui.view.RefreshableViewWrapper.OnListViewEventListener;

public abstract class BaseListFragment<E extends DataHolder, V extends ViewHolder>
		extends BaseFragment implements OnListViewEventListener,
		OnItemClickListener {

	private ListView mListView;
	private WrappedListAdapter<E, V> mAdapter;
	private ArrayList<E> mDataSource = new ArrayList<E>();
	private RefreshableViewWrapper<ListView> mViewWrapper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View parentView = inflater.inflate(R.layout.global_list, null);

		mListView = (ListView) parentView.findViewById(R.id.list);
		registerForContextMenu(mListView);
		mAdapter = initAdapter();
		mAdapter.setDataSource(mDataSource);

		mViewWrapper = new RefreshListWrapper(getActivity(), mListView, true,
				false);
		mViewWrapper.setOnListViewEventListener(this);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(this);

		refreshData();
		super.onCreateView(inflater, container, savedInstanceState);
		return parentView;
	}

	public void refreshData() {
		if (!hasData()) {
			showView(VIEW_TYPE_LOADING);
		}
		requestData(RefreshType.TYPE_REFRESH);
	}

	public abstract void requestData(int refreshType);

	protected abstract WrappedListAdapter<E, V> initAdapter();

	public ListView getListView() {
		return mListView;
	}

	public WrappedListAdapter<E, V> getAdapter() {
		return mAdapter;
	}

	protected void requestFinish(int refresh, ArrayList<E> data,
			boolean hasMore, boolean replace, boolean error) {
		BaseAdapter adapter = mAdapter;
		if (adapter == null) {
			return;
		}

		int updateCount = -1;
		if (data != null) {

			updateCount = data.size();
			if (refresh == RefreshType.TYPE_REFRESH) {
				if (replace) {
					mDataSource.clear();
				}
				mDataSource.addAll(0, data);
			} else {
				mDataSource.addAll(data);
			}
			adapter.notifyDataSetChanged();
		}

		if (mViewWrapper != null) {
			if (refresh == RefreshType.TYPE_REFRESH) {
				mViewWrapper.onRefreshComplete(updateCount);
			}
			if (error) {
				mViewWrapper.showErrorLoadingBar();
			} else {
				if (!hasMore) {
					mViewWrapper.showNoMoreLoadingBar();
				} else {
					mViewWrapper.showLoadingBar();
				}
			}

		} else {
			// if (refresh == RefreshType._REFRESH_TYPE_REFRESH
			// && needShowUpdatedCount()) {
			//
			// showUpdatedToast(updateCount);
			// }
		}

		checkShowEmptyView();
	}

	protected void checkShowEmptyView() {
		if (hasData()) {
			showView(VIEW_TYPE_DATA);
		} else {
			showView(VIEW_TYPE_EMPTY);
		}
	}

	protected boolean hasData() {
		return mDataSource != null && mDataSource.size() != 0;
	}

	@Override
	public void onRefresh() {
		refreshData();
	}

	@Override
	public void onLoading() {

	}

	@Override
	public void onScrollStateChanged(View view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(View view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount, int direction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Adapter adapter = parent.getAdapter();
		Object object = adapter.getItem(position);
		if (object instanceof DataHolder) {
			onItemClick((DataHolder) object, view, position, id);
		}
	}

	protected void onItemClick(DataHolder data, View view, int position, long id) {

	}

}
