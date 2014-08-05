package com.icson.item;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.WanggouProHelper;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ItemGalleryAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {

	private ImageLoader mImageLoader;

	public static final String CACHE_DIR = "item_tab_gallery";

	private String[] urls;

	private BaseActivity mActivity;

	public static final int PIC_WIDTH = 160;

	public static final int PIC_HEIGHT = 198;

	private int pic_width_px;
	private int pic_height_px;
	private Gallery.LayoutParams smallLp;
	private Gallery.LayoutParams largeLp;
	
	private int selectIdx;  
	private static final float SCALE_SIZE = 1.4f;
	
	private boolean mFromMainPic;

	public ItemGalleryAdapter(BaseActivity activity, boolean bCheckMode, boolean fromMainPic)
	{
		
		mActivity = activity;
		// dip -> px
		pic_width_px = ToolUtil.dip2px(mActivity, PIC_WIDTH);
		pic_height_px = ToolUtil.dip2px(mActivity, PIC_HEIGHT);
		mFromMainPic = fromMainPic;
		
		mImageLoader = new ImageLoader(mActivity, CACHE_DIR, true, bCheckMode);
		mImageLoader.setMaxCache(Config.MAX_GALLERY_CACHE);
		smallLp = new Gallery.LayoutParams((int) (pic_width_px/SCALE_SIZE), (int) (pic_height_px/SCALE_SIZE));
		largeLp = new Gallery.LayoutParams(pic_width_px, 
				pic_height_px);
		
		mActivity.addDestroyListener(this);
	}
	
	public void setData(String productCharId, int num) {
		urls = new String[num];
		if(mFromMainPic)
		{
			for (int i = 0, len = num; i < len; i++) {
				urls[i] =  WanggouProHelper.getAdapterPicUrl(productCharId, PIC_WIDTH, i);
			}
		}
		else
		{
			for (int i = 0, len = num; i < len; i++) {
				urls[i] = IcsonProImgHelper.getAdapterPicUrl(productCharId, PIC_WIDTH, i);
			}
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ImageView view;
		if (convertView == null) {
			view = new ImageView(mActivity);
			view.setTag(view);
		} else {
			view = (ImageView) convertView.getTag();
		}

		String url = urls[position];

		Bitmap data = mImageLoader.get(url);

		if (data != null) {
			view.setImageBitmap(data);
		} else {
			view.setImageBitmap(ImageHelper.getResBitmap(mActivity, R.drawable.i_global_loading));
			mImageLoader.get(url, this);
		}

		if(selectIdx==position)
		{  
			view.setLayoutParams(largeLp);
			
			
		}
		else
		{
			view.setLayoutParams(smallLp);
			view.setScaleType(ScaleType.CENTER_INSIDE);
		}

		return view;
	}

	public void setSelectItemIdx(int idx)
	{
		if(selectIdx != idx)
		{
			selectIdx = idx;
			notifyDataSetChanged();
		}
		
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public int getCount() {
		return urls == null ? 0 : urls.length;
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
