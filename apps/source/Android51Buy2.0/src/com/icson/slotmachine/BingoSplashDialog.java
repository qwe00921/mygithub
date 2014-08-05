/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: SlotMachineActivity.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-6-3
 */
package com.icson.slotmachine;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.AppUtils.DescProvider;
import com.icson.util.activity.BaseActivity;

/**
 * 
 * @author xingyao
 *
 */
public abstract class BingoSplashDialog extends Dialog implements DescProvider, android.view.View.OnClickListener {

	public interface OnDialogClickListener
	{
		/**
		 * onDialogClick
		 * @param nButtonId
		 */
		public abstract void onDialogDismiss();
	}
	
	/**
	 * 
	 * @param context
	 */
	public BingoSplashDialog(Context context,OnDialogClickListener aListener, int contentViewResource) {
		super(context, R.style.BingoDialog);
		
		//Window win = this.getWindow();
		//LayoutParams params = new LayoutParams();
		//params.y = -100;
		//params.dimAmount = 0.8f;
		//win.setAttributes(params);
		
		mActivity = (BaseActivity) context;
		mListener = aListener;
		setContentView(contentViewResource);
	}
	

	@Override
	public String getDesc(String strPackageName) {
		return mShareContentText;
	}

	public void setBundle(Bundle pExtras) {
		WxShareBtn = (TextView)this.findViewById(R.id.share_btn);
		WxShareBtn.setOnClickListener(this);

		mCloseBtn = (ImageView)this.findViewById(R.id.close_share_v);
		mCloseBtn.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					if(null != mListener)
						mListener.onDialogDismiss();
					dismiss();
				}
				return true;
			
			}});
	
				
	}
	
	
	

	@Override
	public void onClick(View v)
	{
		if(v==this.WxShareBtn)
			callShareWx();
	}
	
	protected abstract void callShareWx();
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) 
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(null != mListener)
				mListener.onDialogDismiss();
			dismiss();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected TextView WxShareBtn;
	protected ImageView mCloseBtn;
	protected String   mShareContentText;
	
	protected OnDialogClickListener  mListener;
	protected BaseActivity  mActivity;
}
