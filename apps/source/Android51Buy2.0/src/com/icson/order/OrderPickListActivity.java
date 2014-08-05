package com.icson.order;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.my.orderdetail.OrderDetailActivity;
import com.icson.my.orderlist.VPOrderModel;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class OrderPickListActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnErrorListener, OnItemClickListener {
	private static final String 	LOG_TAG 				= OrderPickListActivity.class.getName();
	public static final String 		REQUEST_TAB_NAME 		= "tab_name";
	public static final String 		USER_INFO_MODEL 		= "user_info";
	
	public static final int 	REQUEST_FLAG_ORDER_STATUS 	= 10;
	public static final int 	MENU_ADDRESS_DELETE 		= 1;
	public static final int 	MENU_COLLECT_DELETE 		= 2;
	public static final int 	REQUEST_FLAG_COLLECT 		= 3;
	public static final int 	RESPONSE_FLAG_COLLECT 		= 4;

	private View mNoneOrderView;
	private View mFooterView;
	private ListView mListView;
	private Ajax mAjax;
	private int mPage;  
	
	private boolean loadedDone 		= false;
	private boolean isOneMonthAgo 	= false;
	private boolean firstExce 		= true;
	private boolean orderPickMode   = false;
	//private boolean mBackable       = false;
	
	private ArrayList<OrderModel> mOrderModelList = new ArrayList<OrderModel>();
	private SimpleOrderListAdapter mMyOrderListAdapter;
	private ArrayList<OrderModel> mAppendModels;
	private OrderControl mOrderControl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_pick_list);

		orderPickMode = getIntent().getBooleanExtra("orderPickMode", false);
		initUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// alwaays Reload the information.
		this.cleanup();
		this.init();
	/*
		if( mBackable ) {
			this.cleanup();
			
			// Reload the information.
			this.init();
		} else {
			// Check whether personal information updated.
			String strVal = AppStorage.getData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD);
			if( (TextUtils.isEmpty(strVal)) || (strVal.equals("1")) ) {
				this.cleanup();
				
				// Reload the information.
				this.init();
				
				// Reset the value.
				AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "0", false);
			}
		}*/
	}
	
	private void cleanup() {
		loadedDone = false;
		isOneMonthAgo = false;
		firstExce = true;
		if( null != mOrderModelList ) {
			mOrderModelList.clear();
		}
		if( null != mAppendModels ) {
			mAppendModels.clear();
		}
		if( null != mMyOrderListAdapter ) {
			mMyOrderListAdapter.notifyDataSetChanged();
		}
		mAjax = null;
		mPage = 0;
	}

	private void initUI() {
		if(null == mOrderModelList)
			mOrderModelList = new ArrayList<OrderModel>();
		if(null == mAppendModels)
			mAppendModels = new ArrayList<OrderModel>();
		
		mOrderControl = new OrderControl(this);
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mListView = (ListView) findViewById(R.id.orderlist_container);
		mListView.addFooterView(mFooterView);
		
		mListView.setOnScrollListener(new OnScrollListener(){

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
		
		
		mMyOrderListAdapter = new SimpleOrderListAdapter(this, mOrderModelList);
		
		mListView.setAdapter(mMyOrderListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setDividerHeight(0);
		
		loadNavBar(R.id.order_list_navigation_bar);
	}

	public SimpleOrderListAdapter getAdapter() {
		return mMyOrderListAdapter;
	}

	private void init() {
		if (loadedDone || mAjax != null) {
			return;
		}

		if (firstExce) {
			requestData();
			firstExce = false;
		}
	}

	private void requestData() {
		mAjax = mOrderControl.getOrderList(mPage, isOneMonthAgo, this, this, OrderControl.ORDER_TYPE_BOTH);
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		try {
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

			final JSONArray arrs = data.getJSONArray("orders");
			
			for (int i = 0, len = arrs.length(); i < len; i++) {
				OrderModel model = new OrderModel();
				model.parse(arrs.getJSONObject(i));
				mAppendModels.add(model);
			}
			//虚拟订单列表
			if(!ToolUtil.isEmptyList(data, "vp_orders") && false == orderPickMode){
				JSONArray vp_arrs = data.getJSONArray("vp_orders");
				for (int i = 0, len = vp_arrs.length(); i < len; i++) {
					VPOrderModel model = new VPOrderModel();
					model.parse(vp_arrs.getJSONObject(i));
					mAppendModels.add(model);
				}
			}
			//测试数据
			/*JSONObject vp =new JSONObject(
					"{\"order_char_id\":\"1030123633\",\"order_id\":30123633,\"order_date\":\"1352254266\",\"order_cost\":9923,\"status\":0,\"status_name\":\"待支付\",\"card_money\":10000,\"receiver\":\"1352254266\",\"pay_type\":21,\"pay_type_name\":\"支付宝\",\"product_list_str\":\"上海移动100元充值卡\"}");
			VPOrderModel model = new VPOrderModel();
			model.parse(vp);
			mAppendModels.add(model);*/
			
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
					resetIdxofPackageInOrder();
					mMyOrderListAdapter.notifyDataSetChanged();
				}
			}

			if (loadedDone) {
				mListView.removeFooterView(mFooterView);
				if(mOrderModelList.size()<=0)
				{
					if(null==mNoneOrderView)
						mNoneOrderView =this.findViewById(R.id.no_order_text);
					mNoneOrderView.setVisibility(View.VISIBLE);
				}
			}
			
		} catch (Exception ex) {
			Log.e(LOG_TAG, "getOrderList|onSuccess|" + ToolUtil.getStackTraceString(ex));
			mAppendModels.clear();
			onError(mAjax, response);
		}
	}
/**  
	* method Name:resetIdxofPackageInOrder    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void resetIdxofPackageInOrder() {
		if(mOrderModelList.isEmpty())
			return;
		
		String pOrderId = "";
		int pos = 0;
		OrderModel lastOne = null;
		for(OrderModel item : mOrderModelList)
		{
			if(item.isPackage())
			{
				if(TextUtils.isEmpty(pOrderId) || 	
						!item.getPackageOrderId().equals(pOrderId))
				{
					pos = 0;
					pOrderId = item.getPackageOrderId();
					if(null!=lastOne)
					{
						lastOne.setLastPackageinOrder();
						lastOne = null;
					}
				}
				item.setPackageIdxInOrder(pos++);
				lastOne = item;
			}
			else if(!(item instanceof VPOrderModel))
			{
				if(null!=lastOne)
				{
					lastOne.setLastPackageinOrder();
					lastOne = null;
				}
			}
		}
		
		if(loadedDone && null!=lastOne)
		{
			lastOne.setLastPackageinOrder();
			lastOne = null;
		}
	}

	/*
	private void resetPackinOrder() 
	{
		if(mOrderModelList.isEmpty())
			return;
		
		String pOrderId = "";
		OrderModel lastOne = null;
		for(OrderModel item : mOrderModelList)
		{
			if(item.isPackage())
			{
				//another Order
				if(!item.getPackageOrderId().equals(pOrderId) && null!=lastOne)
				{
					lastOne.setLastPackageinOrder();
				}
				pOrderId = item.getPackageOrderId();
				lastOne = item;
			}
		}
		if(null!=lastOne && loadedDone)
		{
			lastOne.setLastPackageinOrder();
		}
	}
	*/
	@Override
	public void onError(Ajax ajax, Response response) 
	{
		if (mAjax == ajax) {
			mAjax = null;
			UiUtils.makeToast(this, R.string.network_error);
			
			// Reset the reload value.
			AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
		} else {
			closeProgressLayer();
			super.onError(ajax, response);
		}
	}

	public void destroy() {
		mFooterView = null;
		mListView = null;
		mOrderControl = null;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		if (position <= -1 || position >= mMyOrderListAdapter.getCount()) 
		{
			return;
		}
		
		if (orderPickMode) {
			final Intent intent = new Intent();
			OrderModel orderModel = mOrderModelList.get(position);
			ArrayList<OrderProductModel> pModels = orderModel.getOrderProductModelList();
			ArrayList<String> pUrls = new ArrayList<String>();
			for (OrderProductModel model : pModels) {
				String url = IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 95);
				pUrls.add(url);
			}

			intent.putExtra("orderId", orderModel.getOrderCharId());
			intent.putStringArrayListExtra("prodCharIds", pUrls);
			setResult(Activity.RESULT_OK, intent);
			finish();
			return;
		}
		
		if (position > -1 && position < mOrderModelList.size()) {
			OrderModel mOrderModel = mOrderModelList.get(position);
			if(mOrderModel  instanceof VPOrderModel){
				VPOrderModel mVPOrderModel = (VPOrderModel)mOrderModel;
				Bundle param = new Bundle();
				param.putSerializable(OrderDetailActivity.REQUEST_VP_ORDER, mVPOrderModel);
				ToolUtil.startActivity(this, OrderDetailActivity.class, param, REQUEST_FLAG_ORDER_STATUS);
			}else{
				Bundle param = new Bundle();
				param.putString(OrderDetailActivity.REQUEST_ORDER_CHAR_ID, mOrderModel.getOrderCharId());
				param.putInt(OrderDetailActivity.REQUEST_ORDER_STATUS, mOrderModel.getStatus());
				ToolUtil.startActivity(this, OrderDetailActivity.class, param, REQUEST_FLAG_ORDER_STATUS);
				//ToolUtil.startActivity(mActivity, com.icson.amap.CargoMapActivity.class, param);//, REQUEST_FLAG_ORDER_STATUS);
			}
			
			String locationId = "";
			if(position>9)
				locationId ="030"+position;
			else
				locationId ="0300"+position;
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MyIcsonActivity), OrderDetailActivity.class.getName(), getString(R.string.tag_OrderDetailActivity), locationId);
		}
	}

	@Override
	public String getActivityPageId() {
		return "000000";
	}
}
