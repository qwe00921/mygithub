package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.item.ItemActivity;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.IShoppingCart;
import com.icson.lib.FullDistrictHelper.FullDistrictItem;
import com.icson.lib.control.FavorControl;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.AddressRadioDialog;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.CheckBox;
import com.icson.lib.ui.CheckBox.OnCheckedChangeListener;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.order.OrderConfirmActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ShoppingCartView implements OnSuccessListener<JSONObject>, OnItemClickListener, OnClickListener {
	private static final String LOG_TAG = ShoppingCartView.class.getName();
	private static final int REQUEST_FLAG_CARTLIST_NOT_LOGIN 	= 1;
	private static final int REQUEST_FLAG_ACCOUNT 				= 2;
	private static final int REQUEST_REMOVE_PRODUCT_ID 			= 3;
	private static final int REQUEST_SET_BUYCOUNT 				= 4;
	
	private ArrayList<ShoppingCartProductModel> 		mShoppingCartProductModels;		//正常商品列表
	private ArrayList<ShoppingCartProductModel> 		mShoppingCartErrorProductModels;//异常商品列表
	private ArrayList<ShoppingCartProductModel> 		mNoPromoProductModels;			//除满赠加价购之外的商品列表
	private ArrayList<ShoppingCartProductModel> 		mAdpaterProductModels;			
	private ShoppingCartListModel 						mShoppingCartListModel;
	private OnSuccessListener<ShoppingCartListModel> 	success;
	private ArrayList<PromoApplyRuleModel> 				mPromoApplyRules;	//一般促销规则列表
	private ArrayList<PromoApplyRuleModel> 				mFreeGiftRules;		//满赠促销规则列表
	private ArrayList<PromoApplyRuleModel> 				mLessPriceBuyRules;	//加价购促销规则列表
	private ArrayList<PromoBuyMoreRuleModel> 			mPromoBuyMoreRules;
	private ShoppingCartControl 	mShoppingCartControl;
	private ShoppingCartActivity 	mActivity;
	private ShoppingCartAdapter 	mShoppingCartAdapter;
	private ShoppingCartAdapter 	mShoppingCartErrorItemsAdapter;

	private LinearListView 			mItemsListView;   	 	//商品
	private LinearListView 			mErrorItemsListView;	//异常商品

	private BaseAdapter 			mAdapter;
	private TextView 				mCartTotalPrice;
	//private TextView 				mCarriageWarning;
	private Button					mSubmitButton;
	private Drawable				mEditIcon;
	private Drawable				mFinishIcon;
	private ScrollView              mScrollV;
	private FrameLayout				mCoverView;
	private FrameLayout				mCoverErrorListView;

	private boolean                 bSplitShippingTip; //分单提示
	private boolean isEditView 	= true;
//	private int 	checkedId 	= -1;
	private long    mProductId = 0;
	private ShoppingCartListParser mShopCartListParser;
	private MyItemOnClickListener mRuleItemClick;
	private LinearLayout mChooseAddressLayout;
	private TextView mChooseAddressValue;
	private View mErrorTipView;
	
	private AddressRadioDialog mProvinceDialog;
	private AddressRadioDialog mCityDialog;
	private AddressRadioDialog mZoneDialog;
	
	private ProvinceModel mProvinceModel;
	private CityModel mCityModel;
	private ZoneModel mZoneModel;
	private FullDistrictItem mDistrictItem;
	private Ajax mAjax = null;
	
	private LinearListView 	mPromoListView ;			//一般促销规则
	private	LinearListView 	mBuyMoreListView;			//buymore促销规则
	private View 			mFreeGiftsView;				//满赠促销规则view
	private View 			mLessPriceBuyView;			//加价购促销规则view
	private TextView        mBuyMoreTitle;
	private CheckBox 		mChoosePromoCheckBox;		//选择是否使用优惠券不使用促销规则
	private TextView 		mFreeGiftPromoRight;
	private TextView 		mLessPriceBuyPromoRight;
	private boolean 		isChooseUseCoupon = false; 	//是否选择使用优惠券，使用优惠券不能参加促销规则
	
	
	public ShoppingCartView(ShoppingCartActivity activity) {
		mActivity = activity;
		mShoppingCartControl = new ShoppingCartControl(mActivity);
		mActivity.setDefaultBodyContainer(mActivity.findViewById(R.id.shoppingcart_listview));
		mActivity.setDefaultLoadingContainer(mActivity.findViewById(R.id.global_loading));
		mItemsListView = (LinearListView) mActivity.findViewById(R.id.cart_list_listView);
		mErrorItemsListView	= (LinearListView) mActivity.findViewById(R.id.cart_errorlist_listView);
		//mCarriageWarning = (TextView) mActivity.findViewById(R.id.cart_carriage_warning);
		mCartTotalPrice = (TextView) mActivity.findViewById(R.id.cart_textview_price_amt);
		mSubmitButton = (Button) mActivity.findViewById(R.id.cart_confirm);
		mPromoListView = (LinearListView) mActivity.findViewById(R.id.list_apply_rule);
		mBuyMoreListView = (LinearListView) mActivity.findViewById(R.id.cart_list_promo_rule);
		mBuyMoreTitle = (TextView) mActivity.findViewById(R.id.cart_list_promo_rule_title);
		mErrorTipView = (View) mActivity.findViewById(R.id.error_item_headerview);
		
		mFreeGiftsView = (View)mActivity.findViewById(R.id.promo_free_gift);
		mLessPriceBuyView = (View)mActivity.findViewById(R.id.promo_lessprice_buy);
		mFreeGiftPromoRight = (TextView) mActivity.findViewById(R.id.promo_free_gift_right);
		mLessPriceBuyPromoRight = (TextView) mActivity.findViewById(R.id.promo_lessprice_buy_right);
		
		mChoosePromoCheckBox = (CheckBox) mActivity.findViewById(R.id.choose_promo_checkbox);
		mChoosePromoCheckBox.setChecked(isChooseUseCoupon);
		mChoosePromoCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChange(Boolean isChecked) {
				isChooseUseCoupon = isChecked;
				render();
			}
		});

		mScrollV = (ScrollView) mActivity.findViewById(R.id.shopping_cart_scroll);
		mSubmitButton.setOnClickListener(mActivity);
		mShoppingCartProductModels = new ArrayList<ShoppingCartProductModel>();
		mShoppingCartErrorProductModels = new ArrayList<ShoppingCartProductModel>();
		mNoPromoProductModels = new ArrayList<ShoppingCartProductModel>();
		mAdpaterProductModels = new ArrayList<ShoppingCartProductModel>();
		
		mShoppingCartAdapter = new ShoppingCartAdapter(mActivity, mAdpaterProductModels);
		mItemsListView.setAdapter(mShoppingCartAdapter);
		mItemsListView.setOnItemClickListener(this);	
		
		mShoppingCartErrorItemsAdapter = new ShoppingCartAdapter(mActivity, mShoppingCartErrorProductModels);
		mErrorItemsListView.setAdapter(mShoppingCartErrorItemsAdapter);
		mErrorItemsListView.setOnItemClickListener(this);	

		mEditIcon = mActivity.getResources().getDrawable(R.drawable.delete_cart_icon);
		mEditIcon.setBounds(0, 0, mEditIcon.getMinimumWidth(), mEditIcon.getMinimumHeight());
		
		mFinishIcon = mActivity.getResources().getDrawable(R.drawable.finish_cart_icon);
		mFinishIcon.setBounds(0, 0, mFinishIcon.getMinimumWidth(), mFinishIcon.getMinimumHeight());
		
		mChooseAddressLayout = (LinearLayout) mActivity.findViewById(R.id.shoppingcart_choose_address);
		mChooseAddressValue = (TextView) mActivity.findViewById(R.id.shoppingcart_choose_address_value);
		mChooseAddressLayout.setOnClickListener(this);
		
		mDistrictItem = FullDistrictHelper.getFullDistrict();
		
		success = new OnSuccessListener<ShoppingCartListModel>(){
			@Override
			public void onSuccess(ShoppingCartListModel model, Response response) {
				mActivity.closeLoadingLayer();
				
				if(null != mAjax)
						mAjax = null;
				
				if(!mShopCartListParser.isSuccess())
				{
					UiUtils.makeToast(mActivity, mShopCartListParser.getErrMsg());
					mShoppingCartProductModels.clear();
					mShoppingCartErrorProductModels.clear();
					mNoPromoProductModels.clear();

					mShoppingCartListModel = null;
					render();
				}
				else if (null != model) {
					mShoppingCartProductModels.clear();
					mShoppingCartErrorProductModels.clear();
					mNoPromoProductModels.clear();

					mShoppingCartListModel = model;
					ArrayList<ShoppingCartProductModel> tmpModels = new ArrayList<ShoppingCartProductModel>();
					tmpModels = model.getShoppingCartProductModels();
					if(tmpModels != null){
						//处理单品赠券,把券绑定到商品
						ArrayList<ProductCouponGiftModel> couponModels =  model.getProductCouponGiftModels();
						if(null != couponModels && couponModels.size() > 0) {
							ShoppingCartProductModel pProductModel;
							ProductCouponGiftModel couponModel;
							int nLength = tmpModels.size();
							for(int nId = 0; nId < nLength; nId ++) {
								pProductModel = tmpModels.get(nId);
								final long pProductId = pProductModel.getProductId();
								
								int nCouponNum = couponModels.size();
								for(int nCouponIn = 0; nCouponIn < nCouponNum; nCouponIn ++ ) {
									couponModel = couponModels.get(nCouponIn);
									if(pProductId == couponModel.getProductId()) {
										pProductModel.setCouponGiftModel(couponModel);
									}
								}
								
							}
						}
						mShoppingCartProductModels.addAll(tmpModels);
						
						//过滤满赠商品和加价购商品
						for(ShoppingCartProductModel pModel : mShoppingCartProductModels) {
							if(pModel.getPromoType() != ShoppingCartProductModel.PRODUCT_BENEFIT_FREE_GFIT && pModel.getPromoType() != ShoppingCartProductModel.PRODUCT_BENEFIT_LESS_PRICE_BUY ){
								mNoPromoProductModels.add(pModel);
							}
						}
					}
					
					ArrayList<ShoppingCartProductModel> errorModels = new ArrayList<ShoppingCartProductModel>();
					errorModels = model.getShoppingCartProduct_ErrorItemsModels();
					if(null != errorModels )
						mShoppingCartErrorProductModels.addAll(errorModels);
					bSplitShippingTip = model.isSpliTips();
					
					showPromoRule();
					render();
				}
				
			}				
		};
		
