package com.tencent.djcity.util;

import android.view.Display;

public class DPIUtil {

	private Display defaultDisplay;

	public DPIUtil(Display display) {
		defaultDisplay = display;
	}

	public int getHeight() {
		return defaultDisplay.getHeight();
	}

	public int getWidth() {
		return defaultDisplay.getWidth();
	}

	public int percentHeight(float paramFloat) {
		return (int) (defaultDisplay.getHeight() * paramFloat);
	}

	public int percentWidth(float paramFloat) {
		return (int) (defaultDisplay.getWidth() * paramFloat);
	}


}