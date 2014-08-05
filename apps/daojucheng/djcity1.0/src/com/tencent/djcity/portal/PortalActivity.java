package com.tencent.djcity.portal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.main.MainActivity;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.ServiceConfig;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;



public class PortalActivity extends BaseActivity {

	private Intent  mIntent = null;
	
	private static final int DEFAULT_STAY = 2000;
	//private static final int MAX_STAY = 9000; //max stay 9 seconds
	
	private long       mCheckTimeMark;
	private int        mStayTime;
	
	private boolean bAgree = true;
	private Handler mHandler;
	
	private AppDialog  mPermissionDialog;
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			initFinish();
		}
	};

	

	@Override
	public void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		
		// First check whether application is running.
		mIntent = this.getIntent();
		
		mCheckTimeMark = System.currentTimeMillis();
		
		mStayTime = DEFAULT_STAY;
		isReportPV = false;
		
		
		if( !IcsonApplication.APP_RUNNING  )
		{
			setContentView(R.layout.portal_activity);
			if(showPermissionDialog())
			{
				bAgree = false;
			}
			
			if(!bAgree)
				return;
			
			// Update context.
			ServiceConfig.setContext(this.getApplicationContext());
				
			//prestart here
			IcsonApplication.start();
			
			long cur = System.currentTimeMillis();
			delayFinish(mStayTime - cur +mCheckTimeMark);
		}
		else
		{
			this.handleIntent();
		}
	}
	
	/**  
	* method Name:delayFinish    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void delayFinish(long ltime) {
		if(ltime <=0)
		{
			initFinish();
			return;
		}
		
		if(null == mHandler)
			mHandler = new Handler();
		mHandler.postDelayed(mRunnable, ltime);
	}

	@Override
	protected void onDestroy()
	{
		if(null!=mHandler)
			mHandler.removeCallbacks(mRunnable);
		mRunnable = null;
		
		if(null!=mPermissionDialog && mPermissionDialog.isShowing())
			mPermissionDialog.dismiss();
		mPermissionDialog = null;
		
		super.onDestroy();
		
	}
	/**
	 * handleIntent
	 */
	private void handleIntent()
	{
		String strAction = null != mIntent ? mIntent.getAction() : "";
		if( TextUtils.isEmpty(strAction) )
		{
			finish();
			return ;
		}
		
		Bundle pExtras = null;
		String strKey = null;
		String strUri = null;
		//if( (strAction.equals(MessageService.NOTIFY_ACTION)) || (strAction.equals(Intent.ACTION_VIEW)) )
		if(strAction.equals(Intent.ACTION_VIEW)) 
		{
			Uri pData = (null != mIntent ? mIntent.getData() : null);
			strUri = (null != pData ? pData.toString() : "");
			pExtras = (null != mIntent ? mIntent.getExtras() : null);
			if( (!TextUtils.isEmpty(strUri)))
			{
				if( null == pExtras )
				{
					pExtras = new Bundle();
				}
				if(strUri.startsWith("wap2app:")){
					pExtras.putString(Config.EXTRA_BARCODE, strUri);
					strKey = Config.EXTRA_BARCODE;
				} else if(strUri.startsWith(Config.APP_ID + ":")) {
					pExtras.putString(Config.EXTRA_WEIXIN, strUri);
					strKey = Config.EXTRA_WEIXIN;
				}
			}
		}
		ToolUtil.checkLoginOrRedirect(this, MainActivity.class,pExtras);
		
		finish();
	}

	private void initFinish() {
		if(bAgree)
			handleIntent();
	}

	@Override
	public boolean registerBottomMenu() {
		return false;
	}

	@Override
	public boolean isShowSearchPanel() {
		return false;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
	
    /**
	* 
	* method Name:showPermissionDialog    
	* method Description:  
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	private boolean showPermissionDialog() {
		
		if(Preference.getInstance().permissionNeedAsk() || 
				IcsonApplication.mVersionCode > Preference.getInstance().getProjVersion())
		{
			if(null==mPermissionDialog)
			{
				mPermissionDialog = UiUtils.showDialogWithCheckbox(PortalActivity.this, 
						getString(R.string.permission_title_honor3), 
						getString(R.string.permission_hint_honor3),
						R.string.permission_agree,
						R.string.permission_disagree,
						getString(R.string.no_more_hint),
						new AppDialog.OnClickListener(){
							@Override
							public void onDialogClick(int nButtonId) {
								if (nButtonId == AppDialog.BUTTON_NEGATIVE)
								{
									finish();
									IcsonApplication.exit();
								}
								else if(nButtonId == AppDialog.BUTTON_POSITIVE)
								{
									if(mPermissionDialog.isChecked())
										Preference.getInstance().savePermission();
									mPermissionDialog.dismiss();
									
									bAgree = true;
									long cur = System.currentTimeMillis();
									delayFinish(mStayTime - cur +mCheckTimeMark);
								}
							}
				});
			}
			mPermissionDialog.show();
			
			return true;
		}
		return false;
	}
		
}
