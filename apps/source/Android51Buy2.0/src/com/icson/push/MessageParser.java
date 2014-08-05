package com.icson.push;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.icson.preference.Preference;
import com.icson.util.IcsonApplication;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class MessageParser extends Parser<byte[], ArrayList<MsgEntity>>
{
	@Override
	public ArrayList<MsgEntity> parse(byte[] aInput, String strCharset) throws Exception 
	{
		JSONParser parser = new JSONParser();
		JSONObject pObject = parser.parse(aInput, strCharset);
		
		// Check the error code.
		final int nErrCode = pObject.optInt("errno", -1);
		if( 0 != nErrCode )
		{
			return null;
		}
		
		// Get default number.
		mDefaultNum = 0;
		mDefaultNum = pObject.optInt("defaultNum");
		
		// Parse message interval.
		final int nIntervals = pObject.optInt("interval", 30);
		
		// Get previous value.
		Preference pPreference = Preference.getInstance();
		if( null != pPreference ) {
			final int nPrevious = pPreference.getPushInterval(); // By minutes.
			if( nPrevious != nIntervals ) {
				pPreference.setPushInterval(nIntervals);
				
				mContext = IcsonApplication.app;
				if( null != mContext ) {
					// Reset the push task.
					PushAssistor.killTask(mContext, false);
					
					// Restart the task.
					PushAssistor.setTask(mContext, false);
				}
			}
		}
		
		// Parse the data.
		JSONArray aMsgArray = pObject.getJSONArray("data");
		final int nLength = (null != aMsgArray ? aMsgArray.length() : 0);
		if( 0 >= nLength )
			return null;
		
		ArrayList<MsgEntity> aResult = new ArrayList<MsgEntity>();
		for( int nIdx = 0; nIdx < nLength; nIdx++ )
		{
			JSONObject pChild = aMsgArray.getJSONObject(nIdx);
			MsgEntity pEntity = new MsgEntity();
			if( pEntity.parse(pChild) )
			{
				// Save to array.
				aResult.add(pEntity);
			}
			pEntity = null;
		}
		
		return aResult;
	}
	
	public int mDefaultNum = 0;
	Context    mContext;
}
