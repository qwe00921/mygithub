package com.icson.main;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.FullDistrictHelper.FullDistrictItem;
import com.icson.lib.IShippingArea;
import com.icson.lib.ui.AddressRadioDialog;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.CellInfo;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class gpsSelectCity extends BaseActivity implements OnSuccessListener<JSONObject> {
	//private ArrayList<DispatchItem> mDispatchItems;
	private ArrayList<ProvinceModel> mProviceModelList;
	private ProvinceModel mProvinceModel;
	private ArrayList<CityModel> mCityModelList; 
	private CityModel mCityModel;
	private ArrayList<ZoneModel> mZoneModelList; 
	private ZoneModel mZoneModel;
	
	private SelectProvinceAdapter 	mAdapter;
	private ListView 				mProvinceListView;
	
	private FullDistrictItem mDefaultDistrictItem;
	
	private AppDialog    mOpenGPSDialog;
	private AddressRadioDialog mCityDialog;
	private AddressRadioDialog mZoneDialog;
	private RelativeLayout     mGpsResultLayout;
	private LocationManager    mLocationManager;
	private GPSListener        mGpsListener;
	private TextView           mLocationStatus;
	private TextView           mLocationResult;
	private boolean            mSuccessFlag;
	private Ajax               mLocAjax;
	private CellInfo           mCellInfo;
	private FullDistrictItem pDistrictItem;
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
		
		mSuccessFlag = false;
		//mGpsResultLayout = (RelativeLayout) this.findViewById(R.id.location_layout);
		mGpsResultLayout.setOnClickListener(this);
		mProvinceListView = (ListView) findViewById(R.id.city_list_view);
		//mLocationStatus = (TextView)findViewById(R.id.location_status);
		//mLocationResult = (TextView)findViewById(R.id.location_result);
		
		setNavBarRightVisibility(View.VISIBLE);
		mNavBar.setLeftVisibility(View.GONE);
		
		//get whole full district information
		mProviceModelList = IShippingArea.getAreaModels();
		if (mProviceModelList == null || mProviceModelList.size()<=0 ) {
			finish();
			return;
		}

		//default shanghai -- xuhui
		mDefaultDistrictItem = FullDistrictHelper.getFullDistrict();
		mAdapter = new SelectProvinceAdapter(this, mProviceModelList,mDefaultDistrictItem.mProvinceId);
		mProvinceListView.setAdapter(mAdapter);
		if(mAdapter.mSelectPositionId>=0)
			mProvinceListView.setSelection(mAdapter.mSelectPositionId);
		
		mProvinceListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(null == mProviceModelList || mProviceModelList.size()<= position)
				{
					return;
				}
				mProvinceModel = mProviceModelList.get(position);
				mAdapter.setPicked(position);
				mAdapter.notifyDataSetChanged();
				selectCity();
			}
		});
		
		int [] Aa = {-7, 1, 5, 2, -4, 3, 0};
		int result = solution(Aa);
		
	}
	
	public int solution(int []A)
	{
		
		int sumright [] = new int [A.length];
		int sumall = 0;
		for(int i=0; i<A.length;i++)
		{
			sumall += A[i];
		}
		for(int i=0; i<A.length;i++)
		{
			if(i == 0)
				sumright[i] = sumall - A[i];
			else
				sumright[i] = sumright[i-1] - A[i];
		}
		int count = 0;
		for(int i=0; i <A.length;i++)
		{
			if(sumright[i] == sumall - sumright[i] - A[i])
				count ++;
		}
		
		return -1;
	}
	@Override
	protected void onResume()
	{
		fetchLocationInfo();
		super.onResume();
	}
	/**
	 * 
	 */
	private void selectCity(){
		if(null == mProvinceModel ) {
			return;
		}
		
		int selectedId = mDefaultDistrictItem.mCityId;
		int selectedIndex = 0;
		mCityModelList = mProvinceModel.getCityModels();
		if (mCityModelList == null || mCityModelList.size()<=0) {
			UiUtils.makeToast(this, Config.NORMAL_ERROR);
			return;
		}

		int nSize = mCityModelList.size();
		
		if( 1 == nSize ) {
			mCityModel = mCityModelList.get(0);
			selectZone();
			return;
		}
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			CityModel pMode = mCityModelList.get(nIdx);
			names[nIdx] = pMode.getCityName();
			if (selectedId != 0 && pMode.getCityId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		
		if(null == mCityDialog)
		{
			mCityDialog = UiUtils.showAddressListDialog(this, getString(R.string.select_city), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
				@Override
				public void onRadioItemClick(int pos) {
					if(null == mCityModelList || mCityModelList.size() <= pos)
					{
						mCityDialog.dismiss();
						return;
					}
					mCityModel = mCityModelList.get(pos);
					selectZone();
				}
			}, true);
		}
		else
		{
			mCityDialog.setList(names, selectedIndex);
		}
		mCityDialog.show();
	}
	
	/**
	 * 
	 */
	private void selectZone(){
		if(null == mCityModel || null == mProvinceModel) {
			return;
		}
		
		int selectedId = mDefaultDistrictItem.mDistrictId;
		int selectedIndex = 0;
		mZoneModelList = mCityModel.getZoneModels();
		if (mZoneModelList == null || mZoneModelList.size()<=0) {
			UiUtils.makeToast(this, Config.NORMAL_ERROR);
			return;
		}

		int nSize = mZoneModelList.size();
		if( 1 == nSize ) {
			mZoneModel = mZoneModelList.get(0);
			afterSelectFullDistrict();
			return;
		}
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ZoneModel pMode = mZoneModelList.get(nIdx);
			names[nIdx] = pMode.getZoneName();
			if (selectedId != 0 && pMode.getZoneId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mZoneDialog)
		{
			mZoneDialog = UiUtils.showAddressListDialog(this, getString(R.string.select_area), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() 
			{
				@Override
				public void onRadioItemClick(int pos) 
				{
					if(null == mZoneModelList || mZoneModelList.size() <= pos)
					{
						mZoneDialog.dismiss();
						return;
					}
				
					mZoneModel = mZoneModelList.get(pos);
					
					afterSelectFullDistrict();
				}
			}, true, false);
		}
		else
		{
			mZoneDialog.setList(names, selectedIndex);
		}
		mZoneDialog.show();
	}
	
	private void afterSelectFullDistrict() {

		if(null != mZoneDialog && mZoneDialog.isShowing()) {
			mZoneDialog.dismiss();
		}
	
		if(null != mCityDialog && mCityDialog.isShowing()) {
			mCityDialog.dismiss();
		}
	
		
		//Update UI
		//always set beacuse user choose it
		if(null != mCityModel && null != mProvinceModel && null != mZoneModel) 
		{
			FullDistrictItem pDistrictItem = new FullDistrictItem(mProvinceModel.getProvinceId(), mProvinceModel.getProvinceIPId(), mProvinceModel.getProvinceName(), mCityModel.getCityId(), mCityModel.getCityName(), mZoneModel.getZoneId(), mZoneModel.getZoneName());
			FullDistrictHelper.setFullDistrict(pDistrictItem);
		}
		
		finish();
		
	}

	
	@Override
	protected void onDestroy() {
		if(null!=mZoneDialog && mZoneDialog.isShowing())
			mZoneDialog.dismiss();
		mZoneDialog = null;
		if(null!=mCityDialog && mCityDialog.isShowing())
			mCityDialog.dismiss();
		mCityDialog = null;
		mDefaultDistrictItem = null;
		
		mAdapter = null;
		
		mZoneModel = null;
		if(null!=mZoneModelList)
			mZoneModelList.clear();
		mZoneModelList = null;
		mCityModel = null;
		if(null!=mCityModelList)
			mCityModelList.clear();
		mCityModelList = null;
		mProvinceModel = null;
		if(null!=mProviceModelList)
			mProviceModelList.clear();
		mProviceModelList = null;
		
		IShippingArea.clean();
		
		if(null!=mLocationManager)
		{
			if(null!=mGpsListener)
			{
				mLocationManager.removeUpdates(mGpsListener);
				mLocationManager.removeGpsStatusListener(mGpsListener);
			}
			mGpsListener = null;
			mLocationManager = null;
		}
		
		if(null!=mOpenGPSDialog && mOpenGPSDialog.isShowing())
			mOpenGPSDialog.dismiss();
		mOpenGPSDialog = null;
		
		super.onDestroy();
	}

	class SelectProvinceAdapter extends BaseAdapter{
		private BaseActivity mActivity;
		private ArrayList<ProvinceModel> mProvinceModels;
		private LayoutInflater mInflater;
		private int mSelectPositionId;
		
		public SelectProvinceAdapter(BaseActivity pActivity, ArrayList<ProvinceModel> aProvinceModels, int proid){
			mActivity = pActivity;
			mProvinceModels = aProvinceModels;
			mInflater = mActivity.getLayoutInflater();
			mSelectPositionId = -1;
			
			for(int idx = 0; null!=mProvinceModels && idx<mProvinceModels.size(); idx++)
			{
				if(mProvinceModels.get(idx).getProvinceId() == proid)
				{
					mSelectPositionId = idx;
					break;
				}
			}
		}

		public void setPicked(int position) {
			mSelectPositionId = position;
		}

		@Override
		public int getCount() {
			return (null == mProvinceModels) ? 0 : mProvinceModels.size();
		}

		@Override
		public ProvinceModel getItem(int position) {
			return (null == mProvinceModels) ? null : mProvinceModels.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGrop) {
			ItemHolder holder = null;
			if (null == convertView)
			{
				convertView = mInflater.inflate(R.layout.address_radio_item, null);
				holder = new ItemHolder();
				holder.mArrow = (ImageView) convertView.findViewById(R.id.address_radio_item_arrow);
				holder.mName = (TextView) convertView.findViewById(R.id.address_radio_item_name);
				holder.mLine = (View) convertView.findViewById(R.id.address_radio_item_line);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ItemHolder) convertView.getTag();
			}
			
			// set data
			String strName = mProvinceModels.get(position).getProvinceName();
			holder.mName.setText(strName);
			holder.mName.setTextColor(mActivity.getResources().getColor(mSelectPositionId == position ? R.color.filter_item_checked : R.color.global_text_color ));
			holder.mArrow.setVisibility(View.VISIBLE);
			
			if( position == (mProvinceModels.size() - 1 ) ) {
				holder.mLine.setVisibility(View.INVISIBLE);
			}else{
				holder.mLine.setVisibility(View.VISIBLE);
			}
			
			return convertView;
		}
		
		private class ItemHolder
		{
			ImageView mArrow;
			TextView  mName;
			View mLine;
		}
		
		
		//private int getSelectPositionId(){
		//	int position = -1;
		//	if( 0 != mSelectCityId ){
		//		for(int nId = 0; nId < mDispatchItems.size(); nId ++ ){
		//			DispatchItem item = mDispatchItems.get(nId);
		//			if(item.id == mSelectCityId) {
		//				position = nId;
		//				break;
		//			}
		//		}
		//	}
		//	return position;
		//}
		
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_SelectCityActivity);
	}
	
	@Override
	public void onClick(View v) {
		/*if(v.getId() == R.id.location_layout && mSuccessFlag)
		{
			UiUtils.makeToast(this, R.string.settings_ok);
			FullDistrictHelper.setFullDistrict(pDistrictItem);
			finish();
		}*/
	}
	
	
	private void fetchLocationInfo()
	{
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		boolean gpsOpen = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(!gpsOpen)
		{
			if(null == mOpenGPSDialog)
			{
				mOpenGPSDialog = UiUtils.showDialog(this, getString(R.string.caption_hint), "需要设置打开GPS，是否允许前往", R.string.btn_ok,R.string.btn_cancel,
					new AppDialog.OnClickListener(){

						@Override
						public void onDialogClick(int nButtonId) {
							if(AppDialog.BUTTON_POSITIVE == nButtonId)
							{
								  Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
						          startActivity(callGPSSettingIntent);
						    }else
						    {
						    	fetchLoactionByCell(false);
						    }
						}});
			}
			mOpenGPSDialog.show();
		}
		else
		{
			fetchLoactionByCell(true);
		}
    }
	
	protected void fetchLoactionByCell(boolean withGps) {
		if(withGps)
		{
			try
			{
				//Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
				if(null == mGpsListener)
					mGpsListener = new GPSListener();
				mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5, mGpsListener);
				mLocationManager.addGpsStatusListener(mGpsListener);
			}
			catch(Exception e)
			{}
		}
	
		//cell info
		this.mCellInfo = ToolUtil.getCellInfo(this);
		if(mCellInfo!=null)
		{
			requestLocation();
		}
		
	}

	private void requestLocation() {
		if(null!=mLocAjax)
		{
			mLocAjax.abort();
			mLocAjax = null;
		}
		
		//mLocAjax = ServiceConfig.getAjax(Config.URL_GET_LOCATION);
		if(null == mLocAjax)
			return;
		
		if(this.mCellInfo!=null)
		{	
			if(mCellInfo.lat > 0 && mCellInfo.lng > 0)
			{
				mLocAjax.setData("latitude", mCellInfo.lat);
				mLocAjax.setData("longitude",mCellInfo.lng);
			}
		
			
			mLocAjax.setData("mcc",mCellInfo.mcc );
			mLocAjax.setData("mnc", mCellInfo.mnc);
			mLocAjax.setData("lac", mCellInfo.lac);
			mLocAjax.setData("cellid", mCellInfo.cellId);
		
			this.addAjax(mLocAjax);
			
			mLocAjax.setOnSuccessListener(this);
		
			mLocAjax.send();
		}
		
	}

	class GPSListener implements LocationListener, GpsStatus.Listener
	{

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLocationChanged(Location location) {
			if(null!=location)
			{
				mCellInfo.lat = location.getLatitude();
				mCellInfo.lng = location.getLongitude();
				requestLocation();
			}
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		final int errno = v.optInt("errno");
		if (errno != 0) {
			UiUtils.makeToast(this, v.optString("data", Config.NORMAL_ERROR));
			return;
		}
	
		int provid = v.optInt("prov_id");
		int provipid = v.optInt("prov_ipid");
		String provname = v.optString("province");
		int cityid = v.optInt("city_id");
		String cityname = v.optString("city");
		int districtid = v.optInt("dist_id");
		String districtname = v.optString("district");
		pDistrictItem = new FullDistrictItem(provid, provipid,provname, cityid,cityname,
				districtid,districtname);
		
		this.mSuccessFlag  = true;
		this.mLocationStatus.setText("定位到 ");
		this.mLocationResult.setText(provname + "--" + cityname);
	}
}
