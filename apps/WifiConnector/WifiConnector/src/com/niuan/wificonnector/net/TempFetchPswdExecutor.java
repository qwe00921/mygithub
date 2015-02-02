package com.niuan.wificonnector.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.niuan.wificonnector.util.WebViewUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TempFetchPswdExecutor extends
		RequestExecutor<FetchPasswordRequest, FetchPasswordResponse> {
	private Context mContext;

	public TempFetchPswdExecutor(Context context) {
		mContext = context;
	}

	private static HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				38007));
		ClientConnectionManager connManager = new ThreadSafeClientConnManager(
				params, schemeRegistry);
		HttpClient httpClient = new DefaultHttpClient(connManager, params);

		return httpClient;
	}

	public static String sendData(String url, List<NameValuePair> datas) {
		HttpClient client = new DefaultHttpClient();// createHttpClient();
		HttpGet post = new HttpGet(url);
		HttpResponse resp = null;
		String result = "";
		try {
			// if (datas != null) {
			// post.setEntity(new UrlEncodedFormEntity(datas, HTTP.UTF_8));
			// }
			resp = client.execute(post);
			result = EntityUtils.toString(resp.getEntity());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public FetchPasswordResponse executeRequest(FetchPasswordRequest request) {
		// GetHttps();
		// sendData("http://ps.exands.com:38007/ux/", null);

//		WebViewUtils.loadByWebView(mContext, "http://ps.exands.com:38007/ux/");
		// webView.loadUrl("http://ps.exands.com:38007/ux/");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FetchPasswordResponse rsp = new FetchPasswordResponse();
		rsp.pswdMap = new HashMap<String, String>();

		return rsp;
	}
}
