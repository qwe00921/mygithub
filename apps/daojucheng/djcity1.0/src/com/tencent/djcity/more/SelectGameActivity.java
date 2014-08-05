package com.tencent.djcity.more;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.IPageCache;
import com.tencent.djcity.lib.inc.CacheKeyFactory;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageHelper;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.StringUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class SelectGameActivity extends BaseActivity {
	public final static String RESPONSE_SELECT_DISTRICT 	= "city_name";
	public final static String SOURCE_SELECT_CITY 	= "from";
	public final static String SELECT_CITY_FROM_HOME 	= "home";
	public final static int REQUEST_SELECT_DISTRICT 		= 1;
	
	private SelectGameAdapter 		mAdapter;
	private ListView 				mGameListView;
	private EditText				mEditText;
	private int						mSelectedCityId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_select_game);
		loadNavBar(R.id.navigation_bar);
		
		mNavBar.setRightInfo(R.string.close_string, new OnClickListener(){
			@Override
			public void onClick(View view) {
//				finish();
			}
		});
		mEditText = (EditText) findViewById(R.id.search_edittext);
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if(null!= event && event.getAction() != KeyEvent.ACTION_DOWN )
					return false;
				
				CharSequence text = v.getText();
				if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL)
				{

					search(text.toString());
					return true;
				}
				return false;
			}
		});
		
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				search(s.toString());
			}
		});
		
		mGameListView = (ListView) findViewById(R.id.city_list_view);
		
		Intent mIntent = getIntent();
		String strFrom = mIntent.getStringExtra(SOURCE_SELECT_CITY);
		if(null != strFrom && strFrom.equals(SELECT_CITY_FROM_HOME)){
//			String strCityName = (null == DispatchFactory.getDefaultCityName()) ? getString(R.string.shanghai_city) : DispatchFactory.getDefaultCityName();
//			setNavBarText(getString(R.string.settings_current_city) + strCityName);
			setNavBarRightVisibility(View.VISIBLE);
			mNavBar.setLeftVisibility(View.GONE);
		}else{
			setNavBarRightVisibility(View.GONE);
			mNavBar.setLeftVisibility(View.VISIBLE);
		}
		
		mSelectedCityId = getSelectCityId();
//		mDispatchItems = DispatchFactory.getDataSource();
		mAdapter = new SelectGameAdapter(this, mSelectedCityId);
		mGameListView.setAdapter(mAdapter);
		
		mGameListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				mItem = mAdapter.getItem(position);
				
