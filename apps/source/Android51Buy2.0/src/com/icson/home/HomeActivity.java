package com.icson.home;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.icson.R;
import com.icson.event.EventActivityFactory;
import com.icson.event.EventBaseActivity;
import com.icson.event.TimeBuyModel;
import com.icson.home.BgUpdater.BGListener;
import com.icson.hotlist.HotlistActivity;
import com.icson.item.ItemActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.guide.UserGuideDialog;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.inc.DispatchFactory.DispatchItem;
import com.icson.lib.model.SearchModel;
import com.icson.lib.ui.AnnounceView;
import com.icson.lib.ui.AnnounceView.OnAnnounceClickListener;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.EventView;
import com.icson.lib.ui.EventsPanel;
import com.icson.lib.ui.MyScrollView;
import com.icson.lib.ui.PageIndicator;
import com.icson.lib.ui.ProInfoView;
import com.icson.lib.ui.ShortcutView;
import com.icson.lib.ui.SlideView;
import com.icson.lib.ui.SlideView.OnSlideEventListener;
import com.icson.lib.ui.TimeBuyPanel;
import com.icson.lib.ui.UiUtils;
import com.icson.list.ListActivity;
import com.icson.login.LoginActivity;
import com.icson.main.MainActivity;
import com.icson.message.MessageActivity;
import com.icson.more.AppInfoActivity;
import com.icson.my.collect.MyCollectActivity;
import com.icson.my.coupon.CouponShowActivity;
import com.icson.my.main.MyIcsonActivity;
import com.icson.portal.HomeImgPreLoader;
import com.icson.preference.Preference;
import com.icson.push.MsgEntity;
import com.icson.search.SearchActivity;
import com.icson.slotmachine.SlotMachineActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AjaxUtil;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.icson.virtualpay.VirtualPayActivity;
import com.icson.yiqiang.YiQiangActivity;
import com.icson.zxing.client.CaptureActivity;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class HomeActivity extends BaseActivity implements
		OnSuccessListener<Object>, OnSlideEventListener, BGListener, SensorEventListener {
	private Handler mWholeHandler = new Handler();
	private ImageLoader mPreLoader;

	private EventsPanel mEvents;
	private TimeBuyPanel mTimeBuy;
	public static int mTimeBuyType = ModuleInfo.MODULE_ID_MORNING;
	private SlideView mScrollView;

	private AnnounceView mAppAnnounce;
	private PageIndicator mIndicator;
	private ShortcutView mShortcuts;
	private ImageView mNewProduct; // New product information
	private LinearLayout mRootLayout;
	private LinearLayout mFooterLayout;
	private MyScrollView mRootView;
	private LinearLayout mHeaderLoadingLayout;
	// private int mHeaderHeight;
	private TextView mFooters[];

	private boolean mFirstEntry = true;
	private Intent mIntent;
	private MsgEntity mEntity = null;

	private static final int AJAX_EVENT = 1;
	private static final int AJAX_DISPATCHES = 2;
	private static final int AJAX_RELOAD = 3;

	private List<ProInfoView> mCache; // To avoid multi-adding.
	private HomeConfig mConfig;
	private HomeParser mParser;
	private DispatchesParser mDispatchesParser;
	private IPageCache mPageCache;
	private boolean isNullHomeInformtion = false;
	private Ajax mRequest = null;

	// private ImageView mFirstSight1;
	// private OnTouchListener mFirstListener;
	private Runnable mFadingRunnable;
	private Runnable mStartAnimRunnable;

	private List<BannerInfo> mLastBanners;
	private List<ModuleInfo> mLastEvents;
	private List<ProductInfo> mLastSnapup; 
	private List<ModuleInfo> mLastRecommend;
	private String []        mLastBottomAds;
	private SensorManager    mTempManager; // just do ti for release SensorManager from slotmachine. Otherwise context is binded, meme will leak
	private UserGuideDialog  mGuideDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Save the current intent.
		mIntent = this.getIntent();
		if(null == mIntent)
		{
			finish();
			return;
		}
		
		super.onCreate(savedInstanceState);

		mPageCache = new IPageCache();
		mPreLoader = HomeImgPreLoader.getWholeLoader(getApplicationContext());
				
		setContentView(R.layout.activity_home);
		mRootView = (MyScrollView) this.findViewById(R.id.root_layout);
		// Set onScroll listener.
		mRootView.setOnScrollListener(new MyScrollView.OnScrollListener() {
			@Override
			public void onScroll(boolean bIsScrolling) {
				if (bIsScrolling) {
					// android.util.Log.d("HomeActivityScroll",
					// "Scrolling now");
					stopAnimation();
				} else {
					// android.util.Log.d("HomeActivityScroll",
					// "Scrolling end");
					startAnimationDelay();
				}
			}
		});

		mRootLayout = (LinearLayout) findViewById(R.id.home_scrollvew_root_child);

		// Timebuy,抢频道，里面有抢购，天黑黑，团购等
		mTimeBuy = (TimeBuyPanel) findViewById(R.id.home_panel_rush);
		mTimeBuy.setHandler(mWholeHandler);
		mTimeBuy.setImageLoader(mPreLoader);
		mTimeBuy.setOnItemClickListener(new TimeBuyPanel.OnItemClickListener() {
			@Override
			public void onItemClick(View aView, Object aTag) {
				// goneFirstSight();

				if (null == aTag) {
					ToolUtil.reportStatisticsClick(getActivityPageId(), "22001");
					
					StatisticsEngine.trackEvent(HomeActivity.this,
							"click_qiang");
					StatisticsEngine.trackEvent(HomeActivity.this,
							"homepage_click_qiang");
					startYiQiang(HomeActivity.this, HomeActivity.mTimeBuyType,
							YiQiangActivity.PARAM_TAB_QIANG);
					ToolUtil.sendTrack(this.getClass().getName(),
							getString(R.string.tag_Home),
							YiQiangActivity.class.getName(),
							getString(R.string.tag_YiQiangActivity), "04010");
				} else {
					ModuleInfo pInfo = (ModuleInfo) aTag;
					HomeActivity.this.handleEvent(pInfo);
				}
			}
		});

		// Initialize popular information，热门活动

		mEvents = (EventsPanel) findViewById(R.id.home_panel_popular);
		// mEvents.setHandler(mWholeHandler);
		mEvents.setImageLoader(mPreLoader);
		mEvents.setOnEventClickListener(new EventsPanel.OnEventClickListener() {
			@Override
			public void onEventClick(View v, int position, Object aTag) {
				// goneFirstSight();
				ToolUtil.reportStatisticsClick(getActivityPageId(), "23012");
				
				handleEvent(aTag);
			}
		});

		mDispatchesParser = new DispatchesParser();

		// Initialize the instance for scroll view.页面最上方的滚动活动页
		mScrollView = (SlideView) findViewById(R.id.home_banner_slide_view);
		mScrollView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		mScrollView.setHandler(this.mWholeHandler);
		mScrollView.setImageLoader(mPreLoader);
		mScrollView.setOnSlideEventListener(this);

		// Initialize indicator for banners.
		mIndicator = (PageIndicator) findViewById(R.id.home_banner_indicator);
		mIndicator.setDotDrawableRes(R.drawable.page_indicator);
		mIndicator.setVisibility(View.GONE);

		// Search box.,搜索框
		EditText searchBox = (EditText) findViewById(R.id.home_search_box);

		if (null != searchBox) {
			searchBox.setKeyListener(null);
			searchBox.setOnClickListener(this);
		}

		findViewById(R.id.home_search_box_barcode).setOnClickListener(this);
		
		// Announcement.
		mAppAnnounce = (AnnounceView) findViewById(R.id.home_app_announce);
		mAppAnnounce.setOnAnnounceClickListener(new OnAnnounceClickListener() {
			@Override
			public void onAnnounceClick(boolean isClose) {
				// goneFirstSight();
				ToolUtil.reportStatisticsClick(getActivityPageId(), "21004");
				
				if (isClose && null != mAppAnnounce) {
					mAppAnnounce.setVisibility(View.GONE);
					Announce pAnnounce = (null != mConfig.mAnnounce ? mConfig.mAnnounce
							: null);
					if (null != pAnnounce) {
						Preference.getInstance().setAnnounceID(
								pAnnounce.getAnnounceId());
					}
				}
			}
		});

		// Shortcut,有彩票，天天摇的那个
		mShortcuts = (ShortcutView) findViewById(R.id.home_shortcuts);
		mShortcuts
				.setOnShortcutSelectListener(new ShortcutView.onShotcutSelectListner() {
					@Override
					public void onShortcutSelect(int nIndex) {
						// goneFirstSight();

						switch (nIndex) {
						case 0:
							ToolUtil.reportStatisticsClick(getActivityPageId(), "23006");
							
							UiUtils.startActivity(HomeActivity.this,
									SlotMachineActivity.class, true);
							StatisticsEngine.trackEvent(HomeActivity.this,
									"slotmachine_click");
							ToolUtil.sendTrack(
									this.getClass().getName(),
									getString(R.string.tag_Home),
									SlotMachineActivity.class.getName(),
									getString(R.string.tag_SlotMachineActivity),
									"04011");
							break;
						case 1:
							ToolUtil.reportStatisticsClick(getActivityPageId(), "23005");
							
							MainActivity.startActivity(HomeActivity.this,
									MainActivity.TAB_MY);
							StatisticsEngine.trackEvent(HomeActivity.this, "view_my_order");
							ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_Home), MyIcsonActivity.class.getName(), getString(R.string.tag_MyIcsonActivity), "04012");
							break;
						case 2:
						{
							ToolUtil.reportStatisticsClick(getActivityPageId(), "23004");
							
							String strUrl = "http://518.qq.com/go.xhtml?id=6";
							Bundle bundle = new Bundle();
							bundle.putString(HTML5LinkActivity.LINK_URL,
							TextUtils.isEmpty(strUrl) ? null : strUrl);
							bundle.putString(HTML5LinkActivity.ACTIVITY_TITLE, "彩票");
							bundle.putBoolean(HTML5LinkActivity.SHOW_CLOSE, true);
							bundle.putBoolean(HTML5LinkActivity.ENABLE_ZOOM, false);
							
							AppStorage.setData(AppStorage.SCOPE_WAP,
									AppStorage.KEY_WAP_BACK, "1", false);

							// Check back home activity.
							UiUtils.startActivity(HomeActivity.this, HTML5LinkActivity.class, bundle,true);
							StatisticsEngine.trackEvent(HomeActivity.this,"home_lottery");
						}
							// Show collect
//							final long nLoginUid = ILogin.getLoginUid();
//							if( 0 == nLoginUid )
//							{
//								// Not login yet.
//								ToolUtil.startActivity(HomeActivity.this, LoginActivity.class, null, HomeActivity.REQUEST_COLLECT);
//							}
//							else
//							{
//								showCollect(HomeActivity.this, HomeActivity.this.getString(R.string.tag_Home));
//							}
							break;
						case 3:
							ToolUtil.reportStatisticsClick(getActivityPageId(), "23003");
							
							//com.icson.amap.CargoMapActivity.showMap(HomeActivity.this, "", "", "", "62001474");
							showRecharge(HomeActivity.this, HomeActivity.this.getString(R.string.tag_Home));
							break;

				}
			}
				});

		// Save bottom advertisement.页面的底部信息
		mFooterLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.home_bottom_adver, null);
		if (null != mFooterLayout) {
			final int nCount = mFooterLayout.getChildCount();
			mFooters = new TextView[nCount];
			for (int nIdx = 0; nIdx < nCount; nIdx++) {
				mFooters[nIdx] = (TextView) mFooterLayout.getChildAt(nIdx);
			}
		}
		
		//这应该是易迅发现
		mNewProduct = (ImageView) findViewById(R.id.home_new_product);
		mParser = new HomeParser();

		// Firstly, load default city configuration.
		DispatchFactory.loadDefault();
		mFirstEntry = true;

		// 注册易迅网到微信插件�?
		WXAPIFactory.createWXAPI(this, Config.APP_ID)
				.registerApp(Config.APP_ID);
	}

	private int getCurrentPrefIdx() {
		int openCount = Preference.getInstance().getOpenCount();
		if (openCount > 2)
			return -1;

		int preference = -1;
		int firstOpen = Preference.getInstance().getUserGuideOfIndex(
				Preference.USER_GUIDE_FIRST_OPEN);
		if (firstOpen > 0 && openCount == 1 && 0 >= ILogin.getLoginUid()) {
			preference = Preference.USER_GUIDE_FIRST_OPEN;
		} else {
			int secondOpen = Preference.getInstance().getUserGuideOfIndex(
					Preference.USER_GUIDE_SECOND_OPEN);
			if (secondOpen > 0 && openCount == 2) {
				preference = Preference.USER_GUIDE_SECOND_OPEN;
			}
		}
		return preference;
	}

	private void checkFirstSight(int delay) {
		int layoutType = 0;
		int preference = getCurrentPrefIdx();

		if (preference == Preference.USER_GUIDE_FIRST_OPEN) {
			layoutType = UserGuideDialog.LAYOUT_FIRST_OPEN;
		} else if (preference == Preference.USER_GUIDE_SECOND_OPEN) {
			layoutType = UserGuideDialog.LAYOUT_SECOND_OPEN;
		} else {
			return;
		}

		if (layoutType > 0) {
			final int pref = preference;
			if(null==mGuideDialog)
			{
				mGuideDialog = new UserGuideDialog(
					HomeActivity.this, new UserGuideDialog.OnClickListener() {

						@Override
						public void onDialogClick(UserGuideDialog dialog,
								int nButtonId) 
						{
							Preference.getInstance().setUserGuideOfIndex(pref,0);
							
							if (UserGuideDialog.BUTTON_POSITIVE == nButtonId) {
								if (pref == Preference.USER_GUIDE_FIRST_OPEN) {
									MainActivity.startActivity(
											HomeActivity.this,
											MainActivity.TAB_MY);
								} else if (pref == Preference.USER_GUIDE_SECOND_OPEN) {
									UiUtils.startActivity(HomeActivity.this,
											SlotMachineActivity.class, true);
								}
							} else if (UserGuideDialog.BUTTON_NEGATIVE == nButtonId) {
								Preference.getInstance().setUserGuideOfIndex(Preference.USER_GUIDE_USER_CENTER, 0);
							}
							
							if(null!=mGuideDialog)
							{
								mGuideDialog.cleanup();
								mGuideDialog = null;
							}
						}
					}, layoutType);
			}
			
			if (null != mFadingRunnable) {
				mWholeHandler.removeCallbacks(mFadingRunnable);
			}

			mFadingRunnable = new Runnable() {

				@Override
				public void run() {
					if (Preference.getInstance().getUserGuideOfIndex(pref) > 0 && null != mGuideDialog && HomeActivity.this.isBeenSeen()) 
					{
						mGuideDialog.show();
					}
					mWholeHandler.removeCallbacks(this);
				}
			};

			mWholeHandler.postDelayed(mFadingRunnable, delay);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		this.handleNewIntent(intent);
	}

	private void handleNewIntent(Intent aIntent) {
		Bundle pBundle = (null != aIntent ? aIntent.getExtras() : null);
		if (null == pBundle)
			return;

		// handle message
		if (pBundle.containsKey(MsgEntity.SERIAL_NAME_MSGENTITY)) {
			Serializable pSerializable = pBundle
					.getSerializable(MsgEntity.SERIAL_NAME_MSGENTITY);
			if (null != pSerializable) {
				this.onPushMessage((MsgEntity) pSerializable);
				setIntent(null);
			}
		} else if (pBundle.containsKey(Config.EXTRA_BARCODE)) {
			String strUri = pBundle.getString(Config.EXTRA_BARCODE);
			HomeActivity
					.processUrlInfo(this, Activity.RESULT_OK, strUri, false);
			setIntent(null);
		} else if (pBundle.containsKey(Config.EXTRA_WEIXIN)) {
			String strUri = pBundle.getString(Config.EXTRA_WEIXIN);
			this.onWeixinURL(strUri);
			setIntent(null);
		} else {
			String strKey = AppStorage.getData(AppStorage.SCOPE_DEFAULT,
					MainActivity.REQUEST_EXTER_KEY);
			String strUri = AppStorage
					.getData(AppStorage.SCOPE_DEFAULT, strKey);
			this.handleUri(strKey, strUri);
			AppStorage.delData(AppStorage.SCOPE_DEFAULT,
					MainActivity.REQUEST_EXTER_KEY);
			AppStorage.delData(AppStorage.SCOPE_DEFAULT, strKey);
		}
	}

	private boolean handleUri(String strKey, String strUri) {
		if ((!TextUtils.isEmpty(strKey)) && (!TextUtils.isEmpty(strUri))) {
			if (strKey.equalsIgnoreCase(Config.EXTRA_BARCODE)) {
				HomeActivity.processUrlInfo(this, Activity.RESULT_OK, strUri,
						false);
				return true;
			} else if (strKey.equalsIgnoreCase(Config.EXTRA_WEIXIN)) {
				this.onWeixinURL(strUri);
				return true;
			}
		}

		return false;
	}

	/*
	 * private void onAliCenter(Bundle pBundle) { String alipayUserId =
	 * pBundle.getString(Config.EXTRA_ALI_USERID); String authCode =
	 * pBundle.getString("auth_code"); String appId =
	 * pBundle.getString("app_id"); String version =
	 * pBundle.getString("version"); String alipayClientVersion =
	 * pBundle.getString("alipay_client_version"); String source =
	 * pBundle.getString("source");
	 * 
	 * //autologin(); //onsusess --onerror }
	 */

	private void onWeixinURL(String strUri) {
		if(TextUtils.isEmpty(strUri)) {
			return;
		}
		
		String strProductId = queryVal(strUri, "pid");
		String channelId = queryVal(strUri, "channelid");
		String strTag = queryVal(strUri, "ytag");
		//update tag and y_track
		if(!TextUtils.isEmpty(strTag)) {
			IcsonApplication.updateTagAndPageRoute(strTag);
		}
		
		if(!TextUtils.isEmpty(strProductId)) {
			Bundle pBundle = new Bundle();
			pBundle.putLong(ItemActivity.REQUEST_PRODUCT_ID,
					Long.valueOf(strProductId));
			if ((!TextUtils.isEmpty(channelId))
					&& (TextUtils.isDigitsOnly(channelId))) {
				pBundle.putInt(ItemActivity.REQUEST_CHANNEL_ID,
						Integer.valueOf(channelId));
			}
			UiUtils.startActivity(this, ItemActivity.class, pBundle, true);
			ToolUtil.sendTrack("weixin", getString(R.string.tag_weixin),
					ItemActivity.class.getName(),
					getString(R.string.tag_ItemActivity), "1001", "1",
					strProductId, strTag);
		}
	}

	/**
	 * onPushMessage
	 * 
	 * @param aEntity
	 */
	private void onPushMessage(MsgEntity aEntity) {
		// Check whether need login.
		mEntity = null;
		if ((null != aEntity) && (aEntity.mLogin)
				&& (0 >= ILogin.getLoginUid())) {
			UiUtils.makeToast(this, R.string.need_login);
			UiUtils.startActivity(this, LoginActivity.class, null,
					HomeActivity.REQUEST_PUSH_MESSAGE, true);
			mEntity = aEntity;
		} else {
			MessageActivity.processEntity(this, aEntity, "push");
		}
	}

	/**
	 * show recharge activity.
	 * 
	 * @param aContext
	 */
	private static void showRecharge(Activity aContext, String pageId) {
		UiUtils.startActivity(aContext, VirtualPayActivity.class, true);
		StatisticsEngine.trackEvent(aContext, "home_recharge");
		if (!pageId.equals("")) {
			ToolUtil.sendTrack(aContext.getClass().getName(), pageId,
					VirtualPayActivity.class.getName(),
					aContext.getString(R.string.tag_VirtualPayActivity),
					"08014");
		}
	}

	/**
	 * show collect activity.
	 * 
	 * @param aContext
	 */
	private static void showCollect(Activity aContext, String pageId) {
		ToolUtil.startActivity(aContext, MyCollectActivity.class);
		StatisticsEngine.trackEvent(aContext, "home_collect");
		if (!pageId.equals("")) {
			ToolUtil.sendTrack(aContext.getClass().getName(), pageId,
					MyCollectActivity.class.getName(),
					aContext.getString(R.string.tag_MyCollectActivity), "08013");
		}
	}

	@Override
	public void onClick(View view) {
		String pageId = getString(R.string.tag_Home);
		switch (view.getId()) {
		case R.id.home_search_box_barcode:
			ToolUtil.reportStatisticsClick(this.getActivityPageId(), "21003");
			if(Preference.getInstance().needToBarcodeAccess())
			{
				UiUtils.showDialog(HomeActivity.this,
					R.string.permission_title, R.string.permission_hint_barcode,R.string.permission_agree, R.string.permission_disagree,
					new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if (nButtonId == AppDialog.BUTTON_POSITIVE)
						{
							Preference.getInstance().setBarcodeAccess(Preference.ACCESSED);
							UiUtils.startActivity(HomeActivity.this, CaptureActivity.class, null,
													REQUEST_SCAN_BARCODE, true);
							StatisticsEngine.trackEvent(HomeActivity.this,"scan_barcode");
						}
					}
				});
			}
			else
			{
				UiUtils.startActivity(HomeActivity.this, CaptureActivity.class, null,
						REQUEST_SCAN_BARCODE, true);
				StatisticsEngine.trackEvent(HomeActivity.this,"scan_barcode");
			}
		break;
		case R.id.home_search_box:
			ToolUtil.reportStatisticsClick(this.getActivityPageId(), "21002");
			
			ToolUtil.sendTrack(this.getClass().getName(), pageId,
					SearchActivity.class.getName(),
					getString(R.string.tag_SearchActivity), "01013");
			UiUtils.startActivity(this, SearchActivity.class, true);
			StatisticsEngine.trackEvent(this, "text_search");
			break;
		case R.id.home_new_product:
			if (null != mConfig && null != mConfig.mNewInfo) {
				this.handleEvent(mConfig.mNewInfo);
			}
			break;
		}
	}

	@Override
	public void onItemClick(View aView, int nIndex) {
		this.handleEvent(null != mConfig ? mConfig.getBannerInfo(nIndex) : null);
		ToolUtil.reportStatisticsClick(this.getActivityPageId(), "21001");
		// 测试代码
//		String strUrl = "http://518.qq.com/go.xhtml?id=208";
//		Bundle bundle = new Bundle();
//		bundle.putString(HTML5LinkActivity.LINK_URL,
//		TextUtils.isEmpty(strUrl) ? null : strUrl);
//		bundle.putString(HTML5LinkActivity.ACTIVITY_TITLE, "测试");
//
//		// Check back home activity.
//		UiUtils.startActivity(this, HTML5LinkActivity.class, bundle,true);
	}

	@Override
	public void onPostionUpdate(int nIndex, int nTotal) {
		if (null != mIndicator) {
			mIndicator.setCurrentItem(nIndex);
		}
	}

	private void handleEvent(Object aObject) {
		if (null == aObject)
			return;

		final String strPageId = this.getString(R.string.tag_Home);
		ModuleInfo pInfo = (ModuleInfo) aObject;
		StatisticsEngine.trackEvent(this, "handle_event", pInfo.mEvent);
		switch (pInfo.mModule) {
		case ModuleInfo.MODULE_ID_VPAY:
		case ModuleInfo.MODULE_ID_RECHARGE:
		case ModuleInfo.MODULE_ID_QR_RECHARGE:
			showRecharge(this, strPageId);
			break;

		case ModuleInfo.MODULE_ID_MSGCENTER:
		case ModuleInfo.MODULE_ID_MESSAGES:
			if (0 == ILogin.getLoginUid()) {
				// Not login yet.
				UiUtils.makeToast(this, R.string.need_login);
				UiUtils.startActivity(this, LoginActivity.class, null,
						HomeActivity.REQUEST_MESSAGE_CENTER, true);
			} else {
				loadMessageCenter();
			}
			break;

		case ModuleInfo.MODULE_ID_OUTTER_LINK:
			if (!TextUtils.isEmpty(pInfo.mLinkUrl)) {
				// 启动外部浏览器
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				Uri content_url = Uri.parse(pInfo.mLinkUrl);
				intent.setData(content_url);
				startActivity(intent);
			}
			break;

		case ModuleInfo.MODULE_ID_INNER_LINK:
			if (!TextUtils.isEmpty(pInfo.mLinkUrl)) {
				String strTitle = URLDecoder.decode(queryVal(pInfo.mLinkUrl,
						"title"));
				if (TextUtils.isEmpty(strTitle)) {
					strTitle = pInfo.mParams;
				}

				// pInfo.mLinkUrl = "http://m.51buy.com";
				String strYtag = "&ytag=0." + strPageId + "05015";
				String strUrl = pInfo.mLinkUrl;
				if (!TextUtils.isEmpty(strUrl) && !strUrl.contains("?")) {
					strUrl += "?";
				}
				Bundle bundle = new Bundle();
				bundle.putString(HTML5LinkActivity.LINK_URL,
						TextUtils.isEmpty(strUrl) ? null : strUrl + strYtag);
				bundle.putString(HTML5LinkActivity.ACTIVITY_TITLE, strTitle);

				// Check back home activity.
				String strTag = queryVal(pInfo.mLinkUrl,
						AppStorage.KEY_WAP_BACK);
				AppStorage.setData(AppStorage.SCOPE_WAP,
						AppStorage.KEY_WAP_BACK, strTag, false);

				UiUtils.startActivity(this, HTML5LinkActivity.class, bundle,
						true);
				StatisticsEngine.trackEvent(this, "html_page", pInfo.mLinkUrl);
			}
			break;

		case ModuleInfo.MODULE_ID_COUPON:
			UiUtils.startActivity(this, CouponShowActivity.class, true);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					CouponShowActivity.class.getName(),
					getString(R.string.tag_CouponShowActivity), "05011");
			StatisticsEngine.trackEvent(this, "get_coupon");
			break;

		case ModuleInfo.MODULE_ID_PRODUCT_LIST:
			Bundle param = new Bundle();
			param.putLong(EventBaseActivity.ERQUEST_EVENT_ID, pInfo.mEvent);
			Class<?> className = EventActivityFactory
					.getEventActivityClass(pInfo.mTemplate);
			UiUtils.startActivity(this, className, param, true);
			/*ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					className.getName(),
					getString(R.string.tag_EventActivityFactory), "05013");*/
			break;

		case ModuleInfo.MODULE_ID_PRODUCT:
			if (pInfo.mProductId > 0) {
				Bundle product_params = new Bundle();
				product_params.putLong(ItemActivity.REQUEST_PRODUCT_ID,
						pInfo.mProductId);
				product_params.putInt(ItemActivity.REQUEST_CHANNEL_ID,
						pInfo.mChannelId);
				UiUtils.startActivity(this, ItemActivity.class, product_params,
						true);
				ToolUtil.sendTrack(this.getClass().getName(), strPageId,
						ItemActivity.class.getName(),
						getString(R.string.tag_ItemActivity), "05014",
						String.valueOf(pInfo.mProductId));
			}
			break;

		case ModuleInfo.MODULE_ID_TUANGOU:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22002");
			startYiQiang(this, HomeActivity.mTimeBuyType,
					YiQiangActivity.PARAM_TAB_TUAN);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					YiQiangActivity.class.getName(),
					getString(R.string.tag_TuanActivity), "05016");
			StatisticsEngine.trackEvent(this, "click_tuan");
			StatisticsEngine.trackEvent(this, "homepage_click_tuan");
			break;

		case ModuleInfo.MODULE_ID_QIANG:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22001");
			startYiQiang(this, HomeActivity.mTimeBuyType,
					YiQiangActivity.PARAM_TAB_QIANG);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					YiQiangActivity.class.getName(),
					getString(R.string.tag_QiangActivity), "05020");
			StatisticsEngine.trackEvent(this, "click_qiang");
			StatisticsEngine.trackEvent(this, "homepage_click_qiang");
			break;

		case ModuleInfo.MODULE_ID_RECOMM:
			UiUtils.startActivity(this, AppInfoActivity.class, true);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					AppInfoActivity.class.getName(),
					getString(R.string.tag_AppInfoActivity), "05021");
			break;

		case ModuleInfo.MODULE_ID_MORNING:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22003");
			
			HomeActivity.startTimebuy(this, ModuleInfo.MODULE_ID_MORNING,
					strPageId, "05017");
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					YiQiangActivity.class.getName(),
					getString(R.string.tag_EventMorningActivity), "05030");
			break;

		case ModuleInfo.MODULE_ID_BLACK:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "22004");
			
			HomeActivity.startTimebuy(this, ModuleInfo.MODULE_ID_BLACK,
					strPageId, "05018");
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					YiQiangActivity.class.getName(),
					getString(R.string.tag_EventThhActivity), "05031");
			break;

		case ModuleInfo.MODULE_ID_WEEKEND:
			HomeActivity.startTimebuy(this, ModuleInfo.MODULE_ID_WEEKEND,
					strPageId, "05019");
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					YiQiangActivity.class.getName(),
					getString(R.string.tag_EventWeekendActivity), "05032");
			break;

		case ModuleInfo.MODULE_ID_POPULAR:
			// 热销榜
			UiUtils.startActivity(this, HotlistActivity.class, true);
			StatisticsEngine.trackEvent(this, "view_hotlist");
			ToolUtil.sendTrack(this.getClass().getName(), strPageId,
					HotlistActivity.class.getName(),
					getString(R.string.tag_HotlistActivity), "05033");
			break;

		default: {
			@SuppressWarnings("rawtypes")
			Class pClassName = EventActivityFactory
					.getEventActivityClass(pInfo.mTemplate);
			if (null != pClassName) {
				Bundle pParams = new Bundle();
				pParams.putLong(EventBaseActivity.ERQUEST_EVENT_ID,
						pInfo.mEvent);
				UiUtils.startActivity(this, pClassName, pParams, true);
			}
		}
			break;
		}
	}

	/**
	 * 
	 * method Name:startYiQiang method Description:
	 * 
	 * @param aParaent
	 * @param timebuyType
	 * @param paramTab
	 *            void
	 * @exception
	 * @since 1.0.0
	 */
	public static void startYiQiang(Activity aParaent, int timebuyType,
			int paramTab) {
		Bundle pParam = new Bundle();
		pParam.putInt(TimeBuyModel.TIMEBUY_TYPE, timebuyType);
		pParam.putInt(YiQiangActivity.PARAM_TAB, paramTab);
		UiUtils.startActivity(aParaent, YiQiangActivity.class, pParam, true);
	}

	private static void startTimebuy(Activity aParaent, int nType,
			String strPageId, String strLocationId) {
		startYiQiang(aParaent, nType, YiQiangActivity.PARAM_TAB_TIMEBUY);

		ToolUtil.sendTrack(aParaent.getClass().getName(), strPageId,
				AppInfoActivity.class.getName(),
				aParaent.getString(R.string.tag_YiQiangActivity), strLocationId);
		StatisticsEngine.trackEvent(aParaent, "time_buy");
		StatisticsEngine.trackEvent(aParaent, "homepage_click_timebuy");
	}

	/**
	 * initBanner
	 * 
	 * @return
	 */
	private void initBanner() {
		if (null == mScrollView)
			return;

		List<BannerInfo> aBanners = (null != mConfig ? mConfig.getBanners()
				: null);
		if (!bannerNeedReload(aBanners))
			return;

		mScrollView.removeAllViews();
		mLastBanners.clear();
		final int nCount = (null != aBanners ? aBanners.size() : 0);
		mScrollView.setVisibility(nCount > 0 ? View.VISIBLE : View.GONE);
		mIndicator.setVisibility(nCount > 1 ? View.VISIBLE : View.GONE);
		mIndicator.setTotalItems(nCount);

		BannerInfo pInfo = null;
		for (int nIdx = 0; nIdx < nCount; nIdx++) {
			pInfo = aBanners.get(nIdx);
			
			//if(nIdx == 0)
			//	pInfo.mLinkUrl = "http://beta.m.yixun.com/m/touch_in_app_test.html";
				//pInfo.mLinkUrl = "http://beta.m.yixun.com/m/touch_in_app_test.html?wapBack=1";
			mScrollView.addImageView(pInfo.getPicUrl(), nIdx);
			mLastBanners.add(pInfo);
		}
	}

	private void loadMessageCenter() {
		String strPageId = getString(R.string.tag_Home);
		UiUtils.startActivity(this, MessageActivity.class, true);
		ToolUtil.sendTrack(this.getClass().getName(), strPageId,
				MessageActivity.class.getName(),
				getString(R.string.tag_MessageActivity), "08021");
		StatisticsEngine.trackEvent(this, "view_msgcenter");
	}

	private boolean checkHomeConfig() {
		boolean bRequesting = false;
		final boolean bIsExpire = mPageCache
				.isExpire(CacheKeyFactory.HOME_CHANNEL_INFO);
		String strContent = mPageCache
				.getNoDelete(CacheKeyFactory.HOME_CHANNEL_INFO);
		if (null == mConfig || bIsExpire) {
			final long pCreateTime = mPageCache
					.getRowCreateTime(CacheKeyFactory.HOME_CHANNEL_INFO);
			if (!TextUtils.isEmpty(strContent)) {
				try {
					if(null == mParser) {
						mParser = new HomeParser();
					}
					mConfig = mParser.parse(strContent);
				} catch (Exception ex) {
					Log.e(LOG_TAG, ex);
					mConfig = null;
				}finally {
					mParser = null;
				}

				if (null != mConfig) {
					// Update EQiang's mCurrentSecs time
					mConfig.mCurrentSecs = (ToolUtil.getCurrentTime() - pCreateTime)
							/ 1000 + mConfig.mCurrentSecs;
					mIntent = this.getIntent();
					bodyRequestFinish();
					getDispatches();
				}
			} else {
				isNullHomeInformtion = true;
			}
		}

		//version 下载确认在portal进行，mainactivity 已经做过版本保存
		//Preference pPreference = Preference.getInstance();
		// 默认分站id如果变化 可能需要重新拉取.
		if ((null != mConfig && ILogin.getSiteId() != mConfig.mSiteId)
				|| bIsExpire || TextUtils.isEmpty(strContent)){
				//|| IcsonApplication.mVersionCode > pPreference.getProjVersion()) {
			this.getHomeConfig(mFirstEntry);
			bRequesting = true;

			if (isNullHomeInformtion) {
				showLoadingLayer();
			}
		}

		if (null != mConfig
				&& null != BgUpdater.checkVaildBgBitmap(
						HomeActivity.this,
						// "http://b275.photo.store.qq.com/psb?/V11XoEZj3ou0GJ/8KutzGDasnMDVsjJncQhzXNYKUJ3CdsSspJS0Cr56m0!/b/dGLo76OqHwAA&bo=wgEgAwAAAAABAMc!&rf=viewer_4",
						// 1382351570,1390000000))
						mConfig.mHomeBgPicUrl, mConfig.mHomeBgPicStartTime,
						mConfig.mHomeBgPicExpireTime, this)) {
			super.setThemeConfig();
		}

		return bRequesting;
	}

	private void getHomeConfig(boolean bReload) {
		if (null != mRequest)
			return;

		mRequest = ServiceConfig.getAjax(Config.URL_HOME_GETINFO);
		if ((null == mRequest) || (null == mPageCache)) {
			hideHeaderLoading();
			return;
		}
		// hostlist static page. NO Need send latestcate
		// String strLastCate = RecentCates.getString(1);
		// if( !TextUtils.isEmpty(strLastCate) ) {
		// mRequest.setData("latestcate", strLastCate);
		// }

		if(null == mParser) {
			mParser = new HomeParser();
		}
		
		String strProvinceIPId = FullDistrictHelper.getProvinceIPId() + "";
		mRequest.setData("fetchCity", TextUtils.isEmpty(strProvinceIPId) ? "" + DispatchFactory.PROVINCE_IPID_SH : strProvinceIPId);
		
		mRequest.setParser(mParser);
		mRequest.setId(bReload ? AJAX_RELOAD : AJAX_EVENT);
		mRequest.setOnSuccessListener(this);
		mRequest.setOnErrorListener(this);

		mRequest.send();
	}

	/**
	 * getDispatches
	 */
	private void getDispatches() {
		if (null == mPageCache)
			return;

		// first, check dispatch in DB is exist or not, and check it is expired
		// or not
		String content = mPageCache
				.getNoDelete(CacheKeyFactory.CACHE_DISPATCHES_INFO);
		boolean isExpire = mPageCache
				.isExpire(CacheKeyFactory.CACHE_DISPATCHES_INFO);

		if (content != null && isExpire == false) {
			ArrayList<DispatchItem> pDispatchItems = null;

			try {
				if(null == mDispatchesParser) {
					mDispatchesParser = new DispatchesParser();
				}
				pDispatchItems = mDispatchesParser.parse(content);
				DispatchFactory.addItems(pDispatchItems);

			} catch (Exception ex) {
				Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
				pDispatchItems = null;
			}finally{
				mDispatchesParser = null;
			}

			if (pDispatchItems != null) {
				return;
			}
		}

		// if dispatch in DB is null or expired, get it from server.
		if(null == mDispatchesParser) {
			mDispatchesParser = new DispatchesParser();
		}
		Ajax ajax = ServiceConfig.getAjax(Config.URL_DISPATCH_SITE);

		ajax.setParser(mDispatchesParser);
		ajax.setId(AJAX_DISPATCHES);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		ajax.send();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSuccess(Object v, Response response) {
		final int nRequestId = response.getId();
		if (AJAX_EVENT == nRequestId || AJAX_RELOAD == nRequestId) {
			mRequest = null;
			if (!mParser.isSuccess()) {
				UiUtils.makeToast(this, mParser.getErrMsg());
				closeLoadingLayer(true);
				return;
			}

			closeLoadingLayer();

			mConfig = (HomeConfig) v;
			bodyRequestFinish();

			// Update city cache.
			mPageCache.set(CacheKeyFactory.HOME_CHANNEL_INFO,
					mParser.getString(), 5 * 60);
			
			mParser = null;
//			this.setCityCache();

			if (AJAX_EVENT == nRequestId) {
				// Send request for dispatches information.
				this.getDispatches();
			}

			this.hideHeaderLoading();

			mFirstEntry = false;
		} else if (AJAX_DISPATCHES == nRequestId) {
			// Parse the instance of dispatches information.
			ArrayList<DispatchItem> aResults = (ArrayList<DispatchItem>) v;
			DispatchFactory.addItems(aResults);

			// dispatches information saves for 7 days
			mPageCache.set(CacheKeyFactory.CACHE_DISPATCHES_INFO,
					mDispatchesParser.getString(), 24 * 60 * 60);
			mDispatchesParser = null;

			// Set default parser.
//			this.setCityCache();
		}
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		final int nRequestId = response.getId();
		if (AJAX_EVENT == nRequestId || AJAX_RELOAD == nRequestId) {
			closeLoadingLayer();

			mRequest = null;
			this.hideHeaderLoading();

			if (isNullHomeInformtion == true) {
				super.onError(ajax, response);
			}
		}
	}

	private void hideHeaderLoading() {
		if (null != mHeaderLoadingLayout
				&& mHeaderLoadingLayout.getVisibility() == View.VISIBLE) {
			mHeaderLoadingLayout.setVisibility(View.GONE);
		}
	}
	
//	private void setCityCache()
//	{
//		final int nCityId = (null != mConfig ? mConfig.mCityId : -1);
//		if( nCityId >= 0 && null != mPageCache ) {
//			if( mPageCache.get(CacheKeyFactory.CACHE_CITY_ID) == null ){
//				DispatchFactory.setDefaultCityId( mConfig.mCityId );
//			}
//		}
//	}

	/**
	 * 
	 */
	private void bodyRequestFinish() {
		// Parse the banner information.
		this.initBanner();

		// Initialize announcement.
		initAnnounce();

		// Parse new information.
		if (null != mNewProduct) {
			ModuleInfo pNewInfo = mConfig.mNewInfo;
			if ((null != pNewInfo) && (!TextUtils.isEmpty(pNewInfo.mPicUrl))) {
				mNewProduct.setVisibility(View.VISIBLE);
				mNewProduct.setOnClickListener(this);

				// Update image loader listener.
				AjaxUtil.getLocalImage(HomeActivity.this, pNewInfo.mPicUrl,
						new ImageLoadListener() {

							@Override
							public void onLoaded(Bitmap aBitmap, String strUrl) {
								mNewProduct.setImageBitmap(aBitmap);
							}

							@Override
							public void onError(String strUrl) {
							}
						});
			} else {
				mNewProduct.setVisibility(View.GONE);
			}
		}

		// Timebuy.
		if (null != mTimeBuy) {
			List<ProductInfo> aSnapup = mConfig.mProducts;
			int nSize;
			if (timeBuyNeedReload(aSnapup)) {
				mTimeBuy.cleanup();
				mLastSnapup.clear();
				mTimeBuy.setContent(mConfig.mTimebuyCaption);
				// mTimeBuy.setTimerValue(mConfig.mCurrentSecs,
				// mConfig.mEndSecs);

				nSize = (null != aSnapup ? aSnapup.size() : 0);
				for (int nIdx = 0; nIdx < nSize; nIdx++) {
					ProductInfo pInfo = aSnapup.get(nIdx);
					mTimeBuy.addSnapupInfo(pInfo.mMsg, pInfo.mInfo,
							pInfo.mComments, pInfo.getPicUrl());
					mLastSnapup.add(pInfo);
				}
			}// otherwise noneed to reload data and redraw Panel

			// Update channels.
			List<ModuleInfo> aChannels = mConfig.mChannels;
			nSize = (null != aChannels ? aChannels.size() : 0);
			for (int nIdx = 0; nIdx < nSize; nIdx++) {
				ModuleInfo pInfo = aChannels.get(nIdx);
				// if(nIdx == 0)
				if (pInfo.mModule >= ModuleInfo.MODULE_ID_MORNING
						&& pInfo.mModule <= ModuleInfo.MODULE_ID_WEEKEND) {
					HomeActivity.mTimeBuyType = pInfo.mModule;
				}
				mTimeBuy.setChannelInfo(nIdx, pInfo.mPicUrl, pInfo.mSubtitle,
						pInfo.mPromotion, pInfo.mHint, pInfo);
			}

			// Update timer.
			if (null != mConfig) {
				// mTimeBuy.setTimerValue(mConfig.mCurrentSecs,
				// mConfig.mEndSecs);
			}

		}

		// Popular information.
		if (null != mEvents) {
			List<ModuleInfo> aEvents = (null != mConfig ? mConfig.mEvents
					: null);
			if (eventNeedReload(aEvents)) {
				mEvents.removeAll();
				mLastEvents.clear();
				final int nSize = (null != aEvents ? aEvents.size() : 0);
				mEvents.setVisibility(nSize > 0 ? View.VISIBLE : View.GONE);
				for (int i = 0; i < nSize; i++) {
					ModuleInfo pEntity = aEvents.get(i);

					// Get products.
					List<ProductInfo> aProducts = pEntity.mItems;
					List<EventView.EventItem> aItems = new ArrayList<EventView.EventItem>();
					final int nCount = (null != aProducts ? aProducts.size()
							: 0);
					for (int nIdx = 0; nIdx < nCount; nIdx++) {
						ProductInfo pInfo = aProducts.get(nIdx);
						EventView.EventItem entity = new EventView.EventItem(
								pInfo.mMsg, pInfo.mInfo, pInfo.getPicUrl());
						aItems.add(entity);
					}

					mEvents.addEvent(pEntity.mSubtitle, pEntity.mPromotion,
							pEntity.mTag, aItems, pEntity);
					mLastEvents.add(pEntity);
				}
			}// otherwise noneed to reload data and redraw Panel
		}

		// Load recommend.
		this.loadRecommend();

		// Update recharge promo icon
		updateRechargePromoIcon();

		// Set current city.
		// mDispatch.setText(DispatchFactory.getDefaultCityName());

		// Start the animation.
		stopAnimation();
		startAnimation();

		// Try to update intent.
		handleNewIntent(mIntent);
		mIntent = null;

		// get user guide info
		if (mConfig.showUserGuide > 0 || Preference.getInstance().getOpenCount()==2) {
			checkFirstSight(mConfig.userGuideDuriation);
		}
	}

	private void updateRechargePromoIcon() {
		if (null == mConfig)
			return;

		String pRechargePromoUrl = mConfig.mRechargeIconUrl;
		long pRechargePromoStart = mConfig.mRechargeStartTime;
		long pRechargePromoExpire = mConfig.mRechargeExpireTime;
		long pCurrentTime = System.currentTimeMillis() / 1000;

		if (pCurrentTime >= pRechargePromoStart
				&& pCurrentTime <= pRechargePromoExpire) {
			AjaxUtil.getLocalImage(this, pRechargePromoUrl,
					new ImageLoadListener() {
						@Override
						public void onLoaded(Bitmap aBitmap, String strUrl) {
							if (null != mShortcuts) {
								mShortcuts.setRechargePromoIcon(aBitmap, true);
							}
						}

						@Override
						public void onError(String strUrl) {
							if (null != mShortcuts) {
								mShortcuts.setRechargePromoIcon(false);
							}
						}

					});
		}

	}

	/**
	 * method Name:timeBuyNeedReload method Description:
	 * 
	 * @param aSnapup
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	private boolean timeBuyNeedReload(List<ProductInfo> aSnapup) {
		if (null == mLastSnapup)
			mLastSnapup = new ArrayList<ProductInfo>();

		// use last aBanners
		if (null == aSnapup || aSnapup.size()<=0)
			return false;
		for (ProductInfo aInfo : aSnapup) {
			if (!mLastSnapup.contains(aInfo))
				return true;
		}
		return false;
	}

	/**
	 * method Name:bannerNeedReload method Description:
	 * 
	 * @param aBanners
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	private boolean bannerNeedReload(List<BannerInfo> aBanners) {
		if (null == mLastBanners)
			mLastBanners = new ArrayList<BannerInfo>();

		// use last aBanners
		if (null == aBanners || aBanners.size()<=0)
			return false;
		for (BannerInfo aBanner : aBanners) {
			if (!mLastBanners.contains(aBanner))
				return true;
		}
		return false;
	}

	/**
	 * method Name:eventNeedReload method Description:
	 * 
	 * @param aEvents
	 * @return boolean
	 * @exception
	 * @since 1.0.0
	 */
	private boolean eventNeedReload(List<ModuleInfo> aEvents) {
		if (null == mLastEvents)
			mLastEvents = new ArrayList<ModuleInfo>();
		// use last events
		if (null == aEvents || aEvents.size()<=0)
			return false;

		for (ModuleInfo aModel : aEvents) {
			if (!mLastEvents.contains(aModel))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param aRecommand
	 * @return
	 */
	private boolean recommandNeedNeedReload(List<ModuleInfo> aRecommand) {
		if (null == mLastRecommend)
			mLastRecommend = new ArrayList<ModuleInfo>();

		// use last aBanners
		if (null == aRecommand || aRecommand.size()<=0)
			return false;
		for (ModuleInfo aInfo : aRecommand) {
			if (!mLastRecommend.contains(aInfo))
				return true;
		}
		return false;
	}
	
	/**
	 * Load recommend modules.
	 */
	private void loadRecommend() {
		
		// Load information.
		List<ModuleInfo> aRecommend = mConfig.mRecommend;
		int nSize = (null != aRecommend ? aRecommend.size() : 0);
		if (0 >= nSize)
			return;
		boolean bBottomAdsNeedReload = false;
		if(recommandNeedNeedReload(aRecommend))
		{
			bBottomAdsNeedReload = true;
			if ((null != mRootLayout) && (null != mConfig)) {
				// Clear previous views.
				nSize = (null != mCache ? mCache.size() : 0);
				if (nSize > 0) {
					for (int nIdx = 0; nIdx < nSize; nIdx++) {
						mRootLayout.removeView(mCache.get(nIdx));
					}
					mCache.clear();
				}

				if (null == mCache)
					mCache = new ArrayList<ProInfoView>();

				nSize = (null != aRecommend ? aRecommend.size() : 0);
				
				mLastRecommend.clear();
				// Add the new list.
				LinearLayout.LayoutParams pParams = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				for (int nIdx = 0; nIdx < nSize; nIdx++) {
					final ModuleInfo pInfo = aRecommend.get(nIdx);
					mLastRecommend.add(pInfo);
					pParams.bottomMargin = 0;
					pParams.topMargin = 0;
					// Create a new instance for product info.
					ProInfoView pChild = new ProInfoView(this);
					mRootLayout.addView(pChild, pParams);
					mCache.add(pChild);

					// 	Update onClick listener.
					pChild.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (pInfo.mModule == ModuleInfo.MODULE_ID_POPULAR) 
								ToolUtil.reportStatisticsClick(getActivityPageId(), "23013");
							else if (pInfo.mEvent == HomeConfig.EVENT_GUANGGUANG)
								ToolUtil.reportStatisticsClick(getActivityPageId(), "23001");
							else
								ToolUtil.reportStatisticsClick(getActivityPageId(), "23002");
							handleEvent(pInfo);
						}
					});

					// Update information.
					ProductInfo pProduct = pInfo.mProduct;
					if (null != pProduct) {
						if (pInfo.mModule == ModuleInfo.MODULE_ID_POPULAR) {
							pChild.setContent(pInfo.mSubtitle, pProduct.mMsg, null,
								null, null);
							pChild.getImageView().setPadding(0, 0, 0, 0);
							pChild.getImageView().setImageResource(
									R.drawable.hot_image);
						}
						else if (pInfo.mEvent == HomeConfig.EVENT_GUANGGUANG) {
							pChild.setContent(pInfo.mSubtitle, pProduct.mMsg,
									pProduct.mInfo, pProduct.mComments, pInfo.mTag);
								pChild.getImageView().setPadding(0, 0, 0, 0);
								pChild.getImageView().setImageResource(
										R.drawable.guang_image);
							}
						else {
							pChild.setContent(pInfo.mSubtitle, pProduct.mMsg,
									pProduct.mInfo, pProduct.mComments, pInfo.mTag);
							AjaxUtil.getLocalImage(this, pProduct.getPicUrl(),
									pChild);
						}
					}
				}
			}
		}//end of recommandNeedNeedReload
		
		if(!bBottomAdsNeedReload && null!=mLastBottomAds && ToolUtil.equalsStrings(mLastBottomAds,mConfig.mFooter))
			return;
			
		mLastBottomAds = mConfig.mFooter.clone();
			
		// Remove bottom ad.
		mRootLayout.removeView(mFooterLayout);

		// Add bottom advertisement.
		LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		//always in tail
		mRootLayout.addView(mFooterLayout, bottomParams);
		final int nLength = (null != mFooters ? mFooters.length : 0);
		for (int nIdx = 0; nIdx < nLength; nIdx++) {
			mFooters[nIdx].setText(mConfig.mFooter[nIdx]);
		}
	}

	
	private void initAnnounce() {
		if (null == mConfig || null == mAppAnnounce)
			return;
		Announce announce = mConfig.mAnnounce;
		if (announce == null) {
			mAppAnnounce.setVisibility(View.GONE);
			return;
		}

		int saveAnnounceID = Preference.getInstance().getAnnounceID();
		String strAnnounce = announce.getMsg();
		// 如果当前的公告id > 客户端保存的最后一个公告id，并且未过期，那么显示新公告
		if (announce.getAnnounceId() != saveAnnounceID && !announce.isOutTime()
				&& !TextUtils.isEmpty(strAnnounce)) {
			mAppAnnounce.setVisibility(View.VISIBLE);
			mAppAnnounce.setText(strAnnounce);
		} else {
			mAppAnnounce.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		startAnimationDelay();
 
		
		// Check the home configuration status.
		if (!checkHomeConfig()) {
			if (null == mIntent)
				mIntent = this.getIntent();
			this.handleNewIntent(mIntent);
			mIntent = null;
		}

		if (null != mConfig && (mConfig.showUserGuide > 0 || Preference.getInstance().getOpenCount()==2)) 
		{
			checkFirstSight(mConfig.userGuideDuriation);
		}

		if(null == mTempManager)
			mTempManager = (SensorManager)(getApplicationContext().getSystemService(SENSOR_SERVICE));
		if(null!=mTempManager)
			mTempManager.registerListener(this, mTempManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
		
	}

	@Override
	protected void onPause() {
		int preference = getCurrentPrefIdx();
		if (preference >= 0) {
			Preference.getInstance().setUserGuideOfIndex(preference, 0);
			Preference.getInstance().setUserGuideOfIndex(Preference.USER_GUIDE_USER_CENTER, 0);
		}
		//if (null != mFadingRunnable) {
		//	mWholeHandler.removeCallbacks(mFadingRunnable);
		//}
		
		if(null!=mGuideDialog)
		{
			mGuideDialog.cleanup();
			if(mGuideDialog.isShowing())
			{
				mGuideDialog.dismiss();
				mGuideDialog = null;
			}
		}
		
		this.stopAnimation();
		
		if(null!= mTempManager)
			mTempManager.unregisterListener(this);
		
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		this.stopAnimation();
	}

	private void startAnimationDelay() {
		if (null == mStartAnimRunnable) {
			mStartAnimRunnable = new Runnable() {

				@Override
				public void run() {
					stopAnimation();
					startAnimation();
				}
			};
		}
		mWholeHandler.removeCallbacks(mStartAnimRunnable);
		mWholeHandler.postDelayed(mStartAnimRunnable, 500);
	}

	private void startAnimation() {
		if (null != mScrollView)
			mScrollView.startSlide();
		if (null != mTimeBuy)
			mTimeBuy.startLoop();
		if (null != mEvents)
			mEvents.startAnim();
	}

	private void stopAnimation() {
		// Try to do a system gc.
		// System.gc();

		if (null != mScrollView)
			mScrollView.stopSlide();
		if (null != mTimeBuy)
			mTimeBuy.stopLoop();
		if (null != mEvents)
			mEvents.stopAnim();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mEvents) {
			mEvents.removeAll();
			mEvents = null;
		}
		
		if (null != mCache) {
			mCache.clear();
			mCache = null;
		}
		
		mTimeBuy.cleanup();
		mScrollView.removeAllViews();
		HomeImgPreLoader.cleanImageLoader();
		mTempManager = null;
		mPreLoader = null;
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2618;
	private static final int REQUEST_SCAN_BARCODE = 2620;
	private static final int REQUEST_MESSAGE_CENTER = 2621;
	private static final int REQUEST_PUSH_MESSAGE = 2622;
	private static final int REQUEST_COLLECT = 2623;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// 回调获取从谷歌得到的数据
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// 取得语音的字�?
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (results != null && results.size() > 0)
				redirect(results.get(0));
		} else if ((REQUEST_MESSAGE_CENTER == requestCode)
				&& (LoginActivity.FLAG_RESULT_LOGIN_SUCCESS == resultCode)) {
			this.loadMessageCenter();
		} else if ((REQUEST_PUSH_MESSAGE == requestCode)
				&& (LoginActivity.FLAG_RESULT_LOGIN_SUCCESS == resultCode)) {
			if (null != mEntity) {
				MessageActivity.processEntity(this, mEntity, "push");
				mEntity = null;
			}
		} else if ((REQUEST_COLLECT == requestCode)
				&& (LoginActivity.FLAG_RESULT_LOGIN_SUCCESS == resultCode)) {
			showCollect(HomeActivity.this,
					HomeActivity.this.getString(R.string.tag_Home));
		} else if ((REQUEST_SCAN_BARCODE == requestCode)) {
			Bundle pExtras = (null != data ? data.getExtras() : null);
			String strText = (null != pExtras ? pExtras.getString("text") : "");
			// final int nFormat = (null != pExtras ? pExtras.getInt("format") :
			// -1);

			HomeActivity.processUrlInfo(this, resultCode, strText, true);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * onBarcodeResult
	 * 
	 * @param nResultCode
	 * @param strText
	 */
	public static void processUrlInfo(BaseActivity aActivity, int nResultCode,
			String strText, boolean bInternal) {
		if (Activity.RESULT_CANCELED == nResultCode)
			return;

		if (!TextUtils.isEmpty(strText)) {
			String pageId = aActivity.getString(R.string.tag_Home);

			// Check whether have cps_cookie.
			String strCpsCookies = queryVal(strText,
					AppStorage.KEY_CPS_COOKIES, true);
			if (!TextUtils.isEmpty(strCpsCookies)) {
				// Save to local storage.
				AppStorage.setData(AppStorage.SCOPE_CPS,
						AppStorage.KEY_CPS_COOKIES, strCpsCookies, true);

				// Update tkd value.
				String strCpsTkd = queryVal(strText, AppStorage.KEY_CPS_TKD,
						true);
				AppStorage.setData(AppStorage.SCOPE_CPS,
						AppStorage.KEY_CPS_TKD, "", true); // Clear previous
															// content.
				if (!TextUtils.isEmpty(strCpsTkd)) {
					// Save to local storage.
					AppStorage.setData(AppStorage.SCOPE_CPS,
							AppStorage.KEY_CPS_TKD, strCpsTkd, true);
				}
			}

			String strSource = queryVal(strText, AppStorage.KEY_SOURCE);
			if (!TextUtils.isEmpty(strSource)) {
				AppStorage.setData(AppStorage.SCOPE_CPS, AppStorage.KEY_SOURCE,
						strSource, true);

				// Report to mta.
				String strExtra = "[" + bInternal + "]" + strText;
				StatisticsEngine.trackEvent(aActivity, "activate_from_"
						+ strSource, strExtra);
			}

			// 1. find type parameter.
			String strType = queryVal(strText, "type");
			String strSiteId = queryVal(strText, "siteid");
			String strTag = queryVal(strText, "ytag");
			String comefrom = queryVal(strText, "comefrom");			
			//update tag and y_track
			if(!TextUtils.isEmpty(strTag)) {
				IcsonApplication.updateTagAndPageRoute(strTag);
			}
			if (strType.equals("event")) {
				// Handle URL result for event.
				String strTemplateId = queryVal(strText, "templateid");
				if (TextUtils.isEmpty(strTemplateId)
						|| !TextUtils.isDigitsOnly(strTemplateId)) {
					HomeActivity.showBarcodeContent(aActivity, "null",
							bInternal);
					ToolUtil.sendTrack("barcode_cps",
							aActivity.getString(R.string.tag_cps),
							HomeActivity.class.getName(),
							aActivity.getString(R.string.tag_Home), "06010",
							"1", "", strTag);
					return;
				}
				Class<?> pClassName = EventActivityFactory
						.getEventActivityClass(Integer.valueOf(strTemplateId));
				if (null != pClassName) {
					Bundle pBundle = new Bundle();
					String strEventId = queryVal(strText, "eventid");
					if (TextUtils.isEmpty(strEventId)
							|| !TextUtils.isDigitsOnly(strEventId)) {
						HomeActivity.showBarcodeContent(aActivity, "null",
								bInternal);
						ToolUtil.sendTrack("barcode_cps",
								aActivity.getString(R.string.tag_cps),
								HomeActivity.class.getName(),
								aActivity.getString(R.string.tag_Home),
								"06011", "1", "", strTag);
						return;
					}

					// Activate the event template.
					final long nEventId = Long.valueOf(strEventId);
					if (ModuleInfo.MODULE_ID_QR_RECHARGE == nEventId) {
						showRecharge(aActivity, "");
						ToolUtil.sendTrack(
								"barcode_cps",
								aActivity.getString(R.string.tag_cps),
								VirtualPayActivity.class.getName(),
								aActivity
										.getString(R.string.tag_VirtualPayActivity),
								String.valueOf(nEventId), "1", "", strTag);
					} else {
						pBundle.putLong(EventBaseActivity.ERQUEST_EVENT_ID,
								nEventId);
						UiUtils.startActivity(aActivity, pClassName, pBundle,
								true);
						ToolUtil.sendTrack("barcode_cps", aActivity
								.getString(R.string.tag_cps), pClassName
								.getName(), aActivity
								.getString(R.string.tag_EventBaseActivity),
								String.valueOf(nEventId), "1", "", strTag);
					}
				}
			}
			else if(strType.equals("category"))
			{
				MainActivity.startActivity(aActivity, MainActivity.TAB_CATEGORY);
				return;
			}
			else if(strType.equals("search"))
			{
				/*
				 * 打开搜索列表页接口，为app内嵌HTML5页面提供接口
				 * 
				 * @param String keyword  关键字
				 * @param String path 导航id路径
				 * @param String classid  品类id
				 * @param int sort 排序方式
				 * @param int page 第几页
				 * @param String price 价格区间
				 * @param String attrinfo 属性过滤
				 * @param int areacode 仓库地区代码
				 * @param int channelId 用于指定多价的商品
				 * 
				 * added by marcoyao  2013.10.12
		 		 */
				String keyword = URLDecoder.decode(queryVal(strText, "keyword")); //@param String key  关键字
				String path = queryVal(strText, "path"); // @param String path 导航id路径
				String classid = queryVal(strText, "classid"); //@param String classid  品类id
				if( TextUtils.isEmpty(keyword) &&
						TextUtils.isEmpty(path)&&
						TextUtils.isEmpty(classid)
						)
				{
					return ;
				}
				
				String tempValue = queryVal(strText, "sort");
				int sort = -1;//@param int sort 排序方式
				if(!TextUtils.isEmpty(tempValue) && TextUtils.isDigitsOnly(tempValue))
				{
					sort = Integer.valueOf(tempValue);
				}
				
				int page = -1;   //@param int page 第几页
				tempValue = queryVal(strText, "page");
				if(!TextUtils.isEmpty(tempValue) && TextUtils.isDigitsOnly(tempValue))
				{
					page = Integer.valueOf(tempValue);
				}
				String price = queryVal(strText, "price"); //@param String price 价格区间
				String attrinfo = queryVal(strText, "attrinfo");//@param String attrinfo 属性过滤
				
				int areacode = -1;//@param int areacode 仓库地区代码
				tempValue = queryVal(strText, "areacode");
				if(!TextUtils.isEmpty(tempValue) && TextUtils.isDigitsOnly(tempValue))
				{
					areacode = Integer.valueOf(tempValue);
				}
				
				SearchModel model = new SearchModel();
				if(!TextUtils.isEmpty(keyword))
					model.setKeyWord(keyword);
					
				if(!TextUtils.isEmpty(path))
					model.setPath(path);
					
				if(!TextUtils.isEmpty(classid))
					model.setClassId(classid);
					
				if(sort >=0 )
					model.setSort(sort);
					
				if(page >= 0)
					model.setCurrentPage(page);
					
				if(!TextUtils.isEmpty(price))
					model.setPrice(price);
					
				if(!TextUtils.isEmpty(attrinfo))
					model.setOption(attrinfo);
					
				if(areacode > 0)
					model.setAreaCode(areacode);	
					
				Bundle param = new Bundle();
				param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, model);
				param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, keyword);
				param.putBoolean(ListActivity.REQUEST_SEARCH_FROM_WX, "weixin".equals(comefrom));
				UiUtils.startActivity(aActivity, ListActivity.class, param, true);
				
				return;
			}
			//slotmachineActivity
			else if (strType.equals("tiantianyao")) {
				UiUtils.startActivity(aActivity, SlotMachineActivity.class,
						true);
				StatisticsEngine.trackEvent(aActivity, "slotmachine_click");
				ToolUtil.sendTrack("wap2slot",
						aActivity.getString(R.string.tag_Home),
						SlotMachineActivity.class.getName(),
						aActivity.getString(R.string.tag_SlotMachineActivity),
						"04011");
				return;
			} else if (strType.equals("proinfo")) {
				// Handle URL for product info.
				Bundle pBundle = new Bundle();
				String strProductId = queryVal(strText, "productid");
				if(TextUtils.isEmpty(strProductId))
					strProductId = queryVal(strText, "pid");
				if ((TextUtils.isEmpty(strProductId))
						|| (!TextUtils.isDigitsOnly(strProductId))) {
					HomeActivity.showBarcodeContent(aActivity, "null",
							bInternal);
					return;
				}

				pBundle.putLong(ItemActivity.REQUEST_PRODUCT_ID,
						Long.valueOf(strProductId));
				pBundle.putBoolean(ItemActivity.REQUEST_SEARCH_FROM_WX, "weixin".equals(comefrom));

				// Parse channel id.
				String strChannelId = queryVal(strText, "channelId");
				if (!TextUtils.isEmpty(strChannelId)
						&& TextUtils.isDigitsOnly(strChannelId)) {
					pBundle.putInt(ItemActivity.REQUEST_CHANNEL_ID,
							Integer.valueOf(strChannelId));
				}

				UiUtils.startActivity(aActivity, ItemActivity.class, pBundle,
						true);
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						ItemActivity.class.getName(),
						aActivity.getString(R.string.tag_ItemActivity),
						"60010", "1", strProductId, strTag);
			} else if (strType.equals("coupon")) {
				// Handle URL for getting coupon.
				String strCouponId = queryVal(strText, "couponid");
				Bundle pBundle = new Bundle();
				pBundle.putString("couponid", strCouponId);
				pBundle.putString("siteid", strSiteId);
				UiUtils.startActivity(aActivity, CouponShowActivity.class,
						pBundle, true);
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						CouponShowActivity.class.getName(),
						aActivity.getString(R.string.tag_CouponShowActivity),
						"100000", "1", "", strTag);
			} else if (strType.equals("recharge")) {
				showRecharge(aActivity, "");
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						VirtualPayActivity.class.getName(),
						aActivity.getString(R.string.tag_VirtualPayActivity),
						"08015", "1", "", strTag);
			} else if (strType.equals("tuan")) {
				startYiQiang(aActivity, HomeActivity.mTimeBuyType,
						YiQiangActivity.PARAM_TAB_TUAN);
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						YiQiangActivity.class.getName(),
						aActivity.getString(R.string.tag_TuanActivity),
						"08016", "1", "", strTag);
			} else if (strType.equals("qiang")) {
				startYiQiang(aActivity, HomeActivity.mTimeBuyType,
						YiQiangActivity.PARAM_TAB_QIANG);
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						YiQiangActivity.class.getName(),
						aActivity.getString(R.string.tag_QiangActivity),
						"08017", "1", "", strTag);
			} else if (strType.equals("timebuy")
					|| strType.equals("morningmarket")
					|| strType.equals("black")) {
				HomeActivity.startTimebuy(aActivity,
						ModuleInfo.MODULE_ID_MORNING, pageId, "08018");
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						YiQiangActivity.class.getName(),
						aActivity.getString(R.string.tag_EventMorningActivity),
						"08018", "1", "", strTag);
			} else if (strType.equals("weekend")) {
				HomeActivity.startTimebuy(aActivity,
						ModuleInfo.MODULE_ID_MORNING, pageId, "08019");
				ToolUtil.sendTrack("barcode_cps",
						aActivity.getString(R.string.tag_cps),
						YiQiangActivity.class.getName(),
						aActivity.getString(R.string.tag_EventWeekendActivity),
						"08019", "1", "", strTag);
			} else if (strType.equals("home")) {
				// Do nothing, as home page already activated.
				if (!aActivity.getClass().equals(HomeActivity.class)) {
					UiUtils.startActivity(aActivity, HomeActivity.class, true);
					ToolUtil.sendTrack("barcode_cps",
							aActivity.getString(R.string.tag_cps),
							HomeActivity.class.getName(),
							aActivity.getString(R.string.tag_Home), "08020",
							"1", "", strTag);
				}
			} else if (HomeActivity.isAvailableUrl(strText)) {
				// Default action is to start a web browser.
				Intent pIntent = new Intent(Intent.ACTION_VIEW);
				pIntent.setData(Uri.parse(strText));
				aActivity.startActivity(pIntent);
			} else if (strText.startsWith("tel:")) {
				Intent pIntent = new Intent(Intent.ACTION_DIAL,
						Uri.parse(strText));
				
				AppUtils.checkAndCall(aActivity,pIntent);
				
			/*} else if (strText.startsWith("smsto:")) {
				// smsto:13818651108:hello there?
				String aSegments[] = strText.split(":");
				if (aSegments.length >= 3) {
					String strUri = aSegments[0] + ":" + aSegments[1];
					Intent pIntent = new Intent(Intent.ACTION_SENDTO,
							Uri.parse(strUri));
					pIntent.putExtra("sms_body", aSegments[2]);
					aActivity.startActivity(pIntent); 
				} else {
					HomeActivity.showBarcodeContent(aActivity, strText,
							bInternal);
				}*/
			} else {
				// Just show the message.
				HomeActivity.showBarcodeContent(aActivity, strText, bInternal);
			}
		} else {
			// Prompt error message for user.
			HomeActivity.showBarcodeContent(aActivity, "null", bInternal);
		}
	}

	/**
	 * showBarcodeContent
	 * 
	 * @param strContent
	 */
	private static void showBarcodeContent(BaseActivity aActivity,
			String strContent, boolean bInternal) {
		if (bInternal) {
			String strTitle = aActivity.getString(R.string.scan_result);
			String strLabel = aActivity.getString(R.string.btn_ok);
			UiUtils.showDialog(aActivity, strTitle, strContent, strLabel);
		}
	}

	/**
	 * isAvailableUrl
	 * 
	 * @param strText
	 * @return
	 */
	private static boolean isAvailableUrl(String strText) {
		if ((TextUtils.isEmpty(strText)) || (strText.startsWith("wap2app:")))
			return false;

		return (strText.startsWith("http:") || strText.startsWith("https:")
				|| strText.startsWith("wap") || strText.startsWith("www"));
//				|| strText.contains(".com") || strText.contains(".cn")
	//			|| strText.contains(".net") || strText.contains(".org"));
	}

	/**
	 * queryVal
	 * 
	 * @param strUrl
	 * @param strKey
	 * @return
	 */
	public static String queryVal(String strUrl, String strKey,
			boolean bCaseSensitive) {
		String strVal = "";
		if (TextUtils.isEmpty(strUrl)) {
			return strVal;
		}

		String strRegEx = "(?<=" + strKey + "=).*?(?=&|$)";
		Pattern pPattern = Pattern.compile(strRegEx, Pattern.CASE_INSENSITIVE);
		Matcher pMatcher = pPattern.matcher(strUrl);
		strVal = pMatcher.find() ? pMatcher.group() : "";
		if (!TextUtils.isEmpty(strVal)) {
			strVal = strVal.trim();
			if (!bCaseSensitive)
				strVal = strVal.toLowerCase(Locale.getDefault());
		}
		return strVal;
	}

	public static String queryVal(String strUrl, String strKey) {
		return HomeActivity.queryVal(strUrl, strKey, false);
	}

	private void redirect(String keyWord) {
		SearchModel mSearchModel = new SearchModel();
		mSearchModel.setKeyWord(keyWord);

		Bundle param = new Bundle();
		param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, mSearchModel);
		param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, keyWord);
		UiUtils.startActivity(this, ListActivity.class, param, true);
	}

	@Override
	public void onBgLoaded() {
		super.setThemeConfig();

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_Home);
	}
}
