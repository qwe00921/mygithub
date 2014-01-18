package com.niuan.screencapture.util;

import com.niuan.screencapture.client.Point;

public class ScreenDisplayHelper {
	

	private float mBmpWidth;
	private float mBmpHeight;
	private float mScreenWidth;
	private float mScreenHeight;
	
	public void setupConfig(int bmpWidth, int bmpHeight, int screenWidth, int screenHeight) {
		mBmpWidth = bmpWidth;
		mBmpHeight = bmpHeight;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
	}
	
	public Point getActualPositionForRemote(Point localPoint, Point bmpPositionInRemote) {
		
		if(localPoint == null) {
			return null;
		}
		Point pointInRemote = null;
		
		float localX = localPoint.getX();
		float localY = localPoint.getY();
		
		float xInCurrentBmp = localX * mBmpWidth / mScreenWidth;
		float yInCurrentBmp = localY * mBmpHeight / mScreenHeight;
		
		float xInRemote = xInCurrentBmp;
		if(bmpPositionInRemote != null) {
			xInRemote += bmpPositionInRemote.getX(); 
		}
		
		float yInRemote = yInCurrentBmp;
		if(bmpPositionInRemote != null) {
			yInRemote += bmpPositionInRemote.getY(); 
		}
		
		pointInRemote = new Point(xInRemote, yInRemote);
		
		return pointInRemote;
	}
}
