package com.yy.android.gamenews.ui.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class BannerView extends AutoAdjustLinearLayout {
	// 视图切换时的滚动速度
	private static final int SCROLL_SPEED = 1000;
	// 视图切换的间隔
	private static final int SCROLL_DELAY = 4000;

	private static final String TAG = "Banner";
	private ListAdapter mListAdapter;
	private BannerAdapter mAdapter;
	private ViewPager mViewPager;

	private DataSetObserver mObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			notifyDatasetChanged();
			super.onChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDatasetChanged();
			super.onInvalidated();
		}

		private void notifyDatasetChanged() {
			int count = mListAdapter.getCount();
			mViewPager.setAdapter(null);
			mAdapter.setAdapterSize(count);
			mViewPager.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			mViewPager.setCurrentItem(mAdapter.getFirstItemPos(), false);
			animateInternal();
		}
	};

	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BannerView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		mViewPager = new ViewPager(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		mViewPager.setLayoutParams(params);

		try {
			Field mField = ViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);
			FixedSpeedScroller mScroller = new FixedSpeedScroller(getContext(),
					new Interpolator() {
						public float getInterpolation(float t) {
							t -= 1.0f;
							return t * t * t * t * t + 1.0f;
						}
					});
			mField.set(mViewPager, mScroller);
		} catch (Exception e) {
			e.printStackTrace();
		}

		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN: {
					pauseAnimateInternal();
					break;
				}
				case MotionEvent.ACTION_UP: {
					animateInternal();
					break;
				}
				}

				return false;
			}
		});

		addView(mViewPager);
		// setOnPageChangeListener(this);
	}

	private class FixedSpeedScroller extends Scroller {
		private int mDuration = SCROLL_SPEED;

		public FixedSpeedScroller(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		public FixedSpeedScroller(Context context, Interpolator interpolator) {
			super(context, interpolator);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy,
				int duration) {
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			// Ignore received duration, use fixed one instead
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		public void setDuration(int time) {
			mDuration = time;
		}
	}

	public void setAdapter(ListAdapter adapter) {
		if (mListAdapter != null) {
			mListAdapter.unregisterDataSetObserver(mObserver);
		}
		mListAdapter = adapter;

		if (adapter != null) {
			adapter.registerDataSetObserver(mObserver);
			if (mAdapter == null) {
				mAdapter = new BannerAdapter();
			}
			mAdapter.setAdapterSize(mListAdapter.getCount());
			mViewPager.setAdapter(mAdapter);
			mViewPager.setCurrentItem(mAdapter.getFirstItemPos());
		} else {
			mViewPager.setAdapter(null);
		}
	}

	private class BannerAdapter extends PagerAdapter {

		public static final int CACHE_VIEW_SIZE = 4;
		public static final int SCROLLABLE_PAGER_SIZE = Integer.MAX_VALUE;
		public static final int SINGLE_PAGE_SIZE = 1;
		private List<ItemInfo> mViewList = new ArrayList<ItemInfo>(
				CACHE_VIEW_SIZE);
		private int mPageSize = SCROLLABLE_PAGER_SIZE;
		private int mStartPos = SCROLLABLE_PAGER_SIZE / 2;

		public void setAdapterSize(int size) {
			if (size > 1) {
				mPageSize = SCROLLABLE_PAGER_SIZE;
			} else if (size == 1) {
				mPageSize = SINGLE_PAGE_SIZE;
			} else {
				mPageSize = 0;
			}

			mStartPos = mPageSize / 2;
		}

		@Override
		public void notifyDataSetChanged() {
			for (ItemInfo info : mViewList) {
				if (info != null) {
					info.position = POSITION_NONE;
				}
			}
			super.notifyDataSetChanged();
		}

		public boolean isScrollable() {
			return mPageSize == SCROLLABLE_PAGER_SIZE;
		}

		public int getFirstItemPos() {
			return mStartPos;
		}

		@Override
		public int getCount() {
			return mPageSize;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		private int getViewPos(int position) {
			return getRelativePos(position, CACHE_VIEW_SIZE);
		}

		private int getItemPos(int position) {
			if (mListAdapter == null) {
				return 0;
			}
			int count = mListAdapter.getCount();
			if (count <= 0) {
				return 0;
			}
			return getRelativePos(position, count);
		}

		private int getRelativePos(int position, int size) {

			int relativePos = position - mStartPos;
			int returnPos = 0;
			if (relativePos >= 0) {
				returnPos = relativePos % size;
			} else {
				returnPos = (size + relativePos % size) % size;
			}
			Log.d(TAG, "[getRelativePos] position = " + position
					+ ", returnPos = " + returnPos + ", size = " + size);

			return returnPos;
		}

		private View getCurrentView(int position) {
			int viewPos = getViewPos(position);
			View view = null;
			if (mViewList.size() > viewPos) {
				ItemInfo info = mViewList.get(viewPos);
				if (info != null) {
					view = info.obj;
				}
			}
			return view;
		}

		private void cacheCurrentView(int position, View view) {

			int viewPos = getViewPos(position);

			if (viewPos >= mViewList.size()) {
				ItemInfo info = new ItemInfo();
				info.obj = view;
				info.position = position;
				for (int i = mViewList.size(); i < viewPos; i++) {
					mViewList.add(new ItemInfo());
				}
				mViewList.add(info);
			} else {
				ItemInfo info = mViewList.get(viewPos);
				if (info == null) {
					info = new ItemInfo();
					mViewList.set(viewPos, info);
				}
				if (info.position == position && view.equals(info.obj)) {
					info.position = POSITION_UNCHANGED;
				}
				info.obj = view;
			}
		}

		@Override
		public int getItemPosition(Object object) {
			int position = POSITION_NONE;
			for (ItemInfo info : mViewList) {

				if (object.equals(info.obj)) {
					position = info.position;
				}
			}
			return position;
		}

		@Override
		public float getPageWidth(int position) {
			// TODO Auto-generated method stub
			return super.getPageWidth(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(getCurrentView(position));
		}

		/**
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			View returnView = getView(container, position);
			container.addView(returnView);

			// getView(container, position++);
			return returnView;
		}

		private View getView(ViewGroup container, int position) {
			View view = getCurrentView(position);
			final int itemPos = getItemPos(position);
			View returnView = mListAdapter.getView(itemPos, view, container);
			if (returnView.getParent() != null) {
				((ViewGroup) returnView.getParent()).removeView(returnView);
			}

			cacheCurrentView(position, returnView);

			returnView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOnItemClickListener != null) {
						mOnItemClickListener.onBannerItemClick(v, mListAdapter,
								itemPos);
					}
				}
			});
			return returnView;
		}

		private class ItemInfo {
			View obj;
			int position;
		}
	}

	/**
	 * 是否是用户调用
	 */
	public boolean mScrollEnabled;

	public void startScroll() {
		mScrollEnabled = true;
		animateInternal();
	}

	public void pauseScroll() {
		mScrollEnabled = false;
		pauseAnimateInternal();
	}

	private void pauseAnimateInternal() {
		mViewPager.removeCallbacks(mAnimateNextRunnable);
	}

	private void animateInternal() {
		if (mScrollEnabled && mAdapter != null && mAdapter.isScrollable()) {
			mViewPager.removeCallbacks(mAnimateNextRunnable);
			mViewPager.postDelayed(mAnimateNextRunnable, SCROLL_DELAY);
		}
	}

	private Runnable mAnimateNextRunnable = new Runnable() {
		public void run() {
			int next = mViewPager.getCurrentItem() + 1;
			if (next >= mAdapter.getCount()) {
				next = mAdapter.getCount() - 1;
			}

			mViewPager.setCurrentItem(next);
			animateInternal();
		};
	};

	private OnBannerItemClickListener mOnItemClickListener;

	public void setOnItemClickListener(OnBannerItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public interface OnBannerItemClickListener {
		public void onBannerItemClick(View view, ListAdapter adapter, int position);
	}
}
