package com.icson.paytype;

import java.util.ArrayList;
import java.util.HashMap;

import com.icson.lib.ILogin;
import com.icson.lib.control.BaseControl;
import com.icson.order.paytype.PayTypeParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PayTypeControl extends BaseControl {

	private HashMap<String, ArrayList<PayTypeModel>> cache;

	public PayTypeControl(BaseActivity activity) {
		super(activity);
		cache = new HashMap<String, ArrayList<PayTypeModel>>();
	}

	public void getPayTypeList(final PayTypeParser mPayTypeParser, int shippingTypeId, String productIds, final OnSuccessListener<ArrayList<PayTypeModel>> success, final OnErrorListener error) {
		final String key = shippingTypeId + productIds;
		final ArrayList<PayTypeModel> piece = cache.get(key);
		if (piece != null) {
			mPayTypeParser.setSuccess(true);
			success.onSuccess(piece, null);
			return;
		}

		final long uid = ILogin.getLoginUid();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_SHIP_PAYTYPE, uid);
		if( null == ajax )
			return ;
		ajax.setParser(mPayTypeParser);
		ajax.setData("shippingtype", shippingTypeId);
		ajax.setData("products", productIds);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<PayTypeModel>>() {
			@Override
			public void onSuccess(ArrayList<PayTypeModel> v, Response response) {
				success.onSuccess(v, response);
				if (mPayTypeParser.isSuccess()) {
					cache.put(key, v);
				}
			}

		});
		mActivity.addAjax(ajax);
		ajax.send();
	}

	public void destroy() {
		cache = null;
		mActivity = null;
	}
}
