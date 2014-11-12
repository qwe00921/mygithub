package com.yy.android.gamenews.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;

import com.duowan.Comm.ECommAppType;
import com.duowan.gamenews.ActiveInfo;
import com.duowan.gamenews.Channel;
import com.duowan.gamenews.GetTeamListRsp;
import com.duowan.gamenews.GetUnionListRsp;
import com.duowan.gamenews.MeRsp;
import com.duowan.gamenews.PlatType;
import com.duowan.gamenews.RaceInfo;
import com.duowan.gamenews.SportRaceListRsp;
import com.duowan.gamenews.Team;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.taf.jce.JceInputStream;
import com.duowan.taf.jce.JceOutputStream;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.ui.MainActivity;

public class Preference {
	// Preference
	public static final String PREF_NAME = "gamenews_pref";

	// public static final String PREF_SCHED_NAME = "sched_pref";

	private SharedPreferences mPref;
	// private SharedPreferences mSchedPref;

	/**
	 * mCacheMap 把preference的数据保存到内存中
	 */
	private Map<String, Object> mCacheMap = new HashMap<String, Object>();
	private static final String LOG_TAG = Preference.class.getSimpleName();
	private static Preference mInstance = new Preference();
	private boolean isInited;

	public void init(Context context) {
		if (mPref == null) {
			mPref = context.getSharedPreferences(PREF_NAME,
					Context.MODE_PRIVATE);
		}
		// if(mSchedPref == null){
		// mSchedPref = context.getSharedPreferences(PREF_SCHED_NAME,
		// Context.MODE_PRIVATE);
		// }
		isInited = true;
	}

	public boolean isInited() {
		return isInited;
	}

	public static Preference getInstance() {
		return mInstance;
	}

	public SharedPreferences getPreference() {

		return mPref;
	}

	// 是否选过频道
	private static final String KEY_CHANNEL_SELECTED = "channel_selected";

	// 是否是首次启动app
	private static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
	// 是否每天首次启动app
	private static final String KEY_IS_FIRST_LAUNCH_DAILY = "is_first_launch_daily";
	private static final String KEY_LAST_LAUNCH_TIME = "last_launch_time";
	// 频道
	private static final String KEY_TOP_LIST = "top_list";
	private static final String KEY_CHANNEL_LIST = "channel_list";
	private static final String KEY_SEARCH_SUGGESTION = "channel_search_suggestion";
	private static final String KEY_LAST_GET_SUGGESTION = "last_get_suggestion";
	// 用户登录后拿到的id
	private static final String KEY_USER_INIT_LOGIN = "user_init_login";
	// 未登录时生成的id
	private static final String KEY_USER_INIT_DEFAULT = "user_init_default";
	// 是否仅在wifi下自动加载图片
	private static final String KEY_USER_ONLY_WIFI = "only_wifi";
	// 喜欢的评论
	private static final String KEY_COMMENTS_LIKE = "comments_like";
	// 赞的文章
	private static final String KEY_ARTICLES_LIKE = "articles_like";
	// 踩的文章
	private static final String KEY_ARTICLES_DISLIKE = "articles_dislike";
	// 是否仅在wifi下自动加载图片
	private static final String KEY_PUSH_MSG_ENABLED = "push_msg_enabled";

	// 用户收藏数
	private static final String KEY_MY_FAV_COUNT = "my_fav_count";

	// 引导页步骤
	private static final String KEY_GUIDE_STEP = "guide_step";
	// 活动频道
	private static final String KEY_ACTIVE_CHANNEL_LIST = "active_channel_list";

	// 首页最后选择的tab名，两个值：我的最爱和广场
	private static final String KEY_LAST_TAB_NAME = "last_tab_name";

	// 上一次检查更新的时间
	private static final String KEY_LAST_CHECK_TIME = "last_check_time";

	// 重试的状态
	public static final String RETRY_STATE = "retry_state";

	// 测试用的url
	private static final String KEY_TEST_URL = "test_url";
	// 获取测试url的ip地址
	private static final String KEY_TEST_IP = "test_ip";

	private static final String KEY_ME_RSP = "me_rsp";

	private static final String KEY_LOGIN_TYPE = "login_type";

	private static final String KEY_SAVED_ALARM = "alarm_race";
	private static final String KEY_SCHED_SAVED_TEAM = "sched_alarm_race"; // 在赛事表添加的提醒赛事；区分通过添加球队添加的赛事表
	private static final String KEY_FOLLOWED_TEAM = "follow_team";

	private static final String KEY_SPORT_RACE = "sport_race";
	private static final String KEY_TEAM = "team";

