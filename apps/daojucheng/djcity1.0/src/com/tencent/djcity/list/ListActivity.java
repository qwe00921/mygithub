package com.tencent.djcity.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
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

import com.tencent.djcity.R;
import com.tencent.djcity.category.CategoryActivity;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.RoleInfoView;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.lib.ui.NavigationBar.OnLeftButtonClickListener;
import com.tencent.djcity.search.SearchActivity;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class ListActivity extends BaseActivity implements OnClickListener{

	private ListView mListView;
	private View mFooterView;
	private View sortByNumView;
	private View sortByPrice;
	private View sortByTimeView;
//	private RoleInfoView mRoleInfoView;
	private ListAdapter mAdapter;
	public static final String KEY_BUS_ID = "busid";
	public static final String KEY_CATE_ID = "cateid";
	private String mBusId;
	private String mCateId;
	
	public static final String ORDER_TOTAL_BUY_NUM = "lTotalBuyNum";
	public static final String ORDER_PRICE = "iPrice";
	public static final String ORDER_MODIFY_TIME = "dtModifyTime";
	public static final String ORDER_DESC = "desc";
	public static final String ORDER_ASC = "asc";
	
	private boolean mIsDesc = true;
	private String mOrder = ORDER_MODIFY_TIME;
	
	public static final String KEY_FROM_SEARCH = "from_search";
	public static final String KEY_SEARCH_KWD = "search_keyword";
	
	private String mSearchKey;
	private boolean mFromSearch;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_list);
		Intent intent = getIntent();
		mBusId = intent.getStringExtra(KEY_BUS_ID);
		mCateId = intent.getStringExtra(KEY_CATE_ID);
		mFromSearch = intent.getBooleanExtra(KEY_FROM_SEARCH, false);
		mSearchKey = intent.getStringExtra(KEY_SEARCH_KWD);
		
		sortByNumView = findViewById(R.id.list_sort_buy_num);
		sortByNumView.setOnClickListener(this);
		sortByPrice = findViewById(R.id.list_sort_price);
		sortByPrice.setOnClickListener(this);
		sortByTimeView = findViewById(R.id.list_sort_time);
		sortByTimeView.setOnClickListener(this);
		
		mListView = (ListView) findViewById(R.id.list_listview);
		
		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mListView.addFooterView(mFooterView);
		
		mAdapter = new ListAdapter(this, mProductList);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if(scrollState == OnScrollListener.SCROLL_STATE_FLING || scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//					mMyOrderListAdapter.setIsScrolling(true);
				} else {
//					mMyOrderListAdapter.setIsScrolling(false);
				}
				
				
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
						(!loadedDone)  && (view.getLastVisiblePosition() >= view.getCount() - 1) )
				{
					requestData();
				}
			}		
			});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ListActivity.this, ItemActivity.class);
				
				intent.putExtra(ItemActivity.KEY_PROP_ID, String.valueOf(id));
				startActivity(intent);
			}
		});
		
		mNavBar = (NavigationBar) findViewById(R.id.category_navbar);
		
		mNavBar.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
			
			@Override
			public void onClick() {
				onBackPressed();
			}
		});
		mNavBar.setOnIndicatorClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToolUtil.startActivity(ListActivity.this,
						SearchActivity.class);
			}
		});
//		mRoleInfoView = (RoleInfoView) findViewById(R.id.category_role_info_layout);
		
		findViewById(R.id.list_button_redirect).setOnClickListener(this);
		requestData();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
