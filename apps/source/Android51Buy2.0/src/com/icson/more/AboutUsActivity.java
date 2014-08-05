package com.icson.more;

import java.sql.Date;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IVersion;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;

public class AboutUsActivity extends BaseActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.more_aboutus_activity);
		loadNavBar(R.id.aboutus_navbar);

		String strVersion = "版本: " + IVersion.getVersionName();
		String strInfo = ServiceConfig.getInfo();
		if( !TextUtils.isEmpty(strInfo) )
			strVersion += strInfo;
		((TextView) findViewById(R.id.more_contactus_version)).setText(strVersion);

		// Update copyright.
		Date pCurrent = new Date(System.currentTimeMillis());
		final int nYear = pCurrent.getYear() + 1900;
		String strCopyright = this.getString(R.string.icson_copyright_time, nYear);
		((TextView) findViewById(R.id.more_contactus_copyright_time)).setText(strCopyright);
		((TextView) findViewById(R.id.more_contactus_copyright))
		.setText(getString(R.string.icson_copyright));

	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_AboutUsActivity);
	}
}