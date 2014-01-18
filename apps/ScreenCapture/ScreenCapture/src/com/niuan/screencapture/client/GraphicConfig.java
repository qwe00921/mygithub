package com.niuan.screencapture.client;

import java.util.HashMap;
import java.util.Map;

import com.niuan.remoteconnector.data.RemoteDataObject;

public class GraphicConfig extends RemoteDataObject {

	private static final String KEY_START_POINT = "start_point";
	private static final String KEY_BMP_SIZE = "size";
	private static final String KEY_BMP_QUALITY = "quality";
	private static final String KEY_CONFIG_TYPE = "config_type";
	
	public static final int TYPE_SERVER_INIT = 1001;
	public static final int TYPE_CLIENT_REQUEST = 1002;

	private Point mStartPoint;
	private Size mSize;
	private float mQuality;
	private int mConfigType;

	public GraphicConfig() {

	}

	public int getConfigType() {
		return mConfigType;
	}

	public void setConfigType(int configType) {
		mConfigType = configType;
	}

	public Point getStartPoint() {
		return mStartPoint;
	}

	public void setStartPoint(Point startPoint) {
		mStartPoint = startPoint;
	}

	public Size getSize() {
		return mSize;
	}

	public void setSize(Size size) {
		mSize = size;
	}

	public float getQuality() {
		return mQuality;
	}

	public void setQuality(float quality) {
		mQuality = quality;
	}

	public GraphicConfig(String str) {
		super(str);
	}

	@Override
	protected Map<String, Object> getKeyValueMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_START_POINT, getStartPoint());
		map.put(KEY_BMP_SIZE, getSize());
		map.put(KEY_BMP_QUALITY, getQuality());
		map.put(KEY_CONFIG_TYPE, getConfigType());
		return map;
	}

	@Override
	protected void readDataFromMap(Map<String, Object> map) {
		if (map != null) {
			Object point = map.get(KEY_START_POINT);
			Object size = map.get(KEY_BMP_SIZE);
			Object quality = map.get(KEY_BMP_QUALITY);
			Object type = map.get(KEY_CONFIG_TYPE);

			try {
				if (point != null) {
					setStartPoint(new Point(point.toString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (size != null) {
					setSize(new Size(size.toString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (quality != null) {
					setQuality(Float.parseFloat(quality.toString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if (type != null) {
					setConfigType(Integer.parseInt(type.toString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}

		boolean equals = false;
		if(obj instanceof GraphicConfig) {
			GraphicConfig config = (GraphicConfig) obj;
			Point point = config.getStartPoint();
			Size size = config.getSize();
			float quality = config.getQuality();
			int type = config.getConfigType();
			
			equals = isPointEquals(point) && isSizeEquals(size) && isQualityEquals(quality) && isTypeEquals(type);
		}
		
		return equals;
	}
	
	private boolean isPointEquals(Point point) {
		Point thisPoint = getStartPoint();
		if(thisPoint == null) {
			return point == null;
		} else {
			return thisPoint.equals(point);
		}
		
	}
	
	private boolean isSizeEquals(Size size) {
		Size thisSize = getSize();
		if(thisSize == null) {
			return size == null;
		} else {
			return thisSize.equals(size);
		}
	}
	
	private boolean isQualityEquals(float quality) {
		return getQuality() == quality;
	}
	
	private boolean isTypeEquals(int type) {
		return getConfigType() == type;
	}
}