//		mRoleInfoView.refreshInfo();
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.list_sort_buy_num:
			reOrder(ORDER_TOTAL_BUY_NUM);
			break;
		case R.id.list_sort_price:
			reOrder(ORDER_PRICE);
			break;
		case R.id.list_sort_time:
			reOrder(ORDER_MODIFY_TIME);
			break;
			
		case R.id.list_head_back_view:
			this.processBack();
			return;
		case R.id.list_button_redirect:
			this.processBack();
			break;
		}

	}
	
	private void reOrder(String order) {
		
		if(order.equals(mOrder)) {
			mIsDesc = !mIsDesc;
		}
		mOrder = order;
		
		mPage = 1;
		loadedDone = false;
		mProductList.clear();
		
		if(mListView.getFooterViewsCount() == 0) {
			mListView.addFooterView(mFooterView);
		}
		mAdapter.notifyDataSetChanged();
		requestData();
	}
	
	private void renderSortButtons() {
		
		ViewGroup sortByNumViewTab = (ViewGroup) sortByNumView;
		ViewGroup sortByPriceViewTab = (ViewGroup) sortByPrice;
		ViewGroup sortByTimeViewTab = (ViewGroup) sortByTimeView;
		
		((ImageView) sortByNumViewTab.getChildAt(1)).setImageResource( 0 );
		((TextView) (sortByNumViewTab.getChildAt(0))).setTextColor(getResources().getColor(R.color.search_list_tab));
		
		((ImageView) sortByPriceViewTab.getChildAt(1)).setImageResource( 0 );
		((TextView) (sortByPriceViewTab.getChildAt(0))).setTextColor(getResources().getColor(R.color.search_list_tab));
		
		((ImageView) sortByTimeViewTab.getChildAt(1)).setImageResource( 0 );
		((TextView) (sortByTimeViewTab.getChildAt(0))).setTextColor(getResources().getColor(R.color.search_list_tab));
		
		int drawableRes = mIsDesc ? R.drawable.i_price_down_s : R.drawable.i_price_up_s;
		int colorRes = R.color.search_list_tab_s;
		
		if(ORDER_MODIFY_TIME.equals(mOrder)) {
			((ImageView) sortByTimeViewTab.getChildAt(1)).setImageResource(drawableRes);
			((TextView) sortByTimeViewTab.getChildAt(0)).setTextColor(getResources().getColor(colorRes));
		} else if(ORDER_PRICE.equals(mOrder)) {
			((ImageView) sortByPriceViewTab.getChildAt(1)).setImageResource(drawableRes);
			((TextView) sortByPriceViewTab.getChildAt(0)).setTextColor(getResources().getColor(colorRes));
		} else if(ORDER_TOTAL_BUY_NUM.equals(mOrder)) {
			((ImageView) sortByNumViewTab.getChildAt(1)).setImageResource(drawableRes);
			((TextView) sortByNumViewTab.getChildAt(0)).setTextColor(getResources().getColor(colorRes));
		}

		
//		sortByNumViewTab.setBackgroundResource(resid);
//		
//		tab = (ViewGroup) sortByPrice;
//		
//		tab.setBackgroundResource(sort == SearchModel.SORT_DEFAULT ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
//		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_DEFAULT ? R.color.search_list_tab_s : R.color.search_list_tab));
//		
//		tab.setBackgroundResource((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
//		((ImageView) tab.getChildAt(1)).setImageResource((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? (sort == SearchModel.SORT_PRICE_ASC ? R.drawable.i_price_down_s : R.drawable.i_price_up_s) : R.drawable.i_price);
//		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? R.color.search_list_tab_s : R.color.search_list_tab));
//
//		tab = (ViewGroup) sortByTimeView;
//		tab.setBackgroundResource(sort == SearchModel.SORT_SALE_DEC ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
//		((ImageView) tab.getChildAt(1)).setImageResource(sort == SearchModel.SORT_SALE_DEC ? R.drawable.i_sale_down_s  : R.drawable.i_sale_down);
//		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_SALE_DEC ? R.color.search_list_tab_s : R.color.search_list_tab));
//
//		tab = (ViewGroup) reviewView;
//		tab.setBackgroundResource(sort == SearchModel.SORT_REVIEW ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
//		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_REVIEW ? R.color.search_list_tab_s : R.color.search_list_tab));
	}
	
	private List<ProductModel> mProductList = new ArrayList<ProductModel>();
	private int mPage = 1;
	private boolean loadedDone;
	
	private Ajax getSearchAjax() {
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/SearchGoods.php?plat=2");
		
		if(ajax == null) {
			return null;
		}
		
		ajax.setData("orderby", mOrder);
		ajax.setData("busid", mBusId);
		ajax.setData("page", mPage);
		if(mSearchKey != null) {
			ajax.setData("keywords", mSearchKey);
		}
		
		if(mIsDesc) {
			ajax.setData("ordertype", ORDER_DESC);
		} else {

			ajax.setData("ordertype", ORDER_ASC);
		}
		
		return ajax;
	}
	
	private Ajax getItemListAjax() {
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/GoodsList.php?plat=2");
		
		if(ajax == null) {
			return null;
		}
		
		ajax.setData("orderby", mOrder);
		ajax.setData("busid", mBusId);
		ajax.setData("page", mPage);
		if(mCateId != null) {
			ajax.setData("cateid", mCateId);
		}
		if(mIsDesc) {
			ajax.setData("ordertype", ORDER_DESC);
		} else {
			ajax.setData("ordertype", ORDER_ASC);
		}
		return ajax;
	}
	private void requestData() {
		showEmpty(false);
		renderSortButtons();
		showLoadingLayer();
		Ajax ajax = null;
		
		if(mFromSearch) {
			ajax = getSearchAjax();
		} else {
			ajax = getItemListAjax();
		}
		
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			public void onSuccess(JSONObject v, Response response) {
				closeLoadingLayer();

				String msg = v.optString("msg");
				int result = v.optInt("result");
				if(result != 0) {
					UiUtils.makeToast(ListActivity.this, msg);
					return;
				}
				JSONObject data = v.optJSONObject("data");
				if(data != null) {
					JSONArray array = data.optJSONArray("goods");
					if(array == null) {
						return;
					}
					for(int i = 0; i < array.length(); i++) {
						JSONObject product = array.optJSONObject(i);
						if(product != null) {
							ProductModel productModel = ProductModel.fromJson(product);
							mProductList.add(productModel);
						}
					}
					int totalPage = data.optInt("totalPage");
					if(mPage < totalPage) {
						mPage++;
					} else {
						loadedDone = true;
						mListView.removeFooterView(mFooterView);
					}
				}
				mAdapter.notifyDataSetChanged();
				
				if(mProductList.size() == 0) {
					showEmpty(true);
				}
				
			};
		});
		ajax.setParser(new JSONParser());
		
		ajax.send();
		addAjax(ajax);
	}
	
	private void showEmpty(boolean show) {
		if(show) {
			findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {

			findViewById(R.id.list_relative_empty).setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
}
