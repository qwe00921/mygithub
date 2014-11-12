package com.yy.android.gamenews.event;

public class CommentEvent {
	public long id = 0;
	public int commentCount = 0;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
}
