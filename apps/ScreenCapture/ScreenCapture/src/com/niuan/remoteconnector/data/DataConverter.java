package com.niuan.remoteconnector.data;

import java.util.ArrayList;
import java.util.List;

public abstract class DataConverter<E> {
	
	private List<String> mAcceptTypeNameList = new ArrayList<String>();
	
	{
		initAcceptTypeList(mAcceptTypeNameList);
	}
	
	protected abstract void initAcceptTypeList(List<String> acceptTypeList);
	
	public boolean accept(String clsName) {
		if(mAcceptTypeNameList == null) {
			return false;
		}
		
		return mAcceptTypeNameList.contains(clsName);
	}
	
	public abstract byte[] readByteFromObject(Object object);
	
	public abstract E readObjectFromByte(byte[] byteData);
}
