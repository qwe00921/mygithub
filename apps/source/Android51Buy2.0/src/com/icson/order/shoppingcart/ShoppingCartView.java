package com.icson.order.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.ShoppingCartGiftModel;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.order.shippingtype.ShippingTypeTimeModel;
import com.icson.shoppingcart.ProductCouponGiftModel;
import com.icson.shoppingcart.ShoppingCartControl;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class ShoppingCartView extends OrderBaseView<ShoppingCartModel, ShoppingCartModel> {

	private static final String LOG_TAG = ShoppingCartView.class.getName();

	private ShoppingCartControl mShoppingCartControl;

	private ShoppingCartProductAdapter mAdapter;

	public ShoppingCartView(OrderConfirmActivity activity) {
		super(activity);
		mShoppingCartControl = new ShoppingCartControl(mActivity);
		mParser = new ShoppingCartParser();
	}

	public void requestFinish() {
		mIsRequestDone = true;
		renderProductList();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_SHOPPINGCART_VIEW);
	}

	@Override
	public void onSuccess(ShoppingCartModel v, Response response) {
		if (!mParser.isSuccess()) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg(),true);
			mActivity.finish();
			return;
		}
		
		if(null != v) {
			//处理单品赠券，把优惠券和商品绑定
			ArrayList<ProductCouponGiftModel> couponModels =  v.getProductCouponGiftModels();
			ArrayList<SubOrderModel> subOrderModels = v.getSubOrders();
			if(null != subOrderModels && null != couponModels && couponModels.size() > 0) {
				ArrayList<ShoppingCartProductModel> productModels;
				ShoppingCartProductModel productModel;
				ProductCouponGiftModel couponModel;
				
				int subOrderLen = subOrderModels.size();
				for(int subOrderIdx = 0; subOrderIdx < subOrderLen; subOrderIdx ++) {
					productModels = subOrderModels.get(subOrderIdx).getProducts();
					
					int productLen = productModels.size();
					for(int nId = 0; nId < productLen; nId ++) {
						productModel = productModels.get(nId);
						final long productId = productModel.getProductId();
						
						int nCouponLen = couponModels.size();
						for(int nCouponIn = 0; nCouponIn < nCouponLen; nCouponIn ++ ) {
							couponModel = couponModels.get(nCouponIn);
							if(productId == couponModel.getProductId()) {
								productModel.setCouponGiftModel(couponModel);
							}
						}
						
					}
				}
			}
			
			mModel = v;
			requestFinish();
		}

	}

	public void getShoppingCartList(HashMap<String, Object> data) {
		mIsRequestDone = false;
		if (mActivity.isBuyImmediately()) {
			mShoppingCartControl.getBuyImmediatelyList((ShoppingCartParser) mParser,data, mActivity.getProductId(), mActivity.getBuyNum(),mActivity.getChannelId(), this, this);
		} else {
			data.remove("ism");
			mShoppingCartControl.getOrderShoppingCartList(mParser,data, this, this);
		}
	}

	private void renderProductList() {
		LinearListView listView = (LinearListView) mActivity.findViewById(R.id.orderconfirm_product_listview);

		listView.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				
				LinearListView item = new LinearListView(mActivity);
				item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
																ViewGroup.LayoutParams.WRAP_CONTENT));
				item.setOrientation(LinearLayout.VERTICAL);
				final SubOrderModel mSubOrderModel = mModel.getSubOrders().get(position);
				final ArrayList<ShoppingCartProductModel> pShoppingCartProductModels = mSubOrderModel.getProducts();

				if(getCount() > 1){
					int index = position +1;
					TextView view = (TextView) new TextView(mActivity);
					view.setPadding((int)(mActivity.getResources().getDisplayMetrics().density*15),
									(int)(mActivity.getResources().getDisplayMetrics().density)*5, 0, 0);
					view.setText("包裹"+index);
					item.addHeaderView(view);
				}
				if(position != getCount()-1)
				{
					View line = new View(mActivity);
					line.setLayoutParams(new ViewGroup.LayoutParams(
									ViewGroup.LayoutParams.FILL_PARENT,ToolUtil.px2dip(mActivity, 2)));
					line.setBackgroundColor(mActivity.getResources().getColor(R.color.background_color_settings_title));
					item.addFooterView(line);
				}
				mAdapter = new ShoppingCartProductAdapter(mActivity, pShoppingCartProductModels);
				item.setAdapter(mAdapter);
				
				return item;
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return mModel.getSubOrders().size();
			}
		});
	}
