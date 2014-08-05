package com.icson.order.userpoint;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class UserPointParser extends Parser<byte[], UserPointModel> {

	public UserPointModel parse(byte[] bytes, String charset) throws Exception {
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

		UserPointModel mUserPointModel = new UserPointModel();
		
		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONObject data = v.getJSONObject("data");
			mUserPointModel.parse(data);
		}

		mIsSuccess = true;

		return mUserPointModel;
	}

}