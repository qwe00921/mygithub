/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: PopularPanel.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jun 12, 2013
 */

package com.icson.lib.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.icson.R;
import com.icson.lib.ui.EventView.EventItem;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;

public class EventsPanel extends UiBase implements OnClickListener,AnimationListener, ImageLoadListener {
	/**
	 * @author lorenchen
	 */
	public interface OnEventClickListener
	{
		/**
		 * On item click.
		 * @param v the v
		 * @param index the index
		 */
		public abstract void onEventClick(View v, int position, Object aTag);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public EventsPanel(Context context, AttributeSet attrs) {
		super(context, attrs, R.layout.view_popular_panel);
	}
	
	/**
	 * removeAll
	 */
	public void removeAll() {
		if( null != mPanel )
			mPanel.removeAllViews();
		
		if( null != mCache )
			mCache.clear();
	}
	
	/**
	 * startAnim
	 */
	public void startAnim() {
		final int nCount = (null != mCache ? mCache.size() : 0);
		if( 0 >= nCount )
			return ;
		
//		if( null == mRunnable ) {
//			mRunnable = new Runnable(){
//				@Override
//				public void run() {
//					animEventInfo(mCurrent);
//				}
//			};
//		}
//		
//		bAnimationRoll = true;
//		this.startNext(ANIM_DELAY_MS);
	}
	
	/**
	 * stopAnim
	 */
	public void stopAnim() {
		if( null != mHandler && null != mRunnable ) {
			mHandler.removeCallbacks(mRunnable);
		}
		bAnimationRoll = false;
		//mCurrent = -1;
	}
	
	
	/**
	 * @param nDelayMs
	 */
	
	private void startNext(int nDelayMs) {
		if(!bAnimationRoll)
			return;
		final int nCount = (null != mCache ? mCache.size() : 0);
		if( (mCurrent >= 0) && (nCount > 0) ) {
			mHandler.postDelayed(mRunnable, nDelayMs);
		}
	}
	
	/**
	 * setOnEventClickListener
	 * @param listener
	 */
	public void setOnEventClickListener(OnEventClickListener listener) {
		mListener = listener;
	}
	
	/**
	 * addEvent to popular panel
	 * @return
	 */
	public boolean addEvent(String strCaption, String strSubtitle, String strTag, List<EventItem> aItems, Object aTag) {
		EventView aEvent = new EventView(this.getContext());
		
		// Update layout parameters.
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		aEvent.setLayoutParams(params);
		
		// Update information for event.
		aEvent.setInfo(strCaption, strSubtitle, strTag);
		
		// Add optional product information.
		aEvent.attachItems(aItems, mLoader, mHashMap, this);
		
		// Update animation listener.
		aEvent.setAnimationListener(this);
		
		// Add new event to item
		return this.addChild(aEvent, aTag);
	}
	
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		UiUtils.updateImage(aBitmap, strUrl, mHashMap);
	}

	@Override
	public void onError(String strUrl) {
		
	}
	
	/**
	 * add a new view to container.
	 */
	private boolean addChild(EventView aChild, Object aTag) {
		if( null == mPanel )
			return false;
		
		Context pContext = this.getContext();
		if( null == pContext )
			return false;
		
		// Add the new to table layout.
		TableRow pRow = null;
		final int nCount = mCache.size();
		final boolean bNewRow = (0 == nCount % COLUMNS);
		if( bNewRow ) {
			pRow = new TableRow(pContext);
			pRow.setBackgroundResource(R.color.separator_line);
			pRow.setPadding(0, 0, 0, LINE_PIXELS);
			mPanel.addView(pRow, mParams);
		} else {
			final int size = mPanel.getChildCount();
			pRow = (TableRow)mPanel.getChildAt(size - 1);
		}
		mDefault.rightMargin = LINE_PIXELS;
		pRow.addView(aChild, mDefault);
		
		// Update information.
		aChild.setTag(R.id.holder_pos, nCount);
		
		// Update view listener information.
		if( null != aTag ) {
			aChild.setTag(R.id.holder_obj, aTag);
		}
		
		// Update listener information.
		aChild.setOnClickListener(this);
		
		// Save the cache.
		mCache.add(aChild);
		
		return true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	
		// Get width.
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		mDefault.width = 0;
		mDefault.height = (int )((width / COLUMNS) * (14.0/17.0));
		mDefault.weight = 1;
	}
	
	@Override
	public void onClick(View v) {
		if( null != mListener ) {
			final int position = (Integer) v.getTag(R.id.holder_pos);
			final Object object = v.getTag(R.id.holder_obj);
			
			mListener.onEventClick(v, position, object);
		}
	}
	
	@Override
	protected void onInit(Context aContext) {
		mPanel = (TableLayout)findViewById(R.id.popular_panel_activities);
		
		// Initialize the image loader for image loader.
		//mLoader = new ImageLoader(aContext, Config.PIC_CACHE_DIR, true);
	}
	
	
	@Override
	public void onAnimationEnd(Animation animation) {
		mCurrent++;
		final int nCount = (null != mCache ? mCache.size() : 0);
		if( mCurrent >= nCount ) {
			mCurrent = 0;
			startNext(ANIM_DELAY_MS);
		} else {
			startNext(CELL_DELAY_MS);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
	
//	private boolean animEventInfo(int nIndex) {
//		final int nCount = (null != mCache ? mCache.size() : 0);
//		if( 0 > nIndex || nIndex >= nCount )
//			return false;
//		
//		// Get current event info.
//		EventView pChild = mCache.get(nIndex);
//		return (null != pChild && pChild.showNext());
//	}

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
	
	
	private boolean bAnimationRoll;
	private int mCurrent = 0;
	private ImageLoader mLoader;
	private ArrayList<EventView> mCache = new ArrayList<EventView>();
	private HashMap<String, ImageView> mHashMap = new HashMap<String, ImageView>();
	private TableLayout mPanel = null;
	private TableRow.LayoutParams mParams = new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	private TableRow.LayoutParams mDefault = new TableRow.LayoutParams(0, 0);
	private OnEventClickListener mListener;
	private static final int COLUMNS = 2;
	private static final int LINE_PIXELS = 1;
	
	private Handler mHandler;
	private Runnable mRunnable = null;
	private static final int ANIM_DELAY_MS  = 3000;
	private static final int CELL_DELAY_MS  = 50;
}
