package com.icson.order.shippingtype;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.icson.R;
import com.icson.address.AddressModel;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.shoppingcart.SubOrderModel;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class CombineTimeAvaiableView extends OrderBaseView<ShippingTypeTimeModel, ShippingTypeTimeModel> {
	
	private SplitShippingAdapter mSplitAdpter;
	private ArrayList<ShippingTypeTimeModel> selectModels;
	private int                              selectIdx;
	private LinearListView mSplitShippingView;
	private LinearLayout mShippingTimeOpt;
	private boolean  bCombineShipping;
	private ImageView mCheckCombineOpt;

	public CombineTimeAvaiableView(OrderConfirmActivity activity) {
		super(activity);
		bCombineShipping = true;
		
	}
	
//	private boolean isIcsonShippingType() {
//
//		return mShippingTypeModel.getId() == 1;
//	}

	public void getTimeSpan() {
		renderTimeAvaiable();
//		
//		mModel = null;
//		mIsRequestDone = false;
//		mShippingTypeModel = mActivity.getShippingTypeView().getModel();
//		AddressModel pAddressModel = mActivity.getOrderAddressView().getModel();
//		if (null == mShippingTypeModel || null == pAddressModel) {
//			goneTimeAvaiable();
//			return;
//		}
//
//		ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModelList = null;
//		if(isIcsonShippingType()) {
//
//			mShippingTypeTimeModelList = mShippingTypeModel.getSubShippingTypeModelList().get(0).getmShippingTypeTimeModels();
//			if (mShippingTypeTimeModelList == null || mShippingTypeTimeModelList.size() == 0) {
//				goneTimeAvaiable();
//				return;
//			}
//			if(null!=selectModels)
//			{
//				selectModels.clear();
//				for(int idx = 0; idx < mShippingTypeModel.getSubShippingTypeModelList().size();idx++ )
//				{
//					SubShippingTypeModel submodle = mShippingTypeModel.getSubShippingTypeModelList().get(idx);
//					selectModels.add(idx,submodle.getmShippingTypeTimeModels().get(0));
//				}
//			}
//			
//			ArrayList<ShippingTypeTimeModel> combineTimeModel = mShippingTypeModel.getCombineShippingTimeList();
//			
//			if(null==combineTimeModel || combineTimeModel.size()<=0)
//				mModel = mShippingTypeTimeModelList.get(0);
//			else
//				mModel = combineTimeModel.get(0);
//			renderTimeAvaiable();
//		} else {
//			ShippingTypeTimeModel thirdPartyShipTypeModel = mShippingTypeModel.getSubShippingTypeModelList().get(0).getPreShippingTypeTimeModels().get(0);
//			if(thirdPartyShipTypeModel != null) {
//				mActivity.findViewById(R.id.shipping_time_opt).setVisibility(View.GONE);
//				mActivity.findViewById(R.id.combine_ship_time).setVisibility(View.GONE);
//				mActivity.findViewById(R.id.split_ship_time).setVisibility(View.GONE);
//				TextField tv = (TextField) mActivity.findViewById(R.id.pre_ship_time);
//				tv.setVisibility(View.VISIBLE);
//				tv.setContent(getTimeLabel(thirdPartyShipTypeModel, false));
//			}
//		}
	}

	/**  
	* method Name:goneTimeAvaiable    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void goneTimeAvaiable() {
		mIsRequestDone = true;
//		mActivity.findViewById(R.id.shipping_time_opt).setVisibility(View.GONE);
		mActivity.findViewById(R.id.combine_ship_time).setVisibility(View.GONE);
		mActivity.findViewById(R.id.split_ship_time).setVisibility(View.GONE);
		mActivity.findViewById(R.id.pre_ship_time).setVisibility(View.GONE);
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_SHIPPING_TIME_VIEW);
	}

	// 配送时间
	private void renderTimeAvaiable() {
		showSplitShipping();
//		
//		mIsRequestDone = true;
//		ArrayList<ShippingTypeTimeModel> combineShipping = null;
//		ArrayList<SubShippingTypeModel>  subOrderTimeModel = null;
//		
//		if(null!=mShippingTypeModel)
//		{
//			combineShipping= mShippingTypeModel.getCombineShippingTimeList();
//			subOrderTimeModel = mShippingTypeModel.getSubShippingTypeModelList();
//		}
//		
//		if(null!= combineShipping && combineShipping.size()>0)
//		{
//			mActivity.findViewById(R.id.shipping_time_opt).setVisibility(View.VISIBLE);
//			if(null == mShippingTimeOpt)
//			{
//				mShippingTimeOpt = (LinearLayout) mActivity.findViewById(R.id.combine_shipping_container);
//				mCheckCombineOpt = (ImageView) mActivity.findViewById(R.id.check_btn);
//				mShippingTimeOpt.setOnClickListener(new View.OnClickListener() {
//					
//					@Override
//					public void onClick(View v) {
//						
//						bCombineShipping = !bCombineShipping;
//						if(bCombineShipping)
//						{
//							ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22004");
//							showCombineShipping();
//							mCheckCombineOpt.setImageResource(R.drawable.choose_radio_on);
//						}
//						else
//						{
//							ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22005");
//							showSplitShipping();
//							mCheckCombineOpt.setImageResource(R.drawable.choose_radio_off);
//						}
//					}
//				});
//				bCombineShipping =true;
//				mCheckCombineOpt.setImageResource(R.drawable.choose_radio_on);
//				showCombineShipping();
//			}
//			else
//			{
//				if(bCombineShipping)
//					showCombineShipping();
//				else
//					showSplitShipping();
//			}
//		}
//		else if(null!=subOrderTimeModel && subOrderTimeModel.size()> 1)
//		{
//			mActivity.findViewById(R.id.shipping_time_opt).setVisibility(View.GONE);
//			bCombineShipping =false;
//			showSplitShipping();
//		}
//		else 
//		{
//			bCombineShipping =true;
//			showNormalShipping();
//		}
	}

	/**  
	* method Name:showNormalShipping    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void showNormalShipping() {
		//option checkbox gone
//		mActivity.findViewById(R.id.shipping_time_opt).setVisibility(View.GONE);
		
		//shipTime with caption
		EditField shipTime = (EditField)mActivity.findViewById(R.id.combine_ship_time);
		shipTime.setVisibility(mModel == null ? View.GONE : View.VISIBLE);
		shipTime.setCaption(mActivity.getString(R.string.orderconfirm_ship_time_title));
		
		//show single shipping opt
		showCombineShipping();
	}

	/**  
	* method Name:showSplitShipping    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void showSplitShipping() {
		mActivity.findViewById(R.id.combine_ship_time).setVisibility(View.GONE);
		mActivity.findViewById(R.id.pre_ship_time).setVisibility(View.GONE);
		mActivity.findViewById(R.id.split_ship_time).setVisibility(View.VISIBLE);
		
		if(null == mSplitShippingView)
			initSplitShippingView();
		else
		{
			mSplitAdpter.resetSelectAndWholeModel(subOrders);
			mSplitAdpter.notifyDataSetChanged();
		}
	}

	/**  
	* method Name:showCombineShipping    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void showCombineShipping() {

		EditField shipTime = (EditField)mActivity.findViewById(R.id.combine_ship_time);
		shipTime.setVisibility(View.VISIBLE);
		shipTime.setContent(mModel == null ? "" : getTimeLabel(mModel, false));
		shipTime.setOnDrawableRightClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22003");
				selectShippingSpan();
			}
		});
		
		mActivity.findViewById(R.id.split_ship_time).setVisibility(View.GONE);
		mActivity.findViewById(R.id.pre_ship_time).setVisibility(View.GONE);

		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_SHIPPING_TIME_VIEW);
		
	}

	private ArrayList<SubOrderModel> subOrders;
	/**  
	* method Name:initSplitShippingView    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void initSplitShippingView() {
		
		subOrders = mActivity.getShoppingCartView().getModel().getSubOrders();
		
		mSplitShippingView = (LinearListView) mActivity.findViewById(R.id.split_ship_time);
		selectModels = new ArrayList<ShippingTypeTimeModel>();
		for(int idx = 0; idx < subOrders.size();idx++ )
		{
			selectModels.add(idx,subOrders.get(idx).mShippingTypeTimeModelList.get(0));
		}
		mSplitAdpter = new SplitShippingAdapter(mActivity,this, subOrders, selectModels);
	
		mSplitShippingView.setAdapter(mSplitAdpter);
	}

	public static String getTimeLabel(ShippingTypeTimeModel model, boolean showDetail) {
		String name = model.getName().replaceAll("^\\d+-(?:0)?(\\d+)-(?:0)?(\\d+)", "$1月$2日");

		return showDetail ? name.replace("上午", " 9:00-14:00").replace("下午", " 14:00-18:00").replace("晚上", " 18:00-22:00") : name.replace("上午", " 上午").replace("下午", " 下午").replace("晚上", " 晚上");

		/*
		String name = model.getName().replace("星期", "周");
		
		return showDetail ? name.replace("上午", " 9:00-14:00").replace("下午", " 14:00-18:00").replace("晚上", " 18:00-22:00") : name;
		*/
	}

	// 配送时间
	public void selectShippingSpan() {
		final ShippingTypeView mShippingTypeView = mActivity.getShippingTypeView();
		final ShippingTypeModel mShippingTypeModel = mShippingTypeView.getModel();

		if (mShippingTypeModel == null || mShippingTypeModel.getSubShippingTypeModelList().size() == 0 ) {
			UiUtils.makeToast(mActivity, "请先选择收货方式");
			return;
		}

		
		ArrayList<ShippingTypeTimeModel> combineTimeModel = mShippingTypeModel.getCombineShippingTimeList();
		
		if(null==combineTimeModel || combineTimeModel.size()<=0)
			combineTimeModel = mShippingTypeModel.getSubShippingTypeModelList().get(0).getmShippingTypeTimeModels();
		final ArrayList<ShippingTypeTimeModel> uinTimeModel = combineTimeModel;
		
		int selectedIndex = 0;
		ArrayList<String> names = new ArrayList<String>();
		for (ShippingTypeTimeModel model : uinTimeModel) {
			names.add(getTimeLabel(model, true));
			if (mModel == model) {
				selectedIndex = names.size() - 1;
			}
		}
		
		UiUtils.showListDialog(mActivity, mActivity.getString(R.string.orderconfirm_choose_shippingtime), (String[])names.toArray(new String[0]), selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				mModel = uinTimeModel.get(which);
				renderTimeAvaiable();
			}
		}, true);

	}

	public void destroy() {
		super.destroy();
	}

	@Override
	public void onSuccess(ShippingTypeTimeModel v, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Ajax ajax, Response response) {
		// TODO Auto-generated method stub

	}

	/**  
	* method Name:selectSubShippingSpan    
	* method Description:  
	* @param pos   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	public void selectSubShippingSpan(int pos){
		selectIdx = pos;
		ArrayList<SubOrderModel> subOrders = mActivity.getShoppingCartView().getModel().getSubOrders();
//		final ShippingTypeView mShippingTypeView = mActivity.getShippingTypeView();
//		final ShippingTypeModel mShippingTypeModel = mShippingTypeView.getModel();
//
//		if (mShippingTypeModel == null || mShippingTypeModel.getSubShippingTypeModelList().size() == 0) {
//			UiUtils.makeToast(mActivity, "请先选择收货方式");
//			return;
//		}

		final ArrayList<ShippingTypeTimeModel> shippingTypeTimeModels = subOrders.get(pos).mShippingTypeTimeModelList;//getSubShippingTypeModelList().get(pos);

		int selectedIndex = 0;
		ArrayList<String> names = new ArrayList<String>();
		ShippingTypeTimeModel amodel = selectModels.get(selectIdx);
		for (ShippingTypeTimeModel model : shippingTypeTimeModels) {
			names.add(getTimeLabel(model, true));
			if (amodel == model) {
				selectedIndex = names.size() - 1;
			}
		}
		
		UiUtils.showListDialog(mActivity, mActivity.getString(R.string.orderconfirm_choose_shippingtime), (String[])names.toArray(new String[0]), selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				ShippingTypeTimeModel amodel = shippingTypeTimeModels.get(which);
				selectModels.set(selectIdx, amodel);
				renderTimeAvaiable();
			}
		}, true);
		
	}

	public boolean isCombineShipping() {
		return bCombineShipping;
	}
	
	public ArrayList<ShippingTypeTimeModel> getSplitShippingModel()
	{
		return selectModels;
	}
}
