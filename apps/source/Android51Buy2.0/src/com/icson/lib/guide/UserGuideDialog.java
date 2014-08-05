/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: SosoNovel
 * FileName: AppDialog.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jul 11, 2012
 */
package com.icson.lib.guide;

import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;

/**
 * @author qitao
 */
public class UserGuideDialog extends Dialog implements View.OnClickListener, ImageLoadListener
{
	public  static final int    LAYOUT_FIRST_OPEN = 1;
	public  static final int    LAYOUT_SECOND_OPEN = 2;
	public  static final int    LAYOUT_USER_CENTER = 3;
	public  static final int    LAYOUT_USER_CENTER2 = 4;
	public  static final int    LAYOUT_USER_CENTER3 = 5;
	public  static final int    LAYOUT_SLOT = 6;
	
	public int 		mGuideLayout = LAYOUT_FIRST_OPEN;
	
	private ImageLoader mAsyncImageLoader;
	
	private int getLayout()
	{
		switch (mGuideLayout) {
		case LAYOUT_FIRST_OPEN:
			return R.layout.dialog_user_guide;
		case LAYOUT_SECOND_OPEN:
			return R.layout.dialog_user_guide2;
		case LAYOUT_USER_CENTER:
			return R.layout.dialog_user_guide3;
		case LAYOUT_USER_CENTER2:
			return R.layout.dialog_user_guide4;
		case LAYOUT_USER_CENTER3:
			return R.layout.dialog_user_guide5;
		case LAYOUT_SLOT:
			return R.layout.dialog_user_guide6;
		default:
			return R.layout.dialog_user_guide;
		}
		
	}
	
	/**
	 * interface for OnButtonClickListener
	 * @author qitao
	 *
	 */
	public interface OnClickListener
	{
		/**
		 * onDialogClick
		 * @param nButtonId
		 */
		public abstract void onDialogClick(UserGuideDialog dialog, int nButtonId);
	}
	
