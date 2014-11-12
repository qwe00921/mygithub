package com.yy.android.gamenews.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.duowan.Comm.ECommAppType;
import com.duowan.android.base.model.BaseModel.ResponseListener;
import com.duowan.android.base.util.LocalLog;
import com.duowan.gamenews.ArticleDetail;
import com.duowan.gamenews.ArticleInfo;
import com.duowan.gamenews.Channel;
import com.duowan.gamenews.Comment;
import com.duowan.gamenews.ExtraInfoType;
import com.duowan.gamenews.FavType;
import com.duowan.gamenews.GetArticleDetailRsp;
import com.duowan.gamenews.GetCommentListRsp;
import com.duowan.gamenews.Image;
import com.duowan.gamenews.ImageType;
import com.duowan.gamenews.LikeType;
import com.duowan.gamenews.PicInfo;
import com.duowan.gamenews.User;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.gamenews.Video;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.GameNewsApplication;
import com.yy.android.gamenews.event.CommentEvent;
import com.yy.android.gamenews.event.LikeEvent;
import com.yy.android.gamenews.model.ArticleDetailModel;
import com.yy.android.gamenews.model.CommentModel;
import com.yy.android.gamenews.model.ReportModel;
import com.yy.android.gamenews.ui.common.SwitchImageLoader;
import com.yy.android.gamenews.ui.view.ActionBar;
import com.yy.android.gamenews.ui.view.ExtendedListView;
import com.yy.android.gamenews.ui.view.ExtendedScrollView;
import com.yy.android.gamenews.ui.view.ExtendedScrollView.OnFlipListener;
import com.yy.android.gamenews.ui.view.ExtendedWebView;
import com.yy.android.gamenews.util.IPageCache;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.StatsUtil;
import com.yy.android.gamenews.util.TimeUtil;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

import de.greenrobot.event.EventBus;

