package com.anfu.photowebviewer;

import java.util.List;

import org.apache.http.NameValuePair;

import android.os.AsyncTask;

public class AsyncRequestSender {
	
	private MyHttpClient mMyHttpClient;
	
	
	public interface OnSuccessListener { 
		public void onSuccess(String result);
	}
	
	private OnSuccessListener mOnSuccessListener;
	
	public void sendHttpRequest(String path, List<NameValuePair> params, OnSuccessListener onSuccessListener) {
		if(mMyHttpClient == null) {
			mMyHttpClient = new MyHttpClient();
		}
		mOnSuccessListener = onSuccessListener;
		
		SendRequestAsyncTask task = new SendRequestAsyncTask();
		task.execute(path, params);
		
	}
	
	private class SendRequestAsyncTask extends AsyncTask<Object,Void,String> {

		@Override
		protected String doInBackground(Object... args) {
			if(args == null) {
				return null;
			}
			
			String path = (String)args[0];
			List<NameValuePair> params = (List<NameValuePair>) args[1];
			String result = mMyHttpClient.get(path, params);

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			mOnSuccessListener.onSuccess(result);
		}
	}
}
