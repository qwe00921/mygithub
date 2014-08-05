package com.tencent.djcity.discover;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.activity.BaseActivity;

public class ScratchCardActivity extends BaseActivity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discoverlist);
		loadNavBar(R.id.navigation_bar);
	
		
		
	}
	
}
