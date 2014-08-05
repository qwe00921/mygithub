package com.tencent.djcity.msgcenter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.Parser;

public class MsgModelParser extends Parser<byte[], ArrayList<MsgModel>> {

	private String mStr;
	
	@Override
	public ArrayList<MsgModel> parse(byte[] input, String charset) throws Exception
	{
		clean();
		
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(input, charset);
		
		if(0 != json.getInt("ret"))
		{
			mErrMsg = json.optString("msg", Config.NORMAL_ERROR);
			return null;
		}
		
		mStr = parser.getString();
		mIsSuccess = true;
		return parse(json);
	}
	
	public String getString()
	{
		return mStr;
	}
	
	public ArrayList<MsgModel> parse(String str) throws JSONException {
		JSONObject json = new JSONObject(str);
		return this.parse(json);
	}
	
	private ArrayList<MsgModel> parse(JSONObject json) throws JSONException {
		final ArrayList<MsgModel> models = new ArrayList<MsgModel>();
		
		JSONArray arrs = json.getJSONArray("list");
		int nSize = arrs.length();

		MsgModel model = null;
		for (int idx = 0; idx < nSize; idx++) 
		{
			JSONObject obj = arrs.getJSONObject(idx);
			model = new MsgModel();
			model.mID = obj.getInt("id");
			model.mURL = obj.getString("url");
			model.mBiz = obj.getString("biz");
			model.mContent = obj.getString("content");
			model.mType = obj.getString("type");
			model.mTargetID = obj.getInt("targetId");
			
			model.mStatus = obj.getInt("status");
			model.mDate = obj.getString("date");
			model.mTitle = obj.getString("title");
			
			models.add(model);
		}

		return models;
	}
}
