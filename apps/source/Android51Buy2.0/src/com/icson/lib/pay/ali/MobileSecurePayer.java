/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 */

package com.icson.lib.pay.ali;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.alipay.android.app.IAlixPay;
import com.alipay.android.app.IRemoteServiceCallback;
import com.icson.util.ToolUtil;

public class MobileSecurePayer {
	static String TAG = "MobileSecurePayer";

	Integer lock = 0;
	IAlixPay mAlixPay = null;
	boolean mbPaying = false;

	Activity mActivity = null;

	private ServiceConnection mAlixPayConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			//
			// wake up the binder to continue.
			synchronized (lock) {
				mAlixPay = IAlixPay.Stub.asInterface(service);
				lock.notify();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mAlixPay = null;
		}
	};

	public boolean pay(final String strOrderInfo, final Handler callback, final int myWhat, final Activity activity) {
		if (mbPaying)
			return false;
		mbPaying = true;

		//
		mActivity = activity;

		// bind the service.
		Context context = mActivity.getApplicationContext();
		if (mAlixPay == null) {
			boolean ret = false;
			
			//boolean ret = context.bindService(new Intent(IAlixPay.class.getName()), mAlixPayConnection, Context.BIND_AUTO_CREATE);
			if(isAlipayGphoneOK())
			{
				Intent intent = new Intent("com.eg.android.AlipayGphone.IAlixPay");//钱包
				ret = context.bindService(intent, mAlixPayConnection, Context.BIND_AUTO_CREATE);
			}
			else //快捷支付
			{
				Intent intent = new Intent("com.alipay.android.app.IAlixPay");
				ret = context.bindService(intent, mAlixPayConnection, Context.BIND_AUTO_CREATE);
			}
			
			if(!ret)
				return false;
		}
		//else ok.

		new Thread(new Runnable() {
			public void run() {
				try {
					// wait for the service bind operation to completely finished.
					// Note: this is important,otherwise the next mAlixPay.Pay() will fail.
					synchronized (lock) {
						if (mAlixPay == null)
							lock.wait();
					}

					// register a Callback for the service.
					mAlixPay.registerCallback(mCallback);

					// call the MobileSecurePay service.
					String strRet = mAlixPay.Pay(strOrderInfo);

					// set the flag to indicate that we have finished.
					// unregister the Callback, and unbind the service.
					mbPaying = false;
					mAlixPay.unregisterCallback(mCallback);
					Context contxt = mActivity.getApplicationContext();
					contxt.unbindService(mAlixPayConnection);

					// send the result back to caller.
					Message msg = Message.obtain();
					msg.what = myWhat;
					msg.obj = strRet;
					callback.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();

					// send the result back to caller.
					Message msg = Message.obtain();
					msg.what = myWhat;
					msg.obj = e.toString();
					callback.sendMessage(msg);
				}
			}
		}).start();

		return true;
	}

	/**  
	* method Name:isAlipayGphoneOK    
	* method Description:  
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0  
	*/
	private boolean isAlipayGphoneOK() {
		
		PackageInfo packageInfo = ToolUtil.getInstalledPackageInfo(mActivity,
				"com.eg.android.AlipayGphone");
		if(null != packageInfo && packageInfo.versionCode >= 37)
			return true;
		else
			return false;
	}

	/**
	* This implementation is used to receive callbacks from the remote
	* service.
	*/
	private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
		/**
		 * This is called by the remote service regularly to tell us
		 * about new values. Note that IPC calls are dispatched through
		 * a thread pool running in each process, so the code executing
		 * here will NOT be running in our main thread like most other
		 * things -- so, to update the UI, we need to use a Handler to
		 * hop over there.
		 */
		public void startActivity(String packageName, String className, int iCallingPid, Bundle bundle) throws RemoteException {
			Intent intent = new Intent(Intent.ACTION_MAIN, null);

			if (bundle == null)
				bundle = new Bundle();
			// else ok.

			try {
				bundle.putInt("CallingPid", iCallingPid);
				intent.putExtras(bundle);
			} catch (Exception e) {
				e.printStackTrace();
			}

			intent.setClassName(packageName, className);
			mActivity.startActivity(intent);
		}
	};
}