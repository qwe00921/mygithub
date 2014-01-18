package com.niuan.remoteconnector;

public class RemoteConnectionInfo {
	public enum ConnectionType {
		C_BT,
		C_USB,
		C_SOCKET,
		S_BT,
		S_USB,
		S_SOCKET
	}
	
	private ConnectionType mType;
	
	public ConnectionType getConnectionType() {
		return mType;
	}
	
	public void setConnectionType(ConnectionType type) { 
		mType = type;
	}
	
	@Override
	public int hashCode() {
		return getConnectionType().hashCode() * 42; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}

		boolean isTypeEquals = false;
		if(obj instanceof RemoteConnectionInfo) {
			RemoteConnectionInfo info = (RemoteConnectionInfo) obj;
			isTypeEquals = this.getConnectionType() == info.getConnectionType();
		}
		
		return isTypeEquals;
	}
}
