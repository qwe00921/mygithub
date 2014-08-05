package com.icson.statistics;

import android.text.TextUtils;

public class RecordEntity 
{
	/**
	 * Default constructor of RecordEntity
	 */
	public RecordEntity(String strApi, int nErrCode, String strErrMsg, 
			            String strNetType, String strNetState, long nUid,
			            long nTimestamp, String strOrderId, String strExtra)
	{
		mApi = strApi;
		mErrCode = nErrCode;
		mErrMsg = strErrMsg;
		mNetType = strNetType;
		mNetState = strNetState;
		mUid = nUid;
		mTimestamp = nTimestamp;
		mOrderId = strOrderId;
		mExtra = strExtra;
	}
	
	/**
	 * toString
	 * Covert the entity to string record.
	 */
	public StringBuffer toBuffer()
	{
		StringBuffer pBuffer = new StringBuffer();
		
		// API
		pBuffer.append(mApi);
		
		// network type.
		pBuffer.append("|");
		pBuffer.append(mNetType);
		
		// network status.
		pBuffer.append("|");
		pBuffer.append(mNetState);
		
		// error code
		pBuffer.append("|");
		pBuffer.append(mErrCode);
		
		// Error message
		pBuffer.append("|");
		pBuffer.append(TextUtils.isEmpty(mErrMsg) ? "" : mErrMsg);
		
		// UID
		pBuffer.append("|");
		pBuffer.append(mUid);
		
		// time-stamp.
		pBuffer.append("|");
		pBuffer.append(mTimestamp);
		
		// order id.
		pBuffer.append("|");
		pBuffer.append(TextUtils.isEmpty(mOrderId) ? "" : mOrderId);
		
		// extent.
		pBuffer.append("|");
		pBuffer.append(TextUtils.isEmpty(mExtra) ? "" : mExtra);
		
		return pBuffer;
	}

	// Member instances.
	protected String  mApi;
	protected int     mErrCode;
	protected String  mErrMsg;
	protected String  mNetType;
	protected String  mNetState;
	protected String  mOrderId;
	protected long    mUid;
	protected long    mTimestamp;
	protected String  mExtra;
}
