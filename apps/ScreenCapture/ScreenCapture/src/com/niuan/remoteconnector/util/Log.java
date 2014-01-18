package com.niuan.remoteconnector.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Log {
	private static final String AP_TAG = "RemoteConnector";
	private static final boolean DEBUG = true;
	static {
		init();
	}
	
	private static final String LOG_PATH = "/storage/sdcard0" + "/log_file.txt";
	private static PrintStream s_LogOutputStream;
	private static void init() {
//		File file = new File(LOG_PATH);
//		if(file.exists()) {
//			file.delete();
//		}
//		try {
//			
//			s_LogOutputStream = new PrintStream(new FileOutputStream(file));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static void d(String TAG, Object message) {
		if(!DEBUG) {
			return;
		}
		
		String LOG = "[" + AP_TAG + "]" + TAG + ":" + message;
		System.out.println(LOG);
//		s_LogOutputStream.println(LOG);
//		s_LogOutputStream.flush();
	}
	
	public static void e(String TAG, Object message) {
		if(!DEBUG) {
			return;
		}
		
		String LOG = "[" + AP_TAG + "]" + TAG + ":" + message;
		System.err.println(LOG);
//		s_LogOutputStream.println(LOG);
//		s_LogOutputStream.flush();
	}
}
