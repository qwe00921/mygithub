package com.icson.hotlist;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class HotPagerAdapter extends PagerAdapter {

	private BaseActivity mActivity;
	private static final int   DEFAULT_CATE_NUM = 5;

	public HotPagerAdapter(BaseActivity context) {
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
		return DEFAULT_CATE_NUM;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		switch (position) {
		case 0:
			return mActivity
					.findViewById(R.id.hot_cate_0);
		case 1:
			return mActivity
					.findViewById(R.id.hot_cate_1);
		case 2:
			return mActivity
					.findViewById(R.id.hot_cate_2);
		case 3:
			return mActivity
					.findViewById(R.id.hot_cate_3);
		
		case 4:
			return mActivity
					.findViewById(R.id.hot_cate_4);
		
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
