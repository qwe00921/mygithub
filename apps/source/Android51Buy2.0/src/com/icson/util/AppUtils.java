package com.icson.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.preference.Preference;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class AppUtils {
	/**
	 * 描述产生器 
	 */
	public interface DescProvider {
		/**
		 * @param strPackageName: 选择分享程序的package包名称
		 * @return
		 */
		public String getDesc(String strPackageName);
	}
	
	/**
	 * showSharableApps
	 * 微信:需要所有的参数
	 * 非微信：只需要前三个参数
	 */
	
	public static void shareAppInfo(final Context aContext, final String strTitle, final String strLinkUrl, final String strPicUrl, final DescProvider aProvider){
		if( (null == aContext) || (null == aProvider) ){
			return ;
		}
		
		Intent pIntent = new Intent(Intent.ACTION_SEND);
		pIntent.putExtra(Intent.EXTRA_TEXT, "");
		pIntent.setType("text/plain");
		
		PackageManager pManager = aContext.getPackageManager();
		List<ResolveInfo> aResult = pManager.queryIntentActivities(pIntent, PackageManager.MATCH_DEFAULT_ONLY);
		final int nSize = (null != aResult ? aResult.size() : 0);
		if( 0 >= nSize )
			return ;
		
		final List<Sharable> aSharables = new ArrayList<Sharable>(nSize);
		for(ResolveInfo pInfo : aResult)
		{
			ApplicationInfo pAppInfo = pInfo.activityInfo.applicationInfo;
			Sharable pEntity = new Sharable();
			pEntity.mLabel = (String) pManager.getApplicationLabel(pAppInfo);
			pEntity.mIcon = pManager.getApplicationIcon(pAppInfo);
			pEntity.mPackageName = pInfo.activityInfo.packageName;
			if(pEntity.mPackageName.equals("com.tencent.mm")
					|| pEntity.mPackageName.equalsIgnoreCase("com.sina.weibo") 
					|| pEntity.mPackageName.equalsIgnoreCase("com.weico.sinaweibo") 
					|| pEntity.mPackageName.equalsIgnoreCase("com.sina.weiboapp")
					|| pEntity.mPackageName.equalsIgnoreCase("com.sina.weibotab") 
					|| (pEntity.mPackageName.equalsIgnoreCase("com.tencent.WBlog")) 
					|| (pEntity.mPackageName.equalsIgnoreCase("com.tencent.microblog")))
			{
				aSharables.add(0, pEntity);
			}else {
				aSharables.add(pEntity);
			}
		}
		
		SharableAdapter pAdapter = new SharableAdapter(aContext, aSharables);
		UiUtils.showListDialog(aContext, aContext.getString(R.string.share_title), pAdapter, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				if( null != aSharables )
				{
					Sharable pSelected = aSharables.get(which);
					// Compose the content for sharing here.
					String strDesc = aProvider.getDesc(pSelected.mPackageName);
					if( !TextUtils.isEmpty(strDesc) ) {
						if(pSelected.mPackageName.equals("com.tencent.mm")){
							AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, strPicUrl,false);
						}else{
							Intent pIntent = new Intent(Intent.ACTION_SEND);
							pIntent.setPackage(pSelected.mPackageName);
							pIntent.putExtra(Intent.EXTRA_TEXT, strDesc);
							pIntent.setType("text/plain");
							aContext.startActivity(pIntent);
						}
					}
				}
			}
		});
	}
	
	
	/**
	 * 
	 * @param aParent
	 * @return
	 */
	public static boolean checkWX(final Activity aParent)
	{
		return checkWX(aParent, 0);
	}
	/**
	 * 
	 * @param aParent
	 * @return
	 */
	public static boolean checkWX(final Activity aParent, int baseApiLevel)
	{
		IWXAPI pWechatApi = WXAPIFactory.createWXAPI(aParent, Config.APP_ID);
		int apiLevel =  pWechatApi.getWXAppSupportAPI();
		if(apiLevel <= baseApiLevel)
		{
			UiUtils.showDialog(aParent, 
					aParent.getString(R.string.no_support_weixin),
					aParent.getString(R.string.install_newest_weixin),
					aParent.getString(R.string.install_weixin_yes),
					aParent.getString(R.string.btn_cancel),
					new AppDialog.OnClickListener() {

						@Override
						public void onDialogClick(int nButtonId) {
							if(nButtonId == DialogInterface.BUTTON_POSITIVE)
							{
								Bundle gjp = new Bundle();
								gjp.putString(HTML5LinkActivity.LINK_URL, "http://weixin.qq.com/");
								gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, "微信下载");
								ToolUtil.startActivity(aParent, HTML5LinkActivity.class, gjp);
							}
							
						}}
				);
			
			return false;
		}
		
		return true;
	}
	
	
	public static void sendWXLogin(final Activity pActivity){
		if(!AppUtils.checkWX(pActivity)) // 版本code 可加
			return;
			
		IWXAPI pWechatApi = WXAPIFactory.createWXAPI(pActivity, Config.APP_ID);
		final SendAuth.Req pReq = new SendAuth.Req();
		pReq.scope = "snsapi_userinfo";
		pReq.state = "yixunlogin";
		
		pWechatApi.sendReq(pReq);
	}
	
	/**
	 * 
	 * @param aParent
	 * @param aErrcode
	 */
	public static void informWXShareResult(final Activity aParent, int aErrcode) {
		String strInfo= "";
		
		 
		if(aErrcode == BaseResp.ErrCode.ERR_UNSUPPORT)
		{
			UiUtils.showDialog(aParent, 
					aParent.getString(R.string.no_support_weixin),
					aParent.getString(R.string.install_newest_weixin),
					aParent.getString(R.string.install_weixin_yes),
					aParent.getString(R.string.btn_cancel),
					new AppDialog.OnClickListener() {

						@Override
						public void onDialogClick(int nButtonId) {
							if (nButtonId == AppDialog.BUTTON_POSITIVE)
							{
								Bundle gjp = new Bundle();
								gjp.putString(HTML5LinkActivity.LINK_URL, "http://weixin.qq.com/");
								gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, "微信下载");
								ToolUtil.startActivity(aParent, HTML5LinkActivity.class, gjp);
							}
							
						}}
				);
			
			return;
		}
		
		switch (aErrcode)
		{
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			strInfo = "\n" + aParent.getString(R.string.share_fail_title) + "\n\n"
					+ aParent.getString(R.string.share_auth_denied) +"\n";
			break;
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			strInfo = "\n" + aParent.getString(R.string.share_fail_title) + "\n\n"
				+ aParent.getString(R.string.share_fail_net) +"\n";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			strInfo = "\n" + aParent.getString(R.string.share_fail_title) + "\n\n"
				+ aParent.getString(R.string.share_user_cancel) +"\n";
			break;
		case BaseResp.ErrCode.ERR_OK:
			strInfo = aParent.getString(R.string.share_succ_title);
			break;
		}
		UiUtils.makeToast(aParent, strInfo);
	}
	
	
	/**
	 * 
	 * @param aParent
	 * @param aErrcode
	 */
	public static void informWXLoginResult(final Activity aParent, int aErrcode) {
		String strInfo= "";
		 
		if(aErrcode == BaseResp.ErrCode.ERR_UNSUPPORT)
		{
			UiUtils.showDialog(aParent, 
					aParent.getString(R.string.no_support_weixin),
					aParent.getString(R.string.install_newest_weixin),
					aParent.getString(R.string.install_weixin_yes),
					aParent.getString(R.string.btn_cancel),
					new AppDialog.OnClickListener() {

						@Override
						public void onDialogClick(int nButtonId) {
							if (nButtonId == AppDialog.BUTTON_POSITIVE)
							{
								Bundle gjp = new Bundle();
								gjp.putString(HTML5LinkActivity.LINK_URL, "http://weixin.qq.com/");
								gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, "微信下载");
								ToolUtil.startActivity(aParent, HTML5LinkActivity.class, gjp);
							}
							
						}}
				);
			
			return;
		}
		
		switch (aErrcode)
		{
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			strInfo = "\n" + aParent.getString(R.string.login_fail_title) + "\n\n"
					+ aParent.getString(R.string.login_auth_denied) +"\n";
			break;
		case BaseResp.ErrCode.ERR_SENT_FAILED:
			strInfo = "\n" + aParent.getString(R.string.login_fail_title) + "\n\n"
				+ aParent.getString(R.string.login_fail_net) +"\n";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			strInfo = "\n" + aParent.getString(R.string.login_fail_title) + "\n\n"
				+ aParent.getString(R.string.login_user_cancel) +"\n";
			break;
		default:
			strInfo = aParent.getString(R.string.login_fail_title);
			break;
		}
		
		UiUtils.makeToast(aParent, strInfo);
	}
	
	/**
	 * @param aContext
	 * @param strTitle
	 * @param strLinkUrl
	 * @param strPicUrl
	 * @param aProvider
	 */
	public static void shareSlotInfo(final Context aContext, final String strTitle, final String strLinkUrl, final String strPicUrl, final DescProvider aProvider){
		if( (null == aContext) || (null == aProvider) ){
			return ;
		}
		
		IWXAPI pWechatApi = WXAPIFactory.createWXAPI(aContext, Config.APP_ID);
		int apiLevel =  pWechatApi.getWXAppSupportAPI();
		if(apiLevel <=0)
		{
			return;
		}
		//0x21020001
		//just 需要微信 ＋ 微信朋友圈
		/*
		PackageManager pManager = aContext.getPackageManager();
		PackageInfo pInfo = null;
		try 
		{
			pInfo = pManager.getPackageInfo("com.tencent.mm", 0);
		}
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
			pInfo = null;
			return;
		}*/
		if(apiLevel <0x21020001)
		{
			String strDesc = aProvider.getDesc("com.tencent.mm");
			AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, strPicUrl,false);
			return;
		}
	
		final List<Sharable> aSharables = new ArrayList<Sharable>(2);
		Sharable pEntity = new Sharable();
		pEntity.mPackageName = "com.tencent.mm";
		pEntity.mLabel = aContext.getString(R.string.weixin_someone);//(String) pManager.getApplicationLabel(pInfo.applicationInfo);
		pEntity.mIcon = aContext.getResources().getDrawable(R.drawable.share_to_weixin);
		aSharables.add(pEntity);
		
		pEntity = new Sharable();
		pEntity.mPackageName = "com.tencent.mm";
		pEntity.mLabel = aContext.getString(R.string.weixin_circle);
		pEntity.mIcon = aContext.getResources().getDrawable(R.drawable.share_to_time_line_icon);
		aSharables.add(0,pEntity);
		
	
		SharableAdapter pAdapter = new SharableAdapter(aContext, aSharables);
		UiUtils.showListDialog(aContext, aContext.getString(R.string.share_title), pAdapter, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				if( null != aSharables )
				{
					Sharable pSelected = aSharables.get(which);
					// Compose the content for sharing here.
					String strDesc = aProvider.getDesc(pSelected.mPackageName);
					if( !TextUtils.isEmpty(strDesc) ) {
						if(pSelected.mPackageName.equals("com.tencent.mm"))
						{
							if(which==0)//time_line
								AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, strPicUrl,true);
							else
								AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, strPicUrl,false);
						}
					}
				}
			}
		});
	}
	

	public static void shareSlotInfo(final Context aContext, final String strTitle, final String strLinkUrl, 
			final int aImgRid, final DescProvider aProvider) {
		
		if( (null == aContext) || (null == aProvider) ){
			return ;
		}
		
		Bitmap drawImg = ImageHelper.getResBitmap(aContext, aImgRid);
		//Drawable drawImg = BitmapFactory.decodeResource(aImgRid, id);//aContext.getResources().getDrawable(aImgRid);
		//Bitmap   aImgBM;
		if(null!=drawImg)
		{
			AppUtils.shareSlotInfo(aContext,strTitle,strLinkUrl, drawImg ,aProvider, true);
		}
	}
	/**
	 * 
	* method Name:shareSlotInfo    
	* method Description:  
	* @param aContext
	* @param strTitle
	* @param strLinkUrl
	* @param aImg
	* @param aProvider   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public static void shareSlotInfo(final Context aContext, final String strTitle, final String strLinkUrl, 
			final Bitmap aImg, final DescProvider aProvider, final boolean needRecycle){
		if( (null == aContext) || (null == aProvider) ){
			return ;
		}
		
		IWXAPI pWechatApi = WXAPIFactory.createWXAPI(aContext, Config.APP_ID);
		int apiLevel =  pWechatApi.getWXAppSupportAPI();
		if(apiLevel <=0)
		{
			return;
		}
		
		//just 需要微信 ＋ 微信朋友圈
		/*PackageManager pManager = aContext.getPackageManager();
		//PackageInfo pInfo = null;
		try 
		{
			PackageInfo pInfo = pManager.getPackageInfo("com.tencent.mm", 0);
		}
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
			//pInfo = null;
			return;
		}
		*/
		//dont support Time_Line 
		if(apiLevel <0x21020001)
		{
			String strDesc = aProvider.getDesc("com.tencent.mm");
			AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, aImg,false,needRecycle);
			return;
		}
	
		final List<Sharable> aSharables = new ArrayList<Sharable>(2);
		Sharable pEntity = new Sharable();
		pEntity.mPackageName = "com.tencent.mm";
		pEntity.mLabel = aContext.getString(R.string.weixin_someone);//(String) pManager.getApplicationLabel(pInfo.applicationInfo);
		pEntity.mIcon = aContext.getResources().getDrawable(R.drawable.share_to_weixin);
		aSharables.add(pEntity);
		
		pEntity = new Sharable();
		pEntity.mPackageName = "com.tencent.mm";
		pEntity.mLabel = aContext.getString(R.string.weixin_circle);
		pEntity.mIcon = aContext.getResources().getDrawable(R.drawable.share_to_time_line_icon);
		aSharables.add(0,pEntity);
		
		SharableAdapter pAdapter = new SharableAdapter(aContext, aSharables);
		UiUtils.showListDialog(aContext, aContext.getString(R.string.share_title), pAdapter, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				if( null != aSharables )
				{
					Sharable pSelected = aSharables.get(which);
					// Compose the content for sharing here.
					String strDesc = aProvider.getDesc(pSelected.mPackageName);
					if( !TextUtils.isEmpty(strDesc) ) {
						if(pSelected.mPackageName.equals("com.tencent.mm"))
						{
							if(which==0)//time_line
								AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, aImg,true, needRecycle);
							else
								AppUtils.sendToWX(aContext, strDesc, strTitle, strLinkUrl, aImg,false, needRecycle);
						}
					}
				}
			}
		});
	}
	
	
	public static void showWXShareReward(Context aContext, boolean bFlag) {
		
		UiUtils.makeSlotToast(aContext, R.drawable.frog_mouth_happy,
				aContext.getString(R.string.share_succ_title),
				bFlag ? aContext.getString(R.string.share_succ_reward) : aContext.getString(R.string.share_succ_empty));
	}
	
	private static void sendToWX(final Context aContext, final String strDesc, final String strTitle, 
			final String strLinkUrl,  final String strPicUrl,
			final boolean bTimeLineFlag){
		// Check the bitmap.
		final ImageLoader pLoader = new ImageLoader(aContext, Config.CHANNEL_PIC_DIR,false);
		final int nMaxSize = 80;
		Bitmap pThumb = pLoader.getBitmap(strPicUrl, nMaxSize);
		if( null == pThumb ) {
			// Try to download the real image.
			final ProgressDialog pDialog = AppUtils.showProgressDialog(aContext);
			pLoader.get(strPicUrl, new ImageLoadListener(){
				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
					// 1. Resize the bitmap.
					Bitmap pResult = pLoader.resize(aBitmap, nMaxSize);
					
					AppUtils.hideProgressDialog(pDialog);
					
					// 2. Send request.
					AppUtils.sendWechatReq(aContext, strDesc, strTitle, strLinkUrl, pResult,bTimeLineFlag,true);
					pLoader.cleanup();
				}

				@Override
				public void onError(String strUrl) {
					Bitmap pDefault = ImageHelper.getResBitmap(aContext, R.drawable.icon);
					AppUtils.hideProgressDialog(pDialog);
					
					AppUtils.sendWechatReq(aContext, strDesc, strTitle, strLinkUrl, pDefault,bTimeLineFlag,true);
					
					pLoader.cleanup();
				}
			});
			
		} else {
			AppUtils.sendWechatReq(aContext, strDesc, strTitle, strLinkUrl, pThumb,bTimeLineFlag,true);
			pLoader.cleanup();
		}
	}
	
	private static void sendToWX(final Context aContext, final String strDesc, final String strTitle, 
			final String strLinkUrl,  final Bitmap aImg, final boolean bTimeLineFlag, boolean needRecycle){
		// Check the bitmap.
		if(null == aImg)
		{
			UiUtils.makeToast(aContext,R.string.wx_share_thumb_fail);
			return;
		}
		  
		//final int nMaxSize = 80;
		//Bitmap pThumb =  ImageHelper.resizeBitmap(aImg, nMaxSize,false);
		AppUtils.sendWechatReq(aContext, strDesc, strTitle, strLinkUrl, aImg,bTimeLineFlag,needRecycle);
	}
	
	/**
	 * Send wechat share content with bitmap.
	 * @param aContext
	 * @param strDesc
	 * @param strTitle
	 * @param strLinkUrl
	 * @param aThumb
	 */
	private static void sendWechatReq(Context aContext, String strDesc, String strTitle, String strLinkUrl, Bitmap aThumb,
			boolean bTimeLineFlag, boolean needRecycle) {
		if( null == aThumb )
			return ;
		
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = strLinkUrl;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = strTitle;
		msg.description = strDesc;
		msg.thumbData = ToolUtil.bmpToByteArray(aThumb, needRecycle);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis()); // transaction�ֶ�����Ψһ��ʶһ������
		req.message = msg;
		if(bTimeLineFlag)
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		else
			req.scene = SendMessageToWX.Req.WXSceneSession;
		// Create wechat api.
		IWXAPI pWechatApi = WXAPIFactory.createWXAPI(aContext, Config.APP_ID);
		if( null != pWechatApi ) {
			pWechatApi.sendReq(req);
		}

		pWechatApi = null;
	}
	
	private static ProgressDialog showProgressDialog(Context aContext) {
		ProgressDialog pProgressDialog = new ProgressDialog(aContext);
		pProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		
		String strTitle = aContext.getString(R.string.share_loading);

		pProgressDialog.setTitle(strTitle);

		// mProgressDialog.setIcon(R.drawable.icon);//设置图标
		pProgressDialog.setMessage(aContext.getString(R.string.initializing_content));

		pProgressDialog.setIndeterminate(true);// 设置进度条是否为不明确
		pProgressDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		pProgressDialog.show();
		
		return pProgressDialog;
	}
	
	private static void hideProgressDialog(ProgressDialog aDialog) {
		if( null != aDialog )
		{
			aDialog.cancel();
			aDialog = null;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	/**
	* Implementation for Sharable list
	*
	*/
	public static class Sharable
	{
		public String   mLabel;
		public String   mPackageName;
		public Drawable mIcon;
	}
	
	private static class SharableHolder
	{
		public ImageView  mIcon;
		public TextView   mLabel;
	}
	
	public static class SharableAdapter extends RadioDialog.RadioAdapter
	{
		public SharableAdapter(Context aContext, List<Sharable> aSharables)
		{
			super(aContext);
			mSharables = aSharables;
		}
		
		@Override
		public int getCount() 
		{
			return (null != mSharables ? mSharables.size() : 0);
		}
		
		@Override
		public Object getItem(int position) 
		{
			return null;
		}
	
		@Override
		public long getItemId(int position) 
		{
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			SharableHolder holder = null;
			if (null == convertView)
			{
				convertView = View.inflate(mContext, R.layout.share_item, null);
				holder = new SharableHolder();
				holder.mIcon = (ImageView) convertView.findViewById(R.id.item_icon);
				holder.mLabel = (TextView) convertView.findViewById(R.id.item_label);
				convertView.setTag(holder);
			}
			else
			{
				holder = (SharableHolder) convertView.getTag();
			}
		
			// set data
			if(null != mSharables)
			{
				Sharable pEntity = mSharables.get(position);
				holder.mLabel.setText(pEntity.mLabel);
				holder.mIcon.setImageDrawable(pEntity.mIcon);
			}
			return convertView;
		}
	
		@Override
		public int getItemViewType(int position) {
			return 0;
		}
		
		@Override
		public int getViewTypeCount() {
			return 1;
		}
		
		@Override
		public boolean hasStableIds() {
			return false;
		}
	
		@Override
		public boolean isEmpty() {
			return false;
		}
		
		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		
		}
		
		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		
		}
	
		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return true;
		}
	
		private List<Sharable>          mSharables;
	}
	
	/**
	 * Instance of AppUtils is forbidden
	 */
	private AppUtils() {
	}


	public static boolean checkAndCall(final Context aContext, final Intent intent) {
		if(aContext == null || null == intent)
			return false;
		
		if(null != intent.resolveActivity(aContext.getPackageManager())) {
			
			if(Preference.getInstance().needCallAccess())
			{
				UiUtils.showDialog(aContext,
					R.string.permission_title, R.string.permission_hint_call,R.string.permission_agree, R.string.permission_disagree,
					new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if (nButtonId == AppDialog.BUTTON_POSITIVE)
						{
							Preference.getInstance().setCallAccess(Preference.ACCESSED);
							aContext.startActivity(intent);
						}
					}
				});
			}
			else
			{
				aContext.startActivity(intent);
			}
		}else
		{
			UiUtils.makeToast(aContext, R.string.phone_app_not_found);
		}
		
		return true;
		
	}

	
	
}
