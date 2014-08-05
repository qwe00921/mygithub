package com.icson.lib.control;

import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.lib.model.FavorProductListModel;
import com.icson.lib.parser.FavorProductListParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;

public class FavorControl extends BaseControl {

	public FavorControl(BaseActivity activity) {
		super(activity);
	}

	public void getList(int page, FavorProductListParser parser, OnSuccessListener<FavorProductListModel> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_FAVOR_GETLIST);
		if( null == ajax )
			return ;
		
		ajax.setData("page", page);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setParser(parser);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	public Ajax remove(long productId, long favorId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_FAVOR_DELETE);
		if( null == ajax )
			return null;
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("product_ids", productId);
		ajax.setData("favor_ids", favorId);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		return ajax;
	}

	public Ajax add(long productId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_FAVOR_ADDNEW);
		if( null == ajax )
			return null;
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("pid", productId);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		return ajax;
	}

}
