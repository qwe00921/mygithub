package com.tencent.djcity.my;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.Parser;

public class OrderParser extends Parser<byte[], OrderModel> {

	@Override
	public OrderModel parse(byte[] input, String charset) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(input, charset);
		
		return this.parse(json);
	}
	
	private OrderModel parse(JSONObject json) throws JSONException {
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
		
		OrderModel pResult = new OrderModel();
		if (ToolUtil.isEmptyList(json, "list")) {
			return pResult;
		}
		
		ArrayList<OrderItemModel> itemModels = new ArrayList<OrderItemModel>();
		JSONArray aArray = json.optJSONArray("list");
		final int nCount = (null != aArray ? aArray.length() : 0);
		for( int nPos = 0; nPos < nCount; nPos++ )
		{
			OrderItemModel itemModel = new OrderItemModel();
			itemModel.parse(aArray.getJSONObject(nPos));
			itemModels.add(itemModel);
		}
		pResult.setItemModels(itemModels);
		
		mIsSuccess = true;
		return pResult;
		
	}


}
