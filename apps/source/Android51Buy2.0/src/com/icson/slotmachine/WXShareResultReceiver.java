package com.icson.slotmachine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseResp;

public class WXShareResultReceiver extends BroadcastReceiver {
	
	private boolean bShareSucc;
	private Activity mActivity;
	public WXShareResultReceiver(Activity activity)
	{
		bShareSucc = false;
		mActivity = activity;
	}
	
	public boolean isShareSucc()
	{
		return bShareSucc;
	}
	
	public void clearShareSucc()
	{
		bShareSucc = false;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int aType = intent.getIntExtra("type", ConstantsAPI.COMMAND_UNKNOWN);
		int aErrcode = intent.getIntExtra("errcode", -1);
		if(aType == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX)
		{
			if(aErrcode == BaseResp.ErrCode.ERR_OK)
			{
				StatisticsEngine.trackEvent(mActivity, "slot_wechat_share");
				bShareSucc = true;
			}
			else
			{
				AppUtils.informWXShareResult(mActivity,aErrcode);
			}
		}
	}
}