	/**
	 * 保存的app版本，如果是升级，则该值为升级前的app版本
	 */
	private static final String KEY_VERSION_CODE = "version_code";
	private static final String KEY_NEED_SHOW_LOG = "need_show_log";

	public void saveVersionCode(int versionCode) {
		mPref.edit().putInt(KEY_VERSION_CODE, versionCode).commit();
	}

	public int getVersionCode() {
		return mPref.getInt(KEY_VERSION_CODE, 0);
	}

	public boolean isAppUpdated() {
		int savedVersionCode = Preference.getInstance().getVersionCode();
		int currentVersionCode = Util.getVersionCode();
		return savedVersionCode < currentVersionCode;
	}

	public void finishAppUpdate() {
		saveVersionCode(Util.getVersionCode());
	}

	public void saveInitRsp(UserInitRsp rsp) {
		if (rsp == null) {
			return;
		}

		saveJceObject(KEY_USER_INIT_LOGIN, rsp);
	}

	/**
	 * 清除登录信息
	 */
	public void clearLoginInfo() {
		saveJceObject(KEY_USER_INIT_LOGIN, null);
	}

	public UserInitRsp getInitRsp() {
		UserInitRsp tempRsp = new UserInitRsp();
		UserInitRsp rsp = (UserInitRsp) getJceObject(KEY_USER_INIT_LOGIN,
				tempRsp);
		if (rsp == null) {
			rsp = (UserInitRsp) getJceObject(KEY_USER_INIT_DEFAULT, tempRsp);
		}

		return rsp;
	}

