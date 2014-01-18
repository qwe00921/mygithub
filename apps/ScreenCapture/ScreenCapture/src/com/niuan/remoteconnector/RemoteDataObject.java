package com.niuan.remoteconnector;

import java.util.HashMap;
import java.util.Map;

import com.niuan.remoteconnector.util.Log;
import com.niuan.screencapture.client.MouseEvent;

public abstract class RemoteDataObject {
	private static final String TAG = "";

	private static final String MID_SEPARATOR = ":";
	private static final String LEFT_PARENTHESIS = "[";
	private static final String RIGHT_PARENTHESIS = "]";
	public RemoteDataObject() {
		
	}
	
	public RemoteDataObject(String str) {
		toObject(str);
	}
	
	protected abstract Map<String, Object> getKeyValueMap();
	protected abstract void readDataFromMap(Map<String, Object> map);
	
	@Override
	public String toString() {
		Map<String, Object> map = getKeyValueMap();
		StringBuilder strBuilder = new StringBuilder();
		if(map != null) {
			for(String key : map.keySet()) {
				strBuilder.append(LEFT_PARENTHESIS + key + MID_SEPARATOR + map.get(key) + RIGHT_PARENTHESIS);
			}
		}
		
		return strBuilder.toString();
	}
	
	//"[name:xxx][year:xxx][point:[x:1][y:2]]"
	private void toObject(String str) {
		if(str == null) {
			Log.d(TAG, "[toObject] str == null");
			return;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		int keyLevel = 0;
		String keyValueReader = "";
		String key = "";
		String value = "";
		
		for(char c : str.toCharArray()) {
			
			if(c == '[') {
				keyLevel++;
				if(keyLevel <= 1) {
				} else {
					keyValueReader += c;
				}
			} else if(c == ']') {
				
				if(keyLevel <= 1) {
					// Means finish one keyvaluepair 
					value = keyValueReader;
					keyValueReader = "";
					map.put(key, value);
				} else {
					keyValueReader += c;
				}
				keyLevel--;
			} else if(c == ':') {
				if(keyLevel <= 1) {
					key = keyValueReader;
					keyValueReader = "";
				} else {
					keyValueReader += c;
				}
			} else {
				keyValueReader += c;
			}
		}
		readDataFromMap(map);
	}
	
	public static void main(String args[]) {
		MouseEvent mouseEvent = new MouseEvent("[point:[y:200.0][x:100.0]][event:10002]");
//		mouseEvent.setEvent(MouseEvent.RIGHT_CLICK);
//		Point point = new Point(100, 200);
//		mouseEvent.setPoint(point);
		
		System.out.println(mouseEvent);
	}
	
}
