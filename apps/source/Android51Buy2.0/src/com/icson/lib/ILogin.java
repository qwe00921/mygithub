package com.icson.lib;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.text.TextUtils;

import com.icson.lib.model.Account;
import com.icson.util.db.Database;
import com.icson.util.db.DbFactory;

public class ILogin {

	private static Account account;


	public static boolean accountChecked = false;

	/**
	 * 得到当前登录用户的账号，如果不存在返回0
	 * @return 当前登录的用户的账号
	 */
	public static long getLoginUid() {
		Account account = getActiveAccount();
		return null != account ? account.getUid() : 0;
	}
	
	/**
	 * 得到当前登录的用户的密码，如果不存在返回空
	 * @return 当前登录的用户的密码
	 */
	public static String getLoginSkey() {
		Account account = getActiveAccount();
		return null != account ? account.getSkey() : "";
	}
	
	/**
	 * 得到当前登录的用户的token值，如果不存在返回空
	 * @return 当前登录用户的token值
	 */
	public static String getLoginToken() {
		Account account = getActiveAccount();
		return null != account ? account.getToken() : "";
	}

	/**
	 * 得到当前用户登录的一个区域位置：如长三角，珠三角等等
	 * @return
	 */
	public static int getSiteId() {

		return FullDistrictHelper.getSiteId();
	}

	
	/**
	 * 得到已经登录过的用户的信息，从t_login表中得到存储在数据库中的用户的信息。
	 * @return Account对象封装的用户的信息
	 */
	public static Account getActiveAccount() {

		if (null != account || accountChecked)
			return account;
		accountChecked = true;
		Database db = DbFactory.getInstance();
		HashMap<String, String> info = db.getOneRow("select * from t_login");
		if (null == info) {
			return null;
		}

		account = new Account();
		account.setUid(Long.valueOf(info.get("uid")));
		//login type
		/* account.type
		 * 0 other
		 * 1.qq   only qq will relogin
		 * 2.yixun
		 * 3.alipay
		 */
		String loginType = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "login_type");
		if(!TextUtils.isEmpty(loginType) && TextUtils.isDigitsOnly(loginType))
		{
			account.setType(Integer.valueOf(loginType));
		}
		else
			account.setType(Account.TYPE_OTHER);
		account.setSkey(info.get("skey"));
		account.setNickName(info.get("nick_name"));
		account.setRowCreateTime(Long.valueOf(info.get("row_create_time")));
		account.setToken(info.get("token"));
		
		return account;
	}
	
	/**
	 * 设置正在登录的用户
	 * @param acc 登录的用户信息
	 */
	public static void setActiveAccount(Account acc) {
		account = acc;
	}

	/**
	 * 清除用户的登录信息，也就是从t_login表中删除掉登录信息的记录，
	 */
	public static void clearAccount() {
		Database db = DbFactory.getInstance();
		db.execute("delete from t_login");
		account = null;
		//clear alipay thirdcall
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "thirdcallsource", "", false);
		
		/* account.type
		 * 0 other
		 * 1.qq   only qq will relogin
		 * 2.yixun
		 * 3.alipay
		 */
		//clear
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "login_type", "", true);
	}
	
	/**
	 * 保存用户登录的信息到数据库中。
	 * @param account 待保存的用户的信息
	 */
	public static void saveIdentity(Account account) {
		ContentValues values = new ContentValues();
		values.put("uid", account.getUid());
		values.put("skey", account.getSkey());
		values.put("nick_name", account.getNickName());
		values.put("row_create_time", new Date().getTime());
		values.put("token", account.getToken());
		Database db = DbFactory.getInstance();
		db.insert("t_login", values);
		/* account.type
		 * 0 other
		 * 1.qq   only qq will relogin
		 * 2.yixun
		 * 3.alipay
		 */
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "login_type", "" + account.getType(), true);
	}
	
}
