package com.yy.android.gamenews.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ExtendedScrollView extends ScrollView {
	public boolean intercept = false;
	public ExtendedWebView webView;
	public ExtendedListView listView;

	// for slip out
	// 手指向右滑动时的最小速度
	private static final int YDISTANCE_MAX = 50;
	// 手指向右滑动时的最小距离
	private int XDISTANCE_MIN = 0;
	// 记录手指按下时的横坐标。
	private float xDown;
	private float yDown;
	// 记录手指移动时的横坐标。
	private float xMove;
	private float yMove;
	private OnFlipListener flipListener;

	public interface OnFlipListener {
		void onFlip();

		void onUp();

		void onDown();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (XDISTANCE_MIN == 0) {
			XDISTANCE_MIN = this.getWidth() / 5;
			if (XDISTANCE_MIN == 0) {
				return super.onInterceptTouchEvent(ev);
			}
		}

		float deltaY = 0;
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			xMove = ev.getRawX();
			yMove = ev.getRawY();
			// 活动的距离
			int distanceX = (int) (xMove - xDown);
			int distanceY = (int) Math.abs(yDown - yMove);
			// 当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
			if (distanceX > XDISTANCE_MIN && distanceY < YDISTANCE_MAX) {
				if (this.flipListener != null) { 
					this.flipListener.onFlip();
				}
			}
			deltaY = yDown - yMove;
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			xDown = ev.getRawX();
			yDown = ev.getRawY();
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			/* Release the drag */
			break;
		case MotionEvent.ACTION_POINTER_UP:
			break;
		}

		if (deltaY == 0) {
			return super.onInterceptTouchEvent(ev);
		}
		if (deltaY > 0 && flipListener != null) {
			flipListener.onDown();
		}
		if (deltaY < 0 && flipListener != null) {
			flipListener.onUp();
		}

		// deltaY > 0,手指往上。
		if (deltaY > 0 && !webView.canScrollVerticallyex(1)
				&& canScrollVerticallyex(1)) {
			intercept = true;
			if (listView.canScrollVerticallyex(-1)) {
				listView.setSelection(0);
			}
		} else if (deltaY > 0 && listView.canScrollVerticallyex(1)
				&& !canScrollVerticallyex(1)) {
			intercept = false;
		} else if (deltaY < 0 && webView.canScrollVerticallyex(-1)
				&& !canScrollVerticallyex(-1)) {
			intercept = false;
		} else if (deltaY < 0 && !listView.canScrollVerticallyex(-1)
				&& canScrollVerticallyex(-1)) {
			intercept = true;
			if (webView.canScrollVerticallyex(1)) {
				webView.loadUrl("javascript:webview.scrollBottom()");
				// webView.loadUrl("javascript:webview.nativeScrollBottom()");
				// webView.scrollTo(0, webView.getContentHeight());
			}
		}

		if (intercept) {
			return super.onInterceptTouchEvent(ev);
		}
		return false;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (intercept) {
			return super.onTouchEvent(ev);
		}
		return false;
	}

	public ExtendedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ExtendedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ExtendedScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public boolean canScrollVerticallyex(int direction) {
		final int offset = computeVerticalScrollOffset();
		final int range = computeVerticalScrollRange()
				- computeVerticalScrollExtent();
		if (range == 0)
			return false;
		if (direction < 0) {
			return offset > 0;
		} else {
			return offset < range - 1;
		}
	}

	public void setOnFlipListener(OnFlipListener flipListener) {
		this.flipListener = flipListener;
	}
}
