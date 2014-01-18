package com.niuan.remoteconnector.data;

import com.niuan.remoteconnector.RemoteConnectionException;
import com.niuan.remoteconnector.RemoteConnection;


public abstract class RemoteDataProcessor {
	
	/**
	 * Read object from a object
	 * @param tag
	 */
	public abstract Object read(RemoteConnection connection) throws RemoteConnectionException;
	
	public abstract void write(RemoteConnection connection, Object object) throws RemoteConnectionException;
}
