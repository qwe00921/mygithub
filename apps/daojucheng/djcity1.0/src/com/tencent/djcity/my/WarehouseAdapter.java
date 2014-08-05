package com.tencent.djcity.my;

import com.tencent.djcity.R;
import java.util.ArrayList;

import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.activity.BaseActivity.DestroyListener;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class WarehouseAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private LayoutInflater mInflater;
	private ArrayList<WarehouseModel> mModels;
	private ImageLoader mImageLoader;
	private BaseActivity activity;
	
	public WarehouseAdapter(BaseActivity activity, ArrayList<WarehouseModel> dataSource) {
		mInflater = LayoutInflater.from(activity);
		mModels = dataSource;
		this.activity = activity;

		mImageLoader = new ImageLoader(activity, Config.CHANNEL_PIC_DIR, true);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() 
	{
		return null != mModels ? mModels.size() : 0;
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
		ItemHolder holder = null;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_my_warehouse, null);
			holder = new ItemHolder();
			holder.pImage = (ImageView) convertView.findViewById(R.id.good_image);
			holder.pName = (TextView) convertView.findViewById(R.id.good_name);
			holder.pClass = (TextView) convertView.findViewById(R.id.good_class);
			holder.pNum = (TextView) convertView.findViewById(R.id.good_num);

			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		if(null != mModels && 0 != mModels.size()) {
			WarehouseModel model = mModels.get(position);
			loadImage(holder.pImage, model.getGoodPicUrl());
			holder.pName.setText(model.getGoodName());
			holder.pClass.setText(model.getGoodClass());
			holder.pNum.setText(model.getQuantity()+"");
		}

		return convertView;
	}

	private void loadImage(ImageView view, String url) {
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
		view.setImageBitmap(mImageLoader.getLoadingBitmap(activity));
		mImageLoader.get(url, this);
	}
	
	@Override
	public void onDestroy() {
		mModels = null;
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
		ImageView pImage;
		TextView pName;
		TextView pClass;
		TextView pNum;

	}

}
