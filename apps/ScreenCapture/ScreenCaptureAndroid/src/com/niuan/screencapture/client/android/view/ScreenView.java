package com.niuan.screencapture.client.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.niuan.screencapture.client.Size;
import com.niuan.screencapture.client.android.R;

public class ScreenView extends ImageView {
	

	private OnGestureListener mMouseGestureListener;
	private GestureDetector mGestureDetector;
	
	private static final String TAG = "ScreenView";
	
	private Context mContext;
	
	public void setMouseGestureListener(OnGestureListener mouseGestureListener) {
		mMouseGestureListener = mouseGestureListener;
		mGestureDetector = new GestureDetector(mContext, mMouseGestureListener);
	}
	
	public ScreenView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public ScreenView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
		// TODO Auto-generated constructor stub
	}

	public ScreenView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(context);
	}
	
	private float mMinScaleX;
	private float mMinScaleY;
	
	
	
	private Size mOriginalBmpSize;
	private Size mScreenSize;
	private Size mRemoteScreenSize;
	
	public void initScreenSize(Size originalBmpSize, Size screenSize) {
		mRemoteScreenSize = mOriginalBmpSize = originalBmpSize;
		mScreenSize = screenSize;
		if(originalBmpSize != null && screenSize != null) {
			float scaleX = screenSize.getWidth() / originalBmpSize.getWidth();
			float scaleY = screenSize.getHeight() / originalBmpSize.getHeight();
			mMinScaleX = scaleX;
			mMinScaleY = scaleY;
			Matrix matrix = getImageMatrix();
			matrix.setScale(scaleX, scaleY);
			mCurrentMatrix.set(matrix);
		}
	}
	
	void init(Context context) {
//		setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.screen_shot));
		
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
	
	private float getImageRight(Matrix matrix) {
		if(mOriginalBmpSize != null) {

			float bmpWidth = mOriginalBmpSize.getWidth() * getScaleX(matrix);
			
			return bmpWidth + getImageLeft(matrix);
		}
		
		return getWidth();
	}
	
	private float getImageBottom(Matrix matrix) {
		if(mOriginalBmpSize != null) {

			float bmpHeight = mOriginalBmpSize.getHeight() * getScaleY(matrix);
			
			return bmpHeight + getImageTop(matrix);
		}
		
		return getHeight();
	}
	
	Matrix mCurrentMatrix = new Matrix();
	Matrix mSavedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist;
	private ImageView view;

	// button zoom
	private float scaleWidth = 1;
	private float scaleHeight = 1;
	private Bitmap bmp, zoomedBMP;
	private static final double ZOOM_IN_SCALE = 1.25;
	private static final double ZOOM_OUT_SCALE = 0.8;

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Handle touch events here...
		ImageView view = this;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mSavedMatrix.set(mCurrentMatrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			checkZoomBack(event);
			checkTranslateBack(event);
			if(mode == ZOOM) {
				mOnScreenChangeListener.onScaled();
			} else if(mode == DRAG) {
				mOnScreenChangeListener.onTranslated();
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
				
				float deltaX = toX - fromX;
				float deltaY = toY - fromY;

				mCurrentMatrix.set(mSavedMatrix);
				mCurrentMatrix.postTranslate(deltaX, deltaY);
			
			}
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					mCurrentMatrix.set(mSavedMatrix);
					float scaleRate = newDist / oldDist;
					
					mCurrentMatrix.postScale(scaleRate, scaleRate, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(mCurrentMatrix);
		if (mode == ZOOM) {
			return true;
		}
		
		if(mGestureDetector != null) {
			return mGestureDetector.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}
	
	//如果zoom后图片小于原图，则显示原图
	private void checkZoomBack(MotionEvent event) {
		if(mode == ZOOM) {
			Matrix matrix = mCurrentMatrix;//getImageMatrix();
			float currentScaleX = getScaleX(matrix);
			if(currentScaleX < mMinScaleX) {
				float scaleRate = mMinScaleX / currentScaleX;
				mCurrentMatrix.postScale(scaleRate, scaleRate, mid.x, mid.y);
				setImageMatrix(mCurrentMatrix);
			}
		}
	}
	
	// 如果位移位置超过图片边界，则显示至图片边界
	private void checkTranslateBack(MotionEvent event) {
		if(mode == ZOOM || mode == DRAG) {
			
			Matrix matrix = mCurrentMatrix;
			float imageLeft = getImageLeft(matrix);
			float imageTop = getImageTop(matrix);
			float imageDown = getImageBottom(matrix);
			float imageRight = getImageRight(matrix);
			float viewWidth = getWidth();
			float viewHeight = getHeight();

			float deltaX = 0;
			float deltaY = 0;
			
			if (imageLeft >= 0) {

				deltaX = 0 - imageLeft;
			}

			if (imageRight <= viewWidth) {
				deltaX = viewWidth - imageRight;
			}

			if (imageTop >= 0) { // If reaches left edge

				deltaY = 0 - imageTop;
			}
			if (imageDown <= viewHeight) {
				deltaY = viewHeight - imageDown;
			}
			
			mCurrentMatrix.postTranslate(deltaX, deltaY);
			
			setImageMatrix(mCurrentMatrix);
			
		}
	}
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	
	private void small() {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		scaleWidth = (float) (scaleWidth * ZOOM_OUT_SCALE);
		scaleHeight = (float) (scaleHeight * ZOOM_OUT_SCALE);
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		zoomedBMP = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix,
				true);
		view.setImageBitmap(zoomedBMP);
	}

	private void enlarge() {
		try {
			int bmpWidth = bmp.getWidth();
			int bmpHeight = bmp.getHeight();
			scaleWidth = (float) (scaleWidth * ZOOM_IN_SCALE);
			scaleHeight = (float) (scaleHeight * ZOOM_IN_SCALE);
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			zoomedBMP = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
					matrix, true);
			view.setImageBitmap(zoomedBMP);
		} catch (Exception e) {

		}
	}

	private OnScreenChangeListener mOnScreenChangeListener;
	public void setOnScreenChangeListener(OnScreenChangeListener onScreenChangeListener) {
		mOnScreenChangeListener = onScreenChangeListener;
	}
	public interface OnScreenChangeListener {
		public void onScaled();
		public void onTranslated();
	}
}
