package com.icson.postsale;

import java.util.List;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class PostSaleRequestListAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private List<PostSaleRequestModel> mDataSource;
//	private ImageLoader mImageLoader;
	private PostSaleRequestListActivity mActivity;
//	private PayCore mPayCore = null;
//	private OrderControl mOrderControl = null;
	//private int margin_30xp;
	//private int margin_15xp;
	private ImageLoader mImageLoader;
	private PostSaleControl mPostSaleControl;
//	private int margin_20xp;

	
//	private static final int ACTION_NONE = 0;
//	private static final int ACTION_PAY_NOW     = (ACTION_NONE + 1);
//	private static final int ACTION_CMT_NOW     = (ACTION_NONE + 2);
//	private static final int ACTION_CANCEL      = (ACTION_NONE + 3);
	
	public PostSaleRequestListAdapter(PostSaleRequestListActivity activity, List<PostSaleRequestModel> OrderModelList) {
		mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
		
		//String str_30xp = mActivity.getResources().getString(R.dimen.margin_size_30xp);
		//String str_15xp = mActivity.getResources().getString(R.dimen.margin_size_15xp);
//		String str_20xp = mActivity.getResources().getString(R.dimen.margin_size_20xp);
		
		//margin_30xp = (int)(mActivity.getResources().getDisplayMetrics().density*
		//					Float.valueOf(str_30xp.substring(0, str_30xp.length()-2)));
		//margin_15xp = (int)(mActivity.getResources().getDisplayMetrics().density*
		//			Float.valueOf(str_15xp.substring(0, str_15xp.length()-2)));
//		margin_20xp = (int)(mActivity.getResources().getDisplayMetrics().density*
//				Float.valueOf(str_20xp.substring(0, str_20xp.length()-2)));
		
		this.mDataSource = OrderModelList;
		mImageLoader = new ImageLoader(mActivity, Config.MY_ORDERLIST_DIR, true);
		mActivity.addDestroyListener(this);
		mPostSaleControl = new PostSaleControl(activity);
	}

	@Override
	public int getCount() {
		return (null == mDataSource  ? 0 : mDataSource.size());
	}

	@Override
	public Object getItem(int position) {
		// Check if position exceed array bound. For exception number 60508759
		if(position < 0 || position >= getCount()) {
			return null;
		}
		
		return (null == mDataSource ? null : mDataSource.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		final PostSaleRequestModel mOrderModel = (PostSaleRequestModel) getItem(position);
		
		if(mOrderModel instanceof PostSaleRequestModel){
			//虚拟订单
			final PostSaleRequestModel postSaleModel = (PostSaleRequestModel)mOrderModel;
			
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.postsale_request_list, null);
			}
			
			TextView applyTime = (TextView) convertView.findViewById(R.id.apply_time);
			TextView orderCharId = (TextView) convertView.findViewById(R.id.postsale_textview_order_id);
			TextView lastLogTime = (TextView) convertView.findViewById(R.id.postsale_textview_time);
			TextView lastLog = (TextView) convertView.findViewById(R.id.postsale_textview_total);
			TextView applyStatus = (TextView) convertView.findViewById(R.id.postsale_textview_status);
			ImageView itemPic = (ImageView) convertView.findViewById(R.id.postsale_pic_1);
			TextView itemTitle = (TextView) convertView.findViewById(R.id.postsale_tv_title);
			TextView itemCount = (TextView) convertView.findViewById(R.id.postsale_tv_phone);

			applyTime.setText("申请时间：" + postSaleModel.getApplyTime());

			orderCharId.setText("订单号: " + mOrderModel.getOrderCharId()+ " " + getItemCount(1));
			lastLog.setText(mOrderModel.getLogInfo().getContent());
			applyStatus.setText(postSaleModel.getApplyStatus());
			lastLogTime.setText("时间: " + postSaleModel.getLogInfo().getLogTime());
			
			itemPic.setVisibility(View.VISIBLE);
			
			String url = IcsonProImgHelper.getAdapterPicUrl(mOrderModel.getItem().getProductCharId(), 95);
			Bitmap data = mImageLoader.get(url);
			itemPic.setImageBitmap(data != null ? data : ImageHelper.getResBitmap(mActivity, mImageLoader.getLoadingId()));
			if (data == null) {
				mImageLoader.get(url, this);
			}
			
			itemTitle.setText(postSaleModel.getItem().getProductName());
			itemCount.setText("共" + postSaleModel.getItem().getProductNum() + "件");
			
			Button urgentButton = (Button) convertView.findViewById(R.id.postsale_urgent_button);
			if(urgentButton != null) {
				if(mOrderModel.getCanUrgent() == 1) {
					urgentButton.setVisibility(View.VISIBLE);
				} else {
					urgentButton.setVisibility(View.GONE);
				}
				
				urgentButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						v.setVisibility(View.GONE);
						mPostSaleControl.sendLevelUpRequest(postSaleModel.getApplyId(), null, null);
						UiUtils.makeToast(mActivity, R.string.message_send_success);
					}
				});
			}
			
			return convertView;
		}
		return convertView;
	}
	
	private String getItemCount(int nNum)
	{
		return mActivity.getString(R.string.item_count, nNum);
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}

	@Override
	public void onDestroy() {
		if(null!=this.mImageLoader)
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}
}
