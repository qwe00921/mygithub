package com.tencent.djcity.category;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.category.CategoryModel.SubCategoryModel;
import com.tencent.djcity.home.HomeActivity;
import com.tencent.djcity.lib.IPageCache;
import com.tencent.djcity.lib.inc.CacheKeyFactory;
import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.RoleInfoView;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.list.ListActivity;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.search.SearchActivity;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class CategoryActivity extends BaseActivity implements
		OnItemClickListener, OnSuccessListener<ArrayList<CategoryModel>> {

	private final String LOG_TAG = CategoryActivity.class.getName();

	private CategoryAdapter adapter;

	// private Trajectory trajectory;
	private ListView list_view_category;

	private ArrayList<CategoryModel> allCategoryModels;

	private ArrayList<BaseModel> currentModles;
	private CategoryModel currentCategory;
	
	private SubCategoryAdapter subAdapter;
	private ArrayList<SubCategoryModel> subs ;
	
//	private RoleInfoView mRoleInfoView;
	private ListView subListView;
	private ArrayList<BaseModel> nodes = new ArrayList<BaseModel>();

	private CategoryModelParser mCategoryModelParser;
	private String tag1 = "00";
	private String tag2 = "00";
	private String tag3 = "0";
	
	private GameInfo mGameInfo;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);
		
		initUI();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mGameInfo = GameInfo.getGameInfoFromPreference();
		initData();
//		mRoleInfoView.refreshInfo();
	}

	public void initUI() {
		mCategoryModelParser = new CategoryModelParser();
		currentModles = new ArrayList<BaseModel>();
		adapter = new CategoryAdapter(this, currentModles);
		
//		mRoleInfoView = (RoleInfoView) findViewById(R.id.category_role_info_layout);
		mNavBar = (NavigationBar)findViewById(R.id.category_navbar);
		mNavBar.setText(R.string.category);
		mNavBar.setOnLeftButtonClickListener(new NavigationBar.OnLeftButtonClickListener() {
			@Override
			public void onClick() {
				if(currentModles.size() == 0)
				{
					processBack();
//					ToolUtil.reportStatisticsClick(getActivityPageId(), "70000");
					return;
				}
							
				BaseModel model = currentModles.get(0);
				if (model instanceof CategoryModel)
				{
					processBack();
				}
				else if (subListView.getVisibility() == View.VISIBLE)
				{
					subs = null;
					currentModles.clear();
					currentModles.addAll(currentCategory.getSubCategorys());
					adapter = new CategoryAdapter(CategoryActivity.this, currentModles);
					list_view_category.setAdapter(adapter);
					subListView.setVisibility(View.GONE);
				}
				render();
				mNavBar.setText(R.string.category);
//				ToolUtil.reportStatisticsClick(getActivityPageId(), "70000");
			}
		});
		
		mNavBar.setOnIndicatorClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToolUtil.startActivity(CategoryActivity.this,
						SearchActivity.class);
			}
		});
		
		list_view_category = (ListView) findViewById(R.id.category_container);
		list_view_category.setOnItemClickListener(this);
		list_view_category.setAdapter(adapter);
		subListView = (ListView) findViewById(R.id.category_sub_listview);
		setLoadingSwitcher(BaseActivity.LOADING_SWITCHER_FLAG_DEFAULT,
				list_view_category, findViewById(R.id.category_loading));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		BaseModel model = (BaseModel) adapter.getItem(position);
		if (null == model) {
			Log.e(LOG_TAG, "onItemClick|model is null");
			return;
		}
		if (model instanceof CategoryModel) {
//			NodeCategoryModel node = (NodeCategoryModel) nodes.get(position);

//			SearchModel searchModel = SearchHelper.getSearchModel(node);
//			Bundle param = new Bundle();
//			// param.putString(ListActivity.REQUEST_PAGE_TITLE, node.name);
//			param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL,
//					searchModel);
			Bundle param = new Bundle();
			param.putString(ListActivity.KEY_BUS_ID, mGameInfo.getBizCode());
			param.putString(ListActivity.KEY_CATE_ID, ((CategoryModel)model).getId());
			ToolUtil.startActivity(CategoryActivity.this, ListActivity.class, param);
			
//			ToolUtil.reportStatisticsClick(getActivityPageId(), "30001");

		} else if (model instanceof SubCategoryModel) {
			SubCategoryModel subCategory = (SubCategoryModel) model;
			initNodes(subCategory, position);
//			ToolUtil.reportStatisticsClick(getActivityPageId(), "30002");
		}

	}

	private void initNodes(SubCategoryModel subCategory, final int position) {
		if(subs != null ){
			for(SubCategoryModel sub:subs)
				sub.isSelected = false;
			
			subs.get(position).isSelected = true;
			subAdapter.notifyDataSetChanged();
		}else{
			
		}

		nodes.clear();
		nodes.addAll(subCategory.getNodes());
		if(subListView.getVisibility() != View.VISIBLE){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.fly_right_in);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					subs = currentCategory.getSubCategorys();
					for(SubCategoryModel sub:subs)
						sub.isSelected = false;
					subs.get(position).isSelected = true;
					subAdapter = new SubCategoryAdapter(CategoryActivity.this, subs);
					list_view_category.setAdapter(subAdapter);
				}
			});
			subListView.startAnimation(animation);
		}
		subListView.setDividerHeight(0);
		subListView
				.setAdapter(new CategoryAdapter(CategoryActivity.this, nodes));
		subListView.setVisibility(View.VISIBLE);
		subListView.bringToFront();
		subListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				NodeCategoryModel node = (NodeCategoryModel) nodes
