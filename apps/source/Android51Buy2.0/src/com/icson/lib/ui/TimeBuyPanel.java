/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: TimeBuyPanel.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jan 12, 2013
 */

package com.icson.lib.ui;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.icson.R;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;


public class TimeBuyPanel extends UiBase implements OnClickListener, ImageLoadListener {
	/**
	 * OnItemClickListener
	 * @author lorenchen
	 *
	 */
	public interface OnItemClickListener
	{
		
		/**
		 * On item click.
		 *
		 * @param v the v
		 * @param index the index
		 */
		public abstract void onItemClick(View aView, Object aTag);
	}
	
	/**
	 * Constructor of TimeBuyPanel
	 * @param context
	 * @param attrs
	 */
	public TimeBuyPanel(Context context, AttributeSet attrs) {
		super(context, attrs, R.layout.view_timebuy_panel);
	}
	
	/**
	 * @param strCaption
	 */
	public void setContent(String strCaption) {
		if( null != mCaption ) {
			mCaption.setText(strCaption);
		}
	}
	
	/*
	public void setTimerValue(long nCurrentSecs, long nEndSecs) {
		if( (null != mTimer) && (nCurrentSecs > 0) && (nEndSecs > 0) ) {
			if( null == mClockRunnable ) {
				mClockRunnable = new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
					}
				};
			}
			mTimer.setTiming(nCurrentSecs, nEndSecs, mClockRunnable);
		}
	}
	*/
	/**
	 * setOnItemClickListener
	 * @param listener
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		mListener = listener;
	}
	
	/**
	 * clean up the flipper information.
	 */
	public void cleanup() {
		if( null != mFlipper ) {
			mFlipper.removeAllViews();
		}
	}
	
	/**
	 * start loop
	 */
	public void startLoop() {
		final int nSize = null != mFlipper ? mFlipper.getChildCount() : 0;
		if( 1 >= nSize )
			return ;
		
		if( null == mRunnable ) {
			mRunnable = new Runnable(){
				@Override
				public void run() {
					mFlipper.showNext();
				}
			};
		}
		
		mHandler.postDelayed(mRunnable, INTERVAL);
	}
	
	public void stopLoop() {
		
		if( null != mHandler && null != mRunnable ) {
			mHandler.removeCallbacks(mRunnable);
		}
	}
	
	/**
	 * addSnapupInfo
	 * @param strPromotion
	 * @param nImageId
	 */
	public void addSnapupInfo(String strMsg, String strInfo, String strComment, String strPicUrl) {
		if( TextUtils.isEmpty(strComment) || TextUtils.isEmpty(strPicUrl) )
			return ;
		

		View pChild = inflate(this.getContext(), R.layout.view_snapup_info, null);
		if( null == pChild )
			return ;
		
		TextView pComment = (TextView)pChild.findViewById(R.id.snapup_comment);
		pComment.setText(strComment);
		TextView pMsg = (TextView)pChild.findViewById(R.id.snapup_msg);
		pMsg.setText(strMsg);
		TextView pInfo = (TextView)pChild.findViewById(R.id.snapup_info);
		pInfo.setText(strInfo);
		pInfo.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		TextView pInfo_RMB = (TextView)pChild.findViewById(R.id.snapup_info_RMB);
		pInfo_RMB.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		ImageView pImage = (ImageView)pChild.findViewById(R.id.snapup_image);
		UiUtils.loadImage(pImage, strPicUrl, mLoader, mHashMap, this);
		
		// Add to parent.
		mFlipper.addView(pChild);
	}
	
