package com.icson.postsale;

import org.json.JSONObject;

import android.util.Log;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;

public class PostSaleControl {

	private BaseActivity mActivity;
	public static final int DATA_PAGE_SIZE = 5;
	private static final String TAG = PostSaleControl.class.getSimpleName();
	
	public PostSaleControl(BaseActivity activity) {
		mActivity = activity;
	}
	
	public Ajax getProductChangeHistoryList(int page, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_AFTERSALE_ORDER_LIST);
		if(ajax == null) {
			Log.w(TAG, "[getProductChangeHistoryList] ajax is null");
			return null;
		}
		ajax.setData("page", page);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("pagesize", DATA_PAGE_SIZE);
		ajax.setTimeout(10);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		
		return ajax;
	}
	
	public Ajax getProductChangeDetail(int applyId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_AFTERSALE_ORDER_DETAIL);
		if(ajax == null) {
			Log.w(TAG, "[getProductChangeDetail] ajax is null");
			return null;
		}
		ajax.setData("applyID", applyId);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setTimeout(10);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		
		return ajax;
	}
	
	public Ajax sendLevelUpRequest(int applyId, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_AFTERSALE_ORDER_PROMPT);
		if(ajax == null) {
			Log.w(TAG, "[sendLevelUpRequest] ajax is null");
			return null;
		}
		ajax.setData("applyID", applyId);
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setTimeout(10);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		mActivity.addAjax(ajax);
		ajax.send();
		
		return ajax;
	}
}
