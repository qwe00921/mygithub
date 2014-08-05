package com.icson.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.icson.preference.Preference;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context aContext, Intent aIntent) 
	{
		if( (null == aContext) || (null == aIntent) )
			return ;
		
		// Check the action type.
		String strAction = aIntent.getAction();
		if( TextUtils.isEmpty(strAction) )
			return ;
		
		// Check the preference value.
		if ( Preference.getInstance().pushMessageEnabled() )
		{
			// Start message service on boot completed.
			PushAssistor.setTask(aContext, true);
		}
	}
}
