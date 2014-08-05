package com.icson.item;

import java.io.Serializable;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.icson.R;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Log;
import com.icson.util.activity.BaseActivity;

public class ItemImageActivity extends BaseActivity {

	private static final String LOG_TAG = ItemImageActivity.class.getName();

	public static final String REQUEST_PRODUCT_MODEL = "product_model";

	public static final String REQUEST_PIC_INDEX = "pic_index";
	
	public static final String REQUEST_DATASOURCE_TYPE = "datasource_type";
	
	public static final String REQUEST_IMGURL_LIST = "imgurl_list";
	
	public static float screenWidth;
	public static float screenHeight;


	private ImageGallery mGallery;
	
	//private  Gallery mThumbGallery;
	//private  ItemGalleryAdapter mThumbAdapter;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_item_image);

		//mToolBars = (ViewGroup) findViewById(R.id.item_image_tools);
		mGallery = (ImageGallery) findViewById(R.id.item_image_imageview);
		mGallery.setVerticalFadingEdgeEnabled(false);
		mGallery.setHorizontalFadingEdgeEnabled(false);
		

		Intent intent = getIntent();
		int mPicIndex = intent.getIntExtra(REQUEST_PIC_INDEX, 0);
		int type = intent.getIntExtra(REQUEST_DATASOURCE_TYPE, ImageGalleryAdapter.TYPE_PRODUCT_MODEL);
		/*mViewZoomIn = mToolBars.findViewById(R.id.item_image_tools_zoomin);
		mViewZoomOut = mToolBars.findViewById(R.id.item_image_tools_zoomout);
		mViewZoomPrev = mToolBars.findViewById(R.id.item_image_tools_prev);
		mViewZoomNext = mToolBars.findViewById(R.id.item_image_tools_next);
		loadingBar = findViewById(R.id.item_image_loading);
		mViewZoomIn.setOnClickListener(this);
		mViewZoomOut.setOnClickListener(this);
		mViewZoomPrev.setOnClickListener(this);
		mViewZoomNext.setOnClickListener(this);
		*/
		
		ImageGalleryAdapter mAdapter = null;
		if(type == ImageGalleryAdapter.TYPE_PRODUCT_MODEL) {
			mAdapter = getAdapterForProductModel(intent);
		} else if(type == ImageGalleryAdapter.TYPE_IMAGE_URL_ARRAY) {
			mAdapter = getAdapterForUrlList(intent);
		} else {
			// no logic
		}
		
		if(mAdapter == null) {
			Log.e(LOG_TAG, "onCreate|product_model is empty.");
			UiUtils.makeToast(this, R.string.params_empty,true);
			finish();
			return;
		}
		
		mGallery.setAdapter(mAdapter);
		if(mPicIndex >= mAdapter.getCount())
		{
			Log.e(LOG_TAG, "PicIndex < Pic count");
			UiUtils.makeToast(this, R.string.gift_noBigPicture,true);
			finish();
			return;
		}
		mGallery.setSelection(mPicIndex);
		/*
		mGallery.setOnItemSelectedListener(this);
		mThumbGallery = ((Gallery) findViewById(R.id.thumb_gallery));
		mThumbAdapter = new ItemGalleryAdapter(this,
				mProductModel.getProductCharId(), mProductModel.getPicNum(), false);
		
		ViewGroup.LayoutParams param = mThumbGallery.getLayoutParams();
		param.height = ToolUtil.dip2px(this,
				ItemGalleryAdapter.PIC_HEIGHT);
		mThumbGallery.setAdapter(mThumbAdapter);
		mThumbGallery.setSelection(mPicIndex);
		mThumbGallery.setOnItemSelectedListener(this);
		*/
		
		screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
	}
	
	private ImageGalleryAdapter getAdapterForProductModel(Intent intent) {
		ImageGalleryAdapter adapter = null;
		if(intent != null) {
			
			final Serializable serializable = intent.getSerializableExtra(REQUEST_PRODUCT_MODEL);
			if (serializable == null) {
				Log.e(LOG_TAG, "onCreate|product_model is empty.");
				UiUtils.makeToast(this, R.string.params_empty,true);
				finish();
				return null;
			}
			ItemProductModel model = (ItemProductModel) serializable;
			adapter = new ImageGalleryAdapter(this, model);
		}

		return adapter;
	}
	
	private ImageGalleryAdapter getAdapterForUrlList(Intent intent) {
		ImageGalleryAdapter adapter = null;
		
		if(intent != null) {
			List<String> urlList = intent.getStringArrayListExtra(REQUEST_IMGURL_LIST);
			if (urlList == null) {
				Log.e(LOG_TAG, "onCreate|product_model is empty.");
				UiUtils.makeToast(this, R.string.params_empty,true);
				finish();
				return null;
			}
			adapter = new ImageGalleryAdapter(this, urlList);
		}
		
		return adapter;
	}
	/*
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.item_image_imageview:
			mToolBars.setVisibility(mToolBars.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			break;
		case R.id.item_image_tools_zoomin:
		//	mViewZoomIn.setEnabled(mImageView.zoomInBitmap());
			mViewZoomOut.setEnabled(true);
			break;
		case R.id.item_image_tools_zoomout:
		//	mViewZoomOut.setEnabled(mImageView.zoomOutBitmap());
			mViewZoomIn.setEnabled(true);
			break;
		case R.id.item_image_tools_prev:
		//	setImage(mPicIndex - 1);
			break;
		case R.id.item_image_tools_next:
		//	setImage(mPicIndex + 1);
			break;

		}
		
	}

	/*
	@Override
	public void sizeChanged(boolean canZoomIn, boolean canZoomOut) {
		mViewZoomIn.setEnabled(!isEmptyPic && canZoomIn);
		mViewZoomOut.setEnabled(!isEmptyPic && canZoomOut);
	}
	*/

	@Override
	protected void onDestroy() {
	//	if (mImageView != null) {
	//		mImageView.releaseBitmap();
	//	}
		//mViewZoomIn = null;
		//mViewZoomOut = null;
		//mViewZoomPrev = null;
		//mViewZoomNext = null;
	//	if( null != mImageLoader )
	//	{
	//		mImageLoader.cleanup();
	//		mImageLoader = null;
	//	}
		super.onDestroy();
	}

	/*
	@Override
	public void onItemSelected(AdapterView<?> parent, View aV, int pos,
			long arg3) {
		if(parent == mThumbGallery)
			mGallery.setSelection(pos);
		else
			mThumbGallery.setSelection(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	*/
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ItemImageActivity);
	}
}
