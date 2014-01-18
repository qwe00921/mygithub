package com.niuan.remoteconnector.stream;

import java.io.InputStream;
import java.io.OutputStream;

import com.niuan.remoteconnector.RemoteConnection;
import com.niuan.remoteconnector.RemoteConnector;

public class RemoteStreamConnection extends RemoteConnection {

	public RemoteStreamConnection(RemoteConnector connector) {
		super(connector);
		// TODO Auto-generated constructor stub
	}

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	public InputStream getInputStream() {
		return mInputStream;
	}

	public void setInputStream(InputStream inputStream) {
		mInputStream = inputStream;
	}

	public OutputStream getOutputStream() {
		return mOutputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		mOutputStream = outputStream;
	}

}
