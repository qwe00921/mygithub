package com.icson.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.ui.UiUtils;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;

public final class ServiceConfig implements OnSuccessListener<JSONObject> {
	
	public static String getUrl(String strKey) {
		return ServiceConfig.getUrl(strKey, null);
	}
	
	/**
	 * 通过传入的key(URL字符串常量）得到对应的url，并加上附加的信息
	 * @param strKey  传入的key(URL)常量
	 * @param aInfo	  附加的url信息
	 * @return	实际的需要的url
	 */
	public static String getUrl(String strKey, Object aInfo) {
		JSONObject pObject = (null != mSelf ? mSelf.getObject(strKey) : null);
		
		String strUrl = null;
		if( null != pObject ) {
			strUrl = pObject.optString(TAG_URL);
			
			if( null != aInfo ) {
				strUrl = strUrl + aInfo.toString();
			}
		}
		
		return strUrl;
	}
	
	public static Ajax getAjax(String strKey) {
		return ServiceConfig.getAjax(strKey, null, null, null);
	}
	
	public static Ajax getAjax(String strKey, Object aInfo) {
		return ServiceConfig.getAjax(strKey, aInfo, null, null);
	}
	
	public static Ajax getAjax(String strKey, Object aInfo, Parser<?, ?> pParser) {
		return ServiceConfig.getAjax(strKey, aInfo, pParser, null);
	}
	
	/**
	 * 得到请求的ajax对象，并设置该ajax的默认属性：url，method，parse，cookie,Charset等等
	 * @param strKey 通过该key来得到封装这Url和method的jason对象
	 * @param aInfo	 附加的url信息
	 * @param pParser  用来解析返回的jason数据包的Parse
	 * @param strHack  请求的服务器版本：beta，test,w3sg
	 * @return	返回请求的ajax对象
	 */
	public static Ajax getAjax(String strKey, Object aInfo, Parser<?, ?> pParser, String strHack) {
		JSONObject pObject = null != mSelf ? mSelf.getObject(strKey) : null;
		if( null == pObject )
			return null;
		
		// Check whether configuration enabled.
		if( !mSelf.mEnabled ) {
			mSelf.showMessage(mSelf.mMessage);
			return null;
		}
		
		String strUrl = pObject.optString(TAG_URL);
		String strMethod = pObject.optString(TAG_METHOD);
		if( TextUtils.isEmpty(strUrl) || TextUtils.isEmpty(strMethod) )
			return null;
		
		// Check whether current url is disable or not.
		final boolean bEnable = pObject.optBoolean(TAG_ENABLE, true);
		if( !bEnable ) {
			// Check whether there is message.
			String strMessage = pObject.optString(TAG_MESSAGE);
			mSelf.showMessage(strMessage);
			
			return null;
		}
		
		// Check whether hacked or not.
		String strPrev = mSelf.mHack;
		if( !TextUtils.isEmpty(strHack) ) {
			mSelf.mHack = strHack;
		}
		
		if( !TextUtils.isEmpty(mSelf.mHack) ) {
			// Get prefix, such as http://, https://, wap://
			strUrl = strUrl.replace("https://", "http://");
			final String strHttp = "http://";
			final int nStart = strHttp.length(); // length of http://
			final int nOffset = strUrl.indexOf("/", nStart);
			String strHost = strUrl.substring(0, nOffset).toLowerCase(Locale.getDefault());
			if( !strHost.equals(ST_ICSON_COM) && !strHost.equals(PAY_ICSON_COM) ) {
				// 1. Get the module name.
				final int nPos = strUrl.indexOf(".", nStart);
				String strModule = strUrl.substring(nStart, nPos);
				
				// 2. Replace xxx.51buy.com to hack.m.51buy.com/xxx
				String strInfo = strUrl.substring(nOffset);
				strHost = strHost.replaceFirst(strModule, mSelf.mHack + ".m");
				
				strUrl = strHost + "/" + strModule + strInfo;
			}
		}
		
		if( null != aInfo ) {
			strUrl = strUrl + aInfo.toString();
		}
		
		// Restore the previous value.
		mSelf.mHack = strPrev;
		
		// Make sure current application version code is available.
		IcsonApplication.getVersionCode(mSelf.mContext);
		
		// Get exTag.
		String exTag = ServiceConfig.getToken();
		
		Ajax pResult = null;
		if( null == pParser )
			pParser = new JSONParser();
		switch( ServiceConfig.getMethod(strMethod) )
		{
		case Ajax.GET:
			pResult = AjaxUtil.get(strUrl);
			break;
			
		case Ajax.POST:
			pResult = AjaxUtil.post(strUrl);
			break;
			
		case Ajax.STREAM:
			pResult = new Ajax(Ajax.STREAM);
			pResult.setUrl(strUrl);
			break;
			
		default:
			pResult = AjaxUtil.get(strUrl);
			break;
		}
		
		if( !TextUtils.isEmpty(exTag) ) {
			pResult.setData("exAppTag", exTag);
		}
		pResult.setParser(pParser);
		
		return pResult;
	}
	
