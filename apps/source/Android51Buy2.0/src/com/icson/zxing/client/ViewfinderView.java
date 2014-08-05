/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.icson.zxing.client;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.ResultPoint;
import com.icson.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 80L;
  private static final int CURRENT_POINT_OPACITY = 0xA0;
  private static final int MAX_RESULT_POINTS = 20;
  private static final int POINT_SIZE = 6;

  private CameraManager cameraManager;
  private final Paint paint;
  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;
  private final int frameColor;
  private final int laserColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private int mPosition;
  private int mDelta;
  private Bitmap mScanLine = null;
  private Rect mScanRect = null;
  private final int mPixels = 6;
  private int mLineHeight = 0;
  private List<ResultPoint> possibleResultPoints;
  private List<ResultPoint> lastPossibleResultPoints;
  private int mTitlebarHeight = 0;
  private Point mScreenSize = null;

  // This constructor is used when the class is built from an XML resource.
  public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Initialize these once for performance rather than calling them every time in onDraw().
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Resources resources = getResources();
    maskColor = resources.getColor(R.color.viewfinder_mask);
    resultColor = resources.getColor(R.color.result_view);
    frameColor = resources.getColor(R.color.viewfinder_frame);
    laserColor = resources.getColor(R.color.viewfinder_laser);
    resultPointColor = resources.getColor(R.color.possible_result_points);
    mDelta = mPixels << 1;
    scannerAlpha = 0;
    mPosition = 0;
    possibleResultPoints = new ArrayList<ResultPoint>(5);
    lastPossibleResultPoints = null;
  }

  public void setCameraManager(CameraManager cameraManager) {
    this.cameraManager = cameraManager;
  }
  
  public void setTitlebarHeight(int nTitlebarHeight) {
	  mTitlebarHeight = nTitlebarHeight;
  }
  
  public void resetSize() {
	  mScreenSize = null;
  }

  @Override
  public void onDraw(Canvas canvas) {
	cameraManager.setFramingViewSize(getWidth(), getHeight());
    Rect frame = cameraManager.getFramingRect();
    if (frame == null) {
      return;
    }
    
    if( null == mScreenSize ) {
    	mScreenSize = new Point();
    	
    	WindowManager manager = (WindowManager)getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        
        mScreenSize.x = display.getWidth();
        mScreenSize.y = display.getHeight() - mTitlebarHeight;
    }
    
    // Re-loacate the frame
    final int nWidth = frame.right - frame.left;
    final int nHeight = frame.bottom - frame.top;
    frame.left = (mScreenSize.x - nWidth) >> 1;
    frame.top = (mScreenSize.y - nHeight) >> 1;
    frame.right = frame.left + nWidth;
    frame.bottom = frame.top + nHeight;
    
    int width = canvas.getWidth();
    int height = canvas.getHeight();

    // Draw the exterior (i.e. outside the framing rect) darkened
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);

    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(CURRENT_POINT_OPACITY);
      canvas.drawBitmap(resultBitmap, null, frame, paint);
    } else {

      // Draw a two pixel solid black border inside the framing rect
      final int length = (frame.right - frame.left) >> 3;
      paint.setColor(frameColor);
      paint.setStrokeWidth(mPixels);
      
      // Draw the rectangle for preview frame.
      // Left top
      final int nRadius = mPixels >> 1;
      canvas.drawCircle(frame.left, frame.top, nRadius, paint);
      canvas.drawLine(frame.left, frame.top, frame.left, frame.top + length, paint);
      canvas.drawLine(frame.left, frame.top, frame.left + length, frame.top, paint);
      
      // Right top
      canvas.drawCircle(frame.right, frame.top, nRadius, paint);
      canvas.drawLine(frame.right, frame.top, frame.right - length, frame.top, paint);
      canvas.drawLine(frame.right, frame.top, frame.right, frame.top + length, paint);
      
      // Left bottom
      canvas.drawCircle(frame.left, frame.bottom, nRadius, paint);
      canvas.drawLine(frame.left, frame.bottom, frame.left, frame.bottom - length, paint);
      canvas.drawLine(frame.left, frame.bottom, frame.left + length, frame.bottom, paint);
      
      // Right bottom
      canvas.drawCircle(frame.right, frame.bottom, nRadius, paint);
      canvas.drawLine(frame.right, frame.bottom, frame.right, frame.bottom - length, paint);
      canvas.drawLine(frame.right, frame.bottom, frame.right - length, frame.bottom, paint);

      /*
      canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
      canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
      canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
      canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);
      */

      // Draw a red "laser scanner" line through the middle to show decoding is active
      mPosition = 0 == mPosition ? mPosition = frame.height() / 2 + frame.top : mPosition + mDelta;
      if( null != mScanLine ) {
    	  if( (0 == mScanRect.left) || (0 == mScanRect.top) || (0 == mLineHeight) ) {
    		  mScanRect.left = frame.left + mPixels;
    		  mScanRect.right = frame.right - mPixels;
    		  
    		  // Scale the height.
    		  mLineHeight = (mScanLine.getHeight() * (mScanRect.right - mScanRect.left) / mScanLine.getWidth());
    	  }
    	  mScanRect.top = mPosition - mLineHeight / 2;
    	  mScanRect.bottom = mPosition + mLineHeight / 2;
    	  canvas.drawBitmap(mScanLine, null, mScanRect, paint);
      } else {
    	  paint.setColor(laserColor);
          paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
          scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;  
          canvas.drawRect(frame.left + mPixels, mPosition - mPixels / 2, frame.right - mPixels, mPosition + mPixels, paint);
      }
      
      // Update delta.
      if( mPosition >= (frame.bottom - mPixels) || mPosition <= (frame.top + mPixels) )
    	  mDelta = -mDelta;
      
      Rect previewFrame = cameraManager.getFramingRectInPreview();
      float scaleX = frame.width() / (float) previewFrame.width();
      float scaleY = frame.height() / (float) previewFrame.height();

      if(null==possibleResultPoints)
		  possibleResultPoints = new ArrayList<ResultPoint>(5);
      List<ResultPoint> currentPossible = possibleResultPoints;
      List<ResultPoint> currentLast = lastPossibleResultPoints;
      int frameLeft = frame.left;
      int frameTop = frame.top;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
    	lastPossibleResultPoints = currentPossible;
        paint.setAlpha(CURRENT_POINT_OPACITY);
        paint.setColor(resultPointColor);
        synchronized (currentPossible) {
          for (ResultPoint point : currentPossible) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              POINT_SIZE, paint);
          }
        }
      }
      if (currentLast != null) {
        paint.setAlpha(CURRENT_POINT_OPACITY / 2);
        paint.setColor(resultPointColor);
        synchronized (currentLast) {
          float radius = POINT_SIZE / 2.0f;
          for (ResultPoint point : currentLast) {
            canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                              frameTop + (int) (point.getY() * scaleY),
                              radius, paint);
          }
        }
      }

      // Request another update at the animation interval, but only repaint the laser line,
      // not the entire viewfinder mask.
      postInvalidateDelayed(ANIMATION_DELAY,
                            frame.left - POINT_SIZE,
                            frame.top - POINT_SIZE,
                            frame.right + POINT_SIZE,
                            frame.bottom + POINT_SIZE);
    }
  }

  public void drawViewfinder() {
    Bitmap resultBitmap = this.resultBitmap;
    this.resultBitmap = null;
    if (resultBitmap != null) {
      resultBitmap.recycle();
    }
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    List<ResultPoint> points = possibleResultPoints;
    synchronized (points) {
      points.add(point);
      int size = points.size();
      if (size > MAX_RESULT_POINTS) {
        // trim it
        points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
      }
    }
  }

}
