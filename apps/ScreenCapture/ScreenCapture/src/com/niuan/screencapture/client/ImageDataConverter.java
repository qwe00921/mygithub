package com.niuan.screencapture.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.niuan.remoteconnector.data.DataConverter;
import com.niuan.screencapture.util.CaptureUtils;

public class ImageDataConverter extends DataConverter<BufferedImage> {

	@Override
	protected void initAcceptTypeList(List<String> acceptTypeList) {
		acceptTypeList.add(BufferedImage.class.getName());
		acceptTypeList.add("java.awt.image.BufferedImage");
	}
	
	@Override
	public byte[] readByteFromObject(Object object) {

		BufferedImage bmp = null;
		if(object instanceof BufferedImage) {
			bmp = (BufferedImage) object;
		}
		
		if(bmp == null) {
			return null;
		}
		byte[] imageByteArray = CaptureUtils.bufferedImageTobytes(bmp, 0.3f);
		return imageByteArray;//super.readByteFromObject(object);
	}
	
	@Override
	public BufferedImage readObjectFromByte(byte[] byteData) {
		if(byteData == null) {
			return null;
		}
		ByteArrayInputStream is = new ByteArrayInputStream(byteData);
		BufferedImage bmp = null;
		try {
			bmp = ImageIO.read(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
		return bmp;
	}

}
