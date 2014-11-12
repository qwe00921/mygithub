package com.yy.android.gamenews.util.maintab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.duowan.gamenews.UserInitRsp;
import com.yy.android.gamenews.event.FirstButtomTabEvent;
import com.yy.android.gamenews.event.SecondButtomTabEvent;
import com.yy.android.gamenews.event.ThirdButtomTabEvent;
import com.yy.android.gamenews.ui.ChannelArticleInfoFragment;
import com.yy.android.gamenews.ui.MainActivity;
import com.yy.android.gamenews.ui.MyHomeActivity;
import com.yy.android.gamenews.ui.NewsFragment;
import com.yy.android.gamenews.ui.common.SwitchImageLoader;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.gamenews.ui.view.DispatchTouchEvent;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

import de.greenrobot.event.EventBus;

public abstract class MainFragmentTab {

	// public static final String HEAD_INFO = "head_info";
	// public static final String ORDER_INFO = "order_info";
	// public static final String THIRD_INFO = "third_info";
	protected MainActivity mContext;
	protected Fragment mFragment;
	protected ActionBar mActionBar;
	protected Preference mPref;
	protected DispatchTouchEvent mDispatchTouchEvent;
	private SwitchImageLoader mImageLoader;
	private boolean mIsVisible;
	private String mFromTab;

	public MainFragmentTab(MainActivity context, View button,
			ActionBar actionbar, String fromTab, Bundle savedInstance) {

		mContext = context;
		mActionBar = actionbar;
		mFromTab = fromTab;
		mPref = Preference.getInstance();
		mImageLoader = SwitchImageLoader.getInstance();

		if (savedInstance != null) { // onSaveInstanceState里保存的当前选择的tab
			mFragment = mContext.getSupportFragmentManager().findFragmentByTag(
					getTabName());
		}

		if (mFragment == null) {
			mFragment = initFragment();
		}

		mDispatchTouchEvent = new DispatchTouchEvent(mContext,
				Util.getDisplayAttribute(mContext));

		mDispatchTouchEvent.setNewsFragment(mFragment);

	}

	public void add(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.add(R.id.container, mFragment, getTabName());
	}

	public void show(FragmentTabTransaction t) {

		mActionBar.setOnLeftClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MyHomeActivity.startMyHomeActivity(mContext);
				if (mFromTab.equals(FirstButtomTabEvent.HEAD_INFO)) {
					FirstButtomTabEvent event = new FirstButtomTabEvent();
					event.setType(FirstButtomTabEvent._INTO_MY_HOME_CENTER);
					event.setEventId(FirstButtomTabEvent.HEAD_INFO);
					event.setKey(FirstButtomTabEvent.INTO_MY_HOME_CENTER);
					event.setValue(FirstButtomTabEvent.INTO_MY_HOME_CENTER_NAME);
					EventBus.getDefault().post(event);
				} else if (mFromTab.equals(SecondButtomTabEvent.ORDER_INFO)) {
					SecondButtomTabEvent event = new SecondButtomTabEvent();
					event.setType(SecondButtomTabEvent._INTO_MY_HOME_CENTER);
					event.setEventId(SecondButtomTabEvent.ORDER_INFO);
					event.setKey(SecondButtomTabEvent.INTO_MY_HOME_CENTER);
					event.setValue(SecondButtomTabEvent.INTO_MY_HOME_CENTER_NAME);
					EventBus.getDefault().post(event);
				} else if (mFromTab.equals(ThirdButtomTabEvent.THIRD_TAB_INFO)) {
					ThirdButtomTabEvent event = new ThirdButtomTabEvent();
					event.setType(ThirdButtomTabEvent._INTO_MY_HOME_CENTER);
					event.setEventId(ThirdButtomTabEvent.THIRD_TAB_INFO);
					event.setKey(ThirdButtomTabEvent.INTO_MY_HOME_CENTER);
					event.setValue(ThirdButtomTabEvent.INTO_MY_HOME_CENTER_NAME);
					EventBus.getDefault().post(event);
				}

			}
		});

		mIsVisible = true;

		mDispatchTouchEvent.setTitleContainerWidget(getTitleContainer());

		preCustActionbar();
		customizeActionbar();
		FragmentTransaction transaction = t.getTransaction();
		transaction.show(mFragment);
	}

	public void setTitleContainerWidget(View view) {
		mDispatchTouchEvent.setTitleContainerWidget(view);
	}

	public void hide(FragmentTabTransaction t) {
		mIsVisible = false;
		FragmentTransaction transaction = t.getTransaction();
		transaction.hide(mFragment);
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	protected void preCustActionbar() {
		if (mPref.isUserLogin()) {
			UserInitRsp user = mPref.getInitRsp();
			if (user != null && user.getUser().getIcon() != null) {
				mActionBar.showLeftImgBorder(true);
				mImageLoader.displayImage(user.getUser().getIcon(),
						mActionBar.getLeftImageView(), true);
			} else {
				mActionBar.showLeftImgBorder(false);
				mActionBar.setLeftImageResource(R.drawable.ic_person_default);
			}
		} else {
			mActionBar.showLeftImgBorder(false);
			mActionBar.setLeftImageResource(R.drawable.ic_person_default);
		}

		mActionBar.setTitle(getTabName());
	}

	public Fragment getFragment() {
		return mFragment;
	}

	public void checkExpire() {
		if (mFragment == null) {
			return;
		}
		if (mFragment instanceof ChannelArticleInfoFragment) {

			((ChannelArticleInfoFragment) mFragment).checkExpire();
		} else if (mFragment instanceof NewsFragment) {

			((NewsFragment) mFragment).checkExpireCurrent();
		}
	}

	public void refresh() {

	}

	public void setIntercept(boolean intercept) {
		mDispatchTouchEvent.setIntercept(intercept);
	}

	public boolean dispatchTouchEvent(MotionEvent e, String fromTab) {
//		return mDispatchTouchEvent.dispatchTouchEvent(e, fromTab);  //屏蔽滑出个人中心
		return true;
	}

	protected View getTitleContainer() {

		if (mFragment instanceof NewsFragment) {
			return ((NewsFragment) mFragment).mTitleContainer;
		}
		return null;
	}

	protected abstract void customizeActionbar();

	protected abstract Fragment initFragment();

	public abstract String getTabName();
}
