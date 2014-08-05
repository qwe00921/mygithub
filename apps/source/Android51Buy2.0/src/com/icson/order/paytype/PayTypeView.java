package com.icson.order.paytype;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.address.AddressModel;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.ProductModel;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.order.shippingtype.ShippingTypeModel;
import com.icson.order.shoppingcart.ShoppingCartModel;
import com.icson.order.shoppingcart.SubOrderModel;
import com.icson.paytype.PayTypeControl;
import com.icson.paytype.PayTypeModel;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class PayTypeView extends OrderBaseView<PayTypeModel, ArrayList<PayTypeModel>> {

	private static final String LOG_TAG = PayTypeView.class.getName();

	private PayTypeControl mPayTypeControl;

	public PayTypeView(OrderConfirmActivity activity) {
		super(activity);
		mActivity = activity;
		mPayTypeControl = new PayTypeControl(mActivity);
		mParser = new PayTypeParser();
	}

	public void requestFinish() {
		mIsRequestDone = true;
		renderPayType();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_PAYTYPE_VIEW);
	}

	public int getSelectedPayType(){
		if(mModel==null){
			return -1;
		}
		return mModel.getPayType();
	};
	
	
	public void getPayType() {
		mIsRequestDone = false;
		mModels = null;
		mModel = null;

		final ShoppingCartModel mShoppingCartModel = mActivity.getShoppingCartView().getModel();
		final ShippingTypeModel mShippingTypeModel = mActivity.getShippingTypeView().getModel();

		if (mShoppingCartModel == null || mShippingTypeModel == null) {
			requestFinish();
			return;
		}

		String ids = "";
		for (SubOrderModel sub : mShoppingCartModel.getSubOrders()) {
			ArrayList<ShoppingCartProductModel> pros = sub.getProducts();
			if(null == pros || pros.size()<=0)
				continue;
			else
			{
				if (!ids.equals("")) {
					ids += ",";
				}
				ids += pros.get(0).getProductId();
			}
		}

		mPayTypeControl.getPayTypeList((PayTypeParser) mParser, mShippingTypeModel.getId(), ids, this, this);
	}

	public void selectPayType() {
		if (mModels == null) {
			UiUtils.makeToast(mActivity, "请先选择收货地址");
			return;
		}
		if (mModels.size() < 1) {
			UiUtils.makeToast(mActivity, "支付方式为空");
			Log.e(LOG_TAG, "payType list is empty.");
			return;
		}
		
		int selectedIndex = 0;
		ArrayList<String> names = new ArrayList<String>();

		for (PayTypeModel model : mModels) {
			names.add(model.getPayTypeName());
			if (mModel.getPayType() == model.getPayType()) {
				selectedIndex = names.size() - 1;
			}
		}
		
		UiUtils.showListDialog(mActivity, mActivity.getString(R.string.orderconfirm_choose_paytype), (String[])names.toArray(new String[0]), selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				mModel = mModels.get(which);
				//cache it
				IPageCache cache = new IPageCache();
				cache.set(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID, String.valueOf(mModel.getPayType()), 0);
				requestFinish();
			}
		}, true);
		
	}

	private void renderPayType() {
		EditField paytype = (EditField)mActivity.findViewById(R.id.orderconfirm_paytype);
		
		paytype.setContent(mModel == null ? "请选择..." : mModel.getPayTypeName());
		paytype.setOnDrawableRightClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22006");
				selectPayType();
			}
		});
		
		String priceTips = mActivity.getShoppingCartView().getModel().getPriceTips();
		
		View tvPriceTipsLayout = (View)mActivity.findViewById(R.id.orderconfirm_pricetips_layout);
		TextView tvPriceTips = (TextView)mActivity.findViewById(R.id.orderconfirm_pricetips);
		if(priceTips != null && !"".equals(priceTips)) {
			tvPriceTips.setText(priceTips);
			tvPriceTipsLayout.setVisibility(View.VISIBLE);
		} else {
			tvPriceTipsLayout.setVisibility(View.GONE);
		}
	}
	
	public void setPayType(ArrayList<PayTypeModel> v, int payType) {
		mModels = v;
		/*/for WX debug
		int xingyao = 0; //will warning
		PayTypeModel wxPay = new PayTypeModel();
		wxPay.setPayType(PayFactory.PAY_WX);
		wxPay.setPayTypeDesc("WeiXin Can Pay Though tenpay");
		wxPay.setPayTypeName("微信支付");
		mModels.add(wxPay);		
		//end*/
		
		
		//如果木有地址，那么支付方式也是空
		if(mActivity.getOrderAddressView().getModel()==null){
			mModels = null;
			mModel = null ;
			renderPayType();
			return ;
		}
		IPageCache cache = new IPageCache();
		int initPayTypeId = 0;
		if(payType == PayFactory.PAY_CFT)
		{
			initPayTypeId = payType;
		}
		else
		{
			String spayTypeId = cache.get(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID);
			final AddressModel mAddressModel = mActivity.getOrderAddressView().getModel();
			final int defaultPayTypeId = mAddressModel==null ? -1 :mAddressModel.getDefaultPayType();
			final int lastSelectPayTypeId = spayTypeId == null || spayTypeId.equals("") ? 0 : Integer.valueOf(spayTypeId);
			initPayTypeId = lastSelectPayTypeId == 0 ? defaultPayTypeId : lastSelectPayTypeId;
		}
		PayTypeModel tmpModel = null;

		for (PayTypeModel model : mModels) {
			if (model.getPayType() == initPayTypeId) {
				tmpModel = model;
			}
		}

		tmpModel = tmpModel == null ? (mModels.size() > 0 ? mModels.get(0) : null) : tmpModel;

		if (tmpModel != null) {
			mModel = tmpModel;
		}
		renderPayType();
	}

	public boolean setpayTypePackage(OrderPackage pack) {
		if (mModel == null) {
			UiUtils.makeToast(mActivity, "请选择配送方式");
			return false;
		}

		pack.put("payType", mModel.getPayType());
		return true;
	}

	@Override
	public void onSuccess(ArrayList<PayTypeModel> v, Response response) {

		if (!mParser.isSuccess()) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			requestFinish();
			return;
		}
		
		mModels = v;

		final AddressModel mAddressModel = mActivity.getOrderAddressView().getModel();

		final ShoppingCartModel mShoppingCartModel = mActivity.getShoppingCartView().getModel();

		IPageCache cache = new IPageCache();
		String spayTypeId = cache.get(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID);

		final int lastSelectPayTypeId = spayTypeId == null || spayTypeId.equals("") ? 0 : Integer.valueOf(spayTypeId);
		final int defaultPayTypeId = mAddressModel.getDefaultPayType();

		String ids = "";
		for (SubOrderModel sub : mShoppingCartModel.getSubOrders()) {
			for (ProductModel pro : sub.getProducts()) {
				if (!ids.equals("")) {
					ids += ",";
				}

				ids += pro.getProductId();
			}
		}

		final int initPayTypeId = lastSelectPayTypeId == 0 ? defaultPayTypeId : lastSelectPayTypeId;

		PayTypeModel tmpModel = null;

		for (PayTypeModel model : mModels) {
			if (model.getPayType() == initPayTypeId) {
				tmpModel = model;
			}
		}

		tmpModel = tmpModel == null ? (mModels.size() > 0 ? mModels.get(0) : null) : tmpModel;

		if (tmpModel != null) {
			mModel = tmpModel;
		}

		requestFinish();

	}

	@Override
	public void onError(Ajax ajax, Response response) {
		UiUtils.makeToast(mActivity, "加载支付信息失败, 请重试");
		requestFinish();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (mPayTypeControl != null) {
			mPayTypeControl.destroy();
			mPayTypeControl = null;
		}
	}
}
