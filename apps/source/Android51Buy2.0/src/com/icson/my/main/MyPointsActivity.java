package com.icson.my.main;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyPointsActivity extends BaseActivity implements
		OnSuccessListener<JSONObject> {

	public static final int MY_POINTS = 1;
	public static final int MY_BALANCE = 2;
	public static final String TYPE = "type";
	private int type;
	private ListView mListView;
	private View mFooterView;
	private MyPointsAdapter mMyPointsAdapter;
	private int page = 0;
	private boolean isEnd;
	private String total_points;
	private String total_msg;
	private TextView mBalance;
	private TextView mTotalMsg;

	ArrayList<MyPointsModel> pointsFlowList = new ArrayList<MyPointsModel>();
	private ArrayList<MyPointsModel> mAppendModels = new ArrayList<MyPointsModel>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_balance);
		loadNavBar(R.id.balance_navbar);

		mListView = (ListView) findViewById(R.id.message_list);
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.global_listview_loading, null);
		
		mBalance = (TextView)findViewById(R.id.mypoint_tv_total_point);
		mBalance.setVisibility(View.GONE);
		mTotalMsg = (TextView)findViewById(R.id.mypoint_tv_total_msg);
		mTotalMsg.setVisibility(View.GONE);

		type = getIntent().getIntExtra(TYPE, MY_POINTS);
		this.setNavBarText(type == MY_POINTS ? R.string.my_points_title : R.string.my_balance_title);
		if(type == MY_POINTS){
			this.setNavBarRightText(R.string.points_rule);
			mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Bundle pParams = new Bundle();
					pParams.putString(HTML5LinkActivity.LINK_URL, "http://m.51buy.com/t/apphelp/jifen.html");
					pParams.putString(HTML5LinkActivity.ACTIVITY_TITLE, getString(R.string.points_rule));
					ToolUtil.startActivity(MyPointsActivity.this, HTML5LinkActivity.class, pParams);
				}
			});
		}
		getFlowList();
	}

	private void getFlowList() {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_MB_USER_POINTS);
		if (type == MY_BALANCE) {
			ajax = ServiceConfig.getAjax(Config.URL_MB_USER_BALANCE);
		}
		if (null == ajax)
			return;

		ajax.setTimeout(10);
		ajax.setParser(new JSONParser());
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setData("page", page);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		ajax.send();
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		// 收到流水数据
		int errno = v.optInt("errno", -1);
		if (errno != 0) {
			isEnd = true;
			initViews();
			//UiUtils.makeToast(this, v.optString("data", "网络连接异常，请重试"));
			//finish();
			return;
		}
		JSONObject data;
		try {
			data = v.getJSONObject("data");
			total_points = data.optString("total_points","0");
			total_msg = data.optString("msg");
			mAppendModels.clear();
			if (!ToolUtil.isEmptyList(data, "items")) {

				JSONArray items = data.getJSONArray("items");
				int size = items.length();
				MyPointsModel myPoint;
				for (int i = 0; i < size; i++) {
					myPoint = new MyPointsModel();
					myPoint.parse(items.getJSONObject(i));
					mAppendModels.add(myPoint);
				}
			}

			if (mAppendModels.size() == 0) {
				isEnd = true;
			} else {
				pointsFlowList.addAll(mAppendModels);
			}
			initViews();
		} catch (Exception e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
			UiUtils.makeToast(this, "网络访问异常，请重试",true);
			finish();
			return;
		}

	}

	private void initViews() {
		// 如果是加载更多的时候
		if (page > 0) {
			if (isEnd) {
				mListView.removeFooterView(mFooterView);
			} else {
				mMyPointsAdapter.notifyDataSetChanged();
			}
			return;
		}
		// 第一次的时候
		if( null != mBalance ) {
			if (type == MY_POINTS) {
				mBalance.setText(Html.fromHtml("当前可用积分：<font color=\"red\">"
						+ total_points + "</font>积分 <font color=\"#999999\">(10积分=1元)</font>"));
				
			} else {
				mBalance.setText(Html.fromHtml("当前可用余额：<font color=\"red\">¥ "
						+ total_points + "</font>"));
			}
			mBalance.setVisibility(View.VISIBLE);
		}

		// Update status.
		if( null != mTotalMsg && !TextUtils.isEmpty(total_msg) ) {
			mTotalMsg.setText(total_msg);
			mTotalMsg.setVisibility(View.VISIBLE);
		}

		//
		mListView = (ListView) findViewById(R.id.mypoint_mlistView);
		View empty = findViewById(R.id.mypoint_message_empty_layout);

		if (pointsFlowList.isEmpty()) {
			mBalance.setVisibility(View.GONE);
			mTotalMsg.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			empty.setVisibility(View.VISIBLE);
		} else {
			mListView.addFooterView(mFooterView);
			mListView.setOnScrollListener(new OnScrollListener(){

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
				}

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
							(view.getLastVisiblePosition() >= view.getCount() - 2) )
					{
						page++;
						getFlowList();
					}
				}		
				});
			mMyPointsAdapter = new MyPointsAdapter(this, pointsFlowList);
			mListView.setAdapter(mMyPointsAdapter);
			empty.setVisibility(View.GONE);
			//if no need for footer ;hide
			mListView.post(new Runnable() {
			    public void run() {
			    	if(mListView.getLastVisiblePosition()+1 >=mListView.getCount() )
					{
						mListView.removeFooterView(mFooterView);
					}
				}
			});
			
			
			
			
		}
	}

	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MyPointsActivity);
	}
}
