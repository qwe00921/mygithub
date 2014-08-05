package com.icson.filter;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IPageCache;
import com.icson.lib.SearchHelper;
import com.icson.lib.model.SearchCategoryModel;
import com.icson.lib.model.SearchFilterAttributeModel;
import com.icson.lib.model.SearchFilterOptionModel;
import com.icson.lib.model.SearchModel;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class FilterActivity extends BaseActivity implements OnSuccessListener<ArrayList<SearchFilterAttributeModel>>, OnClickListener{
	public static final String REQUEST_SEARCH_MODEL = "search_model";
	public static final String RESULT_SEARCH_MODEL = "search_model";
	public static final String REQUEST_SEARCH_CATEGORY_MODEL = "search_category_model";
	private final static int REQUEST_FLAG_CATEGORY_FILTER = 1;

	private static final String LOG_TAG = FilterActivity.class.getName();
	public static final int FLAG_RESULT_OK = 1;
	public int total_num = 0;
	
	private SearchModel mSearchModel;
	private ArrayList<SearchFilterAttributeModel> mSearchFilterAttributeModels;
	private FilterAdapter mFilterAdapter;
	private ExpandableListView mExpandableListView;
	private Button mButtonConfirm;
	private Button mButtonClear;
	private View mHeaderView;
	private ImageView mHasGoodView;
	private RelativeLayout selectedViewContainer;

	private TextView selectedCategoryView;

	private SearchFilterParser mSearchFilterParser;
	private ArrayList<SearchCategoryModel> mSearchCategoryModels;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_filter);

		Intent intent = getIntent();
		if (intent.getSerializableExtra(REQUEST_SEARCH_MODEL) == null) {
			UiUtils.makeToast(this, R.string.params_empty,true);
			finish();
			return;
		}
		
		mSearchModel = (SearchModel) intent.getSerializableExtra(REQUEST_SEARCH_MODEL);
		if (null != intent.getSerializableExtra(REQUEST_SEARCH_CATEGORY_MODEL)) {
			mSearchCategoryModels = (ArrayList<SearchCategoryModel>) intent.getSerializableExtra(REQUEST_SEARCH_CATEGORY_MODEL);
		} 
		total_num = intent.getIntExtra(FilterCategoryActivity.TOTAL_COUNT, -1);
		
		mExpandableListView = (ExpandableListView)findViewById(R.id.filter_expandablelistview);
		mButtonConfirm = (Button) findViewById(R.id.filter_button_confirm);
		mButtonClear = (Button) findViewById(R.id.filter_button_clear);
		
		String strTitle = (null == mSearchModel.getCategoryName() || mSearchModel.getCategoryName().equals("")) ? "筛选" : mSearchModel.getCategoryName();
		loadNavBar(R.id.filter_navigation_bar, strTitle);
		
		mButtonConfirm.setOnClickListener(this);
		mButtonClear.setOnClickListener(this);
		mSearchFilterParser = new SearchFilterParser();
		mExpandableListView.setOnChildClickListener(new OnChildClickListener(){
			@Override
			public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
				SearchFilterOptionModel mSearchFilterOptionModel = (SearchFilterOptionModel) mFilterAdapter.getChild(groupPosition, childPosition);
				CheckBox box = (CheckBox) view.findViewById(R.id.filteroption_checkbox);
				boolean checked = box.isChecked();
				box.setChecked(!checked);
				mSearchFilterOptionModel.setSelect(!checked);
				
				mFilterAdapter.notifyDataSetChanged();
				return false;
			}
		});
		
		mExpandableListView.setOnGroupExpandListener(new OnGroupExpandListener(){
			@Override
			public void onGroupExpand(int currentGroupId) {
				for(int nId=0; nId<mFilterAdapter.getGroupCount(); nId++){
					if (nId != currentGroupId){
						mExpandableListView.collapseGroup(nId);
					}
				}
			}
		});
		
		mHeaderView =  LayoutInflater.from(this).inflate(R.layout.adapter_filter_attr_header, null);
		selectedViewContainer = (RelativeLayout)mHeaderView.findViewById(R.id.filter_select_class);
		mHasGoodView = (ImageView) mHeaderView.findViewById(R.id.filter_sale_status);
		selectedCategoryView = (TextView) mHeaderView.findViewById(R.id.filter_category_selected);
		initData();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		String strPageId = getString(R.string.tag_FilterActivity);

		switch (id) {
		case R.id.filter_button_confirm:
			if( null != mSearchModel ) {
				mSearchModel.setOption(null);
				if( (null != mSearchFilterAttributeModels) && (mSearchFilterAttributeModels.size() > 0))
				{
					for (SearchFilterAttributeModel mSearchFilterAttributeModel : mSearchFilterAttributeModels) {
						for (SearchFilterOptionModel mSearchFilterOptionModel : mSearchFilterAttributeModel.getSearchFilterOptionModels()) {
							if (mSearchFilterOptionModel.isSelect()) {
								mSearchModel.setOption(mSearchFilterAttributeModel.getId(), mSearchFilterOptionModel.getId());
							}
						}
					}
				}
				Intent intent = getIntent();
				Bundle param = new Bundle();
				param.putSerializable(RESULT_SEARCH_MODEL, mSearchModel);
				intent.putExtras(param);
				setResult(FLAG_RESULT_OK, intent);
				finish();
				
				ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02011");
			}
			break;

		case R.id.filter_button_clear:
			if (mSearchFilterAttributeModels != null) {
				for (SearchFilterAttributeModel mSearchFilterAttributeModel : mSearchFilterAttributeModels) {
					if (mSearchFilterAttributeModel != null && mSearchFilterAttributeModel.getSearchFilterOptionModels() != null) {
						for (SearchFilterOptionModel mSearchFilterOptionModel : mSearchFilterAttributeModel.getSearchFilterOptionModels()) {
							if (mSearchFilterOptionModel != null) {
								mSearchFilterOptionModel.setSelect(false);
							}
						}
					}
				}
			}
			if (mFilterAdapter != null) {
				mFilterAdapter.notifyDataSetInvalidated();
			}
			
			mSearchModel.setHasGood(SearchModel.SORT_HASGOOD_OFF);
			refreshHasGoodView();
			
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02012");
			break;
			
		case R.id.filter_select_class:
			Bundle param = new Bundle();
			param.putSerializable(FilterCategoryActivity.REQUEST_SEARCH_CATEGORY_MODEL, mSearchCategoryModels);
			param.putSerializable(FilterCategoryActivity.REQUEST_SEARCH_MODEL, mSearchModel);
			param.putInt(FilterCategoryActivity.TOTAL_COUNT, total_num);
			ToolUtil.startActivity(FilterActivity.this, FilterCategoryActivity.class, param,REQUEST_FLAG_CATEGORY_FILTER);
			break;
		case R.id.filter_sale_status:
			int hasGood = mSearchModel.getHasGood() == 0? 1:0;
			mSearchModel.setHasGood(hasGood);
			refreshHasGoodView();
			break;
		}
		
	}

	private String getCacheKey() {
		return mSearchModel == null ? "" : ("category_filter_" + mSearchModel.getPath() + (mSearchModel.getKeyWord() == null ? "" : ("-" + mSearchModel.getKeyWord())));
	}

	private void initData() {
		IPageCache cache = new IPageCache();
		String data = cache.get(getCacheKey());

		if (data != null) {
			ArrayList<SearchFilterAttributeModel> models;
			try {
				models = mSearchFilterParser.parse(data);
			} catch (Exception ex) {
				Log.e(LOG_TAG, ex);
				models = null;
			}

			if (models != null) {
				requestFinish(models);
				return;
			}
		}

		showLoadingLayer();
		String option = mSearchModel.getOption();
		mSearchModel.setOption(null);
		
		String strUrl = SearchHelper.getSearchUrlParamter(mSearchModel);
		strUrl += "&dtype=attr";
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_SEARCH_NEW, strUrl);
		if( null == ajax )
			return ;
		mSearchModel.setOption(option);
		ajax.setParser(mSearchFilterParser);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				FilterActivity.this.onError(ajax, response);
			}
		});
		ajax.send();
		addAjax(ajax);
	}

	@Override
	public void onSuccess(ArrayList<SearchFilterAttributeModel> data, Response response) {
		if (!mSearchFilterParser.isSuccess()) {
			closeLoadingLayer(true);
			UiUtils.makeToast(this,  TextUtils.isEmpty(mSearchFilterParser.getErrMsg()) ? Config.NORMAL_ERROR: mSearchFilterParser.getErrMsg());
			return;
		}

		IPageCache cache = new IPageCache();
		cache.set(getCacheKey(), mSearchFilterParser.getString(), 3600 * 24);
		requestFinish(data);
	}

	public void requestFinish(ArrayList<SearchFilterAttributeModel> data) {
		if(null==mSearchFilterAttributeModels)
			mSearchFilterAttributeModels = new ArrayList<SearchFilterAttributeModel>();
		mSearchFilterAttributeModels.clear();
		mSearchFilterAttributeModels.addAll(data);
		
		closeLoadingLayer();
		render();
	}

	private void render() {
		
		if(mHeaderView != null )
		{
			if(mExpandableListView.getHeaderViewsCount() <= 0) {
				mHeaderView.findViewById(R.id.filter_select_class).setOnClickListener(this);
				mHasGoodView.setOnClickListener(this);
				mExpandableListView.addHeaderView(mHeaderView);
				selectedViewContainer.setVisibility(null == mSearchCategoryModels ? View.GONE: View.VISIBLE);
				mHeaderView.findViewById(R.id.filter_select_topline).setVisibility(selectedViewContainer.getVisibility());
			}
		}
		if(null != selectedCategoryView )
		{
			if(!TextUtils.isEmpty(mSearchModel.getCategoryName()))
			{
				selectedCategoryView.setText(mSearchModel.getCategoryName());
			}
		}
		
		refreshHasGoodView();
		
		if (mSearchFilterAttributeModels.size() == 0 && null == mSearchCategoryModels && total_num <= 0) {
			findViewById(R.id.filter_relative_empty).setVisibility(View.VISIBLE);
			findViewById(R.id.global_container).setVisibility(View.GONE);
			return;
		}

		for (SearchFilterAttributeModel mSearchFilterAttributeModel : mSearchFilterAttributeModels) {
			String[] options = mSearchModel.getOptions(mSearchFilterAttributeModel.getId());

			for (SearchFilterOptionModel mSearchFilterOptionModel : mSearchFilterAttributeModel.getSearchFilterOptionModels()) {
				if (options == null || options.length == 0) {
					mSearchFilterOptionModel.setSelect(false);
				} else {
					boolean found = false;
					for (String option : options) {
						if (mSearchFilterOptionModel.getId() == Integer.valueOf(option)) {
							found = true;
							mSearchFilterOptionModel.setSelect(true);
						}
					}

					if (!found) {
						mSearchFilterOptionModel.setSelect(false);
					}
				}
			}
		}

		if(null == mFilterAdapter) {
			mFilterAdapter = new FilterAdapter(this, mSearchFilterAttributeModels);
			mExpandableListView.setAdapter(mFilterAdapter);
		}else {
			mFilterAdapter.notifyDataSetChanged();
			
			//默认刷新数据时将ListView默认收缩
			 int groupCount = mExpandableListView.getCount();   
			 for (int i=0; i < groupCount; i++)  
			  {   
				 mExpandableListView.collapseGroup(i); 
			  };   
		}
	}
	
	private void refreshHasGoodView() {
		// TODO Auto-generated method stub
		if(0 == mSearchModel.getHasGood())
		{
			mHasGoodView.setImageResource(R.drawable.filter_sale_off);
			mSearchModel.setHasGood(SearchModel.SORT_HASGOOD_OFF);
		}
		else
		{
			mHasGoodView.setImageResource(R.drawable.filter_sale_on);
			mSearchModel.setHasGood(SearchModel.SORT_HASGOOD_ON);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case REQUEST_FLAG_CATEGORY_FILTER:
			if (resultCode == FilterActivity.FLAG_RESULT_OK) {
				SearchModel model = (SearchModel) intent.getSerializableExtra(FilterActivity.RESULT_SEARCH_MODEL);
				if ( !SearchHelper.getSearchUrlParamter(mSearchModel).equals(SearchHelper.getSearchUrlParamter(model))) {
					mSearchModel = model;
					mSearchFilterAttributeModels.clear();
					if(mFilterAdapter != null) {
						mFilterAdapter.notifyDataSetChanged();
					}
//					if(mExpandableListView.getHeaderViewsCount() > 0)
//						mExpandableListView.removeHeaderView(mHeaderView);
					initData();	
				}
				else if(!TextUtils.isEmpty(model.getCategoryName()) && TextUtils.isEmpty(mSearchModel.getCategoryName()))
				{
					selectedCategoryView.setText(model.getCategoryName());
					mSearchModel = model;
				}
			}
			break;
		}
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_FilterActivity);
	}

}
