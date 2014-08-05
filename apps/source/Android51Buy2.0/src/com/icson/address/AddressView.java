package com.icson.address;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.BaseView;
import com.icson.lib.parser.AddressParser;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class AddressView extends BaseView implements OnItemClickListener, OnSuccessListener<ArrayList<AddressModel>> {
	private static final String TAG = "AddressView";
	private static final int MSG_REFRESH_LIST = 1001;
	private static final int MSG_REMOVE_BY_OBJ = 1002;
	
	private AddressListActivity mActivity;
	private AddressControl mAddressControl;
	private ArrayList<AddressModel> mAddressModelList;
	private AddressListAdapter mAddressListAdapter;
	private ListView mListView;
	private AddressParser mParser;
	private TextView mListEmtpyView;
	private int mSelectAddressId;

	public AddressView(AddressListActivity activity) {
		mActivity = activity;
		mAddressControl = new AddressControl(mActivity);
		mListView = (ListView) mActivity.findViewById(R.id.address_list_listView);
		mListEmtpyView = (TextView) mActivity.findViewById(R.id.address_list_listView_empty);
		mParser = new AddressParser();
	}

	public void getAddressList(int nSelectAddressId) {
		mSelectAddressId = nSelectAddressId;
		mActivity.showLoadingLayer();
		mAddressModelList = new ArrayList<AddressModel>();
		mAddressListAdapter = new AddressListAdapter(mActivity, mAddressModelList, mActivity.getInitAddressId(), false);
		mListView.setAdapter(mAddressListAdapter);
		mListView.setOnItemClickListener(this);

		mAddressControl.getAddressList(mParser, this, mActivity);
	}

	@Override
	public void onSuccess(ArrayList<AddressModel> v, Response response) {
		mActivity.closeLoadingLayer();
		if( !mParser.isSuccess() ) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			return;
		}
		
		Message msg = mUIHandler.obtainMessage(MSG_REFRESH_LIST, v);
		mUIHandler.sendMessage(msg);
		
	}

	// remove click
	public void deleteAddress(final AddressModel mAddressModel) {
		mActivity.showProgressLayer();
		mAddressControl.remove(mAddressModel.getAid(), new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					Message msg = mUIHandler.obtainMessage(MSG_REMOVE_BY_OBJ, mAddressModel);
					mUIHandler.sendMessage(msg);
				} else {
					UiUtils.makeToast(mActivity, v.optString("data").equals("") ? Config.NORMAL_ERROR : v.optString("data"));
				}
			}
		}, mActivity);
	}
	
	public AddressModel onDeleteAddressFinish(final AddressModel pDelAddressModel, int nSelectedId){
		AddressModel pDefaultModel = null;
		if(mAddressModelList != null && null != pDelAddressModel) {
			int nSize = (null == mAddressModelList) ? 0 : mAddressModelList.size();
			int nDelId = pDelAddressModel.getAid();
			for(int nIndex=0; nIndex < nSize; nIndex++){
				AddressModel model = mAddressModelList.get(nIndex);
				if((null != model) && (nDelId == model.getAid())){
					mAddressModelList.remove(nIndex);
					nSize -- ;
					
					if(nDelId == nSelectedId && nSize > 0) {
						pDefaultModel = mAddressModelList.get(0);
						mAddressListAdapter.setSelectAddress(pDefaultModel.getAid());
					}else if (nDelId == nSelectedId && 0 == nSize) {
						pDefaultModel = null;
					}else{
						for(int i = 0; i < mAddressModelList.size(); i ++ ) {
							AddressModel pModel = mAddressModelList.get(i);
							if(nSelectedId == pModel.getAid()) {
								pDefaultModel = pModel;
								break;
							}
						}
					}
					
					onOperationFinish();
					break;
				}
			}
		}
		
		return pDefaultModel;
	}

	public void destroy() {
		mActivity = null;
		mAddressControl.destroy();
		mAddressControl = null;
		mAddressModelList = null;
		mAddressListAdapter = null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (position > mAddressModelList.size() - 1) {
			return;
		}
		mAddressListAdapter.setClickState(mAddressModelList.get(position).getAid());
		Intent intent = new Intent();
		intent.putExtra(AddressListActivity.FLAG_RESULT_ADDRESS_MODEL, mAddressModelList.get(position));
		mActivity.setResult(AddressListActivity.FLAG_RESULT_ADDRESS_SAVE, intent);
		mActivity.finish();
		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_AddressListActivity), AddressListActivity.class.getName(), mActivity.getString(R.string.tag_AddressListActivity), "0100" + position);
	}
	
	/**
	 * To refresh list view with the given data modelList
	 * This method should be called in UI thread to avoid race condition
	 * If call from non UI thread, please use mUIHandler to send MSG_REFRESH_LIST message instead.
	 * @param modelList
	 */
	private void refreshList(ArrayList<AddressModel> modelList) {
		if(null == mAddressModelList) {
			Log.w(TAG, "[refreshList], mAddressModelList is null");
			return;
		}
		
		mAddressModelList.clear();
		mAddressModelList.addAll(modelList);
		if(0 != mSelectAddressId) {
			int nSize = modelList.size();
			for(int nId = 0; nId < nSize; nId++ ) {
				AddressModel model = modelList.get(nId);
				if(mSelectAddressId == model.getAid()) {
					if(null != mListView)
						mListView.setSelection(nId);
					break;
				}
			}
			 
		}
		onOperationFinish();
	}
	
	/**
	 * To remove the given object in list view
	 * This method should be called in UI thread to avoid race condition
	 * If call from non UI thread, please use mUIHandler to send MSG_REMOVE_BY_OBJ message instead.
	 * @param modelList
	 */
	private void deleteListItem(AddressModel mAddressModel) {
		if(mAddressModelList == null) {
			Log.w(TAG, "[refreshList], mAddressModelList is null");
			return;
		}
		mAddressModelList.remove(mAddressModel);
		onOperationFinish();
	}
	
	private void onOperationFinish() {
		if(null == mAddressModelList ||  0 == mAddressModelList.size()) {
			mAddressListAdapter.notifyDataSetChanged();
			mListEmtpyView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			return;
		}
		
		if(mAddressListAdapter == null) {
			return;
		}
		
		mListEmtpyView.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		mAddressListAdapter.notifyDataSetChanged();
	}

	private Handler mUIHandler = new UIHandler(this);
	private static class UIHandler extends Handler {
		private final WeakReference<AddressView> mRef;
		public UIHandler(AddressView activity) {
			mRef = new WeakReference<AddressView>(activity);
		}
		
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			AddressView parent = mRef.get();
			if(parent == null) {
				return;
			}

			int msgCode = msg.what;
			switch (msgCode) {
			case MSG_REFRESH_LIST: {
				@SuppressWarnings("unchecked")
				ArrayList<AddressModel> modelList = (ArrayList<AddressModel>) msg.obj;
				parent.refreshList(modelList);
				break;
			}
			case MSG_REMOVE_BY_OBJ: {
				AddressModel mAddressModel = (AddressModel) msg.obj;
				parent.deleteListItem(mAddressModel);
				break;
			}
			
			default:
				break;
			}
		};
	}
}
