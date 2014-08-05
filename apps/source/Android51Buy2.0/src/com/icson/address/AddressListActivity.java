package com.icson.address;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.icson.R;
import com.icson.home.FullDistrictModel;
import com.icson.home.FullDistrictParser;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class AddressListActivity extends BaseActivity {
	public static final String REQUEST_ADDRESS_ID = "address_id";
	public static final int FLAG_REQUEST_ADDRESS_ADD = 1;
	public static final int FLAG_REQUEST_ADDRESS_MODIFY = 2;
	public static final String FLAG_RESULT_ADDRESS_MODEL = "address_model";
	public static final int FLAG_RESULT_ADDRESS_SAVE = 3;
	public static final int FLAG_RESULT_ADDRESS_DELETED = 4;


	private int mInitAddressId;
	private AddressView mAddressView;
	private Ajax mAjax;


	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_list_address);

		mAddressView = new AddressView(this);
		mInitAddressId = getIntent().getIntExtra(REQUEST_ADDRESS_ID, 0);
		
		this.loadNavBar(R.id.address_list_navigation_bar);
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ToolUtil.checkLoginOrRedirect(AddressListActivity.this, AddressDetailActivity.class, null, AddressListActivity.FLAG_REQUEST_ADDRESS_ADD);
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_AddressListActivity), AddressDetailActivity.class.getName(), getString(R.string.tag_AddressDetailActivity), "01001");
			}
		});

		IShippingArea.getAreaModels();
		initAreaInfo();
		mAddressView.getAddressList(mInitAddressId);
	}

	public int getInitAddressId() {
		return mInitAddressId;
	}
	
	/*
	 * 拉取三级地址信息
	 */
	private void initAreaInfo(){
		IPageCache cache = new IPageCache();
		String strMD5 = cache.get(CacheKeyFactory.CACHE_FULL_DISTRICT_MD5);
		
		mAjax = ServiceConfig.getAjax(Config.URL_FULL_DISTRICT);
		if( null == mAjax )
			return ;
		
		final FullDistrictParser mFullDistrictParser = new FullDistrictParser();
		if(!TextUtils.isEmpty(strMD5)) {
			mAjax.setData("fileMD5", strMD5);
		}
		
		mAjax.setParser(mFullDistrictParser);
		mAjax.setOnSuccessListener(new OnSuccessListener<FullDistrictModel>(){
			@Override
			public void onSuccess(FullDistrictModel v, Response response) {
				if(mFullDistrictParser.isSuccess()) {
					IPageCache cache = new IPageCache();
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT, mFullDistrictParser.getData(), 0);
					cache.set(CacheKeyFactory.CACHE_FULL_DISTRICT_MD5, v.getMD5Value(), 0);
					IShippingArea.setAreaModel(v.getProvinceModels());
				}
			}
		});
		
		addAjax(mAjax);
		mAjax.send();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case FLAG_REQUEST_ADDRESS_ADD:
		case FLAG_REQUEST_ADDRESS_MODIFY:
			AddressModel pAddressModel = null != data ? (AddressModel)data.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL) : null;
			if (resultCode == AddressDetailActivity.FLAG_RESULT_SAVED) {
				if (pAddressModel != null) {
					onOperationFinish(pAddressModel, FLAG_RESULT_ADDRESS_SAVE);
					finish();
				}
			}else if(resultCode == AddressDetailActivity.FLAG_RESULT_DELETED){
				if(mAddressView == null) {
					mAddressView = new AddressView(this);
				}
				
				AddressModel model = mAddressView.onDeleteAddressFinish(pAddressModel, mInitAddressId);
				onOperationFinish(model, FLAG_RESULT_ADDRESS_DELETED);
			}
			break;
		}
	}
	
	private void onOperationFinish(AddressModel pAddressModel, int resultcode) {
		Intent intent = getIntent();
		intent.putExtra(FLAG_RESULT_ADDRESS_MODEL, pAddressModel);
		setResult(resultcode, intent);
	}

	@Override
	protected void onDestroy() {
		IShippingArea.clean();
		super.onDestroy();
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_AddressListActivity);
	}
}
