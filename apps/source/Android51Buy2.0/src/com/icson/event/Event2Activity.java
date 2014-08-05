package com.icson.event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.icson.R;
import com.icson.event.Event2Model.Event2SubModel;
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

public class Event2Activity extends EventBaseActivity implements OnSuccessListener<Event2Model> {

	private static final String LOG_TAG = Event2Activity.class.getName();

	private Event2Adapter mEvent2Adapter;

	private Event2Model mEvent2Model;

	private ListView mListView;

	private Event2Parser mParser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.event_2_activity);
		if (mEventId == 0) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}

		loadNavBar(R.id.event2_navbar);
		initData();
	}
	
	private String getCacheKey(){
		return CacheKeyFactory.CACHE_EVENT + mEventId + "_" + ILogin.getSiteId();
	}

	private void initData() {
		mParser = new Event2Parser();

		IPageCache cache = new IPageCache();
		String content = cache.get(getCacheKey());

		try {
			if (content != null) {
				mEvent2Model = mParser.parse(content);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ex);
			mEvent2Model = null;
		} finally {
			if (mEvent2Model == null) {
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
	public void onSuccess(Event2Model v, Response response) {
		closeLoadingLayer();
		
		if(null == v) {
			UiUtils.makeToast(Event2Activity.this, Config.NORMAL_ERROR,true);
			finish();
			return;
		}
		
		mEvent2Model = v;
		requestFinish();

		String content = mParser.getString();

		if (content != null) {
			IPageCache cache = new IPageCache();
			cache.set(getCacheKey(), content, Config.CHANNEL_CACHE_TIME);
		}

	}

	private void requestFinish() {
		if (mEvent2Model == null)
			return;

		mListView = (ListView) findViewById(R.id.event_2_listview);
		// mListView.setDividerHeight(0);
		mListView.setHeaderDividersEnabled(false);
		final View mHeaderView = getLayoutInflater().inflate(R.layout.event_2_header, null);
		mHeaderView.setVisibility(View.GONE);
		final ImageView mImageview = (ImageView) mHeaderView.findViewById(R.id.event_2_image);
		AjaxUtil.getLocalImage(this, mEvent2Model.getAdvertiseUrl(), new ImageLoadListener() {
			@Override
			public void onLoaded(Bitmap image, String url) {
				mImageview.setImageBitmap(image);
				mHeaderView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onError(String strUrl)
			{
			}
		});

		if (mEvent2Model.getListUrl() != null && !mEvent2Model.getListUrl().equals("")) {
			mImageview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle param = new Bundle();
					param.putString(ListActivity.REQUEST_SEARCH_URL, mEvent2Model.getListUrl());
					ToolUtil.startActivity(Event2Activity.this, ListActivity.class, param);
					
					ToolUtil.sendTrack(getClass().getName(), "1990"+ (60+mEventId), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "01011");
				}
			});
		}

		mListView.addHeaderView(mHeaderView);
		mEvent2Adapter = new Event2Adapter(this, mEvent2Model.getEvent2SubModels(),mEvent2Model.getPayType());
		mListView.setAdapter(mEvent2Adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < 1)
					return;
				Event2SubModel model = mEvent2Model.getEvent2SubModels().get(position - 1);
				Bundle param = new Bundle();
				param.putString(ListActivity.REQUEST_SEARCH_URL, model.getListUrl());
				param.putString(ListActivity.REQUEST_PAGE_TITLE, model.getTitle());
				ToolUtil.startActivity(Event2Activity.this, ListActivity.class, param);
				String pageId = "1990"+ (60+mEventId);
				String locationId = "";
				if(position>9)
					locationId +="020"+(position);
				else
					locationId +="0200"+(position);
				
				ToolUtil.sendTrack(getClass().getName(), pageId, ListActivity.class.getName(), getString(R.string.tag_ListActivity), locationId);
			}
		});

		
		this.setNavBarStatus(mEvent2Model.getTitle());
	}
}