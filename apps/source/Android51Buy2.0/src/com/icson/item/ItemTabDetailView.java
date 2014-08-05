package com.icson.item;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.AppStorage;
import com.icson.lib.BaseView;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.IPageCache;
import com.icson.lib.FullDistrictHelper.FullDistrictItem;
import com.icson.lib.ILogin;
import com.icson.lib.IShippingArea;
import com.icson.lib.IShoppingCart;
import com.icson.lib.control.FavorControl;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.BaseModel;
import com.icson.lib.model.DiscountModel;
import com.icson.lib.model.ProductGiftModel;
import com.icson.lib.model.ProductModel;
import com.icson.lib.model.ProductOptionColorModel;
import com.icson.lib.model.ProductOptionModel;
import com.icson.lib.model.ProductOptionSizeModel;
import com.icson.lib.model.PromotePriceModel;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.AddressRadioDialog;
import com.icson.lib.ui.AlphaTextView;
import com.icson.lib.ui.HorizontalListView;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.MyScrollView;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.order.OrderConfirmActivity;
import com.icson.shoppingcart.ESShoppingCartActivity;
import com.icson.shoppingcart.ProductCouponGiftModel.CouponGiftModel;
import com.icson.shoppingcart.ShoppingCartActivity;
import com.icson.shoppingcart.ShoppingCartCommunication;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ItemTabDetailView extends BaseView implements OnItemClickListener,
		ItemTabBase, OnClickListener, OnItemSelectedListener,
		OnSuccessListener<ItemProductModel> {

	private TabDetailSuccLisener mSuccListener;
	private ItemActivity mActivity;
	//private ProductOptionColorModel mProductOptionColorModel;
	//private ProductOptionSizeModel mProductOptionSizeModel;
	private ViewGroup mParent;
	private ViewPager mPager;

	private int mBuyCount = 1;

	private boolean firstExec = true;

	private ItemGalleryAdapter mItemGalleryAdapter;

	private int lastBulletIndex;

	private ProductModelAdapter mBuyGalleryAdapter;
	private ProductModelAdapter mRecmdGalleryAdapter;
	private ProductModelAdapter mBrowseGalleryAdapter;
	
	
	private ItemTabDetailOptionAdapter mOptionListAdapter;

	private ItemProductModel mItemProductModel;
	private ShoppingCartProductModel mShoppingCartProductModel;

	private ArrayList<CouponGiftModel> mCouponGiftModels; 
	private ArrayList<ProductGiftModel> mProductGiftModels;
	private ArrayList<BaseModel> mGiftModels;
	private ArrayList<BaseModel> mProductAccessoryModels;
	private ItemTabDetailGiftAdapter mItemAccessoryAdapter;
	private ItemTabDetailGiftAdapter mItemGiftAdapter;
	// 立即购买，且满足促销规则
	private boolean isbuyImmediately_Rules = false;
	final int MAX_CELLS = 3;
	private Ajax mAjax;

	private int payType;


	private ItemProductParser mItemProductParser;
	
	private AddressRadioDialog mProvinceDialog;
	private AddressRadioDialog mCityDialog;
	private AddressRadioDialog mZoneDialog;
	
	private ProvinceModel mProvinceModel;
	private CityModel mCityModel;
	private ZoneModel mZoneModel;
	
	private FullDistrictItem mDistrictItem;
	
	private Handler  mHandler;
	// 所有的view
	private HorizontalListView mItemGiftView;
	private HorizontalListView mItemAccessoryView;
	private ViewGroup bulletContainer;
	private View mCartButton;
	private View mShareButton;
	private TextView mStockView;
	private Button downBtn;
	private Button upBtn;
	private EditText buyNumEditText;
	private HorizontalListView buy_Gallery;
	private HorizontalListView recommend_Gallery;
	private HorizontalListView browse_Gallery;
	private LinearListView mOptionListView;
	
	private MyScrollView itemTabDetailContainer = null;
	private CustomGallery itemGallery = null;
	private ImageView orderDetailButtonCollect = null;
	private TextView itemTextviewName = null;
	private TextView itemTextviewPromoWord = null;
	private LinearLayout itemLinearPrice;
	private AlphaTextView itemDetailTextviewShowprice = null;
	private LinearLayout itemDetailTextviewMarketpriceLayout = null;
	private TextView itemDetailTextviewMarketpriceLabel;
	private TextView itemDetailTextviewMarketprice;
	private LinearLayout itemLinearRules = null;
	private LinearListView itemLinearRulesList;
	private LinearLayout itemDetailLinearServices = null;
	private ImageView itemDetailImageJgbh = null;
	private ImageView itemDetailImageGjp;
	private LinearLayout itemDetailLinearAccessory = null;
	private LinearLayout itemDetailLinearGift = null;
	private TextView itemDetailTextviewStockValue = null;
	private ImageButton itemDetailImageStock = null;
	private TextView itemDetailTextviewColorValue = null;
	private ImageButton itemDetailButtonColor = null;
	private TextView itemDetailTextviewSizeValue = null;
	private ImageButton itemDetailButtonSize = null;
	private TextView itemDetailTextviewBuyLimit = null;
	private LinearLayout itemDetailToastLl = null;
	private TextView itemDetailToastTitletv = null;
	private TextView itemDetailToastTv = null;
	private Button orderDetailButtonBuynow = null;
	private LinearLayout orderDetailButtonCartLabel = null;
	private ImageView orderDetailIconShoppingCar = null;
	private TextView addToShoppingCarText = null;
	private TextView orderDetailButtonAlert = null;
	private TextView browseRecommendGalleryLabel = null;
	private TextView recommendGalleryLabel = null;
	private TextView buyRecommendGalleryLabel = null;
	private TextView shoppingCartNum = null;
	private RelativeLayout shoppingCartView = null;
	
	/*public void initUi()
	{
		//这里需要注意一下mParent和mActivity
		//"赠品"
		mItemGiftView = ((HorizontalListView) mParent.findViewById(R.id.item_detail_gift_listview));
		mItemAccessoryView = ((HorizontalListView) mParent.findViewById(R.id.item_detail_accessory_listview));
		bulletContainer = (ViewGroup) mParent.findViewById(R.id.item_linear_gallery_bullet);
		mCartButton = (View) mParent.findViewById(R.id.order_detail_button_cart);
		mShareButton = mParent.findViewById(R.id.order_detail_button_share);
		mStockView = (TextView) mParent.findViewById(R.id.item_detail_textview_stock);
		downBtn = (Button) mParent.findViewById(R.id.item_detail_downBtn);
		upBtn = (Button) mParent.findViewById(R.id.item_detail_upBtn);
		buyNumEditText = ((EditText) mParent.findViewById(R.id.item_detail_edittext_buy_count));
		buy_Gallery = (HorizontalListView) mParent.findViewById(R.id.buy_recommend_gallery);
		recommend_Gallery = (HorizontalListView) mParent.findViewById(R.id.recommend_gallery);
		browse_Gallery = (HorizontalListView) mParent.findViewById(R.id.browse_recommend_gallery);
		mOptionListView = (LinearListView) mParent.findViewById(R.id.lv_item_detail_option);
		
		itemTabDetailContainer = (MyScrollView) mParent.findViewById(R.id.item_tab_detail_container);
		itemGallery = (CustomGallery)mParent.findViewById(R.id.item_gallery);
		orderDetailButtonCollect = (ImageView)mParent.findViewById(R.id.order_detail_button_collect);
		itemTextviewName = (TextView)mParent.findViewById(R.id.item_textview_name);
		itemTextviewPromoWord = (TextView) mParent.findViewById(R.id.item_textview_promo_word);
		itemLinearPrice = (LinearLayout) mParent.findViewById(R.id.item_linear_price);
		itemDetailTextviewShowprice = (AlphaTextView) mParent.findViewById(R.id.item_detail_textview_showprice);
		itemDetailTextviewMarketpriceLayout = (LinearLayout) mParent.findViewById(R.id.item_detail_textview_marketprice_layout);
		itemDetailTextviewMarketpriceLabel = (TextView) mParent.findViewById(R.id.item_detail_textview_marketprice_label);
		itemDetailTextviewMarketprice = (TextView)mParent.findViewById(R.id.item_detail_textview_marketprice);
		itemLinearRules = (LinearLayout) mParent.findViewById(R.id.item_linear_rules);
		itemLinearRulesList = (LinearListView) mParent.findViewById(R.id.item_linear_rules_list);
		itemDetailLinearServices = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_services);
		itemDetailImageJgbh = (ImageView) mParent.findViewById(R.id.item_detail_image_jgbh);
		itemDetailImageGjp = (ImageView) mParent.findViewById(R.id.item_detail_image_gjp);
		itemDetailLinearAccessory = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_accessory);
		itemDetailLinearGift = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_gift);
		itemDetailTextviewStockValue = (TextView) mParent.findViewById(R.id.item_detail_textview_stock_value);
		itemDetailImageStock = (ImageButton) mParent.findViewById(R.id.item_detail_image_stock);
		itemDetailTextviewColorValue = (TextView) mParent.findViewById(R.id.item_detail_textview_color_value);
		itemDetailButtonColor = (ImageButton) mParent.findViewById(R.id.item_detail_button_color);
		itemDetailTextviewSizeValue = (TextView) mParent.findViewById(R.id.item_detail_textview_size_value);
		itemDetailButtonSize = (ImageButton) mParent.findViewById(R.id.item_detail_button_size);
		itemDetailTextviewBuyLimit = (TextView) mParent.findViewById(R.id.item_detail_textview_buy_limit);
		itemDetailToastLl = (LinearLayout) mParent.findViewById(R.id.item_detail_toast_ll);
		itemDetailToastTitletv = (TextView) mParent.findViewById(R.id.item_detail_toast_titletv);
		itemDetailToastTv = (TextView) mParent.findViewById(R.id.item_detail_toast_tv);
		orderDetailButtonBuynow = (Button) mParent.findViewById(R.id.order_detail_button_buynow);
		orderDetailButtonCartLabel = (LinearLayout) mParent.findViewById(R.id.order_detail_button_cart_label);
		orderDetailIconShoppingCar = (ImageView) mParent.findViewById(R.id.order_detail_icon_shopping_car);
		addToShoppingCarText = (TextView) mParent.findViewById(R.id.add_to_shopping_car_text);
		orderDetailButtonAlert = (TextView) mParent.findViewById(R.id.order_detail_button_alert);
		browseRecommendGalleryLabel = (TextView) mParent.findViewById(R.id.browse_recommend_gallery_label);
		recommendGalleryLabel = (TextView) mParent.findViewById(R.id.recommend_gallery_label);
		buyRecommendGalleryLabel = (TextView) mParent.findViewById(R.id.buy_recommend_gallery_label);
		shoppingCartNum = (TextView) mActivity.findViewById(R.id.shopping_cart_num);
		shoppingCartView = (RelativeLayout) mActivity.findViewById(R.id.shopping_cart_view);
	}*/
	
	public ItemTabDetailView(ItemActivity activity, ViewPager aPager) {
		mActivity = activity;
		mPager = aPager;
		mParent = (ViewGroup) mActivity.findViewById(R.id.item_relative_tab_content_detail);
		if(mParent != null) {
			mParent.removeAllViews();
			LayoutInflater.from(mActivity).inflate(R.layout.item_tab_detail,
					mParent, true);// getrules
		}
		mItemProductParser = new ItemProductParser();
	}

	@Override
	public void init() {
		if (!firstExec)
			return;

//		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
//				ItemTabDetailView.class.getName(), mActivity.getString(R.string.tag_ItemTabDetailView), "02011", 
//				String.valueOf(mActivity.getProductId()));
//		

		firstExec = false;
		mDistrictItem = FullDistrictHelper.getFullDistrict();
		sendRequest();
	}

	@Override
	public void clean() {
		firstExec = true;   
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		if(null!=mParent)
		{
			if(null == itemTabDetailContainer)
				itemTabDetailContainer = (MyScrollView) mParent.findViewById(R.id.item_tab_detail_container);
			itemTabDetailContainer.scrollTo(0, 0);
	
		}
	}

	public void sendRequest() {
		String strInfo = "&pid=" + mActivity.getProductId();
		// 场景id
		if (mActivity.getChannelId() != 0) {
			strInfo += "&channelId=" + mActivity.getChannelId();
		}
		// dap
		if (mActivity.getDAP() != null) {
			strInfo += "&DAP=" + mActivity.getDAP();
		}
		
		if(null != mDistrictItem) {
			//省份ip_id 地区id
			strInfo += "&provinceid=" + mDistrictItem.mProvinceIPId;
			strInfo += "&district=" + mDistrictItem.mDistrictId;
		}
		strInfo += "&allattr=1";
		
//		String url;
//		url = "http://mgray.yixun.com/item/detail?pid=1471585&provinceid=31&channelId=";
//		url = "http://mgray.yixun.com/item/detail?pid=857411&provinceid=31&channelId=";
//		url = "http://mgray.yixun.com/item/detail?pid=590897&provinceid=31&channelId=";
//		url = "http://mgray.yixun.com/item/detail?pid=1612085&provinceid=31&channelId=";

		
		//新联营
//		url = "http://mgray.yixun.com/item/detail?pid=1361430&provinceid=31&channelId=";

//		mAjax = AjaxUtil.get(url);
		mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_DETAIL, strInfo);
		if (null == mAjax)
			return;

		mAjax.setOnSuccessListener(this);
		mAjax.setParser(mItemProductParser);
		mAjax.setOnErrorListener(mActivity);
		if(null == itemTabDetailContainer)
			itemTabDetailContainer = (MyScrollView) mParent.findViewById(R.id.item_tab_detail_container);
		mActivity.setLoadingSwitcher(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT,
				itemTabDetailContainer,
				mParent.findViewById(R.id.global_loading));
		mActivity.showLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
		mAjax.send();
	}

	public ItemProductModel getItemProductModel() {
		return mItemProductModel;
	}

	@Override
	public void onSuccess(ItemProductModel v, Response response) {
		mItemProductModel = v;
		mActivity.closeLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT, mItemProductModel == null);
		
		if( !mItemProductParser.isSuccess() ) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mItemProductParser.getErrMsg()) ? Config.NORMAL_ERROR: mItemProductParser.getErrMsg());
			return;
		}
		
		if (mItemProductModel != null) {
			renderDetailPanel();
			if(null != mSuccListener)
				mSuccListener.onReviewCountModelSucc(mItemProductModel.getReviewCountModel());
			
		}
	}
	
	private String getAddressDetail(){
		String strAddressDetail = "";
		String provinceName = "";
		String cityName = ""; 
		String districtName = "";
		
		
		ProvinceModel pProvinceModel = null; 
		CityModel pCityModel = null;
		ZoneModel pDistrictModel = null;
		pProvinceModel= null == mItemProductModel ? null : mItemProductModel.getFullDistrictModel();
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

	/**
	 * call only once by onSuccess. So no need to save mViews
	 */
	public void renderDetailPanel() {

		// 加入收藏
		/*
		 * ProductModel collectModel =
		 * ICollect.get(mItemProductModel.getProductId()); boolean haveCollect =
		 * collectModel != null;
		 */
		// 网购商品不显示收藏
		if(null == orderDetailButtonCollect)
			orderDetailButtonCollect = (ImageView) mActivity.findViewById(R.id.order_detail_button_collect);
		orderDetailButtonCollect
				.setVisibility(mItemProductModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU ? View.GONE
						: View.VISIBLE);
		orderDetailButtonCollect.setTag(null);
		orderDetailButtonCollect
				.setImageResource(R.drawable.ic_star);
		// 配送至:
		if(itemDetailTextviewStockValue == null)
			itemDetailTextviewStockValue = (TextView) mActivity.findViewById(R.id.item_detail_textview_stock_value);
		itemDetailTextviewStockValue.setText(getAddressDetail());

		// 商品标题
		if(null == itemTextviewName)
			itemTextviewName = ((TextView) mActivity.findViewById(R.id.item_textview_name));
		itemTextviewName.setText(mItemProductModel.getNameNoHTML());

		// 促销语
		if(null == itemTextviewPromoWord)
			itemTextviewPromoWord = ((TextView) mActivity.findViewById(R.id.item_textview_promo_word));
		String wordStr = mItemProductModel.getPromotionWord() == null ? ""
				: mItemProductModel.getPromotionWord().trim();
		itemTextviewPromoWord.setVisibility(wordStr.equals("") ? View.GONE
				: View.VISIBLE);
		itemTextviewPromoWord.setText(Html.fromHtml(wordStr));

		// 易迅价
		if(null == itemDetailTextviewShowprice)
			itemDetailTextviewShowprice = ((AlphaTextView) mParent
				.findViewById(R.id.item_detail_textview_showprice));
		itemDetailTextviewShowprice.setText(ItemTabDetailView.getDisplayPriceStr(mActivity,mItemProductModel));
		
		// 99999900 价格异常，不显示价格
		if(null == itemLinearPrice)
				itemLinearPrice = (LinearLayout) mActivity.findViewById(R.id.item_linear_price);
		itemLinearPrice.setVisibility((mItemProductModel.getIcsonPrice() == 99999900) ? View.GONE : View.VISIBLE);
		
		// 支持服务的提示
		if(null == itemDetailLinearServices)
			itemDetailLinearServices = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_services);
		if(null == itemDetailImageGjp)
			itemDetailImageGjp = (ImageView) mParent.findViewById(R.id.item_detail_image_gjp);
		itemDetailImageGjp.setVisibility(View.GONE);
		if(null == itemDetailImageJgbh)
			itemDetailImageJgbh = (ImageView) mParent.findViewById(R.id.item_detail_image_jgbh);
		itemDetailImageJgbh.setVisibility(View.GONE);
		
		if (mItemProductModel.isGJP() || mItemProductModel.isJGBH()) {
			itemDetailLinearServices.setVisibility(View.VISIBLE);
			if (mItemProductModel.isGJP()) {
				itemDetailImageGjp.setImageResource(R.drawable.item_gjp);
				itemDetailImageGjp.setVisibility(View.VISIBLE);
				itemDetailImageGjp.setOnClickListener(this);
			}
			if (mItemProductModel.isJGBH()) {
				itemDetailImageJgbh.setImageResource(R.drawable.item_jgbh);
				itemDetailImageJgbh.setVisibility(View.VISIBLE);
				itemDetailImageJgbh.setOnClickListener(this);
			}
		}
		
		// 库存状况
		if(null == mStockView)
			mStockView = (TextView) mParent.findViewById(R.id.item_detail_textview_stock);
		if(TextUtils.isEmpty(mItemProductModel.getStock())) {
			mStockView.setVisibility(View.GONE);
		}else{
			mStockView.setVisibility(View.VISIBLE);
			mStockView.setText(mItemProductModel.getStock());
		}
		
		// 特殊商品提示
		if(null == itemDetailToastTitletv)
			itemDetailToastTitletv = (TextView) mParent.findViewById(R.id.item_detail_toast_titletv);
		if(null == itemDetailToastTv)
			itemDetailToastTv = (TextView) mParent.findViewById(R.id.item_detail_toast_tv);
		if(null == itemDetailToastLl)
			itemDetailToastLl = (LinearLayout) mParent.findViewById(R.id.item_detail_toast_ll);
		itemDetailToastLl.setVisibility(View.VISIBLE);
		
		String note_msg = null;
		ArrayList<String> msgList = new ArrayList<String>();
		//String strFDStock = mItemProductModel.getFDStock();
		
		//if(!TextUtils.isEmpty(strFDStock)) {
		//	msgList.add(strFDStock);
		//}
		
		if (!mItemProductModel.canUseCoupon()) {
			String strCouponMsg = "特价商品，不能使用易迅优惠券";
			msgList.add(strCouponMsg);
		}
		
		if (!mItemProductModel.canUseVAT()) {
			String strVATMsg = "本商品为特惠商品，不提供增值税发票";
			msgList.add(strVATMsg);
		}
		
		int nSize = (null == msgList) ? 0 : msgList.size();
		if(0 != nSize) {
			itemDetailToastTitletv.setText("提示 : ");
			if(1 == nSize) {
				note_msg = msgList.get(0);
			}else if(2 == nSize){
				note_msg = "1." + msgList.get(0) + "\n2." + msgList.get(1);
			}else if(3 == nSize) {
				note_msg = "1." + msgList.get(0) + "\n2." + msgList.get(1) + "\n3." + msgList.get(2);
			}
		}
		
		if (note_msg != null) {
			itemDetailToastTv.setText(note_msg);
		} else {
			if(null == itemDetailToastLl)
				itemDetailToastLl = (LinearLayout) mParent.findViewById(R.id.item_detail_toast_ll);
			itemDetailToastLl.setVisibility(View.GONE);
		}
		
		final int nSaleType = mItemProductModel.getSaleType();		
		if(null == mCartButton)
			mCartButton = (View) mParent.findViewById(R.id.order_detail_button_cart);
		mCartButton.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.item_detail_btn));
		if(null == addToShoppingCarText)
			addToShoppingCarText = ((TextView) mParent.findViewById(R.id.add_to_shopping_car_text));
		//addToShoppingCarText.setText(R.string.add_to_cart);
		if(null == orderDetailIconShoppingCar)
			orderDetailIconShoppingCar = (ImageView) mParent.findViewById(R.id.order_detail_icon_shopping_car);
		orderDetailIconShoppingCar.setVisibility(View.VISIBLE);
		if(null == orderDetailButtonBuynow)
			orderDetailButtonBuynow = (Button) mParent.findViewById(R.id.order_detail_button_buynow);
		orderDetailButtonBuynow.setVisibility(View.VISIBLE);
		
		if(mItemProductModel.isESProduct())
		{
			mCartButton.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.confirm_button));
			
			orderDetailButtonBuynow.setVisibility(View.GONE);
			addToShoppingCarText.setText(mActivity.getString(R.string.add_es_cart));
			orderDetailIconShoppingCar.setVisibility(View.GONE);
		}
		else
		{
			// 立即购买
			orderDetailButtonBuynow.setVisibility(
					mItemProductModel.getSaleType() == ProductModel.SALE_AVAILABLE ? View.VISIBLE: View.GONE);
		}
		// 加入购物车
		mCartButton.setEnabled(ProductModel.SALE_AVAILABLE == nSaleType ||
							  ProductModel.SALE_EMPTY == nSaleType
							  );
		
		if(null == orderDetailButtonCartLabel)
			orderDetailButtonCartLabel = (LinearLayout) mActivity.findViewById(R.id.order_detail_button_cart_label);
		orderDetailButtonCartLabel.setVisibility(View.VISIBLE);
		if(null == orderDetailButtonAlert)
			orderDetailButtonAlert = (TextView) mActivity.findViewById(R.id.order_detail_button_alert);
		orderDetailButtonAlert.setVisibility(View.VISIBLE);
		
		addToShoppingCarText.setText(mActivity.getString(R.string.add_to_cart));
		
		if (ProductModel.SALE_AVAILABLE == nSaleType) {
			orderDetailButtonCartLabel.setVisibility(View.VISIBLE);
			orderDetailButtonAlert.setVisibility(View.GONE);
		} else if (ProductModel.SALE_EMPTY == nSaleType) {
			mCartButton.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.item_detail_btn));
			orderDetailButtonAlert.setVisibility(View.VISIBLE);
			orderDetailButtonCartLabel.setVisibility(View.GONE);
		} else if (ProductModel.SALE_UNAVAILABLE == nSaleType)
		{
			orderDetailButtonCartLabel.setVisibility(View.VISIBLE);
			orderDetailIconShoppingCar.setVisibility(View.GONE);
			addToShoppingCarText.setText(mActivity.getString(R.string.not_for_sale));
			orderDetailButtonAlert.setVisibility(View.GONE);
		}
		/*
		 * cartButton .setTextColor(mActivity .getResources() .getColor(
		 * nSaleType == ProductModel.SALE_UNAVAILABLE ?
		 * R.color.global_button_submit_d : R.color.global_button_submit));
		 * cartButton .setText(nSaleType == ProductModel.SALE_AVAILABLE ?
		 * R.string.add_cart : (nSaleType == ProductModel.SALE_EMPTY ?
		 * R.string.arrival_notify : R.string.not_sell));
		 */
		// 顶部加入购物车按钮
		/*
		 * mActivity .findViewById(R.id.global_button_right) .setVisibility(
		 * mItemProductModel.getSaleType() == ProductModel.SALE_AVAILABLE ?
		 * View.VISIBLE : View.GONE);
		 */

		// 是否团购
		if(null == itemDetailTextviewMarketpriceLayout)
			itemDetailTextviewMarketpriceLayout = (LinearLayout) mParent.findViewById(R.id.item_detail_textview_marketprice_layout);
		itemDetailTextviewMarketpriceLayout.setVisibility(View.GONE);
		if(null == itemDetailTextviewMarketpriceLabel)
			itemDetailTextviewMarketpriceLabel = ((TextView) mParent.findViewById(R.id.item_detail_textview_marketprice_label));
		if(null == itemDetailTextviewMarketprice)
			itemDetailTextviewMarketprice = ((TextView) mParent.findViewById(R.id.item_detail_textview_marketprice));
		if (mItemProductModel.isTuanIng()) {
			
			itemDetailTextviewMarketpriceLayout.setVisibility(View.VISIBLE);
			itemDetailTextviewMarketpriceLabel.setText("易迅价：");
			// 划掉价格
			itemDetailTextviewMarketprice.setText(mActivity.getString(R.string.rmb) 
					+ ToolUtil.toPrice(mItemProductModel.getMarketPrice(), 2));
			ToolUtil.setCrossLine(itemDetailTextviewMarketprice);

			// mParent.findViewById(R.id.item_detail_textview_marketprice_label).setVisibility(View.VISIBLE);
			// mParent.findViewById(R.id.item_detail_textview_marketprice).setVisibility(View.VISIBLE);
		}
		// ((TextView)mParent.findViewById(R.id.item_detail_textview_showprice_label)).setText(
		// mItemProductModel.isTuanIng() ? R.string.tuan_price :
		// R.string.icson_price);
		// ((TextView)mParent.findViewById(R.id.item_detail_textview_marketprice_label)).setText(
		// mItemProductModel.isTuanIng() ? R.string.icson_price :
		// R.string.market_price);
		showAttribute();
