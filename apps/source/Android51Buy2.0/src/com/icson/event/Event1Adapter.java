package com.icson.event;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.IcsonProImgHelper;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class Event1Adapter extends BaseAdapter implements DestroyListener, ImageLoadListener, OnClickListener {

	private LayoutInflater mInflater;

	private ArrayList<EventProductModel> mProductModels;

	private ImageLoader mImageLoader;

	private BaseActivity activity;
	
	private int          payType;
	private static final int   mColumnSize     = 3;

	public Event1Adapter(BaseActivity activity, ArrayList<EventProductModel> dataSource, 
			int aPayType) {

		mInflater = LayoutInflater.from(activity);
		mProductModels = dataSource;
		this.activity = activity;

		this.payType = aPayType;
		mImageLoader = new ImageLoader(activity, Config.CHANNEL_PIC_DIR, true);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() 
	{
		final int nSize = (null != mProductModels ? mProductModels.size() : 0);
		return (nSize / mColumnSize + ((0 == nSize % mColumnSize) ? 0 : 1));
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
	public View getView(int position, View convertView, final ViewGroup parent) {
		
		position = position * mColumnSize;

		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.event_1_item, null);
			holder = new ItemHolder();
			holder.container1 = convertView.findViewById(R.id.event_1_container_1);
			holder.image1 = (ImageView) convertView.findViewById(R.id.event_1_image_1);
			holder.title1 = (TextView) convertView.findViewById(R.id.event_1_title_1);
			holder.price1 = (TextView) convertView.findViewById(R.id.event_1_price_1);
			holder.promo1 = (TextView) convertView.findViewById(R.id.event_1_promo_word_1);
			

			holder.container2 = convertView.findViewById(R.id.event_1_container_2);
			holder.image2 = (ImageView) convertView.findViewById(R.id.event_1_image_2);
			holder.title2 = (TextView) convertView.findViewById(R.id.event_1_title_2);
			holder.price2 = (TextView) convertView.findViewById(R.id.event_1_price_2);
			holder.promo2 = (TextView) convertView.findViewById(R.id.event_1_promo_word_2);

			holder.container3 = convertView.findViewById(R.id.event_1_container_3);
			holder.image3 = (ImageView) convertView.findViewById(R.id.event_1_image_3);
			holder.title3 = (TextView) convertView.findViewById(R.id.event_1_title_3);
			holder.price3 = (TextView) convertView.findViewById(R.id.event_1_price_3);
			holder.promo3 = (TextView) convertView.findViewById(R.id.event_1_promo_word_3);

			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		EventProductModel model1 = position < mProductModels.size() ? mProductModels.get(position) : null;

		EventProductModel model2 = (position + 1) < mProductModels.size() ? mProductModels.get(position + 1) : null;

		EventProductModel model3 = (position + 2) < mProductModels.size() ? mProductModels.get(position + 2) : null;

		if (model1 != null) {
			loadImage(holder.image1, model1.getProductCharId());
			holder.title1.setText(model1.getNameNoHTML());
			holder.price1.setText("¥" + ToolUtil.toPrice(model1.getShowPrice(), 2));
			holder.promo1.setText( Html.fromHtml( model1.getPromotionWord() ) );
			holder.container1.setTag(model1);
			holder.container1.setTag(R.layout.event_1_item,position);
			holder.container1.setOnClickListener(this);
		} 
		holder.container1.setVisibility( model1 == null ? View.INVISIBLE : View.VISIBLE);

		if (model2 != null) {
			loadImage(holder.image2, model2.getProductCharId());
			holder.title2.setText(model2.getNameNoHTML());
			holder.price2.setText("¥" + ToolUtil.toPrice(model2.getShowPrice(), 2));
			holder.promo2.setText( Html.fromHtml( model2.getPromotionWord() ) );
			holder.container2.setTag(model2);
			holder.container2.setTag(R.layout.event_1_item,position+1);
			holder.container2.setOnClickListener(this);
		} 
		holder.container2.setVisibility( model2 == null ? View.INVISIBLE : View.VISIBLE);

		if (model3 != null) {
			loadImage(holder.image3, model3.getProductCharId());
			holder.title3.setText(model3.getNameNoHTML());
			holder.price3.setText("¥" + ToolUtil.toPrice(model3.getShowPrice(), 2));
			holder.promo3.setText( Html.fromHtml( model3.getPromotionWord() ) );
			holder.container3.setTag(model3);
			holder.container3.setTag(R.layout.event_1_item,position+2);
			holder.container3.setOnClickListener(this);
		} 
		holder.container3.setVisibility( model3 == null ? View.INVISIBLE : View.VISIBLE);

		return convertView;
	}

	private void loadImage(ImageView view, String productCharId) {
		String url = IcsonProImgHelper.getAdapterPicUrl(productCharId, 110);
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mImageLoader.getLoadingId());
		view.setImageBitmap(mImageLoader.getLoadingBitmap(activity));
		mImageLoader.get(url, this);
	}

	@Override
	public void onDestroy() {
		mProductModels = null;
		mImageLoader.cleanup();
		mImageLoader = null;
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}

	private static class ItemHolder {
		View container1;
		ImageView image1;
		TextView title1;
		TextView price1;
		TextView promo1;

		View container2;
		ImageView image2;
		TextView title2;
		TextView price2;
		TextView promo2;

		View container3;
		ImageView image3;
		TextView title3;
		TextView price3;
		TextView promo3;
	}

	@Override
	public void onClick(View v) {
		Object pTag = v.getTag();

		if (pTag != null) {
			EventProductModel pModel = (EventProductModel) pTag;
			Bundle param = new Bundle();
			param.putLong(ItemActivity.REQUEST_PRODUCT_ID, pModel.getProductId());
			if(pModel.getChannelId() > 0) //场景多价商品
				param.putInt(ItemActivity.REQUEST_CHANNEL_ID, pModel.getChannelId());
			param.putInt(ItemActivity.REQUEST_PAY_TYPE, this.payType);
			ToolUtil.startActivity(activity, ItemActivity.class, param);
			
			String pageId = "199050";
			if(activity instanceof Event1Activity){
				pageId = "1990"+ (50+((Event1Activity)activity).getEventId());
			}
			//01011
			String locationId = "";
			int id = (Integer) v.getTag(R.layout.event_1_item);
			int line = id/mColumnSize +1;
			if(line <10)
				locationId ="0"+line+"01"+(id%mColumnSize+1);
			else
				locationId = ""+line+"01"+(id%mColumnSize+1);
			
			ToolUtil.sendTrack(activity.getClass().getName(), pageId, ItemActivity.class.getName(), activity.getString(R.string.tag_ItemActivity), locationId, String.valueOf(pModel.getProductId()));
		}
	}
}
