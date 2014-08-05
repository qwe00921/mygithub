package com.icson.my.orderlist;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.model.OrderModel;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.order.OrderControl;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class VPOrderListActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnErrorListener {

	private View mFooterView;
	private ListView mVPOrderListView;
	private BaseAdapter mVPOrderListAdapter;
	private OrderControl mOrderControl;
	private Ajax mAjax;
	private int mPage;
	private boolean isOneMonthAgo;
	private boolean loadedDone;
	private ArrayList<OrderModel> mOrderModelList = new ArrayList<OrderModel>();
	private ArrayList<OrderModel> mAppendModels = new ArrayList<OrderModel>();
	
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
		loadNavBar(R.id.listview_navigation_bar);
		mOrderControl = new OrderControl(this);
		
		mVPOrderListView = (ListView) findViewById(R.id.list_container);
		
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mVPOrderListView.addFooterView(mFooterView);
		mVPOrderListView.setOnScrollListener(new OnScrollListener(){

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
		
		mVPOrderListAdapter = new VPOrderListAdapter(this, mOrderModelList);
		mVPOrderListView.setAdapter(mVPOrderListAdapter);
		mVPOrderListView.setDividerHeight(0);
		
		showLoading(true);
	}
	
	private void requestData() {
		mAjax = mOrderControl.getOrderList(mPage, isOneMonthAgo, this, this, OrderControl.ORDER_TYPE_VIRTUAL);
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
			if(!ToolUtil.isEmptyList(data, "vp_orders")){
				JSONArray vp_arrs = data.getJSONArray("vp_orders");
				for (int i = 0, len = vp_arrs.length(); i < len; i++) {
					VPOrderModel model = new VPOrderModel();
					model.parse(vp_arrs.getJSONObject(i));
					mAppendModels.add(model);
				}
			}
			
			JSONObject pageInfo = data.getJSONObject("page");
			int currentPage = pageInfo.getInt("current_page");
			int pageCount = pageInfo.getInt("page_count");

			boolean bak = isOneMonthAgo;
			// 当月订单
			if (!isOneMonthAgo) {
				if (currentPage < pageCount - 1) {
					mPage++;
				} else {
					// 当月订单已拉完, 开始拉取下个月的订单
					isOneMonthAgo = true;
					mPage = 0;
				}
			}
			// 下个月的订单
			else {
				if (currentPage < pageCount - 1) {
					mPage++;
				} else {
					// 拉取完毕标识符
					loadedDone = true;
				}
			}
			
			mAjax = null;
			// 当月订单的最后一页，如果太少，自动拉取下个月的订单，一并显示
			if (!bak && currentPage >= pageCount - 1 && mAppendModels.size() < 3) {
				requestData();
			} else {
				// 通知更新页面
				if (mAppendModels.size() > 0) {
					mOrderModelList.addAll(mAppendModels);
					mAppendModels.clear();
					//按订单时间排序
					/*Collections.sort(mOrderModelList, new Comparator<OrderModel>(){
						@Override
						public int compare(OrderModel lhs, OrderModel rhs) {
							if(rhs.getOrderDate() == lhs.getOrderDate())
							{
								int lid = lhs.getPackageIndex();
								int rid = rhs.getPackageIndex();
								return (lid-rid);
							}
							else
								return (int)(rhs.getOrderDate() - lhs.getOrderDate());
						}
					});*/
//					resetIdxofPackageInOrder();
					mVPOrderListAdapter.notifyDataSetChanged();
				}
			}
			
			if(mOrderModelList == null || mOrderModelList.size() == 0) {
				TextView tv = (TextView) findViewById(R.id.empty_textview);
				if(tv != null) {
					tv.setText(getString(R.string.no_vp_order_record));
				}
				mVPOrderListView.setEmptyView(tv);
			}

			if (loadedDone) {
				mVPOrderListView.removeFooterView(mFooterView);
			}
			showLoading(false);
			
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "getOrderList|onSuccess|" + ToolUtil.getStackTraceString(e));
			onError(mAjax, response);
		}
	}
	
	
//	private ArrayList<OrderModel> getOrderListFromResult(JSONObject data) throws Exception{
//		ArrayList<OrderModel> list = new ArrayList<OrderModel>();
//		
//		if(!ToolUtil.isEmptyList(data, "vp_orders")){
//			JSONArray vp_arrs = data.getJSONArray("vp_orders");
//			for (int i = 0, len = vp_arrs.length(); i < len; i++) {
//				VPOrderModel model = new VPOrderModel();
//				model.parse(vp_arrs.getJSONObject(i));
//				list.add(model);
//			}
//		}
//		return list;
//	}

	@Override
	public void onError(Ajax ajax, Response response) {
		if (mAjax == ajax) {
			mAjax = null;
			UiUtils.makeToast(this, R.string.network_error);

			// Reset the reload value.
			AppStorage.setData(AppStorage.SCOPE_DEFAULT,
					AppStorage.KEY_MINE_RELOAD, "1", false);
		} else {
			showLoading(false);
			super.onError(ajax, response);
		}
	}
	
//	private static class UIHandler extends Handler {
//		private WeakReference<VPOrderListActivity> mRef;
//		public UIHandler(VPOrderListActivity activity) {
//			mRef = new WeakReference<VPOrderListActivity>(activity);
//		}
//		
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			super.handleMessage(msg);
//		}
//	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_VPOrderListActivity);
	}
}
