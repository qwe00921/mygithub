package com.tencent.djcity.discover;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;


public class ShakeActivity extends BaseActivity implements  SensorEventListener, OnSuccessListener<JSONObject>{
	
	private SensorManager mSensorManager;
	private Vibrator mVibrator;
	private float    mTempShakeRange;
	private float    mMaxShakeRange;
	private float    mlastMax;
	private int      mChanceLeft;
	private int      mShakeTryCount;
	private int      mShakeSecond = 1;
	
	private static final float    Sensor_Threadhold = 0.7f;
	
	private Boolean   mRequesting = false;
	
	private GameInfo mGameinfo;
	private ArrayList<ProductModel> mShakeGiftList;
	private ProductModel mShakeGift;
	private long       mShakeGiftPrice;
	
	private static final int    REQ_INFO  = 1;
	private static final int    REQ_SHAKE = 2;
	private TextView    mChanceHintText;
	private RelativeLayout mBingoLayout;
	private ImageView      mProImgView;
	private TextView       mProTitleView;
	private TextView       mProPriceView;
	private TextView       mBuyBtn;
	private ScaleAnimation BingoInAnim;
	private ScaleAnimation BingoOutAnim;
	private ImageView      mBingoCloseBtn;
	private ImageLoader mAsyncImageLoader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake);
		loadNavBar(R.id.discover_navbar);
		
		mChanceHintText = (TextView)findViewById(R.id.info_text);
		mChanceHintText.setText(this.getString(R.string.shake_welcome));
		
		findViewById(R.id.info_btn).setVisibility(View.INVISIBLE);
		mBingoLayout = (RelativeLayout) this.findViewById(R.id.bingo_layout);
		mBingoLayout.setVisibility(View.INVISIBLE);
		mBingoCloseBtn = (ImageView) this.findViewById(R.id.close_bingo);
		mBingoCloseBtn.setVisibility(View.INVISIBLE);
		mBingoCloseBtn.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					hideBingo();
					return true;
				}
				else// TODO Auto-generated method stub
					return false;
			}});
		
		mProImgView = (ImageView) this.findViewById(R.id.pro_img);
		mProTitleView = (TextView) this.findViewById(R.id.title);
		mProPriceView = (TextView) this.findViewById(R.id.price);
		mBuyBtn = (TextView) this.findViewById(R.id.shake_buy_now);
		mBuyBtn.setVisibility(View.INVISIBLE);
		mBuyBtn.setOnClickListener(this);
		
		mTempShakeRange = Preference.getInstance().getShakeRange();
		mMaxShakeRange = mTempShakeRange;
		
		mGameinfo =  GameInfo.getGameInfoFromPreference();
		if(mGameinfo==null)
			finish();
		
		mChanceLeft = -1;
		
		initSensor();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		fetchShakeCount();
	}
	
	@Override
	public void onClick(View v)
	{
		if(v.getId() == R.id.shake_buy_now)
		{
			Bundle abundle = new Bundle();
			abundle.putString(ItemActivity.KEY_PROP_ID, this.mShakeGift.getPropId());
			UiUtils.startActivity(this, ItemActivity.class, abundle, true);
		}
		else
			super.onClick(v);
	}
	
	private void showBingo() {
		
		if(null == BingoInAnim)
		{
			BingoInAnim = new  ScaleAnimation(0f,1f, 0f,1f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
			BingoInAnim.setDuration(1200); 
			BingoInAnim.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mBingoLayout.setVisibility(View.VISIBLE);
					mBuyBtn.setVisibility(View.VISIBLE);
					mBingoCloseBtn.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}});
		}
		mBingoLayout.setAnimation(BingoInAnim);
		BingoInAnim.start();
		
		
		
		this.loadImage(mProImgView, mShakeGift.getPropImg());
		mProTitleView.setText(mShakeGift.getPropName());
		mProPriceView.setText(""+this.mShakeGiftPrice);
	}
	
	private void hideBingo() {
		
		/*if(null == BingoOutAnim)
		{
			BingoOutAnim = new  ScaleAnimation(1f,0f, 1f,0f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
			BingoOutAnim.setDuration(100); 
			BingoOutAnim.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mBingoLayout.setVisibility(View.INVISIBLE);
					mBuyBtn.setVisibility(View.INVISIBLE);
					mBingoCloseBtn.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}});
		}
		
		mBingoLayout.setAnimation(BingoOutAnim);
		BingoOutAnim.start();
		*/
		mBingoLayout.setVisibility(View.INVISIBLE);
		mBuyBtn.setVisibility(View.INVISIBLE);
		mBingoCloseBtn.setVisibility(View.INVISIBLE);
		mShakeGift = null;
	}
	
	private void loadImage(final ImageView view, String url) {
		final Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mAsyncImageLoader.getLoadingId());
		view.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(this));
		mAsyncImageLoader.get(url, new ImageLoadListener() {
			
			@Override
			public void onLoaded(Bitmap aBitmap, String strUrl) {
				view.setImageBitmap(aBitmap);
			}
			
			@Override
			public void onError(String strUrl) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void fetchShakeCount() {
		showLoadingLayer();
		Account act = ILogin.getActiveAccount();
		if(null == act)
		{
			UiUtils.makeToast(this, "Logout");
			return;
		}
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getLimit.php?type=shake&biz=" + mGameinfo.getBizCode() +
				"&uin=" + act.getUin());
		ajx.setId(REQ_INFO);
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		this.addAjax(ajx);
		
	}

	private void shakeOnce() {
		if(mChanceLeft <= 0)
		{
			UiUtils.makeToast(this, "No chance left");
			
			return;
		}
		mChanceLeft --;
		mChanceHintText.setText(getString(R.string.shake_count, mGameinfo.getBizName(),mChanceLeft));
		
		showLoadingLayer();
		Account act = ILogin.getActiveAccount();
		if(null == act)
		{
			UiUtils.makeToast(this, "Logout");
			return;
		}
		mRequesting = true;	
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getShake.php?biz=" + mGameinfo.getBizCode() +
				"&uin=" + act.getUin());
		ajx.setId(REQ_SHAKE);
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		
		this.addAjax(ajx);
		
		
	}
	
	@Override
	protected void onDestroy()
	{
		if(null!= mSensorManager)
			mSensorManager.unregisterListener(this);
		mSensorManager = null;
		
		super.onDestroy();
	}
	
	
	private void initSensor() {
		//Sensor
		mSensorManager = (SensorManager)getApplicationContext().getSystemService(SENSOR_SERVICE);
        //震动
        mVibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        
        if(null!=mSensorManager)
			mSensorManager.registerListener(this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_UI); 
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//set default
		if(mMaxShakeRange < 4)
			mMaxShakeRange = event.sensor.getMaximumRange();
				
		int sensorType = event.sensor.getType();
				
		float[] values = event.values;
				
		if(sensorType == Sensor.TYPE_ACCELEROMETER)
		{
			//alwasy x
			float max = values[SensorManager.DATA_X];
			if(mlastMax > 4  && max - mlastMax > 4.5)
			{
				saveShakeRangeIfNecessary(max);
			}
					
			mlastMax = max;
					
					
			if(max > (mMaxShakeRange*Sensor_Threadhold) && !mRequesting && 
					(mBingoLayout.getVisibility() == View.INVISIBLE))
			{
				saveShakeRangeIfNecessary(max);
				if((mShakeSecond % 2) == 0)
				{
					mVibrator.vibrate(500);
					shakeOnce();
				}
				mShakeSecond++;
			}
		}
	}

	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void onSuccess(JSONObject v, Response response) {
		closeLoadingLayer();
		if(response.getId() == REQ_INFO)
		{
			final int ret = v.optInt("ret",-1);
			if(ret!=0)
			{
				UiUtils.makeToast(this, v.optString("msg"));
				return;
			}
			
			JSONObject data =  v.optJSONObject("data");
			if(null!=data)
			{
				mChanceLeft = data.optInt("left",-1);
			}
			mChanceHintText.setText(getString(R.string.shake_count, mGameinfo.getBizName(),mChanceLeft));
		}
		else if(response.getId() == REQ_SHAKE)
		{
			mRequesting = false;
			
			final int ret = v.optInt("ret",-1);
			if(ret!=0)
			{
				UiUtils.makeToast(this, v.optString("msg"));
				return;
			}
			if(null == mAsyncImageLoader)
				mAsyncImageLoader = new ImageLoader(ShakeActivity.this,true);
			JSONArray array =  v.optJSONArray("list");
			mShakeGiftList = new ArrayList<ProductModel>();
			for(int i = 0 ;i < array.length(); i++)
			{
				JSONObject item;
				try {
					item = array.getJSONObject(i);
					mShakeGift =  new ProductModel();
					mShakeGift.setPropId(item.optString("iGoodsId"));
					mShakeGift.setPropImg(item.optString("sGoodsPic"));
					mShakeGift.setPropName(item.optString("sGoodsName"));
					mShakeGift.setPropName(item.optString("sGoodsName"));
					mShakeGiftPrice = item.optInt("iPrice");
					
					showBingo();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
	}

	
}
