package com.icson.more;

import java.util.ArrayList;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.inc.DispatchFactory.DispatchItem;
import com.icson.lib.ui.UiUtils;
import com.icson.util.activity.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectCityActivity extends BaseActivity {
	public final static String RESPONSE_SELECT_CITY 	= "city_name";
	public final static String SOURCE_SELECT_CITY 	= "from";
	public final static String SELECT_CITY_FROM_HOME 	= "home";
	public final static int REQUEST_SELECT_CITY 		= 1;
	public final static int RESULT_OK 				= 3;
	
	private ArrayList<DispatchItem> mDispatchItems;
	private SelectCityAdapter 		mAdapter;
	private ListView 				mCityListView;
	private int						mSelectedCityId;
	private DispatchItem 			mItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_select_city);
		loadNavBar(R.id.navigation_bar);
		
		mNavBar.setRightInfo(R.string.close_string, new OnClickListener(){
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		mCityListView = (ListView) findViewById(R.id.city_list_view);
		
		Intent mIntent = getIntent();
		String strFrom = mIntent.getStringExtra(SOURCE_SELECT_CITY);
		if(null != strFrom && strFrom.equals(SELECT_CITY_FROM_HOME)){
//			String strCityName = (null == DispatchFactory.getDefaultCityName()) ? getString(R.string.shanghai_city) : DispatchFactory.getDefaultCityName();
//			setNavBarText(getString(R.string.settings_current_city) + strCityName);
			setNavBarRightVisibility(View.VISIBLE);
			mNavBar.setLeftVisibility(View.GONE);
		}else{
			setNavBarRightVisibility(View.GONE);
			mNavBar.setLeftVisibility(View.VISIBLE);
		}
		
		mSelectedCityId = getSelectCityId();
		mDispatchItems = DispatchFactory.getDataSource();
		mAdapter = new SelectCityAdapter(this, mDispatchItems, mSelectedCityId);
		mCityListView.setAdapter(mAdapter);
		
		mCityListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mItem = mAdapter.getItem(position);
				
				UiUtils.makeToast(SelectCityActivity.this, 
						getString(R.string.settings_current_city) + mItem.name, true);
				
				processBack();
			}
		});
	}
	
	private int getSelectCityId(){
		int nCityId = 0;
		IPageCache cache = new IPageCache();
		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);
		
		nCityId = id != null ? Integer.valueOf( id ) : 0;
		return nCityId;
	}
	
	@Override
	protected void onPause() {
		if(null!=mItem)
		{
			FullDistrictHelper.setFullDistrict(mItem.provinceId, mItem.cityId, mItem.district, mItem.siteId);
		}
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		mDispatchItems = null;
		mAdapter = null;
		mCityListView = null;
		mItem = null;
		
		IShippingArea.clean();
		super.onDestroy();
	}

	class SelectCityAdapter extends BaseAdapter{
		private BaseActivity mActivity;
		private ArrayList<DispatchItem> mDispatchItems;
		private LayoutInflater mInflater;
		private int mSelectCityId;
		private int mSelectPositionId;
		
		public SelectCityAdapter(BaseActivity pActivity, ArrayList<DispatchItem> pDispatchItems, int pSelectCityId){
			mActivity = pActivity;
			mDispatchItems = pDispatchItems;
			mInflater = mActivity.getLayoutInflater();
			mSelectCityId = pSelectCityId;
		}

		@Override
		public int getCount() {
			return mDispatchItems.size();
		}

		@Override
		public DispatchItem getItem(int position) {
			return mDispatchItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGrop) {
			ItemHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.adapter_select_city_item, null);
				holder = new ItemHolder();
				holder.pCityName = (TextView) convertView.findViewById(R.id.city_name);
				holder.pImage = (ImageView) convertView.findViewById(R.id.city_tick);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}
			
			mSelectPositionId = getSelectPositionId();
			DispatchItem item = this.getItem(position);
			holder.pCityName.setText(item.name);
			holder.pCityName.setTextColor(mActivity.getResources().getColor(mSelectPositionId == position ? R.color.filter_item_checked : R.color.global_text_color ));
			holder.pImage.setVisibility(mSelectPositionId == position ? View.VISIBLE : View.GONE);
			
			return convertView;
		}
		
		private int getSelectPositionId(){
			int position = -1;
			if( 0 != mSelectCityId ){
				for(int nId = 0; nId < mDispatchItems.size(); nId ++ ){
					DispatchItem item = mDispatchItems.get(nId);
					if(item.id == mSelectCityId) {
						position = nId;
						break;
					}
				}
			}
			
			return position;
			
		}
		
		class ItemHolder{
			TextView pCityName;
			ImageView pImage;
		}
		
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_SelectCityActivity);
	}
}
