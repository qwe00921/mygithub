package com.yy.android.gamenews.util.maintab;

import android.os.Bundle;
import android.view.View;

import com.duowan.Comm.ECommAppType;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.view.ActionBar;

public class MainTabFactory {

	public static MainFragmentTab getTab(int index, MainActivity context,
			View button, ActionBar actionBar, Bundle savedInstance) {
		MainFragmentTab tab = null;

		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)) {
			return getTabForSportbrush(index, context, button, actionBar,
					savedInstance);
		} else if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_CARBRUSH)) {
			return getTabForCarbrush(index, context, button, actionBar,
					savedInstance);
		} else if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
			return getTabForGamenews(index, context, button, actionBar,
					savedInstance);
		}

		return tab;
	}

	private static MainFragmentTab getTabForSportbrush(int index,
			MainActivity context, View button, ActionBar actionBar,
			Bundle savedInstance) {
		MainFragmentTab tab = null;
		switch (index) {
		case 0: {
			tab = new MainTab1(context, button, actionBar, savedInstance);
			break;
		}
		case 1: {
			tab = new MainTab2Sportbrush(context, button, actionBar,
					savedInstance);
			break;
		}
		case 2: {
			tab = new MainTab3SportBrush(context, button, actionBar,
					savedInstance);
			break;
		}
		}
		return tab;
	}

	private static MainFragmentTab getTabForGamenews(int index,
			MainActivity context, View button, ActionBar actionBar,
			Bundle savedInstance) {
		MainFragmentTab tab = null;
		switch (index) {
		case 0: {
			tab = new MainTab1Gamenews(context, button, actionBar,
					savedInstance);
			break;
		}
		case 1: {
			tab = new MainTab2Gamenews(context, button, actionBar,
					savedInstance);
			break;
		}
		case 2: {
			tab = new MainTab3Gamenews(context, button, actionBar,
					savedInstance);
			break;
		}
		case 3: {
			break;
		}
		}
		return tab;
	}

	private static MainFragmentTab getTabForCarbrush(int index,
			MainActivity context, View button, ActionBar actionBar,
			Bundle savedInstance) {
		MainFragmentTab tab = null;
		switch (index) {
		case 0: {
			tab = new MainTab1(context, button, actionBar, savedInstance);
			break;
		}
		case 1: {
			tab = new MainTab2Carbrush(context, button, actionBar,
					savedInstance);
			break;
		}
		case 2: {
			break;
		}
		}
		return tab;
	}

}
