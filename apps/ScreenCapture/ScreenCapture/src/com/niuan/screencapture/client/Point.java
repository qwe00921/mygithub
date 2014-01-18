package com.niuan.screencapture.client;

import java.util.HashMap;
import java.util.Map;

import com.niuan.remoteconnector.data.RemoteDataObject;

public class Point extends RemoteDataObject {
	private float x;
	private float y;
	
	private static final String KEY_X = "x";
	private static final String KEY_Y = "y";
//	private static final String SEPARATOR = ";";
//	private static final String REGULAR_EXPRESSION_NUMBER = "[0-9]+\\.{0,1}[0-9]{0,2}";

	public Point() {
		
	}
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	
	public Point(String pointString) {
		super(pointString);
	}
	
	public static void main(String args[]) {
		float x = 2005;
		float y = 10.3f;
		Point point = new Point(x, y);
		String s = point.toString() + "";
		new Point(s);
	}

	@Override
	protected Map<String, Object> getKeyValueMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_X, getX());
		map.put(KEY_Y, getY());
		return map;
	}

	@Override
	protected void readDataFromMap(Map<String, Object> map) {
		if(map != null) {
			Object x = map.get(KEY_X);
			Object y = map.get(KEY_Y);
			
			try {
				if(x != null) {
					this.x = Float.parseFloat(x.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(y != null) {
					this.y = Float.parseFloat(y.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final int RELATION_LEFT = 1001;
	public static final int RELATION_LEFT_BELOW = 1002;
	public static final int RELATION_LEFT_ABOVE = 1003;
	public static final int RELATION_RIGHT = 1003;
	public static final int RELATION_RIGHT_BELOW = 1004;
	public static final int RELATION_RIGHT_ABOVE = 1005;
	public static final int RELATION_BELOW = 1006;
	public static final int RELATION_ABOVE = 1007;
	public static final int RELATION_EQUAL = 1000;
	public static final int RELATION_NONE = -1;
	
	public int relationWith(Point targetPoint) {
		if(targetPoint == null) {
			return RELATION_NONE;
		}
		float targetX = targetPoint.getX();
		float targetY = targetPoint.getY();
		
		int relation = -1;
		if(x > targetX) {
			if(y > targetY) {
				relation = RELATION_RIGHT_BELOW;
			} else if(y == targetY) {
				relation = RELATION_RIGHT;
			} else if(y < targetY) {
				relation = RELATION_RIGHT_ABOVE;
			}
		} else if(x == targetX) {
			if(y > targetY) {
				relation = RELATION_BELOW;
			} else if(y == targetY) {
				relation = RELATION_EQUAL;
			} else if(y < targetY) {
				relation = RELATION_ABOVE;
			}
		} else if(x < targetX) {
			if(y > targetY) {
				relation = RELATION_LEFT_BELOW;
			} else if(y == targetY) {
				relation = RELATION_LEFT;
			} else if(y < targetY) {
				relation = RELATION_LEFT_ABOVE;
			}
		}
		return relation;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}

		boolean equals = false;
		if(obj instanceof Point) {
			Point point = (Point) obj;
			
			equals = getX() == point.getX() && getY() == point.getY();
		}
		
		return equals;
	}
	
}
