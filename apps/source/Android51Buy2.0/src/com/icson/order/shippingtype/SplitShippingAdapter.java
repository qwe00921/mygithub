/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: CombineShippingAdapter.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-8-14
 */
package com.icson.order.shippingtype;

import java.util.ArrayList;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.TextField;
import com.icson.order.shoppingcart.SubOrderModel;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

/**  
 *   
 * Class SplitShippingAdapter 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-8-14 下午02:18:32 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class SplitShippingAdapter extends BaseAdapter{
	
	private ArrayList<SubOrderModel> mSubOrderModelList;
	private ArrayList<ShippingTypeTimeModel> mSelectSplit;
	private CombineTimeAvaiableView CombineTV;
	private BaseActivity mActivity;
	private LayoutInflater mInflater;
	/**  
	* Create a new Instance SplitShippingAdapter.  
	*  
	* @param subShippingTypeModelList  
	*/
	public SplitShippingAdapter(BaseActivity aActivity,CombineTimeAvaiableView aView,
			ArrayList<SubOrderModel> subOrderModelList,
			ArrayList<ShippingTypeTimeModel> SplitSelectModel) {
		mActivity = aActivity;
		CombineTV = aView;
		mSelectSplit = SplitSelectModel;
		mSubOrderModelList = subOrderModelList;
		mInflater = LayoutInflater.from(mActivity);
	}

	public void resetSelectAndWholeModel(ArrayList<SubOrderModel> subOrderModelList)
	{
		mSubOrderModelList = subOrderModelList;
	}
	
	
	@Override
	public int getCount() {
		return (null == mSubOrderModelList ? 0 : mSubOrderModelList.size());
	}

	@Override
	public Object getItem(int position) {
		return (null == mSubOrderModelList ? null : mSubOrderModelList.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_split_shipping_time, null);
			holder = new  ItemHolder();
			holder.shipTime = (EditField) convertView.findViewById(R.id.orderconfirm_ship_time);
			holder.shipTime.setTagAtDrawableRight(position);
			
			holder.shippingType = (TextField) convertView.findViewById(R.id.orderconfirm_ship_type);
			
			holder.packageTitle = (TextView) convertView.findViewById(R.id.orderconfirm_ship_title);
			
			holder.shipTime.setOnDrawableRightClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v == null)
						return;
					if(v.getTag() == null)
						return;
					int pos = (Integer) v.getTag();
					CombineTV.selectSubShippingSpan(pos);
				}
			});
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ItemHolder) convertView.getTag();
		}
		
		//ArrayList<ShippingTypeTimeModel> availTimeList = (ArrayList<ShippingTypeTimeModel>)getItem(position);
		ShippingTypeTimeModel mModel = mSelectSplit.get(position);
		holder.shipTime.setContent(mModel == null ? "" : CombineTimeAvaiableView.getTimeLabel(mModel, false));
		
		holder.shipTime.setCaption(mActivity.getResources().getString(R.string.orderconfirm_ship_time_title));
		
		double shippingPrice = mSubOrderModelList.get(position).shippingPrice;
		String str = 
				mSubOrderModelList.get(position).shipTypeName + 
				"("
				+ (shippingPrice == 0.0 ? "免运费" : ("运费 <font color=\"red\">&yen;" + ToolUtil.toPrice(shippingPrice, 2) + "</font>")) + ")";;
		holder.shippingType.setContent(Html.fromHtml(str));
		
		if(getCount() > 1) {
			String title = "     "+mActivity.getResources().getString(R.string.package_no_x,position+1);
			holder.packageTitle.setText(title);
			holder.packageTitle.setVisibility(View.VISIBLE);
		} else {
			holder.packageTitle.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	
	private static class ItemHolder {
		TextView packageTitle;
		TextField shippingType;
		EditField shipTime;
	}
}
