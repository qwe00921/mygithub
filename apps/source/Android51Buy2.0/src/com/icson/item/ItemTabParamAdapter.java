package com.icson.item;

import java.util.ArrayList;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.util.activity.BaseActivity;

public class ItemTabParamAdapter extends BaseAdapter {

	private ArrayList<ItemParamModel> mItemParamModels;

	private LayoutInflater mInflator;

	public ItemTabParamAdapter(BaseActivity mActivity,  ArrayList<ItemParamModel> mItemParamModels) {
		mInflator = mActivity.getLayoutInflater();
		this.mItemParamModels = mItemParamModels;
	}

	@Override
	public int getCount() {
		return mItemParamModels.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = null;
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.item_tab_param_item, null);
			holder = new ItemHolder();
			holder.title = (TextView) convertView.findViewById(R.id.item_param_title);
			holder.content = (ViewGroup) convertView.findViewById(R.id.item_param_content);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}
		 
		holder.content.removeAllViews();
		
		ItemParamModel  mItemParamModel = mItemParamModels.get(position);
		
		holder.title.setText(mItemParamModel.getName());
		
		ArrayList<ItemSubParamModel> mItemSubParamModels = mItemParamModel.getItemParamSubModels();
		
		if( mItemSubParamModels != null && mItemSubParamModels.size() > 0 ){
			int SubParamModelsSize = mItemSubParamModels.size();
			for(int subParamIdx = 0; subParamIdx < SubParamModelsSize; subParamIdx++)
			{
				ItemSubParamModel model = mItemSubParamModels.get(subParamIdx);
				View view = mInflator.inflate(R.layout.item_tab_param_item_sub, null);
				((TextView)view.findViewById(R.id.item_param_sub_key)).setText(Html.fromHtml(model.getKey()));
				((TextView)view.findViewById(R.id.item_param_sub_value)).setText( Html.fromHtml(model.getValue()));
				//last item
				if(subParamIdx == SubParamModelsSize -1)
				{
					view.findViewById(R.id.item_param_tailline).setVisibility(View.GONE);
				}
				holder.content.addView(view);
			}
		}
		
		return convertView;
	}

	public void setTab(int tabId) {
	}

	private static class ItemHolder {
		TextView title;
		ViewGroup content;
	}
}
