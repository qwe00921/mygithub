package com.icson.yiqiang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.icson.R;
import com.icson.event.TimeBuyModel;
import com.icson.home.ModuleInfo;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class YiQiangActivity extends BaseActivity implements
		OnCheckedChangeListener, OnClickListener, SubviewNetSuccessListener {

	private RadioGroup mRadioGroup;
	public static final String PARAM_TAB = "param_tab";
	public static final int PARAM_TAB_QIANG = 1;
	public static final int PARAM_TAB_TIMEBUY = 2;
	public static final int PARAM_TAB_TUAN = 3;

	private int lastSelectIndex;
	private ViewPager mViewPager;
	int tabIDs[] = { R.id.item_radio_qianggou, R.id.item_radio_zaowanshi, R.id.item_radio_tuangou };

	private RadioButton  mTimebuy;
	private QiangGouView mQiangGouView;
	private ZaoWanShiView mZaoWanShiView;
	private TuanGouView mTuanGouView;
	private int mTimebuyType;
	private int mType;
	
	//private ImageView    mFirstSight1;
	//private RelativeLayout    mFirstSight2;
	//private OnTouchListener   mFirstListener;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_yiqiang);
		this.loadNavBar(R.id.yiqiang_navbar);

		mRadioGroup = (RadioGroup) findViewById(R.id.item_radiogroup);
		mTimebuy = (RadioButton) findViewById(R.id.item_radio_zaowanshi);
		
		mViewPager = (ViewPager) findViewById(R.id.item_relative_tab_content);
		mViewPager.setAdapter(new ViewPagerAdapter(this));

		mRadioGroup.setOnCheckedChangeListener(this);
		
		Intent pIntent = this.getIntent();
		mType = pIntent.getIntExtra(PARAM_TAB, PARAM_TAB_QIANG);
		init(mType);

		mTimebuyType = pIntent.getIntExtra(TimeBuyModel.TIMEBUY_TYPE, ModuleInfo.MODULE_ID_MORNING);
		setTimebuyName(mTimebuyType);
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
	}
	
	private void setTimebuyName(int nType) {
		final int nResId = TimeBuyModel.getName(nType);
		if( nResId > 0 ) {
			mTimebuy.setText(getString(nResId));
		}
	}

	public void init(int type) {

		if (mQiangGouView != null) {
			mQiangGouView.clean();
		}

		if (mZaoWanShiView != null) {
			mZaoWanShiView.clean();
		}

		if (mTuanGouView != null) {
			mTuanGouView.clean();
		}

		switch (type) {
		case PARAM_TAB_QIANG:
			lastSelectIndex = R.id.item_radio_qianggou;
			break;
		case PARAM_TAB_TIMEBUY:
			lastSelectIndex = R.id.item_radio_zaowanshi;
			break;
		case PARAM_TAB_TUAN:
			lastSelectIndex = R.id.item_radio_tuangou;
			break;

		}
		((RadioButton) findViewById(lastSelectIndex)).setChecked(true);
	}
	
	@Override
	public void onError(final Ajax ajax, final Response response) {
		if( null != mTuanGouView ) {
			mTuanGouView.postRequest();
		}
		
		super.onError(ajax, response, null);
	}


	@Override
	public void onDestroy() {
		if (mQiangGouView != null) {
			mQiangGouView.destroy();
			mQiangGouView = null;
		}

		if (mZaoWanShiView != null) {
			mZaoWanShiView.destroy();
			mZaoWanShiView = null;
		}

		if (mTuanGouView != null) {
			mTuanGouView.destroy();
			mTuanGouView = null;
		}
		
		super.onDestroy();

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		boolean changeFlag = false;
		if (lastSelectIndex != 0 && lastSelectIndex != checkedId) {
			changeFlag = true;
			View title = group.findViewById(lastSelectIndex);
			if (title != null) {
				((RadioButton) title).setTextColor(getResources().getColor(R.color.global_tab_item));
			}

		}

		View title = group.findViewById(checkedId);
		((RadioButton) title).setTextColor(getResources().getColor(R.color.global_tab_item_s));
		//PUT HERE TO AVOID trackEvent 2TIMES
		int tempLast = lastSelectIndex;
		
		lastSelectIndex = checkedId;
		
		((RadioButton) group.findViewById(checkedId)).setChecked(true);
		
		String strPageId = getString(R.string.tag_YiQiangActivity);
		
		switch (checkedId) {
		case R.id.item_radio_qianggou:
			//findViewById(R.id.slidingdrawer).setVisibility(View.GONE);

			if (mQiangGouView == null) {
				mQiangGouView = new QiangGouView(this);
				mQiangGouView.setListener(this);
			}
			mQiangGouView.init();
			if(changeFlag){
				StatisticsEngine.trackEvent(YiQiangActivity.this, "click_qiang");
				ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), getString(R.string.tag_QiangActivity), "02011");
				String tempLastPageId = getActivityPageId();
				if(tempLast == R.id.item_radio_zaowanshi)
				{
					tempLastPageId = TimeBuyModel.getPageId(mTimebuyType) > 0 ? getString(TimeBuyModel.getPageId(mTimebuyType)) : getString(R.string.tag_EventMorningActivity);
				}
				else if(tempLast == R.id.item_radio_tuangou)
				{
					tempLastPageId = getString(R.string.tag_TuanActivity);
				}
				ToolUtil.reportStatisticsClick(tempLastPageId, "21001");
				ToolUtil.reportStatisticsPV(getString(R.string.tag_QiangActivity));
				
			}
			mType = PARAM_TAB_QIANG;
			mViewPager.setCurrentItem(0);
			break;
		case R.id.item_radio_zaowanshi:
			//findViewById(R.id.slidingdrawer).setVisibility(View.GONE);

			if (mZaoWanShiView == null) {
				mZaoWanShiView = new ZaoWanShiView(this, mTimebuyType);
				mZaoWanShiView.setListener(this);
			}
			mZaoWanShiView.init();
			if(changeFlag){
				String strCurrentPageId = TimeBuyModel.getPageId(mTimebuyType) > 0 ? getString(TimeBuyModel.getPageId(mTimebuyType)) : getString(R.string.tag_EventMorningActivity);
				StatisticsEngine.trackEvent(YiQiangActivity.this, "time_buy");
				ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strCurrentPageId, "02012");
				
				String tempLastPageId = getActivityPageId();
				if(tempLast == R.id.item_radio_qianggou)
				{
					tempLastPageId = getString(R.string.tag_QiangActivity);
				}
				else if(tempLast == R.id.item_radio_tuangou)
				{
					tempLastPageId = getString(R.string.tag_TuanActivity);
				}
				ToolUtil.reportStatisticsClick(tempLastPageId, "21003");
				ToolUtil.reportStatisticsPV(strCurrentPageId);
			}
			
			mType = PARAM_TAB_TIMEBUY;
			mViewPager.setCurrentItem(1);
			break;
		case R.id.item_radio_tuangou:
			//findViewById(R.id.slidingdrawer).setVisibility(View.VISIBLE);

			if (mTuanGouView == null) {
				mTuanGouView = new TuanGouView(this);
				mTuanGouView.setListener(this);
			}
			mTuanGouView.init();
			if(changeFlag){
				StatisticsEngine.trackEvent(YiQiangActivity.this, "click_tuan");
				ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), getString(R.string.tag_TuanActivity), "02013");
				
				String tempLastPageId = getActivityPageId();
				if(tempLast == R.id.item_radio_zaowanshi)
				{
					tempLastPageId = TimeBuyModel.getPageId(mTimebuyType) > 0 ? getString(TimeBuyModel.getPageId(mTimebuyType)) : getString(R.string.tag_EventMorningActivity);
				}
				else if(tempLast == R.id.item_radio_qianggou)
				{
					tempLastPageId = getString(R.string.tag_QiangActivity);
				}
				ToolUtil.reportStatisticsClick(tempLastPageId, "21002");
				ToolUtil.reportStatisticsPV(getString(R.string.tag_TuanActivity));
			}
			
			mType = PARAM_TAB_TUAN;
			mViewPager.setCurrentItem(2);
			break;
		}
	}
	
	/*
	private void checkFirstSight()
	{
		int versionCode = Preference.getInstance().getFirstSightVersion(Preference.FIRST_SIGHT_QIANG);
		if(versionCode < IcsonApplication.mVersionCode)
		{
			mFirstSight1 = (ImageView) this.findViewById(R.id.first_sight_head);
			mFirstSight1.setVisibility(View.VISIBLE);
			mFirstSight2 = (RelativeLayout) this.findViewById(R.id.first_sight_tail);
			mFirstSight2.setVisibility(View.VISIBLE);
			
			mFirstListener = new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(mFirstSight1.getVisibility() == View.VISIBLE &&
							event.getAction() == MotionEvent.ACTION_DOWN )
					{
						goneFirstSight();
						
						return true;
					}
					return false;
				}};
				
			mFirstSight1.setOnTouchListener(mFirstListener);
			mFirstSight2.setOnTouchListener(mFirstListener);
			
			if(mFirstSight1.getVisibility() == View.VISIBLE)
			{
				Handler FadingHandler = new Handler();
				FadingHandler.postDelayed(new Runnable(){

					@Override
					public void run() {
						goneFirstSight();
						
					}}, Config.FIRST_SIGHT_FADING_TIME);
			}
		}
	}
	
	
	private void goneFirstSight()
	{
		mFirstSight1.setVisibility(View.GONE);
		mFirstSight2.setVisibility(View.GONE);
		Preference.getInstance().setFirstSightVersion(Preference.FIRST_SIGHT_QIANG,IcsonApplication.mVersionCode);
		Preference.getInstance().savePreference();
		mFirstListener = null;
	}
	
	 */
	/*  
	 * Description:
	 * @see com.icson.yiqiang.SubviewNetSuccessListener#onSubviewFinished()
	 */
	@Override
	public void onSubviewFinished(int nTabIndex, int nParam) {
		//checkFirstSight();
		
		if( PARAM_TAB_TIMEBUY == nTabIndex ) {
			this.setTimebuyName(nParam);
		}
	}
	
	@Override
	public String getActivityPageId() {
		String pageId = "";
		switch (mType) {
		case PARAM_TAB_QIANG:
			pageId = getString(R.string.tag_QiangActivity);
			break;
		case PARAM_TAB_TIMEBUY:
			if(mTimebuyType == R.string.time_buy_morning) {
				pageId = getString(R.string.tag_EventMorningActivity);
			}else{
				pageId = getString(R.string.tag_EventThhActivity);
			}
			break;
		case PARAM_TAB_TUAN:
			pageId = getString(R.string.tag_TuanActivity);
			break;

		}
		return pageId;
	}
}
