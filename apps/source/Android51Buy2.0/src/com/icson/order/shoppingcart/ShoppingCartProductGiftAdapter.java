package com.icson.order.shoppingcart;

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
import com.icson.lib.model.ShoppingCartGiftModel;
import com.icson.shoppingcart.ProductCouponGiftModel.CouponGiftModel;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ShoppingCartProductGiftAdapter extends BaseAdapter implements
		ImageLoadListener, DestroyListener {

	private ImageLoader mAsyncImageLoader;
	private BaseActivity mActivity;
	private ArrayList<BaseModel> mProductGiftModels;

	public ShoppingCartProductGiftAdapter(BaseActivity activity, ArrayList<BaseModel> models) {
		mActivity = activity;
		mProductGiftModels = models;
		mAsyncImageLoader = new ImageLoader(mActivity, false);
		
		mActivity.addDestroyListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BaseModel baseModel = (BaseModel) getItem(position);
		if( baseModel instanceof ShoppingCartGiftModel) {
			View view = mActivity.getLayoutInflater().inflate(
					R.layout.my_icson_tab_gift, null);
	
			ShoppingCartGiftModel model = (ShoppingCartGiftModel) baseModel;
	
			//图片
			ImageView imageView = (ImageView) view.findViewById(R.id.item_detail_gift_image);
			String url = IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 60);
			Bitmap data = mAsyncImageLoader.get(url);
			if (data != null) {
				imageView.setImageBitmap(data);
			} else {
//				imageView.setImageResource(mAsyncImageLoader.getLoadingId());
				imageView.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
				mAsyncImageLoader.get(url, this);
			}
	
			return view;
		}else if( baseModel instanceof CouponGiftModel) {
			//单品赠券
			View view = mActivity.getLayoutInflater().inflate(R.layout.my_icson_tab_coupon_gift, null);
			CouponGiftModel model = (CouponGiftModel) baseModel;

			TextView picAmt = (TextView) view.findViewById(R.id.order_detail_coupon_gift_pic_amt);
			picAmt.setText(String.valueOf(model.getCouponAmt()/100));

			return view;
		}else{
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
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
		if(null!=mAsyncImageLoader)
		{
			mAsyncImageLoader.cleanup();
			mAsyncImageLoader = null;
		}
		
	}

}
