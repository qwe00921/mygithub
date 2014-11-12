package com.yy.android.gamenews.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;

import com.duowan.gamenews.ArticleFlag;
import com.duowan.gamenews.ArticleInfo;
import com.duowan.gamenews.ArticleType;
import com.duowan.gamenews.GetChannelArticleListReq;
import com.duowan.gamenews.GetChannelArticleListRsp;
import com.duowan.gamenews.GetFavArticleListReq;
import com.duowan.gamenews.GetFavArticleListRsp;
import com.duowan.gamenews.GetSpecialArticleListReq;
import com.duowan.gamenews.GetSpecialArticleListRsp;
import com.duowan.gamenews.GetVideoUrlReq;
import com.duowan.gamenews.GetVideoUrlRsp;
import com.duowan.gamenews.MeRsp;
import com.duowan.gamenews.PicInfo;
import com.duowan.gamenews.RefreshType;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.util.ToastUtil;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

/**
 * @author yy:909012970@liuchaoqun
 * @version 创建时间：2014-3-20 下午2:44:18
 */
public class ArticleModel extends CommonModel {

	private static final Handler sHandler;
	private static final boolean TEST_DATA = false;
	// private static int testCount = 0;
	// private static int testReturnCount = 5;
	static {
		sHandler = new Handler(Looper.getMainLooper());
	}

	public static void getMeRsp(final ResponseListener<MeRsp> responseListener) {
		UniPacket uniPacket = createUniPacket("Me");
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				MeRsp rsp = new MeRsp();
				rsp = response.getByClass("result", rsp);
				responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}

	public static void getFavArticleList(
			final ResponseListener<GetFavArticleListRsp> responseListener,
			int refreshType, String attachInfo) {

		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			responseListener.onError(null);
			return;
		}

		UniPacket uniPacket = createUniPacket("GetFavArticleList");

		GetFavArticleListReq req = new GetFavArticleListReq();

		req.setRefreshType(refreshType);
		req.setCount(10); // 默认10个
		req.setAttachInfo(attachInfo);
		uniPacket.put("request", req);

