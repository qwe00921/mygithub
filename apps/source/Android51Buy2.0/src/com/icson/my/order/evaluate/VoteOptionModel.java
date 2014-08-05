package com.icson.my.order.evaluate;

import java.io.Serializable;

import com.icson.lib.model.BaseModel;

@SuppressWarnings("serial")
public class VoteOptionModel extends BaseModel implements Serializable{
	/*
	option_id: 101581
	group_id: 1
	order: 1
	*/
	public int option_id;
	public int group_id;
	public int order;
	
}
