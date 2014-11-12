package com.yy.android.gamenews.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.yy.android.gamenews.event.ImageZoomEvent;
import com.yy.android.gamenews.ui.view.Counter.OnCounterCallback;
import com.yy.android.gamenews.util.Util;

import de.greenrobot.event.EventBus;

/**
 * 使用矩阵对图片进行缩放和位移，支持双击以及手势缩放。 默认设置scaleType为matrix, 图片为居中显示(类似于centerInside)
 * 
 * @author liuchaoqun
 * 
 */
public class ScalableImageView extends ImageView implements
		ChildTouchIntercepter {

	private static final float ACCURACY = 0.001f;
	private SimpleOnGestureListener mMouseGestureListener;
	private GestureDetector mGestureDetector;
	private Counter mRevertCounter = new Counter();

	private float mMinScale;
	private float mMaxScale;
	private float mMidScale;
	boolean mIsAnimating;
	private OnCounterCallback mRevertCallback = new OnCounterCallback() {

		private float mProgress;
		private float tX;
		private float tY;

		@Override
		public void onUpdate(List<CounterItem> list) {
			CounterItem scaleItem = list.get(0);
			CounterItem transItemX = null;
			if (list.size() > 1) {
				transItemX = list.get(1);
			}
			CounterItem transItemY = null;
			if (list.size() > 2) {

				transItemY = list.get(2);
			}
			// 第一次会传start的值，用于初始化
			if (!mIsAnimating) {
				if (scaleItem != null) {
					mProgress = scaleItem.getValue();
				}
				if (transItemX != null) {
					tX = transItemX.getValue();
				}
				if (transItemY != null) {
					tY = transItemY.getValue();
				}
				mIsAnimating = true;
				return;
			}

			Log.d(TAG, "[mRevertCallback]tX = " + tX + ", tY = " + tY);
			// 计算位移
			float valueX = 0;
			if (transItemX != null) {

				valueX = transItemX.getValue() - tX; // valueX为本次位移需移动的距离，如从第1个像素移动到第10个像素，则移动距离为9
				tX = transItemX.getValue();
			}
			float valueY = 0;
			if (transItemY != null) {

				valueY = transItemY.getValue() - tY;
				tY = transItemY.getValue();
			}

			mCurrentMatrix.postTranslate(valueX, valueY);
			if (scaleItem != null) {
				Log.d(TAG, "[mRevertCallback]progress = " + mProgress);

				float divide = scaleItem.getValue() / mProgress;
				mProgress = scaleItem.getValue();
				mCurrentMatrix
						.postScale(divide, divide, mid.x + tX, mid.y + tY); // 通过位移计算相应的scale中点
			}

			setImageMatrix(mCurrentMatrix);
		}

		@Override
		public void onStop() {
			mIsAnimating = false;
			mProgress = 0;
			tX = 0;
			tY = 0;
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onPause() {
		}
	};

	private static final String TAG = "ScreenView";

	private Context mContext;

	public void setGestureListener(SimpleOnGestureListener mouseGestureListener) {
		mMouseGestureListener = mouseGestureListener;
		mGestureDetector = new GestureDetector(mContext, mMouseGestureListener);
	}

	public ScalableImageView(Context context) {
		this(context, null);
	}

	public ScalableImageView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public ScalableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		initOriginalBmpSize(false);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		initOriginalBmpSize(true);
	}

	void init(Context context) {
		mRevertCounter.setOnTimerCallback(mRevertCallback);
		Matrix matrix = getImageMatrix();
		mCurrentMatrix.set(matrix);
		setGestureListener(new SimpleOnGestureListener() {

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				doubleTapToScale(e);
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				ImageZoomEvent event = new ImageZoomEvent();
				event.setMotionEvent(e);
				EventBus.getDefault().post(event);
				return super.onSingleTapConfirmed(e);
			}
		});
	}

	private float getScaleX(Matrix matrix) {
		float[] newValues = new float[9];
		matrix.getValues(newValues);
		return newValues[0];
	}

	private float getScaleY(Matrix matrix) {
		float[] newValues = new float[9];
		matrix.getValues(newValues);
		return newValues[4];
	}

	private float getImageLeft(Matrix matrix) {
		float[] newValues = new float[9];
		matrix.getValues(newValues);
		return newValues[2];
	}

	private float getImageTop(Matrix matrix) {
		float[] newValues = new float[9];
		matrix.getValues(newValues);
		return newValues[5];
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	private boolean mCalculated;

	public void initOriginalBmpSize(boolean reInflate) {

		// ScaleType必须为matrix时才能进行缩放和位移操作
		setScaleType(ScaleType.MATRIX);
		if (mCalculated && !reInflate) {
			return;
		}

		Drawable drawable = getDrawable();
		if (drawable == null || mCurrentMatrix == null) {
			return;
		}
		reInflate = true;
		int drawableWidth = drawable.getIntrinsicWidth();
		int drawableHeight = drawable.getIntrinsicHeight();

		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		int width = getWidth();
		int height = getHeight();

		int viewWidth = width == 0 ? measuredWidth : width;
		int viewHeight = height == 0 ? measuredHeight : height;
		if (viewWidth == 0 || viewHeight == 0) {
			mCalculated = false;
			return;
		}
		mCalculated = true;

		float scaleX = (float) viewWidth / (float) drawableWidth;

		float scaleY = (float) viewHeight / (float) drawableHeight;
		mMinScale = scaleX < scaleY ? scaleX : scaleY;
		mMaxScale = mMinScale * 4;
		mMidScale = mMinScale * 2f;

		Matrix matrix = getImageMatrix();
		matrix.setScale(mMinScale, mMinScale);
		mCurrentMatrix.set(matrix);
		doCheckTranslateBack(false);
		setImageMatrix(mCurrentMatrix);
	}

	private float getImageRight(Matrix matrix) {
		Drawable drawable = getDrawable();
		if (drawable != null) {

			float bmpWidth = drawable.getIntrinsicWidth() * getScaleX(matrix);

			return bmpWidth + getImageLeft(matrix);
		}

		return getWidth();
	}

	private float getImageBottom(Matrix matrix) {
		Drawable drawable = getDrawable();
		if (drawable != null) {

			float bmpHeight = drawable.getIntrinsicHeight() * getScaleY(matrix);

			return bmpHeight + getImageTop(matrix);
		}

		return getHeight();
	}

	Matrix mCurrentMatrix = new Matrix();
	Matrix mSavedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist;

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mIsAnimating) {
			return true;
		}
		// Handle touch events here...
		boolean isGestureDetected = false;
		if (mGestureDetector != null) {
			isGestureDetected = mGestureDetector.onTouchEvent(event);
		}
		if (isGestureDetected) {
			return true;
		}

		ImageView view = this;
		boolean isDown = false;
		// Log.d(TAG, "onTouchEvent, event = " + event.getAction());
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mSavedMatrix.set(mCurrentMatrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			isDown = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			if (mode != NONE) {
				checkRevert(event);
			}

			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				mSavedMatrix.set(mCurrentMatrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				float fromX = start.x;
				float toX = event.getX();

				float fromY = start.y;
				float toY = event.getY();

				if (Math.abs(toX - fromX) < 10 && Math.abs(toY - fromY) < 10) {
					break;
				}

				float deltaX = toX - fromX;
				float deltaY = toY - fromY;

				mCurrentMatrix.set(mSavedMatrix);
				mCurrentMatrix.postTranslate(deltaX, deltaY);

			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				float scaleRate = newDist / oldDist;

				float currentScaleRate = getScaleX(mSavedMatrix);
				if (currentScaleRate * scaleRate > mMaxScale) {
					scaleRate = mMaxScale / currentScaleRate;
				}
				if (newDist > 10f) {
					mCurrentMatrix.set(mSavedMatrix);
					mCurrentMatrix
							.postScale(scaleRate, scaleRate, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(mCurrentMatrix);
		if (mode == ZOOM) {
			return true;
		}

		if (isDown) {
			return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * check the bounds/size of the image against the imageview, if exceed the
	 * bounds/minimun size of the imageview, it will adjust the image view to
	 * the bounds/size
	 * 
	 * @param event
	 * @return
	 */
	private void checkRevert(MotionEvent event) {

		List<CounterItem> timeItems = new ArrayList<CounterItem>();
		CounterItem zoomItem = getZoomBackItem(event, mCurrentMatrix);
		timeItems.add(zoomItem);

		Matrix matrix = new Matrix();
		matrix.set(mCurrentMatrix);

		boolean needRevert = false;
		if (zoomItem != null) {
			float scaleRate = zoomItem.getEnd() / zoomItem.getStart();
			matrix.postScale(scaleRate, scaleRate, mid.x, mid.y);
		}

		needRevert |= zoomItem != null;

		List<CounterItem> transItems = getTranslateBackItems(matrix);
		if (mode == ZOOM || mode == DRAG) { // 只有当操作是zoom和drag时才需要做
			timeItems.addAll(transItems);
		}

		needRevert |= timeItems.size() > 0;

		if (needRevert) {
			CounterItem[] items = new CounterItem[timeItems.size()];
			timeItems.toArray(items);
			mRevertCounter.setValue(items);
			mRevertCounter.start();
		}
	}

	private CounterItem getZoomBackItem(MotionEvent event, Matrix matrix) {
		if (mode == ZOOM) {
			float currentScaleX = getScaleX(matrix);
			if (currentScaleX < mMinScale) {
				return new CounterItem(currentScaleX, mMinScale);
			}
		}
		return null;
	}

	private List<CounterItem> getTranslateBackItems(Matrix matrix) {
		List<CounterItem> itemList = new ArrayList<CounterItem>();

		float imageLeft = getImageLeft(matrix);
		float imageTop = getImageTop(matrix);
		float imageDown = getImageBottom(matrix);
		float imageRight = getImageRight(matrix);

		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		int width = getWidth();
		int height = getHeight();

		int viewWidth = width == 0 ? measuredWidth : width;
		int viewHeight = height == 0 ? measuredHeight : height;

		float deltaX = 0;
		float deltaY = 0;

		float viewCenterX = viewWidth / 2;
		float viewCenterY = viewHeight / 2;
		float imageCenterX = (imageRight + imageLeft) / 2;
		float imageCenterY = (imageDown + imageTop) / 2;

		if (imageRight - imageLeft < viewWidth) {
			if (imageLeft > 0 || imageRight < viewWidth) {
				deltaX = viewCenterX - imageCenterX;
			}
		} else {
			if (imageLeft >= 0) {

				deltaX = 0 - imageLeft;
			}

			if (imageRight <= viewWidth) {
				deltaX = viewWidth - imageRight;
			}
		}

		if (imageDown - imageTop < viewHeight) {
			if (imageTop > 0 || imageDown < viewHeight) {
				deltaY = viewCenterY - imageCenterY;
			}
		} else {
			if (imageTop >= 0) { // If reaches left edge

				deltaY = 0 - imageTop;
			}
			if (imageDown <= viewHeight) {
				deltaY = viewHeight - imageDown;
			}
		}

		Log.d(TAG, "deltaX = " + deltaX + ", deltaY = " + deltaY);
		if (Math.abs(deltaX) < ACCURACY && Math.abs(deltaY) < ACCURACY) {
			return itemList;
		}

		CounterItem xItem = new CounterItem(0, deltaX);
		CounterItem yItem = new CounterItem(0, deltaY);

		itemList.add(xItem);
		itemList.add(yItem);

		return itemList;
	}

	private void doCheckTranslateBack(boolean anim) {
		List<CounterItem> itemList = getTranslateBackItems(mCurrentMatrix);

		if (itemList.size() == 0) {
			return;
		}
		float deltaX = itemList.get(0).getEnd();
		float deltaY = itemList.get(1).getEnd();
		if (deltaX == 0 && deltaY == 0) {
			return;
		}

		mCurrentMatrix.postTranslate(deltaX, deltaY);
		setImageMatrix(mCurrentMatrix);
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * 当Scale时获取两个手指的中间位置
	 * 
	 * @param point
	 * @param event
	 */
	private void midPoint(PointF point, MotionEvent event) {

		float x = 0;
		float y = 0;
		for (int i = 0; i < event.getPointerCount(); i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		point.set(x / 2, y / 2);
	}

	// 双击进行缩放
	private void doubleTapToScale(MotionEvent e) {
		float currentScale = getScaleX(mCurrentMatrix);
		float scale = getLatestScale();
		Log.d(TAG, "tapScale");

		mid.set(e.getX(), e.getY());

		List<CounterItem> timeItems = new ArrayList<CounterItem>();
		CounterItem zoomItem = new CounterItem(currentScale, scale);
		timeItems.add(zoomItem);

		Matrix matrix = new Matrix();
		matrix.set(mCurrentMatrix);

		if (zoomItem != null) {
			float scaleRate = zoomItem.getEnd() / zoomItem.getStart();
			matrix.postScale(scaleRate, scaleRate, mid.x, mid.y);
		}

		List<CounterItem> transItems = getTranslateBackItems(matrix);
		// if (!(mode == ZOOM || mode == DRAG)) { // 只有当操作是zoom和drag时才需要做
		timeItems.addAll(transItems);
		// }
		CounterItem[] items = new CounterItem[timeItems.size()];
		timeItems.toArray(items);

		mRevertCounter.setValue(items);
		mRevertCounter.start();
	}

	/**
	 * 获取最靠近当前位置的缩放比例，该方法用于用户双击进行缩放时
	 * 
	 * @return
	 */
	private float getLatestScale() {
		float currentScale = getScaleX(mCurrentMatrix);
		float minScale = Math.abs(mMinScale - currentScale);
		float midScale = Math.abs(mMidScale - currentScale);
		float maxScale = Math.abs(mMaxScale - currentScale);

		if (Util.isFloatEquals(currentScale, mMinScale)) {
			return mMidScale;
		}
		if (Util.isFloatEquals(currentScale, mMidScale)) {
			return mMaxScale;
		}
		if (Util.isFloatEquals(currentScale, mMaxScale)) {
			return mMinScale;
		}

		float scale = minScale < midScale ? minScale : midScale;
		scale = scale < maxScale ? scale : maxScale;

		float finalScaleValue;
		if (scale == minScale) {
			finalScaleValue = mMinScale;
		} else if (scale == midScale) {
			finalScaleValue = mMidScale;
		} else {
			finalScaleValue = mMaxScale;
		}

		return finalScaleValue;
	}

	public boolean isIntercept(MotionEvent e) {
		float currentScale = getScaleX(mCurrentMatrix);
		return !Util.isFloatEquals(currentScale, mMinScale);
	}
}
