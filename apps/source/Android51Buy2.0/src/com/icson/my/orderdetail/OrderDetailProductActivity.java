package com.icson.my.orderdetail;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.model.OrderGiftModel;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.my.orderlist.MyOrderListAdapter;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class OrderDetailProductActivity extends BaseActivity {
	private static final String LOG_TAG =  OrderDetailProductActivity.class.getName();
	public static final String REQUEST_PRODUCT_ID = "product_id";
	public static final String REQUEST_ORDER_INFO = "order_info";
	private OrderModel mOrderModel;
	private OrderProductModel mProductModel;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_my_orderdetailproduct);
		this.loadNavBar(R.id.orderdetail_product_navigation_bar);

		long productId = getIntent().getLongExtra(REQUEST_PRODUCT_ID, 0);
		String orderInfo = getIntent().getStringExtra(REQUEST_ORDER_INFO);
		if (productId == 0 || orderInfo == null) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}

		try {
			JSONObject json = new JSONObject(orderInfo);
			mOrderModel = new OrderModel();
			mOrderModel.parse(json);

			ArrayList<OrderProductModel> list = mOrderModel.getOrderProductModelList();

			for (int i = 0, len = list.size(); i < len; i++) {
				OrderProductModel model = list.get(i);
				if (model == null || model.getProductId() != productId) {
					list.remove(i);
					i--;
					len--;
				}
			}

		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			mOrderModel = null;
		}

		if (mOrderModel == null) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}

		mProductModel = (mOrderModel.getOrderProductModelList() == null || mOrderModel.getOrderProductModelList().size() == 0) ? null : mOrderModel.getOrderProductModelList().get(0);

		if (mProductModel == null) {
			UiUtils.makeToast(this, R.string.params_empty,true);
			finish();
			return;
		}

		renderOrderInfo();
	}

	public void renderOrderInfo() {

		String orderId = "订单号: " + mOrderModel.getOrderCharId();
		// 订单号
		orderId += " " + MyOrderListAdapter.getStatusHTML(mOrderModel);
		((TextView) findViewById(R.id.orderdetail_textview_order_id)).setText(Html.fromHtml(orderId));

		//赠品数量
		((TextView) findViewById(R.id.orderdetail_textview_gift_count)).setText("赠品(" + mProductModel.getGiftCount() + "):");

		OrderDetailProducGiftAdapter adapter = new OrderDetailProducGiftAdapter(this, mProductModel.getOrderGiftModels() == null ? new ArrayList<OrderGiftModel>() : mProductModel.getOrderGiftModels());

		((LinearListView) findViewById(R.id.orderdetail_relative_gift_listview)).setAdapter(adapter);

		final OrderDetailProductSingleAdapter mAdapter = new OrderDetailProductSingleAdapter(this, mOrderModel.getOrderProductModelList());
		final LinearListView listView = (LinearListView) findViewById(R.id.orderdetail_linear_order_list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle param = new Bundle();
				param.putLong(ItemActivity.REQUEST_PRODUCT_ID, id);
				ToolUtil.startActivity(OrderDetailProductActivity.this, ItemActivity.class, param);
				ToolUtil.sendTrack(OrderDetailProductActivity.class.getName(), getString(R.string.tag_OrderDetailProductActivity), ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), "02011", String.valueOf(id));
			}
		});
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_OrderDetailProductActivity);
	}
}
