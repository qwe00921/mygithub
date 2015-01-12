package com.niuan.wificonnector.net;

public abstract class RequestExecutor<Q extends Request, P extends Response> {

	public abstract P executeRequest(Q request);
}
