package com.niuan.wificonnector.net;

import android.content.Context;

import com.niuan.wificonnector.util.thread.BackgroundTask;

public class Proxy {
	public static <Q, P> void sendRequest(Context context, Q req,
			ResponseListener<P> listener) {

		new SendRequestTask(context).execute(req, listener);
	}

	private static class SendRequestTask extends
			BackgroundTask<Object, Void, Response> {
		private ResponseListener<Response> listener;
		private Context mContext;

		public SendRequestTask(Context context) {
			mContext = context;
		}

		@Override
		protected Response doInBackground(Object... params) {

			Request req = (Request) params[0];
			listener = (ResponseListener<Response>) params[1];
			Response rsp = getExecutor(req).executeRequest(req);

			return rsp;
		}

		@Override
		protected void onPostExecute(Response result) {
			if (listener != null) {
				listener.onResponse(result);
			}
			super.onPostExecute(result);
		}

		private RequestExecutor getExecutor(Request req) {
			if (req instanceof FetchPasswordRequest) {
				return new TempFetchPswdExecutor(mContext);
			}
			return null;
		}
	}

}
