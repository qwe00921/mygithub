package com.tencent.djcity.my;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;


public class MyOrderListActivity extends BaseActivity implements OnCheckedChangeListener, OnScrollListener, OnSuccessListener<OrderModel>, OnItemClickListener{
	public static int REQUEST_ORDER_ONE_MONTH = 1;
	public static int REQUEST_ORDER_AFTER_ONE_MONTH = 2;
	
	private RadioGroup mRadioGroup;
	private RadioButton mOneMonthOrderButton;
	private ListView mOrderOneMonthList;
	private ListView mOrderAfterOneMonthList;
	private int mFlag; // 1: 一个月订单 ； 2: 一个月外订单
	private int mLastCheckedId;
	private int mPageOneMonthOrder;
	private int mPageAfterOneMonthOrder;
	
	private boolean mRequesting;
	private boolean isFirstOneMonth;
	private boolean isFirstAfterOneMonth;
	private Ajax mAjax;
	private OrderParser mOrderParser;
	private OrderListAdapter mAdapter;
	private ArrayList<OrderItemModel> mOneMonthItemModels;
	private OrderModel mOneMonthOrderModel;
	
	private ArrayList<OrderItemModel> mAfterOneMonthItemModels;
	private OrderModel mAfterOneMonthOrderModel;
	
