package com.icson.order.invoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.invoice.InvoiceModel;
import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class InvoiceParser extends Parser<byte[], ArrayList<InvoiceModel>> {

	public ArrayList<InvoiceModel> parse(byte[] bytes, String charset) throws Exception {
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

		ArrayList<InvoiceModel> mInvoiceModels = new ArrayList<InvoiceModel>();

		if (!ToolUtil.isEmptyList(v, "data")) {
			JSONArray data = v.getJSONArray("data");
			for (int i = 0, len = data.length(); i < len; i++) {
				InvoiceModel model = new InvoiceModel();
				model.parse(data.getJSONObject(i));
				//如果是广东站，需要id为4的发票,和2增值
				if(ILogin.getSiteId()==1001 ){
					if (model.getType() == InvoiceModel.INVOICE_TYPE_NORMAL|| model.getType() == InvoiceModel.INVOICE_TYPE_VAD)
						mInvoiceModels.add(model);
				}else{//其他站，发票id为1或者3,和2增值
					if (model.getType() == InvoiceModel.INVOICE_TYPE_COMPANY
						|| model.getType() == InvoiceModel.INVOICE_TYPE_PERSONAL
						|| model.getType() == InvoiceModel.INVOICE_TYPE_VAD) {
					mInvoiceModels.add(model);
					}
				}
			}

			sort(mInvoiceModels);
		}

		mIsSuccess = true;

		return mInvoiceModels;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sort(ArrayList<InvoiceModel> models) {
		if (models == null || models.size() < 2)
			return;

		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				InvoiceModel a = (InvoiceModel) o1;
				InvoiceModel b = (InvoiceModel) o2;
				if (a.getSortfactor() != b.getSortfactor()) {
					return b.getSortfactor() > a.getSortfactor() ? 1 : -1;
				}

				if (a.getUpdatetime() != b.getUpdatetime()) {
					return b.getUpdatetime() > a.getUpdatetime() ? 1 : -1;
				}
				return b.getCreatetime() > a.getCreatetime() ? 1 : -1;
			}
		});
	}
}