package com.icson.item;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.IShoppingCart;
import com.icson.lib.IViewHistory;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.WanggouProHelper;
import com.icson.lib.model.ReviewCountModel;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.main.MainActivity;
import com.icson.shoppingcart.ShoppingCartActivity;
import com.icson.slotmachine.WXShareResultReceiver;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.tencent.mm.sdk.modelbase.BaseResp;

public class ItemActivity extends BaseActivity implements
		OnCheckedChangeListener, AppUtils.DescProvider, TabDetailSuccLisener {

	private static final String LOG_TAG = ItemActivity.class.getName();

	private long mProductId;
	private int channel_id;// 场景id
	private int pay_type;// add by xingyao
	private String dap="";// 看了看，买了买点击跟踪

	public static final String REQUEST_PRODUCT_ID = "product_id";
	public static final String REQUEST_CHANNEL_ID = "channel_id";
	public static final String REQUEST_PAY_TYPE = "pay_type";
	public static final String REQUEST_DAP = "dap";// 统计看了看，买了买点击
	public static final String REQUEST_SEARCH_FROM_WX = "search_from_wx";

	public static final int REQUEST_FLAG_FAVOR = 1;
	public static final int REQUEST_FLAG_ADD_CART = 2;
	public static final int REQUEST_FLAG_ADD_NOTIFY = 3;

	private static final int TAB_DETAIL = 0;
	private static final int TAB_PARAM = 1;
	private static final int TAB_REVIEW = 2;
	private static final int TAB_INTRO = 3;
	public RadioGroup mRadioGroup;
	private int lastSelectIndex;
	private String strPageId;
	private ArrayList<Bundle> mBundleList;
	private static final int   MAX_BUNDLE_SIZE = 3;
	public long getProductId() {
		return mProductId;
	}

	public int getChannelId() {
		return channel_id;
	}

	public String getDAP() {
		return dap;
	}
	private ViewPager mViewPager;
	int tabIDs[] = { R.id.item_radio_detail, R.id.item_radio_param,R.id.item_radio_review,
			R.id.item_radio_intro };
//	private static HashMap<Integer, Integer> tabs = new HashMap<Integer, Integer>();
	private static SparseIntArray tabs = new SparseIntArray();

	static {
		tabs.put(R.id.item_radio_detail, R.id.item_relative_tab_content_detail);
		tabs.put(R.id.item_radio_intro, R.id.item_relative_tab_content_intro);
		tabs.put(R.id.item_radio_param, R.id.item_relative_tab_content_param);
		tabs.put(R.id.item_radio_review, R.id.item_relative_tab_content_review);
	}

	private int       mLastDistrictId = 0;
	ItemTabDetailView mItemTabDetailView;  //商品详情
	ItemTabIntroView mItemTabIntroView; // 图文详情Webview
	ItemTabParamView mItemTabParamView;   //规格参数
	ItemTabReviewView mItemTabReviewView;  //评论
	private boolean mSearchFromWx;
	private Button mBackWx;
	private WXShareResultReceiver mWXShareReceiver;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Intent aintent = getIntent();
		if(null == aintent)
		{
			finish();
			return;
		}

		Bundle abundle = aintent.getExtras();
		if(!freshProcWithBundle(abundle))
			return;
		addBundle(abundle);
		mSearchFromWx = abundle.getBoolean(REQUEST_SEARCH_FROM_WX, false);
		
		setContentView(R.layout.activity_item_tabs);

		findViewById(R.id.navigationbar_drawable_left_view).setOnClickListener(this);
		findViewById(R.id.navigationbar_text).setOnClickListener(this);
		findViewById(R.id.shopping_cart).setOnClickListener(this);
		
		mRadioGroup = (RadioGroup) findViewById(R.id.item_radiogroup);
		
		mViewPager = (ViewPager) findViewById(R.id.item_relative_tab_content);
		mViewPager.setAdapter(new ItemViewPagerAdapter(this));
		
		mRadioGroup.setOnCheckedChangeListener(this);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int index) {
				onCheckedChanged(mRadioGroup, tabIDs[index]);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		mBackWx = (Button) findViewById(R.id.btn_back_wx);
		if(mSearchFromWx) {
			mBackWx.setVisibility(View.VISIBLE);
		} else {
			mBackWx.setVisibility(View.GONE);
		}
		
		reportPid = mProductId + "";
		reportExtraInfo = TextUtils.isEmpty(dap) ? "" : "dap:" + dap + "|";
		
		// Event for viewing item

		mBackWx.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				MainActivity.exitApp(ItemActivity.this);
			}
		});		// Event for viewing item
		
		if(null == mWXShareReceiver)
			mWXShareReceiver = new WXShareResultReceiver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.BROADCAST_FROM_WXSHARE);
		registerReceiver(mWXShareReceiver, filter,Config.SLEF_BROADCAST_PERMISSION,null);
		
		StatisticsEngine.trackEvent(this, "view_proinfo", "productId="
				+ mProductId);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if(mBackWx != null) {
			mBackWx.setVisibility(View.GONE);
		}
	}

	public boolean freshProcWithBundle(Bundle bundle) {
		if(null == bundle)
			return false;
		
		mProductId = bundle.getLong(REQUEST_PRODUCT_ID, 0);
		if (mProductId == 0) {
			Log.e(LOG_TAG, "onCreate|product_id is 0.");
			UiUtils.makeToast(this, R.string.params_empty);
			return false;
		}
		
		strPageId = getString(R.string.tag_ItemActivity);
		channel_id = bundle.getInt(REQUEST_CHANNEL_ID, 0);
		pay_type = bundle.getInt(REQUEST_PAY_TYPE, 0);
		dap = bundle.getString(REQUEST_DAP);
		
		return true;
	}

	/**
	 * 
	 * @param bundle
	 */
	public void addBundle(Bundle bundle)
	{
		if(null == mBundleList)
			mBundleList = new ArrayList<Bundle>();
		if(null!=bundle)
			mBundleList.add(0,bundle);
		//max save 3 steps
		if(mBundleList.size() > MAX_BUNDLE_SIZE)
		{
			mBundleList.remove(MAX_BUNDLE_SIZE);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Bundle popBundle()
	{
		if(null != mBundleList && mBundleList.size()>0)
		{
			//first backpop bundle == current Bundle
			mBundleList.remove(0);
			if(mBundleList.size()>0)
				return mBundleList.get(0);
			else 
				return null;
		}
		return null;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Bundle bundle = popBundle();
			if(null!=bundle)
			{
				freshProcWithBundle(bundle);
				if(null!=mItemTabDetailView)
					mItemTabDetailView.clean();
				init(mProductId);
				return true;
			}
			else
			{
				if(null!=mBundleList)
					mBundleList.clear();		
				return super.onKeyDown(keyCode, event);
			}
		}
		else
			return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onResume()
	{
		//back 回来区域变化重拉数据
		if(mLastDistrictId!=FullDistrictHelper.getDistrictId())
		{
			if(null!=mItemTabDetailView)
				mItemTabDetailView.clean();
			init(mProductId);// 254595
			mLastDistrictId=FullDistrictHelper.getDistrictId();
		}
		if(TAB_INTRO == mViewPager.getCurrentItem())
		{
			if(null!=mItemTabIntroView)
				mItemTabIntroView.onResume();
		}
		
		if(null!=mWXShareReceiver)
		{
			if(mWXShareReceiver.isShareSucc())
			{
				AppUtils.informWXShareResult(this,BaseResp.ErrCode.ERR_OK);
				mWXShareReceiver.clearShareSucc();
			}
		}
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		if(null!=mItemTabIntroView)
			mItemTabIntroView.onPause();
		
		super.onPause();
	}
	
	public void init(long productId) {

		mProductId = productId;

		// 浏览历史
		IViewHistory.set(mProductId);

		if (mItemTabReviewView != null) {
			mItemTabReviewView.clean();
		}

		if (mItemTabParamView != null) {
			mItemTabParamView.clean();
		}

		if (mItemTabIntroView != null) {
			mItemTabIntroView.clean();
		}

		((RadioButton) findViewById(R.id.item_radio_detail)).setChecked(true);

		if (lastSelectIndex == R.id.item_radio_detail) {
			onCheckedChanged(mRadioGroup, lastSelectIndex);
		}
		int num = IShoppingCart.getProductCount();
		TextView topCart = (TextView)findViewById(R.id.shopping_cart_num);
		topCart.setText(num+"");
		topCart.setVisibility(num > 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 进入购物车
		case R.id.shopping_cart:
		//	MainActivity.startActivity(this, MainActivity.TAB_CART);
			ShoppingCartActivity.loadShoppingCart(this, true, true);
			String strDAP = TextUtils.isEmpty(dap) ? "" : "DAP:"+dap+"|";
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21005", strDAP, String.valueOf(mProductId));
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "02011");
			break;
		// 回退
		case R.id.navigationbar_drawable_left_view:
		case R.id.navigationbar_text:
			finish();
			break;
		}
	}

	@Override
	public void onDestroy() {
		if (mItemTabDetailView != null) {
			mItemTabDetailView.destroy();
			mItemTabDetailView = null;
		}

		if (mItemTabIntroView != null) {
			mItemTabIntroView.destroy();
			mItemTabIntroView = null;
		}

		if (mItemTabParamView != null) {
			mItemTabParamView.destroy();
			mItemTabParamView = null;
		}

		if (mItemTabReviewView != null) {
			mItemTabReviewView.destroy();
			mItemTabReviewView = null;
		}
	
		if(null == mBundleList)
			mBundleList.clear();
		mBundleList = null;
	
		if(null!=mWXShareReceiver)
			unregisterReceiver(mWXShareReceiver);
		mWXShareReceiver = null;
		
		super.onDestroy();

	}

	public ItemProductModel getItemProductModel() {
		return mItemTabDetailView == null ? null : mItemTabDetailView
				.getItemProductModel();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		boolean changeFlag = false;
		if (lastSelectIndex != 0 && lastSelectIndex != checkedId) {
			View title = group.findViewById(lastSelectIndex);
			if (title != null) {
				((RadioButton) title).setTextColor(getResources().getColor(
						R.color.global_tab_item));
			}
			changeFlag = true;
		}

		
		lastSelectIndex = checkedId;
		View title = group.findViewById(checkedId);
		((RadioButton) title).setTextColor(getResources().getColor(
				R.color.global_tab_item_s));

		((RadioButton) group.findViewById(checkedId)).setChecked(true);

		switch (checkedId) {
		case R.id.item_radio_detail:
			if (mItemTabDetailView == null) {
				mItemTabDetailView = new ItemTabDetailView(this, mViewPager);
				mItemTabDetailView.setListener(this);
			}
			mItemTabDetailView.init();
			mItemTabDetailView.setPayType(pay_type);
			mViewPager.setCurrentItem(TAB_DETAIL);
			
			if(changeFlag) {
				String strDAP = TextUtils.isEmpty(dap) ? "" : "DAP:"+dap+"|";
				ToolUtil.reportStatisticsClick(getString(R.string.tag_ItemActivity), "23000", strDAP, String.valueOf(mProductId));
				ToolUtil.reportStatisticsPV(getString(R.string.tag_ItemActivity), strDAP, String.valueOf(getProductId()));
			}
			
			break;
		case R.id.item_radio_intro:
			if (mItemTabIntroView == null) {
				mItemTabIntroView = new ItemTabIntroView(this);
			}
			mItemTabIntroView.init();
			mViewPager.setCurrentItem(TAB_INTRO);
			if(changeFlag) {
				String strDAP = TextUtils.isEmpty(dap) ? "" : "DAP:"+dap+"|";
				ToolUtil.reportStatisticsClick(getString(R.string.tag_ItemActivity), "23003", strDAP, String.valueOf(mProductId));
				ToolUtil.reportStatisticsPV(getString(R.string.tag_ItemTabIntroView), strDAP, String.valueOf(getProductId()));
			}
			break;
		case R.id.item_radio_param:
			if (mItemTabParamView == null) {
				mItemTabParamView = new ItemTabParamView(this);
			}
			mItemTabParamView.init();
			mViewPager.setCurrentItem(TAB_PARAM);
			if(changeFlag) {
				String strDAP = TextUtils.isEmpty(dap) ? "" : "DAP:"+dap+"|";
				ToolUtil.reportStatisticsClick(getString(R.string.tag_ItemActivity), "23002", strDAP, String.valueOf(mProductId));
				ToolUtil.reportStatisticsPV(getString(R.string.tag_ItemTabParamView), strDAP, String.valueOf(getProductId()));
			}
			
			break;
		case R.id.item_radio_review:
			if (mItemTabReviewView == null) {
				mItemTabReviewView = new ItemTabReviewView(this);
			}
			mItemTabReviewView.init();
			mViewPager.setCurrentItem(TAB_REVIEW);
			if(changeFlag) {
				String strDAP = TextUtils.isEmpty(dap) ? "" : "DAP:"+dap+"|";
				ToolUtil.reportStatisticsClick(getString(R.string.tag_ItemActivity), "23001", strDAP, String.valueOf(mProductId));
				ToolUtil.reportStatisticsPV(getString(R.string.tag_ItemTabReviewView), strDAP, String.valueOf(getProductId()));
			}
			
			break;
		}

		//pause webview
		if(null!=mItemTabIntroView && checkedId!=R.id.item_radio_intro)
			mItemTabIntroView.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FLAG_FAVOR:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				if(mItemTabDetailView != null) {
					View v = (View) findViewById(R.id.order_detail_button_collect);
					if(null != v)
						mItemTabDetailView.onClick(v);
				} else {
					Log.w(LOG_TAG, "[onActivityResult]REQUEST_FLAG_FAVOR, mItemTabDetailView is null");
				}
			}
			break;
		case REQUEST_FLAG_ADD_CART:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				if(mItemTabDetailView != null) {
					mItemTabDetailView.addToShoppingCart();
				} else {
					Log.w(LOG_TAG, "[onActivityResult]REQUEST_FLAG_ADD_CART, mItemTabDetailView is null");
				}
			}
			break;
		case REQUEST_FLAG_ADD_NOTIFY:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				if(mItemTabDetailView != null) {
					mItemTabDetailView.notifyOnArrival();
				} else {
					Log.w(LOG_TAG, "[onActivityResult]REQUEST_FLAG_ADD_NOTIFY, mItemTabDetailView is null");
				}
			}
			break;
		}
	}

	/**
	 * showSharableApps
	 */
	void showSharableApps() {
		ItemProductModel pModel = getItemProductModel();
		if (null == pModel)
			return;
		String strLinkUrl = "http://m.51buy.com/wad-weixin.html?src=barcode&type=proinfo&productid="
				+ pModel.getProductId() + "&pid=" + pModel.getProductId() 
				+ "&channelId=" + channel_id;
		
		String strPicUrl = null;
		if(pModel.getSaleModelType() == ItemProductModel.PRO_SALE_WANGGOU)
			strPicUrl = WanggouProHelper.getAdapterPicUrl(pModel.getMainPic(), 110);
		else
			strPicUrl = IcsonProImgHelper.getAdapterPicUrl(pModel.getProductCharId(), 110);
		
		Log.d(LOG_TAG, "strPicUrl = " + strPicUrl);
		//AppUtils.shareAppInfo(this, pModel.getName(), strLinkUrl, strPicUrl,
		//		this);
		if(AppUtils.checkWX(this))  
		{
			AppUtils.shareSlotInfo(this, pModel.getName(), strLinkUrl, strPicUrl,
				this);
			StatisticsEngine.trackEvent(this, "share_product");
		}
	}

	@Override
	public String getDesc(String strPackageName) {
		if ((TextUtils.isEmpty(strPackageName)))
			return "";

		ItemProductModel pModel = getItemProductModel();
		if (null == pModel)
			return "";

		String strContent = null;
		if (strPackageName.equals("com.tencent.mm")) {
			// Share to weixin
			String strWords = pModel.getPromotionWord();
			String strPrice = ItemTabDetailView.getDisplayPriceStr(this,pModel);
			strContent = (TextUtils.isEmpty(strPrice) ? getString(R.string.not_for_sale)   : getString(R.string.rmb_yuan, strPrice)) + " "
						+(TextUtils.isEmpty(strWords) ? "" : strWords);
			StatisticsEngine.trackEvent(this, "wechat_share", strContent);
		} else {
			String strFrom = "";
			if (strPackageName.equalsIgnoreCase("com.sina.weibo")
					|| strPackageName.equalsIgnoreCase("com.weico.sinaweibo")
					|| strPackageName.equalsIgnoreCase("com.sina.weiboapp")
					|| strPackageName.equalsIgnoreCase("com.sina.weibotab")) {
				// Sina Weibo app.
				strFrom = this.getString(R.string.weibo_account);
			} else if ((strPackageName.equalsIgnoreCase("com.tencent.WBlog"))
					|| (strPackageName
							.equalsIgnoreCase("com.tencent.microblog"))) {
				// Tencent weibo app
				strFrom = this.getString(R.string.icson_account);
			} else if (strPackageName.equalsIgnoreCase("com.tencent.mm")) {
				// Tencent Wechat app
				strFrom = this.getString(R.string.icson_account);
			} else {
				strFrom = this.getString(R.string.icson_account);
			}

			final int nMaxName = 30;
			String strLabel = pModel.getName();
			String strName = (strLabel.length() > nMaxName ? strLabel
					.substring(0, nMaxName - 1) : strLabel);

			String strUrl = "http://m.51buy.com/wad-weixin.html?type=proinfo&productid="
					+ pModel.getProductId() + "&channelId=" + channel_id;
			;
			String strPrice = ItemTabDetailView.getDisplayPriceStr(this,pModel);
			strContent = this.getString(R.string.share_content, strFrom,
					strName, strPrice, strUrl);

			StatisticsEngine.trackEvent(this, "share_content", strContent);
		}

		return strContent;
	}

	/*  
	 * Description:
	 * @see com.icson.item.TabDetailSuccLisener#onReviewCountModelSucc(com.icson.lib.model.ReviewCountModel)
	 */
	@Override
	public void onReviewCountModelSucc(ReviewCountModel amodel) {
		RadioButton reviewRBtn = (RadioButton) findViewById(R.id.item_radio_review);
		int aa = amodel.getTotal();
		reviewRBtn.setText(this.getString(R.string.review_with_num,  aa));
		
		
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ItemActivity);
	}
}