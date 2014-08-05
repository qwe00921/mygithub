/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: rollhell
 * FileName: TryScrollV.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-5-14
 */
package com.tencent.djcity.lib.ui;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.tencent.djcity.R;

/**  
 *   
 * Class Name:TryScrollV 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-5-14 ����11:33:39 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class LoopScrollView extends ScrollView {

	/**  
	* Create a new Instance LoopScrollV.  
	*  
	* @param context  
	*/
	public LoopScrollView(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	
	/**  
	* Create a new Instance LoopScrollV.  
	*  
	* @param context
	* @param attrs  
	*/
	public LoopScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		parseAttrs(attrs);
	}
	
	private void parseAttrs(AttributeSet attrs) {
		TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.loopscrollview);
		mImgMargin = UiUtils.getInteger(mContext, array, R.styleable.loopscrollview_imgMargin);
		if(mImgMargin >0)
		{
			mImgLayout = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mImgLayout.setMargins(0, mImgMargin,0,mImgMargin);
			mImgLayout.gravity = Gravity.CENTER_HORIZONTAL;
		}
		mFooter = UiUtils.getFloat(mContext, array, R.styleable.loopscrollview_windowFooter);
		if(mFooter<=0)
			mFooter = WINDOW_FOOTER;
		array.recycle();
		
	}

	/**  
	* Create a new Instance LoopScrollV.  
	*  
	* @param context
	* @param attrs
	* @param defStyle  
	*/
	public LoopScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		parseAttrs(attrs);
	}
	
	
	public void initView(List<Integer> aPics,int startIdx)
	{
		this.setVerticalFadingEdgeEnabled(true);
		mPicRowLayout =  new LinearLayout(mContext);
		mPicRowLayout.setOrientation(LinearLayout.VERTICAL);
		addView(mPicRowLayout);
		setDrawables(aPics,startIdx);
	}
	

	@Override
	public boolean onTouchEvent (MotionEvent ev) 
	{
		return true;
		
	}
	/*
	 * 
	 */
	public void setDrawables(List<Integer> aPics,int startIdx) {
		mPicSize = aPics.size();
		//itemHeight = aPics.get(0).getIntrinsicHeight();
		
		startIdx--;
		
		for(int i = 0; i < mPicSize + 1 + 2*mFooter; i++)
		{
			startIdx = (startIdx+1) % mPicSize;
			ImageView aV = new ImageView(mContext);
			if(null!=mImgLayout)
				aV.setLayoutParams(mImgLayout);
			
			aV.setImageResource(aPics.get(startIdx));
			if(itemHeight==0)
			{
				aV.measure(0,0);
				itemHeight = aV.getMeasuredHeight() + mImgMargin*2;
			}
			mPicRowLayout.addView(aV);
		}
	}
	
	
	/**
	 * 
	* method Name:scrollBy    
	* method Description:  
	* @param step   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void loopScrollBy(int step)
	{
		lastY = getScrollY();
		if(lastY >= itemHeight*(mPicRowLayout.getChildCount()-1 - 2*mFooter))// mPicSize- 2*WINDOW_FOOTER + 1))
		{
			scrollTo(0, (int) (lastY%(itemHeight*(mPicRowLayout.getChildCount()-1 - 2*mFooter))));
					
		}
		smoothScrollBy(0, step);
	}
	
	
	/**
	 *  
	* method Name:slowScrollTo    
	* method Description:  
	* @param idx
	* @param speed
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	public boolean slowScrollTo(int idx)
	{
		int curIdx = idx;
		if(idx == 0)
		{
			curIdx = idx;
		}
		if(curIdx <= STOP_AHEAD_MULTE)
		{
			curIdx += mPicSize; 
		}
		int interV = getCursorOffset(curIdx);
		if(interV == 0 && bSlowing)
			return true;
		
		bSlowing = true;
		if (interV > itemHeight * STOP_AHEAD_MULTE || interV < 0)
		{
			scrollTo(0, (int) ((curIdx - mFooter - STOP_AHEAD_MULTE)* itemHeight));
		}
		interV = getCursorOffset(curIdx);
			
		if (interV > itemHeight * SCROLL_SPEED_MULTE )
		{
			loopScrollBy((int) (itemHeight * SCROLL_SPEED_MULTE));
			return false;
		}
		else 
		{
			loopScrollBy(interV);
			return true;
		}
	}
	
	public void quickScrollTo(int idx)
	{
		int curIdx = idx;
		if(idx == 0)
		{
			curIdx = idx;
		}
		if(curIdx <= STOP_AHEAD_MULTE)
		{
			curIdx += mPicSize; 
		}
		int interV = getCursorOffset(curIdx);
		if(interV != 0)
		{
			loopScrollBy(interV);
		}
	}
	/**
	 * 
	* method Name:getCursorOffset    
	* method Description:  
	* @param idx
	* @return   
	* int  
	* @exception   
	* @since  1.0.0
	 */
	private int getCursorOffset(int idx)
	{
		lastY = getScrollY();
		if(lastY >= itemHeight*(mPicRowLayout.getChildCount()-1 - 2*mFooter))// mPicSize- 2*WINDOW_FOOTER + 1))
		{
			scrollTo(0, (int) (lastY%(itemHeight*(mPicRowLayout.getChildCount()-1 - 2*mFooter))));
		}
		
		return (int) ((idx - mFooter) * itemHeight - lastY);
	}
	
	/**
	 * 
	* method Name:getItemHeight    
	* method Description:  
	* @return   
	* int  
	* @exception   
	* @since  1.0.0
	 */
	public int getItemHeight()
	{
		return itemHeight;
	}

   	
	private Context  mContext;
	private int mPicSize;
	/**
	 * --------------
	 * WINDOW_FOOTER
	 * --------------
	 * 
	 *      Pic
	 * 
	 * --------------
	 * WINDOW_FOOTER
	 * --------------
	 */
	public float mFooter;
	public static final float WINDOW_FOOTER = 0.3f;
	
	//mPicSize MUST >= 1 + 2*WINDOW_FOOTER!!!	
	
	/**
	 * ----------------
	 * 
	 * 
	 * STOP_AHEAD_MULTE
	 * --------------
	 * Pic
	 * 
	 *  -   -  -  -  -
	 * slowly move
	 * ---------------
	 */
	private int  mImgMargin = 0;
	private LinearLayout.LayoutParams mImgLayout;
	private static final float STOP_AHEAD_MULTE = 0.5f;
	private static final float SCROLL_SPEED_MULTE = 1.0f/16;
	private LinearLayout mPicRowLayout;
	private int itemHeight;//ÿһ�еĸ߶�  
	private int lastY ;
	private boolean bSlowing;
}
