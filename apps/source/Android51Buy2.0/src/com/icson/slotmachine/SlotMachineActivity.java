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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.home.HomeActivity;
import com.icson.home.ModuleInfo;
import com.icson.hotlist.HotlistActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.guide.UserGuideDialog;
import com.icson.lib.ui.LoopScrollView;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.login.ReloginWatcher;
import com.icson.my.coupon.CouponShowActivity;
import com.icson.preference.Preference;
import com.icson.slotmachine.BingoSplashDialog.OnDialogClickListener;
import com.icson.statistics.StatisticsEngine;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.icson.virtualpay.VirtualPayActivity;
import com.icson.yiqiang.YiQiangActivity;

/**  
 *   
 * Class Name:SlotMachineActivity 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-10-23 上午11:28:30 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class SlotMachineActivity extends BaseActivity implements SensorEventListener, OnDialogClickListener{

	public static final int  ACTID = 10649;//debug=14698; ACTID = 10649;
	private Ajax     mSlotAjax;
	private int  yx = 3;
	private List<Integer>  pics;
	
	private LoopScrollView scroll_1;
	private LoopScrollView scroll_2;
	private LoopScrollView scroll_3;
	private int []      scroll_idxs;
	private ImageView   light_1;
	private ImageView   light_2;
	private ImageView   light_3;
	private TextView    mBillborad;
	private HashMap<String,String> bingoInfo;
	private ImageView   mSlotRuleBtn;
	private ImageView   mBackBtn;
	
	private AnimationDrawable roll_light_anim1;
	private AnimationDrawable roll_light_anim2;
	private AnimationDrawable roll_light_anim3;
	private Drawable          stop_light_drawable;
	private AnimationDrawable shine_light_anim1;
	private AnimationDrawable shine_light_anim2;
	private AnimationDrawable shine_light_anim3;
	
	private int step = 0;
	
	private static final int  LIGHT_ROLL_ON =  0;
	private static final int  LIGHT_ROLL_STOP = 1;
	
	private static final int  COVER_SHIFT_DIS = -400;
	private ImageView switchBtn;
	private ImageView slotBar;
	private ImageView countDownView;
	
	private ImageView  coupon_card;
	private ImageView  coupon_shadow;
	private ImageView  cargo_cover;
	private ImageView  shake_hint; // 摇一摇也能摇奖
	private Drawable    drop_coin_drawable;
	private Drawable    drop_qq_drawable;
	private Drawable    drop_coupon_drawable;
	private Drawable    drop_product_drawable;
	private Drawable    drop_coin_shadow_drawable;
	private Drawable    drop_card_shadow_drawable;
	
	private AnimationDrawable  barAnimation;
	private TranslateAnimation shiftAnim;
	private TranslateAnimation dropAnim;
	
	private int remain_chance;
	//private long timeMark;
	public static final int DEFAULT_CHANCE = 3;
	
	//exp gold
	private List<ImageView> goldenCoins;
	private static final int  GOLDEN_COIN_NUM = 5;
	private Drawable        goldenHole;
	private Drawable        goldenCoininHole;
	private ImageView       expBonus;
	private int exp_count;
	
	private BingoInfo  mBingo;
	private int    bingoType;
	
	private static final float    Sensor_Threadhold = 0.7f;
	private SensorManager mSensorManager;
	private Vibrator mVibrator;
	private float    mTempShakeRange;
	private float    mMaxShakeRange;
	private float    mlastMax;
	private int      mShakeTryCount;
	private int      mShakeSecond;
	
	public static final int GO_SHARE = 1;
	public static final int GO_HISTORY = 2;
	public static final int GO_LOGIN = 3;
	
	
	public static final int RELOGIN_ERRNO = 500;
	public static final int TOO_QUICK_ERRNO = 6;
	
	//share -- > remain_chance += 2
	private static final int REWARD_CHANCE = 2;
	
	//public static final String WX_SHAKE_TRANS = "SHAKE";

	
	private boolean   mRolling;
	 
	private boolean   mFirstingCoin;
	
	private String    mLoginHint;
	private List<String>    mBillInfoSet;
	private int             mBillIdx;
	private SlotLoginDialog mSlotLoginDialog;
	
	private SlotSorryDialog mSlotSorryDialog;
	
	private UserGuideDialog mUserGuideDialog;
	private CouponSplashDialog mCouponSplashDialog;
	private ProductSplashDialog mProductSplashDialog;
	private WXShareResultReceiver mWXShareReceiver;
	
	private ArrayList<ModuleInfo> mModels = new ArrayList<ModuleInfo>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		mBingo = new BingoInfo();
		
		mTempShakeRange = Preference.getInstance().getShakeRange();
		mMaxShakeRange = mTempShakeRange;
		mShakeSecond = 1;
		
		this.setContentView(R.layout.activity_slot_machine,false);
		
		//once only
		//from Preference
		loadSlotExpInfo();
		
		//from net
		fetchRollInfo();

		fetchBingoUsers();
		
		initSensor();
		
		//load roll pics
		loadPics();
		
		mBackBtn = (ImageView)this.findViewById(R.id.slot_back_btn);
		mBackBtn.setOnClickListener(this);
		mSlotRuleBtn = (ImageView)this.findViewById(R.id.slot_title);
		mSlotRuleBtn.setOnClickListener(this);
		
		switchBtn = (ImageView)this.findViewById(R.id.switch_on);
		switchBtn.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mRolling)
					return true;
				if(event.getAction() ==  MotionEvent.ACTION_DOWN)
				{
					showCouponsCollection();
					return true;
				}
				return false;
			}});
		
		
		scroll_1 = (LoopScrollView) this.findViewById(R.id.ls_one);
		scroll_2 = (LoopScrollView) this.findViewById(R.id.ls_two);
		scroll_3 = (LoopScrollView) this.findViewById(R.id.ls_three);
		
		scroll_1.initView(pics,0);
		scroll_2.initView(pics,1);
		scroll_3.initView(pics,2);
		scroll_idxs = new int[3];
		
		//init position
		mHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				scroll_1.quickScrollTo(-1);
				scroll_2.quickScrollTo(-1);
				scroll_3.quickScrollTo(-1);
				
				mHandler.removeCallbacks(this);
			}},500);
		
		roll_light_anim1 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.roll_light);
		roll_light_anim2 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.roll_light);
		roll_light_anim3 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.roll_light);
		
		shine_light_anim1 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.shine_light);
		shine_light_anim2 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.shine_light);
		shine_light_anim3 = (AnimationDrawable) this.getResources().getDrawable(R.drawable.shine_light);
		
		light_1 = (ImageView)this.findViewById(R.id.light_one);
		light_2 = (ImageView)this.findViewById(R.id.light_two);
		light_3 = (ImageView)this.findViewById(R.id.light_three);
		
		mBillborad = (TextView)this.findViewById(R.id.winner_billborad);
		mBillborad.setVisibility(View.INVISIBLE);
		
		//Coins --> expBonus
		expBonus = (ImageView)this.findViewById(R.id.exp_bonus);
		
		//Cover area
		cargo_cover = (ImageView) this.findViewById(R.id.coupon_cover);
		shake_hint  = (ImageView) this.findViewById(R.id.shake_hint_v);
		shake_hint.setVisibility(View.VISIBLE);
		coupon_card = (ImageView)this.findViewById(R.id.coupon_item);
		coupon_shadow = (ImageView)this.findViewById(R.id.coupon_shadow);
			
		//Cover UP
		shiftAnim = new TranslateAnimation(0,0,0,COVER_SHIFT_DIS);
		shiftAnim.setDuration(1200);
		shiftAnim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				boolean coin2other = addExp2Reward();
				showWXShareing(coin2other);
				
				cargo_cover.clearAnimation();
				cargo_cover.layout(0, COVER_SHIFT_DIS, cargo_cover.getRight(), COVER_SHIFT_DIS + cargo_cover.getHeight());
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}});
		
		//Card down
		dropAnim = new TranslateAnimation(0,0,-500,0);
		dropAnim.setDuration(750);
		dropAnim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				if(bingoType == BingoInfo.BINGO_COIN && exp_count == 0)
					showFirstCoinHint();
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationStart(Animation animation) {
			}});
		
		
		//slotBar area
		slotBar = (ImageView) this.findViewById(R.id.start_btn);
		barAnimation = (AnimationDrawable)slotBar.getDrawable();
		slotBar.setOnClickListener(this);

		
		countDownView = (ImageView) this.findViewById(R.id.count_v);
		
		//register weixin share
		if(null == mWXShareReceiver)
			mWXShareReceiver = new WXShareResultReceiver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.BROADCAST_FROM_WXSHARE);
		registerReceiver(mWXShareReceiver, filter,Config.SLEF_BROADCAST_PERMISSION,null);
	}
	
	
	/**
	 * 
	* method Name:refleshGoldenCoins    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void refleshGoldenCoins() {
		if(null == goldenCoins)
		{
			goldenCoins = new ArrayList<ImageView>();
			ImageView item = (ImageView)this.findViewById(R.id.hole_one);
			goldenCoins.add(item);
			
			item = (ImageView)this.findViewById(R.id.hole_two);
			goldenCoins.add(item);
			
			item = (ImageView)this.findViewById(R.id.hole_three);
			goldenCoins.add(item);
		
			item = (ImageView)this.findViewById(R.id.hole_four);
			goldenCoins.add(item);
			
			item = (ImageView)this.findViewById(R.id.hole_five);
			goldenCoins.add(item);
		}
		
		exp_count %= goldenCoins.size();
		if(null == goldenHole)
			goldenHole = this.getResources().getDrawable(R.drawable.gold_hole);
		if(null == goldenCoininHole)
			goldenCoininHole = this.getResources().getDrawable(R.drawable.gold_in);
		
		for(int i = 0; i < goldenCoins.size(); i++)
		{
			if(i < exp_count)
				goldenCoins.get(i).setImageDrawable(goldenCoininHole);
			else
				goldenCoins.get(i).setImageDrawable(goldenHole);
		}
		if(exp_count == goldenCoins.size() )
			expBonus.setImageResource(R.drawable.exp_bonus);
		else
			expBonus.setImageResource(R.drawable.exp_bonus_bg);
		
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		Preference.getInstance().setUserGuideOfIndex(Preference.USER_GUIDE_SECOND_OPEN, 0);
		
		showCountDown();
		refleshGoldenCoins();
		
		coupon_card.setVisibility(View.INVISIBLE);
		coupon_shadow.setVisibility(View.INVISIBLE);
		//reset cover
		cargo_cover.layout(0, 0, cargo_cover.getRight(), cargo_cover.getHeight());
		
		mRolling = false;
		
		
		bingoType = -1;
		step = -1;
		mHandler.postDelayed(slotAnimation, 500);
		
		mFirstingCoin = false;
		showBingoUsers();
		
		if(null!=mWXShareReceiver)
		{
			if(mWXShareReceiver.isShareSucc())
				checkRewardRight();
			mWXShareReceiver.clearShareSucc();
		}
		
	}
	
	@Override
	protected void onPause(){
		switchLightRoll(LIGHT_ROLL_STOP);
		mHandler.removeCallbacks(billboardAnimation);
		mHandler.removeCallbacksAndMessages(null);
		if(null!=mUserGuideDialog)
		{
			mUserGuideDialog.cleanup();
			if(mUserGuideDialog.isShowing())
			{
				mUserGuideDialog.dismiss();
			}
			mUserGuideDialog = null;
		}
		
		super.onPause();
	}
	
	
	@Override
	protected void onDestroy()
	{
		if(null!= mSensorManager)
			mSensorManager.unregisterListener(this);
		mSensorManager = null;
		if(null != mModels) {
			mModels = null;
		}
		
		if(null!=this.mSlotLoginDialog && mSlotLoginDialog.isShowing())
			mSlotLoginDialog.dismiss();
		if(null!=mSlotSorryDialog && mSlotSorryDialog.isShowing())
			mSlotSorryDialog.dismiss();
		
		if(null!=mWXShareReceiver)
			unregisterReceiver(mWXShareReceiver);
		mWXShareReceiver = null;
		
		if(null!=mProductSplashDialog)
		{
			if(mProductSplashDialog.isShowing())
				mProductSplashDialog.dismiss();
			mProductSplashDialog.destroy();
		}
		mProductSplashDialog = null;
		
		if(null!=mCouponSplashDialog && mCouponSplashDialog.isShowing())
			mCouponSplashDialog.dismiss();
		mCouponSplashDialog = null;
		/*drop_coin_drawable = null;
		drop_qq_drawable = null;
		drop_coupon_drawable = null;
		drop_product_drawable = null;
		drop_coin_shadow_drawable = null;
		drop_card_shadow_drawable = null;
		
		goldenHole = null;
		goldenCoininHole = null;
		stop_light_drawable = null;
		
		roll_light_anim1 = null;
		roll_light_anim2 = null;
		roll_light_anim3 = null;
		barAnimation = null;
		shiftAnim = null;
		dropAnim = null;
		
		shine_light_anim1 = null;
		shine_light_anim2 = null;
		shine_light_anim3 = null;
		*/
		if(null!=goldenCoins)
			goldenCoins.clear();
		super.onDestroy();
	}
	
	/**  
	* method Name:initSensor    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void initSensor() {
		//Sensor
		mSensorManager = (SensorManager)getApplicationContext().getSystemService(SENSOR_SERVICE);
        //震动
        mVibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        
        if(null!=mSensorManager)
			mSensorManager.registerListener(this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_UI); 
		else
			shake_hint.setVisibility(View.INVISIBLE);
 	}


	private void showCouponsCollection()
	{
		Bundle params = null;
		if(!TextUtils.isEmpty(mLoginHint))
		{
			params = new Bundle();
			params.putString("hint", mLoginHint);
		}
		
		if(0 == ILogin.getLoginUid())
		{
			showSlotLoginDialog();
		}
		else
		{
			ToolUtil.startActivity(this, com.icson.slotmachine.CouponCollectionActivity.class,
				params,GO_HISTORY);
		}
	}
	/**
	 * 
	* method Name:loadPics    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void loadPics()
	{
		pics = new ArrayList<Integer>();
		//type 1 coupon
		pics.add(R.drawable.slot_machine_1);
		//type 2 qq
		pics.add(R.drawable.slot_machine_2);
		//type 3 goldcoin
		//pics.add(this.getResources().getDrawable(R.drawable.slot_machine_3));
		pics.add(R.drawable.slot_machine_3);
		//product
		pics.add(R.drawable.slot_machine_4);
		
		
	}
	
	/**
	 * 
	* method Name:showCountDown    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	protected void showCountDown() {
		switch (remain_chance)
		{
		case 0:
			countDownView.setImageResource(R.drawable.reciprocal_0);
			break;
		case 1:
			countDownView.setImageResource(R.drawable.reciprocal_1);
			break;
		case 2:
			countDownView.setImageResource(R.drawable.reciprocal_2);
			break;
		case 3:
			countDownView.setImageResource(R.drawable.reciprocal_3);
			break;
		case 4:
			countDownView.setImageResource(R.drawable.reciprocal_4);
			break;
		case 5:
			countDownView.setImageResource(R.drawable.reciprocal_5);
			break;
		case 6:
			countDownView.setImageResource(R.drawable.reciprocal_6);
			break;
		case 7:
			countDownView.setImageResource(R.drawable.reciprocal_7);
			break;
		case 8:
			countDownView.setImageResource(R.drawable.reciprocal_8);
			break;
		case 9:
			countDownView.setImageResource(R.drawable.reciprocal_9);
			break;
		case 10:
			countDownView.setImageResource(R.drawable.reciprocal_10);
			break;
		default:
			countDownView.setImageResource(R.drawable.reciprocal_bg);
			break;
		}
	}
	
	/**
	 * 
	* method Name:showBingoUsers    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	protected void showBingoUsers()
	{
		Random aRan = new Random(System.currentTimeMillis());
		int preIdx = aRan.nextInt(3);
		int Rid;
		switch (preIdx)
		{
		case 2:
			Rid = R.string.billboard_pre2;
			break;
		case 1:
			Rid = R.string.billboard_pre1;
			break;
		case 0:
		default:
			Rid = R.string.billboard_pre0;
			break;
		}
		if(mBillInfoSet == null)
			mBillInfoSet = new ArrayList<String>();
		mBillInfoSet.clear();
		
		mBillInfoSet.add(getString(Rid));
		
		boolean rollflag = false;
		if(null!=bingoInfo)
		{
			Iterator<Entry<String, String>> it = bingoInfo.entrySet().iterator(); 
			while(it.hasNext())
			{
				rollflag = true;
				Entry<String,String> entry = it.next();
				mBillInfoSet.add(""+entry.getKey()+ getString(R.string.billboard_get)
						+ entry.getValue());
			}
		}
		mBillIdx = 0;
		mBillborad.setText(mBillInfoSet.get(mBillIdx));
		mBillborad.setVisibility(View.VISIBLE);
		
		if(rollflag)
		{
			mHandler.postDelayed(billboardAnimation,4000);
		}
	}
	
	/**
	 * 
	* method Name:showFirstCoinHint    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void showFirstCoinHint()
	{
		mBillborad.setText(this.getString(R.string.coin_inspire));
		mFirstingCoin = true;
		
	}
	/**
	 * One and only Handler
	 */
	private Handler mHandler = new Handler();
	//slotAnimation
	private Runnable slotAnimation = new Runnable(){
		
		private static final int MAX_STEP = 800;
		private long startStamp;
		private long curStamp;
		@Override
		public void run() {
			step++;
			if(step == 0 )
			{
				mHandler.removeCallbacks(this);
				
				if(bingoType <=0)
					switchLightRoll(LIGHT_ROLL_ON);
				
				return;
			}
			
			curStamp = System.currentTimeMillis();
			if(step == 1)
				startStamp = curStamp;
			
			if(startStamp + 8000 < curStamp )
			{
				bingoType = 0;
			}
			
			boolean stopFlag1 = scrollWithStep(scroll_1,0);
			boolean stopFlag2 = scrollWithStep(scroll_2,1);
			boolean stopFlag3 = scrollWithStep(scroll_3,2);
			
			if(stopFlag1 && stopFlag2 && stopFlag3)
			{
				mHandler.removeCallbacks(this);
				if(step > 0)
				{
					step = -1;
					mHandler.postDelayed(this, 1000);
					if(bingoType > 0)
					{
						runBingo();
					}else
					{
						UiUtils.makeToast(SlotMachineActivity.this,R.string.no_reward);
					}
				}
			}
			else if(step < MAX_STEP )
				mHandler.postDelayed(this,50);
			
			
				
		}
			
	};
	
	/**
	 * forever roll billboard
	 */
	private Runnable billboardAnimation = new Runnable(){

		@Override
		public void run() {
			if(mFirstingCoin)
			{
				mBillborad.setText(getString(R.string.coin_inspire));
				mHandler.removeCallbacks(this);
				return;
			}
			
			mBillIdx = (mBillIdx+1) % mBillInfoSet.size();
			mBillborad.setText(mBillInfoSet.get(mBillIdx));
			
			mHandler.removeCallbacks(this);
			mHandler.postDelayed(this, 4000); 
			 
		}};
	
	/**
	 * 
	* method Name:scrollWithStep    
	* method Description:  
	* @param scrollV
	* @param idx
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	public boolean scrollWithStep(LoopScrollView scrollV, int idx)
	{
		
		boolean scollStoped = false;
		
		int mark0 = 4 + idx; 
		int mark1 = 30 + (idx+1)*15; 
		int mark2 = mark1+200;
		
		//slow start
		if(step < mark0)
			scrollV.loopScrollBy(scrollV.getItemHeight()/16);
		//loop roll
		else if(step < mark1 || bingoType < 0)
			scrollV.loopScrollBy(100 + (idx+1)*20);
		//stop
		else if(step < mark2 && bingoType >= 0)
		{
			if(bingoType == 0)//miss
			{
				scollStoped = scrollV.slowScrollTo( (scroll_idxs[idx] - 1 - idx + pics.size()) % pics.size());
			}
			else
			{
				scollStoped = scrollV.slowScrollTo( (bingoType - 1 - idx + pics.size()) % pics.size());
			}
		}
		else 
			scollStoped = true;
		
		return scollStoped;
		
	}
	
	
	///////////////////   NET REQUEST   ////////////////////////////////////////////////////////////
	
	/**  
	* method Name:randomAllIdx    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void randomAllIdx() {
		int i = 0;
		Random ad = new Random(System.currentTimeMillis());
		for(i = 0; i < scroll_idxs.length; i++)
		{
			scroll_idxs[i] = ad.nextInt(pics.size()*(i+1) + 1) % pics.size();
		}
		
		for(i = 0; i < scroll_idxs.length-1; i++)
		{
			if(scroll_idxs[i]!= scroll_idxs[i+1])
			{
				i = 0;
				break;
			}
		}
		
		if(i==scroll_idxs.length-1)
		{
			scroll_idxs[0] = (scroll_idxs[0]+1)%pics.size();
		}
	}

	/**
	 * 
	 */
	private void fetchBingoUsers()
	{
		mSlotAjax = ServiceConfig.getAjax(Config.URL_SLOT_BULLETIN);
		if(null == mSlotAjax)
			return;
		
		mSlotAjax.setData("uid", ILogin.getLoginUid());
		mSlotAjax.setData("did", StatisticsUtils.getDeviceUid(this));
		mSlotAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			
			@Override
			public void onSuccess(JSONObject v, Response response) {
				final int errno = null != v ? v.optInt("errno", -1) : -1;
				if(errno!=0)
				{
					return;
				}
				
				JSONArray data = v.optJSONArray("data");
				if(null!=data)
				{
					for(int i=0;i <data.length(); i++)
					{
						JSONObject item = data.optJSONObject(i);
						String aName = item.optString("nick");
						if(TextUtils.isEmpty(aName))
							aName = item.optString("uid");
						String aReward = item.optString("success_name");
						addBingoInfo(aName,aReward);
					}
				}
				
				showBingoUsers();
				
			}});
		
		mSlotAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
			}});
		
		addAjax(mSlotAjax);
		mSlotAjax.send();
	}
	
	/**  
	* method Name:addBingoInfo    
	* method Description:  
	* @param aUid
	* @param aReward   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void addBingoInfo(String aUid, String  aReward) {
		if(TextUtils.isEmpty(aUid) || TextUtils.isEmpty(aReward))
			return;
		
		if(null == bingoInfo)
			bingoInfo = new HashMap<String,String>();
		bingoInfo.put(aUid,aReward);
	}

	/**  
	* method Name:checkLoginNotice    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void checkLoginNotice() {
		
		mSlotAjax = ServiceConfig.getAjax(Config.URL_MB_ROLL_LOGIN_NOTICE);
		if(null == mSlotAjax)
			return;
		mSlotAjax.setData("uid", ILogin.getLoginUid());
		mSlotAjax.setData("did", StatisticsUtils.getDeviceUid(this));
		
		mSlotAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			
			@Override
			public void onSuccess(JSONObject v, Response response) {
				final int errno = null != v ? v.optInt("errno", -1) : -1;
				if(errno!=0)
					return;
				
				mLoginHint = v.optString("data").replace("\\n", "\n");
			}});
		
		mSlotAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
			}});
		
		addAjax(mSlotAjax);
		mSlotAjax.send();
	}
	
	/**
	 * 
	* method Name:fetchRollInfo    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void fetchRollInfo()
	{
		if(0 == ILogin.getLoginUid())
		{
			checkLoginNotice();
			return;
		}
		
		initUserGuideInfo();
		
		mSlotAjax = ServiceConfig.getAjax(Config.URL_MB_ROLL_INFO);
		if(null == mSlotAjax)
			return;
		
		mSlotAjax.setData("uid", ILogin.getLoginUid());
		mSlotAjax.setData("did", StatisticsUtils.getDeviceUid(this));
		mSlotAjax.setData("act_id",ACTID);
		
		mSlotAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			
			@Override
			public void onSuccess(JSONObject v, Response response) {
				closeLoadingLayer();
				final int errno = null != v ? v.optInt("errno", -1) : -1;
				if(errno==RELOGIN_ERRNO && ServiceConfig.isAutoRelogin())
				{
					if(ReloginWatcher.getInstance(SlotMachineActivity.this).quiteReLogin())
					{
						showRefetchInfoDialog(0);
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
					showRefetchInfoDialog(errno);
					return;
				}
				
				JSONObject data = v.optJSONObject("data");
				if(null!=data)
				{
					remain_chance = data.optInt("remain_cnt");
					exp_count     = data.optInt("exp_cnt");
				}
				
				showCountDown();
				refleshGoldenCoins();
				
			}});
		
		mSlotAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
				closeLoadingLayer();
				showRefetchInfoDialog(0);
								
			}});
		addAjax(mSlotAjax);
		showLoadingLayer(false);
		mSlotAjax.send();
	}
	
	
	/**
	 * 
	* method Name:rollSlotOnce    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void rollSlotOnce()
	{
		if(mRolling)
		{
			UiUtils.makeToast(SlotMachineActivity.this,R.string.slot_rolling_too_often);
			return;
		}
		
		bingoType = -1;
		shake_hint.setVisibility(View.INVISIBLE);
		coupon_card.setVisibility(View.INVISIBLE);
		coupon_shadow.setVisibility(View.INVISIBLE);
		
		
		if(0 == ILogin.getLoginUid())
		{
			mRolling = true;
			showSlotLoginDialog();
			return;
		}
		else
		{
			if(remain_chance <= 0)
			{
				UiUtils.makeToast(SlotMachineActivity.this,R.string.no_chance_today);
				return; 
			}
			
			StatisticsEngine.trackEvent(SlotMachineActivity.this, "slot_shakeit");
			//roll lights
			switchLightRoll(LIGHT_ROLL_STOP);
			
			if(barAnimation.isRunning())
			{
				barAnimation.stop();
			}
			barAnimation.start();
			
			//clear
			mBingo.clear();
			
			//mSlotAjax = AjaxUtil.post("http://event.yixun.com/json.php?mod=rewardm&act=lottery");
			//mSlotAjax = ServiceConfig.getAjax(Config.URL_MB_ROLL_INFO);
			//mSlotAjax.setParser(new JSONParser());
			mSlotAjax = ServiceConfig.getAjax(Config.URL_SLOT_ROLL);
			if(null != mSlotAjax)
			{
				mSlotAjax.setData("uid", ILogin.getLoginUid());
				mSlotAjax.setData("award_kk",ILogin.getLoginSkey());
				mSlotAjax.setData("did", StatisticsUtils.getDeviceUid(this));
				mSlotAjax.setData("act_id",ACTID);
				mSlotAjax.setData("province_id", FullDistrictHelper.getProvinceIPId());
				mSlotAjax.setData("site_id",ILogin.getSiteId());
				mSlotAjax.setData("district_id",FullDistrictHelper.getDistrictId());
				mSlotAjax.setTimeout(6);
		
				mSlotAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){

					@Override
					public void onSuccess(JSONObject data, Response response) {
						/* {
						 *  "errno":0, 
						 *  "success_type":"4",
						 *  "success_name":"商品",
						 *  "reward_detail":"恭喜你中了商品，请再接再厉",
						 *  "share_title":"商品",
						 *  "share_content":"抽中天天摇特供神秘价，一般人我不告诉TA！商品：*****",
						 *  "share_link":"http:\/\/m.51buy.com\/touch-cps.html?target_url=m.51buy.com\/ttyao.html&ytag=0.611300001300000",
						 *  "success_code":"20525",
						
						 *  "commodity_info":{ "price_normal":11000,
						 *                     "price_icson":11900,
						 *                     "pro_expire":1384336130,
						 *                     "pro_img_url":"http:\/\/img1.icson.com\/product\/mm\/15\/058\/15-058-141.jpg",
						 *                     "char_id" : "15-058-141"
						 *                     "channel_id": "500"}
						 *  "reward_info":{} 
						 *  {"success_type":-1}
						 *  { "success_type":"4","success_name":"商品","reward_detail":"恭喜你中了商品，请再接再厉",
						 *  	"share_title":"商品","share_content":"抽中天天摇特供神秘价，一般人我不告诉TA！商品：*****", 
						 *  	"share_link":"http:\/\/m.51buy.com\/touch-cps.html?target_url=m.51buy.com\/ttyao.html&ytag=0.611300001300000",
						 *       "success_code":"20525",}
						 * 
						 */
						
						
						final int errno = null != data ? data.optInt("errno", -1) : -1;
				
						bingoType = 0;
						if(errno!=0)
						{
							return;
						}
						
						bingoType = data.optInt("success_type");
						long pid = data.optInt("success_code");
						
						//fakeBingo();
						//return;
						
						//不识别
						if(bingoType > BingoInfo.MAX_BINGO_TYPE ||
								(bingoType == BingoInfo.BINGO_OTHER && pid <= 1 ))
						{
							bingoType = 0;
							return;
						}
						
						mBingo.setBingoType(bingoType);
						mBingo.setBingoName(data.optString("success_name"));
						mBingo.setBingoDetail(data.optString("reward_detail"));
						//5 coins -->exReward
						JSONObject exReward = data.optJSONObject("reward_info");
						BingoInfo exOne = null;
						if(null==exReward)
						{
							mBingo.setExpBingoInfo(null);
						}
						else
						{
							exOne = new BingoInfo();
							exOne.setBingoType(exReward.optInt("success_type",-1));
							if(exOne.getBingoType()>=0)
							{
								exOne.setBingoName(exReward.optString("success_name"));
								exOne.setBingoDetail(exReward.optString("reward_detail"));
								exOne.setShareTitle(exReward.optString("share_title"));
								exOne.setShareContent(exReward.optString("share_content"));
								exOne.setShareUrl(exReward.optString("share_link"));
							}
							mBingo.setExpBingoInfo(exOne);
						}
						
						mBingo.setShareTitle(data.optString("share_title"));
						mBingo.setShareContent(data.optString("share_content"));
						mBingo.setShareUrl(data.optString("share_link"));
						mBingo.setTimestamp(data.optInt("reward_expire"));
						if(bingoType == BingoInfo.BINGO_OTHER)//product about
						{
							mBingo.setBingoId(pid);
							try {
								JSONObject proObj = data.getJSONObject("commodity_info");
								mBingo.setPriceNormal(proObj.optInt("price_normal"));
								mBingo.setPriceIcson(proObj.optInt("price_icson"));
								mBingo.setTimestamp(proObj.optLong("pro_expire"));
								mBingo.setProductChannelId(proObj.optString("channel_id","0"));
								mBingo.setProductCharid(proObj.optString("char_id"));
								mBingo.setProductImgUrl(proObj.optString("pro_img_url"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								mBingo.clear();
								bingoType = 0;
							}
						} 
						
							
						//make sure 5 local coins
						if(bingoType == BingoInfo.BINGO_COIN &&
								(exp_count + 1 >= GOLDEN_COIN_NUM) &&
								null == exOne)
						{
								bingoType = 0;
								mBingo.clear();
						}
						
					}});
		
				mSlotAjax.setOnErrorListener(new OnErrorListener(){

					@Override
					public void onError(Ajax ajax, Response response) {
						bingoType = 0;
					}});
		
				
			
				mSlotAjax.send();
			} //end of NULL ! =  mSlotAjax
			
			
		}
		
		randomAllIdx();
		
		//start to roll
		step = 0;
		mHandler.post(slotAnimation);
		
		remain_chance--;
		
		showCountDown();
	}
	
	
	
	/**  
	* method Name:fakeBingo    
	* method Description:  
	* @param bingoType2   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	protected void fakeBingo() {
		
		yx = (yx+1)%5;
		bingoType = yx;
		mBingo.setBingoType(bingoType);
		switch (bingoType)
		{
		case BingoInfo.BINGO_COIN:
			mBingo.setBingoName("金币");
			mBingo.setBingoDetail("赢取金币赢取金币赢取金币赢取金币赢取金币赢取金币赢取金币赢取金币赢取金币赢取金币");
			mBingo.setExpBingoInfo(null);
			mBingo.setShareTitle("天天摇中了金币");
			mBingo.setShareContent("货真价实的满5可兑换优惠券的金币");
			mBingo.setShareUrl("http://m.51buy.com/wad-weixin.html?type=tiantianyao");
			if(this.exp_count ==4)
			{
				BingoInfo exone = new BingoInfo();
				exone.setBingoType(-1);
				{
					exone.setBingoDetail("优惠价优惠价a啊啊啊");
					exone.setBingoName("满10000减1元");
					exone.setShareTitle("天天摇金币变成了优惠券");
					exone.setShareContent("货真价实的满100减10优惠券哦");
					exone.setShareUrl("http://m.51buy.com/wad-weixin.html?src=barcode&type=tiantianyao");
				}
				mBingo.setExpBingoInfo(exone);
			}
			break;
		case BingoInfo.BINGO_COUPON:
			mBingo.setBingoName("满100减10");
			mBingo.setBingoDetail("百货购物满百减10元百货购物满百减10元百货购物满百减10元");
			mBingo.setExpBingoInfo(null);
			mBingo.setShareTitle("天天摇中了优惠券");
			mBingo.setShareContent("货真价实的满100减10优惠券哦");
			mBingo.setShareUrl("http://m.51buy.com/wad-weixin.html?src=barcode&type=tiantianyao");
			break;
		case BingoInfo.BINGO_OTHER:
			mBingo.setBingoName("特价商品");
			mBingo.setBingoDetail("特价百货商品千载难逢 2014-12-31前使用，与其他优惠不可同享与其他优惠不可同享");
			mBingo.setExpBingoInfo(null);
			mBingo.setShareTitle("天天摇中了商品");
			mBingo.setShareContent("货真价实的特价品");
			mBingo.setShareUrl("http://m.51buy.com/wad-weixin.html?src=barcode&type=proinfo&productid=36560");
			
			//product about
			long cur = System.currentTimeMillis();
			mBingo.setBingoId(36560);
			mBingo.setTimestamp(cur + 86400*30000);
			mBingo.setPriceNormal(299900);
			mBingo.setPriceIcson(279900);
			mBingo.proCharId = "14-058-033";
			String url = IcsonProImgHelper.getAdapterPicUrl("14-058-033", 80);
			mBingo.setProductImgUrl(url);
			break;
		case BingoInfo.BINGO_CDKEY:
			mBingo.setBingoName("QQ会员试用");
			mBingo.setBingoDetail("QQ会员试用一个月QQ会员试用一个月QQ会员试用一个月QQ会员试用一个月QQ会员试用一个月");
			mBingo.setExpBingoInfo(null);
			mBingo.setShareTitle("天天摇中了CDKEY");
			mBingo.setShareContent("货真价实的CDKEY");
			mBingo.setShareUrl("http://m.51buy.com/wad-weixin.html?src=barcode&type=tiantianyao");
			break;
		}
		
	}

	/////////////////////////////////////////    Preference     ////////////////////////////////////////
	/**
	 * 
	 */
	private void loadSlotExpInfo() {
		remain_chance =  0;
		exp_count     =  0;
		
		/*
		String localSlotInfo = Preference.getInstance().getSlotChanceExp();
		if(!TextUtils.isEmpty(localSlotInfo) && localSlotInfo.contains(":"))
		{
			String items [] = localSlotInfo.split(":");
			remain_chance =  Integer.valueOf(items[0]);
			exp_count     =  Integer.valueOf(items[1]);
			
			Calendar todayStart = Calendar.getInstance();  
	        todayStart.set(Calendar.HOUR_OF_DAY, 0);  
	        todayStart.set(Calendar.MINUTE, 0);  
	        todayStart.set(Calendar.SECOND, 0);  
	        todayStart.set(Calendar.MILLISECOND, 0);
	        
	        long curMark = todayStart.getTime().getTime();
			
	        timeMark = 0;
			if(items.length>2)
			{
				timeMark = Long.valueOf(items[2]);
			}
			
			if(curMark - timeMark >= 86400)
			{
				remain_chance = DEFAULT_CHANCE;
			}
			
			timeMark = curMark;
			//int curDay = 
			
		}*/
	}
	
