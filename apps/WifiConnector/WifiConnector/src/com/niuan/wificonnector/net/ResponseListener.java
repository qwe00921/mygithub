package com.niuan.wificonnector.net;

public interface ResponseListener<E> {

	public void onResponse(E data);

	public void onError(Exception e);
}
