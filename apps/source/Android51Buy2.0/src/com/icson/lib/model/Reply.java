package com.icson.lib.model;

import java.util.ArrayList;

public class Reply {
	public String ip;
	public int createTime;
	public int satisfaction;
	
	public int supporter;
	public int objector;
	
	public boolean isTop;
	
	public String content;
	
	private ArrayList<Reply> subReplys;
	
	
	public void addSubReplys(Reply reply){
		if( null == subReplys ){
			subReplys = new ArrayList<Reply>();
		}
		subReplys.add(reply);
	}
	
	public ArrayList<Reply> getSubReplys(){
		return subReplys;
	}
}
