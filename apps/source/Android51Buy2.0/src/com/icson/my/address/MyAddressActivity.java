package com.icson.my.address;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.address.AddressControl;
import com.icson.address.AddressDetailActivity;
import com.icson.address.AddressListAdapter;
import com.icson.address.AddressModel;
import com.icson.home.FullDistrictModel;
import com.icson.home.FullDistrictParser;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.parser.AddressParser;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyAddressActivity extends BaseActivity implements OnSuccessListener<ArrayList<AddressModel>>, OnItemClickListener {
	private static final String LOG_TAG = MyAddressActivity.class.getName();
	public static final int FLAG_REQUEST_ADDRESS_ADD = 1;
	public static final int FLAG_REQUEST_ADDRESS_MODIFY = 2;
	private boolean firstExec = true;
	private AddressControl mAddressControl;
	private AddressListAdapter mAddressListAdapter;
	private ArrayList<AddressModel> mAddressModels;
	private ListView mListView;
	private TextView mListEmptyView;
	private AddressParser mAddressParser;
	private Ajax mAjax;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_addresslist);
		this.loadNavBar(R.id.my_address_list_navigation_bar);
		
		initUi();
		IShippingArea.getAreaModels();
		initAreaInfo();
		init();
	}

	@Override
	protected void onDestroy() {
		
		
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		mAddressControl = null;
		mAddressListAdapter = null;
		mAddressModels = null;
		mListView = null;
		
		IShippingArea.clean();
		super.onDestroy();
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

	private void initUi() {
		mAddressParser = new AddressParser();
		mAddressControl = new AddressControl(this);
		mAddressModels = new ArrayList<AddressModel>();
		mAddressListAdapter = new AddressListAdapter(this, mAddressModels, true);
		
		mListView = (ListView) findViewById(R.id.my_address_listview);
		mListView.setAdapter(mAddressListAdapter);
		mListView.setOnItemClickListener(this);
		
		mListEmptyView = (TextView) findViewById(R.id.my_address_listview_empty);
		
		loadNavBar(R.id.navigation_bar);
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
					ToolUtil.checkLoginOrRedirect(MyAddressActivity.this, AddressDetailActivity.class, null, FLAG_REQUEST_ADDRESS_ADD);
					ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MyAddressActivity), AddressDetailActivity.class.getName(), getString(R.string.tag_AddressDetailActivity), "02010");
			}
			
		});
	}

	public void init() {
		if (!firstExec) {
			return;
		}

		firstExec = false;
		mAddressModels.clear();
		sendRequest();
	}

	private void sendRequest() {
		//((ImageButton) findViewById(R.id.global_loading_bg)).setBackgroundColor(getResources().getColor(R.id.global_loading_bg));
		Ajax ajax = mAddressControl.getAddressList(mAddressParser, this, this);
		if( null != ajax )
		{
			setLoadingSwitcher(ajax.getId(), findViewById(R.id.my_address_listview), findViewById(R.id.global_loading));
			showLoadingLayer(ajax.getId());
		}
	}

	private void requestFinish() {
		if(null == mAddressModels || 0 == mAddressModels.size()) {
			mAddressListAdapter.notifyDataSetChanged();
			mListEmptyView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			return;
		}
		
		mListEmptyView.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		mAddressListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSuccess(ArrayList<AddressModel> v, Response response) {
		closeLoadingLayer(response.getId());
		
		if( !mAddressParser.isSuccess() ) {
			UiUtils.makeToast(this, TextUtils.isEmpty(mAddressParser.getErrMsg()) ? Config.NORMAL_ERROR: mAddressParser.getErrMsg());
			return;
		}
		
		mAddressModels.clear();
		if (v != null) {
			mAddressModels.addAll(v);
		}
		requestFinish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(AddressDetailActivity.REQUEST_ADDRESS_MODEL, mAddressModels.get(position));
		ToolUtil.startActivity(this, AddressDetailActivity.class, bundle, FLAG_REQUEST_ADDRESS_MODIFY);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MyAddressActivity), AddressDetailActivity.class.getName(), getString(R.string.tag_AddressDetailActivity), "0202" + position);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode){
		case FLAG_REQUEST_ADDRESS_MODIFY:
			if (resultCode == AddressDetailActivity.FLAG_RESULT_SAVED){
				AddressModel pAddressModel = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL);
				AddressModel curAddressModel = new AddressModel();
				if( null != pAddressModel){
					int nSize = mAddressModels.size();
					for(int nId=0; nId<nSize; nId++){
						curAddressModel = mAddressModels.get(nId);
						if( pAddressModel.getAid() == curAddressModel.getAid() ){
							mAddressModels.remove(nId);
							mAddressModels.add(pAddressModel);
							break;
						}
					}
				}
				
			}else if(resultCode == AddressDetailActivity.FLAG_RESULT_DELETED) {
				AddressModel pAddressModel = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL);
				AddressModel curAddressModel = new AddressModel();
				if( null != pAddressModel){
					int nSize = mAddressModels.size();
					for(int nId=0; nId<nSize; nId++){
						curAddressModel = mAddressModels.get(nId);
						if( pAddressModel.getAid() == curAddressModel.getAid() ){
							mAddressModels.remove(nId);
							break;
						}
					}
				}
			}
			
			requestFinish();
			break;
		case FLAG_REQUEST_ADDRESS_ADD:
			if (resultCode == AddressDetailActivity.FLAG_RESULT_SAVED){
				AddressModel pAddressModel = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL);
				pAddressModel = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL);
				if (resultCode == AddressDetailActivity.FLAG_RESULT_SAVED){
					if( null != pAddressModel){
						mAddressModels.add(pAddressModel);
					}
				}
				
				requestFinish();
			}
			break;
		}
	}

	// remove click
	public void remove(final int position) {
		AddressModel model = (AddressModel) mAddressListAdapter.getItem(position);

		showProgressLayer();
		mAddressControl.remove(model.getAid(), new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					mAddressModels.remove(position);
					mAddressListAdapter.notifyDataSetChanged();
				} else {
					UiUtils.makeToast(MyAddressActivity.this, v.optString("data").equals("") ? Config.NORMAL_ERROR : v.optString("data"));
				}
			}
		}, this);
	}

	public void afterEdit(Intent intent) {
		if (intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL) == null) {
			Log.e(LOG_TAG, "afterEdit|addressModel is null.");
			return;
		}

		final AddressModel model = (AddressModel) intent.getSerializableExtra(AddressDetailActivity.RESPONSE_ADDRESS_MODEL);

		boolean found = false;
		for(int i = 0, len = mAddressModels.size(); i < len; i++){
			AddressModel mAddressModel = mAddressModels.get(i);
			if (mAddressModel.getAid() == model.getAid()) {
				mAddressModels.remove(i);
				mAddressModels.add(i, model);
				found = true;
				break;
			}
		}
		
		if(!found){
			mAddressModels.add(model);	
		}
		
		mAddressListAdapter.notifyDataSetChanged();
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MyAddressActivity);
	}
}
