package com.duowan.gamenews.bean;

import com.duowan.gamenews.Channel;

public class WelcomeChannel {

	private Channel mChannel;
	private boolean mIsSelected;
	private String mIconPath;

	public void setChannel(Channel channel) {
		mChannel = channel;
	}

	public Channel getChannel() {
		return mChannel;
	}

	public String getIconPath() {
		return mIconPath;
	}
	
	public void setIconPath(String path) {
		mIconPath = path;
	}
	
	public void setIsSelected(boolean selected) {
		mIsSelected = selected;
	}

	public boolean isSelected() {
		return mIsSelected;
	}
}
