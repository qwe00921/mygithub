package com.icson.event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.ui.UiUtils;
import com.icson.list.ListActivity;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class Event3Activity extends EventBaseActivity implements OnSuccessListener<Event3Model> {

	private static final String LOG_TAG = Event3Activity.class.getName();

	private Event3Adapter mEvent3Adapter;

	private Event3Model mEvent3Model;

	private ListView mListView;

	private Event3Parser mParser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.event_3_activity);
		if (mEventId == 0) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}

		loadNavBar(R.id.event3_navbar);
		initData();
	}

	
	private String getCacheKey(){
		return CacheKeyFactory.CACHE_EVENT + mEventId + "_" + ILogin.getSiteId();
	}
	
	private void initData() {
		mParser = new Event3Parser() ;

		IPageCache cache = new IPageCache();
		String content = cache.get(getCacheKey());

		try {
			if (content != null) {
				mEvent3Model = mParser.parse(content);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			mEvent3Model = null;
		} finally {
			if (mEvent3Model == null) {
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
	public void onSuccess(Event3Model v, Response response) {
		closeLoadingLayer();
		
		if(null == v) {
			UiUtils.makeToast(Event3Activity.this, Config.NORMAL_ERROR,true);
			finish();
			return;
		}
		
		mEvent3Model = v;
		requestFinish();

		String content = mParser.getString();

		if (content != null) {
			IPageCache cache = new IPageCache();
			cache.set(getCacheKey(), content, Config.CHANNEL_CACHE_TIME);
		}

	}

	private void requestFinish() {
		if (mEvent3Model == null)
			return;

		//页面背景颜色 
		View container = findViewById(R.id.event_3_body);
		container.setBackgroundColor(mEvent3Model.getBackground());
		mListView = (ListView) findViewById(R.id.event_3_listview);
		mListView.setDividerHeight(0);
		mListView.setHeaderDividersEnabled(false);
		LayoutInflater inflator = LayoutInflater.from(this);

		final View header = inflator.inflate(R.layout.event_3_header, null);
		mListView.addHeaderView(header);
		mEvent3Adapter = new Event3Adapter(this, mEvent3Model.getProductModels(),mEvent3Model.getPayType());
		mListView.setAdapter(mEvent3Adapter);
		
		this.setNavBarStatus(mEvent3Model.getTitle());

		AjaxUtil.getLocalImage(this, mEvent3Model.getAdvertiseUrl(), new ImageLoadListener() {
			@Override
			public void onLoaded(Bitmap image, String url) {
				((ImageView) header.findViewById(R.id.event_3_image)).setImageBitmap(image);
				header.setVisibility(View.VISIBLE);
			}

			@Override
			public void onError(String strUrl) {
			}
		});

		if (mEvent3Model.getListUrl() != null && !mEvent3Model.getListUrl().equals("")) {
			header.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle param = new Bundle();
					param.putString(ListActivity.REQUEST_SEARCH_URL, mEvent3Model.getListUrl());
					ToolUtil.startActivity(Event3Activity.this, ListActivity.class, param);
				}
			});
		}
	}
}