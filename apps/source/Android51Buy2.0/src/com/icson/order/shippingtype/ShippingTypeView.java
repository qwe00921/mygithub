package com.icson.order.shippingtype;

import java.util.ArrayList;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.order.shoppingcart.ShoppingCartModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class ShippingTypeView extends
		OrderBaseView<ShippingTypeModel, ArrayList<ShippingTypeModel>> {

	public double amt;
	public double shippingPrice;

	public ShippingTypeView(OrderConfirmActivity activity) {
		super(activity);
	}

	public void requestFinish() {
		mIsRequestDone = true;
		renderShippingType();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_SHIPPING_VIEW);
	}

	private void renderShippingType() {
		// 开始计算运费，如果是货到付款，去除分
		final ShoppingCartModel mShoppingCartModel = mActivity
				.getShoppingCartView().getModel();
		// 配送类型
//		TextField textViewShippingType = ((TextField) mActivity
//				.findViewById(R.id.orderconfirm_ship_type));
		// 实付金额
		TextView textViewAmt = ((TextView) mActivity
				.findViewById(R.id.orderconfirm_amt_value));
		
//		if (mModel == null) {
//			textViewShippingType.setContent(mActivity.getString(R.string.select_default));
//			textViewAmt.setText("0.00");
//		} else {
			// 实付金额
			amt = mShoppingCartModel.getTotalAmt()
					- mShoppingCartModel.getBenefits()
					- mShoppingCartModel.getTotalCut();
			
			updateShowAmt(amt);

//		}

//		textViewShippingType
//				.setOnDrawableRightClickListener(new OnClickListener() {
//					@Override
//					public void onClick(View arg0) {
//						selectShippingType();
//					}
//				});
	}

	public void setShippingType(ArrayList<ShippingTypeModel> models) {
		// 如果木有地址，那么配送方式也是空
		if (mActivity.getOrderAddressView().getModel() == null) {
//			mModels = null;
//			mModel = null;
			renderShippingType();
			return;
		}
		// 初始化ShippingId
		IPageCache cache = new IPageCache();
		String sShippingId = cache
				.get(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID);
		int shippingId = sShippingId == null ? 0 : Integer.valueOf(sShippingId);
		if (shippingId == 0
				&& mActivity.getOrderAddressView().getModel() != null) {
			shippingId = mActivity.getOrderAddressView().getModel()
					.getDefaultShipping();
		}

//		mModels = models;

//		if (mModels.size() < 1) {
//			UiUtils.makeToast(mActivity, "商品无法送达到该地区，请修改收货地址");
//			requestFinish();
//			return;
//		}

//		ShippingTypeModel tmpModel = null;
//		for (ShippingTypeModel model : mModels) {
//			if (model.getId() == shippingId) {
//				tmpModel = model;
//				break;
//			}
//		}

//		mModel = tmpModel == null ? mModels.get(0) : tmpModel;

		renderShippingType();
	}

	public void updateShowAmt(double _amt) {
		if (_amt < 0)
			_amt = 0;
		long coupon = mActivity.getCoupon();
		long point = mActivity.getPoint();
		shippingPrice = countShippingPrice();
		// 实付金额
		TextView textViewAmt = ((TextView) mActivity
				.findViewById(R.id.orderconfirm_amt_value));
		String amtStr = ToolUtil.toPrice(_amt - coupon - point + shippingPrice);
		textViewAmt.setText(Html.fromHtml(amtStr));

//		if (mModel == null)
//			return;
		// 配送类型
		TextField textViewShippingType = ((TextField) mActivity
				.findViewById(R.id.orderconfirm_ship_type));
		String str = 
//				mModel.getName() + 
				"("
				+ (shippingPrice == 0.0 ? "免运费"
						: ("运费 <font color=\"red\">&yen;"
								+ ToolUtil.toPrice(shippingPrice, 2) + "</font>"))
				+ ")";
		// 配送类型
//		textViewShippingType.setContent(Html.fromHtml(str));

		final ShoppingCartModel mShoppingCartModel = mActivity
				.getShoppingCartView().getModel();
		// 商品总价
		TextField tvTotalPrice = (TextField) mActivity
				.findViewById(R.id.orderconfirm_total_price);
		tvTotalPrice.setContent(mActivity.getString(R.string.rmb)
				+ ToolUtil.toPrice(mShoppingCartModel.getTotalAmt(), 2));
		// 运费
		TextField tvshippingPrice = (TextField) mActivity
				.findViewById(R.id.orderconfirm_shipping_cost);
		tvshippingPrice.setContent(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(shippingPrice, 2));
		if(coupon>0)
		{
			// 优惠券
			TextField tvCouponPrice = (TextField) mActivity
				.findViewById(R.id.orderconfirm_coupon_price);
			tvCouponPrice.setContent("-" + mActivity.getString(R.string.rmb) + ToolUtil.toPrice(coupon, 2));
			tvCouponPrice.setVisibility(View.VISIBLE);
		}
		// 积分抵扣
		TextField tvPointPrice = (TextField) mActivity.findViewById(R.id.orderconfirm_point_cost);
		if(point>0)
		{
			tvPointPrice.setContent("-" + mActivity.getString(R.string.rmb) + ToolUtil.toPrice(point, 2));
			tvPointPrice.setVisibility(View.VISIBLE);
		} else {
			tvPointPrice.setContent("0");
			tvPointPrice.setVisibility(View.GONE);
		}
		
		if(mShoppingCartModel.getTotalCut()>0)
		{
			// 返现优惠
			TextField tvTotalDiscount = (TextField) mActivity
				.findViewById(R.id.orderconfirm_total_discount);
			tvTotalDiscount.setContent("-" + mActivity.getString(R.string.rmb)
				+ ToolUtil.toPrice(mShoppingCartModel.getTotalCut(), 2));
			tvTotalDiscount.setVisibility(View.VISIBLE);
		}
		if(mShoppingCartModel.getBenefits()>0)
		{// 促销优惠
			TextField tvPromoDiscount = (TextField) mActivity
				.findViewById(R.id.orderconfirm_promo_discount);
			tvPromoDiscount.setContent("-" + mActivity.getString(R.string.rmb) + ToolUtil.toPrice(mShoppingCartModel.getBenefits(), 2));
			tvPromoDiscount.setVisibility(View.VISIBLE);
		}

	}

	// 配送方式
	public void selectShippingType() {
//		if (mModels == null) {
//			UiUtils.makeToast(mActivity, "请先填写收货地址");
//			return;
//		}
//
//		if (mModels.size() < 1) {
//			UiUtils.makeToast(mActivity, "商品无法送达到该地区，请修改收货地址");
//			return;
//		}
//		
//
//		int selectedIndex = 0;
//		ArrayList<String> names = new ArrayList<String>();
//		for (ShippingTypeModel model : mModels) {
//			names.add(model.getName());
//			if (mModel.getId() == model.getId()) {
//				selectedIndex = names.size() - 1;
//			}
//		}
//		
//		UiUtils.showListDialog(mActivity, mActivity.getString(R.string.orderconfirm_choose_shippingtype), (String[])names.toArray(new String[0]), selectedIndex, new RadioDialog.OnRadioSelectListener() {
//			@Override
//			public void onRadioItemClick(int which) {
//				//如果一样 不需要重新获取
//				if(mModel.getId() == mModels.get(which).getId())
//					return;
//				
//				mModel = mModels.get(which);
//				// cache it
//				IPageCache cache = new IPageCache();
//				cache.set(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID,
//						String.valueOf(mModel.getId()), 0);
//				requestFinish();
//			}
//		}, true);
	}

	public boolean setShippingTypePackage(OrderPackage pack) {

//		if (mModel == null) {
//			UiUtils.makeToast(mActivity, "请选择配送方式");
//			return false;
//		}
//
//		pack.put("shipType", mModel.getId());

		// TODO do we need choose this?
		pack.put("sign_by_other", 1);

		pack.put("shippingPrice", countShippingPrice());

		return true;

	}

	public double countShippingPrice() {
//		if (mModel == null)
//			return 0;

		// 当免运费类型为1，如果用户付账的金额（不包含运费）小于 免运费的额度，那么还是要计算运费
		ShoppingCartModel shopModel = mActivity.getShoppingCartView()
				.getModel();
		
		return shopModel.getTotalShipPrice();
//		double price = shopModel.getTotalAmt() - shopModel.getTotalCut();
//
//		//优惠券
//		long coupon_amt = 0;
//		if (mActivity.getCouponView() != null
//				&& mActivity.getCouponView().getCouponModel() != null) {
//			coupon_amt = mActivity.getCouponView().getCouponModel().coupon_amt;
//		}
//
//		double useAmt = price - coupon_amt;
//
//		if (mModel.getFreeType() == 1 && mModel.getFreeShippingLimit() > useAmt) {
//			return (mModel.getShippingCost() - mModel.getShippingPriceCut());
//		} else {
//			return (mModel.getShippingPrice() - mModel.getShippingPriceCut());
//		}

	}

	public void destroy() {
	}

	@Override
	public void onSuccess(ArrayList<ShippingTypeModel> v, Response response) {
		if (!mParser.isSuccess()) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			requestFinish();
			return;
		}

		// 初始化ShippingId
		IPageCache cache = new IPageCache();
		String sShippingId = cache
				.get(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID);
		int shippingId = sShippingId == null ? 0 : Integer.valueOf(sShippingId);
		if (shippingId == 0) {
			shippingId = mActivity.getOrderAddressView().getModel()
					.getDefaultShipping();
		}

//		mModels = v;
//
//		if (mModels.size() < 1) {
//			UiUtils.makeToast(mActivity, R.string.message_address_undeliverable);
//			requestFinish();
//			return;
//		}
//
//		ShippingTypeModel tmpModel = null;
//		for (ShippingTypeModel model : mModels) {
//			if (model.getId() == shippingId) {
//				tmpModel = model;
//				break;
//			}
//		}
//
//		mModel = tmpModel == null ? mModels.get(0) : tmpModel;

		requestFinish();
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		UiUtils.makeToast(mActivity, R.string.message_load_deliver_failed);
		requestFinish();
	}
}
