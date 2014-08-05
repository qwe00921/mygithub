package com.icson.item;

import java.util.ArrayList;

import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.BaseView;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ItemTabParamView extends BaseView implements ItemTabBase, OnSuccessListener<ArrayList<ItemParamModel>>{

	private ItemActivity mActivity;

	private boolean firstExec = true;

	private ViewGroup mParent;

	private ListView mListView;

	private ItemTabParamAdapter mItemParamAdapter;

	private Ajax mAjax;

	private ArrayList<ItemParamModel> mItemParamModels;

	private ProductParamParser mParser;

	public ItemTabParamView(ItemActivity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity.findViewById(R.id.item_relative_tab_content_param);
		mActivity.getLayoutInflater().inflate(R.layout.item_tab_param, mParent, true);
		mListView = (ListView) mParent.findViewById(R.id.item_tab_param_container);
		mListView.setDividerHeight(0);
		mItemParamModels = new ArrayList<ItemParamModel>();
		mItemParamAdapter = new ItemTabParamAdapter(mActivity, mItemParamModels);
		mListView.setAdapter(mItemParamAdapter);
		mParser = new ProductParamParser();
	}

	@Override
	public void init() {
		if (!firstExec)
			return;
		
//		ToolUtil.sendTrack(this.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), ItemTabParamView.class.getName(), mActivity.getString(R.string.tag_ItemTabParamView), "02013", String.valueOf(mActivity.getProductId()));
		
		firstExec = false;
		sendRequest();
	}

	public void clean() {
		firstExec = true;

		if (mAjax != null) {
			mAjax.abort();
		}
	}

	private void sendRequest() {
		mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_PARAMETERS, mActivity.getProductId());
		if( null == mAjax )
			return ;
		mItemParamModels.clear();
		mAjax.setParser(mParser);
		mAjax.setOnSuccessListener(this);
		mAjax.setOnErrorListener(mActivity);
		mActivity.setLoadingSwitcher(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT, mParent.findViewById(R.id.item_tab_param_container), mParent.findViewById(R.id.global_loading));

		//((ImageButton) mParent.findViewById(R.id.global_loading_bg)).setBackgroundColor(mActivity.getResources().getColor(R.id.global_loading_bg));

		mActivity.showLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
		mAjax.send();
	}

	public void requestFinish() {
		mItemParamAdapter.notifyDataSetChanged();
	}

	@Override
	public void destroy() {
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		mActivity = null;
		mParent = null;
		mListView = null;
		mItemParamAdapter = null;
		mItemParamModels = null;
	}

	@Override
	public void onSuccess(ArrayList<ItemParamModel> v, Response response) {
		mActivity.closeLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);

		if (!mParser.isSuccess()) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			return;
		}
		
		if (null == v) {
			UiUtils.makeToast(mActivity, Config.NORMAL_ERROR);
			return;
		}

		mItemParamModels.clear();
		mItemParamModels.addAll(v);
		requestFinish();
	}
}
