package com.icson.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.my.orderlist.VPOrderListActivity;
import com.icson.portal.PortalActivity;
import com.icson.util.Config;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_transparent);
        api = WXAPIFactory.createWXAPI(this, Config.APP_ID, false);
        Intent ait = getIntent();
        if(null == ait)
        {
        	finish();
        	return;
        }
        api.handleIntent(getIntent(), this);
    }

    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
		api.handleIntent(intent, this);
	}
    
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			Intent newHome =new Intent(this, PortalActivity.class);
			newHome.setAction("WXEntry");
			startActivity(newHome);
			finish();
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			startActivity(new Intent(this, ItemActivity.class));
			finish();
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.getType())
		{
		case ConstantsAPI.COMMAND_PAY_BY_WX:
			handlePayResp(resp);
			break;
		case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
			handleSend2WxResp(resp);
			break;
		case ConstantsAPI.COMMAND_SENDAUTH:
			handleWXLoginResp(resp);
			break;
		
		default:
			break;
		}
		finish();
	}

	/**  
	* method Name:handlePayResp    
	* method Description:  
	* @param resp   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void handlePayResp(BaseResp resp) {
		int strRid = R.string.pay_send_failed;
		switch (resp.errCode) 
		{
        //Pay resp
		case BaseResp.ErrCode.ERR_OK:
			strRid = R.string.pay_succ;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			strRid =R.string.pay_auth_denied;
			break;
		case BaseResp.ErrCode.ERR_UNSUPPORT:
			strRid =R.string.pay_unsupport;
			break;
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			strRid =R.string.pay_send_failed;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			strRid =R.string.pay_cancel;
			break;
		default:
			break;
		}
		
		String orderID  = AppStorage.getData("WXOrder");
		
		AppStorage.setData("WXOrder", "", true);
		
        UiUtils.makeToast(WXEntryActivity.this, strRid);
        if(!TextUtils.isEmpty(orderID) && orderID.endsWith("_1"))
        	UiUtils.startActivity(this, VPOrderListActivity.class, true);
        else
        	MainActivity.startActivity(WXEntryActivity.this, MainActivity.TAB_MY);
    }

	/**  
	* method Name:handleSend2WxResp    
	* method Description:  
	* @param resp   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void handleSend2WxResp(BaseResp resp) 
	{
			Bundle bundle = new Bundle();
			bundle.putInt("type", resp.getType());
	        bundle.putInt("errcode", resp.errCode);
	        bundle.putString("errstr", resp.errStr);
	        Intent aIt = new Intent(Config.BROADCAST_FROM_WXSHARE);
	        aIt.putExtras(bundle);
	        sendBroadcast(aIt,Config.SLEF_BROADCAST_PERMISSION);
	}
	
	
	/*
	 * handle response result of wechat login
	 */
	private void handleWXLoginResp(BaseResp resp) {
		Bundle pBundle = new Bundle();
		pBundle.putInt("type", resp.getType());
		pBundle.putString("code", ((SendAuth.Resp)resp).code);
		pBundle.putString("state", ((SendAuth.Resp)resp).state);
		pBundle.putInt("errCode", resp.errCode);
		
		Intent pIntent = new Intent(Config.BROADCAST_FROM_WXLOGIN);
		pIntent.putExtras(pBundle);
		sendBroadcast(pIntent, Config.SLEF_BROADCAST_PERMISSION);
	}
}

	
