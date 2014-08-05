package com.tencent.djcity.util.ajax;

import org.apache.http.HttpStatus;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.tencent.djcity.R;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.Log;
import com.tencent.djcity.util.ToolUtil;

/**
 * è¯¥ç±»??§å??ajax?????¢æ?????????????¨ç????§è??æµ?ç¨?
 *
 */
public class AjaxTask extends AsyncTask<Ajax, int[], Object> implements OnProgressListener {

	private static final String LOG_TAG = AjaxTask.class.getName();

	private volatile boolean isCanceled = false;

	private boolean isError = false;

	private Ajax mAjax;

	public AjaxTask(Ajax ajax) {
		mAjax = ajax;
	}

	@Override
	protected void onPreExecute() {
		mAjax.performOnBefore();
	}

	@Override
	public void onProgress(Response response, int downLoaded, int totalSize) {
		if (isCanceled)
			return;

		publishProgress(new int[] { downLoaded, totalSize });
	}

	protected void onProgressUpdate(int[] values) {
		mAjax.performOnProgress(values[0], values[1]);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doInBackground(Ajax... ajaxs) {
		Object data = null;
		final Ajax ajax = mAjax;

		if (ajax == null)
			return null;

		HttpRequest mHttpRequest = ajax.getHttpRequest();
		if (ajax.getOnProgressListener() != null) {
			mHttpRequest.setOnProgressListener(this);
		}

		if (mAjax.getParser() == null && ajax.getOnSuccessListener() == null) {
			mHttpRequest.setIsNeedResponse(false);
		}

		isError = !mHttpRequest.send();

		if (!isError && !isCanceled && ajax.getOnSuccessListener() != null && mHttpRequest.getHttpStatus() == HttpStatus.SC_OK) {
			@SuppressWarnings("rawtypes")
			final Parser parser = mAjax.getParser();
			try {
				if (parser == null) {
					Log.e(LOG_TAG, "you have set onSuccessListener, but the parser is null");
				} else {
					data = parser.parse(mHttpRequest.getResponseData(), mHttpRequest.getCharset());
				}
			} catch (Exception ex) {
				if(null != parser && TextUtils.isEmpty(parser.getErrMsg())) {
					parser.setErrMsg(IcsonApplication.app.getString(R.string.parser_error_msg));
				}
				
				isError = true;
				if (Config.DEBUG) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
				}
			}
		}

		return data;
	}

	public boolean isError() {
		return isError;
	}

	@Override
	protected void onPostExecute(Object data) {
		if (mAjax == null || isCanceled)
			return;
		mAjax.run(data);
	}

	@Override
	protected void onCancelled() {
		if (mAjax == null)
			return;

		mAjax.performOnCancel();
	}

	public void cancel() {
		super.cancel(true);
		isCanceled = true;
		if (mAjax != null) {
			HttpRequest mHttpRequest = mAjax.getHttpRequest();
			if (mHttpRequest != null) {
				mHttpRequest.cancel();
				mHttpRequest = null;
			}
		}
	}

	public void destory() {
		cancel();

		if (mAjax != null) {
			mAjax = null;
		}
	}
}