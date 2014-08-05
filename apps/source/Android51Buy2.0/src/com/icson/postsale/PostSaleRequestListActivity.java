package com.icson.postsale;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PostSaleRequestListActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnErrorListener {
	private View mFooterView;
	private ListView mPostSaleReqListView;
	private BaseAdapter mPostSaleReqListAdapter;
	private PostSaleControl mOrderControl;
	private Ajax mAjax;
	private int mPage = 1;
	private int mTotalPage;
	private boolean loadedDone;
	private ArrayList<PostSaleRequestModel> mRequestModelList = new ArrayList<PostSaleRequestModel>();
	private ArrayList<PostSaleRequestModel> mAppendModels = new ArrayList<PostSaleRequestModel>();
	
	private String mPageId;
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			PostSaleRequestModel model = (PostSaleRequestModel) mPostSaleReqListAdapter.getItem(position);
			if(model != null) {
				Bundle param = new Bundle();
				param.putInt(Constants.KEY_APPLY_ID, model.getApplyId());
				
				ToolUtil.startActivity(PostSaleRequestListActivity.this, PostSaleDetailActivity.class, param, -1);
				ToolUtil.sendTrack(this.getClass().getName(), mPageId, PostSaleRequestListActivity.class.getName(), getString(R.string.tag_PostSaleRequestListActivity), "02018");
				
			}
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview_withempty);
		
		initUI();
		requestData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void initUI() {
		mPageId = getString(R.string.tag_PostSaleRequestListActivity);
		
		loadNavBar(R.id.listview_navigation_bar);
		setNavBarText(R.string.post_sale_request);
		mOrderControl = new PostSaleControl(this);
		
		mPostSaleReqListView = (ListView) findViewById(R.id.list_container);
		
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mPostSaleReqListView.addFooterView(mFooterView);
		mPostSaleReqListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
						(!loadedDone) && mAjax == null && 
						(view.getLastVisiblePosition() >= view.getCount() - 1) )
				{
					
					requestData();
				}
			}		
			});
		mPostSaleReqListView.setOnItemClickListener(mOnItemClickListener);
		mPostSaleReqListAdapter = new PostSaleRequestListAdapter(this, mRequestModelList);
		mPostSaleReqListView.setAdapter(mPostSaleReqListAdapter);
		mPostSaleReqListView.setDividerHeight(0);
		
		showLoading(true);
	}
	
	private void requestData() {
		mAjax = mOrderControl.getProductChangeHistoryList(mPage, this, this);
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		
		try {
			//检查用户是否登录
			final int errno = v.getInt("errno");

			if (errno != 0) {
				String strMsg = v.optString("data", "");
				if (errno == Config.NOT_LOGIN) {
					ILogin.clearAccount();
					UiUtils.makeToast(this, TextUtils.isEmpty(strMsg) ? "您已退出登录" : strMsg);
					MainActivity.startActivity(this, MainActivity.TAB_MY);
					return;
				}

				strMsg = TextUtils.isEmpty(strMsg) ? Config.NORMAL_ERROR : strMsg;
				UiUtils.makeToast(this, strMsg);
				return;
			}
			JSONObject data = v.getJSONObject("data");
			
			int total = data.optInt("total", 0);
			if(total == 0) {
				//没有数据
				loadedDone = true;
				mRequestModelList.clear();
				mAppendModels.clear();
				mPostSaleReqListAdapter.notifyDataSetChanged();
			} else {
				if(!ToolUtil.isEmptyList(data, "entry")) {
					JSONArray vp_arrs = data.getJSONArray("entry");
					for (int i = 0, len = vp_arrs.length(); i < len; i++) {
						PostSaleRequestModel model = new PostSaleRequestModel();
						model.parse(vp_arrs.getJSONObject(i));
						mAppendModels.add(model);
					}
				}
				mTotalPage = (int) Math.ceil((double)total / (double)PostSaleControl.DATA_PAGE_SIZE);
				if(mPage < mTotalPage) {
					mPage++;
				} else {
					loadedDone = true;
				}

			}
			mAjax = null;	
			
			Message msg = mUIHandler.obtainMessage(MSG_REFRESH_UI, mAppendModels);
			mUIHandler.sendMessage(msg);
			
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "getOrderList|onSuccess|" + ToolUtil.getStackTraceString(e));
			onError(mAjax, response);
		}
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		showLoading(false);
		if (mAjax == ajax) {
			mAjax = null;
			UiUtils.makeToast(this, R.string.network_error);

			// Reset the reload value.
			AppStorage.setData(AppStorage.SCOPE_DEFAULT,
					AppStorage.KEY_MINE_RELOAD, "1", false);
		} else {
			closeProgressLayer();
			super.onError(ajax, response);
		}
	}
	
	private void refreshUI(ArrayList<PostSaleRequestModel> appendModels) {
		mRequestModelList.addAll(appendModels);
		
		mAppendModels.clear();
		
		Collections.sort(mRequestModelList, new Comparator<PostSaleRequestModel>(){
			@Override
			public int compare(PostSaleRequestModel lhs, PostSaleRequestModel rhs) {
				return (int)(rhs.getApplyId() - lhs.getApplyId());
			}
		});
		
		if(mRequestModelList == null || mRequestModelList.size() == 0) {
			mPostSaleReqListView.setEmptyView(findViewById(R.id.empty_textview));
		}
		mPostSaleReqListAdapter.notifyDataSetChanged();
		mAjax = null;	
		
		if (loadedDone) {
			mPostSaleReqListView.removeFooterView(mFooterView);
		}
		showLoading(false);
	}
	
	private void showLoading(boolean show) {
		View loadingIcon = findViewById(R.id.global_loading);
		if(loadingIcon != null) {
			if(show) {
				loadingIcon.setVisibility(View.VISIBLE);
			} else {
				loadingIcon.setVisibility(View.GONE);
			}
		}
		
		View dataView = findViewById(R.id.list_container);
		if(loadingIcon != null) {
			if(show) {
				dataView.setVisibility(View.GONE);
			} else {
				dataView.setVisibility(View.VISIBLE);
			}
		}
	}

	private static final String TAG = PostSaleRequestListActivity.class.getSimpleName();
	private UIHandler mUIHandler = new UIHandler(this);
	public static final int MSG_REFRESH_UI = 2001;
	private static class UIHandler extends Handler {
		private WeakReference<PostSaleRequestListActivity> mRef;
		public UIHandler(PostSaleRequestListActivity activity) {
			mRef = new WeakReference<PostSaleRequestListActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if(msg == null) {
				Log.e(TAG, "[handleMessage] msg is null!");
				return;
			}
			
			PostSaleRequestListActivity activity = mRef.get();
			if(activity == null) {
				Log.w(TAG, "[handleMessage] activity is null when handle message, the activity should be destoryed already");
				return;
			}
			int what = msg.what;
			switch(what) {
				case MSG_REFRESH_UI: {
					
					activity.refreshUI((ArrayList<PostSaleRequestModel>) msg.obj);
					break;
				}
				default: {
					// do nothing
					break;
				}
			}
		}
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_PostSaleRequestListActivity);
	}
}
