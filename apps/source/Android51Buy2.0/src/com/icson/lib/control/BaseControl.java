package com.icson.lib.control;

import com.icson.util.activity.BaseActivity;

public class BaseControl {

	protected BaseActivity mActivity;

	public BaseControl(BaseActivity activity) {
		mActivity = activity;
	}
	
	public void destroy(){
		mActivity = null;
	}
}
