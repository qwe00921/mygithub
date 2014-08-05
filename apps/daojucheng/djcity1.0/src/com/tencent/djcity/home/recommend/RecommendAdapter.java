package com.tencent.djcity.home.recommend;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.HomeActivity;
import com.tencent.djcity.home.recommend.ProductModel.Validate;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.IcsonProImgHelper;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;

public class RecommendAdapter extends BaseAdapter implements ImageLoadListener, OnClickListener {
	public RecommendAdapter(Activity aActivity, List<ProductModel> aDataSource) {
		mInflater = LayoutInflater.from(aActivity);
		mContext = aActivity;
		mDataSource = aDataSource;
		mImageLoader = new ImageLoader(mContext, Config.CHANNEL_PIC_DIR, true);
	}
	
	public void setParameters(int nType) {
		mType = nType;
	}

	@Override
	public int getCount() {
		final int count = (null != mDataSource ? mDataSource.size() : 0);
		return count / COLUMN_SIZE + (0 == count % COLUMN_SIZE ? 0 : 1 );
	}

	@Override
	public Object getItem(int position) {
		return (null != mDataSource ? mDataSource.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder pHolder = null;
		if( null == convertView ) {
			convertView = mInflater.inflate(R.layout.listitem_2column, null);
			pHolder = new ViewHolder();
			
			// Initialize the view holder instance.
			pHolder.mEntity1.init(this, convertView, R.id.time_buy_container_1, R.id.time_buy_image_1, R.id.list_prop_name1, R.id.list_price_qq1, R.id.list_price_wechat1);
			pHolder.mEntity2.init(this, convertView, R.id.time_buy_container_2, R.id.time_buy_image_2, R.id.list_prop_name2, R.id.list_price_qq2, R.id.list_price_wechat2);
			
			convertView.setTag(pHolder);
		} else {
			pHolder = (ViewHolder)convertView.getTag();
		}
		
		// Initialize the properties.
		final int nOffset = COLUMN_SIZE * position;
		final int nCount = (null != mDataSource ? mDataSource.size() : 0);
		final boolean bLastVisible = (nOffset + 1 < nCount);
		View pContainer = pHolder.mEntity2.mContainer;
		pContainer.setVisibility(bLastVisible ? View.VISIBLE : View.INVISIBLE);
		pContainer.setClickable(bLastVisible);
		
		if(null!=mDataSource && nOffset < mDataSource.size())
		{
		// Initialize the instance.
		ProductModel pFirst = mDataSource.get(nOffset);
		pHolder.mEntity1.setProperties(pFirst, nOffset);
		
		// Second.
		final int nNext = nOffset + 1;
		if( nNext < nCount ) {
			ProductModel pSecond = mDataSource.get(nNext);
			pHolder.mEntity2.setProperties(pSecond, nNext);
		}
		} 
		
		return convertView;
	}
	
	private void setPrice(TextView aView, double aPrice) {
		aView.setText(mContext.getString(R.string.rmb) + ToolUtil.toPrice(aPrice, 2));
	}
	
	private void setName(TextView aView, String strName, boolean isHasPromo) {
		if( null == aView) {
			return;
		}
		
		if(isHasPromo) {
			aView.setSingleLine(true);
		}else{
			aView.setSingleLine(false);
			aView.setMaxLines(2);
		}
		
		aView.setEllipsize(TextUtils.TruncateAt.END);
		aView.setText(strName);
	}
	
	private void setPromoWord(TextView aView, String strPromoWord) {
		if( null == aView) {
			return;
		}
		
		if(!TextUtils.isEmpty(strPromoWord)) {
			aView.setVisibility(View.VISIBLE);
			aView.setMaxLines(2);
			aView.setEllipsize(TextUtils.TruncateAt.END);
			aView.setText(strPromoWord);
		}else {
			aView.setMaxLines(1);
			aView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void setImage(ImageView aView, String url) {
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			aView.setImageBitmap(data);
			return;
		}
		
//		aView.setImageResource(mImageLoader.getLoadingId());
		aView.setImageBitmap(mImageLoader.getLoadingBitmap(mContext));
		mImageLoader.get(url, this);
	}
	
	@Override
	public void onClick(View v) {
		ProductModel model = (ProductModel)v.getTag();
		
		String id = model.getPropId();
		
		Intent intent = new Intent(mContext, ItemActivity.class);
		
		intent.putExtra(ItemActivity.KEY_PROP_ID, id);
		mContext.startActivity(intent);
//		RecommendEntity pEntity = (RecommendEntity)v.getTag();
//		if( null == pEntity )
//			return ;
//
//		
//		Bundle param = new Bundle();
//		//商品pid
//		param.putLong(ItemActivity.REQUEST_PRODUCT_ID, pEntity.getProductId());
//		//场景CHANNEL_ID
//		param.putInt(ItemActivity.REQUEST_CHANNEL_ID, pEntity.getChannelId());
//		
//		ToolUtil.startActivity(mContext, ItemActivity.class, param);
//		
//		String pageId="";
//		if( ModuleInfo.MODULE_ID_MORNING == mType ){
//			pageId = mContext.getString(R.string.tag_EventMorningActivity);
//		}else if( ModuleInfo.MODULE_ID_BLACK == mType ){
//			pageId = mContext.getString(R.string.tag_EventThhActivity);
//		}else if( ModuleInfo.MODULE_ID_WEEKEND == mType ){
//			pageId = mContext.getString(R.string.tag_EventWeekendActivity);
//		}else {
//			pageId = "1990"+ (70 + mType);
//		}
//		
//		//01011
//		String locationId = "";
//		int id = (Integer) v.getTag(R.layout.time_buy_item);
//		int line = id/COLUMN_SIZE +1;
//		if(line <10)
//			locationId ="0"+line+"01"+(id%COLUMN_SIZE+1);
//		else
//			locationId = ""+line+"01"+(id%COLUMN_SIZE+1);
//		ToolUtil.reportStatisticsClick(((YiQiangActivity)mContext).getActivityPageId(), ""+(30001+line),String.valueOf(pEntity.getProductId()));
//		ToolUtil.sendTrack(mContext.getClass().getName(), pageId, ItemActivity.class.getName(), mContext.getString(R.string.tag_ItemActivity), locationId, String.valueOf(pEntity.getProductId()));
	}
	
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		notifyDataSetChanged();
	}

	@Override
	public void onError(String strUrl) {
	}
	
	final class RowEntity {
		public void init(OnClickListener aListener, View aParent, int nContainer, int nImage, int nTitle, int nPrice, int nPriceWeChat) {
			mContainer = aParent.findViewById(nContainer);
			mContainer.setOnClickListener(aListener);
			
			mImage = (ImageView) aParent.findViewById(nImage);
//			mSaleOutImage = (ImageView) aParent.findViewById(nSaleOutImage);
//			mSaleNotImage = (ImageView) aParent.findViewById(nSaleNotImage);
			mTitle = (TextView) aParent.findViewById(nTitle);
			mPrice = (TextView) aParent.findViewById(nPrice);
			mWechatPrice = (TextView) aParent.findViewById(nPriceWeChat);
		}
		
		public void setProperties(ProductModel aEntity, int nPos) {
			mContainer.setTag(aEntity);
			mContainer.setTag(R.layout.listitem_2column, nPos);
			
			boolean isHasPromoWords = false;
			setImage(mImage, aEntity.getPropImg());
			
			List<Validate> validateList = aEntity.getValidateList();
			if(validateList != null) {
				Validate date = validateList.get(0);
				setName(mTitle, aEntity.getPropName() + "（" + date.getDay() + "天）", isHasPromoWords);
				setPrice(mPrice, Double.parseDouble(date.getCurPrice()));
				setPrice(mWechatPrice, Double.parseDouble(date.getWechatPrice()));
			}
		}
		
		View      mContainer;
		ImageView mImage;
		TextView  mTitle;
		TextView  mPrice;
		TextView  mWechatPrice;
//		ImageView	mSaleOutImage;
//		ImageView	mSaleNotImage;
	}
	
	// Class for view holder.
	final class ViewHolder
	{
		public ViewHolder() {
			mEntity1 = new RowEntity();
			mEntity2 = new RowEntity();
		}
		
		RowEntity mEntity1;
		RowEntity mEntity2;
	}

	private ImageLoader    mImageLoader;
	private Activity       mContext;
	private LayoutInflater mInflater;
	private List<ProductModel> mDataSource;
	private int            mType;
	
	private static final int COLUMN_SIZE = 2;
	//1:正在销售, 2:售謦, 3:尚未开始 
	private static final int SALE_ON = 1;
	private static final int SALE_OUT = 2;
	private static final int SALE_NOT = 3;
}
