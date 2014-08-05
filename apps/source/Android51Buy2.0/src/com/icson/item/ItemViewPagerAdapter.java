package com.icson.item;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class ItemViewPagerAdapter extends PagerAdapter {

	private BaseActivity mActivity;

	public ItemViewPagerAdapter(BaseActivity context) {
		mActivity = context;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {

	}

	@Override
	public void finishUpdate(View view) {

	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		switch (position) {
		case 0:
			return mActivity
					.findViewById(R.id.item_relative_tab_content_detail);
		case 1:
			return mActivity
					.findViewById(R.id.item_relative_tab_content_param);
		case 2:
			return mActivity
					.findViewById(R.id.item_relative_tab_content_review);
		case 3:
			return mActivity
					.findViewById(R.id.item_relative_tab_content_intro);
		default:
			break;
		}
		return container;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View view) {

	}
}
