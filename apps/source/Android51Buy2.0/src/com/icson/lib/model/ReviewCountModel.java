package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ReviewCountModel extends BaseModel {

	// 商品ID
	private long productId;

	// 满意
	private int satisfied;

	// 一般
	private int general;

	// 不满意
	private int unsatisfied;

	// 评论
	private int discussion;

	// 总个数
	private int total;

	// 总评分
	private int satisfaction;

	private int starLength;

	// 发表人数
	private int experienceNumber;

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public int getSatisfied() {
		return satisfied;
	}

	public void setSatisfied(int satisfied) {
		this.satisfied = satisfied;
	}

	public int getGeneral() {
		return general;
	}

	public void setGeneral(int general) {
		this.general = general;
	}

	public int getUnsatisfied() {
		return unsatisfied;
	}

	public void setUnsatisfied(int unsatisfied) {
		this.unsatisfied = unsatisfied;
	}

	public int getDiscussion() {
		return discussion;
	}

	public void setDiscussion(int discussion) {
		this.discussion = discussion;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getSatisfaction() {
		return satisfaction;
	}

	public void setSatisfaction(int satisfaction) {
		this.satisfaction = satisfaction;
	}

	public int getExperienceNumber() {
		return experienceNumber;
	}

	public void setExperienceNumber(int experienceNumber) {
		this.experienceNumber = experienceNumber;
	}

	public float getExperienceLength() {
		return (experienceNumber * 20) / experienceNumber;
	}

	public int getStarLength() {
		return starLength;
	}

	public void setStarLength(int starLength) {
		this.starLength = starLength;
	}

	public void parse(JSONObject jsonObject) throws JSONException {
		setSatisfied(jsonObject.optInt("satisfied_num", 0));
		setGeneral(jsonObject.optInt("general_num", 0));
		setUnsatisfied(jsonObject.optInt("unsatisfied_num", 0));
		setTotal(unsatisfied +  satisfied + general);
		setStarLength(jsonObject.optInt("star_length", 100));
	}
}
