package com.icson.item;

import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;

public class ItemTabReviewControl {

	public final static int REVIEW_SATISFY = 1;

	public final static int REVIEW_GENERAL = 2;

	public final static int REVIEW_UNSATISFY = 3;
	
	
	public ItemTabReviewControl(BaseActivity activity) {
	}

	public Ajax getReviews(long productId, int page, int type, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_PRODUCT_REVIEWS);
		if( null == ajax )
			return null;
		
		ajax.setData("type", type == REVIEW_UNSATISFY ? "unsatisfiedexperience" : (type == REVIEW_GENERAL ? "generalexperience" : "satisfiedexperience"));
		ajax.setData("pid", productId);
		ajax.setData("page", page);
		ajax.setOnSuccessListener(success);
		ajax.setOnErrorListener(error);
		ajax.send();
		return ajax;
	}
}
