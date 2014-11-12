package com.yy.android.gamenews.jcewrapper;

import com.duowan.taf.jce.JceStruct;

public abstract class GetDataRsp<E extends JceStruct> {

	private E object;

	public E getObject() {
		return object;
	}

	public void setObject(E object) {
		this.object = object;
	}

	public abstract E clone();
}
