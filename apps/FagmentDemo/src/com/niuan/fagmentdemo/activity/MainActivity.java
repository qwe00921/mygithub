package com.niuan.fagmentdemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.example.fagmentdemo.R;
import com.niuan.android.lib.ui.ViewPagerWrapper;
import com.niuan.fagmentdemo.fragment.TestFragment;

public class MainActivity extends FragmentActivity {

	private ViewPagerWrapper mViewPager;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mViewPager = (ViewPagerWrapper) findViewById(R.id.viewpager);
		
		View fragment1 = getLayoutInflater().inflate(R.layout.layout_view, null);
		View fragment2 = getLayoutInflater().inflate(R.layout.layout_view, null);
		View fragment3 = getLayoutInflater().inflate(R.layout.layout_viewpager, null);
		
		mViewPager.addViewPage(fragment1, "title1");
		mViewPager.addViewPage(fragment2, "title2");
		mViewPager.addFragmentPage(new TestFragment(), "title3");
		
		mViewPager.refresh();

//		FragmentManager manager = getFragmentManager();
//		manager.beginTransaction();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	PagerAdapter pagerAdapter;

	class MyPagerAdapter extends PagerAdapter {
		private List<View> mViewList;
		private List<String> mTitleList;

		public MyPagerAdapter(Activity activity) {
			mViewList = new ArrayList<View>();
			LayoutInflater inflater = activity.getLayoutInflater();
			
			View fragment1 = inflater.inflate(R.layout.layout_viewpager, null);
			
			mViewList.add(fragment1);
			
			mTitleList = new ArrayList<String>();
			mTitleList.add("title1");
			mTitleList.add("title2");
			mTitleList.add("title3");
			
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public int getCount() {

			return mViewList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViewList.get(position));

		}

		@Override
		public int getItemPosition(Object object) {

			return super.getItemPosition(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return mTitleList.get(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewList.get(position));
			// weibo_button=(Button) findViewById(R.id.button1);
			// weibo_button.setOnClickListener(new OnClickListener() {
			//
			// public void onClick(View v) {
			// intent=new Intent(ViewPagerDemo.this,WeiBoActivity.class);
			// startActivity(intent);
			// }
			// });
			return mViewList.get(position);
		}

	};

}
