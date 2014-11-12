package com.yy.android.gamenews.util.maintab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.yy.android.gamenews.event.ForthButtonTabEvent;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.ViewPagerFragmentFactory;
import com.yy.android.gamenews.ui.ViewPagerFragmentFactory.PageType;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.sportbrush.R;

public class MainTab3Gamenews extends MainFragmentTab {

	public MainTab3Gamenews(MainActivity context, View button,
			ActionBar actionbar, Bundle savedInstance) {
		super(context, button, actionbar, ForthButtonTabEvent.FORTH_TAB_INFO,
				savedInstance);
	}

	@Override
	protected void customizeActionbar() {
		mActionBar.setRightTextVisibility(View.GONE);
		mActionBar.setOnRightClickListener(null);
		mActionBar.getRightImageView().setImageResource(0);
		mActionBar.setRightVisibility(View.INVISIBLE);
	}

	@Override
	protected Fragment initFragment() {
		return ViewPagerFragmentFactory.createViewPagerFragment(
				PageType.ASSOCIATION_ENTRY, null);
	}

	@Override
	public String getTabName() {
		return mContext.getString(R.string.main_info_extra1);
	}

}
