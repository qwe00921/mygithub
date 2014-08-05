package com.icson.my.orderdetail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.model.OrderFlowModel;

public class OrderFlowAdapter extends BaseAdapter {

	private OrderFlowModel mOrderFlowModel;

	private LayoutInflater mLayoutInflater;

	private OrderDetailActivity mActivity;
	public OrderFlowAdapter(OrderDetailActivity activity, OrderFlowModel model) {
		mOrderFlowModel = model == null ? new OrderFlowModel() : model;
		mLayoutInflater = activity.getLayoutInflater();
		mActivity = activity;
	}

	private boolean isLastPosition(int position) {
		return position == getCount() - 1;
	}

	@Override
	public int getCount() {
		return mOrderFlowModel.getItems() == null ? 0 : (mOrderFlowModel
				.getItems().size());
	}


	@Override
	public Object getItem(int position) {
		return mOrderFlowModel.getItems().get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view =  mLayoutInflater.inflate(R.layout.my_orderdetail_orderflow_item, null);
		TextView textViewTime = ((TextView) view.findViewById(R.id.orderdetail_textview_flow_time));
		TextView textViewCotnent = ((TextView) view.findViewById(R.id.orderdetail_textview_flow_content));
		/*if (isLastPosition(position) && haveTotalColumn()) {
			textViewCotnent.setVisibility(View.GONE);
			//LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			//textViewTime.setLayoutParams(lp);
			textViewTime.setText(Html.fromHtml("您的订单处理时间共<strong>" + mOrderFlowModel.getTotal() + "</strong>"));
			view.setBackgroundResource(R.drawable.wuliu_dot_past);
			return view;
		}*/
		OrderFlowModel.Item item = (OrderFlowModel.Item) getItem(position);
		textViewTime.setText(item.getTime());

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
			textViewCotnent.setMovementMethod(LinkMovementMethod.getInstance());
			textViewCotnent.setText(ss);
		} else {
			textViewCotnent.setText(content);
		}
		if(position == 0){
			textViewTime.setTextColor(mActivity.getResources().getColor(R.color.global_label));
			textViewCotnent.setTextColor(mActivity.getResources().getColor(R.color.global_label));
		}else{
			textViewTime.setTextColor(mActivity.getResources().getColor(R.color.global_gray));
			textViewCotnent.setTextColor(mActivity.getResources().getColor(R.color.global_gray));
		}

		/*if(position == 0 && mOrderFlowModel.isShowMap()){
			view.findViewById(R.id.orderdetail_textview_flow_location).setVisibility(View.VISIBLE);
			view.findViewById(R.id.orderdetail_textview_flow_location).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mActivity.startMap();
				}
			});
		}*/
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
