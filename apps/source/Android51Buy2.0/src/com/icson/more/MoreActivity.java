package com.icson.more;

import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.home.HomeActivity;
import com.icson.home.ProvinceModel;
import com.icson.home.ProvinceModel.CityModel;
import com.icson.home.ProvinceModel.CityModel.ZoneModel;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.FullDistrictHelper.FullDistrictItem;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.IShippingArea;
import com.icson.lib.IShoppingCart;
import com.icson.lib.IVersion;
import com.icson.lib.control.VersionControl;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.VersionModel;
import com.icson.lib.ui.AddressRadioDialog;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.CheckBox;
import com.icson.lib.ui.CheckBox.OnCheckedChangeListener;
import com.icson.lib.ui.SettingCellView;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.login.ReloginWatcher;
import com.icson.main.MainActivity;
import com.icson.message.MessageActivity;
import com.icson.preference.Preference;
import com.icson.push.PushAssistor;
import com.icson.shoppingcart.ShoppingCartCommunication;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.AppUtils;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.icson.util.cache.InnerCache;
import com.icson.util.cache.SDCache;

public class MoreActivity extends BaseActivity {
	private SettingCellView mCheckVersion;
	private SettingCellView mSelectAddress;
	private CheckBox 	mNonImage;
	private CheckBox 	mOpenMessage;
	private Preference 	mPreference;
	private boolean    mPushEnabled = true;
	private boolean    mPushChecked = false;
	
	private AddressRadioDialog mProvinceDialog;
	private AddressRadioDialog mCityDialog;
	private AddressRadioDialog mZoneDialog;
	
	private ProvinceModel mProvinceModel;
	private CityModel mCityModel;
	private ZoneModel mZoneModel;
	private FullDistrictItem mDistrictItem;
	private boolean isInitNonImage = true;
	private boolean isInitOpenMessage = true;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_more);
		
		// Load navigation bar configuration.
		this.loadNavBar(R.id.more_navbar);
		
		mDistrictItem = FullDistrictHelper.getFullDistrict();
		
		String strAddressDetail = getAddressDetail();
		String strSelectAddressContent = TextUtils.isEmpty(strAddressDetail) ? "请选择省市" : "当前省市：" + strAddressDetail;

		//地址选择
		mSelectAddress = (SettingCellView) findViewById(R.id.more_relative_address);
		mSelectAddress.setOnClickListener(this);
		mSelectAddress.setContent(strSelectAddressContent);
		
		findViewById(R.id.more_relative_history).setOnClickListener(this);
		findViewById(R.id.more_relative_messages).setOnClickListener(this);
		findViewById(R.id.more_relative_advise).setOnClickListener(this);
		findViewById(R.id.more_relative_recommend_apps).setOnClickListener(this);
		findViewById(R.id.more_relative_aboutus).setOnClickListener(this);
		findViewById(R.id.more_relative_clear).setOnClickListener(this);
		findViewById(R.id.more_relative_contactus).setOnClickListener(this);
		findViewById(R.id.more_relative_rules).setOnClickListener(this);
		
		View pLogout = findViewById(R.id.more_relative_logout);
		if( 0 == ILogin.getLoginUid() ) {
			pLogout.setVisibility(View.GONE);
		} else {
			pLogout.setOnClickListener(this);
		}
		
		//版本检测
		mCheckVersion = (SettingCellView) findViewById(R.id.more_relative_version);
		mCheckVersion.setOnClickListener(this);
		mCheckVersion.setContent("当前版本:" + IVersion.getVersionName());
		
		mPreference = Preference.getInstance();
		
		//无图模式
		mNonImage = (CheckBox) findViewById(R.id.more_relative_settings_non_image_checkbox);
		mNonImage.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChange(Boolean isChecked) {
				mPreference.setImageMode(isChecked ? Preference.MODE_NOIMAGE : Preference.MODE_HASIMAGE);
				if(!isInitNonImage) {
					ToolUtil.reportStatisticsClick(getActivityPageId(), "21002");
				}
				isInitNonImage = false;
			}
		});
		
		mNonImage.setChecked(Preference.MODE_NOIMAGE == mPreference.getImageMode());
		
		
		//消息通知
		mOpenMessage = (CheckBox) findViewById(R.id.more_relative_settings_open_message_checkbox);
		mOpenMessage.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChange(Boolean isChecked) {
				mPushChecked = isChecked;
				if(!isInitOpenMessage) {
					ToolUtil.reportStatisticsClick(getActivityPageId(), "21003");
				}
				isInitOpenMessage = false;
			}
		});
		
		mPushEnabled = mPreference.pushMessageEnabled();
		mPushChecked = mPushEnabled;
		mOpenMessage.setChecked(mPushEnabled);
	}
	
	private String getAddressDetail(){
		String strAddressDetail = "";
		
		if(null != mDistrictItem) {
			String provinceName = mDistrictItem.mProvinceName;
			String cityName = mDistrictItem.mCityName;
			String districtName = mDistrictItem.mDistrictName;
			
			if(provinceName.equals(cityName)) {
				provinceName = "";
			}
			strAddressDetail = provinceName + cityName + districtName;
		}
		
		return strAddressDetail;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.more_relative_address:
			//选择省份的页面
			selectAddress();
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			break;
		case R.id.more_relative_history:
			ToolUtil.startActivity(this, ViewHistoryActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), ViewHistoryActivity.class.getName(), getString(R.string.tag_ViewHistoryActivity), "01011");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21004");
			break;
		case R.id.more_relative_version:
			checkVersion();
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), "checkVersion", getString(R.string.tag_MoreActivity), "01012");
			break;
