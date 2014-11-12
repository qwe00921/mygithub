package com.yy.android.gamenews.jcewrapper;

import java.util.ArrayList;

import com.duowan.gamenews.ArticleInfo;
import com.duowan.taf.jce.JceStruct;

public abstract class GetArticleListRspLocal<E extends JceStruct> extends
		GetDataRsp<E> {

	public abstract Object getAttachInfo();

	public abstract boolean hasMore();

	public abstract void setPictopList(ArrayList<ArticleInfo> pictopList);

	public abstract ArrayList<ArticleInfo> getPictopList();

	public abstract ArrayList<ArticleInfo> getArticleList();

	public abstract void setArticleList(ArrayList<ArticleInfo> list);
}
