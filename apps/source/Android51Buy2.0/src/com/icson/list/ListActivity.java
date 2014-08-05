package com.icson.list;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.filter.FilterActivity;
import com.icson.filter.FilterCategoryActivity;
import com.icson.item.ItemActivity;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.SearchHelper;
import com.icson.lib.model.PageModel;
import com.icson.lib.model.SearchCategoryModel;
import com.icson.lib.model.SearchModel;
import com.icson.lib.model.SearchProductListModel;
import com.icson.lib.model.SearchProductModel;
import com.icson.lib.parser.SearchListParser;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.search.SearchActivity;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnFinishListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ListActivity extends BaseActivity implements OnFinishListener, OnItemClickListener, OnScrollListener, OnSuccessListener<SearchProductListModel>, OnClickListener {
	public static final String REQUEST_SEARCH_MODEL = "search_model";
	public static final String REQUEST_SEARCH_URL = "search_url";
	public static final String REQUEST_PAGE_TITLE = "page_title";
	public static final String REQUEST_SEARCH_CATE = "search_category";
	public static final String REQUEST_SEARCH_FROM_WX = "search_from_wx";

	private final static int REQUEST_FLAG_SEARCH_FILTER = 1;

	private ListView mListView;
	private Button mBackWx;
	private ListAdapter adapter;
	private Ajax mAjax;
	private SearchModel mSearchModel;
	private View defaultView;
	private View priceView;
	private View saleView;
	private View reviewView;
	private SearchProductListModel mSearchProductListModel = new SearchProductListModel();
	private ArrayList<SearchCategoryModel> mSearchCategoryModels;
	private View mFooterView;
	private int mInitCurrentPage;
	private TextView mFilterButton;
	private RelativeLayout mBackImage;
	private SearchListParser mParser;
	private EditText mHeadTitle;
	private String mPageTitle;
	private boolean bFromSearch;
	private boolean bFromSearch_Category;
	private boolean mSearchFromWx;

	public int total_num = 0;

	//private ImageView mFirstSight1;
	//private ImageView mFirstSight2;
	//private OnTouchListener   mFirstListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		Intent intent = getIntent();
		if (intent.getSerializableExtra(REQUEST_SEARCH_MODEL) != null) {
			mSearchModel = (SearchModel) intent.getSerializableExtra(REQUEST_SEARCH_MODEL);
		} else if (intent.getStringExtra(REQUEST_SEARCH_URL) != null) {
			mSearchModel = SearchHelper.getSearchModel(intent.getStringExtra(REQUEST_SEARCH_URL));
		}
		bFromSearch = intent.getBooleanExtra("from_search", false);
		bFromSearch_Category = intent.getBooleanExtra(REQUEST_SEARCH_CATE, false);
		mSearchFromWx = intent.getBooleanExtra(REQUEST_SEARCH_FROM_WX, false);
		
		if (mSearchModel == null) {
			UiUtils.makeToast(this, R.string.params_empty,true);
			finish();
			return;
		}
		
		mBackImage = (RelativeLayout)findViewById(R.id.list_head_back_view);
		if( null != mBackImage ) {
			mBackImage.setOnClickListener(this);
		}

		mInitCurrentPage = mSearchModel.getCurrentPage();
		mPageTitle = intent.getStringExtra(REQUEST_PAGE_TITLE);
		mPageTitle = (null == mPageTitle) ? "搜索结果" : mPageTitle;
		
		mHeadTitle = (EditText) findViewById(R.id.list_editview_head_title);
		mHeadTitle.setKeyListener(null);
		mHeadTitle.setOnClickListener(this);
		mHeadTitle.setText(mPageTitle);
		mHeadTitle.setFocusable(false);
		
		findViewById(R.id.list_button_redirect).setOnClickListener(this);

		mFilterButton = ((TextView) findViewById(R.id.list_button_right));
		mFilterButton.setOnClickListener(this);
		mFilterButton.setText("筛选");

		if (!TextUtils.isEmpty(mSearchModel.getPath())) {
			mFilterButton.setVisibility(View.VISIBLE);
		}

		mParser = new SearchListParser();
		init();
	}

	/*
	private void checkFirstSight()
	{
		int versionCode = Preference.getInstance().getFirstSightVersion(Preference.FIRST_SIGHT_FILTER);
		if(versionCode < IcsonApplication.mVersionCode)
		{
			mFirstSight1 = (ImageView) this.findViewById(R.id.first_sight_head);
			mFirstSight1.setVisibility(View.VISIBLE);
			mFirstSight2 = (ImageView) this.findViewById(R.id.first_sight_tail);
			mFirstSight2.setVisibility(View.VISIBLE);
			
			mFirstListener = new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(mFirstSight1.getVisibility() == View.VISIBLE &&
							event.getAction() == MotionEvent.ACTION_DOWN )
					{
						goneFirstSight();
						
						return true;
					}
					return false;
				}};
				
			mFirstSight1.setOnTouchListener(mFirstListener);
			mFirstSight2.setOnTouchListener(mFirstListener);
			
			if(mFirstSight1.getVisibility() == View.VISIBLE)
			{
				Handler FadingHandler = new Handler();
				FadingHandler.postDelayed(new Runnable(){

					@Override
					public void run() {
						goneFirstSight();
						
					}}, Config.FIRST_SIGHT_FADING_TIME);
			}
		}
	}
	
	protected void goneFirstSight() {
		mFirstSight1.setVisibility(View.GONE);
		mFirstSight2.setVisibility(View.GONE);
		Preference.getInstance().setFirstSightVersion(Preference.FIRST_SIGHT_FILTER,IcsonApplication.mVersionCode);
		Preference.getInstance().savePreference();
		mFirstListener = null;
	}
*/
	public void init() {
		defaultView = findViewById(R.id.list_sort_default);
		defaultView.setOnClickListener(this);
		priceView = findViewById(R.id.list_sort_price);
		priceView.setOnClickListener(this);

		saleView = findViewById(R.id.list_sort_sale);
		saleView.setOnClickListener(this);

		reviewView = findViewById(R.id.list_sort_review);
		reviewView.setOnClickListener(this);
		
		adapter = new ListAdapter(this, mSearchProductListModel.getSearchProductModels());

		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.global_listview_loading, null);
		mListView = (ListView) findViewById(R.id.list_listview);
		mListView.setFooterDividersEnabled(false);
		mListView.addFooterView(mFooterView);
		mListView.setAdapter(adapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		
		mBackWx = (Button) findViewById(R.id.btn_back_wx);
		if(mSearchFromWx) {
			mBackWx.setVisibility(View.VISIBLE);
		} else {
			mBackWx.setVisibility(View.GONE);
		}
		
		mBackWx.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//IWXAPI pWechatApi = WXAPIFactory.createWXAPI(ListActivity.this, Config.APP_ID);
				//pWechatApi.openWXApp();
				
//				Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
////			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
////				intent.setAction(Intent.ACTION_MAIN);
////				intent.addCategory(Intent.CATEGORY_LAUNCHER);
//			    startActivity(intent);
				finish();
				MainActivity.exitApp(ListActivity.this);
			}
		});
		

		switch (mSearchModel.getSort()) {
		case SearchModel.SORT_PRICE_DEC:
		case SearchModel.SORT_PRICE_ASC:
			sortByPrice();
			break;
		case SearchModel.SORT_SALE_DEC:
			sortBySale();
			break;
		case SearchModel.SORT_REVIEW:
			sortByReview();
			break;
		default:
			sortByDefault();
			break;
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if(mBackWx != null) {
			mBackWx.setVisibility(View.GONE);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && mAjax == null && (view.getLastVisiblePosition() >= view.getCount() - 2) && mSearchModel.getCurrentPage() <= mSearchProductListModel.getPageModel().getPageCount()) {
			sendRequest();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.list_sort_default:
			sortByDefault();
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02014");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22001");
			break;
			
		case R.id.list_sort_price:
			sortByPrice();
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02011");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22002");
			break;
		case R.id.list_sort_sale:
			if (mSearchModel.getSort() != SearchModel.SORT_SALE_DEC) {
				sortBySale();
			}
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02012");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22003");
			break;
		case R.id.list_sort_review:
			if (mSearchModel.getSort() != SearchModel.SORT_REVIEW) {
				sortByReview();
			}
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02013");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22004");
			break;
		// 筛选
		case R.id.list_button_right:
			filterButtonAction();
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21002");
			break;
		case R.id.list_button_redirect:
			boolean haveFilter = mSearchModel.getOption() != null && !mSearchModel.getOption().equals("");
			// 重新筛选
			if (haveFilter) {
				onClick(mFilterButton);
				return;
			}

			// 重新搜索
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), SearchActivity.class.getName(), getString(R.string.tag_SearchActivity), "03011");
			if(bFromSearch)
			{
				this.processBack();
			}
			else
			{
				finish();
				Intent intent = new Intent(this, SearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				Bundle abund = null;
				if(!TextUtils.isEmpty(mPageTitle))
				{
					abund = new Bundle();
					abund.putString("key_word", mPageTitle);
				}
				ToolUtil.startActivity(this, intent, abund, -1);
			}
			break;
			
		case R.id.list_editview_head_title:
			ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), SearchActivity.class.getName(), getString(R.string.tag_SearchActivity), "03012");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			if(bFromSearch)
			{
				this.processBack();
			}
			else
			{
				Intent intent = new Intent(this, SearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				Bundle abund = null;
				if(!TextUtils.isEmpty(mPageTitle))
				{
					abund = new Bundle();
//					abund.putString("key_word", mPageTitle);
				}
				ToolUtil.startActivity(this, intent, abund, -1);
			}
			break;
			
		case R.id.list_head_back_view:
			this.processBack();
			break;
		}
	}
	
	/*
	 * 
	 */
	private void filterButtonAction(){
		
		Bundle param = new Bundle();
		param.putSerializable(FilterActivity.REQUEST_SEARCH_MODEL, mSearchModel);
		param.putInt(FilterCategoryActivity.TOTAL_COUNT, total_num);

		if(null != mSearchCategoryModels)
		{
			param.putSerializable(FilterCategoryActivity.REQUEST_SEARCH_CATEGORY_MODEL, mSearchCategoryModels);
		}
		ToolUtil.startActivity(ListActivity.this, FilterActivity.class, param, REQUEST_FLAG_SEARCH_FILTER);
		ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), FilterActivity.class.getName(), getString(R.string.tag_FilterActivity), "01011");
		
	}
	

	private void renderSortButtons() {
		final int sort = mSearchModel.getSort();

		ViewGroup tab = (ViewGroup) defaultView;
		tab.setBackgroundResource(sort == SearchModel.SORT_DEFAULT ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_DEFAULT ? R.color.search_list_tab_s : R.color.search_list_tab));
		
		tab = (ViewGroup) priceView;
		tab.setBackgroundResource((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
		((ImageView) tab.getChildAt(1)).setImageResource((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? (sort == SearchModel.SORT_PRICE_ASC ? R.drawable.i_price_down_s : R.drawable.i_price_up_s) : R.drawable.i_price);
		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor((sort == SearchModel.SORT_PRICE_ASC || sort == SearchModel.SORT_PRICE_DEC) ? R.color.search_list_tab_s : R.color.search_list_tab));

		tab = (ViewGroup) saleView;
		tab.setBackgroundResource(sort == SearchModel.SORT_SALE_DEC ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
		((ImageView) tab.getChildAt(1)).setImageResource(sort == SearchModel.SORT_SALE_DEC ? R.drawable.i_sale_down_s  : R.drawable.i_sale_down);
		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_SALE_DEC ? R.color.search_list_tab_s : R.color.search_list_tab));

		tab = (ViewGroup) reviewView;
		tab.setBackgroundResource(sort == SearchModel.SORT_REVIEW ? R.drawable.i_list_activity_tab_button_s: R.drawable.i_list_activity_tab_button);
		((TextView) (tab.getChildAt(0))).setTextColor(getResources().getColor(sort == SearchModel.SORT_REVIEW ? R.color.search_list_tab_s : R.color.search_list_tab));
	}

	public void sortByDefault() {
		mSearchModel.setSort(SearchModel.SORT_DEFAULT);
		renderSortButtons();
		clearListView();
		sendRequest();
	}

	public void sortByPrice() {
		mSearchModel.setSort( (mSearchModel.getSort() == SearchModel.SORT_PRICE_ASC  || mSearchModel.getSort() == SearchModel.SORT_PRICE_DEC ) ? ((mSearchModel.getSort() == SearchModel.SORT_PRICE_ASC ) ? SearchModel.SORT_PRICE_DEC : SearchModel.SORT_PRICE_ASC ): SearchModel.SORT_PRICE_ASC);
		renderSortButtons();
		clearListView();
		sendRequest();
	}

	public void sortBySale() {
		mSearchModel.setSort(SearchModel.SORT_SALE_DEC);
		renderSortButtons();
		clearListView();
		sendRequest();
	}

	public void sortByReview() {
		mSearchModel.setSort(SearchModel.SORT_REVIEW);
		renderSortButtons();
		clearListView();
		sendRequest();
	}

	public void clearListView() {
		setFilterButtonStatus();
		mSearchModel.setCurrentPage(mInitCurrentPage);
		if (null != mAjax) {
			mAjax.abort();
			mAjax = null;
		}
		mSearchProductListModel.getSearchProductModels().clear();
		if (mListView.getFooterViewsCount() == 0) {
			mListView.addFooterView(mFooterView);
		}

		findViewById(R.id.list_relative_empty).setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		adapter.notifyDataSetChanged();
	}

	public void sendRequest() {
		String strUrl = SearchHelper.getSearchUrlParamter(mSearchModel);
		strUrl += "&dtype=list|page|classes";
		
		mAjax = ServiceConfig.getAjax(Config.URL_SEARCH_NEW, strUrl);
		if( null == mAjax )
			return ;
		mAjax.setParser(mParser);
		mAjax.setData("districtId",FullDistrictHelper.getDistrictId());
		mAjax.setCookie("ingore", mSearchModel.getCurrentPage() == mInitCurrentPage ? "0" : "1");

		mAjax.setOnSuccessListener(this);
		mAjax.setOnErrorListener(this);
		mAjax.setOnFinishListener(this);
		mAjax.send();
	}

	@Override
	public void onDestroy() {
		mSearchProductListModel = null;
		if(adapter != null) {
			adapter.notifyDataSetChanged();
		}
		adapter = null;
		if (mAjax != null) {
			mAjax.abort();
			mAjax = null;
		}
		
		if(null!=mSearchCategoryModels)
		{
			mSearchCategoryModels.clear();
			mSearchCategoryModels= null;
		}
		super.onDestroy();
	}

	@Override
	public void onSuccess(SearchProductListModel result, Response reponse) {

		if (!mParser.isSuccess()) {
			UiUtils.makeToast(this, mParser.getErrorMessage());
			SearchProductListModel model = new SearchProductListModel();
			PageModel pageModel = mSearchProductListModel != null ? mSearchProductListModel.getPageModel() : new PageModel();
			pageModel.setPageCount(pageModel.getCurrentPage());
			model.setPageModel(pageModel);
			render(model);
			return;
		}

		mAjax = null;

		render(result);
		
		//checkFirstSight();

	}

	private void render(SearchProductListModel result) {

		if (result.getSearchProductModels() != null) {
			mSearchProductListModel.getSearchProductModels().addAll(result.getSearchProductModels());
		}

		mSearchProductListModel.setPageModel(result.getPageModel());

		if (mSearchProductListModel.getPageModel().getCurrentPage() >= mSearchProductListModel.getPageModel().getPageCount()) {
			mListView.removeFooterView(mFooterView);
			if (mSearchProductListModel.getSearchProductModels().size() == 0) {
				mListView.setVisibility(View.GONE);
				findViewById(R.id.list_relative_empty).setVisibility(View.VISIBLE);
				boolean haveFilter = mSearchModel.getOption() != null && !mSearchModel.getOption().equals("");
				((TextView) findViewById(R.id.list_button_redirect)).setText(haveFilter ? "重新筛选" : "重新搜索");
			}
		}

		mHeadTitle.setText(mPageTitle + "(约" + mSearchProductListModel.getPageModel().getTotal() + "件)");

		if (TextUtils.isEmpty(mSearchModel.getPath()) &&
			TextUtils.isEmpty(mSearchModel.getOption()) &&
			result.getSearchCategoryModels() != null) {
			mSearchCategoryModels = result.getSearchCategoryModels();
			mFilterButton.setVisibility(View.VISIBLE);
			total_num = result.getPageModel().getTotal();
		}
		else if(bFromSearch_Category)
		{
			total_num = result.getPageModel().getTotal();
		}
		mSearchModel.setCurrentPage(mSearchModel.getCurrentPage() + 1);
		adapter.notifyDataSetChanged();

		onScrollStateChanged(mListView, OnScrollListener.SCROLL_STATE_IDLE);
	}

	@Override
	public void onErrorDialogCacneled(Ajax ajax, Response response) {
		mAjax = null;
		SearchProductListModel model = new SearchProductListModel();
		PageModel pageModel = mSearchProductListModel != null ? mSearchProductListModel.getPageModel() : new PageModel();
		pageModel.setPageCount(pageModel.getCurrentPage());
		model.setPageModel(pageModel);
		render(model);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (position > adapter.getCount() - 1)
			return;

		SearchProductModel model = (SearchProductModel) adapter.getItem(position);

		Bundle param = new Bundle();

		param.putLong(ItemActivity.REQUEST_PRODUCT_ID, model.getProductId());

		ToolUtil.startActivity(this, ItemActivity.class, param);

		String locationId = "";
		if(position>9)
			locationId ="030"+position;
		else
			locationId ="0300"+position;
		
		ToolUtil.sendTrack(ListActivity.class.getClass().getName(), getString(R.string.tag_ListActivity), ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), locationId, String.valueOf(model.getProductId()));
		ToolUtil.reportStatisticsClick(getActivityPageId(), ""+(30001+position),String.valueOf(model.getProductId()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case REQUEST_FLAG_SEARCH_FILTER:
			if (resultCode == FilterActivity.FLAG_RESULT_OK) {
				SearchModel model = (SearchModel) intent.getSerializableExtra(FilterActivity.RESULT_SEARCH_MODEL);
				if ( !SearchHelper.getSearchUrlParamter(mSearchModel).equals(SearchHelper.getSearchUrlParamter(model))) {
					mSearchModel = model;
					clearListView();
					sendRequest();
				}
			}
			
			break;
		}
	}

	@Override
	public void onFinish(Response resonse) {
		mAjax = null;
	}

	private void setFilterButtonStatus() {
		Drawable check = null;

		if (mSearchModel.getOption() != null && !mSearchModel.getOption().equals("") ||
			mSearchModel.getHasGood() == SearchModel.SORT_HASGOOD_ON) {
			check = getResources().getDrawable(R.drawable.i_filter_after);
			check.setBounds(0, 0, check.getMinimumWidth(), check.getMinimumHeight());
		}else{
			check = getResources().getDrawable(R.drawable.i_filter_before);
			check.setBounds(0, 0, check.getMinimumWidth(), check.getMinimumHeight());
		}

		mFilterButton.setCompoundDrawables(check, null, null, null);
	}
	
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ListActivity);
	}
}
