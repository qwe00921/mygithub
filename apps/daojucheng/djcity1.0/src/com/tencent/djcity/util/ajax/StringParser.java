package com.tencent.djcity.util.ajax;

import com.tencent.djcity.util.Log;

public class StringParser extends Parser<byte[], String>{

	private static final String LOG_TAG =  StringParser.class.getName();
	@Override
	public String parse(byte[] bytes, String charset) throws Exception{
		
		String str =  new String(bytes, 0, bytes.length, charset );
		
		Log.d(LOG_TAG, str);
		
		return str;
		
	}
}