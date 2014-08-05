package com.icson.my.orderdetail;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderGiftModel;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class OrderDetailTabGiftAdapter extends BaseAdapter implements
		ImageLoadListener, DestroyListener {

	private ImageLoader mAsyncImageLoader;
	private BaseActivity mActivity;
	private ArrayList<OrderGiftModel> mProductGiftModels;

	public OrderDetailTabGiftAdapter(BaseActivity activity,
			ArrayList<OrderGiftModel> models) {
		mActivity = activity;
		mProductGiftModels = models;
		mAsyncImageLoader = new ImageLoader(mActivity, false);
		
		mActivity.addDestroyListener(this);
	}

	public void setGiftModels(ArrayList<OrderGiftModel> models)
	{
		mProductGiftModels = models;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = mActivity.getLayoutInflater().inflate(
				R.layout.my_icson_tab_gift, null);

		OrderGiftModel model = (OrderGiftModel) getItem(position);

		//图片
		ImageView imageView = (ImageView) view.findViewById(R.id.item_detail_gift_image);
		String url = IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 60);
		Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			imageView.setImageBitmap(data);
		} else {
//			imageView.setImageResource(mAsyncImageLoader.getLoadingId());
			imageView.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
			mAsyncImageLoader.get(url, this);
		}

		return view;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
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
