package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ReviewModel extends BaseModel {
	private long id;

	private int bizId;

	private int type;

	private long userId;

	private int userLevel;

	private String userName;

	private String userNick;

	private String content;

	private long createTime;

	private long reviewNumber;

	private long repliesNumber;

	private long lastReplyTime;

	private long productId;

	private boolean isTop;

	private boolean isBest;

	private int star;

	private long supporter;

	private long objector;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBizId() {
		return bizId;
	}

	public void setBizId(int bizId) {
		this.bizId = bizId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getReviewNumber() {
		return reviewNumber;
	}

	public void setReviewNumber(long reviewNumber) {
		this.reviewNumber = reviewNumber;
	}

	public long getRepliesNumber() {
		return repliesNumber;
	}

	public void setRepliesNumber(long repliesNumber) {
		this.repliesNumber = repliesNumber;
	}

	public long getLastReplyTime() {
		return lastReplyTime;
	}

	public void setLastReplyTime(long lastReplyTime) {
		this.lastReplyTime = lastReplyTime;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public boolean isBest() {
		return isBest;
	}

	public void setBest(boolean isBest) {
		this.isBest = isBest;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public long getSupporter() {
		return supporter;
	}

	public void setSupporter(long supporter) {
		this.supporter = supporter;
	}

	public long getObjector() {
		return objector;
	}

	public void setObjector(long objector) {
		this.objector = objector;
	}

	public void parse(JSONObject v) throws JSONException {
		setId(v.getLong("id"));
		setBizId(v.getInt("biz_id"));
		setType(v.getInt("type"));
		setUserId(v.getLong("user_id"));
		setUserNick(v.getString("user_nick"));
		setUserLevel(v.getInt("user_level"));
		setUserName(v.getString("user_name"));
		setContent(v.getString("content"));
		setCreateTime(v.getLong("create_time"));
		setReviewNumber(v.getLong("review_number"));
		setRepliesNumber(v.getLong("replies_number"));
		setLastReplyTime(v.getLong("last_reply_time"));
		setProductId(v.getLong("product_id"));
		setTop(v.getInt("is_top") == 1);
		setBest(v.getInt("is_best") == 1);
		setStar(v.getInt("star"));
		setSupporter(v.getLong("supporter"));
		setObjector(v.getLong("objector"));
	}
}
