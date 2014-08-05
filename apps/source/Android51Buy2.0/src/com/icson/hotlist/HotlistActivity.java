/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: HotPopularActivity.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-7-18
 */
package com.icson.hotlist;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.icson.R;
import com.icson.hotlist.HotlistModel.HotCate;
import com.icson.hotlist.HotlistModel.HotProductModel;
import com.icson.item.ItemActivity;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

/**  
 *   
 * Class Name:HotPopularActivity 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-7-18 下午06:10:49 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class HotlistActivity extends BaseActivity implements OnCheckedChangeListener, OnItemClickListener{
	
	private Ajax mHotAjax;
	private HotlistModel mHotList;
	
	private int lastSelectIndex;
	private static final int tabIDs[] = { R.id.item_radio_0, R.id.item_radio_1,
			R.id.item_radio_2,R.id.item_radio_3,R.id.item_radio_4};
	
	private ViewPager mPager;
	private RadioGroup mRadioGroup;
	
	private ArrayList<HotProductAdapter> mHotProAdapter;
	
	//private ImageView mFirstSight1;
	//private ImageView mFirstSight2;
	//private OnTouchListener mFirstListener;
	private String strPageId;
	private HotlistParser mHotlistParser;
	
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		this.setContentView(R.layout.activity_hotlist);
		
		// Load navigation process.
		this.loadNavBar(R.id.hot_list_navigation_bar);
		
		mHotlistParser = new HotlistParser();
		strPageId = getString(R.string.tag_HotlistActivity);
		mRadioGroup = (RadioGroup) findViewById(R.id.item_radiogroup);
		mPager = (ViewPager) findViewById(R.id.hotlist_pager);
		mPager.setAdapter(new HotPagerAdapter(this)); 
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int index) {
				onCheckedChanged(mRadioGroup, tabIDs[index]);
			}});
		
		mRadioGroup.setOnCheckedChangeListener(this);
		
		mHotProAdapter = new ArrayList<HotProductAdapter>();
		for(int idx = 0; idx < mPager.getChildCount(); idx++)
		{
			HotProductAdapter proAdapter = new HotProductAdapter(this);
			ListView av = (ListView) mPager.getChildAt(idx);
			av.setOnItemClickListener(this);
			av.setAdapter(proAdapter);
			mHotProAdapter.add(proAdapter);
		}
	
		lastSelectIndex = tabIDs[0];
		((RadioButton) findViewById(tabIDs[0])).setChecked(true);
		mPager.setCurrentItem(0);
		
		fetchPopular();
	}
	
	
	/*
	private void checkFirstSight()
	{
		int versionCode = Preference.getInstance().getFirstSightVersion(Preference.FIRST_SIGHT_HOTLIST);
		if(versionCode < IcsonApplication.mVersionCode)
		{
			mFirstSight1 = (ImageView) this.findViewById(R.id.first_sight_head);
			mFirstSight1.setVisibility(View.VISIBLE);
			mFirstSight2 = (ImageView) this.findViewById(R.id.first_sight_tail);
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
	
	protected void goneFirstSight() {
		mFirstSight1.setVisibility(View.GONE);
		mFirstSight2.setVisibility(View.GONE);
		Preference.getInstance().setFirstSightVersion(Preference.FIRST_SIGHT_HOTLIST,IcsonApplication.mVersionCode);
		Preference.getInstance().savePreference();
		mFirstListener = null;
	}
	*/
	/**
	 * 
	* method Name:refleshHotlist    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void refleshHotlist()
	{
		if(null==mHotList)
			return;
		ArrayList<HotCate> cates = mHotList.getHotList();
		if(null == cates)
			return;
		
		int cateIdx = 0;
		for(cateIdx = 0; cateIdx < cates.size(); cateIdx++)
		{
			HotCate cateItem = cates.get(cateIdx);
			RadioButton aChild = (RadioButton) mRadioGroup.getChildAt(cateIdx);
			aChild.setText(cateItem.getCateName());
			
			mHotProAdapter.get(cateIdx).setModelArray(cateItem.getHotCateProducts());
			mHotProAdapter.get(cateIdx).notifyDataSetChanged();
		}
		
		
	}
	
	//////////////////// NET ///////////////////////////////////////////////////////////
	private void fetchPopular()
	{
		mHotAjax = ServiceConfig.getAjax(Config.URL_HOT_PORDUCTS);
		if (null == mHotAjax)
			return;

		showLoadingLayer();
		mHotAjax.setOnSuccessListener(new OnSuccessListener<HotlistModel>(){

			@Override
			public void onSuccess(HotlistModel v, Response response) {
				closeLoadingLayer();
				
				if(!mHotlistParser.isSuccess()) {
					UiUtils.makeToast(HotlistActivity.this, TextUtils.isEmpty(mHotlistParser.getErrMsg()) ? Config.NORMAL_ERROR: mHotlistParser.getErrMsg());
					return;
				}
				//checkFirstSight();
				mHotList = v;
				if(mHotList != null)
					refleshHotlist();
				
			}});
		
		String strRec = RecentCates.getString(5);
		if(!TextUtils.isEmpty(strRec))
			mHotAjax.setData("cates",strRec );
		mHotAjax.setParser(mHotlistParser);
		mHotAjax.setOnErrorListener(this);
		// mActivity.showLoadingLayer(mAjax.getId());
		mHotAjax.send();
		
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (lastSelectIndex != 0 && lastSelectIndex != checkedId) {
			View title = group.findViewById(lastSelectIndex);
			if (title != null) {
				((RadioButton) title).setTextColor(getResources().getColor(
						R.color.global_tab_item));
			}

		}

		View title = group.findViewById(checkedId);
		((RadioButton) title).setTextColor(getResources().getColor(
				R.color.global_tab_item_s));
		
		((RadioButton) group.findViewById(checkedId)).setChecked(true);
		
		switch (checkedId)
		{
		case R.id.item_radio_0:
			mPager.setCurrentItem(0);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02010");
			break;
		case R.id.item_radio_1:
			mPager.setCurrentItem(1);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02011");
			break;
		case R.id.item_radio_2:
			mPager.setCurrentItem(2);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02012");
			break;
		case R.id.item_radio_3:
			mPager.setCurrentItem(3);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02013");
			break;
		case R.id.item_radio_4:
			mPager.setCurrentItem(4);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, this.getClass().getName(), strPageId, "02014");
			break;
		}
		
		lastSelectIndex = checkedId;
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

		ArrayList<HotCate> cates = mHotList.getHotList();
		if(null == cates)
			return;
		
		int aType = 0;
		switch (lastSelectIndex)
		{
		case R.id.item_radio_0:
			aType = 0;
			break;
		case R.id.item_radio_1:
			aType = 1;
			break;
		case R.id.item_radio_2:
			aType = 2;
			break;
		case R.id.item_radio_3:
			aType = 3;
			break;
		case R.id.item_radio_4:
			aType = 4;
			break;
		}

		HotCate cateItem = cates.get(aType);
		ArrayList<HotProductModel> aArray = cateItem.getHotCateProducts();
		final int nSize = (null != aArray ? aArray.size() : 0);
		if( pos >= 0 && pos < nSize ) {
			HotProductModel model = aArray.get(pos);
			
			Bundle param = new Bundle();
			param.putLong(ItemActivity.REQUEST_PRODUCT_ID, model.getProductId());
			UiUtils.startActivity(this, ItemActivity.class, param,true);
			ToolUtil.sendTrack(this.getClass().getName(), strPageId, ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), "03011", String.valueOf(model.getProductId()));
		}
	}
	
	
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_HotlistActivity);
	}

	
}
