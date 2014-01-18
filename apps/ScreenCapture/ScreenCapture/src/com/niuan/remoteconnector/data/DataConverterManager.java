package com.niuan.remoteconnector.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.niuan.screencapture.client.GraphicConfigConverter;
import com.niuan.screencapture.client.ImageDataConverter;
import com.niuan.screencapture.client.MouseEventConverter;

public class DataConverterManager {

	private static List<DataConverter<?>> s_ConvertList = new ArrayList<DataConverter<?>>();
	
	private static Map<String, DataConverter<?>> s_ConvertMap = new HashMap<String, DataConverter<?>>();
	
	public static void init() {
		DataConverterManager.addConverter(new ImageDataConverter());
		DataConverterManager.addConverter(new MouseEventConverter());
		DataConverterManager.addConverter(new GraphicConfigConverter());
	}
	
	public static void addConverter(DataConverter<?> converter) {
		if(!s_ConvertList.contains(converter)) {
			s_ConvertList.add(converter);
		}
	}
	
	public static DataConverter<?> getConverter(String classTypeName) {
		DataConverter<?> value = null;
		
		value = s_ConvertMap.get(classTypeName);
		if(value == null) {
			for(DataConverter<?> converter : s_ConvertList) {
				if(converter != null && converter.accept(classTypeName)) {
					value = converter;
					s_ConvertMap.put(classTypeName, value);
				}
			}
		}
		return value;
	}
}
