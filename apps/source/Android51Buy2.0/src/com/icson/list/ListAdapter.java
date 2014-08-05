package com.icson.list;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.SearchProductModel;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ListAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private ArrayList<SearchProductModel> dataSource;
	private ImageLoader mAsyncImageLoader;
	private BaseActivity activity;

	public ListAdapter(BaseActivity activity, ArrayList<SearchProductModel> dataSource) {
		mInflater = LayoutInflater.from(activity);
		this.dataSource = dataSource;
		this.activity = activity;
		mAsyncImageLoader = new ImageLoader(activity, true);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		return (null != dataSource ? dataSource.size() : 0);
	}

	@Override
	public Object getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.list_image_pic);
			holder.name = (TextView) convertView.findViewById(R.id.list_textview_name);
			holder.show_price = (TextView) convertView.findViewById(R.id.list_textview_show_price);
			holder.promo_word = (TextView) convertView.findViewById(R.id.list_textview_promo);
			holder.discuss = (TextView) convertView.findViewById(R.id.list_textview_discuss);
			holder.stockStatus = (ImageView) convertView.findViewById(R.id.stockStatus);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		SearchProductModel model = dataSource.get(position);

		holder.name.setText(Html.fromHtml(model.getProductName()));
		holder.show_price.setText( activity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));
		holder.promo_word.setText(model.getPromotionWord());
		if(0 == model.getDiscussCount()){
			holder.discuss.setText("暂无评论");
		}else{
			holder.discuss.setText(model.getDiscussCount() + "人评论");
		}
		
		Drawable check = ( true == model.getIsGift() ) ? activity.getResources().getDrawable(R.drawable.i_list_activity_gift) : null;
		if (check != null) {
			check.setBounds(0, 0, check.getMinimumWidth(), check.getMinimumHeight());
		}
		holder.promo_word.setCompoundDrawables(null, null, check, null);
		
		if(0 >= model.getOnlineQuantity())
		{
			holder.stockStatus.setImageResource(R.drawable.soldout);
			holder.stockStatus.setVisibility(View.VISIBLE);
			holder.stockStatus.bringToFront();
		}
		else if(0 != model.getReachable())
		{
			holder.stockStatus.setImageResource(R.drawable.noreachable);
			holder.stockStatus.setVisibility(View.VISIBLE);
			holder.stockStatus.bringToFront();
		}
		else
		{
			holder.stockStatus.setVisibility(View.GONE);
		}
		loadImage(holder.image, model.getAdapterProductUrl(80));
		
		return convertView;
	}

	private void loadImage(ImageView view, String url) {
		final Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mAsyncImageLoader.getLoadingId());
		view.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(activity));
		mAsyncImageLoader.get(url, this);
	}

	@Override
	public void onDestroy() {
		dataSource = null;
		mAsyncImageLoader.cleanup();
		mAsyncImageLoader = null;
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}
	
	private static class ItemHolder {
		TextView name;
		ImageView image;
		TextView show_price;
		TextView promo_word;
		TextView discuss;
		ImageView stockStatus;
	}
}
