package com.niuan.screencapture.client;

import java.util.HashMap;
import java.util.Map;

import com.niuan.remoteconnector.data.RemoteDataObject;

public class MouseEvent extends RemoteDataObject {
	
	public static final int LEFT_CLICK = 10001;
	public static final int RIGHT_CLICK = 10002;
	public static final int LEFT_DOUBLE_CLICK = 10003;
	public static final int RIGHT_DOUBLE_CLICK = 10004;
	public static final int MOVE = 10005;
	public static final int SCROLL_UP = 10006;
	public static final int SCROLL_DOWN = 10007;
	
	private static final String KEY_EVENT = "event";
	private static final String KEY_POINT1 = "point1";
	private static final String KEY_POINT2 = "point2";
	private static final String TAG = "MouseEvent";
	
	private int mEvent;
	private Point mPoint1;
	private Point mPoint2;

	public MouseEvent() {
		
	}
	
	public MouseEvent(String eventString) {
		super(eventString);
	}
	
	public void setEvent(int event)  {
		mEvent = event;
	}
	
	public int getEvent() {
		return mEvent;
	}
	
	public void setPoint1(Point point) {
		mPoint1 = point;
	}
	
	public Point getPoint1() {
		return mPoint1;
	}
	
	public void setPoint2(Point point) {
		mPoint2 = point;
	}
	
	public Point getPoint2() {
		return mPoint2;
	}
	
	public static void main(String args[]) {
		MouseEvent event = new MouseEvent();
		Point point = new Point();
		point.setX(100);
		point.setY(200);
		event.setEvent(MouseEvent.LEFT_CLICK);
		event.setPoint1(point);
		String str = event.toString();
		new MouseEvent(str);
	}
	
	@Override
	protected Map<String, Object> getKeyValueMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_EVENT, getEvent());
		
		Point point1 = getPoint1();
		if(point1 != null) {
			map.put(KEY_POINT1, point1);
		}
		Point point2 = getPoint2();
		if(point2 != null) {
			map.put(KEY_POINT2, point2);
		}
		return map;
	}

	@Override
	protected void readDataFromMap(Map<String, Object> map) {
		if(map != null) {
			Object event = map.get(KEY_EVENT);
			Object point1 = map.get(KEY_POINT1);
			Object point2 = map.get(KEY_POINT2);
			
			try {
				if(event != null) {
					mEvent = Integer.parseInt(event.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(point1 != null) {
					mPoint1 = new Point(point1.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
			
			try {
				if(point2 != null) {
					mPoint2 = new Point(point2.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
