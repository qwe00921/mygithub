package com.icson.push;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.portal.PortalActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class MessageService extends Service implements OnSuccessListener<Object>, OnErrorListener
{
	@Override
	public IBinder onBind(Intent aIntent) 
	{
		return null;
	}
	
	@Override
	public void onCreate()
	{
		setForeground(true);
	//	registerReceiver();
		super.onCreate();
	}
	
	/*
	private void registerReceiver() {
		if( null == mAssistor ) {
			mAssistor = new PushAssistor();
			
			IntentFilter pFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			this.registerReceiver(mAssistor, pFilter);
		}
	}*/
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if( null == mParser ) {
			mParser = new MessageParser();
		}
		
		if( null != mAjax ) {
			mAjax.abort();
			mAjax = null;
		}
		
	//	Toast.makeText(this, "MessageService.onStartCommand", Toast.LENGTH_SHORT).show();

		checkPushMsg();
		
		super.onStartCommand(intent, flags, startId);

		return START_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		mParser = null;
		
		if( null != mAjax ) {
			mAjax.abort();
			mAjax = null;
		}
		
		/*
		if( null != mAssistor ) {
			this.unregisterReceiver(mAssistor);
			mAssistor = null;
		}*/
		
		// Call base implementation.
		super.onDestroy();
	}
	
	/**
	 * doStart
	 * @param aContext
	 */
	static void doStart(Context aContext)
	{
		Intent pIntent = new Intent(aContext, MessageService.class);
		try{
			aContext.startService(pIntent);
		}catch (SecurityException ex)
		{
			Log.e("MessageService", ex);
		}
	}
	
	/**
	 * stop the message service.
	 * @param aContext
	 */
	static void doStop(Context aContext)
	{
		Intent pIntent = new Intent(aContext, MessageService.class);
		aContext.stopService(pIntent);
	}
	
	@Override
	public void onError(Ajax aAjax, Response aResponse) 
	{
		// Reset ajax task.
		mAjax = null;
		
		// Retry.
		mRetry++;
		if( mRetry <= MAX_RETRY_COUNT ) {
			checkPushMsg();
		} else {
			// Stop myself.
			stopSelf();
			mRetry = 0;
		}
	}

	@Override
	public void onSuccess(Object aObject, Response aResponse)
	{	
		@SuppressWarnings("unchecked")		
		ArrayList<MsgEntity> aResult = (ArrayList<MsgEntity>)aObject;
		
		final int nSize = (null != aResult ? aResult.size() : 0);
		Context pContext = getApplicationContext();
		for( int nIdx = 0; nIdx < nSize; nIdx++ )
		{
			MsgEntity pEntity = aResult.get(nIdx);
			notifyMsg(pContext, pEntity, nIdx);
		}
		
		// Reset ajax task.
		mAjax = null;
		
		// Stop myself.
		this.stopSelf();
	}
	
	/**
	 * checkPushMsg
	 */
	private void checkPushMsg()
	{
		if( null != mAjax )
			return ; // Already send message.
		
		ServiceConfig.setContext(this.getApplicationContext());
		mAjax = ServiceConfig.getAjax(Config.URL_PUSHNOTIFY_GET);
		if( null != mAjax ) {
			mAjax.setParser(mParser);
			mAjax.setOnSuccessListener(this);
			mAjax.setOnErrorListener(this);
			
			// Add version, as protocol updated with Terry.
			mAjax.setData("v", IcsonApplication.mVersionCode);
			mAjax.setData("token", StatisticsUtils.getDeviceUid(this));
			//mAjax.setData("token", "4767286565478728650786858721165860801167672807284721102185021");
			
			mAjax.send();
			
			StatisticsEngine.trackEvent(this, "loop_message");
			
			if( Config.DEBUG ) {
				Toast.makeText(this, "MessageService.checkPushMsg(" + ILogin.getLoginUid() + ":"+ StatisticsUtils.getDeviceUid(this)+")", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * notifyMsg
	 * @param aContext
	 * @param strMessage
	 * @param strData
	 * @param nOffset
	 */
	private void notifyMsg(Context aContext, MsgEntity aEntity, int nOffset)
	{
		String strMessage = (null != aEntity ? aEntity.getMessage() : null);
		if ( (null == aContext) || (TextUtils.isEmpty(strMessage)) )
			return ;
		
		// Get the application context.
		Context pAppContext = aContext.getApplicationContext();
		if ( null == pAppContext )
			return ;
		
		// Get the notification manager.
		NotificationManager pManager = (NotificationManager)pAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Update the configuration of the notification.
		final int nIcon = R.drawable.icon;
		String strTricker = aContext.getString(R.string.app_name);
		final long nWhen = System.currentTimeMillis();
		Notification pNotification = new Notification(nIcon, strTricker, nWhen);
		
		// Define the message and pending intent.
		Intent pIntent = new Intent(pAppContext, PortalActivity.class);
		pIntent.setAction(NOTIFY_ACTION);
		pIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		Bundle pBundle = new Bundle();
		pBundle.putSerializable(MsgEntity.SERIAL_NAME_MSGENTITY, aEntity);
		pIntent.putExtras(pBundle);
		final int nId = (int)(System.currentTimeMillis() / 1000) + nOffset;
		PendingIntent pPending = PendingIntent.getActivity(pAppContext, REQUESTCODE_BASE + nId, pIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Update the message with chapter number.
		String strName = TextUtils.isEmpty(aEntity.mTitle) ? aEntity.mTitle : aContext.getString(R.string.app_name);
		pNotification.setLatestEventInfo(pAppContext, strName, strMessage, pPending);
		pNotification.flags = Notification.FLAG_AUTO_CANCEL;
		pNotification.defaults = Notification.DEFAULT_ALL;
		
		// Show the notification to the manager.
		pManager.notify(nId, pNotification);
		
		// Record the notification event.
		StatisticsEngine.trackEvent(aContext, "receive_push", aEntity.mType);
	}
	
	// Member instances.
	private Ajax          mAjax    = null;
	private MessageParser mParser  = null;
	private int           mRetry   = 0;
	
//	static volatile PushAssistor mAssistor = null;
	
	// Constants member instances.
	public  static final String NOTIFY_ACTION     = "com.icson.push.message";
	private static final int    REQUESTCODE_BASE  = 0x10;
	private static final int    MAX_RETRY_COUNT   = 3;
}
