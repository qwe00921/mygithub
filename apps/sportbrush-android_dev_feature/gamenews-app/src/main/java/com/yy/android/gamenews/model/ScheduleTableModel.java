package com.yy.android.gamenews.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.os.Handler;
import android.os.Looper;

import com.duowan.gamenews.RaceFlag;
import com.duowan.gamenews.RaceInfo;
import com.duowan.gamenews.SportFlag;
import com.duowan.gamenews.SportRaceListReq;
import com.duowan.gamenews.SportRaceListRsp;
import com.duowan.gamenews.Team;
import com.duowan.gamenews.sportInfo;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.util.TimeUtil;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class ScheduleTableModel extends CommonModel {
	private static final Handler sHandler;
	private static final boolean TEST_DATA = false;
	static {
		sHandler = new Handler(Looper.getMainLooper());
	}

	private static Map<String, ArrayList<RaceInfo>> getDummyRaceMap() {
		Map<String, ArrayList<RaceInfo>> map = new HashMap<String, ArrayList<RaceInfo>>();

		// 10天的假数据

		int temp = 0;
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			String s = TimeUtil.parseTimeToYMD(calendar.getTime());
			map.put(s, getDummyRaceList(calendar));

			if(i == 0) {
				
				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 2);
			} else {

				calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
			}
			if (i % 3 == 0) {
				map.get(s).get(temp++).setRaceFlag(RaceFlag._RACE_JUE_FLAG);
			}
		}

		return map;
	}

	private static ArrayList<RaceInfo> getDummyRaceList(Calendar calendar) {
		ArrayList<RaceInfo> list = new ArrayList<RaceInfo>();
		for (int i = 0; i < 20; i++) {
			RaceInfo info = new RaceInfo();
			info.setId(String.valueOf(new Random().nextInt(10000)));

			info.setLeagueName("NBA");
			ArrayList<String> srcList = new ArrayList<String>();
			srcList.add("CCTV5");
			srcList.add("新浪直播");
			info.setLiveSourceList(srcList);
			info.setLiveTime((int) (calendar.getTimeInMillis() / 1000));

			ArrayList<Team> teamList = new ArrayList<Team>();
			Team team = new Team();
			if (i % 3 == 0) {
				info.setLeagueName("环法自行车赛");
				team.setIsGuest(false);
				team.setName("环法自行车赛");
				team.setIcon("http://a.hiphotos.baidu.com/baike/c0%3Dbaike80%2C5%2C5%2C80%2C26/sign=4618deeb7cd98d1062d904634056d36b/34fae6cd7b899e516776720342a7d933c8950d28.jpg");
				teamList.add(team);
			} else {
				team.setIsGuest(true);
				team.setName("湖人");
				team.setIcon("http://t12.baidu.com/it/u=3317513219,2570024703&fm=58");
				teamList.add(team);

				team = new Team();
				team.setName("马刺");
				team.setIsGuest(false);
				team.setIcon("http://t11.baidu.com/it/u=4093726666,3927586898&fm=58");
				teamList.add(team);
			}

			info.setTeamList(teamList);

			info.setSportType(String.valueOf(i % 4 + 1));

			list.add(info);
			calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 10);

		}
		return list;
	}

	public static void getLiveRsp(
			final ResponseListener<SportRaceListRsp> listener, String attachInfo) {
		if (TEST_DATA) {
			sHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					SportRaceListRsp rsp = new SportRaceListRsp();

					ArrayList<sportInfo> channelList = new ArrayList<sportInfo>();

					sportInfo info = new sportInfo();
					info.name = "足球";
					info.setId("1");
					info.setSportFlag(SportFlag._SPORT_INDEX_FLAG);
					channelList.add(info);
					info = new sportInfo();
					info.name = "篮球";
					info.setId("2");
					info.setSportFlag(SportFlag._SPORT_INDEX_FLAG);
					channelList.add(info);
					info = new sportInfo();
					info.name = "网球";
					info.setId("3");
					info.setSportFlag(SportFlag._SPORT_INDEX_FLAG);
					channelList.add(info);
					info = new sportInfo();
					info.name = "排球";
					info.setId("4");
					info.setSportFlag(SportFlag._SPORT_INDEX_FLAG);
					channelList.add(info);
					Map<String, ArrayList<RaceInfo>> map = getDummyRaceMap();
					rsp.setAllRaceList(map);
					rsp.setSportList(channelList);

					listener.onResponse(rsp);
				}
			}, 2000);

			return;
		}

		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			listener.onError(null);
			return;
		}

		UniPacket uniPacket = createUniPacket("SportRaceList");

		SportRaceListReq req = new SportRaceListReq();
		req.setAttachInfo(attachInfo);
		uniPacket.put("request", req);

		// String cacheKey = String.format("%s-%s", uniPacket.getServantName(),
		// uniPacket.getFuncName());
		new Request(listener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				SportRaceListRsp rsp = new SportRaceListRsp();
				rsp = response.getByClass("result", rsp);
				listener.onResponse(rsp);
			}

			public void onError(Exception e) {
				listener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}
}