//		showColors();
//		// 尺寸
//		showSizes();
		// 限购数量
		if(null == itemDetailTextviewBuyLimit)
			itemDetailTextviewBuyLimit = ((TextView) mParent.findViewById(R.id.item_detail_textview_buy_limit));
		int visibility = View.GONE;
		if (mItemProductModel.getNumLimit() != 999999
				|| mItemProductModel.getLowestNum() > 1)
			visibility = View.VISIBLE;
		itemDetailTextviewBuyLimit.setVisibility(visibility);
		String buy_Limit_msg = "";
		if (mItemProductModel.getLowestNum() > 1) {
			buy_Limit_msg = mActivity.getString(R.string.buy_min_msg_short,
					mItemProductModel.getLowestNum());
		}
		if (mItemProductModel.getNumLimit() != 999999) {
			buy_Limit_msg += mActivity.getString(R.string.buy_max,
					mItemProductModel.getNumLimit());
		}
		itemDetailTextviewBuyLimit.setText(buy_Limit_msg);

		// 幻灯片
		final int picNum = mItemProductModel.getPicNum() > 10 ? 10
				: (mItemProductModel.getPicNum() < 0 ? 0 : mItemProductModel
						.getPicNum());
		if(null == itemGallery)
			itemGallery = ((CustomGallery) mParent.findViewById(R.id.item_gallery));
		itemGallery.setPager(mPager);
		itemGallery.setVisibility(picNum > 0 ? View.VISIBLE : View.GONE);
		if (picNum > 0) {
			ViewGroup.LayoutParams param = itemGallery.getLayoutParams();
			param.height = ToolUtil.dip2px(mActivity,
					ItemGalleryAdapter.PIC_HEIGHT);
			
			if(mItemGalleryAdapter == null) {
				if(mItemProductModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU) {
					mItemGalleryAdapter = new ItemGalleryAdapter(mActivity, false, true);
				} else {
					mItemGalleryAdapter = new ItemGalleryAdapter(mActivity, false, false);
				}
			}
			if(mItemProductModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU) {
				mItemGalleryAdapter.setData(mItemProductModel.getMainPic(), picNum);
			}
			else {
				mItemGalleryAdapter.setData(mItemProductModel.getProductCharId(), picNum);
			}
			
			itemGallery.setAdapter(mItemGalleryAdapter);
			itemGallery.setSelection(picNum > 2 ? 1 : 0);
			itemGallery.setOnItemClickListener(this);

			if (picNum > 1) {
				itemGallery.setOnItemSelectedListener(this);
			}
		}

		// 点
		if(bulletContainer == null) {
			bulletContainer = (ViewGroup) mParent.findViewById(R.id.item_linear_gallery_bullet);
		} else {
			bulletContainer.removeAllViews();
		}
		bulletContainer.setVisibility(picNum > 1 ? View.VISIBLE : View.GONE);

		if (picNum > 1) {
			for (int i = 0, len = picNum; i < len; i++) {
				ImageView view = new ImageView(mActivity);
				LinearLayout.LayoutParams lp = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp.weight = 4;
				view.setLayoutParams(lp);
				view.setBackgroundResource(R.drawable.gallery_line_normal);
				bulletContainer.addView(view);
			}
		}

		// 赠品
		showGift();
		//组件
		showAccessory();
		if(null == downBtn)
			downBtn = (Button) mParent.findViewById(R.id.item_detail_downBtn);
		if(null == upBtn)
			upBtn = (Button) mParent.findViewById(R.id.item_detail_upBtn);
		
		bindEvent(mParent);
		
		if(null == mShareButton)
			mShareButton = mParent.findViewById(R.id.order_detail_button_share);
		if(mItemProductModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU)
		{
			mShareButton.setEnabled(false);
		}else
		{
			mShareButton.setEnabled(true);
		}

		if(buy_Gallery == null) 
			buy_Gallery = (HorizontalListView) mParent.findViewById(R.id.buy_recommend_gallery);
		if(browse_Gallery == null) 
			browse_Gallery = (HorizontalListView) mParent.findViewById(R.id.browse_recommend_gallery);
		if(recommend_Gallery == null) 
			recommend_Gallery = (HorizontalListView) mParent.findViewById(R.id.recommend_gallery);
		if(null == buyRecommendGalleryLabel)
			buyRecommendGalleryLabel = (TextView) mParent.findViewById(R.id.buy_recommend_gallery_label);
		if(null == browseRecommendGalleryLabel)
			browseRecommendGalleryLabel = (TextView) mParent.findViewById(R.id.browse_recommend_gallery_label);
		if(null == recommendGalleryLabel)
			recommendGalleryLabel = (TextView) mParent.findViewById(R.id.recommend_gallery_label);
		
		boolean canBuy = (mItemProductModel.getSaleType() == 1);
		
		// 没有买了买的数据
		if (mItemProductModel.getBuyProductModels().size() <= 0 || !canBuy) {
			buy_Gallery.setVisibility(View.GONE);
			buyRecommendGalleryLabel.setVisibility(View.GONE);
		} else {
			buy_Gallery.setVisibility(View.VISIBLE);
			buyRecommendGalleryLabel.setVisibility(View.VISIBLE);
		}
		// 没有看了看的数据
		if (mItemProductModel.getBrowseProductModels().size() <= 0 || !canBuy) {
			browse_Gallery.setVisibility(View.GONE);
			browseRecommendGalleryLabel.setVisibility(View.GONE);
		} else {
			browse_Gallery.setVisibility(View.VISIBLE);
			browseRecommendGalleryLabel.setVisibility(View.VISIBLE);
		}
		
		if(mItemProductModel.getRecommendProductModels().size() <= 0 || canBuy) {
			recommend_Gallery.setVisibility(View.GONE);
			recommendGalleryLabel.setVisibility(View.GONE);
		} else {
			recommend_Gallery.setVisibility(View.VISIBLE);
			recommendGalleryLabel.setVisibility(View.VISIBLE);
		}

		initGallerys();
		// 促销规则展示
		showRules();
	}

	private void showAccessory() {
		ArrayList<ProductGiftModel> list = mItemProductModel.getProductAccessoryModels();
		mProductAccessoryModels = new ArrayList<BaseModel>();
		mProductAccessoryModels.addAll(list);
		int accessoryNum = mProductAccessoryModels != null ? mProductAccessoryModels.size()
				: 0;

		if(null == itemDetailLinearAccessory)
			itemDetailLinearAccessory = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_accessory);
		if (accessoryNum > 0) {
			//Title "组件"
			itemDetailLinearAccessory.setVisibility(View.VISIBLE);
			
			mItemAccessoryAdapter = new ItemTabDetailGiftAdapter(mActivity,mProductAccessoryModels);
			if(null == mItemAccessoryView)
				mItemAccessoryView = ((HorizontalListView) mParent.findViewById(R.id.item_detail_accessory_listview));
					
			mItemAccessoryView.setAdapter(mItemAccessoryAdapter);
			mItemAccessoryView.setVisibility(View.VISIBLE);
			mItemAccessoryView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View aV,
						int pos, long arg3) {
					if(null == mProductAccessoryModels || mProductAccessoryModels.size() <= pos)
						return;
					ProductGiftModel model2 = (ProductGiftModel) mProductAccessoryModels.get(pos);
					ItemProductModel model = new ItemProductModel();
					model.setProductCharId(model2.getProductCharId());
					model.setPicNum(model2.getPicNum());
					Bundle param = new Bundle();
					param.putInt(ItemImageActivity.REQUEST_PIC_INDEX, 0);
					param.putSerializable(ItemImageActivity.REQUEST_PRODUCT_MODEL,
							model);
					ToolUtil.startActivity(mActivity, ItemImageActivity.class, param);
				}});
			
		}else{
			itemDetailLinearAccessory.setVisibility(View.GONE);
		}
	}
	
	//赠品和单品赠券（展示优惠券）
	private void showGift() {
		if(null == mGiftModels) {
			mGiftModels = new ArrayList<BaseModel>();
		}
		mGiftModels.clear();
		mCouponGiftModels = mItemProductModel.getCouponGiftModel().getCouponModels();
		if(null != mCouponGiftModels) {
			mGiftModels.addAll(mCouponGiftModels);
		}
		mProductGiftModels = mItemProductModel.getProductGiftModels();
		if(null != mProductGiftModels) {
			mGiftModels.addAll(mProductGiftModels);
		}
		
		int giftNum = mGiftModels != null ? mGiftModels.size() : 0;

		if(null == itemDetailLinearGift)
			itemDetailLinearGift = (LinearLayout) mParent.findViewById(R.id.item_detail_linear_gift);
		if (giftNum > 0) {
			//Title "赠品"
			
			itemDetailLinearGift.setVisibility(View.VISIBLE);
			
			if(null == mItemGiftView)
				mItemGiftView = ((HorizontalListView) mParent.findViewById(R.id.item_detail_gift_listview));

			mItemGiftAdapter = new ItemTabDetailGiftAdapter(mActivity,
					mGiftModels);

			mItemGiftView.setAdapter(mItemGiftAdapter);
			mItemGiftView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View aV,
						int pos, long arg3) {
					if(null == mGiftModels || mGiftModels.size() <= pos)
						return;
					
					BaseModel giftModel = mGiftModels.get(pos);
					if (giftModel instanceof ProductGiftModel) {
						ProductGiftModel model2 = (ProductGiftModel) giftModel;
						ItemProductModel model = new ItemProductModel();
						model.setProductCharId(model2.getProductCharId());
						model.setPicNum(model2.getPicNum());
						Bundle param = new Bundle();
						param.putInt(ItemImageActivity.REQUEST_PIC_INDEX, 0);
						param.putSerializable(
								ItemImageActivity.REQUEST_PRODUCT_MODEL, model);
						ToolUtil.startActivity(mActivity,
								ItemImageActivity.class, param);
					} else if (giftModel instanceof CouponGiftModel) {
						CouponGiftModel model = (CouponGiftModel) giftModel;
						Bundle bundle = new Bundle();
						bundle.putSerializable(ItemCouponGiftActivity.COUPON_GIFT_KEY, model);
						ToolUtil.startActivity(mActivity, ItemCouponGiftActivity.class, bundle);
					}
				}
			});
		}else{
			itemDetailLinearGift.setVisibility(
					View.GONE);
		}
	}

	static String getDisplayPriceStr(BaseActivity aActivity,ItemProductModel aModel) {
		double dPrice = aModel.getIcsonPrice();

		// 如果有促销价格，并且state为显示，价格低于易迅价, 则显示促销价
		if (aModel.getPromotePriceModel().getState() == PromotePriceModel.PROMOTEPRICE_STATE_SHOW
				&& aModel.getPromotePrice() < aModel.getIcsonPrice()
				&& aModel.getPromotePrice() > 0.0f) {
			dPrice = aModel.getPromotePrice();
		}
		
		if(dPrice == 99999900)
			return "";
		
		return aActivity.getString(R.string.rmb) + ToolUtil.toPrice(dPrice, 2);
	}

	private void showRules() {
		// item_linear_rules
		if(null == itemLinearRulesList)
			itemLinearRulesList = (LinearListView) mParent.findViewById(R.id.item_linear_rules_list);
		if(null == itemLinearRules)
			itemLinearRules = (LinearLayout) mParent.findViewById(R.id.item_linear_rules);
		if (mItemProductModel.getPromoRuleModelList().size() > 0) {
			itemLinearRules.setVisibility(View.VISIBLE);
		} else {
			itemLinearRules.setVisibility(View.GONE);
			return;
		}
		BaseAdapter mAdapter = new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if(null == convertView)
				{
				    holder = new ViewHolder();
					convertView = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.prule_item_info, null);
					holder.setButtomLine(convertView.findViewById(R.id.bottomline));
					holder.setArrowImage((ImageView)convertView.findViewById(R.id.arrowImage));
					holder.setPureText((TextView)convertView.findViewById(R.id.prule_text));
					convertView.setTag(holder);
				
				}else
				{
					holder = (ViewHolder)convertView.getTag();
				}
				
				holder.getPureText().setText(((DiscountModel)getItem(position)).getName());
				if(position == getCount() - 1)
					holder.getButtomLine().setVisibility(View.GONE);
				if(((DiscountModel)getItem(position)).getDiscount_url().compareTo("") == 0)
					holder.getArrowImage().setVisibility(View.INVISIBLE);
				return convertView;
				
				/*下面是原代码
				 * final ViewGroup layout = (ViewGroup) LayoutInflater.from(mActivity).inflate(R.layout.prule_item_info, null);
				final TextView textview = (TextView) layout.findViewById(R.id.prule_text);
				textview.setText(((DiscountModel)getItem(position)).getName());
				if(position == getCount()-1)
					layout.findViewById(R.id.bottomline).setVisibility(View.GONE);
				if( ((DiscountModel)getItem(position)).getDiscount_url().compareTo("") == 0)
					layout.findViewById(R.id.arrowImage).setVisibility(View.INVISIBLE);
				return layout;*/
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return mItemProductModel.getPromoRuleModelList().get(position);
			}
			
			@Override
			public int getCount() {
				return mItemProductModel.getPromoRuleModelList().size();
			}
			
		    class ViewHolder
			{
				View buttomLine = null;
				ImageView arrowImage = null;
				TextView pureText = null;
				
				public View getButtomLine() {
					return buttomLine;
				}
				public void setButtomLine(View buttomLine) {
					this.buttomLine = buttomLine;
				}
				public ImageView getArrowImage() {
					return arrowImage;
				}
				public void setArrowImage(ImageView arrowImage) {
					this.arrowImage = arrowImage;
				}
				public TextView getPureText() {
					return pureText;
				}
				public void setPureText(TextView pureText) {
					this.pureText = pureText;
				}
				
				
			}
		};
		itemLinearRulesList.setAdapter(mAdapter);
		itemLinearRulesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					DiscountModel ruleDecs = (DiscountModel) mItemProductModel.getPromoRuleModelList().get(position);
					if( !TextUtils.isEmpty(ruleDecs.getDiscount_url()) ){
						String strYtag = "&ytag=3." + mActivity.getString(R.string.tag_ItemActivity) + "03020";
						String strUrl = ruleDecs.getDiscount_url();
						if(!strUrl.contains("?")) {
							strUrl += "?" ;
						}
						
						Bundle bundle = new Bundle();
						bundle.putString(HTML5LinkActivity.LINK_URL, strUrl + strYtag);
						bundle.putString(HTML5LinkActivity.ACTIVITY_TITLE, mActivity.getResources().getString(R.string.icson_account));
						UiUtils.startActivity(mActivity, HTML5LinkActivity.class, bundle,true);
						StatisticsEngine.trackEvent(mActivity, "mergeorder_item_click", "discountUrl=" + ruleDecs.getDiscount_url());
				}
			}
		});
