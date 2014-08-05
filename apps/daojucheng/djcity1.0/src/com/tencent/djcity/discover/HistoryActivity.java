package com.tencent.djcity.discover;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.category.CategoryModel;
import com.tencent.djcity.category.CategoryModel.NodeCategoryModel;
import com.tencent.djcity.category.CategoryModel.SubCategoryModel;
import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.lib.ui.NavigationBar.OnLeftButtonClickListener;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class HistoryActivity extends BaseActivity{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_history);
		
		mListView = (ListView) findViewById(R.id.history_list);
		
		mAdapter = new HistoryAdapter(this, mPrizeList);
		mListView.setAdapter(mAdapter);
		
		mNavBar = (NavigationBar) findViewById(R.id.history_navbar);
        mNavBar.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
        	@Override
        	public void onClick()
        	{
        		onBackPressed();
        	}
        });
		
		requestData();
	}
	
	private ListView mListView;
	private HistoryAdapter mAdapter;
	private List<PrizeModel> mPrizeList = new ArrayList<PrizeModel>();
	private void requestData() {
    	Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/scratchLog.php?uin=1234565&page=1&sign=c2f2181eb71571dcb67354115a727");
    	if(ajax == null) {
    		return;
    	}
    	ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
    		public void onSuccess(JSONObject v, Response response) {
    			closeLoadingLayer();
				int ret = v.optInt("ret");
				String msg = v.optString("msg");
				if(ret != 0) {
					UiUtils.makeToast(HistoryActivity.this, msg);
					return;
				}
				JSONArray data = v.optJSONArray("list");
				if(data != null) {
					for(int i = 0; i < data.length(); i++) {
						JSONObject object = data.optJSONObject(i);
						PrizeModel model = new PrizeModel();
						model.parse(object);
						mPrizeList.add(model);
					}
					mAdapter.notifyDataSetChanged();	
				}
				
    		};
		});
    	ajax.setOnErrorListener(this);
    	ajax.setParser(new JSONParser());
    	ajax.send();
	}
	
	private class HistoryAdapter extends BaseAdapter {

		private Context mContext;
		private List<PrizeModel> mDatasource;
		private LayoutInflater mInflater;
		public HistoryAdapter(Context context, List<PrizeModel> datasource) {
			mContext = context;
			mDatasource = datasource;
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			if(mDatasource == null) {
				return 0;
			}
			return mDatasource.size();
		}

		@Override
		public PrizeModel getItem(int position) {
			if(mDatasource == null) {
				return null;
			}
			
			return mDatasource.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder = null;

			PrizeModel item = getItem(position);
			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.history_item, null);
				holder = new ItemHolder();
				holder.name = (TextView) convertView
						.findViewById(R.id.category_textview_name);
				holder.desc = (TextView) convertView
						.findViewById(R.id.category_textview_desc);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}

			PrizeModel prize = (PrizeModel) item;
			holder.name.setText(prize.getName());
			holder.desc.setText(prize.getTime());
			return convertView;
		}
		
		
	}
	
	static class ItemHolder {
		TextView name;
		TextView desc;
	}
}
