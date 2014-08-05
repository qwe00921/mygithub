package com.tencent.djcity.lib.model;
/**
 * 
 * @author xingyao
 *
 */
public class Account 
{
	public static final int TYPE_OTHER = 0;
	public static final int TYPE_QQ = 1;
	public static final int TYPE_WECHAT = 2;
	
	
	// Private part.
	private long uin;
	
	private String cookieUin;
	
	private String skey;
	
	private String pskey;
	
	private String nickName;
	
	private long rowCreateTime;
	
	private int  type;
	
	public String getCookieUin() {
		return cookieUin;
	}
	
	public long getUin() {
		return uin;
	}
	public void setUin(long uin) {
		this.uin = uin;

		cookieUin = "" +uin;
		int len = cookieUin.length();
		switch (len)
		{
		case 4:
			cookieUin = "o000000" + uin;
			break;
		case 5:
			cookieUin = "o00000" + uin;
			break;
		case 6:
			cookieUin = "o0000" + uin;
			break;
		case 7:
			cookieUin = "o000" + uin;
			break;
		case 8:
			cookieUin = "o00" + uin;
			break;
		case 9:
			cookieUin = "o0" + uin;
			break;
		default:
			cookieUin = "o" + uin;
			break;
		}
	}
	
	public String getPskey() {
		return pskey;
	}
	public void setPskey(String akey) {
		this.pskey = akey;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String akey) {
		this.skey = akey;
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
	
	
	public void setType(int aType) {
		this.type = aType;
	}
	public int getType() {
		return type;
	}
}
