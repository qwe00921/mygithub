package com.icson.more;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.model.FeedbackItemModel;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class FeedBackHistoryActivity extends BaseActivity {

	private View mFooterView;
	private ListView mListView;
	private int mPage = 1;
	private boolean loadedDone = false;
	private Ajax mAjax;
	private boolean hasInit = false;
	private FeedbackListAdapter adapter;
	private RelativeLayout mEmptyView;

	private ArrayList<FeedbackItemModel> mfbModelList = new ArrayList<FeedbackItemModel>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_feedback_history);
		loadNavBar(R.id.feedback_navbar);

		mNavBar.setOnDrawableRightClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToolUtil.startActivity(FeedBackHistoryActivity.this, AdviseActivity.class);
			}
		});

		mEmptyView = (RelativeLayout) findViewById(R.id.feedback_empty);
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mListView = ((ListView) findViewById(R.id.feedback_listview));
		mListView.addFooterView(mFooterView);

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
						&& (!loadedDone)
						&& (view.getLastVisiblePosition() >= view.getCount() - 1)) {

					initData(mPage + 1);
				}
			}
		});

		hasInit = true;
		initData(1);
	}

	public void destroy() {
		mFooterView = null;
		mListView = null;
	}

	public void initData(int page) {
		if (null != mAjax || false == hasInit) {
			return;
		}
		mAjax = ServiceConfig.getAjax(Config.URL_FB_GET_HISTORY);

		if (null == mAjax)
			return;

		if (page == 1) {
			showLoadingLayer();
		}

		mPage = page;
		mAjax.setData("uid", ILogin.getLoginUid());
		mAjax.setData("page", mPage);

		mAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				ArrayList<FeedbackItemModel> models = new ArrayList<FeedbackItemModel>();
				try {
					final int errno = v.getInt("errno");
					if (errno != 0) {
						String strMsg = v.optString("data", "");
						if (errno == Config.NOT_LOGIN) {
							ILogin.clearAccount();
							UiUtils.makeToast(FeedBackHistoryActivity.this, TextUtils
									.isEmpty(strMsg) ? "您已退出登录" : strMsg);
							return;
						}

						strMsg = TextUtils.isEmpty(strMsg) ? Config.NORMAL_ERROR
								: strMsg;
						UiUtils.makeToast(FeedBackHistoryActivity.this, strMsg);
						return;
					}

					JSONObject realData = v.getJSONObject("data");
					if (null != realData) {
						int currentPage = realData.getInt("page_current");
						int pageCount = realData.getInt("page_count");

						if (currentPage < pageCount) {
							loadedDone = false;
						} else {
							loadedDone = true;
						}

						JSONArray arrs = realData.getJSONArray("applylist");
						for (int i = 0, len = arrs.length(); i < len; i++) {
							FeedbackItemModel model = new FeedbackItemModel();
							model.parse(arrs.getJSONObject(i));
							models.add(model);
						}
					}

					if (loadedDone) {
						mListView.removeFooterView(mFooterView);
					}

				} catch (Exception ex) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
					UiUtils.makeToast(FeedBackHistoryActivity.this,
							R.string.message_system_busy);
				} finally {
					requestFinish(models);
				}

				mAjax = null;
			}

		});
		mAjax.setOnErrorListener(this);
		addAjax(mAjax);
		mAjax.send();
	}

	private void requestFinish(ArrayList<FeedbackItemModel> models) {
//		for (FeedbackItemModel oneModel : models) {
//			boolean exist = false;
//			for (FeedbackItemModel oldModel : mfbModelList) {
//				if (oldModel.mApplyId == oneModel.mApplyId) {
//					exist = true;
//					break;
//				}
//			}
//			if (false == exist) {
//				mfbModelList.add(oneModel);
//			}
//		}

		mfbModelList.addAll(models);
		closeLoadingLayer();
		if (mfbModelList.size() > 0) {
			if (null == adapter) {
				adapter = new FeedbackListAdapter(this, mfbModelList);
				mListView.setAdapter(adapter);
			} else {
				adapter.setDataSource(mfbModelList);
				adapter.notifyDataSetChanged();
			}
			mEmptyView.setVisibility(View.INVISIBLE);
		} else {
			mEmptyView.setVisibility(View.VISIBLE);
		}
		
		Handler FadingHandler = new Handler();
		FadingHandler.post(new Runnable(){
			@Override
			public void run() {
				if ((!loadedDone) && (mListView.getLastVisiblePosition() >= mListView.getCount() - 1)) {
					initData(mPage + 1);
				}
			}});
	}


	@Override
	public void onError(Ajax ajax, Response response) {
		mAjax = null;
		UiUtils.makeToast(this, R.string.network_error);
	}

	@Override
	public void onResume() {
		super.onResume();
		mfbModelList.clear();
		
		if(null != adapter) {
			adapter.notifyDataSetChanged();
		}
		initData(1);
	}

	@Override
	public String getActivityPageId() {
		return "000000";
	}
}