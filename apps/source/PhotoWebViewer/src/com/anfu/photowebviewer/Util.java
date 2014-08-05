package com.anfu.photowebviewer;

import java.io.File;

import android.os.Environment;

public class Util {

	private static String ROOT;
	public static String getRootDirectory() {
		if(ROOT != null) {
			return ROOT;
		}
		
		return "/storage/sdcard1/";
		
//		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
//		String sdcard0 = "/storage/sdcard0/";
//		File f = Environment.getExternalStorageDirectory();
//		Environment.getExternalStorageState();
//		
//		if(exist(sdcard0)) {
//			ROOT = sdcard0;
//			
//			return sdcard0;
//		}
//		String sdcard1 = "/storage/sdcard1";
//		if(exist(sdcard1)) {
//			ROOT = sdcard1;
//			return sdcard1;
//		}
		
	}
	
	public static boolean exist(String fileName) {
		File file = new File(fileName);
		
		return file.exists();
	}
}
