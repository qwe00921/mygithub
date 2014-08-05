package com.icson.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.icson.R;
import com.icson.address.AddressListActivity;
import com.icson.address.AddressModel;
import com.icson.invoice.InvoiceActivity;
import com.icson.invoice.InvoiceListActivity;
import com.icson.item.ItemProductModel;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.IShoppingCart;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.model.AreaPackageModel;
import com.icson.lib.model.OrderModel;
import com.icson.lib.pay.cft.CFTPayActivity;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.InputDialog;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.order.coupon.CouponView;
import com.icson.order.invoice.InvoiceView;
import com.icson.order.paytype.PayTypeView;
import com.icson.order.shippingtype.CombineTimeAvaiableView;
import com.icson.order.shippingtype.ShippingTypeView;
import com.icson.order.shoppingcart.ShoppingCartView;
import com.icson.order.userpoint.UserPointModel;
import com.icson.order.userpoint.UserPointView;
import com.icson.shoppingcart.ShoppingCartCommunication;
import com.icson.statistics.StatisticsEngine;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class OrderConfirmActivity extends BaseActivity implements OnClickListener, OnSuccessListener<ItemProductModel>{
	private static final String LOG_TAG = OrderConfirmActivity.class.getName();
	public static final String REQUEST_GIFT_MODEL_LIST 			= "gift_model_list";
	public static final String REQUEST_ORDERSUCCESS_ORDER_ID 	= "order_id";
	public static final String REQUEST_PRODUCT_ID 				= "product_id";
	public static final String REQUEST_PRODUCT_BUYNUM 			= "product_num";
	public static final String REQUEST_CHANNEL_ID 				= "channel_id";
	public static final String REQUEST_USE_COUPON_TICKET                = "ticket";
	public static final String REQUEST_PAY_TYPE 				= "pay_type"; 
	public static final String REQUEST_PRULE_ID 				= "prule_id";
	public static final String REQUEST_PRULE_BENEFITS 			= "prule_benefits";
	public static final String REQUEST_ES_INFO 					= "ESInfo";
	public static final String REQUEST_SHOPPING_CART 			= "shopping_cart";
	
	public static final int VIEW_FLAG_SHOPPINGCART_VIEW = 1;
	public static final int VIEW_FLAG_ADDRESS_VIEW 		= 2;
	public static final int VIEW_FLAG_SHIPPING_VIEW 	= 3;
	public static final int VIEW_FLAG_SHIPPING_TIME_VIEW = 4;
	public static final int VIEW_FLAG_PAYTYPE_VIEW 		= 5;
	public static final int VIEW_FLAG_INVOICE_VIEW 		= 6;
	public static final int VIEW_FLAG_USERPOINT_VIEW 	= 7;
	
	private ESInfo esInfo;
	private InvoiceView mInvoiceView;
	private ShoppingCartView mShoppingCartView;
	private ShippingTypeView mShippingTypeView;
	private OrderAddressView mOrderAddressView;
	private CombineTimeAvaiableView mTimeAvaiableView;
	private PayTypeView mPayTypeView;
	private OrderControl mOrderControl;
	private UserPointModel mUserPointModel;
	private ScrollView mScrollView;
	private Rect mRect;
	
	private long productId;
	private int buyNum;
	private int payType;
	private int channel_id;//场景id
	//优惠促销规则prule和使用次数tms
	//private long prule;
	//private long prule_benefits;
	private   int  use_coupon_flag;
	
	//积分
	private UserPointView mUserPointView;
	//优惠券
	private CouponView mCouponView;
	private String mSmsCode = "";
	private String mMobile = "";
	private EditText mPointEditText;
	private PopupWindow mPopup;

	@SuppressWarnings("serial")
	public static class ESInfo implements Serializable{
		public int ism = 2;
		public int es_type = 0;
		public String es_name;
		public String es_idcard;
		public long es_benefit;
	};
	
	public InvoiceView getInvoiceView() {
		return mInvoiceView;
	}

	public ShoppingCartView getShoppingCartView() {
		return mShoppingCartView;
	}

	public ShippingTypeView getShippingTypeView() {
		return mShippingTypeView;
	}

	public OrderAddressView getOrderAddressView() {
		return mOrderAddressView;
	}

	public PayTypeView getPayTypeView() {
		return mPayTypeView;
	}

	public UserPointView getUserPointView() {
		return mUserPointView;
	}
	
	public CouponView getCouponView() {
		return mCouponView;
	}
	
	public CombineTimeAvaiableView getTimeAvaiableView() {
		return mTimeAvaiableView;
	}

	public long getProductId() {
		return productId;
	}

	// productId == 0 if shoppingCartActivity --> OrderConfirmActivity  xingyao 
	public boolean isBuyImmediately() {
		return productId != 0;
	}

	public int getBuyNum() {
		return buyNum;
	}
	
	public int getChannelId() {
		return channel_id;
	}
	
	public ESInfo getESInfo() {
		return esInfo;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_order_confirm);

		final long uid = ILogin.getLoginUid();

		if (uid == 0) {
			Log.e(LOG_TAG, "uid is 0");
			finish();
		}
		
		// Load nav bar.
		this.loadNavBar(R.id.orderconfirm_navigation_bar);

		Intent intent = getIntent();
		//productId == 0 if shoppingCartActivity --> OrderConfirmActivity  xingyao
		productId = intent.getLongExtra(REQUEST_PRODUCT_ID, 0);  

		buyNum = intent.getIntExtra(REQUEST_PRODUCT_BUYNUM, 0);
		buyNum = buyNum < 1 ? 1 : buyNum;
		
		payType = intent.getIntExtra(OrderConfirmActivity.REQUEST_PAY_TYPE, 0);
		//场景id
		channel_id = intent.getIntExtra(REQUEST_CHANNEL_ID,0);
		
		//促销规则
		//prule= intent.getLongExtra(REQUEST_PRULE_ID, 0);
		//prule_benefits = intent.getLongExtra(REQUEST_PRULE_BENEFITS, 0);
		
		//是否使用优惠券
		use_coupon_flag = intent.getIntExtra(REQUEST_USE_COUPON_TICKET, 0);
		Serializable sInfo = intent.getSerializableExtra(REQUEST_ES_INFO);
		esInfo = sInfo ==null? null: (ESInfo)sInfo ;

		// 配送时间
		//findViewById(R.id.orderconfirm_view_line30).setVisibility(View.GONE);

		// 收货地址
		findViewById(R.id.orderconfirm_address).setOnClickListener(this);
		findViewById(R.id.orderconfirm_invoice).setOnClickListener(this);
		
		// 优惠券信息
		TextField toCoupon = (TextField) findViewById(R.id.orderconfirm_coupon); 
		//if(prule == 0 && esInfo == null)//如果参加促销活动或者节能补贴，那么不能使用优惠券
		if(use_coupon_flag==0)
			toCoupon.setOnClickListener(this);
		else
			toCoupon.setSubCaption(getString(R.string.promo_no_coupon));
		// 确认订单
		findViewById(R.id.orderconfirm_button_submit).setOnClickListener(this);

		mOrderControl = new OrderControl(this);
		mShoppingCartView = new ShoppingCartView(this);
		mCouponView = new CouponView(this);
		
		StatisticsEngine.trackEvent(this, "go_order_confirm");
		init();
		
		
		//积分上的浮层
		LayoutInflater inflater = LayoutInflater.from(this);
		final View mView = inflater.inflate(R.layout.orderconfirm_point_popup, null);
		final TextView pPopupTextVeiw = (TextView) mView.findViewById(R.id.point_popup_textview);
		final int nWidth = Integer.valueOf(getString(R.string.point_popup_shift_width));
		final int nHeight = Integer.valueOf(getString(R.string.point_popup_shift_height));
		
		mPopup = new PopupWindow(mView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mPopup.setOutsideTouchable(true);  
		mPopup.setTouchable(true);
		
		mPointEditText = (EditText) findViewById(R.id.orderconfirm_point_value);
		mPointEditText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View view, boolean isFocused) {
				if(isFocused){
					if(mPointEditText.getText().toString().equals("0")){
						mPointEditText.setText("");
					}
					
					if( (null != mPopup) && (!mPopup.isShowing()) && (!TextUtils.isEmpty(mPointEditText.getText().toString())) && isBeenSeen() ){
							
						mPopup.dismiss();
						if(!isFinishing() && isBeenSeen()) {
							mPopup.showAsDropDown(mPointEditText, -nWidth, -nHeight);
							pPopupTextVeiw.setText(OrderConfirmActivity.this.getString(R.string.orderconfirm_point_popup, Float.parseFloat((mPointEditText.getText().toString().equals("") ? "0" : mPointEditText.getText().toString()))/10 ));
						}
					}
					ToolUtil.reportStatisticsClick(getActivityPageId(), "22009");
					
				}else{
					if( null != mPopup && mPopup.isShowing() ){
						mPopup.dismiss();
					}
				}
			}
		});
		
		mScrollView = (ScrollView)findViewById(R.id.global_container);
		mScrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
		            if (mPointEditText.isFocused()) {
		            	if( null == mRect ) {
		            		mRect = new Rect();
		            	}
		                mPointEditText.getGlobalVisibleRect(mRect);
		                if (!mRect.contains((int)event.getRawX(), (int)event.getRawY())) {
		                	mPointEditText.clearFocus();
		                    UiUtils.hideSoftInput(OrderConfirmActivity.this, mPointEditText);
		                }
		            }
		        }
				return false;
			}
		});
		
		mPointEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(mPointEditText.getText().toString().equals("0") || mPointEditText.getText().toString().equals("")) {
					if( null != mPopup && mPopup.isShowing() ){
						mPopup.dismiss();
					}
				}else{
					if( null != mPopup && !mPopup.isShowing() && isBeenSeen() && !isFinishing()){ //如果activity已经销毁，不要显示对话框
						mPopup.dismiss();
						mPopup.showAsDropDown(mPointEditText, -nWidth, -nHeight);
						pPopupTextVeiw.setText(OrderConfirmActivity.this.getString(R.string.orderconfirm_point_popup, Float.parseFloat((mPointEditText.getText().toString().equals("") ? "0" : mPointEditText.getText().toString()))/10 ));
					}
				}
				
				long coupon = 0;
				if(null !=  mCouponView && null != mCouponView.getCouponModel()){
					coupon = mCouponView.getCouponModel().coupon_amt;
				}
				
				String strInput = s.toString().equals("") ? "0" : s.toString();
				Boolean isEmpty = strInput.equals("0") ? true : false;
				long pNewInput = Long.parseLong(strInput);
				if(pNewInput < 0) {
					pNewInput = 0;
				}
				if(null != mUserPointModel) {
					
					if(0 < pNewInput) {
						
						boolean isInputValid = true;
						//如果输入的积分大于用户所拥有的积分
						if(pNewInput > mUserPointModel.getUserPoint()) {
							isInputValid = false;
							mPointEditText.setText(String.valueOf(mUserPointModel.getUserPoint()));
							
						} else if(pNewInput * 10 > mShippingTypeView.amt - coupon) { // 如果输入的积分大于订单金额
							isInputValid = false;
							if(mShippingTypeView.amt - coupon <= 0) {
								
								mPointEditText.setText(String.valueOf(0));
							} else {
								
								mPointEditText.setText(String.valueOf((long)((mShippingTypeView.amt - coupon) / 10)));
							}
						} else {
							isInputValid = true;
							mUserPointModel.setInputPoint(pNewInput);
							mUserPointView.updatePointView(true, isEmpty);
							pPopupTextVeiw.setText(OrderConfirmActivity.this.getString(R.string.orderconfirm_point_popup, (float)pNewInput / 10));
							updateShowAmt();
						}
						
						if(!isInputValid) {
							mPointEditText.setSelection(mPointEditText.getEditableText().length());
							long nOrderCanUsePoint = (long)(mShippingTypeView.amt- coupon);
							if(nOrderCanUsePoint < 0) {
								nOrderCanUsePoint = 0;
							}
							UiUtils.makeToast(OrderConfirmActivity.this, "本次最多只能使用" + (mUserPointModel.getUserPoint()*10 < nOrderCanUsePoint? mUserPointModel.getUserPoint() : (nOrderCanUsePoint)/10) + "积分");
						}
					} else if(0 == pNewInput) {
						mUserPointModel.setInputPoint(pNewInput);
						mUserPointView.updatePointView(true, isEmpty);
						updateShowAmt();
					}
				} 
			}
		});
		
	}

	@Override
	protected void onPause()
	{
		if (null != mPopup && mPopup.isShowing()) {
    		mPopup.dismiss();
    	}
		super.onPause();
	}
	
	private void init() {
		mMobile = "";
		mSmsCode = "";
		showLoadingLayer();
		//商品订单信息列表
		HashMap<String, Object> data = new HashMap<String, Object>();
		
		//初始化 上次订单使用信息
		IPageCache cache = new IPageCache();
		String said = cache.get(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		int nLastAddressId = said == null ? 0 : Integer.valueOf(said);
		
		String sdistrict = cache.get(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID);
		int nLastDistrictId = sdistrict == null ? 0 : Integer.valueOf(sdistrict);
		
		String siid = cache.get(CacheKeyFactory.CACHE_ORDER_INVOICE_ID);
		int iid = siid == null ? 0 : Integer.valueOf(siid);
		
		data.put("provinceId", FullDistrictHelper.getProvinceIPId());//省份id
		//检测缓存地址是否和当前三级地址一致
		int nNewDistrictId = FullDistrictHelper.getDistrictId();
		if( 0 != nLastDistrictId && nNewDistrictId == nLastDistrictId ){
			data.put("addressId", nLastAddressId);//上次使用地址id
			data.put("district", nLastDistrictId);//上次使用地址区域id
		}else{//缓存地址和当前省份不一致，那么传0
			data.put("addressId", 0);
			data.put("district", nNewDistrictId);
		}
		data.put("invoiceId", iid);//上次使用发票id
		
		/*
		if(prule != 0){//有促销规则，要写明
			data.put("prule", prule);
			data.put("benefits", prule_benefits);
		}*/
		
		data.put(REQUEST_USE_COUPON_TICKET, use_coupon_flag);
		
		if(esInfo != null){
			data.put("ism", 2);//节能补贴购物车
		}else if(isBuyImmediately()){
			data.put("ism", 3);//离线
		}
		
		//拉取订单确认页信息
		mShoppingCartView.getShoppingCartList(data);
	}

	public void ajaxFinish(int whichView) {
		switch (whichView) {
		//拉取购物信息OK
		case VIEW_FLAG_SHOPPINGCART_VIEW:
			closeLoadingLayer();
			//showLoadingLayer(false);
			//送货地址列表
			if (mOrderAddressView == null) {
				mOrderAddressView = new OrderAddressView(this);
			}
			mOrderAddressView.setAddress(mShoppingCartView.getModel().mAddressModel);
			//配送方式
			if (mShippingTypeView == null) {
				mShippingTypeView = new ShippingTypeView(this);
			}
//			mShippingTypeView.setShippingType(mShoppingCartView.getModel().mShippingTypeModelList);
			//TODO:
			mShippingTypeView.setShippingType(null);
			//送货时间
			if (mTimeAvaiableView == null) {
				mTimeAvaiableView = new CombineTimeAvaiableView(this);
			}
			mTimeAvaiableView.getTimeSpan();
			//支付方式
			if (mPayTypeView == null) {
				mPayTypeView = new PayTypeView(this);
			}

			mPayTypeView.setPayType(mShoppingCartView.getModel().mPayTypeModelList,payType);
			//发票信息
			if (mInvoiceView == null) {
				mInvoiceView = new InvoiceView(this);
			}
			//默认发票内容
			if(mShoppingCartView.getModel().mInvoiceModel!= null)
				mShoppingCartView.getModel().mInvoiceModel.setContent(mShoppingCartView.getModel().getInvoiceContentOpt().get(0));
			mInvoiceView.setInvoice(mShoppingCartView.getModel().mInvoiceModel);
			
			//积分显示
			if(mUserPointView == null){
				mUserPointView = new UserPointView(this);
			}
			mUserPointView.setUserPoint(mShoppingCartView.getModel().mUserPointModel);
			mUserPointModel = mUserPointView.getUserPointModel();
			//如果用户传递的促销规则，在订单拉取不到，那么默认失效
			/*
			if(prule !=0 && prule == mShoppingCartView.getModel().getPruleID()){
				((TextView) findViewById(R.id.orderconfirm_coupon_label)).setText("促销优惠");
				if(mShoppingCartView.getModel().getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH){
					((TextView) findViewById(R.id.orderconfirm_coupon_value)).setText("- ¥" + ToolUtil.toPrice(mShoppingCartView.getModel().getBenefits()));
				}else{
					((TextView) findViewById(R.id.orderconfirm_coupon_value)).setText("赠送优惠券");
				}
				findViewById(R.id.orderconfirm_imageview_arrow60).setVisibility(View.INVISIBLE);
				findViewById(R.id.orderconfirm_coupon_container).setOnClickListener(null);
			} else if(esInfo !=null){// 节能补贴
				((TextView) findViewById(R.id.orderconfirm_coupon_label)).setText("节能补贴");
				((TextView) findViewById(R.id.orderconfirm_coupon_value)).setText("已优惠¥" + ToolUtil.toPrice(esInfo.es_benefit));
				findViewById(R.id.orderconfirm_imageview_arrow60).setVisibility(View.INVISIBLE);
				findViewById(R.id.orderconfirm_coupon_container).setOnClickListener(null);
			}*/
			break;
		
		case VIEW_FLAG_SHIPPING_VIEW:
			showLoadingLayer(false);
			//费用变更，需要重新更新显示实付金额
			updateShowAmt();
			//送货时间
			if (mTimeAvaiableView == null) {
				mTimeAvaiableView = new CombineTimeAvaiableView(this);
			}
			mTimeAvaiableView.getTimeSpan();
			//支付方式
			//如果来自支付宝钱包独家跳转，不重新获取支付方式
			String callsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
			if(null!=callsource && callsource.equals("alipayapp"))
			{
				mPayTypeView.requestFinish();
				break;
			}
			
			if (mPayTypeView == null) {
				mPayTypeView = new PayTypeView(this);
			}

			mPayTypeView.getPayType();
			break;
		case VIEW_FLAG_PAYTYPE_VIEW:
			if (mPayTypeView != null && mPayTypeView.IsRequestDone()) {
				closeLoadingLayer();
			}
			break;
		/*case VIEW_FLAG_ADDRESS_VIEW:
			showLoadingLayer(false);
			
			//检测省份id变化，需要重新拉取
			if( !this.checkSiteId() )
			{
				//配送方式
				if (mShippingTypeView == null) {
					mShippingTypeView = new ShippingTypeView(this);
				}
				mShippingTypeView.getShippingType();
				
			}
			
			break;
			case VIEW_FLAG_ADDRESS_VIEW:
			showLoadingLayer(false);
			
			// Check site id.
			if( !this.checkSiteId() )
			{
				//配送方式
				if (mShippingTypeView == null) {
					mShippingTypeView = new ShippingTypeView(this);
				}
				mShippingTypeView.getShippingType();
				//发票信息
				if (mInvoiceView == null) {
					mInvoiceView = new InvoiceView(this);
				}

				mInvoiceView.getInvoiceList();
			}
			
			break;
		
		case VIEW_FLAG_PAYTYPE_VIEW:
			if (mPayTypeView != null && mPayTypeView.IsRequestDone()) {
				closeLoadingLayer();
			}
			//积分显示
			if(mUserPointView == null){
				mUserPointView = new UserPointView(this);
				long coupon =0;
				if(mCouponView.getCouponModel()!= null)
					coupon = mCouponView.getCouponModel().coupon_amt;
				
				long es_benefit = esInfo ==null ? 0 : esInfo.es_benefit;
				mUserPointView.getUserPoint(mShippingTypeView.amt- coupon - es_benefit);
			}
			break;
		case VIEW_FLAG_INVOICE_VIEW:
			if (mInvoiceView != null && mInvoiceView.IsRequestDone()) {
				closeLoadingLayer();
			}
			
			break;
		case VIEW_FLAG_SHIPPING_TIME_VIEW:
			break;
		case VIEW_FLAG_USERPOINT_VIEW:
			break;*/
		}

	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 收货地址
		case R.id.orderconfirm_address:
			if( null != mOrderAddressView )
			{
				mOrderAddressView.selectAddress();
				ToolUtil.reportStatisticsClick(getString(R.string.tag_OrderConfirmActivity), "22001");
			}
			break;
		
		// 发票信息
		case R.id.orderconfirm_invoice:
			if( null != mInvoiceView )
			{
				ToolUtil.reportStatisticsClick(getActivityPageId(), "22007");
				mInvoiceView.selectInvoice();
			}
			break;
		
		// 积分信息
//		case R.id.orderconfirm_point_container:
//			if( null != mCouponView ) {
//				long coupon =0;
//				if(mCouponView.getCouponModel()!= null)
//					coupon = mCouponView.getCouponModel().coupon_amt;
//				
//				//long es_benefit = esInfo ==null ? 0 : esInfo.es_benefit;
//				if( null != mUserPointView )
//					mUserPointView.showUserPointDialog(mShippingTypeView.amt- coupon ,mShippingTypeView.shippingPrice);
//			}
//			break;
		// 优惠券信息
		case R.id.orderconfirm_coupon:
			if( null != mUserPointView ) {
				long point =0;
				if(mUserPointView.getUserPointModel()!= null)
					point = mUserPointView.getUserPointModel().getInputPoint()*10;
				
				if( null != mCouponView ) {
					mCouponView.showCouponsList(mShippingTypeView.amt - point,
							mOrderAddressView.getDistrict(),
							mPayTypeView.getSelectedPayType(),
							mShoppingCartView.getProductItems(),isBuyImmediately());
				}
			}
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22008");
			break;
			
		// 确认订单
		case R.id.orderconfirm_button_submit:
			submit();
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			break;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if (null != mPopup && mPopup.isShowing()) {
	    		mPopup.dismiss();
	    	}
	    	
	        setResult(RESULT_OK);
	        finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	@Override
	public void onBackPressed(){
		setResult(RESULT_OK);
		finish();
	}


	
	public boolean setOrderpackage(OrderPackage pack) {

		pack.put("uid", ILogin.getLoginUid());

		if (null==mOrderAddressView || !mOrderAddressView.setAddressPackage(pack))
			return false;

		if (null==mShippingTypeView || !mShippingTypeView.setShippingTypePackage(pack))
			return false;

		if (null==mPayTypeView || !mPayTypeView.setpayTypePackage(pack))
			return false;

		if (null==mInvoiceView || !mInvoiceView.setInvoicePackage(pack))
			return false;
		
		if (null==mShoppingCartView || !mShoppingCartView.setSubOrders(pack) || !mShoppingCartView.setCouponGiftItems(pack))
			return false;

		//订单备注
		EditText comment = (EditText)findViewById(R.id.orderconfirm_beizhu_editText);
		pack.put("comment", comment.getEditableText().toString());
		//获取用户输入积分点数
		if(null==mUserPointView || !mUserPointView.setUserPoint(pack))
			return false;

		// 立即购买
		if (isBuyImmediately()) {
			pack.put("onekey", "1");
		}
		//节能补贴
		if(esInfo !=null){
			pack.put("ism", esInfo.ism);
			pack.put("es_type", esInfo.es_type);
			pack.put("es_name", esInfo.es_name);
			pack.put("es_idCard", esInfo.es_idcard);
		}
		
		/*if(prule > 0 && prule == mShoppingCartView.getModel().getPruleID()){
			pack.put("rule_id", prule);
			pack.put("benefits", prule_benefits);
			pack.put("couponCode", "");//参与促销不使用优惠券
		}else{
			//设置用户输入的优惠券代码
			mCouponView.setCoupon(pack);
		}*/
		
		// Add sms code.
		if( !TextUtils.isEmpty(mSmsCode) ) {
			pack.put("smscode", mSmsCode);
		}
		
		if( !TextUtils.isEmpty(mMobile) ){
			pack.put("mobile", mMobile);
		}

		// android添加
		pack.put("response_detail", "1");
		pack.put("ls", "--android--");
		
		// Set device id for order.
		pack.put("deviceId", StatisticsUtils.getDeviceUid(this));

		return true;

	}
	
	public void submit() {
		final OrderPackage pack = new OrderPackage();

		if (setOrderpackage(pack) == false)
			return;
		
		submitOrder(pack);
	}
	
	private void submitOrder(OrderPackage pack) {
		showProgressLayer("正在提交, 请稍后...");
		StatisticsEngine.trackEvent(this, "submit_order");
		mOrderControl.submitOrder(pack, new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				try {
					final int errno = v.getInt("errno");

					if (errno != 0) {
						
						// Report order failed.
						String strExtra = "errno->" + errno;
						StatisticsEngine.trackEvent(OrderConfirmActivity.this, "order_failed", strExtra);
						
						switch (errno) {
						case 6004:
							// 抱歉，您购物车中的商品与订单商品不一致，请刷新订单页面重新提交！
							UiUtils.showDialog(OrderConfirmActivity.this, R.string.caption_submit_failed, R.string.message_product_mismatch, R.string.btn_ok, new AppDialog.OnClickListener() {
								@Override
								public void onDialogClick(int nButtonId) {
									init();
								}
							});
							break;
						case 6003:
							// 抱歉，由于长时间未操作等原因，订单中的商品价格已有所变化，暂时无法提交订单。点击确定按钮后商品价格信息会进行更新，请您检查后重新提交订单
							UiUtils.showDialog(OrderConfirmActivity.this, R.string.caption_submit_failed, R.string.message_price_changed, R.string.btn_ok, new AppDialog.OnClickListener() {
								@Override
								public void onDialogClick(int nButtonId) {
									init();
								}
							});
							break;
						case 6002:
							// 赠品乎略
							if (!ToolUtil.isEmptyList(v, "data")) {
								final JSONObject error = v.getJSONObject("data");

								final int errCode = error.optInt("errCode");
								final String errMsg = error.optString("errMsg");

								//赠品忽略
								if (errCode == -100) {
									submitIngoreGift(TextUtils.isEmpty(errMsg) ? "赠品已送完，确定继续下单吗?" : errMsg);
								} else  if (-995 == errCode){
									//单品赠券，优惠券忽略
									submitIngoreCouponGift(TextUtils.isEmpty(errMsg) ? "优惠券数量不足，确定继续下单吗?" : errMsg);
								}else{
									String strMsg = TextUtils.isEmpty(errMsg) ? getString(R.string.gift_info_error) : errMsg;
									UiUtils.makeToast(OrderConfirmActivity.this, strMsg);
								}

							} else {
								UiUtils.makeToast(OrderConfirmActivity.this, R.string.message_system_busy);
							}
							break;
						case 7022: // SMS code expired.
						case 7023: // SMS code used too many times.
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								showRequestSmsDialog(strCaption, strMsg);
							}
							break;
						case 7105: // Frequency limit.
						case 7024: // SMS code error.
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								showSmsInputDialog(strCaption, strMsg, 0);
							}
							break;
						case 7002: // Need binding mobile
						case 7005:
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								
								// Show dialog for use to input sms code.
								showMobileInputDialog(strCaption, strMsg);
							}
							break;
						case 7006: // List mobile for user to select.
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								
								// Get mobile list.
								JSONArray aArray = v.optJSONArray("mobiles");
								final int length = (null != aArray ? aArray.length() : 0);
								if( length > 0 ) {
									String[] aMobiles = new String[length];
									final String strHotline = v.optString("hotline");
									for( int nIdx = 0; nIdx < length; nIdx++ ) {
										Object pChild = aArray.get(nIdx);
										if( null != pChild ) {
											aMobiles[nIdx] = pChild.toString();
										}
									}
									
									// Show dialog for mobile number selection.
									showMobileSelectionDialog(strCaption, strMsg, aMobiles, strHotline);
								}
							}
							break;
						case 7003: // Need input sms code.
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								final int nTimeout = v.optInt("timeout");
								
								// Show dialog for use to input sms code.
								showSmsInputDialog(strCaption, strMsg, nTimeout);
							}
							break;
						case 7004: // Call hot-line for support
							{
								String strCaption = v.optString("caption");
								String strMsg = v.optString("msg");
								final String strHotline = v.optString("hotline");
								
								String strDial = getString(R.string.call_support);
								String strCancel = getString(R.string.btn_cancel);
								
								AppDialog.OnClickListener pListener = new AppDialog.OnClickListener(){
									@Override
									public void onDialogClick(int nButtonId) {
										if( !TextUtils.isEmpty(strHotline) )
										{
											Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strHotline));
											AppUtils.checkAndCall(OrderConfirmActivity.this,pIntent);
										}
									}
								}; 
								
								UiUtils.showDialog(OrderConfirmActivity.this, strCaption, strMsg, strDial, strCancel, pListener);
							}
							break;
						default:
							{
								String strMsg = v.optString("msg");
								if( TextUtils.isEmpty(strMsg) ){
									strMsg = "系统繁忙，请稍候再试";
								}
								UiUtils.makeToast(OrderConfirmActivity.this, strMsg);
							}
							break;
						}
					} else {
						commitSuccess(v.getJSONObject("data"));
					}
				} catch (Exception ex) {
					Log.e(LOG_TAG, "submit|" + ToolUtil.getStackTraceString(ex));
					UiUtils.makeToast(OrderConfirmActivity.this, R.string.message_system_busy);
				}

			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				closeProgressLayer();
				UiUtils.makeToast(OrderConfirmActivity.this, R.string.network_error);
			}
		});
	}

	public void commitSuccess(JSONObject json) {

		// 提交订单按钮
		Button button = (Button) findViewById(R.id.orderconfirm_button_submit);
		button.setEnabled(false);
		button.setText("订单已生成");
		button.setTextColor(getResources().getColor(R.color.global_button_submit_d));

		// 清除本地购物车
		IShoppingCart.clear();

		// 更新icon
		ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(this);
		mShoppingCartCommunication.notifyDataSetChange();

		// 清空最后选择
		IPageCache cache = new IPageCache();
//		cache.remove(CacheKeyFactory.CACHE_ORDER_INVOICE_ID);
//		cache.remove(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		cache.remove(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID);
		cache.remove(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID);

		/*
		 * 
		 * data:
		 * 
		 * 'errCode'=>0, 'uid'=>$newOrder['uid'], 'orderId'=>$parentOrderId,
		 * 'orderAmt'=> $orderShipPrice + $orderPrice - $newOrder['point'] -
		 * $couponInfo['amt'], 'payType'=>$newOrder['payType'],
		 * 'payTypeIsOnline' =>
		 * $_PAY_MODE[$wh_id][$newOrder['payType']]['IsNet'], 'payTypeName' =>
		 * $_PAY_MODE[$wh_id][$newOrder['payType']]['PayTypeName'],
		 * 'orderTotalAmt'=>$orderShipPrice + $orderPrice, //订单总金额 'payGoodsAmt'
		 * => $product_cash, //订单客户支付的金额（去掉运费和享受到的其它优惠后的用户实际支付金额）
		 * 'orderCreateTime'=>$now, 'isParentOrder' => $orderNum > 1 ?
		 * true:false, 'isVATInvoice' => ($newOrder['invoiceType'] ==
		 * INVOICE_TYPE_VAT)? true:false, 'order_items' =>
		 * $newOrder['order_items']
		 */
		OrderModel mOrderModel = new OrderModel();
		try {
			mOrderModel.setOrderCharId(json.getString("orderId"));
			mOrderModel.setPayType(json.getInt("payType"));
			mOrderModel.setPayTypeIsOnline(json.getInt("payTypeIsOnline") == 1);
			mOrderModel.setPayTypeName(json.getString("payTypeName"));
			mOrderModel.setCash(json.getDouble("payGoodsAmt"));
			mOrderModel.setOrderCost(json.getDouble("orderTotalAmt"));
			mOrderModel.setUid(ILogin.getLoginUid());
			mOrderModel.setCouponSendRule(json.optString("singleCouponSendRule", ""));
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			UiUtils.makeToast(this, "订单生成成功但解析出错",true);
			mOrderModel = null;
		}

		if (mOrderModel == null) {
			finish();
			return;
		}

		OrderSuccessView mOrderSuccessView = new OrderSuccessView(this, mOrderModel);
		mOrderSuccessView.success();

		// Report order success
		StatisticsEngine.trackEvent(OrderConfirmActivity.this, "order_success", "orderCost=" + mOrderModel.getOrderCost());
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
	}

	public void orderFinish() {
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
		MainActivity.startActivity(this, MainActivity.TAB_MY);
		finish();
		/*Bundle pBundle = new Bundle();
		pBundle.putBoolean("backable", true);
		ToolUtil.startActivity(this, MyIcsonActivity.class, pBundle);
		
		*/
	}

	public void submitIngoreGift(String message) {
		UiUtils.showDialog(this, getString(R.string.caption_hint), message, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					OrderPackage pack = new OrderPackage();
					pack.put("ingoreLackOfGift", 1);
					if (setOrderpackage(pack) == false)
						return;
					submitOrder(pack);
				}
			}
		});
	}
	
	
	public void submitIngoreCouponGift(String message) {
		UiUtils.showDialog(this, getString(R.string.caption_hint), message, R.string.btn_continue_buy, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					OrderPackage pack = new OrderPackage();
					pack.put("ingorePromoCoupon", 1);
					if (setOrderpackage(pack) == false)
						return;
					submitOrder(pack);
				}
			}
		});
	}
	
	/**
	 * requestSmsCode
	 */
	private void requestSmsCode(String strMobile)
	{	
		Ajax pAjax = ServiceConfig.getAjax(Config.URL_SMSCODE_GET);
		if( null == pAjax )
			return ;
		
		showProgressLayer(getString(R.string.getting_smscode));
		final long uid = ILogin.getLoginUid();
		pAjax.setData("uid", uid);
		pAjax.setData("token", StatisticsUtils.getDeviceUid(this));
		if( !TextUtils.isEmpty(strMobile) )
		{
			pAjax.setData("mobile", strMobile); // Fill number if exists.
		}
		pAjax.setOnErrorListener(new OnErrorListener(){
			@Override
			public void onError(Ajax ajax, Response response) {
				mMobile = "";
				closeProgressLayer();
				UiUtils.makeToast(OrderConfirmActivity.this, R.string.get_smscode_failed);
			}
			
		});
		pAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				// Parse the sms code.
				final int errno = v.optInt("errno");
				if( (0 == errno) || (7105 == errno) ){
					String strCaption = v.optString("caption");
					String strMsg = v.optString("msg");
					final int nTimeout = v.optInt("timeout");
					
					// Show dialog for use to input sms code.
					showSmsInputDialog(strCaption, strMsg, nTimeout);
				} else {
					// Get sms code failed.
					UiUtils.makeToast(OrderConfirmActivity.this, R.string.get_smscode_failed);
				}
			}
		});
		this.addAjax(pAjax);
		pAjax.send();
	}
	
	/**
	 * showRequestSmsDialog
	 * @param strCaption
	 * @param strMsg
	 * @return
	 */
	private boolean showRequestSmsDialog(String strCaption, String strMsg)
	{
		if( TextUtils.isEmpty(strCaption) || TextUtils.isEmpty(strMsg) )
			return false;
		
		mSmsCode = "";
        UiUtils.showDialog(this, strCaption, strMsg, R.string.resend_sms, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if( AppDialog.BUTTON_POSITIVE == nButtonId ) {
					requestSmsCode(mMobile);
				}
			}
		});
		
		return true;
	}
	
	/**
	 * showMobileInputDialog
	 * @param strCaption
	 * @param strMsg
	 * @return
	 */
	private boolean showMobileInputDialog(String strCaption, String strMsg)
	{
		if( TextUtils.isEmpty(strCaption) || TextUtils.isEmpty(strMsg) )
			return false;
		
		mMobile = "";
		final InputDialog.Builder pBuilder = new InputDialog.Builder(this);
		pBuilder.setTitle(strCaption)
                .setMessage(strMsg)
                .setPositiveButton(R.string.bind_mobile_now, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	
                        	mMobile = pBuilder.getText();
                        	if( (TextUtils.isEmpty(mMobile)) || (mMobile.length() >= 16) ){
                        		UiUtils.makeToast(OrderConfirmActivity.this, R.string.mobile_input_error);
                        	} else {
                        		// Send request for update sms code.
                            	requestSmsCode(mMobile);
                            	dialog.dismiss();
                        	}
                        }
                })
                .setNegativeButton(R.string.btn_cancel, null);
        
        Dialog dialog = pBuilder.create();
        dialog.show();
		
		return true;
	}
	
	/**
	 * @param strCaption
	 * @param strMsg
	 * @return
	 */
	private boolean showMobileSelectionDialog(String strCaption, String strMsg, final String[] aMobiles, final String strHotline) {
		if( (TextUtils.isEmpty(strCaption)) || (TextUtils.isEmpty(strMsg)) || (null == aMobiles) || (0 >= aMobiles.length) )
			return false;
		
		mMobile = "";
		
		AlertDialog.Builder pBuilder = new AlertDialog.Builder(this);
		pBuilder.setTitle(strMsg);
	//	pBuilder.setMessage(strMsg);
		
		pBuilder.setSingleChoiceItems(aMobiles, -1, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	mMobile = aMobiles[item];
		    	// Send request for update sms code.
            	requestSmsCode(mMobile);
            	dialog.dismiss();
		    }
		}).setPositiveButton(R.string.call_service, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String strNumber = strHotline;
				if( TextUtils.isEmpty(strNumber) ) {
					strNumber = "400-828-1878";
				}
				Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strNumber));
				AppUtils.checkAndCall(OrderConfirmActivity.this,pIntent);
			}
			
		}).setNegativeButton(R.string.btn_cancel, null);
		
		AlertDialog pDialog = pBuilder.create();
		pDialog.show();
		
		return true;
	}
	
	/**
	 * showSmsInputDialog
	 * @param strCaption
	 * @param strMsg
	 * @param nTimeout
	 */
	private boolean showSmsInputDialog(String strCaption, String strMsg, int nTimeout)
	{
		if( TextUtils.isEmpty(strCaption) || TextUtils.isEmpty(strMsg) )
			return false;
		
		mSmsCode = "";
		final InputDialog.Builder pBuilder = new InputDialog.Builder(this);
		pBuilder.setTitle(strCaption)
                .setMessage(strMsg)
                .setPositiveButton(R.string.resend_sms, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	// Send request for update sms code.
                        	requestSmsCode(mMobile);
                        	
                        	dialog.dismiss();
                        }
                }, nTimeout)
                .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        	// Re-send for submit code.
                        	mSmsCode = pBuilder.getText();
                        	if( !TextUtils.isEmpty(mSmsCode) ){
                        		submit();
                            	dialog.dismiss();
                        	} else {
                        		// Show message for input sms code.
                        		UiUtils.makeToast(OrderConfirmActivity.this, R.string.smscode_empty);
                        	}
                        }
                }).setNeutralButton(R.string.btn_cancel, null);
        
        Dialog dialog = pBuilder.create();
        dialog.show();
        		
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		// invoice
		case InvoiceView.FLAG_REQUDST_INVOICE_CHECK:
			@SuppressWarnings("unchecked")
			ArrayList<Integer> aOpts = (null != data) ? (ArrayList<Integer>) data.getSerializableExtra(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT) : null;
			if( null != mInvoiceView ){
				mInvoiceView.setInvoiceContentSelectOpts(aOpts);
				//change default invoice
				if ((null != data) && (resultCode == InvoiceListActivity.FLAG_RESULT_SAVE_OK)) {
					mInvoiceView.onInvoiceConfirm(data);
				}
			}
			
			break;
		// 用户积分
