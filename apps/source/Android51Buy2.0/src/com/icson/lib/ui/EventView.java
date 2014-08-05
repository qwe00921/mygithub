/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: EventView.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 12, 2013
 */

package com.icson.lib.ui;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;

public class EventView extends UiBase {
	/**
	 * Constructor of EventView
	 * @param context
	 */
	public EventView(Context context, AttributeSet attrs) {
		super(context, attrs, R.layout.view_event_info);
	}
	
	/**
	 * Constructor of EventView
	 * @param context
	 */
	public EventView(Context context) {
		super(context, R.layout.view_event_info);
		this.onInit(context);
	}
	
	/**
	 * Set content information for product entity.
	 * @param strCaption
	 * @param strSubtitle
	 * @param strPrice
	 * @param strInfo
	 */
	void setInfo(String strCaption, String strSubtitle, String strTag) {
		if( null != mCaption )
			mCaption.setText(strCaption);
		
		if( null != mSubtitle )
			mSubtitle.setText(strSubtitle);
		
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
	 * attach items.
	 * @param aItems
	 */
	void attachItems(List<EventItem> aItems, ImageLoader aLoader, HashMap<String, ImageView> aHashMap, ImageLoadListener aListener) {
		final int nSize = (null != aItems ? aItems.size() : 0);
		if( (0 >= nSize) || (null == mFlipper) )
			return ;
		
		// Create default info.
		Context pContext = this.getContext();
	//	Typeface typeFace = Typeface.createFromAsset(pContext.getAssets(),"fonts/customed_price.ttf");
	
		int nIdx = 0;
		//if let for do its job. Need check PortalActivity.preLoadImage
		//for(nIdx = 0; nIdx < 1; nIdx ++ ) 
		{
			EventItem entity = aItems.get(nIdx);
			if( null != entity ) {
				View pChild = inflate(pContext, R.layout.view_event_item, mFlipper);
				if( null != pChild ) {
					TextView pCaption = (TextView)pChild.findViewById(R.id.event_item_caption);
//					pCaption.setTypeface(typeFace);
					int index = entity.mPrice.indexOf(".");
					if(index > 0 && entity.mPrice.length() > 5)
					{
						pCaption.setText(entity.mPrice.subSequence(0, index));
					}
					else
					{
						pCaption.setText(entity.mPrice);
					}
					TextView pInfo = (TextView)pChild.findViewById(R.id.event_item_info);
					TextView pInfoRMB = (TextView)pChild.findViewById(R.id.event_item_info_RMB);
					
					Double dPrice = Double.valueOf(entity.mPrice); //促销价
					Double dInfoPrice = Double.valueOf(entity.mInfo); //原价
					if(dPrice>=dInfoPrice)
					{
						pInfo.setVisibility(View.INVISIBLE);
						pInfoRMB.setVisibility(View.INVISIBLE);
					}
					else
					{
						pInfo.setText(entity.mInfo);
						pInfo.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
						pInfoRMB.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
					}

					// Update bitmap.
					ImageView pImage = (ImageView)pChild.findViewById(R.id.event_item_image);
					UiUtils.loadImage(pImage, entity.mUrl, aLoader, aHashMap, aListener);
					// Add to parent.
//					mFlipper.addView(pChild);
				}
			}
		}
	}
	
	/**
	 * Show next page.
	 * @return
	 */
//	boolean showNext() {
//		final int nSize = (null != mFlipper ? mFlipper.getChildCount() : 0);
//		if( nSize > 1 ) {
//			mFlipper.showNext();
//			return true;
//		}
		
//		return false;
//	}
	
	/**
	 * @param listener
	 */
	void setAnimationListener(AnimationListener listener) {
		if( null != mAnimIn ) {
			mAnimIn.setAnimationListener(listener);
		}
	}
	
	/**
	 * initialize the children components.
	 */
	@Override
	protected void onInit(Context aContext) {
		mCaption = (TextView)findViewById(R.id.event_caption);
		mSubtitle = (TextView)findViewById(R.id.event_subtitle);
		mTag = (ImageView)findViewById(R.id.event_tag);
		mFlipper = (ViewGroup)findViewById(R.id.event_product_flipper);
//		mAnimIn = AnimationUtils.loadAnimation(aContext, R.anim.push_down_in);
//		mFlipper.setInAnimation(mAnimIn);
//		Animation pAnimOut = AnimationUtils.loadAnimation(aContext, R.anim.push_down_out);
//		mFlipper.setOutAnimation(pAnimOut);
	}
	
	public static class EventItem {
		public EventItem(String strPrice, String strInfo, String strUrl) {
			mPrice = strPrice;
			mInfo = strInfo;
			mUrl = strUrl;
		}
		String mPrice;
		String mInfo;
		String mUrl = null;
	}
	
	private TextView    mCaption;
	private TextView    mSubtitle;
	private ImageView   mTag;
	private ViewGroup   mFlipper;
	private Animation   mAnimIn;
}
