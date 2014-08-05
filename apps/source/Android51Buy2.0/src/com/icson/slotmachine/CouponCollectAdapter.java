package com.icson.slotmachine;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.lib.ui.UiUtils;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;

public class CouponCollectAdapter extends BaseAdapter{

	//private ImageLoader mImageLoader;

	private LayoutInflater mInflater;
	private BaseActivity   mActivity;
	ArrayList<BingoInfo>  mBingos;
	private ClipboardManager mCm;
	private String strPreCDKey;
	private final static String  qqCardUrl = "http://m.51buy.com/t/qqvip/qqvip.html?copyStr=";
	
	public CouponCollectAdapter(BaseActivity activity,ArrayList<BingoInfo>  aBingos,
			ClipboardManager aCM)
	{
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
		this.mBingos = aBingos;
		mCm = aCM;
		
		strPreCDKey = activity.getResources().getString(R.string.cdkey);
	}	

	/**
	 * 
	* method Name:resetModel    
	* method Description:  
	* @param aModels   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void resetModel(ArrayList<BingoInfo>  aBingos)
	{
		this.mBingos = aBingos;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		CouponGalleryItemHolder holder = null;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_coupon_collection, null);
			holder = new CouponGalleryItemHolder();
			holder.imgv  = (ImageView) convertView.findViewById(R.id.card_img);
			holder.imgv.setBackgroundResource(R.drawable.card_bg);
			holder.nameV = (TextView)convertView.findViewById(R.id.card_name);
			holder.cdkeyv= (TextView) convertView.findViewById(R.id.cdkey);
			holder.cpv =  (TextView) convertView.findViewById(R.id.copy_cdkey);
			holder.cpv.setText(	Html.fromHtml( "<u>" 
											+ mActivity.getResources().getString(R.string.copy_cdk)
											+ "</u>"));
			holder.cpv.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						UiUtils.makeToast(mActivity,R.string.preferences_copy_to_clipboard_title);
						mCm.setText((String)v.getTag());
					}
					return false;
				}});
			
			holder.checkv = (ImageView) convertView.findViewById(R.id.check_cdkey);
			holder.checkv.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{
						Bundle gjp = new Bundle();
						gjp.putString(HTML5LinkActivity.LINK_URL, CouponCollectAdapter.qqCardUrl +
							((String)v.getTag()));
						gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, "QQ体验卡");
						ToolUtil.startActivity(mActivity, HTML5LinkActivity.class, gjp);
					
						mActivity.finish();
					}
					return false;
				}});	
			convertView.setTag(holder);
		} else {
			holder = (CouponGalleryItemHolder) convertView.getTag();
		}

		BingoInfo couponItem = mBingos.get(position);
		
		
		holder.nameV.setText(couponItem.getBingoName());
		holder.cdkeyv.setText(strPreCDKey + couponItem.getCdkey());
		holder.cdkeyv.setVisibility(View.VISIBLE);
		holder.cpv.setVisibility(View.VISIBLE);
		holder.cpv.setTag(couponItem.getCdkey());
		holder.checkv.setTag(couponItem.getCdkey());
		holder.checkv.setVisibility(View.VISIBLE);
		return convertView;
	}
	
		

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return (null==mBingos) ? null : mBingos.get(position);
	}

	@Override
	public int getCount() {
		return (null==mBingos) ? 0 : mBingos.size();
	}
	
	
	private static class CouponGalleryItemHolder {
		ImageView imgv;
		TextView  nameV;
		TextView  cdkeyv;
		TextView  cpv;
		ImageView checkv;
	}
}
