package com.yy.android.gamenews.model;

import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;

import com.duowan.gamenews.Channel;
import com.duowan.gamenews.FeedBackReq;
import com.duowan.gamenews.GetColumnChannelListReq;
import com.duowan.gamenews.GetColumnChannelListRsp;
import com.duowan.gamenews.GetColumnListReq;
import com.duowan.gamenews.GetColumnListRsp;
import com.duowan.gamenews.GetMyFavChannelListReq;
import com.duowan.gamenews.GetMyFavChannelListRsp;
import com.duowan.gamenews.GetSearchSuggestionListReq;
import com.duowan.gamenews.GetSearchSuggestionListRsp;
import com.duowan.gamenews.PushInitReq;
import com.duowan.gamenews.SearchChannelReq;
import com.duowan.gamenews.SearchChannelRsp;
import com.duowan.gamenews.UpdateMyFavChannelListReq;
import com.duowan.gamenews.UpdateMyFavChannelListRsp;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class ChannelModel extends CommonModel {

	public static void getColumnList(
			final ResponseListener<GetColumnListRsp> responseListener,
			String attachInfo, final int count) {
		// cache
		// if (!Util.isNetworkConnected()) {
		// ToastUtil.showToast(R.string.http_not_connected);
		// return;
		// }
		UniPacket uniPacket = createUniPacket("GetColumnList");
		GetColumnListReq req = new GetColumnListReq();
		req.setAttachInfo(attachInfo);
		req.setCount(count);
		uniPacket.put("request", req);

		String cacheKey = String.format("%s-%s-%s-%s",
				uniPacket.getServantName(), uniPacket.getFuncName(),
				attachInfo, String.valueOf(count));

		Request request = new Request(responseListener.get(), uniPacket,
				cacheKey) {
			@Override
			public void onResponse(UniPacket response) {
				GetColumnListRsp rsp = new GetColumnListRsp();
				rsp = response.getByClass("result", rsp);
				if (rsp != null)
					responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		};
		// request.setShowProgressDialog(false);
		request.execute();
	}

	public static void getColumnChannelList(
			final ResponseListener<GetColumnChannelListRsp> responseListener,
			int columnId, String attachInfo, final int count) {
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			return;
		}
		UniPacket uniPacket = createUniPacket("GetColumnChannelList");
		GetColumnChannelListReq req = new GetColumnChannelListReq();
		req.setId(columnId);
		req.setAttachInfo(attachInfo);
		req.setCount(count);
		uniPacket.put("request", req);

		// String cacheKey = String.format("%s-%s-%s-%s",
		// uniPacket.getServantName(), uniPacket.getFuncName(), columnId,
		// attachInfo);

		Request request = new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetColumnChannelListRsp rsp = new GetColumnChannelListRsp();
				rsp = response.getByClass("result", rsp);
				if (rsp != null)
					responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		};
		request.setShowProgressDialog(false);
		request.execute();
	}

	public static void searchChannel(
			final ResponseListener<SearchChannelRsp> responseListener,
			final String keyWord, String attachInfo, final int count) {
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			return;
		}
		UniPacket uniPacket = createUniPacket("SearchChannel");
		SearchChannelReq req = new SearchChannelReq();
		req.setKeyword(keyWord);
		req.setAttachInfo(attachInfo);
		req.setCount(count);
		uniPacket.put("request", req);

		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				SearchChannelRsp rsp = new SearchChannelRsp();
				rsp = response.getByClass("result", rsp);
				if (rsp != null)
					responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.execute();
	}

	public static void getSearchSuggestionList(
			final ResponseListener<GetSearchSuggestionListRsp> responseListener,
			String attachInfo, int count) {
		UniPacket uniPacket = createUniPacket("GetSearchSuggestionList");
		GetSearchSuggestionListReq req = new GetSearchSuggestionListReq();
		req.setAttachInfo(attachInfo);
		req.setCount(count);
		uniPacket.put("request", req);

		Request request = new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetSearchSuggestionListRsp rsp = new GetSearchSuggestionListRsp();
				rsp = response.getByClass("result", rsp);
				if (rsp != null)
					responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		};
		request.setShowProgressDialog(false);
		request.execute();
	}

	public static void getMyFavChannelList(
			final ResponseListener<GetMyFavChannelListRsp> listener) {

		UniPacket uniPacket = createUniPacket("GetMyFavChannelList");
		GetMyFavChannelListReq req = new GetMyFavChannelListReq();
		uniPacket.put("request", req);
		
		Request request = new Request(listener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetMyFavChannelListRsp rsp = new GetMyFavChannelListRsp();
				rsp = response.getByClass("result", rsp);
				if (listener != null) {
					listener.onResponse(rsp);
				}
			}

			public void onError(Exception e) {

				if (listener != null) {
					listener.onError(null);
				}
			};
		};
		request.setShowProgressDialog(false);
		request.execute();
	}

	public static void updateMyFavChannelList(FragmentActivity activity,
			ArrayList<Channel> channels) {

		updateMyFavChannelList(activity, null, channels, false);
	}

	public static void updateMyFavChannelList(FragmentActivity activity,
			final ResponseListener<UpdateMyFavChannelListRsp> responseListener,
			ArrayList<Channel> channels, boolean showDialog) {
		UniPacket uniPacket = createUniPacket("UpdateMyFavChannelList");

		ArrayList<Integer> channelIds = new ArrayList<Integer>();
		if (channels != null) {
			for (Channel channel : channels) {
				channelIds.add(channel.id);
			}
		}
		UpdateMyFavChannelListReq req = new UpdateMyFavChannelListReq();
		req.setChannelList(channelIds);
		uniPacket.put("request", req);

		// String cacheKey = null;
		// if (useCache) {
		// cacheKey = String.format("%s-%s", uniPacket.getServantName(),
		// uniPacket.getFuncName());
		// }
		Request request = new Request(activity, uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				UpdateMyFavChannelListRsp rsp = new UpdateMyFavChannelListRsp();
				rsp = response.getByClass("result", rsp);
				if (responseListener != null) {
					responseListener.onResponse(rsp);
				}
			}

			public void onError(Exception e) {
				Preference pref = Preference.getInstance();
				pref.setRetryState(true);
				e.getCause();

				if (responseListener != null) {
					responseListener.onError(null);
				}
			};
		};
		request.setShowProgressDialog(showDialog);
		request.execute();
	}

	public static void pushInit(String userId) {
		UniPacket uniPacket = createUniPacket("PushInit");

		PushInitReq req = new PushInitReq();
		req.setUserId(userId);
		uniPacket.put("request", req);
		Request request = new Request(uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				response.get("code");
			}

			public void onError(Exception e) {
				e.getCause();
			};
		};
		request.execute();
	}

	public static void sendFeedBack(
			final ResponseListener<Boolean> responseListener, String msg,
			String platformInfo, String appVersion) {
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			return;
		}
		UniPacket uniPacket = createUniPacket("FeedBack");

		FeedBackReq req = new FeedBackReq();
		req.setAppVersion(appVersion);
		req.setPlatformInfo(platformInfo);
		req.setContent(msg);
		uniPacket.put("request", req);
		Request request = new Request(uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				int code = -1;
				code = response.get("code", Integer.class);
				if (code == 0) {
					responseListener.onResponse(true);
				}
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		};
		request.execute();
	}
}
