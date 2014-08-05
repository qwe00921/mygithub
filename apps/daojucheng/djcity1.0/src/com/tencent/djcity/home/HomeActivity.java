package com.tencent.djcity.home;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tencent.djcity.R;
import com.tencent.djcity.discover.GiftcenterActivity;
import com.tencent.djcity.discover.ShakeActivity;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.IPageCache;
import com.tencent.djcity.lib.inc.CacheKeyFactory;
import com.tencent.djcity.lib.ui.AnnounceView;
import com.tencent.djcity.lib.ui.AnnounceView.OnAnnounceClickListener;
import com.tencent.djcity.lib.ui.LinearListView;
import com.tencent.djcity.lib.ui.MyScrollView;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.PageIndicator;
import com.tencent.djcity.lib.ui.ProInfoView;
import com.tencent.djcity.lib.ui.ShortcutView;
import com.tencent.djcity.lib.ui.SlideView;
import com.tencent.djcity.lib.ui.SlideView.OnSlideEventListener;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.list.ListAdapter;
import com.tencent.djcity.main.MainActivity;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.msgcenter.MsgCenterActivity;
import com.tencent.djcity.msgcenter.MsgMgr;
import com.tencent.djcity.msgcenter.MsgMgr.MsgObserver;
import com.tencent.djcity.my.MyOrderListActivity;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.search.SearchActivity;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class HomeActivity extends BaseActivity implements OnSuccessListener<Object>, OnSlideEventListener, MsgObserver {
	private Handler mWholeHandler = new Handler();
	private ImageLoader mPreLoader;

	private SlideView mScrollView;

	private AnnounceView mAppAnnounce;
	private PageIndicator mIndicator;
	private ShortcutView mShortcuts;

	private LinearListView mRecommendList;
	private LinearListView mNewProductList;

	private LinearLayout mRootLayout;
	private MyScrollView mRootView;
	private LinearLayout mHeaderLoadingLayout;

	private Intent mIntent;

	private static final int AJAX_EVENT = 1;
	private static final int AJAX_DISPATCHES = 2;
	private static final int AJAX_RELOAD = 3;

	private List<ProInfoView> mCache; // To avoid multi-adding.
	private HomeConfig mConfig;
	private HomeParser mParser;
	private IPageCache mPageCache;
	private boolean isNullHomeInformtion = false;
	private Ajax mRequest = null;

	// private ImageView mFirstSight1;
	// private OnTouchListener mFirstListener;
	private Runnable mFadingRunnable;
	private Runnable mStartAnimRunnable;

	private boolean mFirstEntry = true;

	private GameInfo mGameInfo;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		// Save the current intent.
		mIntent = this.getIntent();
		if (null == mIntent)
		{
			finish();
			return;
		}

		super.onCreate(savedInstanceState);

		mPageCache = new IPageCache();
		mPreLoader = new ImageLoader(this, true);

		setContentView(R.layout.activity_home);

		mRootView = (MyScrollView) this.findViewById(R.id.root_layout);
		// Set onScroll listener.
		mRootView.setOnScrollListener(new MyScrollView.OnScrollListener() {
			@Override
			public void onScroll(boolean bIsScrolling)
			{
				if (bIsScrolling)
				{
					// android.util.Log.d("HomeActivityScroll",
					// "Scrolling now");
					stopAnimation();
				}
				else
				{
					// android.util.Log.d("HomeActivityScroll",
					// "Scrolling end");
					startAnimationDelay();
				}
			}
		});

		mRootLayout = (LinearLayout) findViewById(R.id.home_scrollvew_root_child);

		// Initialize the instance for scroll view.椤甸�㈡��涓���圭��婊���ㄦ椿��ㄩ〉
		mScrollView = (SlideView) findViewById(R.id.home_banner_slide_view);
		mScrollView.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
		mScrollView.setHandler(this.mWholeHandler);
		mScrollView.setImageLoader(mPreLoader);
		mScrollView.setOnSlideEventListener(this);

		// Initialize indicator for banners.
		mIndicator = (PageIndicator) findViewById(R.id.home_banner_indicator);
		mIndicator.setDotDrawableRes(R.drawable.page_indicator);
		mIndicator.setVisibility(View.GONE);

		// // Search box.,���绱㈡��
		// EditText searchBox = (EditText) findViewById(R.id.home_search_box);
		//
		// if (null != searchBox) {
		// searchBox.setKeyListener(null);
		// searchBox.setOnClickListener(this);
		// }
		//
		// findViewById(R.id.home_search_box_barcode).setOnClickListener(this);

		// Announcement.
		mAppAnnounce = (AnnounceView) findViewById(R.id.home_app_announce);
		mAppAnnounce.setOnAnnounceClickListener(new OnAnnounceClickListener() {
			@Override
			public void onAnnounceClick(boolean isClose)
			{
				if (isClose && null != mAppAnnounce)
				{
					mAppAnnounce.setVisibility(View.GONE);
					Announce pAnnounce = (null != mConfig.mAnnounce ? mConfig.mAnnounce : null);
					if (null != pAnnounce)
					{
						Preference.getInstance().setAnnounceID(pAnnounce.getAnnounceId());
					}
				}
			}
		});

		// Shortcut,���褰╃エ锛�澶╁ぉ��������ｄ釜
		mShortcuts = (ShortcutView) findViewById(R.id.home_shortcuts);
		mShortcuts.setOnShortcutSelectListener(new ShortcutView.onShotcutSelectListner() {
			@Override
			public void onShortcutSelect(int nIndex)
			{
				// goneFirstSight();

				switch (nIndex) {
				case 0:
					MainActivity main = (MainActivity) getParent();
					main.showTab(MainActivity.TAB_CATEGORY);
					break;
				case 1:
					ToolUtil.checkLoginOrRedirect(HomeActivity.this, MyOrderListActivity.class);
					break;
				case 2:
					UiUtils.startActivity(HomeActivity.this, GiftcenterActivity.class
							, true);
					break;
				case 3:
					Intent guaIntent = new Intent(HomeActivity.this, ShakeActivity.class);
					startActivity(guaIntent);
					break;

				}
			}
		});

		mRecommendList = (LinearListView) findViewById(R.id.home_recommend_lv);
		mRecommendList.setClickable(false);
		mRecommendList.setOnItemClickListener(mOnItemClickListener);
		mNewProductList = (LinearListView) findViewById(R.id.home_new_products);
		mNewProductList.setClickable(false);
		mNewProductList.setOnItemClickListener(mOnItemClickListener);

		mNavBar = (NavigationBar) findViewById(R.id.home_navigation_bar);
		mNavBar.setOnIndicatorClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{
				ToolUtil.startActivity(HomeActivity.this, SearchActivity.class);
			}
		});
		
		mNavBar.setOnLeftButtonClickListener(new NavigationBar.OnLeftButtonClickListener() {

			@Override
			public void onClick()
			{
				// TODO Auto-generated method stub
				ToolUtil.startActivity(HomeActivity.this, MsgCenterActivity.class);
				//ToolUtil.checkLoginOrRedirect(HomeActivity.this, MsgCenterActivity.class, null, true);
			}
				
		});

		mParser = new HomeParser();

		mFirstEntry = true;

		// 娉ㄥ�����杩�缃���板井淇℃��浠讹拷?
		WXAPIFactory.createWXAPI(this, Config.APP_ID).registerApp(Config.APP_ID);
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Intent intent = new Intent(HomeActivity.this, ItemActivity.class);

			intent.putExtra(ItemActivity.KEY_PROP_ID, String.valueOf(id));
			startActivity(intent);
		}
	};

	@Override
	public void onNewIntent(Intent intent)
	{
		this.handleNewIntent(intent);
	}

	private void handleNewIntent(Intent aIntent)
	{
		Bundle pBundle = (null != aIntent ? aIntent.getExtras() : null);
		if (null == pBundle)
			return;

		// handle message

		if (pBundle.containsKey(Config.EXTRA_BARCODE))
		{
			String strUri = pBundle.getString(Config.EXTRA_BARCODE);
			HomeActivity.processUrlInfo(this, Activity.RESULT_OK, strUri, false);
			setIntent(null);
		}
		else if (pBundle.containsKey(Config.EXTRA_WEIXIN))
		{
			String strUri = pBundle.getString(Config.EXTRA_WEIXIN);
			this.onWeixinURL(strUri);
			setIntent(null);
		}
	}

	private boolean handleUri(String strKey, String strUri)
	{
		if ((!TextUtils.isEmpty(strKey)) && (!TextUtils.isEmpty(strUri)))
		{
			if (strKey.equalsIgnoreCase(Config.EXTRA_BARCODE))
			{
				HomeActivity.processUrlInfo(this, Activity.RESULT_OK, strUri, false);
				return true;
			}
			else if (strKey.equalsIgnoreCase(Config.EXTRA_WEIXIN))
			{
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

	private void onWeixinURL(String strUri)
	{
		if (TextUtils.isEmpty(strUri))
		{
			return;
		}

		String strProductId = queryVal(strUri, "pid");
		String channelId = queryVal(strUri, "channelid");
		// update tag and y_track
		/*
		 * if(!TextUtils.isEmpty(strProductId)) { Bundle pBundle = new Bundle();
		 * pBundle.putLong(ItemActivity.REQUEST_PRODUCT_ID,
		 * Long.valueOf(strProductId)); if ((!TextUtils.isEmpty(channelId)) &&
		 * (TextUtils.isDigitsOnly(channelId))) {
		 * pBundle.putInt(ItemActivity.REQUEST_CHANNEL_ID,
		 * Integer.valueOf(channelId)); } UiUtils.startActivity(this,
		 * ItemActivity.class, pBundle, true); ToolUtil.sendTrack("weixin",
		 * getString(R.string.tag_weixin), ItemActivity.class.getName(),
		 * getString(R.string.tag_ItemActivity), "1001", "1", strProductId,
		 * strTag); }
		 */
	}

	/*
	 * private void onPushMessage(MsgEntity aEntity) { // Check whether need
	 * login. mEntity = null; if ((null != aEntity) && (aEntity.mLogin) && (0 >=
	 * ILogin.getLoginUid())) { UiUtils.makeToast(this, R.string.need_login);
	 * UiUtils.startActivity(this, LoginActivity.class, null,
	 * HomeActivity.REQUEST_PUSH_MESSAGE, true); mEntity = aEntity; } else {
	 * MessageActivity.processEntity(this, aEntity, "push"); } }
	 */

	@Override
	public void onItemClick(View aView, int nIndex)
	{
		this.handleEvent(null != mConfig ? mConfig.getBannerInfo(nIndex) : null);
	}

	@Override
	public void onPostionUpdate(int nIndex, int nTotal)
	{
		if (null != mIndicator)
		{
			mIndicator.setCurrentItem(nIndex);
		}
	}

	private void handleEvent(Object aObject)
	{
		if (null == aObject)
			return;

		BannerInfo pInfo = (BannerInfo) aObject;
		String strType = pInfo.getBannerType();
		if (strType.equals(BannerInfo.MODULE_INNER_LINK))
		{
			// banner -> h5
			String strUrl = pInfo.getLinkURL();

			strUrl = "http://daoju.qq.com/v3/mapp/cf/index.html";

			Bundle bundle = new Bundle();
			bundle.putString(HTML5LinkActivity.LINK_URL, TextUtils.isEmpty(strUrl) ? null : strUrl);

			// Check back home activity.
			UiUtils.startActivity(this, HTML5LinkActivity.class, bundle, true);

		}
		else if (strType.equals(BannerInfo.MODULE_ITEM))
		{
			// banner -> item
			Bundle abundle = new Bundle();
			abundle.putString(ItemActivity.KEY_PROP_ID, pInfo.mTargetId);
			UiUtils.startActivity(this, ItemActivity.class, abundle, true);
		}

	}

	/**
	 * initBanner
	 * 
	 * @return
	 */
	private void initBanner()
	{
		if (null == mScrollView)
			return;

		List<BannerInfo> aBanners = (null != mConfig ? mConfig.getBanners() : null);
		mScrollView.removeAllViews();
		final int nCount = (null != aBanners ? aBanners.size() : 0);
		mScrollView.setVisibility(nCount > 0 ? View.VISIBLE : View.GONE);
		mIndicator.setVisibility(nCount > 1 ? View.VISIBLE : View.GONE);
		mIndicator.setTotalItems(nCount);

		BannerInfo pInfo = null;
		for (int nIdx = 0; nIdx < nCount; nIdx++)
		{
			pInfo = aBanners.get(nIdx);

			// if(nIdx == 0)
			// pInfo.mLinkUrl =
			// "http://beta.m.yixun.com/m/touch_in_app_test.html";
			// pInfo.mLinkUrl =
			// "http://beta.m.yixun.com/m/touch_in_app_test.html?wapBack=1";
			mScrollView.addImageView(pInfo.getPicUrl(), nIdx);
		}
	}

	private void loadMessageCenter()
	{
	}

	private boolean checkHomeConfig()
	{
		boolean bRequesting = false;
		final boolean bIsExpire = mPageCache.isExpire(CacheKeyFactory.HOME_CHANNEL_INFO);
		String strContent = mPageCache.getNoDelete(CacheKeyFactory.HOME_CHANNEL_INFO);
		if (null == mConfig || bIsExpire)
		{
			final long pCreateTime = mPageCache.getRowCreateTime(CacheKeyFactory.HOME_CHANNEL_INFO);
			if (!TextUtils.isEmpty(strContent))
			{
				try
				{
					if (null == mParser)
					{
						mParser = new HomeParser();
					}
					mConfig = mParser.parse(strContent);
				}
				catch (Exception ex)
				{
					Log.e(LOG_TAG, ex);
					mConfig = null;
				}
				finally
				{
					mParser = null;
				}

				if (null != mConfig)
				{
					mIntent = this.getIntent();
					bodyRequestFinish();
				}
			}
			else
			{
				isNullHomeInformtion = true;
			}
		}

		// version 涓�杞界‘璁ゅ��portal杩�琛�锛�mainactivity 宸茬�����杩�������淇�瀛�
		// Preference pPreference = Preference.getInstance();
		// 榛�璁ゅ��绔�id濡���������� �����介��瑕������版�����.
		if ((null != mConfig) || bIsExpire || TextUtils.isEmpty(strContent))
		{
			// || IcsonApplication.mVersionCode > pPreference.getProjVersion())
			// {
			this.getHomeConfig(mFirstEntry);
			bRequesting = true;

			if (isNullHomeInformtion)
			{
				showLoadingLayer();
			}
		}

		return bRequesting;
	}

	private void getHomeConfig(boolean bReload)
	{
		if (null != mRequest)
			return;

		mRequest = AjaxUtil
				.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/RecommendList.php?plat=2&page=1&recommids=1,2");

		// mRequest =
		// AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getBanner.php");//ServiceConfig.getAjax(Config.URL_HOME_GETINFO);
		if ((null == mRequest) || (null == mPageCache))
		{
			hideHeaderLoading();
			return;
		}

		mRequest.setData("busid", null == mGameInfo ? 0 : mGameInfo.getBizCode());
		// hostlist static page. NO Need send latestcate
		// String strLastCate = RecentCates.getString(1);
		// if( !TextUtils.isEmpty(strLastCate) ) {
		// mRequest.setData("latestcate", strLastCate);
		// }

		if (null == mParser)
		{
			mParser = new HomeParser();
		}

		String strProvinceIPId = "";
		mRequest.setData("fetchCity", strProvinceIPId);

		mRequest.setParser(mParser);
		mRequest.setId(bReload ? AJAX_RELOAD : AJAX_EVENT);
		mRequest.setOnSuccessListener(this);
		mRequest.setOnErrorListener(this);

		mRequest.send();
	}

	@Override
	public void onSuccess(Object v, Response response)
	{
		final int nRequestId = response.getId();
		if (AJAX_EVENT == nRequestId || AJAX_RELOAD == nRequestId)
		{
			mRequest = null;
			if (!mParser.isSuccess())
			{
				UiUtils.makeToast(this, mParser.getErrMsg());
				closeLoadingLayer(true);
				return;
			}

			closeLoadingLayer();

			mConfig = (HomeConfig) v;
			bodyRequestFinish();

			mPageCache.set(CacheKeyFactory.HOME_CHANNEL_INFO, mParser.getString(), 5 * 60);

			mParser = null;
			// this.setCityCache();

			this.hideHeaderLoading();

			mFirstEntry = false;
		}
	}

	@Override
	public void onError(Ajax ajax, Response response)
	{
		final int nRequestId = response.getId();
		if (AJAX_EVENT == nRequestId || AJAX_RELOAD == nRequestId)
		{
			closeLoadingLayer();

			mRequest = null;
			this.hideHeaderLoading();

			if (isNullHomeInformtion == true)
			{
				super.onError(ajax, response);
			}
		}
	}

	private void hideHeaderLoading()
	{
		if (null != mHeaderLoadingLayout && mHeaderLoadingLayout.getVisibility() == View.VISIBLE)
		{
			mHeaderLoadingLayout.setVisibility(View.GONE);
		}
	}

	// private void setCityCache()
	// {
	// final int nCityId = (null != mConfig ? mConfig.mCityId : -1);
	// if( nCityId >= 0 && null != mPageCache ) {
	// if( mPageCache.get(CacheKeyFactory.CACHE_CITY_ID) == null ){
	// DispatchFactory.setDefaultCityId( mConfig.mCityId );
	// }
	// }
	// }

	/**
	 * 
	 */
	private void bodyRequestFinish()
	{
		// Parse the banner information.
		this.initBanner();

		// Initialize announcement.
		initAnnounce();
		initRecommend();
		initNewProducts();
		// Start the animation.
		stopAnimation();
		startAnimation();

		// Try to update intent.
		handleNewIntent(mIntent);
		mIntent = null;

	}

	// public void sendRequest() {
	//
	// Ajax pAjax = ServiceConfig.getAjax(Config.URL_EVENT_TIMEBUY);
	// if( null == pAjax )
	// return ;
	//
	// pAjax.setOnSuccessListener(new OnSuccessListener<RecommendModel>() {
	// @Override
	// public void onSuccess(RecommendModel v, Response response) {
	// // TODO Auto-generated method stub
	// RecommendAdapter mAdapter = new RecommendAdapter(HomeActivity.this,
	// v.getProducts());
	// mRecommendList.setAdapter(mAdapter);
	// }
	// });
	// pAjax.setOnErrorListener(this);
	// pAjax.setParser(new RecommendParser());
	//
	// // Add parameters.
	// pAjax.setData("type", 0);
	// pAjax.setData("page", 0);
	// pAjax.setData("size", 6);
	//
	// pAjax.send();
	//
	// }

	private void initRecommend()
	{

		// sendRequest();
		// mRecommendList.setDividerHeight(0);
		// mRecommendList.setHeaderDividersEnabled(false);
		// mRecommendList.setFooterDividersEnabled(false);
		// // mRecommendList.setOnScrollListener(this);
		//
		// mProducts.addAll(mModel.getProducts());
		//
		// if( null == mAdapter )
		// {
		// RecommendAdapter mAdapter = new RecommendAdapter(mActivity,
		// mProducts);
		// mRecommendList.setAdapter(mAdapter);
		// }
		// else
		// {
		// mAdapter.notifyDataSetChanged();
		// }
		ListAdapter mAdapter = new ListAdapter(HomeActivity.this, mConfig.mRecommends);
		mRecommendList.setAdapter(mAdapter);
	}

	private void initNewProducts()
	{

		// sendRequest();
		// mRecommendList.setDividerHeight(0);
		// mRecommendList.setHeaderDividersEnabled(false);
		// mRecommendList.setFooterDividersEnabled(false);
		// // mRecommendList.setOnScrollListener(this);
		//
		// mProducts.addAll(mModel.getProducts());
		//
		// if( null == mAdapter )
		// {
		// RecommendAdapter mAdapter = new RecommendAdapter(mActivity,
		// mProducts);
		// mRecommendList.setAdapter(mAdapter);
		// }
		// else
		// {
		// mAdapter.notifyDataSetChanged();
		// }
		ListAdapter mAdapter = new ListAdapter(HomeActivity.this, mConfig.mNewProducts);
		mNewProductList.setAdapter(mAdapter);
	}

	private void initAnnounce()
	{
		if (null == mConfig || null == mAppAnnounce)
			return;
		Announce announce = mConfig.mAnnounce;
		if (announce == null)
		{
			mAppAnnounce.setVisibility(View.GONE);
			return;
		}

		int saveAnnounceID = Preference.getInstance().getAnnounceID();
		String strAnnounce = announce.getMsg();
		// 濡����褰�������������id >
		// 瀹㈡�风��淇�瀛����������涓�涓�������id锛�骞朵�����杩����锛���ｄ����剧ず��板�����
		if (announce.getAnnounceId() != saveAnnounceID && !announce.isOutTime() && !TextUtils.isEmpty(strAnnounce))
		{
			mAppAnnounce.setVisibility(View.VISIBLE);
			mAppAnnounce.setText(strAnnounce);
		}
		else
		{
			mAppAnnounce.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		MsgMgr msgMgr = MsgMgr.getInstance();
		msgMgr.setMsgObserver(this);
		msgMgr.getMsgNum();

		startAnimationDelay();

		mGameInfo = GameInfo.getGameInfoFromPreference();
		
		if(mGameInfo != null) {
			mNavBar.setText(mGameInfo.getBizName() + getString(R.string.app_name));
		}
		// Check the home configuration status.
		if (!checkHomeConfig())
		{
			if (null == mIntent)
				mIntent = this.getIntent();
			this.handleNewIntent(mIntent);
			mIntent = null;
		}
	}

	@Override
	public void onResult(int num)
	{
		ImageView imgView = mNavBar.getLeftBack();
		if (num != 0)
		{
			imgView.setImageResource(R.drawable.ico_mail_new);
		}
		else
		{
			imgView.setImageResource(R.drawable.ico_mail);
		}
	}

	@Override
	protected void onPause()
	{
		this.stopAnimation();

		super.onPause();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		this.stopAnimation();
	}

	private void startAnimationDelay()
	{
		if (null == mStartAnimRunnable)
		{
			mStartAnimRunnable = new Runnable() {

				@Override
				public void run()
				{
					stopAnimation();
					startAnimation();
				}
			};
		}
		mWholeHandler.removeCallbacks(mStartAnimRunnable);
		mWholeHandler.postDelayed(mStartAnimRunnable, 500);
	}

	private void startAnimation()
	{
		if (null != mScrollView)
			mScrollView.startSlide();
	}

	private void stopAnimation()
	{
		// Try to do a system gc.
		// System.gc();

		if (null != mScrollView)
			mScrollView.stopSlide();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		if (null != mCache)
		{
			mCache.clear();
			mCache = null;
		}

		mScrollView.removeAllViews();
		mPreLoader = null;
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2618;
	private static final int REQUEST_SCAN_BARCODE = 2620;
	private static final int REQUEST_MESSAGE_CENTER = 2621;
	private static final int REQUEST_PUSH_MESSAGE = 2622;
	private static final int REQUEST_COLLECT = 2623;

	/**
	 * onBarcodeResult
	 * 
	 * @param nResultCode
	 * @param strText
	 */
	public static void processUrlInfo(BaseActivity aActivity, int nResultCode, String strText, boolean bInternal)
	{
		if (Activity.RESULT_CANCELED == nResultCode)
			return;

		if (!TextUtils.isEmpty(strText))
		{
			String strSource = queryVal(strText, AppStorage.KEY_SOURCE);
			if (!TextUtils.isEmpty(strSource))
			{
				AppStorage.setData(AppStorage.SCOPE_CPS, AppStorage.KEY_SOURCE, strSource, true);

			}

			// 1. find type parameter.
			String strType = queryVal(strText, "type");
			String strSiteId = queryVal(strText, "siteid");
			String strTag = queryVal(strText, "ytag");
			String comefrom = queryVal(strText, "comefrom");

			if (strType.equals("event"))
			{
				// Handle URL result for event.
				String strTemplateId = queryVal(strText, "templateid");
				if (TextUtils.isEmpty(strTemplateId) || !TextUtils.isDigitsOnly(strTemplateId))
				{
					HomeActivity.showBarcodeContent(aActivity, "null", bInternal);
					return;
				}
			}
			else if (strType.equals("category"))
			{
				MainActivity.startActivity(aActivity, MainActivity.TAB_CATEGORY);
				return;
			}
			/*
			 * else if(strType.equals("search")) { String keyword =
			 * URLDecoder.decode(queryVal(strText, "keyword")); //@param String
			 * key ��抽��瀛� String path = queryVal(strText, "path"); // @param
			 * String path 瀵艰��id璺�寰� String classid = queryVal(strText,
			 * "classid"); //@param String classid ���绫�id if(
			 * TextUtils.isEmpty(keyword) && TextUtils.isEmpty(path)&&
			 * TextUtils.isEmpty(classid) ) { return ; }
			 * 
			 * String tempValue = queryVal(strText, "sort"); int sort =
			 * -1;//@param int sort ���搴���瑰�� if(!TextUtils.isEmpty(tempValue)
			 * && TextUtils.isDigitsOnly(tempValue)) { sort =
			 * Integer.valueOf(tempValue); }
			 * 
			 * int page = -1; //@param int page 绗����椤� tempValue =
			 * queryVal(strText, "page"); if(!TextUtils.isEmpty(tempValue) &&
			 * TextUtils.isDigitsOnly(tempValue)) { page =
			 * Integer.valueOf(tempValue); } String price = queryVal(strText,
			 * "price"); //@param String price 浠锋�煎�洪�� String attrinfo =
			 * queryVal(strText, "attrinfo");//@param String attrinfo 灞���ц��婊�
			 * 
			 * int areacode = -1;//@param int areacode 浠�搴���板�轰唬��� tempValue =
			 * queryVal(strText, "areacode"); if(!TextUtils.isEmpty(tempValue)
			 * && TextUtils.isDigitsOnly(tempValue)) { areacode =
			 * Integer.valueOf(tempValue); }
			 * 
			 * SearchModel model = new SearchModel();
			 * if(!TextUtils.isEmpty(keyword)) model.setKeyWord(keyword);
			 * 
			 * if(!TextUtils.isEmpty(path)) model.setPath(path);
			 * 
			 * if(!TextUtils.isEmpty(classid)) model.setClassId(classid);
			 * 
			 * if(sort >=0 ) model.setSort(sort);
			 * 
			 * if(page >= 0) model.setCurrentPage(page);
			 * 
			 * if(!TextUtils.isEmpty(price)) model.setPrice(price);
			 * 
			 * if(!TextUtils.isEmpty(attrinfo)) model.setOption(attrinfo);
			 * 
			 * if(areacode > 0) model.setAreaCode(areacode);
			 * 
			 * Bundle param = new Bundle();
			 * param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, model);
			 * param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, keyword);
			 * param.putBoolean(ListActivity.REQUEST_SEARCH_FROM_WX,
			 * "weixin".equals(comefrom)); UiUtils.startActivity(aActivity,
			 * ListActivity.class, param, true);
			 * 
			 * return; }
			 */
			/*
			 * else if (strType.equals("proinfo")) { // Handle URL for product
			 * info. Bundle pBundle = new Bundle(); String strProductId =
			 * queryVal(strText, "productid");
			 * if(TextUtils.isEmpty(strProductId)) strProductId =
			 * queryVal(strText, "pid"); if ((TextUtils.isEmpty(strProductId))
			 * || (!TextUtils.isDigitsOnly(strProductId))) {
			 * HomeActivity.showBarcodeContent(aActivity, "null", bInternal);
			 * return; }
			 * 
			 * pBundle.putLong(ItemActivity.REQUEST_PRODUCT_ID,
			 * Long.valueOf(strProductId));
			 * pBundle.putBoolean(ItemActivity.REQUEST_SEARCH_FROM_WX,
			 * "weixin".equals(comefrom));
			 * 
			 * // Parse channel id. String strChannelId = queryVal(strText,
			 * "channelId"); if (!TextUtils.isEmpty(strChannelId) &&
			 * TextUtils.isDigitsOnly(strChannelId)) {
			 * pBundle.putInt(ItemActivity.REQUEST_CHANNEL_ID,
			 * Integer.valueOf(strChannelId)); }
			 * 
			 * UiUtils.startActivity(aActivity, ItemActivity.class, pBundle,
			 * true); }
			 */else if (strType.equals("home"))
			{
				// Do nothing, as home page already activated.
				if (!aActivity.getClass().equals(HomeActivity.class))
				{
					UiUtils.startActivity(aActivity, HomeActivity.class, true);
				}
			}
			else if (HomeActivity.isAvailableUrl(strText))
			{
				// Default action is to start a web browser.
				Intent pIntent = new Intent(Intent.ACTION_VIEW);
				pIntent.setData(Uri.parse(strText));
				aActivity.startActivity(pIntent);
			}
			else if (strText.startsWith("tel:"))
			{
				Intent pIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(strText));

				AppUtils.checkAndCall(aActivity, pIntent);

				/*
				 * } else if (strText.startsWith("smsto:")) { //
				 * smsto:13818651108:hello there? String aSegments[] =
				 * strText.split(":"); if (aSegments.length >= 3) { String
				 * strUri = aSegments[0] + ":" + aSegments[1]; Intent pIntent =
				 * new Intent(Intent.ACTION_SENDTO, Uri.parse(strUri));
				 * pIntent.putExtra("sms_body", aSegments[2]);
				 * aActivity.startActivity(pIntent); } else {
				 * HomeActivity.showBarcodeContent(aActivity, strText,
				 * bInternal); }
				 */
			}
			else
			{
				// Just show the message.
				HomeActivity.showBarcodeContent(aActivity, strText, bInternal);
			}
		}
		else
		{
			// Prompt error message for user.
			HomeActivity.showBarcodeContent(aActivity, "null", bInternal);
		}
	}

	/**
	 * showBarcodeContent
	 * 
	 * @param strContent
	 */
	private static void showBarcodeContent(BaseActivity aActivity, String strContent, boolean bInternal)
	{
		if (bInternal)
		{
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
	private static boolean isAvailableUrl(String strText)
	{
		if ((TextUtils.isEmpty(strText)) || (strText.startsWith("wap2app:")))
			return false;

		return (strText.startsWith("http:") || strText.startsWith("https:") || strText.startsWith("wap") || strText
				.startsWith("www"));
		// || strText.contains(".com") || strText.contains(".cn")
		// || strText.contains(".net") || strText.contains(".org"));
	}

	/**
	 * queryVal
	 * 
	 * @param strUrl
	 * @param strKey
	 * @return
	 */
	public static String queryVal(String strUrl, String strKey, boolean bCaseSensitive)
	{
		String strVal = "";
		if (TextUtils.isEmpty(strUrl))
		{
			return strVal;
		}

		String strRegEx = "(?<=" + strKey + "=).*?(?=&|$)";
		Pattern pPattern = Pattern.compile(strRegEx, Pattern.CASE_INSENSITIVE);
		Matcher pMatcher = pPattern.matcher(strUrl);
		strVal = pMatcher.find() ? pMatcher.group() : "";
		if (!TextUtils.isEmpty(strVal))
		{
			strVal = strVal.trim();
			if (!bCaseSensitive)
				strVal = strVal.toLowerCase(Locale.getDefault());
		}
		return strVal;
	}

	public static String queryVal(String strUrl, String strKey)
	{
		return HomeActivity.queryVal(strUrl, strKey, false);
	}

}
