package com.yy.android.gamenews.model;

import android.content.Context;

import com.duowan.gamenews.AppInitReq;
import com.duowan.gamenews.AppInitRsp;
import com.duowan.gamenews.DeviceType;
import com.duowan.gamenews.UserInitReq;
import com.duowan.gamenews.UserInitRsp;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class InitModel extends CommonModel {

	public static void sendUserInitReq(Context context,
			final ResponseListener<UserInitRsp> responseListener,
			UserInitReq req, boolean showDialog) {
		UniPacket uniPacket = createUniPacket("UserInit");

		if (req == null) {
			req = new UserInitReq();
		}
		req.setDeviceType(DeviceType._DEVICE_TYPE_ANDROID);
		req.setUuid(Util.getDeviceUUID(context));
		
		String channelName = context.getString(R.string.channelname);
		req.setAppPlat(channelName);

		uniPacket.put("request", req);

//		String cacheKey = String.format("%s-%s-%b", uniPacket.getServantName(),
//				uniPacket.getFuncName(), req == null);
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {

				String strType = "";
				Integer intType = 0;
				String msg = response.getByClass("msg", strType);
				int code = response.getByClass("code", intType);
				int subcode = response.getByClass("subcode", intType);
				if (code == 0 && subcode == 0) {
					UserInitRsp rsp = new UserInitRsp();
					rsp = response.getByClass("result", rsp);
					if (rsp != null) {
						responseListener.onResponse(rsp);
					}

				} else {
					responseListener.onError(new Exception(msg));
				}
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(showDialog).execute();
	}

	public static void checkUpdate(Context context,
			final ResponseListener<Void> responseListener) {

		// UniPacket uniPacket = createUniPacket("doUpgrade");

		// UpgradeReq req = new UpgradeReq();
		//
		// req.setDeviceType(DeviceType._DEVICE_TYPE_ANDROID);
		// req.setSocialAccessToken(socialToken);
		// req.setUserIcon(userIcon);
		// req.setUserName(userName);
		// req.setUuid(Util.getDeviceUUID(context));
		//
		// uniPacket.put("request", req);
		//
		// String cacheKey = String.format("%s-%s-%s",
		// uniPacket.getServantName(),
		// uniPacket.getFuncName(), socialToken);
		// new Request(responseListener.get(), uniPacket, cacheKey) {
		// @Override
		// public void onResponse(UniPacket response) {
		// UserInitRsp rsp = new UserInitRsp();
		// rsp = response.getByClass("result", rsp);
		// if (rsp != null)
		// responseListener.onResponse(rsp);
		// }
		//
		// public void onError(Exception e) {
		// responseListener.onError(e);
		// };
		// }.execute();
	}

	public static void sendAppInitReq(Context context, AppInitReq req,
			final ResponseListener<AppInitRsp> listener) {
		UniPacket uniPacket = createUniPacket("AppInit");

		uniPacket.put("request", req);

//		String cacheKey = String.format("%s-%s-%b", uniPacket.getServantName(),
//				uniPacket.getFuncName(), req == null);
		new Request(listener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				AppInitRsp rsp = new AppInitRsp();
				rsp = response.getByClass("result", rsp);
				listener.onResponse(rsp);
			}

			public void onError(Exception e) {
				listener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}

}