//		mRadioGroup.setOnItemClickListener( new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				if(!isEditView)	{
//					setEditView();
//					return;
//				}	
//				if( checkedId == position){
//					checkedId = -1;
//				}else{
//					checkedId = position;
//				}
//				
//				mAdapter.notifyDataSetChanged();
//				render();
//			}
//		});
		
		bSplitShippingTip = false;
	}
	
	public void getShoppingCartList() {
		if(mShopCartListParser == null) {
			mShopCartListParser = new ShoppingCartListParser();
		}
		
		mPromoApplyRules = null;
		mFreeGiftRules = null;
		mLessPriceBuyRules = null;
		
		mAjax = mShoppingCartControl.getShoppingCartList(mShopCartListParser, success, mActivity);
	}
	
	public void setIsEditView(boolean isEditView) {
		this.isEditView = isEditView;
	}
	
	public void refreshFullDistrictItem(){
		mDistrictItem = FullDistrictHelper.getFullDistrict();
	}
	
	public ShoppingCartProductModel getShoppingCartProductModel(long productId){
		for (ShoppingCartProductModel model : mShoppingCartProductModels) {
			if (model.getProductId() == productId) {
				return model;
			}
		}
		for (ShoppingCartProductModel model : mShoppingCartErrorProductModels) {
			if (model.getProductId() == productId) {
				return model;
			}
		}
		return null;
	}
	
	/*
	 * 用户选择或者取消 “使用优惠券”，更新购物车界面
	 * 隐藏促销规则和满赠加价购商品
	 * 
	 */
	private void updateUIOnChooseUseCoupon(){
		if(isChooseUseCoupon){
			mPromoListView.setVisibility(View.GONE);
			mBuyMoreListView.setVisibility(View.GONE);
			mBuyMoreTitle.setVisibility(View.GONE);
			mFreeGiftsView.setVisibility(View.GONE);
			mLessPriceBuyView.setVisibility(View.GONE);
			mAdpaterProductModels.clear();
			mAdpaterProductModels.addAll(mNoPromoProductModels);
		}else{
			if(null != mPromoApplyRules && !mPromoApplyRules.isEmpty()) {
				mPromoListView.setVisibility(View.VISIBLE);
			}
			
			if(null != mPromoBuyMoreRules && !mPromoBuyMoreRules.isEmpty()) {
				mBuyMoreListView.setVisibility(View.VISIBLE);
				mBuyMoreTitle.setVisibility(View.VISIBLE);
			}
			
			if(null != mFreeGiftRules && !mFreeGiftRules.isEmpty()) {
				mFreeGiftsView.setVisibility(View.VISIBLE);
			}
			
			if(null != mLessPriceBuyRules && !mLessPriceBuyRules.isEmpty()) {
				mLessPriceBuyView.setVisibility(View.VISIBLE);
			}
			mAdpaterProductModels.clear();
			mAdpaterProductModels.addAll(mShoppingCartProductModels);
		}
		
		mShoppingCartAdapter.notifyDataSetChanged();
	}
	
	/*
	 * show promotion rules
	 */
	private void showPromoRule(){
		final LayoutInflater inflater = LayoutInflater.from(mActivity);
		//显示 可以选择的促销优惠活动
		mPromoApplyRules = null;
		mFreeGiftRules = null;
		mLessPriceBuyRules = null;
		//满赠
		int nChoosesFreeGiftsNum = 0;
		mFreeGiftRules = (null == mShoppingCartListModel) ? null : mShoppingCartListModel.getFreeGiftRulesModels();
		if(null != mFreeGiftRules && 0 != mFreeGiftRules.size()) {
			mFreeGiftsView.setVisibility(View.VISIBLE);
			
			for(PromoApplyRuleModel model : mFreeGiftRules) {
				nChoosesFreeGiftsNum += model.getSelectNum();
			}
		}else{
			mFreeGiftsView.setVisibility(View.GONE);
		}
		
		if(0 == nChoosesFreeGiftsNum) {
			mFreeGiftPromoRight.setText("请选择");
		}else{
			mFreeGiftPromoRight.setText("已选" + nChoosesFreeGiftsNum + "件");
		}
		
		//加价购
		int nChooseLessPriceNum = 0;
		mLessPriceBuyRules = (null == mShoppingCartListModel) ? null : mShoppingCartListModel.getLessPriceBuyRulesModels();
		if(null != mLessPriceBuyRules && 0 != mLessPriceBuyRules.size()) {
			mLessPriceBuyView.setVisibility(View.VISIBLE);
			
			for(PromoApplyRuleModel model : mLessPriceBuyRules) {
				nChooseLessPriceNum += model.getSelectNum();
			}
		}else{
			mLessPriceBuyView.setVisibility(View.GONE);
		}
		
		if(0 == nChooseLessPriceNum) {
			mLessPriceBuyPromoRight.setText("请选择");
		}else{
			mLessPriceBuyPromoRight.setText("已选" + nChooseLessPriceNum + "件");
		}
		
		//checkbox: choose whether use coupon or not
		mPromoApplyRules = (null == mShoppingCartListModel) ? null : mShoppingCartListModel.getPromoApplyRuleModels();
		if( (null == mFreeGiftRules || 0 == mFreeGiftRules.size()) && (null == mLessPriceBuyRules || 0 == mLessPriceBuyRules.size()) && (null == mPromoApplyRules || 0 == mPromoApplyRules.size())) {
			mChoosePromoCheckBox.setVisibility(View.GONE);
		}else{
			mChoosePromoCheckBox.setVisibility(View.VISIBLE);
		}
		
		//一般促销规则
		if(mPromoApplyRules == null || mPromoApplyRules.isEmpty()){
			mPromoListView.setVisibility(View.GONE);
		}else{
			mPromoListView.setVisibility(View.VISIBLE);
			mPromoListView.removeAllViews();
		
			mAdapter = new BaseAdapter() {
				@Override
				public View getView(final int position, View convertView, ViewGroup parent) {
					final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.prule_item, null);
					
					final TextView textview = (TextView) layout.findViewById(R.id.promo_item_name);
					PromoApplyRuleModel ruleDecs = (PromoApplyRuleModel)getItem(position);
					textview.setText(Html.fromHtml(ruleDecs.getName()));
					
					return layout;
				}

				@Override
				public long getItemId(int position) {
					return 0;
				}

				@Override
				public Object getItem(int position) {
					return mPromoApplyRules.get(position);
				}

				@Override
				public int getCount() {
					if(mPromoApplyRules == null || mPromoApplyRules.isEmpty())
						return 0;
					
					return mPromoApplyRules.size();
				}
			};

			mPromoListView.setAdapter(mAdapter);
		}
		
		
		//显示 可以参与的促销优惠活动
		mPromoBuyMoreRules = (null == mShoppingCartListModel) ? null : mShoppingCartListModel.getPromoBuyMoreRuleModels();
		if(mPromoBuyMoreRules != null && !mPromoBuyMoreRules.isEmpty()){
			mBuyMoreListView.setVisibility(View.VISIBLE);
			mBuyMoreTitle.setVisibility(View.VISIBLE);
			mBuyMoreListView.removeAllViews();
			BaseAdapter mAdapter = new BaseAdapter() {
				@Override
				public View getView(final int position, View convertView, ViewGroup parent) {
					final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.prule_item_info, null);
					final TextView textview = (TextView) layout.findViewById(R.id.prule_text);
					
					PromoBuyMoreRuleModel ruleDecs = (PromoBuyMoreRuleModel)getItem(position);
					String rule = "参加 <font color=\"#BE2A01\">" + Html.fromHtml(ruleDecs.getName()) + "</font>活动，\n仅需再消费<font color=\"#BE2A01\">¥" + ToolUtil.toPrice(ruleDecs.getBuyMore(), 2) + "</font>";
					textview.setText(Html.fromHtml(rule));
					if(position == getCount()-1)
						layout.findViewById(R.id.bottomline).setVisibility(View.INVISIBLE);
					if( ruleDecs.getUrl().compareTo("")== 0)
						layout.findViewById(R.id.arrowImage).setVisibility(View.INVISIBLE);
					return layout;
				}
	
				@Override
				public long getItemId(int position) {
					return 0;
				}
				@Override
				public boolean areAllItemsEnabled() {
			        return !isEditView;
			    }
				@Override
				public Object getItem(int position) {
					return mPromoBuyMoreRules.get(position);
				}
	
				@Override
				public int getCount() {
					return mPromoBuyMoreRules.size();
				}
			};

			mBuyMoreListView.setAdapter(mAdapter);
			mRuleItemClick = new MyItemOnClickListener(mPromoBuyMoreRules);
			mBuyMoreListView.setOnItemClickListener( mRuleItemClick );
			
		}else
		{
			mBuyMoreListView.setVisibility(View.GONE);
			mBuyMoreTitle.setVisibility(View.GONE);
		}
		
	}

	public void getList() {
		//不支持离线购物车：用户没有登录时，购物车显示为空
		//modified by qingliang
		/*
		ArrayList<ShoppingCartProductModel> models = IShoppingCart.getList();
		if (models != null && models.size() > 0) {
			mActivity.showLoadingLayer();
			mShoppingCartControl.getShoppingCartList(REQUEST_FLAG_CARTLIST_NOT_LOGIN, models, this, mActivity);
			return;
		}*/

		mShoppingCartProductModels.clear();
		mShoppingCartErrorProductModels.clear();
		render();
	}
	
	/*
	 * 获取三级地址的详细信息，展示在购物车页面内。
	 * 如果本地三级地质和线上返回的不一致，以线上返回的三级地质为准，且更新本地。
	 */
	private String getAddressDetail(){
		String strAddressDetail = "";
		String provinceName = "";
		String cityName = ""; 
		String districtName = "";
		
		
		ProvinceModel pProvinceModel = null; 
		CityModel pCityModel = null;
		ZoneModel pDistrictModel = null;
		pProvinceModel= null == mShoppingCartListModel ? null : mShoppingCartListModel.getFullDistrictModel();
		if(null != pProvinceModel) {
			 pCityModel = ( null == pProvinceModel.getCityModels() || 0 == pProvinceModel.getCityModels().size()) ? null : pProvinceModel.getCityModels().get(0);
			if(null != pCityModel) {
				pDistrictModel = ( null == pCityModel.getZoneModels() || 0 == pCityModel.getZoneModels().size()) ? null : pCityModel.getZoneModels().get(0);
			}
		}
		
		if(null == mDistrictItem ) {
			mDistrictItem = FullDistrictHelper.getFullDistrict();
		}
		
		if( (null != pDistrictModel)&& (TextUtils.isEmpty(mDistrictItem.mProvinceName) || TextUtils.isEmpty(mDistrictItem.mCityName) ||TextUtils.isEmpty(mDistrictItem.mDistrictName)  
				|| pProvinceModel.getProvinceId() != mDistrictItem.mProvinceId  || pCityModel.getCityId() != mDistrictItem.mCityId)){
			provinceName = pProvinceModel.getProvinceName();
			cityName = pCityModel.getCityName();
			districtName = pDistrictModel.getZoneName();
			//update local storage
			FullDistrictHelper.setFullDistrict(pProvinceModel.getProvinceId(), pCityModel.getCityId(), pDistrictModel.getZoneId());
		}else{
			provinceName = mDistrictItem.mProvinceName;
			cityName = mDistrictItem.mCityName;
			districtName = mDistrictItem.mDistrictName;
		}
		
		if(cityName.contains(provinceName)) {
			provinceName = "";
		}
		strAddressDetail = provinceName + cityName + districtName;
		
		return TextUtils.isEmpty(strAddressDetail)? "请选择收货省份" : strAddressDetail;
	}

	/*
	 * Update ShoppingCart  View
	 */
	private void render() {
		updateUIOnChooseUseCoupon();
		mActivity.findViewById(R.id.split_tip_tv).setVisibility(bSplitShippingTip ? View.VISIBLE : View.GONE);
		
		// 修改后的数量保存在本地
		IShoppingCart.set(mShoppingCartProductModels);
		// 更新icon
		ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(mActivity);
		mShoppingCartCommunication.notifyDataSetChange();
		
		int shoppingCartCount = mShoppingCartProductModels.size()+ mShoppingCartErrorProductModels.size();
		int visibility = shoppingCartCount > 0 ? View.VISIBLE : View.GONE;
		//如果没有商品了    还原为原始状态
		isEditView = shoppingCartCount > 0 ?  isEditView : true;
		mScrollV.setBackgroundResource(R.color.background_color);
		mActivity.setNavBarRightVisibility(visibility);
		mItemsListView.setVisibility(visibility);
		mActivity.findViewById(R.id.shoppingcart_footer_view).setVisibility(visibility);
		
		mChooseAddressLayout.setVisibility(visibility);
		mErrorItemsListView.setVisibility(mShoppingCartErrorProductModels.size() > 0 ? View.VISIBLE : View.GONE);
		mErrorTipView.setVisibility(mErrorItemsListView.getVisibility());
		mActivity.findViewById(R.id.cart_list_linear_empty).setVisibility(
				shoppingCartCount > 0 ? View.GONE : View.VISIBLE);

		if (shoppingCartCount == 0) {
			mActivity.setNavBarRightVisibility(View.GONE);
			return;
		}
		
		String strAddressDetail = getAddressDetail();
		mChooseAddressValue.setText(strAddressDetail);
		
		if(isEditView) {
			mActivity.setNavBarRightText("删除", mEditIcon);
			mScrollV.setBackgroundResource(R.color.background_color);
			//满赠view
			if(View.VISIBLE == mFreeGiftsView.getVisibility()) {
				mFreeGiftsView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ToolUtil.startActivity(mActivity, FreeGiftsActivity.class);
					}
				});
				mFreeGiftsView.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.global_white_shadow_click_state));
			}
			
			//加价购view
			if(View.VISIBLE == mLessPriceBuyView.getVisibility()) {
				mLessPriceBuyView.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ToolUtil.startActivity(mActivity, LessPriceBuyActivity.class);
					}
				});
				mLessPriceBuyView.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.global_white_shadow_click_state));
			}
			
			mChoosePromoCheckBox.setClickable(true);
		}else{
			mActivity.setNavBarRightText("完成",mFinishIcon);
			mScrollV.setBackgroundResource(R.color.shopping_cart_cover);
			mChooseAddressLayout.setVisibility(View.GONE);
			//满赠view
			if(View.VISIBLE == mFreeGiftsView.getVisibility()) {
				mFreeGiftsView.setOnClickListener(null);
				mFreeGiftsView.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
			}
			//加价购view
			if(View.VISIBLE == mLessPriceBuyView.getVisibility()) {
				mLessPriceBuyView.setOnClickListener(null);
				mLessPriceBuyView.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
			}
			
			mChoosePromoCheckBox.setClickable(false);
		}
		
		mShoppingCartAdapter.setEditView(isEditView);
		mShoppingCartErrorItemsAdapter.setEditView(isEditView);

		//处理覆盖层
		mCoverView = (FrameLayout) mActivity.findViewById(R.id.shoppingcart_footer_view);
		int colorId = isEditView ?  R.color.cart_overlay_null : R.color.cart_overlay;
		mCoverView.setForeground(new ColorDrawable(mActivity.getResources().getColor(colorId)));
		
		mCoverView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isEditView)
					setEditView();
			}
		});
		
		mCoverErrorListView = (FrameLayout) mActivity.findViewById(R.id.coverErrorListView);
		int colorId2 = !isEditView ?  R.color.cart_overlay_null : R.color.global_shortcut_shadow_color;
		mCoverErrorListView.setForeground(new ColorDrawable(mActivity.getResources().getColor(colorId2)));

		mSubmitButton.setClickable(isEditView && 0 != mShoppingCartProductModels.size()? true : false);
		if(0 == mShoppingCartProductModels.size())
		{
			mSubmitButton.setEnabled(false);
		}
		else
		{
			mSubmitButton.setEnabled(true);

		}
		
		//Calculate total price
		double priceAmt = 0;
