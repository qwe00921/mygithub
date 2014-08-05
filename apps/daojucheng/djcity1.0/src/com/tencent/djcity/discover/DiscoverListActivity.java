package com.tencent.djcity.discover;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.activity.BaseActivity;

public class DiscoverListActivity extends BaseActivity implements OnItemClickListener{
	private ListView  mDiscoverList;
	private OptionAdapter mDiscoverAdapter;
	int optrid[] = {R.drawable.ico_find_card,
			R.drawable.ico_find_shake,
			R.drawable.ico_find_get};
		
	int optstr[] = {R.string.scratch_card,R.string.shake,R.string.gift_center};//R.string.gift_box};
	
	//private TextView mShakeBtn;
	//private TextView mScratchBtn;
	//private TextView mCenterBtn;
	//private TextView mGiftBoxBtn;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discoverlist);
		loadNavBar(R.id.discover_navbar);
		
		mDiscoverList = (ListView) this.findViewById(R.id.discover_listview);
		mDiscoverAdapter = new OptionAdapter(this);
		mDiscoverAdapter.setResdata(optrid,optstr);
		
		mDiscoverList.setAdapter(mDiscoverAdapter);
		mDiscoverList.setOnItemClickListener(this);
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position == 0)
		{
			UiUtils.startActivity(this, GuaGuaKaActivity.class
					, true);
		}
		else if(position == 1)
		{
			UiUtils.startActivity(this, ShakeActivity.class
					, true);
		}
		else if(position == 2)
		{
			UiUtils.startActivity(this, GiftcenterActivity.class
					, true);
		}
		
	}
	
}
