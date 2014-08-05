package com.icson.order;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.GroupOrderModel;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.my.orderlist.VPOrderModel;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity.DestroyListener;

public class SimpleOrderListAdapter extends BaseAdapter implements ImageLoadListener, OnClickListener, DestroyListener {
	private LayoutInflater mInflater;
	private ArrayList<OrderModel> dataSource;
	private ImageLoader mImageLoader;
	private OrderPickListActivity mActivity;
	private int margin_30xp;
	private int margin_15xp;
	private int margin_20xp;
	
	public SimpleOrderListAdapter(OrderPickListActivity activity, ArrayList<OrderModel> OrderModelList) {
		mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
		
		String str_30xp = mActivity.getResources().getString(R.dimen.margin_size_30xp);
		String str_15xp = mActivity.getResources().getString(R.dimen.margin_size_15xp);
		String str_20xp = mActivity.getResources().getString(R.dimen.margin_size_20xp);
		
		margin_30xp = (int)(mActivity.getResources().getDisplayMetrics().density*
							Float.valueOf(str_30xp.substring(0, str_30xp.length()-2)));
		margin_15xp = (int)(mActivity.getResources().getDisplayMetrics().density*
					Float.valueOf(str_15xp.substring(0, str_15xp.length()-2)));
		margin_20xp = (int)(mActivity.getResources().getDisplayMetrics().density*
				Float.valueOf(str_20xp.substring(0, str_20xp.length()-2)));
		
		this.dataSource = OrderModelList;
		mImageLoader = new ImageLoader(mActivity, Config.MY_ORDERLIST_DIR, true);
		
		mActivity.addDestroyListener(this);
		
	}

	@Override
	public int getCount() {
		return (null == dataSource  ? 0 : dataSource.size());
	}

