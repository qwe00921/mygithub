package com.icson.item;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ProductGiftModel;
import com.icson.shoppingcart.ProductCouponGiftModel.CouponGiftModel;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ItemTabDetailGiftAdapter extends BaseAdapter implements DestroyListener,
		ImageLoadListener {

	private ImageLoader mAsyncImageLoader;
	private BaseActivity mActivity;
	private ArrayList<BaseModel> mProductGiftModels;

	public ItemTabDetailGiftAdapter(BaseActivity activity,
			ArrayList<BaseModel> models) {
		mActivity = activity;
		mProductGiftModels = models;
		mAsyncImageLoader = new ImageLoader(mActivity, false);
		mActivity.addDestroyListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getCount() == 1) {
			BaseModel baseModel = (BaseModel) getItem(position);
			//赠品
			if(baseModel instanceof ProductGiftModel) {
				View view = mActivity.getLayoutInflater().inflate(
						R.layout.item_tab_gift_one, null);
				
				ProductGiftModel model = (ProductGiftModel) baseModel;
	
				// 图片
				ImageView imageView = (ImageView) view
						.findViewById(R.id.item_detail_gift_image);
				String url = IcsonProImgHelper.getAdapterPicUrl(
						model.getProductCharId(), 60);
				Bitmap data = mAsyncImageLoader.get(url);
				if (data != null) {
					imageView.setImageBitmap(data);
				} else {
//					imageView.setImageResource(mAsyncImageLoader.getLoadingId());
					imageView.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
					mAsyncImageLoader.get(url, this);
				}
				TextView name = (TextView) view
						.findViewById(R.id.item_detail_gift_name);
				TextView count = (TextView) view
						.findViewById(R.id.item_detail_gift_count);
	
				name.setText(model.getName());
				count.setText("共 "+model.getNum()+" 件");
				return view;
			}else if( baseModel instanceof CouponGiftModel) {
				//单品赠券
				View view = mActivity.getLayoutInflater().inflate(R.layout.item_tab_coupon_gift_one, null);
				CouponGiftModel model = (CouponGiftModel) baseModel;

				TextView picAmt = (TextView) view.findViewById(R.id.item_detail_coupon_gift_pic_amt);
				TextView name = (TextView) view.findViewById(R.id.item_detail_coupon_gift_name);
				TextView amt = (TextView) view.findViewById(R.id.item_detail_coupon_gift_amt);

				picAmt.setText(String.valueOf(model.getCouponAmt()/100));
				name.setText(model.getCouponName());
				amt.setText(mActivity.getString(R.string.rmb) + String.valueOf(model.getCouponAmt()/100));
				return view;
			}else{
				return null;
			}
		} else {
			
			BaseModel baseModel = (BaseModel) getItem(position);
			//赠品
			if(baseModel instanceof ProductGiftModel) {
				View view = mActivity.getLayoutInflater().inflate(
						R.layout.item_tab_gift, null);
				ProductGiftModel model = (ProductGiftModel) baseModel;
	
				// 图片
				ImageView imageView = (ImageView) view
						.findViewById(R.id.item_detail_gift_image);
				String url = IcsonProImgHelper.getAdapterPicUrl(
						model.getProductCharId(), 60);
				Bitmap data = mAsyncImageLoader.get(url);
				if (data != null) {
					imageView.setImageBitmap(data);
				} else {
//					imageView.setImageResource(mAsyncImageLoader.getLoadingId());
					imageView.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
					mAsyncImageLoader.get(url, this);
				}
	
				return view;
			} else if(baseModel instanceof CouponGiftModel) {
				//单品赠券
				View view = mActivity.getLayoutInflater().inflate(R.layout.item_tab_coupon_gift, null);
				CouponGiftModel model = (CouponGiftModel) baseModel;

				TextView picAmt = (TextView) view.findViewById(R.id.item_detail_coupon_gift_pic_amt);
				picAmt.setText(String.valueOf(model.getCouponAmt()/100));

				return view;
			}else{
				return null;
			}
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return mProductGiftModels.get(position);
	}

	@Override
	public int getCount() {
		return mProductGiftModels.size();
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}

	@Override
	public void onError(String strUrl) {
	}

	@Override
	public void onDestroy() {
		if( null != mAsyncImageLoader )
		{
			mAsyncImageLoader.cleanup();
			mAsyncImageLoader = null;
		}
	}
}
