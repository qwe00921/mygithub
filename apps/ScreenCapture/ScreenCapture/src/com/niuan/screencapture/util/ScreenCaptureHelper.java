package com.niuan.screencapture.util;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.niuan.remoteconnector.util.Log;
import com.niuan.screencapture.client.GraphicConfig;
import com.niuan.screencapture.client.Point;
import com.niuan.screencapture.client.Size;

public class ScreenCaptureHelper {
	
	public static final String IMAGE_FILE_TYPE = "jpeg";
	private static final String TAG = "ScreenCaptureHelper";
	
	
	public static final GraphicConfig getDefaultConfig() {
		
		GraphicConfig config = new GraphicConfig();
		config.setConfigType(GraphicConfig.TYPE_SERVER_INIT);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Point point = new Point(0, 0);
		Size size = new Size(d.width, d.height);
		
		config.setSize(size);
		config.setStartPoint(point);
		
		return config;
	}
	
	public static BufferedImage snapshot(GraphicConfig config) {
		BufferedImage image = null;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Point point = config.getStartPoint();
		Size size = config.getSize();
		
		image = snapshot((int)point.getX(), (int)point.getY(), (int)size.getWidth(), (int)size.getHeight());
		return image;
	}
	
	public static BufferedImage snapshot() {
		BufferedImage image = null;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		image = snapshot(d.width, d.height);
//		image = snapshot(800,600);
		return image;
	}
	
	public static BufferedImage snapshot(int width, int height) {
		BufferedImage image = snapshot(0, 0, width, height);
		return image;
	}
	
	public static BufferedImage snapshot(int x, int y, int width, int height) {
		BufferedImage image = null;
		try {
			Robot robot = new Robot();
			image = robot.createScreenCapture(new Rectangle(x, y, width, height));
			File file = new File("asset/ic_launcher.png");
			 
			Image cursor = ImageIO.read(file);  
			int mouseX = MouseInfo.getPointerInfo().getLocation().x;  
			int mouseY = MouseInfo.getPointerInfo().getLocation().y;  
			 
			Graphics2D graphics2D = image.createGraphics();  
			graphics2D.drawImage(cursor, mouseX, mouseY, 16, 16, null);

		} catch (AWTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}
	
	public static BufferedImage zipImage(BufferedImage image, int height, int width) {
		BufferedImage zippedImage = null;
		return zippedImage;
	}
	
	public static int write(BufferedImage image, OutputStream out) throws IOException {
		int length = 0;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, IMAGE_FILE_TYPE, os);
//			byte[] imageByteArray = os.toByteArray();
			byte[] imageByteArray = CaptureUtils.bufferedImageTobytes(image, 0.3f);//os.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(imageByteArray);
			length = imageByteArray.length;
			
            writeData(out, in, length);
            
            in.close();
            out.flush();
            

		return length;
	}
	
    private static void writeData(OutputStream out, InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];

        int readBytes = -1;

        String header = length + ";";
        
        out.write(header.getBytes());
        out.write("\r".getBytes());
        out.flush();
        
        while ((readBytes = in.read(buffer)) != -1) {
            out.write(buffer, 0, readBytes);
        }
        out.flush();
    }
    
    public static byte[] readData(InputStream in) throws IOException {
		boolean isEnd = false;
		byte[]data = null;
		BufferedInputStream bis= new BufferedInputStream(in);
		while (!isEnd) {
			int d = -1;
			StringBuilder header = new StringBuilder();

			while ((d = bis.read()) != '\r') {
				if (d == -1) {
					isEnd = true;
					break;
				}
				header.append((char) d);
			}
			if (!isEnd) {
				String[] parms = header.toString().split(";");
				
				int size = Integer.parseInt(parms[0]);
				data = new byte[size];
				int index = 0;
				while (size > 0 && (d = bis.read()) != -1) {
					data[index] = (byte)d;
					size--;
					index++;
				}
				break;
				
			}
		}
		return data;
    }
	
	public static BufferedImage read(InputStream in) {
		BufferedImage image = null;
		try {
			byte[]data  = readData(in);
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			image = ImageIO.read(is);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return image;
	}
	
	public static void main(String args[]) throws IOException {
		
		for(int i = 0; i < 100; i++) {
			BufferedImage image = ScreenCaptureHelper.snapshot(10, 10);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, IMAGE_FILE_TYPE, os);
			byte[] b = os.toByteArray();
			Log.d(TAG, "length = " + b.length);
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			BufferedImage outputImage = null;
			outputImage = ImageIO.read(in);
			System.out.println(outputImage);
		}
	}
}
