package com.yy.android.gamenews.model;

import com.duowan.gamenews.GetRacePortalReq;
import com.duowan.gamenews.GetRacePortalRsp;
import com.duowan.jce.wup.UniPacket;

public class AssociationModel extends CommonModel {

	public static void getRacePortalList(
			final ResponseListener<GetRacePortalRsp> listener, String attachInfo) {

//		if(true) {
//			GetRacePortalRsp rsp = new GetRacePortalRsp();
//			
//			rsp.setHasMore(true);
//			rsp.setArticleList(ArticleModel.getData());
//			listener.onResponse(rsp);
//			
//			return;
//		}
		
		UniPacket uniPacket = createUniPacket("GetRacePortal");

		GetRacePortalReq req = new GetRacePortalReq();
		req.setAttachInfo(attachInfo);

		uniPacket.put("request", req);

		new Request(listener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetRacePortalRsp rsp = new GetRacePortalRsp();
				rsp = response.getByClass("result", rsp);
				listener.onResponse(rsp);
			}

			public void onError(Exception e) {
				listener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}
}
