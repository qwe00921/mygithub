package com.icson.postsale;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.postsale.SizeChangeNotifyingTextView.OnSizeChangedListener;

public class PostSaleLogAdapter extends BaseAdapter {

	private List<PostSaleLogModel> mLogList;

	private LayoutInflater mLayoutInflater;

	private Context mActivity;
	private String mStrCollapse;
	private String mStrShowAll;
	
	public PostSaleLogAdapter(Context context, List<PostSaleLogModel> logList) {
		mLogList = logList == null ? new ArrayList<PostSaleLogModel>() : logList;
		mLayoutInflater = LayoutInflater.from(context);
		mActivity = context;
		
		mStrCollapse = mActivity.getResources().getString(R.string.textview_collapse);
		mStrShowAll = mActivity.getResources().getString(R.string.textview_showall);
	}

	private boolean isLastPosition(int position) {
		return position == getCount() - 1;
	}

	@Override
	public int getCount() {
		return mLogList == null ? 0 : mLogList.size();
	}


	@Override
	public Object getItem(int position) {
		return mLogList == null ? null : mLogList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view =  mLayoutInflater.inflate(R.layout.postsale_detail_log_item, null);
		final TextView textViewTime = ((TextView) view.findViewById(R.id.postsale_textview_log_time));
		final SizeChangeNotifyingTextView textViewContent = ((SizeChangeNotifyingTextView) view.findViewById(R.id.postsale_textview_log_content));
		final TextView textViewShowAll = (TextView) view.findViewById(R.id.postsale_textview_showall);

		PostSaleLogModel item = (PostSaleLogModel) getItem(position);
		textViewTime.setText(item.getLogTime());

		String content = item.getContent();
		Matcher matcher = Pattern.compile("(?:^|[^0-9])(1[358]\\d{9})([^0-9]|$)").matcher(content);
		String phone = null;
		if (matcher.find()) {
			phone = matcher.group(1);
		}

		if (phone != null) {
			SpannableString ss = new SpannableString(content);
			ss.setSpan(new URLSpan("tel:" + phone), matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			ss.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(R.color.my_order_phone_color)),
					matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textViewContent.setMovementMethod(LinkMovementMethod.getInstance());
			textViewContent.setText(ss);
		} else {
			textViewContent.setText(content);
		}
		if(position == 0){
			textViewTime.setTextColor(mActivity.getResources().getColor(R.color.global_label));
			textViewContent.setTextColor(mActivity.getResources().getColor(R.color.global_label));
		}else{
			textViewTime.setTextColor(mActivity.getResources().getColor(R.color.global_gray));
			textViewContent.setTextColor(mActivity.getResources().getColor(R.color.global_gray));
		}

		textViewContent.setOnSizeChangedListener(new OnSizeChangedListener() {
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh) {
				int lineCount = textViewContent.getLineCount();
				if(lineCount > 3) {
					textViewContent.setMaxLines(3);
					textViewContent.setEllipsize(TruncateAt.END);
					textViewContent.requestLayout();
					textViewShowAll.setText(mStrShowAll);
					
					textViewShowAll.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							TextView view = (TextView) v;
							CharSequence text = view.getText();

							if(mStrCollapse.equals(text.toString())) {  //收起
								view.setText(mStrShowAll);
								textViewContent.setMaxLines(3);
								textViewContent.setEllipsize(TruncateAt.END);
							} else { //显示全部
								view.setText(mStrCollapse);
								textViewContent.setMaxLines(100);
							}
						}
					});
				} else {
					textViewShowAll.setVisibility(View.GONE);
				}
				
				textViewContent.setOnSizeChangedListener(null); //设为空之后，调用textViewContent.setMaxLines()后不会再收到onSizeChanged回调,避免多余调用
			}
		});
		
		if(getCount() >1){
			if( position == 0)
				view.setBackgroundResource(R.drawable.wuliu_line_now);
			else if( isLastPosition(position))
				view.setBackgroundResource(R.drawable.wuliu_dot_past);
			else
				view.setBackgroundResource(R.drawable.wuliu_line_past);
		}else{
			view.setBackgroundResource(R.drawable.wuliu_dot_now);
		}
		return view;
	}
	
}
