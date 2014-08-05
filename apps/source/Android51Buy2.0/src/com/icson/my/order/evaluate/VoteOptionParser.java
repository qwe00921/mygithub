package com.icson.my.order.evaluate;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class VoteOptionParser extends Parser<byte[], ArrayList<VoteOptionModel>> {

	public ArrayList<VoteOptionModel> parse(byte[] bytes, String charset) throws Exception {
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
		ArrayList<VoteOptionModel> models = new ArrayList<VoteOptionModel>();
		
		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONArray coupons = v.getJSONArray("data");
			int size = coupons.length();
			JSONObject o;
			for(int i=0;i<size;i++){
				o = coupons.getJSONObject(i);
				VoteOptionModel model = new VoteOptionModel();
				model.option_id = o.getInt("option_id");
				model.group_id = o.getInt("group_id");
				model.order = o.getInt("order");
				
				models.add(model);
			}
		}

		mIsSuccess = true;

		return models;
	}

}