//		case R.id.more_relative_settings:
//			this.showSettings();
//			ToolUtil.sendTrack(this.getClass().getName(),"settings",getString(R.string.tag_MoreActivity)+"01015");
//			break;
		case R.id.more_relative_messages:
			final long nLoginUid = ILogin.getLoginUid();
			if( 0 == nLoginUid )
			{
				// Not login yet.
				UiUtils.makeToast(this, R.string.need_login);
				ToolUtil.startActivity(this, LoginActivity.class, null, MoreActivity.REQUEST_MESSAGE_CENTER);
			}
			else
			{
				loadMessageCenter();
			}
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21006");
			break;
		case R.id.more_relative_advise:
			ToolUtil.checkLoginOrRedirect(MoreActivity.this, FeedBackHistoryActivity.class, null, -1);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AdviseActivity.class.getName(), getString(R.string.tag_AdviseActivity), "01013");
			/*if( 0 == ILogin.getLoginUid() )
			{
				ToolUtil.startActivity(this, AdviseActivity.class);
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AdviseActivity.class.getName(), getString(R.string.tag_AdviseActivity), "01013");
			}
			else
			{
				ToolUtil.startActivity(this, FeedBackHistoryActivity.class);
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AdviseActivity.class.getName(), getString(R.string.tag_AdviseActivity), "01013");
			}*/
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21008");
			break;
		case R.id.more_relative_aboutus:
			ToolUtil.startActivity(this, AboutUsActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AboutUsActivity.class.getName(), getString(R.string.tag_AboutUsActivity), "01014");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21011");
			break;
		case R.id.more_relative_logout:
			UiUtils.showDialog(this, R.string.caption_hint, R.string.message_logout, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE) {
						ILogin.clearAccount();
						
						// Clear QQ account information.
						ReloginWatcher.getInstance(MoreActivity.this).clearAccountInfo();
						
						// 清除本地购物车
						IShoppingCart.clear();
						// 更新icon
						ShoppingCartCommunication mShoppingCartCommunication = new ShoppingCartCommunication(MoreActivity.this);
						mShoppingCartCommunication.notifyDataSetChange();
						
						MainActivity.startActivity(MoreActivity.this, MainActivity.TAB_MY);
						ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), HomeActivity.class.getName(), getString(R.string.tag_Home), "01022");
						ToolUtil.reportStatisticsClick(getActivityPageId(), "21013");
						// Log request for logout.
						StatisticsEngine.updateInfo(ILogin.getLoginUid(), 2);
						
						AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_MINE_RELOAD, "1", false);
					}
				}
			});
			break;
		case R.id.more_relative_contactus:
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4008281878"));
			if(AppUtils.checkAndCall(MoreActivity.this,intent))
			{
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), "contact us", getString(R.string.tag_MoreActivity), "02012");
				ToolUtil.reportStatisticsClick(getActivityPageId(), "21012");
			}
			break;
		case R.id.more_relative_clear:
			cleanCache();
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), "clean Cache", getString(R.string.tag_MoreActivity), "02011");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21005");
			break;
		case R.id.more_relative_recommend_apps:
			ToolUtil.startActivity(this, AppInfoActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AppInfoActivity.class.getName(), getString(R.string.tag_AppInfoActivity), "02013");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21007");
			break;
		case R.id.more_relative_rules:
			Bundle bundle = new Bundle();
			bundle.putString(HTML5LinkActivity.LINK_URL,"http://u.yixun.com/h5agreement");
			UiUtils.startActivity(this, HTML5LinkActivity.class, bundle, true);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), AppInfoActivity.class.getName(), getString(R.string.tag_HTML5LinkActivity), "02019");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21010");
			break;
