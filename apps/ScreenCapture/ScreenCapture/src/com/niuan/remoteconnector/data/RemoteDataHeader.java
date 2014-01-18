package com.niuan.remoteconnector.data;

import java.util.HashMap;
import java.util.Map;

public class RemoteDataHeader extends RemoteDataObject {
	private int mLength;
	private String mClassTypeName;

	private static final String KEY_DATA_LENGTH = "length";
	private static final String KEY_CLASS_TYPE = "class_type";

	public RemoteDataHeader() {
	
	}

	public RemoteDataHeader(String dataHeaderString) {
		super(dataHeaderString);
	}

	public int getLength() {
		return mLength;
	}

	public void setLength(int length) {
		mLength = length;
	}

	public String getClassTypeName() {
		return mClassTypeName;
	}

	public void setClassTypeName(String classTypeName) {
		mClassTypeName = classTypeName;
	}

	@Override
	protected Map<String, Object> getKeyValueMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(KEY_DATA_LENGTH, getLength());
		if(getClassTypeName() != null) {

			map.put(KEY_CLASS_TYPE, getClassTypeName());
		}
		return map;
	}

	@Override
	protected void readDataFromMap(Map<String, Object> map) {
		if(map != null) {
			Object classType = map.get(KEY_CLASS_TYPE);
			Object length = map.get(KEY_DATA_LENGTH);
			
			try {
				if(classType != null) {
					mClassTypeName = classType.toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(length != null) {
					mLength = Integer.parseInt(length.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
