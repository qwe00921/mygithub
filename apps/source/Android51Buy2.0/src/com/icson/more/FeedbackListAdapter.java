package com.icson.more;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.FeedbackItemModel;
import com.icson.lib.model.FeedbackItemModel.FeedbackReplyModel;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class FeedbackListAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private ArrayList<FeedbackItemModel> dataSource;
	private ImageLoader mAsyncImageLoader;
	private BaseActivity activity;

	public FeedbackListAdapter(BaseActivity activity, ArrayList<FeedbackItemModel> dataSource) {

		mInflater = LayoutInflater.from(activity);
		this.dataSource = new ArrayList<FeedbackItemModel>();
		this.dataSource = dataSource;
		this.activity = activity;

		mAsyncImageLoader = new ImageLoader(activity, true);

		this.activity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		return dataSource.size();
	}

	@Override
	public Object getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setDataSource(ArrayList<FeedbackItemModel> data) {
		this.dataSource = data;
	
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.feedback_list_item, null);
			holder = new ItemHolder();
			holder.feedbackOrderId = (TextView) convertView.findViewById(R.id.feedbackOrderId);
			holder.feedbackTextContent = (TextView) convertView.findViewById(R.id.feedbackTextContent);

			holder.fbPicContainer = (LinearLayout) convertView.findViewById(R.id.feedback_pic_container);
			holder.picImages.add((ImageView) convertView.findViewById(R.id.picImage1));
			holder.picImages.add((ImageView) convertView.findViewById(R.id.picImage2));
			holder.picImages.add((ImageView) convertView.findViewById(R.id.picImage3));
			holder.picImages.add((ImageView) convertView.findViewById(R.id.picImage4));
			holder.picImages.add((ImageView) convertView.findViewById(R.id.picImage5));
			
			holder.timeTextContent = (TextView) convertView.findViewById(R.id.timeTextContent);
			holder.typeTextContent = (TextView) convertView.findViewById(R.id.typeTextContent);
			
			holder.replayContainer = (LinearLayout) convertView.findViewById(R.id.feedback_reply_container);
			holder.feedbackReplayContent = (TextView) convertView.findViewById(R.id.feedbackReplayContent);
			holder.timeTextReplayContent = (TextView) convertView.findViewById(R.id.timeTextReplayContent);
			holder.typeImageNew = (ImageView) convertView.findViewById(R.id.typeImageNew);
			
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		FeedbackItemModel model = dataSource.get(position);
		
		if (model.mOrderNo > 0) {
			holder.feedbackOrderId.setText("订单号" + model.mOrderNo + ":");
			holder.feedbackOrderId.setVisibility(View.VISIBLE);
		} else {
			holder.feedbackOrderId.setText("");
			holder.feedbackOrderId.setVisibility(View.GONE);
		}
		
		if (model.mAttachments.size() > 0) {
			holder.fbPicContainer.setVisibility(View.VISIBLE);
		} else {
			holder.fbPicContainer.setVisibility(View.GONE);
		}
		
		holder.feedbackTextContent.setText(model.mContent);
		int nPicNum = holder.picImages.size();
		int nAttachNum = model.mAttachments.size();
		for (int i = 0; i < nPicNum; i++) {
			ImageView picImg = holder.picImages.get(i);
			if (i < nAttachNum) {
				picImg.setVisibility(View.VISIBLE);
				String strUrl = model.mAttachments.get(i);
				final Bitmap pBitmap = mAsyncImageLoader.get(strUrl);
				if ( null != pBitmap ) {
					picImg.setImageBitmap(pBitmap);
				} else {
					mAsyncImageLoader.get(strUrl, this);
				}
			} else {
				picImg.setVisibility(View.INVISIBLE);
			}
		}
		
		holder.timeTextContent.setText(model.mApplyTime);
		holder.typeTextContent.setText(model.mApplyType);
		
		if (model.mReplayList.size() > 0) {
			holder.replayContainer.setVisibility(View.VISIBLE);
			FeedbackReplyModel replay = (FeedbackReplyModel) model.mReplayList.get(0);
			holder.feedbackReplayContent.setText(replay.mReplyContent);
			holder.timeTextReplayContent.setText(replay.mReplyTime);
			if (model.mHasReplay > 0) {
				holder.typeImageNew.setVisibility(View.VISIBLE);
			} else {
				holder.typeImageNew.setVisibility(View.GONE);
			}
		} else {
			holder.replayContainer.setVisibility(View.GONE);
		}
		return convertView;
	}

	@Override
	public void onDestroy() {
		dataSource = null;
		if( null != mAsyncImageLoader )
		{
			mAsyncImageLoader.cleanup();
			mAsyncImageLoader = null;
		}
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}

	private static class ItemHolder {
		TextView feedbackOrderId;
		TextView feedbackTextContent;
		
		LinearLayout  fbPicContainer;
		ArrayList<ImageView>	picImages = new ArrayList<ImageView>();
		
		TextView timeTextContent;
		TextView typeTextContent;
		
		LinearLayout  replayContainer;
		TextView feedbackReplayContent;
		TextView timeTextReplayContent;
		ImageView typeImageNew;
	}
}