	/**
	 * get error message specified by errno
	 * @param strKey
	 * @param nErrNo
	 * @return
	 */
	public static String getErrMsg(String strKey, int nErrNo) {
		JSONObject pObject = null != mSelf ? mSelf.getObject(strKey) : null;
		if( null == pObject )
			return "";
		
		// Get error array configuration.
		JSONArray pArray = pObject.optJSONArray(TAG_MSG_ARRAY);
		final int nLength = (null != pArray ? pArray.length() : 0);
		try {
			for( int nIdx = 0; nIdx < nLength; nIdx++ ) {
				JSONObject entity;
				entity = pArray.getJSONObject(nIdx);
				final int errno = entity.optInt(TAG_ERR_NO);
				if( errno == nErrNo ) {
					return entity.optString(TAG_MESSAGE);
				}
			}
		} catch( JSONException aException ) {
			aException.printStackTrace();
		}
		
		return "";
	}
	
	/**
	 * Get Display information.
	 * @return
	 */
	public static String getInfo(){
		if( (null == mSelf) || (TextUtils.isEmpty(mSelf.mAlias)) )
			return "";
		
		return "(ALIAS: " + mSelf.mAlias + ", NUM: " + mSelf.mVersion + ")";
	}
	
	private void showMessage(String strMessage) {
		if( (!TextUtils.isEmpty(strMessage)) && (null != mContext) ) {
			// Show message for message disabled.
			UiUtils.showDialog(mContext, R.string.app_name, strMessage, R.string.btn_ok);
		}
	}
	
	/**
	 * 通过key从mConfig中得到封装着url和method的jason对象
	 * @param strKey 字符串常量key
	 * @return 封装着url和method的jason对象
	 */
	private JSONObject getObject(String strKey) {
		if( (null == mConfig) || (TextUtils.isEmpty(strKey)) )
			return null;
		
		return mConfig.optJSONObject(strKey);
	}
	
