package com.yy.android.gamenews.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListAdapter;

import com.duowan.android.base.model.BaseModel.ResponseListener;
import com.duowan.gamenews.ArticleFlag;
import com.duowan.gamenews.ArticleInfo;
import com.duowan.gamenews.ArticleType;
import com.duowan.gamenews.GetVideoUrlRsp;
import com.duowan.gamenews.LoginActionFlag;
import com.duowan.gamenews.RefreshType;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.gamenews.VideoFlag;
import com.duowan.taf.jce.JceStruct;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.event.CheckExpireEvent;
import com.yy.android.gamenews.event.CommentEvent;
import com.yy.android.gamenews.event.FirstButtomTabEvent;
import com.yy.android.gamenews.event.FragmentCallbackEvent;
import com.yy.android.gamenews.event.LikeEvent;
import com.yy.android.gamenews.event.RefreshEvent;
import com.yy.android.gamenews.event.SecondButtomTabEvent;
import com.yy.android.gamenews.event.ThirdButtomTabEvent;
import com.yy.android.gamenews.jcewrapper.GetArticleListRspLocal;
import com.yy.android.gamenews.model.ArticleModel;
import com.yy.android.gamenews.plugin.cartport.CartDetailImageActivity;
import com.yy.android.gamenews.ui.common.ImageAdapter;
import com.yy.android.gamenews.ui.common.UiUtils;
import com.yy.android.gamenews.ui.view.AppDialog;
import com.yy.android.gamenews.ui.view.AppDialog.OnClickListener;
import com.yy.android.gamenews.util.IPageCache;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.StatsUtil;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.gamenews.util.thread.BackgroundTask;
import com.yy.android.sportbrush.R;

import de.greenrobot.event.EventBus;

/**
 * 文章列表Fragment，此fragment的数据视图展示ArticleInfo的列表
 * 
 * 对以下事件进行了封装： 1. 数据视图点击事件 2. 检查数据过期 3. 对列表中已查看过的数据进行区别化（变灰） 4. 缓存数据的读取及保存
 * 
 * @author Administrator
 * 
 * @param <RSP>
 */
