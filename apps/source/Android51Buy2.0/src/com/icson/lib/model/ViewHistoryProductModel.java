package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.ToolUtil;

@SuppressWarnings("serial")
public class ViewHistoryProductModel extends ProductModel {
	private int mStarLength;
	//评论数
	private int mDiscussCount;

	public int getStarLength() {
		return mStarLength;
	}

	public void setStarLength(int starLength) {
		this.mStarLength = starLength;
	}
	
	public int getDiscussCount(){
		return this.mDiscussCount;
	}
	
	public void setDiscussCount(int num){
		this.mDiscussCount = num;
	}
	
	@Override
	public void parse(JSONObject v) throws JSONException {
		super.parse(v);
		
		if (!ToolUtil.isEmptyList(v, "comment")) {
			JSONObject json = v.getJSONObject("comment");
			setStarLength(json.optInt("star_length", 100));
			setDiscussCount(json.optInt("total", 0));
		}else{
			//默认5星
			setStarLength(100);
			//默认评论数为0
			setDiscussCount(0);
		}
	}
}
