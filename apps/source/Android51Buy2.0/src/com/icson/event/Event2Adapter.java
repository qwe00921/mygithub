package com.icson.event;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.event.Event2Model.Event2SubModel;
import com.icson.util.Config;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class Event2Adapter extends BaseAdapter implements DestroyListener, ImageLoadListener {

	private LayoutInflater mInflater;

	private ArrayList<Event2SubModel> mEvent2SubModels;

	private ImageLoader mImageLoader;

//	private int          payType;
	
	public Event2Adapter(BaseActivity activity, ArrayList<Event2SubModel> dataSource,
			int aPayType) {

		mInflater = LayoutInflater.from(activity);
		mEvent2SubModels = dataSource;
	//	this.payType = aPayType;
		mImageLoader = new ImageLoader(activity, Config.CHANNEL_PIC_DIR, true);

		activity.addDestroyListener(this);
	}

	@Override
	public int getCount() {
		return mEvent2SubModels.size();
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
			convertView = mInflater.inflate(R.layout.event_2_item, null);
			holder = new ItemHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.event_2_image);
			holder.title = (TextView) convertView.findViewById(R.id.event_2_title);
			holder.desc = (TextView) convertView.findViewById(R.id.event_2_desc);

			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		Event2SubModel model = mEvent2SubModels.get(position);

		loadImage(holder.image, model.getPicUrl());
		holder.title.setText(model.getTitle());
		holder.desc.setText(model.getDesc());

		return convertView;
	}

	private void loadImage(ImageView view, String url) {
		Bitmap data =mImageLoader.get(url);
		if(data != null){
			view.setImageBitmap(data);
			return;
		}
		
		view.setImageResource(mImageLoader.getLoadingId());
		mImageLoader.get(url, this);
	}

	@Override
	public void onDestroy() {
		mEvent2SubModels = null;
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
		ImageView image;
		TextView title;
		TextView desc;
	}
}