//		case R.id.more_relative_welcome:
//			ToolUtil.startActivity(this, WelcomeActivity.class);
//			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), WelcomeActivity.class.getName(), getString(R.string.tag_WelcomeActivity), "02014");
//			break;
		}
	}
	
	private void loadMessageCenter() {
		ToolUtil.startActivity(this, MessageActivity.class);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), MessageActivity.class.getName(), getString(R.string.tag_MessageActivity), "01017");
		StatisticsEngine.trackEvent(this, "msgcenter_more");
	}
	
	
	@Override
	protected void onDestroy() {
		// Update push message status.
		if( mPushEnabled != mPushChecked ) {
			mPushEnabled = mPushChecked;
			
			// Synchronize the status to preference.
			mPreference.setPushMessageEnabled(mPushEnabled);
			
			// Stop previous service instance.
			PushAssistor.killTask(this, true);
			if( mPushEnabled ) {
				// Start the service
				PushAssistor.setTask(this, true);
			} else {
				StatisticsEngine.trackEvent(this, "disable_push");
			}
		}
		
		mPreference = null;
		mProvinceDialog = null;
		mCityDialog = null;
		mZoneDialog = null;
		
		mProvinceModel = null;
		mCityModel = null;
		mZoneModel = null;
		
		IShippingArea.clean();
		super.onDestroy();
	}
	
	
	private void selectAddress(){
		int selectedId = (null == mDistrictItem) ? 0 : mDistrictItem.mProvinceId;
		int selectedIndex = 0;
		
		//get whole full district information
		final ArrayList<ProvinceModel> pProviceModels = IShippingArea.getAreaModels();
		if (pProviceModels == null) {
			UiUtils.makeToast(this, Config.NORMAL_ERROR);
			return;
		}

		final int nSize = pProviceModels.size();
		if( 0 >= nSize )
			return ;
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ProvinceModel pMode = pProviceModels.get(nIdx);
			names[nIdx] = pMode.getProvinceName();
			if (selectedId != 0 && pMode.getProvinceId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mProvinceDialog)
		{
		mProvinceDialog = UiUtils.showAddressListDialog(this, getString(R.string.select_province), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				mProvinceModel = pProviceModels.get(pos);
				selectCity();
			}
		}, true);
		}
		else
			mProvinceDialog.setList(names, selectedIndex);
		
		mProvinceDialog.show();
		
	}
	
	private void selectCity(){
		if(null == mProvinceModel ) {
			return;
		}
		
		int selectedId = (null == mDistrictItem) ? 0 : mDistrictItem.mCityId;;
		int selectedIndex = 0;
		final ArrayList<CityModel> pCityModels = mProvinceModel.getCityModels();
		if (pCityModels == null) {
			UiUtils.makeToast(this, Config.NORMAL_ERROR);
			return;
		}

		final int nSize = pCityModels.size();
		if( 0 >= nSize ){
			return ;
		}
		
		if( 1 == nSize ) {
			mCityModel = pCityModels.get(0);
			selectZone();
			return;
		}
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			CityModel pMode = pCityModels.get(nIdx);
			names[nIdx] = pMode.getCityName();
			if (selectedId != 0 && pMode.getCityId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mCityDialog)
		{
		mCityDialog = UiUtils.showAddressListDialog(this, getString(R.string.select_city), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				mCityModel = pCityModels.get(pos);
				selectZone();
			}
		}, true);
		}
		else
			mCityDialog.setList(names, selectedIndex);
		
		mCityDialog.show();
	}
	
	private void selectZone(){
		if(null == mCityModel || null == mProvinceModel) {
			return;
		}
		
		int selectedId = (null == mDistrictItem) ? 0 : mDistrictItem.mDistrictId;;
		int selectedIndex = 0;
		final ArrayList<ZoneModel> pZoneModels = mCityModel.getZoneModels();
		if (pZoneModels == null) {
			UiUtils.makeToast(this, Config.NORMAL_ERROR);
			return;
		}

		final int nSize = pZoneModels.size();
		if( 0 >= nSize )
			return ;
		
		if( 1 == nSize ) {
			mZoneModel = pZoneModels.get(0);
			afterSelectFullDistrict();
			return;
		}
		
		String names[] = new String[nSize];
		
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			ZoneModel pMode = pZoneModels.get(nIdx);
			names[nIdx] = pMode.getZoneName();
			if (selectedId != 0 && pMode.getZoneId() == selectedId) {
				selectedIndex = nIdx;
			}
		}
		
		if(null == mZoneDialog)
		{
		mZoneDialog = UiUtils.showAddressListDialog(this, getString(R.string.select_area), names, selectedIndex, new AddressRadioDialog.OnAddressRadioSelectListener() {
			@Override
			public void onRadioItemClick(int pos) {
				if(null == pZoneModels || pZoneModels.size() <= pos)
				{
					mZoneDialog.dismiss();
					return;
				}
				
				mZoneModel = pZoneModels.get(pos);
				
				
				//Update UI
				afterSelectFullDistrict();
			}
		}, true, false);
		}
		else
			mZoneDialog.setList(names, selectedIndex);
		mZoneDialog.show();
	}
	
	
	private void afterSelectFullDistrict(){
		if(null == mCityModel || null == mProvinceModel || null == mZoneModel) {
			return;
		}
		
		if(null != mZoneDialog && mZoneDialog.isShowing()) {
			mZoneDialog.dismiss();
		}
		
		if(null != mCityDialog && mCityDialog.isShowing()) {
			mCityDialog.dismiss();
		}
		
		if(null != mProvinceDialog && mProvinceDialog.isShowing()) {
			mProvinceDialog.dismiss();
		}
		
		String strAddressDetail = "";
		if(mCityModel.getCityName().contains(mProvinceModel.getProvinceName())) {
			strAddressDetail = mCityModel.getCityName() + mZoneModel.getZoneName();
		}else{
			strAddressDetail = mProvinceModel.getProvinceName() + mCityModel.getCityName() + mZoneModel.getZoneName();
		}
		
		IPageCache cache = new IPageCache();
		String id = cache.get(CacheKeyFactory.CACHE_CITY_ID);
		cache = null;
		int nCityId = (id != null ? Integer.valueOf( id ) : 0);
		
		//Update UI
		//empty not set yet || different, changed districtid
		if (nCityId == 0 || mZoneModel.getZoneId()!= mDistrictItem.mDistrictId) 
		{
			FullDistrictItem pDistrictItem = new FullDistrictItem(mProvinceModel.getProvinceId(), mProvinceModel.getProvinceIPId(), mProvinceModel.getProvinceName(), mCityModel.getCityId(), mCityModel.getCityName(), mZoneModel.getZoneId(), mZoneModel.getZoneName());
			FullDistrictHelper.setFullDistrict(pDistrictItem);
			mDistrictItem = pDistrictItem;
		}
		
		mSelectAddress.setContent("当前省市：" + strAddressDetail);
	}

	
	// 清除缓存
	private void cleanCache() {
		showProgressLayer("正在清理, 请稍候...");
		// 避免系统数据被删除，使用白名单策略
		IPageCache cache = new IPageCache();

		// 商品分类列表
		cache.remove(CacheKeyFactory.CACHE_BLOCK_CATEGORY);
		// 确认订单：发票ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_INVOICE_ID);
		// 确认订单：地址ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		// 确认订单：配送方式ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID);
		// 确认订单：支付方式ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID);
		//首页运营錧
		cache.remove(CacheKeyFactory.HOME_CHANNEL_INFO);
		//各运营馆数据
		cache.removeLeftLike(CacheKeyFactory.CACHE_EVENT);
		//Remove dispatches information
		cache.remove(CacheKeyFactory.CACHE_DISPATCHES_INFO);
		//Remove search history words
		cache.remove(CacheKeyFactory.CACHE_SEARCH_HISTORY_WORDS);

		// Remove image cache.
		this.removeImaegCache();

		closeProgressLayer();
		UiUtils.makeToast(this, "缓存已清除");
	}
	
	/**
	 * removeImageCache
	 */
	private void removeImaegCache()
	{
		String aDirs[] = {Config.PIC_CACHE_DIR, Config.CHANNEL_PIC_DIR, Config.MY_FAVORITY_DIR, Config.MY_ORDERLIST_DIR, Config.QIANG_PIC_DIR, Config.TUAN_PIC_DIR};
		if (ToolUtil.isSDExists()) 
		{
			SDCache storage = new SDCache();
			for( String folder : aDirs )
			{
				storage.removeFolder(folder);
			}
		}

		InnerCache storage = new InnerCache(this);
		for( String folder : aDirs )
		{
			storage.removeFolder(folder);
		}
		
	}

	private void checkVersion() {
		showProgressLayer("正在检查新版本, 请稍候...");
		VersionControl control = new VersionControl();
		control.getlatestVersionInfo(true, new OnSuccessListener<VersionModel>() {
			@Override
			public void onSuccess(VersionModel v, Response response) {
				closeProgressLayer();
				if (v.getVersion() <= IVersion.getVersionCode()) {
					UiUtils.makeToast(MoreActivity.this, R.string.message_latest_version);
					return;
				}

				IVersion.notify(MoreActivity.this, v);
			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response) {
				showProgressLayer("正在检查新版本, 请稍候...");
				UiUtils.makeToast(MoreActivity.this, "检测失败");
				closeProgressLayer();
			}
		});
	}
	
	private static final int REQUEST_MESSAGE_CENTER = 0x100;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if ( (REQUEST_MESSAGE_CENTER == requestCode) && (LoginActivity.FLAG_RESULT_LOGIN_SUCCESS == resultCode) )
		{
			this.loadMessageCenter();
		}
		
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MoreActivity);
	}
}
