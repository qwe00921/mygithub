package com.icson.item;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class CustomGallery extends Gallery {

	public CustomGallery(Context context) {
		super(context);
	}

	public CustomGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setPager(ViewPager aPager) {
		mPager = aPager;
	}

	@Override  
    public boolean dispatchTouchEvent(MotionEvent ev) {
		if( null != mPager )
			mPager.requestDisallowInterceptTouchEvent(true);  
        return super.dispatchTouchEvent(ev);  
    }  
  
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if( null != mPager )
    		mPager.requestDisallowInterceptTouchEvent(true);  
        return super.onInterceptTouchEvent(ev);  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {
    	if( null != mPager )
    		mPager.requestDisallowInterceptTouchEvent(true);  
        return super.onTouchEvent(event);  
    }  
	
	private ViewPager mPager;
}