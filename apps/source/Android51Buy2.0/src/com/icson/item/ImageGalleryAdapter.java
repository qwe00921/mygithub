package com.icson.item;

import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView.ScaleType;

import com.icson.R;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ImageGalleryAdapter extends BaseAdapter implements DestroyListener,ImageLoadListener {

	private ImageLoader mImageLoader;

	public static final String CACHE_DIR = "item_gallery";
	
	//public static final int PIC_WIDTH = 134;
	//public static final int PIC_HEIGHT = 134;
	//private int pic_width_px;
	//private int pic_height_px;

	private BaseActivity mActivity;
	private ItemProductModel mProductModel;
	private List<String> mUrlList;
	
	public static final int TYPE_IMAGE_URL_ARRAY = 1001;
	public static final int TYPE_PRODUCT_MODEL = 1002;
	private int mDataSourceType;

	public ImageGalleryAdapter(BaseActivity aActivity, ItemProductModel mModel)
	{
		mDataSourceType = TYPE_PRODUCT_MODEL;
		mActivity = aActivity;
		mProductModel = mModel;
		
		//pic_width_px = ToolUtil.dip2px(mContext, PIC_WIDTH);
		//pic_height_px = ToolUtil.dip2px(mContext, PIC_HEIGHT);

		mImageLoader = new ImageLoader(mActivity, CACHE_DIR, true, false);
		mActivity.addDestroyListener(this);
		
		mImageLoader.setMaxCache(Config.MAX_GALLERY_CACHE);
	}
	
	public ImageGalleryAdapter(BaseActivity aActivity, List<String> urlList) {
		mDataSourceType = TYPE_IMAGE_URL_ARRAY;
		mActivity = aActivity;
		mUrlList = urlList;
		
		mImageLoader = new ImageLoader(mActivity, CACHE_DIR, true, false);
		mActivity.addDestroyListener(this);
		mImageLoader.setMaxCache(Config.MAX_GALLERY_CACHE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		

		final String url = (String) getItem(position);
		
		final ItemImageView view;
		if (convertView == null) {
			view = new ItemImageView(mActivity);
			view.setTag(view);
		} else {
			view = (ItemImageView) convertView.getTag();
		}


		Bitmap data = mImageLoader.get(url);
		
		
		if (data != null) {
			view.setImageBitmap(data);
		} else {
			view.setImageBitmap(ImageHelper.getResBitmap(mActivity, R.drawable.i_global_loading));
			view.setScaleType(ScaleType.CENTER_INSIDE);
			mImageLoader.get(url, this);
		}

		//Gallery.LayoutParams lp = new Gallery.LayoutParams( (int) (ToolUtil.getAppWidth() * 9/10f ), LayoutParams.WRAP_CONTENT);
		Gallery.LayoutParams lp = new Gallery.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		view.setLayoutParams(lp);
		
		//view.setScaleType(ScaleType.CENTER_INSIDE);

		return view;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(mDataSourceType == TYPE_PRODUCT_MODEL)
		{
			if(null != mProductModel )
			{
				if(mProductModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU)
				{
					return mProductModel.getItemWanggouUrl(640, position);
				}
				else 
				{
					return (mProductModel.getProductUrl(640, position));
				}
			}
			return null;
		} else if(mDataSourceType == TYPE_IMAGE_URL_ARRAY){
			return (null != mUrlList ? mUrlList.get(position) : null);
		} else {
			return null;
		}
	}

	@Override
	public int getCount() {
		if(mDataSourceType == TYPE_PRODUCT_MODEL) {
			return (null != mProductModel ? mProductModel.getPicNum() : 0);
		} else if(mDataSourceType == TYPE_IMAGE_URL_ARRAY){
			return (null != mUrlList ? mUrlList.size() : 0);
		} else {
			return 0;
		}
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
		if( null != mImageLoader )
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}

}
