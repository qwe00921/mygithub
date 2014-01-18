package com.niuan.screencapture.client.android;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class FrameBufferReader {

	public static Bitmap getScreenshot(Context context) {
		InputStream input = null;
		try {
			input = getInputStream();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取屏幕大小：
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = WM.getDefaultDisplay();
		display.getMetrics(metrics);
		int height = metrics.heightPixels; // 屏幕高
		int width = metrics.widthPixels; // 屏幕的宽
		System.out.println("屏幕高" + height);
		System.out.println("屏幕宽" + width);
		// 获取显示方式
		int pixelformat = display.getPixelFormat();
		PixelFormat localPixelFormat1 = new PixelFormat();
		PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
		int deepth = localPixelFormat1.bytesPerPixel;// 位深

		// byte[] piex =new byte[height * with * deepth];
		DataInputStream dStream = new DataInputStream(input);
		// dStream.readFully(piex);
		DataInput frameBuffer = new DataInputStream(dStream);
		
		
		int size = height * width * deepth;
		
		int[] data = new int[size];

		byte[] byteArray = new byte[size * 4];
		
		try {
			frameBuffer.readFully(byteArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Bitmap screenImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
//		for (int y = 0; y < height; y++) {
//	        int[] oneLine = new int[width];
//			// 从frameBuffer中计算出rgb值
//			convertToRgba32(frameBuffer, oneLine);
//			// 把rgb值设置到image对象中
//			screenImage.setPixels(oneLine, 0, width, 0, y, width, 1);
////			screenImage.setRGB(0, y, xResolution, 1, oneLine, 0, xResolution);
//		}
//		
		
		
		
		
		
		// convertToRgba32(frameBuffer, data);
//		try {
//			frameBuffer.readFully(byteArray);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		convertToRgba32(byteArray, data, pixelformat);
		Bitmap screenImage = Bitmap.createBitmap(data, width, height,
				Bitmap.Config.ARGB_8888);
		
//		Bitmap screenImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		
		
//		File imageFile = new File(Environment.getExternalStorageDirectory()
//				+ "/截屏.png");
//		imageFile.createNewFile();
//		FileOutputStream out = new FileOutputStream(imageFile);
//		bm.compress(Bitmap.CompressFormat.PNG, 100, out);
//		out.flush();
//		out.close();
		System.out.println(screenImage);
		return screenImage;
	}
	
	public static void convertToRgba32(DataInput frameBuffer, int[] into) {
		try {
			for (int x = 0; x < into.length; x++) {
				try {
					into[x] = frameBuffer.readInt();

//					int rgb = frameBuffer.readShort() & 0xffff;
//					int red = rgb >> 11;
//					red = (red << 3) | (red >> 2);
//					int green = (rgb >> 5) & 63;
//					green = (green << 2) | (green >> 4);
//					int blue = rgb & 31;
//					blue = (blue << 3) | (blue >> 2);
//					into[x] = 0xff000000 | (red << 16) | (green << 8) | blue;
				} catch (EOFException e) {
					System.out.println("EOFException=" + e);
				}
			}
		} catch (IOException exception) {
			System.out.println("convertToRgba32Exception=" + exception);
		}
	}
	
	
	public static void convertToRgba32(byte[] btmap, int[] into, int pixelformat) {
		System.out.println("   pixelformat:" + pixelformat);
		switch (pixelformat) {
		case PixelFormat.RGBA_8888:
			for (int x = 0; x < into.length; x++) {
				into[x] = btmap[4 * x] + btmap[4 * x + 1] * 256
						+ btmap[4 * x + 2] * 65536 + btmap[4 * x + 3]
						* 16777216;
			}
			break;

		case PixelFormat.RGB_565:
			for (int x = 0; x < into.length; x++) {

				int rgb = btmap[2 * x] + btmap[2 * x + 1] * 256;
				int red = rgb >> 11;
				red = (red << 3) | (red >> 2);
				int green = (rgb >> 5) & 63;
				green = (green << 2) | (green >> 4);
				int blue = rgb & 31;
				blue = (blue << 3) | (blue >> 2);
				into[x] = 0xff000000 | (red << 16) | (green << 8) | blue;

			}
			break;

		}
	}

//	public static void convertToRgba32(byte[] byteArray, int[] into) {
//		for (int x = 0; x < into.length; x++) {
//			
//			into[x] = byteArray[x];
//			
//            int rgb = byteArray[x] & 0xffff;
//            int red = rgb >> 11;
//            red = (red << 3) | (red >> 2);
//            int green = (rgb >> 5) & 63;
//            green = (green << 2) | (green >> 4);
//            int blue = rgb & 31;
//            blue = (blue << 3) | (blue >> 2);
//            into[x] = 0xff000000 | (red << 16) | (green << 8) | blue;
//		}
//	}
//
	public static InputStream getInputStream() throws FileNotFoundException {
		FileInputStream buf = new FileInputStream(new File("/dev/graphics/fb0"));
		return buf;
	}// get the InputStream from framebuffer

}
