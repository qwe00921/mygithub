package com.niuan.screencapture.client;

import java.util.List;

import com.niuan.remoteconnector.data.DataConverter;


public class GraphicConfigConverter extends DataConverter<GraphicConfig> {
	@Override
	public byte[] readByteFromObject(Object object) {
		
		String data = object.toString();
		
		byte[] byteData = data.getBytes();
		return byteData;
	}
	
	@Override
	public GraphicConfig readObjectFromByte(byte[] byteData) {
		if(byteData == null) {
			return null;
		}
		GraphicConfig event = null;
		
		StringBuilder builder = new StringBuilder();
		
		for(byte b : byteData) {
			char c = (char)b;
			builder.append(c);
		}

		String str = builder.toString();
		event = new GraphicConfig(str);
		
		return event;
	}

	@Override
	protected void initAcceptTypeList(List<String> acceptTypeList) {
		acceptTypeList.add(GraphicConfig.class.getName());
	}
}
