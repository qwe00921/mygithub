package com.yy.android.gamenews.util.maintab;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;

import com.yy.android.gamenews.event.ThirdButtomTabEvent;
import com.yy.android.gamenews.plugin.schetable.GameListFragment;
import com.yy.android.gamenews.plugin.schetable.SchedFragment;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.sportbrush.R;

public class MainTab3SportBrush extends MainFragmentTab {

	public MainTab3SportBrush(MainActivity context, View button,
			ActionBar actionbar, Bundle savedInstance) {
		super(context, button, actionbar, ThirdButtomTabEvent.THIRD_TAB_INFO,
				savedInstance);
	}

	@Override
	protected void customizeActionbar() {
		if (((SchedFragment) getFragment()).getCurrentItem() == 0) {
			mActionBar.setRightTextVisibility(View.VISIBLE);
			mActionBar.setOnRightClickListener(mOnRightClickListener);
			mActionBar.setRightTextResource(R.string.sched_table_filter);
			mActionBar.getRightImageView().setImageResource(0);
		} else {
			mActionBar.setRightTextVisibility(View.GONE);
			mActionBar.setOnRightClickListener(null);
			mActionBar.getRightImageView().setImageResource(0);
			mActionBar.getRightTextView().setText("");
		}
	}

	private OnClickListener mOnRightClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			((SchedFragment) getFragment()).showFilterView(mActionBar);
		}
	};

	@Override
	protected Fragment initFragment() {
		SchedFragment fragment = SchedFragment.newInstance();
		return fragment;
	}

	@Override
	protected View getTitleContainer() {
		Fragment fragment = getFragment();
		if (fragment instanceof SchedFragment && fragment.isResumed()) {
			List<Fragment> fragments = ((SchedFragment) fragment)
					.getChildFragmentManager().getFragments();
			if (fragments != null && fragments.size() > 0) {
				GameListFragment gameListFragment = (GameListFragment) fragments
						.get(0);
				if (gameListFragment != null
						&& gameListFragment.mCalendarView != null) {
					return gameListFragment.mCalendarView;
				}
			}
		}
		return null;
	}

	public void setRightImageViewVisibility(int visibility) {
		if (visibility == View.VISIBLE) {
			mActionBar.setRightTextVisibility(View.VISIBLE);
			mActionBar.setOnRightClickListener(mOnRightClickListener);
			mActionBar.setRightTextResource(R.string.sched_table_filter);
			mActionBar.getRightImageView().setImageResource(0);
		} else {
			mActionBar.setRightTextVisibility(View.GONE);
			mActionBar.setOnRightClickListener(null);
			mActionBar.getRightImageView().setImageResource(0);
			mActionBar.getRightTextView().setText("");
		}
	}

	@Override
	public String getTabName() {
		return mContext.getString(R.string.main_info_extra1);
	}

}
