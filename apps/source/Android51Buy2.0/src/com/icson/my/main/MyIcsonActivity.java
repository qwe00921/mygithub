package com.icson.my.main;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.guide.UserGuideDialog;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.UserModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.more.MoreActivity;
import com.icson.my.address.MyAddressActivity;
import com.icson.my.collect.MyCollectActivity;
import com.icson.my.coupon.MyCouponActivity;
import com.icson.my.orderdetail.OrderDetailActivity;
import com.icson.my.orderlist.MyOrderListAdapter;
import com.icson.my.orderlist.VPOrderListActivity;
import com.icson.my.orderlist.VPOrderModel;
import com.icson.order.OrderControl;
import com.icson.postsale.PostSaleCenterActivity;
import com.icson.preference.Preference;
import com.icson.slotmachine.SlotMachineActivity;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyIcsonActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnErrorListener, OnClickListener, OnItemClickListener, UserGuideDialog.OnClickListener {
	private static final String 	LOG_TAG 				= MyIcsonActivity.class.getName();
	public static final String 		REQUEST_TAB_NAME 		= "tab_name";
	public static final String 		USER_INFO_MODEL 		= "user_info";
	
	public static final int 	REQUEST_FLAG_ORDER_STATUS 	= 10;
	public static final int 	MENU_ADDRESS_DELETE 		= 1;
	public static final int 	MENU_COLLECT_DELETE 		= 2;
	public static final int 	REQUEST_FLAG_COLLECT 		= 3;
	public static final int 	RESPONSE_FLAG_COLLECT 		= 4;

	private TextView mCouponTextView;
	private TextView mFavorTextView;
	private TextView mAddressTextView;
	private TextView mHistoryTextView;
	private ImageView mSettingsButton;
	private View mHeaderView;
	private View mFooterView;
	private View mNoneOrderView;
	private ListView mListView;
	private Ajax mAjax;
	private int mPage;
	private Handler mHandler = new Handler();
	private boolean loadedDone 		= false;
	private boolean isOneMonthAgo 	= false;
	private boolean firstExce 		= true;
	//private boolean mBackable       = false;
	
	private ArrayList<OrderModel> mOrderModelList = new ArrayList<OrderModel>();
	private MyOrderListAdapter mMyOrderListAdapter;
	private ArrayList<OrderModel> mAppendModels;
	private OrderControl mOrderControl;
	private UserModel mUserModel;
	private String mPageId;
	private UserGuideDialog mGuideDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_icson);
		//Intent pIntent = getIntent();
		//mBackable = (null != pIntent ? pIntent.getBooleanExtra("backable", false) : false);
		initUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		
		//clear first
		if(null!=mHandler)
			mHandler.removeCallbacksAndMessages(null);
		if( null != mOrderModelList && mOrderModelList.size() > 0) {
			mOrderModelList.clear();
			if( null != mMyOrderListAdapter ) 
				mMyOrderListAdapter.notifyDataSetChanged();
		}
		
		// alwaays Reload the information.
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
	
	@Override
	protected void onDestroy()
	{
		mFooterView = null;
		mListView = null;
		mOrderControl = null;
		
		if(null!=mHandler)
			mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		if(null!=mGuideDialog)
		{
			mGuideDialog.cleanup();
			if(mGuideDialog.isShowing())
			{
				mGuideDialog.dismiss();
			}
			mGuideDialog = null;
		}
		
		cleanAllAjaxs();
		
		cleanup();
		
		super.onPause();
	}
	
	
	private void cleanup() {
		loadedDone = false;
		isOneMonthAgo = false;
		firstExce = true;
		if( null != mAppendModels ) {
			mAppendModels.clear();
		}
		mHandler.removeCallbacksAndMessages(null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Short of mem ; may cause 反复clean 重新load
				if( null != mOrderModelList && mOrderModelList.size() > 0)
				{
					mOrderModelList.clear();
					if( null != mMyOrderListAdapter ) 
						mMyOrderListAdapter.notifyDataSetChanged();
				}
				mMyOrderListAdapter.cleanUpBitmap();
				mHandler.removeCallbacksAndMessages(null);
			}
			
		}, 1200);
		
		mAjax = null;
		mPage = 0;
	}

	private void initUI() {
		mPageId = getString(R.string.tag_MyIcsonActivity);
		if(null == mOrderModelList)
			mOrderModelList = new ArrayList<OrderModel>();
		if(null == mAppendModels)
			mAppendModels = new ArrayList<OrderModel>();
		
		mOrderControl = new OrderControl(this);
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mListView = (ListView) findViewById(R.id.my_orderlist_container);
		mListView.addFooterView(mFooterView);
		
		mListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if(scrollState == OnScrollListener.SCROLL_STATE_FLING || scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					mMyOrderListAdapter.setIsScrolling(true);
				} else {
					mMyOrderListAdapter.setIsScrolling(false);
				}
				
				
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
						(!loadedDone) && mAjax == null && 
						(view.getLastVisiblePosition() >= view.getCount() - 1) )
				{
					
					requestData();
				}
			}		
			});
		
		
		mMyOrderListAdapter = new MyOrderListAdapter(this, mListView, mOrderModelList);
		
		mHeaderView = this.getLayoutInflater().inflate(R.layout.my_orderlist_header, null);
			
		mListView.addHeaderView(mHeaderView, null, false);
			
		mSettingsButton = (ImageView) findViewById(R.id.my_orderlist_settings);
		mSettingsButton.setOnClickListener(this);
			
		mCouponTextView = (TextView) findViewById(R.id.my_icson_coupon);
		mCouponTextView.setOnClickListener(this);
			
		mFavorTextView = (TextView) findViewById(R.id.my_icson_favor);
		mFavorTextView.setOnClickListener(this);
			
		mAddressTextView = (TextView) findViewById(R.id.my_icson_address);
		mAddressTextView.setOnClickListener(this);
			
		mHistoryTextView = (TextView) findViewById(R.id.my_icson_history);
		mHistoryTextView.setOnClickListener(this);
		
		mListView.setAdapter(mMyOrderListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setDividerHeight(0);
	}

	public MyOrderListAdapter getAdapter() {
		return mMyOrderListAdapter;
	}

	private void init() {
		ToolUtil.sendTrack(MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), "02011");
		if (loadedDone || mAjax != null) {
			return;
		}

		if (firstExce) {
			if(null != mNoneOrderView)
				mListView.removeFooterView(mNoneOrderView);
			if(mListView.getFooterViewsCount() <= 0)
				mListView.addFooterView(mFooterView);
			getUserInfo();
			requestData();
			firstExce = false;
		}
	}

	private void requestData() {
		mAjax = mOrderControl.getOrderList(mPage, isOneMonthAgo, this, this, OrderControl.ORDER_TYPE_REAL);
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		try {
			Log.d("jsonliu", v);
			final int errno = v.getInt("errno");

			if (errno != 0) {
				String strMsg = v.optString("data", "");
				Log.d("jsonliu", strMsg);
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
			Log.d("jsonliu", data);
			final JSONArray arrs = data.getJSONArray("orders");
			
			for (int i = 0, len = arrs.length(); i < len; i++) {
				OrderModel model = new OrderModel();
				model.parse(arrs.getJSONObject(i));
				mAppendModels.add(model);
			}
			//虚拟订单列表
			if(!ToolUtil.isEmptyList(data, "vp_orders")){
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
								//orderid  整体从大到小排列（时间从新到旧）
								int lid = lhs.getPackageIndex();
								int rid = rhs.getPackageIndex();
								return (rid-lid);
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
						mNoneOrderView = (LinearLayout) LayoutInflater.from(MyIcsonActivity.this).inflate(R.layout.empty_orderlist_hint, null);
					mListView.addFooterView(mNoneOrderView);
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

	public void getUserInfo() 
	{
		Ajax ajax = ServiceConfig.getAjax(Config.URL_MB_USER_PROFILE);
		if( null == ajax )
			return ;
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				try {
					if (v.getInt("errno") != 0)
						return;
					mUserModel = new UserModel();
					mUserModel.parse(v.getJSONObject("data"));
				} catch (Exception ex) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
					mUserModel = null;
				} finally {
					userInfoRequestFinish();
				}
			}
		});
		ajax.setData("uid", ILogin.getLoginUid());
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		ajax.send();
	}

	public void userInfoRequestFinish() {
		if (mUserModel == null)
			return;

		String icsonId = mUserModel.getIcsonid(); 
		if( icsonId.indexOf("Login_QQ_") == 0) {
			icsonId = "QQ 用户";
		}else if( icsonId.indexOf("Login_Alipay_") == 0 ){
			icsonId = "支付宝用户";
		}else if( icsonId.indexOf("Login_IcsonWechat_" ) == 0 ) {
			icsonId = "微信用户";
		}
		
//		icsonId = icsonId.indexOf("Login_QQ_") == 0 ? "QQ 用户" : (  icsonId.indexOf("Login_Alipay_") == 0 ? "支付宝用户" : icsonId );
		
		// 昵称
		((TextView) mHeaderView.findViewById(R.id.my_main_textview_name)).setText(icsonId);

		// 等级
		((TextView) mHeaderView.findViewById(R.id.my_main_textview_level)).setText(Html.fromHtml("等级 : " + mUserModel.getLevelDesc()));
		((TextView) mHeaderView.findViewById(R.id.my_main_user_point)).setText(Html.fromHtml("积分：<font color=\"#e01e1e\">"+mUserModel.getPoint()+"</font>"));
		((TextView) mHeaderView.findViewById(R.id.my_main_user_cash_point)).setText(Html.fromHtml("余额：<font color=\"#e01e1e\">¥"+mUserModel.getCashPoint()+"</font>"));

		mHeaderView.findViewById(R.id.my_main_user_point).setOnClickListener(this);
		mHeaderView.findViewById(R.id.my_main_user_cash_point).setOnClickListener(this);
		// 头像
		int level = mUserModel.getLevel();
		level = level > 6 ? 6 : level;
		int pic_order = level + 1;

		String strUrl = ServiceConfig.getUrl(Config.URL_IMAGE_GUEST, "" + pic_order + ".png");
		AjaxUtil.getLocalImage(this, strUrl, new ImageLoadListener() {
			@Override
			public void onLoaded(Bitmap image, String url) {
				((ImageView) mHeaderView.findViewById(R.id.my_main_image_user_pic)).setImageBitmap(image);
			}

			@Override
			public void onError(String strUrl) {
			}
		});
		mCouponTextView.setText( (0 >= mUserModel.getCouponNum()) ? "优惠券" : "优惠券 (" + mUserModel.getCouponNum() + ")");
		mFavorTextView.setText( (0 >= mUserModel.getFavorNum()) ? "收藏" : "收藏 (" + mUserModel.getFavorNum() + ")");
		
		final boolean hasConpon = (mUserModel.mIsNewUser > 0 && mUserModel.mNewUserCouponImg.length() > 0);
		int showUserGuide = Preference.getInstance().getUserGuideOfIndex(Preference.USER_GUIDE_USER_CENTER);
		if (showUserGuide > 0) 
		{
			Preference.getInstance().setUserGuideOfIndex(Preference.USER_GUIDE_USER_CENTER, 0);
			if(null!=mGuideDialog)
			{
				mGuideDialog.cleanup();
				mGuideDialog = null;
			}
			if (hasConpon) 
			{
				mGuideDialog = new UserGuideDialog(
						this, this, UserGuideDialog.LAYOUT_USER_CENTER2);
				mGuideDialog.show();
				mGuideDialog.setUrlForPositiveBtn(mUserModel.mNewUserCouponImg);
			} 
			else 
			{
				mGuideDialog = new UserGuideDialog(
						this, new UserGuideDialog.OnClickListener() {

							@Override
							public void onDialogClick(UserGuideDialog dialog, int nButtonId) 
							{
								if (UserGuideDialog.BUTTON_POSITIVE == nButtonId) {
									UiUtils.startActivity(MyIcsonActivity.this, SlotMachineActivity.class, true);
								}
								
								if(null!=mGuideDialog)
								{
									mGuideDialog.cleanup();
									if(mGuideDialog.isShowing())
										mGuideDialog.dismiss();
									mGuideDialog = null;
								}
							}
						}, UserGuideDialog.LAYOUT_USER_CENTER);
				
				mGuideDialog.show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_main_user_point:
			Bundle point = new Bundle();
			point.putInt(MyPointsActivity.TYPE, MyPointsActivity.MY_POINTS);
			ToolUtil.startActivity(this, MyPointsActivity.class, point);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MyPointsActivity.class.getName(), getString(R.string.tag_MyPointsActivity), "02011");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22001");
			break;
		case R.id.my_main_user_cash_point:
			Bundle balance = new Bundle();
			balance.putInt(MyPointsActivity.TYPE, MyPointsActivity.MY_BALANCE);
			ToolUtil.startActivity(this, MyPointsActivity.class, balance);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MyPointsActivity.class.getName(), getString(R.string.tag_MyPointsActivity), "02012");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22002");
			break;
			
		case R.id.my_icson_coupon:
			ToolUtil.startActivity(this, MyCouponActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MyCouponActivity.class.getName(), getString(R.string.tag_MyCouponActivity), "02013");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22005");
			break;
		case R.id.my_icson_favor:
			ToolUtil.startActivity(this, MyCollectActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MyCollectActivity.class.getName(), getString(R.string.tag_MyCollectActivity), "02014");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22003");
			break;
		case R.id.my_icson_address:
			ToolUtil.startActivity(this, MyAddressActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MyAddressActivity.class.getName(), getString(R.string.tag_MyAddressActivity), "02015");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22004");
			break;
		case R.id.my_icson_history:
			ToolUtil.startActivity(this, PostSaleCenterActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, PostSaleCenterActivity.class.getName(), getString(R.string.tag_PostSaleCenterActivity), "02016");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22006");
			break;
		case R.id.my_orderlist_settings:
			ToolUtil.startActivity(this, MoreActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, MoreActivity.class.getName(), getString(R.string.tag_MyCouponActivity), "02017");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		if(view == mNoneOrderView && null!=mNoneOrderView)
		{
			return;
		}
		position = position - 1;
		if(position == MyOrderListAdapter.ITEM_VP_ORDER_INDEX) {
			ToolUtil.startActivity(this, VPOrderListActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), mPageId, VPOrderListActivity.class.getName(), getString(R.string.tag_MyCouponActivity), "02018");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22007");
			return;
		}
		if (position > -1 && position < mMyOrderListAdapter.getCount()) {
			OrderModel mOrderModel = (OrderModel) mMyOrderListAdapter.getItem(position); //从Adapter里获取而不是从list里面获取，因为adapter里面多加了第一行来跳转到虚拟订单
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
	public void onDialogClick(UserGuideDialog dialog, int nButtonId) 
	{
		// TODO Auto-generated method stub
		if (dialog.mGuideLayout == UserGuideDialog.LAYOUT_USER_CENTER2) 
		{
			if (UserGuideDialog.BUTTON_POSITIVE == nButtonId) 
			{
				
				final Ajax ajax = ServiceConfig.getAjax(Config.URL_GUIDE_GETCOUPON);
				if( null == ajax )
					return ;
				showProgressLayer();
				ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
					@Override
					public void onSuccess(JSONObject v, Response response) {
						closeProgressLayer();
						final int errno = v.optInt("errno", -1);
						if (errno != 0) {
							String strMessage = v.optString("data", "领券失败，请稍后再试");
							String strTitle = getString(R.string.network_error);
							UiUtils.showDialog(MyIcsonActivity.this, strTitle, strMessage, R.string.btn_retry, R.string.btn_cancel, new AppDialog.OnClickListener() {
								@Override
								public void onDialogClick(int nButtonId) {
									if (nButtonId == AppDialog.BUTTON_POSITIVE) {
										ajax.send();
									}
								}
								});
						} 
						else
						{
							if(null!=mGuideDialog)
							{
								mGuideDialog.cleanup();
								mGuideDialog = null;
							}
							mGuideDialog = new UserGuideDialog(
									MyIcsonActivity.this, new UserGuideDialog.OnClickListener() {

										@Override
										public void onDialogClick(UserGuideDialog dialog1, int nButtonId1) {
											if (UserGuideDialog.BUTTON_POSITIVE == nButtonId1) {
												UiUtils.startActivity(MyIcsonActivity.this, SlotMachineActivity.class, true);
											}
											
											if(null!=mGuideDialog)
											{
												mGuideDialog.cleanup();
												if(mGuideDialog.isShowing())
													mGuideDialog.dismiss();
												mGuideDialog = null;
											}
										}
									}, UserGuideDialog.LAYOUT_USER_CENTER3);
							
							mGuideDialog.show();
						}
					}
				});
				ajax.setOnErrorListener(this);
				addAjax(ajax);
				ajax.send();
			}
			
			//clean
			if(null!=mGuideDialog)
			{
				mGuideDialog.cleanup();
				mGuideDialog = null;
			}
		}

	}

	/*
	public void onOrderDetailActivityClosed(Intent intent) {
		Serializable serial = intent.getSerializableExtra(OrderDetailActivity.RESULT_ORDER_MODEL);
		if (serial != null && mOrderModelList != null) {
			OrderModel orderModel = (OrderModel) serial;
			for (int i = 0, len = mOrderModelList.size(); i < len; i++) {
				if (mOrderModelList.get(i).getOrderCharId().equals(orderModel.getOrderCharId())) {
					mOrderModelList.remove(i);
					mOrderModelList.add(i, orderModel);
					break;
				}
			}
			
			if (mMyOrderListAdapter != null) {
				mMyOrderListAdapter.notifyDataSetChanged();
			}

		}
	}*/

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MyIcsonActivity);
	}
}
