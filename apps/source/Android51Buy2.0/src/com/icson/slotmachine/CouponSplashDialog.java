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

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.AppUtils;

/**
 * 
*   
* Class Name:CouponSplashActivity
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-4 下午02:29:23 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class CouponSplashDialog extends BingoSplashDialog{

	public CouponSplashDialog(Context context, OnDialogClickListener aListener,
			int contentViewResource) {
		super(context, aListener, contentViewResource);
	}


	public void setBundle(Bundle pExtras) {
		if(null== pExtras)
			return;
		
		mBingoType = pExtras.getInt("bingo_type", 0);
		if(mBingoType > BingoInfo.MAX_BINGO_TYPE || mBingoType <=0)
		{
			return;
		}
		
		bShareGetReward = false;
		mExBingoType = pExtras.getInt("ex_bingo_type", -1);
		from_coin_reward = pExtras.getBoolean("from_coin_reward", false);
		
		mBingoName = pExtras.getString("bingo_name");
		mBingoDetail = pExtras.getString("bingo_detail");
		mShareContentText = pExtras.getString("share_content");
		mShareTitleText = pExtras.getString("share_title");
		mSharePageUrl = pExtras.getString("link_url");
		if(TextUtils.isEmpty(mShareContentText))
			mShareContentText = mActivity.getString(R.string.wx_share_default_content);
		if(TextUtils.isEmpty(mShareTitleText))
			mShareTitleText = mActivity.getString(R.string.wx_share_default_title);
		if(TextUtils.isEmpty(mSharePageUrl))
			mSharePageUrl= "http://m.51buy.com/";
		
		bingoDetailView = (TextView)this.findViewById(R.id.bingo_info_v);
		
		shareRewardHint = (TextView)this.findViewById(R.id.tips_title_tv2);
		shareRewardTitle = (TextView)this.findViewById(R.id.tips_title_tv);
		
		cardImgView = (ImageView)this.findViewById(R.id.card_img);
		cardNameView = (TextView)this.findViewById(R.id.card_name);
		
		super.setBundle(pExtras);
		
		
		init_couponView();
	}
	
	
	/**  
	* method Name:init_couponImg    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void init_couponView() {
		
		if(null!=bingoDetailView)
			bingoDetailView.setText(mBingoDetail);
		
		switch (mBingoType)
		{
		case BingoInfo.BINGO_CDKEY:
			cardImgView.setImageResource(R.drawable.card_bg);
			cardNameView.setText(mBingoName);
			wxShareImgRid = R.drawable.wx_cdkey;
			break;
		case BingoInfo.BINGO_COUPON:
			cardImgView.setImageResource(R.drawable.coupon_bg);
			cardNameView.setText(mBingoName);
			wxShareImgRid = R.drawable.wx_coupon;
			break;
		case BingoInfo.BINGO_COIN:
			shareRewardHint.setText(mActivity.getString(R.string.coin_inspire));
			if(mExBingoType > 0)
			{
				cardImgView.setImageResource(R.drawable.coupon_bg);
				cardNameView.setText(mBingoName);
				wxShareImgRid = R.drawable.wx_coupon;
			}
			else
			{
				cardImgView.setImageResource(R.drawable.drop_gold);
					cardNameView.setVisibility(View.INVISIBLE);
					wxShareImgRid = R.drawable.wx_coin;
			}
			
			break;
		default:
			cardImgView.setImageResource(R.drawable.i_global_image_none);
			wxShareImgRid = R.drawable.wx_coupon;
			break;
		}
		
		if(from_coin_reward)
		{
			shareRewardTitle.setText(mActivity.getString(R.string.reward_from_coin_title));
		}
	}
	
	
	protected void callShareWx() {
		if(AppUtils.checkWX(mActivity))
		{
			AppUtils.shareSlotInfo(mActivity, 
					mShareTitleText, mSharePageUrl, wxShareImgRid, this);
		}
	}
	
	protected int       mBingoType;
	private ImageView cardImgView;
	private TextView  cardNameView;
	private TextView  bingoDetailView;
	private int       wxShareImgRid;
	
	private int       mExBingoType;
	private String    mBingoName;
	private String    mBingoDetail;
	private String    mShareTitleText;
	private String    mSharePageUrl;
	
	private TextView    shareRewardHint;
	private TextView    shareRewardTitle;
	
	private boolean from_coin_reward;
	protected boolean bShareGetReward;
	
}
