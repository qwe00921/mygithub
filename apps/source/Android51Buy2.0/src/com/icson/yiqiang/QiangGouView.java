package com.icson.yiqiang;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.item.ItemTabBase;
import com.icson.lib.BaseView;
import com.icson.lib.model.BaseModel;
import com.icson.lib.ui.HeaderClock;
import com.icson.lib.ui.UiUtils;
import com.icson.qiang.QiangModel;
import com.icson.qiang.QiangModel.QiangProductModel;
import com.icson.qiang.QiangParser;
import com.icson.qiang.QiangTomorrowModel;
import com.icson.qiang.QiangTomorrowModel.QiangTomorrowProductModel;
import com.icson.qiang.QiangTomorrowParser;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class QiangGouView extends BaseView implements OnItemClickListener,
		ItemTabBase, OnClickListener, OnSuccessListener<QiangModel>, Runnable {

	private BaseActivity mActivity;
	private ViewGroup mParent;
	boolean firstExec = true;
	private Ajax mAjax;
	private QiangModel mQiangModel;
	private HeaderClock mClock;
	private View mHeaderView;
	private QiangItemAdapter mQiangAdapter;
	private ArrayList<BaseModel> models = new ArrayList<BaseModel>();
	private SubviewNetSuccessListener mParentListener;
	private QiangParser mQiangParser;
	private QiangTomorrowParser mQiangTomorrowParser;
	
	public QiangGouView(BaseActivity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity
				.findViewById(R.id.item_relative_tab_content_qianggou);
		mQiangParser = new QiangParser();
		mQiangTomorrowParser = new QiangTomorrowParser();
	}

	public void setListener(SubviewNetSuccessListener aL)
	{
		mParentListener = aL;
	}
	
	@Override
	public void init() {
		if (!firstExec)
			return;

		firstExec = false;
		sendRequest();
	}

	@Override
	public void clean() {
		firstExec = true;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		
	}

	public void sendRequest() {

		mAjax = ServiceConfig.getAjax(Config.URL_EVENT_QIANG);
		// 下期抢购 Config.URL_EVENT_QIANG_NEXT
		if (null == mAjax)
			return;

		mParent.removeAllViews();
		LayoutInflater.from(mActivity).inflate(R.layout.yiqiang_tab_qianggou,
				mParent, true);

		mActivity.showLoadingLayer();
		
		
		mAjax.setOnSuccessListener(this);
		mAjax.setParser(mQiangParser);
		mAjax.setOnErrorListener(mActivity);
		// mActivity.showLoadingLayer(mAjax.getId());
		mAjax.send();
	}

	@Override
	public void run() {
		this.clean();
		this.init();
	}

	@Override
	public void onSuccess(QiangModel model, Response response) {
		mActivity.closeLoadingLayer();
		if(null != mParentListener){
			mParentListener.onSubviewFinished(YiQiangActivity.PARAM_TAB_QIANG, 0);
		}
		
		if( !mQiangParser.isSuccess() ) {
			UiUtils.makeToast(mActivity, TextUtils.isEmpty(mQiangParser.getErrMsg()) ? Config.NORMAL_ERROR: mQiangParser.getErrMsg());
			return;
		}
		mQiangModel = model;
		requestFinish();

		sendTomorrowRequest();
	}

	private void sendTomorrowRequest() {
		mAjax = ServiceConfig.getAjax(Config.URL_EVENT_QIANG_NEXT);
		if (null == mAjax)
			return;

		mAjax.setOnSuccessListener(new OnSuccessListener<QiangTomorrowModel>() {

			@Override
			public void onSuccess(QiangTomorrowModel model, Response response) {
				if( !mQiangTomorrowParser.isSuccess() ) {
					UiUtils.makeToast(mActivity, TextUtils.isEmpty(mQiangTomorrowParser.getErrMsg()) ? Config.NORMAL_ERROR: mQiangTomorrowParser.getErrMsg());
					return;
				}
				
				if(null != model){
					models.addAll(model.getQiangTomorrowProductModels());
					mQiangAdapter.notifyDataSetChanged();
				}
			}
		});
		mAjax.setParser(mQiangTomorrowParser);
		mAjax.setOnErrorListener(mActivity);
		mAjax.send();
	}

	public void requestFinish() {
		if ( null == mQiangModel ) {
			UiUtils.makeToast(mActivity, "抢购已结束.");
			return;
		}
		
		models.clear();
		models.addAll(mQiangModel.getQiangProductModles());
		if (models == null || models.size() == 0) {
			UiUtils.makeToast(mActivity, "抢购已结束.");
			return;
		}
		
		if(null==mClock)
		{
			mHeaderView = LayoutInflater.from(mActivity).inflate(
					R.layout.qiang_list_header, null);
			mClock = (HeaderClock) mHeaderView.findViewById(R.id.qiang_clock);
			mClock.setLayout(R.layout.global_title_clock);
		}
		mClock.setCurrentTime(mQiangModel.getNow() * 1000);
		mClock.setEndTime(mQiangModel.getEnd() * 1000);
		mClock.setOnArriveListener(this);
		mClock.run();

		mQiangAdapter = new QiangItemAdapter(mActivity, models);
		ListView listView = ((ListView) mParent
				.findViewById(R.id.qiang_listview));
		listView.addHeaderView(mHeaderView);
		listView.setDividerHeight(0);
		listView.setAdapter(mQiangAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < 1) {// 点击0的时候是header
					return;
				}
				
				Bundle param = new Bundle();
				BaseModel bModel = models.get(position - 1);
				if (bModel instanceof QiangProductModel) {
					QiangProductModel mProduct = (QiangProductModel)bModel;
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID,
							mProduct.getProductId());
					ToolUtil.reportStatisticsClick(((YiQiangActivity)mActivity).getActivityPageId(), ""+(30001+position),String.valueOf(((QiangProductModel)bModel).getProductId()));
				}else if (bModel instanceof QiangTomorrowProductModel) {
					QiangTomorrowProductModel qtpModel = (QiangTomorrowProductModel)bModel;
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID,
							qtpModel.getProductId());
					ToolUtil.reportStatisticsClick(((YiQiangActivity)mActivity).getActivityPageId(), ""+(30001+position),String.valueOf(((QiangTomorrowProductModel)bModel).getProductId()));
				}
				
				
				UiUtils.startActivity(mActivity, ItemActivity.class, param,true);
			}
		});

	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	public void destroy() {
		mActivity = null;
		mParent = null;

		if(null!=mClock)
			mClock.destroy();
		mClock = null;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
	}
}
