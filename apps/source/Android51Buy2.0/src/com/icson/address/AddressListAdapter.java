package com.icson.address;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.AreaPackageModel;
import com.icson.lib.ui.ListItemView;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class AddressListAdapter extends BaseAdapter {
	private BaseActivity mActivity;
	private ArrayList<AddressModel> mAddressModels;
	private int mAddressId;
	private boolean isFromMyAddress;
	private LayoutInflater mInflater;
	
	public AddressListAdapter(BaseActivity activity, ArrayList<AddressModel> mAddressModels, int addressId, boolean isFromMyAddress) {
		mActivity = activity;
		this.mAddressModels = mAddressModels;
		mAddressId = addressId;
		this.isFromMyAddress = isFromMyAddress;
		mInflater = mActivity.getLayoutInflater();
	}
	
	public AddressListAdapter(BaseActivity activity, ArrayList<AddressModel> mAddressModels, boolean isFromMyAddress) {
		mActivity = activity;
		this.mAddressModels = mAddressModels;
		this.isFromMyAddress = isFromMyAddress;
		mAddressId = 0;
		mInflater = mActivity.getLayoutInflater();
	}
	
	public void setSelectAddress(int pAddressId) {
		this.mAddressId = pAddressId;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ItemHolder holder = null;
		
		if (convertView == null) {
			convertView =  mInflater.inflate(R.layout.address_list_item, null);
			holder = new ItemHolder();
			holder.mItemView =(ListItemView) convertView.findViewById(R.id.address_list_item_content);
			holder.mPersonName = ((TextView) convertView.findViewById(R.id.textview_name));
			holder.mPhoneNum = ((TextView) convertView.findViewById(R.id.textview_phone));
			holder.mAddressDetail = (TextView) convertView.findViewById(R.id.textview_address);
			holder.mEditButton = (ImageView) convertView.findViewById(R.id.address_item_edit);
			convertView.setTag(holder);	
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		final AddressModel addressModel = (AddressModel) getItem(position);

		holder.mPersonName.setText(addressModel.getName());

		String contact = (addressModel.getMobile() == null || addressModel.getMobile().equals("")) ? addressModel.getPhone() :  addressModel.getMobile() ;
		contact = contact == null ? "" : contact;
		holder.mPhoneNum.setText(contact);
		
		AreaPackageModel  addrPack = new AreaPackageModel(addressModel.getDistrict());
		String prex = addrPack.getProvinceLable() + ( addrPack.getCityLabel().equals(addrPack.getProvinceLable() ) ? "" : addrPack.getCityLabel() ) + addrPack.getDistrictLable();
		holder.mAddressDetail.setText(prex + addressModel.getAddress());
		
		holder.mEditButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putSerializable(AddressDetailActivity.REQUEST_ADDRESS_MODEL, addressModel);
				bundle.putBoolean(AddressDetailActivity.REQUEST_FROM_MYADDRESS, isFromMyAddress);
				ToolUtil.checkLoginOrRedirect(mActivity, AddressDetailActivity.class, bundle, AddressListActivity.FLAG_REQUEST_ADDRESS_MODIFY);
			}
		});
		
		if ((mAddressId != 0) && (mAddressId == addressModel.getAid())) {
			holder.mItemView.setSelected(true);
		}else{
			holder.mItemView.setSelected(false);
		}
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return (null == mAddressModels) ? null : mAddressModels.get(position);
	}

	@Override
	public int getCount() {
		return (null == mAddressModels) ? 0 : mAddressModels.size();
	}
	
	public void setClickState(int nAddressId) {
		mAddressId = nAddressId;
		this.notifyDataSetChanged();
	}
	
	private static class ItemHolder {
		TextView mPersonName;
		TextView mPhoneNum;
		TextView mAddressDetail;
		ImageView mEditButton;
		ListItemView mItemView;
	}
}
