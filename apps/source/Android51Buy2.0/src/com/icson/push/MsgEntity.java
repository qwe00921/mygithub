package com.icson.push;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;


import android.text.TextUtils;

final public class MsgEntity implements Serializable
{
	public static final int BIZ_ID_NEW_ARRIVALS  = 1; // Only for new arrivals.
	public static final int BIZ_ID_BASE          = 1000;
	public static final int BIZ_ID_ACTIVATE_APP  = (BIZ_ID_BASE + 1);
	public static final int BIZ_ID_RECHARGE      = (BIZ_ID_BASE + 2);
	public static final int BIZ_ID_COUPON        = (BIZ_ID_BASE + 3);
	public static final int BIZ_ID_EVENT         = (BIZ_ID_BASE + 4);
	public static final int BIZ_ID_SHOW_PAGE     = (BIZ_ID_BASE + 5);
	public static final int BIZ_ID_MSG_CENTER    = (BIZ_ID_BASE + 6);
	public static final int BIZ_ID_PRO_INFO      = (BIZ_ID_BASE + 7); // With multi-price information.
	public static final int BIZ_ID_SLOT_MACHINE  = (BIZ_ID_BASE + 8);
	public static final int BIZ_ID_CMT_BASE      = 2000;
	public static final int BIZ_ID_COMMENT_HINT  = (BIZ_ID_CMT_BASE + 1);
	public static final int BIZ_ID_COMMENT_EXPI  = (BIZ_ID_CMT_BASE + 2);
	public static final int BIZ_ID_PRICE_BASE    = 3000;
	public static final int BIZ_ID_REFUND_ACCEPT = (BIZ_ID_PRICE_BASE + 1);
	public static final int BIZ_ID_REFUND_REFUSE = (BIZ_ID_PRICE_BASE + 2);
	public static final int BIZ_ID_PRICE_MATCH   = (BIZ_ID_PRICE_BASE + 3);
	public static final int BIZ_ID_PRICE_ACCEPT  = (BIZ_ID_PRICE_BASE + 4);
	public static final int BIZ_ID_PRICE_REFUSE  = (BIZ_ID_PRICE_BASE + 5);
	public static final int BIZ_ID_LOGISTICS     = (BIZ_ID_PRICE_BASE + 6);
	public static final int BIZ_ID_MY_ICSON_BASE = 4000;
	public static final int BIZ_ID_MY_POINTS     = (BIZ_ID_MY_ICSON_BASE + 1);
	public static final int BIZ_ID_MY_BALANCE    = (BIZ_ID_MY_ICSON_BASE + 2);
	public static final int BIZ_ID_MY_COUPON     = (BIZ_ID_MY_ICSON_BASE + 3);
	public static final int BIZ_ID_OTHER_BASE 	 = 5000;
	public static final int BIZ_ID_FEEDBACK      = (BIZ_ID_OTHER_BASE + 1);
	public static final int BIZ_ID_SERVICE_CENTER = 6000;
	public static final int BIZ_ID_GOOD_REPAIR 	 = (BIZ_ID_SERVICE_CENTER + 1);
	

	
	// Message status.
	public static final int STATUS_UNREAD  = 0;
	public static final int STATUS_READ    = (STATUS_UNREAD + 1);
	public static final int STATUS_DELETE  = (STATUS_UNREAD + 2);
	
	
	/**
	 * Default constructor of MsgEntity
	 */
	public MsgEntity()
	{
	}
	
	/**
	 * toMessage
	 * Convert the 
	 * @return
	 */
	public String getMessage()
	{
		// Need parse the content for u
		return mMessage;
	}
	
	/**
	 * toJson
	 * @return
	 */
	public JSONObject toJson()
	{
		JSONObject pObject = new JSONObject();
		try {
			pObject.put("msgId", mId);
			pObject.put("msgType", mType);
			pObject.put("status", mStatus);
			pObject.put("eventType", mBizId);
			pObject.put("msgLevel", mLevel);
			pObject.put("reportTime", mTimetag);
			pObject.put("msgJson", mContent);
		} catch (JSONException aException) {
			aException.printStackTrace();
			pObject = null;
		}
		
		return pObject;
	}
	
	/**
	 * parse
	 * @param aObject
	 */
	public boolean parse(JSONObject aObject)
	{
		if( null == aObject )
			return false;
		
		// {"errno":0,"data":[{"msgType":0,"msgLevel":0,"eventType":1,"msgJson":{"msg":"xxxxxxxx已到货","productId":165906,"extra":456}}]}
		// Parse the entity.
		mId = aObject.optInt("msgId");
		mType = aObject.optInt("msgType");
		mStatus = aObject.optInt("status");
		mBizId = aObject.optInt("eventType");
		mLevel = aObject.optInt("msgLevel");
		mTimetag = aObject.optLong("reportTime") * 1000;
		mContent = aObject.optString("msgJson");
		
		return this.parseContext(mContent);
	}
	
	/**
	 * parseContext
	 * @param strContext
	 */
	private boolean parseContext(String strContext)
	{
		if( TextUtils.isEmpty(strContext) )
			return false;
		
		try 
		{
			JSONObject pObject = new JSONObject(strContext);
			mMessage = pObject.optString("msg");
			mCharId = pObject.optString("charId");
			mTitle = pObject.optString("title");
			mUrl = pObject.optString("pic_url");
			mLogin = (1 == pObject.optInt("login"));
			switch( mBizId )
			{
			case BIZ_ID_NEW_ARRIVALS:
			case BIZ_ID_PRO_INFO:
				mValue = pObject.optString("productId");
				mExtra = pObject.optString("extra");
				break;
				
			case BIZ_ID_EVENT:
				mValue = pObject.optString("temple");
				mExtra = pObject.optString("extra");
				break;
				
			case BIZ_ID_SHOW_PAGE:
				mValue = pObject.optString("url");
				break;
				
			case BIZ_ID_COMMENT_HINT:
			case BIZ_ID_COMMENT_EXPI:
			case BIZ_ID_LOGISTICS:
				mValue = pObject.optString("orderId");
				mExtra = pObject.optString("status");
				break;
				
			case BIZ_ID_REFUND_ACCEPT:
			case BIZ_ID_REFUND_REFUSE:
			case BIZ_ID_PRICE_MATCH:
			case BIZ_ID_PRICE_ACCEPT:
			case BIZ_ID_PRICE_REFUSE:
				mValue = pObject.optString("points");
				mExtra = pObject.optString("url");
				break;
			
			case BIZ_ID_GOOD_REPAIR:
				mUid = pObject.optLong("uid");
				mApplyId = pObject.optInt("applyId");
				break;
				
			default:
				// Add default parsing for json object here.
				break;
			}
		}
		catch (JSONException aException)
		{
			aException.printStackTrace();
		}
		
		return true;
	}

	public int     mId;
	public int     mType;
	public int     mLevel;
	public int     mStatus; // 0: unread, 1: read, 2:delete
	public boolean mLogin;
	public String  mContent;
	public String  mCharId;
	public int     mBizId;
	public long    mUid;
	public int     mApplyId;
	public long    mTimetag; // Seconds from server.
	public String  mTitle;
	public String  mMessage;
	public String  mValue;
	public String  mExtra;
	public String  mUrl;
	
	// serialVersionUID
	public static final String SERIAL_NAME_MSGENTITY = "PUSH_MESSAGE_ENTITY";
	private static final long  serialVersionUID      = -3454496717398169402L;
}
