package com.icson.util.ajax;

/**
 * 当Ajax发送请求成功后会调用onSuccess()方法，将返回的response传入，并调用Parse对象来完成对返回数据的解析，然后封装到V中
 */
public interface OnSuccessListener<V> {
	void onSuccess(V v, Response response);
}
