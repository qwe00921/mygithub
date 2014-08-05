/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: AnnounceView.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 18, 2013
 */


package com.icson.lib.ui;

import com.icson.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class AnnounceView extends UiBase implements OnClickListener {
	/**
	 * Slide event listener
	 * @author lorenchen
	 */
	public interface OnAnnounceClickListener
	{
		/**
		 * On item click.
		 *
		 * @param v the v
		 * @param index the index
		 */
		public abstract void onAnnounceClick(boolean isClose);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public AnnounceView(Context context, AttributeSet attrs) {
		super(context, attrs, R.layout.view_announce);
	}
	
	/**
	 * setContent of text.
	 */
	public void setText(String strText) {
		if( null != mMarquee ) {
			mMarquee.setText(strText);
		}
	}
	
	public void setOnAnnounceClickListener(OnAnnounceClickListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onInit(Context context) {
		try {
			mMarquee = (MarqueeTextView)findViewById(R.id.announce_content);
			mMarquee.setOnClickListener(this);
			mClose = (ImageView)findViewById(R.id.announce_close);
			mClose.setOnClickListener(this);
		} catch( Exception aException ) {
			aException.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if( null == mListener )
			return ;
		
		final int nId = null != v ? v.getId() : 0;
		switch( nId ) {
		case R.id.announce_content:
			mListener.onAnnounceClick(false);
			break;
			
		case R.id.announce_close:
			mListener.onAnnounceClick(true);
			break;
		}
	}

	private MarqueeTextView mMarquee;
	private ImageView       mClose;
	private OnAnnounceClickListener mListener;
}
