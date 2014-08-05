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
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.ui.AlphaTextView;
import com.icson.lib.ui.UiUtils;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

/**
 * 
*   
* Class Name:ProductSplashActivity 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-4 下午02:29:23 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class ProductSplashDialog extends BingoSplashDialog implements ImageLoadListener{

	public ProductSplashDialog(Context context,
			OnDialogClickListener aListener, int contentViewResource) {
		super(context, aListener, contentViewResource);
	}


	private ImageView cardImg;
	private Bitmap    productImg;
	private ImageLoader mImgLoader;
	private String    mShareTitleText;
	private String    mSharePageUrl;
	private String    mProCharId;
	
	private long      productId;
	private long      mOriPrice;
	private long      mCurPrice;
	private String    mProImgUrl;
	//private long      mExpireTime;
	private String    mBingoDetail;
    private String    mChannelId;
	
	private AlphaTextView mOriPirceView;
	private AlphaTextView mCurPirceView;
	private TextView mDeltaPirceView;
	
	//private TextView mExpireView;
	private TextView mDetailView;
	
	private TextView mAdd2CartText;
	private ImageView mAdd2CartChecked;
	private LinearLayout mAdd2CartLayout;
	
	//private TextView    shareRewardHint;
	
	public void setBundle(Bundle pExtras) {
		if(null== pExtras)
			return;
		
		productId = pExtras.getLong("proid", 0);
		mOriPrice = pExtras.getLong("ori_price", -1);
		mCurPrice = pExtras.getLong("cur_price", -1);
		mProImgUrl = pExtras.getString("img_url");
		mBingoDetail = pExtras.getString("bingo_detail");
		//mExpireTime = aIt.getLongExtra("expire", 0);
		mShareContentText = pExtras.getString("share_content");
		mShareTitleText = pExtras.getString("share_title");
		mSharePageUrl = pExtras.getString("link_url");
		mChannelId = pExtras.getString("channel_id");
		mProCharId = pExtras.getString("char_id");
		if(!TextUtils.isEmpty(mProCharId))
		{
			mProImgUrl = IcsonProImgHelper.getAdapterPicUrl(mProCharId, 110);
		}
		
		if(TextUtils.isEmpty(mShareContentText))
			mShareContentText = mActivity.getString(R.string.wx_share_default_content);
		if(TextUtils.isEmpty(mShareTitleText))
			mShareTitleText = mActivity.getString(R.string.wx_share_default_title);
		if(TextUtils.isEmpty(mSharePageUrl))
			mSharePageUrl= "http://m.51buy.com/";
		
		
		cardImg = (ImageView)this.findViewById(R.id.card_img);
		//cardImg.setOnClickListener(this);
		
		mOriPirceView = (AlphaTextView)this.findViewById(R.id.ori_price);
		mOriPirceView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		mCurPirceView = (AlphaTextView)this.findViewById(R.id.icson_price);
		mDeltaPirceView = (TextView)this.findViewById(R.id.price_delta);
		
		//mExpireView = (TextView)this.findViewById(R.id.expire_info_v);
		mDetailView = (TextView)this.findViewById(R.id.bingo_info_v);
		
		mAdd2CartLayout = (LinearLayout) this.findViewById(R.id.add_tocart);
		mAdd2CartText = (TextView) this.findViewById(R.id.add_tocart_text);
		mAdd2CartChecked = (ImageView) this.findViewById(R.id.add_tocart_check);
		mAdd2CartLayout.setOnClickListener(this);
		
		super.setBundle(pExtras);
		
		initViews();
		
	}
	
	public void destroy()
	{
		if (null != mImgLoader) {
			mImgLoader.cleanup();
			mImgLoader = null;
		}
	}
	
	/**  
	* method Name:init_couponImg    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void initViews() {
		if(null == mImgLoader)
			mImgLoader = new ImageLoader(mActivity,Config.PIC_CACHE_DIR,true);
		
		productImg = mImgLoader.get(mProImgUrl);
		if(null==productImg)
		{
			cardImg.setImageResource(R.drawable.i_global_image_default);
			mImgLoader.get(mProImgUrl,this);
		}
		else
			cardImg.setImageBitmap(productImg);
		
		//detail
		mDetailView.setText(mBingoDetail);
		
		//prices
		if(mCurPrice >0)
		{
			mCurPirceView.setVisibility(View.VISIBLE);
			mCurPirceView.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(mCurPrice, 2));
		}
		else
			mCurPirceView.setVisibility(View.GONE);
		
		if(mOriPrice >=0 && mOriPrice > mCurPrice)
		{
			mOriPirceView.setVisibility(View.VISIBLE);
			mOriPirceView.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(mOriPrice, 2));
		}
		else
			mOriPirceView.setVisibility(View.GONE);
		
		
		if(mCurPrice >= mOriPrice || mCurPrice<=0 || mOriPrice<=0)
		{
			mDeltaPirceView.setVisibility(View.INVISIBLE);
		}
		else
		{
			mDeltaPirceView.setText(mActivity.getString(R.string.slot_price_delta,ToolUtil.toDiscount(mCurPrice*100.00/mOriPrice)));
		}
		
//		Date pData = new Date(mExpireTime*1000);
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		//String strTime = format.format(pData);
		
		//info
		//mExpireView.setText(this.getString(R.string.slot_share_pro_expire,strTime));
		
		
	}
	
	/**
	 * 
	* method Name:addProductToShoppingCart    
	* method Description:  
	* @param aProId
	* @param aBuynum
	* @param aChannelid   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void addProductToShoppingCart(long aProId, int aBuynum, String strChannelid) {
		StatisticsEngine.trackEvent(mActivity, "slot_add2cart");
		final long uid = ILogin.getLoginUid();
		// 添加购物车标识出来场景id
		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_ADD_PRODUCTS);
		if (null == ajax)
			return;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("district", FullDistrictHelper.getDistrictId());
		data.put("uid", uid);
		data.put("chid", strChannelid);
		// 商品id|数量|主商品id|多价格id|购买路径|商品类型0普通1套餐|场景id
		data.put(
				"ids",""+ aProId + "|" + aBuynum + "|"
						+ aProId + "|0|"
						+ IcsonApplication.getPageRoute() + "|0|"
						+ strChannelid);
		ajax.setData(data);
		ajax.setOnErrorListener(mActivity);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					UiUtils.makeToast(mActivity, R.string.add_cart_ok);
					updateAddBtn();
				} else {
					String strErrMsg = v.optString("data");
					if (TextUtils.isEmpty(strErrMsg))
						strErrMsg = mActivity.getString(R.string.add_cart_error);
					UiUtils.makeToast(mActivity, strErrMsg);
				}
			}
		});
		
		mActivity.addAjax(ajax);
		mActivity.showProgressLayer();
		ajax.send();

	}
	
	/**  
	* method Name:updateAddBtn    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void updateAddBtn() {
		mAdd2CartLayout.setClickable(false);
		mAdd2CartText.setText(mActivity.getString(R.string.add_cart_done));
		//mAdd2CartText.setTextColor(getResources().getColor(R.color.clicked_brown));
		mAdd2CartChecked.setVisibility(View.VISIBLE);
	}

	/**
	 * 
	* method Name:shareCouponInWx    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	protected void callShareWx() {
		if(AppUtils.checkWX(mActivity))
		{
			AppUtils.shareSlotInfo(mActivity, 
					mShareTitleText, mSharePageUrl, mProImgUrl, this);
		}
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onLoaded(android.graphics.Bitmap, java.lang.String)
	 */
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		cardImg.setImageBitmap(aBitmap);
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onError(java.lang.String)
	 */
	@Override
	public void onError(String strUrl) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onClick(View v)
	{
		if(v==cardImg)
		{
			Bundle param = new Bundle();
			param.putLong(ItemActivity.REQUEST_PRODUCT_ID, productId);
			UiUtils.startActivity(mActivity, ItemActivity.class, param,true);
			if ((!TextUtils.isEmpty(mChannelId))
					&& (TextUtils.isDigitsOnly(mChannelId))) {
				param.putInt(ItemActivity.REQUEST_CHANNEL_ID,
						Integer.valueOf(mChannelId));
			}
		}
		else if(v == mAdd2CartLayout)
		{
			ToolUtil.sendTrack(
				this.getClass().getName(),
				mActivity.getString(R.string.tag_SlotMachineActivity),
				SlotMachineActivity.class.getName(),
				mActivity.getString(R.string.tag_SlotMachineActivity),
				"04011");
			addProductToShoppingCart(productId,1,mChannelId);
		}
		else
			super.onClick(v);
	}
	
}
