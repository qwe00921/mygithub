package com.tencent.djcity.my;

import java.util.ArrayList;

import com.tencent.djcity.lib.model.BaseModel;

public class OrderModel extends BaseModel {
	private ArrayList<OrderItemModel> mItemModels;
	private int mPageNum;
	private int mPageTotal;
	
	public ArrayList<OrderItemModel> getItemModels(){
		return mItemModels;
	}
	
	public int getPageTotal(){
		return mPageTotal;
	}
	
	public void  setItemModels(ArrayList<OrderItemModel> models){
		mItemModels = models;
	}
	
	public void setPageTotal(int total){
		 mPageTotal = total;
	}


}
