package com.icson.item;

import org.json.JSONObject;

import android.view.View;
import android.view.ViewGroup;

import com.icson.R;
import com.icson.lib.BaseView;
import com.icson.lib.ui.MyWebView;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.HttpUtil;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ItemTabIntroView extends BaseView implements ItemTabBase, OnSuccessListener<JSONObject> {

	private static final String LOG_TAG =  ItemTabIntroView.class.getName();
	private ItemActivity mActivity;
	private boolean firstExec = true;
	private ViewGroup mParent;
	private String mContent;
	private MyWebView mWebView;
	private Ajax mAjax;
	
		
	public ItemTabIntroView(ItemActivity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity.findViewById(R.id.item_relative_tab_content_intro);
		mActivity.getLayoutInflater().inflate(R.layout.item_tab_intro, mParent, true);
	}

	@Override
	public void init() {
		onResume();
		if (!firstExec)
			return;
		
//		ToolUtil.sendTrack(this.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), ItemTabIntroView.class.getName(), mActivity.getString(R.string.tag_ItemTabIntroView), "02012", String.valueOf(mActivity.getProductId()));
		firstExec = false;
		sendRequest();
	}

	public void onPause()
	{
		if(null!=mWebView)
			mWebView.setVisibility(View.INVISIBLE);
	}
	
	public void onResume()
	{
		if(null!=mWebView)
			mWebView.setVisibility(View.VISIBLE);
		
	}
	public void clean() {
		firstExec = true;
		mContent = null;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
	}

	private void sendRequest() {
		
		String quaInfo = "&qua=" + (HttpUtil.isUsingWifi(mActivity) ? 1 : 0);
		
		mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_INTRO, mActivity.getProductId() + quaInfo);
		if( null == mAjax )
			return ;
		
		mAjax.setOnSuccessListener(this);
		mAjax.setOnErrorListener(mActivity);
		mActivity.addAjax(mAjax);
		mAjax.send();
		mActivity.setLoadingSwitcher(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT, mParent.findViewById(R.id.item_tab_intro_content), mParent.findViewById(R.id.global_loading));
		mActivity.showLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
	}

	public void requestFinish() {
		mWebView = (MyWebView) mParent.findViewById(R.id.item_tab_intro_content);
		mWebView.getSettings().setBuiltInZoomControls(true); //显示放大缩小 controler
		mWebView.getSettings().setSupportZoom(true); //可以缩放
		
		String header= "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta content=\"text/html\" charset=\"utf-8\" /><style type=\"text/css\">img{width:300px;height:auto;}</style></head><body>";
		mWebView.loadDataWithBaseURL("http://www.51buy.com", header+mContent.replaceAll("width=", "w=").replaceAll("width:", "w:").replaceAll("<td>", "").replaceAll("</td>", "")+"</body></html>", "text/html", "utf-8", null);
		
		mWebView.clearCache(true);
	}

	@Override
	public void destroy() {
		mActivity = null;
		mParent = null;
		mContent = null;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		
		if(null!=mWebView)
		{
			ViewGroup pViewGroup = (ViewGroup) mWebView.getParent();
			if(null != pViewGroup) {
				pViewGroup.removeView(mWebView);
				mWebView.destroy();
			}
			mWebView = null;
		}
		
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		mActivity.closeLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
		try {
			final int errno = v.getInt("errno");

			if (errno != 0) {
				UiUtils.makeToast(mActivity, v.optString("data", Config.NORMAL_ERROR));
				return;
			}
			mContent = v.getString("data");
		} catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
		} finally {
			requestFinish();
		}
	}
	
}
