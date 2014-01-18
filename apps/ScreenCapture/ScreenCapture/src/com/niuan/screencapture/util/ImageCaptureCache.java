package com.niuan.screencapture.util;

import java.awt.image.BufferedImage;

public class ImageCaptureCache extends ObjectCache<BufferedImage> {

	@Override
	protected BufferedImage getObject() {
		// TODO Auto-generated method stub
		return ScreenCaptureHelper.snapshot();
	}
	
}
