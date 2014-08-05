package com.icson.yiqiang;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.tuan.TuanModel.TuanProductModel;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class TuanItemAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {

	private LayoutInflater mInflater;

	private ArrayList<TuanProductModel> dataSource;

	private ImageLoader mAsyncImageLoader;
	private BaseActivity mActivity;

	public TuanItemAdapter(BaseActivity activity, ArrayList<TuanProductModel> dataSource) {
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
		this.dataSource = dataSource;
		mAsyncImageLoader = new ImageLoader(activity, true);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		return dataSource == null ? 0 : dataSource.size();
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
			convertView = mInflater.inflate(R.layout.tuan_list_item, null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.list_image_pic);
			holder.name = (TextView) convertView.findViewById(R.id.list_textview_name);
			holder.show_price = (TextView) convertView.findViewById(R.id.list_textview_show_price);
			holder.promo_word = (TextView) convertView.findViewById(R.id.list_textview_promo);
			holder.market_price = (TextView) convertView.findViewById(R.id.list_textview_market_price);
			holder.join_num = (TextView) convertView.findViewById(R.id.list_textview_join_num);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		TuanProductModel model = dataSource.get(position);

		holder.name.setText(Html.fromHtml(model.getName()));
		holder.show_price.setText( mActivity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));
		holder.promo_word.setText(model.getPromotionWord());
		holder.market_price.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(model.getMarketPrice(), 2));
		ToolUtil.setCrossLine(holder.market_price);
		holder.join_num.setText(model.getSaleCount() + "人参团");
		
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
		view.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(mActivity));
		mAsyncImageLoader.get(url, this);
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
	public void onLoaded(Bitmap image, String url) 
	{
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
		TextView market_price;
		TextView join_num;
	}
}
