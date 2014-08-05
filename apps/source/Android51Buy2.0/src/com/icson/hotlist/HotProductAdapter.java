/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: HotProductAdapter.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-7-19
 */
package com.icson.hotlist;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.hotlist.HotlistModel.HotProductModel;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.ui.AlphaTextView;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

/**  
 *   
 * Class Name:HotProductAdapter 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-7-19 上午10:52:59 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class HotProductAdapter extends BaseAdapter implements DestroyListener,ImageLoadListener {

	private ArrayList<HotProductModel> mData;
	private BaseActivity mActivity;
	private LayoutInflater mInflater;
	private ImageLoader mImageLoader;
	
	public HotProductAdapter(BaseActivity aActivity)
	{
		mActivity = aActivity;
		mInflater = LayoutInflater.from(mActivity);
		mImageLoader = new ImageLoader(mActivity, Config.CHANNEL_PIC_DIR, true);
		
		mActivity.addDestroyListener(this);
	}
	
	public void setModelArray(ArrayList<HotProductModel> aArray)
	{
		mData = aArray;
	}
	@Override
	public int getCount() {
		return (null == mData ? 0 : mData.size());
		
	}

	@Override
	public void onDestroy() {
		if( null != mImageLoader )
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}
	/*  
	 * Description:
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return (null == mData ? null : mData.get(position));
	}

	/*  
	 * Description:
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/*  
	 * Description:
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		hotProViewHolder holder = null;
		int type = getItemViewType(position);
		HotProductModel item = (HotProductModel) getItem(position);
		if (convertView == null) {
			holder = new hotProViewHolder();
			if(type==0)
			{
				convertView = mInflater.inflate(R.layout.list_item_hot_first, null);
				holder.detailV = (TextView) convertView.findViewById(R.id.pro_detail);
				holder.priceV = (AlphaTextView) convertView.findViewById(R.id.pro_price);
				holder.buynumV = (TextView) convertView.findViewById(R.id.pro_buynum);
			
				holder.imgV = (ImageView) convertView.findViewById(R.id.pro_img);
			}
			else
			{	convertView = mInflater.inflate(R.layout.list_item_hotlist, null);
				holder.sortedId = (AlphaTextView) convertView.findViewById(R.id.sort_num);
				holder.detailV = (TextView) convertView.findViewById(R.id.pro_detail);
				holder.priceV = (AlphaTextView) convertView.findViewById(R.id.pro_price);
				holder.buynumV = (TextView) convertView.findViewById(R.id.pro_buynum);
			
				holder.imgV = (ImageView) convertView.findViewById(R.id.pro_img);
			}
			
			convertView.setTag(holder);
		} else {
			holder = (hotProViewHolder) convertView.getTag();
		}
		
		if(position!=0)
			holder.sortedId.setText("NO." + (position+1));
		holder.detailV.setText(item.getPromoName());
		holder.priceV.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPriceInterger(item.getShowPrice()));
		holder.buynumV.setText(mActivity.getString(R.string.hot_sale, item.getBuyNum()));
		loadImage(holder.imgV, item.getProductCharId());
		return convertView;
	}
	
	@Override
	public int getViewTypeCount() {
	       return 2;
	}

	@Override
	public int getItemViewType(int position) {
	// 根据position元素返回View的类型, type值是从0开始排序的
		if(position == 0)
			return 0;
		else
			return 1;
	}

	
	
	private void loadImage(ImageView view, String productCharId) {
		String url = IcsonProImgHelper.getAdapterPicUrl(productCharId, 200);
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
		view.setImageBitmap(mImageLoader.getLoadingBitmap(mActivity));
//		view.setImageResource(mImageLoader.getLoadingId());
		mImageLoader.get(url, this);
	}

	private class hotProViewHolder
	{
		AlphaTextView sortedId;
		TextView detailV;
		AlphaTextView priceV;
		TextView buynumV;
		ImageView imgV;
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onLoaded(android.graphics.Bitmap, java.lang.String)
	 */
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		notifyDataSetChanged();
		
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onError(java.lang.String)
	 */
	@Override
	public void onError(String strUrl) {
		// TODO Auto-generated method stub
		
	}
}
