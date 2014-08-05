package com.icson.item;

import com.icson.lib.ui.HorizontalListView;
import com.icson.lib.ui.MyWebView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CustomViewPager extends ViewPager{
	
	public CustomViewPager(Context conext){
		super(conext);
	}
	
	public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof HorizontalListView) {
                return true;
        }else if(v instanceof MyWebView) {
			return ((MyWebView)v).canSrollHorizon(-dx);
		}else{
			return super.canScroll(v, checkV, dx, x, y);
		}
	}
	
	
}
