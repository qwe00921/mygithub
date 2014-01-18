package com.niuan.screencapture.util;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.filechooser.FileSystemView;

import com.niuan.remoteconnector.util.Log;


public class CaptureUtils {
	private static int INDEX = 0;
	
	private static final String TAG = "CaptureUtils";
	
	public static void saveToFile(BufferedImage image) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
		String name = sdf.format(new Date());
		File path = FileSystemView.getFileSystemView().getHomeDirectory() ;
		String format = ScreenCaptureHelper.IMAGE_FILE_TYPE;
		File f = new File(path + "/test/" + File.separator + name + "__" + INDEX + "." + format);
		
		Log.d(TAG, "save file name = " + f.getAbsolutePath());
		INDEX++;
		try {
			ImageIO.write(image, format, f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
    public static byte[] bufferedImageTobytes(BufferedImage image, float quality) {    
        // 如果图片空，返回空    
        if (image == null) {    
            return null;    
        }       
        // 得到指定Format图片的writer    
        Iterator<ImageWriter> iter = ImageIO    
                .getImageWritersByFormatName(ScreenCaptureHelper.IMAGE_FILE_TYPE);// 得到迭代器    
        ImageWriter writer = (ImageWriter) iter.next(); // 得到writer    
        // 得到指定writer的输出参数设置(ImageWriteParam )    
        ImageWriteParam iwp = writer.getDefaultWriteParam();    
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩    
        iwp.setCompressionQuality(quality); // 设置压缩质量参数    
    
        iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);    
    
        ColorModel colorModel = ColorModel.getRGBdefault();    
        // 指定压缩时使用的色彩模式    
        iwp.setDestinationType(new javax.imageio.ImageTypeSpecifier(colorModel,    
                colorModel.createCompatibleSampleModel(16, 16)));    
    
        // 开始打包图片，写入byte[]    
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 取得内存输出流    
        IIOImage iIamge = new IIOImage(image, null, null);    
        try {    
            // 此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput    
            // 通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput    
            writer.setOutput(ImageIO    
                    .createImageOutputStream(byteArrayOutputStream));    
            writer.write(null, iIamge, iwp);    
        } catch (IOException e) {    
        	Log.d(TAG, "write errro");    
            e.printStackTrace();    
        } 
        writer.dispose();
        byte ret[] = byteArrayOutputStream.toByteArray();
        try {
        	byteArrayOutputStream.flush();
			byteArrayOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return ret;    
    }    
}
