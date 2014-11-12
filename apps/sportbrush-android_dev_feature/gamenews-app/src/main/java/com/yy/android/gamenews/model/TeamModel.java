package com.yy.android.gamenews.model;

import java.util.ArrayList;

import com.duowan.gamenews.GetTeamListReq;
import com.duowan.gamenews.GetTeamListRsp;
import com.duowan.gamenews.Team;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class TeamModel extends CommonModel {

	public static void getTeamRsp(
			final ResponseListener<GetTeamListRsp> listener, String attachInfo) {
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			listener.onError(null);
			return;
		}

		UniPacket uniPacket = createUniPacket("GetTeamList");

		GetTeamListReq req = new GetTeamListReq();
		req.setAttachInfo(attachInfo);
		uniPacket.put("request", req);

		// String cacheKey = String.format("%s-%s", uniPacket.getServantName(),
		// uniPacket.getFuncName());
		new Request(listener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetTeamListRsp rsp = new GetTeamListRsp();
				rsp = response.getByClass("result", rsp);
//				if(rsp == null){
//					rsp = getTema();
//				}
				listener.onResponse(rsp);
			}

			public void onError(Exception e) {
				listener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}
	
	@SuppressWarnings("unused")
	private static GetTeamListRsp getTema(){
		ArrayList<Team> teamList = new ArrayList<Team>();
		GetTeamListRsp rsp = new GetTeamListRsp();
		String[] teams = {"美因茨", "西班牙人", "热火", "广东", "斯图加特", "塞维利亚", "马刺", "北京", "拜仁慕尼黑", "皇家马德里", "湖人", "上海", "多特蒙德", "巴塞罗那", "公牛", "江苏", "门兴", "马德里竞技", "骑士","八一",};
		for (int i = 0; i < 20; i++) {
			Team team = new Team();
			if (i % 4 == 0) {
				team.setSportTypeId("1");
				team.setSportTypeName("足球");
				team.setLeagueId("1");
				team.setLeagueName("德甲");
				team.setIsGuest(false);
				team.setId(String.valueOf(i));
				team.setName(teams[i]);
				team.setIcon("http://a.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=4618deeb7cd98d1062d904634056d36b/34fae6cd7b899e516776720342a7d933c8950d28.jpg");
				team.setFlag(0);
			} else if(i % 4 == 1) {
				team.setSportTypeId("1");
				team.setSportTypeName("足球");
				team.setLeagueId("4");
				team.setLeagueName("西甲");
				team.setIsGuest(false);
				team.setId(String.valueOf(i));
				team.setName(teams[i]);
				team.setIcon("http://a.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=4618deeb7cd98d1062d904634056d36b/34fae6cd7b899e516776720342a7d933c8950d28.jpg");
				team.setFlag(0);
			} else if(i % 4 == 2) {
				team.setSportTypeId("2");
				team.setSportTypeName("篮球");
				team.setLeagueId("2");
				team.setLeagueName("NBA");
				team.setIsGuest(false);
				team.setId(String.valueOf(i));
				team.setName(teams[i]);
				team.setIcon("http://a.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=4618deeb7cd98d1062d904634056d36b/34fae6cd7b899e516776720342a7d933c8950d28.jpg");
				team.setFlag(0);
			}else{
				team.setSportTypeId("2");
				team.setSportTypeName("篮球");
				team.setLeagueId("3");
				team.setLeagueName("CBA");
				team.setIsGuest(false);
				team.setId(String.valueOf(i));
				team.setName(teams[i]);
				team.setIcon("http://a.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=4618deeb7cd98d1062d904634056d36b/34fae6cd7b899e516776720342a7d933c8950d28.jpg");
				team.setFlag(0);
			}
			if(i % 5 == 0){
				team.setFlag(1);
			}
			teamList.add(team);
		}
		rsp.setAttachInfo("");
		rsp.setTeamList(teamList);
		
		return rsp;
	}
}
