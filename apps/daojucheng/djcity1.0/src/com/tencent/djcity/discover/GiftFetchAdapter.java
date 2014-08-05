package com.tencent.djcity.discover;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.model.GiftModel;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageHelper;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.activity.BaseActivity.DestroyListener;

public class GiftFetchAdapter extends BaseAdapter implements DestroyListener,
ImageLoadListener, OnClickListener{

	private BaseActivity mActivity;
	private LayoutInflater mInflater;
	private ArrayList<GiftModel> mData;
	private ImageLoader mImageLoader;

	public GiftFetchAdapter(BaseActivity aActivity)
	{
		mActivity = aActivity;
		mInflater = mActivity.getLayoutInflater();
		mImageLoader = new ImageLoader(mActivity, Config.MY_GAME_DIR, true);
		mActivity.addDestroyListener(this);
	}
	
	public void setData(ArrayList<GiftModel> adata)
	{
		mData = adata;
	}
	@Override
	public int getCount() {
		return (null == mData) ? 0 : mData.size();
	}

	@Override
	public Object getItem(int position) {
		return (null == mData) ? null : mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		giftHolder holder = null;
		
		if (convertView == null) {
			convertView =  mInflater.inflate(R.layout.listitem_giftfetch, null);
			holder = new giftHolder();
			holder.imgV = ((ImageView) convertView.findViewById(R.id.image_view));
			holder.nameV = ((TextView) convertView.findViewById(R.id.gift_name));
			holder.timeV = ((TextView) convertView.findViewById(R.id.gift_time));
			holder.fetchBtn = ((TextView) convertView.findViewById(R.id.fetch_btn));
			
			convertView.setTag(holder);	
		} else {
			holder = (giftHolder) convertView.getTag();
		}

		GiftModel item  = this.mData.get(position);
		holder.nameV.setText(item.getName());
		holder.timeV.setText(item.getTime());
		
		if(item.isNew())
		{
			holder.fetchBtn.setTextColor(mActivity.getResources().getColor(R.color.red));
			holder.fetchBtn.setText(R.string.fetch);
			holder.fetchBtn.setBackgroundResource(R.drawable.button_red_frame_round);
			holder.fetchBtn.setOnClickListener(this);
			holder.fetchBtn.setTag(position);
			
		}
		else
		{
			holder.fetchBtn.setTextColor(mActivity.getResources().getColor(R.color.global_gray));
			holder.fetchBtn.setText(R.string.already_fetched);
			holder.fetchBtn.setBackgroundResource(R.drawable.button_gray_round_frame_shape);
			holder.fetchBtn.setOnClickListener(null);
			
		}
		
		String url = item.getPicUrl();
		Bitmap data = mImageLoader.get(url);
		holder.imgV.setImageBitmap(data != null ? data : 
			ImageHelper.getResBitmap(mActivity, mImageLoader.getLoadingId()));
		if (data == null) {
			mImageLoader.get(url, this);
		}
		return convertView;
	}

	private class giftHolder
	{
		ImageView imgV;
		TextView  nameV;
		TextView  timeV;
		TextView  fetchBtn;
	}

	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		this.notifyDataSetChanged();
		
	}

	@Override
	public void onError(String strUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		if(null != mImageLoader)
			mImageLoader.cleanup();
		mImageLoader = null;
	}

	@Override
	public void onClick(View v) {
		Integer poi = (Integer) v.getTag();
		UiUtils.makeToast(mActivity, "GO fetch No " + poi);// TODO Auto-generated method stub
		
	}
	
	

}
