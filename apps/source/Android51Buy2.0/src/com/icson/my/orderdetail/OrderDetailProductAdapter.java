package com.icson.my.orderdetail;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderGiftModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.ui.HorizontalListView;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class OrderDetailProductAdapter extends BaseAdapter implements
		ImageLoadListener, DestroyListener {
	private BaseActivity mActivity;
	private boolean isShowAll;
	private LayoutInflater mLayoutInflater;
	private ImageLoader mAsyncImageLoader;
	private ArrayList<OrderProductModel> mOrderProductModels;

	public OrderDetailProductAdapter(BaseActivity activity,
			ArrayList<OrderProductModel> models) {
		mActivity = activity;
		mOrderProductModels = models;
		mLayoutInflater = activity.getLayoutInflater();
		mAsyncImageLoader = new ImageLoader(mActivity, true);
		
		mActivity.addDestroyListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup item = (ViewGroup) mLayoutInflater.inflate(
				R.layout.my_orderdetail_product_item, null);
		
		View seperator = (View) item.findViewById(R.id.seperator);
		// 描边
		int R_bg = R.drawable.i_my_orderlist_item_bg_shape;
		if (mOrderProductModels.size() > 1) {
			seperator.setVisibility(View.VISIBLE);
			if (position == 0) {
				R_bg = R.drawable.package_up_shape;
				
			} else if (position == (getCount() - 1)) {
				R_bg = R.drawable.package_mid_shape;
			} else {
				R_bg = R.drawable.package_mid_shape;
			}
		}
		item.setBackgroundResource(R_bg);

		OrderProductModel model = (OrderProductModel) getItem(position);
		loadImage((ImageView) item.findViewById(R.id.order_imageview_pic),
				IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 80));
		((TextView) item.findViewById(R.id.order_textview_name)).setText(model
				.getNameNoHTML());
		((TextView) item.findViewById(R.id.order_textview_price)).setText(mActivity.getString(R.string.rmb)
				+ ToolUtil.toPrice(model.getShowPrice(), 2));
		
		String str = model.getBuyCount() + "件";
		((TextView) item.findViewById(R.id.order_textview_count)).setText(str);

		// 可以评论,并且未评论
		if (model.isCanEvaluate() && !model.isEvaluated()) {
			item.findViewById(R.id.evaluateBtn).setVisibility(
					View.VISIBLE);
		} else {
			item.findViewById(R.id.evaluateBtn).setVisibility(
					View.GONE);
		}
		
		//赠品
		LinearLayout giftLayout = (LinearLayout) item.findViewById(R.id.order_linear_gift);
		if (model.getGiftCount() > 0) {
			giftLayout.setVisibility(View.VISIBLE);
			
			ArrayList<OrderGiftModel> models = model.getOrderGiftModels();
			HorizontalListView giftListView = (HorizontalListView) item.findViewById(R.id.item_gift_listview);
			
			OrderDetailTabGiftAdapter pAdapter = new OrderDetailTabGiftAdapter(mActivity, models);
			giftListView.setAdapter(pAdapter);
			
		}else{
			giftLayout.setVisibility(View.GONE);
		}
		
		
		return item;
	}

	public boolean isShowAll() {
		return isShowAll;
	}

	public void setShowAll(boolean show) {
		isShowAll = show;
	}

	@Override
	public long getItemId(int position) {
		return mOrderProductModels.get(position).getProductId();
	}

	@Override
	public Object getItem(int position) {
		return mOrderProductModels.get(position);
	}

	@Override
	public int getCount() {
		if (!isShowAll)
			return 1;
		else
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