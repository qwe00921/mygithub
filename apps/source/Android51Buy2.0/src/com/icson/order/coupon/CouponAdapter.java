package com.icson.order.coupon;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ui.ListItemView;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class CouponAdapter extends BaseAdapter {
	private BaseActivity mActivity;
	
	ArrayList<CouponModel>  models;
	boolean isRadio= false;
	
	public CouponAdapter(BaseActivity activity, ArrayList<CouponModel> models,boolean isRadio) {
		mActivity = activity;
		this.models = models;
		this.isRadio = isRadio;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View item = mActivity.getLayoutInflater().inflate(R.layout.coupon_list_item, null);
		final CouponModel couponModel = (CouponModel) getItem(position);
		item.setTag(R.id.addressmodel, couponModel);

		((TextView) item.findViewById(R.id.coupon_code_tv)).setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(couponModel.coupon_amt));
		((TextView) item.findViewById(R.id.coupon_name_tv)).setText(couponModel.content);
		((TextView) item.findViewById(R.id.coupon_date_tv)).setText(couponModel.valid_time_from+" 至 "+couponModel.valid_time_to);
		
		ListItemView pCouponView = (ListItemView) item.findViewById(R.id.coupon_list_item_container);
		
		if(couponModel.isUseNow) {
			pCouponView.setSelected(true);
		}else{
			pCouponView.setSelected(false);
		}
		
		return item;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return models.get(position);
	}

	@Override
	public int getCount() {
		return models.size();
	}

}