//		int productCount = 0;
		for (ShoppingCartProductModel model : mAdpaterProductModels) {
			priceAmt += model.getShowPrice() * model.getBuyCount();
//			productCount += model.getBuyCount();
		}

	
		//price of all promotion rules
		long promoBenifit = 0;
		if(!isChooseUseCoupon && null != mPromoApplyRules) {
			for(PromoApplyRuleModel promoModel : mPromoApplyRules) {
				if(promoModel.getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH) {
					promoBenifit += promoModel.getBenefits();
				}
			}
		}else{
			promoBenifit = 0;
		}
		
		/*
		if( promoApplyRules == null || promoApplyRules.isEmpty()){
			checkedId = -1;
		}
		
		if(checkedId > -1 ){
			//满送券
			if(promoApplyRules.get(checkedId).getBenefitType() == PromoRuleModel.BENEFIT_TYPE_COUPON){
				promoBenifit=0;
			}//满减
			else if(promoApplyRules.get(checkedId).getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH){
				promoBenifit = promoApplyRules.get(checkedId).getBenefits();
			}
		}
	*/
		
		priceAmt -= promoBenifit;
		
		//Show carriage warnings
		/*if(priceAmt >= 2900){
			mCarriageWarning.setVisibility(View.GONE);
		}else{
			mCarriageWarning.setVisibility(View.VISIBLE);
			
			String strWarning = "还差<font color=\"red\">¥" + ToolUtil.toPrice((2900 - priceAmt), 2) + "</font>" + "元，即可享受<font color=\"red\">满29元免运费</font>";
			mCarriageWarning.setText(Html.fromHtml(strWarning));
		}*/
		
		//总金额
		mCartTotalPrice.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(priceAmt, 2));
