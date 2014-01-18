package com.niuan.screencapture.client.android.view;


import com.niuan.remoteconnector.util.Log;
import com.niuan.screencapture.client.Point;
import com.niuan.screencapture.client.Size;

public class ImageUtils {
	
	/**
	 * 
	 * @param localPoint 所点击的设备屏幕所见范围上的点的位置
	 * @param orignalBmpSize 原始图片大小
	 * @param scaleRateX x轴缩放率
	 * @param scaleRateY y轴绽放率
	 * @param scaledBmpStartPoint 缩放后的图片的起始位置（可超过屏幕范围）
	 * @return 在原始图片上的点的位置
	 */
	public static Point getPointInOriginalBmp(Point localPoint, Size orignalBmpSize, float scaleRateX, float scaleRateY, Point scaledBmpStartPoint) {
		Size scaledBmpSize = null;
		if(orignalBmpSize != null) {
			scaledBmpSize = new Size((int)(orignalBmpSize.getWidth() * scaleRateX), 
					(int)(orignalBmpSize.getHeight() * scaleRateY));
		}
		
		Point scaledBmpEndPoint = getScaledBmpEndPoint(scaledBmpStartPoint, scaledBmpSize);

		Point pointInScaledBmp = getPointInScaledBmp(localPoint, scaledBmpStartPoint, scaledBmpEndPoint);
		
		Point pointInOriginalBmp = changeScaledBmpPointToOriginalPoint(pointInScaledBmp, scaleRateX, scaleRateY);
		
		if(pointInOriginalBmp == null) {
			Log.d(TAG, "");
		}
		
		Log.d(TAG, "[getPointInOriginalBmp]localPoint = " + localPoint 
					+ ", orignalBmpSize = " + orignalBmpSize
					+ ", scaleRateX = " + scaleRateX
					+ ", scaleRateY = " + scaleRateY
					+ ", scaledBmpStartPoint = " + scaledBmpStartPoint
					+ ", pontInOriginalBmp = " + pointInOriginalBmp);
		return pointInOriginalBmp;
	}
	
	private static Point changeScaledBmpPointToOriginalPoint(Point pointInScaledBmp, float scaleRateX, float scaleRateY) {
		Point pointInRemote = null;
		if(pointInScaledBmp == null) {
			return pointInScaledBmp;
		} else {
			pointInRemote = new Point();
			
			float remoteX = pointInScaledBmp.getX() / scaleRateX;
			float remoteY = pointInScaledBmp.getY() / scaleRateY;
			
			pointInRemote.setX(remoteX);
			pointInRemote.setY(remoteY);
		}
		
		return pointInRemote;
	}
	
	
	/**
	 * 获取在缩放后的图像上的点
	 * @param localPoint 本地点
	 * @param scaledBmpStartPoint 缩放图像起始点
	 * @param scaledBmpEndPoint 缩放图像结束点
	 * @return 缩放后的图像上的点
	 */
	public static Point getPointInScaledBmp(Point localPoint, Point scaledBmpStartPoint, Point scaledBmpEndPoint) {
		Point pointInScaledBmp = null;
		
		if(scaledBmpStartPoint == null) {
			return null;
		}
		
		if(localPoint == null) {
			return null;
		}
		
		if(scaledBmpEndPoint == null) {
			return null;
		}
		

		if((localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_RIGHT_BELOW
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_RIGHT
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_BELOW
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_EQUAL)
				&& 
				(localPoint.relationWith(scaledBmpEndPoint) == Point.RELATION_LEFT_ABOVE
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_LEFT
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_ABOVE
				|| localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_EQUAL)
			) {
			// Legal point
			
			pointInScaledBmp = new Point();
			pointInScaledBmp.setX(localPoint.getX() - scaledBmpStartPoint.getX());
			pointInScaledBmp.setY(localPoint.getY() - scaledBmpStartPoint.getY());
		}
		
		return pointInScaledBmp;
	}

	/**
	 * 通过图片起始点和图片大小获取图片结束点（通过左上角的点获取右下角的点）
	 * @param startPoint 图片起始位置
	 * @param size 图片大小
	 * @return
	 */
	public static Point getScaledBmpEndPoint(Point startPoint, Size size) {
		Point endPoint = new Point();
		endPoint.setX(startPoint.getX() + size.getWidth());
		endPoint.setY(startPoint.getY() + size.getHeight());
		
		return endPoint;
	}
	
	private static final String TAG = "ImageUtils";
	
	public static float getValueAfterScale(float midValue, float boundaryValue, float scaleRate) {
		float dif = boundaryValue - midValue;
		float scaleDif = dif * scaleRate;
		
		float offsetValue = dif - scaleDif;
		float scaleValue = boundaryValue - offsetValue;
		
		return scaleValue;
	}
	
	public static Point getPointAfterScale(Point midPoint, Point boundaryPoint, float scaleRate) {
		if(midPoint == null || boundaryPoint == null) {
			return null;
		}
		
//		float boundaryX = boundaryPoint.getX();
//		float boundaryY = boundaryPoint.getY();
//		
//		float midX = midPoint.getX();
//		float midY = midPoint.getY();
//		
//		float disX = boundaryX - midX;
//		float disY = boundaryY - midY;
//		
//		float scaleDisX = disX * scaleRate;
//		float scaleDisY = disY * scaleRate;
//		
//		float offsetX = disX - scaleDisX;
//		float offsetY = disY - scaleDisY;
//		
//		float scaleX = boundaryX - offsetX;
//		float scaleY = boundaryY - offsetY;

		float scaleX = getValueAfterScale(midPoint.getX(), boundaryPoint.getX(), scaleRate);
		float scaleY = getValueAfterScale(midPoint.getY(), boundaryPoint.getY(), scaleRate);
		
		Point pointAfterScale = new Point(scaleX, scaleY);
		
		
		Log.d(TAG, "[getpointAfterScale] midPoint = " + midPoint 
				+ ", boundaryPoint = " + boundaryPoint 
				+ ", scaleRate = " + scaleRate
				+ ", pointAfterScale = " + pointAfterScale);
		return pointAfterScale;
	}
	
	public static void main(String args[]) {
		Size localScreenSize = new Size(1980, 1080);

		float scaleRateRemote = 3 / 2f;
		
		Size remoteScreenSize = new Size(1600, 900);
		
		Size remoteBmpSize = remoteScreenSize;
		float scaleRate = 2 / 3f;
		Size scaledBmpSize = new Size((int)(remoteBmpSize.getWidth() * scaleRate), (int)(remoteBmpSize.getHeight() * scaleRate));
		
		Point localPoint = new Point(200, 300);
		
		Point scaledBmpStartPoint = new Point(10, 10);
		
		float scaleRateX = 1.5f;
		float scaleRateY = 1.5f;
		
		Point pointInOriginalBmp = getPointInOriginalBmp(localPoint, remoteBmpSize, scaleRateX, scaleRateY, scaledBmpStartPoint);
		
		System.out.println(pointInOriginalBmp);
		
		
		Point midPoint = new Point(100, 100);
		Point boundaryPoint = new Point(0, 0);
		scaleRate = 1.5f;
		Point pointAfterScale = getPointAfterScale(midPoint, boundaryPoint, scaleRate);
		System.out.println(pointAfterScale);
	}
}