		// String cacheKey = String.format("%s-%s", uniPacket.getServantName(),
		// uniPacket.getFuncName());
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				GetFavArticleListRsp rsp = new GetFavArticleListRsp();
				rsp = response.getByClass("result", rsp);
				responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}

	public static void getSpecialArticleList(
			final ResponseListener<GetSpecialArticleListRsp> responseListener,
			int refreshType, long specialId, Map<Integer, String> attachInfo) {

		if (TEST_DATA) {
			sHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					GetSpecialArticleListRsp rsp = new GetSpecialArticleListRsp();

					PicInfo info = new PicInfo();
					info.setUrl("http://news.baidu.com/z/resource/r/image/2014-05-16/f465f3ca5010ac13f077853a3156dfe8.jpg");
					info.setWidth(300);
					info.setHeight(300);

					ArrayList<PicInfo> cover = new ArrayList<PicInfo>();
					cover.add(info);
					cover.add(info);
					cover.add(info);
					cover.add(info);
					rsp.setCover(cover);
					rsp.setArticleList(getData());
					rsp.setHasMore(true);
					rsp.setDesc("这是一个魔兽世界专题这是一个魔兽世界专题这是一个魔兽世界专题这是一个魔兽世界专题这是一个魔兽世界专题");
					rsp.setName("魔兽世界专题");

					responseListener.onResponse(rsp);
				}
			}, 2000);

			return;
		}
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			responseListener.onError(null);
			return;
		}
		UniPacket uniPacket = createUniPacket("GetSpecialArticleList");

		GetSpecialArticleListReq req = new GetSpecialArticleListReq();

		req.setRefreshType(refreshType);
		req.setCount(10); // 默认10个
		req.setSpecialId(specialId);
		req.setAttachInfo(attachInfo);
		uniPacket.put("request", req);

		// String cacheKey = String.format("%s-%s-%d",
		// uniPacket.getServantName(),
		// uniPacket.getFuncName(), specialId);
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				// TODO: error handling
				GetSpecialArticleListRsp rsp = new GetSpecialArticleListRsp();
				rsp = response.getByClass("result", rsp);
				responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(false).execute();
	}

	//
	// public static void getArticleCategoryReq(
	// final ResponseListener<GetChannelArticleCategoryRsp> listener,
	// int id) {
	// if (true) {
	// sHandler.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// GetChannelArticleCategoryRsp rsp = new GetChannelArticleCategoryRsp();
	//
	// ArrayList<ArticleCategory> categoryList = new
	// ArrayList<ArticleCategory>();
	// int id = 1;
	// ArticleCategory category = new ArticleCategory();
	// category.setId(id++);
	// category.setName("资讯");
	// categoryList.add(category);
	//
	// category = new ArticleCategory();
	// category.setId(id++);
	// category.setName("攻略");
	// categoryList.add(category);
	//
	// category = new ArticleCategory();
	// category.setId(id++);
	// category.setName("图片");
	// categoryList.add(category);
	//
	// category = new ArticleCategory();
	// category.setId(id++);
	// category.setName("视频");
	// categoryList.add(category);
	//
	// category = new ArticleCategory();
	// category.setId(id++);
	// category.setName("礼包");
	// categoryList.add(category);
	//
	// rsp.setCategoryList(categoryList);
	//
	// listener.onResponse(rsp);
	// }
	// }, 2000);
	//
	// return;
	// }
	//
	// if (!Util.isNetworkConnected()) {
	// ToastUtil.showToast(R.string.http_not_connected);
	// listener.onError(null);
	// return;
	// }
	//
	// UniPacket uniPacket = createUniPacket("GetChannelArticleCategory");
	// GetChannelArticleCategoryReq req = new GetChannelArticleCategoryReq();
	// req.setChannelId(id);
	//
	// uniPacket.put("request", req);
	//
	// String cacheKey = String.format("%s-%s-%s",
	// uniPacket.getServantName(), uniPacket.getFuncName(),
	// String.valueOf(id));
	//
	// new Request(listener.get(), uniPacket, cacheKey) {
	// @Override
	// public void onResponse(UniPacket response) {
	// GetChannelArticleCategoryRsp rsp = new GetChannelArticleCategoryRsp();
	// rsp = response.getByClass("result", rsp);
	// listener.onResponse(rsp);
	// }
	//
	// public void onError(Exception e) {
	// };
	// }.setShowProgressDialog(false).execute();
	// }

	public static void getVideoUrlReq(final ResponseListener<GetVideoUrlRsp> responseListener, long articleId) {
//		
//		if(true) {
//			
//			
//			GetVideoUrlRsp rsp = new GetVideoUrlRsp();
//			rsp.setVideoFlag(VideoFlag._VIDEO_FLAG_SOURCE);
//			rsp.setUrl("http://v.17173.com/api/18430687-1.m3u8");
//			
//			responseListener.onResponse(rsp);
//			return;
//		}
//		
//		
//		if (!Util.isNetworkConnected()) {
//			ToastUtil.showToast(R.string.http_not_connected);
//			responseListener.onError(null);
//			return;
//		}
		UniPacket uniPacket = createUniPacket("GetVideoUrl");

		GetVideoUrlReq req = new GetVideoUrlReq();
		req.setArticleId(articleId);
		uniPacket.put("request", req);
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				// TODO: error handling
				GetVideoUrlRsp rsp = new GetVideoUrlRsp();
				rsp = response.getByClass("result", rsp);
				responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(true).execute();
		
	}

	public static void getArticleList(
			final ResponseListener<GetChannelArticleListRsp> responseListener,
			final int refreshType, int channelId, int subChannelId,
			int subType, Map<Integer, String> attachInfo, boolean showDialog) {

		if (TEST_DATA) {
			sHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					GetChannelArticleListRsp rsp = new GetChannelArticleListRsp();

					rsp.setArticleList(getData());
					// if(testCount == testReturnCount) {
					// rsp.setHasMore(false);
					// } else {
					// testCount++;
					rsp.setHasMore(true);
					// }

					responseListener.onResponse(rsp);
				}
			}, 2000);

			return;
		}
		if (!Util.isNetworkConnected()) {
			ToastUtil.showToast(R.string.http_not_connected);
			responseListener.onError(null);
			return;
		}
		UniPacket uniPacket = createUniPacket("GetChannelArticleList");

		GetChannelArticleListReq req = new GetChannelArticleListReq();

		req.setRefreshType(refreshType);
		// req.setCount(10); // 默认10个
		req.setChannelId(channelId);
		req.setSubChannelId(subChannelId);
		req.setSubType(subType);
		req.setAttachInfo(attachInfo);
		uniPacket.put("request", req);

		// String.format("%s-%s-%d", uniPacket.getServantName(),
		// uniPacket.getFuncName(), channelId);
		new Request(responseListener.get(), uniPacket) {
			@Override
			public void onResponse(UniPacket response) {
				// TODO: error handling
				GetChannelArticleListRsp rsp = new GetChannelArticleListRsp();
				rsp = response.getByClass("result", rsp);
				if (refreshType == RefreshType._REFRESH_TYPE_REFRESH
						&& rsp != null) {
					ArrayList<ArticleInfo> articleList = rsp.getArticleList();
					if (articleList != null) {
						for (int j = 0; j < articleList.size(); j++) {
							articleList.get(j).setTime(
									(int) (System.currentTimeMillis() / 1000));
						}
					}

				}
				responseListener.onResponse(rsp);
			}

			public void onError(Exception e) {
				responseListener.onError(e);
			};
		}.setShowProgressDialog(showDialog).execute();
	}

	private static ArrayList<ArticleInfo> sTopInfoList = new ArrayList<ArticleInfo>();
	static {
		ArticleInfo model = new ArticleInfo();
		model.setId(1989);
		model.setCommentCount(13);
		model.setSourceName("多玩网");
		model.setTime((int) (System.currentTimeMillis() / 1000));
		model.setTitle("置顶的文章");

		model.flag = model.flag | ArticleFlag._ARTICLE_FLAG_TOP;
		Map<Long, String> extraInfo = new HashMap<Long, String>();
		extraInfo.put((long) ArticleFlag._ARTICLE_FLAG_TOP,
				String.valueOf((System.currentTimeMillis() + 6000) / 1000));
		model.setExtraInfo(extraInfo);
		model.setChannelName("");
		model.setVideoList(new ArrayList<String>());

		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add("http://mt1.baidu.com/timg?wh_rate=0&wapiknow&quality=100&size=w250&sec=0&di=48a18b80ffa1fc6ade04d224ab555f58&src=http%3A%2F%2Fimg.iknow.bdimg.com%2Fjctuijian%2F0624%2F6.jpg");
		model.setImageList(urlList);
		sTopInfoList.add(model);
	}

	private static int sAddTopCount;

	public static ArrayList<ArticleInfo> getData() {

		ArrayList<ArticleInfo> modelList = new ArrayList<ArticleInfo>();

		if (sAddTopCount % 3 == 0) {
			modelList.addAll(sTopInfoList);
		}
		sAddTopCount++;

		for (int i = 0; i < 10; i++) {
			ArticleInfo model = new ArticleInfo();
			model.setId(i + 1);
			model.setCommentCount(13);
			model.setSourceName("多玩网");
			model.setTime((int) (System.currentTimeMillis() / 1000));
			model.setTitle("这些你都知道？《口袋战争》基本常识课堂");

			ArrayList<String> urlList = new ArrayList<String>();
			if (i % 3 == 0) {
				urlList.add("http://news.baidu.com/z/resource/r/image/2014-05-16/f465f3ca5010ac13f077853a3156dfe8.jpg");
				urlList.add("http://news.baidu.com/z/resource/r/image/2014-05-16/f465f3ca5010ac13f077853a3156dfe8.jpg");
				urlList.add("http://news.baidu.com/z/resource/r/image/2014-05-16/f465f3ca5010ac13f077853a3156dfe8.jpg");
			} else {
				urlList.add("http://news.baidu.com/z/resource/r/image/2014-05-16/f465f3ca5010ac13f077853a3156dfe8.jpg");
			}

			long flag = model.flag;
			switch (i) {
			case 1: {
				flag |= ArticleFlag._ARTICLE_FLAG_ADV;
				break;
			}
			case 2: {
				flag |= ArticleFlag._ARTICLE_FLAG_BIGIMAGE;
				Map<Long, String> extraInfo = model.extraInfo;
				if (extraInfo == null) {
					extraInfo = new HashMap<Long, String>();
					model.extraInfo = extraInfo;
				}
				extraInfo
						.put((long) ArticleFlag._ARTICLE_FLAG_BIGIMAGE,
								"http://mt1.baidu.com/timg?wh_rate=0&wapiknow&quality=100&size=w250&sec=0&di=48a18b80ffa1fc6ade04d224ab555f58&src=http%3A%2F%2Fimg.iknow.bdimg.com%2Fjctuijian%2F0624%2F6.jpg");
				break;
			}
			case 3: {
				flag |= ArticleFlag._ARTICLE_FLAG_HOT;
				break;
			}
			case 4: {
				flag |= ArticleFlag._ARTICLE_FLAG_RECOMM;
				break;
			}
			case 5: {
				// flag |= ArticleFlag._ARTICLE_FLAG_TOP;
				break;
			}
			case 6: {
				break;
			}
			case 7: {
				break;
			}
			case 8: {
				break;
			}
			}
			model.setFlag(flag);
			if (i % 4 == 0) {
				model.setArticleType(ArticleType._ARTICLE_TYPE_SPECIAL);
			} else {
				model.setArticleType(ArticleType._ARTICLE_TYPE_ARTICLE);
			}

			model.setImageList(urlList);
			modelList.add(model);
		}

		return modelList;
	}
}