	/**
	 * Create a new instance of AppDialog
	 * @param aContext
	 */
	public UserGuideDialog(Context aContext, OnClickListener aListener, int layoutType)
	{
		super(aContext, R.style.Dialog);
		mListener = aListener;
		mGuideLayout = layoutType;
	}
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) 
	{
		if((null != mListener) && (keyCode == KeyEvent.KEYCODE_BACK))
		{
			mListener.onDialogClick(this, BUTTON_NEGATIVE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		
		mAsyncImageLoader = new ImageLoader(this.getContext(), true);
		
		// Load the default configuration.
		setContentView(getLayout());
		
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		mContentBg = (ImageView)findViewById(R.id.dialog_bg_image);
		mMessage = (ImageView)findViewById(R.id.dialog_content_img);
		
		mPositive = (ImageView)findViewById(R.id.dialog_btn_positive);
		if (null != mPositive) {
			mPositive.setOnClickListener(this);
			mPositiveText = (TextView)findViewById(R.id.dialog_text_positive);
		}
		
		mNegative = (ImageView)findViewById(R.id.dialog_btn_negative);
		mNegative.setOnClickListener(this);
		
		ImageView img1 = (ImageView)findViewById(R.id.dialog_content_img1);
		if (null != img1) {
			img1.setOnClickListener(this);
			img1.setVisibility(View.GONE);
		}
		ImageView img2 = (ImageView)findViewById(R.id.dialog_content_img2);
		if (null != img2) {
			img2.setOnClickListener(this);
			img2.setVisibility(View.GONE);
		}
		ImageView img3 = (ImageView)findViewById(R.id.dialog_content_img3);
		if (null != img3) {
			img3.setOnClickListener(this);
			img3.setVisibility(View.GONE);
		}
	}
	
	public void setUrlForPositiveBtn(String url) 
	{
		Bitmap pBitmap = mAsyncImageLoader.get(url);
		if ( null != pBitmap ) {
			mPositiveText.setText(null);
			mPositive.setImageBitmap(pBitmap);
		} else {
			urlType.put(url, BUTTON_POSITIVE);
			mAsyncImageLoader.get(url, this);
		}
	}
	
	public void setUrlForButton(String url, int btnId) 
	{
		ImageView imgBtn = null;
		if (btnId == BUTTON_LIST_BASE) {
			imgBtn = (ImageView)findViewById(R.id.dialog_content_header);
		} else if (btnId == BUTTON_LIST_1) {
			imgBtn = (ImageView)findViewById(R.id.dialog_content_img1);
		} else if (btnId == BUTTON_LIST_2) {
			imgBtn = (ImageView)findViewById(R.id.dialog_content_img2);
		} else if (btnId == BUTTON_LIST_3) {
			imgBtn = (ImageView)findViewById(R.id.dialog_content_img3);
		}

		if (null == imgBtn) return;
		
		imgBtn.setVisibility(View.VISIBLE);
		Bitmap pBitmap = mAsyncImageLoader.get(url);
		if ( null != pBitmap ) {
			imgBtn.setImageBitmap(pBitmap);
		} else {
			urlType.put(url, btnId);
			mAsyncImageLoader.get(url, this);
		}
	}
	
	
	@Override
	public void onClick(View aView)
	{
		if ( null != mListener )
		{
			final int nId = aView.getId();
			switch ( nId )
			{
			case R.id.dialog_btn_positive:
				mListener.onDialogClick(this, BUTTON_POSITIVE);
				break;
				
			case R.id.dialog_btn_negative:
				mListener.onDialogClick(this, BUTTON_NEGATIVE);
				break;
				
			case R.id.dialog_content_img1:
				mListener.onDialogClick(this, BUTTON_LIST_1);
				break;
				
			case R.id.dialog_content_img2:
				mListener.onDialogClick(this, BUTTON_LIST_2);
				break;
				
			case R.id.dialog_content_img3:
				mListener.onDialogClick(this, BUTTON_LIST_3);
				break;
				
			default:
				return;
			}
		}
		
		// Dismiss the dialog.
		if(this.isShowing())
			dismiss();
	}
	
	/**
	 * setAttributes
	 */
	protected int setAttributes()
	{
		Window pWindow = getWindow();
		if ( null == pWindow )
			return 0;
		
		DisplayMetrics pMetrics = new DisplayMetrics();
		pWindow.getWindowManager().getDefaultDisplay().getMetrics(pMetrics);
		
		WindowManager.LayoutParams pParams = pWindow.getAttributes();
		pParams.gravity = Gravity.CENTER_HORIZONTAL;
		pParams.width = (int) (pMetrics.widthPixels * 1.0);
		pWindow.setAttributes(pParams);
		
		// Clean up.
		pMetrics = null;
		pWindow = null;
		
		return pParams.width;
	}

	// Member instances.
	protected int            mWinWidth;
	protected ImageView      mContentBg;
	protected ImageView      mMessage;
	protected ImageView      mPositive;
	protected TextView       mPositiveText;
	protected ImageView      mNegative;
	protected OnClickListener  mListener;
	
	private HashMap<String, Integer>  urlType = new HashMap<String, Integer>();
	
	public static final int BUTTON_LIST_BASE =	10000;
	public static final int BUTTON_LIST_1 =	BUTTON_LIST_BASE+1;
	public static final int BUTTON_LIST_2 =	BUTTON_LIST_BASE+2;
	public static final int BUTTON_LIST_3 =	BUTTON_LIST_BASE+3;
	
	// Constant members.
	public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
	  
	  // Field descriptor #17 I
	public static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;
	  
	  // Field descriptor #17 I
	public static final int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;

	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		// TODO Auto-generated method stub
		Integer type = urlType.get(strUrl);
		if (mAsyncImageLoader.isEmptyBitmap(strUrl)) {

		} else {
			if (type == BUTTON_POSITIVE && null != mPositive) {
				mPositiveText.setText(null);
				mPositive.setImageBitmap(aBitmap);
			} else if (type == BUTTON_LIST_BASE) {
				ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_header);
				if (null != imgBtn) imgBtn.setImageBitmap(aBitmap);
			} else if (type == BUTTON_LIST_1) {
				ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img1);
				if (null != imgBtn) imgBtn.setImageBitmap(aBitmap);
			} else if (type == BUTTON_LIST_2) {
				ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img2);
				if (null != imgBtn) imgBtn.setImageBitmap(aBitmap);
			} else if (type == BUTTON_LIST_3) {
				ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img3);
				if (null != imgBtn) imgBtn.setImageBitmap(aBitmap);
			}
		}
	}
	
	
	@Override
	public void onError(String strUrl) {
		// TODO Auto-generated method stub
		Integer type = urlType.get(strUrl);
		if (type == BUTTON_LIST_1) {
			ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img1);
			if (null != imgBtn) imgBtn.setVisibility(View.INVISIBLE);
		} else if (type == BUTTON_LIST_2) {
			ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img2);
			if (null != imgBtn) imgBtn.setVisibility(View.INVISIBLE);
		} else if (type == BUTTON_LIST_3) {
			ImageView imgBtn = (ImageView)findViewById(R.id.dialog_content_img3);
			if (null != imgBtn) imgBtn.setVisibility(View.INVISIBLE);
		}
		
		urlType.remove(strUrl);
	}

	public void cleanup() {
		if(null != mAsyncImageLoader)
		{
			mAsyncImageLoader.cleanup();
			mAsyncImageLoader = null;
		}
	}
}
