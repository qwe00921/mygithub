/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: NavigationBar.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: May 30, 2013
 */

package com.tencent.djcity.lib.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.djcity.R;

public class NavigationBar extends RelativeLayout implements OnClickListener {
	/**
	 * @author lorenchen
	 */
	public interface OnLeftButtonClickListener {
		public abstract void onClick();
	}
	
	/**
	 * Constructor of NavigationBar
	 * @param context
	 * @param attrs
	 */
	public NavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		parseAttrs(attrs);
	}
	
	public ImageView getLeftBack()
	{
		return mLeftBack;
	}
	
	public void setText(int nResId) {
		this.setText(mContext.getString(nResId));
	}
	
	public void setText(String strText) {
		if( null != mText && !TextUtils.isEmpty(strText) ) {
			mCaption = strText;
			mText.setText(strText);
		}
	}
	
	public void setRightText(String strText) {
		setRightText(strText, null);
	}
	
	public void setRightText(String strText, Drawable pDrawable) {
		if( TextUtils.isEmpty(strText) ) {
			mAction.setVisibility(View.GONE);
			mIndicator.setImageDrawable(pDrawable);
			mIndicator.setVisibility(View.VISIBLE);
		} else {
			mIndicator.setVisibility(View.GONE);
			mAction.setVisibility(View.VISIBLE);
			mAction.setText(strText);
			
			mAction.setCompoundDrawables(null != pDrawable ? pDrawable : null, null, null, null);
		}
	}
	
	
	public void setRightInfo(int nResId, OnClickListener listener) {
		setRightInfo(getContext().getString(nResId), listener);
	}
	
	public void setRightInfo(String strText, OnClickListener listener) {
		if( null != mAction && !TextUtils.isEmpty(strText) ) {
			mAction.setVisibility(View.VISIBLE);
			mAction.setText(strText);
			mAction.setOnClickListener(listener);
		}
	}
	
	public void setOnIndicatorClickListener(OnClickListener listener) {
		if( null != mIndicator ) {
			mIndicator.setOnClickListener(listener);
		}
	}
	
	public void setRightVisibility(int pVisibilit) {
		if( null != mAction  ) {
			mAction.setVisibility( pVisibilit );
		}
		
		if( null != mIndicator  ) {
			mIndicator.setVisibility( pVisibilit );
		}
	}
	
	public void setLeftVisibility(int pVisibilit) {
		if( null != mLeftBack  ) {
			mLeftBack.setVisibility( pVisibilit );
		}
	}
	
	public void setOnDrawableRightClickListener(OnClickListener listener) {
		if( null != mAction ) {
			mAction.setOnClickListener(listener);
		}
		
		if( null != mIndicator ) {
			mIndicator.setOnClickListener(listener);
		}
	}
	
	public void setOnLeftButtonClickListener(OnLeftButtonClickListener listener) {
		mListener = listener;
	}
	
	@Override
	public void onClick(View v) {
		switch( v.getId() ) {
		case R.id.navigationbar_text:
		case R.id.navigationbar_drawable_left:
			if( (null != mListener) && (null != mLeftBack) && (View.VISIBLE == mLeftBack.getVisibility()) )
				mListener.onClick();
			break;
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init(mContext);
	}
	
	private int ID_NAV_LAYOUT = 1001;
	private void init(Context context) {
		// Inflate.
		if( null == context )
			return ;
		
		setBackgroundColor(0xFFF6F6F6);
		View view = inflate(context, R.layout.navigationbar_layout, null);
		view.setId(ID_NAV_LAYOUT);
		addView(view, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		View divider = new View(context);//inflate(context, R.layout.divider_1, null);
		divider.setBackgroundColor(0xeee9e9e9);
//		divider.setBackgroundColor(Color.RED);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, 2);
		params.addRule(BELOW, ID_NAV_LAYOUT);
		divider.setLayoutParams(params);
		addView(divider, params);
		
		// Get children components.
		mText = (TextView)findViewById(R.id.navigationbar_text);
		mText.setText(mCaption);
		mText.setOnClickListener(this);
		
		mAction = (TextView)findViewById(R.id.navigationbar_right_text);
		mIndicator = (ImageView)findViewById(R.id.navigationbar_right_icon);
		if( mDrawableId > 0 ) {
			mIndicator.setImageResource(mDrawableId);
			mIndicator.setVisibility(View.VISIBLE);
		}

		mLeftBack = (ImageView) findViewById(R.id.navigationbar_drawable_left);
		if( mLeftDrawableId > 0 ) {
			mLeftBack.setImageResource(mLeftDrawableId);
		}
		
		mLeftBack.setOnClickListener(this);
	}
	
	private void parseAttrs(AttributeSet attrs) {
		if( null == attrs || null == mContext )
			return ;
		
		// Parse attributes.
		if( null != attrs ) {
			TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.djcityattrs);
			mCaption = UiUtils.getString(mContext, array, R.styleable.djcityattrs_text);
			mDrawableId = UiUtils.getResId(mContext, array, R.styleable.djcityattrs_drawableRight);
			mLeftDrawableId = UiUtils.getResId(mContext, array, R.styleable.djcityattrs_drawableLeft);
		}
	}
	
	private Context        mContext;
	private String         mCaption;
	private TextView       mText;
	private int            mDrawableId = 0;
	private int 		   mLeftDrawableId = 0;
	private TextView       mAction;
	private ImageView      mIndicator;
	private ImageView      mLeftBack;
	private OnLeftButtonClickListener mListener;	
}
