package com.icson.postsale;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.Log;

public class PostSaleRequestModel extends BaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//    "errno": 0,
//    "data": {
//        "total": 1,  //总数，如果总数为0，则显示默认的静态页面
//        "entry": {
//            "applyID": 98005691,   //售后单ID
//            "applyTime": "2013-07-05 11:54:19",  //售后单时间
//            "applyStatus": "已审核处理中",    //售后单状态
//            "can_urgent": 0,  //是否显示催一催按钮
//            "order_char_id": "1001023369",   //订单号
//            "count": 1,    //商品数量
//            "items": [
//                {
//                    "productID": 86,
//                    "productName": "DELL戴尔 8135 USB 多媒体键盘",
//                    "productNum": 1
//                }
//            ],
//            "log": "",    //最后一条流水内容
//            "logTime": ""    //最后一条流水时间
//        }
//    }

	private int mApplyId;
	private String mApplyTime;
	private String mApplyStatus;
	private int mCanUrgent;
	private String mOrderCharId;
	private int mCount;
	private PostSaleItemModel mItem;
	private PostSaleLogModel mLogInfo;

	public String getOrderCharId() {
		return mOrderCharId;
	}

	public void setOrderCharId(String orderCharId) {
		this.mOrderCharId = orderCharId;
	}
	
	public int getApplyId() {
		return mApplyId;
	}

	public void setApplyId(int applyId) {
		this.mApplyId = applyId;
	}

	public String getApplyTime() {
		return mApplyTime;
	}

	public void setApplyTime(String applyTime) {
		this.mApplyTime = applyTime;
	}

	public String getApplyStatus() {
		return mApplyStatus;
	}

	public void setApplyStatus(String applyStatus) {
		this.mApplyStatus = applyStatus;
	}

	public int getCanUrgent() {
		return mCanUrgent;
	}

	public void setCanUrgent(int canUrgent) {
		this.mCanUrgent = canUrgent;
	}

	public int getCount() {
		return mCount;
	}

	public void setCount(int count) {
		this.mCount = count;
	}

	public PostSaleLogModel getLogInfo() {
		return mLogInfo;
	}

	public void setLogInfo(PostSaleLogModel logInfo) {
		this.mLogInfo = logInfo;
	}

	public PostSaleItemModel getItem() {
		return mItem;
	}

	public void setItem(PostSaleItemModel item) {
		this.mItem = item;
	}

	private static final String TAG = PostSaleRequestModel.class.getSimpleName();
	
	public void parse(JSONObject json) throws JSONException {
		if(json == null) {
			Log.w(TAG, "[parse] json is null!");
			return;
		}
		
		setApplyId(json.optInt(Constants.KEY_APPLY_ID, 0));
		setApplyStatus(json.optString(Constants.KEY_APPLY_STATUS, ""));
		setApplyTime(json.optString(Constants.KEY_APPLY_TIME, ""));
		setCanUrgent(json.optInt(Constants.KEY_CAN_URGENT, 0));
		setOrderCharId(json.optString(Constants.KEY_ORDER_CHAR_ID, ""));
		setCount(json.optInt(Constants.KEY_COUNT, 0));
		
		PostSaleLogModel log = new PostSaleLogModel();
		log.parse(json);
		setLogInfo(log);
		
		JSONArray arrs = json.optJSONArray(Constants.KEY_ITEMS);
		if(arrs != null) {
			JSONObject itemJSON = arrs.getJSONObject(0);
			PostSaleItemModel item = new PostSaleItemModel();
			item.parse(itemJSON);
			setItem(item);
		}
	}
}
