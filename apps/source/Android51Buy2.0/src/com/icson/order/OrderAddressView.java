package com.icson.order;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.icson.R;
import com.icson.address.AddressControl;
import com.icson.address.AddressListActivity;
import com.icson.address.AddressModel;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.model.AreaPackageModel;
import com.icson.lib.parser.AddressParser;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class OrderAddressView extends OrderBaseView<AddressModel, ArrayList<AddressModel>> {

	private static final String LOG_TAG = OrderAddressView.class.getName();

	public static final int FLAG_REQUEST_SHIPPING_ADDRESS = 2;

	private AddressControl mAddressControl;

	public OrderAddressView(OrderConfirmActivity activity) {
		super(activity);
		mAddressControl = new AddressControl(mActivity);
		mParser = new AddressParser();
		
		IShippingArea.getAreaModels();
	}

	public void requestFinish() {
		mIsRequestDone = true;
		renderAddress();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_ADDRESS_VIEW);
	}

	public void getAddressList() {
		mIsRequestDone = false;
		mModel = null;
		mAddressControl.getAddressList((AddressParser) mParser, this, this);
	}
	
	public int getDistrict(){
		if(mModel==null)
			return -1;
		return mModel.getDistrict();
	}

	@Override
	public void onSuccess(ArrayList<AddressModel> v, Response response) {
		
		if( !mParser.isSuccess() ){
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			mModel = null;
			requestFinish();
			return;
		}
		//当前分站id
		int mSiteId = ILogin.getSiteId();
		//初始化AID
		IPageCache cache = new IPageCache();
		String said = cache.get(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);

		int initAddressId = said == null ? 0 : Integer.valueOf(said);
		//如果cache保存的地址id不为空
		if (initAddressId != 0) {
			
			for (AddressModel model : v) {
				if (initAddressId == model.getAid()) {
					AreaPackageModel pInitAddress = new AreaPackageModel(model.getDistrict());
					int initAddressSiteId = DispatchFactory.getSiteId(pInitAddress.getProvinceLable());
					//cache保存的地址和用户浏览的分站相同，那么从拉取的地址列表中选择相应地址
					if(initAddressSiteId == mSiteId){
						mModel = model;
						break;
					}
				}
			}
		}

		final int nSize = v.size();
		if (mModel == null && nSize > 0) {
			
			for( AddressModel pAddress : v ){
				AreaPackageModel pPackage = new AreaPackageModel(pAddress.getDistrict());
				int pSiteId = DispatchFactory.getSiteId(pPackage.getProvinceLable());
				// 取得和当前分站相同的地址
				if( mSiteId == pSiteId){
					mModel = pAddress;
					break;
				}
			}	
			
		}

		requestFinish();

	}

	private void renderAddress() {
		IPageCache cache = new IPageCache();
		TextField addressView = (TextField)mActivity.findViewById(R.id.orderconfirm_address);
		if (mModel != null) {
			AreaPackageModel addrPack = new AreaPackageModel(mModel.getDistrict());
			//修复直辖市重复显示
			final String prex = (addrPack.getProvinceLable().equals(addrPack.getCityLabel()) ? "" : addrPack.getProvinceLable()) + addrPack.getCityLabel() + addrPack.getDistrictLable();
			addressView.setContent(mModel.getName() + " " + ((mModel.getMobile() != null && !mModel.getMobile().trim().equals("")) ? mModel.getMobile() : ( ( mModel.getPhone() == null || mModel.getPhone().trim().equals("") ) ? "" : mModel.getPhone() ) ), prex + mModel.getAddress());
			//保存用户最后选择的地址
			cache.set(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID, String.valueOf(mModel.getAid()), 0);
			cache.set(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID, String.valueOf(mModel.getDistrict()), 0);
		} else {
			addressView.setContent("请选择...");
			//保存用户最后选择的地址
			cache.set(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID, "0", 0);
			cache.set(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID, "0", 0);
		}
	}
	
	public void setAddress(AddressModel mAddressModel) {
		mModel = mAddressModel;
		
		renderAddress();
	}

	public boolean setAddressPackage(OrderPackage pack) {
		if (mModel == null) {
			UiUtils.makeToast(mActivity, "请填写或修改收货地址");
			return false;
		}

		// TODO: the aid in the server may not exists
		pack.put("aid", mModel.getAid());
		pack.put("receiver", mModel.getName());
		pack.put("receiveAddrId", mModel.getDistrict());
		pack.put("receiveAddrDetail", mModel.getAddress());
		pack.put("receiverTel", mModel.getPhone());
		pack.put("receiverMobile", mModel.getMobile());
		pack.put("zipCode", mModel.getZipcode());
		pack.put("sign_by_other", 1);

		return true;
	}

	public void selectAddress() {
		Bundle param = new Bundle();
		if (mModel != null) {
			param.putInt(AddressListActivity.REQUEST_ADDRESS_ID, mModel.getAid());
		}

		ToolUtil.checkLoginOrRedirect(mActivity, AddressListActivity.class, param, OrderAddressView.FLAG_REQUEST_SHIPPING_ADDRESS);
		ToolUtil.sendTrack( mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderConfirmActivity), 
				AddressListActivity.class.getName(), mActivity.getString(R.string.tag_AddressListActivity), "05011");
	}

	public void onAddressConfirm(Intent intent) {
		if (intent.getSerializableExtra(AddressListActivity.FLAG_RESULT_ADDRESS_MODEL) == null) {
			Log.e(LOG_TAG, "onAddressConfirm|getSerializableExtra|model is null");
			mModel = null;
			requestFinish();
			return;
		}

		mModel = (AddressModel) intent.getSerializableExtra(AddressListActivity.FLAG_RESULT_ADDRESS_MODEL);
		//保存用户最后选择的地址
		IPageCache cache = new IPageCache();
		cache.set(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID, String.valueOf(mModel.getAid()), 0);
		cache.set(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID, String.valueOf(mModel.getDistrict()), 0);
		requestFinish();
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		UiUtils.makeToast(mActivity, R.string.message_load_address_failed);
		requestFinish();
	}

	public void destroy() {
		super.destroy();
		if (mAddressControl != null) {
			mAddressControl.destroy();
			mAddressControl = null;
		}
	}
}
