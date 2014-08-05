package com.icson.lib.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class FeedbackItemModel extends BaseModel implements Serializable {
	
	public class FeedbackReplyModel extends BaseModel implements Serializable {
		public String mReplyContent;
		public String mReplyTime;
		
		public void parse(JSONObject v) throws JSONException {
			mReplyContent = v.optString("reply_content", "");
			mReplyTime = v.optString("reply_time", "");
		}
	}
	
	public int mApplyId;
	public String mApplyTime;
	public String mApplyType;
	public ArrayList<String> 	mAttachments = new ArrayList<String>();
	public String mContent;
	public int mEstCompTime;
	public int mHasReplay;
	public int mIsOrderNo;
	public int mOrderNo;
	public ArrayList<FeedbackReplyModel>	mReplayList = new ArrayList<FeedbackReplyModel>();
	public String mStatus;

	
	public void parse(JSONObject v) throws JSONException {
		
		mApplyId = v.optInt("apply_id", 0);
		mApplyTime = v.optString("apply_time", "");
		mApplyType = v.optString("apply_type", "其他");
		
		String rawAtta = v.optString("attachment", "");
		String[] separated = rawAtta.split(";");
		for (String oneUrl : separated) {
			if (oneUrl.length() > 5) {
				mAttachments.add(oneUrl);
			}
		}
		
		mContent = v.optString("content", "");
		mEstCompTime = v.optInt("est_comp_time", 0);
		mHasReplay = v.optInt("hasReply", 0);
		mIsOrderNo = v.optInt("isOrderNo", 0);
		mOrderNo = v.optInt("orderNo", 0);
		mStatus = v.optString("status", "");
		
		JSONArray arrs = v.optJSONArray("reply");
		for (int i = 0, len = arrs.length(); i < len; i++) {
			FeedbackReplyModel model = new FeedbackReplyModel();
			model.parse(arrs.getJSONObject(i));
			mReplayList.add(model);
		}
	}
}

//{
//    "apply_id" = 1755686;
//    "apply_time" = "2013-10-21 10:25:26";
//    "apply_type" = "\U8ba2\U5355\U95ee\U9898";
//    attachment = "";
//    content = "TestPlease ignore";
//    "est_comp_time" = 1382324126;
//    hasReply = 0;
//    isOrderNo = 1;
//    orderNo = 1035679857;
//    reply =     (
//    );
//    status = "\U5f85\U5904\U7406";
//}

//{
//    "apply_id" = 1598834;
//    "apply_time" = "2013-10-12 16:14:08";
//    "apply_type" = "APP\U529f\U80fd";
//    attachment = "http://img2.icson.com/event/2013/10/12/13815656349676.jpg";
//    content = "\U6d4b\U8bd5\U529f\U80fdghjkkkkjhhfffg";
//    "est_comp_time" = 1381567448;
//    hasReply = 0;
//    isOrderNo = 1;
//    orderNo = "";
//    reply =     (
//    );
//    status = "\U5df2\U5b8c\U6210";
//}
