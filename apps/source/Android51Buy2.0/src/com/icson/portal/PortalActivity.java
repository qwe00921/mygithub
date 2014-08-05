package com.icson.portal;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.icson.R;
import com.icson.home.BannerInfo;
import com.icson.home.HomeConfig;
import com.icson.home.HomeParser;
import com.icson.home.ModuleInfo;
import com.icson.home.ProductInfo;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.preference.Preference;
import com.icson.push.MessageService;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PortalActivity extends BaseActivity {

	private Intent  mIntent = null;
	
	private static final int DEFAULT_STAY = 2500;
	//private static final int MAX_STAY = 9000; //max stay 9 seconds
	
	private ArrayList<String> mPicUrls;
	private ImageLoader mPreLoader;
	private HomeConfig mConfig;
	private HomeParser mParser;
	private IPageCache mPageCache;
	private long       mCheckTimeMark;
	private int        mStayTime;
	private int        mPreIdx;
	
	private boolean bAgree = true;
	private Handler mHandler;
	private Bitmap abm;
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
		
		//always clear 
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, "thirdcallsource", "", false);
		Preference.getInstance().addOpenCount();
		
		if( !IcsonApplication.APP_RUNNING  )
		{
			ImageView imv = null;
			
			abm = PortalUpdater.getAvailPortalBitmap(this);
			mStayTime = PortalUpdater.getShowDuration()*1000;
			if(null!=abm && mStayTime > 0)
			{
				byte nineChunk[] = abm.getNinePatchChunk();
				imv =  new ImageView(this.getBaseContext());//TextView(this.getBaseContext());
				if(null != nineChunk)
				{
					Rect padding = new Rect();	
					NinePatchDrawable nineDrawable = new NinePatchDrawable(getApplicationContext().getResources(),
								abm, nineChunk, padding,null);
					imv.setBackgroundDrawable(nineDrawable);
				}
				else
				{
					imv.setImageBitmap(abm);
					imv.setScaleType(ScaleType.FIT_XY);
				}
			}
			
			if(null!=imv)
			{
				LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				setContentView(imv,pParams);
			}
			else
			{
				setContentView(R.layout.portal_activity, false);
				String strChannel = ToolUtil.getChannel();
				if(strChannel.equals("hiapk"))
				{
					ImageView av = (ImageView) this.findViewById(R.id.channel_head);
					av.setImageResource(R.drawable.shoufa);
					av.setVisibility(View.VISIBLE);
				}
				//if(strChannel.equals("honor3x"))
				{
					if(showPermissionDialog())
					{
						bAgree = false;
					}
				}
			}
			
			if(!bAgree)
				return;
			
			// Update context.
			ServiceConfig.setContext(this.getApplicationContext());
				
			//prestart here
			IcsonApplication.start();
			
			
			if(!checkHomeConfig())
			{
				long cur = System.currentTimeMillis();
				delayFinish(mStayTime - cur +mCheckTimeMark);
			}
		}
		else
		{
			this.handleIntent();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
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
		if(null!=abm && !abm.isRecycled())
			abm.recycle();
		abm = null;
		
		if(null!=mHandler)
			mHandler.removeCallbacks(mRunnable);
		mRunnable = null;
		mParser = null;
		mConfig = null;
		
		if(null!=mPermissionDialog && mPermissionDialog.isShowing())
			mPermissionDialog.dismiss();
		mPermissionDialog = null;
		
		if(null!=mPicUrls)
			mPicUrls.clear();
		
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
		
		Bundle pExtras = (null != mIntent ? mIntent.getExtras() : null);
		if( (strAction.equals(MessageService.NOTIFY_ACTION)) || (strAction.equals(Intent.ACTION_VIEW)) )
		{
			
			Uri pData = (null != mIntent ? mIntent.getData() : null);
			final String strUri = (null != pData ? pData.toString() : "");
			String strKey = null;
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
			
			MainActivity.startActivity(this, MainActivity.TAB_HOME, false, strKey, strUri, pExtras);
		}
		else
		{
			if(null != pExtras && pExtras.containsKey("alipay_user_id"))
			{
				MainActivity.startActivity(this, MainActivity.TAB_HOME, false, null, null, pExtras);
			}
			MainActivity.startActivity(this, MainActivity.TAB_HOME);
		}

		// Destroy the activity instance.
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
	
	
	private void getHomeConfig() {
		if(null == mParser)
			mParser = new HomeParser();
		
		Ajax mRequest = ServiceConfig.getAjax(Config.URL_HOME_GETINFO);
		if(null == mRequest)
		{
			initFinish();
			return;
		}
		//hostlist static page. NO Need send latestcate
		//String strLastCate = RecentCates.getString(1);
		//if( !TextUtils.isEmpty(strLastCate) ) {
		//	mRequest.setData("latestcate", strLastCate);
		//}
		
		String strProvinceIPId = FullDistrictHelper.getProvinceIPId() + "";
		mRequest.setData("fetchCity", TextUtils.isEmpty(strProvinceIPId) ? ""+DispatchFactory.PROVINCE_IPID_SH : strProvinceIPId);
		
		mRequest.setParser(mParser);
		mRequest.setTimeout(5);
		mRequest.setOnSuccessListener(new OnSuccessListener<Object>(){

			@Override
			public void onSuccess(Object v, Response response) {
				if(null==v)
				{
					initFinish();
				}
				else
				{
					mConfig = (HomeConfig) v;
					mPageCache.set(CacheKeyFactory.HOME_CHANNEL_INFO, mParser.getString(),  5 * 60 );
					
					preloadImg(mConfig);
					
				}
			}});
		mRequest.setOnErrorListener(new OnErrorListener(){
			@Override
			public void onError(Ajax ajax, Response responsep) {
				initFinish();
			}});

		
		addAjax(mRequest);
		mRequest.send();
		
	}
	
	
	
	/**  
	* method Name:preloadImg    
	* method Description:  
	* @param mConfig2   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void preloadImg(HomeConfig aConfig) {
		if(null == mPreLoader)
		{
			mPreLoader = HomeImgPreLoader.getWholeLoader(getApplicationContext());
			//delayFinish(mStayTime - mCheckTimeMark);
			//return;
		}
		if(null==mPicUrls)
			mPicUrls = new ArrayList<String>();
		List<BannerInfo> aBanners = (null != mConfig ? mConfig.getBanners() : null);
		for(int idx = 0; null!=aBanners && idx <aBanners.size(); idx++)
		{
			String url = aBanners.get(idx).getPicUrl();
			if(url.startsWith("http") && !mPicUrls.contains(url))
			{
				mPicUrls.add(url);
			}
		}
		
		
		List<ModuleInfo> aEvents = (null != mConfig ? mConfig.mEvents : null);
		for(int idx = 0; null!=aEvents && idx <aEvents.size(); idx++)
		{
			ModuleInfo pEntity = aEvents.get(idx);

			// Get products.
			List<ProductInfo> aProducts = pEntity.mItems;
			if(null == aProducts || aProducts.size()<=0)
				continue;
			//for(ProductInfo info : aProducts)
			//only first one
			ProductInfo info = aProducts.get(0);
			{
				String url = info.getPicUrl();
				if(null!=url && url.startsWith("http")&& !mPicUrls.contains(url))
				{
					mPicUrls.add(url);
					
				}
			}
		}
		if(mPicUrls.size()<=0)
			this.initFinish();
		else
		{
			mPreIdx = 0;
			ImageLoadListener fakeone = new ImageLoadListener(){

				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
				}

				@Override
				public void onError(String strUrl) {
				}};
				
			while(mPreIdx < mPicUrls.size())
			{
				mPreLoader.get(mPicUrls.get(mPreIdx), fakeone);
				mPreIdx++;
			}
			
			long cur = System.currentTimeMillis();
			delayFinish(mStayTime - cur +mCheckTimeMark);
		}
	}

	private boolean checkHomeConfig() {
		if(null == mPageCache)
			mPageCache = new IPageCache();
		
		final boolean bIsExpire = mPageCache.isExpire(CacheKeyFactory.HOME_CHANNEL_INFO);
		String strContent = mPageCache.getNoDelete(CacheKeyFactory.HOME_CHANNEL_INFO);
		if( !TextUtils.isEmpty(strContent) ) {
			if(null == mParser)
				mParser = new HomeParser();
			try {
				mConfig = mParser.parse(strContent);
			} catch (Exception ex) {
				Log.e(LOG_TAG, ex);
				mConfig = null;
			}
		}
		
		Preference pPreference = Preference.getInstance();
		//默认分站id如果变化  可能需要重新拉取.
		if( (null != mConfig &&  ILogin.getSiteId() != mConfig.mSiteId )||
				bIsExpire || TextUtils.isEmpty(strContent) || IcsonApplication.mVersionCode > pPreference.getProjVersion()){
			
			getHomeConfig();
			
			return true;
		}
		//just preload the pics
		else if(null!=mConfig)
		{
			preloadImg(mConfig);
			return true;
		}
		
		return false;
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
		
		if(Preference.getInstance().needToHONOR3Access() || 
				IcsonApplication.mVersionCode > Preference.getInstance().getProjVersion())
		{
			if(null==mPermissionDialog)
			{
				mPermissionDialog = UiUtils.showDialog(PortalActivity.this, 
						R.string.permission_title_honor3, 
						R.string.permission_hint_honor3,
						R.string.permission_agree,
						R.string.permission_disagree,
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
									mPermissionDialog.dismiss();
									Preference.getInstance().setHONOR3Access(Preference.ACCESSED);
								
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


	/*
	private void fetchNextOrFinish() {
		long cur = System.currentTimeMillis();
		//overtime -->force initFinish()
		if(cur - mCheckTimeMark > MAX_STAY)
		{
			this.initFinish();
		}
		else
		{
			mPreIdx++;
			//finish all pics 
			if(mPreIdx >= mPicUrls.size())
			{
				//too quick-->stay for a while
				if(cur - mCheckTimeMark < mStayTime)
					delayFinish(mStayTime - cur +mCheckTimeMark);
				else
					this.initFinish();
			}
			else
				mPreLoader.get(mPicUrls.get(mPreIdx), this);
		}
	}*/
	
	@Override
	public String getActivityPageId() {
		return "";
	}
}
