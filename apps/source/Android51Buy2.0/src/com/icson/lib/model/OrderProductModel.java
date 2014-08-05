package com.icson.lib.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.ToolUtil;

public class OrderProductModel extends ProductModel {

	private static final long serialVersionUID = 1L;

	private int buyCount;
	private boolean evaluated;
	private boolean canEvaluate;

	private ArrayList<OrderGiftModel> mOrderGiftModel = new ArrayList<OrderGiftModel>();

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	@Override
	public int getGiftCount() {
		return mOrderGiftModel.size();
	}
	
	public boolean isEvaluated() {
		return evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}
	
	public boolean isCanEvaluate() {
		return canEvaluate;
	}

	public void setCanEvaluate(boolean canEvaluate) {
		this.canEvaluate = canEvaluate;
	}

	public ArrayList<OrderGiftModel> getOrderGiftModels() {
		return mOrderGiftModel;
	}

	public void setOrderGiftModel(ArrayList<OrderGiftModel> OrderGiftModel) {
		this.mOrderGiftModel = OrderGiftModel;
	}

	@Override
	public void parse(JSONObject v) throws JSONException {
		super.parse(v);
		setBuyCount(v.optInt("buy_num"));
		setShowPrice(v.optDouble("price"));
		//可否评论
		setCanEvaluate(v.optBoolean("can_evaluate", false));
		//是否评论过
		setEvaluated(v.optBoolean("is_evaluated", false));
		// 礼物
		mOrderGiftModel.clear();
		if (!ToolUtil.isEmptyList(v, "gift")) {
			final JSONArray giftJSON = v.getJSONArray("gift");

			for (int i = 0, len = giftJSON.length(); i < len; i++) {
				JSONObject json = giftJSON.getJSONObject(i);
				OrderGiftModel model = new OrderGiftModel();
				model.parse(json);
				if (model.getType() != 1) {
					mOrderGiftModel.add(model);
				}
			}
		}
	}
}