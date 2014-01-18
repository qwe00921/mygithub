package com.niuan.screencapture.client;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.niuan.remoteconnector.data.DataConverter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageDataConverter extends DataConverter<Bitmap> {
	
	@Override
	public byte[] readByteFromObject(Object object) {
		
		Bitmap bmp = null;
		if(object instanceof Bitmap) {
			bmp = (Bitmap) object;
		}
		
		if(bmp == null) {
			return null;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}
	
	@Override
	public Bitmap readObjectFromByte(byte[] byteData) {
		if(byteData == null) {
			return null;
		}
		Bitmap bmp = BitmapFactory.decodeByteArray(byteData, 0, byteData.length);
		return bmp;
	}

	@Override
	protected void initAcceptTypeList(List<String> acceptTypeList) {
		acceptTypeList.add(Bitmap.class.getName());
		acceptTypeList.add("java.awt.image.BufferedImage");
	}

}
