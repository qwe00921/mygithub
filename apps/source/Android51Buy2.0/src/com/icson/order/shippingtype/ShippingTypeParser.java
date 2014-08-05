package com.icson.order.shippingtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.model.ProductModel;
import com.icson.order.shoppingcart.ShoppingCartModel;
import com.icson.order.shoppingcart.SubOrderModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class ShippingTypeParser extends Parser<byte[], ArrayList<ShippingTypeModel>> {

	public static final int FORBIDDEN_BUY = 100;

	private ShoppingCartModel mShoppingCartModel;

	public ShippingTypeParser(ShoppingCartModel model) {
		mShoppingCartModel = model;
	}

	private String getProductName(long productId) {
		for (SubOrderModel subOrderModel : mShoppingCartModel.getSubOrders()) {
			for (ProductModel product : subOrderModel.getProducts()) {
				if (product.getProductId() == productId) {
					return product.getNameNoHTML();
				}
			}
		}

		return "";

	}

	@SuppressWarnings("unused")
	public ArrayList<ShippingTypeModel> parse(byte[] bytes, String charset) throws Exception {
		clean();

		JSONParser parser = new JSONParser();
		final JSONObject v = parser.parse(bytes, charset);

		final int errno = v.getInt("errno");

		if (errno == Config.NOT_LOGIN) {
			mErrMsg = "您已退出登录，请登录后重试.";
			ILogin.clearAccount();
			return null;
		}

		if (errno != 0) {
			mErrMsg = v.optString("data", "服务器端错误, 请稍候再试");
			return null;
		}

		if (!ToolUtil.isEmptyList(v, "forbidden")) {
			final JSONObject forbidden = v.getJSONObject("forbidden");
			@SuppressWarnings("unchecked")
			Iterator<String> iter = forbidden.keys();
			while (iter.hasNext()) {
				JSONArray productIds = forbidden.getJSONArray(iter.next());
				for (int i = 0, len = productIds.length(); i < len; i++) {
					long productId = productIds.getLong(i);
					String name = getProductName(productId);
					mErrMsg = (name.equals("") ? ("商品号(" + productId + ")") : ("商品(" + name + ")")) + "无法配送到您所选地址,请修改地址";
					break;
				}
			}

			mErrCode = FORBIDDEN_BUY;
			return null;
		}

		if (ToolUtil.isEmptyList(v, "data")) {
			mErrMsg = "暂不支持该地区的配送,请修改地址";
			return null;
		}

		ArrayList<ShippingTypeModel> mShippingTypeModelList = new ArrayList<ShippingTypeModel>();

		final JSONObject data = v.getJSONObject("data");

		@SuppressWarnings("unchecked")
		final Iterator<String> iter = data.keys();
		while (iter.hasNext()) {
			final String key = iter.next();
			ShippingTypeModel model = new ShippingTypeModel();
			model.parse(data.getJSONObject(key));

			//不支持拆单操作
			if (model.getSubShippingTypeModelList() == null || model.getSubShippingTypeModelList().size() == 0) {
				mErrMsg = "子订单为空";
				return null;
			}

			if (model.getSubShippingTypeModelList().size() > 1) {
				mErrMsg = "对不起, 暂不支持拆单操作";
				return null;
			}

			mShippingTypeModelList.add(model);
		}

		sort(mShippingTypeModelList);

		mIsSuccess = true;

		return mShippingTypeModelList;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sort(ArrayList<ShippingTypeModel> models) {
		if (models == null || models.size() < 2) {
			return;
		}

		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ShippingTypeModel a = (ShippingTypeModel) o1;
				ShippingTypeModel b = (ShippingTypeModel) o2;

				return a.getOrderNumber() < b.getOrderNumber() ? 1 : -1;
			}
		});

	}
}