//		((TextView) mActivity.findViewById(R.id.shoppingcart_top_count)).setText("共" + productCount + "件商品");
	}


//	private boolean checkCountAvailable() {
//		for (int i = 0, len = mShoppingCartProductModels.size(); i < len; i++) {
//			ShoppingCartProductModel model = mShoppingCartProductModels.get(i);
//			ViewGroup item = (ViewGroup) mListView.getChildAt(i);
//			String numStr = ((EditText) item.findViewById(R.id.cart_editext)).getText().toString();
//			int num = numStr.equals("") ? 1 : Integer.valueOf(numStr);
//			num = num < 1 ? 1 : num;
//
//			final int numLimit = model.getNumLimit();
//			if (numLimit != 0 && num > numLimit) {
//				UiUtils.makeToast(mActivity, "商品\"" + model.getNameNoHTML() + "\"最多购买" + numLimit + "件");
//				return false;
//			} /*else {
//				model.setBuyCount(num);
//				}*/
//			int lowestNum = model.getLowestNum();
//			if (lowestNum != 0 && num < lowestNum) {
//				UiUtils.makeToast(mActivity, "商品\"" + model.getNameNoHTML() + "\"最低" + lowestNum + "件起售");
//				return false;
//			}
//		}
//
//		return true;
//	}

	public void saveShoppingCart() {
		//重新拉取购物车信息
		getShoppingCartList();
	}

	private void saveFinish() {
		mActivity.setNavBarRightText("修改", mEditIcon);
		mScrollV.setBackgroundResource(R.color.background_color);
		mShoppingCartAdapter.setEditView(false);
		mShoppingCartErrorItemsAdapter.setEditView(false);

		//重新拉取购物车信息
		getShoppingCartList();
	}

	// 修改/完成
	public void setEditView() {
		if (isEditView) {
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21002");
			saveFinish();
			isEditView = false;
			render();
			
			// 限购检查, // 检查是否编辑过
//			if (checkCountAvailable()) {
//				saveFinish();
//				isEditView = false;
//				render();
//			}
		} else {
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21003");
			isEditView = true;
			render();
		}
	}

	// 删除商品
	public void deleteProduct(long productId) {
		for (ShoppingCartProductModel model : mShoppingCartProductModels) {
			if (model.getProductId() == productId) {
				mShoppingCartProductModels.remove(model);
				mPromoListView.setVisibility(View.GONE);
				mBuyMoreListView.setVisibility(View.GONE);
				deleteProductFromOnline(productId, model.getPromoType(), model.getRuleId());
				break;
			}
		}
		for (ShoppingCartProductModel model : mShoppingCartErrorProductModels) {
			if (model.getProductId() == productId) {
				mShoppingCartErrorProductModels.remove(model);
				deleteProductFromOnline(productId);
				break;
			}
		}
		render();
	}

	// 去结算
	public void submit() {
		String str = "";
		for (ShoppingCartProductModel model : mShoppingCartProductModels) {
			if(model != null) {
				str += (str.equals("") ? "" : ",") + model.getProductId() + "|" + model.getBuyCount() + "|" + model.getMainProductId();
			}
		}

		int qingliang;//modify code here
		
		Bundle params =new Bundle();
//		params.putLong(OrderConfirmActivity.TICKET, isChooseUseCoupon ? 1 : 0);
/*		if(checkedId > -1){
			PromoApplyRuleModel model = mPromoApplyRules.get(checkedId);
			if(model != null) {
				params.putLong(OrderConfirmActivity.REQUEST_PRULE_ID, model.getRuleId());
				if(model.getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH) {
					params.putLong(OrderConfirmActivity.REQUEST_PRULE_BENEFITS, model.getBenefits());
				}
			}
		}
*/
		
//		if (uid == 0) {
//			params.putString(OrderConfirmActivity.REQUEST_SHOPPING_CART, str);
//		}

		ToolUtil.checkLoginOrRedirect(mActivity, OrderConfirmActivity.class, params);
			
	}

	private void deleteProductFromOnline(long productId) {
		deleteProductFromOnline( productId, 0, 0);
	}
	private void deleteProductFromOnline(long productId, int promoType, int ruleId) {
		final long uid = ILogin.getLoginUid();
//		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_REMOVE_PRODUCT);
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/deleteProduct?mod=cart");
		if( null == ajax )
			return ;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("uid", uid);
		data.put("pid", productId);
		data.put("district", FullDistrictHelper.getDistrictId());
		if(ShoppingCartProductModel.PRODUCT_BENEFIT_FREE_GFIT == promoType || ShoppingCartProductModel.PRODUCT_BENEFIT_LESS_PRICE_BUY == promoType) {
			//满赠或加价购商品
			data.put("promotionId", ruleId);
		}
		ajax.setId(REQUEST_REMOVE_PRODUCT_ID);
		ajax.setData(data);
		ajax.setParser(new JSONParser());
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(mActivity);
		mActivity.addAjax(ajax);
		ajax.send();
	}
	
	@Override
	public void onSuccess(JSONObject json, Response response) {
		switch (response.getId()) {
		case REQUEST_FLAG_ACCOUNT:
			mActivity.closeProgressLayer();
			int errno = json.optInt("errno", -1);
			String data = json.optString("data", Config.NORMAL_ERROR);

			Bundle params =new Bundle();
			if (errno == 0) {
				ToolUtil.checkLoginOrRedirect(mActivity, OrderConfirmActivity.class, params, -1);
				return;
			}

			UiUtils.makeToast(mActivity, data);
			break;
		case REQUEST_FLAG_CARTLIST_NOT_LOGIN:
			Log.d(LOG_TAG, " onSuccess() REQUEST_FLAG_CARTLIST_NOT_LOGIN");
			mActivity.closeLoadingLayer();
			mShoppingCartProductModels.clear();
			ArrayList<ShoppingCartProductModel> tmpModels = new ArrayList<ShoppingCartProductModel>();
			try {
				errno = json.getInt("errno");
				if (errno != 0) {
					UiUtils.makeToast(mActivity, json.optString("data", Config.NORMAL_ERROR));
					mActivity.closeLoadingLayer(true);
					return;
				}

				if (!ToolUtil.isEmptyList(json, "data")) {
					JSONArray arrs = json.getJSONArray("data");
					for (int i = 0, len = arrs.length(); i < len; i++) {
						ShoppingCartProductModel model = new ShoppingCartProductModel();
						model.parse(arrs.getJSONObject(i));
						tmpModels.add(model);
					}
				}
				mShoppingCartProductModels.addAll(tmpModels);
			} catch (Exception ex) {
				Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
				UiUtils.makeToast(mActivity, R.string.message_server_error);
			}
			render();
			break;
		case REQUEST_REMOVE_PRODUCT_ID:
			if(json.optInt("errno", -1) == 0){
				getShoppingCartList();
			}
			break;
		case REQUEST_SET_BUYCOUNT:
			if(json.optInt("errno", -1) == 0){
				UiUtils.makeToast(mActivity, "修改购买数量成功");
			}else{
				UiUtils.makeToast(mActivity, "修改购买数量失败");
			}
			saveFinish();
			break;
		}
	}

	private void detail(long productId) {
		Bundle param = new Bundle();
		param.putLong(ItemActivity.REQUEST_PRODUCT_ID, productId);
		UiUtils.startActivity(mActivity, ItemActivity.class, param, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		View parentView = (View) view.getParent();
		if(null != parentView)
			switch (parentView.getId()) {
			case R.id.cart_list_listView:
				if (mShoppingCartAdapter != null && position < mShoppingCartAdapter.getCount()) {
						mProductId = (Long) view.getTag(R.layout.shoppingcart_activity_item);
						detail(mProductId);
				}
				break;
				
			case R.id.cart_errorlist_listView:
				if(!isEditView){
					if (mShoppingCartErrorItemsAdapter != null && position < mShoppingCartErrorItemsAdapter.getCount()) {
						mProductId = (Long) view.getTag(R.layout.shoppingcart_activity_item);
						detail(mProductId);
					}
				}
				break;
			default:
				break;
			}
//		if (mShoppingCartAdapter != null && position < mShoppingCartAdapter.getCount()) {
//		//	mListView.showContextMenuForChild(view);
//			mProductId = (Long) view.getTag(R.layout.shoppingcart_activity_item);
//			
//			detail(mProductId);
			/*
			String strCaption = mActivity.getString(R.string.caption_select_action);
			String aOptions[] = {mActivity.getString(R.string.action_view_detail), mActivity.getString(R.string.action_add_favorite), mActivity.getString(R.string.btn_delete)};
			UiUtils.showListDialog(mActivity, strCaption, aOptions, -1, new RadioDialog.OnRadioSelectListener() {
				@Override
				public void onRadioItemClick(int which) {
					switch( which ) {
					case 0:
						detail(mProductId);
						ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "DETAIL.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "06011", String.valueOf(mProductId));
						break;
						
					case 1:
						favorProduct(mProductId);
						ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "FAVOR.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "06012", String.valueOf(mProductId));
						break;
						
					case 2:
						deleteConfirm(mProductId);
						ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "delete.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "06012", String.valueOf(mProductId));
						break;
					}
				}
			});
			*/
//		}
	}
	
	void deleteConfirm(final long nProductId, boolean isLowProduct){
		ShoppingCartProductModel product = getShoppingCartProductModel(nProductId);
		if(product == null)
			return ;
		String strMsg = "";
		if(isLowProduct) {
			int pLowProcut = product.getLowestNum();
			strMsg = "该商品限制最少购买" + pLowProcut+ "件。您确定要删除吗？";
		}else {
			strMsg = "您希望从购物车中删除 '"+product.getName()+"' 吗？";
		}
		
		Dialog dialog = UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_delete_item), strMsg, R.string.btn_delete, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == DialogInterface.BUTTON_POSITIVE) {
					deleteProduct(nProductId);		
				}
			}
		});

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
	}
	
	void addFavorite() {
		favorProduct(mProductId);
	}

	/*
	 * collect product
	 */
	void favorProduct(long productId) {
		long uid = ILogin.getLoginUid();
		if(uid == 0){
			UiUtils.makeToast(mActivity, "请先登录");
			ToolUtil.startActivity(mActivity, LoginActivity.class, null, ShoppingCartActivity.FLAG_REQUEST_FAVOR_LOGIN);
			return;
		}
		
		FavorControl mFavorControl = new FavorControl(mActivity);
		mFavorControl.add(productId, new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				final int errno = v.optInt("errno", -1);
				UiUtils.makeToast(mActivity, errno == 404 ? "您已收藏过该商品" : (errno == 0 ? "收藏成功" : "收藏失败"));
			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				UiUtils.makeToast(mActivity, "收藏失败");
			}
		});
	}
	
	private class MyItemOnClickListener implements OnItemClickListener{

		private ArrayList<PromoBuyMoreRuleModel> mRules = null;
		
		public MyItemOnClickListener(ArrayList<PromoBuyMoreRuleModel> promoBuyMoreRules){
			mRules = promoBuyMoreRules;
		};
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(!isEditView)	{
				setEditView();
				return;
			}	
			PromoBuyMoreRuleModel ruleDecs = (PromoBuyMoreRuleModel)mRules.get(position);
			if( !TextUtils.isEmpty(ruleDecs.getUrl()) ){
				String strYtag = "&ytag=2." + mActivity.getString(R.string.tag_ShoppingCartActivity) + "05015";
				String strUrl = ruleDecs.getUrl();
				if(!strUrl.contains("?")) {
					strUrl += "?" ;
				}
				Bundle bundle = new Bundle();
				bundle.putString(HTML5LinkActivity.LINK_URL, strUrl + strYtag);
				bundle.putString(HTML5LinkActivity.ACTIVITY_TITLE, mActivity.getResources().getString(R.string.icson_account));
				UiUtils.startActivity(mActivity, HTML5LinkActivity.class, bundle,true);
				StatisticsEngine.trackEvent(mActivity, "mergeorder_item_click_cart", "discount_url="+ruleDecs.getUrl());
			}
		
		}
	}

	// 选择配送城市
	private void selectAddress(){
		if(null == mDistrictItem){
			mDistrictItem = FullDistrictHelper.getFullDistrict();
		}
		int selectedId = mDistrictItem.mProvinceId;
		int selectedIndex = 0;
		
		//get whole full district information
		final ArrayList<ProvinceModel> pProviceModels = IShippingArea.getAreaModels();
		if (pProviceModels == null) {
			UiUtils.makeToast(mActivity, Config.NORMAL_ERROR);
			return;
		}

		final int nSize = pProviceModels.size();
		if( 0 >= nSize )
			return ;
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ProvinceModel pMode = pProviceModels.get(nIdx);
			names[nIdx] = pMode.getProvinceName();
			if (selectedId != 0 && pMode.getProvinceId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mProvinceDialog)
		{
		mProvinceDialog = UiUtils.showAddressListDialog(mActivity, mActivity.getString(R.string.select_province), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				if(null == pProviceModels || pProviceModels.size() <= pos)
				{
					mProvinceDialog.dismiss();
					return;
				}
				mProvinceModel = pProviceModels.get(pos);
				selectCity();
			}
		}, true);
		}
		else
			mProvinceDialog.setList(names, selectedIndex);
		
		mProvinceDialog.show();
	}
	
	private void selectCity(){
		if(null == mProvinceModel ) {
			return;
		}
		
		int selectedId = mDistrictItem.mCityId;
		int selectedIndex = 0;
		final ArrayList<CityModel> pCityModels = mProvinceModel.getCityModels();
		if (pCityModels == null) {
			UiUtils.makeToast(mActivity, Config.NET_RROR);
			return;
		}

		final int nSize = pCityModels.size();
		if( 0 >= nSize ){
			return ;
		}
		
		if( 1 == nSize ) {
			mCityModel = pCityModels.get(0);
			selectZone();
			return;
		}
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			CityModel pMode = pCityModels.get(nIdx);
			names[nIdx] = pMode.getCityName();
			if (selectedId != 0 && pMode.getCityId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mCityDialog)
		{
		mCityDialog = UiUtils.showAddressListDialog(mActivity, mActivity.getString(R.string.select_city), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				if(null == pCityModels || pCityModels.size() <= pos)
				{
					mCityDialog.dismiss();
					return;
				}
				mCityModel = pCityModels.get(pos);
				selectZone();
			}
		}, true);
		}
		else
			mCityDialog.setList(names, selectedIndex);
		
		mCityDialog.show();
	}
	
	private void selectZone(){
		if(null == mCityModel || null == mProvinceModel) {
			return;
		}
		
		 int selectedId = mDistrictItem.mDistrictId;
		int selectedIndex = 0;
		final ArrayList<ZoneModel> pZoneModels = mCityModel.getZoneModels();
		if (pZoneModels == null) {
			UiUtils.makeToast(mActivity, Config.NORMAL_ERROR);
			return;
		}

		final int nSize = pZoneModels.size();
		if( 0 >= nSize )
			return ;
		
		if( 1 == nSize ) {
			mZoneModel = pZoneModels.get(0);
			afterSelectFullDistrict();
			return;
		}
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ZoneModel pMode = pZoneModels.get(nIdx);
			names[nIdx] = pMode.getZoneName();
			if (selectedId != 0 && pMode.getZoneId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mZoneDialog)
		{
		mZoneDialog = UiUtils.showAddressListDialog(mActivity, mActivity.getString(R.string.select_area), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				if(null == pZoneModels || pZoneModels.size() <= pos)
				{
					mZoneDialog.dismiss();
					return;
				}
				
				mZoneModel = pZoneModels.get(pos);
				
				afterSelectFullDistrict();
				
			}
		}, true, false);
		}
		else
			mZoneDialog.setList(names, selectedIndex);
		mZoneDialog.show();
	}

	protected void afterSelectFullDistrict() {
		if(null != mZoneDialog && mZoneDialog.isShowing()) {
			mZoneDialog.dismiss();
		}
		
		if(null != mCityDialog && mCityDialog.isShowing()) {
			mCityDialog.dismiss();
		}
		
		if(null != mProvinceDialog && mProvinceDialog.isShowing()) {
			mProvinceDialog.dismiss();
		}
		
		IPageCache cache = new IPageCache();
		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);
		cache = null;
		int nCityId = (id != null ? Integer.valueOf( id ) : 0);

		boolean changedDistrictFlag = false;
		if(mZoneModel.getZoneId()!= mDistrictItem.mDistrictId)
			changedDistrictFlag = true;
		
		//Update UI
		//empty not set yet || different, changed districtid
		if (nCityId == 0 || changedDistrictFlag) {
			if(null != mCityModel && null != mProvinceModel && null != mZoneModel) {
				FullDistrictItem pDistrictItem = new FullDistrictItem(mProvinceModel.getProvinceId(), mProvinceModel.getProvinceIPId(), mProvinceModel.getProvinceName(), mCityModel.getCityId(), mCityModel.getCityName(), mZoneModel.getZoneId(), mZoneModel.getZoneName());
				FullDistrictHelper.setFullDistrict(pDistrictItem);
				mDistrictItem = pDistrictItem;
			}
		}
		
		// 城市切换后要刷新页面，拉取购物车
		if(changedDistrictFlag)
		{
			mActivity.showLoadingLayer();
			getShoppingCartList() ;
		}
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.shoppingcart_choose_address:
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22001");
				selectAddress();
				break;
		}
	}

	public void cleanUp() {
		if(null != mProvinceDialog && mProvinceDialog.isShowing())
			mProvinceDialog.dismiss();
		mProvinceDialog = null;
		if(null != mCityDialog && mCityDialog.isShowing())
			mCityDialog.dismiss();
		mCityDialog = null;
		if(null != mZoneDialog && mZoneDialog.isShowing())
			mZoneDialog.dismiss();
		mZoneDialog = null;
		
		if(null!=mShoppingCartAdapter)
		{
			mShoppingCartAdapter.cleanUpBitmap();
		}
		
	}
}