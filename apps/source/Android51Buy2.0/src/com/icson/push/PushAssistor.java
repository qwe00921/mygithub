package com.icson.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.icson.preference.Preference;

public class PushAssistor extends BroadcastReceiver {
	@Override
	public void onReceive(Context aContext, Intent aIntent) 
	{
		if( (null == aContext) || (null == aIntent) )
			return ;
		
		
		
		// Check preference.
		String strAction = aIntent.getAction();
		
	//	Toast.makeText(aContext, "PushAssistor.onReceive(" + strAction + ")", Toast.LENGTH_SHORT).show();
		
		if( Preference.getInstance().pushMessageEnabled() ) {
			if( strAction.equalsIgnoreCase(ALARM_ACTION) ) {
				// Start the alarm service.
				MessageService.doStart(aContext);
			} else if( strAction.equals(Intent.ACTION_SCREEN_ON) ) {
				PushAssistor.setTask(aContext, true);
			}
		}
	}
	
	/**
	 * set alarm task
	 * @param aContext
	 */
	public static void setTask(Context aContext, boolean bRightNow) {
		if( Preference.getInstance().pushMessageEnabled() ) {
			AlarmManager pAlarmMgr = (AlarmManager)aContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			PendingIntent pPending = PushAssistor.getPendingIntent(aContext, ALARM_ACTION);
			
			// Calculate delay.
			final int nMinutes = Preference.getInstance().getPushInterval();
			long nIntervalMs = nMinutes > 0 ? ((long)nMinutes) * 60 * 1000 : CHECK_INTERVAL_MS;
			long nTriggerMs = bRightNow ? CHECK_NOW_DELAY_MS : nIntervalMs;
		//	nIntervalMs = CHECK_NOW_DELAY_MS;
		//	nTriggerMs = CHECK_NOW_DELAY_MS;
		//	pAlarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + nIntervalMs, pPending);
			pAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + nTriggerMs, nIntervalMs, pPending);
		}
	}
	
	/**
	 * kill task
	 * @param aContext
	 */
	public static void killTask(Context aContext, boolean bStopService) {
		AlarmManager pAlarmMgr = (AlarmManager)aContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		PendingIntent pPending = PushAssistor.getPendingIntent(aContext, ALARM_ACTION);
		pAlarmMgr.cancel(pPending);
		
		if( bStopService ) {
			// Stop the service.
			MessageService.doStop(aContext);
		}
	}
	
	/**
	 * get intent
	 * @param aContext
	 * @param strAction
	 * @return
	 */
	private static PendingIntent getPendingIntent(Context aContext, String strAction)
	{
		Context pAppCxt = aContext.getApplicationContext();
		Intent pIntent = new Intent(pAppCxt, PushAssistor.class);
		pIntent.setAction(strAction);
		PendingIntent pPending = PendingIntent.getBroadcast(pAppCxt, 0, pIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return pPending;
	}

	private static final long   CHECK_NOW_DELAY_MS = 10 * 1000;
	private static final long   CHECK_INTERVAL_MS  = 30 * 60 * 1000; // 30 minutes
	private static final String ALARM_ACTION      = "com.icson.push.alarm";
}
