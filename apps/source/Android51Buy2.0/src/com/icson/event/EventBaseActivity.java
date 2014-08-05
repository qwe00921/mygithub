package com.icson.event;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class EventBaseActivity extends BaseActivity {

	public static final String ERQUEST_EVENT_ID = "event_id";

	protected long mEventId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEventId = getIntent().getLongExtra(ERQUEST_EVENT_ID, 0);
	}
	public long getEventId(){
		return mEventId;
	}
	
	protected void setNavBarStatus(String strText) {
		if( null != mNavBar ) {
			if( TextUtils.isEmpty(strText) )
				mNavBar.setVisibility(View.GONE);
			else
				setNavBarText(strText);
		}
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_EventBaseActivity);
	}
}