//		case UserPointView.FLAG_REQUDST_USERPOINT:
//			if (resultCode == RESULT_OK) {
//				mUserPointView.onUserPointConfirm(data);
//				//费用变更，需要重新更新显示实付金额
//				updateShowAmt();
//			}
//			break;
		// address
		case OrderAddressView.FLAG_REQUEST_SHIPPING_ADDRESS:
			if (resultCode == AddressListActivity.FLAG_RESULT_ADDRESS_SAVE || resultCode == AddressListActivity.FLAG_RESULT_ADDRESS_DELETED) {
				if( null == mOrderAddressView ) {
					mOrderAddressView = new OrderAddressView(this);
				}
				
				AddressModel mModel = (AddressModel) data.getSerializableExtra(AddressListActivity.FLAG_RESULT_ADDRESS_MODEL);
				if(mModel == null) {
					this.setEmptyAddress();
					return ;
				}

				IPageCache cache = new IPageCache();
				//如果地址区域id相同，只需要更改cache
				if(String.valueOf(mModel.getDistrict()).equals(cache.get(CacheKeyFactory.CACHE_ORDER_DISTRICT_ID))){
					//设置并保存当前地址到cache
					mOrderAddressView.setAddress(mModel);
					return ;
				}
				
				//如果地址mModel变化了，需要刷新全部数据
				AreaPackageModel newAreaPackageModel = new AreaPackageModel(mModel.getDistrict());
				if(newAreaPackageModel.isEmptyOfPackage()) {
					UiUtils.makeToast(this, "您选择的地址非法，请重新选择。");
					return;
				}
				
				//设置并保存当前地址到cache
				mOrderAddressView.setAddress(mModel);
				
				String newProvince = newAreaPackageModel.getProvinceLable();
				int newSiteId = DispatchFactory.getSiteId(newProvince);
				if( newSiteId != ILogin.getSiteId() ){
					UiUtils.makeToast(this, "您所选择的站点发生变化，商品信息请以实际结算价格为准");
				}
				
				reloadOrder(newAreaPackageModel, newSiteId);
				
			}
			break;
			//优惠券
		case CouponView.FLAG_REQUDST_COUPON:
			if (null != mCouponView && resultCode == RESULT_OK) {
				mCouponView.onCouponConfirm(data);
				//费用变更，需要重新更新显示实付金额
				updateShowAmt();
			}
			break;
		case CFTPayActivity.REQUEST_CFT_PAY:
			if(resultCode == RESULT_OK){
				UiUtils.makeToast(this, "支付成功, 订单状态稍有迟延，请稍等.");
			}else{
				UiUtils.makeToast(this, "财付通支付失败！");
			}
			orderFinish();
			break;
		}
		
	}
	
	private void setEmptyAddress() {
		if(null != mOrderAddressView) {
			mOrderAddressView.setAddress(null);
		}
		
//		mOrderAddressView.setAddress(mShoppingCartView.getModel().mAddressModel);
//		//配送方式
//		if (mShippingTypeView == null) {
//			mShippingTypeView = new ShippingTypeView(this);
//		}
//		mShippingTypeView.setShippingType(mShoppingCartView.getModel().mShippingTypeModelList);
//		//送货时间
//		if (mTimeAvaiableView == null) {
//			mTimeAvaiableView = new CombineTimeAvaiableView(this);
//		}
//		mTimeAvaiableView.getTimeSpan();
		
		
		if(null != mShippingTypeView) {
			mShippingTypeView.setShippingType(null);
		}
		
		if(null != mTimeAvaiableView) {
			mTimeAvaiableView.getTimeSpan();
		}
		
		if(null != mPayTypeView) {
			mPayTypeView.setPayType(null, 0);
		}
		
		if(null != mInvoiceView) {
			mInvoiceView.setInvoice(null);
		}
	}
	/**
	 * 费用变更，需要重新更新显示实付金额
	 */
	private void updateShowAmt(){
		
		if(null != mShippingTypeView) {
			mShippingTypeView.updateShowAmt(mShippingTypeView.amt);
		}
	}
	
	public long getCoupon(){
		long coupon =0;
		if(mCouponView !=null && mCouponView.getCouponModel() != null)
			coupon = mCouponView.getCouponModel().coupon_amt;
		
		return coupon;
	}
	
	public long getPoint(){
		long point =0;
		if(mUserPointView !=null && mUserPointView.getUserPointModel() != null)
			point = mUserPointView.getUserPointModel().getInputPoint()*10;
		return point;
	}
	
	/**
	 * reloadOrder
	 */
	private void reloadOrder(AreaPackageModel model, int siteId)
	{
		// Get site id by province.
//		DispatchFactory.setDefaultCityId(strProvince);
		FullDistrictHelper.setFullDistrict(model, siteId);
		
		// Save the product item.
		this.init();
	}
	
	@Override
	public void onSuccess(ItemProductModel v, Response response)
	{
		this.closeLoadingLayer();
		
		if( null != v )
		{
			// Update buy number.
			if( buyNum > v.getNumLimit() )
			{
				buyNum = v.getNumLimit();
			}
			
		//	mDefault = mCurrent;
			
			// Save the product item.
			this.init();
		}
	}
	
	@Override
	public void onError(final Ajax ajax, final Response response)
	{
		closeLoadingLayer();
		
		super.onError(ajax, response);
	}
	
	
	@Override
	public void onDestroy() {

		

		if (mOrderControl != null) {
			mOrderControl = null;
		}

		if (mShoppingCartView != null) {
			mShoppingCartView.destroy();
			mShoppingCartView = null;
		}

		if (mOrderAddressView != null) {
			mOrderAddressView.destroy();
			mOrderAddressView = null;
		}

		if (mShippingTypeView != null) {
			mShippingTypeView.destroy();
			mShippingTypeView = null;
		}

		if (mTimeAvaiableView != null) {
			mTimeAvaiableView.destroy();
			mTimeAvaiableView = null;
		}

		if (mPayTypeView != null) {
			mPayTypeView.destroy();
			mPayTypeView = null;
		}

		if (mInvoiceView != null) {
			mInvoiceView.destroy();
			mInvoiceView = null;
		}
		
		if (mUserPointView != null) {
			mUserPointView.destroy();
			mUserPointView = null;
		}
		
		if (mCouponView != null) {
			mCouponView.destroy();
			mCouponView = null;
		}
		
		IShippingArea.clean();
		
		super.onDestroy();
	}

	@Override
	public boolean isShowSearchPanel() {
		return false;
	}
	
//	private int mDefault = 0;
//	private int mCurrent = 0;
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_OrderConfirmActivity);
	}
}
