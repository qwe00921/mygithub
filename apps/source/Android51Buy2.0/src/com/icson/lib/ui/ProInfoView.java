/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: ProInfoView.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 12, 2013
 */

package com.icson.lib.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;

public class ProInfoView extends UiBase implements ImageLoadListener {
	/**
	 * @param context
	 * @param attrs
	 */
	public ProInfoView(Context context, AttributeSet attrs) {
		super(context, attrs, R.layout.view_product_info);
	}
	
	/**
	 * Constructor of EventView
	 * @param context
	 */
	public ProInfoView(Context context) {
		super(context, R.layout.view_product_info);
		this.onInit(context);
	}
	
	/**
	 * Set content information for product entity.
	 * @param strCaption
	 * @param strSubtitle
	 * @param strPrice
	 * @param strInfo
	 */
	public void setContent(String strSubtitle, String strMsg, String strInfo, String strComments, String strTag) {
		if( null != mSubtitle ) {
			mSubtitle.setText(strSubtitle);
		}
		
		if( null != mMessage )
			mMessage.setText(strMsg);
		
		if( null != mInfo )
			mInfo.setText(strInfo);
		if(TextUtils.isEmpty(strInfo))
			findViewById(R.id.proinfo_info_RMB).setVisibility(View.GONE);
		
		if( null != mComments ) {
			mComments.setText(strComments);
			mComments.setVisibility(TextUtils.isEmpty(strComments) ? View.GONE : View.VISIBLE);
		}
		
		if( null != mTag ) {
			final int nResId = UiUtils.getLocalImageId(strTag);
			if( nResId > 0 ) {
				mTag.setVisibility(View.VISIBLE);
				mTag.setImageResource(nResId);
			} else {
				mTag.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * set image for icon.
	 * @param bitmap
	 */
	public void setImage(Bitmap bitmap) {
		if( (null != mImage) && (null != bitmap) ) {
			mImage.setImageBitmap(bitmap);
		}
	}
	/**
	 * get imageView for icon.
	 * @param Local Resource Image
	 */
	public ImageView getImageView() {
		return mImage;
	}
	
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		this.setImage(aBitmap);
	}

	@Override
	public void onError(String strUrl) {
	}
	
	/**
	 * initialize the children components.
	 */
	@Override
	protected void onInit(Context aContext) {
		mSubtitle = (TextView)this.findViewById(R.id.proinfo_subtitle);
		mMessage = (TextView)this.findViewById(R.id.proinfo_msg);
		mInfo = (TextView)this.findViewById(R.id.proinfo_info);
		mComments = (TextView)this.findViewById(R.id.proinfo_comments);
		mImage = (ImageView)this.findViewById(R.id.proinfo_image);
		mTag = (ImageView)this.findViewById(R.id.proinfo_tag);
		mReferWidth = Config.PROINFO_WIDTH;
		mReferHeight = Config.PROINFO_HEIGHT;
	}
	
	private TextView  mSubtitle;
	private TextView  mMessage;
	private TextView  mInfo;
	private TextView  mComments;
	private ImageView mImage;
	private ImageView mTag;
}
