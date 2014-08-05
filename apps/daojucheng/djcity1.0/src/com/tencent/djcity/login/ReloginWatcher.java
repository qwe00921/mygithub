package com.tencent.djcity.login;

import oicq.wlogin_sdk.request.Ticket;
import oicq.wlogin_sdk.request.WUserSigInfo;
import oicq.wlogin_sdk.request.WloginLastLoginInfo;
import oicq.wlogin_sdk.request.WtloginHelper;
import oicq.wlogin_sdk.request.WtloginHelper.SigType;
import oicq.wlogin_sdk.tools.ErrMsg;
import oicq.wlogin_sdk.tools.util;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.IVersion;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.activity.BaseActivity;

/**
 * 
*   
* Class Name:SlotVersionParser 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-18 ä¸????06:38:45 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class ReloginWatcher{
	
	private static final String LOG_TAG = ReloginWatcher.class.getName();
	private static ReloginWatcher mSelf;
	private WtloginHelper mLoginHelper;
	public final static String mDomain = "game.qq.com";
	public final static long mAppid = 535030502;
	public final static int mMainSigMap = SigType.WLOGIN_A2 | SigType.WLOGIN_SKEY | SigType.WLOGIN_ST | SigType.WLOGIN_LSKEY | SigType.WLOGIN_PSKEY;
	public static String mAppVersion = IVersion.getVersionName();
	private Context mAppContext;
	//private  WUserSigInfo mSigInfo;
	
	public static ReloginWatcher getInstance(Context aContext)
	{
		if( null == mSelf )
		{
			synchronized(ReloginWatcher.class)
			{
			mSelf = new ReloginWatcher();
			mSelf.mAppContext = aContext.getApplicationContext();
			mSelf.mLoginHelper = new WtloginHelper(mSelf.mAppContext);
			//mSelf.mSigInfo = new WUserSigInfo();
			}
			//mSelf.mContext = aContext;
		}
		
		return mSelf;
	}
	
	/**
	 * 
	* method Name:getWtloginHelper    
	* method Description:  
	* @return   
	* WtloginHelper  
	* @exception   
	* @since  1.0.0
	 */
	public WtloginHelper getWtloginHelper()
	{
		return mSelf.mLoginHelper;
	}
	
	/**
	 * 
	 */
	public void clearAccountInfo()
	{
		if(null!=mSelf.mLoginHelper)
		{
			WloginLastLoginInfo info = mSelf.mLoginHelper.GetLastLoginInfo();
			
			if ((null != info) && (!TextUtils.isEmpty(info.mAccount))) {
				// Clear the information.
				mSelf.mLoginHelper.ClearUserLoginData(info.mAccount, ReloginWatcher.mAppid);
			}
		}
	}
	
	/**
	 * 
	 * @param userAccount
	 * @param appid
	 * @return
	 */
	public static String getPskeyByLocalSig(final String userAccount)
	{
		String psKey = "";
		if(mSelf == null || mSelf.mLoginHelper == null)
		{
			return "";
		}
		WUserSigInfo localSig =  mSelf.mLoginHelper.GetLocalSig(userAccount, mAppid);
		if(null != localSig)
		{
			Ticket ticket = WtloginHelper.GetUserSigInfoTicket(localSig, SigType.WLOGIN_PSKEY);
			if(null != ticket)
			{
				byte [] pskeyByte = ticket._pskey_map.get(ReloginWatcher.mDomain);
				psKey =  new String(pskeyByte);
				
				if(TextUtils.isEmpty(psKey))
				{
					return "";
				}
			}
		}
		return psKey;
	}
	
	/**
	 * 
	 * @param userAccount
	 * @return
	 */
	public static String getSkeyByLocalSig(final String userAccount)
	{
		String psKey = "";
		if(mSelf == null || mSelf.mLoginHelper == null)
		{
			return "";
		}
		WUserSigInfo localSig =  mSelf.mLoginHelper.GetLocalSig(userAccount, mAppid);
		if(null != localSig)
		{
			Ticket ticket = WtloginHelper.GetUserSigInfoTicket(localSig, SigType.WLOGIN_SKEY);
			if(null != ticket)
			{
				psKey =  new String(ticket._sig);
				
				if(TextUtils.isEmpty(psKey))
				{
					return "";
				}
			}
		}
		return psKey;
	}
	
	/*
	public boolean quiteReLogin() {
		
		String reloginFlag = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "login_type");
		if(TextUtils.isEmpty(reloginFlag) || !reloginFlag.equals("1") )
			return false;
		
		mLoginHelper.SetListener(mListener);
		WloginLastLoginInfo info = mLoginHelper.GetLastLoginInfo();
		//always call net
		//mLoginHelper.SetTimeOut(0);
		
		int ret = -1;
		if (mLoginHelper.IsNeedLoginWithPasswd(info.mAccount, ReloginWatcher.mAppid)) {
			if (mLoginHelper.IsUserHaveA1(info.mAccount, ReloginWatcher.mAppid)) {						
				ret = mLoginHelper.GetStWithPasswd(info.mAccount,ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, "", mSigInfo);
			} 
			else
				//need passwd
				return false;
		
		} else {
			ret = mLoginHelper.GetStWithoutPasswd(info.mAccount, ReloginWatcher.mAppid, 
					ReloginWatcher.mAppid, 0x1, ReloginWatcher.mMainSigMap, mSigInfo);
		}
		
		//need judge the ret
		return (ret == util.S_SUCCESS);
	}
		
		
	
	WtloginListener mListener = new WtloginListener() {
		
		@Override
		public void OnGetStWithoutPasswd(String userAccount, long dwSrcAppid,
				long dwDstAppid, int dwMainSigMap, long dwSubDstAppid,
				WUserSigInfo userSigInfo, int ret, ErrMsg errMsg) {
			if (ret == util.S_SUCCESS) {
				loginSucess(userAccount, userSigInfo);
			} 
			else//fail
				AppStorage.setData(AppStorage.SCOPE_DEFAULT, "login_type", "", true);
		}
		
		
		@Override
		public void OnGetStWithPasswd(String userAccount, long dwSrcAppid,
				int dwMainSigMap, long dwSubDstAppid, String userPasswd,
				WUserSigInfo userSigInfo, int ret,ErrMsg errMsg) {
			if (ret == util.S_SUCCESS) 
				loginSucess(userAccount, userSigInfo);
			else
				AppStorage.setData(AppStorage.SCOPE_DEFAULT, "login_type", "", true);
		}
			
	};
	
	
	private void loginSucess(String userAccount, WUserSigInfo userSigInfo) {
		mSigInfo = userSigInfo;
		WloginSimpleInfo info = new WloginSimpleInfo();
		mLoginHelper.GetBasicUserInfo(userAccount, info);
		Ticket ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_LSKEY);
		String strLskey =  new String(ticket._sig);
		ticket = WtloginHelper.GetUserSigInfoTicket(userSigInfo, SigType.WLOGIN_SKEY);
		String skey =  new String(ticket._sig);
		
		String strInfo = info._uin + "&skey=" + skey +
			"&lskey=" + strLskey;
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_WT_LOGIN, strInfo);
		if( null == ajax )
			return ;
		TelephonyManager ts = (TelephonyManager)(mAppContext.getSystemService(Context.TELEPHONY_SERVICE));
		String guid = ts.getSubscriberId();
		if (guid == null) {
			guid = Secure.getString(mAppContext.getContentResolver(), Secure.ANDROID_ID);
			
		}
		ajax.setData("device_id", ToolUtil.toMD5(guid));
		
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){

			@Override
			public void onSuccess(JSONObject json, Response response) {
				Log.w("relogin","Relogin~ Succ");
				int errno;
				try {
					errno = json.getInt("errno");
				} catch (JSONException e) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
					return;
				}

				if (errno != 0) {
					return;
				}

				String uid = null, skey = null, token = null;
				try {
					JSONArray data = json.getJSONArray("data");
					uid = data.getString(0);
					skey = data.getString(1);
					token = data.getString(2);
				}// å¦????è¿????errno =0ï¼?å¹¶ä??jsonä¸?æ²¡æ??dataï¼???£ä??å°?è¯?ä»?cookieä¸???·å????°æ??
				catch (Exception ex) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
					Cookie cookie = response.getCookie();
					uid = cookie.get("uid");
					skey = cookie.get("skey");
					token = cookie.get("token");
				}

				if (null == uid || null == skey) {
					return;
				}

				Account account = new Account();
				account.setUin(Long.valueOf(uid));
				account.setSkey(skey);
				account.setNickName("");
				account.setRowCreateTime(new Date().getTime());
				ILogin.setActiveAccount(account);
				ILogin.saveIdentity(account);
				
			}});
		ajax.send();
	}
	
	*/
	
	public static void clear()
	{
		if(null == mSelf)
			return;
		
		if(null!=mSelf.mLoginHelper)
			mSelf.mLoginHelper.SetListener(null);
		mSelf.mLoginHelper = null;
		//mSelf.mSigInfo = null;
		mSelf = null;
	}
	
	/**
	 * 
	 * @param userAccount
	 * @param imagePrompt
	 * @return
	 */
	public static String getImagePrompt(String userAccount, byte[] imagePrompt)
	{
		String prompt_value = null;
  		if (imagePrompt != null && imagePrompt.length > 3) {
  			int pos = 0;
  			int dwCnt = util.buf_to_int32(imagePrompt, pos);
  			pos += 4;
  			for (int i = 0; i < dwCnt; i++) {
  				if (imagePrompt.length < pos + 1) {
  					break;
  				}
  				
  				int key_len = util.buf_to_int8(imagePrompt, pos);
  				pos += 1;
  				
  				if (imagePrompt.length < pos + key_len) {
  					break;
  				}
  				String key_data = new String(imagePrompt, pos, key_len);
  				pos += key_len;
  				
  				if (imagePrompt.length < pos + 2) {
  					break;
  				}
  				int value_len = util.buf_to_int32(imagePrompt, pos);
  				pos += 4;
  				
  				if (imagePrompt.length < pos + value_len) {
  					break;
  				}
  				String value = new String(imagePrompt, pos, value_len);
  				pos += value_len;
  				
  				util.LOGI("key_data:" + key_data + " value:" + value);
  				if (key_data.equals("pic_reason")) {
  					prompt_value = value;
  					break;
  				}	      				
  			}
  		}
  		return prompt_value;
	}
	
	/**
	 * 
	 * @param aActivity
	 * @param pErrMsg
	 */
	public static void showErrDialog(final BaseActivity aActivity, ErrMsg pErrMsg) {
		if(null == aActivity || null == pErrMsg)
			return;
		
		String strTitle = aActivity.getString(R.string.caption_hint);
		String strMessage = aActivity.getString(R.string.message_qq_login_failed);
		if( null == mSelf.mLoginHelper ) {
			UiUtils.makeToast(aActivity, aActivity.getString(R.string.message_qq_login_failed));
		}else{
			if(null != pErrMsg){
				String title = pErrMsg.getTitle();
				String message = pErrMsg.getMessage();
				final String strUrl = pErrMsg.getOtherinfo();
				if (title != null && title.length() > 0) {
					strTitle = title;
				} 
				
				if (message != null && message.length() > 0) {
					strMessage = message;
				} 
				
				if(null != strUrl && strUrl.length() > 0) {
					strMessage = strMessage + "??????è·³è½¬??¾æ?¥ï??" + strUrl + "?";
				}
				
				if( 1 == pErrMsg.getType()) {
					UiUtils.showDialog(aActivity, strTitle, strMessage, 
							aActivity.getString(R.string.btn_go), 
							aActivity.getString(R.string.btn_cancel), 
							new AppDialog.OnClickListener() {
						@Override
						public void onDialogClick(int nButtonId) {
							if(nButtonId == AppDialog.BUTTON_POSITIVE) {
								if(null != strUrl && strUrl.length() > 0){
									Uri uri = Uri.parse(strUrl);
									Intent intent = new Intent(Intent.ACTION_VIEW, uri);
									aActivity.startActivity(intent);
								}
							}
						}
					});
				}else{
					UiUtils.showDialog(aActivity, strTitle, strMessage, R.string.btn_ok, new AppDialog.OnClickListener() {
						@Override
						public void onDialogClick(int nButtonId) {
							if(nButtonId == AppDialog.BUTTON_POSITIVE) {
							}
						}
					});
				}
			}
		}
	}
}	
	
	
	
	
	
	
	
	