	/**
	 * @param nPos
	 * @param aInfo
	 */
	public void setChannelInfo(int nPos, String strPicUrl, String strSubtitle, String strPromotion, String strHint, Object aTag) {
		final int nSize = (null != mChannels ? mChannels.size() : 0);
		if( 0 > nPos || nPos >= nSize )
			return ;
		
		ChannelInfo pInfo = mChannels.get(nPos);
		if( null != pInfo ) {
			pInfo.setInfo(strPicUrl, strSubtitle, strPromotion, strHint, aTag);
		}
	}
	
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		UiUtils.updateImage(aBitmap, strUrl, mHashMap);
	}

	@Override
	public void onError(String strUrl) {
	}
	
	@Override
	public void onClick(View v) {
		if( null == mListener )
			return ;
		
		int nPos = -1;
		switch( v.getId() ){
		case R.id.eqiang_snapup:
			nPos = -1;
			break;
		case R.id.eqiang_channel_1:
			nPos = 0;
			break;
		case R.id.eqiang_channel_2:
			nPos = 1;
			break;
		}
		
		Object pObject = null;
		if( nPos >= 0 ) {
			ChannelInfo pInfo = mChannels.get(nPos);
			pObject = pInfo.mObject;
		}
		
		mListener.onItemClick(v, pObject);
	}

	@Override
	protected void onInit(Context aContext) {
		mCaption = (TextView)findViewById(R.id.eqiang_panel_caption);
		mSnapup = findViewById(R.id.eqiang_snapup);
		mSnapup.setOnClickListener(this);
		
		mChannels = new ArrayList<ChannelInfo>(MAX_CHANNELS);
		addChannelInfo(R.id.eqiang_channel_1, R.id.eqiang_channel_1_icon, R.id.eqiang_channel_1_subtitle, R.id.eqiang_channel_1_promotion, R.id.eqiang_channel_1_hint);
		addChannelInfo(R.id.eqiang_channel_2, R.id.eqiang_channel_2_icon, R.id.eqiang_channel_2_subtitle, R.id.eqiang_channel_2_promotion, R.id.eqiang_channel_2_hint);
		
		// Clock
		//mTimer = (ClockView)findViewById(R.id.snapup_clock);
		mFlipper = (ViewFlipper) findViewById(R.id.snapup_info_flipper);
		mFlipper.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		mFlipper.setInAnimation(AnimationUtils.loadAnimation(aContext, android.R.anim.fade_in));
		Animation pAnimation = AnimationUtils.loadAnimation(aContext, android.R.anim.fade_out);
		mFlipper.setOutAnimation(pAnimation);
		pAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				if( (null != mHandler) && (null != mRunnable) )
					mHandler.postDelayed(mRunnable, INTERVAL);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
		
		// Initialize the image loader for image loader.
		//mLoader = new ImageLoader(aContext, Config.PIC_CACHE_DIR, true);
	}
	
	/**
	 * @param nChannelId
	 * @param nIconId
	 * @param nSubtitleId
	 * @param nPromotionId
	 * @param nHintId
	 */
	private void addChannelInfo(int nChannelId, int nIconId, int nSubtitleId, int nPromotionId, int nHintId) {
		if( null == mChannels )
			return ;
		
		ChannelInfo pInfo = new ChannelInfo(this, nChannelId, nIconId, nSubtitleId, nPromotionId, nHintId);
		pInfo.mChannel.setOnClickListener(this);
		mChannels.add(pInfo);
	}
	
	/**  
	* method Name:setHandler    
	* method Description:  
	* @param mWholeHandler   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	public void setHandler(Handler aWholeHandler) {
		mHandler = aWholeHandler;
	}
	
	public void setImageLoader(ImageLoader aLoader) {
		mLoader = aLoader;
	}
	
	/**
	 * @author lorenchen
	 *
	 */
	private class ChannelInfo {
		ChannelInfo(View aParent, int nChannelId, int nIconId, int nSubtitleId, int nPromotionId, int nHintId) {
			mChannel = aParent.findViewById(nChannelId);
			mIcon = (ImageView)aParent.findViewById(nIconId);
			mSubtitle = (TextView)aParent.findViewById(nSubtitleId);
			mPromotion = (TextView)aParent.findViewById(nPromotionId);
			mHint = (TextView)aParent.findViewById(nHintId);
		}
		
		void setInfo(String strPicUrl, String strSubtitle, String strPromotion, String strHint, Object aTag) {
			UiUtils.loadImage(mIcon, strPicUrl, TimeBuyPanel.this.mLoader, mHashMap, TimeBuyPanel.this);
			mSubtitle.setText(strSubtitle);
			mPromotion.setText(strPromotion);
			mHint.setText(strHint);
			mObject = aTag;
		}
		
		View      mChannel;
		ImageView mIcon;
		TextView  mSubtitle;
		TextView  mPromotion;
		TextView  mHint;
		Object    mObject;
	}
	
	private TextView mCaption;
	private View     mSnapup;
	//private ClockView mTimer;
	private ViewFlipper mFlipper;
	private ImageLoader mLoader;
	private OnItemClickListener mListener;
	private List<ChannelInfo> mChannels;
	private HashMap<String, ImageView> mHashMap = new HashMap<String, ImageView>();
	
	private Handler mHandler;
	private Runnable mRunnable = null;
	//private Runnable mClockRunnable = null;
	private static final int INTERVAL = 3000;
	private static final int MAX_CHANNELS = 2;
	
}
