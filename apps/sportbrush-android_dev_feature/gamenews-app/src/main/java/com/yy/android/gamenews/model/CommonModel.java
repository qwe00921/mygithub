package com.yy.android.gamenews.model;

import com.duowan.android.base.model.BaseModel;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.Constants;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.Util;

public class CommonModel extends BaseModel {
	protected static UniPacket createUniPacket(String funcName) {
		UniPacket uniPacket = new UniPacket();
		uniPacket.useVersion3();
		uniPacket.setServantName(Constants.APP_SERVANT_NAME);
		uniPacket.setFuncName(funcName);
		uniPacket.put("request", "");// workaround: 此处需传空字符串，否则后台接收不到
		uniPacket.put("version", "android-" + Util.getVersionName());

		UserInitRsp rsp = Preference.getInstance().getInitRsp();
		String accessToken = "";
		if (rsp != null) {
			accessToken = rsp.getAccessToken();
		}
		uniPacket.put("accessToken", accessToken);
		return uniPacket;
	}
}
