package com.icson.main;

import com.icson.push.MsgEntity;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

public abstract class StackActivityGroup extends ActivityGroup {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = getLocalActivityManager();
	}

	public abstract ViewGroup getContainer();

	public void startSubActivity(Class<?> which, Bundle param) {
		Intent intent = new Intent();
		intent.setClassName(this.getPackageName(), which.getName());
		if (param != null) {
			intent.putExtras(param);
			//message must clear_top otherwise will not call onNewIntent
			if(param.containsKey(MsgEntity.SERIAL_NAME_MSGENTITY))
			{
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}
		}
		startSubActivity(intent);
	}

	public void startSubActivity(Intent intent) {
		final String strClassName = intent.getComponent().getClassName();
		if (mContainer == null) {
			mContainer = getContainer();

		}
		
		// Check whether the activity already exists.
		View pDecorView = mManager.startActivity(strClassName, intent).getDecorView();
		if( null != pDecorView && null != mContainer )
		{
			mContainer.removeAllViews();
			mContainer.addView(pDecorView);
		}
	}

	public void startSubActivity(Class<?> which) {
		startSubActivity(which, null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Activity activity = mManager.getCurrentActivity();
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (activity != null) {
				activity.openOptionsMenu();
				return true;
			}
		} else if (activity != null && activity.onKeyDown(keyCode, event) == true) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Activity activity = mManager.getCurrentActivity();
		if (activity == null || activity.onKeyUp(keyCode, event) != true) {
			return super.onKeyUp(keyCode, event);
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		
		mManager = null;
		mContainer = null;
		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		try{
			return super.dispatchKeyEvent(event);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	private LocalActivityManager mManager;
	private ViewGroup mContainer;
}
