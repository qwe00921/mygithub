package com.icson.my.collect;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.control.FavorControl;
import com.icson.lib.model.FavorProductModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class MyCollectAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener, OnClickListener {

	private LayoutInflater mInflater;
	private ArrayList<FavorProductModel> dataSource;
	private ImageLoader mAsyncImageLoader;
	private MyCollectActivity mActivity;
	private boolean mEditing = false;

	public MyCollectAdapter(BaseActivity activity, ArrayList<FavorProductModel> dataSource, FavorControl aFavorControl) {
		mActivity = (MyCollectActivity) activity;
		mInflater = LayoutInflater.from(mActivity);
		this.dataSource = dataSource;
		mAsyncImageLoader = new ImageLoader(mActivity, Config.MY_FAVORITY_DIR, true);
		
		activity.addDestroyListener(this);
	}
	
	public void setEditing(boolean bEditing, boolean bUpdateNow) {
		mEditing = bEditing;
		if( bUpdateNow )
			this.notifyDataSetChanged();
	}
	
	public boolean isEditing() {
		return mEditing;
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
			
			holder.delete = (ImageView)convertView.findViewById(R.id.my_collect_delete);
			holder.image = (ImageView) convertView.findViewById(R.id.list_image_pic);
			holder.name = (TextView) convertView.findViewById(R.id.list_textview_name);
			holder.promo = (TextView) convertView.findViewById(R.id.list_textview_promo);
			holder.show_price = (TextView) convertView.findViewById(R.id.list_textview_show_price);
			holder.mLayout = (RelativeLayout)convertView.findViewById(R.id.product_list_item);
		//	holder.discuss = (TextView) convertView.findViewById(R.id.list_textview_discuss);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		FavorProductModel model = dataSource.get(position);

		holder.delete.setVisibility(mEditing ? View.VISIBLE : View.GONE);
		holder.delete.setOnClickListener(this);
		holder.delete.setTag(R.id.holder_pos, position);
		holder.name.setText(Html.fromHtml(model.getName()));
		holder.promo.setText(model.getPromotionWord());
		holder.show_price.setText(mActivity.getString(R.string.rmb)+ ToolUtil.toPrice(model.getShowPrice(), 2));
		
		if(mEditing) {
			holder.mLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.white));
		}else{
			holder.mLayout.setBackgroundResource(R.drawable.global_white_shadow_click_state);
		}
		
		//赠品图标
		Drawable check = model.getGiftCount() > 0 ? mActivity.getResources().getDrawable(R.drawable.i_list_activity_gift) : null;
		if (check != null) {
			check.setBounds(0, 0, check.getMinimumWidth(), check.getMinimumHeight());
		}
		holder.promo.setCompoundDrawables(null, null, check, null);

		loadImage(holder.image, model.getAdapterProductUrl(90));
		
		//data prepare for context menu
		convertView.setTag(R.layout.list_item, model.getProductId());
		
		return convertView;
	}
	
	@Override
	public void onClick(View v) {
		if( (null != dataSource) && (R.id.my_collect_delete == v.getId()) ) {
			final int nPos = (Integer)v.getTag(R.id.holder_pos);
			
			FavorProductModel pToDelete = dataSource.get(nPos);
			String strMsg = "您希望从收藏中删除 '"+pToDelete.getName()+"' ？";
			UiUtils.showDialog(mActivity, mActivity.getString(R.string.del_favorite), strMsg,
					R.string.btn_delete, R.string.btn_cancel,
					new AppDialog.OnClickListener()
				{
					@Override
					public void onDialogClick(int nButtonId) {
						if(nButtonId == DialogInterface.BUTTON_POSITIVE)
						{
							mActivity.remove(nPos);
						}
					}//end of onDialogClick
				});
			
			
		}
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
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}

	private static class ItemHolder {
		ImageView delete;
		ImageView image;
		TextView name;
		TextView promo;
		TextView show_price;
		RelativeLayout mLayout;
	//	TextView discuss;
	}
}
