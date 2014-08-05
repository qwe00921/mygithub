package com.icson.my.orderdetail;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderProductModel;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class OrderDetailProductSingleAdapter extends BaseAdapter implements ImageLoadListener, DestroyListener {

	private BaseActivity mActivity;

	private LayoutInflater mLayoutInflater;

	private ImageLoader mAsyncImageLoader;

	private ArrayList<OrderProductModel> mOrderProductModels;

	public OrderDetailProductSingleAdapter(BaseActivity activity, ArrayList<OrderProductModel> models) {
		mActivity = activity;
		mOrderProductModels = models;
		mLayoutInflater = activity.getLayoutInflater();
		mAsyncImageLoader = new ImageLoader(mActivity, true);
		
		mActivity.addDestroyListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup item = (ViewGroup) mLayoutInflater.inflate(R.layout.my_orderdetail_product_single_item, null);
		OrderProductModel model = (OrderProductModel) getItem(position);
		loadImage((ImageView) item.findViewById(R.id.order_imageview_pic), IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 80));
		((TextView) item.findViewById(R.id.order_textview_name)).setText(model.getNameNoHTML());
		((TextView) item.findViewById(R.id.order_textview_price)).setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));
		String str = "数量: " + model.getBuyCount();
/*		if (model.getGiftCount() > 0) {
			str += "   赠品: " + model.getGiftCount();
		}*/
		((TextView) item.findViewById(R.id.order_textview_count)).setText(str);
		return item;
	}

	@Override
	public long getItemId(int position) {
		return mOrderProductModels.get(position).getProductId();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mOrderProductModels.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mOrderProductModels.size();
	}

	private void loadImage(ImageView view, String url) {
		final Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mAsyncImageLoader.getLoadingId());
		view.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
		mAsyncImageLoader.get(url, this);
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