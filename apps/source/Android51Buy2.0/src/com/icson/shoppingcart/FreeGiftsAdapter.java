/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: FreeGiftsAdapter.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: Dec 20, 2013
 * 
 */
package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.ProductHelper;
import com.icson.lib.ui.AlphaTextView;
import com.icson.lib.ui.UiUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FreeGiftsAdapter extends BaseExpandableListAdapter implements ImageLoadListener {
	public static int PRODUCT_UNSELECTED 		= 0;		//商品未选择
	public static int PRODUCT_SELECTED 			= 1;		//商品已选择
	public static int PRODUCT_CANNOT_SELECTED 	= 2;		//商品不能选择
	
	public static int PRODUCT_NO_STOCK 			= 0;		//商品无货
	public static int PRODUCT_HAVE_STOCK 		= 1;		//商品有货
	
	private ArrayList<PromoRuleModel> 	mRuleModels;
	private BaseActivity 				mActivity;
	private LayoutInflater 				mInflater;
	private ImageLoader 				mImageLoader;
	private OnChooseGiftListener		mOnChooseListener;
	private int 						margin_20xp;
	
	public FreeGiftsAdapter(BaseActivity activity, ArrayList<PromoRuleModel> models) {
		mActivity = activity;
		mRuleModels = models;
		mInflater = LayoutInflater.from(mActivity);
		String str_20xp = mActivity.getResources().getString(R.dimen.margin_size_20xp);
		margin_20xp = (int)(mActivity.getResources().getDisplayMetrics().density*
				Float.valueOf(str_20xp.substring(0, str_20xp.length()-2)));
	}

	/*
	 * Set listener
	 */
	public void setOnChooseGiftListener(OnChooseGiftListener listener){
		mOnChooseListener = listener;
		
	}
	
	@Override
	public int getGroupCount() {
		return null == mRuleModels ? 0 : mRuleModels.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(null == mRuleModels || groupPosition < 0 || groupPosition > mRuleModels.size()) {
			return 0;
		}
		
		ArrayList<ProductOfPromoRuleModel> products = mRuleModels.get(groupPosition).getProducts();
		if(null == products) {
			return 0;
		}
		return products.size();
	}

	@Override
	public PromoRuleModel getGroup(int groupPosition) {
		if(null == mRuleModels || groupPosition < 0 || groupPosition >= mRuleModels.size()) {
			return null;
		}
		
		return mRuleModels.get(groupPosition);
	}

	@Override
	public ProductOfPromoRuleModel getChild(int groupPosition, int childPosition) {
		
		PromoRuleModel promoModel = this.getGroup(groupPosition);
		if(null == promoModel){
			return null;
		}
		
		ArrayList<ProductOfPromoRuleModel> products = promoModel.getProducts();
		if(null == products || childPosition < 0 || childPosition >= products.size()) {
			return null;
		}
		
		
		return products.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ItemGroupHolder holder = null ;
		if(null == convertView) {
			convertView = mInflater.inflate(R.layout.adapter_freegifts_parent, null);
			holder = new ItemGroupHolder();
			holder.pLayout = (RelativeLayout) convertView.findViewById(R.id.freegifts_dapter_title);
			holder.pPromoName = (TextView) convertView.findViewById(R.id.freegifts_dapter_promo_name);
			holder.pChooseView = (TextView) convertView.findViewById(R.id.freegifts_dapter_choose);
			convertView.setTag(holder);
		}else{
			holder = (ItemGroupHolder) convertView.getTag();
		}
		
		int nPaddingLeft = holder.pLayout.getPaddingLeft();
		int nPaddingTop = holder.pLayout.getPaddingTop();
		int nPaddingRight = holder.pLayout.getPaddingRight();
		int nPaddingBottom = holder.pLayout.getPaddingBottom();
		if(isExpanded) {
			holder.pLayout.setBackgroundResource(R.drawable.package_up_shape);
		}else{
			holder.pLayout.setBackgroundResource(R.drawable.i_gift_tab_bg);
		}
		holder.pLayout.setPadding(nPaddingLeft, nPaddingTop, nPaddingRight, nPaddingBottom);
		
		Drawable drawable = null;
		if(isExpanded) {
			drawable = mActivity.getResources().getDrawable(R.drawable.freegifts_up);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		}else{
			drawable = mActivity.getResources().getDrawable(R.drawable.freegifts_down);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		}
		holder.pChooseView.setCompoundDrawables(null, null, drawable, null);
		
		
		PromoRuleModel model = this.getGroup(groupPosition);
		if(null != model) {
			holder.pPromoName.setText(model.getDesc());
			
			int nSelectNum = model.getSelectNum();
			int nBenefitType = model.getBenefitType();
			
			if(PromoRuleModel.BENEFIT_TYPE_FREEGIFT == nBenefitType) {
				//满赠商品
				if(0 < nSelectNum) {
					holder.pChooseView.setText("已选择");
				}else{
					holder.pChooseView.setText("");
				}
			}else if(PromoRuleModel.BENEFIT_TYPE_LESSPRICEBUY == nBenefitType){
				//加价购商品
				if(0 < nSelectNum) {
					holder.pChooseView.setText("已选" + nSelectNum + "件");
				}else{
					holder.pChooseView.setText("");
				}
			}
		}
		
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ItemChildHolder holder = null;
		if(null == convertView) {
			holder = new ItemChildHolder();
			convertView = mInflater.inflate(R.layout.adapter_freegifts_child, null);
			holder.pChooseButton = (ImageView) convertView.findViewById(R.id.freegifts_product_checkbox);
			holder.pProductPic = (ImageView) convertView.findViewById(R.id.freegifts_product_pic);
			holder.pProductName = (TextView) convertView.findViewById(R.id.freegifts_product_name);
			holder.pPrice = (AlphaTextView) convertView.findViewById(R.id.freegifts_product_price);
			holder.pPromoPrice = (AlphaTextView) convertView.findViewById(R.id.freegifts_product_promoprice);
			holder.pNum = (TextView) convertView.findViewById(R.id.freegifts_product_num);
			holder.pStatue = (TextView) convertView.findViewById(R.id.freegifts_product_status);
			holder.pAddButton = (TextView) convertView.findViewById(R.id.freegifts_product_add_button);
			holder.pContainer = (RelativeLayout) convertView.findViewById(R.id.freegifts_container);
			convertView.setTag(holder);
		}else{
			holder = (ItemChildHolder) convertView.getTag();
		}
		
		ProductOfPromoRuleModel pProductModel = this.getChild(groupPosition, childPosition);
		final PromoRuleModel pPromoRuleModel = this.getGroup(groupPosition);
		if(null != pProductModel && null != pPromoRuleModel){
			int nSelectStatus = pProductModel.getSelectedStatus();
			int nLocalSelectStatus = pProductModel.getLocalSelectedStatus();
			int nStockStatus = pProductModel.getStockStatus();
			int nBenefitType = pPromoRuleModel.getBenefitType();
			
			//设置背景
			if(childPosition < getChildrenCount(groupPosition) -1) {
				holder.pContainer.setBackgroundResource(R.drawable.package_mid_shape);
				holder.pContainer.setPadding(margin_20xp, 0, margin_20xp, margin_20xp);
			}else{
				holder.pContainer.setBackgroundResource(R.drawable.package_down_shape);
				holder.pContainer.setPadding(margin_20xp, 0, margin_20xp, margin_20xp);
			}
			
			setImage(holder.pProductPic, pProductModel.getProductCharId());
			holder.pProductName.setText(pProductModel.getName());
			
			final long pid = pProductModel.getProductId();
			final long promoRuldId = pPromoRuleModel.getRuleId();
			holder.pAddButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					submitAddCart(pid, promoRuldId);
				}
				
			});
			
			if(PromoRuleModel.BENEFIT_TYPE_FREEGIFT == nBenefitType) {
				//满赠
				holder.pPrice.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(pProductModel.getPrice()));
				holder.pNum.setVisibility(View.VISIBLE);
				holder.pPromoPrice.setVisibility(View.GONE);
				holder.pAddButton.setVisibility(View.GONE);
				holder.pChooseButton.setClickable(true);
				
				if(nSelectStatus == PRODUCT_SELECTED || nLocalSelectStatus == PRODUCT_SELECTED) {
					holder.pChooseButton.setVisibility(View.VISIBLE);
					holder.pChooseButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.choose_radio_on));
					holder.pChooseButton.setOnClickListener(null);
					holder.pStatue.setVisibility(View.GONE);
				}else if(nSelectStatus == PRODUCT_UNSELECTED){
					holder.pChooseButton.setVisibility(View.VISIBLE);
					holder.pChooseButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.choose_radio_off));
					holder.pChooseButton.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							chooseGift(groupPosition, childPosition);
							notifyDataSetChanged();
						}
					});
					holder.pStatue.setVisibility(View.GONE);
				}else if(nSelectStatus == PRODUCT_CANNOT_SELECTED){
					holder.pChooseButton.setVisibility(View.INVISIBLE);
					holder.pChooseButton.setOnClickListener(null);
					holder.pStatue.setVisibility(View.VISIBLE);
					holder.pStatue.setText("不可选");
				}
				
				if(nStockStatus == PRODUCT_NO_STOCK) {
					holder.pChooseButton.setVisibility(View.INVISIBLE);
					holder.pStatue.setVisibility(View.VISIBLE);
					holder.pStatue.setText("已领完");
				}
				
				
			}else if(PromoRuleModel.BENEFIT_TYPE_LESSPRICEBUY == nBenefitType){
				//加价购
				holder.pChooseButton.setVisibility(View.GONE);
				holder.pNum.setVisibility(View.GONE);
				
				if(pProductModel.getPromoPrice() > 0 && pProductModel.getPromoPrice() < pProductModel.getPrice()) {
					holder.pPrice.setVisibility(View.VISIBLE);
					holder.pPromoPrice.setVisibility(View.VISIBLE);
					holder.pPrice.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(pProductModel.getPromoPrice()));
					holder.pPromoPrice.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(pProductModel.getPrice()));
					holder.pPromoPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
				}else{
					holder.pPrice.setVisibility(View.VISIBLE);
					holder.pPromoPrice.setVisibility(View.GONE);
					holder.pPrice.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(pProductModel.getPrice()));
				}
				
				if(nSelectStatus == PRODUCT_SELECTED) {
					holder.pAddButton.setVisibility(View.GONE);
					holder.pStatue.setVisibility(View.VISIBLE);
					holder.pStatue.setText("已换购");
				}else if(nSelectStatus == PRODUCT_UNSELECTED){
					holder.pAddButton.setVisibility(View.VISIBLE);
					holder.pStatue.setVisibility(View.GONE);
				}else if(nSelectStatus == PRODUCT_CANNOT_SELECTED){
					holder.pAddButton.setVisibility(View.GONE);
					holder.pStatue.setVisibility(View.VISIBLE);
					holder.pStatue.setText("不可选");
				}
				
				if(nStockStatus == PRODUCT_NO_STOCK) {
					holder.pAddButton.setVisibility(View.GONE);
					holder.pStatue.setVisibility(View.VISIBLE);
					holder.pStatue.setText("已领完");
				}
			}
		}
		
		return convertView;
	}
	
	
	private void setImage(ImageView aView, String strCharId) {
		if(null == mImageLoader) {
			mImageLoader = new ImageLoader(mActivity, Config.CHANNEL_PIC_DIR, true);
		}
		String url = ProductHelper.getAdapterPicUrl(strCharId, 110);
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			aView.setImageBitmap(data);
			return;
		}
		
		aView.setImageBitmap(mImageLoader.getLoadingBitmap(mActivity));
		mImageLoader.get(url, this);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		PromoRuleModel pPromoModel = this.getGroup(groupPosition);
		ProductOfPromoRuleModel pProductModel = this.getChild(groupPosition, childPosition);
		//The conditions of item clickable:
		//1. product is FreeGift product
		//2. select status of product is PRODUCT_UNSELECTED
		//3. stock status of product is PRODUCT_HAVE_STOCK
		if(null != pPromoModel && null != pProductModel) {
			if(PromoRuleModel.BENEFIT_TYPE_FREEGIFT == pPromoModel.getBenefitType() && PRODUCT_UNSELECTED == pProductModel.getSelectedStatus() && PRODUCT_HAVE_STOCK == pProductModel.getStockStatus()) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		this.notifyDataSetChanged();
	}

	@Override
	public void onError(String strUrl) {
		
	}
	
	public void chooseGift(int groupPosition, int childPosition) {
		PromoRuleModel pPromoModel = getGroup(groupPosition);
		ProductOfPromoRuleModel pProductModel = getChild(groupPosition, childPosition);
		if(null != pPromoModel && null != pProductModel) {
			if(FreeGiftsAdapter.PRODUCT_UNSELECTED == pProductModel.getLocalSelectedStatus()) {
				ArrayList<ProductOfPromoRuleModel> pProductsModels = pPromoModel.getProducts();
				for(ProductOfPromoRuleModel model : pProductsModels) {
					if( FreeGiftsAdapter.PRODUCT_SELECTED == model.getLocalSelectedStatus()) {
						model.setLocalSelectedStatus(FreeGiftsAdapter.PRODUCT_UNSELECTED);
					}
				}
				pProductModel.setLocalSelectedStatus(FreeGiftsAdapter.PRODUCT_SELECTED);
			}else if(FreeGiftsAdapter.PRODUCT_SELECTED == pProductModel.getLocalSelectedStatus()){
				pProductModel.setLocalSelectedStatus(FreeGiftsAdapter.PRODUCT_UNSELECTED);
			}
			
			if(null != mOnChooseListener) {
				mOnChooseListener.onChooseListener();
			}
		}
	}
	
	/*
	 * submit one LessPriceBuy product
	 */
	private void submitAddCart(final long pid, long promoRuleId ) {
		if(0 == pid) {
			return;
		}
		
//		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_ADD_PRODUCTS);
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/addpromotionproduct?mod=cart");
		if (null == ajax)
			return;
		// 商品id|数量|主商品id|多价格id|购买路径|商品类型:0普通1套餐2加价购3满赠|场景id
		String strIDS = pid + "|1|0|0|" + IcsonApplication.getPageRoute() + "|2|0|" + promoRuleId;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("district", FullDistrictHelper.getDistrictId());
		data.put("uid", ILogin.getLoginUid());
		data.put("ids",strIDS);
		ajax.setData(data);
		ajax.setParser(new JSONParser());
		ajax.setOnErrorListener(mActivity);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					UiUtils.makeToast(mActivity, mActivity.getString(R.string.add_cart_ok), true);
					if(null != mRuleModels) {
						for(PromoRuleModel model : mRuleModels) {
							ArrayList<ProductOfPromoRuleModel> productModels = model.getProducts();
							for(ProductOfPromoRuleModel productModel : productModels) {
								if(productModel.getProductId() == pid) {
									productModel.setSelectedStatus(PRODUCT_SELECTED);
									notifyDataSetChanged();
									break;
								}
							}
						}
					}
				} else {
					String strErrMsg = v.optString("data", mActivity.getString(R.string.add_cart_error));
					UiUtils.makeToast(mActivity, strErrMsg);
				}
			}
		});
		
		mActivity.showProgressLayer();
		ajax.send();
	}

	
	/*
	 * Interface used to update number chosen when user chooses product
	 */
	public interface OnChooseGiftListener{
		public void onChooseListener();
	}
	
	private class ItemGroupHolder {
		RelativeLayout pLayout;
		TextView pPromoName;
		TextView pChooseView;
	}
	
	private class ItemChildHolder {
		ImageView pChooseButton;
		ImageView pProductPic;
		TextView pProductName;
		AlphaTextView pPrice;
		AlphaTextView pPromoPrice;
		TextView pNum;
		TextView pStatue;
		TextView pAddButton;
		RelativeLayout	pContainer;
	}

}
