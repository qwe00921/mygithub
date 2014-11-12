package com.duowan.android.base.net.toolbox;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.duowan.jce.wup.UniPacket;

/**
 * Created by duowan@lishaoqi on 14-3-6.
 */
public class WupRequest extends Request<UniPacket> {

	private UniPacket uniPacket;
	private Response.Listener<UniPacket> listener;
	private String cacheKey = null;

	private long cacheHitButRefreshed = 10 * 60 * 1000; // in 10 minutes cache
														// will be hit, but
														// also refreshed on
														// background
	private long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache
														// entry expires
														// completely

	public WupRequest(String host, UniPacket uniPacket,
			Response.Listener<UniPacket> listener,
			Response.ErrorListener errorListener) {
		super(Method.POST, host, errorListener);
		this.uniPacket = uniPacket;
		this.listener = listener;

		setShouldCache(false);
	}

	@Override
	public String getBodyContentType() {
		return "application/multipart-formdata";
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		return uniPacket.encode();
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		if (cacheKey != null && !cacheKey.equals(""))
			setShouldCache(true);
	}

	public void setCacheHitButRefreshed(long cacheHitButRefreshed) {
		this.cacheHitButRefreshed = cacheHitButRefreshed;
	}

	public void setCacheExpired(long cacheExpired) {
		this.cacheExpired = cacheExpired;
	}

	@Override
	public String getCacheKey() {
		return cacheKey;
	}

	@Override
	protected Response<UniPacket> parseNetworkResponse(NetworkResponse response) {
		try {
			UniPacket uniPacket = new UniPacket();
			uniPacket.decode(response.data);
			return Response.success(
					uniPacket,
					parseIgnoreCacheHeaders(uniPacket, response,
							cacheHitButRefreshed, cacheExpired));
		} catch (RuntimeException e) {
			e.printStackTrace();
			return Response.error(new ParseError(e));
		}

	}

	@Override
	protected void deliverResponse(UniPacket response) {
		listener.onResponse(response);
	}

	public static Cache.Entry parseIgnoreCacheHeaders(UniPacket uniPacket,
			NetworkResponse response, long cacheHitButRefreshed,
			long cacheExpired) {

		if (response.data == null || response.data.length == 0)
			return null;

		long now = System.currentTimeMillis();

		Map<String, String> headers = response.headers;

		long serverDate = 0;
		String serverEtag = null;
		String headerValue;

		headerValue = headers.get("Date");
		if (headerValue != null) {
			serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
		}

		serverEtag = uniPacket.getFuncName();
		long softExpire = now + cacheHitButRefreshed;
		long ttl = now + cacheExpired;

		Cache.Entry entry = new Cache.Entry();
		entry.data = response.data;
		entry.etag = serverEtag;
		entry.softTtl = softExpire;
		entry.ttl = ttl;
		entry.serverDate = serverDate;
		entry.responseHeaders = headers;

		return entry;
	}

}
