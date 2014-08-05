package com.icson.item;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.ProductOptionDetailModel;
import com.icson.lib.model.ProductOptionModel;
import com.icson.lib.ui.FlowLayout;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Log;

public class ItemTabDetailOptionAdapter extends BaseAdapter {

	private List<ProductOptionModel> mOptionModelList;
	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private static final String TAG = "ItemTabDetailOptionAdapter";
	private OnOptionItemSelectListener mOnOptionItemSelectListener;
	private long mProductId;
	
	public ItemTabDetailOptionAdapter(Context context, OnOptionItemSelectListener listener) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
		mOnOptionItemSelectListener = listener;
	}
	
	public void setDataSource(List<ProductOptionModel> optionModelList, long id) {
		mProductId = id;
		mOptionModelList = optionModelList;
	}
	
	@Override
	public int getCount() {
		if(mOptionModelList == null) {
			Log.e(TAG, "[getCount] optionModelList is null");
			return 0;
		}
		return mOptionModelList.size();
	}

	@Override
	public ProductOptionModel getItem(int position) {
		if(mOptionModelList == null || position < 0 || position >= mOptionModelList.size()) {
			Log.e(TAG, "[getItem] mOptionModelList is null or IndexOutOfBounds");
			return null;
		}
		return mOptionModelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	private final static int MAX_CELLS = 3;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.item_tab_detail_option_item, null);
		}
		
		final ProductOptionModel model = getItem(position);
		
		FlowLayout sizes_LinearLayout = (FlowLayout) convertView
				.findViewById(R.id.item_linear_attrs);
		final List<ProductOptionDetailModel> sizes = model.getProductOptionDetailModelList();
		
		convertView.findViewById(R.id.item_detail_option_block).setVisibility(
				sizes != null && sizes.size() > 0 ? View.VISIBLE : View.GONE);
		
//		if(position % 2 == 0) {
//			convertView.findViewById(R.id.item_detail_option_block).setBackgroundColor(0xff2fcef3);
//		} else {
//			convertView.findViewById(R.id.item_detail_option_block).setBackgroundColor(0xfff3c6ef);
//		}
		TextView longTitle = (TextView) convertView.findViewById(R.id.item_detail_textview_attr_label_long);
		TextView shortTitle = (TextView) convertView.findViewById(R.id.item_detail_textview_attr_label_short);
		String title = model.getName();
		if(title.length() <= 2) {
			longTitle.setVisibility(View.GONE);
			shortTitle.setVisibility(View.VISIBLE);
			shortTitle.setText(title);
		} else {
			shortTitle.setVisibility(View.INVISIBLE);
			longTitle.setVisibility(View.VISIBLE);
			longTitle.setText(title);
		}

		if (null == sizes)
			return convertView;
		
		if (sizes.size() > MAX_CELLS) {
			sizes_LinearLayout.setVisibility(View.GONE);
			View container = convertView.findViewById(R.id.item_detail_linear_attr_spinner);
			container.setVisibility(View.VISIBLE);
			
			ProductOptionDetailModel optionDetailModel = null;  
			for (ProductOptionDetailModel detailModel : sizes) {
				if (detailModel.getSelectStatus() == ProductOptionDetailModel.STATUS_SELECTED) {
					optionDetailModel = detailModel;
				}
			}
			optionDetailModel = optionDetailModel == null ? sizes.get(0) : optionDetailModel;
			TextView tv = ((TextView) convertView.findViewById(R.id.item_detail_textview_attr_value));//.setText(optionDetailModel.getName());
			tv.setText(optionDetailModel.getName());
			container.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final List<ProductOptionDetailModel> mProductOptionSizeModelList = sizes;

					if (mProductOptionSizeModelList.size() == 0)
						return;

					int checkedItem = -1;
					String[] names = new String[mProductOptionSizeModelList.size()];
					for (int i = 0, len = names.length; i < len; i++) {
						ProductOptionDetailModel model = mProductOptionSizeModelList.get(i);
						names[i] = model.getName();
						if (model.getSelectStatus() == ProductOptionDetailModel.STATUS_SELECTED)
							checkedItem = i;
					}

					UiUtils.showListDialog(mContext,
							mContext.getString(R.string.item_choose_size), names,
							checkedItem, new RadioDialog.OnRadioSelectListener() {
								@Override
								public void onRadioItemClick(int which) {
									long selId = mProductOptionSizeModelList.get(which)
											.getProductId();
									if (selId != mProductId) {
										if(mOnOptionItemSelectListener != null) {
											mOnOptionItemSelectListener.onSelected(selId);
										}
									}
								}
							}, true);

				}
			});

		} else {
			sizes_LinearLayout.setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.item_detail_linear_attr_spinner)
					.setVisibility(View.GONE);
			for (final ProductOptionDetailModel size : sizes) {
				
				TextView value = (TextView) mLayoutInflater.inflate(R.layout.view_btn, null);

				value.setText(size.getName());
				value.setSingleLine(true);
				if (size.getSelectStatus() == ProductOptionDetailModel.STATUS_SELECTED) {
					value.setBackgroundResource(R.drawable.choose_btn_focus);
				} else if(size.getSelectStatus() == ProductOptionDetailModel.STATUS_DISELECTED) {
					value.setBackgroundResource(R.drawable.choose_btn_normal);
				}  else {
					value.setBackgroundResource(R.drawable.choose_btn_normal);
					value.setTextColor(mContext.getResources().getColor(R.color.global_gray));
					value.setEnabled(false);
				}

				LayoutParams params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(3, 0, 3, 0);
				value.setLayoutParams(params);
				value.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						long selId = size.getProductId();
						if (selId != mProductId) {
							if(mOnOptionItemSelectListener != null) {
								mOnOptionItemSelectListener.onSelected(selId);
							}
//							firstExec = true;
//							mActivity.init(selId);
						}

					}
				});
				sizes_LinearLayout.addView(value);
			}
		}
		
		return convertView;
	}
	
	public interface OnOptionItemSelectListener {
		public void onSelected(long id);
	}

}