/*
	public boolean setOrder(OrderPackage pack) {
		pack.put("comment", mModel.getComment() == null ? "" : mModel.getComment());
		pack.put("sign_by_other", mModel.isSignByOther() ? "1" : "0");
		pack.put("coupon", "");
		pack.put("point", 0);

		double price = mModel.getTotalAmt() - mModel.getTotalCut();

		price = Math.round(price / 10.0) * 10.0;

		pack.put("Price", price);

		// TODO: 拆单
		final ShippingTypeModel mShippingTypeModel = mActivity.getShippingTypeView().getModel();
		pack.put("shippingPrice", mShippingTypeModel.getShippingPrice() - mShippingTypeModel.getShippingPriceCut());

		return true;
	}
	*/
	public String getProductItems(){
		StringBuilder str = new StringBuilder();
		String productComma = "";
		str.append("[");
		
		for(SubOrderModel model : mModel.getSubOrders())
		{
		ArrayList<ShoppingCartProductModel> products = model.getProducts();
		//[{"product_id":157137,"num":1},{"product_id":142705,"num":1}]
		for (ShoppingCartProductModel product : products) {
			str.append(productComma).append("{");
			str.append("\"product_id\":" + product.getProductId() + ",");
			str.append("\"chid\":" + mActivity.getChannelId() + ",");//享受场景id价格后会有优惠
			str.append("\"buy_count\":" + product.getBuyCount() );
			str.append("}");
			productComma = ",";
		}
		}
		str.append("]");
		
		return str.toString();
	}

	public boolean setSubOrders(OrderPackage pack) {

		ArrayList<SubOrderModel> models = mModel.getSubOrders();

		if (models == null || models.size() == 0) {
			UiUtils.makeToast(mActivity, "购物车为空");
			return false;
		}

		final StringBuilder str = new StringBuilder();
		String subOrderComma = "";

		ShippingTypeTimeModel itemTimeModel = null;
		ArrayList<ShippingTypeTimeModel> splitTimeModel = null;
		if(mActivity.getTimeAvaiableView().isCombineShipping())
		{
			itemTimeModel = mActivity.getTimeAvaiableView().getModel();
		}else
		{
			splitTimeModel = mActivity.getTimeAvaiableView().getSplitShippingModel();
		}
		str.append("{");
		// TODO: 拆单
		for (int idx = 0; idx < models.size();idx++)
		{
			str.append(subOrderComma);
			SubOrderModel model =  models.get(idx);
		//for (SubOrderModel model : models) {
			str.append("\"" + model.getItemId() + "\":{");
			str.append("\"price\":" + model.getPrice() + ",");
			str.append("\"psystock\":\"" + model.getPsystock() + "\",");

			str.append("\"shipPrice\":" + mActivity.getShippingTypeView().countShippingPrice() + ",");
			if(mActivity.getTimeAvaiableView().isCombineShipping())
			{
				str.append("\"expectDate\":\"" + ((itemTimeModel == null || itemTimeModel.getShip_date() == null) ? "" : itemTimeModel.getShip_date()) + "\",");
				if(null!=itemTimeModel && itemTimeModel.getTime_span_inlist_size()>0)//in span_list
					str.append("\"expectSpan\":\"" + itemTimeModel.getTime_span_inlist(model.getItemId()) + "\",");
				else//one package only
					str.append("\"expectSpan\":\"" + ((itemTimeModel == null) ? "" : itemTimeModel.getTime_span()) + "\",");
			}
			else
			{
				if(null == splitTimeModel || idx >= splitTimeModel.size())
				{
					str.append("\"expectDate\":\"\",");
					str.append("\"expectSpan\":\"\",");
				}
				else
				{
					itemTimeModel = splitTimeModel.get(idx);
					str.append("\"expectDate\":\"" + ((itemTimeModel == null || itemTimeModel.getShip_date() == null) ? "" : itemTimeModel.getShip_date()) + "\",");
					str.append("\"expectSpan\":\"" + ((itemTimeModel == null) ? "" : itemTimeModel.getTime_span()) + "\",");
				}
			}
			str.append("\"arrived_limit_time\":\"\",");

			//start of items[]
			str.append("\"items\":[");
			final ArrayList<ShoppingCartProductModel> products = model.getProducts();

			if (products == null || products.size() == 0) {
				Log.e(LOG_TAG, "setSubOrders|" + model.getItemId() + "|product is empty.");
				return false;
			}

			String productComma = "";
			for (ShoppingCartProductModel product : products) {
				str.append(productComma + "{");
				str.append("\"product_id\":" + product.getProductId() + ",");
				str.append("\"num\":" + product.getBuyCount() + ",");
				str.append("\"price_id\":\"0\",");//不再有多价id
				str.append("\"chid\":"+mActivity.getChannelId()+",");//场景id
				str.append("\"gift\":[");

				final ArrayList<ShoppingCartGiftModel> gifts = product.getShoppingCartGiftModels();

				if (gifts != null && gifts.size() != 0) {
					String giftComma = "";
					for (ShoppingCartGiftModel gift : gifts) {
						str.append(giftComma + gift.getProductId());
						giftComma = ",";
					}
				}

				str.append("]");//end of gift

				str.append("}");
				productComma = ",";
			}
			str.append("]"); //end of items
			
			str.append("}");
			subOrderComma = ",";
		}

		str.append("}");

		pack.put("suborders", str);

		double price = mModel.getTotalAmt() - mModel.getTotalCut();

		if (mActivity.getPayTypeView().getModel().getPayType() == 1) {
			price = ((int) (price * 10)) / 10.0f;
		}

		pack.put("Price", price);

		return true;
	}
	
	
	//单品赠券信息
	public boolean setCouponGiftItems(OrderPackage pack) {
		ArrayList<ProductCouponGiftModel> couponModels = mModel.getProductCouponGiftModels();
		if(null != couponModels && couponModels.size() > 0) {
			StringBuilder str = new StringBuilder();
			str.append("{");
			for(ProductCouponGiftModel couponModel : couponModels) {
				str.append("\"" + couponModel.getProductId() + "\"");
				str.append(":");
				str.append("\"" +couponModel.getCouponId() + "\"");
				str.append(",");
			}
			
			str.deleteCharAt(str.length() - 1);
			
			str.append("}");
			pack.put("promoCoupon", str);
		}
		
		return true;
	}

	/*
	 public boolean setSubOrders(OrderPackage pack) {

		ArrayList<SubOrderModel> models = mModel.getSubOrders();

		if (models == null || models.size() == 0) {
			UiUtils.makeToast(mActivity, "购物车为空");
			return false;
		}

		final StringBuilder str = new StringBuilder();
		String subOrderComma = "";

		str.append("{");
		// TODO: 拆单
		for (SubOrderModel model : models) {
			str.append("\"" + model.getItemId() + "\":{");
			str.append("\"price\":" + model.getPrice() + ",");

			final ShippingTypeModel mShippingTypeModel = mActivity.getShippingTypeView().getModel();

			final ArrayList<SubShippingTypeModel> mSubShippingTypeModelList = mShippingTypeModel.getSubShippingTypeModelList();

			for (SubShippingTypeModel mSubShippingTypeMode : mSubShippingTypeModelList) {
				if (mSubShippingTypeMode.getSubOrderId() == model.getItemId()) {
					str.append("\"shipPrice\":" + mActivity.getShippingTypeView().countShippingPrice() + ",");

					final ShippingTypeTimeModel mShippingTypeTimeModel = mActivity.getTimeAvaiableView().getModel();

					str.append("\"expectDate\":\"" + ((mShippingTypeTimeModel == null || mShippingTypeTimeModel.getShip_date() == null) ? "" : mShippingTypeTimeModel.getShip_date()) + "\",");
					str.append("\"expectSpan\":\"" + ((mShippingTypeTimeModel == null) ? "" : mShippingTypeTimeModel.getTime_span()) + "\",");
					str.append("\"arrived_limit_time\":\"\",");
				}
			}

			str.append("\"items\":[");

			final ArrayList<ShoppingCartProductModel> products = model.getProducts();

			if (products == null || products.size() == 0) {
				Log.e(LOG_TAG, "setSubOrders|" + model.getItemId() + "|product is empty.");
				return false;
			}

			String productComma = "";
			for (ShoppingCartProductModel product : products) {
				str.append(productComma + "{");
				str.append("\"product_id\":" + product.getProductId() + ",");
				str.append("\"num\":" + product.getBuyCount() + ",");
				str.append("\"price_id\":\"0\",");//不再有多价id
				str.append("\"chid\":"+mActivity.getChannelId()+",");//场景id
				str.append("\"gift\":[");

				final ArrayList<ShoppingCartGiftModel> gifts = product.getShoppingCartGiftModels();

				if (gifts != null && gifts.size() != 0) {
					String giftComma = "";
					for (ShoppingCartGiftModel gift : gifts) {
						str.append(giftComma + gift.getProductId());
						giftComma = ",";
					}
				}

				str.append("]");

				str.append("}");
				productComma = ",";
			}
			str.append("]");
			str.append("}");
			str.append(subOrderComma);
			subOrderComma = ",";

			// 不支持多单
			break;
		}

		str.append("}");

		pack.put("suborders", str);

		double price = mModel.getTotalAmt() - mModel.getTotalCut();

		if (mActivity.getPayTypeView().getModel().getPayType() == 1) {
			price = ((int) (price * 10)) / 10.0f;
		}

		pack.put("Price", price);

		return true;
	} 
	*/
	public void destroy() {
		super.destroy();
		if (mShoppingCartControl != null) {
			mShoppingCartControl.destroy();
			mShoppingCartControl = null;
		}
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		mActivity.onError(ajax, response);
	}
}
