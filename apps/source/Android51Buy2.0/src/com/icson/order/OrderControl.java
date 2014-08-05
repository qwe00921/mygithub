package com.icson.order;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.model.OrderFlowModel;
import com.icson.lib.parser.OrderFlowParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;

public class OrderControl {
	private BaseActivity mActivity;
	
	public static final String KEY_ORDERTYPE = "order_type";
	public static final int ORDER_TYPE_REAL = 1;
	public static final int ORDER_TYPE_VIRTUAL = 2;
	public static final int ORDER_TYPE_BOTH = 0;

	public OrderControl(BaseActivity activity) {
		mActivity = activity;
	}

	public Ajax submitOrder(OrderPackage pack, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		final long uid = ILogin.getLoginUid();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_SUBMIT_ORDER, uid);
		if( null == ajax )
			return null;
		
		ajax.setData(pack);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.setTimeout(60);
		ajax.send();

		return ajax;
	}

	public Ajax getOrderList(long page, boolean isOneMonthAgo, OnSuccessListener<JSONObject> success, OnErrorListener error, int type) {
		final Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_GETLIST);
		if( null == ajax )
			return null;
		
		// Set information.
		if(type != ORDER_TYPE_REAL && type != ORDER_TYPE_VIRTUAL) {
			type = ORDER_TYPE_BOTH;
		}
		if(type != ORDER_TYPE_BOTH) { // 1为实物，2为虚拟，不设此属性则获取两者。
			ajax.setData(KEY_ORDERTYPE, type);
		}
		
		ajax.setData("page", page);
		ajax.setData("before", isOneMonthAgo ? 1 : 0);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setTimeout(10);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		return ajax;
	}

	public Ajax getOrderDetail(String orderCharId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		if (orderCharId == null)
			return null;

		final Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_GETDETAIL);
		if( null == ajax )
			return null;
		
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("orderCharId", orderCharId);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		ajax.send();
		mActivity.addAjax(ajax);
		return ajax;
	}

	public Ajax getOrderFlow(String orderCharId, OnSuccessListener<OrderFlowModel> success, OnErrorListener error) {
		if (orderCharId == null)
			return null;
		
		final Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_GETFLOW);
		if( null == ajax )
			return null;
		ajax.setParser(new OrderFlowParser());
		ajax.setTimeout(10);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("orderCharId", Integer.valueOf(orderCharId));
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		ajax.send();
		mActivity.addAjax(ajax);
		return ajax;
	}

	// for test
	// $_GET['type_id'] = 1;
	// $_GET['sysno'] = 1697371630;

	// $_GET['type_id'] = 2;
	// $_GET['sysno'] = 468235346764;

	// $_GET['type_id'] = 3;
	// $_GET['sysno'] = 9021019346;

	/*
	private Ajax getOrderFlowFromThird(String thirdSysno, int thirdType, OnSuccessListener<OrderFlowModel> success, OnErrorListener error) {
		if (thirdSysno == null || thirdType == 0)
			return null;

		final Ajax ajax = ServiceConfig.getAjax(Config.URL_DELIVERY_FLOW_INFO, ILogin.getLoginUid());
		if( null == ajax )
			return null;
		ajax.setTimeout(10);
		ajax.setParser(new OrderThirdFlowParser());
		ajax.setData("type_id", thirdType);
		ajax.setData("sysno", thirdSysno);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		ajax.send();
		mActivity.addAjax(ajax);
		return ajax;
	} */

	public Ajax orderCancel(String pOrderId, boolean isPackage, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_CANCEL_ORDER);
		if( null == ajax )
			return null;
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("orderId", pOrderId);
		if(isPackage)
			ajax.setData("ispackage", 1);
		
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		ajax.send();
		mActivity.addAjax(ajax);
		return ajax;
	}
}
