package com.icson.item;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.ReviewModel;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class ItemTabReviewAdapter extends BaseAdapter {

	private int tabId;

	private ArrayList<ReviewModel> mSatisfyReviewModels;

	private ArrayList<ReviewModel> mGeneralReviewModels;

	private ArrayList<ReviewModel> mUnSatisfyReviewModels;

	private BaseActivity mActivity;

	private LayoutInflater mInflator;

	public ItemTabReviewAdapter(BaseActivity mActivity, ArrayList<ReviewModel> mSatisfyReviewModels, ArrayList<ReviewModel> mGeneralReviewModels, ArrayList<ReviewModel> mUnSatisfyReviewModels) {
		this.mActivity = mActivity;
		this.mSatisfyReviewModels = mSatisfyReviewModels;
		this.mGeneralReviewModels = mGeneralReviewModels;
		this.mUnSatisfyReviewModels = mUnSatisfyReviewModels;
		mInflator = mActivity.getLayoutInflater();
	}

	@Override
	public int getCount() {
		final ArrayList<ReviewModel> models = tabId == ItemTabReviewView.TAB_SATISFY ? mSatisfyReviewModels : (  tabId == ItemTabReviewView.TAB_GENERAL ? mGeneralReviewModels : mUnSatisfyReviewModels);
		return models == null ? 0 : models.size();
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
			convertView = mInflator.inflate(R.layout.item_tab_review_item, null);
			holder = new ItemHolder();
			holder.star1 = (ImageView) convertView.findViewById(R.id.item_tab_review_star_1);
			holder.star2 = (ImageView) convertView.findViewById(R.id.item_tab_review_star_2);
			holder.star3 = (ImageView) convertView.findViewById(R.id.item_tab_review_star_3);
			holder.star4 = (ImageView) convertView.findViewById(R.id.item_tab_review_star_4);
			holder.star5 = (ImageView) convertView.findViewById(R.id.item_tab_review_star_5);
			
			holder.point = (TextView) convertView.findViewById(R.id.item_tab_review_point);
			holder.result = (TextView) convertView.findViewById(R.id.item_tab_review_result);
			holder.content = (TextView) convertView.findViewById(R.id.item_tab_review_content);
			
			holder.userName = (TextView) convertView.findViewById(R.id.item_tab_review_name);
			holder.level = (TextView) convertView.findViewById(R.id.item_tab_review_level);
			holder.time = (TextView) convertView.findViewById(R.id.item_tab_review_time);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}
		
		final ArrayList<ReviewModel> models = tabId == ItemTabReviewView.TAB_SATISFY ? mSatisfyReviewModels : (  tabId == ItemTabReviewView.TAB_GENERAL ? mGeneralReviewModels : mUnSatisfyReviewModels);
		ReviewModel model = models.get(position);
		
		if(model == null){
			return convertView;
		}
		
		holder.star1.setImageResource( model.getStar() > 0 ? R.drawable.i_global_star_active : R.drawable.i_global_star );
		holder.star2.setImageResource( model.getStar() > 1 ? R.drawable.i_global_star_active : R.drawable.i_global_star );
		holder.star3.setImageResource( model.getStar() > 2 ? R.drawable.i_global_star_active : R.drawable.i_global_star );
		holder.star4.setImageResource( model.getStar() > 3 ? R.drawable.i_global_star_active : R.drawable.i_global_star );
		holder.star5.setImageResource( model.getStar() > 4 ? R.drawable.i_global_star_active : R.drawable.i_global_star );
		
		holder.point.setText(mActivity.getString(R.string.review_star,model.getStar()));
		holder.result.setText(model.getType() == 1 ? R.string.satisfy : ( model.getType() == 2 ? R.string.general : R.string.unsatisfy ));
		holder.content.setText(model.getContent());
		
		holder.userName.setText(model.getUserName());
		holder.level.setText( ToolUtil.getUserLevelName( model.getUserLevel()));
		holder.time.setText(ToolUtil.toDate(model.getCreateTime() * 1000));
		
		return convertView;
	}

	public void setTab(int tabId) {
		this.tabId = tabId;
	}

	private static class ItemHolder {
		TextView point;
		ImageView star1;
		ImageView star2;
		ImageView star3;
		ImageView star4;
		ImageView star5;
		TextView result;
		TextView content;
		TextView userName;
		TextView level;
		TextView time;
	}
}
