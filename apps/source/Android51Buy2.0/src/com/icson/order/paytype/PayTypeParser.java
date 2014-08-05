package com.icson.order.paytype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.paytype.PayTypeModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class PayTypeParser extends Parser<byte[], ArrayList<PayTypeModel>> {

	@SuppressWarnings("unchecked")
	public ArrayList<PayTypeModel> parse(byte[] bytes, String charset) throws Exception {
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

		ArrayList<PayTypeModel> mPayTypeModels = new ArrayList<PayTypeModel>();

		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONObject data = v.getJSONObject("data");
			final Iterator<String> iter = data.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				PayTypeModel model = new PayTypeModel();
				model.parse(data.getJSONObject(key));
				mPayTypeModels.add(model);
			}

			sort(mPayTypeModels);
		}

		mIsSuccess = true;

		return mPayTypeModels;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sort(ArrayList<PayTypeModel> models) {
		if (models == null || models.size() < 2)
			return;

		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				PayTypeModel a = (PayTypeModel) o1;
				PayTypeModel b = (PayTypeModel) o2;
				return a.getPayType() > b.getPayType() ? 1 : -1;
			}
		});
	}
}