	public void setXinGeListData(String key, List<Channel> channels) {
		if (channels != null && channels.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < channels.size(); j++) {
				Channel channel = channels.get(j);
				if (channel != null) {
					sb.append(channel.getName() + "_" + channel.getId());
					sb.append(",");
				}
			}
			String value = sb.toString().substring(0,
					sb.toString().length() - 1);
			mPref.edit().putString(key, value).commit();
		}
	}

	public void setXinGeData(String key, String value) {
		mPref.edit().putString(key, value).commit();
	}

	public String getXinGeData(String key) {
		return mPref.getString(key, "");
	}

	public boolean isUserLogin() {
		UserInitRsp rsp = (UserInitRsp) getJceObject(KEY_USER_INIT_LOGIN,
				new UserInitRsp());
		return rsp != null && rsp.getAccessToken() != null;
	}

	public void saveDefaultInitRsp(UserInitRsp rsp) {

		if (rsp != null) {
			saveJceObject(KEY_USER_INIT_DEFAULT, rsp);
		}
	}

	public void saveMyFavCount(int count) {
		saveObject(KEY_MY_FAV_COUNT, count);
	}

	public int getMyFavCount() {
		Integer count = getObject(KEY_MY_FAV_COUNT);
		if (count == null) {
			return 0;
		}
		return count;
	}

	/**
	 * 设置是否仅在wifi下自动加载图片
	 */
	public void setOnlyWifi(boolean isOnlyWifi) {
		mPref.edit().putBoolean(KEY_USER_ONLY_WIFI, isOnlyWifi).commit();
		mCacheMap.put(KEY_USER_ONLY_WIFI, isOnlyWifi);
	}

	/**
	 * 是否仅在wifi下自动加载图片
	 * 
	 * @return
	 */
	public boolean isOnlyWifi() {
		if (mCacheMap.containsKey(KEY_USER_ONLY_WIFI)) {
			return (Boolean) mCacheMap.get(KEY_USER_ONLY_WIFI);
		}

		boolean isOnlyWifi = mPref.getBoolean(KEY_USER_ONLY_WIFI, false);
		mCacheMap.put(KEY_USER_ONLY_WIFI, isOnlyWifi);
		return isOnlyWifi;
	}

	public void saveActiveChannelList(List<ActiveInfo> list) {
		saveJceObject(KEY_ACTIVE_CHANNEL_LIST, list);
	}

	public List<ActiveInfo> getActiveChannelList() {
		List<ActiveInfo> list = new ArrayList<ActiveInfo>();
		list.add(new ActiveInfo());

		return getJceObject(KEY_ACTIVE_CHANNEL_LIST, list);
	}

	/**
	 * 设置频道仓库是否需要重试
	 * 
	 * @param enabled
	 */
	public void setRetryState(boolean enabled) {
		mPref.edit().putBoolean(RETRY_STATE, enabled).commit();
	}

	/**
	 * 获取频道仓库是否需要重试
	 * 
	 * @return
	 */
	public boolean getRetryState() {
		return mPref.getBoolean(RETRY_STATE, false);
	}

	/**
	 * 设置是否推送通知
	 * 
	 * @param enabled
	 */
	public void setPushMsgEnabled(boolean enabled) {
		mPref.edit().putBoolean(KEY_PUSH_MSG_ENABLED, enabled).commit();
	}

	/**
	 * 设置上次选中的tab名
	 * 
	 * @return
	 */
	public String getLastTabName() {
		String defaultTab = MainActivity.TAG_NAME_INFO;
		if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
			defaultTab = MainActivity.TAG_NAME_NEWS;
		}
		return mPref.getString(KEY_LAST_TAB_NAME, defaultTab);
	}

	/**
	 * 获取上次选中的tab名
	 * 
	 * @param tabName
	 */
	public void setLastTabName(String tabName) {
		mPref.edit().putString(KEY_LAST_TAB_NAME, tabName).commit();
	}

	/**
	 * 设置上一次检查更新的时间
	 * 
	 * @param time
	 */
	public void setLastCheckTime(long time) {
		mPref.edit().putLong(KEY_LAST_CHECK_TIME, time).commit();
	}

	/**
	 * 获取上一次检查更新的时间
	 * 
	 * @return
	 */
	public long getLastCheckTime() {
		return mPref.getLong(KEY_LAST_CHECK_TIME, 0);
	}

	/**
	 * 是否推送通知
	 * 
	 * @return
	 */
	public boolean isPushMsgEnabled() {
		return mPref.getBoolean(KEY_PUSH_MSG_ENABLED, true);
	}

	public UserInitRsp getDefaultInitRsp() {
		UserInitRsp rsp = getJceObject(KEY_USER_INIT_DEFAULT, new UserInitRsp());// new
		// UserInitRsp();
		return rsp;
	}

	/**
	 * 判断是否是第一次启动，该方法只有在第一次被调用时返回true
	 * 
	 * @return true if this method is called at the first time. false otherwise
	 */
	public boolean isFirstLaunch() {
		boolean isFirstLaunch = mPref.getBoolean(KEY_IS_FIRST_LAUNCH, true);
		return isFirstLaunch;
	}

	public void finishFirstLaunch() {
		mPref.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).commit();
	}

	/**
	 * 获取头条频道
	 * 
	 * @return
	 */
	public List<Channel> getTopChannelList() {
		ArrayList<Channel> list = new ArrayList<Channel>();
		list.add(new Channel());
		List<Channel> channelList = (List<Channel>) getJceObject(KEY_TOP_LIST,
				list);// new
						// ArrayList<Channel>();
		return channelList;
	}

	/**
	 * 保存头条频道
	 * 
	 * @return
	 */
	public void saveTopChannelList(List<Channel> channelList) {
		if (channelList == null) {
			Log.w(LOG_TAG, "[saveMyFavorChannelList] channelList is null");
			return;
		}

		Set<Channel> tempSet = new LinkedHashSet<Channel>();
		tempSet.addAll(channelList);
		channelList.clear();
		channelList.addAll(tempSet);
		saveJceObject(KEY_TOP_LIST, channelList);
	}

	public List<Channel> getMyFavorChannelList() {
		ArrayList<Channel> list = new ArrayList<Channel>();
		list.add(new Channel());
		List<Channel> channelList = (List<Channel>) getJceObject(
				KEY_CHANNEL_LIST, list);// new
										// ArrayList<Channel>();
		return channelList;
	}

	public void saveLastGetSuggestionTime(long timeV) {
		mPref.edit().putLong(KEY_LAST_GET_SUGGESTION, timeV).commit();
	}

	public long getLastGetSuggestionTime() {
		return mPref.getLong(KEY_LAST_GET_SUGGESTION, 0);
	}

	private void saveJceObject(String key, Object object) {
		JceOutputStream os = new JceOutputStream();

		try {
			os.write(object, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String productBase64 = new String(Base64.encode(os.toByteArray(),
				Base64.DEFAULT));

		mPref.edit().putString(key, productBase64).commit();
	}

	@SuppressWarnings("unchecked")
	private <T> T getJceObject(String key, T t) {

		String productBase64 = mPref.getString(key, "");
		if (productBase64 == null || "".equals(productBase64)) {
			return null;
		}
		byte[] data = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);
		JceInputStream is = new JceInputStream(data);
		T returnValue = null;
		try {
			returnValue = (T) is.read(t, 0, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}

	private void saveObject(String key, Object object) {
		mCacheMap.put(key, object);
		if (object == null) {
			mPref.edit().putString(key, "").commit();
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);

			String productBase64 = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));
			mPref.edit().putString(key, productBase64).commit();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getObject(String key) {
		if (mCacheMap.containsKey(key)) {
			return (T) mCacheMap.get(key);
		}
		String productBase64 = mPref.getString(key, "");

		byte[] data = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = null;
		T obj = null;
		try {
			ois = new ObjectInputStream(bais);
			obj = (T) ois.readObject();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		mCacheMap.put(key, obj);

		return obj;
	}

	// private Object getObject(String key) {
	// if (mCacheMap.containsKey(key)) {
	// return mCacheMap.get(key);
	// }
	// String productBase64 = mPref.getString(key, "");
	//
	// byte[] data = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);
	//
	// ByteArrayInputStream bais = new ByteArrayInputStream(data);
	// ObjectInputStream ois = null;
	// Object obj = null;
	// try {
	// ois = new ObjectInputStream(bais);
	// obj = ois.readObject();
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// mCacheMap.put(key, obj);
	//
	// return obj;
	// }

	public void saveMyFavorChannelList(List<Channel> channelList) {
		if (channelList == null) {
			Log.w(LOG_TAG, "[saveMyFavorChannelList] channelList is null");
			return;
		}

		Set<Channel> tempSet = new LinkedHashSet<Channel>();
		tempSet.addAll(channelList);
		channelList.clear();
		channelList.addAll(tempSet);
		saveJceObject(KEY_CHANNEL_LIST, channelList);
	}

	public void saveMyCommentsLike(Set<String> commentList) {
		if (commentList == null) {
			Log.w(LOG_TAG, "[saveMyCommentsLike] commentList is null");
			return;
		}
		saveObject(KEY_COMMENTS_LIKE, commentList);
	}

	public Set<String> getMyCommentsLike() {
		Set<String> commentList = (Set<String>) getObject(KEY_COMMENTS_LIKE);
		return commentList;
	}

	public void saveMyArticlesLike(Set<Long> articleList) {
		if (articleList == null) {
			Log.w(LOG_TAG, "[saveMyCommentsLike] commentList is null");
			return;
		}
		saveObject(KEY_ARTICLES_LIKE, articleList);
	}

	public Set<Long> getMyArticlesLike() {
		Set<Long> articleList = (Set<Long>) getObject(KEY_ARTICLES_LIKE);
		return articleList;
	}

	public void saveMyArticlesDislike(Set<Long> articleList) {
		if (articleList == null) {
			Log.w(LOG_TAG, "[saveMyCommentsLike] commentList is null");
			return;
		}
		saveObject(KEY_ARTICLES_DISLIKE, articleList);
	}

	public Set<Long> getMyArticlesDislike() {
		Set<Long> articleList = (Set<Long>) getObject(KEY_ARTICLES_DISLIKE);
		return articleList;
	}

	public void saveSearchSuggestion(Map<String, ArrayList<String>> map) {
		if (map == null) {
			return;
		}
		saveObject(KEY_SEARCH_SUGGESTION, map);
	}

	public Map<String, ArrayList<String>> getSearchSuggestion() {
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		map = getObject(KEY_SEARCH_SUGGESTION);
		return map;
	}

	public void setTestUrl(String url) {
		mPref.edit().putString(KEY_TEST_URL, url).commit();
	}

	public String getTestUrl() {
		return mPref.getString(KEY_TEST_URL, "");
	}

	public void setTestIp(String ip) {

		mPref.edit().putString(KEY_TEST_IP, ip).commit();
	}

	public String getTestIp() {
		return mPref.getString(KEY_TEST_IP, "");
	}

	public static final int STEP_0 = 0;
	public static final int STEP_1 = 1;
	public static final int STEP_2 = 2;
	public static final int STEP_3 = 3;
	public static final int STEP_DONE = 1;

	public int getCurrentGuideStep() {
		return mPref.getInt(KEY_GUIDE_STEP, STEP_0);
	}

	public void setGuideStep(int step) {
		mPref.edit().putInt(KEY_GUIDE_STEP, step).commit();
	}

	public void setMeRsp(MeRsp rsp) {
		saveJceObject(KEY_ME_RSP, rsp);
	}

	public MeRsp getMeRsp() {
		return getJceObject(KEY_ME_RSP, new MeRsp());
	}

	public void setLoginType(int type) {
		mPref.edit().putInt(KEY_LOGIN_TYPE, type).commit();
	}

	public int getLoginType() {
		return mPref.getInt(KEY_LOGIN_TYPE, PlatType._PLAT_TYPE_DEFAULT);
	}

	/**
	 * 获取赛事表数据
	 * 
	 * @return
	 */
	public SportRaceListRsp getSportRaceRsp() {
		SportRaceListRsp rsp = (SportRaceListRsp) getJceObject(KEY_SPORT_RACE,
				new SportRaceListRsp());
		return rsp;
	}

	public void saveSportRaceRsp(SportRaceListRsp rsp) {
		if (rsp == null) {
			return;
		}
		saveJceObject(KEY_SPORT_RACE, rsp);
	}

	/**
	 * 获取球队数据
	 * 
	 * @return
	 */
	public GetTeamListRsp getTeamListRsp() {
		GetTeamListRsp rsp = (GetTeamListRsp) getJceObject(KEY_TEAM,
				new GetTeamListRsp());
		return rsp;
	}

	public void saveTeamListRsp(GetTeamListRsp rsp) {
		if (rsp == null) {
			return;
		}
		saveJceObject(KEY_TEAM, rsp);
	}

	/**
	 * 获取需要提醒的所有赛事列表
	 */
	public List<RaceInfo> getAlarmRaceList() {

		List<RaceInfo> list = new ArrayList<RaceInfo>();
		list.add(new RaceInfo());

		List<RaceInfo> retList = getJceObject(KEY_SAVED_ALARM, list);
		if (retList == null) {
			retList = new ArrayList<RaceInfo>();
		}
		return retList;
	}

	public void saveAlarmRaceList(List<RaceInfo> list) {
		saveJceObject(KEY_SAVED_ALARM, list);
	}

	/**
	 * 获取在赛事表添加提醒的赛事列表
	 */
	public List<RaceInfo> getSchedAlarmRaceList() {

		List<RaceInfo> list = new ArrayList<RaceInfo>();
		list.add(new RaceInfo());

		List<RaceInfo> retList = getJceObject(KEY_SCHED_SAVED_TEAM, list);
		if (retList == null) {
			retList = new ArrayList<RaceInfo>();
		}
		return retList;
	}

	public void saveSchedAlarmRaceList(List<RaceInfo> list) {
		saveJceObject(KEY_SCHED_SAVED_TEAM, list);
	}

	/**
	 * 获取提醒的球队列表
	 */
	public List<Team> getFollowTeamList() {

		List<Team> list = new ArrayList<Team>();
		list.add(new Team());

		List<Team> retList = getJceObject(KEY_FOLLOWED_TEAM, list);
		if (retList == null) {
			retList = new ArrayList<Team>();
		}
		return retList;
	}

	public void saveFollowTeamList(List<Team> list) {
		saveJceObject(KEY_FOLLOWED_TEAM, list);
	}

	/**
	 * 获取公会数据
	 * 
	 * @return
	 */
	public GetUnionListRsp getUnionListRsp(String key) {
		GetUnionListRsp rsp = (GetUnionListRsp) getJceObject(key,
				new GetUnionListRsp());
		return rsp;
	}

	public void saveUnionListRsp(String key, GetUnionListRsp rsp) {
		if (rsp == null) {
			return;
		}
		saveJceObject(key, rsp);
	}

	public void setNeedShowLog(boolean needShowLog) {
		mPref.edit().putBoolean(KEY_NEED_SHOW_LOG, needShowLog).commit();
	}

	public boolean getNeedShowLog() {
		return mPref.getBoolean(KEY_NEED_SHOW_LOG, false);
	}

	public void setChannelSelected(boolean selected) {
		mPref.edit().putBoolean(KEY_CHANNEL_SELECTED, selected).commit();
	}

	public boolean isChannelSelected() {
		return mPref.getBoolean(KEY_CHANNEL_SELECTED, false);
	}

	public boolean isFirstLaunchDaily() {

		return mPref.getBoolean(KEY_IS_FIRST_LAUNCH_DAILY, true);
	}

	public void finishFirstLaunchDaily() {
		mPref.edit().putBoolean(KEY_IS_FIRST_LAUNCH_DAILY, false).commit();
	}

	/**
	 * 记录启动时间
	 */
	public void recordLaunchTime() {
		long lastLaunchTime = mPref.getLong(KEY_LAST_LAUNCH_TIME,
				System.currentTimeMillis());

		long now = System.currentTimeMillis();

		Calendar cLast = Calendar.getInstance();
		cLast.setTimeInMillis(lastLaunchTime);

		Calendar cNow = Calendar.getInstance();
		cNow.setTimeInMillis(now);

		Editor editor = mPref.edit();
		if (cLast.get(Calendar.DATE) != cNow.get(Calendar.DATE)) {
			editor.putBoolean(KEY_IS_FIRST_LAUNCH_DAILY, true);
		}
		editor.putLong(KEY_LAST_LAUNCH_TIME, now).commit();
	}
}