public class ArticleDetailActivity extends BaseActivity implements
		OnClickListener, OnFlipListener {
	public static String CURRENT_ARTICLE_TAB = "我的最爱";// 来自于哪个tab
	public static String CURRENT_BUTTON_TAB = MainActivity.TAG_NAME_NEWS;// 来自于哪个tab
	public static final String KEY_ARTICLE_INFO = "article_info";
	public static final String KEY_ARTICLE_ID = "article_id";
	public static final String KEY_COMMENT = "article_comment";
	public static final String TAG_SOCIAL_DIALOG = "article_social_dialog";
	public static final String TAG_REPORT_DIALOG = "article_report_dialog";
	private static final int ARTICLE_UNCERTAIN = -1;
	private static final String TAG = "ArticleDetailActivity";
	private ExtendedWebView mBody;
	private ExtendedListView mCommentList;
	private ExtendedScrollView mScrollView;
	private ListAdapter mListAdapter;
	// private ArticleReportDialog mArticleReportDialog;
	// private ArticleSocialDialog mArticleSocialDialog;
	private SharedPreferences msharedPre;
	private ArticleDetail mArticleDetail = null;
	private View mHeaderView;
	private ActionBar mActionBar;
	private View mJump;
	private TextView mLikeCount;
	private TextView mDislikeCount;
	private ImageView mLikeArticle;
	private ImageView mDislikeArticle;
	private TextView[] mTag;
	private TextView mCommentCount;
	// private ArrayList<ArticleInfo> mMyFavorite;
	private Set<String> mMyCommentsLike;
	private Set<Long> mMyArticlesLike;
	private Set<Long> mMyArticlesDislike;
	private ArticleInfo mArticleInfo;
	private ImageView mKeep;
	private boolean mKept = false;
	private View mTitleCommentNewest;
	private View mTitleCommentEmpty;
	private User me;
	private Handler mHandler = new Handler();
	private String mSocialImgUrl;

	// for progress
	private View mLoadingView;
	private ProgressBar mFooterProgressBar;
	private TextView mFooterTipsTv;
	private View mProgressBar;
	private View mProgressBarInner;
	private View mRetryArticle;
	private View mBodyContainer;
	private Animation mLoadingAnimation;

	// for video
	private View mCustomView;
	private int mState;
	private static final int STATE_INIT = 1;
	private static final int STATE_WEB = 5;
	private static final int STATE_VIDEO = 6;
	private FrameLayout mCustomViewContainer;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;
	private MyWebChromeClient mWebChromeClient;

	// for
	private Map<Integer, String> mExtraInfo = null;
	// for menu
	private View mMenu;
	private Animation mAnimRadioIn;
	private Animation mAnimRadioOut;
	private boolean isAnimating;
	private boolean mIsRadioVisible = true; // 初始化时为显示状态
	private AnimationListener mAnimListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			isAnimating = true;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (animation == mAnimRadioIn) {
				mIsRadioVisible = true;
				mMenu.setVisibility(View.VISIBLE);
			} else {
				mIsRadioVisible = false;
				mMenu.setVisibility(View.GONE);
			}
			isAnimating = false;
		}
	};

	public static void startArticleDetailActivity(Context context,
			long articleId) {
		Intent intent = new Intent(context, ArticleDetailActivity.class);
		ArticleInfo info = new ArticleInfo();
		info.setId(articleId);
		info.setArticleType(ARTICLE_UNCERTAIN);
		intent.putExtra(ArticleDetailActivity.KEY_ARTICLE_INFO, info);
		context.startActivity(intent);
	}

	public static void startArticleDetailActivity(Context context,
			ArticleInfo articleInfo) {
		Intent intent = new Intent(context, ArticleDetailActivity.class);
		intent.putExtra(ArticleDetailActivity.KEY_ARTICLE_INFO, articleInfo);
		context.startActivity(intent);
	}

	private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			loadWebImageFailed(imageUri);

		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {

			File cache = ImageLoader.getInstance().getDiscCache().get(imageUri);
			if (cache.exists()) {
				String destFileName = String.format("file:///%s", cache);
				// Toast.makeText(ArticleDetailActivity.this,
				// "cache.exists() " + destFileName, Toast.LENGTH_SHORT)
				// .show();
				replaceWebImage(imageUri, destFileName);
			} else {
				replaceWebImage(imageUri, imageUri);
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {

		}

	};

	private void replaceWebImage(final String srcUri, final String destUrl) {

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				int id = getSrcImage(srcUri);
				if (id >= 0) {
					mBody.loadUrl(String
							.format("javascript:webview.fillImageById(\"image\",%d,\"%s\")",
									id, destUrl));
				} else {
					id = getSrcVideo(srcUri);
					if (id >= 0) {
						mBody.loadUrl(String
								.format("javascript:webview.fillImageById(\"video\",%d,\"%s\")",
										id, destUrl));
					} else {
					}
				}
			}
		});
	}

	private void loadWebImageFailed(final String srcUri) {

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				int id = getSrcImage(srcUri);
				if (id >= 0) {
					mBody.loadUrl(String
							.format("javascript:webview.reloadFail(\"image\",%d,\"image_bg_failed.png\")",
									id));
				} else {
					id = getSrcVideo(srcUri);
					if (id >= 0) {
						mBody.loadUrl(String
								.format("javascript:webview.reloadFail(\"video\",%d,\"image_bg_failed.png\")",
										id));
					} else {
					}
				}
			}
		});
	}

	private int getSrcImage(String url) {
		if (TextUtils.isEmpty(url)) {
			return -1;
		}
		List<Image> images = mArticleDetail.getImageList();
		if (images == null) {
			return -1;
		}
		int size = images.size();
		for (int i = 0; i < size; ++i) {
			PicInfo picInfo = images.get(i).getUrls()
					.get(ImageType._IMAGE_TYPE_BIG);
			if (picInfo == null) {
				continue;
			}
			if (url.equals(picInfo.getUrl())) {
				return i;
			}
		}
		return -1;
	}

	private int getSrcVideo(String url) {
		if (TextUtils.isEmpty(url)) {
			return -1;
		}
		List<Video> videos = mArticleDetail.getVideoList();
		if (videos == null) {
			return -1;
		}
		int size = videos.size();
		for (int i = 0; i < size; ++i) {
			PicInfo picInfo = videos.get(i).getUrls()
					.get(ImageType._IMAGE_TYPE_BIG);
			if (picInfo == null) {
				continue;
			}
			if (url.equals(picInfo.getUrl())) {
				return i;
			}
		}
		return -1;
	}

	private void hideLoadingBar() {
		mFooterTipsTv.setVisibility(View.VISIBLE);
		mFooterTipsTv.setText(getResources().getString(
				R.string.global_list_no_more));
		mFooterTipsTv.setOnClickListener(null);
		mFooterProgressBar.setVisibility(View.GONE);
	}

	private void showLoadingBar() {
		mFooterTipsTv.setVisibility(View.VISIBLE);
		mFooterTipsTv.setText(getResources().getString(
				R.string.global_list_loading));
		mFooterTipsTv.setOnClickListener(null);
		mFooterProgressBar.setVisibility(View.VISIBLE);
	}

	private void setArticleLoadingView() {
		mProgressBar.setVisibility(View.VISIBLE);
		mRetryArticle.setVisibility(View.INVISIBLE);
		mCommentList.setVisibility(View.GONE);
		mCommentCount.setVisibility(View.INVISIBLE);
		mProgressBarInner.startAnimation(mLoadingAnimation);
	}

	private void setArticleLoadedView() {
		mProgressBar.setVisibility(View.INVISIBLE);
		mRetryArticle.setVisibility(View.INVISIBLE);
		mCommentList.setVisibility(View.VISIBLE);
		mCommentCount.setVisibility(View.VISIBLE);
		mProgressBarInner.clearAnimation();
	}

	private void setArticleLoadFailedView() {
		LocalLog.d(TAG, "[getArticleDetail][setArticleLoadFailedView]");
		mProgressBar.setVisibility(View.INVISIBLE);
		mRetryArticle.setVisibility(View.VISIBLE);
		mCommentCount.setVisibility(View.INVISIBLE);
		mProgressBarInner.clearAnimation();
	}

	private void setCommentLoadFailedView() {
		mFooterTipsTv.setText(getResources().getString(R.string.global_retry));
		mFooterTipsTv.setOnClickListener(this);
		mFooterProgressBar.setVisibility(View.GONE);
		mFooterTipsTv.setOnClickListener(this);
	}

	private class MyWebChromeClient extends WebChromeClient {
		private View mVideoProgressView;

		@Override
		public void onShowCustomView(View view, int requestedOrientation,
				CustomViewCallback callback) {
			onShowCustomView(view, callback);
		}

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			if (mCustomView != null) {
				callback.onCustomViewHidden();
				return;
			}
			mCustomView = view;
			mState = STATE_VIDEO;
			updateView();
			mCustomViewContainer.addView(view);
			mCustomViewCallback = callback;
		}

		@Override
		public View getVideoLoadingProgressView() {
			if (mVideoProgressView == null) {
				LayoutInflater inflater = LayoutInflater
						.from(ArticleDetailActivity.this);
				mVideoProgressView = inflater.inflate(R.layout.video_progress,
						null);
			}
			return mVideoProgressView;
		}

		@Override
		public void onHideCustomView() {
			super.onHideCustomView();
			if (mCustomView == null)
				return;

			mState = STATE_WEB;
			updateView();

			// Hide the custom view.
			mCustomView.setVisibility(View.GONE);

			// Remove the custom view from its container.
			mCustomViewContainer.removeView(mCustomView);
			mCustomViewCallback.onCustomViewHidden();

			mCustomView = null;
		}
	}

	private void updateView() {
		mCustomViewContainer.setVisibility(mState == STATE_VIDEO ? View.VISIBLE
				: View.GONE);
		toggleFullScreen(mState == STATE_VIDEO);

		if (mState == STATE_VIDEO) {
			if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				/* 若目前为竖排，则更改为横排呈现 */
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		} else {
			if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				/* 若目前为竖排，则更改为横排呈现 */
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}

		}
	}

	@SuppressLint("NewApi")
	private void toggleFullScreen(boolean fullscreen) {
		if (fullscreen) {
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			getWindow().setAttributes(attrs);
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
		} else {
			WindowManager.LayoutParams attrs = getWindow().getAttributes();
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			getWindow().setAttributes(attrs);
			if (android.os.Build.VERSION.SDK_INT >= 14) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
	}

	private void setCommentTitleView() {
		if (mArticleDetail.getCommentCount() == 0) {
			mTitleCommentNewest.setVisibility(View.GONE);
			mTitleCommentEmpty.setVisibility(View.VISIBLE);

			mFooterTipsTv.setVisibility(View.GONE);
			mFooterProgressBar.setVisibility(View.GONE);
			mTitleCommentEmpty.setOnClickListener(this);
		} else {
			mFooterTipsTv.setVisibility(View.VISIBLE);
			mTitleCommentNewest.setVisibility(View.VISIBLE);
			mTitleCommentEmpty.setVisibility(View.GONE);
			mTitleCommentEmpty.setOnClickListener(null);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		LocalLog.d(TAG, "[onCreate]");
		super.onResume();
		if (Build.VERSION.SDK_INT >= 11) {
			mBody.onResume();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onPause() {
		super.onPause();
		if (Build.VERSION.SDK_INT >= 11) {
			mBody.onPause();
		}
	}

	//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, false);
		LocalLog.d(TAG, "[onCreate]");
		Intent intent = getIntent();
		msharedPre = getSharedPreferences(Constants.KEY_UPDATE_GLOBAL,
				Context.MODE_PRIVATE);
		if (intent != null
				&& msharedPre.getBoolean(Constants.KEY_COPY_DETAIL_WEB, false)) {
			mArticleInfo = (ArticleInfo) intent
					.getSerializableExtra(KEY_ARTICLE_INFO);
			if (mArticleInfo == null) {
				finish();
				return;
			}
		} else {
			finish();
			return;
		}

		setContentView(R.layout.activity_article_detail);
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mActionBar.setOnLeftClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		if (mArticleInfo.getSourceUrl() != null) {
			mActionBar.setTitle(mArticleInfo.getSourceUrl());
		}

		mScrollView = (ExtendedScrollView) findViewById(R.id.scroll);
		initListView();
		initWebView();
		initExtraView();
		loadArticle();
		loadMyData();
		initMenu();

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBody.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, mBodyContainer
								.getHeight()));
				mCommentList.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, mBodyContainer
								.getHeight()));
			}
		}, 100);

		mState = STATE_INIT;
		if (ARTICLE_UNCERTAIN != mArticleInfo.getArticleType()) {
			sendArticleReadReport(mArticleInfo.getId(), mArticleInfo.getTitle());
		}
	}

	private void sendArticleReadReport(long id, String title) {
		if (title == null) {
			title = "";
		}
		StatsUtil
				.statsReport(this, "stats_read_article", "article_name", title);
		StatsUtil.statsReportByMta(this, "stats_read_article", "article_name",
				"(" + mArticleInfo.getId() + ")" + title);
		StatsUtil.statsReportByHiido("stats_read_article", "article_name:"
				+ title);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Fragment fs = getSupportFragmentManager().findFragmentByTag(
				TAG_SOCIAL_DIALOG);
		if (fs != null && fs.isAdded() && fs instanceof ArticleSocialDialog) {
			((ArticleSocialDialog) fs).onActivityResult(requestCode,
					resultCode, data);
		}

		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {
				// String comment = data.getStringExtra(KEY_COMMENT);
				// Comment myComment = new Comment();
				// myComment.setId(COMMENT_MINE);
				// myComment.setContent(comment);
				// myComment.setCount(new Count(0, 0));
				// myComment.setUser(me);
				// myComment.setTime((int) (System.currentTimeMillis() / 1000));
				// mListAdapter.insert(myComment, 0);
				// mJump.performClick();
				// mArticleDetail
				// .setCommentCount(mArticleDetail.getCommentCount() + 1);
				// mCommentCount.setText(Integer.toString(mArticleDetail
				// .getCommentCount()));
				//
				// notifyCommentCountChanged();
				// setCommentTitleView();
				mListAdapter.startLoad();
				mJump.performClick();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (inCustomView()) {
				hideCustomView();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (inCustomView()) {
			hideCustomView();
		}
	}

	private boolean inCustomView() {
		return mCustomView != null;
	}

	private void hideCustomView() {
		mWebChromeClient.onHideCustomView();
	}

	private void initExtraView() {
		mProgressBarInner = findViewById(R.id.progressbar_load_article_inner);
		mProgressBar = findViewById(R.id.progressbar_load_article);
		mRetryArticle = findViewById(R.id.retry_article);
		mRetryArticle.setOnClickListener(this);
		mBodyContainer = findViewById(R.id.body_container);
		mLikeArticle = (ImageView) mHeaderView.findViewById(R.id.like_article);
		mDislikeArticle = (ImageView) mHeaderView
				.findViewById(R.id.dislike_article);
		mLikeCount = (TextView) mHeaderView.findViewById(R.id.like_count);
		mDislikeCount = (TextView) mHeaderView.findViewById(R.id.dislike_count);
		mTag = new TextView[3];
		mTag[0] = (TextView) mHeaderView.findViewById(R.id.tag_a);
		mTag[1] = (TextView) mHeaderView.findViewById(R.id.tag_b);
		mTag[2] = (TextView) mHeaderView.findViewById(R.id.tag_c);
		mCommentCount = (TextView) findViewById(R.id.comment_count);
		mCommentCount.setVisibility(View.INVISIBLE);
		mKeep = (ImageView) findViewById(R.id.keep_btn);
		mKeep.setOnClickListener(this);
		findViewById(R.id.shared_btn).setOnClickListener(this);
		mJump = findViewById(R.id.jump_btn);
		mJump.setOnClickListener(this);
		findViewById(R.id.comment_btn).setOnClickListener(this);
		findViewById(R.id.report_btn).setOnClickListener(this);
		mHeaderView.findViewById(R.id.like_article_container)
				.setOnClickListener(this);
		mHeaderView.findViewById(R.id.dislike_article_container)
				.setOnClickListener(this);

		// mRootContainer = (ExtendedLinearLayout)
		// findViewById(R.id.article_detail_container);
		mScrollView.setOnFlipListener(this);
		mLoadingAnimation = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.article_detail_loading);
		mLoadingAnimation.setInterpolator(new LinearInterpolator());
		mLoadingAnimation.setFillAfter(true);// 动画停止时保持在该动画结束时的状态
		mTitleCommentNewest = findViewById(R.id.title_comment_newest);
		mTitleCommentEmpty = findViewById(R.id.title_comment_empty);
	}

	private void initListView() {
		mCommentList = (ExtendedListView) findViewById(R.id.comment_list);
		mScrollView.listView = mCommentList;
		LayoutInflater lif = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHeaderView = lif.inflate(R.layout.article_detail_comment_header,
				mCommentList, false);
		mCommentList.addHeaderView(mHeaderView, null, false);

		mLoadingView = lif.inflate(R.layout.lv_footer_loading, null);
		mFooterProgressBar = (ProgressBar) mLoadingView
				.findViewById(R.id.progressBar1);
		mFooterTipsTv = (TextView) mLoadingView
				.findViewById(R.id.global_loading_text);
		mCommentList.addFooterView(mLoadingView);

		mListAdapter = new ListAdapter(ArticleDetailActivity.this,
				R.layout.article_detail_comment_list_item);
		mCommentList.setAdapter(mListAdapter);

		mCommentList.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 当不滚动时
					if (mCommentList.getLastVisiblePosition() == (mCommentList
							.getCount() - 1)) {
						mListAdapter.loadMore();
					}
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			};
		});
		mCommentList.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

		});
	}

	private void initWebView() {
		mBody = (ExtendedWebView) findViewById(R.id.article_body);
		mCustomViewContainer = (FrameLayout) findViewById(R.id.custom_view_container);
		mScrollView.webView = mBody;
		mBody.getSettings().setJavaScriptEnabled(true);
		mBody.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mBody.getSettings().setDomStorageEnabled(true);
		mBody.addJavascriptInterface(new JsInterface(this), "client"); // JS交互
		mBody.getSettings().setSupportZoom(false);
		String userAgent = Constants.USER_AGENT_PREFIX
				+ GameNewsApplication.getInstance().getPackageInfo().versionName;
		mBody.getSettings().setUserAgentString(userAgent);
		mWebChromeClient = new MyWebChromeClient();
		mBody.setWebChromeClient(mWebChromeClient);
		mBody.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				LocalLog.d(TAG,
						"[WebViewClient][shouldOverrideUrlLoading], url = "
								+ url);
				Intent intent = new Intent(ArticleDetailActivity.this,
						AppWebActivity.class);
				intent.putExtra(AppWebActivity.KEY_URL, url);
				startActivity(intent);
				// Intent intent = new Intent(Intent.ACTION_VIEW,
				// Uri.parse(url));
				// startActivity(intent);
				return true;
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				// TODO Auto-generated method stub
				LocalLog.d(TAG, "[WebViewClient][onLoadResource], url = " + url);
				super.onLoadResource(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				LocalLog.d(TAG, "[WebViewClient][onPageStarted], url = " + url);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				LocalLog.d(TAG, "[WebViewClient][onPageFinished], url = " + url);
				super.onPageFinished(view, url);

			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				LocalLog.d(TAG, "[WebViewClient][onReceivedError], url = "
						+ failingUrl + ", errorCode = " + errorCode);
				Toast.makeText(ArticleDetailActivity.this, description,
						Toast.LENGTH_SHORT).show();
			}

		});

	}

	private void initMenu() {
		mMenu = findViewById(R.id.menu);
		mAnimRadioOut = AnimationUtils.loadAnimation(this,
				R.anim.article_detail_menu_tans_out);
		mAnimRadioOut.setAnimationListener(mAnimListener);
		mAnimRadioIn = AnimationUtils.loadAnimation(this,
				R.anim.article_detail_menu_tans_in);
		mAnimRadioIn.setAnimationListener(mAnimListener);
	}

	private void showMenu() {
		// mLastDisplayTime = System.currentTimeMillis();

		if (isAnimating) {
			return;
		}
		if (!mIsRadioVisible) {
			mAnimRadioOut.cancel();
			mMenu.startAnimation(mAnimRadioIn);
		}
	}

	private void hideMenu() {
		if (isAnimating) {
			return;
		}
		if (mIsRadioVisible) {
			mAnimRadioIn.cancel();
			mMenu.startAnimation(mAnimRadioOut);
		}
	}

	private void loadMyData() {

		UserInitRsp rsp = Preference.getInstance().getInitRsp();
		me = null;
		if (rsp != null) {
			me = rsp.getUser();
		}
		if (me == null) {
			me = new User();
			me.setId("-1");
			me.setName(getResources()
					.getString(R.string.global_my_default_name));
		}

		IPageCache pageCache = new IPageCache();

		mMyCommentsLike = Preference.getInstance().getMyCommentsLike();
		if (mMyCommentsLike == null) {
			mMyCommentsLike = new HashSet<String>();
		}

		mMyArticlesLike = Preference.getInstance().getMyArticlesLike();
		if (mMyArticlesLike == null) {
			mMyArticlesLike = new HashSet<Long>();
		}
		mMyArticlesDislike = Preference.getInstance().getMyArticlesDislike();
		if (mMyArticlesDislike == null) {
			mMyArticlesDislike = new HashSet<Long>();
		}

	}

	private void setArticleDetail(GetArticleDetailRsp data) {

		LocalLog.d(TAG, "[setArticleDetail] enter");
		boolean afterCache = false;
		if (mArticleDetail != null) {
			afterCache = true;
			if (data.getArticleDetail() != null) {
				mArticleDetail = data.getArticleDetail();
				mExtraInfo = data.getExtraInfo();
			}
		} else {
			mArticleDetail = data.getArticleDetail();
			mExtraInfo = data.getExtraInfo();
		}
		if (mArticleDetail == null) {
			Toast.makeText(
					ArticleDetailActivity.this,
					getResources().getString(
							R.string.article_detail_load_failed),
					Toast.LENGTH_SHORT).show();
			setArticleLoadFailedView();
			return;
		}

		/**
		 * 如果是推送过来的数据，在拉取到数据之后发送报告
		 */
		if (ARTICLE_UNCERTAIN == mArticleInfo.getArticleType()) {
			sendArticleReadReport(mArticleDetail.getId(),
					mArticleDetail.getTitle());
		}

		LocalLog.d(TAG, "[setArticleDetail]afterCache = " + afterCache);
		if (!afterCache) {
			SharedPreferences mPref = GameNewsApplication.getInstance()
					.getSharedPreferences(Constants.KEY_UPDATE_GLOBAL,
							MODE_PRIVATE);
			File dir = null;
			if (mPref.getBoolean(Constants.NEW_VERSION_READY, false)) {
				int currDir = mPref.getInt(Constants.CURR_DIR, 0);
				int newDir = 1 - currDir;
				dir = this.getDir(String.valueOf(newDir), Context.MODE_PRIVATE);
				mPref.edit().putBoolean(Constants.NEW_VERSION_READY, false)
						.putInt(Constants.CURR_DIR, newDir).commit();
			} else {
				int currDir = mPref.getInt(Constants.CURR_DIR, 0);
				dir = this
						.getDir(String.valueOf(currDir), Context.MODE_PRIVATE);
			}
			String filepath = null;
			if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_GAMENEWS)) {
				filepath = dir.getAbsolutePath() + "/" + Constants.NEWS_HTML;
			} else if (Constants
					.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH)) {
				filepath = dir.getAbsolutePath() + "/" + Constants.SPORTS_HTML;
			} else {
				filepath = dir.getAbsolutePath() + "/" + Constants.NEWS_HTML;
			}
			LocalLog.d(TAG, "[setArticleDetail][mBody.loadUrl], filepath = "
					+ filepath);
			mBody.loadUrl("file://" + filepath);
		}
		mLikeCount.setText(Integer.toString(mArticleDetail.getCount()
				.getLikeCount()));

		LikeEvent event = new LikeEvent();
		event.id = mArticleDetail.getId();
		event.likeCount = Integer.parseInt(mLikeCount.getText().toString());
		EventBus.getDefault().post(event);

		mDislikeCount.setText(Integer.toString(mArticleDetail.getCount()
				.getDislikeCount()));
		if (mMyArticlesLike.contains(mArticleInfo.getId())) {
			mLikeArticle
					.setImageResource(R.drawable.article_detail_like_pressed);
		} else {
			mLikeArticle
					.setImageResource(R.drawable.article_detail_like_normal);
		}
		if (mMyArticlesDislike.contains(mArticleInfo.getId())) {
			mDislikeArticle
					.setImageResource(R.drawable.article_detail_dislike_pressed);
		} else {
			mDislikeArticle
					.setImageResource(R.drawable.article_detail_dislike_normal);
		}

		List<Channel> channels = mArticleDetail.getChannelList();
		if (channels != null && channels.size() > 0) {
			int tagCount = Math.min(channels.size(), 3);
			int maxTagWidth = (getWindowManager().getDefaultDisplay()
					.getWidth() - 170) / tagCount;
			for (int i = 0; i < tagCount; ++i) {
				mTag[i].setMaxWidth(maxTagWidth);
				mTag[i].setText(channels.get(i).getName());
				mTag[i].setOnClickListener(ArticleDetailActivity.this);
			}
		}

		mActionBar.setTitle(mArticleDetail.getSourceUrl());

		if (mArticleDetail.hasFav) {
			mKeep.setImageResource(R.drawable.article_detail_kept);
			mKept = true;
		} else {
			mKeep.setImageResource(R.drawable.article_detail_keep_normal);
			mKept = false;
		}
		mCommentCount
				.setText(Integer.toString(mArticleDetail.getCommentCount()));

		mSocialImgUrl = "";
		List<Image> images = mArticleDetail.getImageList();
		if (images != null) {
			for (Image image : images) {
				if (image.getUrls() == null) {
					continue;
				}
				// PicInfo picInfoS = image.getUrls().get(
				// ImageType._IMAGE_TYPE_SMALL);
				// if (picInfoS != null
				// && !TextUtils.isEmpty(picInfoS.getUrl())) {
				// imgUrl = picInfoS.getUrl();
				// break;
				// }

				PicInfo picInfoB = image.getUrls().get(
						ImageType._IMAGE_TYPE_BIG);
				if (picInfoB != null && !TextUtils.isEmpty(picInfoB.getUrl())) {
					mSocialImgUrl = picInfoB.getUrl();
					break;
				}
			}
		}
		notifyCommentCountChanged();

	}

	private void loadArticle() {
		LocalLog.d(TAG, "[loadArticle]");
		setArticleLoadingView();
		ArticleDetailModel.getArticleDetail(
				new ResponseListener<GetArticleDetailRsp>(
						ArticleDetailActivity.this) {
					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
						LocalLog.d(TAG, "[getArticleDetail][onError]");
						if (mArticleDetail != null) {
							return;
						}
						if (!TextUtils.isEmpty(e.getMessage())) {
							Toast.makeText(
									ArticleDetailActivity.this,
									getResources().getString(
											R.string.load_failed),
									Toast.LENGTH_SHORT).show();
						}
						setArticleLoadFailedView();
					}

					@Override
					public void onResponse(GetArticleDetailRsp data) {
						LocalLog.d(TAG,
								"[getArticleDetail][onResponse], data = "
										+ data);
						setArticleDetail(data);
					}
				}, // Listener
				mArticleInfo.getId());
	}

	class ListAdapter extends ArrayAdapter<Comment> {

		@Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			return false;
		}

		private LayoutInflater mInflater;
		private int mResource;
		private boolean mLoading = false;
		private boolean mReloading = false;

		private GetCommentListRsp mCommentListRsp;

		public ListAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mResource = textViewResourceId;
		}

		public void setData(List<Comment> data) {
			setNotifyOnChange(false);
			clear();
			if (data != null) {
				for (Comment item : data) {
					add(item);
				}
			}
			notifyDataSetChanged();
		}

		public void appendData(List<Comment> data) {
			setNotifyOnChange(false);
			if (data != null) {
				for (Comment item : data) {
					add(item);
				}
			}
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(mResource, null);
				holder.mContent = (TextView) convertView
						.findViewById(R.id.content);
				holder.mUserName = (TextView) convertView
						.findViewById(R.id.user_name);
				holder.mLikeCount = (TextView) convertView
						.findViewById(R.id.like_count);
				holder.mTime = (TextView) convertView
						.findViewById(R.id.comment_time);
				holder.mUserLogo = (ImageView) convertView
						.findViewById(R.id.user_logo);
				holder.mLike = (ImageView) convertView
						.findViewById(R.id.like_comment);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Comment item = getItem(position);
			holder.mContent.setText(item.getContent());
			if (mMyCommentsLike.contains(item.getId())) {
				holder.mLike
						.setImageResource(R.drawable.article_detail_comment_like_pressed);
			} else {
				holder.mLike
						.setImageResource(R.drawable.article_detail_comment_like_normal);
			}
			holder.mComment = item;
			holder.mLike.setTag(holder);
			holder.mLike.setOnClickListener(ArticleDetailActivity.this);
			holder.mLikeCount.setText(Integer.toString(item.getCount()
					.getLikeCount()));
			holder.mUserName.setText(item.getUser().getName());
			SwitchImageLoader.getInstance().displayImage(item.getUser().icon,
					holder.mUserLogo, SwitchImageLoader.DEFAULT_USER_DISPLAYER);
			// holder.mTime.setText(TimeUtil.parseTime(ArticleDetailActivity.this,
			// item.getTime()));
			holder.mTime.setText(TimeUtil.parseTime(item.getTime()));
			return convertView;
		}

		public void startLoad() {
			mReloading = true;
			mLoading = true;
			mCommentListRsp = null;
			showLoadingBar();
			loadComments();
		}

		public void loadMore() {
			if (getCount() == 0 || mCommentListRsp == null) {
				startLoad();
			}
			if (!mLoading && !mReloading && mCommentListRsp != null
					&& mCommentListRsp.hasMore) {
				mLoading = true;
				showLoadingBar();
				loadComments();
			}
		}

		private void loadComments() {
			String attachInfo = null;
			if (mCommentListRsp != null) {
				attachInfo = mCommentListRsp.getAttachInfo();
			}
			CommentModel.getCommentList(
					new ResponseListener<GetCommentListRsp>(
							ArticleDetailActivity.this) {
						@Override
						public void onError(Exception e) {
							// TODO Auto-generated method stub
							super.onError(e);
							mLoading = false;
							mReloading = false;
							if (!TextUtils.isEmpty(e.getMessage())) {
								Toast.makeText(
										ArticleDetailActivity.this,
										getResources().getString(
												R.string.load_failed),
										Toast.LENGTH_SHORT).show();
							}
							setCommentLoadFailedView();
						}

						@Override
						public void onResponse(GetCommentListRsp data) {
							mCommentListRsp = data;
							// 重新加载
							if (mReloading) {
								if (mCommentListRsp == null) {
									mArticleDetail.setCommentCount(0);
									mListAdapter.setData(null);
								} else {
									mArticleDetail
											.setCommentCount(mCommentListRsp
													.getCount());
									mListAdapter.setData(data.getCommentList());
								}
								mCommentCount.setText(Integer
										.toString(mArticleDetail
												.getCommentCount()));
								notifyCommentCountChanged();
								mReloading = false;
								setCommentTitleView();
							} else if (mCommentListRsp != null) {
								mListAdapter.appendData(mCommentListRsp
										.getCommentList());
							}
							mLoading = false;
							if (mCommentListRsp != null
									&& !mCommentListRsp.hasMore
									&& mArticleDetail.getCommentCount() > 0) {
								hideLoadingBar();
							}
						}
					}, // Listener
					mArticleInfo.getId(), attachInfo);
		}
	}

	private static class ViewHolder {
		TextView mUserName;
		TextView mLikeCount;
		TextView mTime;
		TextView mContent;
		ImageView mUserLogo;
		ImageView mLike;
		Comment mComment;
	}

	class JsInterface {
		Context context;

		public JsInterface(Context context) {
			this.context = context;
		}

		// 查看图片url
		@JavascriptInterface
		public void openVideo(String url) {
			LocalLog.d(TAG, "[JsInterface][openVideo], url = " + url);
			Intent intent = new Intent(ArticleDetailActivity.this,
					VideoPlayerActivity.class);
			intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, url);
			startActivity(intent);
		}

		// 查看图片url
		@JavascriptInterface
		public void openImage(String img) {
			LocalLog.d(TAG, "[JsInterface][openImage], img = " + img);
			// Intent intent = new Intent(ArticleDetailActivity.this,
			// ImageDetailViewerActivity.class);
			// intent.putExtra(ImageDetailViewerActivity.EXTRA_IMAGE_LIST,
			// (Serializable) mSrcImages);
			// intent.putExtra(ImageDetailViewerActivity.EXTRA_CURRENT_IMAGE,
			// 0);
			// startActivity(intent);
			//
			ArrayList<String> imageList = new ArrayList<String>();
			if (!TextUtils.isEmpty(img)) {
				imageList.add(img);
				Intent intent = new Intent(ArticleDetailActivity.this,
						ImageZoomDetailViewerActivity.class);
				intent.putExtra(
						ImageZoomDetailViewerActivity.EXTRA_IMAGE_LIST_ZOOM,
						imageList);
				intent.putExtra(
						ImageZoomDetailViewerActivity.EXTRA_CURRENT_IMAGE, 0);
				intent.putExtra(ImageZoomDetailViewerActivity.EXTRA_TITLE,
						mArticleDetail.getTitle());
				startActivity(intent);
			}

		}

		// 获取文章信息
		@JavascriptInterface
		public String getArticleDetail() {
			// Toast.makeText(context, "getArticleDetail()", Toast.LENGTH_SHORT)
			// .show();
			LocalLog.d(TAG, "[JsInterface][getArticleDetail]");
			try {
				JSONObject info = new JSONObject();
				info.put("content", mArticleDetail.getContent());
				info.put("timeStamp",
						TimeUtil.parseTime(mArticleDetail.getTimeStamp()));
				info.put("title", mArticleDetail.getTitle());
				info.put("refer", mArticleDetail.getRefer());
				info.put("sourceUrl", mArticleDetail.getSourceUrl());
				if (mExtraInfo != null
						&& mExtraInfo
								.containsKey(ExtraInfoType.EATRAINFO_TYPE_SUPRISE_AWARD
										.value())) {
					info.put("extraInfo", mExtraInfo
							.get(ExtraInfoType.EATRAINFO_TYPE_SUPRISE_AWARD
									.value()));
				}
				return info.toString();
			} catch (Exception e) {
				return null;
			}
		}

		// 获取文章信息
		@JavascriptInterface
		public void toast(String msg) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}

		// 获取文章信息
		@JavascriptInterface
		public void longtoast(String msg) {
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}

		// 获取文章信息
		@JavascriptInterface
		public String getArticleDetail(String key) {
			// Toast.makeText(context, "getArticleDetail " + key,
			// Toast.LENGTH_SHORT).show();

			LocalLog.d(TAG, "[JsInterface][getArticleDetail(key) = ]" + key);
			if ("content".equals(key)) {
				return mArticleDetail.getContent();
			}
			if ("timeStamp".equals(key)) {
				return TimeUtil.parseTime(mArticleDetail.getTimeStamp());
			}
			if ("title".equals(key)) {
				return mArticleDetail.getTitle();
			}
			if ("refer".equals(key)) {
				return mArticleDetail.getRefer();
			}
			if ("sourceUrl".equals(key)) {
				return mArticleDetail.getSourceUrl();
			}
			return "";
		}

		// 获取加载中本地图片地址
		@JavascriptInterface
		public String getLoadingPicInfo() {
			return "image_bg_loading.png";
		}

		// 获取所有的图片尺寸信息
		@JavascriptInterface
		public String getPicInfo() {
			// Toast.makeText(context, "getPicInfo()",
			// Toast.LENGTH_SHORT).show();
			JSONArray infos = new JSONArray();
			// 图片信息
			{
				List<Image> images = mArticleDetail.getImageList();
				if (images != null) {
					int size = images.size();
					for (int i = 0; i < size; ++i) {
						try {
							PicInfo picInfo = images.get(i).getUrls()
									.get(ImageType._IMAGE_TYPE_BIG);
							JSONObject info = new JSONObject();
							info.put("id", i);
							info.put("type", "image");
							info.put("width", picInfo.getWidth());
							info.put("height", picInfo.getHeight());
							File cache = ImageLoader.getInstance()
									.getDiscCache().get(picInfo.getUrl());
							if (cache.exists()) {
								String destFileName = String.format(
										"file:///%s", cache);
								info.put("src", destFileName);
							} else {
								info.put("src", "");
							}

							infos.put(info);
							// return new JSONObject(bean.ManeuverInfo);
						} catch (Exception e) {
							return infos.toString();
						}
					}
				}
			}
			// 视频cover信息
			{

				List<Video> videos = mArticleDetail.getVideoList();
				if (videos != null) {
					int size = videos.size();
					for (int i = 0; i < size; ++i) {
						try {
							PicInfo picInfo = videos.get(i).getUrls()
									.get(ImageType._IMAGE_TYPE_BIG);
							JSONObject info = new JSONObject();
							info.put("id", i);
							info.put("type", "video");
							info.put("width", picInfo.getWidth());
							info.put("height", picInfo.getHeight());
							File cache = ImageLoader.getInstance()
									.getDiscCache().get(picInfo.getUrl());
							if (cache.exists()) {
								String destFileName = String.format(
										"file:///%s", cache);
								info.put("src", destFileName);
							} else {
								info.put("src", "");
							}
							infos.put(info);
							// return new JSONObject(bean.ManeuverInfo);
						} catch (Exception e) {
							return infos.toString();
						}
					}
				}
			}
			return infos.toString();
		}

		// 加载图片
		@JavascriptInterface
		public void loadImage(final int id) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (mArticleDetail.getImageList() == null) {
						return;
					}
					Image image = mArticleDetail.getImageList().get(id);
					if (image == null) {
						return;
					}
					PicInfo picInfo = image.getUrls().get(
							ImageType._IMAGE_TYPE_BIG);
					if (picInfo == null) {
						return;
					}
					if (ImageLoader.getInstance().getDiscCache()
							.get(picInfo.getUrl()).exists()
							|| SwitchImageLoader.getInstance().needLoadImage()) {
						ImageLoader.getInstance().loadImage(picInfo.url,
								mImageLoadingListener);
					}
				}
			});
		}

		// 加载视频cover
		@JavascriptInterface
		public void loadVideoCover(final int id) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mArticleDetail.getVideoList() == null) {
						return;
					}
					Video video = mArticleDetail.getVideoList().get(id);
					if (video == null) {
						return;
					}
					PicInfo picInfo = video.getUrls().get(
							ImageType._IMAGE_TYPE_BIG);
					if (picInfo == null) {
						return;
					}
					ImageLoader.getInstance().loadImage(picInfo.url,
							mImageLoadingListener);

				}
			});
		}

		// content渲染结束
		@JavascriptInterface
		public void onPageFinished() {
			LocalLog.d(TAG, "[JsInterface][onPageFinished]");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					setArticleLoadedView();
					mListAdapter.startLoad();
				}
			});
		}

		// 点击图片标签
		@JavascriptInterface
		public void onClickImage(int id) {
			ArrayList<Image> imageList = mArticleDetail.getImageList();
			if (imageList == null || imageList.size() == 0) {
				return;
			}
			ArrayList<String> arrayList = new ArrayList<String>();
			for (Image image : imageList) {
				arrayList.add(image.getUrls().get(ImageType._IMAGE_TYPE_BIG)
						.getUrl());
			}
			Intent intent = new Intent(ArticleDetailActivity.this,
					ImageZoomDetailViewerActivity.class);
			intent.putExtra(
					ImageZoomDetailViewerActivity.EXTRA_IMAGE_LIST_ZOOM,
					arrayList);
			intent.putExtra(ImageZoomDetailViewerActivity.EXTRA_CURRENT_IMAGE,
					id);
			intent.putExtra(ImageZoomDetailViewerActivity.EXTRA_TITLE,
					mArticleDetail.getTitle());
			startActivity(intent);

		}

		// 点击视频标签
		@JavascriptInterface
		public void onClickVideo(int id) {
			// Toast.makeText(context, "onClickVideo " + Integer.toString(id),
			// Toast.LENGTH_SHORT).show();
			Video video = mArticleDetail.getVideoList().get(id);
			Intent intent = new Intent(ArticleDetailActivity.this,
					VideoPlayerActivity.class);
			intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL,
					video.getMp4Url());
			startActivity(intent);
		}

		// 滚动到指定y
		@JavascriptInterface
		public void scrollTo(final int y) {
			Toast.makeText(context, "loadVideoCover " + Integer.toString(y),
					Toast.LENGTH_SHORT).show();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mBody.scrollTo(0, y);

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		Intent intent = null;
		switch (v.getId()) {
		case R.id.shared_btn:
			if (mArticleDetail == null) {
				return;
			}
			DialogFragment fs = ArticleSocialDialog.newInstance(
					mArticleDetail.getId(), mArticleDetail.getTitle(),
					mSocialImgUrl, ArticleSocialDialog.SHARED_FROM_ARTICLE);
			Util.showDialog(ArticleDetailActivity.this, fs, TAG_SOCIAL_DIALOG);
			break;
		case R.id.jump_btn:
			if (mArticleDetail == null) {
				return;
			}
			mCommentList.setSelection(0);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			break;
		case R.id.title_comment_empty:
			postComment();
			break;
		case R.id.comment_btn:
			postComment();
			break;
		case R.id.report_btn:
			if (mArticleDetail == null) {
				return;
			}
			DialogFragment fr = ArticleReportDialog.newInstance(
					mArticleInfo.getId(), mArticleDetail.getTitle());
			Util.showDialog(ArticleDetailActivity.this, fr, TAG_REPORT_DIALOG);
			break;
		case R.id.keep_btn:
			if (mArticleDetail == null) {
				return;
			}
			if (mKept) {

				ReportModel.AddFavArticle(new ResponseListener<Boolean>(
						ArticleDetailActivity.this) {
					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
						super.onError(e);
						ToastUtil.showToast(R.string.load_failed);
					}

					@Override
					public void onResponse(Boolean arg0) {
						mKept = false;
						mKeep.setImageResource(R.drawable.article_detail_keep_normal);
						Toast.makeText(
								ArticleDetailActivity.this,
								getResources().getString(
										R.string.article_keep_cancel),
								Toast.LENGTH_SHORT).show();
						Preference.getInstance().saveMyFavCount(
								Preference.getInstance().getMyFavCount() - 1);
					}
				}, mArticleInfo.getId(), FavType.FAV_TYPE_DEL);
			} else {

				ReportModel.AddFavArticle(new ResponseListener<Boolean>(
						ArticleDetailActivity.this) {
					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
						super.onError(e);
						ToastUtil.showToast(R.string.load_failed);
					}

					@Override
					public void onResponse(Boolean arg0) {
						// TODO Auto-generated method stub
						// Toast.makeText(ArticleDetailActivity.this, "success",
						// Toast.LENGTH_SHORT).show();
						mKept = true;
						mKeep.setImageResource(R.drawable.article_detail_kept);
						Toast.makeText(
								ArticleDetailActivity.this,
								getResources().getString(
										R.string.article_keep_success),
								Toast.LENGTH_SHORT).show();
						Preference.getInstance().saveMyFavCount(
								Preference.getInstance().getMyFavCount() + 1);
					}

				}, mArticleInfo.getId(), FavType.FAV_TYPE_ADD);
			}

			StatsUtil.statsReport(this, "stats_collect", "article_name",
					mArticleDetail.getTitle());
			StatsUtil.statsReportByMta(this, "stats_collect", "article_name",
					mArticleInfo.getTitle());
			StatsUtil.statsReportByHiido("stats_collect", "article_name"
					+ mArticleDetail.getTitle());
			break;
		case R.id.tag_a: {
			Channel channel = mArticleDetail.getChannelList().get(0);
			ArticleListActivity.startChannelListActivity(
					ArticleDetailActivity.this, channel);
		}
			break;
		case R.id.tag_b: {
			Channel channel = mArticleDetail.getChannelList().get(1);
			ArticleListActivity.startChannelListActivity(
					ArticleDetailActivity.this, channel);
		}
			break;
		case R.id.tag_c: {
			Channel channel = mArticleDetail.getChannelList().get(2);
			ArticleListActivity.startChannelListActivity(
					ArticleDetailActivity.this, channel);
		}
			break;
		case R.id.like_comment:
			final ViewHolder holder = (ViewHolder) v.getTag();
			// if (COMMENT_MINE.equals(holder.mCommentId)) {
			// Toast.makeText(this,
			// getResources().getString(R.string.like_self_hint),
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			if (mMyCommentsLike.contains(holder.mComment.getId())) {
				Toast.makeText(this,
						getResources().getString(R.string.liked_hint),
						Toast.LENGTH_SHORT).show();
			} else {

				ReportModel.LikeComment(
						new ResponseListener<Boolean>(
								ArticleDetailActivity.this) {

							@Override
							public void onError(Exception e) {
								// TODO Auto-generated method stub
								super.onError(e);
								ToastUtil.showToast(R.string.load_failed);
							}

							@Override
							public void onResponse(Boolean arg0) {
								// TODO Auto-generated method stub
								// Toast.makeText(ArticleDetailActivity.this,
								// "success", Toast.LENGTH_SHORT).show();
								mMyCommentsLike.add(holder.mComment.getId());
								Preference.getInstance().saveMyCommentsLike(
										mMyCommentsLike);
								holder.mLike
										.setImageResource(R.drawable.article_detail_comment_like_pressed);
								int likeCount = Integer
										.parseInt(holder.mLikeCount.getText()
												.toString()) + 1;
								holder.mLikeCount.setText(Integer
										.toString(likeCount));
								holder.mComment.getCount().setLikeCount(
										likeCount);

							}

						}, mArticleInfo.getId(), holder.mComment.getId(),
						LikeType.LIKE_TYPE_LIKE);
			}
			break;
		case R.id.dislike_article_container:
			if (mMyArticlesDislike.contains(mArticleInfo.getId())) {
				Toast.makeText(this,
						getResources().getString(R.string.disliked_hint),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (mMyArticlesLike.contains(mArticleInfo.getId())) {
				Toast.makeText(this,
						getResources().getString(R.string.liked_hint),
						Toast.LENGTH_SHORT).show();
				return;
			}

			ReportModel.LikeArticle(new ResponseListener<Boolean>(
					ArticleDetailActivity.this) {

				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					super.onError(e);
					ToastUtil.showToast(R.string.load_failed);
				}

				@Override
				public void onResponse(Boolean arg0) {
					mMyArticlesDislike.add(mArticleInfo.getId());
					Preference.getInstance().saveMyArticlesDislike(
							mMyArticlesDislike);
					mDislikeArticle
							.setImageResource(R.drawable.article_detail_dislike_pressed);
					mDislikeCount.setText(Integer.toString(Integer
							.parseInt(mDislikeCount.getText().toString()) + 1));

				}

			}, mArticleInfo.getId(), LikeType.LIKE_TYPE_DISLIKE);
			break;
		case R.id.like_article_container:
			if (mMyArticlesDislike.contains(mArticleInfo.getId())) {
				Toast.makeText(this,
						getResources().getString(R.string.disliked_hint),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (mMyArticlesLike.contains(mArticleInfo.getId())) {
				Toast.makeText(this,
						getResources().getString(R.string.liked_hint),
						Toast.LENGTH_SHORT).show();
				return;
			}
			ReportModel.LikeArticle(new ResponseListener<Boolean>(
					ArticleDetailActivity.this) {

				@Override
				public void onError(Exception e) {
					super.onError(e);
					ToastUtil.showToast(R.string.load_failed);
				}

				@Override
				public void onResponse(Boolean arg0) {
					mMyArticlesLike.add(mArticleInfo.getId());
					Preference.getInstance()
							.saveMyArticlesLike(mMyArticlesLike);
					mLikeArticle
							.setImageResource(R.drawable.article_detail_like_pressed);
					mLikeCount.setText(Integer.toString(Integer
							.parseInt(mLikeCount.getText().toString()) + 1));

					LikeEvent event = new LikeEvent();
					event.id = mArticleInfo.getId();
					event.likeCount = Integer.parseInt(mLikeCount.getText()
							.toString());
					EventBus.getDefault().post(event);
				}

			}, mArticleInfo.getId(), LikeType.LIKE_TYPE_LIKE);
			break;
		case R.id.retry_article:
			loadArticle();
			break;
		case R.id.global_loading_text:
			mListAdapter.loadMore();
		default:
			break;

		}

	}

	private void postComment() {

		if (mArticleDetail == null) {
			return;
		}
		Intent intent = new Intent(ArticleDetailActivity.this,
				CommentActivity.class);
		intent.putExtra(CommentActivity.KEY_ARTICLE_ID, mArticleInfo.getId());
		startActivityForResult(intent, 0);
		StatsUtil.statsReport(this, "stats_comment", "article_name",
				mArticleDetail.getTitle());
		StatsUtil.statsReportByMta(this, "stats_comment", "article_name",
				mArticleInfo.getTitle());
		StatsUtil.statsReportByHiido("stats_comment", "article_name"
				+ mArticleDetail.getTitle());
	}

	private void notifyCommentCountChanged() {

		CommentEvent event = new CommentEvent();
		event.id = mArticleDetail.getId();
		event.commentCount = mArticleDetail.getCommentCount();
		EventBus.getDefault().post(event);
	}

	@Override
	public void onFlip() {
		// TODO Auto-generated method stub
		finish();
	}

	@Override
	public void onUp() {
		// TODO Auto-generated method stub
		// this.showMenu();
	}

	@Override
	public void onDown() {
		// TODO Auto-generated method stub
		// this.hideMenu();

	}

	// private void showDialog(DialogFragment f, String tag) {
	// // DialogFragment.show() will take care of adding the fragment
	// // in a transaction. We also want to remove any currently showing
	// // dialog, so make our own transaction and take care of that here.
	// FragmentManager fm = getSupportFragmentManager();
	// FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	// Fragment prev = fm.findFragmentByTag(tag);
	// if (prev != null) {
	// ft.remove(prev);
	// }
	// ft.addToBackStack(null);
	// f.show(ft, tag);
	// }

}
