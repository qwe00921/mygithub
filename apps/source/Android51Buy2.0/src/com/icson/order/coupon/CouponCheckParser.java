package com.icson.order.coupon;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class CouponCheckParser extends Parser<byte[], CouponModel> {

	public CouponModel parse(byte[] bytes, String charset) throws Exception {
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
			mErrMsg = v.optString("data", v.getString("data"));
			return null;
		}
		CouponModel model = new CouponModel();
		JSONObject coupon = v.getJSONObject("data");
		model.code = coupon.optString("code");
		model.coupon_amt = coupon.optLong("amt");
		return model;
	}

}