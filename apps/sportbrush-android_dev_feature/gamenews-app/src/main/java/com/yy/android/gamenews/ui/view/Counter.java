package com.yy.android.gamenews.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

public class Counter {

	// private float mStartValue;
	// private float mEndValue;
	// private float mCurrentValue;
	// private float mValueInterval;
	// private int mDuration;
	private Handler mHandler = new Handler();
	// private int mStatus;
	// private static final int STATUS_START = 0;
	// private static final int STATUS_CANCEL = 1;
	// private static final int STATUS_PAUSE = 2;
	// private static final int STATUS_FINISH = 3;
	// private static final String TAG = "Timer";
	private List<CounterItem> mItemList;

	public void setValue(CounterItem... itemList) {
		List<CounterItem> list = new ArrayList<CounterItem>();
		for (CounterItem item : itemList) {

			list.add(item);
		}
		setValue(list);
	}

	public void setValue(List<CounterItem> list) {
		mItemList = list;
	}

	private Runnable mRunnable = new Runnable() {
		public void run() {

			mOnTimerCallback.onUpdate(mItemList);
			boolean needContinue = false;
			for (CounterItem item : mItemList) {
				if (item == null) {
					continue;
				}
				if (!item.isEnd()) {
					needContinue = true;
					item.timing();
				}
			}
			if (needContinue) {
				mHandler.postDelayed(mRunnable, 0);
			} else {
				stop();
			}
		}
	};

	// public void setDuration(int duration) {
	// mDuration = duration;
	// }

	public void start() {
		mHandler.post(mRunnable);
	}

	// private void goStop() {
	// mCurrentValue = mEndValue;
	// mOnTimerCallback.onUpdate(mEndValue);
	// }

	private void checkEnd() {
		if (mOnTimerCallback != null) {
			mOnTimerCallback.onStop();
		}
	}

	public void stop() {
		// goStop();
		checkEnd();
	}

	public void pause() {
		// TODO: implement
	}

	public void cancel() {
		// TODO: implement
	}

	public interface OnCounterCallback {
		public void onStart();

		public void onStop();

		public void onPause();

		public void onUpdate(List<CounterItem> list);
	}

	private OnCounterCallback mOnTimerCallback;

	public void setOnTimerCallback(OnCounterCallback onTimerCallback) {
		mOnTimerCallback = onTimerCallback;
	}

}
