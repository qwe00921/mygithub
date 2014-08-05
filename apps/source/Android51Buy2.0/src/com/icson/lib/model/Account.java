package com.icson.lib.model;

/**
 * 存储用户登录的信息，如用户名，密码，token，昵称，登录类型等等的信息，是一个Model类
 * @author jasonlliu
 *
 */
public class Account 
{
	public static final int TYPE_OTHER = 0;
	public static final int TYPE_QQ = 1;
	public static final int TYPE_YIXUN = 2;
	public static final int TYPE_ALI = 3;
	public static final int TYPE_WECHAT = 4;
	
	
	// Private part.
	private long uid;
	
	private String skey;
	
	private String nickName;
	
	private String token;
	
	private long rowCreateTime;
	
	private int  type;
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public long getRowCreateTime() {
		return rowCreateTime;
	}
	public void setRowCreateTime(long rowCreateTime) {
		this.rowCreateTime = rowCreateTime;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public void setType(int aType) {
		this.type = aType;
	}
	public int getType() {
		return type;
	}
}
