package com.niuan.wificonnector;

import java.util.Arrays;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

import com.niuan.wificonnector.lib.ui.ViewPagerAdapter;
import com.niuan.wificonnector.lib.ui.ViewPagerFragment;

public class MainFragment extends ViewPagerFragment {

	@Override
	protected PagerAdapter getAdapter() {

		SectionsPagerAdapter adapter = new SectionsPagerAdapter(
				getChildFragmentManager());

		String[] titles = { "连接", "地图", "安全须知", "百宝箱"};

		adapter.updateDataSource(Arrays.asList(titles));

		return adapter;
	}
	
	@Override
	protected boolean needCheckDivide() {
		return true;
	}

	public class SectionsPagerAdapter extends ViewPagerAdapter<String> {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String channel = getData(position);
			return channel;
		}

		@Override
		public Fragment getFragment(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0: {
				fragment = new WifiListFragment();
				break;
			}
			case 1: {
				fragment = new BaseMapDemo();
				break;
			}
			case 2: {
				fragment = new SecurityFragment();
				break;
			}
			case 3: {
				fragment = new MoreFragment();
				break;
			}
			case 4: {

				fragment = new EmptyFragment();
				break;
			}
			}
			return fragment;
		}
	}
}
