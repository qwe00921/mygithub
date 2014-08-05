package com.icson.postsale;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.postsale.PostSaleDetailModel.TwoColObject;
import com.icson.util.Log;

public class PostSaleHandleDetailAdapter extends BaseAdapter {
	
	public static final int VIEW_TYPE_FORM = 1001;
	public static final int VIEW_TYPE_METHOD_FORM = 1002;
	public static final int VIEW_TYPE_TEXTAREA = 1003;
	private static final String TAG = PostSaleHandleDetailAdapter.class.getSimpleName();
	private int mViewType;
	private Context mContext;
	private LayoutInflater mInflater;
	private List<? extends Object> mDataSource;
	
	public PostSaleHandleDetailAdapter(Context context, int viewType) {
		mViewType = viewType;
		mContext = context;

		mInflater = LayoutInflater.from(context);
	}
	
	public void setDataSource(List<? extends Object> dataSource) {
		mDataSource = dataSource;
	}

	@Override
	public int getCount() {
		if(mDataSource == null) {
			return 0;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		if(mDataSource == null) {
			return null;
		}
		return mDataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.listitem_2column_text, null);
		}
		if(convertView == null) {
			// inflater error;
			Log.e(TAG, "[getView] convertView is null!");
			return new View(mContext);
		}
		TextView leftTextView = (TextView) convertView.findViewById(R.id.textview_left);
		TextView rightTextView = (TextView) convertView.findViewById(R.id.textview_right);
		leftTextView.setVisibility(View.VISIBLE);
		
		Object item = getItem(position);
		switch(getItemViewType(position)) {
			case VIEW_TYPE_FORM: {
				if(item instanceof TwoColObject) {
					TwoColObject detailForm = (TwoColObject) item;
					leftTextView.setText(detailForm.getTitle());
					rightTextView.setText(detailForm.getValue());
				}
				break;
			}
			case VIEW_TYPE_METHOD_FORM: {
				if(item instanceof TwoColObject) {
					TwoColObject methodForm = (TwoColObject) item;
					leftTextView.setText(methodForm.getTitle());
					rightTextView.setText(methodForm.getValue());
				}
				break;
			}
			case VIEW_TYPE_TEXTAREA: {
				if(item instanceof String) {
					String textAreaContent = (String) item;
					rightTextView.setText(textAreaContent);
					leftTextView.setVisibility(View.GONE);
				}
				break;
			}
			default: {
				break;
			}
		}
		
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
		return mViewType;
	}
//	
//	private View getViewByType(int viewType) {
//		switch(viewType) {
//			case VIEW_TYPE_FORM: {
//				return getFormView();
//			}
//			case VIEW_TYPE_METHOD_FORM: {
//				return getMethodFormView();
//			}
//			case VIEW_TYPE_TEXTAREA: {
//				return getTextAreaView();
//			}
//			default: {
//				return null;
//			}
//		}
//		
//	}

//	private View getFormView() {
//		View view = null;
//		
//		
//		
//		return view;
//	}
//	
//	private View getMethodFormView() {
//		View view = null;
//		return view;
//	}
//	
//	private View getTextAreaView() {
//		View view = null;
//		return view;
//	}
	
	
}
