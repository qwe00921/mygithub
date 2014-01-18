package com.niuan.screencapture.client;

import java.util.HashMap;
import java.util.Map;

import com.niuan.remoteconnector.data.RemoteDataObject;

public class Size extends RemoteDataObject {
	private float mWidth;
	private float mHeight;
	
	private static final String KEY_WIDTH = "width";
	private static final String KEY_HEIGHT = "height";

	public Size() {
		
	}
	public Size(float width, float height) {
		this.mWidth = width;
		this.mHeight = height;
	}
	
	public Size(String pointString) {
		super(pointString);
	}
	
	public float getWidth() {
		return mWidth;
	}

	public void setWidth(float width) {
		this.mWidth = width;
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(float height) {
		this.mHeight = height;
	}

	@Override
	protected Map<String, Object> getKeyValueMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_WIDTH, getWidth());
		map.put(KEY_HEIGHT, getHeight());
		return map;
	}

	@Override
	protected void readDataFromMap(Map<String, Object> map) {
		if(map != null) {
			Object width = map.get(KEY_WIDTH);
			Object height = map.get(KEY_HEIGHT);
			
			try {
				if(width != null) {
					this.mWidth = Float.parseFloat(width.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(height != null) {
					this.mHeight = Float.parseFloat(height.toString());
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

		boolean isTypeEquals = false;
		if(obj instanceof Size) {
			Size size = (Size) obj;
			isTypeEquals = this.getWidth() == size.getWidth() && this.getHeight() == size.getHeight();
		}
		
		return isTypeEquals;
	}
}
