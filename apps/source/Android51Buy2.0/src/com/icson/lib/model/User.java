package com.icson.lib.model;

public class User {
	public boolean isIcson;
	public int id;
	public String nickName;
	public String loginName;
	public String pwd;
	public int level;
	public int core;
	public UserType userType;
	public LoginType loginType;
	public String pic;
	
	public class UserType{
		public int id;
		public int name;
	}
	
	public class LoginType{
		public int id;
		public String name;
	}
}