//		for (DiscountModel rule : mItemProductModel.getPromoRuleModelList()) {
//			TextView tv = new TextView(mActivity);
//
//			tv.setText(rule.getName());
//			tv.setSingleLine(true);
//			tv.setTextColor(mActivity.getResources().getColor(
//					R.color.global_label));
//			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
//					LayoutParams.WRAP_CONTENT);
//			params.setMargins(3, 0, 3, 0);
//			tv.setLayoutParams(params);
//			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//			rulesView.addView(tv);
//		}

	}
/*
	private void showColors() {
		FlowLayout colors_LinearLayout = (FlowLayout) mParent
				.findViewById(R.id.item_linear_colors);
		final ArrayList<ProductOptionColorModel> colors = mItemProductModel
				.getProductOptionColorModelList();

		mParent.findViewById(R.id.item_detail_linear_color_block)
				.setVisibility(
						colors != null && colors.size() > 0 ? View.VISIBLE
								: View.GONE);

		if(null==colors)
			return;
		
		if (colors.size() > MAX_CELLS) {
			colors_LinearLayout.setVisibility(View.GONE);
			mParent.findViewById(R.id.item_detail_linear_color_spinner)
					.setVisibility(View.VISIBLE);
			for (ProductOptionColorModel model : colors) {
				if (model.isSelected()) {
					mProductOptionColorModel = model;
				}
			}

			mProductOptionColorModel = mProductOptionColorModel == null ? colors
					.get(0) : mProductOptionColorModel;
			((TextView) mParent
					.findViewById(R.id.item_detail_textview_color_value))
					.setText(mProductOptionColorModel.getColor());

		} else {
			colors_LinearLayout.setVisibility(View.VISIBLE);
			mParent.findViewById(R.id.item_detail_linear_color_spinner)
					.setVisibility(View.GONE);
			for (final ProductOptionColorModel color : colors) {

				TextView tv = (TextView) mActivity.getLayoutInflater().inflate(
						R.layout.view_btn, null);

				tv.setText(color.getColor());
				tv.setSingleLine(true);
				if (color.isSelected()) {
					tv.setBackgroundResource(R.drawable.choose_btn_focus);
				} else {
					tv.setBackgroundResource(R.drawable.choose_btn_normal);
				}

				LayoutParams params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(3, 0, 3, 0);
				tv.setLayoutParams(params);
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int selId = color.getProductId();
						if (selId != mItemProductModel.getProductId()) {
							firstExec = true;
							mActivity.init(selId);
						}

					}
				});
				colors_LinearLayout.addView(tv);
			}
		}

	}
	*/
	
	private void showAttribute() {
		ArrayList<ProductOptionModel> optionModelList = mItemProductModel.getProductOptionModelList();
//		if(mOptionListView == null) {
			if(null == mOptionListView)
				mOptionListView = (LinearListView) mParent.findViewById(R.id.lv_item_detail_option);
			mOptionListAdapter = new ItemTabDetailOptionAdapter(mActivity, new ItemTabDetailOptionAdapter.OnOptionItemSelectListener() {

				@Override
				public void onSelected(long id) {
					firstExec = true;
					mActivity.init(id);
				}
				
			});
			mOptionListView.setAdapter(mOptionListAdapter);
//		}
		mOptionListAdapter.setDataSource(optionModelList, mItemProductModel.getProductId());
		mOptionListAdapter.notifyDataSetChanged();
	}
