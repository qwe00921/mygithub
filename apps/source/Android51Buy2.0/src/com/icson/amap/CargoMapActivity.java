/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: CargoMapActivity.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-4-17
 */
package com.icson.amap;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.icson.R;
import com.icson.amap.mapUtils.AMapUtil;
import com.icson.lib.ILogin;
import com.icson.lib.model.DeliverInfoModel;
import com.icson.lib.parser.DeliverInfoParser;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.NavigationBar;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.more.AdviseActivity;
import com.icson.my.orderdetail.OrderDetailActivity;
import com.icson.preference.Preference;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.AppUtils.Sharable;
import com.icson.util.AppUtils.SharableAdapter;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;

/**  
 *   
 * Class Name:CargoMapActivity 
 * Class Description: 
 * Author: xingyao 
 * Modify: xingyao 
 * Modify Date: 2013-4-17 上午11:09:02 
 * Modify Remarks: 
 * @version 1.0.0
 *   
 */
public class CargoMapActivity extends BaseActivity implements
		OnMarkerClickListener, OnInfoWindowClickListener,
		//LocationSource, AMapLocationListener,  //Location no use
		ImageLoadListener, OnMapClickListener,OnCameraChangeListener, OnGeocodeSearchListener, OnMapLoadedListener{
	
	private static final String RECEIVER_NAME = "map_receiver_name";
	private static final String RECEIVER_TEL  = "map_receiver_tel";
	private static final String RECEIVER_ADDR = "map_receiver_addr";
	private static final String ORDER_ID      = "map_order_id";
	
	private AMap aMap;
	private MapView mapView;
	private static final float WIDTH_RATE = 0.6f;
	private static final float HEIGHT_RATE = 0.4f;
	//Location no use
	//private OnLocationChangedListener mListener;
	//private LocationManagerProxy mAMapLocationManager;
	private CircleOptions  mDistanceCircleOpt;
	private static final int  MARKER_SHIFT_TV = 100;
	public static final double HINT_DISTANCE = 1500;
	
	private static final float MAP_CITY_ZOOM = 11.0f; // 1000m <-->14.0D; range-50
	private static final float MAP_MAX_ZOOM = 20.0f; // 1000m <-->14.0D; range-50
	private static final float SHOW_RANGE_HINT_ZOOM = 13.0f; // 1000m <-->14.0D; range-50
															// 5000m <-->11.5D; range-150
	private double           mCurDistance;
	private int            mCircleCoverColor;
	private Circle         mDisCircle;
	
	//3 Marker
	private Marker destMarker;
	//private Marker startMarker;
	private Marker currentMarker1;
	private Marker currentMarker2;
	private Marker currentMarker3;
	private Marker currentMarker4;
	private Marker currentMarker0;
	final private int    currentCount = 6;
	private int    currentState = 0;
	
	private Marker eastMarker;
	private Marker westMarker;
	private Marker northMarker;
	private Marker southMarker;
	//Cargo 
	private Ajax     mDeliverAjax;
	private DeliverInfoModel mDeliverInfo;
	private DeliverInfoParser mDeliverParser;
	
	private ImageLoader mHeadImgLoader;
	
	private CustomInfoWindowAdapter mPopInfoAdapter;
	
	private LinearLayout   hintLayout; 
	private TextView       distancInfoView;
	private TextView       distancHintView;
	private TextView       hintFootView;
	private NavigationBar  mNavBar;
	
	//Receiver Info
	private String         mOrderId;
	private String         mReceiverAddr;
	private String         mReceiverName;
	private String         mReceiverTel;
	
	private String         mCallNumber;
	private float          mZoomLevel = CargoMapActivity.MAP_CITY_ZOOM;
	private LatLng         mCenter;
	
	private final static int  ROLE_DELIVER = 0;
	private final static int  ROLE_RECEIVER = 1;
	
	private View   mLoadingV;
	
	//correct amap about
	private GeocodeSearch  mDeocodeSearcher;
	private ImageView     mCorrectIcon;
	private TextView      mCorrectText;
	private RelativeLayout mCorrectLayout;
	private OnClickListener  mCorrectBtnListener;
	private RadioDialog    mCorrectChoiceDialog;
	private RegeocodeQuery mQuery;
	private LatLonPoint    mQueryPoint;
	private Ajax           mCorrectAjax;
	private LatLng         mNewDestLatLng;
	private LatLng         mLastDestLatLng;
	private TextView       mRedressHint;
	private ImageView      mArrowLeft;
	private AppDialog mCallDialog;
	private AppDialog mHintDialog;
	/**
	 * Show map
	 * @param aFrom
	 * @param strRecvName
	 * @param strTel
	 * @param strAddr
	 * @param strOrderCharId
	 */
	public static void showMap(final Activity aFrom, final String strRecvName, final String strTel, final String strAddr, final String strOrderCharId) {
		
		if(Preference.getInstance().needToMapAccess())
		{
			UiUtils.showDialog(aFrom,
					R.string.permission_title, R.string.permission_hint_cargomap,R.string.permission_agree, R.string.permission_disagree,
					new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if (nButtonId == AppDialog.BUTTON_POSITIVE)
						{
							Preference.getInstance().setMapAccess(Preference.ACCESSED);
							showMapDetail(aFrom,strRecvName,strTel,strAddr,strOrderCharId);
						}
					}
				});
		}
		else
		{
			showMapDetail(aFrom,strRecvName,strTel,strAddr,strOrderCharId);
		}
		
	}
	
	protected static void showMapDetail(Activity aFrom, String strRecvName,
			String strTel, String strAddr, String strOrderCharId)
	{
			Bundle pExtras = new Bundle();
			pExtras.putString(CargoMapActivity.RECEIVER_NAME, strRecvName);
			pExtras.putString(CargoMapActivity.RECEIVER_TEL, strTel);
			pExtras.putString(CargoMapActivity.RECEIVER_ADDR, strAddr);
			pExtras.putString(CargoMapActivity.ORDER_ID, strOrderCharId);
			ToolUtil.startActivity(aFrom, CargoMapActivity.class, pExtras);
			
			// Report click information.
			Date pData = new Date(System.currentTimeMillis());
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String strTime = format.format(pData);
			String strInfo = "uid=" + ILogin.getLoginUid() + "&orderId=" + strOrderCharId + "&time=" + strTime;
			
			if(aFrom instanceof OrderDetailActivity){
				ToolUtil.sendTrack(aFrom.getClass().getName(), aFrom.getString(R.string.tag_OrderDetailActivity), CargoMapActivity.class.getName(), aFrom.getString(R.string.tag_CargoMapActivity), "50001");
			}else{
				ToolUtil.sendTrack(aFrom.getClass().getName(), aFrom.getString(R.string.tag_MyIcsonActivity), CargoMapActivity.class.getName(), aFrom.getString(R.string.tag_CargoMapActivity), "50001");
			}
			StatisticsEngine.trackEvent(aFrom, "orderflow_map", strInfo);
	}

	/**
	 * 
	*   
	* Class Name:HintViewHolder 
	* Class Description: 
	* Author: xingyao 
	* Modify: xingyao 
	* Modify Date: 2013-5-6 下午06:13:52 
	* Modify Remarks: 
	* @version 1.0.0
	*
	 */
	private class HintViewHolder
	{
		ImageView aImgView;
		TextView phoneTitleV;
		
		TextView nameTv;
		TextView phoneTv;
		TextView dynamicTv;
	}
	
	
	/**
	 * 
	*   
	* Class Name:CustomInfoWindowAdapter 
	* Class Description: 
	* Author: xingyao 
	* Modify: xingyao 
	* Modify Date: 2013-5-6 下午06:13:47 
	* Modify Remarks: 
	* @version 1.0.0
	*
	 */
	private class CustomInfoWindowAdapter implements InfoWindowAdapter {
		private final View mWindow;
		private HintViewHolder mVHolder;
		CustomInfoWindowAdapter() {
			mWindow = getLayoutInflater().inflate(R.layout.amap_info_window,null);
			mVHolder = null;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			render(marker, mWindow);
			return mWindow;
		}

		@Override
		public View getInfoContents(Marker marker) {
			return null;
		}

		private void render(Marker marker, View view) {
			if(mVHolder == null)
			{
				mVHolder = new HintViewHolder();
				mVHolder.aImgView = ((ImageView) view.findViewById(R.id.headimg));
				mVHolder.phoneTitleV = ((TextView) view.findViewById(R.id.phone_title));
				
				mVHolder.nameTv = ((TextView) view.findViewById(R.id.name));
				mVHolder.phoneTv = ((TextView) view.findViewById(R.id.phone));
				mVHolder.dynamicTv = ((TextView) view.findViewById(R.id.dynamic_info));
			}
			
			if(marker.equals(currentMarker0))
			{
				mVHolder.aImgView.setVisibility(View.VISIBLE);
				//test
				loadImage(mVHolder.aImgView, mDeliverInfo.getImgUrl());//http://f.520qq.com/qqtouxiang/20120112/81a14063d1285bc129ec74e817060ea3.jpg");
				//loadImage(aImgView, mDeliverInfo.getImgUrl());
				
				mVHolder.phoneTitleV.setVisibility(View.VISIBLE);
				mVHolder.nameTv.setText(mDeliverInfo.getName());
				mVHolder.phoneTv.setText(Html.fromHtml("<u>"+mDeliverInfo.getPhoneNo()+"</u>"));//下划线
				String strSlogan = mDeliverInfo.getSlogan();
				if(TextUtils.isEmpty(strSlogan))
				{
					strSlogan = getString(R.string.default_slogan);
				}
				mVHolder.dynamicTv.setText(strSlogan);
			}
			else if(marker.equals(destMarker))//destination
			{
				mVHolder.aImgView.setVisibility(View.GONE);
				mVHolder.phoneTitleV.setVisibility(View.GONE);
				
				//mOrder.getReceiver()
				mVHolder.nameTv.setText(mReceiverName);
				//mReceiverTel
				mVHolder.phoneTv.setText(Html.fromHtml("<u>"+mReceiverTel+"</u>"));//下划线
				//mOrder.getReceiverAddress();
				mVHolder.dynamicTv.setText(mReceiverAddr);
				//mVHolder.dynamicTv.setText(mDeliverInfo.getDestAddressName());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mOrderId =  getIntent().getStringExtra(CargoMapActivity.ORDER_ID);
		mReceiverName = getIntent().getStringExtra(CargoMapActivity.RECEIVER_NAME);
		mReceiverAddr = getIntent().getStringExtra(CargoMapActivity.RECEIVER_ADDR);
		mReceiverTel = getIntent().getStringExtra(CargoMapActivity.RECEIVER_TEL);
		
		mHeadImgLoader = new ImageLoader(this, true);
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_amap);
		
		mapView = (MapView) this.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		
		// Load navigation bar.
		mNavBar = (NavigationBar)findViewById(R.id.amap_navbar);
		mNavBar.setText(R.string.amap_current_location);
		mNavBar.setOnNavBackListener(new NavigationBar.OnNavBackListener() {
			@Override
			public void onNavBackClick() {
				ToolUtil.reportStatisticsClick(getActivityPageId(), "19999");
				onBackPressed();
			}
		});
		
		mCorrectLayout = (RelativeLayout)this.findViewById(R.id.center_address_layout);
		mCorrectLayout.setVisibility(View.INVISIBLE);
		
		hintLayout = (LinearLayout) this.findViewById(R.id.distance_hint_layout);
		
		distancHintView = (TextView)this.findViewById(R.id.distance_hint);
		distancInfoView = (TextView)this.findViewById(R.id.distance_info);
		hintFootView = (TextView)this.findViewById(R.id.hint_footer);
		hintLayout.setVisibility(View.INVISIBLE);
		
		fetchDeliverInfo();
		
		mNewDestLatLng = null;
		initMap();
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mapView.onResume();
	}
	@Override 
	protected void onDestroy()
	{
		mapView.onDestroy();
		
		if( null != mHeadImgLoader )
		{
			mHeadImgLoader.cleanup();
			mHeadImgLoader = null;
		}
		mDeocodeSearcher = null;
		//maybe will send for a while
		if(null!= mCorrectAjax)
		{
			mCorrectAjax.abort();
			mCorrectAjax = null;
		}
		mCallDialog = null;
		mCorrectChoiceDialog = null;
		mHintDialog = null;
		
		CargoRedressCache.cleanUp();
		if(null!=mHandler)
			mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		mapView.onPause();
		
		if(null!= mDeliverAjax)
		{
			mDeliverAjax.abort();
			mDeliverAjax = null;
		}
		if(null!=mCorrectChoiceDialog && mCorrectChoiceDialog.isShowing())
		{
			mCorrectChoiceDialog.dismiss();
		}
		
		if(null!=mCallDialog && mCallDialog.isShowing())
		{
			mCallDialog.dismiss();
		}
		if(null!=mHintDialog && mHintDialog.isShowing())
		{
			mHintDialog.dismiss();
		}
		
		super.onPause();
	}
	/**
	 * 初始化AMap对象
	 */
	private void initMap() {
		mCenter = Preference.getInstance().getMapLastCenter();
		if (aMap == null) {
			aMap = mapView.getMap();
			
			if (AMapUtil.checkReady(this, aMap)) {
				if(mCenter!=null)
					aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
							mCenter,mZoomLevel));
				
				aMap.setOnMapLoadedListener(this);
				aMap.getUiSettings().setZoomControlsEnabled(true);// 设置系统默认缩放按钮可见
				aMap.setOnMarkerClickListener(this);// 对marker添加点击监听器
				aMap.setOnInfoWindowClickListener(this);
				
				aMap.setOnMapClickListener(this);
				aMap.setOnCameraChangeListener(this);
				//location no use
				//aMap.setLocationSource(this);
				//aMap.setMyLocationEnabled(true);
				
				
				mCircleCoverColor = this.getResources().getColor(R.color.range_circle); 
					//Color.HSVToColor(20, new float[] {
					//	0,1,0 });
			}
		}
	}

	/**
	 * 
	* method Name:pinPostions    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void rendDeliverView()
	{
		if(null == mDeliverInfo)
			return;
		
		if(null == mCorrectBtnListener && mDeliverInfo.mCheckDistance >= 0)
		{
			mCorrectBtnListener = new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(null!=mRedressHint && mRedressHint.getVisibility() == View.VISIBLE)
						mRedressHint.setVisibility(View.GONE);
					if(mCorrectLayout.getVisibility() != View.VISIBLE)
					{
						showCorrectChoiceDialog();
					}else
					{
						submitCorrectPos();
					}
				}
			};
			mNavBar.setRightText(getString(R.string.correct_amap_pos));
			mNavBar.setOnDrawableRightClickListener(mCorrectBtnListener);
		}
		boolean changeFlag = false;
		
		LatLng aEnd = getCorrectEndPos();
		LatLng aCur = mDeliverInfo.getCurPos();
		
		//pinDest if changed
		if(null==destMarker || !(destMarker.getPosition()).equals(aEnd))
		{
			changeFlag = true;
			if(null!=destMarker)
			{
				destMarker.remove();
				destMarker.destroy();
			}
			destMarker = aMap.addMarker(new MarkerOptions()
				.position(aEnd).snippet(mReceiverTel)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_pos))
				.anchor(0.5f, 1f));
		}
		
		//pinCur if changed
		if(null==currentMarker0 || !(currentMarker0.getPosition()).equals(aCur))
		{
			changeFlag = true;
			if(null!=currentMarker1)
			{
				currentMarker1.remove();
				currentMarker1.destroy();
			}
			if(null!=currentMarker2)
			{
				currentMarker2.remove();
				currentMarker2.destroy();
			}
			if(null!=currentMarker3)
			{
				currentMarker3.remove();
				currentMarker3.destroy();
			}
			if(null!=currentMarker4)
			{
				currentMarker4.remove();
				currentMarker4.destroy();
			}
			if(null!=currentMarker0)
			{
				currentMarker0.remove();
				currentMarker0.destroy();
			}
			currentMarker1 = aMap.addMarker(new MarkerOptions()
				.position(aCur).anchor(0.5f, 0.9f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_1)));
				
			currentMarker2 = aMap.addMarker(new MarkerOptions()
					.position(aCur).anchor(0.5f, 0.9f)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_2))
					.visible(false));
		
			currentMarker3 = aMap.addMarker(new MarkerOptions()
				.position(aCur).anchor(0.5f, 0.9f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_3))
				.visible(false));
			
			currentMarker4 = aMap.addMarker(new MarkerOptions()
				.position(aCur).anchor(0.5f, 0.9f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_4))
				.visible(false));
				
			currentMarker0= aMap.addMarker(new MarkerOptions()
				.position(aCur).snippet(mDeliverInfo.getPhoneNo()).anchor(0.5f, 0.9f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.cur_pos_0)));
			
			if(mHandler == null)
			{
				mHandler = new Handler();
			}
			mHandler.removeCallbacksAndMessages(null);
			mHandler.postDelayed(rawAnimation, MARKER_SHIFT_TV);
				
		}
			
		if(changeFlag)	
		{
			//reset center
			if(mCenter != null)
				mCenter = null;
			mCenter = new LatLng((aEnd.latitude + aCur.latitude)/2.0,
					(aEnd.longitude + aCur.longitude)/2.0);
			Preference.getInstance().setMapLastCenter(mCenter);
		
			caculateZoom(aEnd,aCur);
		
			aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				mCenter,mZoomLevel));
		
			//circle and info
			drawDistanceCircle(aEnd,aCur);
		}
			
	}
	
	/**
	 * 
	 * @return
	 */
	private LatLng getCorrectEndPos() {
		if(null!=mNewDestLatLng)
			return mNewDestLatLng;
		else if(null!=mLastDestLatLng)
			return mLastDestLatLng;
		else if(null!=mDeliverInfo)
			return 	mDeliverInfo.getDestPos();
		return null;
	}
			
	
	private void pinRangeHints(LatLng aEnd,double aRange)
	{
		double deltaLng = AMapUtil.getDeltaLngWithSameLat(aEnd, aRange-120);
		
		if(null!=eastMarker)
		{
			eastMarker.remove();
			eastMarker.destroy();
		}
		
		// east latitude same;  longitude +
		eastMarker = aMap.addMarker(new MarkerOptions()
			.position(new LatLng(aEnd.latitude,aEnd.longitude + deltaLng))
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.east_hint)).anchor(1f, 0.5f));
		
		if(null!=westMarker)
		{
			westMarker.remove();
			westMarker.destroy();
		}
		//deltaLng = AMapUtil.getDeltaLngWithSameLat(aEnd, aRange-500);
		// west latitude same;  longitude  -
		westMarker = aMap.addMarker(new MarkerOptions()
			.position(new LatLng(aEnd.latitude,aEnd.longitude - deltaLng))
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.west_hint)).anchor(0, 0.5f));
	
		double deltaLat = AMapUtil.getDeltaLatWithSameLng(aEnd, aRange-120);
		
		if(null!=northMarker)
		{
			northMarker.remove();
			northMarker.destroy();
		}
		//north longitude same;latitude +
		northMarker = aMap.addMarker(new MarkerOptions()
			.position(new LatLng(aEnd.latitude + deltaLat ,aEnd.longitude))
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.north_hint)).anchor(0.5f, 0.1f));

		if(null!=southMarker)
		{
			southMarker.remove();
			southMarker.destroy();
		}
		//deltaLat = AMapUtil.getDeltaLatWithSameLng(aEnd, aRange-100);
		southMarker = aMap.addMarker(new MarkerOptions()
			.position(new LatLng(aEnd.latitude - deltaLat ,aEnd.longitude))
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.south_hint)));
		
		if(this.mZoomLevel < SHOW_RANGE_HINT_ZOOM)
		{
			eastMarker.setVisible(false);
			westMarker.setVisible(false);
			northMarker.setVisible(false);
			southMarker.setVisible(false);
		}

	}
	

	/**
	 * 
	* method Name:caculateZoom    
	* method Description:  zoom + 1, distance*2. 
	* 						 So	let distance(width,heigth) <= Space/2 (width,height)   
	* @param aEnd
	* @param aCur   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void caculateZoom(LatLng aEnd,LatLng aCur)
	{
		Projection pg = aMap.getProjection();
		Point aEndPoint = pg.toScreenLocation(aEnd);
		Point aCurPoint = pg.toScreenLocation(aCur);
		
		int xd = Math.abs(aEndPoint.x-aCurPoint.x);
		int yd = Math.abs(aEndPoint.y-aCurPoint.y);
		
		int screenWidth = ToolUtil.getEquipmentWidth(IcsonApplication.app);
		int screenHeight = ToolUtil.getEquipmentHeight(IcsonApplication.app);
		float zm = aMap.getCameraPosition().zoom;
		double widthLog = Math.log(screenWidth*WIDTH_RATE/xd) / Math.log(2.0);
		double heightLog = Math.log(screenHeight*HEIGHT_RATE/yd) /Math.log(2.0);
		double x = Math.min(widthLog, heightLog);
		mZoomLevel = (float) (zm + x);
		
		if(mZoomLevel >= CargoMapActivity.MAP_MAX_ZOOM)
		{
			mZoomLevel = CargoMapActivity.MAP_MAX_ZOOM - 1; 
		}
	}
	
	
	/**
	 * 
	* method Name:drawDistanceCircle    
	* method Description:  
	* @param aEnd
	* @param aCur   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void drawDistanceCircle(LatLng aEnd,LatLng aCur)
	{
		//mCurDistance = AMapUtil.getDistance2Poi(aEnd,aCur);
		float[] result = new float[1];
		Location.distanceBetween(aEnd.latitude, aEnd.longitude,
				aCur.latitude, aCur.longitude, result);
		mCurDistance = result[0];
		double dis = CargoMapActivity.HINT_DISTANCE;
		
	//	String strHT = "";
		if(mDisCircle == null)
		{
			if(null == mDistanceCircleOpt)
				mDistanceCircleOpt = new CircleOptions();
			if(mCurDistance > CargoMapActivity.HINT_DISTANCE)
			{
				dis = CargoMapActivity.HINT_DISTANCE;
				mDistanceCircleOpt.strokeWidth(0);
			}
			else
			{
				dis = CargoMapActivity.HINT_DISTANCE;
				//dis = mCurDistance;
				mDistanceCircleOpt.strokeWidth(4);
			}
			
			mDistanceCircleOpt.strokeColor(this.getResources().getColor(R.color.range_stroker));
			mDistanceCircleOpt.fillColor(mCircleCoverColor);
			mDistanceCircleOpt.center(aEnd);
			mDisCircle =  aMap.addCircle(mDistanceCircleOpt);
		}
		else
		{
			mDisCircle.setCenter(aEnd);
		}
		
		
		//always HINT_DISTANCE
		mDisCircle.setRadius(dis);
		
		// 4 hints
		pinRangeHints(aEnd,dis);
		
		//header hint
		distancHintView.setText(getString(R.string.distance_hint_header));
		//distance
		distancInfoView.setText(AMapUtil.getCustomLength((int)mCurDistance));
		
		String strCountdown = null != mDeliverInfo ? mDeliverInfo.getCountdown() : "";
		hintFootView.setText(strCountdown);
		hintFootView.setVisibility(TextUtils.isEmpty(strCountdown) ? View.GONE : View.VISIBLE);
		hintLayout.setVisibility(View.VISIBLE);
	}
	
	public void drawPath()
	{
		
	}
	
	/**
	 * 对marker标注点点击响应事件
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		if(marker.equals(currentMarker0))
		{
			currentMarker0.showInfoWindow();
		}
		else if(marker.equals(currentMarker1))
		{
			currentMarker0.showInfoWindow();
		}
		else if(marker.equals(currentMarker2))
		{
			currentMarker0.showInfoWindow();
		}
		else if(marker.equals(currentMarker3))
		{
			currentMarker0.showInfoWindow();
		}
		else if(marker.equals(currentMarker4))
		{
			currentMarker0.showInfoWindow();
		}
		else if(marker.equals(destMarker))
		{
			//bClickDest= true;
			destMarker.showInfoWindow();
		}


		return false;//marker will be center of map
		//return true 
	}

	/*  
	 * Description:
	 * @see com.amap.api.maps.AMap.OnInfoWindowClickListener#onInfoWindowClick(com.amap.api.maps.model.Marker)
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		mCallNumber = marker.getSnippet();
		if(marker.equals(this.destMarker))
		{
			showCallDialog(CargoMapActivity.ROLE_RECEIVER);
		}
		else if(marker.equals(this.currentMarker0))
			showCallDialog(CargoMapActivity.ROLE_DELIVER);
	}

	/**
	 * 
	* method Name:showCallDialog    
	* method Description:  
	* @param role   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void showCallDialog(int role){
		
		if(null == mCallDialog)
		{
			int info_id = role  == ROLE_DELIVER ? R.string.call_devliver_prefix : R.string.call_receiver_prefix;
			String strMsg = "\n" + CargoMapActivity.this.getString(info_id) + mCallNumber + "\n";
			mCallDialog = UiUtils.showDialog(this, getString(R.string.caption_hint), strMsg, R.string.btn_call, R.string.btn_cancel, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE)
					{
						Uri uri = Uri.parse("tel:" + mCallNumber);
						Intent it = new Intent(Intent.ACTION_DIAL,uri);
						AppUtils.checkAndCall(CargoMapActivity.this,it);
					}
				}
			});
			mCallDialog.setCancelable(true);
			mCallDialog.setCanceledOnTouchOutside(true);
		}else
			mCallDialog.show();
	}
	
	//Location no use
	/*
	@Override
	public void onLocationChanged(Location location) {
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null) {
			//mListener.onLocationChanged(aLocation);
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
		}
		mAMapLocationManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 10, 5000, this);
		
	}
	@Override
	public void deactivate() {
		mListener = null;
		if(null!=mAMapLocationManager)
		{	
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
		
	}
	
	
	@Override
	public void onPause()
	{
		deactivate();
		super.onPause();
		
	}
	
	public void onResume()
	{
		aMap.getUiSettings().setZoomControlsEnabled(true);// 设置系统默认缩放按钮可见
		//aMap.setMyLocationEnabled(true);
		super.onResume();
		
	}
	*/
	
	/**
	 * 
	* method Name:fetchDeliverInfo    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void fetchDeliverInfo()
	{
		showLoading();
		if(null == mDeliverAjax)
		{
			mDeliverAjax = ServiceConfig.getAjax(Config.URL_ORDER_GETTRACE);
			if( null == mDeliverAjax )
				return ;
			
			//Version + Image Parser
			if(null == mDeliverParser)
				mDeliverParser = new DeliverInfoParser();
		
			mDeliverAjax.setParser(mDeliverParser);
			mDeliverAjax.setData("orderId", mOrderId);
		
			mDeliverAjax.setOnSuccessListener(new OnSuccessListener<DeliverInfoModel>()
				{
					@Override
					public void onSuccess(DeliverInfoModel v,
							Response response) {
						
						endLoading();
						
						//has new version
						mDeliverInfo = (DeliverInfoModel)v;
						
						//fail
						if(null==v)
						{
							//fakeOne();
							UiUtils.makeToast(CargoMapActivity.this, R.string.cargo_map_fail,true);
							finish();
							return;
						}
						//cargo redress address--latlng
						CargoRedressCache.init(24);//mDeliverInfo.mExpireHour);
						
						//do once
						if(null == mLastDestLatLng)
							mLastDestLatLng = CargoRedressCache.getRedressLatLng(mDeliverInfo.getDestAddressName());
						rendDeliverView();
						
						
						mPopInfoAdapter = new CustomInfoWindowAdapter();
						aMap.setInfoWindowAdapter(mPopInfoAdapter);
						
						}
						// TODO Auto-generated method stub
				 });
			mDeliverAjax.setTimeout(10);
			mDeliverAjax.setOnErrorListener(new OnErrorListener(){

			@Override
			public void onError(Ajax ajax, Response response) {
				endLoading();
				UiUtils.makeToast(CargoMapActivity.this, R.string.cargo_map_fail,true);
				finish();
				
				
			}});
		}
		mDeliverAjax.send();
	}
	
	/*
	private void fakeOne()
	{
		mDeliverInfo = new DeliverInfoModel();
		mDeliverInfo.setName("吴宝华");
		mDeliverInfo.setPhoneNo("15821802342");
		mDeliverInfo.setOrderId(1288);
		mDeliverInfo.setCurPos("31.19348", "121.391906");
		//mDeliverInfo.setDestPos("31.16348", "121.398006"); //far 11km
		//mDeliverInfo.setDestPos("31.1417", "121.26314");//near
		mDeliverInfo.setDestPos("31.18008", "121.395006");
		mDeliverInfo.mCheckDistance = 5000;
		mOrderId =  "1288";
		mReceiverName = "王小二";
		mReceiverAddr = "中国上海市上海市上海市上海市上海市上海市上海市徐汇区古美路1528号A1座2楼2001";
		mDeliverInfo.setDestAddressName(mReceiverAddr);
		mReceiverTel = "13012666669";
	}
	*/
	/**
	 * 
	* method Name:loadImage    
	* method Description:  
	* @param view
	* @param url   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void loadImage(View view, String url) {
		final Bitmap data = mHeadImgLoader.get(url);
		if (data != null) {
			((ImageView)view).setImageBitmap(data);
			return;
		}
		
		((ImageView)view).setImageResource(R.drawable.avatar);
		
		mHeadImgLoader.get(url, this);
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onLoaded(android.graphics.Bitmap, java.lang.String)
	 */
	@Override
	public void onLoaded(Bitmap aBitmap, String strUrl) {
		if(currentMarker0.isInfoWindowShown())
			currentMarker0.showInfoWindow();
	}

	/*  
	 * Description:
	 * @see com.icson.util.ImageLoadListener#onError(java.lang.String)
	 */
	@Override
	public void onError(String strUrl) {
		
	}

	/*  
	 * Description:
	 * @see com.amap.api.maps.AMap.OnMapClickListener#onMapClick(com.amap.api.maps.model.LatLng)
	 */
	@Override
	public void onMapClick(LatLng aLatLng) {
		if(null!=mRedressHint && mRedressHint.getVisibility() == View.VISIBLE)
			mRedressHint.setVisibility(View.GONE);
		
		hideFourArrows();
		
		if(currentMarker0!=null && 
				currentMarker0.isInfoWindowShown() )
		{
			currentMarker0.hideInfoWindow();
		}
		if(destMarker!=null && destMarker.isInfoWindowShown())
			destMarker.hideInfoWindow();
		if(null!= mDeliverInfo)
			caculateZoom(getCorrectEndPos() ,mDeliverInfo.getCurPos());
	}
	
	
	private Handler mHandler;
	private Runnable rawAnimation = new Runnable(){

		@Override
		public void run() {
			if(mDeliverInfo.mCheckDistance >= 0 && 
					mDeliverInfo.mCheckDistance < mCurDistance &&
					null == mRedressHint && null!=mCorrectBtnListener)
			{
				mRedressHint = (TextView)CargoMapActivity.this.findViewById(R.id.redress_tips_v);
				mRedressHint.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					mRedressHint.setVisibility(View.GONE);
					mRedressHint.setOnClickListener(null);
					
				}});
				mRedressHint.setVisibility(View.VISIBLE);
			}
			
			
			switch (currentState)
			{
			//
				case 0:
					currentMarker1.setVisible(false);
					currentMarker2.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;
				case 1:
					currentMarker2.setVisible(false);
					currentMarker3.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;
				case 2:
					currentMarker3.setVisible(false);
					currentMarker4.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;
				case 3:
					currentMarker4.setVisible(false);
					currentMarker3.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;
				case 4:
					currentMarker3.setVisible(false);
					currentMarker2.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;	
				case 5:
					currentMarker2.setVisible(false);
					currentMarker1.setVisible(true);
					currentState = (currentState + 1) % currentCount;
					break;	
			}
			
			mHandler.postDelayed(this,MARKER_SHIFT_TV);
			
			//if(aMap.getCameraPosition().zoom > 
			
			
		}};
	/*  
	 * Description:
	 * @see com.amap.api.maps.AMap.OnCameraChangeListener#onCameraChange(com.amap.api.maps.model.CameraPosition)
	 */
	@Override
	public void onCameraChange(CameraPosition cameraOpt) {
		if(null!=mRedressHint && mRedressHint.getVisibility() == View.VISIBLE)
			mRedressHint.setVisibility(View.GONE);
		hideFourArrows();
		
		if(mCorrectLayout.getVisibility() == View.VISIBLE)
		{
			return;
		}
		float zm = aMap.getCameraPosition().zoom;
		if(eastMarker == null)
			return;
		boolean bBear = false;
		if(cameraOpt.bearing > 350 || cameraOpt.bearing<10)
			bBear = true;
		
		//Hide 4 range pics if zoom/bearing/tilt not fit
		if((zm < SHOW_RANGE_HINT_ZOOM && eastMarker.isVisible()) 
				|| cameraOpt.tilt!=0.0f	|| !bBear)
				
		{
			eastMarker.setVisible(false);
			westMarker.setVisible(false);
			northMarker.setVisible(false);
			southMarker.setVisible(false);
		}
		else if(zm >= SHOW_RANGE_HINT_ZOOM && !eastMarker.isVisible() && 
				cameraOpt.tilt==0.0f && bBear)
		{
			eastMarker.setVisible(true);
			westMarker.setVisible(true);
			northMarker.setVisible(true);
			southMarker.setVisible(true);
		}
		
	}
	
	private void hideFourArrows() {
		
		if(null!= mArrowLeft && mArrowLeft.getVisibility() == View.VISIBLE)
		{
			mArrowLeft.setVisibility(View.GONE);
			findViewById(R.id.right_arrow).setVisibility(View.GONE);
			findViewById(R.id.top_arrow).setVisibility(View.GONE);
			findViewById(R.id.bottom_arrow).setVisibility(View.GONE);
		}
	}
	
	
	private void showFourArrows() {
		
		findViewById(R.id.left_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.right_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.top_arrow).setVisibility(View.VISIBLE);
		findViewById(R.id.bottom_arrow).setVisibility(View.VISIBLE);
	}

	/**
	 * 
	* method Name:showLoading    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void showLoading(){
		if(mLoadingV == null)
		{
			mLoadingV = findViewById(R.id.global_loading);
		}
		
		if (mLoadingV!= null) 
			mLoadingV.setVisibility(View.VISIBLE);
	}

	/**
	 * 
	* method Name:endLoading    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void endLoading() {
		if (mLoadingV!= null && mLoadingV.getVisibility()==View.VISIBLE)
			mLoadingV.setVisibility(View.GONE);
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if(mCorrectLayout.getVisibility() == View.VISIBLE)
		{
			if(null == mDeocodeSearcher)
			{	
				mDeocodeSearcher = new GeocodeSearch(this);
				mDeocodeSearcher.setOnGeocodeSearchListener(this);
			}
			LatLng centerLL = aMap.getCameraPosition().target;
			if(null == mQueryPoint)
				mQueryPoint = new LatLonPoint(centerLL.latitude,centerLL.longitude);
			else
			{
				mQueryPoint.setLatitude(centerLL.latitude);
				mQueryPoint.setLongitude(centerLL.longitude);
			}
			
			if(null == mQuery)
				mQuery = new RegeocodeQuery(mQueryPoint,200,GeocodeSearch.AMAP);
			else
				mQuery.setPoint(mQueryPoint);
			
			mDeocodeSearcher.getFromLocationAsyn(mQuery);
		}
		
	}

	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		
		
	}

	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if(rCode == 0)
		{
			if(result!=null && result.getRegeocodeAddress()!=null && 
					mCorrectLayout.getVisibility() == View.VISIBLE && null!=mCorrectText)
			{
				mCorrectText.setText(result.getRegeocodeAddress().getFormatAddress() + " 附近");
			}
		}
	}

	@Override
	public void onMapLoaded() {
		rendDeliverView();
		
	}

	
	protected void submitCorrectPos() {
		if(null == mDeliverInfo)
			return;
		
		mNewDestLatLng = aMap.getCameraPosition().target;
		LatLng aCur = mDeliverInfo.getCurPos();
		float[] result = new float[1];
		Location.distanceBetween(mNewDestLatLng.latitude, mNewDestLatLng.longitude,
				aCur.latitude, aCur.longitude, result);
		if(result[0] > mDeliverInfo.mCheckDistance)
		{
			if(mHintDialog == null)
			{
				mHintDialog = UiUtils.showDialog(CargoMapActivity.this, R.string.caption_hint, 
					R.string.correct_amap_too_far_away, 
					R.string.btn_ok,R.string.continue_to_correct,
					new AppDialog.OnClickListener(){

						@Override
						public void onDialogClick(int nButtonId) {
							if(nButtonId == AppDialog.BUTTON_POSITIVE)
							{
								sendCorrectAjax();
							}
						}}
					);
			}else
				mHintDialog.show();
		}
		else
			sendCorrectAjax();
		
		
	}

	private void sendCorrectAjax() {
		
		if(null == mCorrectAjax)
		{
			
			//mCorrectAjax = com.icson.util.AjaxUtil.get("http://beta.m.51buy.com/mc/json.php?mod=order&act=reportGIS");
			mCorrectAjax = ServiceConfig.getAjax(Config.URL_REDRESS_GIS);
			
			if(null == mCorrectAjax)
				return;
			//mCorrectAjax.setParser(new com.icson.util.ajax.JSONParser());
			
			mCorrectAjax.setData("orderId", mOrderId);
			mCorrectAjax.setData("type" ,1); //纠正type  收货地址 1
			mCorrectAjax.setData("address",mDeliverInfo.getDestAddressName());
			mCorrectAjax.setData("lat",mDeliverInfo.getDestPos().latitude);
			mCorrectAjax.setData("lon",mDeliverInfo.getDestPos().longitude);
			mCorrectAjax.setData("newLat",mNewDestLatLng.latitude);
			mCorrectAjax.setData("newLon",mNewDestLatLng.longitude);
			
			
			mCorrectAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){

				@Override
				public void onSuccess(JSONObject v, Response response) {
					endLoading();
					final int errno = v.optInt("errno", -1);
					if (errno != 0) {
						UiUtils.makeToast(CargoMapActivity.this, v.optString("data", getString(R.string.submit_redress_fail)));
						return;
					}
					UiUtils.makeToast(CargoMapActivity.this, v.optString("data", getString(R.string.submit_redress_succ)));
					
					CargoRedressCache.setAddressLatLng(mDeliverInfo.getDestAddressName(), mNewDestLatLng);
					mNavBar.setRightText(getString(R.string.correct_amap_pos));
					rendDeliverView();
					shiftCorrectLayout(false);
				}});
			
			mCorrectAjax.setOnErrorListener(new OnErrorListener(){

				@Override
				public void onError(Ajax ajax, Response response) {
					endLoading();
					Parser pParser = ajax.getParser();
					String pErrMsg = null == pParser ? getString(R.string.network_error_info) : pParser.getErrMsg();
					UiUtils.makeToast(CargoMapActivity.this, TextUtils.isEmpty(pErrMsg) ? getString(R.string.submit_redress_fail) : pErrMsg);
				}});
		}
		
		showLoading();
		mCorrectAjax.send();
		
		//CargoRedressCache.setAddressLatLng(mDeliverInfo.getDestAddressName(), mNewDestLatLng);
		//mNavBar.setRightText(getString(R.string.correct_amap_pos));
		//rendDeliverView();
		//shiftCorrectLayout(false);
		
	}

	/**
	 * 
	 */
	protected void showCorrectChoiceDialog() {
		
		if(null == mCorrectChoiceDialog)
		{
			final List<Sharable> aSharables = new ArrayList<Sharable>(2);
			Sharable pEntity = new Sharable();
			pEntity.mLabel = getString(R.string.correct_amap_dest_pos);//(String) pManager.getApplicationLabel(pInfo.applicationInfo);
			pEntity.mIcon = getResources().getDrawable(R.drawable.dest_pos);
			aSharables.add(pEntity);
		
			pEntity = new Sharable();
			pEntity.mLabel = getString(R.string.correct_amap_deliver_pos);
			pEntity.mIcon = getResources().getDrawable(R.drawable.cur_pos_2);
			aSharables.add(pEntity);
		
	
			SharableAdapter pAdapter = new SharableAdapter(CargoMapActivity.this, aSharables);
			mCorrectChoiceDialog = UiUtils.showListDialog(CargoMapActivity.this, getString(R.string.correct_amap_pos), pAdapter, 
					new RadioDialog.OnRadioSelectListener(){

						@Override
						public void onRadioItemClick(int which) {
							if(which == 0)
							{
								shiftCorrectLayout(true);
								aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
										getCorrectEndPos(),mZoomLevel));
								mNavBar.setRightText(getString(R.string.done_correct_amap_pos));
								onCameraChangeFinish(null);
							}
							else
							{
								Bundle b = new Bundle();
								b.putString("orderId", mOrderId);
								UiUtils.startActivity(CargoMapActivity.this, AdviseActivity.class, false);
							}
							
							
						}}); 
					
		}
		else
			mCorrectChoiceDialog.show();
	}

	protected void shiftCorrectLayout(boolean correctLayoutVisiable) {
		
		mCorrectLayout.setVisibility(correctLayoutVisiable ? View.VISIBLE : View.INVISIBLE);
		if(null == mCorrectText)
		{
			mCorrectText = (TextView)this.findViewById(R.id.center_addr_info_v);
			mCorrectIcon = (ImageView)this.findViewById(R.id.center_addr_icon);
			mCorrectIcon.setImageResource(R.drawable.dest_pos);
		}
		hintLayout.setVisibility(correctLayoutVisiable ? View.INVISIBLE : View.VISIBLE);
		destMarker.setVisible(!correctLayoutVisiable);
		mHandler.removeCallbacksAndMessages(null);
		
		if(!correctLayoutVisiable)
		{
			mHandler.postDelayed(rawAnimation, MARKER_SHIFT_TV);
			currentMarker0.setVisible(true);
		}
		else
		{
			currentMarker1.setVisible(false);
			currentMarker2.setVisible(false);
			currentMarker3.setVisible(false);
			currentMarker4.setVisible(false);
			currentMarker0.setVisible(false);
		}
		mDisCircle.setVisible(!correctLayoutVisiable);
		eastMarker.setVisible(!correctLayoutVisiable);
		westMarker.setVisible(!correctLayoutVisiable);
		northMarker.setVisible(!correctLayoutVisiable);
		southMarker.setVisible(!correctLayoutVisiable);
		
		if(null!=mCorrectLayout && mCorrectLayout.getVisibility() == View.VISIBLE)
		{
			if(null == mArrowLeft)
				mArrowLeft = (ImageView) findViewById(R.id.left_arrow);
			mHandler.postDelayed(showArrowDelay,100);
		}
		// TODO Auto-generated method stub
		
	}
	
	private Runnable showArrowDelay = new Runnable(){
		@Override
		public void run() 
		{
			showFourArrows();
			mHandler.removeCallbacks(this);
		}
	};

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_CargoMapActivity);
	}
		
}