@SuppressLint("ValidFragment")
public abstract class ArticleListFragment<RSP extends JceStruct, WRAPPER extends GetArticleListRspLocal<RSP>>
		extends BaseListFragment<ArticleInfo> {
	// private static final String TAG =
	// ArticleListFragment.class.getSimpleName();
	private static final String KEY_VIEWED_LIST = "viewed_list";
	private static final String KEY_RESPONSE = "response";
	protected static final String KEY_CACHE_SIZE = "cache_size";

	protected IPageCache mPageCache;
	private ArticleListAdapter mAdapter;
	private boolean mIsDarkTheme;

	private ArrayList<Long> mViewedList = new ArrayList<Long>();
	private WRAPPER mRspWrapper;
	private boolean mIsLoading;
	private int mCacheSize;
	protected Preference mPref;

	public static final String LOG_TAG = "ArticleListFragment";

	public ArticleListFragment() {
		mPageCache = new IPageCache();
		mPref = Preference.getInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		EventBus.getDefault().register(this);
		mRspWrapper = initRspWrapper();
		if (bundle != null) {
			mViewedList = (ArrayList<Long>) bundle
					.getSerializable(KEY_VIEWED_LIST);
			if (mViewedList == null) {
				mViewedList = new ArrayList<Long>();
			}

			mRspWrapper.setObject((RSP) bundle.getSerializable(KEY_RESPONSE));
			mCacheSize = bundle.getInt(KEY_CACHE_SIZE);
		} else {
			Bundle args = getArguments();
			if(args != null) {
				mCacheSize = args.getInt(KEY_CACHE_SIZE);
			}
		}
		if (mCacheSize == 0) {
			mCacheSize = Constants.CACH_SIZE_HOME_ARTI_LIST;
		}
	}

	protected abstract WRAPPER initRspWrapper();

	protected RSP getRspInstance() {
		if (mRspWrapper != null) {
			return mRspWrapper.getObject();
		}
		return null;
	}

	public void setIsDarkTheme(boolean isDark) {
		mIsDarkTheme = isDark;
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	protected ImageAdapter<ArticleInfo> initAdapter() {
		if (mAdapter == null) {
			mAdapter = new ArticleListAdapter(getActivity());
			// mAdapter.setChannel(mChannel);
			if (needShowViewedArticle()) {
				mAdapter.setViewedArticleList(mViewedList);
			}
			mAdapter.setIsDarkTheme(mIsDarkTheme);
		}
		return mAdapter;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(KEY_VIEWED_LIST, mViewedList);
		if (mRspWrapper != null) {
			outState.putSerializable(KEY_RESPONSE, mRspWrapper.getObject());
		}
		outState.putInt(KEY_CACHE_SIZE, mCacheSize);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		if (mRspWrapper.getObject() == null && needLoadData()) { // 只有在第一次进入界面加载，避免因被系统杀掉而再次加载
			showView(VIEW_TYPE_LOADING);
			new BgTask().execute();
		}
		if (mIsDarkTheme) {
			getDataView().setBackgroundResource(
					R.color.global_list_item_bg_dark);
		}
		super.onViewCreated(view, savedInstanceState);
		Log.d(LOG_TAG, "onViewCreated");
	}

	@Override
	public void onDestroyView() {
		setAdapter(null);
		super.onDestroyView();
	}

	@Override
	protected ImageAdapter<ArticleInfo> initBannerAdapter() {
		return new ArticleListBannerAdapter(getActivity());
	}

	@Override
	public void onResume() {

		checkInfoUpdate();

		super.onResume();
	}

	/**
	 * 检查评论数，点赞数等是否有更新，若有更新，则刷新adapter
	 */
	private void checkInfoUpdate() {
		if (mSelectedPos != -1) {
			Adapter adapter = getDataViewAdapter();
			if (adapter != null) {
				ArticleInfo model = (ArticleInfo) adapter.getItem(mSelectedPos);

				boolean hasChanged = false;
				int commentCount = model.commentCount;
				int likeCount = model.praiseCount;

				if (mCommentEvent != null) {
					commentCount = mCommentEvent.commentCount;
				}

				if (mLikeEvent != null) {
					likeCount = mLikeEvent.likeCount;
				}

				if (commentCount != model.commentCount) {

					model.setCommentCount(commentCount);
					hasChanged = true;
				}

				if (likeCount != model.praiseCount) {

					model.setPraiseCount(likeCount);
					hasChanged = true;
				}

				if (hasChanged) {
					mAdapter.notifyDataSetChanged();
					saveListToDisk(getDataSource());
				}
			}

			mCommentEvent = null;
			mLikeEvent = null;
			mSelectedPos = -1;
		}
	}

	private CommentEvent mCommentEvent;

	public void onEvent(CommentEvent event) {
		mCommentEvent = event;
	}

	private LikeEvent mLikeEvent;

	public void onEvent(LikeEvent event) {
		mLikeEvent = event;
	}

	public void onEvent(RefreshEvent event) {
		if (event == null) {
			return;
		}
		if (needCheckRefresh(event)) {
			callRefresh();
		}
	}

	public void onEvent(CheckExpireEvent event) {
		if (event == null) {
			return;
		}

		if (needCheckExpire(event)) {
			checkExpire();
		}
	}

	@Override
	public void onScrollStateChanged(View view, int scrollState) {
		if ((scrollState == OnScrollListener.SCROLL_STATE_FLING || scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)) {
			mAdapter.pause();
		} else {
			mAdapter.resume();
		}
		super.onScrollStateChanged(view, scrollState);

	}

	private int mSelectedPos = -1;

	@Override
	public void onItemClick(View parent, Adapter adapter, View view,
			int position, long id) {
		ArticleInfo model = (ArticleInfo) adapter.getItem(position);
		onItemClick(model, position, TYPE_LIST);
		super.onItemClick(parent, adapter, view, position, id);
	}

	private static final int TYPE_BANNER = 1;
	private static final int TYPE_LIST = 2;

	@SuppressWarnings("unchecked")
	protected void onItemClick(final ArticleInfo model, int position, int type) {

		if (model != null) {
			if (TYPE_LIST == type) {
				mSelectedPos = position;
			}
			if (mViewedList != null && !mViewedList.contains(model.getId())) {
				if (mViewedList.size() >= Constants.CACHE_SIZE_VIEWED_ARTI_LIST) {
					mViewedList.remove(0);
				}
				mViewedList.add(model.getId());

				ArrayList<Long> savedList = (ArrayList<Long>) mViewedList
						.clone();
				mSaveCacheTask.execute(Constants.CACHE_KEY_VIEWED_ARTICLE_LIST,
						savedList, Constants.CACHE_DURATION_FOREVER, false);
				mAdapter.notifyDataSetChanged();
			}
			boolean isRedirect = (model.getFlag() & ArticleFlag._ARTICLE_FLAG_REDIRECT) != 0;
			if (isRedirect) {
				startWeb(model, "");
				addStatisticsEvent(model.getId(), model.getTitle());
				sendArticlestatics(FirstButtomTabEvent.HEAD_INFO);
				return;
			}

			switch (model.getArticleType()) {
			case ArticleType._ARTICLE_TYPE_ARTICLE: {

				ArticleDetailActivity.startArticleDetailActivity(getActivity(),
						model);
				addStatisticsEvent(model.getId(), model.getTitle());
				sendArticlestatics(SecondButtomTabEvent.ORDER_INFO);
				break;
			}
			case ArticleType._ARTICLE_TYPE_CART_IMAGE_COLUMN: {
				CartDetailImageActivity.startCartDetailActivity(getActivity(),
						model.getId(), model.getTitle());
				break;
			}
			case ArticleType._ARTICLE_TYPE_SPECIAL: {
				ArticleListActivity.startSpecialListActivity(getActivity(),
						model.getId());
				addStatisticsEvent(model.getId(), model.getTitle());
				sendArticlestatics(SecondButtomTabEvent.ORDER_INFO);
				break;
			}
			case ArticleType._ARTICLE_TYPE_VIDEO: {
				if (Util.isNetworkConnected()) {
					if (!Util.isWifiConnected()) {
						UiUtils.showDialog(getActivity(),
								R.string.global_caption,
								R.string.play_video_no_wifi,
								R.string.global_ok, R.string.global_cancel,
								new OnClickListener() {

									@Override
									public void onDismiss() {
										// TODO Auto-generated method stub

									}

									@Override
									public void onDialogClick(int nButtonId) {
										if (nButtonId == AppDialog.BUTTON_POSITIVE) {
											playVideo(model);
										}
									}
								});
					} else {
						playVideo(model);
					}
				} else {
					ToastUtil.showToast(R.string.global_network_error);
				}
				break;
			}
			case ArticleType._ARTICLE_TYPE_BANG:
			case ArticleType._ARTICLE_TYPE_ACTIVITY:
			case ArticleType._ARTICLE_TYPE_TEQUAN:
			case ArticleType._ARTICLE_TYPE_CAIDAN: {
				startWeb(model, AppWebActivity.TITLE_HD);
				break;
			}
			}
		}
	}

	private void playVideo(ArticleInfo model) {

		ArticleModel.getVideoUrlReq(new ResponseListener<GetVideoUrlRsp>(
				getActivity()) {
			public void onResponse(GetVideoUrlRsp rsp) {
				if (rsp != null) {
					String url = rsp.url;
					switch (rsp.videoFlag) {
					case VideoFlag._VIDEO_FLAG_REDIRECT: {
						AppWebActivity.startWebActivityFromNotice(
								getActivity(), url);
						break;
					}
					case VideoFlag._VIDEO_FLAG_SOURCE: {
						VideoPlayerActivity.startVideoPlayerActivity(
								getActivity(), rsp.getTitle(), rsp.getUrl());
						break;
					}
					}
				}
			};
		}, model.getId());
	}

	private void startWeb(ArticleInfo model, String title) {
		Intent intent = new Intent(getActivity(), AppWebActivity.class);
		String url = model.getSourceUrl();
		if (model.getArticleType() == ArticleType._ARTICLE_TYPE_TEQUAN) {
			UserInitRsp rsp = mPref.getInitRsp();
			String token = "";
			if (rsp != null) {
				token = rsp.extraInfo
						.get(LoginActionFlag._LOGIN_ACTION_FLAG_YY_TOKEN);
			}
			// String channelName = getString(R.string.channelname);
			// if ("test".equals(channelName) || "dev".equals(channelName)) {
			// url =
			// "http://mtq.yy.com/utl/shuazilogin?url=http%3A%2F%2Fmtq.yy.com%2F%23detail.index%3Ftid%3D14853%26type%3Dissue&token="
			// + token;// 测试地址
			// } else {
			// url = url + token;
			// }
			if (token != null && (!TextUtils.isEmpty(token))) {
				url = url + token;
			}
		}
		intent.putExtra(AppWebActivity.KEY_URL, url);
		intent.putExtra(AppWebActivity.KEY_TITLE, title);
		startActivity(intent);
	}

	private void sendArticlestatics(String tab) {
		if (tab.equals(FirstButtomTabEvent.HEAD_INFO)) {
			FirstButtomTabEvent event = new FirstButtomTabEvent();
			event.setType(FirstButtomTabEvent._ARTICLE_INFO);
			event.setEventId(FirstButtomTabEvent.HEAD_INFO);
			event.setKey(FirstButtomTabEvent.ARTICLE_INFO);
			event.setValue(FirstButtomTabEvent.ARTICLE_INFO_NAME);
			EventBus.getDefault().post(event);
		} else if (tab.equals(SecondButtomTabEvent.ORDER_INFO)) {
			SecondButtomTabEvent event = new SecondButtomTabEvent();
			event.setType(SecondButtomTabEvent._ARTICLE_INFO);
			event.setEventId(SecondButtomTabEvent.ORDER_INFO);
			event.setKey(SecondButtomTabEvent.ARTICLE_INFO);
			event.setValue(SecondButtomTabEvent.ARTICLE_INFO_NAME);
			EventBus.getDefault().post(event);
		} else if (tab.equals(ThirdButtomTabEvent.THIRD_TAB_INFO)) {

		}

	}

	public void addStatisticsEvent(long id, String title) {
		// 下面tab统计（方案一）
		StatsUtil.statsReport(getActivity(), "stats_read_article_tab",
				ArticleDetailActivity.CURRENT_BUTTON_TAB, title);
		StatsUtil.statsReportByMta(getActivity(), "stats_read_article_tab",
				ArticleDetailActivity.CURRENT_BUTTON_TAB, "(" + id + ")"
						+ title);
		StatsUtil.statsReportByHiido("stats_read_article_tab",
				ArticleDetailActivity.CURRENT_BUTTON_TAB + title);
		// 下面tab统计（方案二）
		StatsUtil.statsReport(getActivity(),
				"stats_read_article_tab_second_method",
				ArticleDetailActivity.CURRENT_BUTTON_TAB, title);
		StatsUtil.statsReportByMta(getActivity(),
				"stats_read_article_tab_second_method", "bottom_tab_name",
				ArticleDetailActivity.CURRENT_BUTTON_TAB);
		StatsUtil.statsReportByHiido("stats_read_article_tab_second_method",
				ArticleDetailActivity.CURRENT_BUTTON_TAB);
	}

	@Override
	public void onBannerItemClick(View view, ListAdapter adapter, int position) {
		ArticleListBannerAdapter bannerAdapter = (ArticleListBannerAdapter) adapter;
		if (bannerAdapter != null) {
			ArticleInfo info = bannerAdapter.getItem(position);
			onItemClick(info, position, TYPE_BANNER);
		}

		super.onBannerItemClick(view, bannerAdapter, position);
	}

	protected abstract void requestDataImpl(int refresh, Object attachInfo);

	protected final void requestData(final int refresh) {
		if (mIsLoading) {
			return;
		}
		if (refresh == RefreshType._REFRESH_TYPE_REFRESH) {
			notifyListener(FragmentCallbackEvent.FRGMT_LIST_REFRESHING, null);
		}

		mIsLoading = true;

		Object attachInfo = null;
		if (mRspWrapper != null) {
			attachInfo = mRspWrapper.getAttachInfo();
		}
		requestDataImpl(refresh, attachInfo);
	}

	private void prepareEmptyText(boolean error) {
		String emptyText = getEmptyText(error);

		if (TextUtils.isEmpty(emptyText)) {
			if (error) {
				emptyText = strEmptyReload;
			} else {
				emptyText = strEmptyNoData;
			}
		}
		setEmptyText(emptyText);
	}

	protected String getEmptyText(boolean error) {
		return "";
	}

	protected boolean needShowViewedArticle() {
		return true;
	}

	protected void requestFinishImpl(int refresh, RSP data, boolean error) {
		ArrayList<ArticleInfo> dataList = null;
		ArrayList<ArticleInfo> localList = getDataSource(); // 本地列表
		boolean hasMore = false;
		if (data != null) {

			if (mRspWrapper != null) {
				hasMore = mRspWrapper.hasMore();
				dataList = mRspWrapper.getArticleList();
			}

			if (dataList != null && localList != null) {
				for (ArticleInfo info : dataList) {
					for (int i = 0; i < localList.size(); i++) {
						ArticleInfo localInfo = localList.get(i);
						if (localInfo.getId() == info.getId()) {
							localList.remove(i);
							i--;
						}
					}
				}
			}

			// 只有下拉刷新的时候需要更新banner
			if (refresh == RefreshType._REFRESH_TYPE_LOAD_MORE) {

				if (mRspWrapper != null) {
					mRspWrapper.setPictopList(getBannerDataSource());
				}
			} else {
				updateBanner(mRspWrapper == null ? null : mRspWrapper
						.getPictopList());
				// updateHotChannelView(mRsp.getHotChannel(), dataList);
			}
		}

		requestFinish(refresh, dataList, hasMore, false);

		Log.d(LOG_TAG, "requestFinish");

		/**
		 * 需求：下拉刷新后，列表中展示最新的CACH_SIZE_HOME_ARTI_LIST篇文章
		 */
		if (refresh == RefreshType._REFRESH_TYPE_REFRESH) {
			ArrayList<ArticleInfo> dataSource = getDataSource();
			if (dataSource != null) {
				int size = dataSource.size();
				if (size > mCacheSize) {
					ArrayList<ArticleInfo> replaceList = new ArrayList<ArticleInfo>();
					replaceList.addAll(dataSource);

					dataSource.clear();
					dataSource.addAll(replaceList.subList(0, mCacheSize));

					getAdapter().notifyDataSetChanged();
				}
			}
		}
		// 保存到本地
		saveListToDisk(getDataSource());
	}

	protected final void requestFinish(int refresh, RSP data, boolean error) {

		prepareEmptyText(error);

		mIsLoading = false;

		if (!error) {
			mRspWrapper.setObject(data);
		}
		requestFinishImpl(refresh, data, error);
	}

	protected SaveCacheTask mSaveCacheTask = new SaveCacheTask();

	public class SaveCacheTask extends BackgroundTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {

			String key = (String) params[0];
			Object value = params[1];
			int duration = (Integer) params[2];
			boolean isJceObject = (Boolean) params[3];

			if (isJceObject) {
				mPageCache.setJceObject(key, value, duration);
			} else {
				mPageCache.setObject(key, value, duration);
			}
			return null;
		}
	}

	protected boolean needSaveToDisk() {
		return true;
	}

	private void saveListToDisk(ArrayList<ArticleInfo> list) {

		if (!needSaveToDisk()) {
			return;
		}
		ArrayList<ArticleInfo> savedList = new ArrayList<ArticleInfo>();
		ArrayList<ArticleInfo> totalList = list;
		if (totalList == null || mRspWrapper == null
				|| mRspWrapper.getObject() == null) {
			return;
		}
		if (totalList.size() > mCacheSize && mCacheSize > 0) {
			savedList.addAll(totalList.subList(0, mCacheSize));
		} else {
			savedList.addAll(totalList);
		}
		if (savedList.size() > 0) {
			mRspWrapper.setArticleList(savedList);
		}

		saveResponseToDisk();
	}

	protected void saveResponseToDisk() {
		if (mRspWrapper == null) {
			return;
		}
		RSP savedRsp = mRspWrapper.clone();
		String key = getKey();
		mSaveCacheTask.execute(key, savedRsp, getDuration(), true);
	}

	protected abstract RSP newRspObject();

	protected RSP getResponseFromDisk() {
		RSP rsp = mPageCache.getJceObject(getKey(), newRspObject());
		return rsp;
	}

	// protected abstract ArrayList<ArticleInfo> getDataFromRsp(RSP rsp);

	// protected abstract boolean hasMore(RSP rsp);

	protected boolean isExpire() {
		return false;
	}

	protected boolean needLoadData() {
		return true;
	}

	protected boolean needCheckRefresh(RefreshEvent event) {
		return false;
	}

	protected boolean needCheckExpire(CheckExpireEvent event) {
		return false;
	}

	private class BgTask extends BackgroundTask<Void, Void, Boolean> {
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(LOG_TAG, "doInBackground+");
			mRspWrapper.setObject(getResponseFromDisk());
			List<Long> idList = mPageCache.getObject(
					Constants.CACHE_KEY_VIEWED_ARTICLE_LIST, ArrayList.class);
			if (idList != null) {

				if (mViewedList != null) {
					mViewedList.addAll(idList);
				}
			}
			Log.d(LOG_TAG, "doInBackground-");
			return true;
		}

		@Override
		protected void onPostExecute(Boolean needReload) {

			Log.d(LOG_TAG, "onPostExecute+");
			if (isDetached()) {
				return;
			}
			ArrayList<ArticleInfo> list = null;

			if (mRspWrapper != null) {
				list = mRspWrapper.getArticleList();// getDataFromRsp(mRsp.getObject());
			}

			if ((list == null || list.size() == 0) && needReload) {
				refreshData();
			} else {
				requestFinish(0, mRspWrapper.getObject(), false);
			}

			checkExpire();
			super.onPostExecute(needReload);
			Log.d(LOG_TAG, "onPostExecute-");
		}
	}

	public void checkExpire() {
		if (isExpire()) {
			callRefresh();
		}
	}

	protected String getKey() {
		return "";
	}

	protected int getDuration() {
		return Constants.CACHE_DURATION_HOMELIST;
	}
}
