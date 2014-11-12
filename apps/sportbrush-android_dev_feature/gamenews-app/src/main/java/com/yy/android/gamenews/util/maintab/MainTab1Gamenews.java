package com.yy.android.gamenews.util.maintab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.duowan.gamenews.Channel;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.ui.ChannelArticleInfoFragment;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.view.ActionBar;
/**
 * 游戏刷子首页的第一个 tab为单独的广场
 * @author liuchaoqun
 *
 */
public class MainTab1Gamenews extends MainTab1 {

	public MainTab1Gamenews(MainActivity context, View button,
			ActionBar actionbar, Bundle savedInstance) {
		super(context, button, actionbar, savedInstance);
		// TODO Auto-generated constructor stub
	}

//	/**
//	 * 游戏刷子的第一个tab为单独的广场
//	 */
//	@Override
//	public Fragment initFragment() {
//
//		Channel channel = new Channel();
//		channel.setId(Constants.RECOMMD_ID);
//		Fragment fragment = ChannelArticleInfoFragment.newInstance(channel, null);
//		return fragment;
//	}

}
