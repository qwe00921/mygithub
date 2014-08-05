package com.icson.lib.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.address.AddressModel;
import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class AddressParser extends Parser<byte[], ArrayList<AddressModel>> {

	public ArrayList<AddressModel> parse(byte[] bytes, String charset) throws Exception {

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
			mErrMsg = v.optString("data", "获取收货地址信息失败, 请稍候再试");
			return null;
		}

		ArrayList<AddressModel> models = new ArrayList<AddressModel>();
		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONArray arrs = v.getJSONArray("data");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				AddressModel model = new AddressModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}
	
			sort(models);
		}

		mIsSuccess = true;

		return models;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void sort(ArrayList<AddressModel> models) {
		if (models == null || models.size() < 2)
			return;

		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				AddressModel a = (AddressModel) o1;
				AddressModel b = (AddressModel) o2;

				if (a.getSortfactor() != b.getSortfactor()) {
					return b.getSortfactor() > a.getSortfactor() ? 1 : -1;
				}

				if (a.getLastUseTime() != b.getLastUseTime()) {
					return b.getLastUseTime() > a.getLastUseTime() ? 1 : -1;
				}

				if (a.getUpdatetime() != b.getUpdatetime()) {
					return b.getUpdatetime() > a.getUpdatetime() ? 1 : -1;
				}
				return a.getCreatetime() > b.getCreatetime() ? 1 : -1;
			}
		});
	}
}
