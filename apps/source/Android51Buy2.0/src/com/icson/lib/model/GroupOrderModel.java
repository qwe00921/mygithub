package com.icson.lib.model;

import java.util.ArrayList;

public class GroupOrderModel extends OrderModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ArrayList<GroupOrderModel> mSubOrders;

	public void addSubOrder(GroupOrderModel item)
	{
		if(null == mSubOrders)
			mSubOrders = new ArrayList<GroupOrderModel>();
		mSubOrders.add(item);
	}
	
	public ArrayList<GroupOrderModel> getSubOrders()
	{
		return mSubOrders;
	}
}