/*
	private void showSizes() {
		FlowLayout sizes_LinearLayout = (FlowLayout) mParent
				.findViewById(R.id.item_linear_sizes);
		final ArrayList<ProductOptionSizeModel> sizes = mItemProductModel
				.getProductOptionSizeModelList();
		mParent.findViewById(R.id.item_detail_linear_size_block).setVisibility(
				sizes != null && sizes.size() > 0 ? View.VISIBLE : View.GONE);

		if(null==sizes)
			return;
		
		if (sizes.size() > MAX_CELLS) {
			sizes_LinearLayout.setVisibility(View.GONE);
			mParent.findViewById(R.id.item_detail_linear_size_spinner)
					.setVisibility(View.VISIBLE);
			for (ProductOptionSizeModel model : sizes) {
				if (model.isSelected()) {
					mProductOptionSizeModel = model;
				}
			}

			mProductOptionSizeModel = mProductOptionSizeModel == null ? sizes
					.get(0) : mProductOptionSizeModel;
			((TextView) mParent
					.findViewById(R.id.item_detail_textview_size_value))
					.setText(mProductOptionSizeModel.getSize());

		} else {
			sizes_LinearLayout.setVisibility(View.VISIBLE);
			mParent.findViewById(R.id.item_detail_linear_size_spinner)
					.setVisibility(View.GONE);
			for (final ProductOptionSizeModel size : sizes) {

				TextView tv = (TextView) mActivity.getLayoutInflater().inflate(
						R.layout.view_btn, null);

				tv.setText(size.getSize());
				tv.setSingleLine(true);
				if (size.isSelected()) {
					tv.setBackgroundResource(R.drawable.choose_btn_focus);
				} else {
					tv.setBackgroundResource(R.drawable.choose_btn_normal);
				}

				LayoutParams params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(3, 0, 3, 0);
				tv.setLayoutParams(params);
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						long selId = size.getProductId();
						if (selId != mItemProductModel.getProductId()) {
							firstExec = true;
							mActivity.init(selId);
						}

					}
				});
				sizes_LinearLayout.addView(tv);
			}
		}

	}
*/
	private void initGallerys() {
		
		if(mBuyGalleryAdapter == null) {
			
			mBuyGalleryAdapter = new ProductModelAdapter(mActivity);
		}
		mBuyGalleryAdapter.setDataSource(mItemProductModel.getBuyProductModels());
		buy_Gallery.setAdapter(mBuyGalleryAdapter);
		buy_Gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(null == mItemProductModel || mItemProductModel.getBuyProductModels() == null || mItemProductModel.getBuyProductModels().size() <= position)
					return;
				
				ProductModel modle = mItemProductModel.getBuyProductModels()
						.get(position);
				String strDAP = TextUtils.isEmpty(modle.getDAP()) ? "" : "DAP:"+modle.getDAP()+"|";
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "24002", strDAP, String.valueOf(mActivity.getProductId()));
				
				Bundle param = new Bundle();
				param.putLong(ItemActivity.REQUEST_PRODUCT_ID,
						modle.getProductId());
				param.putString(ItemActivity.REQUEST_DAP, modle.getDAP());
				
				//just reload 
				if(mActivity.freshProcWithBundle(param))
				{
					mActivity.addBundle(param);
					clean();
					mActivity.init(modle.getProductId());
				//ToolUtil.startActivity(mActivity, ItemActivity.class, param);
				ToolUtil.reportStatisticsPV(mActivity.getActivityPageId(),strDAP, String.valueOf(modle.getProductId()));
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ItemActivity.class.getName(), mActivity.getString(R.string.tag_ItemActivity), "0601" + (position + 1), 
						String.valueOf(mItemProductModel.getProductId()));
				}
				// mActivity.finish();
			}
		});
		
		if(mBrowseGalleryAdapter == null) {
			
			mBrowseGalleryAdapter = new ProductModelAdapter(mActivity);
		}
		mBrowseGalleryAdapter.setDataSource(mItemProductModel.getBrowseProductModels());
		
		browse_Gallery.setAdapter(mBrowseGalleryAdapter);
		browse_Gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(null == mItemProductModel || mItemProductModel.getBrowseProductModels()==null || mItemProductModel.getBrowseProductModels().size()<=position)
					return;
				ProductModel modle = mItemProductModel.getBrowseProductModels()
						.get(position);
				String strDAP = TextUtils.isEmpty(modle.getDAP()) ? "" : "DAP:"+modle.getDAP()+"|";
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "24001", strDAP, String.valueOf(mActivity.getProductId()));
				
				Bundle param = new Bundle();
				param.putLong(ItemActivity.REQUEST_PRODUCT_ID,
						modle.getProductId());
				param.putString(ItemActivity.REQUEST_DAP, modle.getDAP());
				
				//just reload 
				if(mActivity.freshProcWithBundle(param))
				{
					mActivity.addBundle(param);
					clean();
					mActivity.init(modle.getProductId());
				
					//ToolUtil.startActivity(mActivity, ItemActivity.class, param);
					StatisticsEngine.trackEvent(mActivity, "PVC_CLICKRECOMMENDLIST_EVENT");
					ToolUtil.reportStatisticsPV(mActivity.getActivityPageId(),strDAP, String.valueOf(modle.getProductId()));
					ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ItemActivity.class.getName(), mActivity.getString(R.string.tag_ItemActivity), "0701" + (position + 1), 
						String.valueOf(mItemProductModel.getProductId()));
				}
				// mActivity.finish();
			}
		});
		
		if(mRecmdGalleryAdapter == null) {
			
			mRecmdGalleryAdapter = new ProductModelAdapter(mActivity);
		}
		mRecmdGalleryAdapter.setDataSource(mItemProductModel.getRecommendProductModels());
		recommend_Gallery.setAdapter(mRecmdGalleryAdapter);
		recommend_Gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(null == mItemProductModel || mItemProductModel.getRecommendProductModels() == null 
						|| mItemProductModel.getRecommendProductModels().size()<=position)
					return;
				ProductModel modle = mItemProductModel.getRecommendProductModels()
						.get(position);
				String strDAP = TextUtils.isEmpty(modle.getDAP()) ? "" : "DAP:"+modle.getDAP()+"|";
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "24003", strDAP, String.valueOf(mActivity.getProductId()));
				
				Bundle param = new Bundle();
				param.putLong(ItemActivity.REQUEST_PRODUCT_ID,
						modle.getProductId());
				param.putString(ItemActivity.REQUEST_DAP, modle.getDAP());
				//just reload 
				if(mActivity.freshProcWithBundle(param))
				{
					mActivity.addBundle(param);
					clean();
					mActivity.init(modle.getProductId());
					
				//ToolUtil.startActivity(mActivity, ItemActivity.class, param);
				
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ItemActivity.class.getName(), mActivity.getString(R.string.tag_ItemActivity), "0801" + (position + 1), 
						String.valueOf(mItemProductModel.getProductId()));
				ToolUtil.reportStatisticsPV(mActivity.getActivityPageId(),strDAP, String.valueOf(modle.getProductId()));
				}
				// mActivity.finish();
			}
		});

	}

	// 立即购买
	private void layout_buyImmediately() {

		if (mItemProductModel.getLowestNum() != 0
				&& getBuyCount() < mItemProductModel.getLowestNum()) {
			UiUtils.makeToast(mActivity, mActivity.getString(
					R.string.buy_min_msg_short,
					mItemProductModel.getLowestNum()));
			return;
		}
		Bundle param = new Bundle();
		param.putLong(OrderConfirmActivity.REQUEST_PRODUCT_ID,
				mItemProductModel.getProductId());
		param.putInt(OrderConfirmActivity.REQUEST_PRODUCT_BUYNUM, getBuyCount());
		param.putInt(OrderConfirmActivity.REQUEST_PAY_TYPE, payType);
		// 场景id
		if (mActivity.getChannelId() != 0) {
			param.putInt(OrderConfirmActivity.REQUEST_CHANNEL_ID,
					mActivity.getChannelId());
		}
		ToolUtil.checkLoginOrRedirect(mActivity, OrderConfirmActivity.class,
				param, -1);
	}

	// 加入/取消收藏
	public void addToCollect() {
		if(null == orderDetailButtonCollect)
			orderDetailButtonCollect = ((ImageView) mParent.findViewById(R.id.order_detail_button_collect));

		boolean isAddOperate = (orderDetailButtonCollect.getTag() == null);

		FavorControl mFavorControl = new FavorControl(mActivity);
		if (isAddOperate) {
			mActivity.showProgressLayer();
			mFavorControl.add(mItemProductModel.getProductId(),
					new OnSuccessListener<JSONObject>() {
						@Override
						public void onSuccess(JSONObject v, Response response) {
							mActivity.closeProgressLayer();
							final int errno = v.optInt("errno", -1);
							if (errno == 404 || errno == 0) {
								// 获取收藏后的favoriteID
								orderDetailButtonCollect.setTag(v.optLong("data"));
								orderDetailButtonCollect
										.setImageResource(R.drawable.ic_star_press);

								UiUtils.makeToast(mActivity,
										(errno == 404) ? R.string.had_favorite_msg:R.string.add_favorite_succ);
								
								AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
							} else {
								UiUtils.makeToast(mActivity,
										R.string.add_favorite_error);
							}
						}
					}, new OnErrorListener() {
						@Override
						public void onError(Ajax ajax, Response response) {
							mActivity.closeProgressLayer();
							UiUtils.makeToast(mActivity,
									R.string.add_favorite_error);
						}
					});
		} else {
			mActivity.showProgressLayer();
			// 取消 收藏的favoriteID
			mFavorControl.remove(mItemProductModel.getProductId(),
					(Long) orderDetailButtonCollect.getTag(),
					new OnSuccessListener<JSONObject>() {
						@Override
						public void onSuccess(JSONObject v, Response response) {
							mActivity.closeProgressLayer();
							final int errno = v.optInt("errno", -1);
							if (errno == 0) {
								orderDetailButtonCollect
										.setImageResource(R.drawable.ic_star);
								orderDetailButtonCollect.setTag(null);
								UiUtils.makeToast(mActivity,
										R.string.remove_favorite_success);
							} else {
								UiUtils.makeToast(mActivity,
										R.string.remove_favorite_error);
							}
						}
					}, new OnErrorListener() {
						@Override
						public void onError(Ajax ajax, Response response) {
							mActivity.closeProgressLayer();
							UiUtils.makeToast(mActivity,
									R.string.remove_favorite_error);
						}
					});
		}
	}

	public void notifyOnArrival() {
		this.notifyOnArrival(null != mItemProductModel ? mItemProductModel
				.getProductId() : 0);
	}

	/**
	 * notifyOnArrival Notify user when product arrival.
	 */
	private void notifyOnArrival(long nProductId) {
		if (0 >= nProductId)
			return;

		if (ILogin.getLoginUid() == 0) {
			UiUtils.makeToast(mActivity, R.string.need_login);
			ToolUtil.startActivity(mActivity, LoginActivity.class, null,
					ItemActivity.REQUEST_FLAG_ADD_NOTIFY);
			return;
		}

		// Send request for notification.
		Ajax pAjax = ServiceConfig.getAjax(Config.URL_ADD_PRODUCT_NOTICE);
		if (null == pAjax)
			return;
		pAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				final int nErrCode = null != v ? v.optInt("errno", -1) : -1;
				String strMessage = null != v ? v.optString("msg") : null;
				if (TextUtils.isEmpty(strMessage)) {
					final int nErrMsgId = 0 == nErrCode ? R.string.add_notify_success
							: R.string.add_notify_failed;
					strMessage = mActivity.getString(nErrMsgId);
				}

				UiUtils.makeToast(mActivity, strMessage);
			}
		});
		pAjax.setOnErrorListener(new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				mActivity.closeProgressLayer();
				UiUtils.makeToast(mActivity, R.string.add_notify_failed);
			}
		});
		pAjax.setParser(new JSONParser());

		// Set post value.
		pAjax.setData("pid", nProductId);
		pAjax.setData("email", "android-app");

		// Send the request.
		mActivity.addAjax(pAjax);
		mActivity.showProgressLayer();
		pAjax.send();

		StatisticsEngine.trackEvent(mActivity, "arrival_notify");
	}

	// 加入购物车
	public void addToShoppingCart() {
		if (ILogin.getLoginUid() == 0) {
			UiUtils.makeToast(mActivity, R.string.need_login);
			ToolUtil.startActivity(mActivity, LoginActivity.class, null,
					ItemActivity.REQUEST_FLAG_ADD_CART);
			return;
		}
		
		if(null == mItemProductModel) {
			UiUtils.makeToast(mActivity, mActivity.getString(R.string.add_cart_error));
			return;
		}

		if (mItemProductModel.getLowestNum() != 0
				&& getBuyCount() < mItemProductModel.getLowestNum()) {
			UiUtils.makeToast(mActivity, mActivity.getString(
					R.string.buy_min_msg_short,
					mItemProductModel.getLowestNum()));
			return;
		}
		// 节能补贴商品
		if (mItemProductModel.isESProduct()) {
			addToESShoppingCart();
			return;
		}
		int haveAdd = IShoppingCart.getBuyCount(mItemProductModel
				.getProductId());

		int wantBuyCount = haveAdd + getBuyCount();

		if (mItemProductModel.getNumLimit() != 0
				&& wantBuyCount > mItemProductModel.getNumLimit()) {
			UiUtils.makeToast(
					mActivity,
					mActivity.getString(R.string.buy_max_msg,
							mItemProductModel.getNumLimit(), haveAdd));
			return;
		}
		mShoppingCartProductModel = new ShoppingCartProductModel();
		mShoppingCartProductModel
				.setProductId(mItemProductModel.getProductId());
		mShoppingCartProductModel.setProductCharId(mItemProductModel
				.getProductCharId());
		mShoppingCartProductModel.setBuyCount(wantBuyCount);

		if (ILogin.getLoginUid() != 0) {// 添加到线上购物车
			addProductToShoppingCart(mItemProductModel);
		}

	}


	private void addProductToShoppingCart(ItemProductModel product) {
		final long uid = ILogin.getLoginUid();
		// 添加购物车标识出来场景id
		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_ADD_PRODUCTS);
		if (null == ajax)
			return;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("district", FullDistrictHelper.getDistrictId());
		data.put("uid", uid);
		data.put("chid", mActivity.getChannelId());
		// 商品id|数量|主商品id|多价格id|购买路径|商品类型0普通1套餐|场景id
		data.put(
				"ids",
				product.getProductId() + "|" + getBuyCount() + "|"
						+ product.getProductId() + "|0|"
						+ IcsonApplication.getPageRoute() + "|0|"
						+ mActivity.getChannelId());
		ajax.setData(data);
		ajax.setOnErrorListener(mActivity);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				final int errno = v.optInt("errno", -1);
				if (errno == 0) {
					// 如果立即购买，就进入购物车
					if (isbuyImmediately_Rules) {
						ShoppingCartActivity.loadShoppingCart(mActivity, true, true);
						ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemTabDetailView), ShoppingCartActivity.class.getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "02040");
					} else {// 添加到购物车，需要显示对话框
						// showDialog();
						onAddToCartAnimation();
					}
					
					AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_CART_RELOAD, "1", false);
				} else {
					String strErrMsg = v.optString("data");
					if (TextUtils.isEmpty(strErrMsg))
						strErrMsg = mActivity
								.getString(R.string.add_cart_error);
					UiUtils.makeToast(mActivity, strErrMsg);
				}
			}
		});
		mActivity.addAjax(ajax);
		mActivity.showProgressLayer();
		ajax.send();

	}

	public void onAddToCartAnimation() {
		UiUtils.makeToast(mActivity, R.string.add_cart_ok);
		
		TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
		shake.setInterpolator(new CycleInterpolator(6));
		shake.setDuration(600);
		shake.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 本地存储
				IShoppingCart.set(mShoppingCartProductModel);
				// 更新icon
				ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(
						mActivity);
				mShoppingCartCommunication.notifyDataSetChange();

				int num = IShoppingCart.getProductCount();
				if(null == shoppingCartNum)
					shoppingCartNum = (TextView) mActivity.findViewById(R.id.shopping_cart_num);
				shoppingCartNum.setText(num + "");
				shoppingCartNum.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
			}
		});
		if(null == shoppingCartView)
			shoppingCartView = (RelativeLayout) mActivity.findViewById(R.id.shopping_cart_view);
		shoppingCartView.startAnimation(shake);
	}

	// 选择尺寸，当前版本已经不用，都放到ItemTabDetailOptionAdapter中了
	private void selectSize() {
		final ArrayList<ProductOptionSizeModel> mProductOptionSizeModelList = mItemProductModel
				.getProductOptionSizeModelList();

		if (mProductOptionSizeModelList.size() == 0)
			return;

		int checkedItem = -1;
		String[] names = new String[mProductOptionSizeModelList.size()];
		for (int i = 0, len = names.length; i < len; i++) {
			ProductOptionSizeModel model = mProductOptionSizeModelList.get(i);
			names[i] = model.getSize();
			if (model.isSelected())
				checkedItem = i;
		}

		UiUtils.showListDialog(mActivity,
				mActivity.getString(R.string.item_choose_size), names,
				checkedItem, new RadioDialog.OnRadioSelectListener() {
					@Override
					public void onRadioItemClick(int which) {
						long selId = mProductOptionSizeModelList.get(which)
								.getProductId();
						if (selId != mItemProductModel.getProductId()) {
							firstExec = true;
							mActivity.init(selId);
						}
					}
				}, true);

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
				if(null == pProviceModels || pProviceModels.size()<= pos)
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
		{
			mProvinceDialog.setList(names, selectedIndex);
		}
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
			UiUtils.makeToast(mActivity, Config.NORMAL_ERROR);
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
		{
			mCityDialog.setList(names, selectedIndex);
		}
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
		{
			mZoneDialog.setList(names, selectedIndex);
		}
		mZoneDialog.show();
	}

	/**
	 * 
	 */
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
		
		//Update UI
		//empty not set yet || different, changed districtid
		if (nCityId == 0 || mZoneModel.getZoneId()!= mDistrictItem.mDistrictId) {
			if(null != mCityModel && null != mProvinceModel && null != mZoneModel) {
				FullDistrictItem pDistrictItem = new FullDistrictItem(mProvinceModel.getProvinceId(), mProvinceModel.getProvinceIPId(), mProvinceModel.getProvinceName(), mCityModel.getCityId(), mCityModel.getCityName(), mZoneModel.getZoneId(), mZoneModel.getZoneName());
				FullDistrictHelper.setFullDistrict(pDistrictItem);
				mDistrictItem = pDistrictItem;
			}

			// 城市切换后要刷新页面，拉取配送信息
			firstExec = true;
			mActivity.init(mActivity.getProductId());
		}
		
	}

	// 选择颜色,当前版本已经不用，都放到ItemTabDetailOptionAdapter中了
	private void selectColor() {
		final ArrayList<ProductOptionColorModel> mProductOptionColorModelList = mItemProductModel
				.getProductOptionColorModelList();

		if (mProductOptionColorModelList.size() == 0)
			return;

		int checkedItem = -1;
		String[] names = new String[mProductOptionColorModelList.size()];
		for (int i = 0, len = names.length; i < len; i++) {
			ProductOptionColorModel model = mProductOptionColorModelList.get(i);
			names[i] = model.getColor();
			if (model.isSelected()) {
				checkedItem = i;
			}
		}

		UiUtils.showListDialog(mActivity,
				mActivity.getString(R.string.item_choose_color), names,
				checkedItem, new RadioDialog.OnRadioSelectListener() {
					@Override
					public void onRadioItemClick(int which) {
						long selId = mProductOptionColorModelList.get(which)
								.getProductId();
						if (selId != mItemProductModel.getProductId()) {
							firstExec = true;
							mActivity.init(selId);
						}
					}
				}, true);
	}

	private void bindEvent(ViewGroup tab) {
		if(null == orderDetailButtonCollect)
			orderDetailButtonCollect = (ImageView) tab.findViewById(R.id.order_detail_button_collect);
		orderDetailButtonCollect.setOnClickListener(this);
		if(null == mCartButton)
			mCartButton = tab.findViewById(R.id.order_detail_button_cart);
		if(null!=mCartButton)
			mCartButton.setOnClickListener(this);
		
		if(null == mShareButton)
			mShareButton = tab.findViewById(R.id.order_detail_button_share);
		if(null!=mShareButton)
			mShareButton.setOnClickListener(this);
		if(null == orderDetailButtonBuynow)
			orderDetailButtonBuynow = (Button) tab.findViewById(R.id.order_detail_button_buynow);
		orderDetailButtonBuynow.setOnClickListener(this);
		if(null == itemDetailTextviewSizeValue)
			itemDetailTextviewSizeValue = (TextView) tab.findViewById(R.id.item_detail_textview_size_value);
		itemDetailTextviewSizeValue.setOnClickListener(this);
		if(null == itemDetailButtonSize)
			itemDetailButtonSize = (ImageButton) tab.findViewById(R.id.item_detail_button_size);
		itemDetailButtonSize.setOnClickListener(this);
		if(null == itemDetailTextviewColorValue)
			itemDetailTextviewColorValue = (TextView) tab.findViewById(R.id.item_detail_textview_color_value);
		itemDetailTextviewColorValue.setOnClickListener(this);
		if(null == itemDetailButtonColor)
			itemDetailButtonColor = (ImageButton) tab.findViewById(R.id.item_detail_button_color);
		itemDetailButtonColor.setOnClickListener(this);
		if(null == itemDetailTextviewStockValue)
			itemDetailTextviewStockValue = (TextView) tab.findViewById(R.id.item_detail_textview_stock_value);
		itemDetailTextviewStockValue.setOnClickListener(this);
		if(null == itemDetailImageStock)
			itemDetailImageStock = (ImageButton) tab.findViewById(R.id.item_detail_image_stock);
		itemDetailImageStock.setOnClickListener(this);

		downBtn.setOnClickListener(this);
		upBtn.setOnClickListener(this);

		if(buyNumEditText == null) {
			buyNumEditText = ((EditText) tab
					.findViewById(R.id.item_detail_edittext_buy_count));

			//必须先设置listener 后设置text
			buyNumEditText.addTextChangedListener(new TextWatcher() {

				private CharSequence lastInfo;
				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					String str = s.toString();
					int countInput = str.equals("") ? 1 : Integer.valueOf(str);
					if(countInput > Config.MAXNUM_PER_ORDER)
					{
						buyNumEditText.setText(lastInfo);
						UiUtils.makeToast(mActivity, mActivity.getString(R.string.buy_max_msg_short,Config.MAXNUM_PER_ORDER));
					}
					else
					{
						lastInfo = str;
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// buyNumEditText.selectAll();
				}

				@Override
				public void afterTextChanged(Editable s) {

					String str = buyNumEditText.getText().toString();

					int countInput = str.equals("") ? 1 : Integer.valueOf(str);
					int lowestNum = mItemProductModel.getLowestNum();
					if (!str.equals("") && countInput < lowestNum) {
						buyNumEditText.setText(String.valueOf(lowestNum));
						UiUtils.makeToast(mActivity, mActivity.getString(
								R.string.buy_min_msg_short, lowestNum));
						countInput = lowestNum;
					}

					int buyLimit = mItemProductModel.getNumLimit();
					if (buyLimit > 0 && countInput > buyLimit) {
						buyNumEditText.setText(String.valueOf(buyLimit));
						UiUtils.makeToast(mActivity, mActivity.getString(
								R.string.buy_max_msg_short, buyLimit));
						countInput = buyLimit;
					}
					buyNumEditText.setSelection(buyNumEditText.getEditableText()
							.length());
					mBuyCount = countInput;

					downBtn.setEnabled(mBuyCount > lowestNum);
					upBtn.setEnabled(mBuyCount < buyLimit);
					
					if(null!=mHandler)
						mHandler.removeCallbacksAndMessages(null);
					
					if(TextUtils.isEmpty(str))
					{
						if(null==mHandler)
							mHandler = new Handler();
						mHandler.postDelayed(new Runnable(){

							@Override
							public void run() {
								String str = buyNumEditText.getText().toString();
								if(TextUtils.isEmpty(str))
								{
									buyNumEditText.setText("" + mItemProductModel.getLowestNum());
								}
								mHandler.removeCallbacksAndMessages(null);
							}}, 1500);
					}
					
				
				}
			});
		}
		buyNumEditText.setText("");
		if (mItemProductModel.getLowestNum() > 1) {
			mBuyCount = mItemProductModel.getLowestNum();
		}
		buyNumEditText.setText(mBuyCount + "");
	}

	public int getBuyCount() {
		return mBuyCount;
	}

	@Override
	public void onClick(View v) {
		if(null == mItemProductModel || null == v)
			return;
		
		String strDAP = TextUtils.isEmpty(mActivity.getDAP()) ? "" : "DAP:"+mActivity.getDAP()+"|";
		switch (v.getId()) {
		// 添加收藏
		case R.id.order_detail_button_collect:
			if (ILogin.getLoginUid() == 0) {
				UiUtils.makeToast(mActivity, R.string.need_login);
				ToolUtil.startActivity(mActivity, LoginActivity.class, null,
						ItemActivity.REQUEST_FLAG_FAVOR);
				break;
			}
			addToCollect();
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21004", strDAP, String.valueOf(mActivity.getProductId()));
			StatisticsEngine.trackEvent(mActivity, "add_favorite", "productId=" + mItemProductModel.getProductId());
			break;
		case R.id.order_detail_button_cart: {
			// notifyOnArrival(mItemProductModel.getProductId());
			final int nSaleType = mItemProductModel.getSaleType();
			if (ProductModel.SALE_AVAILABLE == nSaleType) {
				addToShoppingCart();
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ESShoppingCartActivity.class.getName(), mActivity.getString(R.string.tag_ESShoppingCartActivity), "03015", 
						String.valueOf(mItemProductModel.getProductId()));
				
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21001", strDAP, String.valueOf(mItemProductModel.getProductId()), "", false);
				StatisticsEngine.trackEvent(mActivity, "add_shopping_cart", "productId=" + mItemProductModel.getProductId());
			} else if (ProductModel.SALE_EMPTY == nSaleType) {
				notifyOnArrival(mItemProductModel.getProductId());
				StatisticsEngine.trackEvent(mActivity, "notify_arrival", "productId=" + mItemProductModel.getProductId());
			}
		}
			break;
		case R.id.order_detail_button_buynow:
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21002", strDAP, String.valueOf(mItemProductModel.getProductId()), "", false);
			if (mItemProductModel.isESProduct()) {
				// 节能补贴商品，进入节能补贴活动选择页面
				addToESShoppingCart();
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ESShoppingCartActivity.class.getName(), mActivity.getString(R.string.tag_ESShoppingCartActivity), "03016", 
						String.valueOf(mItemProductModel.getProductId()));
				StatisticsEngine.trackEvent(mActivity, "add_es_shopping_cart", "productId=" + mItemProductModel.getProductId());
			} else if (mItemProductModel.getPromoRuleModelList().size() == 0) {
				// 如果没有促销活动，那么立即购买
				layout_buyImmediately();
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						OrderConfirmActivity.class.getName(), mActivity.getString(R.string.tag_OrderConfirmActivity), "03011", 
						String.valueOf(mItemProductModel.getProductId()));
				StatisticsEngine.trackEvent(mActivity, "detail_buy_now", "productId=" + mItemProductModel.getProductId());
			} else {// 如果有促销规则，那么进入购物车
				isbuyImmediately_Rules = true;
				addToShoppingCart();
				ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
						ShoppingCartActivity.class.getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "03012", 
						String.valueOf(mItemProductModel.getProductId()));
				StatisticsEngine.trackEvent(mActivity, "buy_now_via_cart", "productId=" + mItemProductModel.getProductId());
			}
			break;
		case R.id.item_detail_textview_size_value:
		case R.id.item_detail_button_size:
			selectSize();
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22004", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
		case R.id.item_detail_textview_color_value:
		case R.id.item_detail_button_color:
			selectColor();
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22004", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
//		case R.id.item_detail_edittext_buy_count:
//			selectBuyCount();
//			break;
		case R.id.item_detail_textview_stock_value:
		case R.id.item_detail_image_stock:
			selectAddress();
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22005", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;

		case R.id.order_detail_button_share:
			mActivity.showSharableApps();
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21003", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
		case R.id.item_detail_image_gjp:
			Bundle gjp = new Bundle();
			String gjp_url = mItemProductModel.getGJPURL();
			if (gjp_url == null || gjp_url.equals(""))
				gjp_url = Config.GJP_URL;

			gjp.putString(HTML5LinkActivity.LINK_URL, gjp_url);
			gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE,
					mActivity.getString(R.string.icson_gjp_jgbh_title));
			ToolUtil.startActivity(mActivity, HTML5LinkActivity.class, gjp);
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22003", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
		case R.id.item_detail_image_jgbh:
			Bundle jgbh = new Bundle();
			String jgbh_url = mItemProductModel.getJGBHURL();
			if (jgbh_url == null || jgbh_url.equals(""))
				jgbh_url = Config.JGBH_URL;

			jgbh.putString(HTML5LinkActivity.LINK_URL, jgbh_url);
			jgbh.putString(HTML5LinkActivity.ACTIVITY_TITLE,
					mActivity.getString(R.string.icson_gjp_jgbh_title));
			ToolUtil.startActivity(mActivity, HTML5LinkActivity.class, jgbh);
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22003", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
		case R.id.item_detail_downBtn:
			buyNumEditText.setText(String.valueOf(mBuyCount - 1));
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22005", strDAP, String.valueOf(mItemProductModel.getProductId()));
			break;
		case R.id.item_detail_upBtn:
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22005", strDAP, String.valueOf(mItemProductModel.getProductId()));
			if(mBuyCount>= Config.MAXNUM_PER_ORDER)
			{
				UiUtils.makeToast(mActivity, mActivity.getString(R.string.buy_max_msg_short,Config.MAXNUM_PER_ORDER));
			}
			else
				buyNumEditText.setText(String.valueOf(mBuyCount + 1));
			break;
		}
	}

	private void addToESShoppingCart() {

		if (mItemProductModel.getLowestNum() != 0
				&& getBuyCount() < mItemProductModel.getLowestNum()) {
			UiUtils.makeToast(mActivity, mActivity.getString(
					R.string.buy_min_msg_short,
					mItemProductModel.getLowestNum()));
			return;
		}
		Bundle param = new Bundle();
		param.putInt("BuyCount", getBuyCount());
		param.putInt(ESShoppingCartActivity.REQUEST_PAY_TYPE, payType);
		param.putSerializable("esRule", mItemProductModel.getESPromoRuleModel());
		param.putLong("esProduct", mItemProductModel.getProductId());
		ToolUtil.checkLoginOrRedirect(mActivity, ESShoppingCartActivity.class,
				param, -1);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		if (bulletContainer == null)
			return;

		mItemGalleryAdapter.setSelectItemIdx(position);
		View oldBullet = bulletContainer.getChildAt(lastBulletIndex);

		if (oldBullet != null && oldBullet instanceof ImageView) {
			((ImageView) oldBullet)
					.setBackgroundResource(R.drawable.gallery_line_normal);
		}

		View newBullet = bulletContainer.getChildAt(position);

		if (newBullet != null && newBullet instanceof ImageView) {
			((ImageView) newBullet)
					.setBackgroundResource(R.drawable.gallery_line_active);
		}

		lastBulletIndex = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position > -1 && null != mItemProductModel && position < mItemProductModel.getPicNum()) {
			ItemProductModel model = new ItemProductModel();
			model.setProductCharId(mItemProductModel.getProductCharId());
			model.setMainPic(mItemProductModel.getMainPic());
			model.setPicNum(mItemProductModel.getPicNum());
			model.setSaleModelType(mItemProductModel.getSaleModelType());
			Bundle param = new Bundle();
			param.putInt(ItemImageActivity.REQUEST_PIC_INDEX, position);
			param.putSerializable(ItemImageActivity.REQUEST_PRODUCT_MODEL,
					model);
			ToolUtil.startActivity(mActivity, ItemImageActivity.class, param);

			ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
					ItemImageActivity.class.getName(), mActivity.getString(R.string.tag_ItemImageActivity), "05011", 
					String.valueOf(mItemProductModel.getProductId()));
			
			String strDAP = TextUtils.isEmpty(mActivity.getDAP()) ? "" : "DAP:"+mActivity.getDAP()+"|";
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22001", strDAP, String.valueOf(mActivity.getProductId()));
		}
	}

	@Override
	public void destroy() {
		mItemProductParser = null;
		mItemGiftAdapter = null;
		mActivity = null;
		//mProductOptionColorModel = null;
		//mProductOptionSizeModel = null;
		mParent = null;
		mItemGalleryAdapter = null;
		bulletContainer = null;
		mItemProductModel = null;
		mCouponGiftModels = null; 
		mProductGiftModels = null;
		mGiftModels = null;

		browse_Gallery = null;
		buy_Gallery = null;
		
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		
		if(null != mProvinceDialog && mProvinceDialog.isShowing())
			mProvinceDialog.dismiss();
		mProvinceDialog = null;
		if(null != mCityDialog && mCityDialog.isShowing())
			mCityDialog.dismiss();
		mCityDialog = null;
		if(null != mZoneDialog && mZoneDialog.isShowing())
			mZoneDialog.dismiss();
		mZoneDialog = null;
		
		IShippingArea.clean();
	}

	public void setPayType(int aType) {
		this.payType = aType;
	}
	
	/**
	 * 
	* method Name:setListener    
	* method Description:  
	* @param ali   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void setListener(TabDetailSuccLisener ali)
	{
		mSuccListener = ali;
	}
}
