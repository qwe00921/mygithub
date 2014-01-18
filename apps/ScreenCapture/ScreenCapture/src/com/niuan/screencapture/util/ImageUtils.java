package com.niuan.screencapture.util;

import com.niuan.screencapture.client.Point;

public class ImageUtils {
	public static class Size {
		float width;
		float height;
		
		public Size(float width, float height) {
			this.width = width;
			this.height = height;
		}
	}
	
	public static Point getGraphicPoint(Point localPoint, Size orignalBmpSize, Size scaledBmpSize, Point scaledBmpStartPoint) {
		
		Point scaledBmpEndPoint = getScaledBmpEndPoint(scaledBmpStartPoint, scaledBmpSize);

		Point pointInScaledBmp = getPointInScaledBmp(localPoint, scaledBmpStartPoint, scaledBmpEndPoint);
		
		Point pontInOriginalBmp = changeScaledBmpPointToOriginalPoint(pointInScaledBmp, orignalBmpSize, scaledBmpSize);
		
		return pontInOriginalBmp;
	}
	
	private static Point changeScaledBmpPointToOriginalPoint(Point pointInScaledBmp, Size originalSize, Size scaledSize) {
		Point pointInRemote = null;
		if(originalSize == null || scaledSize == null || pointInScaledBmp == null) {
			return pointInScaledBmp;
		} else {
			pointInRemote = new Point();
			float scaleRateX = originalSize.width / scaledSize.width;
			float scaleRateY = originalSize.height / scaledSize.height;
			
			float remoteX = pointInScaledBmp.getX() * scaleRateX;
			float remoteY = pointInScaledBmp.getY() * scaleRateY;
			
			pointInRemote.setX(remoteX);
			pointInRemote.setY(remoteY);
		}
		
		return pointInRemote;
	}
	
	private static Point getPointInScaledBmp(Point localPoint, Point scaledBmpStartPoint, Point scaledBmpEndPoint) {
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
		

		if(localPoint.relationWith(scaledBmpStartPoint) == Point.RELATION_RIGHT_BELOW
				&& localPoint.relationWith(scaledBmpEndPoint) == Point.RELATION_LEFT_ABOVE) {
			// Legal point
			
			pointInScaledBmp = new Point();
			pointInScaledBmp.setX(localPoint.getX() - scaledBmpStartPoint.getX());
			pointInScaledBmp.setY(localPoint.getY() - scaledBmpStartPoint.getY());
		}
		
		return pointInScaledBmp;
	}

	private static Point getScaledBmpEndPoint(Point startPoint, Size size) {
		Point endPoint = new Point();
		endPoint.setX(startPoint.getX() + size.width);
		endPoint.setY(startPoint.getY() + size.height);
		
		return endPoint;
	}
	
	public static Point calculate(Point midPoint, Point boundaryPoint, float scaleRate) {
		if(midPoint == null || boundaryPoint == null) {
			return null;
		}
		
		float boundaryX = boundaryPoint.getX();
		float boundaryY = boundaryPoint.getY();
		
		float midX = midPoint.getX();
		float midY = midPoint.getY();
		
		float disX = boundaryX - midX;
		float disY = boundaryY - midY;
		
//		disX = disX < 0 ? disX : -disX;
//		disY = disY < 0 ? disY : -disY;
		
		float scaleDisX = disX * scaleRate;
		float scaleDisY = disY * scaleRate;
		
		float scaleX = midX + scaleDisX;
		float scaleY = midY + scaleDisY;
		
		Point pointAfterScale = new Point(scaleX, scaleY);
		
		return pointAfterScale;
	}
	
	public static void main(String args[]) {
		Size localScreenSize = new Size(1980, 1080);

		float scaleRateRemote = 3 / 2f;
		
		Size remoteScreenSize = new Size(1600, 900);
		
		Size remoteBmpSize = remoteScreenSize;
		float scaleRate = 2 / 3f;
		Size scaledBmpSize = new Size(remoteBmpSize.width * scaleRate, remoteBmpSize.height * scaleRate);
		
		Point localPoint = new Point(200, 300);
		
		Point scaledBmpStartPoint = new Point(10, 10);
		
		Point pointInOriginalBmp = getGraphicPoint(localPoint, remoteBmpSize, scaledBmpSize, scaledBmpStartPoint);
		
		System.out.println(pointInOriginalBmp);
		
		
		Point midPoint = new Point(100, 100);
		Point boundaryPoint = new Point(0, 0);
		scaleRate = 1.5f;
		Point pointAfterScale = calculate(midPoint, boundaryPoint, scaleRate);
		System.out.println(pointAfterScale);
	}
}
