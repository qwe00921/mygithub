package com.icson.my.orderdetail;

import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.BaseView;
import com.icson.lib.model.OrderModel;
import com.icson.lib.pay.PayCore;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.UiUtils;
import com.icson.my.orderlist.VPOrderModel;
import com.icson.util.Log;
import com.icson.util.ToolUtil;

public class VPOrderDetailView extends BaseView {
	private static final String LOG_TAG = VPOrderDetailView.class.getName();
	private OrderDetailActivity mActivity;
	private VPOrderModel mOrderModel;
	private PayCore mPayCore;

	public VPOrderDetailView(OrderDetailActivity activity,
			VPOrderModel mOrderModel) {
		mActivity = activity;
		this.mOrderModel = mOrderModel;
		init();
	}

	public OrderModel getOrderModel() {
		return mOrderModel;
	}

	private void init() {
		// check cancel button show or gone
		final int orderStatus = mOrderModel.getStatus();

		mActivity.findViewById(R.id.orderdetail_button_pay).setVisibility(
				orderStatus == 0 ? View.VISIBLE : View.GONE);

		mPayCore = PayFactory.getInstance(mActivity,
				mOrderModel.getPayType(), mOrderModel.getOrderCharId(),true);

		initVPOrderInfo();
	}

	public void destroy() {
		mActivity = null;
		mOrderModel = null;
		mPayCore = null;
	}

	public void initVPOrderInfo() {
		
		// 订单号
		String orderId = "订单号: " + mOrderModel.getOrderCharId();
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_order_id))
				.setText(Html.fromHtml(orderId));
		((TextView)mActivity.findViewById(R.id.orderdetail_textview_order_status)).setText(Html.fromHtml(mOrderModel.getStatus_name()));

		// 商品图片
		ImageView pic1 = ((ImageView) mActivity.findViewById(R.id.orderlist_pic_1));
		pic1.setVisibility(View.VISIBLE);
		if(mOrderModel.getProduct_list_str().contains("移动")){
			pic1.setImageResource(R.drawable.chinamobile);
		}else if(mOrderModel.getProduct_list_str().contains("联通")){
			pic1.setImageResource(R.drawable.chinauincom);
		}else if(mOrderModel.getProduct_list_str().contains("电信")){
			pic1.setImageResource(R.drawable.chinatelcom);
		}
		((TextView) mActivity.findViewById(R.id.orderlist_tv_Card)).setText((int)mOrderModel.getCard_money()/100 +"元");
		// 商品名字
		((TextView) mActivity.findViewById(R.id.orderlist_tv_title))
				.setText(mOrderModel.getProduct_list_str());
		
		// 手机
		((TextView) mActivity.findViewById(R.id.orderlist_tv_phone))
				.setText("手机号码:"+mOrderModel.getReceiver());

		// 成交时间
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_time))
				.setText("成交时间: "
						+ ToolUtil.toDate(mOrderModel.getOrderDate() * 1000));

		// 总金额
		String price = "总额: <font color=\"red\">¥" + ToolUtil.toPrice(mOrderModel.getOrderCost())+ "</font>";
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_total))
				.setText(Html.fromHtml(price));

		// 付款方式
		((TextView) mActivity
				.findViewById(R.id.orderdetail_textview_pay_type_name))
				.setText("(" + mOrderModel.getPayTypeName() + ")");

		// 数量
		((TextView) mActivity.findViewById(R.id.orderdetail_textview_count))
				.setText("数量: 1");

	}

	public void pay() {
		if (mPayCore == null)
			return;

		mPayCore.setPayResponseListener(new PayResponseListener() {
			@Override
			public void onSuccess(String... message) {
				mActivity.setIsOperate(true);
				UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_pay_success, R.string.btn_ok);
				mActivity.findViewById(R.id.orderdetail_button_pay)
						.setVisibility(View.GONE);
			}

			@Override
			public void onError(String... message) {
				String str = ((message == null || message[0] == null) ? "未知错误"
						: message[0]);
				Log.e(LOG_TAG, str);
				UiUtils.showDialog(mActivity, R.string.caption_pay_failed, str, R.string.btn_ok);
			}
		});

		mPayCore.submit();
	}
}