	private ArrayList<OrderItemModel> mItemModels;
	private ImageView mGameIcon;
	private TextView mGameInfo;
	private ImageLoader mImageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_list);
		
		this.loadNavBar(R.id.orderlist_navbar);
		initUI();
		
		mPageOneMonthOrder = 1;
		mPageAfterOneMonthOrder = 1;
		mRequesting = false;
		isFirstOneMonth = true;
		isFirstAfterOneMonth = true;
		mOrderParser = new OrderParser();
		mOneMonthItemModels = new ArrayList<OrderItemModel>();
		mAfterOneMonthItemModels = new ArrayList<OrderItemModel>();
		mItemModels = new ArrayList<OrderItemModel>();
		mImageLoader = new ImageLoader(this, Config.CHANNEL_PIC_DIR, true);
		
		mAdapter = new OrderListAdapter(this, mItemModels);
		mOrderOneMonthList.setAdapter(mAdapter);
		mOrderAfterOneMonthList.setAdapter(mAdapter);
		
		setGameInfo();
		mOneMonthOrderButton.setChecked(true);
		
	}

	private void initUI(){
		mRadioGroup = (RadioGroup) findViewById(R.id.order_radiogroup);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		mOneMonthOrderButton = (RadioButton) findViewById(R.id.order_one_month);
		
		mOrderOneMonthList = (ListView) findViewById(R.id.order_oneMonth);
		mOrderAfterOneMonthList = (ListView) findViewById(R.id.order_after_oneMonth);
		mOrderOneMonthList.setOnItemClickListener(this);
		mOrderAfterOneMonthList.setOnItemClickListener(this);
		
		mGameIcon = (ImageView) findViewById(R.id.game_icon);
		mGameInfo = (TextView) findViewById(R.id.game_name);
		
		
	}
	
	private void setGameInfo(){
		String strUrl = null;
		String strGameInfo = "";
		GameInfo info = GameInfo.getGameInfoFromPreference();
		if(info != null) {
			strUrl = info.getBizImg();
			strGameInfo = info.getDescription();
		}
		
		if(TextUtils.isEmpty(strUrl) && TextUtils.isEmpty(strGameInfo)) {
			findViewById(R.id.orderlist_gameinfo).setVisibility(View.GONE);
		}
		
		if(TextUtils.isEmpty(strUrl)) {
			mGameIcon.setVisibility(View.GONE);
		}else{
			final Bitmap data = mImageLoader.get(strUrl);
			if (data != null) {
				mGameIcon.setImageBitmap(data);
				return;
			}
			
			mGameIcon.setImageBitmap(mImageLoader.getLoadingBitmap(this));
			mImageLoader.get(strUrl, new ImageLoadListener() {
				
				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
					mGameIcon.setImageBitmap(aBitmap);
				}
				
				@Override
				public void onError(String strUrl) {
					
				}
			});
		}
		
		mGameInfo.setText(strGameInfo);
		
	}
	
	
	private void getOrderOneMonth(int page) {
		mAjax = AjaxUtil.post("http://apps.game.qq.com/daoju/v3/test_apps/listOrder.php");
//		mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_DETAIL, strInfo);
		if (null == mAjax)
			return;

		mAjax.setData("uin", ILogin.getLoginUin());
		mAjax.setData("biz", ILogin.getLoginUin());
		mAjax.setData("isOneMonth", 1);
		mAjax.setData("page", 1);
		mAjax.setData("pageSize", 2);
		mAjax.setOnSuccessListener(this);
		mAjax.setParser(mOrderParser);
		mAjax.setOnErrorListener(this);
		mAjax.setId(REQUEST_ORDER_ONE_MONTH);
//		mActivity.showLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
		mAjax.send();
		
	}
	
	private void getOrderAfterOneMonth(int page) {
		mAjax = AjaxUtil.post("http://apps.game.qq.com/daoju/v3/test_apps/listOrder.php");
//		mAjax = ServiceConfig.getAjax(Config.URL_PRODUCT_DETAIL, strInfo);
		if (null == mAjax)
			return;

		mAjax.setData("uin", ILogin.getLoginUin());
		mAjax.setData("biz", ILogin.getLoginUin());
		mAjax.setData("isOneMonth", 0);
		mAjax.setData("page", 1);
		mAjax.setData("pageSize", 2);
		mAjax.setOnSuccessListener(this);
		mAjax.setParser(mOrderParser);
		mAjax.setOnErrorListener(this);
		mAjax.setId(REQUEST_ORDER_AFTER_ONE_MONTH);
//		mActivity.showLoadingLayer(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT);
		mAjax.send();
		
	}
	
	@Override
	protected void onDestroy() {
		isFirstOneMonth = true;
		isFirstAfterOneMonth = true;
		
		mOrderParser = null;
		mAdapter = null;
		mOneMonthItemModels.clear();
		mOneMonthItemModels = null;
		mOneMonthOrderModel = null;
		
		mAfterOneMonthItemModels.clear();
		mAfterOneMonthItemModels = null;
		mAfterOneMonthOrderModel = null;;
		
		mItemModels.clear();
		mItemModels = null;
		
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (mLastCheckedId != 0 && mLastCheckedId != checkedId) {
			View title = group.findViewById(mLastCheckedId);
			if (title != null) {
				((RadioButton) title).setTextColor(getResources().getColor(R.color.red));
//				((RadioButton) title).setBackgroundDrawable(getResources().getDrawable(R.drawable.login_tab_normal));
			}

//			View content = findViewById(tabs.get(lastSelectIndex));
//			if (content != null) {
//				content.setVisibility(View.GONE);
//			}
		}

		View title = group.findViewById(checkedId);
		((RadioButton) title).setTextColor(getResources().getColor(R.color.white));
//		((RadioButton) title).setBackgroundDrawable(getResources().getDrawable(R.drawable.login_tab_press));

//		View content = findViewById(tabs.get(checkedId));
//		if (content != null) {
//			content.setVisibility(View.VISIBLE);
//		}

		switch (checkedId) {
		case R.id.order_one_month:
			mFlag = 1;
			if(isFirstOneMonth) {
				getOrderOneMonth(mPageOneMonthOrder);
				isFirstOneMonth = false;
			}
			break;
			
		case R.id.order_after_one_month:
			mFlag = 2;
			if(isFirstAfterOneMonth) {
				getOrderAfterOneMonth(mPageAfterOneMonthOrder);
				isFirstAfterOneMonth = false;
			}
			break;
		}
		
		freshUI();
		mLastCheckedId = checkedId;
	}
	
	private void freshUI(){
		if(1 == mFlag) {
			mOrderOneMonthList.setVisibility(View.VISIBLE);
			mOrderAfterOneMonthList.setVisibility(View.GONE);
			mItemModels.clear();
			mItemModels.addAll(mOneMonthItemModels);
		}else if(2 == mFlag) {
			mOrderOneMonthList.setVisibility(View.GONE);
			mOrderAfterOneMonthList.setVisibility(View.VISIBLE);
			mItemModels.clear();
			mItemModels.addAll(mAfterOneMonthItemModels);
		}
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if ( (OnScrollListener.SCROLL_STATE_IDLE == scrollState) && (view.getLastVisiblePosition() == view.getCount() - 1) )
		{
			// Check whether is requesting.
//			if( (mRequesting) || (null == mModel) || (mPageNum >= mModel.getPageCount())  )
//				return ;
			
			// Send request for next page.
			if(1 == mFlag) {
				mPageOneMonthOrder ++ ;
				getOrderOneMonth(mPageOneMonthOrder);
			}else if(2 == mFlag) {
				mPageAfterOneMonthOrder ++ ;
				getOrderAfterOneMonth(mPageAfterOneMonthOrder);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
	}


	@Override
	public void onSuccess(OrderModel model, Response response) {
		int id = response.getId();
		if(!mOrderParser.isSuccess()) {
			if(id == REQUEST_ORDER_ONE_MONTH) {
				mOneMonthOrderModel = null;
				mOneMonthItemModels.clear();
			}else if(id == REQUEST_ORDER_AFTER_ONE_MONTH) {
				mAfterOneMonthOrderModel = null;
				mAfterOneMonthItemModels.clear();
			}
			
			mAdapter.notifyDataSetChanged();
			return;
		}
		
		
		if(id == REQUEST_ORDER_ONE_MONTH) {
			mOneMonthOrderModel = model;
			if(null == mOneMonthItemModels) {
				mOneMonthItemModels = new ArrayList<OrderItemModel>();
			}
			
			if(null != mOneMonthOrderModel.getItemModels()) {
				mOneMonthItemModels.addAll(mOneMonthOrderModel.getItemModels());
			}
			
		}else if(id == REQUEST_ORDER_AFTER_ONE_MONTH) {
			mAfterOneMonthOrderModel = model;
			if(null == mOneMonthItemModels) {
				mAfterOneMonthItemModels = new ArrayList<OrderItemModel>();
			}
			
			if(null != mAfterOneMonthOrderModel.getItemModels()) {
				mAfterOneMonthItemModels.addAll(mAfterOneMonthOrderModel.getItemModels());
			}
		}
		
		
		if(1 == mFlag) {
			mItemModels.clear();
			mItemModels.addAll(mOneMonthItemModels);
		}else if(2 == mFlag) {
			mItemModels.clear();
			mItemModels.addAll(mAfterOneMonthItemModels);
		}
		
		mAdapter.notifyDataSetChanged();
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(null != mItemModels && mItemModels.size() > position) {
			OrderItemModel model = mItemModels.get(position);
			String strSerial = model.getSerialNum();
			Bundle param  = new Bundle();
			param.putString(OrderDetailActivity.ORDER_SERIAL, strSerial);
			ToolUtil.startActivity(this, OrderDetailActivity.class, param);
		}
		
	}

	

}
