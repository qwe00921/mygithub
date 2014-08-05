package com.icson.order.shoppingcart;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ShoppingCartGiftModel;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.HorizontalListView;
import com.icson.shoppingcart.ProductCouponGiftModel.CouponGiftModel;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ShoppingCartProductAdapter extends BaseAdapter implements ImageLoadListener, DestroyListener {
	private BaseActivity mActivity;
	private LayoutInflater mLayoutInflater;
	private ImageLoader mAsyncImageLoader;
	private ArrayList<ShoppingCartProductModel> mShoppingCartProductModels;

	public ShoppingCartProductAdapter(BaseActivity activity, ArrayList<ShoppingCartProductModel> models) {
		mActivity = activity;
		mShoppingCartProductModels = models;
		mLayoutInflater = activity.getLayoutInflater();
		mAsyncImageLoader = new ImageLoader(mActivity, true);
		
		mActivity.addDestroyListener(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup item = (ViewGroup) mLayoutInflater.inflate(R.layout.orderconfirm_product_item, null);
		ShoppingCartProductModel productModel = (ShoppingCartProductModel) getItem(position);
		ImageView imgV = (ImageView) item.findViewById(R.id.order_imageview_pic);
		imgV.setTag(productModel.getProductId());
		imgV.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					Bundle param = new Bundle();
					long productID = (Long) v.getTag();
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID, productID);
					ToolUtil.startActivity(mActivity, ItemActivity.class, param);
				}
				return false;
			}});
		
		loadImage(imgV, IcsonProImgHelper.getAdapterPicUrl(productModel.getProductCharId(), 80));
		
		((TextView) item.findViewById(R.id.order_textview_name)).setText(productModel.getNameNoHTML());
		((TextView) item.findViewById(R.id.order_textview_price)).setText(mActivity.getString(R.string.rmb) + productModel.getShowPriceStr());
		((TextView) item.findViewById(R.id.order_textview_num)).setText("共" + productModel.getBuyCount() + "件");
		
		//赠品
		ArrayList<ShoppingCartGiftModel> productGiftModels = productModel.getShoppingCartGiftModels();
		ArrayList<CouponGiftModel> couponGiftModels = (null != productModel.getCouponGiftModel()) ? productModel.getCouponGiftModel().getCouponModels() : null;
		ArrayList<BaseModel> baseModels = new ArrayList<BaseModel>();
		if(null != couponGiftModels) {
			baseModels.addAll(couponGiftModels);
		}
		if(null != productGiftModels) {
			baseModels.addAll(productGiftModels);
		}
		
		LinearLayout giftLayout = (LinearLayout) item.findViewById(R.id.order_linear_gift);
		if (baseModels.size() > 0) {
			giftLayout.setVisibility(View.VISIBLE);
			
			HorizontalListView giftListView = (HorizontalListView) item.findViewById(R.id.item_gift_listview);
			
			ShoppingCartProductGiftAdapter pAdapter = new ShoppingCartProductGiftAdapter(mActivity, baseModels);
			giftListView.setAdapter(pAdapter);
			/*
			giftListView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View aV,
						int pos, long arg3) {
					ShoppingCartGiftModel model2 = (ShoppingCartGiftModel) pAdapter.getItem(pos);
					ProductModel model = new ProductModel();
					model.setProductCharId(model2.getProductCharId());
					model.setPicNum(model2.getPicNum());
					Bundle param = new Bundle();
					param.putInt(ItemImageActivity.REQUEST_PIC_INDEX, 0);
					param.putSerializable(ItemImageActivity.REQUEST_PRODUCT_MODEL,
							model);
					ToolUtil.startActivity(mActivity, ItemImageActivity.class, param);
				}});
			*/
		}else{
			giftLayout.setVisibility(View.GONE);
		}
		
		
		View line = (View) item.findViewById(R.id.orderconfirm_product_line);
		if ( position == (mShoppingCartProductModels.size() -1) ){
			line.setVisibility(View.GONE);
		}else{
			line.setVisibility(View.VISIBLE);
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
		return mShoppingCartProductModels.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mShoppingCartProductModels.size();
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