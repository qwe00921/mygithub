package com.tencent.djcity.lib.control;

import com.tencent.djcity.util.activity.BaseActivity;

public class BaseControl {

	protected BaseActivity mActivity;

	public BaseControl(BaseActivity activity) {
		mActivity = activity;
	}
	
	public void destroy(){
		mActivity = null;
	}
}
