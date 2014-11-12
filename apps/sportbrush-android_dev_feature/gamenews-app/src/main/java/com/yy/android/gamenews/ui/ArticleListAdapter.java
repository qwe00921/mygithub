package com.yy.android.gamenews.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duowan.Comm.ECommAppType;
import com.duowan.gamenews.ArticleCategory;
import com.duowan.gamenews.ArticleFlag;
import com.duowan.gamenews.ArticleInfo;
import com.duowan.gamenews.ArticleType;
import com.duowan.gamenews.Channel;
import com.duowan.gamenews.SubType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.ui.common.ImageAdapter;
import com.yy.android.gamenews.ui.common.SwitchImageLoader;
import com.yy.android.gamenews.ui.view.AutoAdjustHelper;
import com.yy.android.gamenews.ui.view.AutoAdjustImageView;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class ArticleListAdapter extends ImageAdapter<ArticleInfo> {
	private List<Long> mViewedArticleList;
	private Context mContext;
	private Channel mChannel;
	private ArticleCategory mCategory;
	private DisplayImageOptions mWaterFallDisplayer = SwitchImageLoader.DEFAULT_ARTICLE_ITEM_WATERFALL_DISPLAYER;
	private DisplayImageOptions mBigDisplayer = SwitchImageLoader.DEFAULT_ARTICLE_ITEM_BIG_DISPLAYER;
	private DisplayImageOptions mDisplayer = SwitchImageLoader.DEFAULT_ARTICLE_ITEM_DISPLAYER;
	private DisplayImageOptions mBigDisplayerDark = SwitchImageLoader.DEFAULT_ARTICLE_ITEM_BIG_DISPLAYER_DARK;

	private static final String LOG_TAG = "[ArticleListAdapter]";

	public ArticleListAdapter(Context context) {
		super(context);
		mContext = context;
		// mViewedArticleList = new IPageCache().getObject(
		// Constants.CACHE_KEY_VIEWED_ARTICLE_LIST, ArrayList.class);
	}

	private boolean mIsDark;

	public void setIsDarkTheme(boolean isDarkTheme) {
		mIsDark = isDarkTheme;
	}

	public void setChannel(Channel channel) {
		mChannel = channel;
	}

	public void setCategory(ArticleCategory category) {
		mCategory = category;
	}

	public void setViewedArticleList(List<Long> list) {
		mViewedArticleList = list;
	}

	@Override
	public int getItemViewType(int position) {
		if (mIsDark) {
			return VIEW_TYPE_BIG_DARK;
		}
		if (mCategory != null) {
			if (mCategory.getId() == SubType._SUBTYPE_FALL) {
				return VIEW_TYPE_WATER_FALL;
			} else if (mCategory.getId() == SubType._SUBTYPE_VIDEO) {
				return VIEW_TYPE_BIG_SIMPLE;
			} else if (mCategory.getId() == SubType._SUBTYPE_GONGLUE) {
				return VIEW_TYPE_HORIZONTAL_IMG_LEFT;
			}
		}
		ArticleInfo model = getItem(position);
		if ((model.flag & ArticleFlag._ARTICLE_FLAG_BIGIMAGE) != 0) {
			return VIEW_TYPE_VERTICAL_BIG;
		}
		List<String> imageList = model.getImageList();

		if (imageList == null || imageList.size() < 2) {
			return VIEW_TYPE_HORIZONTAL;
		} else {
			return VIEW_TYPE_VERTICAL;
		}
	}

	private static final int VIEW_TYPE_HORIZONTAL = 0;
	private static final int VIEW_TYPE_VERTICAL = 1;
	private static final int VIEW_TYPE_VERTICAL_BIG = 2;
	private static final int VIEW_TYPE_BIG_DARK = 3;
	private static final int VIEW_TYPE_WATER_FALL = 4;
	private static final int VIEW_TYPE_HORIZONTAL_IMG_LEFT = 5;
	private static final int VIEW_TYPE_BIG_SIMPLE = 6;

	@Override
	public int getViewTypeCount() {

		return 7;
	}

	private boolean isItemViewed(ArticleInfo info) {
		if (info == null) {
			return false;
		}
		long id = info.getId();
		if (mViewedArticleList != null) {
			return mViewedArticleList.contains(id);
		}

		return false;
	}

	private DisplayImageOptions getDisplayOptions(int viewType) {
		switch (viewType) {
		case VIEW_TYPE_BIG_DARK: {
			return mBigDisplayerDark;
		}
		case VIEW_TYPE_WATER_FALL: {
			return mWaterFallDisplayer;
		}
		case VIEW_TYPE_BIG_SIMPLE:
		case VIEW_TYPE_VERTICAL_BIG: {
			return mBigDisplayer;
		}
		}

		return mDisplayer;
	}

	private View getConvertView(int viewType) {
		ViewHolder holder = null;
		View convertView = null;
		switch (viewType) {
		case VIEW_TYPE_HORIZONTAL: {
			convertView = mInflater.inflate(R.layout.list_item_article_h, null);
			break;
		}
		case VIEW_TYPE_VERTICAL: {
			convertView = mInflater.inflate(R.layout.list_item_article_v, null);
			break;
		}
		case VIEW_TYPE_VERTICAL_BIG: {
			convertView = mInflater.inflate(
					R.layout.list_item_article_v_single_big, null);
			break;
		}
		case VIEW_TYPE_BIG_DARK: {
			convertView = mInflater.inflate(
					R.layout.list_item_article_v_single_big_dark, null);
			break;
		}
		case VIEW_TYPE_BIG_SIMPLE: {
			convertView = mInflater.inflate(
					R.layout.list_item_article_big_simple, null);
			break;
		}
		case VIEW_TYPE_WATER_FALL: {
			convertView = mInflater.inflate(
					R.layout.list_item_article_waterfall, null);
			break;
		}
		case VIEW_TYPE_HORIZONTAL_IMG_LEFT: {
			convertView = mInflater.inflate(
					R.layout.list_item_article_h_image_left, null);
			break;
		}
		default: {
			// never goes here
			break;
		}
		}
		holder = new ViewHolder();
		holder.mCommentCount = (TextView) convertView
				.findViewById(R.id.list_article_count);
		holder.mFrom = (TextView) convertView
				.findViewById(R.id.list_article_from);
		holder.imageViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_img1));
		holder.imageViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_img2));
		holder.imageViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_img3));
		holder.maskViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_mask_img1));
		holder.maskViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_mask_img2));
		holder.maskViewList.add((ImageView) convertView
				.findViewById(R.id.list_article_mask_img3));
		holder.mFavCount = (TextView) convertView
				.findViewById(R.id.list_article_fav);
		holder.mTitle = (TextView) convertView
				.findViewById(R.id.list_article_title);
		holder.mCornerImg = (ImageView) convertView
				.findViewById(R.id.list_article_corner);
		holder.mInfoLayout = convertView
				.findViewById(R.id.list_article_info_layout);
		holder.mVideoIcon = convertView.findViewById(R.id.list_article_video);
		holder.hotView = convertView.findViewById(R.id.iv_hot);
		holder.orderView = convertView
				.findViewById(R.id.list_articl_order_layout);
		holder.orderTextView = (TextView) convertView
				.findViewById(R.id.tv_order);

		if (convertView != null) {
			convertView.setTag(holder);
		}
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		int itemViewType = getItemViewType(position);
		if (convertView == null) {
			convertView = getConvertView(itemViewType);
		}

		holder = (ViewHolder) convertView.getTag();

		if (mIsDark) {
			convertView
					.setBackgroundResource(R.drawable.article_list_item_selector_dark);
		} else {

			convertView
					.setBackgroundResource(R.drawable.article_list_item_selector);
		}

		// Log.d(LOG_TAG, "[getView] + for item :" + position);
		if (holder != null) {
			ArticleInfo model = getItem(position);
			if (model != null) {
				updateInfo(holder, model, position);
				updateCorner(holder, model, convertView);
				updateImage(holder, model, itemViewType);
			}
		}
		return convertView;
	}

	private void updateInfo(ViewHolder holder, ArticleInfo model, int position) {
		if (model == null) {
			return;
		}

		if (holder.orderView != null) {
			if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH) && mChannel != null && mChannel.getId() == Constants.RECOMMD_ID
					&& position < 9) {
				holder.orderView.setVisibility(View.VISIBLE);
				holder.orderTextView.setText(String.valueOf(position + 1));
			} else {
				holder.orderView.setVisibility(View.INVISIBLE);
			}
		}

		int articleType = model.getArticleType();
		int style = 0;
		if (articleType == ArticleType._ARTICLE_TYPE_CAIDAN) {
			style = R.style.HomeListBingoPrimaryText;
		} else if (isItemViewed(model)) {
			style = R.style.HomeListPrimaryTextDisabled;
			if (mIsDark) {
				style = R.style.HomeListPrimaryTextDisabledDark;
			} else if (getItemViewType(position) == VIEW_TYPE_WATER_FALL) {
				style = R.style.WaterFallListPrimaryTextDisabled;
			}
		} else {
			style = R.style.HomeListPrimaryText;
			if (mIsDark) {
				style = R.style.HomeListPrimaryTextDark;
			} else if (getItemViewType(position) == VIEW_TYPE_WATER_FALL) {
				style = R.style.WaterFallListPrimaryText;
			}
		}

		if (holder.mTitle != null) {

			holder.mTitle.setTextAppearance(mContext, style);
			holder.mTitle.setText(model.getTitle());
		}

		boolean needShowLayout = false;
		// 来源
		String sourceName = model.getSourceName();
		if (!TextUtils.isEmpty(sourceName)) {
			if (mChannel != null) {
				int id = mChannel.getId();
				String channelName = model.getChannelName();
				if (id == Constants.MY_FAVOR_CHANNEL_ID) {
					if (!TextUtils.isEmpty(channelName)) {
						sourceName = String.format("%s-%s", channelName,
								sourceName);
					}
				}
			}
		}

		int visibility = 0;
		if (!TextUtils.isEmpty(sourceName)) {
			visibility = View.VISIBLE;
			needShowLayout = true;
		} else {
			visibility = View.GONE;
		}

		if (holder.mFrom != null) {
			holder.mFrom.setText(sourceName);
			holder.mFrom.setVisibility(visibility);
		}

		// 视频标签
		boolean isVideo = (model.getFlag() & ArticleFlag._ARTICLE_FLAG_VIDEO) != 0;
		if (isVideo) {
			visibility = View.VISIBLE;
			needShowLayout = true;
		} else {
			visibility = View.GONE;
		}
		if (holder.mVideoIcon != null) {

			holder.mVideoIcon.setVisibility(visibility);
		}

		// 点赞数
		int favCount = model.getPraiseCount();
		if (favCount > 0) {

			visibility = View.VISIBLE;
			needShowLayout = true;
		} else {
			visibility = View.GONE;
		}

		if (holder.mFavCount != null) {
			holder.mFavCount.setText(String.valueOf(favCount));
			holder.mFavCount.setVisibility(visibility);
		}

		// 评论数
		int commentCount = model.getCommentCount();
		if (commentCount > 0) {
			needShowLayout = true;
			if (holder.mCommentCount != null && holder.hotView != null) {
				holder.mCommentCount.setText("" + model.getCommentCount());
				holder.mCommentCount.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.home_ic_comment_count, 0, 0, 0);
				holder.mCommentCount.setVisibility(View.VISIBLE);
				holder.hotView.setVisibility(View.GONE);
				if (Constants.isFunctionEnabled(ECommAppType._Comm_APP_SPORTBRUSH) && mChannel != null
						&& mChannel.getId() == Constants.RECOMMD_ID) {
					holder.mCommentCount
							.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
					holder.mCommentCount.setText(String.format(
							mContext.getResources().getString(
									R.string.comment_count_formater),
							model.getCommentCount()));
					holder.hotView.setVisibility(View.VISIBLE);
				}
			}

		} else {
			if (holder.mCommentCount != null && holder.hotView != null) {
				holder.mCommentCount.setVisibility(View.GONE);
				holder.hotView.setVisibility(View.GONE);
			}
		}

		if (holder.mInfoLayout != null) {
			if (needShowLayout) {
				holder.mInfoLayout.setVisibility(View.VISIBLE);
			} else {
				holder.mInfoLayout.setVisibility(View.GONE);
			}
		}
	}

	private void updateCorner(ViewHolder holder, ArticleInfo model,
			View convertView) {
		if (model == null || holder.mCornerImg == null) {
			return;
		}
		holder.mCornerImg.setVisibility(View.GONE);
		int articleType = model.getArticleType();
		switch (articleType) {
		case ArticleType._ARTICLE_TYPE_SPECIAL: {
			holder.mCornerImg.setImageResource(R.drawable.ic_special_corner);
			holder.mCornerImg.setVisibility(View.VISIBLE);
			break;
		}
		case ArticleType._ARTICLE_TYPE_ACTIVITY: {
			holder.mCornerImg.setImageResource(R.drawable.ic_active_corner);
			holder.mCornerImg.setVisibility(View.VISIBLE);
			convertView
					.setBackgroundResource(R.drawable.article_list_item_active_selector);
			break;
		}
		case ArticleType._ARTICLE_TYPE_CAIDAN: {
			holder.mCornerImg.setImageResource(R.drawable.ic_bingo_corner);
			holder.mCornerImg.setVisibility(View.VISIBLE);
			convertView
					.setBackgroundResource(R.drawable.article_list_item_bingo_selector);
			break;
		}
		case ArticleType._ARTICLE_TYPE_BANG: {
			holder.mCornerImg.setImageResource(R.drawable.ic_data_corner);
			holder.mCornerImg.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			int resId = 0;
			long flag = model.flag;
			if ((flag & ArticleFlag._ARTICLE_FLAG_ADV) == ArticleFlag._ARTICLE_FLAG_ADV) {
				resId = R.drawable.ic_adv;
			} else if ((flag & ArticleFlag._ARTICLE_FLAG_HOT) == ArticleFlag._ARTICLE_FLAG_HOT) {
				resId = R.drawable.ic_hot;
			} else if ((flag & ArticleFlag._ARTICLE_FLAG_RECOMM) == ArticleFlag._ARTICLE_FLAG_RECOMM) {
				resId = R.drawable.ic_recom;
			}
			if (resId != 0) {
				holder.mCornerImg.setImageResource(resId);
				holder.mCornerImg.setVisibility(View.VISIBLE);
			}
			break;
		}
		}
	}

	private void updateImage(ViewHolder holder, ArticleInfo model, int viewType) {
		if (model == null) {
			return;
		}
		List<String> videoList = model.getVideoList();
		List<String> urlList = model.getImageList();

		List<String> imgList = null;
		boolean hasVideoList = false;
		if ((model.flag & ArticleFlag._ARTICLE_FLAG_BIGIMAGE) != 0) {
			String bigImage = model.extraInfo
					.get((long) ArticleFlag._ARTICLE_FLAG_BIGIMAGE);
			imgList = new ArrayList<String>();
			imgList.add(bigImage);
		} else if (videoList != null && videoList.size() > 0) {
			imgList = videoList;
		} else {
			imgList = urlList;
		}
		for (int i = 0; i < holder.imageViewList.size(); i++) {
			ImageView view = holder.imageViewList.get(i);
			ImageView mask = holder.maskViewList.get(i);
			if (mask == null || view == null) {
				continue;
			}

			// 大图模式按后台配置的宽和高来展示，如果没有配置，则按比例展示
			if ((model.flag & ArticleFlag._ARTICLE_FLAG_BIGIMAGE) != 0) {
				String imageWidth = model.extraInfo
						.get((long) ArticleFlag._ARTICLE_FLAG_BIGIMAGE_WIDTH);
				String imageHeight = model.extraInfo
						.get((long) ArticleFlag._ARTICLE_FLAG_BIGIMAGE_HEIGHT);

				int width = 0;
				int height = 0;
				try {
					width = Integer.parseInt(imageWidth);
					height = Integer.parseInt(imageHeight);
				} catch (Exception e) {

				}
				if (view instanceof AutoAdjustImageView) {
					AutoAdjustImageView autoView = (AutoAdjustImageView) view;
					AutoAdjustImageView autoMask = (AutoAdjustImageView) mask;
					if (width != 0 && height != 0) {

						autoView.setCustWidth(width);
						autoView.setCustHeight(height);
						autoView.setAdjustType(AutoAdjustHelper.AUTO_ADJUST_HEIGHT);
						autoView.invalidate();

						autoMask.setCustWidth(width);
						autoMask.setCustHeight(height);
						autoMask.setAdjustType(AutoAdjustHelper.AUTO_ADJUST_HEIGHT);
						autoMask.invalidate();
					} else {
						autoView.setAdjustType(AutoAdjustHelper.AUTO_ADJUST_SCALE_HEIGHT);
						autoView.invalidate();
						autoMask.setAdjustType(AutoAdjustHelper.AUTO_ADJUST_SCALE_HEIGHT);
						autoMask.invalidate();
					}
				}

			}

			if (hasVideoList) {
				mask.setVisibility(View.VISIBLE);
			} else {
				mask.setVisibility(View.GONE);
			}

			String url = null;
			if (imgList != null && i < imgList.size()) {
				url = imgList.get(i);
			}
			if (url != null) {
				((View) view.getParent()).setVisibility(View.VISIBLE);
				view.setVisibility(View.VISIBLE);
				// view.setImageResource(R.drawable.article_list_item_loading);
				displayImage(url, view, getDisplayOptions(viewType));
				if (viewType == VIEW_TYPE_VERTICAL) {
					int padding = 0;
					if (i == 0) {
						padding = Util.dip2px(mContext, 5f);
						((View) view.getParent()).setPadding(0, 0, padding, 0);
					} else if (i == 1) {
						padding = Util.dip2px(mContext, 2.5f);
						((View) view.getParent()).setPadding(padding, 0,
								padding, 0);
					} else if (i == 2) {
						padding = Util.dip2px(mContext, 5f);
						((View) view.getParent()).setPadding(padding, 0, 0, 0);
					}
				}
			} else {
				((View) view.getParent()).setVisibility(View.GONE);
				view.setVisibility(View.GONE);
				if (i == 1) {
					((View) holder.imageViewList.get(i - 1).getParent())
							.setPadding(0, 0, 0, 0);
				} else if (i == 2) {
					int padding = Util.dip2px(mContext, 3.5f);
					((View) holder.imageViewList.get(i - 2).getParent())
							.setPadding(0, 0, padding, 0);
					((View) holder.imageViewList.get(i - 1).getParent())
							.setPadding(padding, 0, 0, 0);
				}
			}
		}
	}

	private static class ViewHolder {
		TextView mTitle;
		TextView mFrom;
		TextView mCommentCount;
		TextView mFavCount;
		View mVideoIcon;
		List<ImageView> imageViewList = new ArrayList<ImageView>();
		List<ImageView> maskViewList = new ArrayList<ImageView>();
		View mInfoLayout;
		// ImageView mImageView1;
		// ImageView mImageView2;
		// ImageView mImageView3;
		ImageView mCornerImg;

		View hotView;
		View orderView;
		TextView orderTextView;
	}
}
