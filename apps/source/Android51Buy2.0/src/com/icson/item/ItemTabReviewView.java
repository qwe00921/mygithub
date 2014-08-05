package com.icson.item;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.icson.R;
import com.icson.lib.BaseView;
import com.icson.lib.model.ReviewCountModel;
import com.icson.lib.model.ReviewModel;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ItemTabReviewView extends BaseView implements ItemTabBase, OnCheckedChangeListener, OnSuccessListener<JSONObject>, OnErrorListener, OnScrollListener {

	private static final String LOG_TAG = ItemTabReviewView.class.getName();

	private ItemActivity mActivity;

	private boolean firstExec = true;

	private ReviewCountModel mReviewCountModel;

	public static final int TAB_SATISFY = R.id.item_review_tab_satisfy;

	public static final int TAB_GENERAL = R.id.item_review_tab_general;

	public static final int TAB_UNSATISFY = R.id.item_review_tab_unsatisfy;

	private static final int REQUEST_PREPARE = 0;

	private static final int REQUEST_LOADING = 1;

	private static final int REQUEST_FINISH = 2;

	private ArrayList<ReviewModel> mSatisfyReviewModels;

	private ArrayList<ReviewModel> mGeneralReviewModels;

	private ArrayList<ReviewModel> mUnSatisfyReviewModels;

	private int mCurrentSelectTab = 0;

//	private HashMap<Integer, Integer> mPageInfo = new HashMap<Integer, Integer>();
	private SparseIntArray mPageInfo = new SparseIntArray();

//	private HashMap<Integer, Integer> mRequestStatusInfo = new HashMap<Integer, Integer>();
	private SparseIntArray mRequestStatusInfo = new SparseIntArray();

	private ViewGroup mParent;

	private ViewGroup mFooterView;

	private ItemTabReviewAdapter mItemReviewAdapter;

	private ListView mListView;

	private ArrayList<Ajax> mAjaxPool;

	private ItemTabReviewControl mItemReviewControl;

	public ItemTabReviewView(ItemActivity activity) {
		mActivity = activity;
		mItemReviewControl = new ItemTabReviewControl(mActivity);

		mSatisfyReviewModels = new ArrayList<ReviewModel>();
		mGeneralReviewModels = new ArrayList<ReviewModel>();
		mUnSatisfyReviewModels = new ArrayList<ReviewModel>();

		mAjaxPool = new ArrayList<Ajax>();
		mParent = (ViewGroup) mActivity.findViewById(R.id.item_relative_tab_content_review);
		mActivity.getLayoutInflater().inflate(R.layout.item_tab_review, mParent, true);
		
		
		mItemReviewAdapter = new ItemTabReviewAdapter(mActivity, mSatisfyReviewModels, mGeneralReviewModels, mUnSatisfyReviewModels);
		mFooterView = (ViewGroup) mActivity.getLayoutInflater().inflate(R.layout.global_listview_loading, null);

		mListView = (ListView) mActivity.findViewById(R.id.item_review_listview);
		mListView.setHeaderDividersEnabled(false);
		mListView.setFooterDividersEnabled(false);
		mListView.setDividerHeight(0);
		mListView.addFooterView(mFooterView);
		mListView.setAdapter(mItemReviewAdapter);
		mListView.setOnScrollListener(this);
	}

	@Override
	public void init() {
		if (!firstExec)
			return;
		
//		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), 
//				ItemTabParamView.class.getName(), mActivity.getString(R.string.tag_ItemTabReviewView), "02014", String.valueOf(mActivity.getProductId()));
		
		firstExec = false;

		ItemProductModel product = mActivity.getItemProductModel();
		if(product == null){
			mReviewCountModel = null;
		}else{
			mReviewCountModel = product.getReviewCountModel();
		}
		
		mSatisfyReviewModels.clear();
		mGeneralReviewModels.clear();
		mUnSatisfyReviewModels.clear();

		// 初始分页信息
		initPageInfo();

		renderTab();
		
		if(mItemReviewAdapter != null) {
			mItemReviewAdapter.notifyDataSetChanged();
		}
	}

	public void clean() {
		firstExec = true;
		if (mAjaxPool != null) {
			for (Ajax ajax : mAjaxPool) {
				ajax.abort();
			}
			mAjaxPool.clear();
		}

		if (mListView != null && mFooterView != null) {
			mListView.addFooterView(mFooterView);
		}
	}

	private void initPageInfo() {
		mPageInfo.put(TAB_SATISFY, 1);
		mPageInfo.put(TAB_GENERAL, 1);
		mPageInfo.put(TAB_UNSATISFY, 1);
	}

	public void renderTab() {
		final int satisfyNum = mReviewCountModel == null ? 0 : mReviewCountModel.getSatisfied();
		final int generalNum = mReviewCountModel == null ? 0 : mReviewCountModel.getGeneral();
		final int unSatisfyNum = mReviewCountModel == null ? 0 : mReviewCountModel.getUnsatisfied();

		// 初始状态
		mRequestStatusInfo.put(TAB_SATISFY, satisfyNum == 0 ? REQUEST_FINISH : REQUEST_PREPARE);
		mRequestStatusInfo.put(TAB_GENERAL, generalNum == 0 ? REQUEST_FINISH : REQUEST_PREPARE);
		mRequestStatusInfo.put(TAB_UNSATISFY, unSatisfyNum == 0 ? REQUEST_FINISH : REQUEST_PREPARE);

		RadioGroup group = (RadioGroup) mParent.findViewById(R.id.item_review_tab);
		group.setOnCheckedChangeListener(this);

		((RadioButton) group.findViewById(TAB_SATISFY)).setText(mActivity.getString(R.string.satisfy_num,satisfyNum));
		((RadioButton) group.findViewById(TAB_GENERAL)).setText(mActivity.getString(R.string.general_num,generalNum ));
		((RadioButton) group.findViewById(TAB_UNSATISFY)).setText(mActivity.getString(R.string.unsatisfy_num,unSatisfyNum));

		((RadioButton) group.findViewById(TAB_SATISFY)).setChecked(true);
	}

	private void notifyDataSetChanged() {
		mItemReviewAdapter.notifyDataSetChanged();
		setUpFooterView();
	}

	private void notifyDataSetInvalidated() {
		initPageInfo();
		mItemReviewAdapter.notifyDataSetInvalidated();
		setUpFooterView();
	}

	private void sendRequest() {
		if (mRequestStatusInfo.get(mCurrentSelectTab) != REQUEST_PREPARE)
			return;
		mRequestStatusInfo.put(mCurrentSelectTab, REQUEST_LOADING);
		setUpFooterView();
		int type = mCurrentSelectTab == TAB_SATISFY ? ItemTabReviewControl.REVIEW_SATISFY : (mCurrentSelectTab == TAB_GENERAL ? ItemTabReviewControl.REVIEW_GENERAL : ItemTabReviewControl.REVIEW_UNSATISFY);
		Ajax ajax = mItemReviewControl.getReviews(mActivity.getProductId(), mPageInfo.get(mCurrentSelectTab), type, this, this);
		if( null != ajax ) {
			ajax.setId(mCurrentSelectTab);
			mAjaxPool.add(ajax);
		}
	}

	private void setUpFooterView() {
		int status = mRequestStatusInfo.get(mCurrentSelectTab);
		
		if( status == REQUEST_FINISH ){
			mListView.removeFooterView(mFooterView);
		}
		else{
			if( mListView.getFooterViewsCount() == 0){
				mListView.addFooterView(mFooterView);
			}
		}
	}

	@Override
	public void destroy() {
		if (mAjaxPool != null) {
			for (Ajax ajax : mAjaxPool) {
				ajax.abort();
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (mCurrentSelectTab != 0) {
			View view = mParent.findViewById(mCurrentSelectTab);
			if (view != null) {
				((RadioButton) view).setTextColor(mActivity.getResources().getColor(R.color.main_tab));
			}
		}
		((RadioButton) mActivity.findViewById(checkedId)).setTextColor(mActivity.getResources().getColor(R.color.review_item_s));
		mCurrentSelectTab = checkedId;
		mItemReviewAdapter.setTab(mCurrentSelectTab);
		notifyDataSetInvalidated();
		sendRequest();
		
		int id = 1;
		switch(mCurrentSelectTab){
		case TAB_GENERAL:
			id = 2;
			break;
		case TAB_UNSATISFY:
			id = 3;
			break;
		}
		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_ItemActivity), ItemTabReviewView.class.getName(), mActivity.getString(R.string.tag_ItemTabReviewView), "0204"+id, String.valueOf(mActivity.getProductId()));
		
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		final int tabId = response.getId();
		mRequestStatusInfo.put(tabId, REQUEST_FINISH);
		setUpFooterView();
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		/* {"errno" : 0, "data":[]}*/
		final int tabId = response.getId();
		ArrayList<ReviewModel> models = new ArrayList<ReviewModel>();
		try {
			final int errno = v.getInt("errno");
			if (errno != 0) {
				UiUtils.makeToast(mActivity, v.optString("data", Config.NORMAL_ERROR));
				return;
			}
			
			JSONArray arrs = v.optJSONArray("data");
			if(null==arrs)
				return;
			
			for (int i = 0, len = arrs.length(); i < len; i++) {
				ReviewModel model = new ReviewModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			UiUtils.makeToast(mActivity, R.string.message_system_busy);
		} finally {
			if (models.size() == 0) {
				mRequestStatusInfo.put(tabId, REQUEST_FINISH);
				setUpFooterView();
			} else {
				switch (tabId) {
				case TAB_SATISFY:
					mSatisfyReviewModels.addAll(models);
					break;
				case TAB_GENERAL:
					mGeneralReviewModels.addAll(models);
					break;
				case TAB_UNSATISFY:
					mUnSatisfyReviewModels.addAll(models);
					break;
				}

				mPageInfo.put(tabId, mPageInfo.get(tabId) + 1);
				mRequestStatusInfo.put(tabId, models.size() < 10 ? REQUEST_FINISH : REQUEST_PREPARE);
				notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && (view.getLastVisiblePosition() == view.getCount() - 1)) {
			sendRequest();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
}
