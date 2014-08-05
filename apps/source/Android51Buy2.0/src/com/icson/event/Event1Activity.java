package com.icson.event;

import android.os.Bundle;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class Event1Activity extends EventBaseActivity implements OnSuccessListener<Event1Model> {

	private static final String LOG_TAG = Event1Activity.class.getName();

	private Event1Adapter mEvent1Adapter;

	private Event1Model mEvent1Model;

	private ListView mListView;

	private Event1Parser mParser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.event_1_activity);
		if (mEventId == 0) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}

		loadNavBar(R.id.event1_navbar);
		initData();
	}

	private String getCacheKey(){
		return CacheKeyFactory.CACHE_EVENT + mEventId + "_" + ILogin.getSiteId();
	}
	
	private void initData() {
		mParser = new Event1Parser();

		IPageCache cache = new IPageCache();
		String content = cache.get(getCacheKey());

		try {
			if (content != null) {
				mEvent1Model = mParser.parse(content);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			mEvent1Model = null;
		} finally {
			if (mEvent1Model == null) {
				sendRequest();
			} else {
				requestFinish();
			}
		}
	}

	private void sendRequest() {
		Ajax ajax = ServiceConfig.getAjax(Config.URL_EVENT_PAGE, mEventId);
		if( null == ajax )
			return ;
		showLoadingLayer();
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		ajax.setParser(mParser);
		ajax.send();
		addAjax(ajax);
	}

	@Override
	public void onSuccess(Event1Model v, Response response) {
		closeLoadingLayer();
		
		if(null == v) {
			UiUtils.makeToast(Event1Activity.this, Config.NORMAL_ERROR,true);
			finish();
			return;
		}
		
		mEvent1Model = v;
		requestFinish();

		String content = mParser.getString();

		if (content != null) {
			IPageCache cache = new IPageCache();
			cache.set(getCacheKey(), content, Config.CHANNEL_CACHE_TIME);
		}
	}

	private void requestFinish() {
		if (mEvent1Model == null)
			return;

		mListView = (ListView) findViewById(R.id.event_1_listview);
		mListView.setDividerHeight(0);
		mListView.setHeaderDividersEnabled(false);
		mEvent1Adapter = new Event1Adapter(this, mEvent1Model.getProductModels(),mEvent1Model.getPayType());
		
		mListView.setAdapter(mEvent1Adapter);
		
		this.setNavBarStatus(mEvent1Model.getTitle());
	}
}