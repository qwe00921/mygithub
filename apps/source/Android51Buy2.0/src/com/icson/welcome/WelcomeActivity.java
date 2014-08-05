/*
 * Copyright (C) 2012 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51buy
 * FileName: WelcomeActivity.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: 2013-07-18
 */
package com.icson.welcome;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.icson.R;

public class WelcomeActivity extends Activity implements OnPageChangeListener{
	private WelcomePageAdapter 	mAdapter;
	private ViewPager 			mViewPager;
	private LinearLayout 		mBottomLayout;
	private ImageView[]			mBottomViews;
	private List<View> 			mViews;
	private int 				mCurrentPos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_welcome);
		
		initView();
		
		mAdapter = new WelcomePageAdapter(this, mViews);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		
	}
	

	@Override
	protected void onDestroy() {
		
		
		mViews = null;
		mBottomViews = null;
		
		super.onDestroy();
	}



	private void initView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		
		mViews = new ArrayList<View>();
		mViews.add(inflater.inflate(R.layout.view_welcome_one, null));
		mViews.add(inflater.inflate(R.layout.view_welcome_two, null));
		mViews.add(inflater.inflate(R.layout.view_welcome_three, null));
		mViews.add(inflater.inflate(R.layout.view_welcome_four, null));
		
		mViewPager = (ViewPager) findViewById(R.id.welcom_viewpager);
		mBottomLayout = (LinearLayout) findViewById(R.id.welcome_indicaters);
		
		mBottomViews = new ImageView[mViews.size()];
		for(int nId = 0; nId < mViews.size(); nId++) {
			mBottomViews[nId] = (ImageView) mBottomLayout.getChildAt(nId);
			mBottomViews[nId].setEnabled(false);
		}
		
		mCurrentPos = 0;
		mBottomViews[mCurrentPos].setEnabled(true);
	}

	@Override
	public void onPageScrollStateChanged(int position) {
		
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int position) {
		setCurrentIndicator(position);
	}

	private void setCurrentIndicator(int pos) {
		if(pos < 0 || pos > mViews.size() - 1 || mCurrentPos == pos) {
			return;
		}
		
		mBottomViews[pos].setEnabled(true);
		mBottomViews[mCurrentPos].setEnabled(false);
		
		mCurrentPos = pos;
	}

}
