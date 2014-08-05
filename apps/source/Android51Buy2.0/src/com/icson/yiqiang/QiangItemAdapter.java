package com.icson.yiqiang;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ProductModel;
import com.icson.qiang.QiangModel.QiangProductModel;
import com.icson.qiang.QiangTomorrowModel.QiangTomorrowProductModel;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class QiangItemAdapter extends BaseAdapter implements DestroyListener,
		ImageLoadListener {

	private LayoutInflater mInflater;

	private ArrayList<BaseModel> mQiangProductModels;

	private ImageLoader mImageLoader;

	private BaseActivity activity;
	int resId[] = { R.drawable.qiang1, R.drawable.qiang2, R.drawable.qiang3,
			R.drawable.qiang4, R.drawable.qiang5, R.drawable.qiang6,
			R.drawable.qiang7 };

	public QiangItemAdapter(BaseActivity activity,
			ArrayList<BaseModel> dataSource) {

		mInflater = LayoutInflater.from(activity);
		mQiangProductModels = dataSource;
		this.activity = activity;

		mImageLoader = new ImageLoader(activity, Config.QIANG_PIC_DIR, true);
		mImageLoader.setMaxCache(Config.MAX_GALLERY_CACHE*2);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		if(mQiangProductModels == null) {
			return 0;
		}
		return mQiangProductModels.size();
	}

	@Override
	public Object getItem(int position) {
		if(mQiangProductModels == null) {
			return null;
		}
		return mQiangProductModels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.qiang_list_item, null);
			holder = new ItemHolder();

			holder.label = (ImageView) convertView
					.findViewById(R.id.list_image_label);
			holder.image = (ImageView) convertView
					.findViewById(R.id.list_image_pic);
			holder.name = (TextView) convertView
					.findViewById(R.id.list_textview_name);
			holder.show_price = (TextView) convertView
					.findViewById(R.id.list_textview_show_price);

			holder.full = convertView.findViewById(R.id.qiang_view_full);
			holder.white = convertView.findViewById(R.id.qiang_view_white);

			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		BaseModel bModel = null;
		
		if(mQiangProductModels != null) {
			bModel = mQiangProductModels.get(position);
		}
		if (bModel instanceof QiangProductModel) {
			QiangProductModel model = (QiangProductModel) bModel;
			if(position >=0 && position < resId.length) {
				holder.label.setImageResource(resId[position]);
			}
			loadImage(holder.image, model.getProductCharId());
			holder.name.setText(Html.fromHtml(TextUtils.isEmpty(model.getPromotionWord()) ? model.getPromoName() : model.getPromotionWord() ));
			holder.show_price
					.setText(activity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));

			convertView.findViewById(R.id.qiang_view_kucun_container).setVisibility(View.VISIBLE);
			holder.full
					.setBackgroundColor(activity
							.getResources()
							.getColor(
									model.getSaleType() != ProductModel.SALE_AVAILABLE ? R.color.qiang_stock_zero
											: (model.getProgress() < 31 ? R.color.qiang_stock_less
													: R.color.qiang_stock_enough)));
			LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) holder.full
					.getLayoutParams();
			param.weight = model.getProgress();
			holder.full.setLayoutParams(param);
			
			LinearLayout.LayoutParams param1 = (LinearLayout.LayoutParams) holder.white
					.getLayoutParams();
			param1.weight = 100 - model.getProgress();
			holder.white.setLayoutParams(param1);
		}else if(bModel instanceof QiangTomorrowProductModel) {
			QiangTomorrowProductModel qtpModel = (QiangTomorrowProductModel)bModel;
			
			holder.label.setImageResource(R.drawable.qiang_off);
			loadImage(holder.image, qtpModel.getProductCharId());
			holder.name.setText(Html.fromHtml(qtpModel.getPromotionWord()));
			holder.show_price.setText("");
			
			convertView.findViewById(R.id.qiang_view_kucun_container).setVisibility(View.GONE);
		}
		return convertView;
	}

	private void loadImage(ImageView view, String productCharId) {
		String url = IcsonProImgHelper.getAdapterPicUrl(productCharId, 110);
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setScaleType(ScaleType.FIT_CENTER);
			view.setImageBitmap(data);
			return;
		}
		
		view.setImageBitmap(mImageLoader.getLoadingBitmap(activity));
		mImageLoader.get(url, this);
	}

	@Override
	public void onDestroy() {
		mQiangProductModels = null;
		if (null != mImageLoader) {
			mImageLoader.cleanup();
			mImageLoader = null;
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
		ImageView label;
		ImageView image;
		TextView show_price;
		View full;
		View white;
	}

}
