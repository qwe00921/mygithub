package com.tencent.djcity.msgcenter;

import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.BannerInfo;
import com.tencent.djcity.home.HTML5LinkActivity;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.activity.BaseActivity.DestroyListener;

public class MsgAdapter extends BaseAdapter implements DestroyListener {
	
	private LayoutInflater mInflater;

	private List<BaseModel> mDataSource;

	private BaseActivity mActivity;

	public MsgAdapter(BaseActivity activity,
			List<BaseModel> dataSource) {

		mInflater = LayoutInflater.from(activity);
		this.mDataSource = dataSource;
		mActivity = activity;

		mActivity.addDestroyListener(this);
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return mDataSource.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ItemViewHolder holder = null;

		BaseModel item = (BaseModel) getItem(position);
		if (convertView == null) 
		{
			
			convertView = mInflater.inflate(R.layout.msg_item, null);
			
			holder = new ItemViewHolder();
			holder.mTitleView = (TextView) convertView
					.findViewById(R.id.textview_title);
			
			holder.mDateView = (TextView) convertView
					.findViewById(R.id.textview_date);
			
			holder.mContentView = (TextView) convertView
					.findViewById(R.id.textview_content);
			
			holder.mDetailsLayout = convertView.findViewById(R.id.details_layout);
			
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ItemViewHolder) convertView.getTag();
		}

		if (item instanceof MsgModel) 
		{
			final MsgModel msg = (MsgModel) item;
			holder.mTitleView.setText(msg.mBiz + msg.mTitle);
			holder.mDateView.setText(msg.mDate);
			
			final String sType = msg.mType;
			if(null == sType || sType.equals(""))
			{
				holder.mDetailsLayout.setVisibility(View.GONE);
			}
			else
			{
				holder.mDetailsLayout.setVisibility(View.VISIBLE);
				
				holder.mDetailsLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0)
					{
						//TODO type: item,action,h5
						UiUtils.makeToast(mActivity, msg.mTargetID + msg.mURL);
						
						if(sType.equals(BannerInfo.MODULE_INNER_LINK) || sType.equals("action"))
						{
							// banner -> h5
							String strUrl = msg.mURL;

							strUrl = "http://daoju.qq.com/v3/mapp/cf/index.html";

							Bundle bundle = new Bundle();
							bundle.putString(HTML5LinkActivity.LINK_URL, TextUtils.isEmpty(strUrl) ? null : strUrl);

							// Check back home activity.
							UiUtils.startActivity(mActivity, HTML5LinkActivity.class, bundle, true);
						}
						else if(sType.equals(BannerInfo.MODULE_ITEM))
						{
							// banner -> item
							Bundle abundle = new Bundle();
							abundle.putString(ItemActivity.KEY_PROP_ID, String.valueOf(msg.mTargetID));
							UiUtils.startActivity(mActivity, ItemActivity.class, abundle, true);
						}
						
					}
				});
			}
			
			holder.mContentView.setText(msg.mContent);
		}
		return convertView;
	}
	
	private class ItemViewHolder 
	{
		TextView mTitleView;
		TextView mDateView;
		TextView mContentView;
		
		View mDetailsLayout;
	}

	@Override
	public void onDestroy()
	{
		mInflater = null;
		mDataSource = null;
		mActivity = null;
	}
}
