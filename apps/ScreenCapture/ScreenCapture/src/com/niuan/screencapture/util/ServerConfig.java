package com.niuan.screencapture.util;

public class ServerConfig {
	
	public static String getServerAddr() {
		// TODO: read config from xml file.
		String serverAddr = "127.0.0.1";
		
		return serverAddr;
	}
	
	public static int getServerPort() {
		// TODO: read from xml file.
		return 10000;
	}

}
