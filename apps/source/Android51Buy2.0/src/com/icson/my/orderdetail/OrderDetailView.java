package com.icson.my.orderdetail;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.BaseView;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderFlowModel;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.pay.PayCore;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.more.AdviseActivity;
import com.icson.my.OrderStatus;
import com.icson.my.order.evaluate.OrderEvaluateActivity;
import com.icson.my.orderlist.MyOrderListAdapter;
import com.icson.order.OrderControl;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class OrderDetailView extends BaseView implements OnClickListener {
	private static final String LOG_TAG = OrderDetailView.class.getName();
	private OrderDetailActivity mActivity;
	private boolean isShowDetail;
	private View mFooterView;
	private OrderControl mOrderControl;
	private OrderModel mOrderModel;
	private PayCore mPayCore;
	private String mOrderInfo;
	private OrderDetailProductAdapter mAdapter;
	private OrderFlowModel mOrderFlowModel;
	private OrderFlowAdapter mOrderFlowAdapter;

	public OrderDetailView(OrderDetailActivity activity) {
		mActivity = activity;

		mOrderControl = new OrderControl(mActivity);
	}

	public OrderModel getOrderModel() {
		return mOrderModel;
	}

	public void getOrderInfo(String orderCharId) {

		mActivity.showLoadingLayer();
		mOrderControl.getOrderDetail(orderCharId,
				new OnSuccessListener<JSONObject>() {
					@Override
					public void onSuccess(JSONObject v, Response response) {
						mActivity.closeLoadingLayer();
						try {
							if (v.getInt("errno") != 0) {
								mOrderModel = null;
								UiUtils.makeToast(mActivity, v.optString("data") == null ? Config.NORMAL_ERROR
										: v.optString("data"));
							} else {
								mOrderModel = new OrderModel();
								JSONObject json = v.optJSONObject("data");
								mOrderInfo = v.getString("data");
								mOrderModel.parse(json);
							}
						} catch (Exception ex) {
							Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
							mOrderModel = null;
						}

						if (mOrderModel != null) {
							renderOrderInfo();
						}
					}
				}, new OnErrorListener() {
					@Override
					public void onError(Ajax ajax, Response response) {
						((TextView) mActivity
								.findViewById(R.id.orderdetail_textview_loading))
								.setText("查询失败, 请稍后再试.");
						mActivity.findViewById(R.id.orderdetail_loadingbar)
								.setVisibility(View.GONE);
					}
				});
	}

	public void getOrderFlow(String orderCharId) {

		mOrderControl.getOrderFlow(orderCharId,
				new OnSuccessListener<OrderFlowModel>() {
					@Override
					public void onSuccess(OrderFlowModel v, Response response) {
						if(null == v) {
							mActivity.findViewById(
									R.id.orderdetail_linear_orderflow)
									.setVisibility(View.GONE);
							return;
						}
						mActivity.findViewById(
								R.id.orderdetail_linear_orderflow)
								.setVisibility(View.VISIBLE);
						mOrderFlowModel = v;
						mOrderFlowAdapter = new OrderFlowAdapter(mActivity,
								mOrderFlowModel);
						((LinearListView) mActivity
								.findViewById(R.id.orderdetail_linear_orderflow))
								.setAdapter(mOrderFlowAdapter);
						
						if (mOrderFlowModel.isShowMap()) {
							mActivity.findViewById(R.id.orderdetail_layout_map)
									.setVisibility(View.VISIBLE);
							mActivity.findViewById(R.id.orderdetail_layout_map)
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21003");
											mActivity.startMap();
										}
									});
							mActivity.findViewById(R.id.seperator_5).setVisibility(View.VISIBLE);
						}
						// if(mOrderFlowModel.getTotal() !=null )
						// ((TextView)mActivity.findViewById(
						// R.id.orderdetail_textview_flow_total_time)).setText(Html.fromHtml("您的订单处理时间共<strong>"
						// + mOrderFlowModel.getTotal() + "</strong>"));
						// 第三方流水
						/*
						 * if (mOrderFlowModel.getThirdSysno() != null &&
						 * mOrderFlowModel.getThirdType() != 0) { ((TextView)
						 * mActivity
						 * .findViewById(R.id.orderdetail_textview_loading))
						 * .setText("正在从第三方快递查询流水,请稍候...");
						 * getOrderFlowFromThird(); } else
						 */{
							mActivity.findViewById(
									R.id.orderdetail_linear_loading)
									.setVisibility(View.GONE);
						}
					}
				}, new OnErrorListener() {
					@Override
					public void onError(Ajax ajax, Response response) {
						((TextView) mActivity
								.findViewById(R.id.orderdetail_textview_loading))
								.setText("查询失败, 请稍后再试.");
						mActivity.findViewById(R.id.orderdetail_loadingbar)
								.setVisibility(View.GONE);
					}
				});
	}

	private void setButtonStatus() {
		// 显示物流和客服电话
		mActivity.findViewById(R.id.orderdetail_wuliu_button)
				.setOnClickListener(this);
		mActivity.findViewById(R.id.orderdetail_callphone_button)
				.setOnClickListener(this);
		//拆单的订单 不能取消订单，不能支付
		if(!mOrderModel.getPackageOrderId().equals(mOrderModel.getOrderCharId()))
		{
			mActivity.findViewById(R.id.orderdetail_relative_pay)
				.setVisibility(View.GONE);
			mActivity.findViewById(R.id.orderdetail_button_cancel)
				.setVisibility(View.GONE);
			mActivity.findViewById(R.id.orderdetail_button_pay)
				.setVisibility(View.GONE);
			return;
		}
		
		final int orderStatus = mOrderModel.getStatus();
		boolean showCancel =  mOrderModel.isCanCancel() && OrderStatus.canCancel(orderStatus);
		// check cancel button show or gone
		if (showCancel) {
			mActivity.findViewById(R.id.orderdetail_button_cancel)
					.setVisibility(View.VISIBLE);
		} else {
			mActivity.findViewById(R.id.orderdetail_button_cancel)
					.setVisibility(View.GONE);
		}

		boolean showPay = mOrderModel.isNeedPay() && 
				(mPayCore = PayFactory.getInstance(mActivity, mOrderModel.getPayType(), mOrderModel.getOrderCharId(), false)) != null;
		mActivity.findViewById(R.id.orderdetail_button_pay).setVisibility(showPay ? View.VISIBLE : View.GONE);
		
		boolean showLayout = showCancel && showPay;
		mActivity.findViewById(R.id.orderdetail_relative_pay).setVisibility(showLayout ? View.VISIBLE : View.GONE);

	}

	public void cancelOrder(boolean withoutNotice) {
		if (withoutNotice == false) {
			UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_order_cancel, R.string.btn_yes, R.string.btn_no, new AppDialog.OnClickListener() {
				
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE) {
						cancelOrder(true);
					}
				}
			});
			return;
		}
		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderDetailActivity), OrderDetailActivity.class.getName(), mActivity.getString(R.string.tag_OrderDetailActivity), "03012");
		final String orderCharId = mOrderModel.getOrderCharId();

		OnSuccessListener<JSONObject> success = new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				if (v.optInt("errno", -1) == 0) {
					mActivity.setIsOperate(true);
					mActivity.initOrderDetailView();
					
					// Report for canceling order.
					StatisticsEngine.trackEvent(mActivity, "cancel_order", "orderId=" + orderCharId);
					AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
				} else {
					String data = v.optString("data", "");
					data = data.equals("") ? Config.NORMAL_ERROR : data;
					UiUtils.makeToast(mActivity, data);
				}
			}
		};

		mActivity.showProgressLayer("正在取消订单, 请稍候...");
		mOrderControl.orderCancel(orderCharId, false, success, mActivity);
	}

	public void destroy() {
		mOrderControl = null;
		mActivity = null;
		mOrderModel = null;
		mPayCore = null;
		mOrderInfo = null;
		mAdapter = null;
		mOrderFlowModel = null;
		mOrderFlowAdapter = null;
	}

	public void renderOrderInfo() {

		// 订单号
		// int status = mOrderModel.getStatus();
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_order_id))
				.setText(Html.fromHtml("订单号 : " + mOrderModel.getOrderCharId() + "<color=\"666666\"   共"
						+ mOrderModel.getBuyNum() + "件</color>"));
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_order_status))
				.setText(Html.fromHtml(MyOrderListAdapter
						.getStatusHTML(mOrderModel)));
		// 收货地址
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_receive_addr))
				.setText("收货地址 : "
						+ mOrderModel.getReceiverAddress().replaceAll(
								"^(.+市){2}", "$1"));
		// 收货人
		String receive_name = "收　货人 : "
				+ mOrderModel.getReceiver()
				+ "  "
				+ (mOrderModel.getReceiverMobile().equals("") ? mOrderModel
						.getReceiverTel() : mOrderModel.getReceiverMobile())
				+ "";
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_receive_name))
				.setText(receive_name);
		// 成交时间
		final String orderTime = "下单时间 : "
				+ ToolUtil.toDate(mOrderModel.getOrderDate() * 1000);
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_time))
				.setText(orderTime);
		// 配送时间
		if ("易迅快递".equals(mOrderModel.getShippingTypeName())) {
			mActivity.findViewById(R.id.orderdetail_textview_shipping_time)
					.setVisibility(View.VISIBLE);
			((TextView) mActivity
					.findViewById(R.id.orderdetail_textview_shipping_time))
					.setText("配送时间 : " + mOrderModel.getExpectDlyTime());
		} else {
			mActivity.findViewById(R.id.orderdetail_textview_shipping_time)
					.setVisibility(View.GONE);
		}
		// 配送方式
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_shipping_name))
				.setText("配送方式 : " + mOrderModel.getShippingTypeName());

		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_invoice_name))
				.setText("发票抬头 : " + mOrderModel.getInvoiceTitle());
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_invoice_type))
				.setText("发票信息 : " + mOrderModel.getInvoiceType());
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_invoice_content))
				.setText("发票内容 : " + mOrderModel.getInvoiceContent());

		mActivity.findViewById(R.id.orderdetail_relative_bottom_tab)
				.setOnClickListener(this);

		String priceTips = mOrderModel.getPriceTips();
		
		View tvPriceTipsLayout = (View)mActivity.findViewById(R.id.orderdetail_pricetips_layout);
		TextView tvPriceTips = (TextView)mActivity.findViewById(R.id.orderdetail_pricetips);
		if(priceTips != null && !"".equals(priceTips)) {
			tvPriceTips.setText(priceTips);
			tvPriceTipsLayout.setVisibility(View.VISIBLE);
		} else {
			tvPriceTipsLayout.setVisibility(View.GONE);
		}
		
		// 总金额
		String price = "总　　额 : <font color=\"red\">¥"
				+ ToolUtil.toPrice(mOrderModel.getCash()) + "</font>";
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_total))
				.setText(Html.fromHtml(price));

		// 付款方式
		String pay = mOrderModel.getPayTypeName();
		int PAY_LABEL_MAX = 5;
		pay = pay.substring(0, Math.min(PAY_LABEL_MAX, pay.length()))
				+ (pay.length() > PAY_LABEL_MAX ? "..." : "");
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_pay_type_name))
				.setText("(" + pay + ")");

		setButtonStatus();

		renderProductList();

	}

	private void renderProductList() {
		final LinearListView listView = (LinearListView) mActivity
				.findViewById(R.id.orderdetail_linear_order_list);
		listView.removeAllViews();

		if (mOrderModel == null) {
			listView.setVisibility(View.GONE);
			return;
		}
		listView.setVisibility(mOrderModel.getOrderProductModelList().size() > 0 ? View.VISIBLE
				: View.GONE);

		final ArrayList<OrderProductModel> products = mOrderModel
				.getOrderProductModelList();

		listView.setVisibility(products.size() > 0 ? View.VISIBLE : View.GONE);

		if (products.size() > 0) {
			mAdapter = new OrderDetailProductAdapter(mActivity, products);
			mAdapter.setShowAll(mOrderModel.isShowAll());
			if (products.size() > 1) {
				if (mOrderModel.isShowAll()) {
					mFooterView = mActivity.getLayoutInflater().inflate(
							R.layout.my_orderdetail_product_item_footer, null);
					((TextView) mFooterView
							.findViewById(R.id.orderdetail_products_bottom_tab_tv))
							.setText("收起商品");

					((ImageView) mFooterView
							.findViewById(R.id.orderdetail_products_bottom_tab_img))
							.setImageResource(R.drawable.ic_up);
				} else {
					mFooterView = mActivity.getLayoutInflater().inflate(
							R.layout.my_orderdetail_product_item_footer, null);
					((TextView) mFooterView
							.findViewById(R.id.orderdetail_products_bottom_tab_tv))
							.setText("其他"
									+ (mOrderModel.getLeftNum()) + "件商品");

				}
				listView.addFooterView(mFooterView);
				mFooterView.setOnClickListener(this);
			}
			listView.setAdapter(mAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					OrderProductModel model = (OrderProductModel) mAdapter
							.getItem(position);
					// 可以评论,并且未评论
					if (model.isCanEvaluate() && !model.isEvaluated()) {
						Bundle param = new Bundle();
						param.putSerializable(
								OrderEvaluateActivity.ORDER_PRODUCT_MODEL,
								model);
						ToolUtil.startActivity(mActivity,
								OrderEvaluateActivity.class, param,
								OrderDetailActivity.REQUEST_CODE);
						ToolUtil.sendTrack(mActivity.getClass().getName(), 
								mActivity.getString(R.string.tag_OrderDetailActivity), 
								OrderEvaluateActivity.class.getName(), 
								mActivity.getString(R.string.tag_OrderEvaluateActivity), "01011");
					} else if (model.getGiftCount() == 0) {
						Bundle param = new Bundle();
						param.putLong(ItemActivity.REQUEST_PRODUCT_ID, id);
						ToolUtil.startActivity(mActivity, ItemActivity.class,
								param);
						
						ToolUtil.sendTrack(mActivity.getClass().getName(), 
								mActivity.getString(R.string.tag_OrderDetailActivity), 
								ItemActivity.class.getName(), 
								mActivity.getString(R.string.tag_ItemActivity), "01012", String.valueOf(id));
					} else {
						Bundle param = new Bundle();
						param.putLong(
								OrderDetailProductActivity.REQUEST_PRODUCT_ID,
								model.getProductId());
						param.putString(
								OrderDetailProductActivity.REQUEST_ORDER_INFO,
								mOrderInfo);
						ToolUtil.startActivity(mActivity,
								OrderDetailProductActivity.class, param);
						ToolUtil.sendTrack(mActivity.getClass().getName(), 
								mActivity.getString(R.string.tag_OrderDetailActivity), 
								OrderDetailProductActivity.class.getName(), 
								mActivity.getString(R.string.tag_OrderDetailProductActivity), "01013");
					}
				}
			});
		}
	}

	public void pay() {
		if (mPayCore == null)
			return;

		mPayCore.setPayResponseListener(new PayResponseListener() {
			@Override
			public void onSuccess(String... message) {
				mActivity.setIsOperate(true);
				UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_pay_success, R.string.btn_ok);
				mActivity.findViewById(R.id.orderdetail_relative_pay)
						.setVisibility(View.GONE);
			}

			@Override
			public void onError(String... message) {
				String str = ((message == null || message[0] == null) ? "未知错误"
						: message[0]);
				Log.e(LOG_TAG, str);
				UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_pay_failed), str, R.string.btn_ok);
			}
		});

		mPayCore.submit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.orderdetail_relative_bottom_tab:
			isShowDetail = !isShowDetail;
			if (isShowDetail) {
				mActivity.findViewById(R.id.orderdetail_relative_bottom)
						.setVisibility(View.VISIBLE);
				mActivity.findViewById(R.id.orderdetail_relative_center)
						.setVisibility(View.VISIBLE);
				((ImageView) mActivity
						.findViewById(R.id.orderdetail_relative_bottom_tab_img))
						.setImageResource(R.drawable.ic_up);
			} else {
				mActivity.findViewById(R.id.orderdetail_relative_bottom)
						.setVisibility(View.GONE);
				mActivity.findViewById(R.id.orderdetail_relative_center)
						.setVisibility(View.GONE);
				((ImageView) mActivity
						.findViewById(R.id.orderdetail_relative_bottom_tab_img))
						.setImageResource(R.drawable.ic_down);
			}
			break;
		case R.id.orderdetail_products_bottom_tab:
			mAdapter.setShowAll(!mAdapter.isShowAll());
			mAdapter.notifyDataSetChanged();
			if (mAdapter.isShowAll()) {
				((TextView) mFooterView
						.findViewById(R.id.orderdetail_products_bottom_tab_tv))
						.setText("收起商品");

				((ImageView) mFooterView
						.findViewById(R.id.orderdetail_products_bottom_tab_img))
						.setImageResource(R.drawable.ic_up);
			} else {
				((TextView) mFooterView
						.findViewById(R.id.orderdetail_products_bottom_tab_tv))
						.setText("其他"
								+ (mOrderModel.getLeftNum()) + "件商品");

				((ImageView) mFooterView
						.findViewById(R.id.orderdetail_products_bottom_tab_img))
						.setImageResource(R.drawable.ic_down);
			}
			break;
		case R.id.orderdetail_wuliu_button:
			Bundle b = new Bundle();
			ArrayList<OrderProductModel> pModels = mOrderModel.getOrderProductModelList();
			ArrayList<String> pUrls = new ArrayList<String>();
			for (OrderProductModel model : pModels) {
				String url = IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 95);
				pUrls.add(url);
			}
			b.putString("orderId", mOrderModel.getOrderCharId());
			b.putStringArrayList("prodCharIds", pUrls);

			ToolUtil.startActivity(mActivity, AdviseActivity.class, b);
			ToolUtil.sendTrack(mActivity.getClass().getName(), 
					mActivity.getString(R.string.tag_OrderDetailActivity), 
					AdviseActivity.class.getName(), 
					mActivity.getString(R.string.tag_AdviseActivity), "01013");
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21001");
			break;
		case R.id.orderdetail_callphone_button:
			Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4008281878"));
			AppUtils.checkAndCall(mActivity,pIntent);
			ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "21002");
			break;

		}

	}
}
