package com.icson.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.icson.R;
import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.ITrack;
import com.icson.lib.IVersion;
import com.icson.lib.model.Account;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.main.MainActivity;
import com.icson.preference.Preference;
import com.icson.service.DownLoadService;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.ajax.Cookie;

public class ToolUtil {

	private static String[] WEEKDAYS = new String[] { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private static String[] MONTHS = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

	private static final String LOG_TAG = ToolUtil.class.getName();

	private static float mDensity = 0;

	private static int mAppWidthDip = 0;
	
	private static int mAppWidth = 0;

	private static SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static float getDensity() {
		if (mDensity != 0)
			return mDensity;

		mDensity = IcsonApplication.app.getResources().getDisplayMetrics().density;

		return mDensity;

	}

	public static int getAppWidthDip() {
		if (mAppWidthDip != 0)
			return mAppWidthDip;

		final Context context = IcsonApplication.app;
		mAppWidthDip = ToolUtil.px2dip(context, getEquipmentWidth(context));

		return mAppWidthDip;
	}

	public static int getAppWidth() {
		if (mAppWidth != 0)
			return mAppWidth;

		mAppWidth = getEquipmentWidth(IcsonApplication.app);

		return mAppWidth;
	}

	public static float getDensityDpi() {
		return IcsonApplication.app.getResources().getDisplayMetrics().densityDpi;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = getDensity();
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int getEquipmentWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getEquipmentHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	private static void checkLoginOrRedirect(Activity activity, Class<?> which, Bundle params, int requestFlag, boolean openInFrame) {

		Account account = ILogin.getActiveAccount();

		if (account != null) {
			if (openInFrame) {
				if (activity != null && activity instanceof MainActivity) {
					MainActivity mParent = (MainActivity) activity;
					mParent.startSubActivity(which, params);
				} else {
					Log.e(LOG_TAG, "checkLoginOrRedirect|activity's parent is null or the instance is not MainActivity");
				}
			} else {
				startActivity(activity, which, params, requestFlag);
			}
			return;
		}

		Bundle bundle = new Bundle();
		//下面的两个变量的设置是重点，他们记录了转到登录界面之前应该显示的这个activity的信息，这样在登录成功之后才能再调回到这个activity上
		bundle.putString(LoginActivity.REQUEST_PACKAGE_NAME, activity.getPackageName());
		bundle.putString(LoginActivity.REQUEST_CLASS_NAME, which.getName());
		/*Log.d("liumsg", which.getName());
		Log.d("liumsg", activity.getPackageName());*/
		if (params != null) {
			bundle.putBundle(LoginActivity.REQUEST_BUNDLE, params);
		}

		if (openInFrame) {
			if (activity != null && activity instanceof MainActivity) {
				MainActivity mParent = (MainActivity) activity;
				bundle.putBoolean(LoginActivity.REQUEST_OPEN_IN_FRAME, openInFrame);
				mParent.startSubActivity(LoginActivity.class, bundle);
			} else {
				Log.e(LOG_TAG, "checkLoginOrRedirect|activity's parent is null or the instance is not MainActivity");
			}
		} else {
			ToolUtil.startActivity(activity, LoginActivity.class, bundle, requestFlag);
		}
	}

	public static void checkLoginOrRedirect(Activity activity, Class<?> which, Bundle params, boolean openInFrame) {
		checkLoginOrRedirect(activity, which, params, -1, openInFrame);
	}

	public static void checkLoginOrRedirect(Activity activity, Class<?> which, Bundle params, int requestFlag) {
		checkLoginOrRedirect(activity, which, params, requestFlag, false);
	}

	public static void checkLoginOrRedirect(Activity activity, Class<?> which, Bundle params) {
		checkLoginOrRedirect(activity, which, params, -1, false);
	}

	public static void checkLoginOrRedirect(Activity activity, Class<?> which) {
		checkLoginOrRedirect(activity, which, null, -1, false);
	}

	public static void startActivity(Activity activity, Intent intent, Bundle params, int requestFlag) {

		if (null != params) {
			intent.putExtras(params);
		}

		activity.startActivityForResult(intent, requestFlag);
		//ToolUtil.sendTrack(activity.getClass().getName(), intent.getComponent().getClassName(),"","");
	}

	public static void startActivity(Activity activity, Class<?> which, Bundle params, int requestFlag) {
		Intent intent = new Intent(activity, which);
		startActivity(activity, intent, params, requestFlag);
	}

	public static void startVoiceSearchActivity(final Activity activity, int VOICE_RECOGNITION_REQUEST_CODE) {
		try {
			activity.getPackageManager().getPackageInfo("com.google.android.voicesearch", 0);
			
			// 通过Intent传递语音识别的模式，开启语音
			Intent intent = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			// 语言模式和自由模式的语音识别
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			// 提示语音开始
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "开始语音");
			// 开始语音识别
			activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		}catch (NameNotFoundException ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			UiUtils.showDialog(activity, R.string.caption_google_voice, R.string.message_not_support_google_voice, R.string.btn_install, R.string.btn_cancel, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE) {
						Intent intent = new Intent(activity, DownLoadService.class);
						intent.putExtra(DownLoadService.REQUEST_URL, ServiceConfig.getUrl(Config.URL_DOWNLOAD_VOICESEARCH));
						intent.putExtra(DownLoadService.REQUEST_TITLE, "语音搜索");
						activity.startService(intent);
					}
				}
			});
		} catch (Exception e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
			Toast.makeText(activity, "找不到语音设备", Toast.LENGTH_LONG).show();
		}
	}
	public static void startActivity(Activity activity, Class<?> target, Bundle params) {
		startActivity(activity, target, params, -1);
	}

	public static void startActivity(Activity activity, Class<?> target) {
		startActivity(activity, target, null);
	}

	public static String getMD5(String val) {

		String ret = null;

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(val.getBytes());
			byte[] m = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < m.length; i++) {
				sb.append(m[i]);
			}

			ret = sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			Log.e(LOG_TAG, ex);
		}

		return ret;
	}
	
	/**
	 * toMD5
	 * The version to match PHP version.
	 * @param strVal
	 * @return
	 */
	public static String toMD5(String strVal) 
	{
		if( TextUtils.isEmpty(strVal) )
			return "";
		
		String strResult = "";
        try 
        {
            MessageDigest pDigest = MessageDigest.getInstance("MD5");
            pDigest.reset();
            pDigest.update(strVal.getBytes());
            
            BigInteger hash = new BigInteger(1, pDigest.digest());  
            strResult = hash.toString(16);  
            while(strResult.length() < 32) 
            {  
            	strResult = "0" + strResult;  
            }
        }
        catch (NoSuchAlgorithmException e)
        {
        	e.printStackTrace();
        	strResult = "";
        }
        
        return strResult;
	}

	public static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}

		return android.util.Log.getStackTraceString(tr);

		// return tr.getMessage();
	}

	public static String getPriceStr(long fen) {
		return String.valueOf(new java.text.DecimalFormat("#0.00").format(fen / 100));
	}

	public static Cookie getIcsonCookie() {
		
		Account account = ILogin.getActiveAccount();
		
		Cookie cookie = new Cookie();
		if (account != null) {
			cookie.set("uid", String.valueOf(account.getUid()));
			cookie.set("skey", account.getSkey());
			cookie.set("token", account.getToken());
		}

		cookie.set(Config.COOKIE_SITE, String.valueOf(ILogin.getSiteId()));
		cookie.set("version", IVersion.getVersionCode() + "");
		
		cookie.set("appVersion", IVersion.getVersionCode() + "");
		cookie.set("appSource", "android");
		cookie.set("deviceId", StatisticsUtils.getDeviceUid(IcsonApplication.app));
		cookie.set("channelId", AppStorage.getData("channel"));
		cookie.set("districtid", String.valueOf(FullDistrictHelper.getDistrictId()));
		String strCpsCookie = AppStorage.getData(AppStorage.SCOPE_CPS, AppStorage.KEY_CPS_COOKIES);
		if( !TextUtils.isEmpty(strCpsCookie) ) {
			cookie.set(AppStorage.KEY_CPS_COOKIES, strCpsCookie);
		}
		String strCpsTkd = AppStorage.getData(AppStorage.SCOPE_CPS, AppStorage.KEY_CPS_TKD);
		if( !TextUtils.isEmpty(strCpsTkd) ) {
			cookie.set(AppStorage.KEY_CPS_TKD, strCpsTkd);
		}
		/* As site Wuhan updated, remove the tricky codes here.
		final int cityId = DispatchFactory.getDefaultCityId();
		if(cityId == 42){
			cookie.set(Config.COOKIE_SITE_SC, "3001");
		}
		*/

		return cookie;
	}

	public static boolean isEmptyList(JSONObject json, String key) {
		JSONArray jarray = json.optJSONArray(key);
		if(null != jarray) //"[]"
			return (jarray.length()<=0);
		
		JSONObject jobj = json.optJSONObject(key);// ! {}
		if(jobj!=null  && null!=jobj.names())//{aa:b}
			return false;
		
		final String str = json.optString(key);

		return str.equals("") || str.equals("{}")|| str.equals("[]") || str.equalsIgnoreCase("null") || str.equalsIgnoreCase("false");
	}

	/*
	 * public static Ajax loadImage(BaseActivity activity, final ImageView view,
	 * String url) { Ajax ajax = AjaxUtil.getImage(url);
	 * ajax.setOnSuccessListener(new OnSuccessListener<Bitmap>() {
	 * 
	 * @Override public void onSuccess(Bitmap v, Response response) { if (view
	 * != null) { view.setImageBitmap(v); } } }); activity.addAjax(ajax);
	 * ajax.send();
	 * 
	 * return ajax; }
	 */

	public static String toPrice(double fen, int len) {
//		String format = len == 2 ? "#0.00" : "#0.0";
		double pRemainder = fen % 100;
		if( 0 == pRemainder ) {
			return String.valueOf(new java.text.DecimalFormat("#0").format(fen/100));
		}
		return String.valueOf(fen/100);
	}

	/**
	 * 
	 * @param dValue
	 * @return
	 */
	 public static String toDiscount(double dValue)
	{
		BigDecimal BDdiscount = new BigDecimal(dValue);
		double percent = BDdiscount.setScale(0,BigDecimal.ROUND_UP).doubleValue();
		if(percent%10 == 0)
			return String.valueOf(new java.text.DecimalFormat("#0").format(percent/10));
		else
			return String.valueOf(percent/10);
	}
	
	public static String toPrice(double fen) {
		return toPrice(fen, 2);
	}
	
	public static String toPriceInterger(double fen) {
		return String.valueOf(new java.text.DecimalFormat("#0").format(fen/100));
	}

	public static String toDate(long milliSeconds, String formatStr) {
		Date date = new Date(milliSeconds);
	//	return SimpleDateFormat.getDateTimeInstance().format(date);
		return new SimpleDateFormat(formatStr).format(date);
	}

	// @param format yyyy-MM-dd HH:mm:ss
	public static String toDate(long milliSeconds) {
		return mFormat.format(new Date(milliSeconds));
	}

	public static String getUserLevelName(int level) {
		String name;
		switch (level) {
		case 0:
			name = "土星会员";
			break;
		case 1:
			name = "铜盾会员";
			break;
		case 2:
			name = "银盾会员";
			break;
		case 3:
			name = "金盾会员";
			break;
		case 4:
			name = "钻石会员";
			break;
		case 5:
			name = "皇冠会员";
			break;
		case 6:
			name = "易金鲸";
			break;
		default:
			name = "";
		}
		return name;
	}

	public static long parseDate(String string) {
		int offset = 0, length = string.length(), state = 0;
		int year = -1, month = -1, date = -1;
		int hour = -1, minute = -1, second = -1;
		final int PAD = 0, LETTERS = 1, NUMBERS = 2;
		StringBuilder buffer = new StringBuilder();

		while (offset <= length) {
			char next = offset < length ? string.charAt(offset) : '\r';
			offset++;

			int nextState;
			if ((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z'))
				nextState = LETTERS;
			else if (next >= '0' && next <= '9')
				nextState = NUMBERS;
			else if (" ,-:\r\t".indexOf(next) == -1)
				throw new IllegalArgumentException();
			else
				nextState = PAD;

			if (state == NUMBERS && nextState != NUMBERS) {
				int digit = Integer.parseInt(buffer.toString());
				buffer.setLength(0);
				if (digit >= 70) {
					if (year != -1 || (next != ' ' && next != ',' && next != '\r'))
						throw new IllegalArgumentException();
					year = digit;
				} else if (next == ':') {
					if (hour == -1)
						hour = digit;
					else if (minute == -1)
						minute = digit;
					else
						throw new IllegalArgumentException();
				} else if (next == ' ' || next == ',' || next == '-' || next == '\r') {
					if (hour != -1 && minute == -1)
						minute = digit;
					else if (minute != -1 && second == -1)
						second = digit;
					else if (date == -1)
						date = digit;
					else if (year == -1)
						year = digit;
					else
						throw new IllegalArgumentException();
				} else if (year == -1 && month != -1 && date != -1)
					year = digit;
				else
					throw new IllegalArgumentException();
			} else if (state == LETTERS && nextState != LETTERS) {
				String text = buffer.toString().toUpperCase(Locale.getDefault());
				buffer.setLength(0);
				if (text.length() < 3)
					throw new IllegalArgumentException();
				if (parse(text, WEEKDAYS) != -1) {
				} else if (month == -1 && (month = parse(text, MONTHS)) != -1) {
				} else if (text.equals("GMT")) {
				} else
					throw new IllegalArgumentException();
			}

			if (nextState == LETTERS || nextState == NUMBERS)
				buffer.append(next);
			state = nextState;
		}

		if (year != -1 && month != -1 && date != -1) {
			if (hour == -1)
				hour = 0;
			if (minute == -1)
				minute = 0;
			if (second == -1)
				second = 0;
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			int current = cal.get(Calendar.YEAR) - 80;
			if (year < 100) {
				year += current / 100 * 100;
				if (year < current)
					year += 100;
			}
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DATE, date);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime().getTime();
		}
		throw new IllegalArgumentException();
	}

	private static int parse(String string, String[] array) {
		int length = string.length();
		for (int i = 0; i < array.length; i++) {
			if (string.regionMatches(true, 0, array[i], 0, length))
				return i;
		}
		return -1;
	}

	public static boolean checkApkInstalled(Context context, String packageName) {
		PackageManager manager = context.getPackageManager();
		List<PackageInfo> pkgList = manager.getInstalledPackages(0);
		for (PackageInfo pack : pkgList) {
			if (pack.packageName.equalsIgnoreCase(packageName)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 
	* method Name:getInstalledPackageInfo    
	* method Description:  
	* @param context
	* @param packageName
	* @return   
	* PackageInfo  
	* @exception   
	* @since  1.0.0
	 */
	public static PackageInfo getInstalledPackageInfo(Context context, String packageName) {
		PackageManager manager = context.getPackageManager();
		List<PackageInfo> pkgList = manager.getInstalledPackages(0);
		for (PackageInfo pack : pkgList) {
			if (pack.packageName.equalsIgnoreCase(packageName)) {
				return pack;
			}
		}
		
		return null;
	}

	public static void shell(String command) {
		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
		}
	}

	public static long getSDSizeSpare() {
		File pathFile = Environment.getExternalStorageDirectory();
		StatFs statfs = new StatFs(pathFile.getPath());
		long nAvailaBlock = statfs.getAvailableBlocks();
		long nBlocSize = statfs.getBlockSize();
		return (nAvailaBlock * nBlocSize) / 1024;
	}

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public static void setCrossLine(TextView view) {
		view.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		view.getPaint().setAntiAlias(true);
	}

	public static void installApk(Context context, File apkFile) {
		if (apkFile == null) {
			return;
		}

		ToolUtil.shell("chmod 777 " + apkFile.getAbsolutePath());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(Uri.fromFile(apkFile), type);
		context.startActivity(intent);
	}
	
	
	/*
	 * 页面展示数据上报 广播（type＝1）
	 * 上报类型:1=pv上报，2=点击上报，3=客户端设备日志上报
	 * @param strPageId 当前页面Id,特殊页面（如商详）要重新计算pageId
	 * @param strExtraInfo 附加信息
	 * @param strPid 商品id
	 * 
	 */
	public static void reportStatisticsPV(String strPageId, String strExtraInfo, String strPid){
		String pType = ITrack.REPORT_TYPE_PV;
		String pPageId = strPageId;
		if(TextUtils.isEmpty(pPageId)) {
			return;
		}
		
		//如果pageId是商详页的，则pageId＝pid ＋ 1；
		String pItemActivtyPageId = IcsonApplication.app.getString(R.string.tag_ItemActivity);
		if(pPageId.equals(pItemActivtyPageId)) {
			pPageId = strPid + "1";
		}
		
		Intent traceintent = new Intent();
		traceintent.setAction(Config.BROADCAST_TRACE);
		
		traceintent.putExtra(ITrack.REQUEST_TYPE, pType);
		traceintent.putExtra(ITrack.REQUEST_PAGE_ID, pPageId);
		traceintent.putExtra(ITrack.REQUEST_EXT_INFO, strExtraInfo);
		traceintent.putExtra(ITrack.REQUEST_PID, strPid);
		
		IcsonApplication.app.sendBroadcast(traceintent,Config.SLEF_BROADCAST_PERMISSION);
	}
	
	public static void reportStatisticsPV(String strPageId, String strPid){
		reportStatisticsPV(strPageId, "", strPid);
	}
	
	public static void reportStatisticsPV(String strPageId){
		reportStatisticsPV(strPageId, "");
	}
	
	
	/*
	 * 点击数据上报 广播（type＝2）
	 * 上报类型:1=pv上报，2=点击上报，3=客户端设备日志上报
	 * @param strPageId 当前页面Id,特殊页面（如商详）要重新计算pageId
	 * @param strLocationId 位置Id
	 * @param strNextPageId 下一个页面Id,特殊页面（如商详）要重新计算pageId，如果点击之后没有切换页面，则strPageId = strNextPageId
	 * @param strExtraInfo 附加信息
	 * @param strPid 商品id
	 * @param strYtag 外部表示ytag
	 * @param isUpdateTag 是否更新tag（商详加入购物车时不更新tag和y_track）
	 * 
	 */
	public static void reportStatisticsClick(String strPageId, String strLocationId, String strExtraInfo, String strPid, String strYtag, boolean isUpdateTag){
		String pType = ITrack.REPORT_TYPE_CLICK;
		String pPageId = strPageId;
		if(TextUtils.isEmpty(pPageId)) {
			return;
		}
		
		//如果pageId是商详页的，则pageId＝pid ＋ 1；
		String pItemActivtyPageId = IcsonApplication.app.getString(R.string.tag_ItemActivity);
		if(pPageId.equals(pItemActivtyPageId)) {
			pPageId = strPid + "1";
		}
		
		//upage tag and y_track
		if(isUpdateTag) {
			//如果ytag不为空，优先使用ytag来更新tag和y_track		
			if(!TextUtils.isEmpty(strYtag)) {
				IcsonApplication.updateTagAndPageRoute(strYtag);
			}else{
				IcsonApplication.updateTagAndPageRoute(pPageId, strLocationId);
			}
		}
		
//		Log.d(LOG_TAG, "stat.51buy ytrack:" + IcsonApplication.getPageRoute());
		
//		Intent traceintent = new Intent();
//		traceintent.setAction(Config.BROADCAST_TRACE);
//		
//		traceintent.putExtra(ITrack.REQUEST_TYPE, pType);
//		traceintent.putExtra(ITrack.REQUEST_PAGE_ID, pPageId);
//		traceintent.putExtra(ITrack.REQUEST_LOCATION_ID, strLocationId);
//		traceintent.putExtra(ITrack.REQUEST_EXT_INFO, strExtraInfo);
//		traceintent.putExtra(ITrack.REQUEST_PID, strPid);
//		
//		IcsonApplication.app.sendBroadcast(traceintent,Config.SLEF_BROADCAST_PERMISSION);
		
	}
	
	public static void reportStatisticsClick(String strPageId, String strLocationId, String strExtraInfo, String strPid, String strYtag){
		reportStatisticsClick(strPageId, strLocationId, strExtraInfo, strPid, strYtag, true);
	}
	
	public static void reportStatisticsClick(String strPageId, String strLocationId, String strExtraInfo, String strPid){
		reportStatisticsClick(strPageId, strLocationId, strExtraInfo, strPid, "", true);
	}
	
	public static void reportStatisticsClick(String strPageId, String strLocationId, String strPid){
		reportStatisticsClick(strPageId, strLocationId, "", strPid, "", true);
	}
	
	public static void reportStatisticsClick(String strPageId, String strLocationId){
		reportStatisticsClick(strPageId, strLocationId, "", "", "", true);
	}
	
	
	/*
	 * 客户端设备日志上报（type＝3）
	 */
	public static void reportStatisticsDevice(String strExtraInfo){
		String pType = ITrack.REPORT_TYPE_DEVICE;
		
		Intent traceintent = new Intent();
		traceintent.setAction(Config.BROADCAST_TRACE);
		traceintent.putExtra(ITrack.REQUEST_TYPE, pType);
		traceintent.putExtra(ITrack.REQUEST_EXT_INFO, strExtraInfo);
		
		IcsonApplication.app.sendBroadcast(traceintent,Config.SLEF_BROADCAST_PERMISSION);
	}
	
	
	/*
	 * uid  uin qq 关系
	 * 
	 *  qq       登陆类型 		=>  uin
	 * 		  没有登陆｜QQ登陆			qq
	 * 			非QQ登陆				0
	 * 
	 */
	public static String getUinForReport() {
		String uin = "0";
		String qq = String.valueOf(Preference.getInstance().getQQAccount());
		if(TextUtils.isEmpty(qq)) {
			return uin;
		}
		
		long uid = ILogin.getLoginUid();
		if(uid != 0 && !TextUtils.isEmpty(AppStorage.getData(AppStorage.SCOPE_DEFAULT, "login_type")) &&!AppStorage.getData(AppStorage.SCOPE_DEFAULT, "login_type").equals(String.valueOf(Account.TYPE_QQ))) {
			uin = "0";
		}else{
			uin =qq;
		}
		
		return uin;
	}
	
	
/**
 * 
 * 数据点击+页面展示上报 广播
 * @param refer 前一个页面ClassName
 * @param referId 前一个页面Id
 * @param path 当前页面ClassName
 * @param pathId 当前页面Id
 * @param locationID 点击位置Id
 * @param type 上报类型
 * @param pid 商品id
 * @param tag 外部表示ytag
 * 
 */
	public static void sendTrack(String refer, String referId, String path, String pathId, String locationID, 
			String type, String pid, String ytag) {
//		Intent traceintent = new Intent();
//		traceintent.setAction(Config.BROADCAST_TRACE);
//		traceintent.putExtra(ITrack.REQUEST_PATH, path);
//		traceintent.putExtra(ITrack.REQUEST_PATH_ID, pathId);
//		traceintent.putExtra(ITrack.REQUEST_REFER, refer);
//		traceintent.putExtra(ITrack.REQUEST_REFER_ID, referId);
//		traceintent.putExtra(ITrack.REQUEST_LOCATION_ID, locationID);
//		traceintent.putExtra(ITrack.REQUEST_TYPE, type);
//		traceintent.putExtra(ITrack.REQUEST_PID, pid);
//		
//		String tag = "";
//		if(null != ytag && !ytag.equals("")) {
//			String[] ytags  = ytag.split("\\.");
//			
//			if( ytags.length > 1 ){
//				tag = ytags[1];
//			}else{
//				tag = ytags[0];
//			}
//			
//			traceintent.putExtra(ITrack.REQUEST_TAG, tag);
//			
//		}else{
//			traceintent.putExtra(ITrack.REQUEST_TAG, "");
//			tag = referId + locationID;
//		}
//		
//		IcsonApplication.app.sendBroadcast(traceintent,Config.SLEF_BROADCAST_PERMISSION);
//		 
//		IcsonApplication.setPageRoute(tag, referId);
	}
	
	public static void sendTrack(String refer, String referId, String path, String pathId, String locationID, String type, String pid){
		sendTrack(refer, referId, path, pathId, locationID, type, pid, "") ;
	}
	
	
	public static void sendTrack(String refer, String referId, String path, String pathId, String locationID, String pid) {
		sendTrack(refer, referId, path, pathId, locationID, "1", pid, "") ;
	}
	
	public static void sendTrack(String refer, String referId, String path, String pathId, String locationID) {
		sendTrack(refer, referId, path, pathId, locationID, "1", "", "") ;
	}

	public static String getApkVersionName(String absolutePath) {

		if (absolutePath == null) {
			return null;
		}

		File file = new File(absolutePath);

		if (!file.exists()) {
			return null;
		}

		PackageManager pm = IcsonApplication.app.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(absolutePath, PackageManager.GET_ACTIVITIES);
		if (info == null) {
			return null;
		}

		return info.versionName;
	}

	public static String getChannel() {
		if( ToolUtil.isSimulator() ) {
			return "simulator";
		}
		
		// Load information from assert for channel value
		String channel = "";
		try {
			InputStream input = IcsonApplication.app.getAssets().open("channel", AssetManager.ACCESS_STREAMING);
			if (input != null) 
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(input));
				String line;
				if ((line = in.readLine()) != null) {
					channel = line;
				}
				
				input.close();
				input = null;
			}
		} catch (IOException ex) {
			Log.e(LOG_TAG, ex);
			channel = "";
		}

		return channel;
	}
	
	public static boolean isSimulator() {
		//	String model = Build.MODEL;
		    String product = Build.PRODUCT;
		    boolean isEmulator = false;
		    if (product != null) {
		        isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
		    }
		    return isEmulator;
		}

	public static boolean isSDExists() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static String getExtension(String fileName) {
		if (null == fileName || fileName.length() == 0)
			return "";

		int index = fileName.lastIndexOf(".");

		if (index != -1 && index < fileName.length() - 1) {
			return fileName.substring(index);
		}

		return "";
	}
	
    private static final char[] base64EncodeChars = new char[] { 
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
		'w', 'x', 'y', 'z', '0', '1', '2', '3', 
		'4', '5', '6', '7', '8', '9', '+', '/' 
	}; 
    
	public static String base64Encode(byte[] data) { 
		StringBuffer sb = new StringBuffer(); 
		int len = data.length; 
		int i = 0; 
		int b1, b2, b3; 

		while (i < len) { 
			b1 = data[i++] & 0xff; 
			if (i == len) { 
			sb.append(base64EncodeChars[b1 >>> 2]); 
			sb.append(base64EncodeChars[(b1 & 0x3) << 4]); 
			sb.append("=="); 
			break; 
			} 
			b2 = data[i++] & 0xff; 
			if (i == len) { 
				sb.append(base64EncodeChars[b1 >>> 2]); 
				sb.append( 
				base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]); 
				sb.append("="); 
				break; 
			} 
			b3 = data[i++] & 0xff; 
			sb.append(base64EncodeChars[b1 >>> 2]); 
			sb.append( 
			base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
			sb.append( 
			base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]); 
			sb.append(base64EncodeChars[b3 & 0x3f]); 
		} 
		return sb.toString(); 
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static int compareInt(int num, int num2) {
		if(num > num2) {
			return 1;
		}else if(num < num2){
			return -1;
		}else{
			return 0;
		}
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equalsStrings(String[] a, String []b)
	{
		if(null==a && null==b)
			return true;
		else if(null == a || null == b)
			return false;
		
		if(a.length!=b.length)
			return false;
		
		for(int i = 0; i< a.length; i++)
		{
			if(!a[i].equals(b[i]))
				return false;
		}
		
		return true;
	}
	

	public static CellInfo getCellInfo(Context aContext)
	{
		TelephonyManager manager = (TelephonyManager)aContext.getSystemService(Context.TELEPHONY_SERVICE);  
	     if(null == manager)
	    	 return  null;
	     
		int netType = manager.getNetworkType();
	    CellInfo aInfo = new CellInfo();
		
		if (netType == TelephonyManager.NETWORK_TYPE_GPRS              // GSM网  
                || netType == TelephonyManager.NETWORK_TYPE_EDGE  
                || netType == TelephonyManager.NETWORK_TYPE_HSDPA)  
        {  
            GsmCellLocation gsm = ((GsmCellLocation) manager.getCellLocation());  
            if (gsm == null)  
                return null;  
            
            String operator = manager.getNetworkOperator();  
    	    aInfo.lac = gsm.getLac();  
            aInfo.mcc = operator.substring(0, 3);  
            aInfo.mnc = operator.substring(3, 5);  
            aInfo.cellId = gsm.getCid();  
            aInfo.radioType = "gsm";  
        }else if (netType == TelephonyManager.NETWORK_TYPE_CDMA        // 电信cdma网  
                || netType == TelephonyManager.NETWORK_TYPE_1xRTT  
                || netType == TelephonyManager.NETWORK_TYPE_EVDO_0  
                || netType == TelephonyManager.NETWORK_TYPE_EVDO_A)  
        {  
              
            CdmaCellLocation cdma = (CdmaCellLocation) manager.getCellLocation();     
            if (cdma == null)  
            	return null;  
              
            String operator = manager.getNetworkOperator();  
            aInfo.lac = cdma.getNetworkId();  
            aInfo.mcc = operator.substring(0, 3);  
            aInfo.mnc = ""+cdma.getSystemId();  
            aInfo.cellId = cdma.getBaseStationId();  
            aInfo.radioType = "cdma";  
              
            // 获得邻近基站信息  
            /*List<NeighboringCellInfo> list = manager.getNeighboringCellInfo();  
            int size = list.size();  
            for (int i = 0; i < size; i++) {  
  
                CellIDInfo info = new CellIDInfo();  
                info.cellId = list.get(i).getCid();  
                info.mobileCountryCode = mcc;  
                info.mobileNetworkCode = mnc;  
                info.locationAreaCode = lac;  
              
                CellID.add(info);  
            }  */
        }  
		return aInfo;
	}
}
