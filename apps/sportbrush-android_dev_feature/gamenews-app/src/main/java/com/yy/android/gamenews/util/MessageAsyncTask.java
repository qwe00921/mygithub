package com.yy.android.gamenews.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.duowan.android.base.model.BaseModel.ResponseListener;
import com.duowan.gamenews.RefreshType;
import com.duowan.show.NoteCallType;
import com.duowan.show.NotificationRsp;
import com.yy.android.gamenews.event.MessageEvent;
import com.yy.android.gamenews.model.MessageModel;

import de.greenrobot.event.EventBus;

public class MessageAsyncTask extends AsyncTask<Void, NotificationRsp, Void> {
	private Context mContext;
	private Preference mPref;

	public MessageAsyncTask(Context context) {
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mPref = Preference.getInstance();
	}

	@Override
	protected Void doInBackground(Void... params) {
		MessageModel.getPersonMessage(new ResponseListener<NotificationRsp>(
				(FragmentActivity) mContext) {

			@Override
			public void onResponse(NotificationRsp arg0) {
				mPref.setNotifacation(arg0);
				onProgressUpdate(arg0);

			}
		}, RefreshType._REFRESH_TYPE_REFRESH,
				NoteCallType._NOTE_CALL_NOREAD_COUNT_TYPE, null);

		return null;
	}

	@Override
	protected void onProgressUpdate(NotificationRsp... values) {
		super.onProgressUpdate(values);
		MessageEvent event = new MessageEvent();
		event.setNeedUpdate(MessageEvent.STATUS_SUCESS);
		EventBus.getDefault().post(event);
	}

}