	/**
	 * check configuration version.
	 */
	public static void checkConfig() {
		// Check latst configuration from server.
		if( null == mSelf || mSelf.mDebug )
			return ;
		
		mSelf.updateCheckAjax = AjaxUtil.post(ServiceConfig.GET_INTERFACE_URL + mSelf.mVersion);
		mSelf.updateCheckAjax.setData("deviceid", StatisticsUtils.getDeviceUid(mSelf.mContext));
		mSelf.updateCheckAjax.setData("userid", ILogin.getLoginUid());
		mSelf.updateCheckAjax.setParser(new JSONParser());
		mSelf.updateCheckAjax.setOnSuccessListener(mSelf);
		mSelf.updateCheckAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
				cleanUpdateAjax();
				
			}});
		mSelf.updateCheckAjax.send();
	}
	
	@Override
	public void onSuccess(JSONObject object, Response response) {
		if( null != object ) {
			try {
				// Parse the err number.
				final int errno = object.getInt("errno");
				if( errno == 0 ) {
					// Parse Info
					parseInfo(object);
						
					// Save configuration.
					saveConfig();
				}
			} catch (JSONException aException) {
				aException.printStackTrace();
			}finally
			{
				cleanUpdateAjax();
			}
			
		}
	}
	
	private static void cleanUpdateAjax()
	{
		if(null!=mSelf.updateCheckAjax)
		{
			mSelf.updateCheckAjax.abort();
			mSelf.updateCheckAjax = null;
		}
	}
	
	private void parseInfo(JSONObject aObject) {
		if( null != aObject ) {
			mVersion = aObject.optInt(TAG_VERSION);
			mEnabled = aObject.optBoolean(TAG_ENABLE, true);
			mMessage = aObject.optString(TAG_MESSAGE);
			mConfig = aObject.optJSONObject(TAG_DATA);
			mAlias = aObject.optString(TAG_ALIAS);
			mAutoRelogin = aObject.optInt(TAG_AUTO_RELOGIN, 0);
		}
	}
	
	/**
	 * Set value to hack default host.
	 * @param strHack
	 */
	private void setHack(String strHack) {
		if( TextUtils.isEmpty(strHack) ) {
			mHack = null;
			return ;
		}

		final int nLength = mHacks.length;
		for( int nIdx = 0; nIdx < nLength; nIdx++ ) {
			if( strHack.equalsIgnoreCase(mHacks[nIdx]) ) {
				mHack = strHack;
				return ;
			}
		}
	}
	
	/**
	 * Default constructor of ServerAdapter
	 */
	private ServiceConfig(Context aContext) {
		mContext = aContext;
		loadConfig(mDebug);
	}
	
	/**
	 * Set context for instance.
	 * @param aContext
	 */
	public static void setContext(Context aContext) {
		if( null == mSelf )
			mSelf = new ServiceConfig(aContext);
		else
			mSelf.mContext = aContext;
	}
	
	/**
	 * get request method.
	 * @param strMethod
	 * @return
	 */
	private static int getMethod(String strMethod) {
		int nMethod = 0;
		if( strMethod.equalsIgnoreCase("get") ) {
			nMethod = Ajax.GET;
		} else if( strMethod.equalsIgnoreCase("post") ) {
			nMethod = Ajax.POST;
		} else if( strMethod.equalsIgnoreCase("stream") ) {
			nMethod = Ajax.STREAM;
		}
		
		return nMethod;
	}
	
	/**
	 * Load configuration from storage, if not exists, set default.
	 */
	private void loadConfig(boolean bTestOnly)
	{
		if( null == mContext )
			return ;
		
		// Check the version of application.
		Preference pPreference = Preference.getInstance();
		final int nPrevious = pPreference.getProjVersion();
		
		String strContent = null;
		// Check the timetag.
		File pFile = mContext.getFileStreamPath(CACHE_FILE);
		if( (null != pFile) && (!bTestOnly) && (IcsonApplication.mVersionCode == nPrevious) )
		{
			String strTag = "" + pFile.lastModified();
			String strPrev = pPreference.getConfigTag();
			if( strTag.equals(strPrev) ) {
				// Load the server configuration from local storage.
				FileInputStream pInputStream = null;
				try {
					pInputStream = mContext.openFileInput(CACHE_FILE);
					
					byte aBytes[] = new byte[pInputStream.available()];
					pInputStream.read(aBytes);
					
					strContent = new String(aBytes);
					
					// Parse the json object.
					JSONObject pRoot = new JSONObject(strContent);
					parseInfo(pRoot);
				} catch (FileNotFoundException aException) {
					aException.printStackTrace();
					strContent = null;
				} catch (IOException aException) {
					aException.printStackTrace();
					strContent = null;
				} catch (JSONException aException) {
					aException.printStackTrace();
				} finally {
					if( null != pInputStream ) {
						try {
							pInputStream.close();
						} catch (IOException aException) {
							aException.printStackTrace();
						}
						pInputStream = null;
					}
				}
			}
		}
		
		// Check need to build up default values.
		if( TextUtils.isEmpty(strContent) ) {
			// Build default configuration.
			loadDefault(bTestOnly);
		}
		
		// Set hack.
		this.setHack(null);
	}
	
	private boolean loadRawInfo(boolean bDisable) {
		if( null == mContext || bDisable )
			return false;
		
		InputStream pInputStream  = null;
		boolean bSuccess = true;
		try {
			Resources pResources = mContext.getResources();
			pInputStream = pResources.openRawResource(R.raw.config);
			byte[] aBytes = new byte[pInputStream.available()];
			pInputStream.read(aBytes);
			
			// Compose JSON object info
			String strContect = new String(aBytes);
			JSONObject pObject = new JSONObject(strContect);
			
			// Parse information.
			parseInfo(pObject);
			
			// Save information.
			saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
			bSuccess = false;
		} catch (JSONException aException) {
			aException.printStackTrace();
			bSuccess = false;
		}finally
		{
			if(null!= pInputStream)
			{
				try {
					pInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					bSuccess = false;
				}
				pInputStream = null;
			}
		}
		
		return bSuccess;
	}
	
	/**
	 * 将实际的Url地址和请求方法封装到一个jason对象中，然后将一个字符串常量作为key，封装的jason对象作为alue，封装到一个新的jason对象mConfig中
	 * @param bDisable
	 */
	private void loadDefault(boolean bDisable) {
		mVersion = 0;
		
		// 1. Try to load information from raw file.
		if( !loadRawInfo(bDisable) ) {
			if( null == mConfig )
				mConfig = new JSONObject();
			
			// Key and url list.
			try {
				// 1. app.51buy.com
				mConfig.put(Config.URL_AFTERSALE_ORDER_LIST, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=getPostsaleList&jsontype=str", METHOD_POST));
				mConfig.put(Config.URL_AFTERSALE_ORDER_DETAIL, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=getPostsaleDetail&jsontype=str", METHOD_POST));
				mConfig.put(Config.URL_AFTERSALE_ORDER_PROMPT, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=postsaleLevelUp&jsontype=str", METHOD_POST));
				mConfig.put(Config.URL_CATEGORY_LIST, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=postsaleLevelUp&jsontype=str", METHOD_GET));
				mConfig.put(Config.URL_CATEGORY_LIST, getChild(APP_ICSON_COM + "/json.php?mod=acategory&act=list", METHOD_GET));
				mConfig.put(Config.URL_EVENT_PAGE, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=page&id=", METHOD_GET));
				mConfig.put(Config.URL_EVENT_MORNING, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=morningmarketpage&page=", METHOD_GET));
				mConfig.put(Config.URL_EVENT_THH, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=thhpage&page=", METHOD_GET));
				mConfig.put(Config.URL_EVENT_WEEKEND, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=weekendpage&page=", METHOD_GET));
				mConfig.put(Config.URL_SEARCH_FILTER, getChild(APP_ICSON_COM + "/json.php?mod=asearch&act=filter&", METHOD_GET));
				mConfig.put(Config.URL_EVENT_HOMEVPAY, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=homeVPay", METHOD_GET));
				mConfig.put(Config.URL_DISPATCH_SITE, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=dispatches", METHOD_GET));
				mConfig.put(Config.URL_PRODUCT_DETAIL, getChild(APP_ICSON_COM + "/json.php?mod=aitem&act=getdetail", METHOD_GET));
				mConfig.put(Config.URL_ADD_PRODUCT_NOTICE, getChild(APP_ICSON_COM + "/json.php?mod=mynotify&act=addMynotify", METHOD_POST));
				mConfig.put(Config.URL_PRODUCT_INTRO, getChild(APP_ICSON_COM + "/json.php?mod=aitem&act=introduction&pid=", METHOD_GET));
				mConfig.put(Config.URL_PRODUCT_PARAMETERS, getChild(APP_ICSON_COM + "/json.php?mod=aitem&act=param&pid=", METHOD_GET));
				mConfig.put(Config.URL_CHECK_VERSION, getChild(APP_ICSON_COM + "/json.php?mod=amore&act=checkversion", METHOD_POST));
				mConfig.put(Config.URL_SEARCH_PAGE, getChild(APP_ICSON_COM + "/json.php?mod=asearch&act=page&", METHOD_GET));
				mConfig.put(Config.URL_ALIPAY_LOGIN, getChild(APP_ICSON_COM + "/index.php?mod=alogin&act=alipay", METHOD_WAP));
				mConfig.put(Config.URL_WT_LOGIN, getChild(APP_ICSON_COM + "/json.php?mod=alogin&act=wtlogin&uin=", METHOD_GET));
				mConfig.put(Config.URL_LOTTERY_GETINFO, getChild(APP_ICSON_COM + "/json.php?mod=alottery&act=getinfo", METHOD_GET));
				mConfig.put(Config.URL_LOTTERY_DRAWNOW, getChild(APP_ICSON_COM + "/json.php?mod=alottery&act=drawnow", METHOD_GET));
				mConfig.put(Config.URL_LOTTERY_GETMYCODE, getChild(APP_ICSON_COM + "/json.php?mod=alottery&act=getmycode", METHOD_GET));
				mConfig.put(Config.URL_LOTTERY_GETWONCODE, getChild(APP_ICSON_COM + "/json.php?mod=alottery&act=getwoncode", METHOD_GET));
				mConfig.put(Config.URL_GET_MESSAGES, getChild(APP_ICSON_COM + "/json.php?mod=amessage&act=getMessage&jsontype=str", METHOD_POST));
				mConfig.put(Config.URL_SET_MESSAGE_STATUS, getChild(APP_ICSON_COM + "/json.php?mod=amessage&act=setMsgStatus", METHOD_POST));
				mConfig.put(Config.URL_FB_GET_TYPE, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=getComplaintType", METHOD_GET));
				mConfig.put(Config.URL_FEEDBACK_ADD, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=send", METHOD_POST));
				mConfig.put(Config.URL_FB_ADD_NEW, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=addComplaint", METHOD_POST));
				mConfig.put(Config.URL_FB_GET_HISTORY, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=getAppllyList", METHOD_GET));
				mConfig.put(Config.URL_FB_IMAGE_STREAM_UPLOAD, getChild(APP_ICSON_COM + "/json.php?mod=afeedback&act=upload", METHOD_STREAM));
				mConfig.put(Config.URL_RECOMMEND_LOADLIST, getChild(APP_ICSON_COM + "/json.php?mod=arecommapp&act=loadlist", METHOD_POST));
				mConfig.put(Config.URL_SEARCH_GETBYIDS, getChild(APP_ICSON_COM + "/json.php?mod=asearch&act=getbyids", METHOD_GET));
				mConfig.put(Config.URL_EVENT_COUPON, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=coupon", METHOD_GET));
				mConfig.put(Config.URL_GET_USER_COUPON, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=couponlist&uid=", METHOD_GET));
				mConfig.put(Config.URL_ITEM_GETVOTES, getChild(APP_ICSON_COM + "/json.php?mod=aitem&act=getvotes&pid=", METHOD_GET));
				mConfig.put(Config.URL_CHECK_USER_COUPON, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=checkcoupon&uid=", METHOD_POST));
				mConfig.put(Config.URL_PREORDER_SHIPPINGTYPE, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=getShippingInfo&uid=", METHOD_POST));
				mConfig.put(Config.URL_GET_USER_CAN_USE_POINT, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=getpointrange&uid=", METHOD_GET));
				mConfig.put(Config.URL_SMSCODE_GET, getChild(APP_ICSON_COM + "/json.php?mod=asmscode&act=get", METHOD_GET));
				mConfig.put(Config.URL_ORDER_LISTPAGE, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=list&page=", METHOD_GET));
				mConfig.put(Config.URL_ORDER_DETAIL, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=detail&uid=", METHOD_GET));
				mConfig.put(Config.URL_ORDER_SHIP_PAYTYPE, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=getpaytypeofshipping&uid=", METHOD_POST));
				mConfig.put(Config.URL_PUSHNOTIFY_GET, getChild(APP_ICSON_COM + "/json.php?mod=apushnotify&act=get", METHOD_POST));
				mConfig.put(Config.URL_EVENT_QIANG, getChild(APP_ICSON_COM + "/json.php?mod=aqiang&act=get", METHOD_GET));
				mConfig.put(Config.URL_EVENT_QIANG_NEXT, getChild(APP_ICSON_COM + "/json.php?mod=aqiang&act=tomorrow", METHOD_GET));
				mConfig.put(Config.URL_HOT_SEARCH_WORDS, getChild(APP_ICSON_COM + "/json.php?mod=ahotkey&act=GET", METHOD_GET));
				mConfig.put(Config.URL_POST_LOG, getChild(APP_ICSON_COM + "/json.php?mod=alog&act=post", METHOD_STREAM));
				mConfig.put(Config.URL_LIST_ORDER_ONEKEYBUY, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=onekeybuymprice&uid=", METHOD_POST));
				mConfig.put(Config.URL_UPLOAD_ALERT, getChild(APP_ICSON_COM + "/json.php?mod=aalert&act=upload", METHOD_STREAM));
				mConfig.put(Config.URL_USERINFO_UPDATE, getChild(APP_ICSON_COM + "/json.php?mod=abaseinfo&act=update", METHOD_POST));
				mConfig.put(Config.URL_EVENT_TUAN, getChild(APP_ICSON_COM + "/json.php?mod=atuan&act=getpage&page=", METHOD_GET));
				mConfig.put(Config.URL_DOWNLOAD_VOICESEARCH, getChild(APP_ICSON_COM + "/download/android/voicesearch", METHOD_WAP));
				mConfig.put(Config.URL_RECHARGE_MOBILE_PAYMENT, getChild(APP_ICSON_COM + "/json.php?mod=avirtualpay&act=ordernew&uid=", METHOD_POST));
				mConfig.put(Config.URL_RECHARGE_MOBILE_INFO, getChild(APP_ICSON_COM + "/json.php?mod=avirtualpay&act=getMobileInfo&mobile=", METHOD_GET));
				mConfig.put(Config.URL_RECHARGE_MOBILE_MONEY, getChild(APP_ICSON_COM + "/json.php?mod=avirtualpay&act=getCardMoney", METHOD_GET));
				mConfig.put(Config.URL_ALERT_INFO, getChild(APP_ICSON_COM + "/json.php?mod=aalert&act=info", METHOD_GET));
				mConfig.put(Config.URL_EVENT_TIMEBUY, getChild(APP_ICSON_COM + "/json.php?mod=aevent&act=timebuy", METHOD_POST));
				mConfig.put(Config.URL_ORDER_CONFIRM, getChild(APP_ICSON_COM + "/json.php?mod=aorder&act=packageInfoList&uid=", METHOD_POST));
				mConfig.put(Config.URL_MSGOP_SPLASH, getChild(APP_ICSON_COM + "/json.php?mod=amsgop&act=getsplash", METHOD_POST));
				mConfig.put(Config.URL_HOT_PORDUCTS, getChild(APP_ICSON_COM + "/json.php?mod=amy&act=hotlist", METHOD_GET));
				mConfig.put(Config.URL_RECHARGE_INFO, getChild(APP_ICSON_COM + "/json.php?mod=avirtualpay&act=getPayBanner", METHOD_GET));
			
				// 2. base.51buy.com
				mConfig.put(Config.URL_REGISTER, getChild(BASE_ICSON_COM + "/json.php?mod=user&act=register", METHOD_POST)); // no
				mConfig.put(Config.URL_ACCOUNT_EXISTS, getChild(BASE_ICSON_COM + "/json.php?mod=user&act=accountexists", METHOD_POST));  // no
				mConfig.put(Config.URL_EMAIL_EXISTS, getChild(BASE_ICSON_COM + "/json.php?mod=user&act=emailexists", METHOD_POST)); // no
			//	mConfig.put(Config.URL_ADDRESS_LIST, getChild(BASE_ICSON_COM + "/json.php?mod=address&act=get&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_ADDRESS_ADD, getChild(BASE_ICSON_COM + "/json.php?mod=address&act=add&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_ADDRESS_UPDATE, getChild(BASE_ICSON_COM + "/json.php?mod=address&act=modify&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_ADDRESS_DEL, getChild(BASE_ICSON_COM + "/json.php?mod=address&act=del&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_INVOICE_LIST, getChild(BASE_ICSON_COM + "/json.php?mod=invoice&act=get&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_INVOICE_ADD, getChild(BASE_ICSON_COM + "/json.php?mod=invoice&act=add&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_INVOICE_UPDATE, getChild(BASE_ICSON_COM + "/json.php?mod=invoice&act=modify&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_FAVOR_LIST, getChild(BASE_ICSON_COM + "/json.php?mod=myfavor&act=getfromapp&page=", METHOD_GET));
			//	mConfig.put(Config.URL_FAVOR_REMOVE, getChild(BASE_ICSON_COM + "/json.php?mod=myfavor&act=remove&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_FAVOR_ADD, getChild(BASE_ICSON_COM + "/json.php?mod=myfavor&act=addfromapp&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_USER_PROFILE, getChild(BASE_ICSON_COM + "/json.php?mod=user&act=profile&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_ORDER_FLOW, getChild(MB_ICSON_COM + "/json.php?mod=myorder&act=orderflow&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_DELIVERY_FLOW_INFO, getChild(MB_ICSON_COM + "/json.php?mod=myorder&act=deliveryflow&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_ORDER_CANCEL, getChild(BASE_ICSON_COM + "/json.php?mod=orderdetail&act=cancel&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_LOGIN_STATUS, getChild(BASE_ICSON_COM + "/json.php?mod=user&act=loginstatusfromapp&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_ICSON_LOGIN, getChild(BASES_ICSON_COM + "/json.php?mod=login", METHOD_POST));
				
				// 3. event.51buy.com
				mConfig.put(Config.URL_GET_COUPON_EVTNO, getChild(EVENT_ICSON_COM + "/json.php?mod=coupon&act=get&evtno=", METHOD_POST));
				
				// 6. st.icson.com
				mConfig.put(Config.URL_IMAGE_GUEST, getChild(ST_ICSON_COM + "/static_v1/img/guest/guest", METHOD_GET));
				mConfig.put(Config.URL_AREA_JS, getChild(ST_ICSON_COM + "/static_v1/js/area.js", METHOD_GET));
				
				// 7. buy.51buy.com
			//	mConfig.put(Config.URL_ADD_PRODUCT, getChild(BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=addproduct&uid=", METHOD_POST));
				mConfig.put(Config.URL_GET_PRODUCT_COUPON, getChild(BUY_ICSON_COM + "/json.php?mod=order&act=getcoupon&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_NEW_ORDER, getChild(BUY_ICSON_COM + "/json.php?mod=aorder&act=new&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_CART_UPDATE, getChild(BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=setnum&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_CART_REMOVE, getChild(BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=remove&uid=", METHOD_POST));
			//	mConfig.put(Config.URL_LIST_CART, getChild(BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=list&uid=", METHOD_GET));
			//	mConfig.put(Config.URL_ORDER_ITEMS, getChild(BUY_ICSON_COM + "/json.php?mod=order&act=listcart&uid=", METHOD_POST));
				mConfig.put(Config.URL_LIST_CART_NONMEMBER, getChild(BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=listnotlogin", METHOD_POST));
			//	mConfig.put(Config.URL_LIST_CART_WITH_PROVINCEID, getChild(BUY_ICSON_COM + "/json.php?mod=product&fmt=0", METHOD_POST));
				
				// 8. pay.51buy.com
				mConfig.put(Config.URL_PAY_TRADE, getChild(PAY_ICSON_COM + "/apptrade_", METHOD_GET));
				
				// 9. mc.51buy.com
				mConfig.put(Config.URL_SUBMIT_ORDER, getChild(MC_ICSON_COM + "/json.php?mod=order&act=create&fmt=0&uid=", METHOD_POST));
				mConfig.put(Config.URL_ORDER_GETLIST, getChild(MC_ICSON_COM + "/json.php?mod=order&act=getList", METHOD_POST));
				mConfig.put(Config.URL_ORDER_GETDETAIL, getChild(MC_ICSON_COM + "/json.php?mod=order&act=getDetail", METHOD_POST));
				mConfig.put(Config.URL_ORDER_GETFLOW, getChild(MC_ICSON_COM + "/json.php?mod=order&act=getOrderFlow", METHOD_POST));
				mConfig.put(Config.URL_ORDER_GETTRACE, getChild(MC_ICSON_COM + "/json.php?mod=order&act=getTrace", METHOD_POST));
			//	mConfig.put(Config.URL_ORDER_GETCOUPON, getChild(MC_ICSON_COM + "/json.php?mod=order&act=getCoupon", METHOD_POST));
				mConfig.put(Config.URL_CART_GET_PRODUCT_LIST, getChild(MC_ICSON_COM + "/json.php?mod=shoppingcart&act=getProductsList", METHOD_POST));
				mConfig.put(Config.URL_CART_ADD_PRODUCTS, getChild(MC_ICSON_COM + "/json.php?mod=shoppingcart&act=addProducts", METHOD_POST));
				mConfig.put(Config.URL_CART_ADD_PRODUCT_NOTLOGIN, getChild(MC_ICSON_COM + "/json.php?mod=shoppingcart&act=addProductsNotLogin", METHOD_POST));
				mConfig.put(Config.URL_CART_REMOVE_PRODUCT, getChild(MC_ICSON_COM + "/json.php?mod=shoppingcart&act=remove", METHOD_POST));
				mConfig.put(Config.URL_CART_UPDATE_PRODUCT, getChild(MC_ICSON_COM + "/json.php?mod=shoppingcart&act=setNum", METHOD_POST));
				mConfig.put(Config.URL_CANCEL_ORDER, getChild(MC_ICSON_COM + "/json.php?mod=order&act=cancel", METHOD_POST));
				mConfig.put(Config.URL_ORDER_CONFIRM_NEW, getChild(MC_ICSON_COM + "json.php?mod=order&act=getpackageInfoList&uid=", METHOD_POST));
				//for 拆单
				// 10. mb.51buy.com
				mConfig.put(Config.URL_ADD_COMMENT, getChild(MB_ICSON_COM + "/json.php?mod=review&act=addexperienceOld&uid=", METHOD_POST));
				mConfig.put(Config.URL_PRODUCT_REVIEWS, getChild(MB_ICSON_COM + "/json.php?mod=review&act=getOld", METHOD_GET));
				
				mConfig.put(Config.URL_MB_USER_PROFILE, getChild(MB_ICSON_COM + "/json.php?mod=user&act=profile", METHOD_POST));
				mConfig.put(Config.URL_MB_USER_POINTS, getChild(MB_ICSON_COM + "/json.php?mod=user&act=myintegral", METHOD_POST));
				mConfig.put(Config.URL_MB_USER_BALANCE, getChild(MB_ICSON_COM + "/json.php?mod=user&act=mybalance", METHOD_POST));
				
				mConfig.put(Config.URL_ADDRESS_ADDNEW, getChild(MB_ICSON_COM + "/json.php?mod=address&act=add", METHOD_POST));
				mConfig.put(Config.URL_ADDRESS_DELETE, getChild(MB_ICSON_COM + "/json.php?mod=address&act=del", METHOD_POST));
				mConfig.put(Config.URL_ADDRESS_GETLIST, getChild(MB_ICSON_COM + "/json.php?mod=address&act=get", METHOD_POST));
				mConfig.put(Config.URL_ADDRESS_MODIFY, getChild(MB_ICSON_COM + "/json.php?mod=address&act=modify", METHOD_POST));
				mConfig.put(Config.URL_FAVOR_ADDNEW, getChild(MB_ICSON_COM + "/json.php?mod=myfavor&act=add", METHOD_POST));
				mConfig.put(Config.URL_FAVOR_GETLIST, getChild(MB_ICSON_COM + "/json.php?mod=myfavor&act=get", METHOD_POST));
				mConfig.put(Config.URL_FAVOR_DELETE, getChild(MB_ICSON_COM + "/json.php?mod=myfavor&act=remove", METHOD_POST));
				mConfig.put(Config.URL_APP_LOGIN, getChild(MB_ICSON_COM + "/json.php?mod=login&act=loginIcson", METHOD_POST));
				mConfig.put(Config.URL_INVOICE_ADDNEW, getChild(MB_ICSON_COM + "/json.php?mod=invoice&act=add", METHOD_POST));
				mConfig.put(Config.URL_INVOICE_GETLIST, getChild(MB_ICSON_COM + "/json.php?mod=invoice&act=get", METHOD_POST));
				mConfig.put(Config.URL_INVOICE_MODIFY, getChild(MB_ICSON_COM + "/json.php?mod=invoice&act=modify", METHOD_POST));
				mConfig.put(Config.URL_INVOICE_DELETE, getChild(MB_ICSON_COM + "/json.php?mod=invoice&act=del", METHOD_POST));
				mConfig.put(Config.URL_LOGIN_GETSTATUS, getChild(MB_ICSON_COM + "/json.php?mod=login&act=getStatus", METHOD_POST));
				mConfig.put(Config.URL_QUERY_SUGGEST, getChild(MB_ICSON_COM + "/json.php?mod=search&act=suggest", METHOD_POST));
				mConfig.put(Config.URL_HOME_GETINFO, getChild(MB_ICSON_COM + "/json.php?mod=home&act=getinfo", METHOD_POST));
				mConfig.put(Config.URL_CATEGORY_TREE, getChild(MB_ICSON_COM + "/json.php?mod=category&act=get", METHOD_POST));
				mConfig.put(Config.URL_SEARCH_NEW, getChild(MB_ICSON_COM + "/json.php?mod=Search&act=page", METHOD_POST));
				mConfig.put(Config.URL_CATEGORY_NEW, getChild(MB_ICSON_COM + "/json.php?mod=Class&act=get", METHOD_POST));
				mConfig.put(Config.URL_UNION_LOGIN, getChild(MB_ICSON_COM + "/json.php?mod=login&act=unionLogin", METHOD_POST));
				mConfig.put(Config.URL_GUIDE_GETCOUPON, getChild(MB_ICSON_COM + "/json.php?mod=coupon&act=getNewUserCoupon", METHOD_POST));
				mConfig.put(Config.URL_GUIDE_PLANIMG, getChild(MB_ICSON_COM + "/json.php?mod=roll&act=fetchPlanImage", METHOD_POST));
				mConfig.put(Config.URL_FULL_DISTRICT,  getChild(MB_ICSON_COM + "/json.php?mod=home&act=getFullDistrict", METHOD_POST));
				//wechat login				
				mConfig.put(Config.URL_WECHAT_LOGIN, getChild(MB_ICSON_COM + "/json.php?mod=login&act=weixinlogin", METHOD_POST));
				
				// Slotmachine
				mConfig.put(Config.URL_MB_ROLL_INFO, getChild(MB_ICSON_COM + "/json.php?mod=roll&act=info", METHOD_POST));
				mConfig.put(Config.URL_SLOT_ROLL, getChild(EVENT_ICSON_COM + "/json.php?mod=rewardm&act=lottery", METHOD_POST));
				mConfig.put(Config.URL_MB_ROLL_SHARE, getChild(MB_ICSON_COM + "/json.php?mod=roll&act=reward", METHOD_POST));
				mConfig.put(Config.URL_REWARD_HISTORY, getChild(EVENT_ICSON_COM + "/json.php?mod=rewardm&act=hislist", METHOD_POST));
				mConfig.put(Config.URL_SLOT_BULLETIN, getChild(MB_ICSON_COM + "/json.php?mod=roll&act=bulletin", METHOD_GET));
				mConfig.put(Config.URL_MB_ROLL_LOGIN_NOTICE, getChild(MB_ICSON_COM + "/json.php?mod=roll&act=login_notice", METHOD_GET));
				
				// Others.
				mConfig.put(Config.URL_APP_TRACK, getChild("http://stat.51buy.com/stat.fcg?", METHOD_GET));
				mConfig.put(Config.URL_MSP_ALIPAY, getChild("https://msp.alipay.com/x.htm", METHOD_POST));
			} catch( JSONException aException ) {
				aException.printStackTrace();
			}
		}
	}
	
	private JSONObject getChild(String strUrl, String strMethod) throws JSONException {
		JSONObject pChild = new JSONObject();
		pChild.put(TAG_URL, strUrl);
		pChild.put(TAG_METHOD, strMethod);
		
		return pChild;
	}
	
	/**
	 * Save server configuration to local storage.
	 */
	private boolean saveConfig() {
		if( (0 >= mVersion) || (null == mConfig) || (null == mContext) )
			return false;
		
		boolean bSuccess = false;
		FileOutputStream pOutputStream = null;
		try {
			// Compose root json object
			JSONObject pRoot = new JSONObject();
			pRoot.put(TAG_VERSION, mVersion);
			pRoot.put(TAG_ENABLE, mEnabled);
			if( !TextUtils.isEmpty(mMessage) )
				pRoot.put(TAG_MESSAGE, mMessage);
			pRoot.put(TAG_DATA, mConfig);
			pRoot.put(TAG_AUTO_RELOGIN, mAutoRelogin);
			
			// Save the the output to local storage.
			pOutputStream = mContext.openFileOutput(CACHE_FILE, Context.MODE_PRIVATE);
			pOutputStream.write(pRoot.toString().getBytes());
			bSuccess = true;
		} catch( JSONException aException ) {
			aException.printStackTrace();
		} catch (FileNotFoundException aException) {
			aException.printStackTrace();
		} catch (IOException aException) {
			aException.printStackTrace();
		} finally {
			if( null != pOutputStream ) {
				try {
					pOutputStream.close();
				} catch (IOException aException) {
					aException.printStackTrace();
				}
				pOutputStream = null;
			}
		}
		
		// Save last time tag to preference.
		File pFile = mContext.getFileStreamPath(CACHE_FILE);
		if( null != pFile ) {
			Preference.getInstance().setConfigTag("" + pFile.lastModified());
		}
		
		return bSuccess;
	}
	
	/*
	G.util.token = {
			//给连接加上token
			addToken : function(url,type){
				//type标识请求的方式,jq标识jquery，lk标识普通链接,fr标识form表单,ow打开新窗口
				var token=this.getToken();
				//只支持http和https协议，当url中无协议头的时候，应该检查当前页面的协议头
				if(url=="" || (url.indexOf("://")<0?location.href:url).indexOf("http")!=0){
					return url;
				}
				if(url.indexOf("#")!=-1){
					var f1=url.match(/\?.+\#/);
					 if(f1){
						var t=f1[0].split("#"),newPara=[t[0],"&g_tk=",token,"&g_ty=",type,"#",t[1]].join("");
						return url.replace(f1[0],newPara);
					 }else{
						var t=url.split("#");
						return [t[0],"?g_tk=",token,"&g_ty=",type,"#",t[1]].join("");
					 }
				}
				//无论如何都把g_ty带上，用户服务器端判断请求的类型
				return token==""?(url+(url.indexOf("?")!=-1?"&":"?")+"g_ty="+type):(url+(url.indexOf("?")!=-1?"&":"?")+"g_tk="+token+"&g_ty="+type);
			},
			//获取转换后的token
			getToken : function(){
				var skey=G.util.cookie.get("skey"),
					token=skey==null?"":this.time33(skey);
					return token;
			},
			//skey转token
			time33 : function(str){
				//哈希time33算法
				for(var i = 0, len = str.length,hash = 5381; i < len; ++i){
				   hash += (hash << 5) + str.charAt(i).charCodeAt();
				};
				return hash & 0x7fffffff;
			}
		}
		*/
	private static String getToken() {
		String skey = ILogin.getLoginSkey();
		String token = TextUtils.isEmpty(skey) ? "" : "" + ServiceConfig.getTime33(skey);
		
		return token;
	}
	
	private static long getTime33(String skey) {
		long hash = 5381;
		final int length = TextUtils.isEmpty(skey) ? 0 : skey.length();
		for( int i = 0; i < length; i++ ) {
			hash += (hash << 5) + skey.codePointAt(i);
		}
		
		return hash & 0x7fffffff;
	}
	
	
	public static boolean isAutoRelogin()
	{
		return (mSelf.mAutoRelogin > 0 );
	}
	
	
	// Member instance.
	private static ServiceConfig mSelf = null;
	private Context    mContext;
	private int        mVersion; // Latest time tag.
	private boolean    mEnabled = true;  // Configuration is enabled or not.
	private int        mAutoRelogin = 0; //Only qq has this 
	private String     mMessage = ""; // Message.
	private String     mAlias = "";
	private JSONObject mConfig;
	private String     mHack = null;
	private static final String[] mHacks = {"w3sg", "test", "beta"};
	private final boolean mDebug = false;
	private Ajax   updateCheckAjax;
	
	// Constants definition in Server Adapter.
	private static final String METHOD_GET    = "get";
	private static final String METHOD_POST   = "post";
	private static final String METHOD_WAP    = "wap";
	private static final String METHOD_STREAM = "stream";
	private static final String TAG_DATA      = "data";
	private static final String TAG_VERSION   = "version";
	private static final String TAG_ALIAS     = "alias";
	private static final String TAG_URL       = "url";
	private static final String TAG_METHOD    = "method";
	private static final String TAG_ENABLE    = "enable";
	private static final String TAG_MESSAGE   = "message";
	private static final String TAG_MSG_ARRAY = "msg_arr";
	private static final String TAG_ERR_NO    = "errno";
	private static final String TAG_AUTO_RELOGIN    = "autoResume";
	private static final String CACHE_FILE    = "icson_config.cache";
	
	// Default host configuration.
	private static final String APP_ICSON_COM = "http://app.51buy.com";
	private static final String BASE_ICSON_COM = "http://base.51buy.com";
	private static final String EVENT_ICSON_COM = "http://event.51buy.com";
	//private static final String ITEM_ICSON_COM = "http://item.51buy.com";
	private static final String ST_ICSON_COM = "http://st.icson.com";
	private static final String BUY_ICSON_COM = "http://buy.51buy.com";
	private static final String PAY_ICSON_COM = "http://pay.51buy.com";
	private static final String MB_ICSON_COM = "http://mb.51buy.com";
	private static final String MC_ICSON_COM = "http://mc.51buy.com";
	
	//private static final String GET_INTERFACE_URL = "http://mb.51buy.com/json.php?mod=main&act=getinterface&cfgver=";
	private static final String GET_INTERFACE_URL = "http://mb.51buy.com/json.php?mod=main&act=getinterface_new&app=2&cfgver=";
}
