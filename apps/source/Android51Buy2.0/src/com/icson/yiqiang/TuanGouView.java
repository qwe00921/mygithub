package com.icson.yiqiang;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.item.ItemTabBase;
import com.icson.lib.BaseView;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.tuan.TuanModel;
import com.icson.tuan.TuanModel.CateInfo;
import com.icson.tuan.TuanModel.TuanProductModel;
import com.icson.tuan.TuanParser;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class TuanGouView extends BaseView implements OnItemClickListener,
		ItemTabBase, OnClickListener, OnSuccessListener<TuanModel>,
		OnScrollListener {

	private BaseActivity mActivity;
	private ViewGroup mParent;
	boolean firstExec = true;
	private Ajax mAjax;
	private ListView mListView;
	private TuanItemAdapter mTuanListAdapter;
	private View mFooterView;
	private View mHeaderView;
	private TuanModel mTuanModel;
	private ArrayList<TuanProductModel> mModels;
	private boolean mRequesting = false;
	private int mPageNum = 1;
	private int cid = 0;
	// private SlidingDrawer mSlidingDrawer;
	private TextView mCategoryView;
	// private ListView cateInfoListView;
	private SubviewNetSuccessListener mParentListener;
	private ArrayList<TextView> mTextViewList;
	private ArrayList<ImageView> mImageDivViewList;
	private static final String TAG = TuanGouView.class.getSimpleName();

	public TuanGouView(BaseActivity activity) {
		mActivity = activity;
		mParent = (ViewGroup) mActivity
				.findViewById(R.id.item_relative_tab_content_tuangou);
	}

	public void setListener(SubviewNetSuccessListener aL) {
		mParentListener = aL;
	}

	@Override
	public void init() {
		if (!firstExec)
			return;

		firstExec = false;
		mParent.removeAllViews();
		LayoutInflater.from(mActivity).inflate(R.layout.yiqiang_tab_tuangou,
				mParent, true);
		
		if( null == mCategoryView ) {
			mCategoryView = (TextView) mParent.findViewById(R.id.myCategory);
			mCategoryView.setOnClickListener(this);
		}
		
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
		if (0 >= nPageNum)
			return;

		Ajax ajax = ServiceConfig.getAjax(Config.URL_EVENT_TUAN, nPageNum
				+ "&cid=" + cid);
		if (null == ajax)
			return;
		if (1 == nPageNum) {
			if( null != mCategoryView )
				mCategoryView.setVisibility(View.GONE);
			mActivity.showLoadingLayer();
		}
		else {
			addFooterView();
		}

		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(mActivity);
		ajax.setParser(new TuanParser());
		ajax.send();
		mRequesting = true;
	}

	@Override
	public void onSuccess(TuanModel model, Response response) {
		mActivity.closeLoadingLayer();
		if (null != mParentListener)
			mParentListener.onSubviewFinished(YiQiangActivity.PARAM_TAB_TUAN, 0);

		mTuanModel = model;
		mRequesting = false;
		mPageNum = mTuanModel.getPageNum();
		requestFinish();

		this.postRequest();
	}
	
	void postRequest() {
		if( null != mCategoryView )
			mCategoryView.setVisibility(View.VISIBLE);
	}
	
	
	/*
	 * 把整数变成数组形式
	 */
	private ArrayList<Integer> handleBuyNum(int nNum) {
		ArrayList<Integer> pNumList = new ArrayList<Integer>();
		if(0 >= nNum) {
			return pNumList;
		}
		
		int pReminder = 0;
		int nId;
		for( nId=0; 0 != nNum; nId++){
			pReminder = nNum % 10;
			pNumList.add(nId, Integer.valueOf(pReminder));
			nNum = nNum / 10;
		}
		
		return pNumList;
	}

	public void requestFinish() {
		if (null == mModels || mPageNum == 1) {
			mModels = mTuanModel.getTuanProductModles();
		} else {
			ArrayList<TuanProductModel> aRef = mTuanModel
					.getTuanProductModles();
			if (null != aRef) {
				mModels.addAll(aRef);
			}
		}

		if (mModels == null || mModels.size() == 0) {
			UiUtils.makeToast(mActivity, "团购已结束.");
			if(mTuanListAdapter != null) {
				mTuanListAdapter.notifyDataSetChanged();
			}
			return;
		}

		if (mTuanModel.getNow() <= mTuanModel.getBegin()) {
			UiUtils.makeToast(mActivity, "团购暂未开始");
			if(mTuanListAdapter != null) {
				mTuanListAdapter.notifyDataSetChanged();
			}
			return;
		}

		if(null==mHeaderView)
			mHeaderView =  LayoutInflater.from(mActivity).inflate(
				R.layout.tuan_list_header, null);
		/*
		 * if( null == mClock ) { mClock = (HeaderClock)
		 * mHeaderView.findViewById(R.id.tuan_clock);
		 * mClock.setLayout(R.layout.tuan_title_clock);
		 * mClock.setCurrentTime(mTuanModel.getNow() * 1000);
		 * mClock.setEndTime(mTuanModel.getEnd() * 1000);
		 * mClock.setOnArriveListener(new Runnable() {
		 * 
		 * @Override public void run() {} }); mClock.run(); }
		 */

		if (null == mListView || mPageNum == 1) {
			mListView = (ListView) mParent.findViewById(R.id.tuan_listview);
			mTuanListAdapter = new TuanItemAdapter(mActivity, mModels);

			if (mListView.getHeaderViewsCount()<=0) {
			
				mListView.addHeaderView(mHeaderView);
			}
			
			//处理参团人数
			if(null==mTextViewList)
			{
				mTextViewList = new ArrayList<TextView>();
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_1));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_2));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_3));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_4));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_5));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_6));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_7));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_8));
				mTextViewList.add((TextView) mActivity.findViewById(R.id.buynum_9));
				
				mImageDivViewList = new ArrayList<ImageView>();
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div1));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div2));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div3));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div4));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div5));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div6));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div7));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div8));
				mImageDivViewList.add((ImageView)mActivity.findViewById(R.id.buynum_div9));
				
			}
			
			ArrayList<Integer> pBuyNumList = handleBuyNum(mTuanModel.getBuyNum());
			int nLength = pBuyNumList.size();
			if(nLength > 9) {
				nLength = 9;
			}
			for(int nId=0; nId<nLength; nId++) {
				ImageView  pDiv = mImageDivViewList.get(nId);
				TextView pTextView = mTextViewList.get(nId);
				int nValue = pBuyNumList.get(nId).intValue();
				if(null!=pTextView)
				{
					pTextView.setVisibility(View.VISIBLE);
					pTextView.setText(String.valueOf(nValue));
				}
				if(null!=pDiv)
				{
					pDiv.setVisibility(View.VISIBLE);
				}
			}
			if(null==mFooterView)
			{
				mFooterView = (LinearLayout) LayoutInflater.from(mActivity)
					.inflate(R.layout.global_listview_loading, null);
			}
			//only one page
			if (mListView.getFooterViewsCount() > 0 ||
					(mPageNum >= mTuanModel.getPageCount())) {
				removeFooterView();
			} else
				addFooterView();
			
			mListView.setDividerHeight(0);
			mListView.setAdapter(mTuanListAdapter);
			mListView.setOnItemClickListener(this);
			mListView.setOnScrollListener(this);
		} else {
			removeFooterView();

			mTuanListAdapter.notifyDataSetChanged();
		}

		/*
		 * 右侧抽屉，现在不用了 if(cateInfoListView == null || mPageNum ==1){
		 * mSlidingDrawer = (SlidingDrawer)
		 * mActivity.findViewById(R.id.slidingdrawer); cateInfoListView =
		 * (ListView) mActivity.findViewById(R.id.myContent);
		 * cateInfoListView.setDividerHeight(0); cateInfoListView.setAdapter(new
		 * TuanCategoryItemAdapter(mActivity, mTuanModel.getCateInfos()));
		 * cateInfoListView.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) {
		 * 
		 * mSlidingDrawer.close(); cid =
		 * mTuanModel.getCateInfos().get(position).cid; mPageNum=1;
		 * sendRequest(mPageNum); } }); }
		 */

	}

	private void removeFooterView() {
		if ((null != mFooterView) && (null != mListView)
				&& (mListView.getFooterViewsCount() > 0)) {
			mListView.removeFooterView(mFooterView);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView aView, int nScrollState) {
		if(aView == null) {
			Log.d(TAG, "[onScrollStateChanged], aView is null");
			return;
		}
		Drawable drawable = mCategoryView.getBackground();
		if (OnScrollListener.SCROLL_STATE_IDLE == nScrollState) {
			if(drawable != null) {
				drawable.setAlpha(255);
			}

			if (aView.getLastVisiblePosition() == aView.getCount() - 1) {
				// Check whether is requesting.
				if ((mRequesting) || (null == mTuanModel)
						|| (mPageNum >= mTuanModel.getPageCount()))
					return;
				// Send request for next page.
				sendRequest(mPageNum + 1);
			}
		} else if (OnScrollListener.SCROLL_STATE_FLING == nScrollState
				|| OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == nScrollState) {
			if(drawable != null) {
				drawable.setAlpha(255);
			}
		}
	}

	private void addFooterView() {
		if ((null != mFooterView) && (null != mListView)
				&& (0 >= mListView.getFooterViewsCount())) {
			mListView.addFooterView(mFooterView);
		}
	}

	@Override
	public void onClick(View v) {
		ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "23001");
		showCategory();
	}

	private void showCategory() {
		if(null==mTuanModel)
			return;
		
		final ArrayList<CateInfo> items = mTuanModel.getCateInfos();
		if(null == items)
			return;
		
		String[] names = new String[items.size()];

		int selectedIndex = 0;
		String category = mCategoryView.getText().toString();
		for (int i = 0, len = names.length; i < len; i++) {
			CateInfo item = items.get(i);
			names[i] = item.name;

			if (category.equals(item.name)) {
				selectedIndex = i;
			}
		}

		UiUtils.showListDialog(mActivity,
				mActivity.getString(R.string.select_category), names,
				selectedIndex, new RadioDialog.OnRadioSelectListener() {
					@Override
					public void onRadioItemClick(int which) {
						mCategoryView.setText(items.get(which).name);
						cid = items.get(which).cid;
						mPageNum = 1;
						sendRequest(mPageNum);
					}
				}, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int headCount = mListView.getHeaderViewsCount();
		int modelSz = mModels.size();
		position -= headCount;
		
		if (position < 0 || position >= modelSz) {// 点击0的时候是header || footer
			return;
		}
		
		Bundle param = new Bundle();
		int channel_id = ((TuanProductModel) mModels.get(position)).getChannelId();
		long product_id = ((TuanProductModel) mModels.get(position))
				.getProductId();
		param.putLong(ItemActivity.REQUEST_PRODUCT_ID, product_id);
		
		//场景CHANNEL_ID
		param.putInt(ItemActivity.REQUEST_CHANNEL_ID, channel_id);
		
		UiUtils.startActivity(mActivity, ItemActivity.class, param,true);

		String pageId = mActivity.getResources().getString(
				R.string.tag_TuanActivity);
		ToolUtil.reportStatisticsClick(((YiQiangActivity)mActivity).getActivityPageId(), ""+(30001+position),String.valueOf(product_id));
		ToolUtil.sendTrack(this.getClass().getName(), pageId, ItemActivity.class.getName(), mActivity.getString(R.string.tag_MyCouponActivity), "01011", String.valueOf(product_id));
	}

	@Override
	public void destroy() {
		mActivity = null;
		mParent = null;

		if(null!=mImageDivViewList)
			mImageDivViewList.clear();
		
		if(null!=mTextViewList)
			mTextViewList.clear();
		
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
	}
}
