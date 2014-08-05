package com.icson.my.orderlist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.amap.CargoMapActivity;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.GroupOrderModel;
import com.icson.lib.model.OrderModel;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.pay.PayCore;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.PayFactory.PayResponseListener;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.my.OrderStatus;
import com.icson.my.main.MyIcsonActivity;
import com.icson.order.OrderControl;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity.DestroyListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MyOrderListAdapter extends BaseAdapter implements ImageLoadListener, OnClickListener, DestroyListener {
	private LayoutInflater mInflater;
	private ArrayList<OrderModel> dataSource;
	private ImageLoader mImageLoader;
	private MyIcsonActivity mActivity;
	private PayCore mPayCore = null;
	private OrderControl mOrderControl = null;
	private ListView mListView;
	private int margin_30xp;
	private int margin_15xp;
	private int margin_20xp;

	public static final int ITEM_VP_ORDER_INDEX = 0;
	public static final int CUSTOMIZED_ITEM_COUNT = 1;
	
	private static final int ACTION_NONE = 0;
	private static final int ACTION_PAY_NOW     = (ACTION_NONE + 1);
	private static final int ACTION_CMT_NOW     = (ACTION_NONE + 2);
	private static final int ACTION_CANCEL      = (ACTION_NONE + 3);
	
	private static final int VIEW_TYPE_VP_ORDER = 0;
	private static final int VIEW_TYPE_ORDER_ITEM = 1;
	private static final int ITEM_VIEW_TYPE_COUNT = 2;
	
	public MyOrderListAdapter(MyIcsonActivity activity, ListView listView, ArrayList<OrderModel> OrderModelList) {
		mActivity = activity;
		mInflater = LayoutInflater.from(mActivity);
		mListView = listView;
		
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
		return (null == dataSource  ? 0 : dataSource.size()) + CUSTOMIZED_ITEM_COUNT;
	}
	
	private boolean mIsScroll;
	public void setIsScrolling(boolean isScroll) {
		if(mIsScroll == isScroll) {
			return;
		}
		if(mImageLoader != null) {
			if(isScroll) {
				if(mUIHandler.hasMessages(MSG_RESUME_DECODE)) {
					mUIHandler.removeMessages(MSG_RESUME_DECODE);
				}
				mImageLoader.pauseDecode();
			} else {
				mUIHandler.sendEmptyMessageDelayed(MSG_RESUME_DECODE, 200);
			}
		}
		mIsScroll = isScroll;
	}
	
	@Override
	public int getItemViewType(int position) {
		if(position == ITEM_VP_ORDER_INDEX) {
			return VIEW_TYPE_VP_ORDER;
		}
		return VIEW_TYPE_ORDER_ITEM;
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return ITEM_VIEW_TYPE_COUNT;
	}

	@Override
	public Object getItem(int position) {
		// Check if position exceed array bound. For exception number 60508759
		if(position < CUSTOMIZED_ITEM_COUNT || position >= getCount()) {
			return null;
		}
		position = position - CUSTOMIZED_ITEM_COUNT;
		
		return (null == dataSource ? null : dataSource.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private View getCustomizeView(int position, View convertView, final ViewGroup parent) {
		
		// 跳转到虚拟订单页面的按钮
		if(position == ITEM_VP_ORDER_INDEX) {
			
			convertView = mInflater.inflate(R.layout.my_vp_orderlist_item_single, null);
			return convertView;
		}
		
		return null;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		View view = getCustomizeView(position, convertView, parent);
		if(view != null) {
			return view;
		}
		
		//普通订单
		final OrderModel mOrderModel = (OrderModel) getItem(position);
		final ItemHolder holder;
		
		if (convertView == null || convertView.getTag(R.id.holder_obj) == null)
		{
			convertView = mInflater.inflate(R.layout.my_orderlist_item, null);
			holder = new ItemHolder();
			//convertView视图初始化并保存到holder.
			findViewsInit(holder,convertView);
		} else 
		{
			holder = (ItemHolder) convertView.getTag(R.id.holder_obj);
		}
		
		convertView.setTag(R.id.holder_pos, position);
		
		//根据订单是不是拆单来设置对应的View样式
		checkPackageAndInitItemViews(holder,mOrderModel,position,convertView);
		
		//将mOrderModel的数据显示在Item View上
		fillDataToViews(holder,mOrderModel,position);
		
		//设置订单产品的图片
		setImage(holder, mOrderModel.getOrderProductModelList());
		
		// Update status for operate button.
		setActionStatus(mOrderModel, holder.btnOperate, position);
		if(mOrderModel.isPackage())
		{
			setPackageActionStatus(mOrderModel, holder.package_btn_opt, 
					holder.package_btn_cancel, holder.package_btn_container,
					position);
			holder.btnOperate.setVisibility(View.GONE);
		}

		// logistics information.
		loadLogisticsInfo(holder,mOrderModel);
		
		return convertView;
	}
	
	/**  
	* method Name:loadLogisticsInfo    
	* method Description:  
	* @param holder
	* @param mOrderModel
	* @return void    
	* @since  2.2.1  
	*/
	private void loadLogisticsInfo(ItemHolder holder, OrderModel mOrderModel) {
		// TODO Auto-generated method stub
		String strLogistics = mOrderModel.getLogistics();
		final boolean bHasLogistics = !TextUtils.isEmpty(strLogistics);
		holder.seperator2.setVisibility(bHasLogistics ? View.VISIBLE : View.GONE);
		holder.logistics.setVisibility(bHasLogistics ? View.VISIBLE : View.GONE);
		holder.logiLayout.setVisibility(bHasLogistics ? View.VISIBLE : View.GONE);
		holder.logiLayout.setOnClickListener(this);
		holder.logiTime.setText(mOrderModel.getLogiTime());
		holder.logiTime.setVisibility(bHasLogistics ? View.VISIBLE : View.GONE);
		final String strTel = mOrderModel.getTelephone();
		if( !TextUtils.isEmpty(strTel) ) {
			final int nIndex = strLogistics.indexOf(strTel);
			if( nIndex > 0 ) {
				final int nOffset = nIndex + strTel.length();
				final String strHtml = strLogistics.substring(0, nIndex) + "<font color=\"blue\">" + "<u>" + strTel + "</u></font>" + strLogistics.substring(nOffset);
				holder.logistics.setText(Html.fromHtml(strHtml));
				holder.logiLayout.setTag(R.id.holder_obj, strTel);
			} else {
				holder.logistics.setText(strLogistics);
				holder.logiLayout.setTag(R.id.holder_obj, "");
			}
		} else {
			holder.logistics.setText(strLogistics);
			holder.logiLayout.setTag(R.id.holder_obj, "");
		}
		
		final boolean bHasLoc = mOrderModel.hasLoc() && bHasLogistics;
		holder.mapicon.setVisibility(bHasLoc ? View.VISIBLE : View.GONE);
		holder.mapLabel.setVisibility(bHasLoc ? View.VISIBLE : View.GONE);
		holder.mapLayout.setOnClickListener(bHasLoc ? this : null);
		holder.mapLayout.setTag(R.id.holder_obj, (bHasLoc ? mOrderModel : null));
		
		// Set visibility for seperator
		holder.seperator3.setVisibility(bHasLoc ? View.VISIBLE : View.GONE);
		holder.mapArrow.setVisibility(bHasLoc ? View.VISIBLE : View.GONE);
	}

	/**  
	* method Name:fillDataToViews    
	* method Description:  
	* @param holder
	* @param mOrderModel
	* @param position
	* @return void    
	* @since  2.2.1  
	*/
	private void fillDataToViews(ItemHolder holder, OrderModel mOrderModel, int position) {

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
		holder.order_id.setText(orderId);
		holder.order_id_numbers.setText(" "+getItemCount(wholeNum));

		// 成交时间
		final String orderTime = "时间: " + ToolUtil.toDate(mOrderModel.getOrderDate() * 1000);

		// 总金额
		String price = "¥" + ToolUtil.toPrice(mOrderModel.getCash());
		if(mOrderModel.isLastPackage())
		{
			price = this.getWholeOrderPrice(mOrderModel,position);
		}
		
		if(mOrderModel.isPackage())
		{
			holder.time_package.setText(orderTime);
			holder.total_package.setText(price);
		}else
		{
			holder.time.setText(orderTime);
			holder.total.setText(price);
		}

		// 付款方式
		int PAY_LABEL_MAX = 5;
		String pay = mOrderModel.getPayTypeName();
		pay = pay.substring(0, Math.min(PAY_LABEL_MAX, pay.length())) + ( pay.length() > PAY_LABEL_MAX? "..." : "" );
		holder.pay_type_name.setText("(" + pay  + ")");
		
		if(mOrderModel.isPackage())
			holder.pay_type_name_package.setText("(" + pay  + ")");
		
		// Status
		holder.status.setText(mOrderModel.getStatus_name());
	}

	/**  
	* method Name:checkPackageAndInitItemViews    
	* method Description:  
	* @param holder
	* @param mOrderModel
	* @param position
	* @param convertView
	* @return void    
	* @since  2.2.1  
	*/
	private void checkPackageAndInitItemViews(ItemHolder holder,
			OrderModel mOrderModel, int position, View convertView) {
		// TODO Auto-generated method stub
		if(mOrderModel.isPackage())
		{
			//订单类型：拆单
			holder.subOrderid.setVisibility(View.VISIBLE);
			holder.subOrderid.setText(mActivity.getResources().getString(R.string.package_no_x, (mOrderModel.mPackageIdx+1)));
			holder.subOrderStatus.setVisibility(View.VISIBLE);
			holder.subOrderStatus.setText(mOrderModel.getStatus_name());
			
			holder.payrl.setVisibility(View.GONE);
			holder.payrl_package.setVisibility(mOrderModel.isLastPackage()? View.VISIBLE : View.GONE);
			holder.orderlist_seperator.setVisibility(mOrderModel.isLastPackage() ? View.VISIBLE : View.GONE);
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
					holder.orderlist_title.setVisibility(View.VISIBLE);
					holder.orderlist_seperator_top.setVisibility(View.VISIBLE);
					holder.backgroundView.setBackgroundResource(R.drawable.package_up_shape);
					holder.bottomline.setVisibility(View.VISIBLE);
					convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, 0);
				}
				else
				{
					//订单中间的包裹
					holder.orderlist_title.setVisibility(View.GONE);
					holder.orderlist_seperator_top.setVisibility(View.GONE);
					holder.backgroundView.setBackgroundResource(R.drawable.package_mid_shape);
					holder.bottomline.setVisibility(View.VISIBLE);
					convertView.setPadding(margin_30xp, 0, margin_30xp, 0);
				}
			}
			else
			{
				//如果下面的订单跟此订单    不是    同一个包裹
				if(0 == mOrderModel.mPackageIdx)
				{
					//是 第一个包裹
					holder.orderlist_title.setVisibility(View.VISIBLE);
					holder.orderlist_seperator_top.setVisibility(View.VISIBLE);
					holder.backgroundView.setBackgroundResource(R.drawable.i_my_orderlist_item_bg_shape);
					holder.bottomline.setVisibility(View.GONE);
					convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, margin_15xp);
				}
				else
				{
					//是 中间或者最后的包裹
					holder.orderlist_title.setVisibility(View.GONE);
					holder.orderlist_seperator_top.setVisibility(View.GONE);
					holder.backgroundView.setBackgroundResource(R.drawable.choose_btn_normal_shape);
					holder.bottomline.setVisibility(View.GONE);
					convertView.setPadding(margin_30xp, 0, margin_30xp, margin_15xp);
				}
			}
		}
		else
		{
			//订单类型：未拆单
			holder.orderlist_title.setVisibility(View.VISIBLE);
			holder.orderlist_seperator_top.setVisibility(View.VISIBLE);
			holder.package_btn_container.setVisibility(View.GONE);
			holder.backgroundView.setBackgroundResource(R.drawable.i_my_orderlist_item_bg_shape);
			holder.status.setVisibility(View.VISIBLE);
			holder.subOrderid.setVisibility(View.GONE);
			holder.subOrderStatus.setVisibility(View.GONE);
			holder.payrl.setVisibility(View.VISIBLE);
			holder.payrl_package.setVisibility(View.GONE);
			holder.bottomline.setVisibility(View.GONE);
			holder.orderlist_seperator.setVisibility(View.VISIBLE);
			convertView.setPadding(margin_30xp, margin_15xp, margin_30xp, margin_15xp);
		}
		
		holder.backgroundView.setPadding(margin_20xp,0, margin_20xp,0);
	}

	/**  
	* method Name:findViewsInit    
	* method Description:  
	* @param holder
	* @param convertView
	* @return void    
	* @since  2.2.1  
	*/
	private void findViewsInit(ItemHolder holder, View convertView) {
		// TODO Auto-generated method stub
		holder.order_id = (TextView) convertView.findViewById(R.id.orderlist_textview_order_id);
		holder.order_id_numbers = (TextView) convertView.findViewById(R.id.orderlist_textview_order_numbers);
		
		holder.time = (TextView) convertView.findViewById(R.id.orderlist_textview_time);
		holder.total = (TextView) convertView.findViewById(R.id.orderlist_textview_total);
		holder.time_package = (TextView) convertView.findViewById(R.id.orderlist_textview_time_package);
		holder.total_package = (TextView) convertView.findViewById(R.id.orderlist_textview_total_package);
		holder.pay_type_name = (TextView) convertView.findViewById(R.id.orderlist_textview_pay_type_name);
		holder.pay_type_name_package = (TextView) convertView.findViewById(R.id.orderlist_textview_pay_type_name_package);

		holder.status = (TextView) convertView.findViewById(R.id.orderlist_textview_status);
		holder.pic1 = (ImageView) convertView.findViewById(R.id.orderlist_pic_1);
		holder.pic2 = (ImageView) convertView.findViewById(R.id.orderlist_pic_2);
		holder.pic3 = (ImageView) convertView.findViewById(R.id.orderlist_pic_3);
		holder.btnOperate = (TextView)convertView.findViewById(R.id.orderlist_item_btn_operate);
		holder.bottomline = (View)convertView.findViewById(R.id.orderlist_seperator_bottom);
		//package Btns
		holder.package_btn_container = (LinearLayout) convertView.findViewById(R.id.orderlist_btns_container);
		holder.package_btn_opt = (TextView)convertView.findViewById(R.id.orderlist_package_item_opt);
		holder.package_btn_cancel = (TextView)convertView.findViewById(R.id.orderlist_package_item_cancel);
		//end
		
		holder.logistics = (TextView)convertView.findViewById(R.id.orderlist_item_logistics);
		holder.logiTime = (TextView)convertView.findViewById(R.id.orderlist_item_logitime);
		holder.logiLayout = convertView.findViewById(R.id.orderlist_item_logistics_layout);
		holder.mapicon = (ImageView) convertView.findViewById(R.id.orderlist_item_map);
		holder.mapLabel = (TextView)convertView.findViewById(R.id.orderlist_item_map_label);
		holder.seperator2 = convertView.findViewById(R.id.orderlist_seperator_2);
		holder.seperator3 = convertView.findViewById(R.id.orderlist_seperator_3);
		holder.mapLayout = convertView.findViewById(R.id.orderlist_item_map_layout);
		holder.mapArrow = (ImageView)convertView.findViewById(R.id.orderlist_item_map_arrow);
		
		holder.payrl = (RelativeLayout) convertView.findViewById(R.id.order_pay_info_layout);
		holder.payrl_package = (RelativeLayout) convertView.findViewById(R.id.order_pay_info_layout_package);
		
		holder.subOrderid = (TextView) convertView.findViewById(R.id.orderlist_textview_suborder_id);
		holder.subOrderStatus = (TextView) convertView.findViewById(R.id.orderlist_textview_suborder_status);
		
		holder.orderlist_seperator = (View)convertView.findViewById(R.id.orderlist_seperator_1);
		holder.backgroundView = (RelativeLayout)convertView.findViewById(R.id.orderlist_relative_order_background);
		holder.orderlist_title = (RelativeLayout) convertView.findViewById(R.id.orderlist_textview_line);
		
		holder.orderlist_seperator_top = (View) convertView.findViewById(R.id.orderlist_seperator_top);
		//holder.orderlist_seperator_bottom = (View) convertView.findViewById(R.id.orderlist_seperator_bottom);
		
		convertView.setTag(R.id.holder_obj, holder);
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

	/**  
	* method Name:getWholeOrderPrice    
	* method Description:  
	* @param mOrderModel
	* @return   
	* String  
	* @exception   
	* @since  1.0.0  
	*/
	private String getWholeOrderPrice(OrderModel mOrderModel, int nPos) {
		double total = mOrderModel.getCash();
		int posCus = nPos-1;
		if(posCus>=0 && !TextUtils.isEmpty(mOrderModel.getPackageOrderId()))
		{
			OrderModel checkItem = (OrderModel) getItem(posCus);
			while(null!=checkItem && checkItem.isPackage() &&
					mOrderModel.getPackageOrderId().equals(checkItem.getPackageOrderId()))
			{
				total += checkItem.getCash();
				posCus--;
				if(posCus < 0)
					break;
				checkItem = (OrderModel) getItem(posCus);
			}
		}
		
		return "¥" + ToolUtil.toPrice(total);
		
	}

	/**  
	* method Name:setPackageActionStatus    
	* method Description:  
	* @param mOrderModel
	* @param btn_operate_pack
	* @param position   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void setPackageActionStatus(OrderModel aModel,
			TextView aPayBtn, TextView aCancelBtn, LinearLayout aLayout, int nPos) {

		if( null == aPayBtn || null == aCancelBtn || null == aLayout)
			return ;
		
		if( null == aModel || aModel.forceHide || 
				!aModel.isPackage() || !aModel.isLastPackage()) {
			aLayout.setVisibility(View.GONE);
		}
		else if(aModel.isLastPackage())
		{
			int posCus = nPos-1;
			boolean needPayFlag = aModel.isNeedPay() && (null != (mPayCore = PayFactory.getInstance(mActivity, aModel.getPayType(), aModel.getOrderCharId(), false)));
			boolean needCancel = aModel.isCanCancel() && (OrderStatus.canCancel(aModel.getStatus()));
			OrderModel checkItem = (OrderModel) getItem(posCus);
			while(null!=checkItem && checkItem.isPackage() && 
					aModel.getPackageOrderId().equals(checkItem.getPackageOrderId()))
			{
				if(needPayFlag)
					needPayFlag = checkItem.isNeedPay() && (null != (mPayCore = PayFactory.getInstance(mActivity, checkItem.getPayType(), checkItem.getOrderCharId(), false)));
				if(needCancel)
					needCancel = checkItem.isCanCancel() && (OrderStatus.canCancel(checkItem.getStatus()));
				
				posCus--;
				if(posCus < 0)
					break;
				checkItem = (OrderModel) getItem(posCus);
			}
			
			if(!needPayFlag && !needCancel)
			{
				aLayout.setVisibility(View.GONE);
			}
			else
			{
				aLayout.setVisibility(View.VISIBLE);
				if(needPayFlag)
				{
					aPayBtn.setVisibility(View.VISIBLE);
					aPayBtn.setEnabled(needPayFlag);
					aPayBtn.setTag(R.id.holder_obj, aModel);
					aCancelBtn.setTag(R.id.holder_layout, aLayout);
					aPayBtn.setOnClickListener(this);
				}
				else
					aPayBtn.setVisibility(View.GONE);
				
				if(needCancel)
				{
					aCancelBtn.setVisibility(View.VISIBLE);
					aCancelBtn.setEnabled(needCancel);
					aCancelBtn.setTag(R.id.holder_obj, aModel);
					aCancelBtn.setTag(R.id.holder_layout, aLayout);
					aCancelBtn.setOnClickListener(this);
				}
				else
					aCancelBtn.setVisibility(View.GONE);
					
			}
		}
		
	}

	private void setActionStatus(OrderModel aModel, TextView aButton, int nPos) {
		if( null == aButton )
			return ;
		
		// Update padding information.
		final int paddingLeft = aButton.getPaddingLeft();
		final int paddingRight = aButton.getPaddingRight();
		
		int nAction = ACTION_NONE;
		if( null == aModel || aModel.forceHide || aModel.isPackage()) {
			aButton.setVisibility(View.GONE);
		} else {
			// 1. Check need pay.
			if( aModel.isNeedPay() && (null != (mPayCore = PayFactory.getInstance(mActivity, aModel.getPayType(), aModel.getOrderCharId(), false))) ) {
				aButton.setVisibility(View.VISIBLE);
				aButton.setText(R.string.btn_text_pay_now);
				aButton.setBackgroundResource(R.drawable.confirm_button_off);
				aButton.setTextColor(mActivity.getResources().getColor(R.color.white));
				nAction = ACTION_PAY_NOW;
			} else if( aModel.isCanCancel() && (OrderStatus.canCancel(aModel.getStatus())) ) {
				//取消订单
				aButton.setVisibility(View.VISIBLE);
				aButton.setText(R.string.btn_text_cancel_order);
				aButton.setBackgroundResource(R.drawable.item_detail_btn);
				aButton.setTextColor(mActivity.getResources().getColor(R.color.global_text_color));
				nAction = ACTION_CANCEL;
			} else if( aModel.isCanEvaluate() ) { //评论
				aButton.setVisibility(View.VISIBLE);
				aButton.setText(R.string.btn_text_comment_now);
				aButton.setBackgroundResource(R.drawable.button_blue);
				aButton.setTextColor(mActivity.getResources().getColor(R.color.white));
				nAction = ACTION_CMT_NOW;
			} else {
				aButton.setVisibility(View.GONE);
			}
		}
		
		aButton.setPadding(paddingLeft, 0, paddingRight, 0);
		aButton.setTag(R.id.holder_status, nAction);
		aButton.setTag(R.id.holder_pos, nPos);
		aButton.setTag(R.id.holder_obj, (ACTION_NONE == nAction ? null : aModel));
		aButton.setOnClickListener(ACTION_NONE == nAction ? null : this);
	}
	
	private String getItemCount(int nNum)
	{
		return mActivity.getString(R.string.item_count, nNum);
	}

	//private static final String TAG = "MyOrderListAdapter";
	private void setImage(ItemHolder holder, ArrayList<OrderProductModel> models) {
		final int PIC_COUNT = 3;
		if(null==models)
			return;
		
		if(null == mImageLoader)
			mImageLoader = new ImageLoader(mActivity, Config.MY_ORDERLIST_DIR, true);
		
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
		TextView order_id_numbers;
		// 成交时间
		TextView time;
		TextView time_package;
		// 总金额
		TextView total;
		TextView total_package;
		
		// 付款方式
		TextView pay_type_name;
		TextView pay_type_name_package;
		// Status
		TextView status;
		ImageView pic1;
		ImageView pic2;
		ImageView pic3;
		TextView btnOperate;
		TextView logistics = null;
		TextView logiTime = null;
		ImageView mapicon = null;
		View seperator2 = null;
		View seperator3 = null;
		TextView mapLabel = null;
		View mapLayout = null;
		View logiLayout = null;
		ImageView mapArrow = null;
		View bottomline = null;
		
		RelativeLayout payrl;
		RelativeLayout payrl_package;
		TextView       subOrderid;
		TextView       subOrderStatus;
		TextView package_btn_opt;
		TextView package_btn_cancel;
		LinearLayout package_btn_container;
		
		View orderlist_seperator;
		View orderlist_seperator_top;
		//View orderlist_seperator_bottom;
		RelativeLayout orderlist_title;
		RelativeLayout backgroundView;
		
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
//		notifyDataSetChanged();
		updateListItemByUrl(image, url);
	}
	
	private void updateListItemByUrl(Bitmap bmp, String url) {
		if(mListView == null) {
			return;
		}
		
		int first = getFirstUpdatePosition();  //只更新可见范围内的条目，避免不必要的更新
		int last = getLastUpdatePosition();
		
		for(int listItemPos = first; listItemPos < last; listItemPos++) {
			OrderModel orderModel = (OrderModel) getItem(listItemPos);
			if(orderModel == null) {
				continue;
			}
			
			ArrayList<OrderProductModel> proModelList = orderModel.getOrderProductModelList();
			if(proModelList != null) {
				for(int imageViewPos = 0; imageViewPos < proModelList.size(); imageViewPos++) {
					OrderProductModel proModel = proModelList.get(imageViewPos);
					if(proModel == null) {
						continue;
					}
					String proImgUrl = IcsonProImgHelper.getAdapterPicUrl(proModel.getProductCharId(), 95);
					if(proImgUrl != null && proImgUrl.equals(url)) { //找到URL图片对应的list item
						updateImageView(bmp, listItemPos, imageViewPos);
					}
				}
			}
		}
	}
	
	private int getFirstUpdatePosition() {
		int first = mListView.getFirstVisiblePosition();
		if(first < CUSTOMIZED_ITEM_COUNT) {
			first = CUSTOMIZED_ITEM_COUNT;
		} else if(first > CUSTOMIZED_ITEM_COUNT + 1) {
			first = first - 1; //从之前一个开始更新
		}
		return first;
	}
	
	private int getLastUpdatePosition() {
		int last = mListView.getLastVisiblePosition();
		if(last > getCount()) {
			last = getCount();
		}
		return last;
	}
	
	private void updateImageView(Bitmap bmp, int listItemPos, int imageViewPos) {
		View view = getListItem(listItemPos);
		if(view == null) {
			return;
		}
		
		Integer viewPos = (Integer) view.getTag(R.id.holder_pos);
		if(viewPos == null || viewPos.intValue() != listItemPos) {
			return;
		}
		
		ItemHolder holder = (ItemHolder) view.getTag(R.id.holder_obj);
		
		if(holder == null) {
			return;
		}
		ImageView imageView = imageViewPos == 0 ? holder.pic1 : (imageViewPos == 1 ? holder.pic2 : holder.pic3);
		imageView.setVisibility(View.VISIBLE);
		imageView.setImageBitmap(bmp);
	}
	
	private View getListItem(int position) {
		View listItem = null;
		
		for(int i = 0; i < mListView.getChildCount(); i++) {
			View view = mListView.getChildAt(i);
			if(view == null) {
				continue;
			}
			
			Integer viewPos = (Integer) view.getTag(R.id.holder_pos);
			if(viewPos == null || viewPos.intValue() != position) {
				continue;
			}
			
			listItem = view;
			break;
		}
		
		return listItem;
	}
	
	@Override
	public void onError(String strUrl) {
	}

	@Override
	public void onClick(View v) {
		if( R.id.orderlist_item_btn_operate == v.getId()) {
			switch( (Integer)v.getTag(R.id.holder_status) ) {
			case ACTION_PAY_NOW:
				payNow(v, (OrderModel)v.getTag(R.id.holder_obj));
				break;
				
			case ACTION_CMT_NOW:
				mActivity.onItemClick(null, null, (Integer) v.getTag(R.id.holder_pos) + 1, 0);
				break;
				
			case ACTION_CANCEL:
				cancelOrder(v, (OrderModel)v.getTag(R.id.holder_obj), false);
				break;
			}
		} else if( R.id.orderlist_item_map_layout == v.getId() ) {
			// Show map view.
			OrderModel pModel = (OrderModel)v.getTag(R.id.holder_obj);
			if( null != pModel ) {
				String strTelephone = TextUtils.isEmpty(pModel.getReceiverMobile()) ? pModel.getReceiverTel() : pModel.getReceiverMobile();
				CargoMapActivity.showMap(mActivity, pModel.getReceiver(), strTelephone, pModel.getReceiverAddress(), pModel.getOrderCharId());
			}
		} else if(R.id.orderlist_item_logistics_layout == v.getId()) {
			String strTel = (String)v.getTag(R.id.holder_obj);
			if( !TextUtils.isEmpty(strTel) ) {
				// Make phone call.
				Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strTel));
				AppUtils.checkAndCall(mActivity,pIntent);
			}
		} else if(R.id.orderlist_package_item_opt == v.getId()) {
			payWholePackageNow((LinearLayout)v.getTag(R.id.holder_layout), (OrderModel)v.getTag(R.id.holder_obj));
		}else if(R.id.orderlist_package_item_cancel == v.getId()) {
			cancelWholePackage((LinearLayout)v.getTag(R.id.holder_layout), (OrderModel)v.getTag(R.id.holder_obj),  false);
		}
		else {
			mActivity.onItemClick(null, null, (Integer) v.getTag(R.id.holder_pos) + 1, 0);
		}
	}
	
	/**
	 * 
	* method Name:cancelWholePackage    
	* method Description: Cancel whole Order (all package)   success -- > aLayout gone
	* @param aLayout
	* @param aModel
	* @param bConfirmed   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void cancelWholePackage(final LinearLayout aLayout, final OrderModel aModel, boolean bConfirmed) {
		if ( !bConfirmed ) {
			UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_order_cancel, R.string.btn_yes, R.string.btn_no, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int which) {
					if (which == AppDialog.BUTTON_POSITIVE) {
						cancelWholePackage(aLayout, aModel,true);
					}
				}
			});
			return;
		}
		
		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderDetailActivity), 
				mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderDetailActivity), "03012");
		
		// Get tag.
		final String pOrderId = aModel.getPackageOrderId();
		if(TextUtils.isEmpty(pOrderId))
		{
			UiUtils.makeToast(mActivity, R.string.params_error);
			return;
		}

		OnSuccessListener<JSONObject> success = new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				if (v.optInt("errno", -1) == 0) {
					aModel.forceHide = true;
					//whole pay + cancel layout gone
					aLayout.setVisibility(View.GONE);
					
					// Report for canceling order.
					StatisticsEngine.trackEvent(mActivity, "cancel_order", "orderId=" + pOrderId);
				} else {
					String data = v.optString("data", "");
					data = data.equals("") ? Config.NORMAL_ERROR : data;
					UiUtils.makeToast(mActivity, data);
				}
			}
		};

		mActivity.showProgressLayer("正在取消订单, 请稍候...");
		if( null == mOrderControl )
			mOrderControl = new OrderControl(mActivity);
		
		mOrderControl.orderCancel(pOrderId, true, success, mActivity);
	}
	
	private void cancelOrder(final View aView, final OrderModel aModel, boolean bConfirmed) {
		if ( !bConfirmed ) {
			UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_order_cancel, R.string.btn_yes, R.string.btn_no, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int which) {
					if (which == AppDialog.BUTTON_POSITIVE) {
						cancelOrder(aView, aModel, true);
					}
				}
			});
			return;
		}
		
		ToolUtil.sendTrack(mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderDetailActivity), 
				mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderDetailActivity), "03012");
		
		// Get tag.
		final String orderCharId = aModel.getOrderCharId();
		final String pOrderId = aModel.getPackageOrderId();
		

		OnSuccessListener<JSONObject> success = new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				mActivity.closeProgressLayer();
				if (v.optInt("errno", -1) == 0) {
					aModel.forceHide = true;
					aView.setVisibility(View.GONE);
					
					// Report for canceling order.
					StatisticsEngine.trackEvent(mActivity, "cancel_order", "orderId=" + 
							(null==pOrderId ? orderCharId :pOrderId ));
				} else {
					String data = v.optString("data", "");
					data = data.equals("") ? Config.NORMAL_ERROR : data;
					UiUtils.makeToast(mActivity, data);
				}
			}
		};

		mActivity.showProgressLayer("正在取消订单, 请稍候...");
		if( null == mOrderControl )
			mOrderControl = new OrderControl(mActivity);
		
		mOrderControl.orderCancel(pOrderId, aModel.isPackage(),
				success, mActivity);
	}
	
	/**
	 * 
	* method Name:payWholePackageNow    
	* method Description:  pay whole order 。success -- > aLayout gone
	* @param aLayout
	* @param aModel   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void payWholePackageNow(final LinearLayout aLayout, final OrderModel aModel) {
		//must reset
		mPayCore = PayFactory.getInstance(mActivity, aModel.getPayType(), aModel.getPackageOrderId()
				, false);
		if (mPayCore == null)
			return;

		mPayCore.setPayResponseListener(new PayResponseListener() {
			@Override
			public void onSuccess(String... message) {
				aModel.forceHide = true;
				aLayout.setEnabled(false);
				UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_pay_success, R.string.btn_ok);
			}

			@Override
			public void onError(String... message) {
				String str = ((message == null || message[0] == null) ? "未知错误" : message[0]);
				UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_pay_failed), str, R.string.btn_ok);
			}
		});

		mPayCore.submit();
	}
	
	private void payNow(final View aView, final OrderModel aModel) {
		//must reset
		mPayCore = PayFactory.getInstance(mActivity, aModel.getPayType(), aModel.getPackageOrderId()
				, false);
		if (mPayCore == null)
			return;

		mPayCore.setPayResponseListener(new PayResponseListener() {
			@Override
			public void onSuccess(String... message) {
				aModel.forceHide = true;
				aView.setVisibility(View.GONE);
				UiUtils.showDialog(mActivity, R.string.caption_hint, R.string.message_pay_success, R.string.btn_ok);
			}

			@Override
			public void onError(String... message) {
				String str = ((message == null || message[0] == null) ? "未知错误" : message[0]);
				UiUtils.showDialog(mActivity, mActivity.getString(R.string.caption_pay_failed), str, R.string.btn_ok);
			}
		});

		mPayCore.submit();
	}

	@Override
	public void onDestroy() {
		cleanUpBitmap();
		mOrderControl = null;
	}
	
	public void cleanUpBitmap()
	{
		if(null!=mImageLoader)
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}
	
	private static final int MSG_RESUME_DECODE = 1001;
	private Handler mUIHandler = new UIHandler(this);
	
	private static class UIHandler extends Handler {
		private final WeakReference<MyOrderListAdapter> mRef;
		public UIHandler(MyOrderListAdapter aRef) {
			mRef = new WeakReference<MyOrderListAdapter>(aRef);
		}
		
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
				case MSG_RESUME_DECODE: {
					MyOrderListAdapter parent = mRef.get();
					if(null==parent)
						return;
					ImageLoader loader = parent.mImageLoader;
					if(loader != null) {
						loader.resumeDecode();
					}
					break;
				}
			}
		}
	}
	
	
}
