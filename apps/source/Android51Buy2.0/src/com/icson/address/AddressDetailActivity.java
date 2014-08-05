package com.icson.address;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.AreaPackageModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class AddressDetailActivity extends BaseActivity {
	public static final int FLAG_RESULT_SAVED = 1;
	public static final int FLAG_RESULT_DELETED = 2;
	private static final String LOG_TAG = AddressDetailActivity.class.getName();
	public static final String REQUEST_ADDRESS_MODEL = "address_model";
	public static final String RESPONSE_ADDRESS_MODEL = "address_model";
	public static final String REQUEST_FROM_MYADDRESS = "request_from_myaddress";
	final boolean FAILED = false;
	final boolean OK = true;
	private String AREA_SELECT_DEFAULT;

	private AddressModel mAddressModel;
	private AddressControl mAddressControl;
	private AddressModel mInitAddressModel;
	private boolean 	mFromMyAddress;
	
	private EditField mAddressAddProvince;
	private EditField mAddressAddCity;
	private EditField mAddressAddZone;
	private EditField mPersonName;
	private EditField mPhoneNum;
	private EditField mAddressDetail;
	
	private TextView mButtonOK;
	private TextView mButtonDelete;
	private AppDialog pDialog;
	private String mPageId;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_add_address);
		loadNavBar(R.id.address_add_navigation_bar);
		
		AREA_SELECT_DEFAULT = getString(R.string.select_default);
		mAddressControl = new AddressControl(this);
		init();
	}

	private void init() {
		mPersonName = (EditField) findViewById(R.id.address_add_name);
		mPhoneNum = (EditField) findViewById(R.id.address_add_phone);
		mPhoneNum.setEditInputType(InputType.TYPE_CLASS_PHONE);
		
		mAddressDetail = (EditField) findViewById(R.id.address_add_address_detail);
		
		mAddressAddProvince = (EditField) findViewById(R.id.address_add_province);
		mAddressAddCity = (EditField) findViewById(R.id.address_add_city);
		mAddressAddZone = (EditField) findViewById(R.id.address_add_zone);
		
		mPageId = getString(R.string.tag_AddressDetailActivity);
		
		mAddressAddProvince.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				selectProvince();
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, AddressDetailActivity.class.getName(), mPageId, "01001");
			}
		});
		
		mAddressAddCity.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				selectShi(false);
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, AddressDetailActivity.class.getName(), mPageId, "01002");
			}
		});
		
		mAddressAddZone.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				selectArea();
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, AddressDetailActivity.class.getName(), mPageId, "01003");
			}
		});
		
		mButtonOK = (TextView) findViewById(R.id.address_btn_okay);
		mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				add();
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, AddressDetailActivity.class.getName(), mPageId, "01004");
			};
		});
		
		mButtonDelete = (TextView) findViewById(R.id.address_btn_delete);
		mButtonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(pDialog == null)
				{
					pDialog = UiUtils.showDialog(AddressDetailActivity.this, R.string.caption_hint, 
						R.string.is_sure_del_address, R.string.btn_delete,R.string.btn_cancel,
						new AppDialog.OnClickListener(){

							@Override
							public void onDialogClick(int nButtonId) {
								if(AppDialog.BUTTON_POSITIVE == nButtonId)
									deleteAddress(mInitAddressModel);
								//else if(null!=pDialog)
								//	pDialog.hide();
							}});
				}
				else
					pDialog.show();
				
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, AddressDetailActivity.class.getName(), mPageId, "01005");
			};
		});
		
		Intent intent = getIntent();
		if(null != intent) {
			mFromMyAddress = intent.getBooleanExtra(REQUEST_FROM_MYADDRESS, false);
			mInitAddressModel = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.REQUEST_ADDRESS_MODEL);
			if(null != mInitAddressModel) {
				mButtonDelete.setVisibility(View.VISIBLE);
				initData();
			}else{
				mButtonDelete.setVisibility(View.GONE);
			}
			
		}
//		if (getIntent().getSerializableExtra(AddressDetailActivity.REQUEST_ADDRESS_MODEL) != null) {
//			mInitAddressModel = (AddressModel) getIntent().getSerializableExtra(AddressDetailActivity.REQUEST_ADDRESS_MODEL);
//			mButtonDelete.setVisibility(View.VISIBLE);
//			initData();
//		}else{
//			mButtonDelete.setVisibility(View.GONE);
//		}

