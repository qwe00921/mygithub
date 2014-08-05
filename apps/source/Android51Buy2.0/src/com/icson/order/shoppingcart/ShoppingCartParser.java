package com.icson.order.shoppingcart;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class ShoppingCartParser extends Parser<byte[], ShoppingCartModel> {

	private static final String LOG_TAG = ShoppingCartParser.class.getName();

	private static final int ERROR_SURORDER_EMPTY = 1;

	//private static final int ERROR_SURORDER_MUL = 2;

	public ShoppingCartModel parse(byte[] bytes, String charset) throws Exception {

		clean();

		JSONParser parser = new JSONParser();
		final JSONObject v = parser.parse(bytes, charset);

		final int errno = v.getInt("errno");

		if (errno == Config.NOT_LOGIN) {
			mErrMsg = "您已退出登录";
			ILogin.clearAccount();
			return null;
		}

		if (errno != 0) {
			mErrMsg = v.optString("data", "服务器端错误, 请稍候再试");
			return null;
		}
		ShoppingCartModel model = new ShoppingCartModel();
		model.parse(v.getJSONObject("data"));
		if (model.getSubOrders() == null || model.getSubOrders().size() == 0) {
			Log.e(LOG_TAG, "getShoppingCartList|sub order is empty.");
			mErrCode = ERROR_SURORDER_EMPTY;
			mErrMsg = "您当前收货地址不支持配送该商品，请至首页修改送至区域";
			return null;
		}

		/*
		if (model.getSubOrders().size() > 1) {
			Log.e(LOG_TAG, "getShoppingCartList|sub order is not 1");
			mErrCode = ERROR_SURORDER_MUL;
			mErrMsg = "不支持拆单业务";
			return model;
		}*/

		mIsSuccess = true;

		return model;
	}

	public boolean isSuccess() {
		return mIsSuccess;
	}

	public String getErrMsg() {
		return mErrMsg;
	}
}
