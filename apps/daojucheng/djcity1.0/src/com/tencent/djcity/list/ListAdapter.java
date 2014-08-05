package com.tencent.djcity.list;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.home.recommend.ProductModel.Validate;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.activity.BaseActivity.DestroyListener;

public class ListAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private List<ProductModel> dataSource;
	private ImageLoader mAsyncImageLoader;
	private BaseActivity activity;

	public ListAdapter(BaseActivity activity, List<ProductModel> dataSource) {
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
	public ProductModel getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(getItem(position).getPropId());
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new ItemHolder();
			holder.waterMark = (ImageView) convertView.findViewById(R.id.water_mark);
			holder.image = (ImageView) convertView.findViewById(R.id.list_image_pic);
			holder.name = (TextView) convertView.findViewById(R.id.list_textview_name);
			holder.tvPriceOld = (TextView) convertView.findViewById(R.id.list_textview_price_old);
			
			Paint pPaint = holder.tvPriceOld.getPaint();
			pPaint.setFlags(pPaint.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			
			holder.tvPriceQQ = (TextView) convertView.findViewById(R.id.list_textview_price_qq);
			holder.tvPriceWechat = (TextView) convertView.findViewById(R.id.list_textview_price_wechat);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		ProductModel model = dataSource.get(position);

		holder.name.setText(Html.fromHtml(model.getPropName()));
		
		Validate date = model.getValidateList().get(0);
		double oldPrice = Double.parseDouble(date.getOldPrice());
		double priceQQ = Double.parseDouble(date.getCurPrice());
		double priceWechat = Double.parseDouble(date.getWechatPrice());
		
		holder.tvPriceOld.setText(activity.getString(R.string.price_old) + activity.getString(R.string.rmb) + ToolUtil.toPrice(oldPrice, 2));
		holder.tvPriceQQ.setText(ToolUtil.toPrice(priceQQ, 2) + activity.getString(R.string.qb));
		holder.tvPriceWechat.setText(activity.getString(R.string.rmb) + ToolUtil.toPrice(priceWechat, 2));
		
		int resource = model.getWaterMarkResource();
		if(resource != 0) {
			holder.waterMark.setImageResource(resource);
		}
//		if(0 == model.getDiscussCount()){
//			holder.discuss.setText("暂无评论");
//		}else{
//			holder.discuss.setText(model.getDiscussCount() + "人评论");
//		}
		
		loadImage(holder.image, model.getPropImg());
		
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
		ImageView waterMark;
		TextView tvPriceOld;
		TextView tvPriceQQ;
		TextView tvPriceWechat;
	}
}
