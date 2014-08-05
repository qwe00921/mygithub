package com.icson.login;

import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class WechatLogin {
	private static Activity mActivity;
	private static OnWXLoginResponseListener mOnWXLoginResponseListener;
	private static WXLoginResponseReceiver mWXLoginResponseReceiver;
	
	public static void setOnWXLoginResponseListener(OnWXLoginResponseListener listener, Activity pActivity) {
		if(null == mWXLoginResponseReceiver){
			mWXLoginResponseReceiver = new WXLoginResponseReceiver();
		}
		mOnWXLoginResponseListener = listener;
		if(pActivity != null) {
			if(mActivity == pActivity) { //相同的activity, 说明已经为当前activity注册过，无需再注册
				
			} else { //不同的activity
				if(mActivity != null) { //不是空，说明已经为另一个activity注册过，先注销前一个activity,再注册当前activity
					mActivity.unregisterReceiver(mWXLoginResponseReceiver);
				} else { //前一个activity 为空，说明没有为任何activity注册过，直接注册
					
				}
				
				// 注册receiver
				IntentFilter filter = new IntentFilter();
				filter.addAction(Config.BROADCAST_FROM_WXLOGIN);
				pActivity.registerReceiver(mWXLoginResponseReceiver, filter, Config.SLEF_BROADCAST_PERMISSION,null);
			} 
		}
		
		mActivity = pActivity;
	}
	
	public static void unset(Activity activity) {
		if(activity != null) {
			if(activity != mActivity) {
				return;
			}
			if(mActivity != null) {
				mActivity.unregisterReceiver(mWXLoginResponseReceiver);
			}
			mWXLoginResponseReceiver = null;
			mOnWXLoginResponseListener = null;
			mActivity = null;
		}
	}
	
	
	private static class WXLoginResponseReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int nErrCode = intent.getIntExtra("errCode", ConstantsAPI.COMMAND_UNKNOWN);
			int nType = intent.getIntExtra("type", -1);
			String strCode = intent.getStringExtra("code");
			String strState = intent.getStringExtra("state"); 
			
			if(nType == ConstantsAPI.COMMAND_SENDAUTH)
			{
				if(nErrCode == BaseResp.ErrCode.ERR_OK)
				{
					mOnWXLoginResponseListener.OnWXLoginResponse(strCode, strState);
				}
				else
				{
					AppUtils.informWXLoginResult(mActivity,nErrCode);
				}
			}
		}
	}
	
	public interface OnWXLoginResponseListener {
		void OnWXLoginResponse(String code, String state);
	}

}
