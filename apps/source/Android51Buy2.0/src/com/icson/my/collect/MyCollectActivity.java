package com.icson.my.collect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.icson.R;
import com.icson.home.HomeActivity;
import com.icson.item.ItemActivity;
import com.icson.lib.control.FavorControl;
import com.icson.lib.model.FavorProductListModel;
import com.icson.lib.model.FavorProductModel;
import com.icson.lib.model.PageModel;
import com.icson.lib.parser.FavorProductListParser;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.my.main.MyIcsonActivity;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyCollectActivity extends BaseActivity implements View.OnClickListener,OnSuccessListener<FavorProductListModel>, OnErrorListener {
	private static final int STATUS_PREPARE	= 1;
	private static final int STATUS_LOADING	= 2;
	private static final int STATUS_FINISH	= 3;
	private boolean 		 firstExec   	= true;
	
	private FavorProductListModel 	mFavorProductListModel;
	private MyCollectAdapter 		mMyCollectAdapter;
	private FavorControl 			mFavorControl;
	private FavorProductListParser 	mParser;
	private ListView 				mListView;
	private Drawable 				mEditIcon;
	private Drawable 				mFinishIcon;
	private View 					mFooter;
	private OnItemClickListener		mOnItemClickListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_collect);
		InitUi() ;
		init();
	}

	@Override
	protected void onDestroy() {
		
		
		mListView = null;
		mMyCollectAdapter = null;
		mFavorProductListModel = null;
		super.onDestroy();
	}

	private void InitUi() {
		mListView = (ListView) findViewById(R.id.collect_listview);
		mFooter =  getLayoutInflater().inflate(R.layout.my_collect_footer, null);
		mFooter.findViewById(R.id.my_collect_listview_clickmore).setOnClickListener(this);
		
		loadNavBar(R.id.my_collect_navbar);
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if( null != mMyCollectAdapter ) {
					// Update status.
					mMyCollectAdapter.setEditing(!mMyCollectAdapter.isEditing(), true);
					updateRightText();
					if(mMyCollectAdapter.isEditing()) {
						mListView.setOnItemClickListener(null);
					}else{
						mListView.setOnItemClickListener(mOnItemClickListener);
					}
				}
			}
		});
		
		//点击Item之后挑战到详细信息的页面
		mOnItemClickListener = new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if( position <  mFavorProductListModel.getFavorProductModels().size() ){
					Bundle param = new Bundle();
					long pid = mFavorProductListModel.getFavorProductModels().get(position).getProductId();
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID, pid);
					ToolUtil.startActivity(MyCollectActivity.this, ItemActivity.class, param);
					ToolUtil.sendTrack(MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), "02010", String.valueOf(pid));
				}
			}
		};
		
		mListView.addFooterView(mFooter);
		mFavorControl = new FavorControl(this);
		mParser = new FavorProductListParser();

		mFavorProductListModel = new FavorProductListModel();
		mFavorProductListModel.setFavorProductModels(new ArrayList<FavorProductModel>());
		PageModel mPageModel = new PageModel();
		mPageModel.setCurrentPage(-1);
		mFavorProductListModel.setPageModel(mPageModel);
		mMyCollectAdapter = new MyCollectAdapter(this, mFavorProductListModel.getFavorProductModels(), mFavorControl);
		mListView.setAdapter(mMyCollectAdapter);
		if(mMyCollectAdapter.isEditing()) {
			mListView.setOnItemClickListener(null);
		}else{
			mListView.setOnItemClickListener(mOnItemClickListener);
		}
		
		mEditIcon = getResources().getDrawable(R.drawable.delete_cart_icon);
		mEditIcon.setBounds(0, 0, mEditIcon.getMinimumWidth(), mEditIcon.getMinimumHeight());
		
		mFinishIcon = getResources().getDrawable(R.drawable.edit_cart_icon);
		mFinishIcon.setBounds(0, 0, mEditIcon.getMinimumWidth(), mEditIcon.getMinimumHeight());
		
		findViewById(R.id.collect_button_redirect).setOnClickListener(this);

	}
	
	private void updateRightText() {
		if( null != mFavorProductListModel && 0 != mFavorProductListModel.getFavorProductModels().size() ) {
			final boolean bEditing = mMyCollectAdapter.isEditing();
			String strText = getString(bEditing ? R.string.btn_done : R.string.btn_edit);
			Drawable pDrawable = bEditing ?  mFinishIcon : mEditIcon;
			mNavBar.setRightVisibility(View.VISIBLE);
			mNavBar.setRightText(strText, pDrawable);
		}else{
			mNavBar.setRightVisibility(View.GONE);
		}
	}
	
	private void updateLayout(ArrayList<FavorProductModel> pPageModel)
	{
		if (pPageModel.size() == 0) {
			mListView.setVisibility(View.GONE);
			findViewById(R.id.collect_relative_empty).setVisibility(View.VISIBLE);
		}
	}
	

	public void init() {
		if (!firstExec) {
			return;
		}

		firstExec = false;
		sendRequest();
	}

	private void sendRequest() {
		setFooterStatus(STATUS_LOADING);
		mFavorControl.getList(mFavorProductListModel.getPageModel().getCurrentPage() + 1, mParser, this, this);
	}


	/**
	 * 
	* method Name:remove    
	* method Description: remove favorate product at pos  
	* @param position   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void remove(final int position) {
		if (position == mMyCollectAdapter.getCount())
			return;

		showProgressLayer();
		FavorProductModel model = (FavorProductModel) mMyCollectAdapter.getItem(position);
		mFavorControl.remove(model.getProductId(), model.getFavorId(), new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeProgressLayer();
				if (v.optInt("errno", -1) == 0) {
					mFavorProductListModel.getFavorProductModels().remove(position);
					//mMyCollectAdapter.notifyDataSetChanged();
					refreshUI(mFavorProductListModel);
				} else {
					UiUtils.makeToast(MyCollectActivity.this, R.string.message_delete_failed);
				}
			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				closeProgressLayer();
				UiUtils.makeToast(MyCollectActivity.this, R.string.message_delete_failed);
			}
		});
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		setFooterStatus(STATUS_PREPARE);
		UiUtils.makeToast(this, "加载失败, 请重试!");
	}

	private void setFooterStatus(int status) {
		if (status == STATUS_FINISH) {
			mListView.removeFooterView(mFooter);
			return;
		}

		mFooter.findViewById(R.id.my_collect_listview_loading).setVisibility(status == STATUS_LOADING ? View.VISIBLE : View.GONE);
		mFooter.findViewById(R.id.my_collect_listview_clickmore).setVisibility(status == STATUS_PREPARE ? View.VISIBLE : View.GONE);

	}

	@Override
	public void onSuccess(FavorProductListModel v, Response response) {

		if (!mParser.isSuccess()) {
			setFooterStatus(STATUS_PREPARE);
			UiUtils.makeToast(this, mParser.getErrorMsg());
			return;
		}
		
		
		// Moved to refreshList method, need to modify datasource in UI thread.
//		mFavorProductListModel.getFavorProductModels().addAll(v.getFavorProductModels());
//		mFavorProductListModel.setPageModel(v.getPageModel());
//		mMyCollectAdapter.notifyDataSetChanged();
//		
//		final PageModel mPageModel = v.getPageModel();
//		setFooterStatus(mPageModel.getCurrentPage() >= mPageModel.getPageCount() - 1 ? STATUS_FINISH : STATUS_PREPARE);
//		updateLayout(mPageModel);
//		updateRightText();

		Message msg = mUIHandler.obtainMessage(MSG_REFRESH_LIST, v);
		mUIHandler.sendMessage(msg);
	}
	
	private void refreshUI(FavorProductListModel v) {
		if(mFavorProductListModel == null) {
			Log.w(TAG, "[refreshList], mFavorProductListModel is null");
			return;
		}
		if(mMyCollectAdapter == null) {
			Log.w(TAG, "[refreshList], mMyCollectAdapter is null");
			return;
		}
		
		final PageModel newPageModel = v.getPageModel();
		PageModel curPageModel = mFavorProductListModel.getPageModel();
		
		if(null!=mFavorProductListModel.getFavorProductModels() && v!=mFavorProductListModel)
		{	
			if(newPageModel.getCurrentPage()==curPageModel.getCurrentPage())
				mFavorProductListModel.getFavorProductModels().clear();
			mFavorProductListModel.getFavorProductModels().addAll(v.getFavorProductModels());
		}
		mFavorProductListModel.setPageModel(v.getPageModel());
		mMyCollectAdapter.notifyDataSetChanged();
		
		setFooterStatus(newPageModel.getCurrentPage() >= newPageModel.getPageCount() - 1 ? STATUS_FINISH : STATUS_PREPARE);
		updateLayout(v.getFavorProductModels());
		updateRightText();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_collect_listview_clickmore:
			sendRequest();
			break;
		case R.id.collect_button_redirect:
			MainActivity.startActivity(this, MainActivity.TAB_HOME);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MyCollectActivity), HomeActivity.class.getName(), getString(R.string.tag_Home), "02011");
			break;
		}
	}
	
	private static final String TAG = "MyCollectActivity";
	private static final int MSG_REFRESH_LIST = 1001;
	private Handler mUIHandler = new UIHandler(this);
	private static class UIHandler extends Handler {
		private final WeakReference<MyCollectActivity> mActivityRef;
		public UIHandler(MyCollectActivity activity) {
			// TODO Auto-generated constructor stub
			mActivityRef = new WeakReference<MyCollectActivity>(activity);
		}
		
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			MyCollectActivity activity = mActivityRef.get();
			if(activity == null) {
				return;
			}

			int msgCode = msg.what;
			switch (msgCode) {
			case MSG_REFRESH_LIST: {
				FavorProductListModel v = (FavorProductListModel) msg.obj;
				activity.refreshUI(v);
				break;
			}
			default:
				break;
			}
		};
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MyCollectActivity);
	}
}
