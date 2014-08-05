package com.icson.postsale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.more.AdviseActivity;
import com.icson.more.FeedBackHistoryActivity;
import com.icson.util.AppUtils;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class PostSaleCenterActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_service_center);
		
		loadNavBar(R.id.listview_navigation_bar);
		setNavBarText(R.string.service_center_title);
		
		ListView listView = (ListView) findViewById(R.id.list_container);
		listView.setDividerHeight(0);
		
		if(listView != null) {
			listView.setAdapter(new PostSaleCenterAdapter(this));
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String pageId = getString(R.string.tag_PostSaleCenterActivity);
					switch (position) {
						case PostSaleCenterAdapter.ITEM_POSTSALE_LIST: {
							ToolUtil.startActivity(PostSaleCenterActivity.this, PostSaleRequestListActivity.class);
							ToolUtil.sendTrack(PostSaleCenterActivity.class.getName(), pageId, PostSaleRequestListActivity.class.getName(), getString(R.string.tag_PostSaleRequestListActivity), "02019");
							break;
						}
						case PostSaleCenterAdapter.ITEM_FEEDBACK: {
							ToolUtil.checkLoginOrRedirect(PostSaleCenterActivity.this, FeedBackHistoryActivity.class, null, -1);
							ToolUtil.sendTrack(PostSaleCenterActivity.class.getName(), pageId, AdviseActivity.class.getName(), getString(R.string.tag_AdviseActivity), "01013");
							break;
						}
						case PostSaleCenterAdapter.ITEM_CONTACT_US: {
							Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4008281878"));
							if(AppUtils.checkAndCall(PostSaleCenterActivity.this,pIntent))
								ToolUtil.sendTrack(PostSaleCenterActivity.class.getName(), pageId, "contact us", getString(R.string.tag_MoreActivity), "02012");
							break;
						}
						default: {
							break;
						}
					}
				}
			});
		}
	}
	
	private class PostSaleCenterAdapter extends BaseAdapter {
		
		private Context mContext;
		private LayoutInflater mInflater;
		public PostSaleCenterAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}
		
		public static final int ITEM_POSTSALE_LIST = 0;
		public static final int ITEM_FEEDBACK = 1;
		public static final int ITEM_CONTACT_US = 2;
		public static final int ITEM_COUNT = 3;
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ITEM_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.listitem2line_image, null);
			}
			
			TextView primaryText = (TextView) convertView.findViewById(R.id.tv_primary);
			TextView secondaryText = (TextView) convertView.findViewById(R.id.tv_secondary);
			switch(position) {
				case ITEM_POSTSALE_LIST: {
					primaryText.setText(mContext.getText(R.string.postsale_req_btn_primary));
					secondaryText.setText(mContext.getText(R.string.text_view_history));
					break;
				}
				case ITEM_FEEDBACK: {
					primaryText.setText(mContext.getText(R.string.feedback_btn_primary));
					secondaryText.setText(mContext.getText(R.string.feedback_btn_secondary));
					break;
				}
				case ITEM_CONTACT_US: {
					primaryText.setText(mContext.getText(R.string.contactus_btn_primary));
					secondaryText.setText(mContext.getText(R.string.contactus_btn_secondary));
					break;
				}
				default: {
					break;
				}
			}
			
			return convertView;
		}
		
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_PostSaleCenterActivity);
	}
}
