package com.niuan.android.lib.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

public class ViewPagerWrapper extends ViewPager {

	private PagerTabStrip mPagerTabStrip;
	private ViewPagerWrapperAdapter mAdapter;
	private List<ViewPage> mPageList = new ArrayList<ViewPage>();
	
	public ViewPagerWrapper(Context context) {
		super(context);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public ViewPagerWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}
	
	private void init(Context context) {
		mPagerTabStrip = new PagerTabStrip(context);
		LayoutParams params = (LayoutParams) mPagerTabStrip.getLayoutParams();
		if(params == null) {
			params = new LayoutParams();
		}
		params.height = LayoutParams.WRAP_CONTENT;
		params.width = LayoutParams.WRAP_CONTENT;
		params.gravity = Gravity.TOP;
		addView(mPagerTabStrip, params);

		mAdapter = new ViewPagerWrapperAdapter(context);
		mAdapter.updateDatasource(mPageList);
		
		setAdapter(mAdapter);
		
	}
	
	public void addFragmentPage(Fragment fragment, String title) {
		ViewPage viewPage = new ViewPage();
		viewPage.setFragment(fragment);
		viewPage.setTitle(title);
		viewPage.setType(ViewPage.TYPE_FRAGMENT);
		
		mPageList.add(viewPage);
	}
	
	public void addViewPage(View view, String title) {
		ViewPage viewPage = new ViewPage();
		viewPage.setView(view);
		viewPage.setTitle(title);
		viewPage.setType(ViewPage.TYPE_VIEW);
		
		mPageList.add(viewPage);
		
	}
	
	public void refresh() {
		mAdapter.updateDatasource(mPageList);
		mAdapter.notifyDataSetChanged();
	}
}