//		((TextView) findViewById(R.id.global_textview_title)).setText(mInitAddressModel == null || mInitAddressModel.getAid() == 0 ? R.string.title_address_add : R.string.title_address_modify);
	}

	
	private void initData() {
		mPersonName.setContent(mInitAddressModel.getName());
		mPhoneNum.setContent(mInitAddressModel.getMobile() == null || mInitAddressModel.getMobile().equals("") ? (mInitAddressModel.getPhone() == null ? "" : mInitAddressModel.getPhone()) : mInitAddressModel.getMobile());
		mAddressDetail.setContent(mInitAddressModel.getAddress() == null ? "" : mInitAddressModel.getAddress());
		
		AreaPackageModel mAreaPackageModel = new AreaPackageModel(mInitAddressModel.getDistrict());
		mAddressAddZone.setContent(mAreaPackageModel.getDistrictLable(AREA_SELECT_DEFAULT));
		mAddressAddZone.setTag(mAreaPackageModel.getDistrictModel());

		mAddressAddCity.setContent(mAreaPackageModel.getCityLabel(AREA_SELECT_DEFAULT));
		mAddressAddCity.setTag(mAreaPackageModel.getCityModel());

		mAddressAddProvince.setContent(mAreaPackageModel.getProvinceLable(AREA_SELECT_DEFAULT));
		mAddressAddProvince.setTag(mAreaPackageModel.getProvinceModel());

		if (mAddressAddProvince.getTag() == null || mAddressAddCity.getTag() == null || mAddressAddZone.getTag() == null) {
			UiUtils.makeToast(this, R.string.area_init_error);
		}

		
	//	((EditText) findViewById(R.id.address_add_editbox_zipcode)).setText(mInitAddressModel.getZipcode() == null ? "" : mInitAddressModel.getZipcode());
	}

	private void selectProvince() {

		final int selectedId = mAddressAddProvince.getTag() == null ? 0 : ((ProvinceModel) mAddressAddProvince.getTag()).getProvinceId();
		int selectedIndex = 0;

		final ArrayList<ProvinceModel> pProviceModels = IShippingArea.getAreaModels();
		if (pProviceModels == null) {
			UiUtils.makeToast(this, R.string.address_not_exist,true);
			Log.e(LOG_TAG, "getAllProvince return empty.");
			finish();
			return;
		}

		final int nSize = pProviceModels.size();
		if( 0 >= nSize )
			return ;
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ProvinceModel pMode = pProviceModels.get(nIdx);
			names[nIdx] = pMode.getProvinceName();
			if (selectedId != 0 && pMode.getProvinceId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		UiUtils.showListDialog(this, getString(R.string.select_province), names, selectedIndex, new RadioDialog.OnRadioSelectListener() {
			
			@Override
			public void onRadioItemClick(int which) {
				final ProvinceModel model = pProviceModels.get(which);

				if (selectedId != Integer.valueOf(model.getProvinceId())) {
					mAddressAddProvince.setContent(model.getProvinceName());
					mAddressAddProvince.setTag(model);

					selectShi(true);

					mAddressAddZone.setContent(AREA_SELECT_DEFAULT);
					mAddressAddZone.setTag(null);
				}
				
			}
		});
	}

	private void selectShi(boolean autoComplete) {
		if (autoComplete) {
			if (mAddressAddProvince.getTag() != null) {
				final ProvinceModel pProvinceModel = (ProvinceModel) mAddressAddProvince.getTag();
				final ArrayList<CityModel> pCityModels = pProvinceModel.getCityModels();
				if (pCityModels != null && pCityModels.size() == 1) {
					mAddressAddCity.setContent(pCityModels.get(0).getCityName());
					mAddressAddCity.setTag(pCityModels.get(0));
					return;
				}
			}
			mAddressAddCity.setContent(AREA_SELECT_DEFAULT);
			mAddressAddCity.setTag(null);
			return;
		}

		if (mAddressAddProvince.getTag() == null) {
			UiUtils.makeToast(this, R.string.select_province_first);
			return;
		}
		

		final int selectedId = mAddressAddCity.getTag() == null ? 0 : ((CityModel) mAddressAddCity.getTag()).getCityId();
		int selectedIndex = 0;

		final ProvinceModel pProvinceModel = (ProvinceModel) mAddressAddProvince.getTag();
		final ArrayList<CityModel> pCityModels = pProvinceModel.getCityModels();
		if (pCityModels == null) {
			UiUtils.makeToast(this, R.string.select_province_first);
			return;
		}

		final int nSize = pCityModels.size();
		String names[] = new String[nSize];
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			CityModel pModel = pCityModels.get(nIdx);
			names[nIdx] = pModel.getCityName();
			if (selectedId != 0 && pModel.getCityId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		UiUtils.showListDialog(this, getString(R.string.select_city), names, selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				final CityModel model = pCityModels.get(which);

				if (selectedId != Integer.valueOf(model.getCityId())) {
					mAddressAddCity.setContent(model.getCityName());
					mAddressAddCity.setTag(model);
					mAddressAddZone.setTag(null);
					mAddressAddZone.setContent(AREA_SELECT_DEFAULT);
				}
			}
		});
	}

	private void selectArea() {

		if (mAddressAddCity.getTag() == null) {
			UiUtils.makeToast(this, R.string.select_city_first);
			return;
		}

		final int selectedId = mAddressAddZone.getTag() == null ? 0 : ((ZoneModel) mAddressAddZone.getTag()).getZoneId();
		int selectedIndex = 0;

		final CityModel pCityModel = (CityModel) mAddressAddCity.getTag();
		final ArrayList<ZoneModel> pZoneModels = pCityModel.getZoneModels();
		if (pZoneModels == null) {
			UiUtils.makeToast(this, R.string.select_city_first);
			return;
		}

		final int nSize = pZoneModels.size();
		String names[] = new String[nSize];
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ZoneModel pModel = pZoneModels.get(nIdx);
			names[nIdx] = pModel.getZoneName();
			
			if (selectedId != 0 && pModel.getZoneId() == selectedId) {
				selectedIndex = nIdx;
			}
		}

		UiUtils.showListDialog(this, getString(R.string.select_area), names, selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				final ZoneModel model = pZoneModels.get(which);

				if (selectedId != Integer.valueOf(model.getZoneId())) {
					mAddressAddZone.setContent(model.getZoneName());
					mAddressAddZone.setTag(model);
				}
			}
		});
	}
	
	private void onOperatonFinish(int nResultCode, AddressModel addressModel) {
		Intent intent = new Intent();
		intent.putExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL, addressModel);
		setResult(nResultCode, intent);
		finish();
	}

	private void add() {
		if (checkUserInput() == false) {
			return;
		}

		if (checkhaveModified() == false) {
			onOperatonFinish(FLAG_RESULT_SAVED, mAddressModel);
			return;
		}

		showProgressLayer();
		mAddressControl.set(mAddressModel, new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();

				try {
					String data = "";
					final int errno = v.getInt("errno");
					if (errno != 0) {
						switch (errno) {
						case 6:
						case 7:
							data = getString(R.string.phone_number_error);
							break;
						default:
							data = v.has("data") ? v.getString("data") : "";
						}
						UiUtils.makeToast(AddressDetailActivity.this, data.equals("") ? Config.NORMAL_ERROR : data);
						return;
					}

					final boolean newModel = mAddressModel.getAid() == 0;
					if (newModel) {
						mAddressModel.setAid(v.getJSONObject("data").getInt("aid"));
					}
					
					if(mFromMyAddress) {
						updateDefaultAddressInfo();
					}
					onOperatonFinish(FLAG_RESULT_SAVED, mAddressModel);
				} catch (Exception ex) {
					Log.e(LOG_TAG, "add|add|" + ToolUtil.getStackTraceString(ex));
					UiUtils.makeToast(AddressDetailActivity.this, Config.NORMAL_ERROR);
				}
			}
		}, this);
	}
	
	/*
	 * 如果用户修改地址是保存的默认地址，且区域id改变，则更新保存的默认区域id
	 */
	private void updateDefaultAddressInfo() {
		IPageCache cache = new IPageCache();
		String strSavedAddressId = cache.get(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		String strSavedDistrictId = cache.get(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID);
		
		if(null != mAddressModel && !TextUtils.isEmpty(strSavedAddressId) && !TextUtils.isEmpty(strSavedDistrictId)) {
			int nSavedAddressId = Integer.parseInt(strSavedAddressId);
			int nSavedDistrictId = Integer.parseInt(strSavedDistrictId);
			if( nSavedAddressId == mAddressModel.getAid() && nSavedDistrictId != mAddressModel.getDistrict()) {
				//update default district id	
				cache.set(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID, String.valueOf(mAddressModel.getDistrict()), 0);
			}
		}
	}
	
	/*
	 * 如果用户删除的地址是保存的默认地址，则清除保存的默认地址和默认区域id
	 */
	private void clearDefaultAddressInfo(AddressModel pAddressModel){
		if( null == pAddressModel ) {
			return;
		}
		
		IPageCache cache = new IPageCache();
		String strSavedAddressId = cache.get(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		if(!TextUtils.isEmpty(strSavedAddressId)){
			int nSavedAddressId = Integer.parseInt(strSavedAddressId);
			if(nSavedAddressId == pAddressModel.getAid()) {
				cache.set(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID, "0", 0);
				cache.set(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID, "0", 0);
			}
		}
	}
	
	public void deleteAddress(final AddressModel pAddressModel) {
		if (pAddressModel == null) {
			return;
		}
		
		showProgressLayer();
		mAddressControl.remove(pAddressModel.getAid(), new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					clearDefaultAddressInfo(pAddressModel);
					onOperatonFinish(FLAG_RESULT_DELETED, pAddressModel);
				} else {
					UiUtils.makeToast(AddressDetailActivity.this, v.optString("data").equals("") ? Config.NORMAL_ERROR : v.optString("data"));
				}
			}
		}, this);
	}

	private boolean checkhaveModified() {
		if (mInitAddressModel == null) {
			return true;
		}

		if (!isEqual(mAddressModel.getName(), mInitAddressModel.getName())) {
			return true;
		}

		if (mAddressModel.getDistrict() != mInitAddressModel.getDistrict()) {
			return true;
		}

		if (!isEqual(mAddressModel.getAddress(), mInitAddressModel.getAddress())) {
			return true;
		}

		if (!isEqual(mAddressModel.getZipcode(), mInitAddressModel.getZipcode())) {
			return true;
		}

		if (!isEqual(mAddressModel.getMobile(), mInitAddressModel.getMobile())) {
			return true;
		}

		if (!isEqual(mAddressModel.getPhone(), mInitAddressModel.getPhone())) {
			return true;
		}

		return false;

	}

	private boolean isEqual(String arg1, String arg2) {
		return (arg1 == null && arg2 == null) || (arg1 != null && arg2 != null && arg1.equals(arg2));
	}

	private boolean checkUserInput() {

		mAddressModel = new AddressModel();

		if (mInitAddressModel != null) {
			mAddressModel.setAid(mInitAddressModel.getAid());
			mAddressModel.setMobile(mInitAddressModel.getMobile());
			mAddressModel.setPhone(mInitAddressModel.getPhone());
		}

		final String name = mPersonName.getContent().trim();
		if (name.equals("")) {
			UiUtils.makeToast(this, R.string.no_recipient);
			return FAILED;
		}

		mAddressModel.setName(name);

		final String phone = mPhoneNum.getContent().toString().trim();
		if (phone.equals("")) {
			UiUtils.makeToast(this, R.string.no_phone);
			return FAILED;
		}
		Matcher matcher = Pattern.compile("^1\\d{10}$").matcher(phone);

		if (!matcher.find()) {
			matcher = Pattern.compile("^\\d+-\\d+$").matcher(phone);
			if (!matcher.find()) {
				UiUtils.makeToast(this, R.string.phone_format_error);
				return FAILED;
			} else {
				mAddressModel.setPhone(phone);
				mAddressModel.setMobile("");
			}
		} else {
			mAddressModel.setPhone("");
			mAddressModel.setMobile(phone);
		}

		final int distinct = mAddressAddZone.getTag() == null ? 0 : ((ZoneModel) mAddressAddZone.getTag()).getZoneId();
		if (distinct == 0) {
			UiUtils.makeToast(this, R.string.select_area_first);
			return FAILED;
		}

		mAddressModel.setDistrict(Integer.valueOf(distinct));

		final String address = mAddressDetail.getContent().toString().trim();
		if (address.equals("")) {
			UiUtils.makeToast(this, R.string.address_first);
			return FAILED;
		}

		mAddressModel.setAddress(address);
		mAddressModel.setZipcode("");

		if (TextUtils.isEmpty(mAddressModel.getWorkplace()) || TextUtils.isEmpty(mAddressModel.getWorkplace().trim())) {
			mAddressModel.setWorkplace(mAddressModel.getName().substring(0, Math.min(4, mAddressModel.getName().length())));
		}

		return OK;
	}

	@Override
	public void onDestroy() {
		mAddressModel = null;
		mAddressControl = null;
		if(null!=pDialog)
		{
			pDialog.dismiss();
			pDialog = null;
		}
		IShippingArea.clean();
		
		super.onDestroy();
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_AddressDetailActivity);
	}

}
