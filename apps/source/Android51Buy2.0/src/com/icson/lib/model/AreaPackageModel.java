package com.icson.lib.model;

import java.util.ArrayList;

import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.IShippingArea;

public class AreaPackageModel extends BaseModel {
	private ProvinceModel mProvinceModel;
	private CityModel mCityModel;
	private ZoneModel mDistrictModel;

	public String getProvinceLable(String defaultName) {
		return (mProvinceModel == null || mProvinceModel.getProvinceName()== null) ? defaultName : mProvinceModel.getProvinceName();
	}
	
	public String getProvinceLable() {
		return getProvinceLable("");
	}

	public ProvinceModel getProvinceModel(){
		return this.mProvinceModel;
	}

	
	public String getCityLabel(String defaultName) {
		return (mCityModel == null || mCityModel.getCityName() == null) ? defaultName : mCityModel.getCityName();
	}
	
	public String getCityLabel() {
		return getCityLabel("");
	}

	public CityModel getCityModel(){
		return this.mCityModel;
	}

	public String getDistrictLable(String defaultName) {
		return (mDistrictModel == null || mDistrictModel.getZoneName() == null) ? defaultName : mDistrictModel.getZoneName();
	}

	public String getDistrictLable() {
		return getDistrictLable("");
	}
	
	public ZoneModel getDistrictModel(){
		return this.mDistrictModel;
	}
	
	public boolean isEmptyOfPackage(){
		boolean isEmpty = false;
		if(null == mDistrictModel || null == mCityModel || null == mProvinceModel) {
			isEmpty = true;
		}
		return isEmpty;
	}

	public AreaPackageModel(int districtId) {
		ArrayList<ProvinceModel> models =  IShippingArea.getAreaModels();

		if (models == null) {
			mDistrictModel = null;
			mCityModel = null;
			mProvinceModel = null;
		}else{
			boolean isContinue = true;
			int nProvinceNum = models.size();
			ProvinceModel pProvinceModel;
			CityModel pCityModel;
			ZoneModel pDistrictModel;
			for (int nProId = 0; nProId < nProvinceNum && isContinue; nProId ++ ) {
				pProvinceModel = models.get(nProId);
				ArrayList<CityModel> pCityModels = pProvinceModel.getCityModels();
				
				int nCityNum = pCityModels.size();
				for(int nCityId = 0; nCityId < nCityNum && isContinue; nCityId ++ ) {
					pCityModel = pCityModels.get(nCityId);
					ArrayList<ZoneModel> pDistrictModels = pCityModel.getZoneModels();
					
					int nDistrictNum = pDistrictModels.size();
					for(int nDistrictId = 0; nDistrictId < nDistrictNum; nDistrictId ++ ) {
						pDistrictModel = pDistrictModels.get(nDistrictId);
						if(pDistrictModel.getZoneId() == districtId) {
							mProvinceModel = pProvinceModel;
							mCityModel = pCityModel;
							mDistrictModel = pDistrictModel;
							isContinue = false;
							break;
						}
					}
				}
			}
		}
		
	}
	
	
	public AreaPackageModel(int provinceId, int cityId, int districtId) {
		ArrayList<ProvinceModel> models =  IShippingArea.getAreaModels();

		if (models == null) {
			mDistrictModel = null;
			mCityModel = null;
			mProvinceModel = null;
		}else{
			boolean isContinue = true;
			int nProvinceNum = models.size();
			ProvinceModel pProvinceModel;
			for (int nProId = 0; nProId < nProvinceNum && isContinue; nProId ++ ) {
				pProvinceModel = models.get(nProId);
				if(provinceId == pProvinceModel.getProvinceId()) {
					mProvinceModel = pProvinceModel;
					break;
				}
			}
			
			if(null != mProvinceModel) {
				CityModel pCityModel;
				ArrayList<CityModel> pCityModels = mProvinceModel.getCityModels();
				int nCityNum = pCityModels.size();
				for(int nCityId = 0; nCityId < nCityNum && isContinue; nCityId ++ ) {
					pCityModel = pCityModels.get(nCityId);
					if(cityId == pCityModel.getCityId()) {
						mCityModel = pCityModel;
						break;
					}
				}
				
			}
			
			if(null != mProvinceModel && null != mCityModel) {
				ZoneModel pDistrictModel;
				ArrayList<ZoneModel> pDistrictModels = mCityModel.getZoneModels();
				int nDistrictNum = pDistrictModels.size();
				for(int nDistrictId = 0; nDistrictId < nDistrictNum; nDistrictId ++ ) {
					pDistrictModel = pDistrictModels.get(nDistrictId);
					if(pDistrictModel.getZoneId() == districtId) {
						mDistrictModel = pDistrictModel;
						break;
					}
				}
				
			}
		}
	}
				
}