//				UiUtils.makeToast(SelectCityActivity.this, 
//						getString(R.string.settings_current_city) + mItem.name, true);
				
				Intent intent = new Intent(SelectGameActivity.this, SelectDistrictActivity.class);
				GameNameModel model = (GameNameModel) parent.getAdapter().getItem(position);
				intent.putExtra(SelectDistrictActivity.KEY_BIZ_CODE, model.getBizCode());
				intent.putExtra(SelectDistrictActivity.KEY_BIZ_NAME, model.getBizName());
				intent.putExtra(SelectDistrictActivity.KEY_BIZ_IMG, model.getImageUrl());
				intent.putExtra(SelectDistrictActivity.KEY_ROLE_FLAG, model.getRoleFlag());
				startActivityForResult(intent, REQUEST_SELECT_DISTRICT);
			}
		});
		
		requestData();
	}
	
	private void search(String text) {
		if(text == null || "".equals(text)) {
			mAdapter.setDatasource(mList);
			mAdapter.notifyDataSetChanged();
			return;
		}
		List<GameNameModel> gameNameList = new ArrayList<GameNameModel>();
		for(int i = 0; i < mList.size(); i++) {
			Object object = mList.get(i);
			if(object instanceof GameNameModel) {
				GameNameModel model = (GameNameModel) object;
				if(model.getBizName().contains(text)) {
					gameNameList.add(model);
				}
			}
				
		}

		mAdapter.setDatasource(gameNameList);
		mAdapter.notifyDataSetChanged();
	}
	
	private void requestData() {
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/listBiz.php?xxx");
		
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				
				try {
					JSONArray array = v.getJSONArray("data");
					parseGameNames(array);
					mAdapter.setDatasource(mList);
					mAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		ajax.setOnErrorListener(this);
		ajax.setParser(new JSONParser());
		ajax.send();
		
//		String str = "{\"ret\":0, \"msg\":\"ok!\", \"total\":2, \"list\":[{\"bizCode\":\"lol\", \"hot\":1, \"bizName\":\"英雄联盟\", \"icon\":\"http://image-cache.cdn13.qq.com/daoju/biz/lol.png\"}, { \"bizCode\":\"cf\", \"bizName\":\"穿越火线\", \"icon\":\"http://image-cache.cdn13.qq.com/daoju/biz/cf.png\"}], \"sign\" : \"1dcb672bb215a 727c2f2181eb7157dee\" }";
//		try {
//			JSONObject obj = new JSONParser().parse(str.getBytes(), "utf-8");
//			JSONArray array = obj.getJSONArray("list");
//			parseGameNames(array);
//			mAdapter.notifyDataSetChanged();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_SELECT_DISTRICT: {
			if(resultCode == RESULT_OK) {
				finish();
			}
			break;
		}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private List<Object> mList = new ArrayList<Object>();
	private void parseGameNames(JSONArray array) throws JSONException {
		List<Object> listForAdapter = mList;
		String hotTitle = "热门推荐";
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			GameNameModel model = new GameNameModel();
			model.parse(object);
			
			if(model.isHot()) { //如果是热门推荐，则加到列表的最顶端
				Object firstTitle = null;
				if(listForAdapter.size() > 0) { 
					firstTitle = listForAdapter.get(0);
				}
				if(!hotTitle.equals(firstTitle)) {
					listForAdapter.add(hotTitle);
				}
				listForAdapter.add(1, model);
			} else { 
				GameNameModel lastModel = null;
				char currentAlpha = StringUtil.convert(model.getBizName());
				if(listForAdapter.size() > 0) {
					lastModel = (GameNameModel)listForAdapter.get(listForAdapter.size() - 1); //上次添加的最后一个
					char lastAlpha = StringUtil.convert(lastModel.getBizName());
					if(currentAlpha == lastAlpha) {
						listForAdapter.add(model);
					} else {
						listForAdapter.add(String.valueOf(Character.toUpperCase(currentAlpha)));
						listForAdapter.add(model);
					}
				} else {
					listForAdapter.add(String.valueOf(Character.toUpperCase(currentAlpha)));
					listForAdapter.add(model);
				}
			}
		}
		
//		Comparator<GameNameModel> comparator = new Comparator<GameNameModel>() {
//
//			@Override
//			public int compare(GameNameModel left, GameNameModel right) {
//
//				char leftFirst = StringUtil.convert(left.getBizName());
//				char rightFirst = StringUtil.convert(right.getBizName());
//				
//				
//				return leftFirst - rightFirst;
//			}
//		};
//		
//		Collections.sort(nameList, comparator);
		
//		List<Object> listForAdapter = new ArrayList<Object>();
//		
//		for(GameNameModel model : nameList) {
//			
//		}
	}
	
	private int getSelectCityId(){
		int nCityId = 0;
		IPageCache cache = new IPageCache();
		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);
		
		nCityId = id != null ? Integer.valueOf( id ) : 0;
		return nCityId;
	}
	
	@Override
	protected void onPause() {
//		if(null!=mItem)
//		{
//			FullDistrictHelper.setFullDistrict(mItem.provinceId, mItem.cityId, mItem.district, mItem.siteId);
//		}
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
//		mDispatchItems = null;
		mAdapter = null;
		mGameListView = null;
//		mItem = null;
		
//		IShippingArea.clean();
		super.onDestroy();
	}

	class SelectGameAdapter extends BaseAdapter implements ImageLoadListener {
		private BaseActivity mActivity;
		private List<?> mDispatchItems;
		private LayoutInflater mInflater;
		private ImageLoader mImageLoader;
		
		public SelectGameAdapter(BaseActivity pActivity, int pSelectCityId){
			mActivity = pActivity;
			mInflater = mActivity.getLayoutInflater();
//			mSelectCityId = pSelectCityId;
			mImageLoader = new ImageLoader(mActivity, Config.MY_GAME_DIR, true);
		}
		
		public void setDatasource(List<?> datasource) {
			mDispatchItems = datasource;
		}

		@Override
		public int getCount() {
			if(mDispatchItems == null) {
				return 0;
			}
			return mDispatchItems.size();
		}

		@Override
		public Object getItem(int position) {
			if(mDispatchItems == null) {
				return null;
			}
			return mDispatchItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		private static final int TYPE_TITLE = 0;
		private static final int TYPE_ITEM = 1;
		@Override
		public int getItemViewType(int position) {
			
			Object object = getItem(position);
			if(object instanceof String) {
				return TYPE_TITLE;
			} else {
				return TYPE_ITEM;
			}
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public boolean isEnabled(int position) {
			switch(getItemViewType(position)) {
				case TYPE_ITEM: {
					return true;
				}
				case TYPE_TITLE: {
					return false;
				}
			}
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGrop) {
			
			switch(getItemViewType(position)) {
				case TYPE_ITEM: {
					GameNameModel item = (GameNameModel) this.getItem(position);
					ItemHolder holder = null;
					if (convertView == null) {
						convertView = mInflater.inflate(R.layout.adapter_select_game_item, null);
						holder = new ItemHolder();
						holder.gameName = (TextView) convertView.findViewById(R.id.game_name);
						holder.pImage = (ImageView) convertView.findViewById(R.id.game_icon);
						convertView.setTag(holder);
					} else {
						holder = (ItemHolder) convertView.getTag();
					}
					holder.gameName.setText(item.getBizName());
					
					String url = item.getImageUrl();
					Bitmap data = mImageLoader.get(url);
					holder.pImage.setImageBitmap(data != null ? data : ImageHelper.getResBitmap(mActivity, mImageLoader.getLoadingId()));
					if (data == null) {
						mImageLoader.get(url, this);
					}
					break;
				}
				case TYPE_TITLE: {
					String title =  this.getItem(position).toString();
					ItemHolder holder = null;
					if (convertView == null) {
						convertView = mInflater.inflate(R.layout.listitem_header, null);
						holder = new ItemHolder();
						holder.title = (TextView) convertView.findViewById(R.id.title);
						convertView.setTag(holder);
					} else {
						holder = (ItemHolder) convertView.getTag();
					}
					holder.title.setText(title);
					break;
				}
			}
			
			return convertView;
		}
		
		class ItemHolder{
			TextView gameName;
			TextView title;
			ImageView pImage;
		}

		@Override
		public void onLoaded(Bitmap aBitmap, String strUrl) {
			notifyDataSetChanged();
		}

		@Override
		public void onError(String strUrl) {
			// TODO Auto-generated method stub
			
		}
		
	}

//	@Override
//	public String getActivityPageId() {
//		return getString(R.string.tag_SelectCityActivity);
//	}
}
