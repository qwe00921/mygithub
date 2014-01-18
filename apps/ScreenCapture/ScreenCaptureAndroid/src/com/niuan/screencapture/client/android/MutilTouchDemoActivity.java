package com.niuan.screencapture.client.android;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class MutilTouchDemoActivity extends Activity implements
		OnTouchListener, OnClickListener {
	private static final String TAG = "Touch";
	// These matrices will be used to move and zoom image
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 鏀惧ぇ鎸夐挳
		zoomIn = (Button) findViewById(R.id.zoom_in);
		// 缂╁皬鎸夐挳
		zoomOut = (Button) findViewById(R.id.zoom_out);
		zoomIn.setOnClickListener(this);
		zoomOut.setOnClickListener(this);
		view = (ImageView) findViewById(R.id.imageView);
		view.setOnTouchListener(this);
		// 鍙栧緱drawable涓浘鐗囷紝鏀惧ぇ锛岀缉灏忥紝澶氱偣瑙︽懜鐨勪綔鐢ㄥ璞�		
		bmp = BitmapFactory.decodeResource(
				MutilTouchDemoActivity.this.getResources(), R.drawable.screen_shot);
	}

	public boolean onTouch(View v, MotionEvent event) {
		// Handle touch events here...
		ImageView view = (ImageView) v;
		// Handle touch events here...
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
		// 璁剧疆澶氱偣瑙︽懜妯″紡
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
		// 鑻ヤ负DRAG妯″紡锛屽垯鐐瑰嚮绉诲姩鍥剧墖
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				// 璁剧疆浣嶇Щ
				matrix.postTranslate(event.getX() - start.x, event.getX()
						- start.x);
			}
			// 鑻ヤ负ZOOM妯″紡锛屽垯澶氱偣瑙︽懜缂╂斁
			else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					// 璁剧疆缂╂斁姣斾緥鍜屽浘鐗囦腑鐐逛綅缃�					matrix.postScale(scale, scale, mid.x, mid.y);
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

	// 鏀惧ぇ锛岀缉灏忔寜閽偣鍑讳簨浠�	@Override
	public void onClick(View v) {
		if (v == zoomIn) {
			enlarge();
		} else if (v == zoomOut) {
			small();
		}

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