//						.get(position);
//
//				SearchModel searchModel = SearchHelper.getSearchModel(node);
//				Bundle param = new Bundle();
////				param.putString(ListActivity.REQUEST_PAGE_TITLE, node.name);
//				param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL,
//						searchModel);
//				ToolUtil.startActivity(CategoryActivity.this,
//						ListActivity.class, param);
//				ToolUtil.sendTrack(CategoryActivity.class.getClass().getName(), getString(R.string.tag_CategoryActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), tag1 + tag2
//						+ tag3);
//				ToolUtil.reportStatisticsClick(getActivityPageId(), "30003");
//				RecentCates.addCate(node.path);
			}
		});
	}

	private String getCacheKey() {
//		return CacheKeyFactory.CACHE_BLOCK_CATEGORY + "_" + String.valueOf(ILogin.getSiteId());
		return CacheKeyFactory.CACHE_BLOCK_CATEGORY + "_" + String.valueOf(mGameInfo.getBizCode());
	}

	private void initData() {
//		if(currentModles.size()>0)
//			return;
		
		IPageCache cache = new IPageCache();
		String content = cache.get(getCacheKey());
		if (content != null) {
			try {
				if(null == mCategoryModelParser) {
					mCategoryModelParser = new CategoryModelParser();
				}
				allCategoryModels = mCategoryModelParser.parse(content);
			} catch (Exception ex) {
				Log.e(LOG_TAG, ex);
				allCategoryModels = null;
			}finally{
				mCategoryModelParser = null;
			}
		} else {
			allCategoryModels = null;
		}

		if (allCategoryModels != null && allCategoryModels.size() > 0) {
			render();
		} else {
			showLoadingLayer();
			
			Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/CateList.php?plat=2");//ServiceConfig.getAjax(Config.URL_CATEGORY_NEW);
			if (null == ajax)
				return;
			
			ajax.setData("busid", mGameInfo.getBizCode());
//			ajax.setData("busid", "nz");

			if(null == mCategoryModelParser) {
				mCategoryModelParser = new CategoryModelParser();
			}
			ajax.setParser(mCategoryModelParser);
			ajax.setOnSuccessListener(this);
			ajax.setOnErrorListener(this);
			ajax.send();
			addAjax(ajax);
		}
	}

	private void render() {
		if(currentModles == null) {
			Log.w(TAG, "[render], currentModles is null");
			return;
		}
		if(adapter == null) {
			Log.w(TAG, "[render], adapter is null");
			return;
		}
		currentModles.clear();
		currentModles.addAll(allCategoryModels);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onSuccess(ArrayList<CategoryModel> models, Response response) {
		closeLoadingLayer();
		
		if( !mCategoryModelParser.isSuccess() ) {
			UiUtils.makeToast(this, TextUtils.isEmpty(mCategoryModelParser.getErrMsg()) ? Config.NORMAL_ERROR: mCategoryModelParser.getErrMsg());
			return;
		}
		
		allCategoryModels = models;
		mUIHandler.sendEmptyMessage(MSG_REFRESH_LIST);
		
		if(allCategoryModels != null && allCategoryModels.size() > 0) {
			IPageCache cache = new IPageCache();
			cache.set(getCacheKey(), mCategoryModelParser.getString(), 3600 * 24);
		}
		
		mCategoryModelParser = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//如果分类数据为空，直接返回
			if(currentModles.size() == 0)
				return super.onKeyDown(keyCode, event);
			
			BaseModel model = currentModles.get(0);

			if (model instanceof SubCategoryModel) {
				if (subListView.getVisibility() == View.VISIBLE) {
					subs = null;
					currentModles.clear();
					currentModles.addAll(currentCategory.getSubCategorys());
					adapter = new CategoryAdapter(CategoryActivity.this, currentModles);
					list_view_category.setAdapter(adapter);
					
					subListView.setVisibility(View.GONE);
					Animation animation = AnimationUtils.loadAnimation(this, R.anim.fly_right_out);
					subListView.startAnimation(animation);
					list_view_category.bringToFront();
				} else {
					render();
					mNavBar.setText(R.string.category);
				}
				return true;
			}
				}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		mUIHandler.removeCallbacksAndMessages(null);
		adapter = null;
		list_view_category = null;
		allCategoryModels = null;
		currentModles = null;
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		cleanAllAjaxs();
		super.onPause();
	}
	
	private static final String TAG = "CategoryActivity";
	private static final int MSG_REFRESH_LIST = 1001;
	private Handler mUIHandler = new UIHandler(this);
	private static class UIHandler extends Handler {
		private final WeakReference<CategoryActivity> mActivityRef;
		public UIHandler(CategoryActivity activity) {
			// TODO Auto-generated constructor stub
			mActivityRef = new WeakReference<CategoryActivity>(activity);
		}
		
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			CategoryActivity activity = mActivityRef.get();
			if(activity == null) {
				return;
			}

			int msgCode = msg.what;
			switch (msgCode) {
			case MSG_REFRESH_LIST: {
				activity.render();
				break;
			}
			default:
				break;
			}
		};
	}
	
	
//	@Override
//	public String getActivityPageId() {
//		return getString(R.string.tag_CategoryActivity);
//	}

}
