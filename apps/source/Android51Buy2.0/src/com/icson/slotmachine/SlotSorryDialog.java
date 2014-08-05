/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: SosoNovel
 * FileName: AppDialog.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: Jul 11, 2012
 */
package com.icson.slotmachine;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.icson.R;

/**
 * @author xingyao
 */
public class SlotSorryDialog extends Dialog implements View.OnClickListener
{
	/**
	 * interface for OnButtonClickListener
	 * @author xingyao
	 *
	 */
	public interface OnClickListener
	{
		/**
		 * onDialogClick
		 * @param nButtonId
		 */
		public abstract void onDialogClick(int nButtonId);
	}
	
	/**
	 * Create a new instance of AppDialog
	 * @param aContext
	 */
	public SlotSorryDialog(Context aContext, OnClickListener aListener)
	{
		super(aContext, R.style.Dialog);
		mListener = aListener;
	}
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			mListener.onDialogClick(BUTTON_NEGATIVE);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * setProperty
	 * @param strCaption
	 * @param strMessage
	 * @param nPostiveBtnTextId
	 */
	public void setProperty(String strMessage, int nPostiveBtnTextId)
	{
		setProperty(strMessage, nPostiveBtnTextId, 0);
	}
	
	/**
	 * setProperty
	 * @param strCaption
	 * @param strMessage
	 * @param nPostiveBtnTextId
	 * @param nNegativeBtnTextId
	 */
	public void setProperty(String strMessage, int nPostiveBtnTextId, int nNegativeBtnTextId)
	{
		Context pContext = getContext();
		
		setProperty(strMessage,  (nPostiveBtnTextId > 0 ? pContext.getString(nPostiveBtnTextId) : ""), (nNegativeBtnTextId > 0 ? pContext.getString(nNegativeBtnTextId) : ""));
	}
	
	/**
	 * @param strCaption
	 * @param strMessage
	 * @param strPostiveBtnText
	 * @param strNegativeBtnText
	 */
	public void setProperty(String strMessage, String strPostiveBtnText, String strNegativeBtnText) {
		//(mCaption = getComponent(mCaption)).mText = strCaption;
		(mMessage = getComponent(mMessage)).mText = strMessage;
		
		(mPositive = getComponent(mPositive)).mText = strPostiveBtnText;
		(mNegative = getComponent(mNegative)).mText = strNegativeBtnText;
		
	}
	
	/**
	 * setProperty
	 * @param nCaptionId
	 * @param nMessageId
	 * @param nPostiveBtnTextId
	 * @param nNegativeBtnTextId
	 */
	public void setProperty(int nMessageId, int nPostiveBtnTextId, int nNegativeBtnTextId)
	{
		Context pContext = getContext();
		String strMessage = pContext.getString(nMessageId);
		
		setProperty(strMessage, nPostiveBtnTextId, nNegativeBtnTextId);
		strMessage = null;
	}
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		
		// Load the default configuration.
		setContentView(R.layout.dialog_slot_sorry);
		
		//(mCaption = getComponent(mCaption)).mView = (TextView)findViewById(R.id.dialog_caption);
		(mMessage = getComponent(mMessage)).mView = (TextView)findViewById(R.id.dialog_message);
		
		(mPositive = getComponent(mPositive)).mView = (TextView)findViewById(R.id.dialog_btn_positive);
		mPositive.mView.setOnClickListener(this);
		
		(mNegative = getComponent(mNegative)).mView = (TextView)findViewById(R.id.dialog_btn_negative);
		mNegative.mView.setOnClickListener(this);
		
		// Set UI configuration.
		boolean bVisible = (null != mNegative.mText && mNegative.mText.length() > 0);
		mNegative.mView.setVisibility(bVisible ? View.VISIBLE : View.GONE);
		
		bVisible = (null != mPositive.mText && mPositive.mText.length() > 0);
		mPositive.mView.setVisibility(bVisible ? View.VISIBLE : View.GONE);
		
		// Update ui configuration.
		this.updateUi();
	}
	
	/**
	 * onStart
	 */
	@Override
	protected void onStart()
	{
		// Call the base implementation.
		super.onStart();
		
		TextView pMessage = (null != mMessage ? mMessage.mView : null);
		if ( null != pMessage )
		{
			// Measure the text length.
			TextPaint pPaint = pMessage.getPaint();
			final int nWidth = (null != mMessage.mText && mMessage.mText.length() > 0) ? (int)(pPaint.measureText(mMessage.mText)) : 0;
			pPaint = null;
			
			final int nMsgWidth = mWinWidth - (10 << 1); // 10 pixels in both sides.
			pMessage.setGravity(nMsgWidth > nWidth ? Gravity.CENTER_HORIZONTAL : Gravity.LEFT);
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
				mListener.onDialogClick(BUTTON_POSITIVE);
				break;
				
			case R.id.dialog_btn_negative:
				mListener.onDialogClick(BUTTON_NEGATIVE);
				break;
				
			default:
				break;
			}
		}
		
		// Dismiss the dialog.
		dismiss();
	}
	
	/**
	 * updateUi
	 * Update the UI configuration.
	 */
	protected void updateUi()
	{
		// Update the text.
		Component aComponents[] = {mMessage, mPositive, mNegative};
		for ( int nIdx = 0; nIdx < aComponents.length; nIdx++ )
		{
			Component pComponent = aComponents[nIdx];
			pComponent.mView.setText(pComponent.mText);
		}
		
		mWinWidth = this.setAttributes();
		
		// Update the width for buttons.
		this.initButtons(mWinWidth);
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
		pParams.width = (int) (pMetrics.widthPixels * 0.9);
		pWindow.setAttributes(pParams);
		
		// Clean up.
		pMetrics = null;
		pWindow = null;
		
		return pParams.width;
	}
	
	/**
	 * initButtons
	 * @param nWidth
	 */
	protected void initButtons(int nWidth)
	{
		if ( 0 >= nWidth )
			return ;
		
		final int nButtonWidth = (nWidth * 2 / 5);
		(mPositive = getComponent(mPositive)).mView.setWidth(nButtonWidth);
		(mNegative = getComponent(mNegative)).mView.setWidth(nButtonWidth);
	}
	
	/**
	 * getComponent
	 * @param aComponent
	 * @return
	 */
	protected Component getComponent(Component aComponent)
	{
		return (null != aComponent ? aComponent : new Component());
	}
	
	/**
	 * Component
	 * @author xingyao
	 */
	class Component
	{
		/**
		 * Default constructor of Component
		 */
		public Component()
		{
			mView = null;
			mText = null;
		}
		
		public void setColor(int nTextColor)
		{
			if ( null != mView )
			{
				mView.setTextColor(nTextColor);
			}
		}
		
		public TextView mView;
		public String   mText;
	}
	
	// Member instances.
	protected int            mWinWidth;
	//protected Component      mCaption;
	protected Component      mMessage;
	protected Component      mPositive;
	protected Component      mNegative;
	protected OnClickListener  mListener;
	
	// Constant members.
	public static final int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
	  
	  // Field descriptor #17 I
	public static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;
	  
	  // Field descriptor #17 I
	public static final int BUTTON_NEUTRAL = DialogInterface.BUTTON_NEUTRAL;
}
