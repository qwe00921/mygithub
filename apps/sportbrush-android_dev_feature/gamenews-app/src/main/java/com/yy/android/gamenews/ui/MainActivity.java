package com.yy.android.gamenews.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.duowan.Comm.ECommAppType;
import com.duowan.android.base.model.BaseModel.ResponseListener;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.gamenews.bean.WelcomeChannel;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.event.FirstButtomTabEvent;
import com.yy.android.gamenews.event.FragmentCallbackEvent;
import com.yy.android.gamenews.event.SchedTabChangedEvent;
import com.yy.android.gamenews.event.SecondButtomTabEvent;
import com.yy.android.gamenews.event.ThirdButtomTabEvent;
import com.yy.android.gamenews.model.InitModel;
import com.yy.android.gamenews.plugin.schetable.SchedFragment;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.gamenews.ui.view.ImagePagerAdapter;
import com.yy.android.gamenews.ui.view.WelcomeChannelView;
import com.yy.android.gamenews.util.AnimationHelper;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.PushUtil;
import com.yy.android.gamenews.util.SignUtil;
import com.yy.android.gamenews.util.SignUtil.OnSignEndListener;
import com.yy.android.gamenews.util.StatsUtil;
import com.yy.android.gamenews.util.TipsHelper;
import com.yy.android.gamenews.util.Util;
import com.yy.android.gamenews.util.maintab.FragmentTabTransaction;
import com.yy.android.gamenews.util.maintab.MainFragmentTab;
import com.yy.android.gamenews.util.maintab.MainTab3SportBrush;
import com.yy.android.gamenews.util.maintab.MainTabFactory;
import com.yy.android.sportbrush.R;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	public static final String TAG_NAME_INFO = "info"; // 头条，第一个tab
	public static final String TAG_NAME_NEWS = "news"; // 频道，第二个tab
	public static final String TAG_NAME_BRUSH = "brush";
	public static final String TAG_NAME_EXTRA1 = "extra1"; // 额外的tab1
	public static final String TAG_NAME_EXTRA2 = "extra2"; // 额外的tab2

	public static final String ACTION_BRUSH_CLICKED = "action_brush_clicked";
	private static final String KEY_CURRENT_TAB = "current_tab";

	private MainFragmentTab mTab1;
	private MainFragmentTab mTab2;
	private MainFragmentTab mTab3;
	private MainFragmentTab mTab4;
	private SignUtil mSignUtil;
	private View mWelcome_main;
	private View mNewsTab;
	private View mBrushTab;
	private View mExtraTab1;
	private View mExtraTab2;
	private ImageView mIndicator;
	private View mInfoTab;

	private Animation mAnimCenterRotate;
	private Animation mAnimRadioIn;
	private Animation mAnimRadioOut;
	// 游戏刷子
	private ImageView imageViewOne;
	private ImageView imageViewTwo;
	private ImageView imageViewThree;
	private ImageView imageView1;
	private ImageView imageView3;

	private View mRadioGroup;
	private ActionBar mActionBar;

	private Preference mPref;
	private boolean mShowWelcomeView;
	private boolean mWelcomeAnimationView;
	private boolean mIsShowWelcomeGuide;

	// private List<ActiveInfo> mActiveChannelList;

	private static final int DURATION_EXIT_APP = 2000; // 在该间隔内按两次返回键退出应用
	// 要隐藏时，layout最少要显示的时间
	private static final int FILT_DURATION = 200; // 在该时间内被发送过来的消息会覆盖之前的消息
	private static final int MSG_SHOW_RADIO = 1001;
	private static final int MSG_HIDE_RADIO = 1002;
	private static final int MSG_FAKE_EXIT_APP = 1003;

	private Handler mHandler = new UIHandler(this);

	private static class UIHandler extends Handler {
		private WeakReference<MainActivity> mRef;

		public UIHandler(MainActivity activity) {
			mRef = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mRef.get();
			if (activity == null) {
				return;
			}
			switch (msg.what) {
			case MSG_SHOW_RADIO: {
				activity.showMainRadioNow();
				break;
			}
			case MSG_HIDE_RADIO: {
				activity.hideMainRadioNow();
				break;
			}
			}
		}
	}

	private String mCurrentTabName = TAG_NAME_INFO;
	private boolean isFirstLaunch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");

		setContentView(R.layout.activity_main);

		handleIntent(getIntent());
		mSignUtil = new SignUtil(this);

		mSignUtil.setOnSignEndListener(new OnSignEndListener() {

			@Override
			public void onEnd() {
				checkHint();
			}
		});
		mPref = Preference.getInstance();
		// 当用户更新到版本1.2.0，显示新手引导
		boolean appUpdated = mPref.isAppUpdated();
		isFirstLaunch = mPref.isFirstLaunch();
		mShowWelcomeView = isFirstLaunch || appUpdated;

		// if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
		// if (isFirstLaunch) {
		// showWelcomeChannelView();
		// }
		// }
		if (mShowWelcomeView) {
			// 初始化
			mPref.setGuideStep(Preference.STEP_0);
			initViewPager();
		} else {
			// checkHint();
		}
		if (mPref.isPushMsgEnabled()) {
			PushUtil.start(getApplicationContext());
		}

		// mImageLoader = SwitchImageLoader.getInstance();
		mRadioGroup = findViewById(R.id.main_radio);

		mAnimRadioOut = AnimationUtils.loadAnimation(this,
				R.anim.main_radio_tans_out);
		mAnimRadioOut.setAnimationListener(mAnimListener);
		mAnimRadioIn = AnimationUtils.loadAnimation(this,
				R.anim.main_radio_tans_in);
		mAnimRadioIn.setAnimationListener(mAnimListener);

		mAnimCenterRotate = AnimationUtils.loadAnimation(this,
				R.anim.main_radio_center_rotation);
		mAnimCenterRotate.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIndicator.setVisibility(View.INVISIBLE);
			}
		});

		mActionBar = (ActionBar) findViewById(R.id.actionbar);

		mCurrentTabName = mPref.getLastTabName();// .getString(KEY_CURRENT_TAB);

		mIndicator = (ImageView) findViewById(R.id.main_radio_center_indicator);

		mNewsTab = findViewById(R.id.news_btn);
		mNewsTab.setOnClickListener(mOnClickListener);

		mExtraTab1 = findViewById(R.id.extra_btn_1);
		mExtraTab1.setOnClickListener(mOnClickListener);
		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)
				|| Constants
						.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)) {
			mExtraTab1.setVisibility(View.VISIBLE);
		}

		mExtraTab2 = findViewById(R.id.extra_btn_2);
		mExtraTab2.setOnClickListener(mOnClickListener);
		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
			mExtraTab2.setVisibility(View.VISIBLE);
		}

		mBrushTab = findViewById(R.id.brush_btn);
		mBrushTab.setOnClickListener(mOnClickListener);

		mInfoTab = findViewById(R.id.info_btn);
		mInfoTab.setOnClickListener(mOnClickListener);

		if (savedInstanceState != null) { // onSaveInstanceState里保存的当前选择的tab
			mTab1 = MainTabFactory.getTab(0, this, mInfoTab, mActionBar,
					savedInstanceState);
			mTab2 = MainTabFactory.getTab(1, this, mNewsTab, mActionBar,
					savedInstanceState);
			mTab3 = MainTabFactory.getTab(2, this, mExtraTab1, mActionBar,
					savedInstanceState);
			mTab4 = MainTabFactory.getTab(3, this, mExtraTab2, mActionBar,
					savedInstanceState);
		}

		if (!mShowWelcomeView) {
			changeTab(mCurrentTabName);
		}
		EventBus.getDefault().register(this);
	}

	public void startWebWithYYToken(String url) {
		AppWebActivity.startWebActivityWithYYToken(this, url);
	}

	public void handleIntent(Intent intent) {
		if (null == intent || intent.getBooleanExtra(ACTION_EXIT_APP, false)) {
			finish();
			return;
		}

		int type = intent.getIntExtra(Constants.PUSH_TYPE, -1);
		long id = intent.getLongExtra(Constants.PUSH_ID, -1);
		String url = intent.getStringExtra(Constants.PUSH_URL);

		if (type == 2) {
			// 网页
			if (url != null) {
				AppWebActivity.startWebActivityFromNotice(this, url);
			}
		} else if (type == 1) {
			// 专题
			if (id != -1) {
				ArticleListActivity.startSpecialListActivity(this, id);
			}
		} else if (type == 0) {
			// 文章
			if (id != -1) {
				ArticleDetailActivity.startArticleDetailActivity(this, id);
			}
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.news_btn: {
				/**
				 * 体育刷子和游戏刷子的频道选择页面在点击频道之后出现
				 */
				if (Constants
						.isFunctionEnabled(ECommAppType._Comm_APP_CARBRUSH)
						|| Constants
								.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)) {
					if (!mPref.isChannelSelected()) {
						showWelcomeChannelView();
						mPref.setChannelSelected(true);
					}
				}
				changeTab(TAG_NAME_NEWS);
				break;
			}
			case R.id.brush_btn: {
				break;
			}
			case R.id.info_btn: {
				changeTab(TAG_NAME_INFO);
				break;
			}

			case R.id.extra_btn_1: {
				changeTab(TAG_NAME_EXTRA1);
				break;
			}
			case R.id.extra_btn_2: {
				changeTab(TAG_NAME_EXTRA2);
				break;
			}
			}
		}
	};

	/**
	 * 统计
	 * 
	 * @param tabName
	 */
	public void addStatistics(String tabName) {
		String lastTabName = mPref.getLastTabName();
		if (lastTabName.equals(TAG_NAME_NEWS) && tabName.equals(TAG_NAME_INFO)) {
			FirstButtomTabEvent event = new FirstButtomTabEvent();
			event.setType(FirstButtomTabEvent._INTO_ORDER_CHANNEL);
			event.setEventId(FirstButtomTabEvent.HEAD_INFO);
			event.setKey(FirstButtomTabEvent.INTO_ORDER_CHANNEL);
			event.setValue(FirstButtomTabEvent.INTO_ORDER_CHANNEL_NAME);
			EventBus.getDefault().post(event);
		} else if (lastTabName.equals(TAG_NAME_NEWS)
				&& tabName.equals(TAG_NAME_EXTRA1)) {
			FirstButtomTabEvent event = new FirstButtomTabEvent();
			event.setType(FirstButtomTabEvent._INTO_SCHETABLE);
			event.setEventId(FirstButtomTabEvent.HEAD_INFO);
			event.setKey(FirstButtomTabEvent.INTO_SCHETABLE);
			event.setValue(FirstButtomTabEvent.INTO_SCHETABLE_NAME);
			EventBus.getDefault().post(event);
		} else if (lastTabName.equals(TAG_NAME_INFO)
				&& tabName.equals(TAG_NAME_NEWS)) {
			SecondButtomTabEvent event = new SecondButtomTabEvent();
			event.setType(SecondButtomTabEvent._INTO_HAND_INFO);
			event.setEventId(SecondButtomTabEvent.ORDER_INFO);
			event.setKey(SecondButtomTabEvent.INTO_HAND_INFO);
			event.setValue(SecondButtomTabEvent.INTO_HAND_INFO_NAME);
			EventBus.getDefault().post(event);
		} else if (lastTabName.equals(TAG_NAME_INFO)
				&& tabName.equals(TAG_NAME_EXTRA1)) {
			SecondButtomTabEvent event = new SecondButtomTabEvent();
			event.setType(SecondButtomTabEvent._INTO_SCHETABLE);
			event.setEventId(SecondButtomTabEvent.ORDER_INFO);
			event.setKey(SecondButtomTabEvent.INTO_SCHETABLE);
			event.setValue(SecondButtomTabEvent.INTO_SCHETABLE_NAME);
			EventBus.getDefault().post(event);
		} else if (lastTabName.equals(TAG_NAME_EXTRA1)
				&& tabName.equals(TAG_NAME_NEWS)) {
			ThirdButtomTabEvent event = new ThirdButtomTabEvent();
			event.setType(ThirdButtomTabEvent._INTO_HAND_INFO);
			event.setEventId(ThirdButtomTabEvent.THIRD_TAB_INFO);
			event.setKey(ThirdButtomTabEvent.INTO_HAND_INFO);
			event.setValue(ThirdButtomTabEvent.INTO_HAND_INFO_NAME);
			EventBus.getDefault().post(event);
		} else if (lastTabName.equals(TAG_NAME_EXTRA1)
				&& tabName.equals(TAG_NAME_INFO)) {
			ThirdButtomTabEvent event = new ThirdButtomTabEvent();
			event.setType(ThirdButtomTabEvent._INTO_ORDER);
			event.setEventId(ThirdButtomTabEvent.THIRD_TAB_INFO);
			event.setKey(ThirdButtomTabEvent.INTO_ORDER);
			event.setValue(ThirdButtomTabEvent.INTO_ORDER_NAME);
			EventBus.getDefault().post(event);
		}
	}

	private void checkDailyLaunch() {
		if (!mShowWelcomeView) {
			mSignUtil.requestSignDaily();
		}
	}

	private void checkBehaviorFromLauncher() {
		if (mFromOtherActivity) {
			mFromOtherActivity = false;
			return;
		}

		checkExpireRefresh();
		checkDailyLaunch();
	}

	private void checkExpireRefresh() {

		if (mTab1 == null || mTab2 == null) {
			return;
		}
		if (mTab1.isVisible()) {
			mTab1.checkExpire();
		}
		if (mTab2.isVisible()) {
			mTab2.checkExpire();
		}

	}

	private int mRequestRefreshCount;

	private void setIndicatorRefreshing(boolean isRefreshing) {

		if (mAnimCenterRotate == null) {
			return;
		}
		if (isRefreshing) {
			mRequestRefreshCount++;
			mIndicator.setVisibility(View.VISIBLE);
			mIndicator
					.setBackgroundResource(R.drawable.btn_main_radio_refreshing);
			mIndicator.startAnimation(mAnimCenterRotate);
		} else {
			mRequestRefreshCount--;
			if (mRequestRefreshCount <= 0) {
				mIndicator.setVisibility(View.INVISIBLE);
				mAnimCenterRotate.cancel();
				mAnimCenterRotate.reset();
				mIndicator.clearAnimation();
			}
		}

	}

	private boolean isAnimating;
	private boolean mIsRadioVisible = true; // 初始化时为显示状态
	private AnimationListener mAnimListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			isAnimating = true;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == mAnimRadioIn) {
				mIsRadioVisible = true;
			} else {
				mIsRadioVisible = false;
			}
			isAnimating = false;
		}
	};

	// private long mLastDisplayTime;

	private void showMainRadio(int delay) {
		Log.d(TAG, "[showMainRadio]");

		if (!mHandler.hasMessages(MSG_SHOW_RADIO)) {
			mHandler.removeMessages(MSG_HIDE_RADIO);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_RADIO, delay);
		}
	}

	private void hideMainRadio(int delay) {
		if (!mHandler.hasMessages(MSG_HIDE_RADIO)) {
			Log.d(TAG, "[hideMainRadio], delay = " + delay);
			mHandler.removeMessages(MSG_SHOW_RADIO);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_RADIO, delay);
		}
	}

	private void showMainRadioNow() {
		if (isAnimating) {
			showMainRadio(10);
			return;
		}
		if (!mIsRadioVisible) {
			mAnimRadioOut.cancel();
			mRadioGroup.startAnimation(mAnimRadioIn);
			mRadioGroup.setClickable(true);
			mInfoTab.setClickable(true);
			mNewsTab.setClickable(true);
			mExtraTab1.setClickable(true);
			mExtraTab2.setClickable(true);
		}
	}

	private void hideMainRadioNow() {
		if (isAnimating) {
			hideMainRadio(10);
			return;
		}
		if (mIsRadioVisible) {
			mAnimRadioIn.cancel();
			mRadioGroup.startAnimation(mAnimRadioOut);
			mRadioGroup.setClickable(false);
			mInfoTab.setClickable(false);
			mNewsTab.setClickable(false);
			mExtraTab1.setClickable(false);
			mExtraTab2.setClickable(false);
		}
	}

	/**
	 * 根据tab名称来做切换动作，显示相应tab并高亮
	 * 
	 * @param tabName
	 */
	private void changeTab(final String tabName) {
		addStatistics(tabName);// 统计
		if (TAG_NAME_NEWS.equals(tabName)) {
			mNewsTab.setSelected(true);
			mInfoTab.setSelected(false);
			mExtraTab1.setSelected(false);
			mExtraTab2.setSelected(false);
		} else if (TAG_NAME_INFO.equals(tabName)) {

			mExtraTab2.setSelected(false);
			mExtraTab1.setSelected(false);
			mNewsTab.setSelected(false);
			mInfoTab.setSelected(true);
		} else if (TAG_NAME_EXTRA1.equals(tabName)) {

			mExtraTab1.setSelected(true);
			mNewsTab.setSelected(false);
			mInfoTab.setSelected(false);
			mExtraTab2.setSelected(false);
		} else if (TAG_NAME_EXTRA2.equals(tabName)) {
			if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
				startWebWithYYToken(Constants.GIFT_URL);
				return;
			} else {
				mExtraTab1.setSelected(false);
				mNewsTab.setSelected(false);
				mInfoTab.setSelected(false);
				mExtraTab2.setSelected(true);
			}
		}

		changeTabFragment(tabName);

		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)
				&& !TAG_NAME_EXTRA1.equals(mCurrentTabName)
				&& TAG_NAME_EXTRA1.equals(tabName)) {
			reloadSchedData();
		}
		ArticleDetailActivity.CURRENT_BUTTON_TAB = tabName;
		mCurrentTabName = tabName;
		mPref.setLastTabName(tabName);
	}

	/**
	 * 根据tab名称来显示对应的fragment，如果fragment为空，会创建并添加
	 * 
	 * @param tabName
	 *            tab的名称
	 */
	private void changeTabFragment(String tabName) {

		FragmentTabTransaction tabTransaction = FragmentTabTransaction
				.beginTransaction(this);
		// 如果为空，则添加
		if (mTab1 == null) {
			mTab1 = MainTabFactory.getTab(0, this, mInfoTab, mActionBar, null);
			tabTransaction.add(mTab1);
		}
		if (mTab2 == null) {
			mTab2 = MainTabFactory.getTab(1, this, mNewsTab, mActionBar, null);
			tabTransaction.add(mTab2);
		}
		if (mTab3 == null) {
			mTab3 = MainTabFactory
					.getTab(2, this, mExtraTab1, mActionBar, null);
			tabTransaction.add(mTab3);
		}
		if (mTab4 == null) {
			mTab4 = MainTabFactory
					.getTab(3, this, mExtraTab2, mActionBar, null);
			tabTransaction.add(mTab4);
		}
		String eventKey = "";
		String param = "";
		if (TAG_NAME_NEWS.equals(tabName)) {
			eventKey = "into_news_tag";
			param = mTab2.getTabName();
			tabTransaction.show(mTab2).hide(mTab1).hide(mTab3).hide(mTab4);
		} else if (TAG_NAME_INFO.equals(tabName)) {
			eventKey = "into_info_tag";
			param = mTab1.getTabName();
			tabTransaction.show(mTab1).hide(mTab2).hide(mTab3).hide(mTab4);
		} else if (TAG_NAME_EXTRA1.equals(tabName)) {
			eventKey = "into_extra1_tag";
			param = mTab3.getTabName();
			tabTransaction.show(mTab3).hide(mTab1).hide(mTab2).hide(mTab4);
		} else {
			eventKey = "into_extra2_tag";
			param = mTab4.getTabName();
			tabTransaction.show(mTab4).hide(mTab1).hide(mTab2).hide(mTab3);
		}
		tabTransaction.commit();
		StatsUtil.statsReport(this, eventKey, "change_tag_name", param);
		StatsUtil.statsReportByHiido(eventKey, param);
		StatsUtil.statsReportByMta(this, eventKey, param);
	}

	@Override
	public void onResume() {

		if (mPref.getInitRsp() == null) {
			InitModel.sendUserInitReq(this, mUserInitRspListener, null, false);
		}

		changeTab(mCurrentTabName);
		checkBehaviorFromLauncher();
		// if (mShowWelcomeView) {
		//
		// setIntercept(mTab1, true);
		// setIntercept(mTab2, true);
		// setIntercept(mTab3, true);
		//
		// } else {
		// setIntercept(mTab1, false);
		// setIntercept(mTab2, false);
		// setIntercept(mTab3, false);
		// }
		super.onResume();
		startGameWelcomeAinamin();
	}

	private void startGameWelcomeAinamin() {
		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)
				&& mShowWelcomeView && mIsShowWelcomeGuide) {
			Animation createAnimRightToLeftIn = AnimationHelper
					.createAnimRightToLeftIn(getBaseContext(),
							new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									imageViewTwo.startAnimation(AnimationHelper
											.createAnimRightToLeftSecondLongIn(
													getBaseContext(), null));
									imageViewThree.startAnimation(AnimationHelper
											.createAnimRightToLeftLongIn(
													getBaseContext(), null));
									imageViewTwo.setVisibility(View.VISIBLE);
									imageViewThree.setVisibility(View.VISIBLE);
								}
							});
			imageViewOne.startAnimation(createAnimRightToLeftIn);
			imageViewOne.setVisibility(View.VISIBLE);

		}

	}

	public void setTitleContainerWidget(View view) {
		// if (mTab3 != null && mTab3.getFragment() instanceof SchedFragment) {
		// mTab3.setTitleContainerWidget(view);
		// }
	}

	/**
	 * 体育刷子切换到赛事tab，重新请求赛事和球队数据
	 * 
	 * @author yuelai.ye
	 */
	public void reloadSchedData() {
		if (mTab3 != null && mTab3.getFragment() instanceof SchedFragment) {
			((SchedFragment) mTab3.getFragment()).reloadSchedData();
		}
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	/*
	 * 判断是否有启动过其它的activity, 此flag用于判断是否要检查列表数据过期
	 * 现在的逻辑是，只有当activity处于列表页时，如果列表内容上次加载距当前时间 超过一定时间，则在下次onResume时自动刷新
	 */
	private boolean mFromOtherActivity; //

	@Override
	public void startActivityForResult(Intent intent, int requestCode,
			Bundle options) {
		mFromOtherActivity = true;
		super.startActivityForResult(intent, requestCode, options);
	}

	private ResponseListener<UserInitRsp> mUserInitRspListener = new ResponseListener<UserInitRsp>(
			this) {
		public void onResponse(UserInitRsp rsp) {
			mPref.saveDefaultInitRsp(rsp);
		};

		public void onError(Exception e) {
		};
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_CURRENT_TAB, mCurrentTabName);
		super.onSaveInstanceState(outState);
	}

	public static final String ACTION_EXIT_APP = "exit_app";

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	public void onBackPressed() {
		if (mHandler.hasMessages(MSG_FAKE_EXIT_APP)) {
			super.onBackPressed();
		} else {
			Toast.makeText(this, R.string.main_exit_app, DURATION_EXIT_APP)
					.show();
			mHandler.sendEmptyMessageDelayed(MSG_FAKE_EXIT_APP,
					DURATION_EXIT_APP);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// ToastUtil.showToast("onMenuOpened");
		return false; // 返回为true则显示系统menu
	}

	public static void exitApp(Activity from) {
		Intent intent = new Intent(from, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(ACTION_EXIT_APP, true);
		from.startActivityForResult(intent, -1);
	}

	public void onEvent(SchedTabChangedEvent event) {
		if (mTab3 != null) {
			int visibility = event.getVisibility();
			((MainTab3SportBrush) mTab3)
					.setRightImageViewVisibility(visibility);
		}
	}

	public void onEvent(FragmentCallbackEvent event) {
		if (event == null
				|| (event.mTarget != MainActivity.this && event.mTarget != null)) {
			Log.d(TAG, "[onEvent]" + ", event = " + event.getEventType()
					+ ", activity = " + event.mTarget);
			return;
		}
		if (event.getEventType() == 1005 || event.getEventType() == 1006) {

			Log.d(TAG, "[onEvent]" + ", eventId = " + event.getEventType()
					+ ", activity = " + event.mTarget);

		}

		int eventType = event.mEventType;
		switch (eventType) {
		case FragmentCallbackEvent.FRGMT_LIST_SCROLL_DOWN: {
			// Log.d(TAG, "[onEvent]" + ", eventId = FRGMT_LIST_SCROLL_DOWN");
			// showMainRadio(FILT_DURATION);
			break;
		}
		case FragmentCallbackEvent.FRGMT_LIST_SCROLL_UP: {
			// Log.d(TAG, "[onEvent]" + ", eventId = FRGMT_LIST_SCROLL_UP");
			// hideMainRadio(FILT_DURATION);
			break;
		}
		case FragmentCallbackEvent.FRGMT_LIST_SCROLL_TO_HEAD: {
			// Log.d(TAG, "[onEvent]" +
			// ", eventId = FRGMT_LIST_SCROLL_TO_HEAD");
			// showMainRadio(0);
			break;
		}
		case FragmentCallbackEvent.FRGMT_LIST_SCROLL_END: {
			// Log.d(TAG, "[onEvent]" + ", eventId = FRGMT_LIST_SCROLL_END");
			// hideMainRadio(DURATION_RADIO_SHOW_MIN);
			break;
		}
		case FragmentCallbackEvent.FRGMT_LIST_REFRESHING: {

			setIndicatorRefreshing(true);
			break;
		}

		case FragmentCallbackEvent.FRGMT_LIST_REFRESH_DONE: {
			setIndicatorRefreshing(false);
			break;
		}
		case FragmentCallbackEvent.FRGMT_TAB_CHANGED: {
			// showMainRadio(0);
			break;
		}
		}
	}

	public void onEvent(FirstButtomTabEvent event) {
		if (event == null) {
			return;
		}
		StatsUtil.statsReportAllData(this, event.getEventId(), event.getKey(),
				event.getValue());
	}

	public void onEvent(SecondButtomTabEvent event) {
		if (event == null) {
			return;
		}
		StatsUtil.statsReportAllData(this, event.getEventId(), event.getKey(),
				event.getValue());
	}

	public void onEvent(ThirdButtomTabEvent event) {
		if (event == null) {
			return;
		}
		StatsUtil.statsReportAllData(this, event.getEventId(), event.getKey(),
				event.getValue());
	}

	private void setIntercept(MainFragmentTab tab, boolean intercept) {
		if (tab != null) {
			tab.setIntercept(intercept);
		}
	}

	// private WelcomeView mWelcomeView;
	private ViewPager mViewPager;
	private ImagePagerAdapter mAdapter;

	private void initViewPager() {
		mWelcome_main = findViewById(R.id.welcome_pager_main);
		mViewPager = (ViewPager) findViewById(R.id.welcome_pager);
		mAdapter = new ImagePagerAdapter(this);
		mWelcome_main.setVisibility(View.VISIBLE);
		mViewPager.setVisibility(View.VISIBLE);

		final List<View> resList = new ArrayList<View>();

		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
			addGameWelcomeAnimation(mViewPager, resList);// 游戏刷子欢迎页
		} else {
			View layout1 = getLayoutInflater().inflate(
					R.layout.welcome_pager_layout, null);
			ImageView view1 = (ImageView) layout1
					.findViewById(R.id.welcome_pager_img);
			view1.setBackgroundResource(R.drawable.welcome_1);
			resList.add(layout1);

			View layout2 = getLayoutInflater().inflate(
					R.layout.welcome_pager_layout, null);
			ImageView view2 = (ImageView) layout2
					.findViewById(R.id.welcome_pager_img);
			view2.setBackgroundResource(R.drawable.welcome_2);
			resList.add(layout2);
		}
		/**
		 * 汽车刷子有4个欢迎页面
		 */
		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_CARBRUSH)
				|| Constants
						.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)) {
			View layout3 = getLayoutInflater().inflate(
					R.layout.welcome_pager_layout, null);
			ImageView view3 = (ImageView) layout3
					.findViewById(R.id.welcome_pager_img);
			view3.setBackgroundResource(R.drawable.welcome_3);
			resList.add(layout3);
			View layout4 = getLayoutInflater().inflate(
					R.layout.welcome_pager_layout, null);
			ImageView view4 = (ImageView) layout4
					.findViewById(R.id.welcome_pager_img);
			view4.setBackgroundResource(R.drawable.welcome_4);
			resList.add(layout4);
		}

		// mWelcomeView = new WelcomeView(this);
		// mWelcomeView.setOnCompletedListener(new OnCompletedListener() {
		// @Override
		// public void onCompleted() {
		// mPref.finishFirstLaunch();
		// mPref.finishAppUpdate();
		// // checkAndShowTips();
		// if (mWelcomeChannelView != null) {
		// mWelcomeChannelView.checkHint();
		// }
		// mViewPager.setVisibility(View.GONE);
		// }
		// });
		resList.add(new View(this));
		mAdapter.updateDatasource(resList);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (Constants
						.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
					if (position == 1 && !mWelcomeAnimationView) {
						Animation createAnimRightToLeftIn = AnimationHelper
								.createAnimRightToLeftIn(getBaseContext(),
										new AnimationListener() {

											@Override
											public void onAnimationStart(
													Animation animation) {
											}

											@Override
											public void onAnimationRepeat(
													Animation animation) {

											}

											@Override
											public void onAnimationEnd(
													Animation animation) {
												imageView3
														.startAnimation(AnimationHelper
																.createAnimRightToLeftSecondLongIn(
																		getBaseContext(),
																		null));

												imageView3
														.setVisibility(View.VISIBLE);
											}
										});
						imageView1.startAnimation(createAnimRightToLeftIn);
						imageView1.setVisibility(View.VISIBLE);
						mWelcomeAnimationView = true;
					} else if (position == 2) {
						if (isFirstLaunch) {
							showWelcomeChannelView();
						}
						if (imageViewOne != null
								&& imageViewOne.getDrawingCache() != null) {
							imageViewOne.getDrawingCache().recycle();
						}
						if (imageViewTwo != null
								&& imageViewTwo.getDrawingCache() != null) {
							imageViewTwo.getDrawingCache().recycle();
						}
						if (imageViewThree != null
								&& imageViewThree.getDrawingCache() != null) {
							imageViewThree.getDrawingCache().recycle();
						}
						if (imageView1 != null
								&& imageView1.getDrawingCache() != null) {
							imageView1.getDrawingCache().recycle();
						}
						if (imageView3 != null
								&& imageView3.getDrawingCache() != null) {
							imageView3.getDrawingCache().recycle();
						}
					}
				}

				if (position == resList.size() - 1) {
					mViewPager.postDelayed(new Runnable() {
						@Override
						public void run() {
							mPref.finishFirstLaunch();
							mPref.finishAppUpdate();
							// checkAndShowTips();
							if (mWelcomeChannelView != null) {
								mWelcomeChannelView.checkHint();
							} else {
								changeTab(mCurrentTabName);
								// mFloatEvent.setVisibility(View.VISIBLE);
								mShowWelcomeView = false;
								// setIntercept(mTab1, false);
								// setIntercept(mTab2, false);
								// setIntercept(mTab3, false);
								// checkHint();
							}
							mWelcome_main.setVisibility(View.GONE);
							mViewPager.setVisibility(View.GONE);
							mViewPager.setAdapter(null);
							mAdapter.updateDatasource(null);
							mViewPager = null;
							mAdapter = null;
							resList.clear();
						}
					}, 200);
				}
			}
		});
	}

	private WelcomeChannelView mWelcomeChannelView;

	private void showWelcomeChannelView() {
		mWelcomeChannelView = (WelcomeChannelView) findViewById(R.id.welcome_channel_view);

		List<WelcomeChannel> welcomeChannelList = Util.getWelcomeChannelList();
		mWelcomeChannelView.setChannelList(welcomeChannelList);
		mWelcomeChannelView.setVisibility(View.VISIBLE);
		mWelcomeChannelView.setOnSaveClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeTab(mCurrentTabName);
				mSignUtil.requestSignDaily();
				mWelcomeChannelView.setVisibility(View.GONE);

				if (mTab2 != null) {
					mTab2.refresh();
				}
				mShowWelcomeView = false;
				// setIntercept(mTab1, false);
				// setIntercept(mTab2, false);
				// setIntercept(mTab3, false);
				// checkHint();
			}
		});
	}

	/**
	 * 游戏刷子欢迎页Viewpager动画
	 */
	public void addGameWelcomeAnimation(ViewPager mViewPager, List<View> resList) {
		mWelcome_main
				.setBackgroundResource(R.color.main_welcome_guide_bg_color);
		findViewById(R.id.welcome_image_one).setVisibility(View.VISIBLE);
		findViewById(R.id.welcome_image_two).setVisibility(View.VISIBLE);
		View layoutOne = getLayoutInflater().inflate(
				R.layout.welcome_pager_gamenews_layout, null);
		imageViewOne = (ImageView) layoutOne
				.findViewById(R.id.welcome_pager_img_one);
		imageViewTwo = (ImageView) layoutOne
				.findViewById(R.id.welcome_pager_img_two);
		imageViewThree = (ImageView) layoutOne
				.findViewById(R.id.welcome_pager_img_three);
		imageViewOne
				.setImageResource(R.drawable.game_news_welcome_one_pager_one);
		imageViewTwo
				.setImageResource(R.drawable.game_news_welcome_one_pager_two);
		imageViewThree
				.setImageResource(R.drawable.game_news_welcome_one_pager_three);
		imageViewOne.setVisibility(View.GONE);
		imageViewTwo.setVisibility(View.GONE);
		imageViewThree.setVisibility(View.GONE);
		resList.add(layoutOne);
		View layoutTwo = getLayoutInflater().inflate(
				R.layout.welcome_pager_gamenews_layout, null);
		imageView1 = (ImageView) layoutTwo
				.findViewById(R.id.welcome_pager_img_one);
		ImageView imageView2 = (ImageView) layoutTwo
				.findViewById(R.id.welcome_pager_img_two);
		imageView3 = (ImageView) layoutTwo
				.findViewById(R.id.welcome_pager_img_three);
		imageView1.setImageResource(R.drawable.game_news_welcome_two_pager_one);
		imageView3.setImageResource(R.drawable.game_news_welcome_two_pager_two);
		imageView1.setVisibility(View.GONE);
		imageView2.setVisibility(View.GONE);
		imageView3.setVisibility(View.GONE);
		resList.add(layoutTwo);
		mIsShowWelcomeGuide = true;// 展示欢迎引导页
	}

	private TextView mHintView;
	private View mHintLayout;
	private TipsHelper mTipsHelper;

	public void checkHint() {

		if (mPref.getCurrentGuideStep() == Preference.STEP_0) {
			Util.showHelpTips(MainActivity.this, mExtraTab2, null);
		}
		// if (mTipsHelper == null) {
		// mHintView = (TextView) findViewById(R.id.welcome_main_hint);
		// mHintLayout = findViewById(R.id.welcome_hint_layout);
		// mTipsHelper = new TipsHelper(this, mHintLayout, mHintView);
		// }
		// int step = Preference.getInstance().getCurrentGuideStep();
		// if (step < Preference.STEP_2) { // 如果用户在前两步时退出，则从第三步开始
		// step = Preference.STEP_2;
		// }
		// if (Preference.STEP_2 == step) {
		// mTipsHelper.checkHint(step, true);
		// }
	}

	// @Override
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	// boolean ignored = mTab1 == null ? true : mTab1.dispatchTouchEvent(ev,
	// FirstButtomTabEvent.HEAD_INFO);
	// if (ignored) {
	// ignored = mTab2 == null ? true : mTab2.dispatchTouchEvent(ev,
	// SecondButtomTabEvent.ORDER_INFO);
	// if (ignored) {
	// ignored = mTab3 == null ? true : mTab3.dispatchTouchEvent(ev,
	// ThirdButtomTabEvent.THIRD_TAB_INFO);
	// }
	// }
	//
	// if (ignored) {
	// return super.dispatchTouchEvent(ev);
	// } else {
	// return false;
	// }
	// }

}
