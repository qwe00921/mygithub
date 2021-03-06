package com.anfu.photowebviewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends Activity {

	public static final String KEY_URL = "key_url";
	private WebView mWebView;
	private static final String TAG = "WebViewActivity";
	private static File ROOT_DIRECTORY = new File(Util.getRootDirectory());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		Intent intent = getIntent();
		String url = intent.getStringExtra(KEY_URL);

		mWebView = (WebView) findViewById(R.id.web_view);
		MyHttpClient.synCookies(this, url);
		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new HelloWebViewClient());
		mWebView.setDownloadListener(new MyWebViewDownLoadListener());
	}

	// Web视图
	private class HelloWebViewClient extends WebViewClient {
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			Log.i(TAG, "shouldOverviewUrlLoading");
			view.loadUrl(url);
			return true;
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i(TAG, "onPageStarted");
			showProgressDialog();
		}

		public void onPageFinished(WebView view, String url) {
			Log.i(TAG, "onPageFinished");
			closeProgressDialog();
		}

		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Log.i(TAG, "onReceivedError");
			closeProgressDialog();
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack(); // goBack()表示返回WebView的上一页面
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View view) {
		if (view != null) {
			switch (view.getId()) {
			case R.id.btn_back: {
				onBackPressed();
				break;
			}
			case R.id.btn_exit: {

				Intent intent = new Intent();
				intent.setClass(WebViewActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 注意本行的FLAG设置
				startActivity(intent);
				finish();
				break;
			}

			case R.id.btn_previous: {
				mWebView.goBack();
				break;
			}
			case R.id.btn_next: {
				mWebView.goForward();
				break;
			}
			}
		}
	}

	private class MyWebViewDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
//			if (!Environment.getExternalStorageState().equals(
//					Environment.MEDIA_MOUNTED)) {
//				Toast t = Toast.makeText(WebViewActivity.this, "需要SD卡。", Toast.LENGTH_LONG);
//				t.setGravity(Gravity.CENTER, 0, 0);
//				t.show();
//				return;
//			}
			DownloaderTask task = new DownloaderTask();
			task.execute(url);
		}

	}

	// 内部类
	private class DownloaderTask extends AsyncTask<String, Void, String> {

		public DownloaderTask() {
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			// Log.i("tag", "url="+url);
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			fileName = URLDecoder.decode(fileName);
			Log.i("tag", "fileName=" + fileName);

			File file = new File(ROOT_DIRECTORY, fileName);
			if (file.exists()) {
				Log.i("tag", "The file has already exists.");
				return fileName;
			}
			try {
				HttpClient client = new DefaultHttpClient();
				// client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				if (HttpStatus.SC_OK == response.getStatusLine()
						.getStatusCode()) {
					HttpEntity entity = response.getEntity();
					InputStream input = entity.getContent();

					writeToSDCard(fileName, input);

					input.close();
					// entity.consumeContent();
					return fileName;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			closeProgressDialog();
			if (result == null) {
				Toast t = Toast.makeText(WebViewActivity.this, "连接错误！请稍后再试！",
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				return;
			}

			String filePath = ROOT_DIRECTORY.getAbsolutePath() + result;
			Toast t = Toast.makeText(WebViewActivity.this, "已保存到" + filePath, Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			File file = new File(ROOT_DIRECTORY, result);
			Log.i("tag", "Path=" + file.getAbsolutePath());

			Intent intent = getFileIntent(file);

			startActivity(intent);

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgressDialog();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

	}

	private ProgressDialog mDialog;

	private void showProgressDialog() {
		if (mDialog == null) {
			mDialog = new ProgressDialog(WebViewActivity.this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			mDialog.setMessage("正在加载 ，请等待...");
			mDialog.setIndeterminate(false);// 设置进度条是否为不明确
			mDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					mDialog = null;
				}
			});
			mDialog.show();

		}
	}

	private void closeProgressDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public Intent getFileIntent(File file) {
		// Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
		Uri uri = Uri.fromFile(file);
		String type = getMIMEType(file);
		Log.i("tag", "type=" + type);
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, type);
		return intent;
	}

	public void writeToSDCard(String fileName, InputStream input) {

		File file = new File(ROOT_DIRECTORY, fileName);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// if(file.exists()){
		// Log.i("tag", "The file has already exists.");
		// return;
		// }
		try {
			
			FileOutputStream fos = new FileOutputStream(file);
			byte[] b = new byte[2048];
			int j = 0;
			while ((j = input.read(b)) != -1) {
				fos.write(b, 0, j);
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		/* 取得扩展名 */
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		/* 依扩展名的类型决定MimeType */
		if (end.equals("pdf")) {
			type = "application/pdf";//
		} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio/*";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video/*";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image/*";
		} else if (end.equals("apk")) {
			/* android.permission.INSTALL_PACKAGES */
			type = "application/vnd.android.package-archive";
		}
		// else if(end.equals("pptx")||end.equals("ppt")){
		// type = "application/vnd.ms-powerpoint";
		// }else if(end.equals("docx")||end.equals("doc")){
		// type = "application/vnd.ms-word";
		// }else if(end.equals("xlsx")||end.equals("xls")){
		// type = "application/vnd.ms-excel";
		// }
		else {
			// /*如果无法直接打开，就跳出软件列表给用户选择 */
			type = "*/*";
		}
		return type;
	}
}
