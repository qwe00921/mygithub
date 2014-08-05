package com.icson.item;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.ProductModel;
import com.icson.util.Config;
import com.icson.util.ImageLoader;
import com.icson.util.ImageLoadListener;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class ProductModelAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener{

	private BaseActivity mActivity;
    private ImageLoader mImageLoader;
    private ArrayList<ProductModel> models = new ArrayList<ProductModel>();

    public ProductModelAdapter(BaseActivity activity) {
    	mActivity = activity;
        mImageLoader = new ImageLoader(activity, Config.CHANNEL_PIC_DIR, true);
        
        mActivity.addDestroyListener(this);
    }
    
    public void setDataSource(ArrayList<ProductModel> _models) {
        models = _models;
    }

    public int getCount() {
        return models.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
        	LayoutInflater inflater = LayoutInflater.from(mActivity);
            convertView = inflater.inflate(R.layout.detail_product_item, null);
        }
        
        ImageView image = (ImageView) convertView.findViewById(R.id.event_3_image_1);
        TextView title = (TextView) convertView.findViewById(R.id.event_3_title_1);
        TextView price = (TextView) convertView.findViewById(R.id.event_3_price_1);

        ProductModel model = models.get(position);
        loadImage(image, model.getProductCharId());
		title.setText(model.getNameNoHTML());
		price.setText(mActivity.getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));

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
		view.setImageBitmap(mImageLoader.getLoadingBitmap(mActivity));
		mImageLoader.get(url, this);
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
		models = null;
		if( null != mImageLoader )
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}
}