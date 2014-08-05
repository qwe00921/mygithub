package com.tencent.djcity.lib;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.text.TextUtils;

import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.util.db.Database;
import com.tencent.djcity.util.db.DbFactory;

public class ILogin {

	private static Account account;


	public static boolean accountChecked = false;

	/**
	 * å¾???°å???????»å????¨æ?·ç??è´???·ï??å¦????ä¸?å­???¨è?????0
	 * @return å½??????»å???????¨æ?·ç??è´????
	 */
	public static long getLoginUin() {
		Account account = getActiveAccount();
		return null != account ? account.getUin() : 0;
	}
	
	/**
	 * 
	 * @return
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
		account.setUin(Long.valueOf(info.get("uin")));
		//login type
		/* account.type
		 * 0 other
		 * 1.qq   only qq will relogin
		 */
		String loginType = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "login_type");
		if(!TextUtils.isEmpty(loginType) && TextUtils.isDigitsOnly(loginType))
		{
			account.setType(Integer.valueOf(loginType));
		}
		else
			account.setType(Account.TYPE_OTHER);
		
		account.setNickName(info.get("nick_name"));
		account.setRowCreateTime(Long.valueOf(info.get("row_create_time")));
		
		return account;
	}
	
	/**
	 * è®¾ç½®æ­£å?¨ç?»å???????¨æ??
	 * @param acc ??»å???????¨æ?·ä¿¡???
	 */
	public static void setActiveAccount(Account acc) {
		account = acc;
	}

	/**
	 * 
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
	 * 
	 * @param account
	 */
	public static void saveIdentity(Account account) {
		ContentValues values = new ContentValues();
		values.put("uin", account.getUin());
		values.put("nick_name", account.getNickName());
		values.put("row_create_time", new Date().getTime());
		Database db = DbFactory.getInstance();
		db.replace("t_login", values);
		/* account.type
		 * 0 other
		 * 1.qq   only qq will relogin
		 */
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "login_type", "" + account.getType(), true);
	}
	
}
