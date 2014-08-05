package com.tencent.djcity.my;

import org.json.JSONException;
import org.json.JSONObject;


import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.Parser;

public class OrderDetailParser extends Parser<byte[], OrderDetailModel> {

	@Override
	public OrderDetailModel parse(byte[] input, String charset) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(input, charset);
		
		return this.parse(json);
	}
	
	private OrderDetailModel parse(JSONObject json) throws JSONException {
		clean();
		mIsSuccess = false;
		
		final int errno = json.optInt("ret", -1);

		if (errno == Config.NOT_LOGIN) {
			mErrMsg = json.optString("msg", "您已退出登录");;
			ILogin.clearAccount();
			return null;
		}

		if (errno != 0) {
			mErrMsg = json.optString("msg", "服务器端错误, 请稍候再试");
			return null;
		}
		
		OrderDetailModel model = new OrderDetailModel();
		model.parse(json);
		
		mIsSuccess = true;
		
		return model;
		
	}


}
