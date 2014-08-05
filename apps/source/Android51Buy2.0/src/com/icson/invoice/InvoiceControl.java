package com.icson.invoice;

import java.util.ArrayList;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.control.BaseControl;
import com.icson.order.invoice.InvoiceParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class InvoiceControl extends BaseControl {

	public InvoiceControl(BaseActivity activity) {
		super(activity);
	}

	public void getInvoiceList(final InvoiceParser mInvoiceParser, final OnSuccessListener<ArrayList<InvoiceModel>> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_INVOICE_GETLIST);
		if( null == ajax )
			return ;
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setParser(mInvoiceParser);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<InvoiceModel>>() {
			@Override
			public void onSuccess(ArrayList<InvoiceModel> v, Response response) {

				success.onSuccess(v, response);
			}
		});
		mActivity.addAjax(ajax);
		ajax.send();
	}
	
	public void delete(InvoiceModel model, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_INVOICE_DELETE);
		if( null == ajax )
			return ;
		
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("iid", model.getIid());
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	public void set(InvoiceModel model, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		String strKey = model.getIid() == 0 ? Config.URL_INVOICE_ADDNEW : Config.URL_INVOICE_MODIFY;
		Ajax ajax = ServiceConfig.getAjax(strKey);
		if( null == ajax )
			return ;
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("type", model.getType());
		ajax.setData("title", model.getTitle());
		if (model.getIid() != 0) {
			ajax.setData("iid", model.getIid());
		}
		if(model.getType() == InvoiceModel.INVOICE_TYPE_VAD){
			ajax.setData("name", model.getName());
			ajax.setData("addr", model.getAddress());
			ajax.setData("phone", model.getPhone());
			ajax.setData("taxno", model.getTaxno());
			ajax.setData("bankno", model.getBankno());
			ajax.setData("bankname", model.getBankname());
			ajax.setData("vat_normal_name", "");
		}
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	public void destroy() {
		mActivity = null;
	}
}
