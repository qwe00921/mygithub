package com.niuan.android.lib.ui;

import android.support.v4.app.Fragment;
import android.view.View;

public class ViewPage {

	public static final int TYPE_FRAGMENT = 1001;
	public static final int TYPE_VIEW = 1002;
	public static final int TYPE_VIEW_FRAGMENT = 1003;
	public static final int TYPE_NONE = 1000;
	
	private int type;
	private String title;
	private Fragment fragment;
	private View view;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
