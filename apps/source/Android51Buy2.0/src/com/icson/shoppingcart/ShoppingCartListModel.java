package com.icson.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;

import com.icson.home.ProvinceModel;
import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ShoppingCartProductModel;

@SuppressWarnings("serial")
public class ShoppingCartListModel extends BaseModel implements Serializable{
	private boolean   spliTips = false;//�ֵ���ʾ
	private ArrayList<ShoppingCartProductModel>  mShoppingCartProductModels;
	private ArrayList<ShoppingCartProductModel>  mShoppingCartProduct_ErrorItemsModels;

	private ArrayList<PromoApplyRuleModel> mPromoApplyRuleModels;
	private ArrayList<PromoBuyMoreRuleModel> mPromoBuyMoreRuleModels;
	private ArrayList<ProductCouponGiftModel> mProductCouponGiftModels;
	private ProvinceModel mProvinceModel;
	private ArrayList<PromoApplyRuleModel>	mFreeGiftsRules;		//满赠促销规则
	private ArrayList<PromoApplyRuleModel>  mLessPriceBuyRules;		//加价购促销规
	
	public void setFreeGiftsRulesModels(ArrayList<PromoApplyRuleModel> models) {
		this.mFreeGiftsRules = models;
	}
	
	public ArrayList<PromoApplyRuleModel> getFreeGiftRulesModels(){
		return mFreeGiftsRules;
	}
	
	public void setLessPriceBuyRulesModels(ArrayList<PromoApplyRuleModel> models) {
		this.mLessPriceBuyRules = models;
	}
	
	public ArrayList<PromoApplyRuleModel> getLessPriceBuyRulesModels(){
		return mLessPriceBuyRules;
	}
	
	public void setShoppingCartProductModels(ArrayList<ShoppingCartProductModel> models){
		mShoppingCartProductModels = models;
	}
	
	public ArrayList<ShoppingCartProductModel> getShoppingCartProductModels(){
		return mShoppingCartProductModels;
	}
	
	public void setShoppingCartProduct_ErrorItemsModels(ArrayList<ShoppingCartProductModel> models){
		mShoppingCartProduct_ErrorItemsModels = models;
	}
	
	public ArrayList<ShoppingCartProductModel> getShoppingCartProduct_ErrorItemsModels(){
		return mShoppingCartProduct_ErrorItemsModels;
	}
	public void setPromoApplyRuleModels(ArrayList<PromoApplyRuleModel> models){
		mPromoApplyRuleModels = models;
	}
	
	public ArrayList<PromoApplyRuleModel> getPromoApplyRuleModels(){
		return mPromoApplyRuleModels;
	}
	
	public void setPromoBuyMoreRuleModels(ArrayList<PromoBuyMoreRuleModel> models){
		mPromoBuyMoreRuleModels = models;
	}
	
	public ArrayList<PromoBuyMoreRuleModel> getPromoBuyMoreRuleModels(){
		return mPromoBuyMoreRuleModels;
	}

	public void setSpliTips(boolean bTips) {
		this.spliTips = bTips;
	}

	public boolean isSpliTips() {
		return spliTips;
	}
	
	
	public void setProductCouponGiftModels(ArrayList<ProductCouponGiftModel> models) {
		this.mProductCouponGiftModels = models;
	}
	
	public ArrayList<ProductCouponGiftModel> getProductCouponGiftModels() {
		return this.mProductCouponGiftModels;
	}
	
	public ProvinceModel getFullDistrictModel(){
		return this.mProvinceModel;
	}	
	
	public void setFullDistrictModel(ProvinceModel model){
		this.mProvinceModel = model;
	}
	
}


