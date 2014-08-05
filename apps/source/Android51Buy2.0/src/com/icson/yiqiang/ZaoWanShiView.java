package com.icson.yiqiang;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.icson.R;
import com.icson.event.TimeBuyAdapter;
import com.icson.event.TimeBuyEntity;
import com.icson.event.TimeBuyModel;
import com.icson.event.TimeBuyParser;
import com.icson.item.ItemTabBase;
import com.icson.lib.BaseView;
//import com.icson.lib.ui.HeaderClock;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ZaoWanShiView extends BaseView implements OnItemClickListener,
		ItemTabBase, OnClickListener,
		OnSuccessListener<TimeBuyModel>, OnScrollListener {

	private BaseActivity mActivity;
	private ViewGroup mParent;
	//HeaderClock mClock;
	boolean firstExec = true;
	private Ajax mAjax;
	private View  mContainer = null;
	private ListView mListView;
	private View mFooterView;
	private View mHeaderView;
	private TimeBuyAdapter mAdapter;
	private TimeBuyModel   mModel;
	private ArrayList<TimeBuyEntity> mProducts = new ArrayList<TimeBuyEntity>();
	private boolean mRequesting = false;
	private int     mPageNum     = 1;
	private static final int DEFAULT_SIZE = 20;
	private int mType;
	private SubviewNetSuccessListener mParentListener;
	
	public ZaoWanShiView(BaseActivity activity,int type) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity
				.findViewById(R.id.item_relative_tab_content_zaowanshi);
		mType = type;
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
		mParent.removeAllViews();
		LayoutInflater.from(mActivity).inflate(R.layout.yiqiang_tab_zaowanshi,
				mParent, true);
		sendRequest(mPageNum);
	}

	@Override
	public void clean() {
		firstExec = true;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
	}

	public void sendRequest(int nPageNum) {

		Ajax pAjax = ServiceConfig.getAjax(Config.URL_EVENT_TIMEBUY);
		if( null == pAjax )
			return ;
		
		if( 1 == nPageNum )
			mActivity.showLoadingLayer();
		else
			addFooterView();
		
		pAjax.setOnSuccessListener(this);
		pAjax.setOnErrorListener(mActivity);
		pAjax.setParser(new TimeBuyParser());
		
		// Add parameters.
		pAjax.setData("deviceid", StatisticsUtils.getDeviceUid(mActivity));
		pAjax.setData("type", mType);
		pAjax.setData("page", nPageNum);
		pAjax.setData("size", DEFAULT_SIZE);
		
		pAjax.send();
		
		mRequesting = true;
	}

	@Override
	public void onSuccess(TimeBuyModel model, Response response) {
		mActivity.closeLoadingLayer();
		
		mModel = model;
		mRequesting = false;
		mPageNum = mModel.getPageNum();
		
		if(null!=mParentListener)
			mParentListener.onSubviewFinished(YiQiangActivity.PARAM_TAB_TIMEBUY, mModel.getType());
		
		requestFinish();
	}

	public void requestFinish() 
	{
		if (mModel == null)
			return;

		if( null == mContainer )
		{
			mContainer = mParent.findViewById(R.id.event_time_buy_container);
			mContainer.setBackgroundColor(mModel.getBackground());
		}
		if( null == mListView )
		{
			mListView = (ListView) mParent.findViewById(R.id.zaowanshi_listview);
			mListView.setDividerHeight(0);
			mListView.setHeaderDividersEnabled(false);
			mListView.setFooterDividersEnabled(false);
			mListView.setOnScrollListener(this);
			
			LayoutInflater inflator = LayoutInflater.from(mActivity);

			mHeaderView = inflator.inflate(R.layout.event_time_buy_header, null);
			mListView.addHeaderView(mHeaderView);
			
			mFooterView = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.global_listview_loading, null);
		}
		
		mProducts.addAll(mModel.getProducts());
		
		if( null == mAdapter )
		{
			mAdapter = new TimeBuyAdapter(mActivity, mProducts);
			mListView.setAdapter(mAdapter);
		}
		else
		{
			this.removeFooterView();
			mAdapter.notifyDataSetChanged();
		}
		
		// Update type.
		mType = mModel.getType();
		
		// Update properties of adapter.
		mAdapter.setParameters(mType);
		
		// Set the timeout for refresh.
		//setTimer();

		AjaxUtil.getLocalImage(mActivity, mModel.getAdvertiseUrl(), new ImageLoadListener() {
			@Override
			public void onLoaded(Bitmap image, String url) {
				ImageView view = ((ImageView) mHeaderView.findViewById(R.id.event_header_image));
				view.setImageBitmap(image);
				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onError(String strUrl) {
			}
		});
		
	}
	private void removeFooterView()
	{
		if( (null != mFooterView) && (null != mListView) && (mListView.getFooterViewsCount() > 0) )
		{
			mListView.removeFooterView(mFooterView);
		}
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
	}

	@Override
	public void onScrollStateChanged(AbsListView aView, int nScrollState)
	{
		if ( (OnScrollListener.SCROLL_STATE_IDLE == nScrollState) && (aView.getLastVisiblePosition() == aView.getCount() - 1) )
		{
			// Check whether is requesting.
			if( (mRequesting) || (null == mModel) || (mPageNum >= mModel.getPageCount())  )
				return ;
			
			// Send request for next page.
			sendRequest(mPageNum + 1);
		}
	}
	private void addFooterView()
	{
		if( (null != mFooterView) && (null != mListView) && (0 >= mListView.getFooterViewsCount()) )
		{
			mListView.addFooterView(mFooterView);
		}
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

		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
	}
}
