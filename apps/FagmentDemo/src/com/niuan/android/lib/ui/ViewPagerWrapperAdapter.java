package com.niuan.android.lib.ui;

import java.util.ArrayList;
import java.util.List;

import com.example.fagmentdemo.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerWrapperAdapter extends PagerAdapter {
	private List<ViewPage> mPageList = new ArrayList<ViewPage>();
	
	private FragmentManager mFragmentMgr;
	private Context mContext;
	private FragmentActivity mActivity;
	
	private LayoutInflater mInflater;
	
	private int mDefaultResId = R.layout.layout_viewpager;
	
	public ViewPagerWrapperAdapter(Context context) {
		if(context instanceof FragmentActivity) {
			mActivity = (FragmentActivity) context;
		}
		mContext = context;
		mInflater = LayoutInflater.from(context);
		if(mActivity != null) {
			mFragmentMgr = mActivity.getSupportFragmentManager();
		}
	}
	
	public void updateDatasource(List<ViewPage> pageList) {
		if(mPageList.size() > 0) {
			mPageList.clear();
		}
		
		mPageList.addAll(pageList);
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}

	@Override
	public int getCount() {

		return mPageList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {

		ViewPage page = mPageList.get(position);
		View view = page.getView();
		Fragment fragment = page.getFragment();
		switch(page.getType()) {
			case ViewPage.TYPE_FRAGMENT: {
				FragmentTransaction mTransaction;
				mTransaction = mFragmentMgr.beginTransaction();
				mTransaction.remove(fragment).commit();
				break;
			}
			case ViewPage.TYPE_VIEW: {
				break;
			}
			case ViewPage.TYPE_VIEW_FRAGMENT: {
				break;
			}
			default: {
				break;
			}
		}
		
		container.removeView(view);
	}

	@Override
	public int getItemPosition(Object object) {

		return super.getItemPosition(object);
	}

	@Override
	public CharSequence getPageTitle(int position) {

		return mPageList.get(position).getTitle();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		
		View returnView = null;
		ViewPage page = mPageList.get(position);
		Fragment fragment = page.getFragment();
		View view = page.getView();
		switch(page.getType()) {
			case ViewPage.TYPE_FRAGMENT: {
				View fragmentView = mInflater.inflate(mDefaultResId, null);
				
				returnView = fragmentView;
				page.setView(returnView);
				int layoutId = mDefaultResId + position;
				fragmentView.setId(layoutId);
				
				FragmentTransaction mTransaction;
				mTransaction = mFragmentMgr.beginTransaction();
				mTransaction.add(layoutId, fragment).commit();
				break;
			}
			case ViewPage.TYPE_VIEW: {
				returnView = view;
				break;
			}
			case ViewPage.TYPE_VIEW_FRAGMENT: {
				break;
			}
			default: {
				break;
			}
		}
		
		container.addView(returnView);
		return returnView;
	}
}
