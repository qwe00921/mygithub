package com.tencent.djcity.msgcenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

/**
 * 消息中心模块
 * 
 * @author junbaozhang
 * 
 */
public class MsgCenterActivity extends BaseActivity implements OnItemClickListener,
		OnSuccessListener<ArrayList<MsgModel>> {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msgcenter);
		
		this.loadNavBar(R.id.msg_navbar);

		mListView = (ListView) this.findViewById(R.id.msg_listview);

		currentModles = new ArrayList<BaseModel>();
		mMsgAdapter = new MsgAdapter(this, currentModles);

		mListView.setAdapter(mMsgAdapter);
		// mListView.setOnItemClickListener(this);
	}
	

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		this.fetchData();
	}

	private void fetchData()
	{
		showLoadingLayer();

		String url = "http://apps.game.qq.com/daoju/v3/test_apps/pullMsg.php?type=list&uin=123124123&biz=cf&sign=1dcb672bb215a727c2f2181eb71";
		Ajax ajax = AjaxUtil.get(url);// ServiceConfig.getAjax(Config.URL_CATEGORY_NEW);
		
		if (null == ajax)
			return;

		if (null == mParser)
		{
			mParser = new MsgModelParser();
		}
		ajax.setParser(mParser);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		ajax.send();
		addAjax(ajax);
	}

	@Override
	public void onSuccess(ArrayList<MsgModel> models, Response response)
	{
		closeLoadingLayer();

		if (!mParser.isSuccess())
		{
			UiUtils.makeToast(this, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR : mParser.getErrMsg());
			return;
		}

		allMsgModels = models;
		mUIHandler.sendEmptyMessage(MSG_REFRESH_LIST);

		mParser = null;
	}

	@Override
	protected void onDestroy()
	{
		mUIHandler.removeCallbacksAndMessages(null);
		mMsgAdapter = null;
		mListView = null;
		allMsgModels = null;
		currentModles = null;
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		cleanAllAjaxs();
		super.onPause();
	}

	private void render()
	{
		if (currentModles == null)
		{
			Log.w(TAG, "[render], currentModles is null");
			return;
		}
		if (mMsgAdapter == null)
		{
			Log.w(TAG, "[render], adapter is null");
			return;
		}
		currentModles.clear();
		currentModles.addAll(allMsgModels);
		mMsgAdapter.notifyDataSetChanged();
	}

	private static final String TAG = "MsgCenterActivity";
	private static final int MSG_REFRESH_LIST = 1001;
	private Handler mUIHandler = new UIHandler(this);

	private static class UIHandler extends Handler {
		private final WeakReference<MsgCenterActivity> mActivityRef;

		public UIHandler(MsgCenterActivity activity)
		{
			// TODO Auto-generated constructor stub
			mActivityRef = new WeakReference<MsgCenterActivity>(activity);
		}

		public void handleMessage(Message msg)
		{
			if (msg == null)
			{
				return;
			}
			MsgCenterActivity activity = mActivityRef.get();
			if (activity == null)
			{
				return;
			}

			int msgCode = msg.what;
			switch (msgCode)
			{
			case MSG_REFRESH_LIST:
			{
				activity.render();
				break;
			}
			default:
				break;
			}
		};
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub

	}

	private ListView mListView;
	private MsgAdapter mMsgAdapter;

	private ArrayList<MsgModel> allMsgModels;

	private ArrayList<BaseModel> currentModles;

	private MsgModelParser mParser;

}
