/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: SlotMachineActivity.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-6-3
 */
package com.icson.slotmachine;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.login.LoginActivity;
import com.icson.login.ReloginWatcher;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

/**
 * 
*   
* Class Name:CouponCollectionActivity 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-4 下午02:29:23 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class CouponCollectionActivity extends BaseActivity {

	private ImageView switchBtn;
	private ArrayList<BingoInfo> mCoupons;
	
	private CouponCollectAdapter mCouponAdapter;
	private ListView mListView;
	private TextView mNoPrizeView;
	private TextView  mNoPrizeTextView; 
	//private ItemGallery mGallery;
	//private ViewGroup bulletContainer;
	//private int lastBulletIndex;
	
	private Ajax mAjax;
	private ClipboardManager mCM;
	
	private int mCurrentPageId;
	private int mNextPageId;
	private static final int EACH_PAGE_SIZE = 5;
	private int  mTotalNum;
	private int mTotalPageNum;
	private boolean mRequesting;
	private String  mLoginHint;
	private SlotLoginDialog pSlotLoginDialog;
	private SlotSorryDialog pSlotSorryDialog;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.activity_list_coupon_collection,false);
		
		mLoginHint = getIntent().getStringExtra("hint");
		mCM =(ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
		
		mNextPageId = mCurrentPageId = 0;
		fetchCoupons(mCurrentPageId);
		
		switchBtn = (ImageView)this.findViewById(R.id.switch_off);
		switchBtn.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					finish();
				}
				return true;
			}});
		
		
		// 幻灯片
		//mGallery  = (ItemGallery) this.findViewById(R.id.item_gallery);
		// 点
		//bulletContainer = (ViewGroup)this.findViewById(R.id.item_linear_gallery_bullet);
		mNoPrizeView = (TextView) this.findViewById(R.id.no_prize_img);
		mNoPrizeTextView = (TextView) this.findViewById(R.id.no_prize_hint);
		
		mListView = (ListView) this.findViewById(R.id.coupon_item_list);
		final View mHeaderView = getLayoutInflater().inflate(R.layout.empty_header, null);
		mListView.addHeaderView(mHeaderView);
		mListView.setSelector(R.drawable.transparent_listclick);
		mListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState)
				{
				case OnScrollListener.SCROLL_STATE_IDLE:
					// last position
					if (view.getLastVisiblePosition() == (view.getCount() - 1))
					{
						if (mRequesting)
						{
							return;
						}

						if (mCurrentPageId < mTotalPageNum)
						{
							mNextPageId = mCurrentPageId+1;
							fetchCoupons(mNextPageId);
						}
						else
						{
							
						}
					}
					break;
				default:
					break;
				}
				
			}});
		
				
	}
	
	@Override
	protected void onPause()
	{
		if(null!=pSlotSorryDialog)
			pSlotSorryDialog.dismiss();
		pSlotLoginDialog = null;
		if(null!=pSlotLoginDialog)
			pSlotLoginDialog.dismiss();
		pSlotLoginDialog = null;
		super.onPause();
	}
	
	/**
	 * 
	* method Name:fetchCoupons    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void fetchCoupons(int pid)
	{
		if(0 == ILogin.getLoginUid())
		{
			showSlotLoginDialog();
			return;
		}
		mAjax = ServiceConfig.getAjax(Config.URL_REWARD_HISTORY);
		if(null == mAjax)
			return;
		
		mAjax.setData("uid", ILogin.getLoginUid());
		mAjax.setData("award_kk",ILogin.getLoginSkey());
		mAjax.setData("act_id",SlotMachineActivity.ACTID);
		
		if(null == mCoupons || mCoupons.size() <=0)
			mAjax.setData("timestamp", 0);
		else
		{
			BingoInfo aItem = mCoupons.get(mCoupons.size()-1);
			mAjax.setData("timestamp",aItem.getTimestamp());
		}
		mAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
		
		@Override
		public void onSuccess(JSONObject v, Response response) {
			closeLoadingLayer();
			mRequesting = false;
			final int errno = null != v ? v.optInt("errno", -1) : -1;
			if(errno==SlotMachineActivity.RELOGIN_ERRNO && ServiceConfig.isAutoRelogin())
			{
					if(ReloginWatcher.getInstance(CouponCollectionActivity.this).quiteReLogin())
					{
						showRefetchCouponDialog(0);
						return;
					}
					else
					{
						showSlotLoginDialog();
						return;
					}
			}
			else if(errno!=0)
			{
				showRefetchCouponDialog(errno);
				return;
			}
				
			/** {"total":null,
			 *   "history":[ {"cdkey":"ALVZRAAAPQmUJktk","extra":"QQ会员试用卡","time":"2013-11-06 11:43:14","success_code":"4"},
			 *               {"cdkey":"ALVZRAAAPSddtrnf","extra":"QQ会员试用卡","time":"2013-11-06 11:27:13","success_code":"4"},
			 *               {"cdkey":"ALVZRAAAPJjnLkmX","extra":"QQ会员试用卡","time":"2013-11-05 19:45:19","success_code":"4"},
			 *               {"cdkey":"ALVZRAAAPLDKEeay","extra":"QQ会员试用卡","time":"2013-11-05 19:44:58","success_code":"4"},
			 *               {"cdkey":"ALVZRAAAPJwvTHAk","extra":"QQ会员试用卡","time":"2013-11-05 19:15:36","success_code":"4"}],
			 *               "errno":0}
			 */
			
			mTotalNum  = v.optInt("total");
			mTotalPageNum = mTotalNum / CouponCollectionActivity.EACH_PAGE_SIZE;
			JSONArray historyArray = v.optJSONArray("history");
			if(null == historyArray)
				return;
			
			if(null == mCoupons )
				mCoupons = new ArrayList<BingoInfo>();
			for(int idx = 0 ; idx < historyArray.length(); idx++)
			{
				JSONObject item = historyArray.optJSONObject(idx);
				int success_code = item.optInt("success_code");
				
				BingoInfo aBingo = new BingoInfo();
				aBingo.setBingoId(success_code);
				aBingo.setBingoName(item.optString("extra"));
				aBingo.setTimestamp(item.optInt("time"));
				aBingo.setCdkey(item.optString("cdkey"));
				mCoupons.add(aBingo);
			}
			
			mCurrentPageId = mNextPageId;
			refreshCoupons();
			
		}});
	
		mAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
				closeLoadingLayer();
				mRequesting = false;
				showRefetchCouponDialog(0);
				
				refreshCoupons();
				
			}});
	
		showLoadingLayer(false);
		addAjax(mAjax);
		mAjax.send();
		mRequesting = true;
	}


	/**  
	* method Name:refreshCoupons    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void refreshCoupons() {
		
		if(null==mCoupons || mCoupons.size()<=0)
		{
			mListView.setVisibility(View.INVISIBLE);
			mNoPrizeView.setVisibility(View.VISIBLE);
			mNoPrizeTextView.setVisibility(View.VISIBLE);
			
			return;
		}
		
		if(null == mCouponAdapter){
			mCouponAdapter = new CouponCollectAdapter(this, mCoupons,mCM);
			mListView.setAdapter(mCouponAdapter);
		}
		mListView.setVisibility(View.VISIBLE);
		mNoPrizeView.setVisibility(View.INVISIBLE);
		mNoPrizeTextView.setVisibility(View.INVISIBLE);
		mCouponAdapter.resetModel(mCoupons);
		mCouponAdapter.notifyDataSetChanged();
	}
		/*
		bulletContainer.setVisibility(mCouponNum > 1 ? View.VISIBLE : View.GONE);
		if (mCouponNum > 1) {
			for (int i = 0, len = mCouponNum; i < len; i++) {
				ImageView view = new ImageView(CouponCollectionActivity.this);
				LinearLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp.setMargins(ToolUtil.dip2px(CouponCollectionActivity.this, 5), 0, 0, 0);
				view.setLayoutParams(lp);
				view.setImageResource(R.drawable.collect_bullet);
				bulletContainer.addView(view);
			}
		}

		
		mGallery.setVisibility(mCouponNum > 0 ? View.VISIBLE : View.GONE);
		
		if (mCouponNum > 0) {
			mCouponAdapter = new CouponGalleryItemAdapter(this, mCoupons);
			
			mGallery.setAdapter(mCouponAdapter);
			mGallery.setSelection(mCouponNum > 2 ? 1 : 0);
			mGallery.setOnItemClickListener(this);

			if (mCouponNum > 1) {
				mGallery.setOnItemSelectedListener(this);
			}
		}
		 
		
	}


	/*  
	 * Description:
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		
		if (bulletContainer == null)
			return;

		View oldBullet = bulletContainer.getChildAt(lastBulletIndex);

		if (oldBullet != null && oldBullet instanceof ImageView) {
			((ImageView) oldBullet).setImageResource(R.drawable.collect_bullet);
		}

		View newBullet = bulletContainer.getChildAt(position);

		if (newBullet != null && newBullet instanceof ImageView) {
			((ImageView) newBullet).setImageResource(R.drawable.collect_bullet_active);
		}

		lastBulletIndex = position;
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	 */
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(SlotMachineActivity.GO_LOGIN == requestCode)
		{
			fetchCoupons(mNextPageId);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	protected void showRefetchCouponDialog(int errorNo)
	{
		pSlotSorryDialog = new SlotSorryDialog(CouponCollectionActivity.this, 
				new SlotSorryDialog.OnClickListener()
				{

					@Override
					public void onDialogClick(int nButtonId) 
					{
						if (nButtonId == SlotSorryDialog.BUTTON_POSITIVE)
						{
							fetchCoupons(mNextPageId);
						}
						else if (nButtonId == SlotSorryDialog.BUTTON_NEGATIVE)
						{
							pSlotSorryDialog.dismiss();
							processBack();
						}
					}
				}
		);
		
		if(errorNo==SlotMachineActivity.TOO_QUICK_ERRNO)
		{
			pSlotSorryDialog.setProperty(R.string.request_too_often,R.string.try_again,
					R.string.btn_cancel);
		}
		else
			pSlotSorryDialog.setProperty(R.string.sorry_fail,R.string.try_again,
					R.string.btn_cancel);
		
		pSlotSorryDialog.show();
	}
	
	/**
	 * 
	* method Name:showSlotLoginDialog    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	protected void showSlotLoginDialog()
	{
		pSlotLoginDialog = new SlotLoginDialog(CouponCollectionActivity.this, 
				new SlotLoginDialog.OnClickListener()
				{

					@Override
					public void onDialogClick(int nButtonId) 
					{
						if (nButtonId == SlotLoginDialog.BUTTON_POSITIVE)
						{
							ToolUtil.startActivity(CouponCollectionActivity.this, 
									LoginActivity.class, null, SlotMachineActivity.GO_LOGIN);
						}
						else if (nButtonId == SlotLoginDialog.BUTTON_NEGATIVE)
						{
							Intent intent = new Intent(CouponCollectionActivity.this,
									SlotMachineActivity.class);  
			                intent.putExtra("try_again", "cancel");  
			                setResult(RESULT_OK, intent);  
			                
			                finish();
						}
						
					}
				}
		);
		
		if(TextUtils.isEmpty(mLoginHint))
		{
			mLoginHint = this.getString(R.string.slot_login_info);
		}
		
		pSlotLoginDialog.setProperty(getString(R.string.slot_not_login),
				mLoginHint,
				R.string.slot_login_now,
				R.string.left_away);
		
		pSlotLoginDialog.show();
	}
	
	@Override
	public String getActivityPageId() {
		return "000000";
	}
}
