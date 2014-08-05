package com.niuan.android.lib.bannerview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class TwoActivity extends Activity implements OnPageChangeListener {
	private BannerView viewPager;
	private static final String TAG = "TwoActivity";

	private ImageView[] tips;

	private int[] imgIdArray;
	
	private BaseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		viewPager = (BannerView) findViewById(R.id.viewPager); 

		imgIdArray = new int[] { R.drawable.item01, R.drawable.item02,
				R.drawable.item03, R.drawable.item04, R.drawable.item05,
				R.drawable.item06, R.drawable.item07, R.drawable.item08 };

		mAdapter = new MyAdapter();
		viewPager.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		
		viewPager.startScroll();
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imgIdArray.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return imgIdArray[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ImageView imgView = null;
			if(convertView == null) {
				imgView = new ImageView(TwoActivity.this);
			} else {
				imgView = (ImageView) convertView;
			}
			
			imgView.setImageResource((Integer) getItem(position));
			Log.d(TAG, "[getView] position = " + position + ", view = " + imgView);
			return imgView;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		setImageBackground(arg0 % imgIdArray.length);
	}

	/**
	 * @param selectItems
	 */
	private void setImageBackground(int selectItems) {
		for (int i = 0; i < tips.length; i++) {
			if (i == selectItems) {
				tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
			}
		}
	}
}
