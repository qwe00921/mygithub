package com.icson.address;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.parser.AddressParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;

public class AddressControl {

	private BaseActivity mActivity;

	public AddressControl(BaseActivity activity) {
		mActivity = activity;
	}

	public Ajax getAddressList(AddressParser parser, final OnSuccessListener<ArrayList<AddressModel>> success, final OnErrorListener error) {
		final long uid = ILogin.getLoginUid();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_ADDRESS_GETLIST);
		if( null == ajax )
			return null;
		ajax.setData("uid", uid);
		ajax.setOnErrorListener(error);
		ajax.setParser(parser);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();
		return ajax;
	}

	public void set(AddressModel model, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		final long uid = ILogin.getLoginUid();
		String strKey = model.getAid() == 0 ? Config.URL_ADDRESS_ADDNEW : Config.URL_ADDRESS_MODIFY;
		Ajax ajax = ServiceConfig.getAjax(strKey);
		if( null == ajax )
			return ;
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("uid", uid);
		data.put("workplace", model.getWorkplace());
		data.put("district", model.getDistrict());
		data.put("address", model.getAddress());
		data.put("zipcode", model.getZipcode());
		data.put("name", model.getName());
		data.put("mobile", model.getMobile());
		data.put("phone", model.getPhone());
		if (model.getAid() != 0) {
			data.put("aid", model.getAid());
		}
		ajax.setData(data);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	public Ajax remove(int addressId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		final long uid = ILogin.getLoginUid();
		Ajax ajax = ServiceConfig.getAjax(Config.URL_ADDRESS_DELETE);
		if( null == ajax )
			return null;
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		ajax.setData("uid", uid);
		ajax.setData("aid", addressId);
		mActivity.addAjax(ajax);
		ajax.send();

		return ajax;
	}

	public void destroy() {
		mActivity = null;
	}
}
