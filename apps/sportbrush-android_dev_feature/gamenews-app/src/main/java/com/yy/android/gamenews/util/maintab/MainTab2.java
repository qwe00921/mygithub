package com.yy.android.gamenews.util.maintab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.yy.android.gamenews.event.SecondButtomTabEvent;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.NewsFragment;
import com.yy.android.gamenews.ui.ViewPagerFragmentFactory;
import com.yy.android.gamenews.ui.ViewPagerFragmentFactory.PageType;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.sportbrush.R;

public class MainTab2 extends MainFragmentTab {

	public MainTab2(MainActivity context, View button, ActionBar actionbar,
			Bundle savedInstance) {
		super(context, button, actionbar, SecondButtomTabEvent.ORDER_INFO,
				savedInstance);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void customizeActionbar() {
		mActionBar.setRightTextVisibility(View.GONE);
		mActionBar.setOnRightClickListener(null);
		mActionBar.getRightImageView().setImageResource(0);
		mActionBar.setRightVisibility(View.INVISIBLE);
	}

	/**
	 * 第二个tab默认为频道文章列表
	 */
	@Override
	public Fragment initFragment() {
		return ViewPagerFragmentFactory.createViewPagerFragment(PageType.NEWS,
				null);
	}

	@Override
	public String getTabName() {
		return mContext.getString(R.string.mine);
	}

	@Override
	public void refresh() {
		NewsFragment fragment = (NewsFragment) mFragment;
		fragment.refreshChannelPager();
		fragment.showTab(0);
	}
}
