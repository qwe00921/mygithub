package com.yy.android.gamenews.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.yy.android.gamenews.ui.ViewPagerFragmentFactory.PageType;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.sportbrush.R;

public class ViewPagerActivity extends BaseActivity {
	// private static final String TAG =
	// ViewPagerActivity.class.getSimpleName();
	private Fragment mFragment;
	public static final String KEY_PAGE_TYPE = "";
	private static final String KEY_FRAGMENT = "fragment";
	public static final String KEY_TITLE = "title";
	private String mTitle;
	private PageType mPageType;
	private ActionBar mActionBar;

	public static void startViewPagerActivity(Context context, PageType type,
			String title, Bundle bundle) {
		Intent intent = new Intent(context, ViewPagerActivity.class);
		intent.putExtra(KEY_PAGE_TYPE, type);
		intent.putExtra(KEY_TITLE, title);
		intent.putExtra(ViewPagerFragmentFactory.KEY_BUNDLE, bundle);
		context.startActivity(intent);
	}

	public static void startViewPagerActivity(Context context, PageType type,
			String title) {
		startViewPagerActivity(context, type, title, null);
	}

	@Override
	protected void onCreate(Bundle data) {
		super.onCreate(data);
		setContentView(R.layout.activity_view_pager);
		Intent intent = getIntent();
		if (intent != null) {
			mPageType = (PageType) intent.getSerializableExtra(KEY_PAGE_TYPE);
			mTitle = intent.getStringExtra(KEY_TITLE);
		}
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mActionBar.setTitle(mTitle);
		mActionBar.setOnLeftClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mFragment = getSupportFragmentManager().findFragmentByTag(KEY_FRAGMENT);
	}

	@Override
	public void onResume() {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		if (mFragment == null) {

			Bundle args = getIntent().getBundleExtra(
					ViewPagerFragmentFactory.KEY_BUNDLE);
			mFragment = ViewPagerFragmentFactory.createViewPagerFragment(
					mPageType, args);
			transaction.add(R.id.container, mFragment, KEY_FRAGMENT);
		}
		transaction.show(mFragment);
		transaction.commitAllowingStateLoss();
		super.onResume();
	}

}