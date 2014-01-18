package com.niuan.screencapture.client.android.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;

import com.niuan.remoteconnector.util.Log;

public class ZoomGestureProcessor {
	
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist;
	private ImageView view;
	private Button zoomIn, zoomOut;

	// button zoom
	private float scaleWidth = 1;
	private float scaleHeight = 1;
	private Bitmap bmp, zoomedBMP;
	private int zoom_level = 0;
	private static final double ZOOM_IN_SCALE = 1.25;// 鏀惧ぇ绯绘暟
	private static final double ZOOM_OUT_SCALE = 0.8;// 缂╁皬绯绘暟

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	private static final String TAG = "ZoomGestureProcessor";
	
	
	public boolean onTouchEvent(ImageView view, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 璁剧疆鎷栨媺妯″紡
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				// 璁剧疆浣嶇Щ
				matrix.postTranslate(event.getX() - start.x, event.getX()
						- start.x);
			}
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		// Perform the transformation
		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	// 璁＄畻绉诲姩璺濈
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// 璁＄畻涓偣浣嶇疆
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	// 鎸夐挳鐐瑰嚮缂╁皬鍑芥暟
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

	// 鎸夐挳鐐瑰嚮鏀惧ぇ鍑芥暟
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
}
