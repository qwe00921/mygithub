/*
 * Copyright (C) 2012 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51buy
 * FileName: WelcomePageAdapter.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: 2013-07-18
 */

package com.icson.welcome;


import java.util.List;

import com.icson.R;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class WelcomePageAdapter extends PagerAdapter {
	private List<View> mViews;
	private Activity mActivity;
	
	public WelcomePageAdapter(Activity pActivity, List<View> pViews) {
		this.mViews = pViews;
		this.mActivity = pActivity;
	}
	
	/*
	 * Destroy page located at position
	 */
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(mViews.get(position));
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(mViews.get(position));
		
		if( position == (this.mViews.size() - 1) ){
			ImageView mButton = (ImageView) container.findViewById(R.id.welcome_button);
			mButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mActivity.finish();
				}
			});
			
		}
		
		return mViews.get(position);
		
	}

	/*
	 * Get number of pages
	 */
	@Override
	public int getCount() {
		if( null != this.mViews){
			return this.mViews.size();
		}
		
		return 0;
	}

	@Override
	public boolean isViewFromObject(View view1, Object view2) {
		return (view1 == view2);
	}

}