	@Override
	public Object getItem(int position) {
		// Check if position exceed array bound. For exception number 60508759
		if(position < 0 || position >= getCount()) {
			return null;
		}
		
		return (null == dataSource ? null : dataSource.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		final OrderModel mOrderModel = (OrderModel) getItem(position);
		
		if(mOrderModel instanceof VPOrderModel){
			//虚拟订单
			VPOrderModel mVPOrderModel = (VPOrderModel)mOrderModel;
			convertView = mInflater.inflate(R.layout.my_vp_orderlist_item, null);
			
			TextView order_id = (TextView) convertView.findViewById(R.id.orderlist_textview_order_id);
			TextView time = (TextView) convertView.findViewById(R.id.orderlist_textview_time);
			TextView total = (TextView) convertView.findViewById(R.id.orderlist_textview_total);
			TextView pay_type_name = (TextView) convertView.findViewById(R.id.orderlist_textview_pay_type_name);
			TextView status = (TextView) convertView.findViewById(R.id.orderlist_textview_status);
			ImageView pic1 = (ImageView) convertView.findViewById(R.id.orderlist_pic_1);
			TextView card = (TextView) convertView.findViewById(R.id.orderlist_tv_Card);
			TextView title = (TextView) convertView.findViewById(R.id.orderlist_tv_title);
			TextView phone = (TextView) convertView.findViewById(R.id.orderlist_tv_phone);
			
			convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, margin_15xp);

			order_id.setText("订单号: " + mOrderModel.getOrderCharId()+ " " + getItemCount(1));
			// 总金额
			total.setText(Html.fromHtml("总额: <font color=\"#e01e1e\">¥" + ToolUtil.toPrice(mOrderModel.getOrderCost()) + "</font>"));
			pay_type_name.setText("("+mVPOrderModel.getPayTypeName()+")");
			status.setText(mVPOrderModel.getStatus_name());
			// 成交时间
			time.setText("时间: " + ToolUtil.toDate(mVPOrderModel.getOrderDate() * 1000));
			
			pic1.setVisibility(View.VISIBLE);
			if(mVPOrderModel.getProduct_list_str().contains("移动")){
				pic1.setImageResource(R.drawable.chinamobile);
			}else if(mVPOrderModel.getProduct_list_str().contains("联通")){
				pic1.setImageResource(R.drawable.chinauincom);
			}else if(mVPOrderModel.getProduct_list_str().contains("电信")){
				pic1.setImageResource(R.drawable.chinatelcom);
			}
			
			card.setText((int)mVPOrderModel.getCard_money()/100 +"元");
			title.setText(mVPOrderModel.getProduct_list_str());
			phone.setText("手机号码:"+mVPOrderModel.getReceiver());
			
			return convertView;
		}
		//普通订单
		
		final ItemHolder holder;

		if (convertView == null || convertView.getTag(R.id.holder_obj) == null) {
			convertView = mInflater.inflate(R.layout.simple_orderlist_item, null);
			holder = new ItemHolder();
			holder.order_id = (TextView) convertView.findViewById(R.id.simpleorderlist_textview_order_id);
			holder.status = (TextView) convertView.findViewById(R.id.simpleorderlist_textview_status);
			holder.pic1 = (ImageView) convertView.findViewById(R.id.simpleorderlist_pic_1);
			holder.pic2 = (ImageView) convertView.findViewById(R.id.simpleorderlist_pic_2);
			holder.pic3 = (ImageView) convertView.findViewById(R.id.simpleorderlist_pic_3);

			convertView.setTag(R.id.holder_obj, holder);
			convertView.setTag(R.id.holder_pos, position);
		} else {
			holder = (ItemHolder) convertView.getTag(R.id.holder_obj);
		}
		
		RelativeLayout backgroundView = (RelativeLayout)convertView.findViewById(R.id.simpleorderlist_relative_order_background);

		
		if(mOrderModel.isPackage())
		{
			//订单类型：拆单
			holder.status.setVisibility(View.GONE);

			OrderModel nextModel = null;
			if(position < getCount()-1)
			{
				  nextModel = (OrderModel) getItem(position+1);
			}
			if(nextModel !=null && !TextUtils.isEmpty(nextModel.getPackageOrderId()) &&
				nextModel.getPackageOrderId().equals(mOrderModel.getPackageOrderId()))
			{	
				//如果下面的订单跟此订单    是    同一个包裹
				if(0 == mOrderModel.mPackageIdx){
					//订单第一个包裹
					convertView.findViewById(R.id.simpleorderlist_textview_line).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.simpleorderlist_seperator_top).setVisibility(View.VISIBLE);
					backgroundView.setBackgroundResource(R.drawable.package_up_shape);
					backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
					convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, 0);
				}
				else
				{
					//订单中间的包裹
					convertView.findViewById(R.id.simpleorderlist_textview_line).setVisibility(View.GONE);
					convertView.findViewById(R.id.simpleorderlist_seperator_top).setVisibility(View.GONE);
					backgroundView.setBackgroundResource(R.drawable.package_mid_shape);
					backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
					convertView.setPadding(margin_30xp, 0, margin_30xp, 0);
				}
			}
			else
			{
				//如果下面的订单跟此订单    不是    同一个包裹
				if(0 == mOrderModel.mPackageIdx)
				{
					//是 第一个包裹
					convertView.findViewById(R.id.simpleorderlist_textview_line).setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.simpleorderlist_seperator_top).setVisibility(View.VISIBLE);
					backgroundView.setBackgroundResource(R.drawable.i_my_orderlist_item_bg_shape);
					backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
					convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, margin_15xp);
				}
				else
				{
					//是 中间或者最后的包裹
					convertView.findViewById(R.id.simpleorderlist_textview_line).setVisibility(View.GONE);
					convertView.findViewById(R.id.simpleorderlist_seperator_top).setVisibility(View.GONE);
					backgroundView.setBackgroundResource(R.drawable.choose_btn_normal_shape);
					backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
					convertView.setPadding(margin_30xp, 0, margin_30xp, margin_15xp);
				}
			}
		}
		else
		{
			//订单类型：未拆单
			convertView.findViewById(R.id.simpleorderlist_textview_line).setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.simpleorderlist_seperator_top).setVisibility(View.VISIBLE);
			backgroundView.setBackgroundResource(R.drawable.i_my_orderlist_item_bg_shape);
			backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
			holder.status.setVisibility(View.VISIBLE);
			convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, margin_15xp);
		}
		
		
		
		String orderId = "订单号: ";
		if(mOrderModel.isPackage())
			orderId = "订单号: " + mOrderModel.getPackageOrderId();
		else
			orderId = "订单号: " + mOrderModel.getOrderCharId();

		// 订单号
		int wholeNum = mOrderModel.getBuyNum();
		if(mOrderModel.isPackage() && 0==mOrderModel.mPackageIdx)
		{
			wholeNum = getWholeOrderBuyNum(mOrderModel,position);
		}
		orderId += "  " + "<font color=\"#666666\">" + getItemCount(wholeNum) + "</font>";
		holder.order_id.setText(Html.fromHtml(orderId));

		// 付款方式
		int PAY_LABEL_MAX = 5;
		String pay = mOrderModel.getPayTypeName();
		pay = pay.substring(0, Math.min(PAY_LABEL_MAX, pay.length())) + ( pay.length() > PAY_LABEL_MAX? "..." : "" );

		// Status
		holder.status.setText(Html.fromHtml(getStatusHTML(mOrderModel)));
		setImage(holder, mOrderModel.getOrderProductModelList());

		return convertView;
	}
	
	/**  
	* method Name:getWholeOrderBuyNum    
	* method Description:  
	* @param mOrderModel
	* @param position
	* @return   
	* int  
	* @exception   
	* @since  1.0.0  
	*/
	private int getWholeOrderBuyNum(OrderModel mOrderModel, int nPos) {
		int totalNum  = mOrderModel.getBuyNum();
		int posCus = nPos+1;
		if(posCus >= getCount() || TextUtils.isEmpty(mOrderModel.getPackageOrderId()))
			return totalNum;
		
		OrderModel checkItem = (OrderModel) getItem(posCus);
		while(null!=checkItem && checkItem.isPackage() && checkItem.getPackageOrderId().equals(mOrderModel.getPackageOrderId()))
		{
			totalNum += checkItem.getBuyNum();
			posCus++;
			if(posCus >= getCount())
				break;
			checkItem = (OrderModel) getItem(posCus);
		}
		return totalNum;
		
	}

	private String getItemCount(int nNum)
	{
		return mActivity.getString(R.string.item_count, nNum);
	}

	private void setImage(ItemHolder holder, ArrayList<OrderProductModel> models) {
		final int PIC_COUNT = 3;
		if(null==models)
			return;
		
		for (int i = 0, len = models.size(); i < PIC_COUNT; i++) {

			ImageView view = i == 0 ? holder.pic1 : (i == 1 ? holder.pic2 : holder.pic3);
			//for layout_weight = 1, use View.INVISIBLE instead.
			view.setVisibility( ( i > len - 1 ) ? View.INVISIBLE : View.VISIBLE);
			if (i < len) {
				OrderProductModel model = models.get(i);
				String url = IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 95);
				Bitmap data = mImageLoader.get(url);
				view.setImageBitmap(data != null ? data : ImageHelper.getResBitmap(mActivity, mImageLoader.getLoadingId()));
				if (data == null) {
					mImageLoader.get(url, this);
				}
			}
		}
	}

	public static String getStatusHTML( GroupOrderModel mOrderModel ) {
//		int status = mOrderModel.getStatus();
//		String color = status < OrderStatus.ORIGINAL ? "#222222" : (status == OrderStatus.OUTSTOCK ? "#5fb840" : "#fdc142");
		String color = "#222222";
		return "<font color=\"" + color + "\">" + mOrderModel.getStatus_name() + "</font>";
	}
	
	public static String getStatusHTML( OrderModel mOrderModel ) {
//		int status = mOrderModel.getStatus();
//		String color = status < OrderStatus.ORIGINAL ? "#222222" : (status == OrderStatus.OUTSTOCK ? "#5fb840" : "#fdc142");
		String color = "#222222";
		return "<font color=\"" + color + "\">" + mOrderModel.getStatus_name() + "</font>";
	}

	private static class ItemHolder {
		// 订单号
		TextView order_id;
		// Status
		TextView status;
		ImageView pic1;
		ImageView pic2;
		ImageView pic3;
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
	}

	@Override
	public void onClick(View v) {
		mActivity.onItemClick(null, null, (Integer) v.getTag(R.id.holder_pos) + 1, 0);
	}

	@Override
	public void onDestroy() {
		if(null!=mImageLoader)
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}

}
