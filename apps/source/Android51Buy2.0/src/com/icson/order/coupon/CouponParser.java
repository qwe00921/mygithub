package com.icson.order.coupon;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class CouponParser extends Parser<byte[], ArrayList<CouponModel>> {

	public ArrayList<CouponModel> parse(byte[] bytes, String charset) throws Exception {
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
		ArrayList<CouponModel> models = new ArrayList<CouponModel>();
		
		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONArray coupons = v.getJSONArray("data");
			int size = coupons.length();
			JSONObject o;
			for(int i=0;i<size;i++){
				o = coupons.getJSONObject(i);
				CouponModel model = new CouponModel();
				model.parser(o);
				models.add(model);
			}
		}

		mIsSuccess = true;

		return models;
	}

}