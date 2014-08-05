package com.icson.more;

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
import com.icson.lib.model.ViewHistoryProductModel;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class HistoryAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private ArrayList<ViewHistoryProductModel> dataSource;
	private ImageLoader mAsyncImageLoader;
	private BaseActivity activity;

	public HistoryAdapter(BaseActivity activity, ArrayList<ViewHistoryProductModel> dataSource) {

		mInflater = LayoutInflater.from(activity);
		this.dataSource = dataSource;
		this.activity = activity;

		mAsyncImageLoader = new ImageLoader(activity, true);

		activity.addDestroyListener(this);
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
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		ViewHistoryProductModel model = dataSource.get(position);

		holder.name.setText(Html.fromHtml(model.getName()));
		holder.show_price.setText(activity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 1));
		holder.promo_word.setText(model.getPromotionWord());
		if(0 == model.getDiscussCount()){
			holder.discuss.setText("暂无评论");
		}else{
			holder.discuss.setText(model.getDiscussCount() + "人评论");
		}

		Drawable check = model.getGiftCount() > 0 ? activity.getResources().getDrawable(R.drawable.i_list_activity_gift) : null;
		if (check != null) {
			check.setBounds(0, 0, check.getMinimumWidth(), check.getMinimumHeight());
		}
		holder.promo_word.setCompoundDrawables(null, null, check, null);

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
		TextView name;
		ImageView image;
		TextView show_price;
		TextView promo_word;
		TextView discuss;
	}
}