//////////   Sensor about  //////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	/*  
	 * Description:
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		//set default
		if(mMaxShakeRange < 4)
			mMaxShakeRange = event.sensor.getMaximumRange();
		
		int sensorType = event.sensor.getType();
		
		float[] values = event.values;
		
		if(sensorType == Sensor.TYPE_ACCELEROMETER)
		{
			/*
			float max = Math.max(
					Math.abs(values[0]), Math.abs(values[1]));
			max = Math.max(max, Math.abs(values[2]));
			*/
			//alwasy x
			float max = values[SensorManager.DATA_X];
			Log.v("ShakeSensor", "max:" + max + " last:" + mlastMax+ "mMaxShakeRange:" + mMaxShakeRange);
			if(mlastMax > 4  && max - mlastMax > 4.5)
			{
				saveShakeRangeIfNecessary(max);
			}
			
			mlastMax = max;
			
			
			if(max > (mMaxShakeRange*Sensor_Threadhold) && !mRolling)
			{
				saveShakeRangeIfNecessary(max);
				if((mShakeSecond % 2) == 0)
				{
					mVibrator.vibrate(500);
					this.rollSlotOnce();
				}
				mShakeSecond++;
			}
			
		}
	}
	
	/**
	 * 
	* method Name:saveShakeRangeIfNecessary    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void saveShakeRangeIfNecessary(float max)
	{
		if(max > mTempShakeRange)
		{
			mTempShakeRange = max;
			Preference.getInstance().setShakeRange(mTempShakeRange);
		}
		mShakeTryCount++;
		if(mShakeTryCount > 2)
			mMaxShakeRange = mTempShakeRange;
	}
/////////////////////////  Animation views   //////////////////////////////////////////////////////////////////////
	/**
	 * 
	* method Name:switchLightRoll    
	* method Description:  
	* @param on_off   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void switchLightRoll(int  on_off)
	{
		shine_light_anim1.stop();
		shine_light_anim2.stop();
		shine_light_anim3.stop();
		
		if(LIGHT_ROLL_STOP == on_off)
		{
			roll_light_anim1.stop();
			roll_light_anim2.stop();
			roll_light_anim3.stop();
			
			if(null == stop_light_drawable)
				stop_light_drawable = this.getResources().getDrawable(R.drawable.prize_light4);
			light_1.setImageDrawable(stop_light_drawable);
			light_2.setImageDrawable(stop_light_drawable);
			light_3.setImageDrawable(stop_light_drawable);
			
			mRolling = true;
			
			mHandler.removeCallbacks(billboardAnimation);
			
		}
		else if(LIGHT_ROLL_ON == on_off)
		{
			light_1.setImageDrawable(this.roll_light_anim1);
			light_2.setImageDrawable(this.roll_light_anim2);
			light_3.setImageDrawable(this.roll_light_anim3);
			
			roll_light_anim1.start();
			roll_light_anim2.start();
			roll_light_anim3.start();
			
			mRolling = false;
			
			if(!mFirstingCoin)
				showBingoUsers();
			
		}
	}
	
	
	/**
	 * 
	* method Name:runBingo    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void runBingo()
	{
		mFirstingCoin = false;
		
		roll_light_anim1.stop();
		roll_light_anim2.stop();
		roll_light_anim3.stop();
		
		light_1.setImageDrawable(shine_light_anim1);
		light_2.setImageDrawable(shine_light_anim2);
		light_3.setImageDrawable(shine_light_anim3);
		
		shine_light_anim1.start();
		shine_light_anim2.start();
		shine_light_anim3.start();
		
		switch(mBingo.getBingoType())
		{
		case BingoInfo.BINGO_COIN:
			if(drop_coin_drawable == null)
				drop_coin_drawable = this.getResources().getDrawable(R.drawable.drop_gold);
			if(drop_coin_shadow_drawable == null)
				drop_coin_shadow_drawable = this.getResources().getDrawable(R.drawable.drop_coin_shadow);
			coupon_shadow.setImageDrawable(drop_coin_shadow_drawable);
			coupon_card.setImageDrawable(drop_coin_drawable);
			break;
		case BingoInfo.BINGO_CDKEY:
			if(drop_qq_drawable == null)
				drop_qq_drawable = this.getResources().getDrawable(R.drawable.drop_qq);
			if(drop_card_shadow_drawable == null)
				drop_card_shadow_drawable = this.getResources().getDrawable(R.drawable.drop_card_shadow);
			coupon_shadow.setImageDrawable(drop_card_shadow_drawable);
			coupon_card.setImageDrawable(drop_qq_drawable);
			break;
		case BingoInfo.BINGO_COUPON:
			if(drop_coupon_drawable == null)
				drop_coupon_drawable = this.getResources().getDrawable(R.drawable.drop_coupon);
			if(drop_card_shadow_drawable == null)
				drop_card_shadow_drawable = this.getResources().getDrawable(R.drawable.drop_card_shadow);
			coupon_shadow.setImageDrawable(drop_card_shadow_drawable);
			coupon_card.setImageDrawable(drop_coupon_drawable);
			break;
		case BingoInfo.BINGO_OTHER:
			if(drop_product_drawable == null)
				drop_product_drawable = this.getResources().getDrawable(R.drawable.drop_product);
			if(drop_coin_shadow_drawable == null)
				drop_coin_shadow_drawable = this.getResources().getDrawable(R.drawable.drop_coin_shadow);
				
			coupon_shadow.setImageDrawable(drop_coin_shadow_drawable);
			coupon_card.setImageDrawable(drop_product_drawable);
			break;
		}
		
		cargo_cover.startAnimation(shiftAnim);
		coupon_card.setVisibility(View.VISIBLE);
		coupon_shadow.setVisibility(View.VISIBLE);
		coupon_card.startAnimation(dropAnim);
		
	}

	/**
	 * 
	* method Name:addExp2Reward    
	* method Description:  
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	private boolean addExp2Reward()
	{
		if(mBingo.getBingoType()== BingoInfo.BINGO_COIN)
		{
			String str  = ""+exp_count +  " expbingo:" + mBingo.getExpBingoInfo();
			Log.w("coin", str);
			
			BingoInfo exone = mBingo.getExpBingoInfo();
			//full at once
			if(null!=exone)
			{
				exp_count = 0;
				if(null == goldenCoininHole)
					goldenCoininHole = this.getResources().getDrawable(R.drawable.gold_in);
				
				while(exp_count < goldenCoins.size())
					goldenCoins.get(exp_count++).setImageDrawable(goldenCoininHole);
				  
				
				expBonus.setImageResource(R.drawable.exp_bonus);
				return true;
			}
			else if(exp_count + 1 < goldenCoins.size())//coin ++
			{
				goldenCoins.get(exp_count++).setImageDrawable(goldenCoininHole);
				return false;
			}
			else
				return false;
		}
		return false;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(GO_HISTORY == requestCode)
		{
			if( null != data)
			{
				Bundle pExtras = data.getExtras();
				String strText = (null != pExtras ? pExtras.getString("try_again") : "");
				if(null!=strText && strText.equals("cancel"))  
					finish();
			}
		}
		else if(GO_LOGIN == requestCode)
		{ 
			if(LoginActivity.FLAG_RESULT_LOGIN_SUCCESS == resultCode) {
				fetchRollInfo();
			}
		}
		else if(GO_SHARE == requestCode)
		{
			if( null != data)
			{
				Bundle pExtras = data.getExtras();
				boolean addFlag =  (null != pExtras ? pExtras.getBoolean("add_chance") : false);
				if(addFlag)
					remain_chance += REWARD_CHANCE;
			}
		}	
	}
	
	/**
	 * 
	* method Name:goShareing    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void showWXShareing(boolean coin2other)
	{
		Bundle pExtras = new Bundle();
		if(mBingo.getBingoType() == BingoInfo.BINGO_OTHER)
		{
			pExtras.putLong("proid", mBingo.getBingoId());
			pExtras.putLong("ori_price",mBingo.getPriceNormal());
			pExtras.putLong("cur_price",mBingo.getPriceIcson());
			pExtras.putString("img_url", mBingo.getProductImgUrl());
			pExtras.putString("bingo_detail",mBingo.getBingoDetail());
			pExtras.putLong("expire", mBingo.getTimestamp());
			pExtras.putString("share_content",mBingo.getShareContent());
			pExtras.putString("share_title", mBingo.getShareTitle());
			pExtras.putString("link_url", mBingo.getShareUrl());
			pExtras.putString("channel_id", mBingo.getProductChannelId());
			pExtras.putString("char_id", mBingo.proCharId);
			
			if(null == mProductSplashDialog)
				mProductSplashDialog = new ProductSplashDialog(this, this,R.layout.activity_slot_product_share);
			mProductSplashDialog.setBundle(pExtras);
			mProductSplashDialog.show();
			//ToolUtil.startActivity(SlotMachineActivity.this, 
			//		com.icson.slotmachine.ProductSplashActivity.class,
			//		pExtras,GO_SHARE);
		}
		else 
		{
			BingoInfo exone = mBingo.getExpBingoInfo();
			if(coin2other && null!=exone && exone.getBingoType() < 0)
			{
				showCoin2CouponFailDialog();
			}
			else
			{
				BingoInfo shareOne = mBingo;
				if(coin2other && null!=exone)
					shareOne = exone;
				pExtras.putBoolean("from_coin_reward", coin2other);
				
				pExtras.putInt("bingo_type", shareOne.getBingoType());
				pExtras.putString("bingo_name", shareOne.getBingoName());
				pExtras.putString("bingo_detail",shareOne.getBingoDetail());
				pExtras.putString("share_content",shareOne.getShareContent());
				pExtras.putString("share_title", shareOne.getShareTitle());
				pExtras.putString("link_url", shareOne.getShareUrl());
			
				//if(this.bingoType%2 == 0)
				//{
				//	ToolUtil.startActivity(SlotMachineActivity.this, 
				//			com.icson.slotmachine.CouponSplashActivity.class,
				//			pExtras,GO_SHARE);
				//	return;
				//}
				if(null == mCouponSplashDialog)
					mCouponSplashDialog = new CouponSplashDialog(this, this,R.layout.activity_slot_share);
				mCouponSplashDialog.setBundle(pExtras);
				mCouponSplashDialog.show();
			}
		}
	}
	
	/**
	 * 
	* method Name:showRefetchInfoDialog    
	* method Description:  
	* @param errorNo   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	protected void showRefetchInfoDialog(int errorNo)
	{
		mSlotSorryDialog = null;
		mSlotSorryDialog = new SlotSorryDialog(SlotMachineActivity.this, 
				new SlotSorryDialog.OnClickListener()
				{

					@Override
					public void onDialogClick(int nButtonId) 
					{
						if (nButtonId == SlotSorryDialog.BUTTON_POSITIVE)
						{
							fetchRollInfo();
						}
						else if (nButtonId == SlotSorryDialog.BUTTON_NEGATIVE)
						{
							finish();
						}
						
					}
				}
		);
		
		if(errorNo==TOO_QUICK_ERRNO)
		{
			mSlotSorryDialog.setProperty(R.string.request_too_often,R.string.try_again,
					R.string.left_away);
		}
		else
			mSlotSorryDialog.setProperty(R.string.sorry_fail,R.string.try_again,
					R.string.left_away);
		
		mSlotSorryDialog.show();
	}
	
	/**
	 * 
	 */
	protected void showCoin2CouponFailDialog()
	{
		mSlotSorryDialog = null;
		mSlotSorryDialog = new SlotSorryDialog(SlotMachineActivity.this, new SlotSorryDialog.OnClickListener() {
			
			@Override
			public void onDialogClick(int nButtonId) {
				mSlotSorryDialog.dismiss();
				onResume();
				
			}
		} );
		mSlotSorryDialog.setProperty(R.string.coin_inspire_fail, 0, R.string.i_know);
		mSlotSorryDialog.show();
		
		this.onPause();
		
		
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
		if(null == mSlotLoginDialog)
		{
			mSlotLoginDialog = new SlotLoginDialog(SlotMachineActivity.this, 
				new SlotLoginDialog.OnClickListener()
				{

					@Override
					public void onDialogClick(int nButtonId) 
					{
						if (nButtonId == SlotLoginDialog.BUTTON_POSITIVE)
						{
							ToolUtil.startActivity(SlotMachineActivity.this, 
									LoginActivity.class, null, GO_LOGIN);
						}
						else if (nButtonId == SlotLoginDialog.BUTTON_NEGATIVE)
						{
							finish();
						}
						
					}
				}
				);
		
			if(TextUtils.isEmpty(mLoginHint))
			{
				mLoginHint = this.getString(R.string.slot_login_info);
			}
		
			mSlotLoginDialog.setProperty(getString(R.string.slot_not_login),
				mLoginHint,
				R.string.slot_login_now,
				R.string.left_away);
		}
		mSlotLoginDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(mRolling)
			return;
		
		int showSlotGuide = Preference.getInstance().getUserGuideOfIndex(
				Preference.USER_GUIDE_SLOT);
		if (showSlotGuide > 0 && mModels.size() > 1) {
			Preference.getInstance().setUserGuideOfIndex(Preference.USER_GUIDE_SLOT, 0);
			if(null == mUserGuideDialog)
			{
				mUserGuideDialog = new UserGuideDialog(
					SlotMachineActivity.this, new UserGuideDialog.OnClickListener() {

						@Override
						public void onDialogClick(UserGuideDialog dialog1, int nButtonId1) {
							ModuleInfo info = null;
							if (UserGuideDialog.BUTTON_LIST_1 == nButtonId1) {
								info = mModels.get(1);
							} else if (UserGuideDialog.BUTTON_LIST_2 == nButtonId1) {
								info = mModels.get(2);
							} else if (UserGuideDialog.BUTTON_LIST_3 == nButtonId1) {
								info = mModels.get(3);
							}
							
							SlotMachineActivity.this.handleEvent(info);
						}
					}, UserGuideDialog.LAYOUT_SLOT);
			}
			
			mUserGuideDialog.show();
			
			for (int i = 0; i < mModels.size(); i++) {
				ModuleInfo info = mModels.get(i);
				if (i == 0) {
					mUserGuideDialog.setUrlForButton(info.mPicUrl, UserGuideDialog.BUTTON_LIST_BASE);
				} else if (i == 1) {
					mUserGuideDialog.setUrlForButton(info.mPicUrl, UserGuideDialog.BUTTON_LIST_1);
				} else if (i == 2) {
					mUserGuideDialog.setUrlForButton(info.mPicUrl, UserGuideDialog.BUTTON_LIST_2);
				} else if (i == 3) {
					mUserGuideDialog.setUrlForButton(info.mPicUrl, UserGuideDialog.BUTTON_LIST_3);
				}
			}
			
		} else {
			super.onBackPressed();
		}
	}

	private void initUserGuideInfo() {
		int showSlotGuide = Preference.getInstance().getUserGuideOfIndex(
				Preference.USER_GUIDE_SLOT);
		if (showSlotGuide > 0) 
		{
			Ajax ajax = ServiceConfig.getAjax(Config.URL_GUIDE_PLANIMG);
			if (null == ajax)
				return;
			ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
					final int errno = v.optInt("errno", -1);
					if (errno == 0) {
						try {
							JSONArray items = v.optJSONArray("data");
							if(null == mModels) {
								mModels = new ArrayList<ModuleInfo>();
							}
							mModels.removeAll(null);
							final int size = (null != items ? items.length()
									: 0);
							for (int i = 0; i < size; i++) {
								ModuleInfo node = ModuleInfo.fromJson(items
										.getJSONObject(i));
								mModels.add(node);
							}
						} catch (Exception ex) {
						}
					}
				}
			});
			addAjax(ajax);
			ajax.send();
		} else {
			if(null != mModels) {
				mModels.removeAll(null);
			}
		}
	}


	private void handleEvent(Object aObject) {
		if (null == aObject)
			return;

		final String strPageId = this.getString(R.string.tag_Home);
		ModuleInfo pInfo = (ModuleInfo) aObject;

		switch (pInfo.mModule) {
		case ModuleInfo.MODULE_ID_VPAY:
		case ModuleInfo.MODULE_ID_RECHARGE:
		case ModuleInfo.MODULE_ID_QR_RECHARGE:
			UiUtils.startActivity(this, VirtualPayActivity.class, true);
			break;

		case ModuleInfo.MODULE_ID_INNER_LINK:
			if (!TextUtils.isEmpty(pInfo.mLinkUrl)) {
				String strTitle = URLDecoder.decode(HomeActivity.queryVal(pInfo.mLinkUrl,
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
				String strTag = HomeActivity.queryVal(pInfo.mLinkUrl,
						AppStorage.KEY_WAP_BACK);
				AppStorage.setData(AppStorage.SCOPE_WAP,
						AppStorage.KEY_WAP_BACK, strTag, false);

				UiUtils.startActivity(this, HTML5LinkActivity.class, bundle,
						true);
			}
			break;

		case ModuleInfo.MODULE_ID_COUPON:
			UiUtils.startActivity(this, CouponShowActivity.class, true);
			break;

		case ModuleInfo.MODULE_ID_TUANGOU:
			HomeActivity.startYiQiang(this, HomeActivity.mTimeBuyType,
					YiQiangActivity.PARAM_TAB_TUAN);
			break;

		case ModuleInfo.MODULE_ID_QIANG:
			HomeActivity.startYiQiang(this, HomeActivity.mTimeBuyType,
					YiQiangActivity.PARAM_TAB_QIANG);
			break;

		case ModuleInfo.MODULE_ID_MORNING:
			HomeActivity.startYiQiang(this, ModuleInfo.MODULE_ID_MORNING, YiQiangActivity.PARAM_TAB_TIMEBUY);
			break;

		case ModuleInfo.MODULE_ID_BLACK:
			HomeActivity.startYiQiang(this, ModuleInfo.MODULE_ID_BLACK, YiQiangActivity.PARAM_TAB_TIMEBUY);
			break;

		case ModuleInfo.MODULE_ID_WEEKEND:
			HomeActivity.startYiQiang(this, ModuleInfo.MODULE_ID_WEEKEND, YiQiangActivity.PARAM_TAB_TIMEBUY);
			break;

		case ModuleInfo.MODULE_ID_POPULAR:
			// 热销榜
			UiUtils.startActivity(this, HotlistActivity.class, true);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v)
	{
		if(mRolling)
			return;
		
		if(v == mBackBtn)
			onBackPressed();	
		else if(v == mSlotRuleBtn)
		{
			Bundle gjp = new Bundle();
			gjp.putString(HTML5LinkActivity.LINK_URL, "http://m.51buy.com/event_static/oneprod_156.html?eventid=156");
			gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, "天天摇规则");
			ToolUtil.startActivity(SlotMachineActivity.this, HTML5LinkActivity.class, gjp);
		}
		else if(v == slotBar)
		{
			rollSlotOnce();
		}
		else
			super.onClick(v);
	}
	
	
	private void checkRewardRight()
	{
		Ajax checkRewardAjax = ServiceConfig.getAjax(Config.URL_MB_ROLL_SHARE);
		if(null == checkRewardAjax)
		{
			AppUtils.showWXShareReward(this,false);
			return;
		}
			
		checkRewardAjax.setData("uid", ILogin.getLoginUid());
		checkRewardAjax.setData("did", StatisticsUtils.getDeviceUid(this));
			
			
		checkRewardAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>()
				{

				@Override
				public void onSuccess(JSONObject v, Response response) {
					//bShareGetReward = false;
					final int errno = null != v ? v.optInt("errno", -1) : -1;
					if(errno!=0)
					{
						AppUtils.showWXShareReward(SlotMachineActivity.this,false);
						return;
					}
					else
					{
						remain_chance += REWARD_CHANCE;
						showCountDown();
						AppUtils.showWXShareReward(SlotMachineActivity.this,true);
					}
						
				}});
			
		checkRewardAjax.setOnErrorListener(new OnErrorListener(){

				@Override
				public void onError(Ajax ajax, Response response) {
					AppUtils.showWXShareReward(SlotMachineActivity.this,false);
				}});
			
		addAjax(checkRewardAjax);
		checkRewardAjax.send();
	}
	
	
	@Override
	public void onDialogDismiss() {
		coupon_card.setVisibility(View.INVISIBLE);
		coupon_shadow.setVisibility(View.INVISIBLE);
		//reset cover
		cargo_cover.layout(0, 0, cargo_cover.getRight(), cargo_cover.getHeight());
		
		mRolling = false;
		
		
		bingoType = -1;
		step = -1;
		mHandler.postDelayed(slotAnimation, 500);
		
		mFirstingCoin = false;
		showBingoUsers();
		
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_SlotMachineActivity);
	}
}
